package clegoues.genprog4java.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import org.apache.log4j.BasicConfigurator;

import clegoues.genprog4java.Search.OracleSearch;
import clegoues.genprog4java.Search.Population;
import clegoues.genprog4java.Search.RandomSingleEdit;
import clegoues.genprog4java.Search.Search;
import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.localization.DefaultLocalization;
import clegoues.genprog4java.localization.EntropyLocalization;
import clegoues.genprog4java.localization.Localization;
import clegoues.genprog4java.localization.UnexpectedCoverageResultException;
import clegoues.genprog4java.mut.edits.java.JavaEditFactory;
import clegoues.genprog4java.rep.CachingRepresentation;
import clegoues.genprog4java.rep.JavaRepresentation;
import clegoues.genprog4java.rep.Representation;
import clegoues.util.ConfigurationBuilder;

public class Run {
	
	public static void main(String[] args) throws Exception{
		if (args.length < 4) {
			System.err.println(
					"<projectName> <iteration in projectID> <Path to defect4j projects> <Path to where output will be stored>");
			return;
		}
		String projectName = args[0];
		int limit = Integer.parseInt(args[1]);
		String examplePath = args[2];
		String outputPath = args[3]; 
		
		int i = limit;
		String[] arg = {examplePath + "/"+ projectName.toLowerCase()+ Integer.toString(i)+"Buggy/defects4j.config"};
		String filePath = outputPath+ "/" + projectName + Integer.toString(i);
		
		Search searchEngine = null;
		Representation baseRep = null;
		Localization localization = null;
		Fitness fitnessEngine = null;
		Population incomingPopulation = null;
		assert (args.length > 0);
		long startTime = System.currentTimeMillis();
		BasicConfigurator.configure();

		ConfigurationBuilder.register( Configuration.token );
		ConfigurationBuilder.register( Fitness.token );
		ConfigurationBuilder.register( CachingRepresentation.token );
		ConfigurationBuilder.register( Localization.token );
		ConfigurationBuilder.register( DefaultLocalization.token );
		ConfigurationBuilder.register( EntropyLocalization.token );
		ConfigurationBuilder.register( JavaRepresentation.token );
		ConfigurationBuilder.register( JavaEditFactory.token );
		ConfigurationBuilder.register( Population.token );
		ConfigurationBuilder.register( Search.token );
		ConfigurationBuilder.register( OracleSearch.token );
		ConfigurationBuilder.register( RandomSingleEdit.token );
		ConfigurationBuilder.parseArgs( arg );
		Configuration.saveOrLoadTargetFiles();
		ConfigurationBuilder.storeProperties();
		fitnessEngine = new Fitness(); // Fitness must be created before rep!
		baseRep = (Representation) new JavaRepresentation();
		baseRep.load(Configuration.targetClassNames);;
		localization = new EntropyLocalization(baseRep);	
		
		ArrayList<ArrayList<String>> results = localization.rankFaults();
		
		FileWriter writerEntropy = new FileWriter(filePath+"_Entropy.txt"); 
		for(String str: results.get(0)) {
			writerEntropy.write(str);
		}
		writerEntropy.close();
		
		FileWriter writerNames = new FileWriter(filePath+"_Names.txt"); 
		for(String str: results.get(1)) {
			writerNames.write(str);
			writerNames.write("\n");
		}
		writerNames.close();
	}
}
























