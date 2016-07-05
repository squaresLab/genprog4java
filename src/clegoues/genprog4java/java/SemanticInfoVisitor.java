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
import java.util.Stack;
import java.util.TreeSet;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import clegoues.genprog4java.rep.JavaRepresentation;
import clegoues.util.Pair;

public class SemanticInfoVisitor extends ASTVisitor {

	private List<ASTNode> nodeSet;
	private ScopeInfo scopes;

	private HashSet<String> requiredNames = new HashSet<String>();
	private Stack<HashSet<String>> requiredNamesStack = new Stack<HashSet<String>>();

	// it might make sense to store these separately, but for now, this will do
	private HashSet<String> availableMethodsAndFields;

	// FIXME: types on variables in different scopes? Types in general, really

	private HashSet<String> currentMethodScope;
	private Stack<HashSet<String>> methodScopeStack;

	private HashSet<Pair<String,String>> methodReturnType;
	private HashMap<String,String> variableType;
	
	private HashSet<String> localVariables  = new HashSet<String>();
	private Stack<HashSet<String>> localVariableStack  = new Stack<HashSet<String>>();

	private HashSet<String> currentLoopScope = new HashSet<String>();
	private Stack<HashSet<String>> loopScopeStack = new Stack<HashSet<String>>();

	// declared or imported; primitive types are always available;
	private HashSet<String> availableTypes; 

	private CompilationUnit cu;

	public SemanticInfoVisitor() {
		this.availableMethodsAndFields = new HashSet<String>();
		this.availableMethodsAndFields.add("this");
	}

	public void setAvailableTypes(HashSet<String> typs) {
		this.availableTypes = typs;
	}

	@Override
	public void preVisit(ASTNode node) {
		requiredNamesStack.push(requiredNames);
		requiredNames = new HashSet<String>();

		if (JavaRepresentation.canRepair(node)) 
		{
			// add scope information
			TreeSet<String> newScope = new TreeSet<String>();
			newScope.addAll(this.currentMethodScope);
			newScope.addAll(this.currentLoopScope);
			newScope.addAll(this.availableMethodsAndFields);
			newScope.addAll(this.availableTypes);
			this.scopes.addScope4Stmt(node, newScope);
			this.nodeSet.add(node);
		}

		if(node instanceof EnhancedForStatement || 
				node instanceof ForStatement) {
			loopScopeStack.push(currentLoopScope);
			currentLoopScope = new HashSet<String>(currentLoopScope);
		}

		if(node instanceof Block) {
			this.methodScopeStack.push(currentMethodScope);
			currentMethodScope = new HashSet<String>(currentMethodScope);
			this.localVariableStack.push(this.localVariables);
			this.localVariables = new HashSet<String>(this.localVariables);
		}
		super.preVisit(node);
	}

	@Override
	public void postVisit(ASTNode node) {

		// required names are known only after the node has been processed
		HashSet<String> oldRequired = requiredNamesStack.pop();
		if(node instanceof EnhancedForStatement || 
				node instanceof ForStatement) {
			requiredNames.removeAll(currentLoopScope);
			currentLoopScope = loopScopeStack.pop(); 
		}

		if(node instanceof Block) {
			Set<String> newLocalVariables = this.localVariableStack.pop();
			Set<String> toRemove =new HashSet<String>(this.localVariables);
			toRemove.removeAll(newLocalVariables);

			requiredNames.removeAll(toRemove);

			currentMethodScope = this.methodScopeStack.pop();
		}
		if(JavaRepresentation.canRepair(node)) {
			this.scopes.addRequiredNames(node,new HashSet<String>(this.requiredNames));
		}
		
		requiredNames.addAll(oldRequired);

		super.postVisit(node);
	}

	private boolean anywhereInScope(String lookingFor) {
		return (availableMethodsAndFields != null && availableMethodsAndFields.contains(lookingFor)) || 
				(availableTypes != null && availableTypes.contains(lookingFor)) ||
				(currentMethodScope != null && currentMethodScope.contains(lookingFor)) ||
				(localVariables != null && localVariables.contains(lookingFor) ||
				(currentLoopScope != null && currentLoopScope.contains(lookingFor)));
	}
	
