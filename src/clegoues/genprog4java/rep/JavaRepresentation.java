package clegoues.genprog4java.rep;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.util.GlobalUtils;

import org.apache.commons.exec.CommandLine;
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
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;

public class JavaRepresentation extends FaultLocRepresentation<EditOperation> {
	
	// compile assumes that it's been written to disk.  Should it continue to assume that
	// the subdirectory has already been created?
	
	private CommandLine testCommand = null;
	private String javaRuntime;
	private String javaVM; // FIXME
	private String libs; // FIXME
	private String filterClass = "";
			private ArrayList<EditOperation> genome = null;
			
	protected void instrumentForFaultLocalization(){
		String coverageOutputDir = "coverage";
		filterClass = "clegoues.genprog4java.util.CoverageFilter";

		String classPath = coverageOutputDir + File.separator + 0
				+ System.getProperty("path.separator") + libs;
		
		CommandLine command = CommandLine.parse(javaRuntime);
		command.addArgument("-classpath");
		command.addArgument(classPath);
		
		command.addArgument("-Xmx1024m");
		command.addArgument(
				"-javaagent:../lib/jacocoagent.jar=excludes=org.junit.*,append=false");
		
		command.addArgument("clegoues.genprog4java.Fitness.UnitTestRunner");
		
	//	command.addArgument(testFile);
		
	//	command.addArgument(filterClass);
		
		
	}
	
	public boolean compile(String sourceName, String exeName, String wd)
	{
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		Iterable<? extends JavaFileObject> fileObjects = this.computeSourceBuffers();
		
		LinkedList<String> options = new LinkedList<String>();
		
		options.add("-cp");
		options.add(Configuration.libs);
		
		options.add("-source");
		options.add(Configuration.sourceVersion);
		
		options.add("-target");
		options.add(Configuration.targetVersion);
		
		options.add("-d");
		options.add(wd);  // e.g., tmp/10210/
		
		File outDir = new File(wd + File.separator);
		if(!outDir.exists())
			outDir.mkdir();		
		
		StringWriter compilerErrorWriter = new StringWriter();
		
		if(!compiler.getTask(compilerErrorWriter, null, null, options, null, fileObjects).call())
		{
			compilerErrorWriter.flush();
			System.err.println(compilerErrorWriter.getBuffer().toString());
			return false;
		}
		
		return true;
	}

// Java-specific coverage stuff:
	
	// this all comes originally from CoverageRuntime, where I learned to use jacoco

	private static String coverageFile = "jacoco.exec";
	
	private IRuntime runtime;
	
	private ExecutionDataStore executionData;

	public TreeSet<Integer> getCoverageInfo() throws IOException
	{
		InputStream targetClass = null; // FIXME 
		if(executionData == null) {
		executionData = new ExecutionDataStore();
		}
		if(runtime == null) {
			runtime = new LoggerRuntime();
		}
		
		final FileInputStream in = new FileInputStream(new File(coverageFile));
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
		final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
		analyzer.analyzeClass(targetClass);
		
		TreeSet<Integer> coveredLines = new TreeSet<Integer>();

		for (final IClassCoverage cc : coverageBuilder.getClasses())
		{
			for (int i = cc.getFirstLine(); i <= cc.getLastLine(); i++)
			{
				//System.err.println("Covered? ["+i+"]: " + isCovered(cc.getLine(i).getStatus()));
				if(isCovered(cc.getLine(i).getStatus()))
				{
					coveredLines.add(i);
				}
			}
		}

		return coveredLines;
	}
	
	
	private boolean isCovered(final int status) {
		switch (status) {
		case ICounter.NOT_COVERED:
			return false;
		case ICounter.PARTLY_COVERED:
			return true;
		case ICounter.FULLY_COVERED:
			return true;
		}
		return false;
	}
}
