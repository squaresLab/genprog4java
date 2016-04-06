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

package clegoues.genprog4java.fitness;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.runner.Request;

import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.mut.EditOperation;
import clegoues.genprog4java.rep.Representation;
import clegoues.genprog4java.util.GlobalUtils;
import clegoues.genprog4java.util.Pair;

public class Fitness<G extends EditOperation> {
	protected static Logger logger = Logger.getLogger(Fitness.class);

	private static int generation = -1;
	// FIXME: we're already doing sampling, so note to self to kill it in the genprog setup scripts and just pass
	// the desired sample size to genprog via the config.
	private static List<Integer> testSample = GlobalUtils.range(1,Fitness.numPositiveTests); // FIXME: THIS IS WRONG
	private static List<Integer> restSample = null;

	private static double negativeTestWeight = 2.0;
	private static double sample = 1.0;
	private static String sampleStrategy = "variant"; // options: all,
	// generation, variant
	public static String posTestFile = "pos.tests";
	public static String negTestFile = "neg.tests";
	public static ArrayList<String> positiveTests = new ArrayList<String>();
	public static ArrayList<String> negativeTests = new ArrayList<String>();
	public static int numPositiveTests = 5;
	public static int numNegativeTests = 1;

	public static void configure(Properties prop) {
		if (prop.getProperty("negativeTestWeight") != null) {
			Fitness.negativeTestWeight = Double.parseDouble(prop.getProperty(
					"negativeTestWeight").trim());
		}
		if (prop.getProperty("sample") != null) {
			Fitness.sample = Double.parseDouble(prop.getProperty("sample")
					.trim());
		}
		if (prop.getProperty("sampleStrategy") != null) {
			Fitness.sampleStrategy = prop.getProperty("sampleStrategy").trim();
		}
		if (prop.getProperty("positiveTests") != null) {
			posTestFile = prop.getProperty("positiveTests").trim();
		}

		if (prop.getProperty("negativeTests") != null) {
			negTestFile = prop.getProperty("negativeTests").trim();
		}


		if (prop.getProperty("pos-tests") != null) { // FIXME: what is this supposed to do?
			Fitness.numPositiveTests = Integer.parseInt(prop
					.getProperty("pos-tests"));
		} else {
			Fitness.numPositiveTests = Fitness.positiveTests.size();
		}
		if (prop.getProperty("neg-tests") != null) {
			Fitness.numNegativeTests = Integer.parseInt(prop
					.getProperty("neg-tests"));
		} else {
			Fitness.numNegativeTests = Fitness.negativeTests.size();
		}

		Fitness.configureTests();
	}


	public static void configureTests() {
		ArrayList<String> intermedPosTests = null, intermedNegTests = null;

		intermedPosTests = getTests(posTestFile);
		intermedNegTests = getTests(negTestFile);

		filterTests(intermedPosTests, intermedNegTests);
		filterTests(intermedNegTests, intermedPosTests);
		positiveTests.addAll(intermedPosTests);
		negativeTests.addAll(intermedNegTests);
	}

	public static void filterTests(ArrayList<String> toFilter, ArrayList<String> filterBy) {
		HashSet<String> clazzesInFilterSet = new HashSet<String>();
		HashSet<String> removeFromFilterSet = new HashSet<String>();
		
		// stuff in negative tests, must remove class from positive test list and add non-negative tests to list
		for(String specifiedMethod : filterBy) {
			if(specifiedMethod.contains("::")) { 
				// remove from toFilter all tests that have this class name
				// remove from filterBy this particular entry and replace it with just the className
				String[] split = specifiedMethod.split("::");
				clazzesInFilterSet.add(split[0]);
				removeFromFilterSet.add(specifiedMethod);
			}
		}
		for(String removeFromFilterBy : removeFromFilterSet ) {
			filterBy.remove(removeFromFilterBy);
		}
		filterBy.addAll(clazzesInFilterSet);
		
		HashSet<String> removeFromFilteredSet = new HashSet<String>();
		for(String testNameInToFilter : toFilter ) {
			String clazzName = "";
			if(testNameInToFilter.contains("::")) {
				String[] split = testNameInToFilter.split("::");
				clazzName = split[0];
			} else {
				clazzName = testNameInToFilter;
			}
			if(clazzesInFilterSet.contains(clazzName)) {
				removeFromFilteredSet.add(testNameInToFilter);
			}
		}
		for(String removeFromFiltered : removeFromFilteredSet) {
			toFilter.remove(removeFromFiltered);
		}
	}

