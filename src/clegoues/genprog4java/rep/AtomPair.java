package clegoues.genprog4java.rep;

public class AtomPair {
	private int atomid;
	private double weight;
	public AtomPair(int atomid, double weight) {
		this.atomid = atomid;
		this.weight = weight;
	}
	public AtomPair(int atomid) {
		this.atomid = atomid;
		this.weight = 1.0;
	}
	public int getAtom() {
		return atomid;
	}
	public double getWeight() {
		return weight;
	}
}
