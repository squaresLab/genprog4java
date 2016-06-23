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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.WhileStatement;

import clegoues.genprog4java.main.ClassInfo;
import clegoues.genprog4java.mut.Location;
import clegoues.genprog4java.rep.WeightedAtom;

public class JavaStatement implements Comparable<JavaStatement>{

	private ASTNode astNode;
	private ClassInfo classInfo;

	private int lineno;
	private int stmtId; // unique
	private Set<String> names;
	private Set<String> types;
	private Set<String> mustBeInScope;

	public void setClassInfo(ClassInfo ci) {
		this.classInfo = ci;
	}

	public ClassInfo getClassInfo() {
		return this.classInfo;
	}

	public void setStmtId(int id) {
		this.stmtId = id;
	}

	public int getStmtId() {
		return this.stmtId;
	}

	public ASTNode getASTNode() {
		return astNode;
	}

	public void setASTNode(ASTNode node) {
		this.astNode = node;
	}

	public int getLineno() {
		return lineno;
	}

	public void setLineno(int lineno) {
		this.lineno = lineno;
	}

	public Set<String> getNames() {
		return names;
	}

	public void setNames(Set<String> names) {
		this.names = names;
	}

	public Set<String> getTypes() {
		return types;
	}

	public void setTypes(Set<String> types) {
		this.types = types;
	}

	public Set<String> getRequiredNames() {
		return mustBeInScope;
	}

	public void setRequiredNames(Set<String> scopes) {
		this.mustBeInScope = scopes;
	}

	public String toString() {
		if (astNode != null)
			return this.astNode.toString();
		else
			return "null";
	}

	// method to get the statemetn parent of an ASTnode. We traverse the ast upwards until the parent node is an instance of statement
	private ASTNode getParent(ASTNode node) {
		ASTNode parent = node.getParent();
		while(!(parent instanceof Statement)){
			parent = parent.getParent();
		}
		return parent;
	}

	@Override
	public int compareTo(JavaStatement other) {
		return this.stmtId - other.getStmtId();
	}


	/******* Cached information for applicability of various mutations/templates ******/
	private Map<ASTNode, List<ASTNode>> arrayAccesses = null; // to track the parent nodes of array access nodes

	public Map<ASTNode, List<ASTNode>> getArrayAccesses() {
		if(arrayAccesses == null) {
			arrayAccesses =  new HashMap<ASTNode, List<ASTNode>>();
			this.getASTNode().accept(new ASTVisitor() {
				// method to visit all ArrayAccess nodes in locationNode and store their parents
				public boolean visit(ArrayAccess node) {
					ASTNode parent = getParent(node);
					if(!arrayAccesses.containsKey(parent)){
						List<ASTNode> arraynodes = new ArrayList<ASTNode>();
						arraynodes.add(node);
						arrayAccesses.put(parent, arraynodes);		
					}else{
						List<ASTNode> arraynodes = (List<ASTNode>) arrayAccesses.get(parent);
						if(!arraynodes.contains(node))
							arraynodes.add(node);
						arrayAccesses.put(parent, arraynodes);	
					}

					return true;
				}
			});

		}
		return this.arrayAccesses;
	}


	private Map<ASTNode, List<ASTNode>> nullCheckable = null;

	public  Map<ASTNode, List<ASTNode>> getNullCheckables() {
		if(nullCheckable == null) {
			nullCheckable = new HashMap<ASTNode, List<ASTNode>>();
			if(this.getASTNode() instanceof MethodInvocation 
					|| this.getASTNode() instanceof ExpressionStatement 
					|| this.getASTNode() instanceof ReturnStatement ){

				this.getASTNode().accept(new ASTVisitor() {
					// method to visit all Expressions relevant for this in locationNode and
					// store their parents
					public boolean visit(MethodInvocation node) {
						saveDataOfTheExpression(node);

						return true;
					}
					public boolean visit(FieldAccess node) {
						saveDataOfTheExpression(node);
						return true;
					}

					public boolean visit(QualifiedName node) {
						saveDataOfTheExpression(node);
						return true;
					}

					public void saveDataOfTheExpression(ASTNode node){
						ASTNode parent = getParent(node);
						if (!nullCheckable.containsKey(parent)) {
							List<ASTNode> thisList = new ArrayList<ASTNode>();
							thisList.add(node);
							nullCheckable.put(parent, thisList);
						} else {
							List<ASTNode> thisList = (List<ASTNode>) nullCheckable
									.get(parent);
							if (!thisList.contains(node))
								thisList.add(node);
							nullCheckable.put(parent, thisList);
						}
					}
				});

			}
		}
		return nullCheckable;
	}


