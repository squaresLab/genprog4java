package clegoues.genprog4java.fitness;

import clegoues.genprog4java.rep.Representation;

public class InvariantDiversityObjective implements Objective {

	@Override
	public double getScore(Representation item, int generation) 
	{
		return item.diversity;
	}

}
