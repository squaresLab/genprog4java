package clegoues.genprog4java.Fitness;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import clegoues.genprog4java.main.Configuration;
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
	}

	/* {b test_to_first_failure} variant returns true if the variant passes all
	    test cases and false otherwise; unlike other search strategies and as an
	    optimization for brute_force search, gives up on a variant as soon as it
	    fails a test case.  This makes less sense for single_fitness, but
	    single_fitness being true won't break it.  Does do sampling if specified. */

	public boolean testToFirstFailure(Representation<G> rep) {
		boolean retVal = true;
		try {
			for( int i = 1; i <= Configuration.numNegativeTests; i++) {
				TestCase thisTest = new TestCase(TestType.NEGATIVE, i);
				if(!rep.testCase(thisTest)) {
					throw new TestFailedException();
				}
			} 
			Long L = Math.round(sample * Configuration.numPositiveTests);
			int sampleSize = Integer.valueOf(L.intValue());

			ArrayList<Integer> allPositiveTests = GlobalUtils.range(1,Configuration.numPositiveTests);
			List<Integer> positiveSample;
			if(sampleSize == Configuration.numPositiveTests) {
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
			}
			if(Fitness.sample < 1.0) {
				List<Integer> restOfSample = allPositiveTests.subList(sampleSize+1, allPositiveTests.size());
				for(Integer testNum : restOfSample) {
					TestCase thisTest = new TestCase(TestType.POSITIVE, testNum);
					if(!rep.testCase(thisTest)) {
						throw new TestFailedException();
					}
				}
			}
		} catch (TestFailedException e) {
			retVal = false;
		} finally {
			rep.cleanup();
		}
		return retVal;
	}

	private  Pair<Double,Double> testFitnessGeneration(Representation<G> rep, int generation) {
		throw new UnsupportedOperationException();
	}

	private  Pair<Double,Double> testFitnessVariant(Representation<G> rep) {
		throw new UnsupportedOperationException();
	}

	private Pair<Double,Double> testFitnessFull(Representation<G> rep) {
		throw new UnsupportedOperationException();
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
		double fac = Configuration.numPositiveTests * Fitness.negativeTestWeight / Configuration.numNegativeTests;

		double maxFitness = Configuration.numPositiveTests + ((Configuration.numNegativeTests * fac));
		HashMap<String,FitnessValue> curFit = rep.getFitness();
		if(!curFit.isEmpty()) {
			Set<Entry<String,FitnessValue>> entrySet = curFit.entrySet();
			double totalFitness = 0.0; // FIXME the problem here is that the fitness entries don't know if they're positive or negative so the weighting will be wrong. 
			for(Entry<String,FitnessValue> fitnessEntry : entrySet) {
				if(fitnessEntry.getValue().isAllPassed()) {
					totalFitness += 1.0; 
				}
			}
			System.out.printf("\t%3g %s\n", totalFitness, rep.getName());

			return !(totalFitness < maxFitness);
		}

		Pair<Double,Double> fitnessPair = new Pair<Double,Double>(-1.0, -1.0); 
		if(Fitness.sample < 1.0) {
			if (Fitness.sampleStrategy == "generation") {
				fitnessPair = this.testFitnessGeneration(rep,generation);
			} else if (Fitness.sampleStrategy == "variant") {
				fitnessPair = this.testFitnessVariant(rep);
			} else {
				throw new UnsupportedOperationException(); // not doing all right now because don't see a need for those experiments any time soon
			}
		} else {
			fitnessPair = this.testFitnessFull(rep);
		}
		System.out.printf("\t%3g %s\n", fitnessPair.getFirst(), rep.getName());
		// OK I don't think I need to set fitness here like we do in OCaml b/c rep.testCase will store it
		rep.cleanup();
		return !(fitnessPair.getSecond() < maxFitness);

	}
}

