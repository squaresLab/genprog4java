package clegoues.genprog4java.mut;

import clegoues.genprog4java.util.Pair;

public interface Location<G> extends Cloneable {
	
	// FIXME: add a toString
	public G getLocation();
	public int getId();
	public void setLocation(G location);
	public Double getWeight();
	public void setWeight(Double weight);
	public Pair<Integer,Double> asPair(); // FIXME: life would be better if the first argument were the code
	public Object clone();
}

