package clegoues.genprog4java.mut;

import clegoues.util.Pair;

public abstract class Location<G> extends Pair<G, Double> implements Cloneable {
	
	public abstract int getId();

	public Location(G location, Double weight) {
		super(location, weight);
	}
	
	public G getLocation() {
		return this.getFirst();
	}
	public void setLocation(G location) {
		this.setFirst(location);	
	}

	public Double getWeight() {
		return this.getSecond();
	}

	public void setWeight(Double weight) {
		this.setSecond(weight);
	}

}

