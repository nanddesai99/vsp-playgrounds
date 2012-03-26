/* *********************************************************************** *
 * project: org.matsim.*
 * PTransitAgentFactory.java
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

package playground.andreas.P2.hook;

import org.matsim.api.core.v01.population.Person;
import org.matsim.pt.qsim.MobsimDriverPassengerAgent;
import org.matsim.ptproject.qsim.agents.AgentFactory;
import org.matsim.ptproject.qsim.interfaces.Netsim;


/**
 * @author aneumann
 */
public class PTransitAgentFactory implements AgentFactory {

	private final Netsim simulation;


	public PTransitAgentFactory(final Netsim simulation) {
		this.simulation = simulation;
	}

	@Override
	public MobsimDriverPassengerAgent createMobsimAgentFromPerson(final Person p) {
		MobsimDriverPassengerAgent agent = PTransitAgent.createTransitAgent(p, this.simulation);
		return agent;
	}

}
