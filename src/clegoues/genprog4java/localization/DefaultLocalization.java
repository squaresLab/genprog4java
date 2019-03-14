package clegoues.genprog4java.localization;

import static clegoues.util.ConfigurationBuilder.BOOLEAN;
import static clegoues.util.ConfigurationBuilder.BOOL_ARG;
import static clegoues.util.ConfigurationBuilder.DOUBLE;
import static clegoues.util.ConfigurationBuilder.STRING;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodRef;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;
import org.jacoco.core.data.SessionInfo;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import clegoues.genprog4java.Search.GiveUpException;
import clegoues.genprog4java.Search.Search;
import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.fitness.TestCase;
import clegoues.genprog4java.java.ClassInfo;
import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.mut.WeightedMutation;
import clegoues.genprog4java.rep.JavaRepresentation;
import clegoues.genprog4java.rep.Representation;
import clegoues.genprog4java.rep.WeightedAtom;
import clegoues.util.ConfigurationBuilder;
import clegoues.util.GlobalUtils;

//this class implements boring, default path-file-style localization.
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

	protected static String fixStrategy = ConfigurationBuilder.of ( STRING )
			.withVarName("fixStrategy")
			.withHelp("Fix source strategy")
			.withDefault("classScope")
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

	// FIXME: I think this should be pushed to subclasses
	protected static String faultLocStrategy = ConfigurationBuilder.of ( STRING )
			.withVarName("faultLocStrategy")
			.withHelp("Fault localization strategy")
			.withDefault("standardPathFile")
			.inGroup( "FaultLocRepresentation Parameters" )
			.build();
	protected static String pathToFileHumanInjectedFaultLoc = ConfigurationBuilder.of ( STRING )
			.withVarName("pathToFileHumanInjectedFaultLoc")
			.withHelp("The path of the file with classes and line numbers of the faulty stmts, when fault localization is human inserted and not created by the coverage")
			.withDefault("fileHumanInjectedFaultLoc.txt")
			.inGroup( "FaultLocRepresentation Parameters" )
			.build();

	protected Representation original = null;
	protected ArrayList<Location> faultLocalization = new ArrayList<Location>();
	private ArrayList<Location> faultSortedByWeight = null;
	private int index = 0;

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

	@Override
	public ArrayList<WeightedAtom> getFixSourceAtoms() {
		return this.fixLocalization;
	}

	@Override
	public void setAllPossibleStmtsToFixLocalization() {
		fixLocalization.clear();
		for(int i = 0; i < JavaRepresentation.stmtCounter; i++) {
			fixLocalization.add(new WeightedAtom(i,1.0));
		}
	}

	@Override
	public void reduceSearchSpace() throws GiveUpException {
		if(Search.checkSpace) {
			boolean thereIsAtLeastOneMutThatApplies;
			ArrayList<Location> locsToRemove = new ArrayList<Location>();
			for (Location potentiallyBuggyLoc : faultLocalization) {
				thereIsAtLeastOneMutThatApplies = false;
				List<WeightedMutation> availableMutations = original.availableMutations(potentiallyBuggyLoc);
				if(availableMutations.isEmpty()){
					locsToRemove.add(potentiallyBuggyLoc);
				}else{
					for (WeightedMutation mutation : availableMutations) {
						thereIsAtLeastOneMutThatApplies = thereIsAtLeastOneMutThatApplies || original.doesEditApply(potentiallyBuggyLoc, mutation.getLeft());
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

	@Override
	protected void computeLocalization() throws IOException, UnexpectedCoverageResultException {
		// THIS ONLY DOES STANDARD PATH FILE localization
		/*
		 * Default "ICSE'09"-style fault and fix localization from path files.
		 * The weighted path fault localization is a list of <atom,weight>
		 * pairs. The fix weights are a hash table mapping atom_ids to weights.
		 */
		logger.info("Start Fault Localization");
		TreeSet<Integer> positivePath = getPathInfo(DefaultLocalization.posCoverageFile, Fitness.positiveTests, true);
		TreeSet<Integer> negativePath = null;

		switch(faultLocStrategy.trim()) { // FIXME: push this to a subclass.

		case "humanInjected":
			negativePath = transformFileWithLineNumbersToStmtNumbers(pathToFileHumanInjectedFaultLoc);
			break;
		case "standardPathFile":
		default:
			negativePath = getPathInfo(DefaultLocalization.negCoverageFile, Fitness.negativeTests, false);
		}
		switch(faultLocStrategy.trim()) { // FIXME: push this to a subclass.
		case "humanInjected":
		case "standardPathFile":
		default:
			computeFaultSpace(negativePath,positivePath);
		}
		computeFixSpace(negativePath, positivePath);

		//printout fault space with their weights
		PrintWriter writer = new PrintWriter("FaultyStmtsAndWeights.txt", "UTF-8");
		for (int i = 0; i < faultLocalization.size(); i++) {
			writer.println("Location:\n" + faultLocalization.get(i).getLeft() + "Weight:\n" + faultLocalization.get(i).getWeight() + "\n");
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
				ClassInfo newCi = new ClassInfo(packageFile.getName(), packageFile.getPath());
				try {
					original.fromSource(newCi, packageFile.getPath(), packageFile);
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

			//System.out.println(test);
			logger.info(test);
			// this expectedResult is just 'true' for positive tests and 'false'
			// for neg tests
			if (original.testCase(test, true).isAllPassed() != expectedResult
					&& !allowCoverageFail) {
				logger.error("FaultLocRep: unexpected coverage result: "
						+ test.toString());
				logger.error("Number of coverage errors so far: "
						+ ++counterCoverageErrors);

			}
			ylyu1.morewood.MethodTracker.mcov.put(test, new HashSet<String>());
			TreeSet<Integer> thisTestResult = this.getCoverageInfo(test);
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

	public TreeSet<Integer> getCoverageInfo(TestCase test) throws IOException {
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
				for(IMethodCoverage mc : cc.getMethods()) {
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
							ylyu1.morewood.MethodTracker.mcov.get(test).add(mc.getName());
						}
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

	private TreeSet<Integer> transformFileWithLineNumbersToStmtNumbers(String pathOfFileWithFaultyLineNumbers){
		TreeSet<Integer> negativePath = new TreeSet<Integer>();
		Scanner reader = null;
		try {
			reader = new Scanner(new FileInputStream(pathOfFileWithFaultyLineNumbers));
			while (reader.hasNextLine()) {
				String line = reader.nextLine();
				if(!line.equalsIgnoreCase("")){
					String packageName = line.split(",")[0].trim();
					String className = line.split(",")[1].trim();
					//trim .java if the user types the class name with the extension
					className = className.contains(".")? className.split(".")[0] : className;
					String lineNumberString = line.split(",")[2].trim();
					int lineNumber = Integer.parseInt(lineNumberString);
					ArrayList<Integer> atomIds = original.atomIDofSourceLine(lineNumber);
					if(atomIds!=null){
						for(int atomId:atomIds){
							ClassInfo ci = original.getFileFromStmt(atomId);
							if(ci.getClassName().equalsIgnoreCase(className) && ci.getPackage().equalsIgnoreCase(packageName)){
								negativePath.addAll(atomIds);
							}
						}
					}
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			logger.error("coverage file " + pathOfFileWithFaultyLineNumbers + " not found");
			e.printStackTrace();
		} finally {
			if (reader != null)
				reader.close();
		}


		return negativePath;
	}


}
