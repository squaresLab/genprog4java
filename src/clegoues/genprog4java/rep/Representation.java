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
import clegoues.genprog4java.mut.HistoryEle;
import clegoues.genprog4java.mut.JavaEditOperation;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.util.Pair;

// it's not clear that this EditOperation thing is a good choice because 
// it basically forces the patch representation.  Possibly it's flexible and the naming scheme is 
// just bad.  I'll have to think about it.

public abstract class Representation<G extends EditOperation> implements Comparable<Representation<G>> {
	// will compare on fitness!
	private ArrayList<HistoryEle> history = new ArrayList<HistoryEle>();
	public Representation() { }

	
	public Representation(ArrayList<HistoryEle> history, ArrayList<JavaEditOperation> genome2) {
		this.setGenome(new ArrayList<G>(((List<G>) genome2)));
		this.setHistory(new ArrayList<HistoryEle>(history));
	}
	public String getName() {
		String result = "";
		if(history.size() == 0) {
			return "original";
		}
		for(HistoryEle h : history) {
			if(result.length() > 0) {
				result += " ";
			}
			String hstr = h.toString();
			result += hstr;
		}
		return result; 
		} 


	public ArrayList<HistoryEle> getHistory() { return history; }
	public void setHistory(ArrayList<HistoryEle> history) { this.history = history; }
	public abstract Representation<G> copy();
	public abstract boolean getVariableLength();
	public abstract ArrayList<G> getGenome();
	public abstract void loadGenomeFromString(String genome);
	public abstract void setGenome(List<G> genome);
	public abstract int genomeLength();
	public abstract void noteSuccess();
	public abstract void load(String filename) throws IOException, UnexpectedCoverageResultException;
	public abstract void serialize(String filename);
	public abstract boolean deserialize(String filename);
	public abstract ArrayList<WeightedAtom> getFaultyAtoms();
	public abstract ArrayList<WeightedAtom> getFixSourceAtoms();
	public abstract boolean sanityCheck();
	public abstract void fromSource(String filename) throws IOException;
	public abstract void outputSource(String filename);
	public abstract List<String> sourceName();
	public abstract void cleanup();
	public abstract double getFitness();
	public abstract boolean compile(String sourceName, String exeName);
	public abstract boolean testCase(TestCase test);
	public abstract void reduceSearchSpace(); // do this?
	public abstract void reduceFixSpace(); 
	public abstract TreeSet<Pair<Mutation, Double>> availableMutations(int atomId);
	protected static TreeSet<Pair<Mutation,Double>> mutations = null;

	// FIXME: this static mutation thing is so lazy of me, I can't even.  But I'm tired of this clone/copy debacle and just want it to Go Away. 
	public static void registerMutations(TreeSet<Pair<Mutation,Double>> availableMutations) { 
		Representation.mutations = new TreeSet<Pair<Mutation,Double>> ();
		for(Pair<Mutation,Double> candidateMut : availableMutations) {
			if(candidateMut.getSecond() > 0.0) {
				Representation.mutations.add(candidateMut);
			}
		}
		
	}
	// TODO: do we need  availableCrossoverPoints? Crossover is so stupid.
	public void delete(int atomId) {
		history.add(new HistoryEle(Mutation.DELETE, atomId));
	}
	public void append(int whereToAppend, int whatToAppend) {
		history.add(new HistoryEle(Mutation.APPEND, whereToAppend, whatToAppend));
	}
	public abstract TreeSet<WeightedAtom> appendSources(int atomId);
	public void swap(int swap1, int swap2) {
		history.add(new HistoryEle(Mutation.SWAP, swap1, swap2));
	}
	public abstract TreeSet<WeightedAtom>  swapSources(int atomId);
	public void replace(int whatToReplace, int whatToReplaceWith) {
		history.add(new HistoryEle(Mutation.REPLACE, whatToReplace, whatToReplaceWith));
	}
	public abstract TreeSet<WeightedAtom>  replaceSources(int atomId);

	public static void configure(Properties prop) {
	}
	public abstract void computeLocalization() throws IOException,
	UnexpectedCoverageResultException;
	public abstract void recordFitness(String key, FitnessValue fitness); 
	public abstract void setFitness(double fitness);  


}
