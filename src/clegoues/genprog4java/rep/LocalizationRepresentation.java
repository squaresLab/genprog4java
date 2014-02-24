package clegoues.genprog4java.rep;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;


import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.fitness.TestCase;
import clegoues.genprog4java.fitness.TestType;



public class LocalizationRepresentation extends JavaRepresentation {
	
	private HashMap<String,TreeSet<Integer>> testAtomMap = new HashMap<String,TreeSet<Integer>>();
	private TreeSet<String> interestingPositiveTests = new TreeSet<String>();

	private TreeSet<Integer> runTestsCoverage(String pathFile, TestType testT, ArrayList<String> tests, boolean expectedResult, String wd) throws IOException, UnexpectedCoverageResultException {
		TreeSet<Integer> atoms = new TreeSet<Integer>();
		for(String test : tests)  {
			File coverageRaw = new File("jacoco.exec"); // FIXME: likely a mistake to put this in this class 

			if(coverageRaw.exists())
			{
				coverageRaw.delete();
			}
			TestCase newTest = new TestCase(testT, test);


			if(this.testCase(newTest) != expectedResult && !FaultLocRepresentation.allowCoverageFail) {
				throw new UnexpectedCoverageResultException("FaultLocRep: unexpected coverage result: " + newTest.toString());
			}
			TreeSet<Integer> thisTestResult = this.getCoverageInfo();
			atoms.addAll(thisTestResult);
			testAtomMap.put(test, thisTestResult);
		}

		BufferedWriter out = new BufferedWriter(new FileWriter(new File(pathFile)));

		for(int atom : atoms)
		{
			out.write(""+atom+"\n");
		}

		out.flush();
		out.close();

		return atoms;
	}

	protected void computeLocalization() throws IOException, UnexpectedCoverageResultException {
		// this version of localization keeps track of which tests execute which atoms
		// and then computes which positive tests overlap with the negative tests
		// we use this later when getting input/output information for interesting methods/blocks
		// to avoid running tests unnecessarily.
		
		this.doingCoverage = true;

		// OK, we don't instrument Java programs, rather, use java library that computes coverage for us.
		// which means either instrumentFaultLocalization should still exist and change the commands used for test case execution
		// or we don't pretend this is trying to match OCaml exactly?
		this.instrumentForFaultLocalization();
		File covDir = new File("tmp/coverage/");
		if(!covDir.exists())
			covDir.mkdir();
		if(!this.compile("coverage", "coverage/coverage.out")) {
			System.err.println("faultLocRep: Coverage failed to compile");
			throw new UnexpectedCoverageResultException("compilation failure");
		}

		runTestsCoverage(FaultLocRepresentation.posCoverageFile, TestType.POSITIVE, Fitness.positiveTests, true, "coverage/"); 
		runTestsCoverage(FaultLocRepresentation.negCoverageFile, TestType.NEGATIVE, Fitness.negativeTests, false, "coverage/");
		TreeSet<Integer> allNegativeAtoms = new TreeSet<Integer>();
		for(String negativeTest : Fitness.negativeTests) {
			TreeSet<Integer> negativeAtoms = testAtomMap.get(negativeTest);
			allNegativeAtoms.addAll(negativeAtoms);
		}
		for(String positiveTest : Fitness.positiveTests) {
			TreeSet<Integer> atoms = testAtomMap.get(positiveTest);
			TreeSet<Integer> intersection = new TreeSet<Integer>(allNegativeAtoms);
			intersection.retainAll(atoms);
			if(!intersection.isEmpty()) {
				interestingPositiveTests.add(positiveTest);
			}
		}
		for(String interestingTest : interestingPositiveTests) {
			System.out.println(interestingTest);
		}
		super.computeLocalization();
		this.doingCoverage = false;
	}
}
