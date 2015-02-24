package test;

import java.io.File;
import java.io.IOException;

import clegoues.genprog4java.Search.Population;
import clegoues.genprog4java.Search.RepairFoundException;
import clegoues.genprog4java.Search.Search;
import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.mut.JavaEditOperation;
import clegoues.genprog4java.rep.JavaRepresentation;
import clegoues.genprog4java.rep.LocalizationRepresentation;
import clegoues.genprog4java.rep.Representation;
import clegoues.genprog4java.rep.UnexpectedCoverageResultException;

/**
 * Removed some of the conditional statements to avoid confusion.
 * @author alexmaeda
 *
 */
public class TestRewrite {
	public static void main(String[] args) throws IOException, UnexpectedCoverageResultException{
		Search searchEngine = null;
		JavaRepresentation baseRep = null;
		Fitness fitnessEngine = null;
		Population incomingPopulation = null;
		
		assert(args.length > 0);
		long startTime = System.currentTimeMillis();

		Configuration.setProperties(args[0]);
		File workDir = new File(Configuration.outputDir);
		if(!workDir.exists())
			workDir.mkdir();
		System.out.println("Configuration file loaded");
	
		baseRep = new JavaRepresentation(); //
		fitnessEngine = new Fitness<JavaEditOperation>();
		searchEngine = new Search<JavaEditOperation>(fitnessEngine);
		incomingPopulation = new Population<JavaEditOperation>(); 
			
		incomingPopulation = new Population<JavaEditOperation>(); // FIXME: read from incoming if applicable?

		baseRep.load(Configuration.targetClassName);
			//Run the genetic algorithm
		try{
			searchEngine.geneticAlgorithm(baseRep, incomingPopulation);
		} catch(RepairFoundException e) {
			// FIXME: this is stupid
		} catch (CloneNotSupportedException e) {
			e.printStackTrace(); 
		}
		
		int elapsed = getElapsedTime(startTime);
		System.out.printf("\nTotal elapsed time: " + elapsed + "\n");
		Runtime.getRuntime().exit(0);
	}
	
	private static int getElapsedTime(long start)
	{
		return (int) ( System.currentTimeMillis() - start ) / 1000;
	}
}
