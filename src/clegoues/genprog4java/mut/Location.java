package clegoues.genprog4java.mut;

import clegoues.util.Pair;

public interface Location<G extends Comparable<G>> extends Cloneable {
	
	public G getLocation();
	public int getId();
	public void setLocation(G location);
	public Double getWeight();
	public void setWeight(Double weight);
	public Pair<G,Double> asPair(); // FIXME: life would be better if the first argument were the code
	public Object clone();
}

