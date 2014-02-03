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

import java.io.Serializable;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;

public class JavaStatement implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -9053826138990827966L;
	/**
	 * 
	 */
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
