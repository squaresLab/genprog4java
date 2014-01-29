package clegoues.genprog4java.rep;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.util.GlobalUtils;

public class JavaRepresentation extends FaultLocRepresentation {
	
	// compile assumes that it's been written to disk.  Should it continue to assume that
	// the subdirectory has already been created?
	
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
	
	// this all comes from CoverageRuntime

	private static String coverageFile = "jacoco.exec";
	
	private IRuntime runtime;
	
	private ExecutionDataStore executionData;
	
	public CoverageRuntime()
	{
		executionData = new ExecutionDataStore();
	}
	
	public Set<Integer> obtainCoverageData(InputStream targetClass) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException
	{
		TreeSet<Integer> coveredLines = new TreeSet<Integer>();
		
		readCoverageDataFile();
		
		final CoverageBuilder coverageBuilder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
		analyzer.analyzeClass(targetClass);
		
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
	
	public void initRuntime()
	{
		this.runtime = new LoggerRuntime();
	}
	
	public void startRuntime() throws Exception
	{
		this.runtime.startup();
	}
	
	private void readCoverageDataFile(String coverageFile) throws IOException
	{
		final FileInputStream in = new FileInputStream(new File(coverageFile));
		final ExecutionDataReader reader = new ExecutionDataReader(in);
		reader.setSessionInfoVisitor(new ISessionInfoVisitor() {
			public void visitSessionInfo(final SessionInfo info) {
				/*System.out.printf("Session \"%s\": %s - %s%n", info.getId(),
						new Date(info.getStartTimeStamp()),
						new Date(info.getDumpTimeStamp()));*/
			}
		});
		reader.setExecutionDataVisitor(new IExecutionDataVisitor() {
			public void visitClassExecution(final ExecutionData data) {
				executionData.put(data);
			}
		});
		reader.read();
		in.close();
	}
	
	
	public InputStream getTargetClass(final String name) {
		final String resource = '/' + name.replace('.', '/') + ".class";
		return getClass().getResourceAsStream(resource);
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
