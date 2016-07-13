package clegoues.genprog4java.localization;

import static clegoues.util.ConfigurationBuilder.BOOLEAN;
import static clegoues.util.ConfigurationBuilder.BOOL_ARG;
import static clegoues.util.ConfigurationBuilder.DOUBLE;
import static clegoues.util.ConfigurationBuilder.STRING;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;
import org.jacoco.core.data.SessionInfo;

import clegoues.genprog4java.Search.GiveUpException;
import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.fitness.TestCase;
import clegoues.genprog4java.java.ClassInfo;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.rep.JavaRepresentation;
import clegoues.genprog4java.rep.Representation;
import clegoues.genprog4java.rep.UnexpectedCoverageResultException;
import clegoues.genprog4java.rep.WeightedAtom;
import clegoues.util.ConfigurationBuilder;
import clegoues.util.GlobalUtils;
import clegoues.util.Pair;

// this class implements boring, default path-file-style localization.
@SuppressWarnings("rawtypes")
public class DefaultLocalization extends Localization {
	protected Logger logger = Logger.getLogger(DefaultLocalization.class);


	public static final ConfigurationBuilder.RegistryToken token =
			ConfigurationBuilder.getToken();

	public static boolean justTestingFaultLoc = ConfigurationBuilder.of( BOOL_ARG )
			.withVarName( "justTestingFaultLoc" )
			.withDefault( "false" )
			.withHelp( "boolean to be turned true if the purpose is to test that fault loc is performed correctly" )
			.inGroup( "DefaultLocalization Parameters" )
			.build();
	//private static double positivePathWeight = 0.1;
	private static double positivePathWeight = ConfigurationBuilder.of( DOUBLE )
			.withVarName( "positivePathWeight" )
			.withDefault( "0.1" )
			.withHelp( "weighting for statements on the positive path" )
			.inGroup( "DefaultLocalization Parameters" )
			.build();
	//private static double negativePathWeight = 1.0;
	private static double negativePathWeight = ConfigurationBuilder.of( DOUBLE )
			.withVarName( "negativePathWeight" )
			.withDefault( "1.0" )
			.withHelp( "weighting for statements on the negative path" )
			.inGroup( "DefaultLocalization Parameters" )
			.build();
	//protected static boolean allowCoverageFail = false;
	protected static boolean allowCoverageFail = ConfigurationBuilder.of( BOOLEAN )
			.withVarName( "allowCoverageFail" )
			.withHelp( "ignore unexpected test results in coverage" )
			.inGroup( "DefaultLocalization Parameters" )
			.build();
	//protected static String posCoverageFile = "coverage.path.pos";
	protected static String posCoverageFile = ConfigurationBuilder.of( STRING )
			.withVarName( "posCoverageFile" )
			.withDefault( "coverage.path.pos" )
			.withHelp( "file containing the statements covered by positive tests" )
			.inGroup( "DefaultLocalization Parameters" )
			.build();
	//protected static String negCoverageFile = "coverage.path.neg";
	protected static String negCoverageFile = ConfigurationBuilder.of( STRING )
			.withVarName( "negCoverageFile" )
			.withDefault( "coverage.path.neg" )
			.withHelp( "file containing the statements covered by negative tests" )
			.inGroup( "DefaultLocalization Parameters" )
			.build();
	//protected static boolean regenPaths = false;
	protected static boolean regenPaths = ConfigurationBuilder.of( BOOLEAN )
			.withVarName( "regenPaths" )
			.withHelp( "regenerate coverage information" )
			.inGroup( "DefaultLocalization Parameters" )
			.build();

	protected Representation original = null;

	protected ArrayList<Location> faultLocalization = new ArrayList<Location>();
	private ArrayList<Location> faultSortedByWeight = null;
	int index = 0;
	protected ArrayList<WeightedAtom> fixLocalization = new ArrayList<WeightedAtom>();

	public DefaultLocalization(Representation orig) throws IOException, UnexpectedCoverageResultException {
		this.original = orig;
		this.computeLocalization();

		if(justTestingFaultLoc == true){
			logger.info("Fault localization was peprformed successfully");
			System.exit(0);
		}
	}


	@Override
	public ArrayList<WeightedAtom> getFixSourceAtoms() {
		return this.fixLocalization;
	}

