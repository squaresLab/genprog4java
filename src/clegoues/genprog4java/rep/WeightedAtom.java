package clegoues.genprog4java.rep;
import clegoues.genprog4java.util.*;

public class WeightedAtom extends Pair<Integer,Double> {

	public WeightedAtom(Integer atomid) {
		super(atomid, 1.0);
	}
	public WeightedAtom(Integer atomid, Double weight) {
		super(atomid, weight);
	}
	public int getAtom() { return super.getFirst(); }
	public double getWeight() { return super.getSecond(); }

	public int compareTo(WeightedAtom otherAtomPair) {
		return new Double(this.getSecond()).compareTo(new Double(otherAtomPair.getSecond()));
	}
}
