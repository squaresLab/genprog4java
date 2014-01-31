package clegoues.genprog4java.rep;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;

import clegoues.genprog4java.Fitness.FitnessValue;
import clegoues.genprog4java.Fitness.TestCase;
import clegoues.genprog4java.mut.EditOperation;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.util.Pair;

// it's not clear that this EditOperation thing is a good choice because 
// it basically forces the patch representation.  Possibly it's flexible and the naming scheme is 
// just bad.  I'll have to think about it.

public abstract class Representation<G extends EditOperation> implements Comparable<Representation<G>>, Cloneable {
// will compare on fitness!
	private String name = null;
		public Representation<G> clone() throws CloneNotSupportedException {
			Representation<G> clone = (Representation<G>) super.clone();
			clone.name = newName();
			return clone;
		}
		public Representation() {
			this.name = newName();
		}
		public static int sequence = 0;
		public static String newName() {
			String result = String.format("%6d", sequence);
			sequence++;
			return result;
		}
		public String getName() { return name; } // this won't give the history, which I should fix
		public void setName(String newName) { this.name = newName; }
		
		public abstract boolean getVariableLength();
		public abstract ArrayList<G> getGenome();
		public abstract void loadGenomeFromString(String genome);
		public abstract void setGenome(List<G> genome);
		public abstract int genomeLength();
		public abstract void noteSuccess();
		public abstract void load(String filename) throws IOException, UnexpectedCoverageResultException;
		public abstract void serialize(String filename);
		public abstract boolean deserialize(String filename);
		public abstract List<WeightedAtom> getFaultyAtoms();
		public abstract List<WeightedAtom> getFixSourceAtoms();
		public abstract boolean sanityCheck() throws SanityCheckException;
		public abstract void fromSource(String filename) throws IOException;
		public abstract void outputSource(String filename);
		public abstract List<String> sourceName();
		public abstract void cleanup();
		public abstract HashMap<String, FitnessValue> getFitness();
		public abstract boolean compile(String sourceName, String exeName);
		public abstract boolean testCase(TestCase test);
		public abstract void reduceSearchSpace(); // do this?
		public abstract void reduceFixSpace(); 
		public abstract TreeSet<Pair<Mutation, Double>> availableMutations(int atomId);
		public abstract void registerMutations(TreeSet<Pair<Mutation,Double>> availableMutations);
		// TODO: do we need  availableCrossoverPoints? Crossover is so stupid.
		public abstract void delete(int atomId);
		public abstract void append(int whereToAppend, int whatToAppend);
		public abstract TreeSet<WeightedAtom> appendSources(int atomId);
		public abstract void swap(int swap1, int swap2);
		public abstract TreeSet<WeightedAtom>  swapSources(int atomId);
		public abstract void replace(int whatToReplace, int whatToReplaceWith);
		public abstract TreeSet<WeightedAtom>  replaceSources(int atomId);

		public abstract int num_test_evals_ignore_cache(); // FIXME this really needs to not be here
		public static void configure(Properties prop) {
		}
		public abstract void computeLocalization() throws IOException,
				UnexpectedCoverageResultException;
		public abstract void setFitness(String key, FitnessValue fitness); 


}
