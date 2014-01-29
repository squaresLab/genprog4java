package clegoues.genprog4java.java;

import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SimpleName;

// FIXME: direct steal from PAR

public class NameCollector extends ASTVisitor
{
	private Set<String> nameSet;
	
	public NameCollector(Set<String> o)
	{
		nameSet = o;
	}
	
	@Override
	public boolean visit(SimpleName node)
	{
		String name = node.getIdentifier();
		
		nameSet.add(name);
		
		return super.visit(node);
	}
}
