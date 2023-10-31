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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;

import clegoues.genprog4java.rep.JavaRepresentation;

public class JavaStatement implements Comparable<JavaStatement>{

	private ASTNode astNode;
	private ClassInfo classInfo;

	private int stmtId; // unique
	private Set<String> mustBeInScope;
	private Set<String> namesDeclared;

	public void setClassInfo(ClassInfo ci) {
		this.classInfo = ci;
	}

	public Set<String> getNamesDeclared() {
		return this.namesDeclared;
	}

	public void setNamesDeclared(Set<String> names) {
		this.namesDeclared = names;
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
					if(node.getIndex() instanceof SimpleName) {
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
			this.getASTNode().accept(new ASTVisitor() {

				public boolean visit(MethodInvocation node) {
					ASTNode stmtParent = statementParent(node);
					saveDataOfTheExpression(stmtParent, node.getExpression());
					return true;
				}
				public boolean visit(FieldAccess node) {
					ASTNode stmtParent = statementParent(node);
					saveDataOfTheExpression(stmtParent, node.getExpression());
					return true;
				}

				public boolean visit(QualifiedName node) {
					ASTNode stmtParent = statementParent(node);
					saveDataOfTheExpression(stmtParent, node.getQualifier());
					return true;
				}

				private ASTNode statementParent(ASTNode node) {
					while(node != null && !(node instanceof Statement)) {
						node = node.getParent();
					}
					return node;
				}

				private boolean isNullCheckable(ASTNode node) {
					if(node == null) 
						return false;
					if(node instanceof SimpleName) {
						SimpleName asSimpleName = (SimpleName) node;
						IBinding binding = asSimpleName.resolveBinding();
						return binding != null && binding instanceof IVariableBinding;
					}

					return node instanceof ArrayAccess || node instanceof CastExpression || node instanceof ClassInstanceCreation ||
							node instanceof SuperMethodInvocation || node instanceof  ParenthesizedExpression ||
							node instanceof SuperFieldAccess || node instanceof SuperMethodInvocation 
							;
					/* maybe: ArrayCreation */
				}

				private void saveDataOfTheExpression(ASTNode parent, ASTNode node){
					if(parent == null || parent instanceof VariableDeclarationStatement) // heuristic pseudo-hack to prevent breaking code badly
						return;
					if(isNullCheckable(node)) {
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
				}
			});
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

	private Map<Expression, List<Expression>> shrinkableExpressions = null;

	public Map<Expression, List<Expression>> getShrinkableConditionalExpressions() {
		if(shrinkableExpressions != null) {
			return shrinkableExpressions;
		}
		shrinkableExpressions = new HashMap<Expression, List<Expression>>();
		this.getASTNode().accept(new ASTVisitor() {

			private boolean isShrinkable(InfixExpression.Operator op) {
				return (op == InfixExpression.Operator.CONDITIONAL_AND) ||
						(op == InfixExpression.Operator.CONDITIONAL_OR);
			}

			private void handleExp(Expression node, Expression condExp) {
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
				handleExp(node.getExpression(), node.getExpression());
				return true;
			}
		});
		return shrinkableExpressions;
	}

	private Map<Expression,List<Expression>> extendableExpressions = null;

	public Map<Expression, List<Expression>> getExtendableConditionalExpressions() {
		if(extendableExpressions == null) {
			extendableExpressions = new HashMap<Expression, List<Expression>>();
			final MethodDeclaration md = (MethodDeclaration) ASTUtils.getEnclosingMethod(this.getASTNode());
			if (md == null) {
				return extendableExpressions;
			}
			final String methodName = md.getName().getIdentifier();
			final List<Expression> replacements = JavaSemanticInfo.getConditionalExtensionExpressions(methodName, md);

			if(replacements != null && !replacements.isEmpty()) {
				final ASTMatcher matcher = new ASTMatcher();

				this.getASTNode().accept(new ASTVisitor() {

					public boolean visit(IfStatement node) {
						handleCondition(node.getExpression());				
						return true;
					}

					public boolean visit(ConditionalExpression node) {
						handleCondition(node.getExpression());
						return true;
					}

					private void handleCondition(Expression exp) {
						List<Expression> thisList = new LinkedList<Expression>();

						List<Expression> decomposedExps = new LinkedList<Expression>();
						CollectBooleanExpressions myVisitor = new CollectBooleanExpressions(decomposedExps);
						exp.accept(myVisitor);

						// only extend expressions with candidate subexpressions they do not already contain.
						for(Expression replacement : replacements) {
							for(Expression decomposedExp : decomposedExps) {
								if(!replacement.subtreeMatch(matcher, decomposedExp)) {
									thisList.add(replacement);
								}
							}
						}
						if(!thisList.isEmpty()) {
							if(extendableExpressions.containsKey(exp)) {
								extendableExpressions.get(exp).addAll(thisList);
							} else {
								extendableExpressions.put(exp, thisList);
							}
						}
					}
				});
			}
		}
		return extendableExpressions;

	}

	private Map<Expression,List<Expression>> methodParamReplacements = null;

	public Map<Expression,List<Expression>> getReplacableMethodParameters() {
		if(methodParamReplacements == null) {
			methodParamReplacements = new HashMap<Expression,List<Expression>>();

			final MethodDeclaration md = (MethodDeclaration) ASTUtils.getEnclosingMethod(this.getASTNode());
			if (md == null) {
				return methodParamReplacements;
			}
			final String methodName = md.getName().getIdentifier();
			final Set<String> namesInScopeHere = JavaSemanticInfo.inScopeAt(this);

			this.getASTNode().accept(new ASTVisitor() {

				private void handleCandidateReplacements(List<Expression> args) {
					for(Expression arg : args) {
						ITypeBinding paramType = arg.resolveTypeBinding();
						if(paramType != null) { 
							String typName = paramType.getName();
							List<Expression> replacements = JavaSemanticInfo.getMethodParamReplacementExpressions(methodName, md, typName);
							String argAsString = arg.toString();
							if(replacements != null && !replacements.isEmpty()) {
								List<Expression> thisList = null;
								List<Expression> filteredReplacements = new LinkedList<Expression>();
								for(Expression candRep : replacements) {
									if(JavaRepresentation.semanticInfo.areNamesInScope(candRep, namesInScopeHere))
									{
										String candString = candRep.toString();
										if(!candString.equals(argAsString)) {
											filteredReplacements.add(candRep);
										}
									}
								}
								if(!filteredReplacements.isEmpty()) {
									if(methodParamReplacements.containsKey(arg)) {
										thisList = methodParamReplacements.get(arg);
									} else {
										thisList = new ArrayList<Expression>();
										methodParamReplacements.put(arg, thisList);
									}
									thisList.addAll(filteredReplacements);
								}
							}
						}
					}
				}

				@SuppressWarnings("unchecked")
				public boolean visit(SuperMethodInvocation node) {
					List<Expression> args = node.arguments();
					handleCandidateReplacements(args);
					return true;
				}

				@SuppressWarnings("unchecked")
				public boolean visit(MethodInvocation node) {		
					List<Expression> args = node.arguments();
					handleCandidateReplacements(args);
					return true;
				}
			});


		}
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

		ITypeBinding returnType1 = method1.getReturnType();
		ITypeBinding returnType2 = method2.getReturnType();
		if(!returnType1.isAssignmentCompatible(returnType2))
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


	private Map<ASTNode,List<List<ASTNode>>> extendableParameterMethods = null;

	public Map<ASTNode, List<List<ASTNode>>> getExtendableParameterMethods() {
		if(extendableParameterMethods == null) {
			extendableParameterMethods = new HashMap<ASTNode,List<List<ASTNode>>>();

			final MethodDeclaration md = (MethodDeclaration) ASTUtils.getEnclosingMethod(this.getASTNode());
			if (md == null) {
				return extendableParameterMethods;
			}
			final String methodName = md.getName().getIdentifier();
			final Set<String> namesInScopeHere = JavaSemanticInfo.inScopeAt(this);

			this.getASTNode().accept(new ASTVisitor() {

				private void handleInvocation(ASTNode node, IMethodBinding mb) {
					if(mb == null) {
						return;
					}
					IMethodBinding myMethodBinding = mb.getMethodDeclaration();
					if(myMethodBinding == null)
						return;

					ITypeBinding classBinding = myMethodBinding.getDeclaringClass();
					List<IMethodBinding> compatibleMethods = new LinkedList<IMethodBinding>();

					for(IMethodBinding otherMethod : classBinding.getDeclaredMethods()) {
						int modifiers = otherMethod.getModifiers();

						if(!Modifier.isAbstract(modifiers) && compatibleMethodMatch(otherMethod,myMethodBinding, true)) {
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
						List<List<ASTNode>> thisNodesOptions;

						if(extendableParameterMethods.containsKey(node)) {
							thisNodesOptions = extendableParameterMethods.get(node);
						} else {
							thisNodesOptions = new ArrayList<List<ASTNode>>();
							extendableParameterMethods.put(node,thisNodesOptions);
						}

						for(IMethodBinding compatibleMethod : compatibleMethods) {
							ArrayList<ITypeBinding> compatibleParamTypes = getParamTypes(compatibleMethod);
							int startIndex = myTypes.size() == 0 ? 0 : myTypes.size() - 1;
							List<ITypeBinding> toExtend = compatibleParamTypes.subList(startIndex, compatibleParamTypes.size());

							List<ASTNode> thisExtension = new ArrayList<ASTNode>();
							boolean extensionDoable = true;
							for(ITypeBinding necessaryExp : toExtend) {
								List<Expression> replacements = JavaSemanticInfo.getMethodParamReplacementExpressions(methodName, md, necessaryExp.getName());
								List<Expression> filteredReplacements = new LinkedList<Expression>();
								if(replacements != null){
								for(Expression candRep : replacements) {
									if(JavaRepresentation.semanticInfo.areNamesInScope(candRep, namesInScopeHere))
										filteredReplacements.add(candRep);
								}
								}
								if(filteredReplacements != null){
								if(filteredReplacements.isEmpty()) {
									extensionDoable = false;
									break;
								}
								thisExtension.addAll(filteredReplacements);
								}
							}
							if(extensionDoable) {
								thisNodesOptions.add(thisExtension);
							}
						}
					}
				}
				public boolean visit(SuperMethodInvocation node) {
					IMethodBinding mb = node.resolveMethodBinding();
					handleInvocation(node, mb);
					return true;
				}

				public boolean visit(MethodInvocation node) {
					IMethodBinding mb = node.resolveMethodBinding();
					handleInvocation(node, mb);
					return true;
				}

			});
		}
		return extendableParameterMethods;	
	}

	private List<ASTNode> indexedCollectionObjects = null;

	public  List<ASTNode> getIndexedCollectionObjects() {
		if(indexedCollectionObjects == null) {
			indexedCollectionObjects = new ArrayList<ASTNode>();

			this.getASTNode().accept(new ASTVisitor() {

				public boolean visit(MethodInvocation node) {
					Expression methodCall = node.getExpression();
					SimpleName methodName = node.getName();
					if(methodCall == null || methodName == null) {
						return true;
					}
					switch(methodName.getIdentifier()) {
					case "removeRange":
					case "subList":
						break;
					case "add":
					case "addAll": 
						if(node.arguments().size() > 1) {
							break;
						}
						else
							return true;
					case "get":
					case "remove":
					case "set":
						break;
					default: return true;
					}

					ITypeBinding methodCallTypeBinding = methodCall.resolveTypeBinding();
					if(methodCallTypeBinding == null) 
						return true;
					ITypeBinding td = methodCallTypeBinding.getTypeDeclaration();
					if(td == null) 
						return true;
					String name = td.getName();
					ITypeBinding decl = methodCallTypeBinding.getTypeDeclaration();
					while(decl != null && decl.getSuperclass() != null && !name.equals("AbstractList")) {
						decl = decl.getSuperclass().getTypeDeclaration();
						name = decl.getName();
					}
					if(!name.equals("AbstractList")) { 
						return true;
					}
					indexedCollectionObjects.add(node);
					return true;
				}
				/*	void	add(int index, E element)
					boolean	addAll(int index, Collection<? extends E> c)
					abstract E	get(int index)
					E	remove(int index)
					protected void	removeRange(int fromIndex, int toIndex)
					E	set(int index, E element)
					List<E>	subList(int fromIndex, int toIndex) */
			});
		}
		return indexedCollectionObjects;
	}

	/*
	 * B = buggy statements
collect method invocations of (@\textbf{[collection objects]}@) in B and put them into collection C
insert a if statement before B

loop for all method invocation in C
{
 if a method invocation has an index parameter
 {
 insert a conditional expression that checks whether the index parameter is smaller than the size of its collection object
 }
}
concatenate conditions using AND 

if B include return statement
{
 negate the concatenated the conditional expression
 insert a return statement that returns a default value into THEN section of the if statement
 insert B after the if statement
} else {
 insert B into THEN section of the if statement
}
	 */


	private Map<ASTNode,List<Integer>> shrinkableParameterMethods = null;

	public Map<ASTNode,List<Integer>> getShrinkableParameterMethods() {
		if(shrinkableParameterMethods == null) {
			shrinkableParameterMethods = new HashMap<ASTNode,List<Integer>>();
			this.getASTNode().accept(new ASTVisitor() {
				// FIXME: also supermethodinvocations

				public boolean visit(MethodInvocation node) {
					IMethodBinding mb = node.resolveMethodBinding();
					if(mb == null) {
						return true;
					}
					IMethodBinding myMethodBinding = mb.getMethodDeclaration();

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


	private List<ASTNode> candidateObjectsToInit = null;

	public   List<ASTNode> getObjectsAsMethodParams() {
		if(candidateObjectsToInit == null) {
			candidateObjectsToInit = new LinkedList<ASTNode>();

			this.getASTNode().accept(new ASTVisitor() {
				public boolean visit(MethodInvocation node) {
					for(Object arg : node.arguments()) {
						if(arg instanceof SimpleName) {
							Expression argNode = (Expression) arg;
							ITypeBinding binding = argNode.resolveTypeBinding();
							if(binding != null && binding.isClass()) {
								candidateObjectsToInit.add(node);
							}
						}
					}
					return true;
				}
			});
		}

		return candidateObjectsToInit;
	}

	private Map<ASTNode, List<IMethodBinding>> candidateMethodReplacements= null;

	public Map<ASTNode, List<IMethodBinding>> getCandidateMethodReplacements() {
		if(candidateMethodReplacements == null) {
			candidateMethodReplacements = new HashMap<ASTNode, List<IMethodBinding>>();

			this.getASTNode().accept(new ASTVisitor() {

				private void handleMethod(ASTNode node, IMethodBinding myMethodBinding) {
					boolean addToMap = false;
					List<IMethodBinding> possibleReps;
					if(candidateMethodReplacements.containsKey(node)) {
						possibleReps = candidateMethodReplacements.get(node);
					} else {
						possibleReps = new LinkedList<IMethodBinding> ();
					}
					ITypeBinding classBinding = myMethodBinding.getDeclaringClass();

					for(IMethodBinding otherMethod : classBinding.getDeclaredMethods()) {
						int modifiers = otherMethod.getModifiers();

						if(!Modifier.isAbstract(modifiers) &&
								compatibleMethodMatch(myMethodBinding, otherMethod, false)) {
							possibleReps.add(otherMethod);
							addToMap = true;
						}
					}

					ITypeBinding superClass = classBinding.getSuperclass();
					while(superClass != null) {
						IMethodBinding[] superMethods = superClass.getDeclaredMethods();

						for(IMethodBinding superMethod : superMethods) {
							int modifiers = superMethod.getModifiers();

							if(!Modifier.isAbstract(modifiers) &&
									(Modifier.isProtected(modifiers) || Modifier.isPublic(modifiers)) &&
									compatibleMethodMatch(myMethodBinding, superMethod, false)) {
								possibleReps.add(superMethod);
								addToMap = true;
							}
						}
						superClass = superClass.getSuperclass();
					}

					if(addToMap) {
						candidateMethodReplacements.put(node,possibleReps);
					}
				}

				public boolean visit(SuperMethodInvocation node) {
					IMethodBinding myMethodBinding = node.resolveMethodBinding();
					if(myMethodBinding != null) {
						handleMethod(node, myMethodBinding);
					}
					return true;
				}

				public boolean visit(MethodInvocation node) {
					IMethodBinding myMethodBinding = node.resolveMethodBinding();
					if(myMethodBinding != null) {
						handleMethod(node, myMethodBinding);
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
		howManyReturns = 0;
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
		//Heuristic: Don't remove returns from functions that have only one return statement.
		if(faultyNode instanceof ReturnStatement){
			parent = ASTUtils.getEnclosingMethod(this.getASTNode());
			if(parent != null && parent instanceof MethodDeclaration) {
				if(!hasMoreThanOneReturn((MethodDeclaration)parent))
					return false;
			}
		}

		return true;
	}

	
	private List<Expression> casteeExpressions = null;
	public  List<Expression> getCasteeExpressions(){
		if(casteeExpressions == null) {
			casteeExpressions = new LinkedList<Expression>();

			this.getASTNode().accept(new ASTVisitor() {
				private List<String> casteeExpressionsString = new ArrayList<String>();
				public boolean visit(CastExpression node) {
					if(!casteeExpressionsString.contains(node.getExpression().toString())){
						casteeExpressions.add(node.getExpression());
						casteeExpressionsString.add(node.getExpression().toString());
					}
					return true;
				}
			});
		}

		return casteeExpressions;
	}

	private List<Expression> toReplaceCasteeExpressions = null;
	public  List<Expression> getExpressionsToReplaceCastee(){
		if(toReplaceCasteeExpressions == null) {
			toReplaceCasteeExpressions = new LinkedList<Expression>();

			ASTNode startAt = this.getASTNode();
			while(!(startAt instanceof TypeDeclaration)){
				startAt=startAt.getParent();
			}

			startAt.accept(new ASTVisitor() {
				private List<String> toReplaceCasteeExpressionsStrings = new ArrayList<String>();
				public boolean visit(CastExpression node) {
					if(!toReplaceCasteeExpressionsStrings.contains(node.getExpression().toString())){
						toReplaceCasteeExpressions.add(node.getExpression());
						toReplaceCasteeExpressionsStrings.add(node.getExpression().toString());
					}
					return true;
				}
				public boolean visit(MethodInvocation node) {
					if(!toReplaceCasteeExpressionsStrings.contains(node.toString())){
						toReplaceCasteeExpressions.add(node);
						toReplaceCasteeExpressionsStrings.add(node.toString());
					}
					return true;
				}
				public boolean visit(VariableDeclarationFragment node) {
					if(!toReplaceCasteeExpressionsStrings.contains(node.getName().toString())){
						toReplaceCasteeExpressions.add(node.getName());
						toReplaceCasteeExpressionsStrings.add(node.getName().toString());
					}
					return true;
				}
				public boolean visit(ArrayAccess node) {
					if(!toReplaceCasteeExpressionsStrings.contains(node.toString())){
						toReplaceCasteeExpressions.add(node);
						toReplaceCasteeExpressionsStrings.add(node.toString());
					}
					return true;
				}
			});
		}

		return toReplaceCasteeExpressions;
	}
	
	private List<Type> casterTypes = null;
	public  List<Type> getCasterTypes(){
		if(casterTypes == null) {
			casterTypes = new LinkedList<Type>();
			this.getASTNode().accept(new ASTVisitor() {
				/*
				public boolean visit(SimpleType node) {
					casterTypes.add(node);
					return true;
				}*/
				private List<String> casterTypesString = new ArrayList<String>();
				public boolean visit(CastExpression node) {
					if(!casterTypesString.contains(node.getType().toString())){
						casterTypes.add(node.getType());
						casterTypesString.add(node.getType().toString());
					}
					return true;
				}

			});
		}

		return casterTypes;
	}

	private List<ASTNode> toReplaceCasterTypes = null;
	public  List<ASTNode> getTypesToReplaceCaster(){
		if(toReplaceCasterTypes == null) {
			toReplaceCasterTypes = new LinkedList<ASTNode>();

			ASTNode startAt = this.getASTNode();
			while(!(startAt instanceof TypeDeclaration)){
				startAt=startAt.getParent();
			}

			startAt.accept(new ASTVisitor() {
				private List<String> toReplaceCasterTypesStrings = new ArrayList<String>();
				public boolean visit(ArrayType node) {
					if(!toReplaceCasterTypesStrings.contains(node.toString())){
						toReplaceCasterTypes.add(node);
						toReplaceCasterTypesStrings.add(node.toString());
					}
					return true;
				}
				public boolean visit(ParameterizedType node) {
					if(!toReplaceCasterTypesStrings.contains(node.toString())){
						toReplaceCasterTypes.add(node);
						toReplaceCasterTypesStrings.add(node.toString());
					}
					return true;
				}
				public boolean visit(PrimitiveType node) {
					if(!toReplaceCasterTypesStrings.contains(node.toString())){
						toReplaceCasterTypes.add(node);
						toReplaceCasterTypesStrings.add(node.toString());
					}
					return true;
				}
				public boolean visit(QualifiedType node) {
					if(!toReplaceCasterTypesStrings.contains(node.toString())){
						toReplaceCasterTypes.add(node);
						toReplaceCasterTypesStrings.add(node.toString());
					}
					return true;
				}
				public boolean visit(SimpleType node) {
					if(!toReplaceCasterTypesStrings.contains(node.toString())){
						toReplaceCasterTypes.add(node);
						toReplaceCasterTypesStrings.add(node.toString());
					}
					return true;
				}
				public boolean visit(UnionType node) {
					if(!toReplaceCasterTypesStrings.contains(node.toString())){
						toReplaceCasterTypes.add(node);
						toReplaceCasterTypesStrings.add(node.toString());
					}
					return true;
				}
				public boolean visit(WildcardType node) {
					if(!toReplaceCasterTypesStrings.contains(node.toString())){
						toReplaceCasterTypes.add(node);
						toReplaceCasterTypesStrings.add(node.toString());
					}
					return true;
				}
			});
		}

		return toReplaceCasterTypes; 
	}

	public boolean isLikelyAConstructor() {
		ASTNode enclosingMethod = ASTUtils.getEnclosingMethod(this.getASTNode());
		return (enclosingMethod != null) && (enclosingMethod instanceof MethodDeclaration) && 
				((MethodDeclaration) enclosingMethod).isConstructor();
	}

	public boolean parentMethodReturnsVoid() { // FIXME fix this.
		ASTNode enclosingMethod = ASTUtils.getEnclosingMethod(this.getASTNode());
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
		this.setASTNode(node);
	}



}
