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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
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
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import clegoues.genprog4java.Search.Search;
import clegoues.genprog4java.fitness.TestCase;
import clegoues.genprog4java.java.ASTUtils;
import clegoues.genprog4java.java.JavaParser;
import clegoues.genprog4java.java.JavaSemanticInfo;
import clegoues.genprog4java.java.JavaSourceInfo;
import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.java.SemanticInfoVisitor;
import clegoues.genprog4java.localization.EntropyLocalization;
import clegoues.genprog4java.localization.Localization;
import clegoues.genprog4java.localization.Location;
import clegoues.genprog4java.java.ClassInfo;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.WeightedHole;
import clegoues.genprog4java.mut.WeightedMutation;
import clegoues.genprog4java.mut.edits.java.JavaEditFactory;
import clegoues.genprog4java.mut.edits.java.JavaEditOperation;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.holes.java.JavaStatementLocation;
import clegoues.util.ConfigurationBuilder;
import clegoues.util.GlobalUtils;

public class JavaRepresentation extends
CachingRepresentation<JavaEditOperation> {
	protected Logger logger = Logger.getLogger(JavaRepresentation.class);

	private JavaEditFactory editFactory = new JavaEditFactory();

	public static final ConfigurationBuilder.RegistryToken token =
			ConfigurationBuilder.getToken();

	public JavaSourceInfo sourceInfo = new JavaSourceInfo();
	public  static JavaSemanticInfo semanticInfo = new JavaSemanticInfo();

	public static int stmtCounter = 0;

	private ArrayList<JavaEditOperation> genome = new ArrayList<JavaEditOperation>();

	public JavaRepresentation(ArrayList<JavaEditOperation> genome2,
			Localization localizationInfo) {
		super(genome2);
		this.localization = localizationInfo; // FIXME make a legit copy of this for copy?
		if(localizationInfo instanceof EntropyLocalization) {
			editFactory.setEntropyLocalization((EntropyLocalization) localization);
		}
	}

	public JavaRepresentation() {
		super();
	}

	@Override
	public List<WeightedMutation> availableMutations(Location atomId) {
		List<WeightedMutation> retVal = new LinkedList<WeightedMutation>();
		for (WeightedMutation mutation : Search.availableMutations) {
			if(this.doesEditApply(atomId, (Mutation) mutation.getKey())) {
				retVal.add(new WeightedMutation((Mutation) mutation.getKey(), (Double) mutation.getValue()));
			}
		}
		return retVal;
	}

	// Java-specific coverage stuff:

	@Override
	public ArrayList<Integer> atomIDofSourceLine(int lineno) {
		return sourceInfo.atomIDofSourceLine(lineno);
	}
	
	public ClassInfo getFileFromStmt(int stmtId){
		return sourceInfo.getFileFromStmt(stmtId);
	}

	@Override
	public void fromSource(ClassInfo pair, String path, File sourceFile) throws IOException {

		SemanticInfoVisitor visitor = new SemanticInfoVisitor();
		JavaParser myParser = new JavaParser(visitor);
		String source = FileUtils.readFileToString(sourceFile);
		sourceInfo.addToOriginalSource(pair, source);

		myParser.parse(path, Configuration.libs.split(File.pathSeparator));

		List<ASTNode> stmts = myParser.getStatements();
		sourceInfo.addToBaseCompilationUnits(pair, myParser.getCompilationUnit());
		
		for (ASTNode node : stmts) {
			if (JavaRepresentation.canRepair(node)) {
				JavaStatement s = new JavaStatement(stmtCounter, node, pair);
				sourceInfo.augmentLineInfo(stmtCounter, node);
				sourceInfo.storeStmtInfo(s, pair);
				
				s.setRequiredNames(visitor.getRequiredNames(node)); // now in a map in the visitor
				s.setNamesDeclared(visitor.getNamesDeclared(node));
				
				semanticInfo.addToClassScopeMap(s, visitor.getClassScope());
				semanticInfo.addToMethodScopeMap(s, visitor.getMethodScope(node));
				semanticInfo.collectFinalVarInfo(s, visitor.getFinalVarInfo(node));
				stmtCounter++;

			}
		}	
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

			retVal.add(Pair.of(ci, original.get()));
		}
		return retVal;
	}

	protected CommandLine internalTestCaseCommand(String exeName,
			String fileName, TestCase test) {
		return this.internalTestCaseCommand(exeName, fileName, test, false);
	}

	@Override
	public CommandLine internalTestCaseCommand(String exeName,
			String fileName, TestCase test, boolean doingCoverage) {
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

		if (doingCoverage) {
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

		if (doingCoverage) {

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
				ClassInfo ci = ele.getLeft();
				String program = ele.getRight();
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
			options.add(Configuration.sourceVersion);

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
				this.getGenome(), this.localization);
		return copy;
	}

	@Override
	public Boolean doesEditApply(Location location, Mutation editType) {
		return editFactory.doesEditApply(this,location,editType); 
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List<WeightedHole> editSources(Location location, Mutation editType) {
		return editFactory.editSources(this,location,editType);
	}


	@SuppressWarnings("rawtypes")
	@Override
	public Location instantiateLocation(Integer i, double negWeight) {
		if(this.sourceInfo.getLocationInformation().containsKey(i)) {
			return this.sourceInfo.getLocationInformation().get(i);
		}
		JavaStatement stmt = sourceInfo.getBase().get(i);
		JavaStatementLocation location = new JavaStatementLocation(stmt, negWeight);
		location.setClassInfo(stmt.getClassInfo());
		this.sourceInfo.getLocationInformation().put(i, location);
		return location;
	}


	public HashMap<Integer, JavaStatement> getCodeBank() {
		return sourceInfo.getCodeBank(); 
	}

	@Override
	public Localization getLocalization() {
		return this.localization;
	}

	@Override
	public Boolean shouldBeRemovedFromFix(WeightedAtom potentialFixAtom) {
		// Possible FIXME: I don't really like having this here, but it needs enough
		// variant-specific info that it's hard to put into localization where it may actually belong
		int index = potentialFixAtom.getAtom();
		JavaStatement potentialFixStmt = getFromCodeBank(index); 
		ASTNode fixASTNode = potentialFixStmt.getASTNode();

		//Don't make a call to a constructor
		if(fixASTNode instanceof MethodRef){
			MethodRef mr = (MethodRef) potentialFixStmt.getASTNode();
			// mrt = method return type
			String returnType = semanticInfo.returnTypeOfThisMethod(mr.getName().toString());
			if(returnType != null && returnType.equalsIgnoreCase("null")) {
				return true;
			}
		}

		// we collected this info on parse.  If the block contains any type of assignment to
		// or declaration of a final variable, this should be true.
		if(semanticInfo.getFinalVarStatus(index)) {
			return true;
		}
				

		return false;
	}

	@Override
	public Map<ClassInfo, String> getOriginalSource() {
		return sourceInfo.getOriginalSource();
	}

	@Override
	protected void printDebugInfo() {
		// TODO Auto-generated method stub
		
	}
}