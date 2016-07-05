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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import clegoues.genprog4java.rep.JavaRepresentation;
import clegoues.util.Pair;

public class SemanticInfoVisitor extends ASTVisitor {

	private String sourcePath;

	private List<ASTNode> nodeSet;
	private ScopeInfo scopes;

	// declared or imported; primitive types are always available;
	
	private HashSet<String> availableTypes = new HashSet<String>(); 
	private HashSet<String> fieldName;
	private HashSet<String> currentMethodScope;
	private HashSet<Pair<String,String>> methodReturnType;
	private HashMap<String,String> variableType;

	// unlike in the OCaml implementation, this only collects the statements and
	// the semantic information. It doesn't number.
	private CompilationUnit cu;

	public void init(String p) {
		this.sourcePath = p;
	}

	public SemanticInfoVisitor() {
		this.fieldName = new HashSet<String>();
		this.fieldName.add("this");
	}
	
	public void setAvailableTypes(HashSet<String> typs) {
		this.availableTypes = typs;
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		if(!node.isOnDemand() && !node.isStatic()) { // possible FIXME: handle all static stuff separately?
			String name = node.getName().getFullyQualifiedName();
			String[] split = name.split("\\.");
			availableTypes.add(split[split.length - 1]);
		}
		return true;
	}
	
	@Override
	public boolean visit(TypeDeclaration node) {
		if(!node.isInterface()) {
			availableTypes.add(node.getName().getIdentifier());
		}
		return true;
	}
	
	
	@Override
	public boolean visit(FieldDeclaration node) {
		for (Object o : node.fragments()) {
			if (o instanceof VariableDeclarationFragment) {
				VariableDeclarationFragment v = (VariableDeclarationFragment) o;
				this.fieldName.add(v.getName().getIdentifier());
			}
		}
		return super.visit(node);
	}

	
	public Set<String> getFieldSet() {
		return this.fieldName;
	}
	public void setNodeSet(List<ASTNode> o) {
		this.nodeSet = o;
	}

	public Set<Pair<String,String>> getMethodReturnType() {
		return this.methodReturnType;
	}

	public void setMethodReturnType(HashSet<Pair<String,String>> methodReturnTypeSet) {
		this.methodReturnType = methodReturnTypeSet;
	}
	
	public HashMap<String,String> getVariableType() {
		return this.variableType;
	}

	public void setVariableType(HashMap<String,String> variableTypeSet) {
		this.variableType = variableTypeSet;
	}
	
	public List<ASTNode> getNodeSet() {
		return this.nodeSet;
	}

	public void setScopeList(ScopeInfo scopeList) {
		this.scopes = scopeList;
	}


	@Override
	public boolean visit(MethodDeclaration node) {
		this.currentMethodScope = new HashSet<String>();

		if(node.isConstructor() || node.isVarargs()) return true; // ain't nobody got time for that
		for (Object o : node.parameters()) {
			if (o instanceof SingleVariableDeclaration) {
				SingleVariableDeclaration v = (SingleVariableDeclaration) o;
				this.currentMethodScope.add(v.getName().getIdentifier());
			}
		}
		String returnType = node.getReturnType2()==null?"null":node.getReturnType2().toString();
		this.methodReturnType.add(new Pair<String, String>(node.getName().toString(),returnType));
		
		return super.visit(node);
	}

	@Override
	public boolean visit(Initializer node) {
		List mods = node.modifiers();

		for (Object o : mods) {
			if (o instanceof Modifier) {
				if (((Modifier) o).isStatic()) {
					this.currentMethodScope = new HashSet<String>();
				}
			}
		}

		return super.visit(node);
	}

	
	public boolean visit(SingleVariableDeclaration node){
		
		return super.visit(node);
	}
	
	public boolean visit(Assignment node){
		
		return super.visit(node);
	}

	@Override
	public void endVisit(Initializer node) {
		super.endVisit(node);
	}

	@Override
	public void endVisit(MethodDeclaration node) {
		super.endVisit(node);
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		for (Object o : node.fragments()) {
			if (o instanceof VariableDeclarationFragment) {
				VariableDeclarationFragment v = (VariableDeclarationFragment) o;
				this.currentMethodScope.add(v.getName().getIdentifier());
				variableType.put(v.getName().toString(), node.getType().toString());
			}
		}
		
		return super.visit(node);
	}

	public void preVisit(ASTNode node) {
		if (JavaRepresentation.canRepair(node)) // FIXME: why is this necessary
												// to not crash and die?
		{
			// add scope information
			TreeSet<String> newScope = new TreeSet<String>();
			newScope.addAll(this.currentMethodScope);
			this.scopes.addScope4Stmt(node, newScope);
			this.nodeSet.add(node);
		}

		super.preVisit(node);
	}

	public void setCompilationUnit(CompilationUnit ast) {
		this.cu = ast;
	}

	public CompilationUnit getCompilationUnit() {
		return this.cu;
	}
}
