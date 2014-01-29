package clegoues.genprog4java.java;

import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
// FIXME: grabbed verbatim out of PAR

public class SingleStatement // "Single*" To avoid name space overlapping with JDT. 
{
	private ASTNode node;
	private int lineno;
	
	private Set<String> names;
	private Set<String> types;
	
	private Set<String> necessaryScopeNames;

	public ASTNode getNode()
	{
		return node;
	}

	public void setNode(ASTNode node)
	{
		this.node = node;
	}

	public int getLineno()
	{
		return lineno;
	}

	public void setLineno(int lineno)
	{
		this.lineno = lineno;
	}

	public Set<String> getNames()
	{
		return names;
	}

	public void setNames(Set<String> names)
	{
		this.names = names;
	}

	public Set<String> getTypes()
	{
		return types;
	}

	public void setTypes(Set<String> types)
	{
		this.types = types;
	}

	public Set<String> getNecessaryScopeNames()
	{
		return necessaryScopeNames;
	}

	public void setNecessaryScopeNames(Set<String> necessaryScopeNames)
	{
		this.necessaryScopeNames = necessaryScopeNames;
	}
	
	public String toString()
	{
		if(node != null)
			return this.node.toString();
		else
			return "null";
	}
}
