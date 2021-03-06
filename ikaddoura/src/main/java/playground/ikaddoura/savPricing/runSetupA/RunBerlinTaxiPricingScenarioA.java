/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2017 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package playground.ikaddoura.savPricing.runSetupA;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.contrib.decongestion.DecongestionConfigGroup;
import org.matsim.contrib.noise.NoiseConfigGroup;
import org.matsim.contrib.noise.utils.MergeNoiseCSVFile;
import org.matsim.contrib.noise.utils.ProcessNoiseImmissions;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import playground.ikaddoura.analysis.IKAnalysis;
import playground.ikaddoura.analysis.modalSplitUserType.AgentAnalysisFilter;
import playground.ikaddoura.analysis.modalSplitUserType.ModalSplitUserTypeControlerListener;
import playground.ikaddoura.savPricing.RunBerlinTaxiScenarioA;
import playground.ikaddoura.savPricing.SAVPricingConfigGroup;
import playground.ikaddoura.savPricing.SAVPricingModule;

/**
* @author ikaddoura
*/

public class RunBerlinTaxiPricingScenarioA {
	private static final Logger log = Logger.getLogger(RunBerlinTaxiPricingScenarioA.class);

	private static String configFileName;
	private static String overridingConfigFileName;
	private static String serviceAreaShapeFile;
	private static double dailyRewardTaxiInsteadOfPrivateCar;
	private static String runId;
	private static String outputDirectory;
	private static String visualizationScriptDirectory;
	private static Integer scaleFactor;

	public static void main(String[] args) {

		if (args.length > 0) {		
			configFileName = args[0];
			overridingConfigFileName = args[1];
			serviceAreaShapeFile = args[2];
			dailyRewardTaxiInsteadOfPrivateCar = Double.parseDouble(args[3]);
			runId = args[4];
			outputDirectory = args[5];
			visualizationScriptDirectory = args[6];
			scaleFactor = Integer.parseInt(args[7]);
			
		} else {		
			String baseDirectory = "/Users/ihab/Documents/workspace/matsim-berlin/";	
			configFileName = baseDirectory + "scenarios/berlin-v5.2-1pct/input/berlin-taxiA-v5.2-1pct.config.xml";
			overridingConfigFileName = null;
			serviceAreaShapeFile = "http://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-sav-v5.2-10pct/input/shp-inner-city-area/inner-city-area.shp";
			dailyRewardTaxiInsteadOfPrivateCar = 0.;
			runId = "taxiA-test-1";
			outputDirectory = "/Users/ihab/Documents/workspace/runs-svn/savPricing/output/output-local-run_" + runId + "/";
			visualizationScriptDirectory = "./visualization-scripts/";
			scaleFactor = 100;
		}
			
		log.info("run Id: " + runId);
		log.info("output directory: " + outputDirectory);
		
		RunBerlinTaxiPricingScenarioA drtPricing = new RunBerlinTaxiPricingScenarioA();
		drtPricing.run();
	}
	
