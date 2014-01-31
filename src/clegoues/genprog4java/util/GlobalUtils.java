package clegoues.genprog4java.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.rep.WeightedAtom;

public class GlobalUtils {
	// range is inclusive!
	public static ArrayList<Integer> range(int start, int end) {
		ArrayList<Integer> returnVal = new ArrayList<Integer>();
		for(int i=start; i<=end; i++) {
			returnVal.add(i);
		}
		return returnVal;

	}


	public static Pair<?,Double> chooseOneWeighted(ArrayList<Pair<?,Double>> atoms) {
		assert(atoms.size() > 0);
		double totalWeight = 0.0;
		for(Pair<?,Double> atom : atoms) {
			totalWeight += atom.getSecond();
		}
		assert(totalWeight > 0.0) ;
		double wanted = Configuration.randomizer.nextDouble() * totalWeight;
		double sofar = 0.0;
		for(Pair<?,Double> atom : atoms) {
			double here = sofar + atom.getSecond();
			if(here >= wanted) {
				return atom;
			}
			sofar = here;
		}
		return null;
	}

}
