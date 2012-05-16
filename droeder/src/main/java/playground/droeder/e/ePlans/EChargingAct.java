/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
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
package playground.droeder.e.ePlans;

import org.matsim.api.core.v01.Id;


/**
 * @author droeder
 *
 */
public class EChargingAct implements EVehiclePlanElement {

	private double start;
	private Id personId;
	private Id profileId;
	private double end;
	private Id poiId;

	public EChargingAct(double start, double end, Id profileId, Id personId, Id poiId){
		this.start = start;
		this.end = end;
		this.profileId = profileId;
		this.personId = personId;
		this.poiId = poiId;
	}
	
	public double getStart() {
		return this.start;
	}

	@Override
	public Id getPersonId() {
		return this.personId;
	}

	@Override
	public Id getProfileId() {
		return this.profileId;
	}

	public double getEnd() {
		return this.end;
	}

	/**
	 * @return
	 */
	public Id getPoiId() {
		return this.poiId;
	}

}
