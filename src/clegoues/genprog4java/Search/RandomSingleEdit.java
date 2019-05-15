package clegoues.genprog4java.Search;

import static clegoues.util.ConfigurationBuilder.INT;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.tuple.Pair;

import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.fitness.TestCase;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.mut.EditOperation;
import clegoues.genprog4java.rep.Representation;
import clegoues.util.ConfigurationBuilder;
import ylyu1.wean.AbstractDataProcessor;
import ylyu1.wean.VariantCheckerMain;
import ylyu1.wean.YAMLDataProcessor;

public class RandomSingleEdit<G extends EditOperation> extends Search<G>{

	YAMLDataProcessor dp;

	public static final ConfigurationBuilder.RegistryToken token =
			ConfigurationBuilder.getToken();

	protected static int maxVariants = ConfigurationBuilder.of( INT )
			.withVarName( "maxVariants" )
			.withDefault( "50" )
			.withHelp( "maximum number of variants to consider" )
			.inGroup( "Search Parameters" )
			.build();

	public RandomSingleEdit(Fitness engine, AbstractDataProcessor dataprocessor) {
		super(engine);
		engine.initializeModel();
		if(dataprocessor instanceof YAMLDataProcessor)
			dp = (YAMLDataProcessor)dataprocessor;
		else
			throw new UnsupportedOperationException("Only YAMLDataProcessors supported for RandomSingleEdit");
	}

	@Override
	protected Population<G> initialize(Representation<G> original, Population<G> incomingPopulation)
			throws RepairFoundException, GiveUpException {
		original.getLocalization().reduceSearchSpace();
		return null;
	}

	@Override
	protected void runAlgorithm(Representation<G> original, Population<G> initialPopulation)
			throws RepairFoundException, GiveUpException {
		
		/*
		//under the new dataflow plan for mutation testing, Daikon is now run as a preprocessing step
		if(!(new File(Configuration.workingDir+"/JUSTUSE.ywl")).exists())
			throw new RuntimeException("Daikon output not found!");
		else
		{
			VariantCheckerMain.checkModify();
		}
		*/
		
		//Using the full Daikon initialization process for debugging
		System.out.println("mode: "+Configuration.invariantCheckerMode);
		if(/*Configuration.invariantCheckerMode>0*/true)
		{
			int trials = 0;
			while((trials<5)&&(!(new File(Configuration.workingDir+"/JUSTUSE.ywl")).exists()))
			{
				System.out.println("Daikon output not present: running Daikon");
				VariantCheckerMain.runDaikon(dp);
				trials++;
			}//VariantCheckerMain.checkInvariantOrig();
			if(!(new File(Configuration.workingDir+"/JUSTUSE.ywl")).exists())
			{
				dp.storeError("weirddaikon");
				Runtime.getRuntime().exit(1);
			}
			else {
				System.out.println("Daikon output found");
				VariantCheckerMain.checkModify();
			}
		}
		
		logger.info("begin variant generation");
		
		int numVariantsConsidered = 0;
		while(numVariantsConsidered < RandomSingleEdit.maxVariants) {
			Representation<G> variant = original.copy();
			mutate(variant);
			
			Pair<List<TestCase>, List<TestCase>> posTestResults = fitnessEngine.testPosTests(variant);
			//running tests would ensure compilation if the variant isn't cached.
			if ( ! variant.getVariantFolder().equals(""))
			{ //continue further analysis only if the variant isn't already seen before (and not cached).
				Pair<List<TestCase>, List<TestCase>> negTestResults = fitnessEngine.testNegTests(variant);
				List<TestCase> passingPosTests = posTestResults.getLeft();
				List<TestCase> passingNegTests = negTestResults.getLeft();
				List<TestCase> failingPosTests = posTestResults.getRight();
				List<TestCase> failingNegTests = negTestResults.getRight();
				boolean repairFound = failingPosTests.size() == 0 && failingNegTests.size() == 0;
				if(repairFound)
					this.noteSuccess(variant, original, 0);
					//continue the search, since we're doing mutation testing
				byte[] invariantProfile = VariantCheckerMain.checkInvariantForSingleRep(variant);
				
				//store info
				Map<String, Object> info  = new LinkedHashMap<>(); //keys are sorted in order of insertion
				info.put("Passing positive tests", passingPosTests);
				info.put("Passing negative tests", passingNegTests);
				info.put("Failing positive tests", failingPosTests);
				info.put("Failing negative tests", failingNegTests);
				info.put("Invariant profile", invariantProfile);
				Map<String, Object> wrapper = new LinkedHashMap<>(1);
				wrapper.put(variant.getVariantID(), info);
				dp.dumpData(wrapper);
				
				copyClassFilesIntoOutputDir(variant);
				numVariantsConsidered++;
			}
		}
	}

	/**
	 * Copies the compiled source files (from classSourceFolder, as defined in the .config file) to the outputDir (default for experiments: the tmp folder)
	 * @param item
	 */
	private void copyClassFilesIntoOutputDir(Representation<G> item)
	{
		if (item.getVariantFolder().equals(""))
		{
			//if there's no variant folder name, do nothing
			return;
		}
		
		String copyDestination = Configuration.outputDir + //no space added
				(Configuration.outputDir.endsWith(File.separator) ? "" : File.separator) + //add a separator if necessary
				"d_" + item.getVariantFolder();
		
		File dFolder = new File(copyDestination);
		if(!dFolder.exists())
			dFolder.mkdirs();
		
		File classSourceFolderFile = new File(Configuration.classSourceFolder);
		if(!classSourceFolderFile.exists())
			System.err.println("classSourceFolder does not exist");
		
		
		/*
		CommandLine cpCommand = CommandLine.parse(
				"cp -R " +
				Configuration.classSourceFolder + //no space added
				(Configuration.classSourceFolder.endsWith(File.separator) ? "" : File.separator) + //add a separator if necessary
				"* " + //a wildcard char may or may not be needed
				copyDestination
				);
		*/
		CommandLine cpCommand = CommandLine.parse(
				"rsync -r " +
				Configuration.classSourceFolder + //no space added
				(Configuration.classSourceFolder.endsWith(File.separator) ? "" : File.separator) + //add a separator if necessary
				" " +
				copyDestination
				);
		
		//System.err.println("cp command: " + cpCommand);
		
		ExecuteWatchdog watchdog = new ExecuteWatchdog(1000000);
		DefaultExecutor executor = new DefaultExecutor();
		String workingDirectory = System.getProperty("user.dir");
		executor.setWorkingDirectory(new File(workingDirectory));
		executor.setWatchdog(watchdog);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		executor.setExitValue(0);

		executor.setStreamHandler(new PumpStreamHandler(out));
		
		try
		{
			executor.execute(cpCommand);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try
			{
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