	private Map<ASTNode, List<ASTNode>> casts = null;

	public Map<ASTNode, List<ASTNode>> getCasts() {
		if(casts != null) {
			return casts;
		}
		casts = new HashMap<ASTNode, List<ASTNode>>();
		this.getASTNode().accept(new ASTVisitor() {

			public boolean visit(CastExpression node) {
				ASTNode parent = getParent(node); 
				List<ASTNode> thisParentsCasts;
				if(casts.containsKey(parent)) {
					thisParentsCasts = casts.get(parent);
				} else {
					thisParentsCasts = new ArrayList<ASTNode>();
					casts.put(parent, thisParentsCasts);
				}
				thisParentsCasts.add(node);
				return true;
			}
		});

		return casts;
	}

	private Map<ASTNode, List<Expression>> shrinkableExpressions = null;

	public Map<ASTNode, List<Expression>> getShrinkableConditionalExpressions() {
		if(shrinkableExpressions != null) {
			return shrinkableExpressions;
		}
		shrinkableExpressions = new HashMap<ASTNode, List<Expression>>();
		this.getASTNode().accept(new ASTVisitor() {

			private boolean isShrinkable(InfixExpression.Operator op) {
				return (op == InfixExpression.Operator.CONDITIONAL_AND) ||
						(op == InfixExpression.Operator.CONDITIONAL_OR);
			}
			
			private void handleExp(ASTNode node, Expression condExp) {
				if(condExp instanceof InfixExpression) {
					if(isShrinkable(((InfixExpression) condExp).getOperator())) {
						List<Expression> shrinkable;
						if(shrinkableExpressions.containsKey(node)) {
							shrinkable = shrinkableExpressions.get(node);
						} else {
							shrinkable = new ArrayList<Expression>();
							shrinkableExpressions.put(node, shrinkable);
						}
						shrinkable.add(condExp);
					}
				}
			}
			public boolean visit(ConditionalExpression node) {
				handleExp(node, node.getExpression());
				return true;

			}
			public boolean visit(IfStatement node) {
				handleExp(node, node.getExpression());
				return true;
			}
		});
		return shrinkableExpressions;
	}

	private Map<ASTNode,List<Expression>> extendableExpressions = null;

	// FIXME: find a way to sort options by distance where sorting by distance is specified
	// in PAR paper
	public Map<ASTNode, List<Expression>> getConditionalExpressions(final JavaSemanticInfo semanticInfo) {
		if(extendableExpressions != null) {
			return extendableExpressions;
		}
		extendableExpressions = new HashMap<ASTNode, List<Expression>>();

		final MethodDeclaration md = (MethodDeclaration) this.getEnclosingMethod();
		final String methodName = md.getName().getIdentifier();
		final List<Expression> replacements = semanticInfo.getConditionalExtensionExpressions(methodName, md);

		this.getASTNode().accept(new ASTVisitor() {

			private void handleCondition(Expression exp) {
				// possible FIXME: exclude those that are already in the condition?
				if(replacements != null) {
					List<Expression> thisList = null;
					if(extendableExpressions.containsKey(exp)) {
						thisList = extendableExpressions.get(exp);
					} else {
						thisList = new ArrayList<Expression>();
						extendableExpressions.put(exp, thisList);
					}
					thisList.addAll(replacements);
				}
			}
			public boolean visit(IfStatement node) {
				handleCondition(node.getExpression());				
				return true;
			}

			public boolean visit(ConditionalExpression node) {
				handleCondition(node.getExpression());
				return true;
			}
		});
		return extendableExpressions;
	}

	// FIXME: fix Search for when we don't have enough options or edit list is empty.
	
	private Map<ASTNode, Map<ASTNode,List<ASTNode>>> methodParamReplacements = null;