	private void run() {
		RunBerlinTaxiScenarioA berlin = new RunBerlinTaxiScenarioA(configFileName, overridingConfigFileName, serviceAreaShapeFile, dailyRewardTaxiInsteadOfPrivateCar);
		
		ConfigGroup[] modulesToAdd = {new SAVPricingConfigGroup(), new DecongestionConfigGroup(), new NoiseConfigGroup()};
		Config config = berlin.prepareConfig(modulesToAdd);
		config.controler().setRunId(runId);
		config.controler().setOutputDirectory(outputDirectory);
						
		Scenario scenario = berlin.prepareScenario();
		
		Controler controler = berlin.prepareControler();		

		// sav pricing
		controler.addOverridingModule(new SAVPricingModule(scenario, RunBerlinTaxiScenarioA.modeToReplaceCarTripsInBrandenburg));
		
		// modal split analysis
		controler.addOverridingModule(new org.matsim.core.controler.AbstractModule() {	
			@Override
			public void install() {
				this.addControlerListenerBinding().to(ModalSplitUserTypeControlerListener.class);
			}
		});
		
		controler.run();
		
		log.info("Done.");
		
		log.info("Running offline analysis...");
		
		final String scenarioCRS = TransformationFactory.DHDN_GK4;	
		final String shapeFileZones = null;
		final String zonesCRS = null;
		final String homeActivity = "home";
		final int scalingFactor = scaleFactor;
		
		List<AgentAnalysisFilter> filters = new ArrayList<>();

		AgentAnalysisFilter filter1 = new AgentAnalysisFilter(scenario);
		filter1.setPersonAttribute("berlin");
		filter1.setPersonAttributeName("home-activity-zone");
		filter1.preProcess(controler.getScenario());
		filters.add(filter1);
		
		AgentAnalysisFilter filter2 = new AgentAnalysisFilter(scenario);
		filter2.preProcess(controler.getScenario());
		filters.add(filter2);
		
		AgentAnalysisFilter filter3 = new AgentAnalysisFilter(scenario);
		filter3.setPersonAttribute("brandenburg");
		filter3.setPersonAttributeName("home-activity-zone");
		filter3.preProcess(controler.getScenario());
		filters.add(filter3);
		
		List<String> modes = new ArrayList<>();
		modes.add(TransportMode.car);
		modes.add(TransportMode.taxi);
		modes.add(TransportMode.pt);
		modes.add(TransportMode.ride);
		modes.add("bicycle");
		modes.add(TransportMode.walk);
		modes.add(TransportMode.transit_walk);

		final String[] helpLegModes = {TransportMode.transit_walk, TransportMode.access_walk, TransportMode.egress_walk};
		final String stageActivitySubString = "interaction";
		final String zoneId = "NO";
		
		IKAnalysis analysis = new IKAnalysis(
				controler.getScenario(),
				null,
				visualizationScriptDirectory,
				scenarioCRS,
				shapeFileZones,
				zonesCRS,
				homeActivity,
				scalingFactor,
				filters,
				null,
				modes,
				null,
				zoneId, helpLegModes, stageActivitySubString);
		analysis.run();
		
		// noise post-analysis
		
		SAVPricingConfigGroup optAVParams = ConfigUtils.addOrGetModule(config, SAVPricingConfigGroup.class);
		if (optAVParams.isAccountForNoise()) {
			String immissionsDir = controler.getConfig().controler().getOutputDirectory() + "/ITERS/it." + controler.getConfig().controler().getLastIteration() + "/immissions/";
			String receiverPointsFile = controler.getConfig().controler().getOutputDirectory() + "/receiverPoints/receiverPoints.csv";
				
			NoiseConfigGroup noiseParams = ConfigUtils.addOrGetModule(controler.getConfig(), NoiseConfigGroup.class);

			ProcessNoiseImmissions processNoiseImmissions = new ProcessNoiseImmissions(immissionsDir, receiverPointsFile, noiseParams.getReceiverPointGap());
			processNoiseImmissions.run();
				
			final String[] labels = { "immission", "consideredAgentUnits" , "damages_receiverPoint" };
			final String[] workingDirectories = { controler.getConfig().controler().getOutputDirectory() + "/ITERS/it." + controler.getConfig().controler().getLastIteration() + "/immissions/" , controler.getConfig().controler().getOutputDirectory() + "/ITERS/it." + controler.getConfig().controler().getLastIteration() + "/consideredAgentUnits/" , controler.getConfig().controler().getOutputDirectory() + "/ITERS/it." + controler.getConfig().controler().getLastIteration()  + "/damages_receiverPoint/" };
		
			MergeNoiseCSVFile merger = new MergeNoiseCSVFile() ;
			merger.setReceiverPointsFile(receiverPointsFile);
			merger.setOutputDirectory(controler.getConfig().controler().getOutputDirectory() + "/ITERS/it." + controler.getConfig().controler().getLastIteration() + "/");
			merger.setTimeBinSize(noiseParams.getTimeBinSizeNoiseComputation());
			merger.setWorkingDirectory(workingDirectories);
			merger.setLabel(labels);
			merger.run();
		}
		
		log.info("Done.");
	}

}

