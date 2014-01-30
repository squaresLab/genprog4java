package clegoues.genprog4java.rep;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import clegoues.genprog4java.Fitness.TestCase;
import clegoues.genprog4java.Search.JavaEditOperation;
import clegoues.genprog4java.java.ASTUtils;
import clegoues.genprog4java.java.JavaParser;
import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.main.Main;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.util.Pair;

import org.apache.commons.exec.CommandLine;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
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


// this can handle ONE FILE right now

public class JavaRepresentation extends FaultLocRepresentation<JavaEditOperation> {

	// compile assumes that it's been written to disk.  Should it continue to assume that
	// the subdirectory has already been created?
	public static HashMap<Integer,JavaStatement> codeBank = new HashMap<Integer,JavaStatement>();
	private static HashMap<Integer,JavaStatement> base = new HashMap<Integer,JavaStatement>();
	private static CompilationUnit baseCompilationUnit = null;
	private static HashMap<Integer,ArrayList<Integer>> lineNoToAtomIDMap = new HashMap<Integer,ArrayList<Integer>>();
	private static String originalSource = "";
			
	private CommandLine testCommand = null;
	private String javaRuntime;
	private String javaVM; // FIXME
	private String libs; // FIXME
	private String filterClass = "";
	private ArrayList<JavaEditOperation> genome = null;
	private String classUnderRepair = "";

	private static String getOriginalSource() { return originalSource; }
	
	protected void instrumentForFaultLocalization(){
		String coverageOutputDir = "coverage";
		this.filterClass = "clegoues.genprog4java.util.CoverageFilter";

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

		Iterable<? extends JavaFileObject> fileObjects = null ; // FIXME: this.computeSourceBuffers();

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

	private ExecutionDataStore executionData = null;
	

	protected ArrayList<Integer> atomIDofSourceLine(int lineno) {
		return lineNoToAtomIDMap.get(lineno);
	}

	public TreeSet<Integer> getCoverageInfo() throws IOException
	{
		InputStream targetClass = new FileInputStream(new File(Configuration.outputDir + File.separator + "coverage"+File.separator+Configuration.packageName.replace(".","/")
				+ File.separator + this.classUnderRepair + ".class"));
		
		if(executionData == null) {
			executionData = new ExecutionDataStore();
		}


		final FileInputStream in = new FileInputStream(new File("jacoco.exec"));
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
				boolean covered = false;
				switch(cc.getLine(i).getStatus()) {
				case ICounter.PARTLY_COVERED: covered = true;
				break;
				case ICounter.FULLY_COVERED: covered = true;
				break;
				default: break;
				}
				if(covered) {
					coveredLines.add(i);
				}
			}
		}
		TreeSet<Integer> atoms = new TreeSet<Integer>();
		for(int line : coveredLines) {
			ArrayList<Integer> atomIds = this.atomIDofSourceLine(line);
			if(atomIds.size() >= 0) {
			atoms.addAll(atomIds); 
			}
		}

