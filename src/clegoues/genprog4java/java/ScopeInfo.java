package clegoues.genprog4java.java;

import java.util.HashMap;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;

// FIXME: grabbed from PAR
public class ScopeInfo
{
	
	private HashMap<ASTNode,Set<String>> stmtScope;
	
	public ScopeInfo()
	{
		this.stmtScope = new HashMap<ASTNode,Set<String>>();
	}
	
	public void addScope4Stmt(ASTNode buggy, Set<String> shown)
	{
		if(this.stmtScope.containsKey(buggy))
		{
			this.stmtScope.get(buggy).addAll(shown);
		}
		else
		{
			this.stmtScope.put(buggy, shown);
		}
	}
	
	public boolean isScopeSafe(ASTNode buggy, Set<String> necessary)
	{
		boolean isSafe = true;
		
		Set<String> provided = this.stmtScope.get(buggy);		
		
		for(String n : necessary)
		{
			if(!provided.contains(n))
			{
				isSafe = false;
				break;
			}
		}
		
		return isSafe;
	}
	
	public Set<String> getScope(ASTNode buggy)
	{
		return this.stmtScope.get(buggy);
	}
}
