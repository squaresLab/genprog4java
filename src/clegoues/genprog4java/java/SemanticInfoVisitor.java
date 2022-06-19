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

import org.eclipse.jdt.core.dom.*;

import clegoues.genprog4java.rep.JavaRepresentation;

public class SemanticInfoVisitor extends ASTVisitor {

	private List<ASTNode> nodeSet;
	private ScopeInfo scopes;
	private CFBlockInfo cfBlockInfo;

	private boolean containsFinalVar = false;
	private Stack<Boolean> finalVarStack = new Stack<Boolean>();


	private HashSet<String> requiredNames = new HashSet<String>();
	private Stack<HashSet<String>> requiredNamesStack = new Stack<HashSet<String>>();


	// FIXME: types on variables in different scopes? Types in general, really

	private HashSet<String> currentMethodScope = new HashSet<String>();
	private Stack<HashSet<String>> methodScopeStack = new Stack<HashSet<String>>();

	private HashMap<String,String> methodReturnType;
	private HashMap<String,String> variableType;

	private HashSet<String> currentLoopScope = new HashSet<String>();
	private Stack<HashSet<String>> loopScopeStack = new Stack<HashSet<String>>();

	// declared or imported; primitive types are always available;
	private HashSet<String> availableTypes;
	// it might make sense to store these separately, but for now, this will do
	// upon reflection, it makes more sense to do add these after the whole thing has been parsed
	private HashSet<String> availableMethodsAndFields;

	private HashSet<String> namesDeclared = new HashSet<String>();
	private Stack<HashSet<String>> namesDeclaredStack = new Stack<HashSet<String>>();

	private CompilationUnit cu;

	public SemanticInfoVisitor() {

	}
	public void setAvailableMethodsAndFields(HashSet<String> mandf) {
		this.availableMethodsAndFields = mandf;
		this.availableMethodsAndFields.add("this");
	}
	public void setAvailableTypes(HashSet<String> typs) {
		this.availableTypes = typs;
	}

	// FIXME figure out what a node declares

	@Override
	public void preVisit(ASTNode node) {
		requiredNamesStack.push(requiredNames);
		requiredNames = new HashSet<String>();

		finalVarStack.push(containsFinalVar);
		containsFinalVar = false;

		if (JavaRepresentation.canRepair(node))
		{
			// add scope information
			TreeSet<String> newScope = new TreeSet<String>();
			newScope.addAll(this.currentMethodScope);
			newScope.addAll(this.currentLoopScope);
			newScope.addAll(this.availableTypes);
			this.scopes.addToMethodScope(node, newScope);
			this.scopes.addToClassScope(this.availableMethodsAndFields);
			this.nodeSet.add(node);

			// only register nodes we will later keep track of.
			this.cfBlockInfo.registerNode(node);
		}

		if(node instanceof EnhancedForStatement ||
				node instanceof ForStatement) {
			loopScopeStack.push(currentLoopScope);
			currentLoopScope = new HashSet<String>(currentLoopScope);
		}

		if(node instanceof Block || node instanceof MethodDeclaration) {
			this.methodScopeStack.push(currentMethodScope);
			currentMethodScope = new HashSet<String>(currentMethodScope);
		}



		this.namesDeclaredStack.push(namesDeclared);
		namesDeclared = new HashSet<String>();
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

		if(node instanceof Block || node instanceof MethodDeclaration) {
			HashSet<String> newLocalVariables = this.methodScopeStack.pop();
			Set<String> toRemove =new HashSet<String>(this.currentMethodScope);
			toRemove.removeAll(newLocalVariables);

			requiredNames.removeAll(toRemove);

			currentMethodScope = newLocalVariables;
		}

		namesDeclared.addAll(namesDeclaredStack.pop());

		if(JavaRepresentation.canRepair(node)) {
			this.scopes.addRequiredNames(node,new HashSet<String>(this.requiredNames));
			this.scopes.setContainsFinalVarDecl(node, containsFinalVar);
			this.scopes.setNamesDeclared(node, new HashSet<String>(this.namesDeclared));

			this.cfBlockInfo.endOfNode(node);
		}

		if (node instanceof Block) {
			containsFinalVar = finalVarStack.pop();
		} else {
			containsFinalVar = containsFinalVar || finalVarStack.pop();
		}

		requiredNames.addAll(oldRequired);

		super.postVisit(node);
	}

