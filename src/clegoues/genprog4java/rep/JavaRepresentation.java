/*
 * Copyright (c) 2014-2015, 
 *  Claire Le Goues     <clegoues@cs.cmu.edu>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. The names of the contributors may not be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package clegoues.genprog4java.rep;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
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

import clegoues.genprog4java.fitness.TestCase;
import clegoues.genprog4java.java.ASTUtils;
import clegoues.genprog4java.java.JavaParser;
import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.java.ScopeInfo;
import clegoues.genprog4java.main.ClassInfo;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.main.Utils;
import clegoues.genprog4java.mut.HistoryEle;
import clegoues.genprog4java.mut.JavaEditOperation;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.util.Pair;

public class JavaRepresentation extends
FaultLocRepresentation<JavaEditOperation> {
	protected Logger logger = Logger.getLogger(JavaRepresentation.class);

	private static HashMap<Integer, JavaStatement> codeBank = new HashMap<Integer, JavaStatement>();
	private static HashMap<Integer, JavaStatement> base = new HashMap<Integer, JavaStatement>();
	private static HashMap<ClassInfo, CompilationUnit> baseCompilationUnits = new HashMap<ClassInfo, CompilationUnit>();
	private static HashMap<Integer, ArrayList<Integer>> lineNoToAtomIDMap = new HashMap<Integer, ArrayList<Integer>>();
	private static HashMap<ClassInfo, String> originalSource = new HashMap<ClassInfo, String>();
	private static HashMap<Integer, ClassInfo> stmtToFile = new HashMap<Integer, ClassInfo>();
	private int mutationNumber = 0;

	// semantic check cache stuff, so we don't have to walk stuff a million
	// times unnecessarily
	// should be the same for all of append, replace, and swap, so we only need
	// the one.
	private static String semanticCheck = "scope";
	private static int stmtCounter = 0;

	private static HashMap<Integer, TreeSet<WeightedAtom>> scopeSafeAtomMap = new HashMap<Integer, TreeSet<WeightedAtom>>();
	private static HashMap<Integer, Set<String>> inScopeMap = new HashMap<Integer, Set<String>>();
	private static TreeSet<Pair<String,String>> methodReturnType = new TreeSet<Pair<String,String>>();


	private ArrayList<JavaEditOperation> genome = new ArrayList<JavaEditOperation>();

	public static void configure(Properties prop) {
		if (prop.getProperty("semantic-check") != null) {
			JavaRepresentation.semanticCheck = prop.getProperty(
					"semantic-check").trim(); // options: scope, none
		}
	}

	public JavaRepresentation(ArrayList<HistoryEle> history,
			ArrayList<JavaEditOperation> genome2,
			ArrayList<WeightedAtom> arrayList,
			ArrayList<WeightedAtom> arrayList2) {
		super(history, genome2, arrayList, arrayList2);
	}

	public JavaRepresentation() {
		super();
	}

	private static HashMap<ClassInfo, String> getOriginalSource() {
		return originalSource;
	}

	protected void instrumentForFaultLocalization() {
		// needs nothing for Java. Don't love the "doing coverage" boolean flag
		// thing, but it's possible I just decided it's fine.
	}

	// Java-specific coverage stuff:

	private ExecutionDataStore executionData = null;

	protected ArrayList<Integer> atomIDofSourceLine(int lineno) {
		return lineNoToAtomIDMap.get(lineno);
	}

	public TreeSet<Integer> getCoverageInfo() throws IOException {
		TreeSet<Integer> atoms = new TreeSet<Integer>();

		for (Map.Entry<ClassInfo, String> ele : JavaRepresentation.originalSource
				.entrySet()) {
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
					default:
						break;
					}
					if (covered) {
						coveredLines.add(i);
					}
				}
			}
			for (int line : coveredLines) {
				ArrayList<Integer> atomIds = this.atomIDofSourceLine(line);
				if (atomIds != null && atomIds.size() >= 0) {
					atoms.addAll(atomIds);
				}
			}
		}
		return atoms;
	}

	public void fromSource(ClassInfo pair) throws IOException {
		// load here, get all statements and the compilation unit saved
		// parser can visit at the same time to collect scope info
		// apparently names and types and scopes are visited here below in
		// the calls to ASTUtils

		// we can assume that that's what Configuration.globalExtension is,
		// because we're in JavaRepresentation
		ScopeInfo scopeInfo = new ScopeInfo();
		JavaParser myParser = new JavaParser(scopeInfo);
		// originalSource entire class file written as a string
		String path = Configuration.outputDir +  "/original/" + pair.pathToJavaFile();
		String source = FileUtils.readFileToString(new File(path));
		JavaRepresentation.originalSource.put(pair, source);

		myParser.parse(path, Configuration.libs.split(File.pathSeparator));
		List<ASTNode> stmts = myParser.getStatements();
		baseCompilationUnits.put(pair, myParser.getCompilationUnit());
		JavaRepresentation.methodReturnType.addAll(myParser.getMethodReturnTypeSet());

		for (ASTNode node : stmts) {
			if (JavaRepresentation.canRepair(node)) {
				JavaStatement s = new JavaStatement();
				s.setStmtId(stmtCounter);
				stmtCounter++;
				int lineNo = ASTUtils.getLineNumber(node);
				s.setLineno(lineNo);
				s.setNames(ASTUtils.getNames(node));
				s.setTypes(ASTUtils.getTypes(node));
				s.setRequiredNames(ASTUtils.getScope(node));
				s.setASTNode(node);
				ArrayList<Integer> lineNoList = null;
				if (lineNoToAtomIDMap.containsKey(lineNo)) {
					lineNoList = lineNoToAtomIDMap.get(lineNo);
				} else {
					lineNoList = new ArrayList<Integer>();
				}
				lineNoList.add(s.getStmtId());
				lineNoToAtomIDMap.put(lineNo, lineNoList);
				if (semanticCheck.equals("scope")
						|| semanticCheck.equals("none")
						|| semanticCheck.equals("javaspecial")) {
					base.put(s.getStmtId(), s);
					codeBank.put(s.getStmtId(), s);
					stmtToFile.put(s.getStmtId(),pair);
				}
				scopeInfo.addScope4Stmt(s.getASTNode(), myParser.getFields());
				JavaRepresentation.inScopeMap.put(s.getStmtId(),
						scopeInfo.getScope(s.getASTNode()));
			}
			/*if(node instanceof MethodDeclaration){
				methodReturnType.addAll(myParser.getMethodReturnTypeSet());
			}*/

		}
	}

	public static boolean canRepair(ASTNode node) {


		return node instanceof AssertStatement 
				|| node instanceof Block
				//|| node instanceof MethodInvocation
				|| node instanceof BreakStatement
				|| node instanceof ConstructorInvocation
				|| node instanceof ContinueStatement
				|| node instanceof DoStatement
				|| node instanceof EmptyStatement
				|| node instanceof EnhancedForStatement
				|| node instanceof ExpressionStatement
				|| node instanceof ForStatement 
				|| node instanceof IfStatement
				|| node instanceof LabeledStatement
				|| node instanceof ReturnStatement
				|| node instanceof SuperConstructorInvocation
				|| node instanceof SwitchCase
				|| node instanceof SwitchStatement
				|| node instanceof SynchronizedStatement
				|| node instanceof ThrowStatement
				|| node instanceof TryStatement
				|| node instanceof TypeDeclarationStatement
				|| node instanceof WhileStatement;
	}

	public ArrayList<JavaEditOperation> getGenome() {
		return this.genome;
	}

	@Override
	public void loadGenomeFromString(String genome) {
		// TODO Auto-generated method stub

	}

	public void setGenome(List<JavaEditOperation> genome) {
		this.genome = new ArrayList<JavaEditOperation>(genome);
	}

	@Override
	public int genomeLength() {
		if (genome == null) {
			return 0;
		}
		return genome.size();
	}

	@Override
	public void serialize(String filename, ObjectOutputStream fout,
			boolean globalinfo) {
		// fout is going to be null for sure until I implement a subclass, but
		// whatever
		ObjectOutputStream out = null;
		FileOutputStream fileOut = null;
		try {
			if (fout == null) {
				fileOut = new FileOutputStream(filename + ".ser");
				out = new ObjectOutputStream(fileOut);
			} else {
				out = fout;
			}
			super.serialize(filename, out, globalinfo);
			out.writeObject(this.genome);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (fout == null) {
					if (out != null)
						out.close();
					if (fileOut != null)
						fileOut.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean deserialize(String filename, ObjectInputStream fin,
			boolean globalinfo) {
		ObjectInputStream in = null;
		FileInputStream fileIn = null;
		boolean succeeded = true;
		try {
			if (fin == null) {
				fileIn = new FileInputStream(filename + ".ser");
				in = new ObjectInputStream(fileIn);
			} else {
				in = fin;
			}
			if (super.deserialize(filename, in, globalinfo)) {
				if (globalinfo) {
					// OK, tragically none of the dom.ASTNode stuff is
					// serializable, and it's *really* not obvious
					// how to fix that. So we need to parse the file again,
					// which is a total bummer.
					// this is still worth doing for the genome thing below, I
					// guess, in particular
					// because it allows us to serialize/deserialize incoming
					// populations
					//					this.fromSource(filename.replace('.', '/')
					//							+ Configuration.globalExtension);
					// FIXME: deserialize needs fixed; fromSource wants a classname and package, now....
				}
				this.genome.addAll((ArrayList<JavaEditOperation>) (in
						.readObject()));
				logger.info("javaRepresentation: " + filename + "loaded\n");
			} else {
				succeeded = false;
			}
		} catch (ClassNotFoundException e) {
			logger.error("ClassNotFoundException in deserialize " + filename
					+ " which is probably *not* OK");
			e.printStackTrace();
			succeeded = false;
		} catch (IOException e) {
			logger.error("IOException in deserialize " + filename
					+ " which is probably OK");
			succeeded = false;
		} finally {
			try {
				if (fin == null) {
					if (in != null)
						in.close();
					if (fileIn != null)
						fileIn.close();
				}
			} catch (IOException e) {
				System.err
				.println("javaRepresentation: IOException in file close in deserialize "
						+ filename + " which is weird?");
				e.printStackTrace();
			}
		}
		return succeeded;
	}

	@Override
	public void outputSource(String filename) {
		// TODO Auto-generated method stub

	}

	@Override
	protected ArrayList<Pair<ClassInfo, String>> internalComputeSourceBuffers() {
		ArrayList<Pair<ClassInfo, String>> retVal = new ArrayList<Pair<ClassInfo, String>>();
		for (Map.Entry<ClassInfo, String> pair : JavaRepresentation
				.getOriginalSource().entrySet()) {
			ClassInfo ci = pair.getKey();
			String filename = ci.getClassName();
			String source = pair.getValue();
			Document original = new Document(source);
			CompilationUnit cu = baseCompilationUnits.get(ci);
			AST ast = cu.getAST();
			ASTRewrite rewriter = ASTRewrite.create(ast);

			try {
				for (JavaEditOperation edit : genome) {
					if(edit.getFileInfo().getClassName().equalsIgnoreCase(filename)){
						edit.edit(rewriter, ast, cu);
					}
				}

				TextEdit edits = null;

				edits = rewriter.rewriteAST(original, null);
				edits.apply(original);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				return null;
			} catch (MalformedTreeException e) {
				e.printStackTrace();
				return null;
			} catch (BadLocationException e) {
				e.printStackTrace();
				return null;
			} catch (ClassCastException e) {
				e.printStackTrace();
				return null;
			}
			// FIXME: I sense there's a better way to signify that
			// computeSourceBuffers failed than
			// to return null at those catch blocks

			retVal.add(new Pair<ClassInfo, String>(ci, original.get()));
		}
		return retVal;
	}

	@Override
	protected CommandLine internalTestCaseCommand(String exeName,
			String fileName, TestCase test) {
		// read in the test files to get a list of test class names
		// store it in the testcase object, which will be the name
		// this is a little strange because each test class has multiple
		// test cases in it. I think we can actually change this behavior
		// through various
		// hacks on StackOverflow, but for the time being I just want something
		// that works at all
		// rather than a perfect implementation. One thing at a time.
		CommandLine command = CommandLine.parse(Configuration.javaVM);
		String outputDir = "";

		if (this.doingCoverage) {
			outputDir =  Configuration.outputDir + File.separator
					+ "coverage/coverage.out/";
			//+ System.getProperty("path.separator") + ":"
			//		+ Configuration.outputDir + File.separator + exeName + "/";
		} else {
			String variantName = this.getVariantFolder();
			if(variantName!=null && !variantName.equalsIgnoreCase("")){
				outputDir += Configuration.outputDir + File.separator 
						+ variantName + File.separator + ":";
			}
			outputDir += Configuration.outputDir + File.separator + exeName + "/";
		}
		String classPath = outputDir + System.getProperty("path.separator")
		+ Configuration.libs + System.getProperty("path.separator") 
		+ Configuration.classTestFolder; 
		if(Configuration.classSourceFolder != "") {
			classPath += System.getProperty("path.separator") + Configuration.classSourceFolder;
		}
		// Positive tests
		command.addArgument("-classpath");
		command.addArgument(classPath);

		if (this.doingCoverage) {

			command.addArgument("-Xmx1024m");
			command.addArgument("-javaagent:" + Configuration.jacocoPath
					+ "=excludes=org.junit.*,append=false");
		} else {
			command.addArgument("-Xms128m");
			command.addArgument("-Xmx256m");
			command.addArgument("-client");
		}

		command.addArgument("clegoues.genprog4java.fitness.JUnitTestRunner");

		command.addArgument(test.toString());
		logger.info("Command: " + command.toString());
		return command;

	}
	private void editHelper(int location, int fixCode, Mutation mutType) {
		JavaStatement locationStatement = base.get(location);
		JavaStatement fixCodeStatement = codeBank.get(fixCode);
		ClassInfo fileName = stmtToFile.get(location);
		JavaEditOperation newEdit = new JavaEditOperation(mutType, fileName,
				locationStatement, fixCodeStatement);
		this.genome.add(newEdit);
	}

	public void performEdit(Mutation edit, int dst, int source) {
		super.performEdit(edit, dst, source);
		switch(edit) {
		case DELETE: 
		case NULLINSERT:
		case RANGECHECK:
		case LBOUNDSET:
		case UBOUNDSET:
		case OFFBYONE:
		case NULLCHECK:
			JavaStatement locationStatement = base.get(dst);
			ClassInfo fileName = stmtToFile.get(dst);
			JavaEditOperation newEdit = new JavaEditOperation(fileName, locationStatement,
					Mutation.DELETE);
			this.genome.add(newEdit);
			break;
		case APPEND:
		case REPLACE: this.editHelper(dst, source, edit);
		break;
		case SWAP:
			JavaStatement loc = base.get(dst);
			JavaStatement fixCodeStatement = base.get(source);
			ClassInfo swapFileName = stmtToFile.get(dst);
			JavaEditOperation swapEdit = new JavaEditOperation(Mutation.SWAP, swapFileName, 
					loc, fixCodeStatement);
			this.genome.add(swapEdit);
			break;
			default: logger.fatal("unhandled edit template type in performEdit; this should be impossible (famous last words...)");
		}
	}


	@Override
	protected boolean internalCompile(String progName, String exeName) {

		List<Pair<ClassInfo, String>> sourceBuffers = this.computeSourceBuffers();
		if (sourceBuffers == null) {
			return false;
		}
		String outDirName = Configuration.outputDir + File.separatorChar
				+ exeName + File.separatorChar ;

		File sanRepDir = new File(Configuration.outputDir + File.separatorChar+ exeName);
		if (!sanRepDir.exists()){
			sanRepDir.mkdir();
		}


		File mutDir = new File(outDirName);
		if (!mutDir.exists()){
			mutDir.mkdir();
		}

		try {
			for (Pair<ClassInfo, String> ele : sourceBuffers) {
				ClassInfo ci = ele.getFirst();
				String program = ele.getSecond();
				String pathToFile = ci.pathToJavaFile();


				createPathFiles(outDirName, pathToFile);

				BufferedWriter bw = new BufferedWriter(new FileWriter(
						outDirName + File.separatorChar + pathToFile));
				bw.write(program);
				bw.flush();

				bw.close();
				if(Configuration.compileCommand != "") {
					String path = 
							Configuration.workingDir+ File.separatorChar + Configuration.sourceDir+ File.separatorChar + pathToFile; 

					BufferedWriter bw2 = new BufferedWriter(new FileWriter(path)); 
					bw2.write(program);
					bw2.flush();
					bw2.close();
				}	
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}

		if(Configuration.compileCommand == "") {
			Iterable<? extends JavaFileObject> fileObjects = ASTUtils
					.getJavaSourceFromString(progName, sourceBuffers);

			LinkedList<String> options = new LinkedList<String>();

			options.add("-cp");
			options.add(Configuration.libs);

			options.add("-source");
			options.add(Configuration.sourceVersion);

			options.add("-target");
			options.add(Configuration.targetVersion);

			options.add("-d");

			File outDirFile = new File(outDirName);
			if (!outDirFile.exists())
				outDirFile.mkdir();
			options.add(outDirName);

			StringWriter compilerErrorWriter = new StringWriter();
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

			// Here is where it runs the command to compile the code
			if (!compiler.getTask(compilerErrorWriter, null, null, options,
					null, fileObjects).call()) {
				logger.error(compilerErrorWriter.toString());
				compilerErrorWriter.flush();
				return false;
			} else {
				return true;
			}
		} else {
			return Utils.runCommand(Configuration.compileCommand);
		}
	}

	private void createPathFiles(String base, String pathToFile){
		pathToFile = pathToFile.substring(0,pathToFile.lastIndexOf(File.separatorChar));
		String[] array = pathToFile.split(String.valueOf(File.separatorChar));

		for(String s : array){
			File fileName = new File(base + File.separatorChar + s);
			if (!fileName.exists()){
				fileName.mkdir();
			}
			base += File.separatorChar + s;
		}
	}


	public JavaRepresentation copy() {
		JavaRepresentation copy = new JavaRepresentation(this.getHistory(),
				this.getGenome(), this.getFaultyAtoms(),
				this.getFixSourceAtoms());
		return copy;
	}

	private TreeSet<WeightedAtom> scopeHelper(int stmtId) {
		if (JavaRepresentation.scopeSafeAtomMap.containsKey(stmtId)) {
			return JavaRepresentation.scopeSafeAtomMap.get(stmtId);
		}
		JavaStatement locationStmt = codeBank.get(stmtId);
		// I *believe* this is just variable names and doesn't check required
		// types, which are also collected
		// at parse time and thus could be considered here.
		Set<String> inScopeAt = JavaRepresentation.inScopeMap.get(locationStmt
				.getStmtId());
		TreeSet<WeightedAtom> retVal = new TreeSet<WeightedAtom>();
		for (WeightedAtom atom : this.getFixSourceAtoms()) {
			int index = atom.getAtom();
			JavaStatement stmt = codeBank.get(index);
			Set<String> requiredScopes = stmt.getRequiredNames();

			boolean ok = true;
			for (String req : requiredScopes) {
				if (!inScopeAt.contains(req)) {
					ok = false;
					break;
				}
			}

			if(stmt.getASTNode() instanceof MethodRef){
				MethodRef mr = (MethodRef) stmt.getASTNode();
				Pair<String,String> methodRefInMrt = mrtContainsMethodName(mr.getName().toString());
				if(methodRefInMrt != null){
					if( methodRefInMrt.getSecond().equalsIgnoreCase("void") || methodRefInMrt.getSecond().equalsIgnoreCase("null")){
						ok=false;
						break;
					}
				}
			}

			if (ok) {
				retVal.add(atom);
			}

		}
		JavaRepresentation.scopeSafeAtomMap.put(stmtId, retVal);
		return retVal;
	}

	private Pair<String,String> mrtContainsMethodName(String matchString){
		for (Pair<String,String> p : methodReturnType) {
			if(p.getFirst().equalsIgnoreCase(matchString)){
				return p;
			}
		}
		return null;
	}
	
	@Override
	public Boolean doesEditApply(int location, Mutation editType) {
		switch(editType) {
		case APPEND: 
		case REPLACE:
		case SWAP:
			return this.editSources(location,  editType).size() > 0;
		case DELETE: return true; // possible FIXME: not always true in Java?
		case NULLCHECK: 
			break; 
		case NULLINSERT:
		case FUNREP:
		case PARREP:
		case PARADD:
		case PARREM:
		case EXPREP:
		case EXPADD:
		case EXPREM:
		case OBJINIT:
		case RANGECHECK:
		case SIZECHECK:
		case CASTCHECK:
		case LBOUNDSET:
		case UBOUNDSET:
		case OFFBYONE:
			logger.fatal("Unhandled edit type in DoesEditApply.  Handle it in JavaRepresentation and try again.");
			break;
		}
		return false;
	}


	@Override
	public TreeSet<WeightedAtom> editSources(int stmtId, Mutation editType) {
		switch(editType) {
		case APPEND: 	
		case REPLACE:
			if (JavaRepresentation.semanticCheck.equals("scope")) {
			return this.scopeHelper(stmtId);
			} else {
				return super.editSources(stmtId, editType);
			}
		case DELETE: 
			TreeSet<WeightedAtom> retval = new TreeSet<WeightedAtom>();
			retval.add(new WeightedAtom(stmtId, 1.0));
			return retval;
		case SWAP:
			if (JavaRepresentation.semanticCheck.equals("scope")) {
				TreeSet<WeightedAtom> retVal = new TreeSet<WeightedAtom>();
				for (WeightedAtom item : this.scopeHelper(stmtId)) {
					int atom = item.getAtom();
					TreeSet<WeightedAtom> inScopeThere = this.scopeHelper(atom);
					boolean containsThisAtom = false;
					for (WeightedAtom there : inScopeThere) {
						if (there.getAtom() == stmtId) {
							containsThisAtom = true;
							break;
						}
					}
					if (containsThisAtom)
						retVal.add(item);
				}
				return retVal;
			} else {
				return super.editSources(stmtId, editType);
			}
		case NULLINSERT:
		case FUNREP:
		case PARREP:
		case PARADD:
		case PARREM:
		case EXPREP:
		case EXPADD:
		case EXPREM:
		case NULLCHECK:
		case OBJINIT:
		case RANGECHECK:
		case SIZECHECK:
		case CASTCHECK:
		case LBOUNDSET:
		case UBOUNDSET:
		case OFFBYONE:
		default:
			// IMPORTANT FIXME FOR MANISH AND MAU: you must add handling here to check legality for templates as you add them.
			// if a template always applies, then you can move the template type to the DELETE case, above.
			// however, I don't think most templates always apply; it doesn't make sense to null check a statement in which nothing
			// could conceivably be null, for example.
			logger.fatal("Unhandled template type in editSources!  Fix code in JavaRepresentation to do this properly.");
			return new TreeSet<WeightedAtom>();
		}
	}

	@Override
	protected void printDebugInfo() {
		ArrayList<WeightedAtom> buggyStatements = this.getFaultyAtoms();
		for (WeightedAtom atom : buggyStatements) {
			int atomid = atom.getAtom();
			JavaStatement stmt = JavaRepresentation.base.get(atomid);
			ASTNode actualStmt = stmt.getASTNode();
			String stmtStr = actualStmt.toString();
			logger.debug("statement " + atomid + " at line " + stmt.getLineno()
			+ ": " + stmtStr);
			logger.debug("\t Names:");
			for (String name : stmt.getNames()) {
				logger.debug("\t\t" + name);
			}
			logger.debug("\t Scopes:");
			for (String scope : stmt.getRequiredNames()) {
				logger.debug("\t\t" + scope);
			}
			logger.debug("\t Types:");
			for (String t : stmt.getTypes()) {
				logger.debug("\t\t" + t);
			}
		}

	}

	public void test() {
		String newName = CachingRepresentation.newVariantFolder();
		internalCompile(newName, newName);
	}

}