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

package clegoues.genprog4java.Fitness.Fitness;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import clegoues.genprog4java.mut.EditOperation;
import clegoues.genprog4java.rep.Representation;
import clegoues.genprog4java.util.GlobalUtils;
import clegoues.genprog4java.util.Pair;


public class Fitness<G extends EditOperation> {
	private static double negativeTestWeight = 2.0; 
	private static double sample = 1.0;
	private static String sampleStrategy = "variant"; // options: all, generation, variant
	public static String posTestFile = "pos.tests";
	public static String negTestFile = "neg.tests";
	public static ArrayList<String> positiveTests = new ArrayList<String>();
	public static ArrayList<String> negativeTests = new ArrayList<String>();
	public static int numPositiveTests = 5;
	public static int numNegativeTests = 1;

	public static void configure(Properties prop) {
		if(prop.getProperty("negativeTestWeight") != null) {
			Fitness.negativeTestWeight = Double.parseDouble(prop.getProperty("negativeTestWeight").trim());
		}
		if(prop.getProperty("sample") != null) {
			Fitness.sample = Double.parseDouble(prop.getProperty("sample").trim());
		}
		if(prop.getProperty("sampleStrategy") != null) {
			Fitness.sampleStrategy = prop.getProperty("sampleStrategy").trim();
		}
		if(prop.getProperty("positiveTests") != null)
		{
			posTestFile = prop.getProperty("positiveTests").trim();
		}

		if(prop.getProperty("negativeTests") != null)
		{
			negTestFile = prop.getProperty("negativeTests").trim();
		}

		try {
			positiveTests.addAll(getTests(posTestFile));
		} catch (IOException e) {
			System.err.println("failed to read " + posTestFile + " giving up");
			Runtime.getRuntime().exit(1);
		}
		try {
			negativeTests.addAll(getTests(negTestFile));
		} catch (IOException e) {
			System.err.println("failed to read " + negTestFile + " giving up");
			Runtime.getRuntime().exit(1);
		}
		if(prop.getProperty("pos-tests") != null) {
			Fitness.numPositiveTests = Integer.parseInt(prop.getProperty("pos-tests"));
		} else {
			Fitness.numPositiveTests = Fitness.positiveTests.size();
		}
		if(prop.getProperty("neg-tests") != null) {
			Fitness.numNegativeTests = Integer.parseInt(prop.getProperty("neg-tests"));
		} else {
			Fitness.numNegativeTests = Fitness.negativeTests.size();
		}
	}

	private static ArrayList<String> getTests(String filename) throws IOException {
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

	/* {b test_to_first_failure} variant returns true if the variant passes all
	    test cases and false otherwise; unlike other search strategies and as an
	    optimization for brute_force search, gives up on a variant as soon as it
	    fails a test case.  This makes less sense for single_fitness, but
	    single_fitness being true won't break it.  Does do sampling if specified. */

	public boolean testToFirstFailure(Representation<G> rep) {
		for( int i = 1; i <= Fitness.numNegativeTests; i++) {
			TestCase thisTest = new TestCase(TestType.NEGATIVE, i);
			if(!rep.testCase(thisTest)) {
				rep.cleanup();
				return false;
			}
		} 
		Long L = Math.round(sample * Fitness.numPositiveTests);
		int sampleSize = Integer.valueOf(L.intValue());

		ArrayList<Integer> allPositiveTests = GlobalUtils.range(1,Fitness.numPositiveTests);
		List<Integer> positiveSample;
		if(sampleSize == Fitness.numPositiveTests) {
			positiveSample = allPositiveTests;
		} else {
			long seed = System.nanoTime();
			Collections.shuffle(allPositiveTests, new Random(seed));
			positiveSample = allPositiveTests.subList(0,sampleSize);
		}
		for(Integer testNum : positiveSample) {
			TestCase thisTest = new TestCase(TestType.POSITIVE, testNum);
			if(!rep.testCase(thisTest)) {
				rep.cleanup();
				return false;
			}
		}
		if(Fitness.sample < 1.0) {
			List<Integer> restOfSample = allPositiveTests.subList(sampleSize+1, allPositiveTests.size());
			for(Integer testNum : restOfSample) {
				TestCase thisTest = new TestCase(TestType.POSITIVE, testNum);
				if(!rep.testCase(thisTest)) {
					rep.cleanup();
					return false;
				}
			}
		}

		return true;
	}

	private  Pair<Double,Double> testFitnessGeneration(Representation<G> rep, int generation) {
		throw new UnsupportedOperationException();
	}

	private  Pair<Double,Double> testFitnessVariant(Representation<G> rep) {
		throw new UnsupportedOperationException();
	}

	private Pair<Double,Double> testFitnessFull(Representation<G> rep, double fac) {
		double fitness = 0.0;
		for(String test :Fitness.positiveTests){
			TestCase thisTest = new TestCase(TestType.POSITIVE, test);
			if(rep.testCase(thisTest)) { 
				fitness += 1.0;
			}
		}
		for(String test : Fitness.negativeTests) {
			TestCase thisTest = new TestCase(TestType.NEGATIVE, test);
			if(rep.testCase(thisTest)) {
				fitness += fac;
			}
		}
		return new Pair<Double,Double>(fitness,fitness);
	}
	
	/* {b test_fitness} generation variant returns true if the variant passes all
	    test cases and false otherwise.  Only tests fitness if the rep has not
	    cached it.  Postcondition: records fitness in rep, calls rep#cleanup(). May
	    implement sampling strategies if specified by the command line.*/
	public boolean testFitness(int generation, Representation<G> rep) {

		/* Find the relative weight of positive and negative tests
		 * If negative_test_weight is 2 (the default), then the negative tests are
		 * worth twice as much, total, as the positive tests. This is the old
		 * ICSE'09 behavior, where there were 5 positives tests (worth 1 each) and
		 * 1 negative test (worth 10 points). 10:5 == 2:1. */
		double fac = Fitness.numPositiveTests * Fitness.negativeTestWeight / Fitness.numNegativeTests;

		double maxFitness = Fitness.numPositiveTests + ((Fitness.numNegativeTests * fac));
		double curFit = rep.getFitness();
		if(curFit > -1.0) {
			System.out.printf("\t%3g %s\n", curFit, rep.getName());
			return !(curFit < maxFitness);
		}

		Pair<Double,Double> fitnessPair = new Pair<Double,Double>(-1.0, -1.0); 
		if(Fitness.sample < 1.0) {
			if (Fitness.sampleStrategy == "generation") {
				fitnessPair = this.testFitnessGeneration(rep,generation);
			} else if (Fitness.sampleStrategy == "variant") {
				fitnessPair = this.testFitnessVariant(rep);
			} else {
				throw new UnsupportedOperationException("Fitness: Claire did not implement the \"all\" strategy, or you asked for something totally bonkers (requested sample strategy: " + Fitness.sampleStrategy + ")\n"); // not doing all right now because don't see a need for those experiments any time soon
			}
		} else {
			fitnessPair = this.testFitnessFull(rep, fac);
		}
		System.out.printf("\t%3g %s\n", fitnessPair.getFirst(), rep.getName());
		rep.setFitness(fitnessPair.getSecond());
		rep.cleanup();
		return !(fitnessPair.getSecond() < maxFitness);

	}
}
