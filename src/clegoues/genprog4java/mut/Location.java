package clegoues.genprog4java.mut;

import clegoues.util.Pair;

public abstract class Location<G extends Comparable<G>> extends Pair<G, Double> implements Cloneable {
	
	public abstract G getLocation();
	public abstract int getId();
	public abstract void setLocation(G location);
	public abstract Double getWeight();
	public abstract void setWeight(Double weight);
}

