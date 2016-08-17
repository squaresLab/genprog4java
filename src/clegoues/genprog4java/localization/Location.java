package clegoues.genprog4java.localization;

import org.apache.commons.lang3.tuple.Pair;

public abstract class Location<G> extends Pair<G, Double> implements Cloneable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7299957582697769112L;

	public abstract int getId();

	private G location;
	private Double weight;
	
	public Location(G location, Double weight) {
		this.location = location;
		this.weight = weight;
	}
	
	public G getLocation() {
		return this.location;
	}
	public void setLocation(G location) {
		this.location = location;
	}

	public Double getWeight() {
		return this.weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}

	@Override
	public Double setValue(Double value) {
		Double oldValue = this.weight;
		weight = value;
		return oldValue;
	}

	@Override
	public G getLeft() {
		return this.getLocation();
	}

	@Override
	public Double getRight() {
		return this.getWeight();
	}

}

