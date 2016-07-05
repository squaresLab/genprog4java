/*
 * Copyright (c) 2014-2015, 
 *  Claire Le Goues     <clegoues@cs.cmu.edu>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. The names of the contributors may not be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package clegoues.genprog4java.java;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;

public class ScopeInfo
{
	
	private HashMap<ASTNode,Set<String>> stmtScope; // stuff that's IN SCOPE at the statement, not used at the statement
	private HashMap<ASTNode,Set<String>> requiredNames; 

	public ScopeInfo()
	{
		this.stmtScope = new HashMap<ASTNode,Set<String>>();
		this.requiredNames = new HashMap<ASTNode,Set<String>>();

	}
	
	public void addRequiredNames(ASTNode buggy, Set<String> names) {
		this.requiredNames.put(buggy, new HashSet<String>(names));
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
		
		for(String s : necessary)
		{
			if(!provided.contains(s))
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
	
	public Set<String> getRequiredNames(ASTNode buggy)
	{
		return this.requiredNames.get(buggy);
	}
}
