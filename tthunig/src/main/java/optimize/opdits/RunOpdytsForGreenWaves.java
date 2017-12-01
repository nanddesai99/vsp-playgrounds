/*
 *  *********************************************************************** *
 *  * project: org.matsim.*
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  * copyright       : (C) 2014 by the members listed in the COPYING, *
 *  *                   LICENSE and WARRANTY file.                            *
 *  * email           : info at matsim dot org                                *
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  *   This program is free software; you can redistribute it and/or modify  *
 *  *   it under the terms of the GNU General Public License as published by  *
 *  *   the Free Software Foundation; either version 2 of the License, or     *
 *  *   (at your option) any later version.                                   *
 *  *   See also COPYING, LICENSE and WARRANTY file                           *
 *  *                                                                         *
 *  * ***********************************************************************
 */
package optimize.opdits;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import analysis.TtTotalTravelTime;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.contrib.opdyts.MATSimSimulator2;
import org.matsim.contrib.opdyts.MATSimStateFactoryImpl;
import org.matsim.contrib.opdyts.utils.MATSimOpdytsControler;
import org.matsim.contrib.opdyts.utils.OpdytsConfigGroup;
import org.matsim.contrib.signals.SignalSystemsConfigGroup;
import org.matsim.contrib.signals.controler.SignalsModule;
import org.matsim.contrib.signals.data.SignalsData;
import org.matsim.contrib.signals.data.SignalsDataLoader;
import org.matsim.contrib.signals.data.signalgroups.v20.SignalControlData;
import org.matsim.contrib.signals.data.signalgroups.v20.SignalControlDataFactory;
import org.matsim.contrib.signals.data.signalgroups.v20.SignalData;
import org.matsim.contrib.signals.data.signalgroups.v20.SignalGroupData;
import org.matsim.contrib.signals.data.signalgroups.v20.SignalGroupsData;
import org.matsim.contrib.signals.data.signalgroups.v20.SignalPlanData;
import org.matsim.contrib.signals.data.signalgroups.v20.SignalSystemControllerData;
import org.matsim.contrib.signals.data.signalsystems.v20.SignalSystemData;
import org.matsim.contrib.signals.data.signalsystems.v20.SignalSystemsData;
import org.matsim.contrib.signals.data.signalsystems.v20.SignalSystemsDataFactory;
import org.matsim.contrib.signals.model.DefaultPlanbasedSignalSystemController;
import org.matsim.contrib.signals.model.Signal;
import org.matsim.contrib.signals.model.SignalGroup;
import org.matsim.contrib.signals.model.SignalPlan;
import org.matsim.contrib.signals.model.SignalSystem;
import org.matsim.contrib.signals.utils.SignalUtils;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.listener.ShutdownListener;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule.DefaultStrategy;
import org.matsim.core.scenario.ScenarioUtils;
import playground.agarwalamit.analysis.tripTime.ModalTravelTimeControlerListener;
import playground.agarwalamit.analysis.tripTime.ModalTripTravelTimeHandler;
import playground.agarwalamit.opdyts.plots.OpdytsConvergenceChart;

/**
 * @author tthunig
 */
public class RunOpdytsForGreenWaves {

	private static String OUTPUT_DIR = "../../runs-svn/opdytsForSignals/greenWaveSingleStreamPlanbasedSignals_stepSize10_selfTuning4/" ;
	
