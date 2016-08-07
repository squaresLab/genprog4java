package clegoues.genprog4java.mut;

import org.apache.commons.lang3.tuple.Pair;

import clegoues.genprog4java.mut.EditHole;

public class WeightedHole<T> extends Pair<EditHole<T>,Double> {

	private EditHole<T> hole;
	private Double weight;
	
	public WeightedHole(EditHole<T> hole) {
		this.hole = hole;
		this.weight = 1.0;
	}
	public WeightedHole(EditHole<T> hole, Double weight) {
		this.hole = hole;
		this.weight = weight;
	}
	public EditHole<T> getHole() { return this.hole; }
	public double getWeight() { return this.weight; }

	public int compareTo(WeightedHole<T> otherAtomPair) {
		return new Double(this.getRight()).compareTo(new Double(otherAtomPair.getRight()));
	}
	public void setWeight(double d) {
		this.weight = d;
	}
	@Override
	public Double setValue(Double value) {
		double oldValue = weight;
		this.setWeight(value);
		return oldValue;
	}
	@Override
	public EditHole<T> getLeft() {
		return this.hole;
	}
	@Override
	public Double getRight() {
		return this.weight;
	}
}