		return atoms;
	}


	public void fromSource(String fname) throws IOException
	{
		// load here, get all statements and the compilation unit saved
		// parser can visit at the same time to collect scope info
		// apparently names and types and scopes are visited here below in
		// the calls to ASTUTils
		
		JavaParser myParser = new JavaParser();
			myParser.parse(fname, this.libs.split(File.pathSeparator)); 
			List<ASTNode> stmts = myParser.getStatements();

			baseCompilationUnit = myParser.getCompilationUnit();
			int stmtCounter = 0;
			for(ASTNode node : stmts)
			{
				if(JavaRepresentation.canRepair(node)) { // FIXME: I think this check was already done in the parser, but whatever
				JavaStatement s = new JavaStatement();
				s.setStmtId(stmtCounter);
				stmtCounter++;
				int lineNo = ASTUtils.getStatementLineNo(node);
				s.setLineno(lineNo);
				s.setNames(ASTUtils.getNames(node));
				s.setTypes(ASTUtils.getTypes(node));
				s.setScopes(ASTUtils.getScope(node));
				ASTNode copy = ASTNode.copySubtree(node.getAST(), node);
				s.setASTNode(copy);
				ArrayList<Integer> lineNoList = null;
				if(lineNoToAtomIDMap.containsKey(lineNo)) {
					lineNoList = lineNoToAtomIDMap.get(lineNo);
				} else {
					lineNoList = new ArrayList<Integer>();
				}
				lineNoList.add(s.getStmtId());
				lineNoToAtomIDMap.put(lineNo,  lineNoList);
				base.put(s.getStmtId(),s);
				codeBank.put(s.getStmtId(), s); // FIXME: possibly a copy here as well
				}
		}

	}


	public static boolean canRepair(ASTNode node) {
		return node instanceof ExpressionStatement || node instanceof AssertStatement
				|| node instanceof BreakStatement || node instanceof ContinueStatement
				|| node instanceof LabeledStatement || node instanceof ReturnStatement
				|| node instanceof ThrowStatement || node instanceof VariableDeclarationStatement
				|| node instanceof IfStatement; // FIXME: I  think we actually don't want to repair some of these things
	}


	public ArrayList<JavaEditOperation> getGenome() {
		return this.genome;
	}

	@Override
	public void loadGenomeFromString(String genome) {
		// TODO Auto-generated method stub
		
	}

	public void setGenome(List<JavaEditOperation> genome) {
		this.genome = (ArrayList<JavaEditOperation>) genome;
	}

	@Override
	public int genomeLength() {
		if(genome == null) { return 0 ; }
		return genome.size();
	}

	@Override
	public void serialize(String filename) {
		// TODO Auto-generated method stub
		
	}




	@Override
	public void outputSource(String filename) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean fitnessIsValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Representation<JavaEditOperation> copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int num_test_evals_ignore_cache() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int compareTo(Representation<JavaEditOperation> o) {
		// TODO Auto-generated method stub
		return 0;
	}



	@Override
	protected List<Pair<String,String>> computeSourceBuffers() {
		CompilationUnit cu = (CompilationUnit) ASTNode.copySubtree(baseCompilationUnit.getAST(), baseCompilationUnit); // FIXME: possibly a disaster

		Document doc = new Document(JavaRepresentation.getOriginalSource()); // FIXME: SET ORIGINAL SOURCE IN LOAD
		ASTRewrite rewriter = ASTRewrite.create(cu.getAST());
		
		try
		{
			for(JavaEditOperation edit : genome) { 
				edit.edit(rewriter);
			}
			
			TextEdit edits = null;
			
			edits = rewriter.rewriteAST(doc, null);
			edits.apply(doc);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (MalformedTreeException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		} 
		ArrayList<Pair<String,String>> retVal = new ArrayList<Pair<String,String>>();
		retVal.add(new Pair<String,String>(this.classUnderRepair, doc.get()));
		return retVal;
	}

	@Override
	protected boolean internalTestCase(String sanityExename,
			String sanityFilename, TestCase thisTest) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void delete(int location) {
		JavaStatement locationStatement = base.get(location);
		JavaEditOperation newEdit = new JavaEditOperation(locationStatement);
		this.genome.add(newEdit);
	}

	private void editHelper(int location, int fixCode, Mutation mutType) {
		JavaStatement locationStatement = base.get(location);
		JavaStatement fixCodeStatement = codeBank.get(fixCode); // FIXME correct for Swap? Hm.
		JavaEditOperation newEdit = new JavaEditOperation(mutType,locationStatement,fixCodeStatement);
		this.genome.add(newEdit);
	}
	@Override
	public void append(int whereToAppend, int whatToAppend) {
		this.editHelper(whereToAppend,whatToAppend,Mutation.APPEND); 
	}

	@Override
	public void swap(int swap1, int swap2) {
		this.editHelper(swap1,swap2,Mutation.SWAP); 

	}

	@Override
	public void replace(int whatToReplace, int whatToReplaceWith) {
		this.editHelper(whatToReplace,whatToReplaceWith,Mutation.REPLACE);		
	}
}