	public void setAllPossibleStmtsToFixLocalization(){
		fixLocalization.clear();
		for(int i = 0; i < JavaRepresentation.stmtCounter; i++) {
			fixLocalization.add(new WeightedAtom(i,1.0));
		}
	}

	@Override
	public void reduceSearchSpace() throws GiveUpException {
		//Reduce Fault space
		boolean thereIsAtLeastOneMutThatApplies;
		ArrayList<Location> locsToRemove = new ArrayList<Location>();
		for (Location potentiallyBuggyLoc : faultLocalization) {
			thereIsAtLeastOneMutThatApplies = false;
			Set<Pair<Mutation, Double>> availableMutations = original.availableMutations(potentiallyBuggyLoc);
			if(availableMutations.isEmpty()){
				locsToRemove.add(potentiallyBuggyLoc);
			}else{
				for (Pair<Mutation, Double> mutation : availableMutations) {
					thereIsAtLeastOneMutThatApplies = thereIsAtLeastOneMutThatApplies || original.doesEditApply(potentiallyBuggyLoc, mutation.getFirst());
				}
				if(!thereIsAtLeastOneMutThatApplies){
					locsToRemove.add(potentiallyBuggyLoc);
				}
			}
		}
		faultLocalization.removeAll(locsToRemove);
		if(faultLocalization.isEmpty()){
			logger.info("\nThere is no valid mutation to perform in the fault space. Exiting program\n");
			throw new GiveUpException();
		}

		//Reduce Fix space
		ArrayList<WeightedAtom> toRemove = new ArrayList<WeightedAtom>();
		//potentialFix is a potential fix statement
		for (WeightedAtom potentialFixAtom : this.getFixSourceAtoms()) {
			if(original.shouldBeRemovedFromFix(potentialFixAtom)) {
				toRemove.add(potentialFixAtom);

			}

		}
		for(WeightedAtom atom : toRemove) {
			fixLocalization.remove(atom);
		}
	}

	/*
	 * 
	 * (** run the instrumented code to attain coverage information. Writes the
	 * generated paths to disk (the fault and fix path files respectively) but
	 * does not otherwise return.
	 * 
	 * If the calls to [Unix.unlink] fail, they will do so silently.
	 * 
	 * @param coverage_sourcename instrumented source code on disk
	 * 
	 * @param coverage_exename compiled executable
	 * 
	 * @param coverage_outname on disk path file name
	 * 
	 * @raise Fail("abort") if variant produces produces unexpected behavior on
	 * either positive or negative test cases and [--allow-coverage-fail] is not
	 * on. get_coverage will abort if allow_coverage_fail is not toggled and the
	 * variant)
	 * 
	 * Traditional "weighted path" or "set difference" or Reiss-Renieris fault
	 * localization involves finding all of the statements visited while
	 * executing the negative test case(s) and removing/down-weighting
	 * statements visited while executing the positive test case(s).
	 */

	private TreeSet<Integer> runTestsCoverage(String pathFile,
			ArrayList<TestCase> tests, boolean expectedResult, String wd)
					throws IOException, UnexpectedCoverageResultException {
		int counterCoverageErrors = 0;

		TreeSet<Integer> atoms = new TreeSet<Integer>();
		for (TestCase test : tests) {
			File coverageRaw = new File("jacoco.exec");

			if (coverageRaw.exists()) {
				coverageRaw.delete();
			}

			logger.info(test);
			// this expectedResult is just 'true' for positive tests and 'false'
			// for neg tests
			if (original.testCase(test, true) != expectedResult
					&& !DefaultLocalization.allowCoverageFail) {
				logger.error("FaultLocRep: unexpected coverage result: "
						+ test.toString());
				logger.error("Number of coverage errors so far: "
						+ ++counterCoverageErrors);

			}
			TreeSet<Integer> thisTestResult = this.getCoverageInfo();
			atoms.addAll(thisTestResult);
		}

		BufferedWriter out = new BufferedWriter(new FileWriter(new File(
				pathFile)));

		for (int atom : atoms) {
			out.write("" + atom + "\n");
		}

		out.flush();
		out.close();

		return atoms;
	}

	private ExecutionDataStore executionData = null;

