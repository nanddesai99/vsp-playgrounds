/* *********************************************************************** *
 * project: org.matsim.*
 * SignalSystemsConsistencyChecker
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
package playground.cottbus;

/**
 * @author 	rschneid-btu
 * start-up for our scenarios
 */

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.events.EventsManagerImpl;
import org.matsim.core.scenario.ScenarioLoaderImpl;
import org.matsim.ptproject.qsim.QSim;
import org.matsim.vis.otfvis.OTFVisMobsimFeature;

public class CottbusController {
	
	public static void main (String[] args) {
		String scenario = "cottbus";
//		String scenario = "denver";
//		String scenario = "portland";
						
		String config = "./input/"+scenario+"/config.xml";
		// configuration that describes current scenario
		
		Controler controler = new Controler(config);
		controler.getConfig().addQSimConfigGroup(new QSimConfigGroup());
		controler.setOverwriteFiles(true);
		// effects output-folder
		controler.run();
		
		// @deprecated output via NetVis:
		//String[] visargs = {"output/denver/ITERS/it.0/Snapshot"};
		//NetVis.main(visargs);
		
		// visualization via OTFVis
		ScenarioLoaderImpl scl = new ScenarioLoaderImpl(config);
		Scenario sc = scl.loadScenario();
		sc.getConfig().addQSimConfigGroup(new QSimConfigGroup());
		EventsManagerImpl e = new EventsManagerImpl();
		QSim otfVisQSim = new QSim(sc, e);
		OTFVisMobsimFeature queueSimulationFeature = new OTFVisMobsimFeature(otfVisQSim);
		otfVisQSim.addFeature(queueSimulationFeature);
		queueSimulationFeature.setVisualizeTeleportedAgents(sc.getConfig().otfVis().isShowTeleportedAgents());
		
		QSim sim = otfVisQSim;
		sim.run();
		
		
	}
}