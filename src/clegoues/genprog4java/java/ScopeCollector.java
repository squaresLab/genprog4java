package clegoues.genprog4java.java;


import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

// FIXME: direct steal from PAR

public class ScopeCollector extends ASTVisitor
{
	
	private Set<String> nameSet;
	
	public ScopeCollector(Set<String> o)
	{
		nameSet = o;
	}
	
	@Override
	public boolean visit(SimpleName node)   // Need to filter out VariableDeclarationStatement
	{
		String name = node.getIdentifier();
		IBinding binding = node.resolveBinding();
		
		boolean isVarDecl = false;
		ASTNode parent = node.getParent();
		
		while(parent.getParent() != null && !(parent instanceof MethodDeclaration) && !(parent instanceof CompilationUnit))
		{
			if(parent.getParent() == null)
			{
				Runtime.getRuntime().exit(1);
			}
			
			if(parent instanceof VariableDeclarationStatement)
			{
				isVarDecl = true;
				break;
			}
			
			if(parent instanceof QualifiedName)
			{
				isVarDecl = true;
				break;
			}
			parent = parent.getParent();
		}
		
		if(!isVarDecl && binding instanceof IVariableBinding)
			this.nameSet.add(name);
		
		return super.visit(node);
	}
}
