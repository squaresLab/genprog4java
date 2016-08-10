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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.ASTNode;

import clegoues.genprog4java.rep.JavaRepresentation;
import clegoues.genprog4java.treelm.SymbolTable;

public class ScopeInfo implements SymbolTable
{
	private Set<String> classScope; // stuff that's IN SCOPE at the statement, not used at the statement

	private HashMap<ASTNode,Set<String>> methodScope; // stuff that's IN SCOPE at the statement, not used at the statement
	private HashMap<ASTNode,Set<String>> requiredNames; 
	private HashMap<ASTNode,Set<String>> namesDeclared; 
	private HashMap<ASTNode, Boolean> containsFinalVarAssignment;
	
	
	/** all ASTNodes of interest, corresponding to "repairable" Java statement types
	 * a question (currently a question answered by {@link JavaRepresentation.canRepair})
	 */
	private LinkedList<ASTNode> stmts;
	
	/** method names --> type name.  I keep considering changing this --- hate having
	 * types as strings --- but haven't had a good reason to yet.
	 */
	private HashMap<String,String> methodReturnType;
	/** same thing, for variable names.  In theory might not work well b/c of scoping; in practice
	 * doesn't seem to be a problem. 
	 */
	private HashMap<String,String> variableTypes;
	
	/** all imported types, or types seen over the course of parsing the CU */
	// declared or imported; primitive types are always available;

	private HashSet<String> availableTypes;
	
	/** methods and fields available in this CU, which we know either because
	 * we see their declaration, or because we've seen them used at some point (heuristic);
	 */
	private HashSet<String> availableMethodsAndFields;
	
	public ScopeInfo()
	{
		this.methodScope = new HashMap<ASTNode,Set<String>>();
		this.classScope = new HashSet<String>();
		this.requiredNames = new HashMap<ASTNode,Set<String>>();
		this.namesDeclared = new HashMap<ASTNode,Set<String>>();
		this.containsFinalVarAssignment = new HashMap<ASTNode, Boolean>();
		
		this.stmts = new LinkedList<ASTNode>();
		this.methodReturnType = new HashMap<String,String>();
		this.variableTypes = new HashMap<String,String>();
		this.availableTypes = new HashSet<String>();
		this.availableMethodsAndFields = new HashSet<String>();
		this.availableMethodsAndFields.add("this");
	}
	
	public Set<String> getNamesDeclared(ASTNode buggy) {
		return this.namesDeclared.get(buggy);
	}
	
	public void setNamesDeclared(ASTNode buggy, Set<String> names) {
		this.namesDeclared.put(buggy, names);
	}
	
	public void addRequiredNames(ASTNode buggy, Set<String> names) {
		this.requiredNames.put(buggy,names);
	}
	
	public void addToMethodScope(ASTNode buggy, Set<String> shown)
	{
		if(this.methodScope.containsKey(buggy))
		{
			this.methodScope.get(buggy).addAll(shown);
		}
		else
		{
			this.methodScope.put(buggy, shown);
		}
	}
	
	public boolean getFinalVarInfo(ASTNode node) {
		return containsFinalVarAssignment.get(node);
	}
	public void setContainsFinalVarDecl(ASTNode node, boolean status) {
		containsFinalVarAssignment.put(node, status);
	}
	
	public Set<String> getMethodScope(ASTNode buggy)
	{
		return this.methodScope.get(buggy);
	}
	
	
	public void addToClassScope(String varname) {
		this.classScope.add(varname);
	}
	
	public void addToClassScope(Set<String> vars) {
		this.classScope.addAll(vars);
	}
	public Set<String> getClassScope()
	{
		return this.classScope;
	}
	
	public Set<String> getRequiredNames(ASTNode buggy)
	{
		return this.requiredNames.get(buggy);
	}

	@Override
	public void enter(int nodeType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void leave(int nodeType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getFullyQualifiedTypeName(String simpleName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Supplier<String> allocFreeName(String type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Supplier<String> getNameForType(String type) {
		// TODO Auto-generated method stub
		return null;
	}

	public void addToNodeSet(ASTNode node, Set<String> currentMethodScope, Set<String> currentLoopScope) {
		TreeSet<String> newScope = new TreeSet<String>();
		newScope.addAll(currentMethodScope);
		newScope.addAll(currentLoopScope);
		newScope.addAll(this.availableTypes);
		this.addToMethodScope(node, newScope);
		this.addToClassScope(this.availableMethodsAndFields);
		this.stmts.add(node);
	}

	public void addMethodReturnType(String methodName, String methodType) {
		this.methodReturnType.put(methodName, methodType);
	}

	public void addVariableType(String varName, String varTyp) {
		this.variableTypes.put(varName, varTyp);
	}

	public void addToAvailableTypes(String identifier) {
		this.availableTypes.add(identifier);
	}
	
	public boolean anywhereInScope(String lookingFor, Set<String> currentMethodScope, Set<String> currentLoopScope) {
		return (availableMethodsAndFields != null && availableMethodsAndFields.contains(lookingFor)) || 
				(availableTypes != null && availableTypes.contains(lookingFor)) ||
				(currentMethodScope != null && currentMethodScope.contains(lookingFor)) ||
				(currentLoopScope != null && currentLoopScope.contains(lookingFor));
	}

	public void addToAvailableMethodsAndFields(String identifier) {
		this.availableMethodsAndFields.add(identifier);
	}

	public List<ASTNode> getStatements() {
		return this.stmts;
	}

	public Set<String> getAvailableTypes() {
		return this.availableTypes;
	}

	public Set<String> getAvailableMethodsAndFields() {
		return this.availableMethodsAndFields;
	}
}