	public Map<ASTNode, Map<ASTNode,List<ASTNode>>> getReplacableMethodParameters(final JavaSemanticInfo semanticInfo) {
		if(methodParamReplacements == null) {
			methodParamReplacements = new HashMap<ASTNode, Map<ASTNode,List<ASTNode>>>();
		}
		final MethodDeclaration md = (MethodDeclaration) this.getEnclosingMethod();
		final String methodName = md.getName().getIdentifier();

		this.getASTNode().accept(new ASTVisitor() {
			// method to visit all Expressions relevant for this in locationNode and
			// store their parents
			public boolean visit(MethodInvocation node) {
				// if there exists another invocation that this works for...
				Map<ASTNode,List<ASTNode>> thisMethodCall;
				if(methodParamReplacements.containsKey(node)) {
					thisMethodCall = methodParamReplacements.get(node);
				} else {
					thisMethodCall = new HashMap<ASTNode, List<ASTNode>>();
					methodParamReplacements.put(node, thisMethodCall);
				}

				List<Expression> args = node.arguments();
				for(Expression arg : args) {
					ITypeBinding paramType = arg.resolveTypeBinding();
					if(paramType != null) { 
						String typName = paramType.getName();
						List<ASTNode> replacements = semanticInfo.getMethodParamReplacementExpressions(methodName, md, typName);
						List<ASTNode> thisList = null;
						if(thisMethodCall.containsKey(arg)) {
							thisList = thisMethodCall.get(arg);
						} else {
							thisList = new ArrayList<ASTNode>();
							thisMethodCall.put(arg, thisList);
						}
						thisList.addAll(replacements);
					}
				}
				return true;

			}
		});
		return methodParamReplacements;
	}


	// FIXME: if the fix set is expanded, clear any of these caches?? 

	private boolean paramTypesMatch(ArrayList<ITypeBinding> firstList, ArrayList<ITypeBinding> secondList) {
		for(int i = 0; i < firstList.size(); i++) {
			ITypeBinding candParam = firstList.get(i);
			ITypeBinding myParam = secondList.get(i);
			if(!myParam.isAssignmentCompatible(candParam)) {
				return false;
			}
		}
		return true;
	}

	private ArrayList<ITypeBinding> getParamTypes(IMethodBinding mb) {
		return new ArrayList<ITypeBinding>(Arrays.asList(mb.getParameterTypes()));
	}

	private boolean compatibleMethodMatch(IMethodBinding method1, IMethodBinding method2, boolean checkShrinkable) {
		if(method2.equals(method1) || (checkShrinkable && !method2.getName().equals(method1.getName()))) 
			return false;
		ArrayList<ITypeBinding> paramTypes1 = getParamTypes(method1); 
		ArrayList<ITypeBinding> paramTypes2 = getParamTypes(method2); 
		if(checkShrinkable && (paramTypes2.size() < paramTypes1.size()) ||
				(!checkShrinkable && (paramTypes2.size() == paramTypes1.size() )))
		{
			return paramTypesMatch(paramTypes2,paramTypes1);
		}
		return false;
	}
	

	private Map<ASTNode,List<Map<Integer,List<ASTNode>>>> extendableParameterMethods = null;

	public Map<ASTNode, List<Map<Integer, List<ASTNode>>>> getExtendableParameterMethods(final JavaSemanticInfo semanticInfo) {
		if(extendableParameterMethods == null) {
			extendableParameterMethods = new HashMap<ASTNode,List<Map<Integer,List<ASTNode>>>>();

			final MethodDeclaration md = (MethodDeclaration) this.getEnclosingMethod();
			final String methodName = md.getName().getIdentifier();

			this.getASTNode().accept(new ASTVisitor() {
				// FIXME: also supermethodinvocations?

				public boolean visit(MethodInvocation node) {
					IMethodBinding myMethodBinding = node.resolveMethodBinding().getMethodDeclaration();

					ITypeBinding classBinding = myMethodBinding.getDeclaringClass();
					ArrayList<IMethodBinding> compatibleMethods = new ArrayList<IMethodBinding>();

					for(IMethodBinding otherMethod : classBinding.getDeclaredMethods()) {
						if(compatibleMethodMatch(otherMethod,myMethodBinding, true)) {
							compatibleMethods.add(otherMethod);
						}
					}

					ITypeBinding superClass = classBinding.getSuperclass();
					while(superClass != null) {
						IMethodBinding[] superMethods = superClass.getDeclaredMethods();
						for(IMethodBinding superMethod : superMethods) {
							int modifiers = superMethod.getModifiers();
							if(!Modifier.isAbstract(modifiers) &&
									(Modifier.isProtected(modifiers) || Modifier.isPublic(modifiers)) &&
									compatibleMethodMatch(superMethod,myMethodBinding, true)) {
								compatibleMethods.add(superMethod);
							}
						}
						superClass = superClass.getSuperclass();
					}
					if(compatibleMethods.size() > 0) {
						ArrayList<ITypeBinding> myTypes = getParamTypes(myMethodBinding);
						List<Map<Integer,List<ASTNode>>> thisNodesOptions;
						if(extendableParameterMethods.containsKey(node)) {
							thisNodesOptions = extendableParameterMethods.get(node);
						} else {
							thisNodesOptions = new ArrayList<Map<Integer,List<ASTNode>>>();
							extendableParameterMethods.put(node,thisNodesOptions);
						}
						for(IMethodBinding compatibleMethod : compatibleMethods) {
							ArrayList<ITypeBinding> compatibleParamTypes = getParamTypes(compatibleMethod);
							List<ITypeBinding> toExtend = compatibleParamTypes.subList(myTypes.size()-1, compatibleParamTypes.size());
							
							Map<Integer, List<ASTNode>> thisExtension = new HashMap<Integer, List<ASTNode>>();
							boolean extensionDoable = true;
							int i = 0;
							for(ITypeBinding necessaryExp : toExtend) {
								List<ASTNode> replacements = semanticInfo.getMethodParamReplacementExpressions(methodName, md, necessaryExp.getName());
								if(replacements.isEmpty()) {
									extensionDoable = false;
									break;
								}
								thisExtension.put(i, replacements);
								i++;
							}
							if(extensionDoable) {
								thisNodesOptions.add(thisExtension);
							}
						}
					}
					return true;
				}

			});
		}
		return extendableParameterMethods;	
	}


