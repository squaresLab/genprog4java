package clegoues.genprog4java.fitness;

import clegoues.genprog4java.rep.Representation;

public class PositiveTestCasesObjective implements Objective {
	
	@Override
	public double getScore(Representation item, int generation) 
	{
		return item.getNumSampledPosTestsPassed();
	}

}
