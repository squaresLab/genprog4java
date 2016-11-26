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

import static clegoues.util.ConfigurationBuilder.BOOLEAN;
import static clegoues.util.ConfigurationBuilder.BOOL_ARG;
import static clegoues.util.ConfigurationBuilder.DOUBLE;
import static clegoues.util.ConfigurationBuilder.STRING;
import static clegoues.util.ConfigurationBuilder.INT;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import clegoues.genprog4java.Search.Search;
import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.fitness.TestCase;
import clegoues.genprog4java.java.ClassInfo;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.mut.EditOperation;
import clegoues.genprog4java.mut.Location;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.WeightedMutation;
import clegoues.util.ConfigurationBuilder;

@SuppressWarnings("rawtypes")
public abstract class FaultLocRepresentation<G extends EditOperation> extends
CachingRepresentation<G> {

	protected Logger logger = Logger.getLogger(FaultLocRepresentation.class);

	public transient static final ConfigurationBuilder.RegistryToken token =
			ConfigurationBuilder.getToken();

	public static boolean justTestingFaultLoc = ConfigurationBuilder.of( BOOL_ARG )
			.withVarName( "justTestingFaultLoc" )
			.withDefault( "false" )
			.withHelp( "boolean to be turned true if the purpose is to test that fault loc is performed correctly" )
			.inGroup( "FaultLocRepresentation Parameters" )
			.build();

	//private static double positivePathWeight = 0.1;
	private static double positivePathWeight = ConfigurationBuilder.of( DOUBLE )
			.withVarName( "positivePathWeight" )
			.withDefault( "0.65" )
			.withHelp( "weighting for statements on the positive path" )
			.inGroup( "FaultLocRepresentation Parameters" )
			.build();
	//private static double negativePathWeight = 1.0;
	private static double negativePathWeight = ConfigurationBuilder.of( DOUBLE )
			.withVarName( "negativePathWeight" )
			.withDefault( "0.35" )
			.withHelp( "weighting for statements on the negative path" )
			.inGroup( "FaultLocRepresentation Parameters" )
			.build();
	//protected static boolean allowCoverageFail = false;
	protected static boolean allowCoverageFail = ConfigurationBuilder.of( BOOLEAN )
			.withVarName( "allowCoverageFail" )
			.withHelp( "ignore unexpected test results in coverage" )
			.inGroup( "FaultLocRepresentation Parameters" )
			.build();
	//protected static String posCoverageFile = "coverage.path.pos";
	protected static String posCoverageFile = ConfigurationBuilder.of( STRING )
			.withVarName( "posCoverageFile" )
			.withDefault( "coverage.path.pos" )
			.withHelp( "file containing the statements covered by positive tests" )
			.inGroup( "FaultLocRepresentation Parameters" )
			.build();
	//protected static String negCoverageFile = "coverage.path.neg";
	protected static String negCoverageFile = ConfigurationBuilder.of( STRING )
			.withVarName( "negCoverageFile" )
			.withDefault( "coverage.path.neg" )
			.withHelp( "file containing the statements covered by negative tests" )
			.inGroup( "FaultLocRepresentation Parameters" )
			.build();
	//protected static boolean regenPaths = false;
	protected static boolean regenPaths = ConfigurationBuilder.of( BOOLEAN )
			.withVarName( "regenPaths" )
			.withHelp( "regenerate coverage information" )
			.inGroup( "FaultLocRepresentation Parameters" )
			.build();

	protected static String fixStrategy = ConfigurationBuilder.of ( STRING )
			.withVarName("fixStrategy")
			.withHelp("Fix source strategy")
			.withDefault("classScope")
			.inGroup( "FaultLocRepresentation Parameters" )
			.build();
	protected static String faultLocStrategy = ConfigurationBuilder.of ( STRING )
			.withVarName("faultLocStrategy")
			.withHelp("Fault localization strategy")
			.withDefault("standardPathFile")
			.inGroup( "FaultLocRepresentation Parameters" )
			.build();
	protected static String pathToFileHumanInjectedFaultLoc = ConfigurationBuilder.of ( STRING )
			.withVarName("pathToFileHumanInjectedFaultLoc")
			.withHelp("The path of the file with classes and line numbers of the faulty stmts, when fault localization is human inserted and not created by the coverage")
			.withDefault("fileHumanInjectedFaultLoc.txt")
			.inGroup( "FaultLocRepresentation Parameters" )
			.build();

	protected boolean doingCoverage = false;
	protected ArrayList<Location> faultLocalization = new ArrayList<Location>();
	protected ArrayList<WeightedAtom> fixLocalization = new ArrayList<WeightedAtom>();

	public FaultLocRepresentation(ArrayList<G> genome2, ArrayList<Location> arrayList,
			ArrayList<WeightedAtom> arrayList2) {
		super(genome2);
		this.faultLocalization = new ArrayList<Location>(arrayList);
		this.fixLocalization = new ArrayList<WeightedAtom>(arrayList2);
	}

	public FaultLocRepresentation() {
		super();
	}

	public ArrayList<Location> getFaultyLocations() {
		return this.faultLocalization;
	}

	public ArrayList<WeightedAtom> getFixSourceAtoms() {
		return this.fixLocalization;
	}

	@Override
	public List<WeightedMutation> availableMutations(Location atomId) {
		List<WeightedMutation> retVal = new LinkedList<WeightedMutation>();
		for (WeightedMutation mutation : Search.availableMutations) {
			if(this.doesEditApply(atomId, (Mutation) mutation.getKey())) {
				retVal.add(new WeightedMutation((Mutation) mutation.getKey(), (Double) mutation.getValue()));
			}
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

	protected abstract ClassInfo getFileFromStmt(int stmtId);

	private TreeSet<Integer> runTestsCoverage(String pathFile,
			ArrayList<TestCase> tests, boolean expectedResult, String wd)
					throws IOException, UnexpectedCoverageResultException {
		int counterCoverageErrors = 0;

		TreeSet<Integer> atoms = new TreeSet<Integer>();
		for (TestCase test : tests) {
			File coverageRaw = new File("jacoco.exec");

			if (coverageRaw.exists()) {
				coverageRaw.delete();
			}

			//System.out.println(test);
			logger.info(test);
			// this expectedResult is just 'true' for positive tests and 'false'
			// for neg tests
			if (this.testCase(test).isAllPassed() != expectedResult
					&& !FaultLocRepresentation.allowCoverageFail) {
				logger.error("FaultLocRep: unexpected coverage result: "
						+ test.toString());
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
		// THIS ONLY DOES STANDARD PATH FILE localization
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
			positivePath = createPosFile();
		}
		File negativePathFile = new File(FaultLocRepresentation.negCoverageFile);

		if (negativePathFile.exists() && !FaultLocRepresentation.regenPaths) {
			negativePath = readPathFile(FaultLocRepresentation.negCoverageFile);
		} else {
			negativePath = createNegFile();	
		}

		computeFixSpace(negativePath, positivePath);
		computeFaultSpace(negativePath,positivePath); 

		//printout fault space with their weights
		PrintWriter writer = new PrintWriter("FaultyStmtsAndWeights.txt", "UTF-8");
		for (int i = 0; i < faultLocalization.size(); i++) {
			writer.println("Location:\n" + faultLocalization.get(i).getLeft() + "Weight:\n" + faultLocalization.get(i).getWeight() + "\n");
		}
		writer.close();

		assert (faultLocalization.size() > 0);
		assert (fixLocalization.size() > 0);
		this.doingCoverage = false;
		logger.info("Finish Fault Localization");
	}

	private TreeSet<Integer> createPosFile(){
		TreeSet<Integer> positivePath = null;
		switch(faultLocStrategy.trim()) {
		case "humanInjected": 
			//positivePath = new TreeSet<Integer>();
			//positivePath.add(faultLocFromFileHoleNumber);

		case "standardPathFile": 
		default:
			try {
				positivePath = runTestsCoverage(
						FaultLocRepresentation.posCoverageFile,
						Fitness.positiveTests, true, Configuration.outputDir + "/coverage/");
			} catch (IOException | UnexpectedCoverageResultException e) {
				e.printStackTrace();
			}
			break;
		}
		return positivePath;
	}

	private TreeSet<Integer> createNegFile(){
		TreeSet<Integer> negativePath = null;
		switch(faultLocStrategy.trim()) {
		case "humanInjected": 
			negativePath = transformFileWithLineNumbersToStmtNumbers(pathToFileHumanInjectedFaultLoc);
			break;
		case "standardPathFile": 
		default:
			try {
				negativePath = runTestsCoverage(
						FaultLocRepresentation.negCoverageFile,
						Fitness.negativeTests, false, Configuration.outputDir + "/coverage/");
			} catch (IOException | UnexpectedCoverageResultException e) {
				e.printStackTrace();
			}
			break;
		}
		return negativePath;
	}

	private TreeSet<Integer> transformFileWithLineNumbersToStmtNumbers(String pathOfFileWithFaultyLineNumbers){
		TreeSet<Integer> negativePath = new TreeSet<Integer>();
		Scanner reader = null;
		try {
			reader = new Scanner(new FileInputStream(pathOfFileWithFaultyLineNumbers));
			while (reader.hasNextLine()) {
				String line = reader.nextLine();
				if(!line.equalsIgnoreCase("")){
					String packageName = line.split(",")[0].trim();
					String className = line.split(",")[1].trim();
					//trim .java if the user types the class name with the extension
					className = className.contains(".")? className.split(".")[0] : className;
					String lineNumberString = line.split(",")[2].trim();
					int lineNumber = Integer.parseInt(lineNumberString);
					ArrayList<Integer> atomIds = this.atomIDofSourceLine(lineNumber);
					if(atomIds!=null){
						for(int atomId:atomIds){
							ClassInfo ci = this.getFileFromStmt(atomId);
							if(ci.getClassName().equalsIgnoreCase(className) && ci.getPackage().equalsIgnoreCase(packageName)){
								negativePath.addAll(atomIds);
							}
						}
					}
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			logger.error("coverage file " + pathOfFileWithFaultyLineNumbers + " not found");
			e.printStackTrace();
		} finally {
			if (reader != null)
				reader.close();
		}


		return negativePath;
	}

	protected void computeFaultSpace(TreeSet<Integer> negativePath, TreeSet<Integer> positivePath) {
		HashMap<Integer, Double> fw = new HashMap<Integer, Double>();
		TreeSet<Integer> negHt = new TreeSet<Integer>();
		TreeSet<Integer> posHt = new TreeSet<Integer>();

		for (Integer i : positivePath) {
			if(faultLocStrategy.trim().equalsIgnoreCase("standardPathFile")){
				fw.put(i, FaultLocRepresentation.positivePathWeight);
			}else if(faultLocStrategy.trim().equalsIgnoreCase("humanInjected")){
				fw.put(i, 0.0);
			}
		}

		for (Integer i : positivePath) {
			posHt.add(i);
			//	fw.put(i, 0.5);
		}
		for (Integer i : negativePath) {
			if (!negHt.contains(i)) {
				double negWeight = FaultLocRepresentation.negativePathWeight;
				if (posHt.contains(i)) {
					negWeight = FaultLocRepresentation.positivePathWeight;
				}
				negHt.add(i);
				//fw.put(i, 0.5);
				faultLocalization.add(this.instantiateLocation(i, negWeight)); 
			}
		}		
	}

	protected void computeFixSpace(TreeSet<Integer> negativePath, TreeSet<Integer> positivePath) {
		for (Integer i : positivePath) {
			fixLocalization.add(new WeightedAtom(i, FaultLocRepresentation.positivePathWeight));
		}
		for (Integer i : negativePath) {
			fixLocalization.add(new WeightedAtom(i, FaultLocRepresentation.positivePathWeight));

		}	
	}

	protected abstract Location instantiateLocation(Integer i, double negWeight);

	protected abstract void instrumentForFaultLocalization();

	@Override
	public void load(ArrayList<ClassInfo> bases) throws IOException {
		super.load(bases); // calling super so that the code is loaded and the
		// sanity check happens before localization is
		// computed
		try {
			this.computeLocalization();
			if(justTestingFaultLoc == true){
				logger.info("Fault localization was peprformed successfully");
				System.exit(0);
			}
		} catch (UnexpectedCoverageResultException e) {
			logger.error("FaultLocRep: UnexpectedCoverageResult");
			Runtime.getRuntime().exit(1);
		}
		// }
	}
}