	private Map<ASTNode,List<Integer>> shrinkableParameterMethods = null;

	public Map<ASTNode,List<Integer>> getShrinkableParameterMethods() {
		if(shrinkableParameterMethods == null) {
			shrinkableParameterMethods = new HashMap<ASTNode,List<Integer>>();
			this.getASTNode().accept(new ASTVisitor() {
				// FIXME: also supermethodinvocations

				public boolean visit(MethodInvocation node) {
					IMethodBinding myMethodBinding = node.resolveMethodBinding().getMethodDeclaration();

					ITypeBinding classBinding = myMethodBinding.getDeclaringClass();
					ArrayList<IMethodBinding> compatibleMethods = new ArrayList<IMethodBinding>();

					for(IMethodBinding otherMethod : classBinding.getDeclaredMethods()) {
						if(compatibleMethodMatch(myMethodBinding, otherMethod, true)) {
							compatibleMethods.add(otherMethod);
						}
					}

					ITypeBinding superClass = classBinding.getSuperclass();
					while(superClass != null) {
						IMethodBinding[] superMethods = superClass.getDeclaredMethods();
						for(IMethodBinding superMethod : superMethods) {
							int modifiers = superMethod.getModifiers();
							if(!Modifier.isAbstract(modifiers) &&
									(Modifier.isProtected(modifiers) || Modifier.isPublic(modifiers)) &&
									compatibleMethodMatch(myMethodBinding, superMethod, true)) {
								compatibleMethods.add(superMethod);
							}
						}
						superClass = superClass.getSuperclass();
					}
					if(compatibleMethods.size() > 0) {
						List<Integer> thisNodesOptions;
						if(shrinkableParameterMethods.containsKey(node)) {
							thisNodesOptions = shrinkableParameterMethods.get(node);
						} else {
							thisNodesOptions = new ArrayList<Integer>();
							shrinkableParameterMethods.put(node,thisNodesOptions);
						}
						for(IMethodBinding compatibleMethod : compatibleMethods) {
							ITypeBinding[] parameterTypes = compatibleMethod.getParameterTypes();
							int numReduce = myMethodBinding.getParameterTypes().length - parameterTypes.length;
							thisNodesOptions.add(numReduce);
						}
					}
					return true;
				}

			});
		}
		return shrinkableParameterMethods;
	}
	
	private Map<ASTNode, List<IMethodBinding>> candidateMethodReplacements= null;

	public Map<ASTNode, List<IMethodBinding>> getCandidateMethodReplacements() {
		if(candidateMethodReplacements == null) {
			candidateMethodReplacements = new HashMap<ASTNode, List<IMethodBinding>>();

			this.getASTNode().accept(new ASTVisitor() {
				// FIXME: supermethodinvocations?
				// FIXME: should I try stuff in superclasses too?  I'm not convinced this is the spec, double-check!

				public boolean visit(MethodInvocation node) {
					IMethodBinding myMethodBinding = node.resolveMethodBinding();

					if(myMethodBinding != null) {
						List<IMethodBinding> possibleReps;
						if(candidateMethodReplacements.containsKey(node)) {
							possibleReps = candidateMethodReplacements.get(node);
						} else {
							possibleReps = new ArrayList<IMethodBinding> ();
							candidateMethodReplacements.put(node,possibleReps);
						}
						ITypeBinding classBinding = myMethodBinding.getDeclaringClass();
						ITypeBinding thisReturnType = myMethodBinding.getReturnType();

						for(IMethodBinding otherMethod : classBinding.getDeclaredMethods()) {
							ITypeBinding candReturnType = otherMethod.getReturnType();

							if(candReturnType.isAssignmentCompatible(thisReturnType) &&
									compatibleMethodMatch(myMethodBinding, otherMethod, false)) {
								possibleReps.add(otherMethod);
							}
						}
					}
					return true;
				}

			});
		}
		return candidateMethodReplacements;
	}


