package clegoues.genprog4java.java;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

// FIXME: grabbed from PAR
public class StatementParser
{
	private LinkedList<ASTNode> stmts;
	private StatementVisitor visitor;
	private CompilationUnit compilationUnit;
	
	private HashMap<Integer, ASTNode> buggyStmt;
	private ScopeInfo scopeList;
	
	public HashMap<Integer, ASTNode> getBuggyStmt()
	{
		return buggyStmt;
	}

	public StatementParser()
	{
		this.buggyStmt = new HashMap<Integer, ASTNode>();
		this.stmts = new LinkedList<ASTNode>();
		this.visitor = new StatementVisitor();
		this.visitor.setNodeSet(this.stmts);
		this.visitor.setBuggySet(buggyStmt);
		
		this.scopeList = new ScopeInfo();
		this.visitor.setScopeList(this.scopeList);
	}

	public ScopeInfo getScopeInfo()
	{
		return this.scopeList;
	}
	
	
	public LinkedList<ASTNode> getStatements()
	{
		return this.stmts;
	}
	
	public CompilationUnit getCompilationUnit()
	{
		return this.compilationUnit;
	}
	
	
	// FIXME: this gets scope info for all "buggy" statements at once.  Do it properly (with coverage)
	public void parse(String file, String[] libs)
	{
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setEnvironment(libs, new String[] {}, null, true);
		
		Map options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_6, options);
		parser.setCompilerOptions(options);
		
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.setStatementsRecovery(true);
		
		parser.createASTs(new String[]{file}, null, new String[0], new ParserRequestor(visitor), null);
		
		this.compilationUnit = visitor.getCompilationUnit();
		
		for(ASTNode buggy : this.buggyStmt.values())
		{
			this.scopeList.addScope4Stmt(buggy, visitor.getFieldSet());
		}
	}
}