	@Override
	public boolean visit(SimpleName node) {
		
		// if I were doing something smart with types, I'd probably want to do something
		// to track in-scope method names at method invocations
		// but I'm not yet so we'll make do
		String name = node.getIdentifier();
		this.requiredNames.add(name);
		if(!anywhereInScope(name)) {
			// because we're parsing, *if this CU parses*, we can assume it doesn't reference
			// anything that's not in scope
			// this means that if we haven't seen a name before, it's almost certainly the name of a method
			// being invoked on an expression of a type that we *have* seen
			// because it's annoying to actually figure out everything available to us by walking the loaded
			// imports, we just add the SimpleName to the list of available names
			// kind of a cheap trick, but whatever
			// the one thing I'm not sure about is if I should add this to available types or...something else
			this.availableTypes.add(name);
		}
		return true;
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		SingleVariableDeclaration vd = node.getParameter();
		this.currentLoopScope.add(vd.getName().getIdentifier());
		return true;
	}

	@Override
	public boolean visit(ForStatement node) {
		for(Object o : node.initializers()) {
			List fragments = null;
			if(o instanceof VariableDeclarationExpression) {
				VariableDeclarationExpression vd = (VariableDeclarationExpression) o;
				fragments = vd.fragments();
			}
			if(o instanceof VariableDeclarationStatement) {
				VariableDeclarationStatement vd = (VariableDeclarationStatement) o;
				fragments = vd.fragments();
			}
			for(Object f : fragments) {
				VariableDeclarationFragment frag = (VariableDeclarationFragment) f;
				this.currentLoopScope.add(frag.getName().getIdentifier());
			}
		}
		return true;
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		if(!node.isOnDemand() && !node.isStatic()) { // possible FIXME: handle all static stuff separately?
			String name = node.getName().getFullyQualifiedName();
			String[] split = name.split("\\.");
			availableTypes.add(split[split.length - 1]);
		}
		return false;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		if(!node.isInterface()) {
			availableTypes.add(node.getName().getIdentifier());
			for(FieldDeclaration fd : node.getFields()) {
				for (Object o : fd.fragments()) {
					if (o instanceof VariableDeclarationFragment) {
						VariableDeclarationFragment v = (VariableDeclarationFragment) o;
						this.availableMethodsAndFields.add(v.getName().getIdentifier());
					}
				}
			}

			for(MethodDeclaration md : node.getMethods()) {
				this.availableMethodsAndFields.add(md.getName().getIdentifier());
			}
			if(node.getSuperclassType() != null) {
				this.availableMethodsAndFields.add("super");
			}
		}
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		this.currentMethodScope = new HashSet<String>();
		this.methodScopeStack = new Stack<HashSet<String>>();

		// FIXME: what happens if var args?
		for (Object o : node.parameters()) {
			if (o instanceof SingleVariableDeclaration) {
				SingleVariableDeclaration v = (SingleVariableDeclaration) o;
				this.currentMethodScope.add(v.getName().getIdentifier());
			}
		}
		String returnType = node.getReturnType2()==null?"null":node.getReturnType2().toString();
		this.methodReturnType.add(new Pair<String, String>(node.getName().toString(),returnType));

		return true;
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		for (Object o : node.fragments()) {
			if (o instanceof VariableDeclarationFragment) {
				VariableDeclarationFragment v = (VariableDeclarationFragment) o;
				String name = v.getName().getIdentifier();
				if(!currentLoopScope.contains(name)) {
					this.currentMethodScope.add(v.getName().getIdentifier());
					variableType.put(v.getName().toString(), node.getType().toString());
				}
				this.localVariables.add(v.getName().toString());
			}
		}
		return true;
	}

	public CompilationUnit getCompilationUnit() {
		return cu;
	}

	public void setCompilationUnit(CompilationUnit cu) {
		this.cu = cu;
	}

	public Set<String> getFieldSet() {
		return this.availableMethodsAndFields;
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
	public boolean visit(Initializer node) {
		List mods = node.modifiers(); // FIXME need to deal with static.

		for (Object o : mods) {
			if (o instanceof Modifier) {
				if (((Modifier) o).isStatic()) {
					this.currentMethodScope = new HashSet<String>();
				}
			}
		}
		return super.visit(node);
	}
}
