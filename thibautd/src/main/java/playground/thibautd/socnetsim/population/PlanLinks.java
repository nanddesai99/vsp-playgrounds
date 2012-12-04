/* *********************************************************************** *
 * project: org.matsim.*
 * PlanLinks.java
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
package playground.thibautd.socnetsim.population;

import java.lang.ref.WeakReference;

import java.util.Map;
import java.util.WeakHashMap;

import org.matsim.api.core.v01.population.Plan;


/**
 * Stores links between individual plans,
 * as defined by joint plans.
 * <br>
 * It uses internally a WeakHashMap, so that if an indivudal plan is not "strongly"
 * referenced anywhere, the mapping will be automatically forgotten at the next
 * garbage collection.
 *
 * @author thibautd
 */
public class PlanLinks {
	// this assumes equality for Plans is actually identity
	private final Map<Plan, WeakReference<JointPlan>> jointPlans =
		new WeakHashMap<Plan, WeakReference<JointPlan>>();
	
	PlanLinks() {}

	public JointPlan getJointPlan(final Plan indivPlan) {
		final WeakReference<JointPlan> value = jointPlans.get( indivPlan );
		return value == null ? null : value.get();
	}

	public void removeJointPlan(final JointPlan jointPlan) {
		for (Plan indivPlan : jointPlan.getIndividualPlans().values()) {
			JointPlan removed = jointPlans.remove( indivPlan ).get();
			if (removed != jointPlan) throw new RuntimeException( removed+" differs from "+indivPlan );
		}
	}

	public void addJointPlan(final JointPlan jointPlan) {
		for (Plan indivPlan : jointPlan.getIndividualPlans().values()) {
			WeakReference<JointPlan> removed = jointPlans.put(
					indivPlan,
					new WeakReference<JointPlan>(jointPlan) );
			if (removed != null && removed.get() != jointPlan) {
				throw new PlanLinkException( removed+" was associated to "+indivPlan+
						" while trying to associate "+jointPlan );
			}
		}
	}

	public static class PlanLinkException extends RuntimeException {
		private PlanLinkException( final String msg ) {
			super( msg );
		}
	}
}

