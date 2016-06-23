package clegoues.genprog4java.mut;

import clegoues.util.Pair;
import clegoues.genprog4java.mut.EditHole;

public class WeightedHole<T> extends Pair<EditHole<T>,Double> {

	public WeightedHole(EditHole<T> hole) {
		super(hole, 1.0);
	}
	public WeightedHole(EditHole<T> hole, Double weight) {
		super(hole, weight);
	}
	public EditHole<T> getHole() { return super.getFirst(); }
	public double getWeight() { return super.getSecond(); }

	public int compareTo(WeightedHole<T> otherAtomPair) {
		return new Double(this.getSecond()).compareTo(new Double(otherAtomPair.getSecond()));
	}
	public void setWeight(double d) {
		this.setSecond(d);
	}
}