	public TreeSet<Integer> getCoverageInfo() throws IOException {
		TreeSet<Integer> atoms = new TreeSet<Integer>();

		Map<ClassInfo,String> source = original.getOriginalSource();
		for (Map.Entry<ClassInfo, String> ele : source.entrySet()) {
			ClassInfo targetClassInfo = ele.getKey();
			String pathToCoverageClass = Configuration.outputDir + File.separator
					+ "coverage/coverage.out" + File.separator + targetClassInfo.pathToClassFile();
			File compiledClass = new File(pathToCoverageClass);
			if(!compiledClass.exists()) {
				pathToCoverageClass = Configuration.classSourceFolder + File.separator + targetClassInfo.pathToClassFile();
				compiledClass = new File(pathToCoverageClass);
			}

			if (executionData == null) {
				executionData = new ExecutionDataStore();
			}

			final FileInputStream in = new FileInputStream(new File(
					"jacoco.exec"));
			final ExecutionDataReader reader = new ExecutionDataReader(in);
			reader.setSessionInfoVisitor(new ISessionInfoVisitor() {
				public void visitSessionInfo(final SessionInfo info) {
				}
			});
			reader.setExecutionDataVisitor(new IExecutionDataVisitor() {
				public void visitClassExecution(final ExecutionData data) {
					executionData.put(data);
				}
			});

			reader.read();
			in.close();

			final CoverageBuilder coverageBuilder = new CoverageBuilder();
			final Analyzer analyzer = new Analyzer(executionData,
					coverageBuilder);
			analyzer.analyzeAll(new File(pathToCoverageClass));

			TreeSet<Integer> coveredLines = new TreeSet<Integer>();
			for (final IClassCoverage cc : coverageBuilder.getClasses()) {
				for (int i = cc.getFirstLine(); i <= cc.getLastLine(); i++) {
					boolean covered = false;
					switch (cc.getLine(i).getStatus()) {
					case ICounter.PARTLY_COVERED:
						covered = true;
						break;
					case ICounter.FULLY_COVERED:
						covered = true;
						break;
					case ICounter.NOT_COVERED:
						break;
					case ICounter.EMPTY:
						break;
					default:
						break;
					}
					if (covered) {
						coveredLines.add(i);
					}
				}
			}
			for (int line : coveredLines) {
				ArrayList<Integer> atomIds = original.atomIDofSourceLine(line);
				if (atomIds != null && atomIds.size() >= 0) {
					atoms.addAll(atomIds);
				}
			}
		}
		return atoms;
	}

