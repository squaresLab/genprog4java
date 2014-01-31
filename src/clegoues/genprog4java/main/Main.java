package clegoues.genprog4java.main;
import java.io.File;
import java.io.IOException;

import clegoues.genprog4java.Fitness.Fitness;
import clegoues.genprog4java.Search.JavaEditOperation;
import clegoues.genprog4java.Search.Population;
import clegoues.genprog4java.Search.RepairFoundException;
import clegoues.genprog4java.Search.Search;
import clegoues.genprog4java.rep.JavaRepresentation;
import clegoues.genprog4java.rep.Representation;
import clegoues.genprog4java.rep.UnexpectedCoverageResultException;

public class Main {

	public static void main(String[] args) throws IOException, UnexpectedCoverageResultException
	{
		Search searchEngine = null;
		Representation baseRep = null;
		Fitness fitnessEngine = null;
		Population incomingPopulation = null;
		assert(args.length > 0);
		long startTime = System.currentTimeMillis();

		Configuration.setProperties(args[0]);
		File workDir = new File(Configuration.outputDir);
		if(!workDir.exists())
			workDir.mkdir();
		System.out.println("Configuration file loaded");
		if(Configuration.globalExtension == ".java") {
			baseRep = (Representation) new JavaRepresentation();
			fitnessEngine = new Fitness<JavaEditOperation>();
			searchEngine = new Search<JavaEditOperation>(fitnessEngine);
			incomingPopulation = new Population<JavaEditOperation>(); // FIXME: read from incoming if applicable!
		}
		baseRep.load(Configuration.sourceDir + "/" + Configuration.targetClassName + ".java");
		try {
			if(Configuration.searchStrategy == "ga") {
				searchEngine.geneticAlgorithm(baseRep, incomingPopulation);
			} else if (Configuration.searchStrategy == "brute") {
				searchEngine.bruteForceOne(baseRep);
			} else if (Configuration.searchStrategy == "oracle") {
				searchEngine.oracleSearch(baseRep);
			}
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
