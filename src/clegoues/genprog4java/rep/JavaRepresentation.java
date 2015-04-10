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
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
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
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.mut.HistoryEle;
import clegoues.genprog4java.mut.JavaEditOperation;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.util.Pair;

public class JavaRepresentation extends
		FaultLocRepresentation<JavaEditOperation> {
	protected Logger logger = LogManager.getLogger(this.getClass());

	private static HashMap<Integer, JavaStatement> codeBank = new HashMap<Integer, JavaStatement>();
	private static HashMap<Integer, JavaStatement> base = new HashMap<Integer, JavaStatement>();
	private static HashMap<String, CompilationUnit> baseCompilationUnits = new HashMap<String, CompilationUnit>();
	private static HashMap<Integer, ArrayList<Integer>> lineNoToAtomIDMap = new HashMap<Integer, ArrayList<Integer>>();
	private static HashMap<String, String> originalSource = new HashMap<String, String>();

	// semantic check cache stuff, so we don't have to walk stuff a million
	// times unnecessarily
	// should be the same for all of append, replace, and swap, so we only need
	// the one.
	private static String semanticCheck = "scope";
	private static int stmtCounter = 0;

	private static HashMap<Integer, TreeSet<WeightedAtom>> scopeSafeAtomMap = new HashMap<Integer, TreeSet<WeightedAtom>>();
	private static HashMap<Integer, Set<String>> inScopeMap = new HashMap<Integer, Set<String>>();

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

	private static HashMap<String, String> getOriginalSource() {
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

		for (Map.Entry<String, String> ele : JavaRepresentation.originalSource
				.entrySet()) {
			String targetClassName = ele.getKey();
			InputStream targetClass = new FileInputStream(new File(
					Configuration.outputDir + File.separator
							+ "coverage/coverage.out" + File.separator
							+ Configuration.packageName.replace(".", "/")
							+ File.separator + targetClassName + ".class"));

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
			analyzer.analyzeClass(targetClass);

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

	public void fromSource(String className) throws IOException {
		// load here, get all statements and the compilation unit saved
		// parser can visit at the same time to collect scope info
		// apparently names and types and scopes are visited here below in
		// the calls to ASTUtils

		String fname = Configuration.sourceDir + File.separatorChar
				+ className.replace(".", "/") + ".java";
		// we can assume that that's what Configuration.globalExtension is,
		// because we're in JavaRepresentation
		ScopeInfo scopeInfo = new ScopeInfo();
		JavaParser myParser = new JavaParser(scopeInfo);
		// originalSource entire class file written as a string
		String source = FileUtils.readFileToString(new File(fname));
		JavaRepresentation.originalSource.put(className, source);

		myParser.parse(fname, Configuration.libs.split(File.pathSeparator));
		List<ASTNode> stmts = myParser.getStatements();
		baseCompilationUnits.put(className, myParser.getCompilationUnit());

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
				}
				scopeInfo.addScope4Stmt(s.getASTNode(), myParser.getFields());
				JavaRepresentation.inScopeMap.put(s.getStmtId(),
						scopeInfo.getScope(s.getASTNode()));
			}

		}
	}

	public static boolean canRepair(ASTNode node) {
		return node instanceof AssertStatement || node instanceof Block
				|| node instanceof BreakStatement
				|| node instanceof ConstructorInvocation
				|| node instanceof ContinueStatement
				|| node instanceof DoStatement
				|| node instanceof EmptyStatement
				|| node instanceof EnhancedForStatement
				|| node instanceof ExpressionStatement
				|| node instanceof ForStatement || node instanceof IfStatement
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
					this.fromSource(Configuration.sourceDir
							+ File.separatorChar + filename
							+ Configuration.globalExtension);
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
	protected ArrayList<Pair<String, String>> internalComputeSourceBuffers() {
		ArrayList<Pair<String, String>> retVal = new ArrayList<Pair<String, String>>();
		for (Map.Entry<String, String> pair : JavaRepresentation
				.getOriginalSource().entrySet()) {
			String filename = pair.getKey();
			String source = pair.getValue();
			Document original = new Document(source);
			CompilationUnit cu = baseCompilationUnits.get(filename);
			ASTRewrite rewriter = ASTRewrite.create(cu.getAST());

			try {
				for (JavaEditOperation edit : genome) {
					edit.edit(rewriter, cu.getAST());
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

			retVal.add(new Pair<String, String>(filename, original.get()));
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
			outputDir = Configuration.outputDir + File.separator
					+ "coverage/coverage.out/"
					+ System.getProperty("path.separator")
					+ Configuration.outputDir + File.separator + exeName + "/";
		} else {
			outputDir = Configuration.outputDir + File.separator
					+ this.getName() + System.getProperty("path.separator")
					+ Configuration.outputDir + File.separator + exeName + "/";
		}
		String classPath = outputDir + System.getProperty("path.separator")
				+ Configuration.libs;
		// Positive tests
		command.addArgument("-classpath");
		command.addArgument(classPath);

		if (this.doingCoverage) {

			command.addArgument("-Xmx1024m");
			command.addArgument("-javaagent:" + Configuration.jacocoPath
					+ "=excludes=org.junit.*,append=false");
			// FIXME: I actually think we need this, no?
			// "-javaagent:"+Configuration.jacocoPath+"=excludes=" +
			// Configuration.testsDir+".*" + ",includes="+targetClassString
			// +",append=false");

		} else {
			command.addArgument("-Xms128m");
			command.addArgument("-Xmx256m");
			command.addArgument("-client");
		}

		command.addArgument("clegoues.genprog4java.fitness.JUnitTestRunner");

		command.addArgument(test.toString());
		// System.out.println(command.toString());
		return command;

	}

	@Override
	public void delete(int location) {
		super.delete(location);
		JavaStatement locationStatement = base.get(location);
		JavaEditOperation newEdit = new JavaEditOperation(locationStatement,
				Mutation.DELETE);
		this.genome.add(newEdit);
	}

	private void editHelper(int location, int fixCode, Mutation mutType) {
		JavaStatement locationStatement = base.get(location);
		JavaStatement fixCodeStatement = codeBank.get(fixCode);
		JavaEditOperation newEdit = new JavaEditOperation(mutType,
				locationStatement, fixCodeStatement);
		this.genome.add(newEdit);
	}

	@Override
	public void append(int whereToAppend, int whatToAppend) {
		super.append(whereToAppend, whatToAppend);
		this.editHelper(whereToAppend, whatToAppend, Mutation.APPEND);
	}

	@Override
	public void swap(int swap1, int swap2) {
		super.append(swap1, swap2);
		JavaStatement locationStatement = base.get(swap1);
		JavaStatement fixCodeStatement = base.get(swap2);
		JavaEditOperation newEdit = new JavaEditOperation(Mutation.SWAP,
				locationStatement, fixCodeStatement);
		this.genome.add(newEdit);

	}

	@Override
	public void replace(int whatToReplace, int whatToReplaceWith) {
		super.append(whatToReplace, whatToReplaceWith);
		this.editHelper(whatToReplace, whatToReplaceWith, Mutation.REPLACE);
	}

	public void nullInsert(int location) {
		super.nullInsert(location);
		JavaStatement locationStatement = base.get(location);
		JavaEditOperation newEdit = new JavaEditOperation(locationStatement,
				Mutation.NULLINSERT);
		this.genome.add(newEdit);
	}

	@Override
	protected boolean internalCompile(String progName, String exeName) {
		// OK, it might be possible to turn this into something closer to the
		// OCaml implementation (as was done with testCaseCommand), but I don't
		// know that I care enough to bother at the moment.
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		List<Pair<String, String>> sourceBuffers = this.computeSourceBuffers();
		if (sourceBuffers == null) {
			return false;
		} else {
			Iterable<? extends JavaFileObject> fileObjects = ASTUtils
					.getJavaSourceFromString(progName, sourceBuffers);

			// FIXME: why does an append that fails in computeSourceBuffers have
			// a fitness of 204?
			LinkedList<String> options = new LinkedList<String>();

			options.add("-cp");
			options.add(Configuration.libs);

			options.add("-source");
			options.add(Configuration.sourceVersion);

			options.add("-target");
			options.add(Configuration.targetVersion);

			options.add("-d");
			String outDirName = Configuration.outputDir + File.separatorChar
					+ exeName + File.separatorChar;
			File outDir = new File(outDirName);
			if (!outDir.exists())
				outDir.mkdir();
			options.add(outDirName);
			try {
				for (Pair<String, String> ele : sourceBuffers) {
					String sourceName = ele.getFirst();
					String program = ele.getSecond();

					// FIXME: can I write this in the folders to match where the
					// class file is compiled? I think the answer is YES: see
					// fixme in astutils

					BufferedWriter bw = new BufferedWriter(new FileWriter(
							outDirName + File.separatorChar + sourceName
									+ Configuration.globalExtension));
					bw.write(program);
					bw.flush();
					bw.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}

			StringWriter compilerErrorWriter = new StringWriter();

			// Here is where it runs the command to compile the code
			if (!compiler.getTask(compilerErrorWriter, null, null, options,
					null, fileObjects).call()) {
				System.err.println(compilerErrorWriter.toString());
				compilerErrorWriter.flush();
				return false;
			}

			return true;
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
			if (ok) {
				retVal.add(atom);
			}

		}
		JavaRepresentation.scopeSafeAtomMap.put(stmtId, retVal);
		return retVal;
	}

	@Override
	// you probably want to override these for semantic legality check
	public TreeSet<WeightedAtom> appendSources(int stmtId) {
		if (JavaRepresentation.semanticCheck.equals("scope")) {
			return this.scopeHelper(stmtId);
		} else {
			return super.appendSources(stmtId);
		}
	}

	@Override
	public TreeSet<WeightedAtom> swapSources(int stmtId) {
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
			return super.swapSources(stmtId);
		}
	}

	@Override
	public TreeSet<WeightedAtom> replaceSources(int stmtId) {
		if (JavaRepresentation.semanticCheck.equals("scope")) {
			return this.scopeHelper(stmtId);
		} else {
			return super.replaceSources(stmtId);
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
		String newName = CachingRepresentation.newVariant();
		internalCompile(newName, newName);
	}

}