package clegoues.genprog4java.java;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;


public class JavaParser
{
	private LinkedList<ASTNode> stmts;
	private SemanticInfoVisitor visitor;
	private CompilationUnit compilationUnit;
	
	private ScopeInfo scopeList;
	

	public JavaParser()
	{
		this.stmts = new LinkedList<ASTNode>();
		this.visitor = new SemanticInfoVisitor();
		this.visitor.setNodeSet(this.stmts);		
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
	for(ASTNode stmt : this.stmts) { 
			this.scopeList.addScope4Stmt(stmt, visitor.getFieldSet());
		} 
	}
}
