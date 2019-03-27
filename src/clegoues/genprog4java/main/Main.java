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

package clegoues.genprog4java.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import clegoues.genprog4java.Search.BruteForce;
import clegoues.genprog4java.Search.GeneticProgramming;
import clegoues.genprog4java.Search.NSGAII;
import clegoues.genprog4java.Search.OracleSearch;
import clegoues.genprog4java.Search.Population;
import clegoues.genprog4java.Search.RandomSingleEdit;
import clegoues.genprog4java.Search.Search;
import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.fitness.TestCase;
import clegoues.genprog4java.fitness.TestCase.TestType;
import clegoues.genprog4java.fitness.Objective;
import clegoues.genprog4java.fitness.PositiveTestCasesObjective;
import clegoues.genprog4java.fitness.NegativeTestCasesObjective;
import clegoues.genprog4java.fitness.InvariantDiversityObjective;
import clegoues.genprog4java.localization.DefaultLocalization;
import clegoues.genprog4java.localization.Localization;
import clegoues.genprog4java.localization.UnexpectedCoverageResultException;
import clegoues.genprog4java.mut.edits.java.JavaEditOperation;
import clegoues.genprog4java.rep.CachingRepresentation;
import clegoues.genprog4java.rep.JavaRepresentation;
import clegoues.genprog4java.rep.Representation;
import clegoues.util.ConfigurationBuilder;
import ylyu1.wean.AbstractDataProcessor;
import ylyu1.wean.GPDataProcessor;
import ylyu1.wean.NSGAIIDataProcessor;

public class Main {

	protected static Logger logger = Logger.getLogger(Main.class);
	public static String GP4J_HOME = null; //guaranteed to NOT have a slash at the end
	public static String JAVA8_HOME = null; //guaranteed to NOT have a slash at the end
	public static String DAIKON_HOME = null; //guaranteed to NOT have a slash at the end
	public static String JUNIT_AND_HAMCREST_PATH = null;

