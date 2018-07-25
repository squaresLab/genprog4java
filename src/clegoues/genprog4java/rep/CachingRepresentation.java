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
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import clegoues.genprog4java.Search.GiveUpException;
import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.fitness.FitnessValue;
import clegoues.genprog4java.fitness.TestCase;
import clegoues.genprog4java.java.ClassInfo;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.mut.EditOperation;
import clegoues.util.ConfigurationBuilder;
import ylyu1.wean.DataProcessor;

@SuppressWarnings("rawtypes")
public abstract class CachingRepresentation<G extends EditOperation> extends
Representation<G>  {

	protected transient Logger logger = Logger.getLogger(CachingRepresentation.class);

	public transient static final ConfigurationBuilder.RegistryToken token =
			ConfigurationBuilder.getToken();

	public static boolean skipFailedSanity = ConfigurationBuilder.of( BOOL_ARG )
			.withVarName( "skipFailedSanity" )
			.withDefault( "true" )
			.withHelp( "do not include positive tests if they fail sanity" )
			.inGroup( "CachingRepresentation Parameters" )
			.build();

	public static String sanityFilename = "repair.sanity";
	public static String sanityExename = "repair.sanity";

	private double fitness = -1.0;

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

	private Pair<Boolean, String> alreadyCompiled = null;
	
	public Pair<Boolean, String> getAlreadyCompiled()
	{
		return alreadyCompiled;
	}

	public boolean getVariableLength() {
		return true;
	}

	public void load(ArrayList<ClassInfo> bases) throws IOException {
		// FIXME: deserialize properly
		for (ClassInfo base : bases) {
			this.fromSource(base);
			logger.info("loaded from source " + base);
		}
		if (Configuration.doSanity) {
			if (!this.sanityCheck()) {
				logger.error("Sanity check failed, giving up");
				DataProcessor.storeError("sanity");
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
					CachingRepresentation.sanityFilename, posTest, false);
			if (!res.isAllPassed()) {
				testsOutOfScope++;
				logger.info(testsOutOfScope + " tests out of scope so far, out of " + Fitness.positiveTests.size());
				logger.info("false (0)\n");
				logger.error("cacheRep: sanity: "
						+ CachingRepresentation.sanityFilename
						+ " failed positive test " + posTest.getTestName());
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
					CachingRepresentation.sanityFilename, negTest, false);
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


	@Override
	public FitnessValue testCase(TestCase test, boolean doingCoverage) {
		if (this.alreadyCompiled == null) {
			String newName = CachingRepresentation.newVariantFolder();
			this.variantFolder = newName;
			if (!this.compile(newName, newName)) {
				this.setFitness(0.0);
				logger.info(this.getName() + " at " + newName + " fails to compile\n");
				FitnessValue compileFail = new FitnessValue();
				compileFail.setTestClassName(test.getTestName());
				compileFail.setAllPassed(false);
				return compileFail;
			}
		} else if (!this.alreadyCompiled.getLeft()) {
			FitnessValue compileFail = new FitnessValue();
			compileFail.setTestClassName(test.getTestName());
			compileFail.setAllPassed(false);
			this.setFitness(0.0);
			return compileFail;
		}
		return this.internalTestCase(this.variantFolder,
				this.variantFolder + Configuration.globalExtension, test, doingCoverage);
	}

	public FitnessValue testCase(TestCase test) {
		return this.testCase(test,false);

	}


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
			String sanityFilename, TestCase thisTest, boolean doingCoverage) {

		CommandLine command = this.internalTestCaseCommand(sanityExename,
				sanityFilename, thisTest, doingCoverage);
		// System.out.println("command: " + command.toString());
		ExecuteWatchdog watchdog = new ExecuteWatchdog(96000);
		DefaultExecutor executor = new DefaultExecutor();
		String workingDirectory = System.getProperty("user.dir");
		executor.setWorkingDirectory(new File(workingDirectory));
		executor.setWatchdog(watchdog);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		executor.setExitValue(0);

		executor.setStreamHandler(new PumpStreamHandler(out));
		FitnessValue posFit = new FitnessValue();

		try {
			executor.execute(command);
			out.flush();
			String output = out.toString();
			out.reset();
			posFit = CachingRepresentation.parseTestResults(
					thisTest.getTestName(), output);

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
		// TODO: remove source code from disk?
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
			return alreadyCompiled.getLeft();
		} else {
			boolean result = this.internalCompile(sourceName, exeName);
			this.alreadyCompiled =  Pair.of(result, exeName);
			return result;
		}
	}

	protected abstract boolean internalCompile(String sourceName, String exeName);

	/*
	 * indicates that cached information based on our AST structure is no longer
	 * valid
	 */
	void updated() {
		alreadySourceBuffers = null;
		alreadyCompiled = null;
		fitness = -1.0;
		myHashCode = -1;
	}

	private void writeObject(java.io.ObjectOutputStream out)
			throws IOException {

	}
}