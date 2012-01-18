/* *********************************************************************** *
 * project: org.matsim.*
 * JointPlanOptimizerOTFFitnessFunction.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2011 by the members listed in the COPYING,        *
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
package playground.thibautd.jointtrips.replanning.modules.jointplanoptimizer.fitness;

import org.jgap.IChromosome;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.router.PlansCalcRoute;
import org.matsim.core.scoring.ScoringFunctionFactory;

import playground.thibautd.jointtrips.config.JointReplanningConfigGroup;
import playground.thibautd.jointtrips.population.JointPlan;
import playground.thibautd.jointtrips.replanning.modules.jointplanoptimizer.JointPlanOptimizerDecoder;
import playground.thibautd.jointtrips.replanning.modules.jointplanoptimizer.costestimators.JointPlanOptimizerLegTravelTimeEstimatorFactory;
import playground.thibautd.jointtrips.replanning.modules.jointplanoptimizer.pipeddecoder.DurationOnTheFlyScorer;
import playground.thibautd.jointtrips.replanning.modules.jointplanoptimizer.pipeddecoder.FinalScorer;
import playground.thibautd.jointtrips.replanning.modules.jointplanoptimizer.pipeddecoder.JointPlanOptimizerDecoderFactory;
import playground.thibautd.jointtrips.replanning.modules.jointplanoptimizer.pipeddecoder.JointPlanOptimizerPartialDecoderFactory;

/**
 * Implementation of a scoring function that scores durations "on the fly" rather
 * than on a decoded JointPlan instance.
 *
 * @author thibautd
 */
public class JointPlanOptimizerOTFFitnessFunction extends AbstractJointPlanOptimizerFitnessFunction {

	private static final long serialVersionUID = 1L;

	private final JointPlanOptimizerDecoder decoder;
	private final JointPlanOptimizerDecoder fullDecoder;
	//private final DurationOnTheFlyScorer scorer;
	private final FinalScorer scorer;

	public JointPlanOptimizerOTFFitnessFunction(
			final JointPlan plan,
			final JointReplanningConfigGroup configGroup,
			final JointPlanOptimizerLegTravelTimeEstimatorFactory legTravelTimeEstimatorFactory,
			final PlansCalcRoute routingAlgorithm,
			final Network network,
			final int numJointEpisodes,
			final int numEpisodes,
			final int nMembers,
			final ScoringFunctionFactory scoringFunctionFactory) {
		this(plan,
			configGroup,
			legTravelTimeEstimatorFactory,
			routingAlgorithm,
			network,
			numJointEpisodes,
			numEpisodes,
			nMembers,
			scoringFunctionFactory,
			(new JointPlanOptimizerPartialDecoderFactory(
				plan,
				configGroup,
				numJointEpisodes,
				numEpisodes)).createDecoder(),
			(new JointPlanOptimizerDecoderFactory(
				plan,
				configGroup,
				legTravelTimeEstimatorFactory,
				routingAlgorithm,
				network,
				numJointEpisodes,
				numEpisodes,
				nMembers)).createDecoder());
	}

	public JointPlanOptimizerOTFFitnessFunction(
			final JointPlan plan,
			final JointReplanningConfigGroup configGroup,
			final JointPlanOptimizerLegTravelTimeEstimatorFactory legTravelTimeEstimatorFactory,
			final PlansCalcRoute routingAlgorithm,
			final Network network,
			final int numJointEpisodes,
			final int numEpisodes,
			final int nMembers,
			final ScoringFunctionFactory scoringFunctionFactory,
			final JointPlanOptimizerDecoder partialDecoder,
			final JointPlanOptimizerDecoder fullDecoder) {
		super();
		this.decoder = partialDecoder;
		this.fullDecoder = fullDecoder;

		this.scorer = new DurationOnTheFlyScorer(
					plan,
					configGroup,
					scoringFunctionFactory,
					legTravelTimeEstimatorFactory,
					routingAlgorithm,
					network,
					numJointEpisodes,
					numEpisodes,
					nMembers);
	}


	@Override
	protected double evaluate(final IChromosome chromosome) {
		JointPlan plan = this.decoder.decode(chromosome);
		return this.scorer.score(chromosome, plan);
	}

	@Override
	public JointPlanOptimizerDecoder getDecoder() {
		return this.fullDecoder;
	}
}

