/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
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

package playground.agarwalamit.opdyts;

import java.util.Collection;
import java.util.Map;
import floetteroed.opdyts.DecisionVariable;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;

/**
 * Created by amit on 13/10/16.
 */


@SuppressWarnings("DefaultFileTemplate")
public class ModeChoiceDecisionVariable implements DecisionVariable {

    private final playground.kai.usecases.opdytsintegration.modechoice.ModeChoiceDecisionVariable delegate ;

    private final OpdytsScenario opdytsScenario;
    private final String subPopulation;
    private final PlanCalcScoreConfigGroup newScoreConfig;

    private final Collection<String> considerdModes ;

    public ModeChoiceDecisionVariable(final PlanCalcScoreConfigGroup newScoreConfig, final Scenario scenario,
                                      final OpdytsScenario opdytsScenario, final Collection<String> considerdModes, final String subPopulatioun){
        delegate = new playground.kai.usecases.opdytsintegration.modechoice.ModeChoiceDecisionVariable(newScoreConfig,scenario);
        this.newScoreConfig = newScoreConfig;
        this.opdytsScenario = opdytsScenario;
        this.subPopulation = subPopulatioun;
        this.considerdModes = considerdModes;
    }

    public ModeChoiceDecisionVariable(final PlanCalcScoreConfigGroup newScoreConfig, final Scenario scenario, final Collection<String> considerdModes,
                               final OpdytsScenario opdytsScenario){
        this (newScoreConfig, scenario, opdytsScenario, considerdModes, null);
    }

    @Override
    public void implementInSimulation() {
        delegate.implementInSimulation();
    }

    @Override
    public String toString() {
        final Map<String, PlanCalcScoreConfigGroup.ModeParams> allModes = this.newScoreConfig.getScoringParameters(this.subPopulation).getModes();
        switch (this.opdytsScenario){
            case EQUIL:
            case EQUIL_MIXEDTRAFFIC:
            case PATNA_1Pct:
            case PATNA_10Pct:
            StringBuilder str = new StringBuilder();
                for(String mode : considerdModes) {
                    PlanCalcScoreConfigGroup.ModeParams mp = allModes.get(mode);
                    if(mp.getMode().equals(TransportMode.other)) continue;
                    str.append(mp.getMode())
                       .append(": ")
                       .append(mp.getConstant())
                       .append(" + ")
                       .append(mp.getMarginalUtilityOfTraveling())
                       .append(" * ttime ")
                       .append(" + ")
                       .append(mp.getMarginalUtilityOfDistance())
                       .append(" * tdist ")
                       .append(" + ")
                       .append(mp.getMonetaryDistanceRate())
                       .append(" * ")
                       .append(this.newScoreConfig.getMarginalUtilityOfMoney())
                       .append(" * tdist;");
                }
                return str.toString();
            default:
                throw new RuntimeException("not implemented yet.");
            }
        }

    public PlanCalcScoreConfigGroup getScoreConfig() {
        return this.delegate.getScoreConfig();
    }
}
