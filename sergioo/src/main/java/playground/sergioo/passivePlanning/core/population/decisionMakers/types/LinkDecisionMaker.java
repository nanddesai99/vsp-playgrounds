package playground.sergioo.passivePlanning.core.population.decisionMakers.types;

import org.matsim.api.core.v01.Coord;

public interface LinkDecisionMaker extends DecisionMaker {

	//Methods
	public Coord decideLink();

}