	public ASTNode blockThatContainsThisStatement(){
		ASTNode parent = this.getASTNode().getParent();
		while(parent != null && !(parent instanceof Block)){
			parent = parent.getParent();
		}
		return parent;
	}

	private static int howManyReturns = 0;

	public static boolean hasMoreThanOneReturn(MethodDeclaration method){
		method.accept(new ASTVisitor() {
			@Override
			public boolean visit(ReturnStatement node) {
				howManyReturns++;
				return true;
			}
		});
		return howManyReturns>=2;
	}

	public boolean canBeDeleted() {
		ASTNode faultyNode = this.getASTNode();
		ASTNode parent = faultyNode.getParent();
		//Heuristic: If it is the body of an if, while, or for, it should not be removed
		if(faultyNode instanceof Block){
			//this boolean states if the faultyNode is the body of an IfStatement
			if (parent instanceof IfStatement
					&& ((IfStatement)parent).getThenStatement().equals(faultyNode))
				return false;
			//same for all these booleans
			if(parent instanceof WhileStatement
					&& ((WhileStatement)parent).getBody().equals(faultyNode))
				return false;
			if(parent instanceof ForStatement
					&& ((ForStatement)parent).getBody().equals(faultyNode))
				return false;
			if(parent instanceof EnhancedForStatement
					&& ((EnhancedForStatement)parent).getBody().equals(faultyNode))
				return false;
			if(parent instanceof IfStatement && (((IfStatement)parent).getElseStatement() != null) &&
					((IfStatement)parent).getElseStatement().equals(faultyNode))
				return false;
		}

		//Heuristic: Don't remove returns from functions that have only one return statement.
		if(faultyNode instanceof ReturnStatement){
			parent = this.getEnclosingMethod();
			if(parent != null && parent instanceof MethodDeclaration) {
				if(hasMoreThanOneReturn((MethodDeclaration)parent))
					return false;
			}
		}

		//Heuristic: If an stmt is the only stmt in a block, don´t delete it
		parent = blockThatContainsThisStatement();
		if(parent instanceof Block){
			if(((Block)parent).statements().size()==1){
				return false;
			}
		}
		return true;
	}

	public ASTNode getEnclosingMethod() {
		ASTNode parent = this.getASTNode().getParent();
		while(parent != null && !(parent instanceof MethodDeclaration)){
			parent = parent.getParent();
		}
		return parent;
	}
	public boolean isLikelyAConstructor() {
		ASTNode enclosingMethod = this.getEnclosingMethod();
		return (enclosingMethod != null) && (enclosingMethod instanceof MethodDeclaration) && 
				((MethodDeclaration) enclosingMethod).isConstructor();
	}

	public boolean parentMethodReturnsVoid() { // FIXME fix this.
		ASTNode enclosingMethod = this.getEnclosingMethod();
		if (enclosingMethod != null & enclosingMethod instanceof MethodDeclaration) {
			MethodDeclaration asMd = (MethodDeclaration) enclosingMethod;
			Type returnType = asMd.getReturnType2();
			if(returnType != null) {
				String asStr = returnType.toString(); 
				return asStr.equalsIgnoreCase("void") || asStr.equalsIgnoreCase("null");
			} else {
				return true;
			}
		}
		return false;
	}

	public boolean isWithinLoopOrCase() {
		ASTNode buggyNode = this.getASTNode();
		if(buggyNode instanceof SwitchStatement 
				|| buggyNode instanceof ForStatement 
				|| buggyNode instanceof WhileStatement
				|| buggyNode instanceof DoStatement
				|| buggyNode instanceof EnhancedForStatement)
			return true;

		while(buggyNode.getParent() != null){
			buggyNode = buggyNode.getParent();
			if(buggyNode instanceof SwitchStatement 
					|| buggyNode instanceof ForStatement 
					|| buggyNode instanceof WhileStatement
					|| buggyNode instanceof DoStatement
					|| buggyNode instanceof EnhancedForStatement)
				return true;
		}
		return false;
	}

	public void setInfo(int stmtCounter, ASTNode node) {
		this.setStmtId(stmtCounter);
		this.setLineno(ASTUtils.getLineNumber(node));
		this.setNames(ASTUtils.getNames(node));
		this.setTypes(ASTUtils.getTypes(node));
		this.setRequiredNames(ASTUtils.getScope(node));
		this.setASTNode(node);

	}



}
