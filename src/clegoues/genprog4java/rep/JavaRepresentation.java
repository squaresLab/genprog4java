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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
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

import clegoues.genprog4java.Search.GiveUpException;
import clegoues.genprog4java.Search.Search;
import clegoues.genprog4java.fitness.TestCase;
import clegoues.genprog4java.java.ASTUtils;
import clegoues.genprog4java.java.ClassInfo;
import clegoues.genprog4java.java.JavaParser;
import clegoues.genprog4java.java.JavaSemanticInfo;
import clegoues.genprog4java.java.JavaSourceInfo;
import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.java.ScopeInfo;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Location;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.WeightedHole;
import clegoues.genprog4java.mut.WeightedMutation;
import clegoues.genprog4java.mut.edits.java.JavaEditFactory;
import clegoues.genprog4java.mut.edits.java.JavaEditOperation;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.util.ConfigurationBuilder;
import clegoues.util.GlobalUtils;
import clegoues.util.Pair;

public class JavaRepresentation extends
FaultLocRepresentation<JavaEditOperation> {
	protected Logger logger = Logger.getLogger(JavaRepresentation.class);

	private JavaEditFactory editFactory = new JavaEditFactory();

	public static final ConfigurationBuilder.RegistryToken token =
			ConfigurationBuilder.getToken();

	private JavaSourceInfo sourceInfo = new JavaSourceInfo();
	public JavaSemanticInfo semanticInfo = new JavaSemanticInfo();

	private static int stmtCounter = 0;

	private ArrayList<JavaEditOperation> genome = new ArrayList<JavaEditOperation>();

	public JavaRepresentation(ArrayList<JavaEditOperation> genome2,
			ArrayList<Location> arrayList,
			ArrayList<WeightedAtom> arrayList2) {
		super(genome2, arrayList, arrayList2);
	}

	public JavaRepresentation() {
		super();
	}

	protected void instrumentForFaultLocalization() {
		// needs nothing for Java. Don't love the "doing coverage" boolean flag
		// thing, but it's possible I just decided it's fine.
	}

	// Java-specific coverage stuff:

	private ExecutionDataStore executionData = null;

	protected ArrayList<Integer> atomIDofSourceLine(int lineno) {
		return sourceInfo.atomIDofSourceLine(lineno);
	}

	public TreeSet<Integer> getCoverageInfo() throws IOException {
		TreeSet<Integer> atoms = new TreeSet<Integer>();

		for (Map.Entry<ClassInfo, String> ele : sourceInfo.getOriginalSource()
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
				ArrayList<Integer> atomIds = this.atomIDofSourceLine(line);
				if (atomIds != null && atomIds.size() >= 0) {
					atoms.addAll(atomIds);
				}
			}
		}
		return atoms;
	}
	
	static int dumpCount = 0;
	private void dumpStmts() {
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter("/Users/clegoues/Desktop/stmtinfo" + dumpCount + ".txt"));
	
		dumpCount++;
		HashMap<Integer,JavaStatement> base = sourceInfo.getBase();
		HashMap<Integer, JavaStatement> codeBank = sourceInfo.getCodeBank();
		
		bw.write("::BASE::\n");
		for(Map.Entry<Integer,JavaStatement> s : base.entrySet()) {
			JavaStatement node = s.getValue();
			bw.write("\n\nStmt id: " + s.getKey() + " node: " + node.getASTNode());
			bw.write("in class scope here: [[");
			bw.write((JavaSemanticInfo.classScopeMap.get(node.getStmtId())).toString());
			bw.write("in method scope here: [[");
			bw.write((JavaSemanticInfo.methodScopeMap.get(node.getStmtId())).toString());
			bw.write(" ]]\n");
			bw.write("required names: [[");
			bw.write((node.getRequiredNames()).toString()); 
			bw.write("]] \n\n");
		
		}
		bw.write("::CODEBANK::");
		
		for(Map.Entry<Integer,JavaStatement> s : codeBank.entrySet()) {
			JavaStatement node = s.getValue();
			bw.write("\n\nStmt id: " + s.getKey() + " node: " + node.getASTNode());
		}
	
		bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	
	private void fromSource(ClassInfo pair, String path, File sourceFile) throws IOException {

		ScopeInfo scopeInfo = new ScopeInfo();
		JavaParser myParser = new JavaParser(scopeInfo);
		String source = FileUtils.readFileToString(sourceFile);
		sourceInfo.addToOriginalSource(pair, source);

		myParser.parse(path, Configuration.libs.split(File.pathSeparator));

		List<ASTNode> stmts = myParser.getStatements();
		sourceInfo.addToBaseCompilationUnits(pair, myParser.getCompilationUnit());
		semanticInfo.addAllSemanticInfo(myParser);
		
		Set<String> knownTypesInScope = myParser.getAvailableTypes();
		Set<String> knownMethodsAndFields = myParser.getAvailableMethodsAndFields();
		
		for (ASTNode node : stmts) {
			if (JavaRepresentation.canRepair(node)) {
				JavaStatement s = new JavaStatement();
				s.setStmtId(stmtCounter);
				s.setClassInfo(pair);
				s.setInfo(stmtCounter, node);
				stmtCounter++;

				sourceInfo.augmentLineInfo(s.getStmtId(), node);
				sourceInfo.storeStmtInfo(s, pair);
				s.setRequiredNames(scopeInfo.getRequiredNames(node));
				s.setNamesDeclared(scopeInfo.getNamesDeclared(node));
				scopeInfo.addToClassScope(knownTypesInScope);
				scopeInfo.addToClassScope(knownMethodsAndFields);
				
				// possible FIXME: more than one class per compilation unit will probably break this.
				semanticInfo.addToClassScopeMap(s, scopeInfo.getClassScope());
				semanticInfo.addToMethodScopeMap(s, scopeInfo.getMethodScope(node));
				semanticInfo.collectFinalVarInfo(s, scopeInfo.getFinalVarInfo(node));
			}
		}	

		dumpStmts();
	}

	public void fromSource(ClassInfo pair) throws IOException {
		// load here, get all statements and the compilation unit saved
		// parser can visit at the same time to collect scope info
		// apparently names and types and scopes are visited here below in
		// the calls to ASTUtils

		// originalSource entire class file written as a string
		String path = Configuration.outputDir +  "/original/" + pair.pathToJavaFile();
		this.fromSource(pair, path, new File(path));
	}


	public static boolean canRepair(ASTNode node) { // FIXME: variable declarations that have bodies?
		return node instanceof AssertStatement 
				|| node instanceof Block
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
		//		|| node instanceof TypeDeclarationStatement
		//		|| node instanceof VariableDeclarationStatement
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
				//System.err.println("javaRepresentation: IOException in file close in deserialize " + filename + " which is weird?");
				logger.error("javaRepresentation: IOException in file close in deserialize "
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
		for (Map.Entry<ClassInfo, String> pair : sourceInfo.getOriginalSource().entrySet()) {
			ClassInfo ci = pair.getKey();
			String filename = ci.getClassName();
			String path = ci.getPackage();
			String source = pair.getValue();
			IDocument original = new Document(source);
			CompilationUnit cu = sourceInfo.getBaseCompilationUnits().get(ci);
			AST ast = cu.getAST();
			ASTRewrite rewriter = ASTRewrite.create(ast);

			try {
				for (JavaEditOperation edit : genome) {
					JavaLocation locationStatement = (JavaLocation) edit.getLocation();
					if(locationStatement.getClassInfo()!=null && locationStatement.getClassInfo().getClassName().equalsIgnoreCase(filename) && locationStatement.getClassInfo().getPackage().equalsIgnoreCase(path)){
						edit.edit(rewriter);
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
		+ Configuration.testClassPath + System.getProperty("path.separator") 
		+ Configuration.srcClassPath;
		//; 
		//		if(Configuration.classSourceFolder != "") {
		//			classPath += System.getProperty("path.separator") + Configuration.classSourceFolder;
		//		}
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
	//	logger.info("Command: " + command.toString());
		return command;

	}

	public JavaStatement getFromCodeBank(int atomId) {
		return sourceInfo.getCodeBank().get(atomId);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void performEdit(Mutation edit, Location dst, EditHole source) {
		JavaEditOperation thisEdit = this.editFactory.makeEdit(edit, dst, source);
		this.genome.add(thisEdit);
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
			return GlobalUtils.runCommand(Configuration.compileCommand);
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
		JavaRepresentation copy = new JavaRepresentation(
				this.getGenome(), this.getFaultyLocations(),
				this.getFixSourceAtoms());
		return copy;
	}

	public String returnTypeOfThisMethod(String matchString){
		for (Pair<String,String> p : semanticInfo.getMethodReturnTypes()) {
			if(p.getFirst().equalsIgnoreCase(matchString)){
				return p.getSecond();
			}
		}
		return null;
	}

	@Override
	public void reduceSearchSpace() throws GiveUpException {
		if(Search.checkSpace) {
		boolean thereIsAtLeastOneMutThatApplies;
		ArrayList<Location> locsToRemove = new ArrayList<Location>();
		for (Location potentiallyBuggyLoc : faultLocalization) {
			thereIsAtLeastOneMutThatApplies = false;
			Set<WeightedMutation> availableMutations = availableMutations(potentiallyBuggyLoc);
			if(availableMutations.isEmpty()){
				locsToRemove.add(potentiallyBuggyLoc);
			}else{
				for (Pair<Mutation, Double> mutation : availableMutations) {
					thereIsAtLeastOneMutThatApplies = thereIsAtLeastOneMutThatApplies || this.editFactory.doesEditApply(this, potentiallyBuggyLoc, mutation.getFirst());
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
		final ArrayList<WeightedAtom> toRemove = new ArrayList<WeightedAtom>();

		for (final WeightedAtom potentialFixAtom : this.getFixSourceAtoms()) {
			int index = potentialFixAtom.getAtom();
			JavaStatement potentialFixStmt = this.getFromCodeBank(index); 
			ASTNode fixASTNode = potentialFixStmt.getASTNode();

			//Don't make a call to a constructor
			if(fixASTNode instanceof MethodRef){
				MethodRef mr = (MethodRef) potentialFixStmt.getASTNode();
				// mrt = method return type
				String returnType = returnTypeOfThisMethod(mr.getName().toString());
				if(returnType != null && returnType.equalsIgnoreCase("null")) {
					toRemove.add(potentialFixAtom);
					continue;
				}
			}
			
			// we collected this info on parse.  If the block contains any type of assignment to
			// or declaration of a final variable, this should be true.
			if(semanticInfo.getFinalVarStatus(index)) {
				toRemove.add(potentialFixAtom);
			}
			
		}
		for(WeightedAtom atom : toRemove) {
			fixLocalization.remove(atom);
		}
	}


	@Override
	public Boolean doesEditApply(Location location, Mutation editType) {
		return editFactory.doesEditApply(this,location,editType); 
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List<WeightedHole> editSources(Location location, Mutation editType) {
		List<EditHole> holes = editFactory.editSources(this,location,editType);
		List<WeightedHole> retVal = new ArrayList<WeightedHole>();
		for(EditHole hole : holes) {
			// possible FIXME for later: weighting options equally for the time being.  One day
			// maybe we'll do something smarter...
			retVal.add(new WeightedHole(hole));
		}
		return retVal;
	}


	@SuppressWarnings("rawtypes")
	@Override
	protected void printDebugInfo() {
		ArrayList<Location> buggyLocations = this.getFaultyLocations();
		for (Location location : buggyLocations) {
			int atomid = ((JavaStatement) location.getLocation()).getStmtId(); 
			JavaStatement stmt = sourceInfo.getBase().get(atomid);
			ASTNode actualStmt = stmt.getASTNode();
			String stmtStr = actualStmt.toString();
			logger.debug("statement " + atomid + " at line " + stmt.getLineno()
			+ ": " + stmtStr);
		
			logger.debug("\t Required Names:");
			for (String scope : stmt.getRequiredNames()) {
				logger.debug("\t\t" + scope);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Location instantiateLocation(Integer i, double negWeight) {
		if(this.sourceInfo.getLocationInformation().containsKey(i)) {
			return this.sourceInfo.getLocationInformation().get(i);
		}
		JavaStatement stmt = sourceInfo.getBase().get(i);
		JavaLocation location = new JavaLocation(stmt, negWeight);
		location.setClassInfo(stmt.getClassInfo());
		this.sourceInfo.getLocationInformation().put(i, location);
		return location;
	}

	public void setAllPossibleStmtsToFixLocalization(){
		super.fixLocalization.clear();
		for(int i = 0; i < JavaRepresentation.stmtCounter; i++) {
			super.fixLocalization.add(new WeightedAtom(i,1.0));
		}
	}

	public void setAllPossibleStmtsToFaultyLocalization(){
		super.faultLocalization.clear();
		for(JavaStatement js : getCodeBank().values()) {	
			super.faultLocalization.add(this.instantiateLocation(js.getStmtId(), 0.1));
		}
	}
	
	public HashMap<Integer, JavaStatement> getCodeBank() {
		return sourceInfo.getCodeBank(); // FIXME: this is for the replacement model, there should be a better way to do this based on
		// hole fill-in information, but I just want this thing to compile for now.
	}

	@Override
	protected void computeFixSpace(TreeSet<Integer> negativePath, TreeSet<Integer> positivePath) {
		if(FaultLocRepresentation.fixStrategy.equalsIgnoreCase("packageScope")) {
			HashMap<ClassInfo,String> originalSource = sourceInfo.getOriginalSource();
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
					this.fromSource(newCi, packageFile.getPath(), packageFile);
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
			super.computeFixSpace(negativePath, positivePath);
		}
	}
}