	private TreeSet<Integer> readPathFile(String pathFile) {
		TreeSet<Integer> retVal = new TreeSet<Integer>();
		Scanner reader = null;
		try {
			reader = new Scanner(new FileInputStream(pathFile));
			while (reader.hasNextInt()) {
				int i = reader.nextInt();
				retVal.add(i);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			logger.error("coverage file " + pathFile + " not found");
			e.printStackTrace();
		} finally {
			if (reader != null)
				reader.close();
		}
		return retVal;

	}

	protected TreeSet<Integer> getPathInfo(String path, ArrayList<TestCase> tests, boolean shouldPass) throws UnexpectedCoverageResultException, IOException {
		File pathFile = new File(path);
		if(pathFile.exists() && !DefaultLocalization.regenPaths) {
			return readPathFile(path);
		} else {
			String covPath = Configuration.outputDir + "/coverage/";
			File covDir = new File(covPath);
			if (!covDir.exists())
				covDir.mkdir();
			if (!original.compile("coverage", "coverage/coverage.out")) {
				logger.error("faultLocRep: Coverage failed to compile");
				throw new UnexpectedCoverageResultException("compilation failure");
			}
			return runTestsCoverage(path, tests, shouldPass, covPath);
		}

	}

	@Override
	protected void computeLocalization() throws IOException,
	UnexpectedCoverageResultException {
		/*
		 * Default "ICSE'09"-style fault and fix localization from path files.
		 * The weighted path fault localization is a list of <atom,weight>
		 * pairs. The fix weights are a hash table mapping atom_ids to weights.
		 */
		logger.info("Start Fault Localization");
		TreeSet<Integer> positivePath = getPathInfo(DefaultLocalization.posCoverageFile, Fitness.positiveTests, true);
		TreeSet<Integer> negativePath = getPathInfo(DefaultLocalization.negCoverageFile, Fitness.negativeTests, false);

		computeFixSpace(negativePath, positivePath);
		computeFaultSpace(negativePath,positivePath); 

		//printout fault space with their weights
		PrintWriter writer = new PrintWriter("FaultyStmtsAndWeights.txt", "UTF-8");
		for (int i = 0; i < faultLocalization.size(); i++) {
			writer.println("Location:\n" + faultLocalization.get(i).getFirst() + "Weight:\n" + faultLocalization.get(i).getWeight() + "\n");
		}
		writer.close();

		assert (faultLocalization.size() > 0);
		assert (fixLocalization.size() > 0);
		logger.info("Finish Fault Localization");
	}

	protected void computeFaultSpace(TreeSet<Integer> negativePath, TreeSet<Integer> positivePath) {
		HashMap<Integer, Double> fw = new HashMap<Integer, Double>();
		TreeSet<Integer> negHt = new TreeSet<Integer>();
		TreeSet<Integer> posHt = new TreeSet<Integer>();

		for (Integer i : positivePath) {
			fw.put(i, DefaultLocalization.positivePathWeight);
		}

		for (Integer i : positivePath) {
			posHt.add(i);
			fw.put(i, 0.5);
		}
		for (Integer i : negativePath) {
			if (!negHt.contains(i)) {
				double negWeight = DefaultLocalization.negativePathWeight;
				if (posHt.contains(i)) {
					negWeight = DefaultLocalization.positivePathWeight;
				}
				negHt.add(i);
				fw.put(i, 0.5);
				faultLocalization.add(original.instantiateLocation(i, negWeight)); 
			}
		}		
	}

	protected void computeFixSpace(TreeSet<Integer> negativePath, TreeSet<Integer> positivePath) {
		if(DefaultLocalization.fixStrategy.equalsIgnoreCase("packageScope")) {
			Map<ClassInfo,String> originalSource = original.getOriginalSource();
			Set<String> packages = new TreeSet<String>();
			final Set<String> clazzes = new TreeSet<String>();
			ArrayList<File> packageFiles = new ArrayList<File>();
			for(ClassInfo clazzInfo : originalSource.keySet()) {
				packages.add(clazzInfo.getPackage());
				clazzes.add(clazzInfo.getClassName() + ".java");
			}
			for(String packagePath : packages) {
				// question: should I save the package code as well in original?  I think no...
				List<File> list = Arrays.asList(new File(Configuration.workingDir + File.separatorChar + Configuration.sourceDir + File.separatorChar + packagePath).listFiles(new FilenameFilter(){
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".java") && !clazzes.contains(name); // or something else
					}}));
				packageFiles.addAll(list);	
			}
			for(File packageFile : packageFiles) {
				ClassInfo newCi = new ClassInfo(packageFile.getName(), packageFile.getPath()); // FIXME this is wrong
				try {
					original.fromSource(newCi);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			this.fixLocalization.clear();
			for(int i = 0; i < JavaRepresentation.stmtCounter; i++) {
				this.fixLocalization.add(new WeightedAtom(i, 1.0));
			}
		} else {
			for (Integer i : positivePath) {
				fixLocalization.add(new WeightedAtom(i, 0.5));
			}
			for (Integer i : negativePath) {
				fixLocalization.add(new WeightedAtom(i, 0.5));
			}	
		}
	}

	@Override
	public ArrayList<Location> getFaultLocalization() {
		return this.faultLocalization;
	}

	@Override
	public Location getRandomLocation(double weight) {
		return (Location) GlobalUtils.chooseOneWeighted(new ArrayList(this.getFaultLocalization()), weight);
	}

	@Override
	public Location getNextLocation() throws GiveUpException {
		if(faultSortedByWeight == null) {
			faultSortedByWeight = new ArrayList(this.getFaultLocalization());
			//	Collections.sort(faultSortedByWeight);
		}
		if(faultSortedByWeight.size() < index) {
			Location ele = faultSortedByWeight.get(index);
			index++;
			return ele;
		} 
		throw new GiveUpException();
	}

}
