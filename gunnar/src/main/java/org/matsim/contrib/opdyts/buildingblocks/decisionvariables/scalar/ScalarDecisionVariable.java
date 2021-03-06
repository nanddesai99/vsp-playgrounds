/*
 * Copyright 2018 Gunnar Flötteröd
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * contact: gunnar.flotterod@gmail.com
 *
 */
package org.matsim.contrib.opdyts.buildingblocks.decisionvariables.scalar;

import floetteroed.opdyts.DecisionVariable;

/**
 *
 * @author Gunnar Flötteröd
 *
 */
public interface ScalarDecisionVariable<U extends ScalarDecisionVariable<U>> extends DecisionVariable {

	/**
	 * If the decision variable is limited to a particular value range, the
	 * implementing class is responsible for adjusting the set value accordingly.
	 */
	public void setValue(double val);

	public double getValue();

	public U newDeepCopy();

}
