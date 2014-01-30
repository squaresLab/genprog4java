package clegoues.genprog4java.rep;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import clegoues.genprog4java.Fitness.TestCase;
import clegoues.genprog4java.mut.EditOperation;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.util.Pair;

// it's not clear that this EditOperation thing is a good choice because 
// it basically forces the patch representation.  Possibly it's flexible and the naming scheme is 
// just bad.  I'll have to think about it.

public interface Representation<G extends EditOperation> extends Comparable<Representation<G>> {
// will compare on fitness!
		boolean getVariableLength();
		ArrayList<G> getGenome();
		void loadGenomeFromString(String genome);
		void setGenome(List<G> genome);
		int genomeLength();
		void noteSuccess();
		void load(String filename) throws IOException;
		void serialize(String filename);
		boolean deserialize(String filename);
		List<WeightedAtom> getFaultyAtoms();
		List<WeightedAtom> getFixSourceAtoms();
		boolean sanityCheck() throws SanityCheckException;
		void fromSource(String filename);
		void outputSource(String filename);
		List<String> sourceName();
		void cleanup();
		void setFitness(double fitness);
		double getFitness();
		boolean fitnessIsValid();
		boolean compile(String sourceName, String exeName);
		boolean testCase(TestCase test);
		String getName();
		void reduceSearchSpace(); // do this?
		void reduceFixSpace(); 
		TreeSet<Pair<Mutation, Double>> availableMutations(int atomId);
		void registerMutations(TreeSet<Pair<Mutation,Double>> availableMutations);
		// TODO: do we need  availableCrossoverPoints? Crossover is so stupid.
		void delete(int atomId);
		void append(int whereToAppend, int whatToAppend);
		TreeSet<WeightedAtom> appendSources(int atomId);
		void swap(int swap1, int swap2);
		TreeSet<WeightedAtom>  swapSources(int atomId);
		void replace(int whatToReplace, int whatToReplaceWith);
		TreeSet<WeightedAtom>  replaceSources(int atomId);

		Representation<G> copy();
		int num_test_evals_ignore_cache(); // FIXME this really needs to not be here
		void computeLocalization(String wd) throws IOException,
				UnexpectedCoverageResultException;

}