/* from PAR: package edu.ust.hk.par.util.runner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.concurrent.Callable;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.log4j.Logger;

import edu.ust.hk.par.algorithm.GPProcessor;
import edu.ust.hk.par.data.Chromosome;
import edu.ust.hk.par.data.Constants;
import edu.ust.hk.par.data.Fitness;
import edu.ust.hk.par.util.AutoTester;

public class FitnessTask implements Callable<Chromosome>
{
	private static Logger logger = Logger.getLogger(FitnessTask.class);

	private Chromosome chromosome;
	private File workDIR = new File(Constants.workingDir);
	private String source;

	public FitnessTask(Chromosome o)
	{
		this.setChromosome(o);
	}

	public Chromosome getChromosome()
	{
		return chromosome;
	}

	public void setChromosome(Chromosome chromosome)
	{
		this.chromosome = chromosome;
	}

	public void obtainSource()
	{
		this.source = this.getChromosome().getSource();
	}

	@Override
	public Chromosome call() throws Exception
	{
		if(this.chromosome.isAlreadyEvaluated())
			return this.chromosome;

		boolean flag = AutoTester.compile(source, this.chromosome.getSeqNumber());

		if(flag == false)
		{
			Fitness ret = new Fitness();
			ret.setCompilable(false);
			this.chromosome.setFitness(ret);
			this.chromosome.setAlreadyEvaluated();
			//System.err.println("In fitness: \n"+this.chromosome.getGenes().toString());
			return this.chromosome;
		}


		// Positive tests
		CommandLine posCommand = CommandLine.parse(Constants.javaVM);
		posCommand.addArgument("-classpath");
		posCommand.addArgument( Constants.outputDir + File.separator + this.chromosome.getSeqNumber()
				+ System.getProperty("path.separator") + Constants.libs);

		posCommand.addArgument("-Xms128m");
		posCommand.addArgument("-Xmx256m");
		posCommand.addArgument("-client");
		//posCommand.addArgument("-Xshare:on");
		//posCommand.addArgument("-Xquickstart");

		posCommand.addArgument("edu.ust.hk.par.util.runner.UnitTestRunner");

		posCommand.addArgument(Constants.posTestFile);
		posCommand.addArgument(GPProcessor.SamplingTestFilter);


		// Negative tests
		CommandLine negCommand = CommandLine.parse(Constants.javaVM);
		negCommand.addArgument("-classpath");
		negCommand.addArgument( Constants.outputDir + File.separator + this.chromosome.getSeqNumber()
				+ System.getProperty("path.separator") + Constants.libs);

		negCommand.addArgument("-Xms128m");
		negCommand.addArgument("-Xmx256m");
		negCommand.addArgument("-client");
		//posCommand.addArgument("-Xshare:on");
		//negCommand.addArgument("-Xquickstart");

		negCommand.addArgument("edu.ust.hk.par.util.runner.UnitTestRunner");

		negCommand.addArgument(Constants.negTestFile);
		negCommand.addArgument(GPProcessor.AllTestFilter);


		ExecuteWatchdog watchdog = new ExecuteWatchdog(60*6000);
		DefaultExecutor posExecutor = new DefaultExecutor();
		DefaultExecutor negExecutor = new DefaultExecutor();
		posExecutor.setWorkingDirectory(workDIR);
		negExecutor.setWorkingDirectory(workDIR);

		posExecutor.setWatchdog(watchdog);
		negExecutor.setWatchdog(watchdog);

		ByteArrayOutputStream out = new ByteArrayOutputStream(); 

		posExecutor.setExitValue(0);
		negExecutor.setExitValue(0);

		posExecutor.setStreamHandler(new PumpStreamHandler(out));
		negExecutor.setStreamHandler(new PumpStreamHandler(out));


		try {
			int exitValue = posExecutor.execute(posCommand);		
			out.flush();
			String output = out.toString();
			out.reset();

			Fitness posFit = this.parseTestResults(output);

			try {
			  Thread.sleep(500);
			} catch (InterruptedException exception) {
				//ignore exception
			}

			exitValue = negExecutor.execute(negCommand);		
			out.flush();
			output = out.toString();

			Fitness negFit = this.parseTestResults(output);

			Fitness integrated = Fitness.sum(posFit, negFit);
			this.chromosome.setFitness(integrated);
			if(!integrated.isAllSuccessful())
			{
				this.chromosome.setAlreadyEvaluated();
			}
			else
			{
				if(GPProcessor.SamplingTestFilter.equals("edu.ust.hk.par.util.runner.filter.AllSamplingFilter"))
					this.chromosome.setAlreadyEvaluated();	
			}

			return this.chromosome;
		} catch (ExecuteException exception) {
			int exitValue = exception.getExitValue();
			String output = out.toString();
			logger.error("Process threw an exception and ended with " + exitValue);
			logger.error("Output:\n" + output);
			logger.error("Stack Trace:");
			logger.error("", exception);
			return null;
		} catch (Exception e)
		{
			String output = out.toString();
			logger.error("Unknown exception: " + e.getClass().toString());
			logger.error("Output:\n" + output);
			logger.error("Stack Trace:");
			logger.error("", e);
			return null;
		}
		finally
		{
			if(out!=null)
				out.close();
		}
	}

	private Fitness parseTestResults(String output)
	{
		String[] lines = output.split("\n");
		Fitness ret = new Fitness();
		ret.setCompilable(true);
		for(String line : lines)
		{
			try
			{
				if(line.startsWith("[SUCCESS]:"))
				{
					String[] tokens = line.split("[:\\s]+");
					ret.setAllSuccessful(Boolean.parseBoolean(tokens[1]));
				}
			} catch (Exception e)
			{
				ret.setAllSuccessful(false);
				ret.setCompilable(false);
			}

			try
			{
				if(line.startsWith("[TOTAL]:"))
				{
					String[] tokens = line.split("[:\\s]+");
					ret.setnTotalCases(Integer.parseInt(tokens[1]));
				}
			} catch (NumberFormatException e)
			{
				ret.setCompilable(false);
			}

			try
			{
				if(line.startsWith("[FAILURE]:"))
				{
					String[] tokens = line.split("[:\\s]+");
					ret.setnFailureCases(Integer.parseInt(tokens[1]));
				}
			} catch (NumberFormatException e)
			{
				ret.setCompilable(false);
			}
		}

		return ret;
	}
}*/