	/**
	 * Change from the original main method:
	 * the 0th argument is where genprog4java is installed
	 * the 1st argument is the directory where the java 8 installation is located
	 * the 2nd argument is where daikon is installed
	 * All other arguments are now shifted to the right by one position.
	 * @param args
	 * @throws IOException
	 * @throws UnexpectedCoverageResultException
	 */
	public static void main(String[] args) throws IOException,
	UnexpectedCoverageResultException {
		Search searchEngine = null;
		Representation baseRep = null;
		Fitness fitnessEngine = null;
		Population incomingPopulation = null;
		assert (args.length > 3); //changed from args.length > 0, as now there exists 3 new arguments
		long startTime = System.currentTimeMillis();
		BasicConfigurator.configure();
		
		GP4J_HOME = args[0];
		//if there's a slash at the end of GP4J_HOME, get rid of it
		if(GP4J_HOME.endsWith(File.separator))
			GP4J_HOME = GP4J_HOME.substring(0, GP4J_HOME.length()-1);
		JAVA8_HOME = args[1];
		if(JAVA8_HOME.endsWith(File.separator))
			JAVA8_HOME = JAVA8_HOME.substring(0, JAVA8_HOME.length()-1);
		DAIKON_HOME = args[2];
		if(DAIKON_HOME.endsWith(File.separator))
			DAIKON_HOME = DAIKON_HOME.substring(0, DAIKON_HOME.length()-1);
		
		JUNIT_AND_HAMCREST_PATH = GP4J_HOME+"/lib/junit-4.12.jar" + ":" + GP4J_HOME+"/lib/hamcrest-core-1.3.jar";
		
		String[] origArgs = Arrays.<String>copyOfRange(args, 3, args.length);

		ConfigurationBuilder.register( Configuration.token );
		ConfigurationBuilder.register( Fitness.token );
		ConfigurationBuilder.register( CachingRepresentation.token );
		ConfigurationBuilder.register( JavaRepresentation.token );
		ConfigurationBuilder.register( Population.token );
		ConfigurationBuilder.register( Search.token );
		ConfigurationBuilder.register( OracleSearch.token );
		ConfigurationBuilder.register( RandomSingleEdit.token );
		ConfigurationBuilder.register( DefaultLocalization.token );

		ConfigurationBuilder.parseArgs( origArgs );
		Configuration.saveOrLoadTargetFiles();
		ConfigurationBuilder.storeProperties();
		
		if(Configuration.invariantCheckerMode == 4)
		{
			Search.searchStrategy = "nsgaii-diversity";
		}
		
		if(Configuration.invariantCheckerMode == 5)
		{
			Search.searchStrategy = "nsgaii-tests-only";
		}

		File workDir = new File(Configuration.outputDir);
		if (!workDir.exists())
			workDir.mkdir();
		logger.info("Configuration file loaded");
		
		if (Configuration.positiveTestClassesDaikonSample == "")
		{
			System.err.println("Daikon will run on all of the positive tests!");
			Fitness.positiveTestsDaikonSample = Fitness.positiveTests;
		}
		else
		{
			List<String> allModifiedTestClasses = Files.readAllLines(Paths.get(Configuration.positiveTestClassesDaikonSample), Charset.defaultCharset());
			for(String modTestClass : allModifiedTestClasses)
			{
				Fitness.positiveTestsDaikonSample.add(new TestCase(TestType.POSITIVE, modTestClass)); //modified test classes contain only positive tests, since the negative tests are removed from them during preprocessing
			}
		}

		fitnessEngine = new Fitness();  // Fitness must be created before rep!
		baseRep = (Representation) new JavaRepresentation();
		baseRep.load(Configuration.targetClassNames);
		Localization localization = new DefaultLocalization(baseRep);
		baseRep.setLocalization(localization);
		baseRep.setVariantID("baseRepVariantID"); //included for debugging reasons
		
		//ylyu1.morewood.MethodTracker.printmcov();
		
		AbstractDataProcessor dp = null;
		
		switch(Search.searchStrategy.trim()) {

		case "brute": searchEngine = new BruteForce<JavaEditOperation>(fitnessEngine);
		break;
		case "trp": 
			dp = new GPDataProcessor();
			searchEngine = new RandomSingleEdit<JavaEditOperation>(fitnessEngine, dp);
		break;
		case "oracle": searchEngine = new OracleSearch<JavaEditOperation>(fitnessEngine);
		break;
		case "nsgaii":
		case "nsgaii-diversity":
			dp = new NSGAIIDataProcessor();
			searchEngine = new NSGAII<JavaEditOperation>(fitnessEngine, new Objective[]{
					new PositiveTestCasesObjective(),
					new NegativeTestCasesObjective(),
					new InvariantDiversityObjective()
			}, (NSGAIIDataProcessor) dp);
			break;
		case "nsgaii-tests-only":
			dp = new NSGAIIDataProcessor();
			searchEngine = new NSGAII<JavaEditOperation>(fitnessEngine, new Objective[]{
					new PositiveTestCasesObjective(),
					new NegativeTestCasesObjective()
			}, (NSGAIIDataProcessor) dp);
			break;
		case "ga":
		default: 
			dp = new GPDataProcessor();
			searchEngine = new GeneticProgramming<JavaEditOperation>(fitnessEngine, (GPDataProcessor) dp);
		break;
			
		}
		incomingPopulation = new Population<JavaEditOperation>(); 

		try {
			searchEngine.doSearch(baseRep, incomingPopulation);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		int elapsed = getElapsedTime(startTime);
		
		if(dp != null) dp.storeNormal();
		logger.info("\nTotal elapsed time: " + elapsed + "\n");
		
		Runtime.getRuntime().exit(0);
	}

	private static int getElapsedTime(long start) {
		return (int) (System.currentTimeMillis() - start) / 1000;
	}
}
