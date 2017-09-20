package playground.gregor.misanthrope.router;
/* *********************************************************************** *
 * project: org.matsim.*
 *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
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

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.router.AStarLandmarksFactory;
import org.matsim.core.router.DefaultRoutingModules;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelTime;

/**
 * Created by laemmel on 21/10/15.
 */
public class CTRoutingModule implements Provider<RoutingModule> {

	private Scenario scenario;
	private Map<String, TravelTime> travelTimes;
	private Map<String, TravelDisutilityFactory> travelDisutilities;

	@Inject
	CTRoutingModule(Scenario scenario, Map<String, TravelTime> travelTimes, Map<String, TravelDisutilityFactory> travelDisutilities) {
		this.scenario = scenario;
		this.travelTimes = travelTimes;
		this.travelDisutilities = travelDisutilities;
	}

	@Override
	public RoutingModule get() {
		return DefaultRoutingModules.createPureNetworkRouter("walkct", scenario.getPopulation()
				.getFactory(), scenario.getNetwork(), createRoutingAlgo());
	}

	private LeastCostPathCalculator createRoutingAlgo() {
		return new AStarLandmarksFactory().createPathCalculator(scenario.getNetwork(),
				travelDisutilities.get("car").createTravelDisutility(travelTimes.get("car")),
				travelTimes.get("car"));
	}
}
