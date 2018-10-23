package clegoues.genprog4java.fitness;

import java.util.Comparator;

import clegoues.genprog4java.rep.Representation;

public interface Objective {
	public double getScore(Representation item, int generation);
}
