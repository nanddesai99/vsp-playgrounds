/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
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
package playground.wrashid.parkingSearch.ppSim.jdepSim;

import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.LegImpl;
import org.matsim.core.population.routes.LinkNetworkRouteImpl;

import playground.wrashid.parkingSearch.ppSim.jdepSim.searchStrategies.ParkingSearchStrategy;
import playground.wrashid.parkingSearch.ppSim.jdepSim.searchStrategies.manager.ParkingStrategyManager;
import playground.wrashid.parkingSearch.ppSim.jdepSim.zurich.ParkingManagerZH;

public class AgentWithParking extends AgentEventMessage{

	public static ParkingStrategyManager parkingStrategyManager;
	public static ParkingManagerZH parkingManager;
	
	public AgentWithParking(Person person) {
		this.setPerson(person);
		this.setPlanElementIndex(0);
		ActivityImpl ai = (ActivityImpl) person.getSelectedPlan().getPlanElements().get(getPlanElementIndex());
		setMessageArrivalTime(ai.getEndTime());
	}
	
	public void scheduleMessage(){
		messageQueue.schedule(this);
	}
	
	@Override
	public void processEvent() {
		if (getPerson().getSelectedPlan().getPlanElements().get(getPlanElementIndex()) instanceof ActivityImpl){
			Activity act=(Activity) getPerson().getSelectedPlan().getPlanElements().get(getPlanElementIndex());
			Leg nextLeg=(Leg) getPerson().getSelectedPlan().getPlanElements().get(getPlanElementIndex() + 1);
			
			if (act.getType().equalsIgnoreCase("parking") && nextLeg.getMode().equals(TransportMode.car)){
				
				// (don't do this for first parking)
				if (getPlanElementIndex()>getIndexOfFirstCarLegOfDay()){
					parkingStrategyManager.getParkingStrategyForCurrentLeg(getPerson(),duringAct_getPlanElementIndexOfPreviousCarLeg()).handleParkingDepartureActivity(this);
				}
			
				AgentWithParking.parkingManager.unParkAgentVehicle(getPerson().getId());
			}
			
			
			handleActivityEndEvent();
		} else {
			Leg leg=(Leg) getPerson().getSelectedPlan().getPlanElements().get(getPlanElementIndex());
			if (leg.getMode().equalsIgnoreCase(TransportMode.car)){
				parkingStrategyManager.getParkingStrategyForCurrentLeg(getPerson(),planElementIndex).handleAgentLeg(this);
			} else {
				handleLeg();
			}
		}
	}
	
	private int getIndexOfFirstCarLegOfDay() {
		Plan selectedPlan = getPerson().getSelectedPlan();
		List<PlanElement> planElements = selectedPlan.getPlanElements();

		int i = 0;
		while (i < planElements.size()) {
			if (planElements.get(i) instanceof LegImpl) {
				Leg leg = (Leg) planElements.get(i);
				if (leg.getMode().equalsIgnoreCase(TransportMode.car)) {
					return i;
				}
			}
			i++;
		}

		return -1;
	}

	public void processLegInDefaultWay(){
		handleLeg();
	}

	// avoid temporary problem with car leave and next planned parking on same link
	// TODO: resolve in future implementation
	public boolean isInvalidLinkForParking() {
		Leg leg = (LegImpl) getPerson().getSelectedPlan().getPlanElements().get(getPlanElementIndex());
		LinkNetworkRouteImpl route= (LinkNetworkRouteImpl)leg.getRoute();
		
		boolean isInvalidLink=false;
		int nextCarLegIndex = duringCarLeg_getPlanElementIndexOfNextCarLeg();
		if (nextCarLegIndex!=-1){
			ActivityImpl nextActAfterNextCarLeg = (ActivityImpl) getPerson().getSelectedPlan().getPlanElements().get(nextCarLegIndex+3);
			isInvalidLink=route.getEndLinkId().toString().equalsIgnoreCase(nextActAfterNextCarLeg.getLinkId().toString());
		}
		return isInvalidLink;
	}

	public ActivityImpl getCurrentActivity() {
		return (ActivityImpl) getPerson().getSelectedPlan().getPlanElements().get(getPlanElementIndex());
	}
	
	
	



}

