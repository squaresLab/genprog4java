package clegoues.genprog4java.java;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

// FIXME: grabbed from PAR directly

public class ParserRequestor extends FileASTRequestor
{
	private StatementVisitor visitor;
	
	public ParserRequestor(StatementVisitor v)
	{
		this.visitor = v;
	}
	 
	
	@Override
	public void acceptAST(String sourceFilePath, CompilationUnit ast)
	{
		this.visitor.setCompilationUnit(ast);
		this.visitor.init(sourceFilePath);
		ast.accept(this.visitor);
		super.acceptAST(sourceFilePath, ast);
	}
}
