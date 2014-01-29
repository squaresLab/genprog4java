package clegoues.genprog4java.java;

import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;

// FIXME: direct steal from PAR

public class TypeCollector extends ASTVisitor
{
	private Set<String> typeSet;
	
	public TypeCollector(Set<String> o)
	{
		this.typeSet = o;
	}
	
	@Override
	public boolean visit(SimpleName node)
	{
		IBinding nodeBinding = node.resolveBinding();
		ITypeBinding typeBinding = node.resolveTypeBinding();
		
		if(nodeBinding != null && typeBinding != null && 
				nodeBinding instanceof IVariableBinding && 
				!typeBinding.isPrimitive())
		{
			String type = typeBinding.getQualifiedName();
			
			if(!type.equals("java.lang.String"))
				typeSet.add(type);
		}
		
		return super.visit(node);
	}
}