	private static ArrayList<String> getTests(String filename) {
		ArrayList<String> allLines = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line;
			allLines = new ArrayList<String>();
			while ((line = br.readLine()) != null) {
				allLines.add(line);
			}
			br.close();
		} catch(IOException e) {
			logger.error("failed to read " + filename + " giving up");
			Runtime.getRuntime().exit(1);
		}
		return allLines;
	}

	private int testPassCount(Representation<G> rep, boolean shortCircuit, TestType type, List<Integer> tests) {
		int numPassed = 0;
		for (Integer testNum : tests) {
			TestCase thisTest = new TestCase(type, testNum);
			if (!rep.testCase(thisTest)) {
				rep.cleanup();
				if(shortCircuit) {
					return numPassed;
				}
			} else {
				numPassed++;
			}
		}
		return numPassed;
	}

	/*
	 * {b test_to_first_failure} variant returns true if the variant passes all
	 * test cases and false otherwise; unlike other search strategies and as an
	 * optimization for brute_force search, gives up on a variant as soon as it
	 * fails a test case. This makes less sense for single_fitness, but
	 * single_fitness being true won't break it. Does do sampling if specified.
	 */

	public boolean testToFirstFailure(Representation<G> rep) {
		int numNegativePassed = this.testPassCount(rep, true, TestType.NEGATIVE, GlobalUtils.range(1, Fitness.numNegativeTests));
		if(numNegativePassed < Fitness.numNegativeTests) {
			return false;
		}

		int numPositivePassed = this.testPassCount(rep,  true, TestType.POSITIVE, GlobalUtils.range(1, Fitness.numPositiveTests));
		if(numPositivePassed < Fitness.numPositiveTests) {
			return false;
		}
		return true;
	}

	private static void resample() {
		ArrayList<Integer> allPositiveTests = GlobalUtils.range(1,
				Fitness.numPositiveTests);
		Long L = Math.round(sample * Fitness.numPositiveTests);
		int sampleSize = Integer.valueOf(L.intValue());
		Collections.shuffle(allPositiveTests, Configuration.randomizer);
		Fitness.testSample = allPositiveTests.subList(0,sampleSize-1); 
		Fitness.restSample = allPositiveTests.subList(sampleSize, allPositiveTests.size()-1);
	}

	private Pair<Double,Double> testFitnessSample(Representation<G> rep, double fac) {
		int numNegPassed = this.testPassCount(rep,false,TestType.NEGATIVE, GlobalUtils.range(1, Fitness.numNegativeTests));
		int numPosPassed = this.testPassCount(rep,false,TestType.POSITIVE, testSample);
		int numRestPassed = 0;
		if((numNegPassed == Fitness.numNegativeTests) &&
				(numPosPassed == testSample.size())) {
			if(Fitness.sample < 1.0) { // restSample won't be null by definition here
				numRestPassed = this.testPassCount(rep, false, TestType.POSITIVE, restSample);				
			}
		} 
		double sampleFitness = fac * numNegPassed + numPosPassed;
		double totalFitness = sampleFitness + numRestPassed;
		return new Pair<Double,Double>(totalFitness,sampleFitness);
	}

	private Pair<Double, Double> testFitnessFull(Representation<G> rep,
			double fac) {
		double fitness = 0.0;
		for (String test : Fitness.positiveTests) {
			TestCase thisTest = new TestCase(TestType.POSITIVE, test);
			if (rep.testCase(thisTest)) {
				fitness += 1.0;
			}
		}
		for (String test : Fitness.negativeTests) {
			TestCase thisTest = new TestCase(TestType.NEGATIVE, test);
			if (rep.testCase(thisTest)) {
				fitness += fac;
			}
		}
		return new Pair<Double, Double>(fitness, fitness);
	}

	/*
	 * {b test_fitness} generation variant returns true if the variant passes
	 * all test cases and false otherwise. Only tests fitness if the rep has not
	 * cached it. Postcondition: records fitness in rep, calls rep#cleanup().
	 * May implement sampling strategies if specified by the command line.
	 */
	public boolean testFitness(int generation, Representation<G> rep) {

		/*
		 * Find the relative weight of positive and negative tests If
		 * negative_test_weight is 2 (the default), then the negative tests are
		 * worth twice as much, total, as the positive tests. This is the old
		 * ICSE'09 behavior, where there were 5 positives tests (worth 1 each)
		 * and 1 negative test (worth 10 points). 10:5 == 2:1.
		 */
		double fac = Fitness.numPositiveTests * Fitness.negativeTestWeight
				/ Fitness.numNegativeTests;

		double maxFitness = Fitness.numPositiveTests
				+ ((Fitness.numNegativeTests * fac));
		double curFit = rep.getFitness();
		if (curFit > -1.0) {
			logger.info("\t gen: " + generation + " " + curFit + " " + rep.getName() + " (stored at: " + rep.getVariantFolder() + ")");
			return !(curFit < maxFitness);
		}

		Pair<Double, Double> fitnessPair = new Pair<Double, Double>(-1.0, -1.0);
		if (Fitness.sample < 1.0) {
			if (((Fitness.sampleStrategy == "generation") && (Fitness.generation != generation)) ||
					(Fitness.sampleStrategy == "variant")) {
				generation = Fitness.generation;
				Fitness.resample();
			} 	
			fitnessPair = this.testFitnessSample(rep, fac);
		} else {
			fitnessPair = this.testFitnessFull(rep, fac);
		}
		logger.info("\t gen: " + generation + " " + fitnessPair.getFirst() + " " + rep.getName());
		rep.setFitness(fitnessPair.getSecond());
		rep.cleanup();
		return !(fitnessPair.getSecond() < maxFitness);

	}
}