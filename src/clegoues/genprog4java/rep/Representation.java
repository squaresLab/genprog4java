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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.exec.CommandLine;
import org.apache.log4j.Logger;

import clegoues.genprog4java.Search.GiveUpException;
import clegoues.genprog4java.fitness.TestCase;
import clegoues.genprog4java.localization.Localization;
import clegoues.genprog4java.localization.Location;
import clegoues.genprog4java.java.ClassInfo;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.EditOperation;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.WeightedHole;
import clegoues.genprog4java.mut.WeightedMutation;
import clegoues.util.Pair;


@SuppressWarnings("rawtypes")
public abstract class Representation<G extends EditOperation> implements
Comparable<Representation<G>> {

	protected Logger logger = Logger.getLogger(Representation.class);
	protected Localization localization = null;

	protected String variantFolder = "";

	public Representation() {
	}

	public Representation(ArrayList<G> genome2) {
		this.setGenome(new ArrayList<G>(((List<G>) genome2)));
	}

	public String getName() {
		String result = "";
		if (this.getGenome().size() == 0) {
			return "original";
		}
		for (G h : this.getGenome()) {
			if (result.length() > 0) {
				result += " ";
			}
			String hstr = h.toString();
			result += hstr;
		}
		return result;
	}

	public String getVariantFolder() {
		return this.variantFolder;
	}
	public abstract Representation<G> copy();

	public abstract boolean getVariableLength();

	public abstract ArrayList<G> getGenome();

	public abstract void loadGenomeFromString(String genome);

	public abstract void setGenome(List<G> genome);

	public abstract int genomeLength();

	public abstract void load(ArrayList<ClassInfo> classNames) throws IOException,
	UnexpectedCoverageResultException;

	public void serialize(String filename, ObjectOutputStream fout,
			boolean globalinfo) { // second parameter is optional
		ObjectOutputStream out = null;
		FileOutputStream fileOut = null;
		try {
			if (fout == null) {
				fileOut = new FileOutputStream(filename + ".ser");
				out = new ObjectOutputStream(fileOut);
			} else {
				out = fout;
			}
			out.writeObject(this.getGenome());
		} catch (IOException e) {
			System.err
			.println("Representation: largely unexpected failure in serialization.");
			e.printStackTrace();
		} finally {
			if (fout == null) {
				try {
					if (out != null)
						out.close();
					if (fileOut != null)
						fileOut.close();
				} catch (IOException e) {
					System.err
					.println("Representation: largely unexpected failure in serialization.");
					e.printStackTrace();
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public boolean deserialize(String filename, ObjectInputStream fin,
			boolean globalinfo) { // second parameter is optional
		FileInputStream fileIn = null;
		ObjectInputStream in = null;
		boolean succeeded = true;
		try {
			if (fin == null) {
				fileIn = new FileInputStream(filename + ".ser");
				in = new ObjectInputStream(fileIn);
			} else {
				in = fin;
			}
			this.setGenome((ArrayList<G>) in.readObject());
		} catch (IOException e) {
			logger.error("Representation: IOException in deserialize "
					+ filename + " which is probably OK");
			e.printStackTrace();
			succeeded = false;
		} catch (ClassNotFoundException e) {
			System.err
			.println("Representation: ClassNotFoundException in deserialize "
					+ filename + " which is probably *not* OK");
			e.printStackTrace();
			succeeded = false;
		} finally {
			try {
				if (fin == null) {
					in.close();
					fileIn.close();
				}
			} catch (IOException e) {
				System.err
				.println("Representation: IOException in file close in deserialize "
						+ filename + " which is weird?");
				succeeded = false;
				e.printStackTrace();
			}
		}
		return succeeded;
	}
	
	public abstract boolean sanityCheck();

	public abstract void fromSource(ClassInfo base) throws IOException;

	public abstract void outputSource(String filename);

	public abstract void cleanup();

	public abstract double getFitness();

	public abstract boolean compile(String sourceName, String exeName);

	// I don't love this solution (test case knowing about coverage), but
	// it's the easiest way to get the necessary info to internalTestCaseCommand
	// without making coverage computation a state variable on rep.
	public abstract boolean testCase(TestCase test);
	public abstract boolean testCase(TestCase test, boolean doingCoverage);

	public abstract Set<WeightedMutation> availableMutations(
			Location faultyLocation);


	public static void configure(Properties prop) {
	}
	
	public abstract void performEdit(Mutation edit, Location dst, EditHole source); 

	public abstract void setFitness(double fitness);

	@Override
	public int compareTo(Representation<G> o) {
		Double myFitness = new Double(this.getFitness());
		return myFitness.compareTo(new Double(o.getFitness()));
	}

	protected List<Pair<ClassInfo, String>> computeSourceBuffers() {
		return null;
	}

	protected static ArrayList<String> getClasses(String filename)
			throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line;
		ArrayList<String> allLines = new ArrayList<String>();
		while ((line = br.readLine()) != null) {
			// print the line.
			allLines.add(line);
		}
		br.close();
		return allLines;
	}

	public abstract List<WeightedHole> editSources(Location stmtId, Mutation editType);

	public abstract Boolean shouldBeRemovedFromFix(WeightedAtom atom);
	
	public abstract Boolean doesEditApply(Location location, Mutation editType);

	public abstract ArrayList<Integer> atomIDofSourceLine(int line);

	public abstract Location instantiateLocation(Integer i, double negWeight);
	
	public abstract CommandLine internalTestCaseCommand(String exeName, String fileName, TestCase test, boolean doingCoverage);

	protected abstract CommandLine internalTestCaseCommand(String exeName,
			String fileName, TestCase test);

	protected abstract void printDebugInfo();

	public abstract Map<ClassInfo, String> getOriginalSource();

	public void setLocalization(Localization l) {
		this.localization = l;
	}
	
	public Localization getLocalization() {
		return this.localization ;
	}
	
}
