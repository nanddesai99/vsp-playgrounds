/* *********************************************************************** *
 * project: org.matsim.*
 * MulitAnalyzer.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
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
package playground.benjamin.scenarios.munich.analysis.kuhmo;

import java.util.Map;
import java.util.SortedMap;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsReaderXMLv1;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;

import playground.benjamin.emissions.events.EmissionEventsReader;
import playground.benjamin.emissions.types.ColdPollutant;
import playground.benjamin.emissions.types.WarmPollutant;
import playground.benjamin.scenarios.munich.analysis.EmissionUtils;
import playground.benjamin.scenarios.munich.analysis.filter.UserGroup;
import playground.benjamin.scenarios.munich.analysis.mobilTUM.EmissionsPerPersonColdEventHandler;
import playground.benjamin.scenarios.munich.analysis.mobilTUM.EmissionsPerPersonWarmEventHandler;
import playground.benjamin.scenarios.zurich.analysis.MoneyEventHandler;

/**
 * @author benjamin
 *
 */
public class MultiAnalyzer {
	private static final Logger logger = Logger.getLogger(MultiAnalyzer.class);


	private static String [] cases = {
		"baseCase_ctd" ,
		"policyCase_zone30" ,
		"policyCase_pricing" ,
//		"policyCase_pricing_x5" ,
//		"policyCase_pricing_x10" ,
	};
	
	private static String runDirectoryStub = "../../runs-svn/detEval/kuhmo/output/output_";
//	private static String initialIterationNo = "1000";
	private static String finalIterationNo = "1500";
	private static String netFile;
	private static String configFile;
	private static String plansFile;
	private static String eventsFile;
	private static String emissionEventsFile;

	private final MultiAnalyzerWriter writer;

	MultiAnalyzer(){
		this.writer = new MultiAnalyzerWriter(runDirectoryStub + cases[0] + "/");
	}

	private void run() {
		
		for(String caseName : cases){
			
			String runDirectory = runDirectoryStub + caseName + "/";
			netFile = runDirectory + "output_network.xml.gz";
			configFile = runDirectory + "output_config.xml.gz";
			plansFile = runDirectory + "ITERS/it." + finalIterationNo + "/" + finalIterationNo + ".plans.xml.gz";
			eventsFile = runDirectory + "ITERS/it." + finalIterationNo + "/" + finalIterationNo + ".events.xml.gz";
			emissionEventsFile = runDirectory + "ITERS/it." + finalIterationNo + "/" + finalIterationNo + ".emission.events.xml.gz";
			
			calculateUserWelfareAndTollRevenueStatisticsByUserGroup(netFile, configFile, plansFile, eventsFile, caseName);
			calculateDistanceTimeStatisticsByUserGroup(netFile, eventsFile, caseName);
			calculateEmissionStatisticsByUserGroup(emissionEventsFile, caseName);
		}
	}

	private void calculateUserWelfareAndTollRevenueStatisticsByUserGroup(String netFile, String configFile, String plansFile, String eventsFile, String runName) {

		Scenario scenario = loadScenario(netFile, plansFile);
		Population pop = scenario.getPopulation();

		EventsManager eventsManager = EventsUtils.createEventsManager();
		EventsReaderXMLv1 eventsReader = new EventsReaderXMLv1(eventsManager);
		MoneyEventHandler moneyEventHandler = new MoneyEventHandler();
		eventsManager.addHandler(moneyEventHandler);
		eventsReader.parse(eventsFile);

		Map<Id, Double> personId2Toll = moneyEventHandler.getPersonId2TollMap();

		writer.setRunName(runName);
		writer.writeWelfareTollInformation(configFile, pop, personId2Toll);
	}

