/*
 * Copyright (c) 2014-2015, 
 *  Claire Le Goues     <clegoues@cs.cmu.edu>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. The names of the contributors may not be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package clegoues.genprog4java.rep;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
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

@SuppressWarnings("rawtypes")
public abstract class Representation<G extends EditOperation> implements Comparable<Representation<G>> {

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
	public abstract void serialize(String filename, ObjectOutputStream fout); // second parameter is optional
	public abstract boolean deserialize(String filename, ObjectInputStream fin) throws UnexpectedCoverageResultException; // second parameter is optional
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

	public abstract void recordFitness(String key, FitnessValue fitness); 
	public abstract void setFitness(double fitness);  


	@Override
	public int compareTo(Representation<G> o) {
		Double myFitness = new Double(this.getFitness());
		return myFitness.compareTo(new Double(o.getFitness()));
	}


	protected List<Pair<String, String>> computeSourceBuffers() {
		// TODO Auto-generated method stub
		return null;
	}

}
