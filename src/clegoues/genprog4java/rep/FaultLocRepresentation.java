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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.fitness.TestCase;
import clegoues.genprog4java.fitness.TestType;
import clegoues.genprog4java.main.ClassInfo;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.mut.EditOperation;
import clegoues.genprog4java.mut.HistoryEle;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.util.Pair;

@SuppressWarnings("rawtypes")
public abstract class FaultLocRepresentation<G extends EditOperation> extends
		CachingRepresentation<G> {
	protected Logger logger = Logger.getLogger(FaultLocRepresentation.class);

	private static double positivePathWeight = 0.1;
	private static double negativePathWeight = 1.0;
	protected static boolean allowCoverageFail = false;
	protected static String posCoverageFile = "coverage.path.pos";
	protected static String negCoverageFile = "coverage.path.neg";
	protected static boolean regenPaths = false;

	protected boolean doingCoverage = false;
	private ArrayList<WeightedAtom> faultLocalization = new ArrayList<WeightedAtom>();
	private ArrayList<WeightedAtom> fixLocalization = new ArrayList<WeightedAtom>();

	public FaultLocRepresentation(ArrayList<HistoryEle> history,
			ArrayList<G> genome2, ArrayList<WeightedAtom> arrayList,
			ArrayList<WeightedAtom> arrayList2) {
		super(history, genome2);
		this.faultLocalization = new ArrayList<WeightedAtom>(arrayList);
		this.fixLocalization = new ArrayList<WeightedAtom>(arrayList2);
	}

	public FaultLocRepresentation() {
		super();
	}

	public static void configure(Properties prop) {
		if (prop.getProperty("positivePathWeight") != null) {
			positivePathWeight = Double.parseDouble(prop.getProperty(
					"positivePathWeight").trim());
		}
		if (prop.getProperty("negativePathWeight") != null) {
			negativePathWeight = Double.parseDouble(prop.getProperty(
					"negativePathWeight").trim());
		}
		if (prop.getProperty("allowCoverageFail") != null) {
			allowCoverageFail = true;
		}
		if (prop.getProperty("posCoverageFile") != null) {
			posCoverageFile = prop.getProperty("posCoverageFile").trim();
		}
		if (prop.getProperty("negCoverageFile") != null) {
			negCoverageFile = prop.getProperty("negCoverageFile").trim();
		}
		if (prop.getProperty("regenPaths") != null) {
			regenPaths = true;
		}
	}

	@Override
	public void serialize(String filename, ObjectOutputStream fout,
			boolean globalinfo) {
		ObjectOutputStream out = null;
		FileOutputStream fileOut = null;
		try {
			if (fout == null) {
				fileOut = new FileOutputStream(filename + ".ser");
				out = new ObjectOutputStream(fileOut);
			} else {
				out = fout;
			}
			super.serialize(filename, out, globalinfo);
			out.writeObject(this.faultLocalization);
			out.writeObject(this.fixLocalization);

			// doesn't exist yet, but remember when you add it:
			// out.writeObject(FaultLocalization.faultScheme);
			if (globalinfo) {
				out.writeObject(FaultLocRepresentation.negativePathWeight);
				out.writeObject(FaultLocRepresentation.positivePathWeight);
			}
		} catch (IOException e) {
			System.err
					.println("faultLocRep: largely unexpected failure in serialization.");
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
							.println("faultLocRep: largely unexpected failure in serialization.");
					e.printStackTrace();
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean deserialize(String filename, ObjectInputStream fin,
			boolean globalinfo) {
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
			if (super.deserialize(filename, in, globalinfo)) {
				this.faultLocalization = (ArrayList<WeightedAtom>) in
						.readObject();
				this.fixLocalization = (ArrayList<WeightedAtom>) in
						.readObject();
				// doesn't exist yet, but remember when you add it:
				// out.writeObject(FaultLocalization.faultScheme);
				double negWeight = (double) in.readObject();
				double posWeight = (double) in.readObject();
				if (negWeight != FaultLocRepresentation.negativePathWeight
						|| posWeight != FaultLocRepresentation.positivePathWeight
						|| FaultLocRepresentation.regenPaths) {
					this.computeLocalization(); // I remember needing to do this
												// in OCaml but I don't remember
												// why?
				}
				logger.info("faultLocRepresentation: " + filename + "loaded\n");
			} else {
				succeeded = false;
			}
		} catch (IOException e) {
			System.err
					.println("faultLocRepresentation: IOException in deserialize "
							+ filename + " which is probably OK");
			succeeded = false;
		} catch (ClassNotFoundException e) {
			System.err
					.println("faultLocRepresentation: ClassNotFoundException in deserialize "
							+ filename + " which is probably *not* OK");
			e.printStackTrace();
			succeeded = false;
		} catch (UnexpectedCoverageResultException e) {
			System.err
					.println("faultLocRepresentation: reran coverage in faultLocRep deserialize and something unexpected happened, so I'm giving up.");
			Runtime.getRuntime().exit(1);
		} finally {
			try {
				if (fin == null) {
					if (in != null)
						in.close();
					if (fileIn != null)
						fileIn.close();
				}
			} catch (IOException e) {
				succeeded = false;
				System.err
						.println("faultLocRepresentation: IOException in file close in deserialize "
								+ filename + " which is weird?");
				e.printStackTrace();
			}
		}
		return succeeded;
	}

	public ArrayList<WeightedAtom> getFaultyAtoms() {
		return this.faultLocalization;
	}

	public ArrayList<WeightedAtom> getFixSourceAtoms() {
		return this.fixLocalization;
	}

	public TreeSet<Pair<Mutation, Double>> availableMutations(int atomId) {
		TreeSet<Pair<Mutation, Double>> retVal = new TreeSet<Pair<Mutation, Double>>();
		for (Pair<Mutation, Double> mutation : Representation.mutations) {
			boolean addToSet = false;
			switch (mutation.getFirst()) {
			case DELETE:
				addToSet = true;
				break;
			case APPEND:
				addToSet = this.appendSources(atomId).size() > 0;
				break;
			case REPLACE:
				addToSet = this.replaceSources(atomId).size() > 0;
				break;
			case SWAP:
				addToSet = this.swapSources(atomId).size() > 0;
				break;
			}
			if (addToSet) {
				retVal.add(mutation);
			}
		}
		return retVal;
	}

	@Override
	// you probably want to override these for semantic legality check
	public TreeSet<WeightedAtom> appendSources(int stmtId) {
		TreeSet<WeightedAtom> retVal = new TreeSet<WeightedAtom>();
		for (WeightedAtom item : this.fixLocalization) {
			retVal.add(item);
		}
		return retVal;
	}

	@Override
	public TreeSet<WeightedAtom> swapSources(int stmtId) {
		TreeSet<WeightedAtom> retVal = new TreeSet<WeightedAtom>();
		for (WeightedAtom item : this.fixLocalization) {
			retVal.add(item);
		}
		return retVal;
	}

	@Override
	public TreeSet<WeightedAtom> replaceSources(int stmtId) {
		TreeSet<WeightedAtom> retVal = new TreeSet<WeightedAtom>();
		for (WeightedAtom item : this.fixLocalization) {
			retVal.add(item);
		}
		return retVal;
	}

	/*
	 * 
	 * (** run the instrumented code to attain coverage information. Writes the
	 * generated paths to disk (the fault and fix path files respectively) but
	 * does not otherwise return.
	 * 
	 * If the calls to [Unix.unlink] fail, they will do so silently.
	 * 
	 * @param coverage_sourcename instrumented source code on disk
	 * 
	 * @param coverage_exename compiled executable
	 * 
	 * @param coverage_outname on disk path file name
	 * 
	 * @raise Fail("abort") if variant produces produces unexpected behavior on
	 * either positive or negative test cases and [--allow-coverage-fail] is not
	 * on. get_coverage will abort if allow_coverage_fail is not toggled and the
	 * variant)
	 * 
	 * Traditional "weighted path" or "set difference" or Reiss-Renieris fault
	 * localization involves finding all of the statements visited while
	 * executing the negative test case(s) and removing/down-weighting
	 * statements visited while executing the positive test case(s).
	 */

	protected abstract ArrayList<Integer> atomIDofSourceLine(int lineno);

	private TreeSet<Integer> runTestsCoverage(String pathFile, TestType testT,
			ArrayList<String> tests, boolean expectedResult, String wd)
			throws IOException, UnexpectedCoverageResultException {
		int counterCoverageErrors = 0;

		TreeSet<Integer> atoms = new TreeSet<Integer>();
		for (String test : tests) {
			File coverageRaw = new File("jacoco.exec");

			if (coverageRaw.exists()) {
				coverageRaw.delete();
			}
			TestCase newTest = new TestCase(testT, test);

			// this expectedResult is just 'true' for positive tests and 'false'
			// for neg tests
			if (this.testCase(newTest) != expectedResult
					&& !FaultLocRepresentation.allowCoverageFail) {
				logger.error("FaultLocRep: unexpected coverage result: "
						+ newTest.toString());
				logger.error("Number of coverage errors so far: "
						+ ++counterCoverageErrors);

			}
			TreeSet<Integer> thisTestResult = this.getCoverageInfo();
			atoms.addAll(thisTestResult);
		}

		BufferedWriter out = new BufferedWriter(new FileWriter(new File(
				pathFile)));

		for (int atom : atoms) {
			out.write("" + atom + "\n");
		}

		out.flush();
		out.close();

		return atoms;
	}

	protected abstract TreeSet<Integer> getCoverageInfo()
			throws FileNotFoundException, IOException;

	private TreeSet<Integer> readPathFile(String pathFile) {
		TreeSet<Integer> retVal = new TreeSet<Integer>();
		Scanner reader = null;
		try {
			reader = new Scanner(new FileInputStream(pathFile));
			while (reader.hasNextInt()) {
				int i = reader.nextInt();
				retVal.add(i);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			logger.error("coverage file " + pathFile + " not found");
			e.printStackTrace();
		} finally {
			if (reader != null)
				reader.close();
		}
		return retVal;

	}

	protected void computeLocalization() throws IOException,
			UnexpectedCoverageResultException {
		// FIXME: THIS ONLY DOES STANDARD PATH FILE localization
		/*
		 * Default "ICSE'09"-style fault and fix localization from path files.
		 * The weighted path fault localization is a list of <atom,weight>
		 * pairs. The fix weights are a hash table mapping atom_ids to weights.
		 */
		logger.info("Start Fault Localization");
		this.doingCoverage = true;
		TreeSet<Integer> positivePath = null;
		TreeSet<Integer> negativePath = null;
		File positivePathFile = new File(FaultLocRepresentation.posCoverageFile);
		// OK, we don't instrument Java programs, rather, use java library that
		// computes coverage for us.
		// which means either instrumentFaultLocalization should still exist and
		// change the commands used for test case execution
		// or we don't pretend this is trying to match OCaml exactly?
		this.instrumentForFaultLocalization();
		File covDir = new File(Configuration.outputDir + "/coverage/");
		if (!covDir.exists())
			covDir.mkdir();
		if (!this.compile("coverage", "coverage/coverage.out")) {
			logger.error("faultLocRep: Coverage failed to compile");
			throw new UnexpectedCoverageResultException("compilation failure");
		}
		if (positivePathFile.exists() && !FaultLocRepresentation.regenPaths) {
			positivePath = readPathFile(FaultLocRepresentation.posCoverageFile);
		} else {
			positivePath = runTestsCoverage(
					FaultLocRepresentation.posCoverageFile, TestType.POSITIVE,
					Fitness.positiveTests, true, Configuration.workingDir + Configuration.outputDir + "/coverage/");
		}
		File negativePathFile = new File(FaultLocRepresentation.negCoverageFile);

		if (negativePathFile.exists() && !FaultLocRepresentation.regenPaths) {
			negativePath = readPathFile(FaultLocRepresentation.negCoverageFile);
		} else {
			negativePath = runTestsCoverage(
					FaultLocRepresentation.negCoverageFile, TestType.NEGATIVE,
					Fitness.negativeTests, false, Configuration.workingDir + Configuration.outputDir + "/coverage/");
		}
		HashMap<Integer, Double> fw = new HashMap<Integer, Double>();
		TreeSet<Integer> negHt = new TreeSet<Integer>();
		TreeSet<Integer> posHt = new TreeSet<Integer>();

		for (Integer i : positivePath) {
			// this is negative path in the OCaml code and I think that may be
			// wrong.
			fw.put(i, FaultLocRepresentation.positivePathWeight);
		}
		for (Integer i : positivePath) {
			posHt.add(i);
			fw.put(i, 0.5);
		}
		for (Integer i : negativePath) {
			if (!negHt.contains(i)) {
				double negWeight = FaultLocRepresentation.negativePathWeight;
				if (posHt.contains(i)) {
					negWeight = FaultLocRepresentation.positivePathWeight;
				}
				negHt.add(i);
				fw.put(i, 0.5);
				faultLocalization.add(new WeightedAtom(i, negWeight));
			}
		}
		for (Map.Entry<Integer, Double> entry : fw.entrySet()) {
			Integer key = entry.getKey();
			Double value = entry.getValue();
			fixLocalization.add(new WeightedAtom(key, value));
		}
		assert (faultLocalization.size() > 0);
		assert (fixLocalization.size() > 0);
		this.doingCoverage = false;
		logger.info("Finish Fault Localization");
		// this.printDebugInfo();
		// System.exit(0);
	}

	protected abstract void printDebugInfo();

	protected abstract void instrumentForFaultLocalization();

	@Override
	public void load(ArrayList<ClassInfo> bases) throws IOException {

		// SHOULD WE DO SOMETHING SO THAT THE FAULT LOCALIZATION ALSO CONSIDERS
		// MULTIPLE FILES TO LOCATE THE FAULT?
		// ArrayList<String> targetClassNames = new ArrayList<String>();
		// targetClassNames.addAll(getClasses(classList));

		// for(String fname : targetClassNames){

		// String filename =
		// fname.substring(fname.lastIndexOf(".")+1,fname.length());
		// filename += Configuration.globalExtension;

		super.load(bases); // calling super so that the code is loaded and the
							// sanity check happens before localization is
							// computed
		try {
			this.computeLocalization();
		} catch (UnexpectedCoverageResultException e) {
			logger.error("FaultLocRep: UnexpectedCoverageResult");
			Runtime.getRuntime().exit(1);
		}
		// }
	}

}