	private void calculateEmissionStatisticsByUserGroup(String emissionEventsFile, String runName) {
		EmissionUtils summarizer = new EmissionUtils();

		EventsManager eventsManager = EventsUtils.createEventsManager();
		EmissionEventsReader emissionReader = new EmissionEventsReader(eventsManager);
		
		EmissionsPerPersonWarmEventHandler warmHandler = new EmissionsPerPersonWarmEventHandler();
		EmissionsPerPersonColdEventHandler coldHandler = new EmissionsPerPersonColdEventHandler();
		
		eventsManager.addHandler(warmHandler);
		eventsManager.addHandler(coldHandler);
		emissionReader.parse(emissionEventsFile);

		Map<Id, Map<WarmPollutant, Double>> person2warmEmissions = warmHandler.getWarmEmissionsPerPerson();
		Map<Id, Map<ColdPollutant, Double>> person2coldEmissions = coldHandler.getColdEmissionsPerPerson();
		Map<Id, SortedMap<String, Double>> person2totalEmissions = summarizer.sumUpEmissionsPerId(person2warmEmissions, person2coldEmissions);
		SortedMap<UserGroup, SortedMap<String, Double>> group2totalEmissions = summarizer.getEmissionsPerGroup(person2totalEmissions);

		writer.setRunName(runName);
		writer.writeEmissionInformation(group2totalEmissions);
	}

	private void calculateDistanceTimeStatisticsByUserGroup(String netFile, String eventsFile, String runName) {
		Scenario sc = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(sc).readFile(netFile);

		EventsManager eventsManager = EventsUtils.createEventsManager();
		EventsReaderXMLv1 eventsReader = new EventsReaderXMLv1(eventsManager);
		
		CarDistanceEventHandler carDistanceEventHandler = new CarDistanceEventHandler(sc.getNetwork());
		TravelTimePerModeEventHandler ttHandler = new TravelTimePerModeEventHandler();

		eventsManager.addHandler(carDistanceEventHandler);
		eventsManager.addHandler(ttHandler);
		eventsReader.parse(eventsFile);
		
		Map<Id, Double> personId2carDistance = carDistanceEventHandler.getPersonId2CarDistance();
		Map<UserGroup, Double> userGroup2carTrips = carDistanceEventHandler.getUserGroup2carTrips();
		Map<String, Map<Id, Double>> mode2personId2TravelTime = ttHandler.getMode2personId2TravelTime();
		Map<UserGroup, Map<String, Double>> userGroup2mode2noOfTrips = ttHandler.getUserGroup2mode2noOfTrips();
		
		logger.warn(runName + ": number of car users in distance map (users with departure events): " + personId2carDistance.size());
//		int depArrOnSameLinkCnt = carDistanceEventHandler.getDepArrOnSameLinkCnt().size();
//		logger.warn("number of car users with two activities followed one by another on the same link: +" + depArrOnSameLinkCnt);
//		int personIsDrivingADistance = 0;
//		for(Id personId : carDistanceEventHandler.getDepArrOnSameLinkCnt().keySet()){
//			if(personId2carDistance.get(personId) == null){
//				// do nothing
//			} else {
//				personIsDrivingADistance ++;
//			}
//		}
//		logger.warn(runName + ": number of car users with two activities followed one by another on the same link BUT driving to other acts: -" + personIsDrivingADistance);
		logger.warn(runName + ": number of car users in traveltime map (users with departure and arrival events): " + mode2personId2TravelTime.get(TransportMode.car).size());
		
		
		writer.setRunName(runName);
		writer.writeCarDistanceInformation(personId2carDistance, userGroup2carTrips);
		writer.writeAvgTTInformation(mode2personId2TravelTime, userGroup2mode2noOfTrips);
	}

	private Scenario loadScenario(String netFile, String plansFile) {
		Config config = ConfigUtils.createConfig();
		config.network().setInputFile(netFile);
		config.plans().setInputFile(plansFile);
		Scenario scenario = ScenarioUtils.loadScenario(config);
		return scenario;
	}

	public static void main(String[] args) {
		MultiAnalyzer ma = new MultiAnalyzer();
		ma.run();
	}
}