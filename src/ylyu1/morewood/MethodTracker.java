package ylyu1.morewood;

import java.util.*;

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

import clegoues.genprog4java.fitness.TestCase;
import clegoues.genprog4java.java.ClassInfo;
import clegoues.genprog4java.localization.UnexpectedCoverageResultException;
import clegoues.genprog4java.main.*;
import clegoues.genprog4java.rep.Representation;

import java.io.*;


public class MethodTracker {
	/*
	public Map<TestCase,Set<String>> runTestsCoverage(String pathFile,
			ArrayList<TestCase> tests, boolean expectedResult, String wd)
					throws IOException, UnexpectedCoverageResultException {
		//int counterCoverageErrors = 0;

		Map<TestCase,Set<String>> results = new HashMap<TestCase,Set<String>>();
		
		//TreeSet<Integer> atoms = new TreeSet<Integer>();
		for (TestCase test : tests) {
			File coverageRaw = new File("jacoco.exec");

			if (coverageRaw.exists()) {
				coverageRaw.delete();
			}

			//System.out.println(test);
			//logger.info(test);
			// this expectedResult is just 'true' for positive tests and 'false'
			// for neg tests
			//if (original.testCase(test, true).isAllPassed() != expectedResult
				//	&& !allowCoverageFail) {
				//logger.error("FaultLocRep: unexpected coverage result: "
						//+ test.toString());
				//logger.error("Number of coverage errors so far: "
						//+ ++counterCoverageErrors);

			//}
			Set<String> thisTestResult = this.getCoverageInfo();
			for(String s : thisTestResult) {
				System.out.println(s);
			}
			results.put(test,thisTestResult);
		}

		return results;
	}
	
	private ExecutionDataStore executionData = null;
	
	public Representation original = null;

	public Set<String> getCoverageInfo() throws IOException {
		//TreeSet<Integer> atoms = new TreeSet<Integer>();
		Set<String> coveredmethods = new HashSet<String>();
		
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

			//TreeSet<Integer> coveredLines = new TreeSet<Integer>();
			for (IClassCoverage cc : coverageBuilder.getClasses()) {
				for(IMethodCoverage mc: cc.getMethods() )
				for (int i = mc.getFirstLine(); i <= mc.getLastLine(); i++) {
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
						coveredmethods.add(cc.getSignature()+"."+mc.getSignature());
						break;
					}
					
				}
			}
		}
		return coveredmethods;
	}*/
	public static Map<TestCase,Set<String>> mcov = new HashMap<TestCase,Set<String>>();
	
	public static void printmcov() {
		for(TestCase tc : mcov.keySet()) {
			System.out.println("Test Case: "+ tc.getTestName());
			for(String s : mcov.get(tc)) {
				System.out.println(s);
			}
		}
	}
}
