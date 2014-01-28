package clegoues.genprog4java.rep;
import clegoues.genprog4java.util.*;

public class AtomPair extends Pair<Integer,Double> {


	public AtomPair(Integer atomid) {
		super(atomid, 1.0);
	}

	public int getAtom() { return super.getFirst(); }
	public double getWeight() { return super.getSecond(); }

}
