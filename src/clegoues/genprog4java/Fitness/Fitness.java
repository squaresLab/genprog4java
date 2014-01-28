package clegoues.genprog4java.Fitness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import clegoues.genprog4java.main.Main;
import clegoues.genprog4java.rep.Representation;
import clegoues.genprog4java.util.GlobalUtils;
import clegoues.genprog4java.util.Pair;


public class Fitness<G,C> {
	private double negativeTestWeight = 2.0; 
	private double sample = 1.0;
	private String sampleStrategy = "variant"; // options: all, generation, variant

	public Fitness() { }
	public Fitness(double negativeWeight, double sample, String sampleStrategy) {
		this.negativeTestWeight = negativeWeight;
		this.sample = sample;
		this.sampleStrategy = sampleStrategy;

	}

	/* {b test_to_first_failure} variant returns true if the variant passes all
	    test cases and false otherwise; unlike other search strategies and as an
	    optimization for brute_force search, gives up on a variant as soon as it
	    fails a test case.  This makes less sense for single_fitness, but
	    single_fitness being true won't break it.  Does do sampling if specified. */

	public  boolean testToFirstFailure(Representation<G,C> rep) {
		int count=0;
		boolean retVal = true;
		try {
			for( int i = 1; i <= Main.config.getNumNegativeTests(); i++) {
				TestCase thisTest = new TestCase(TestType.NEGATIVE, i);
				if(!rep.testCase(thisTest)) {
					throw new TestFailedException();
				}
				count++;
			} 
			Long L = Math.round(sample * Main.config.getNumPositiveTests());
			int sampleSize = Integer.valueOf(L.intValue());

			ArrayList<Integer> allPositiveTests = GlobalUtils.range(1,Main.config.getNumPositiveTests());
			List<Integer> positiveSample;
			if(sampleSize == Main.config.getNumPositiveTests()) {
				positiveSample = allPositiveTests;
			} else {
				long seed = System.nanoTime();
				Collections.shuffle(allPositiveTests, new Random(seed));
				positiveSample = allPositiveTests.subList(0,sampleSize);
			}
			for(Integer testNum : positiveSample) {
				TestCase thisTest = new TestCase(TestType.POSITIVE, testNum);
				if(!rep.testCase(thisTest)) {
					throw new TestFailedException();
				}
				count ++;	
			}
			if(this.sample < 1.0) {
				List<Integer> restOfSample = allPositiveTests.subList(sampleSize+1, allPositiveTests.size());
				for(Integer testNum : restOfSample) {
					TestCase thisTest = new TestCase(TestType.POSITIVE, testNum);
					if(!rep.testCase(thisTest)) {
						throw new TestFailedException();
					}
					count ++;
				}
			}
		} catch (TestFailedException e) {
			retVal = false;
		} finally {
			rep.cleanup();
		}
		return retVal;
	}

	private Pair<Double,Double> testFitnessGeneration(Representation<G,C> rep, int generation) {
		throw new UnsupportedOperationException();
	}

	private Pair<Double,Double> testFitnessVariant(Representation<G,C> rep) {
		throw new UnsupportedOperationException();
	}

	private Pair<Double,Double> testFitnessFull(Representation<G,C> rep) {
		throw new UnsupportedOperationException();
	}
	/* {b test_fitness} generation variant returns true if the variant passes all
	    test cases and false otherwise.  Only tests fitness if the rep has not
	    cached it.  Postcondition: records fitness in rep, calls rep#cleanup(). May
	    implement sampling strategies if specified by the command line.*/
	public boolean testFitness(int generation, Representation<G,C> rep) {

		/* Find the relative weight of positive and negative tests
		 * If negative_test_weight is 2 (the default), then the negative tests are
		 * worth twice as much, total, as the positive tests. This is the old
		 * ICSE'09 behavior, where there were 5 positives tests (worth 1 each) and
		 * 1 negative test (worth 10 points). 10:5 == 2:1. */
		double fac = Main.config.getNumPositiveTests() * this.negativeTestWeight / Main.config.getNumNegativeTests();

		// possible TODO: make num positive and num negative tests configuration flags for this class, not the 
		// main config?

		double maxFitness = Main.config.getNumPositiveTests() + ((Main.config.getNumNegativeTests() * fac));
		if(rep.fitnessIsValid()) {
			System.out.printf("\t%3g %s\n", rep.getFitness(), rep.getName());
			return !(rep.getFitness() < maxFitness);
		}

		Pair<Double,Double> fitnessPair = new Pair<Double,Double>(-1.0, -1.0); 
		if(this.sample < 1.0) {
			if (this.sampleStrategy == "generation") {
				fitnessPair = this.testFitnessGeneration(rep,generation);
			} else if (this.sampleStrategy == "variant") {
				fitnessPair = this.testFitnessVariant(rep);
			} else {
				throw new UnsupportedOperationException(); // not doing all right now because don't see a need for those experiments any time soon
			}
		} else {
			fitnessPair = this.testFitnessFull(rep);
		}
		System.out.printf("\t%3g %s\n", fitnessPair.getFirst(), rep.getName());
		rep.setFitness(fitnessPair.getFirst());
		rep.cleanup();
		return !(fitnessPair.getSecond() < maxFitness);

	}
}