	private IBinding getVarBinding(FieldAccess node) {
		return node.resolveFieldBinding();
	}

	private IBinding getVarBinding(Name node) {
		return node.resolveBinding();
	}
	private IBinding getVarBinding(SuperFieldAccess node) {
		return node.resolveFieldBinding();
	}

	@Override
	public boolean visit(Assignment node) {
		Expression lhs = node.getLeftHandSide();
		IBinding binding = null;
		if(lhs instanceof FieldAccess) {
			binding = getVarBinding((FieldAccess) lhs);
		} else if (lhs instanceof Name) {
			binding = getVarBinding((Name) lhs);
		} else if (lhs instanceof SuperFieldAccess) {
			binding = getVarBinding((SuperFieldAccess) lhs);
		}
		if(binding != null && binding instanceof IVariableBinding) {
			IVariableBinding vb = (IVariableBinding) binding;
			int modifiers = vb.getModifiers();
			if(Modifier.isFinal(modifiers)) {
				containsFinalVar = true;
			}
		}
		return true;
	}

	private boolean anywhereInScope(String lookingFor) {
		return (availableMethodsAndFields != null && availableMethodsAndFields.contains(lookingFor)) || 
				(availableTypes != null && availableTypes.contains(lookingFor)) ||
				(currentMethodScope != null && currentMethodScope.contains(lookingFor)) ||
				(currentLoopScope != null && currentLoopScope.contains(lookingFor));
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
		this.namesDeclared.add(vd.getName().getIdentifier());
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
			if(fragments != null) {
				for(Object f : fragments) {
					VariableDeclarationFragment frag = (VariableDeclarationFragment) f;
					this.currentLoopScope.add(frag.getName().getIdentifier());
					this.namesDeclared.add(frag.getName().getIdentifier());
				}
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

		// FIXME: what happens if var args?
		for (Object o : node.parameters()) {
			if (o instanceof SingleVariableDeclaration) {
				SingleVariableDeclaration v = (SingleVariableDeclaration) o;
				this.currentMethodScope.add(v.getName().getIdentifier());
			}
		}
		String returnType = node.getReturnType2()==null?"null":node.getReturnType2().toString();
		this.methodReturnType.put(node.getName().getIdentifier(), returnType);
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		for (Object o : node.fragments()) {
			if (o instanceof VariableDeclarationFragment) {
				VariableDeclarationFragment v = (VariableDeclarationFragment) o;
				String name = v.getName().getIdentifier();
				namesDeclared.add(name);
				if(!currentLoopScope.contains(name)) {
					this.currentMethodScope.add(v.getName().getIdentifier());
					variableType.put(v.getName().toString(), node.getType().toString());
				}
			}
		}
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationExpression node) {
		for (Object o : node.fragments()) {
			if (o instanceof VariableDeclarationFragment) {
				VariableDeclarationFragment v = (VariableDeclarationFragment) o;
				String name = v.getName().getIdentifier();
				namesDeclared.add(name);
				if(!currentLoopScope.contains(name)) {
					this.currentMethodScope.add(v.getName().getIdentifier());
					variableType.put(v.getName().toString().toLowerCase(), node.getType().toString());
				}
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


	public void setNodeSet(List<ASTNode> o) {
		this.nodeSet = o;
	}

	public HashMap<String,String> getMethodReturnType() {
		return this.methodReturnType;
	}

	public void setMethodReturnType(HashMap<String,String> methodReturnTypeMap) {
		this.methodReturnType = methodReturnTypeMap;
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

	public void setCFBlockInfo(CFBlockInfo cfBlockInfo) {
		this.cfBlockInfo = cfBlockInfo;
	}

	//	@Override
	//	public boolean visit(Initializer node) {
	//		List mods = node.modifiers(); // FIXME need to deal with static.
	//
	//		for (Object o : mods) {
	//			if (o instanceof Modifier) {
	//				if (((Modifier) o).isStatic()) {
	//					this.currentMethodScope = new HashSet<String>();
	//				}
	//			}
	//		}
	//		return super.visit(node);
	//	}
}
