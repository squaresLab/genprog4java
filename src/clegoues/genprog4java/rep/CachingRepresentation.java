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

import static clegoues.util.ConfigurationBuilder.BOOL_ARG;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.log4j.Logger;

import clegoues.genprog4java.Search.GiveUpException;
import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.fitness.FitnessValue;
import clegoues.genprog4java.fitness.TestCase;
import clegoues.genprog4java.java.ClassInfo;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.mut.EditOperation;
import clegoues.util.ConfigurationBuilder;
import clegoues.util.Pair;

@SuppressWarnings("rawtypes")
public abstract class CachingRepresentation<G extends EditOperation> extends
Representation<G> {
	protected Logger logger = Logger.getLogger(CachingRepresentation.class);

	public static final ConfigurationBuilder.RegistryToken token =
		ConfigurationBuilder.getToken();
	
	//public static boolean skipFailedSanity = true;
	public static boolean skipFailedSanity = ConfigurationBuilder.of( BOOL_ARG )
		.withVarName( "skipFailedSanity" )
		.withDefault( "true" )
		.withHelp( "do not include positive tests if they fail sanity" )
		.inGroup( "CachingRepresentation Parameters" )
		.build();
	
	public static String sanityFilename = "repair.sanity";
	public static String sanityExename = "repair.sanity";
	
	private double fitness = -1.0;

	/*
	 * cached file contents from [internal_compute_source_buffers]; avoid
	 * recomputing/reserializing
	 */
	public ArrayList<Pair<ClassInfo, String>> alreadySourceBuffers = null;

	public static int sequence = 0;

	public CachingRepresentation(ArrayList<G> genome2) {
		super(genome2);
	}

	public CachingRepresentation() {
		super();
	}

	public static String newVariantFolder() {
		String result = String.format("variant%d", sequence);
		sequence++;
		return result;
	}

	@Override
	public double getFitness() {
		return this.fitness;
	}

	private ArrayList<String> alreadySourced = new ArrayList<String>();
	private Pair<Boolean, String> alreadyCompiled = null;

	public boolean getVariableLength() {
		return true;
	}

	public void load(ArrayList<ClassInfo> bases) throws IOException {
		
		// FIXME: do deserializing String cacheName = base + ".cache";
		// boolean didDeserialize = this.deserialize(cacheName,null, true);
		// if(!didDeserialize) {
		// this.serialize(cacheName, null, true);
		for (ClassInfo base : bases) {
			this.fromSource(base);
			logger.info("loaded from source " + base);
		}
		if (Configuration.doSanity) {
			if (!this.sanityCheck()) {
				logger.error("Sanity check failed, giving up");
				Runtime.getRuntime().exit(1);
			}
		}
	}

	// have omitted serialize/deserialize at this representation implementation
	// level
	// because I haven't done the version thing, which is the only thing the
	// ocaml version of
	// this representation implementation does

	public boolean sanityCheck() {
		long startTime = System.currentTimeMillis();

		this.outputSource(CachingRepresentation.sanityFilename);
		logger.info("sanity checking begins");
		if (!this.compile(CachingRepresentation.sanityFilename,
				CachingRepresentation.sanityExename)) {
			logger.error("sanity: " + CachingRepresentation.sanityFilename
					+ " does not compile.");
			return false;
		}
		int testNum = 1;

		ArrayList<TestCase> passingTests = new ArrayList<TestCase>();
		// make list of passing files (sanitizing out of scope tests)
		int testsOutOfScope = 0;
		int testNumber = 0;
		for (TestCase posTest : Fitness.positiveTests) {
			testNumber++;
			logger.info("Checking test number " + testNumber + " out of " + Fitness.positiveTests.size());
			FitnessValue res = this.internalTestCase(
					CachingRepresentation.sanityExename,
					CachingRepresentation.sanityFilename, posTest);
			if (!res.isAllPassed()) {
				testsOutOfScope++;
				logger.info(testsOutOfScope + " tests out of scope so far, out of " + Fitness.positiveTests.size());
				logger.info("false (0)\n");
				logger.error("cacheRep: sanity: "
						+ CachingRepresentation.sanityFilename
						+ " failed positive test " + posTest.toString());
				if (!skipFailedSanity) {
					return false;
				}
			} else {
				passingTests.add(posTest);
			}
			logger.info("true (1)\n");
			testNum++;
		}
		Fitness.positiveTests = passingTests;
		testNum = 1;
		if (passingTests.size() < 1) {
			logger.error("no positive tests pass.");
			return false;
		}

		//print to a file only the tests in scope
		Fitness.printTestsInScope(passingTests);

		testNum = 1;
		for (TestCase negTest : Fitness.negativeTests) {
			logger.info("\tn" + testNum + ": ");
			FitnessValue res = this.internalTestCase(
					CachingRepresentation.sanityExename,
					CachingRepresentation.sanityFilename, negTest);
			if (res.isAllPassed()) {
				logger.info("true (1)\n");
				logger.error("cacheRep: sanity: "
						+ CachingRepresentation.sanityFilename
						+ " passed negative test " + negTest.toString());
				return false;
			}
			logger.info("false (0)\n");
			testNum++;
		}
		this.cleanup();
		this.updated();
		logger.info("sanity checking completed (time taken = "
				+ (System.currentTimeMillis() - startTime) + ")");
		return true;
	}

	public boolean testCase(TestCase test) {
		List<Integer> hash = astHash();
		HashMap<String, FitnessValue> thisVariantsFitness = null;
		if(cacheflag == true){ 
			getFitnessCache().putAll(desearializeTestCache());
			cacheflag = false;
		}
		if (getFitnessCache().containsKey(hash)) {
			thisVariantsFitness = getFitnessCache().get(hash);
			if (thisVariantsFitness.containsKey(test.toString()))
				return thisVariantsFitness.get(test.toString()).isAllPassed();
		} else {
			thisVariantsFitness = new HashMap<String, FitnessValue>();
			getFitnessCache().put(hash, thisVariantsFitness);
		}
		 		
		if (this.alreadyCompiled == null) {
			String newName = CachingRepresentation.newVariantFolder();
			this.variantFolder = newName;
		//	logger.info("History of variant " + getVariantFolder() + " is: " + getHistory());
			if (!this.compile(newName, newName)) {
				this.setFitness(0.0);
				logger.info(this.getName() + " at " + newName + " fails to compile\n");
				return false;
			}
		} else if (!this.alreadyCompiled.getFirst()) {
			FitnessValue compileFail = new FitnessValue();
			compileFail.setTestClassName(test.toString());
			compileFail.setAllPassed(false);
			thisVariantsFitness.put(test.toString(), compileFail);
			// this WILL update it in the fitness cache, right, because state?
			this.setFitness(0.0);
			return false;
		}
		FitnessValue fitness = this.internalTestCase(this.variantFolder,
				this.variantFolder + Configuration.globalExtension, test);
		thisVariantsFitness.put(test.toString(), fitness);
		return fitness.isAllPassed();
	}

	// kind of think internal test case should return here to save in
	// fitnessTable,
	// but wtfever for now

	// compile assumes that the source has already been serialized to disk.

	// I think for here, it's best to put it down in Java representation

	// FIXME: OK, in OCaml there's an outputSource declaration here that assumes
	// that
	// the way we output code is to compute the source buffers AS STRINGS and
	// then print out one per file.
	// it's possible this is the same in Java, but unlikely, so I'm going to not
	// implement this here yet
	// and figure out how java files are manipulated first
	// it would be nice if this, as the caching representation superclass,
	// cached the "already sourced" info somehow, as with compile below
	/*
	 * void outputSource(String filename) { List<Pair<String,String>>
	 * sourceBuffers = this.computeSourceBuffers(); for(Pair<String,String>
	 * element : sourceBuffers) { String sourcename = element.getFirst(); String
	 * outBuffer = element.getSecond; // output to disk } // alreadySourced :=
	 * Some(lmap (fun (sname,_) -> sname) many_files); }
	 */

	@Override
	protected List<Pair<ClassInfo, String>> computeSourceBuffers() {
		if (this.alreadySourceBuffers != null) {
			return this.alreadySourceBuffers;
		} else {
			this.alreadySourceBuffers = this.internalComputeSourceBuffers();
			return this.alreadySourceBuffers;
		}
	}

	private static FitnessValue parseTestResults(String testClassName,
			String output) {
		String[] lines = output.split("\n");
		FitnessValue ret = new FitnessValue();
		ret.setTestClassName(testClassName);
		for (String line : lines) {
			try {
				if (line.startsWith("[SUCCESS]:")) {
					String[] tokens = line.split("[:\\s]+");
					ret.setAllPassed(Boolean.parseBoolean(tokens[1]));
				}
			} catch (Exception e) {
				ret.setAllPassed(false);
			}

			try {
				if (line.startsWith("[TOTAL]:")) {
					String[] tokens = line.split("[:\\s]+");
					ret.setNumberTests(Integer.parseInt(tokens[1]));
				}
			} catch (NumberFormatException e) {
			}

			try {
				if (line.startsWith("[FAILURE]:")) {
					String[] tokens = line.split("[:\\s]+");
					ret.setNumTestsFailed(Integer.parseInt(tokens[1]));
				}
			} catch (NumberFormatException e) {
			}
		}

		return ret;
	}

	protected abstract ArrayList<Pair<ClassInfo, String>> internalComputeSourceBuffers();

	protected FitnessValue internalTestCase(String sanityExename,
			String sanityFilename, TestCase thisTest) {
		// FIXME: the filename is wrong, here; it's looking based on the name of the edits incorporated, not the variant folder name, as it should be
		
		CommandLine command = this.internalTestCaseCommand(sanityExename,
				sanityFilename, thisTest);
		// System.out.println("command: " + command.toString());
		ExecuteWatchdog watchdog = new ExecuteWatchdog(96000);
		DefaultExecutor executor = new DefaultExecutor();
		String workingDirectory = System.getProperty("user.dir");
		executor.setWorkingDirectory(new File(workingDirectory));
		executor.setWatchdog(watchdog);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		// FIXME: the problem is it's not finding the jacocagent because it's at ./lib, not at /path/to/lib
		executor.setExitValue(0);

		executor.setStreamHandler(new PumpStreamHandler(out));
		FitnessValue posFit = new FitnessValue();

		try {
			executor.execute(command);
			out.flush();
			String output = out.toString();
			out.reset();
			posFit = CachingRepresentation.parseTestResults(
					thisTest.toString(), output);

		} catch (ExecuteException exception) {
			posFit.setAllPassed(false);
		} catch (Exception e) {
		} finally {
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
					// you know, having to either catch or throw
					// all exceptions is really tedious.
				}
		}
		return posFit;
	}

	public void cleanup() {
		// TODO: remove source code from disk
		// TODO: remove compiled binary from disk
		// TODO: remove applicable subdirectories from disk
	}

	@Override
	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	// while the OCaml implementation does compile in CachingRepresentation
	// assuming that it's always a call to an external script, I'm leaving that
	// off from here for the
	// time being and just doing the caching, which makes sense anyway

	public boolean compile(String sourceName, String exeName) {

		if (this.alreadyCompiled != null) {
			return alreadyCompiled.getFirst();
		} else {
			boolean result = this.internalCompile(sourceName, exeName);
			this.alreadyCompiled = new Pair<Boolean, String>(result, exeName);
			return result;
		}
	}

	protected abstract boolean internalCompile(String sourceName, String exeName);

	private List<Integer> astHashCode = null;

	protected List<Integer> astHash() {
		if (astHashCode != null)
			return astHashCode;
		astHashCode = new ArrayList<Integer>();
		List<Pair<ClassInfo, String>> sourceBuffers = computeSourceBuffers();
		for (Pair<ClassInfo, String> ele : sourceBuffers) {
			String code = ele.getSecond();
			astHashCode.add(code.hashCode());
		}
		return astHashCode;
	}

	/*
	 * indicates that cached information based on our AST structure is no longer
	 * valid
	 */
	void updated() {
		alreadySourceBuffers = null;
		alreadySourced = new ArrayList<String>();
		alreadyCompiled = null;
		fitness = -1.0;
		astHashCode = null;
	}

	public void reduceSearchSpace() throws GiveUpException {
	} // subclasses can override as desired

	public void reduceFixSpace() {

	}

	protected abstract CommandLine internalTestCaseCommand(String exeName,
			String fileName, TestCase test);

}