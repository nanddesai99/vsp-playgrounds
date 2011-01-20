package playground.ciarif.flexibletransports.router;

import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.router.costcalculators.TravelCostCalculatorFactory;
import org.matsim.core.router.util.PersonalizableTravelCost;
import org.matsim.core.router.util.PersonalizableTravelTime;
import org.matsim.core.router.util.TravelTime;
import playground.ciarif.flexibletransports.config.FtConfigGroup;

public class FtTravelCostCalculatorFactory
  implements TravelCostCalculatorFactory
{
  private FtConfigGroup ftConfigGroup = null;

  public FtTravelCostCalculatorFactory(FtConfigGroup ftConfigGroup)
  {
    this.ftConfigGroup = ftConfigGroup;
  }

  public PersonalizableTravelCost createTravelCostCalculator(TravelTime timeCalculator, PlanCalcScoreConfigGroup cnScoringGroup) {
    return new FtTravelTimeDistanceCostCalculator(timeCalculator, cnScoringGroup, this.ftConfigGroup);
  }

@Override
public PersonalizableTravelCost createTravelCostCalculator(
		PersonalizableTravelTime timeCalculator,
		PlanCalcScoreConfigGroup cnScoringGroup) {
	// TODO Auto-generated method stub
	return null;
}
}
