package clegoues.genprog4java.java;


import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;



// FIXME: direct copy from PAR
public class ASTUtils
{
	
	public static int getStatementLineNo(ASTNode node)
	{
		ASTNode root = node.getRoot();
		int lineno = -1;
		if(root instanceof CompilationUnit)
		{
			CompilationUnit cu = (CompilationUnit)root;
			lineno = cu.getLineNumber(node.getStartPosition());
		}
	
		return lineno;
	}
	
	public static Set<String> getNames(ASTNode node)     // it does not count.
	{
		TreeSet<String> names = new TreeSet<String>();
		
		NameCollector visitor = new NameCollector(names);
		
		node.accept(visitor);
		
		return names;
	}
	
	public static Set<String> getTypes(ASTNode node)
	{
		TreeSet<String> types = new TreeSet<String>();
		
		TypeCollector visitor = new TypeCollector(types);
		
		node.accept(visitor);
		
		return types;
	}
	
	public static Set<String> getScope(ASTNode node)
	{
		TreeSet<String> scope = new TreeSet<String>();
		
		ScopeCollector visitor = new ScopeCollector(scope);
		
		node.accept(visitor);
		
		return scope;
	}
}
