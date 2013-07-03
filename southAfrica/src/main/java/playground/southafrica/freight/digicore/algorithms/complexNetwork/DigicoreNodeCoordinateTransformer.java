/* *********************************************************************** *
 * project: org.matsim.*
 * DigicoreNodeCoordinateTransformer.java
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

package playground.southafrica.freight.digicore.algorithms.complexNetwork;

import java.util.Map;

import org.apache.commons.collections15.Transformer;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;

public class DigicoreNodeCoordinateTransformer implements Transformer<Id, String> {
	private Map<Id, Coord> map;
	
	public DigicoreNodeCoordinateTransformer(Map<Id, Coord> coordinateMap) {
		this.map = coordinateMap;
	}

	@Override
	public String transform(Id id) {
		return String.format("[%.2f ; %.2f]", map.get(id).getX(), map.get(id).getY());
	}


}