	public static void main(String[] args) {
		 
		Config config = createConfig();
		Scenario scenario = ScenarioUtils.loadScenario(config);
		createNetwork(scenario);
		createSignals(scenario);
		createDemand(scenario);
		
        OpdytsConfigGroup opdytsConfigGroup = ConfigUtils.addOrGetModule(scenario.getConfig(), OpdytsConfigGroup.class);
        opdytsConfigGroup.setNumberOfIterationsForAveraging(5); //2
        opdytsConfigGroup.setNumberOfIterationsForConvergence(20); //5

        opdytsConfigGroup.setMaxIteration(10);
        opdytsConfigGroup.setOutputDirectory(scenario.getConfig().controler().getOutputDirectory());
//        opdytsConfigGroup.setVariationSizeOfRandomizeDecisionVariable(0.5);
        opdytsConfigGroup.setVariationSizeOfRandomizeDecisionVariable(10);
        opdytsConfigGroup.setUseAllWarmUpIterations(false);
        opdytsConfigGroup.setWarmUpIterations(5); // 1 this should be tested (parametrized).
        opdytsConfigGroup.setPopulationSize(1);
        opdytsConfigGroup.setSelfTuningWeight(4);

        MATSimOpdytsControler<OffsetDecisionVariable> runner = new MATSimOpdytsControler<>(scenario);

        MATSimSimulator2<OffsetDecisionVariable> simulator = new MATSimSimulator2<>(new MATSimStateFactoryImpl<>(), scenario);
        simulator.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                // TODO why does it work to inject this in TravelTimeObjectiveFunction, although TravelTimeObjectiveFunction is not created by guice??
                bind(TtTotalTravelTime.class).asEagerSingleton();
                addEventHandlerBinding().to(TtTotalTravelTime.class);

                bind(ModalTripTravelTimeHandler.class);
                addControlerListenerBinding().to(ModalTravelTimeControlerListener.class);

                addControlerListenerBinding().toInstance(new ShutdownListener() { //plot only after one opdyts transition.
					@Override
					public void notifyShutdown(ShutdownEvent event) {
						// post-process analysis
						String opdytsConvergenceFile = OUTPUT_DIR + "/opdyts.con";
						if (new File(opdytsConvergenceFile).exists()) {
							OpdytsConvergenceChart opdytsConvergencePlotter = new OpdytsConvergenceChart();
							opdytsConvergencePlotter.readFile(OUTPUT_DIR + "/opdyts.con");
							opdytsConvergencePlotter.plotData(OUTPUT_DIR + "/convergence.png");
						}
					}
				});
            }
        });
        simulator.addOverridingModule(new SignalsModule());
        runner.addNetworkModeOccupancyAnalyzr(simulator);

        runner.run(simulator,
                new OffsetRandomizer(scenario),
                new OffsetDecisionVariable(((SignalsData) scenario.getScenarioElement(SignalsData.ELEMENT_NAME)).getSignalControlData(), scenario),
                new TravelTimeObjectiveFunction());
        
	}

	private static void createSignals(Scenario scenario) {
		// add missing scenario elements
        scenario.addScenarioElement(SignalsData.ELEMENT_NAME, new SignalsDataLoader(scenario.getConfig()).loadSignalsData());
        
        SignalsData signalsData = (SignalsData) scenario.getScenarioElement(SignalsData.ELEMENT_NAME);
        SignalSystemsData signalSystems = signalsData.getSignalSystemsData();
        SignalSystemsDataFactory sysFac = signalSystems.getFactory();
        SignalGroupsData signalGroups = signalsData.getSignalGroupsData();
        SignalControlData signalControl = signalsData.getSignalControlData();
        SignalControlDataFactory conFac = signalControl.getFactory();

        String[] signalizedIntersections = {"2", "3", "4"};
        
        for (int i = 0; i < signalizedIntersections.length; i++) {
        		// create the signal system for the intersection
        		Id<SignalSystem> signalSystemId = Id.create("SignalSystem" + signalizedIntersections[i], SignalSystem.class);
            SignalSystemData signalSystem = sysFac.createSignalSystemData(signalSystemId);
            signalSystems.addSignalSystemData(signalSystem);
            
            // add the signal (there is only one inLink)
            for (Link inLink : scenario.getNetwork().getNodes().get(Id.createNodeId(i+2)).getInLinks().values()) {
                SignalData signal = sysFac.createSignalData(Id.create("Signal" + inLink.getId(), Signal.class));
                signal.setLinkId(inLink.getId());
                signalSystem.addSignalData(signal);
                
                // create a group for this single signal
                Id<SignalGroup> signalGroupId = Id.create("SignalGroup" + signal.getId(), SignalGroup.class);
                SignalGroupData signalGroup = signalGroups.getFactory().createSignalGroupData(signalSystemId, signalGroupId);
                signalGroup.addSignalId(signal.getId());
                signalGroups.addSignalGroupData(signalGroup);
            }
            
            // create signal control
            SignalSystemControllerData signalSystemControl = conFac.createSignalSystemControllerData(signalSystemId);
            signalSystemControl.setControllerIdentifier(DefaultPlanbasedSignalSystemController.IDENTIFIER);
            signalControl.addSignalSystemControllerData(signalSystemControl);

            // create a plan for the signal system (with defined cycle time and offset 0)
            SignalPlanData signalPlan = SignalUtils.createSignalPlan(conFac, 60, 0, Id.create("SignalPlan", SignalPlan.class));
            signalSystemControl.addSignalPlanData(signalPlan);
            for (Id<SignalGroup> signalGroupId : signalGroups.getSignalGroupDataBySystemId(signalSystemId).keySet()) {
            		// there is only one element in this set
            		signalPlan.addSignalGroupSettings(SignalUtils.createSetting4SignalGroup(conFac, signalGroupId, 0, 30));
            }
        }
        
	}

	private static void createDemand(Scenario scenario) {
		Population pop = scenario.getPopulation();
		PopulationFactory popFac = pop.getFactory();
		
		int startTime = 0;
		int endTime = 3600;
		int interval = 1;
		
		Id<Link> fromLinkId = Id.createLinkId("0_1");
		Id<Link> toLinkId = Id.createLinkId("4_5");
		
		for (int i = startTime; i < endTime; i += interval) {
			// create a person (the i-th person)
			Person person = popFac.createPerson(Id.createPersonId(fromLinkId + "-" + toLinkId + "-" + i));
			pop.addPerson(person);

			// create a plan for the person that contains all this information
			Plan plan = popFac.createPlan();
			person.addPlan(plan);

			// create a start activity at the from link
			Activity startAct = popFac.createActivityFromLinkId("dummy", fromLinkId);
			startAct.setEndTime(i);
			plan.addActivity(startAct);
			// create a dummy leg
			Leg leg = popFac.createLeg(TransportMode.car);

			// create routes for the agents
			List<Id<Link>> path = new ArrayList<>();
			path.add(Id.createLinkId("1_2"));
			path.add(Id.createLinkId("2_3"));
			path.add(Id.createLinkId("3_4"));
			leg.setRoute(RouteUtils.createLinkNetworkRouteImpl(fromLinkId, path, toLinkId));
			plan.addLeg(leg);

			// create a drain activity at the to link
			Activity drainAct = popFac.createActivityFromLinkId("dummy", toLinkId);
			plan.addActivity(drainAct);
		}
	}

	private static void createNetwork(Scenario scenario) {
		Network net = scenario.getNetwork();
		NetworkFactory netFac = net.getFactory();
		
		net.addNode(netFac.createNode(Id.createNodeId(0), new Coord(0, 0)));
		net.addNode(netFac.createNode(Id.createNodeId(1), new Coord(1000, 0)));
		net.addNode(netFac.createNode(Id.createNodeId(2), new Coord(2000, 0)));
		net.addNode(netFac.createNode(Id.createNodeId(3), new Coord(3000, 0)));
		net.addNode(netFac.createNode(Id.createNodeId(4), new Coord(4000, 0)));
		net.addNode(netFac.createNode(Id.createNodeId(5), new Coord(5000, 0)));
		
		String[] links = { "0_1", "1_2", "2_3", "3_4", "4_5"};

		for (String linkId : links) {
			String fromNodeId = linkId.split("_")[0];
			String toNodeId = linkId.split("_")[1];
			Link link = netFac.createLink(Id.createLinkId(linkId), net.getNodes().get(Id.createNodeId(fromNodeId)), net.getNodes().get(Id.createNodeId(toNodeId)));
			link.setCapacity(3600);
			link.setLength(1000);
			link.setFreespeed(10);
			net.addLink(link);
		}
	}

	private static Config createConfig() {
		Config config = ConfigUtils.createConfig();
		config.controler().setOutputDirectory(OUTPUT_DIR);
		config.controler().setLastIteration(0);
		
		SignalSystemsConfigGroup signalConfigGroup = ConfigUtils.addOrGetModule(config, SignalSystemsConfigGroup.GROUPNAME, SignalSystemsConfigGroup.class);
		signalConfigGroup.setUseSignalSystems(true);
		
		// define strategies
//		{
//			StrategySettings strat = new StrategySettings();
//			strat.setStrategyName(DefaultSelector.KeepLastSelected);
//			strat.setWeight(1);
//			strat.setDisableAfter(config.controler().getLastIteration());
//			config.strategy().addStrategySettings(strat);
//		}
		{
			StrategySettings strat = new StrategySettings();
			strat.setStrategyName(DefaultStrategy.ReRoute.toString());
			strat.setWeight(1);
			strat.setDisableAfter(config.controler().getLastIteration());
			config.strategy().addStrategySettings(strat);
		}
		
		config.travelTimeCalculator().setTraveltimeBinSize(10);
		
		config.qsim().setStuckTime(360*10);
		config.qsim().setRemoveStuckVehicles(false);
		
		config.qsim().setUsingFastCapacityUpdate(false);

		config.qsim().setStartTime(0);
		config.qsim().setEndTime(4 * 3600);

		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);

		config.vspExperimental().setWritingOutputEvents(true);
		config.planCalcScore().setWriteExperiencedPlans(true);
		config.controler().setCreateGraphs(true);

		config.controler().setWriteEventsInterval(1);
		config.controler().setWritePlansInterval(config.controler().getLastIteration());

		// define activity types
		{
			ActivityParams dummyAct = new ActivityParams("dummy");
			dummyAct.setTypicalDuration(12 * 3600);
			config.planCalcScore().addActivityParams(dummyAct);
		}
		
		return config;
	}

}
