package clegoues.genprog4java.java;

import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;

public class JavaStatement
{
	private ASTNode astNode;
	private int lineno;
	private int stmtId; // unique
	private Set<String> names;
	private Set<String> types;
	
	private Set<String> scopes;

	public void setStmtId(int id) {
		this.stmtId = id;
	}
	
	public int getStmtId() {
		return this.stmtId;
	}
	
	public ASTNode getASTNode()
	{
		return astNode;
	}

	public void setASTNode(ASTNode node)
	{
		this.astNode = node;
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

	public void setNames(Set<String> names)  // FIXME: understand the distinction between names, types, and scopes
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

	public Set<String> getScopes()
	{
		return scopes;
	}

	public void setScopes(Set<String> scopes)
	{
		this.scopes = scopes;
	}
	
	public String toString()
	{
		if(astNode != null)
			return this.astNode.toString();
		else
			return "null";
	}
}
