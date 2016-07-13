package clegoues.genprog4java.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.print.attribute.SetOfIntegerSyntax;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import clegoues.util.Pair;

/** stores and computes compilation unit- or file-level semantic info. */  
public class JavaSemanticInfo {

	/** these maps are static because they store information about the 
	 * original program under repair and are not updated..
	 */

	/** names in scope at a statement (identified by integer ID, hence the key type in this map 
	 * from the class level -- fields and methods, basically */
	public static HashMap<Integer, Set<String>> classScopeMap = new HashMap<Integer, Set<String>>();

	/** names in scope at a statement (ID'd by integer) within the enclosing method */ 
	public static HashMap<Integer, Set<String>> methodScopeMap = new HashMap<Integer, Set<String>>();

	/** type information, stored heuristically as strings, for methods and variables */
	private static Map<String,String> methodReturnType = new HashMap<String,String>();
	private static HashMap<String, String> variableDataTypes = new HashMap<String, String>();
	

	/** whether a statement references a final variable is relevant to whether we
	 * can move or copy it around.
	 */
	private static Map<Integer, Boolean> containsFinalVarRef = new HashMap<Integer,Boolean> ();

	public void collectFinalVarInfo(JavaStatement s, boolean fvi) {
		containsFinalVarRef.put(s.getStmtId(), fvi);
	}

	public boolean getFinalVarStatus(int id) {
		return containsFinalVarRef.get(id);
	}


	private static Map<String, Map<String,List<Expression>>> methodParamExpressionsInScope = null;
	
	/** 
	 * information for the "method parameter replacement" edit template"
	 * 
	 * identifies valid expressions from within the same method that can be used to replace
	 * a method parameter of the given (string, heuristically) type.
	 * @param methodName method for which this is being computed; serves as key
	 * in cache
	 *  @param md method declaration for that methodName
	 *  @param string description of desired type of expression
	 *  @return list of expressions, from within this method scope, that can be swapped in for the
	 *  given desired expression type
	 */
	public List<Expression> getMethodParamReplacementExpressions(final String methodName, MethodDeclaration md, String desiredType) {
		Map<String,List<Expression>> typeToExpressions = null;
		if(methodParamExpressionsInScope == null) {
			methodParamExpressionsInScope = new HashMap<String, Map<String,List<Expression>>>();
		}
		if(methodParamExpressionsInScope.containsKey(methodName)) {
			typeToExpressions = methodParamExpressionsInScope.get(methodName);
		} else {
			typeToExpressions = new HashMap<String,List<Expression>>();
			methodParamExpressionsInScope.put(methodName, typeToExpressions);
		}
		if(typeToExpressions.containsKey(desiredType)) {
			return typeToExpressions.get(desiredType);
		}
		final Map<String,List<Expression>> forVisitor = methodParamExpressionsInScope.get(methodName);
		md.accept(new ASTVisitor() {
			public boolean visit(MethodInvocation node) {
				List<Expression> args = node.arguments();
				for(Expression arg : args) {
					ITypeBinding typeBinding = 	arg.resolveTypeBinding();
					if(typeBinding != null) {
						String typeName = typeBinding.getName();
						List<Expression> ofType = null;
						if(forVisitor.containsKey(typeName)) {
							ofType = forVisitor.get(typeName);
						} else {
							ofType = new ArrayList<Expression>();
							forVisitor.put(typeName, ofType);
						}
						ofType.add(arg);			
					}
				}
				return true;
			}

		});
		return typeToExpressions.get(desiredType);
	}

	private static Map<String, List<Expression>> conditionExtensionsInScope = null;

	public List<Expression> getConditionalExtensionExpressions(String methodName, MethodDeclaration md) {
		if(conditionExtensionsInScope == null) {
			conditionExtensionsInScope = new HashMap<String, List<Expression>>();
		}
		if(conditionExtensionsInScope.containsKey(methodName)) {
			return conditionExtensionsInScope.get(methodName);
		} 
		List<Expression> fullConditionsInScope = this.getConditionalReplacementExpressions(methodName, md);
		final List<Expression> expressionsInScope = new ArrayList<Expression>(); // possible FIXME: do I start with the list above?  I think it will auto-populate, right?
		conditionExtensionsInScope.put(methodName, expressionsInScope);
		for(ASTNode cond : fullConditionsInScope) {
			cond.accept(new ASTVisitor() {
				private void tryAdd(Expression node) {
					ITypeBinding tb = node.resolveTypeBinding();
					if(tb != null) {
						if(tb.getName().equals("boolean")) {
							expressionsInScope.add(node);
						}
					}
				}
				public boolean visit(PrefixExpression node) {
					if(node.getOperator() == PrefixExpression.Operator.NOT) {
						expressionsInScope.add(node);
					}
					return true;
				}
				public boolean visit(ConditionalExpression node) {
					expressionsInScope.add(node);
					return true;
				}
				public boolean visit(FieldAccess node) {
					tryAdd(node);
					return true;
				}
				public boolean visit(InfixExpression node) {
					InfixExpression.Operator op = node.getOperator();
					if(op ==  InfixExpression.Operator.LESS ||   
							op == InfixExpression.Operator.GREATER ||
							op == InfixExpression.Operator.LESS_EQUALS ||
							op == InfixExpression.Operator.GREATER_EQUALS ||
							op == InfixExpression.Operator.EQUALS || 
							op == InfixExpression.Operator.NOT_EQUALS ||
							op == InfixExpression.Operator.CONDITIONAL_AND ||
							op == InfixExpression.Operator.CONDITIONAL_OR) {
						expressionsInScope.add(node);
					}
					return true;
				} 
				public boolean visit(InstanceofExpression node) {
					expressionsInScope.add(node);
					return true;
				} 
				public boolean visit(MethodInvocation node) {
					tryAdd(node);
					return true;
				} 
				public boolean visit(SimpleName node) {
					tryAdd(node);
					return true;
				} 
				public boolean visit(QualifiedName node) {
					tryAdd(node);
					return true;
				}
				public boolean visit(SuperFieldAccess node) {
					tryAdd(node);
					return true;
				}
				public boolean visit(SuperMethodInvocation node) {
					tryAdd(node);
					return true;
				}

			});
		}
		return expressionsInScope;
	}

	private static Map<String, List<Expression>> conditionalExpressionsInScope = null;

	public List<Expression> getConditionalReplacementExpressions(final String methodName, MethodDeclaration md) {
		if(conditionalExpressionsInScope == null) {
			conditionalExpressionsInScope = new HashMap<String,List<Expression>>();
		}
		if(conditionalExpressionsInScope.containsKey(methodName)) {
			return conditionalExpressionsInScope.get(methodName);
		} else {		
			final List<Expression> expressionsInScope = new ArrayList<Expression>();
			conditionalExpressionsInScope.put(methodName, expressionsInScope);
			md.accept(new ASTVisitor() {
				public boolean visit(ConditionalExpression node) {
					expressionsInScope.add(node.getExpression());
					return true;
				}
				public boolean visit(IfStatement node) {
					expressionsInScope.add(node.getExpression());
					return true;
				}

			});
			return expressionsInScope;
		}
	}

	public void addAllSemanticInfo(JavaParser myParser) {
		JavaSemanticInfo.methodReturnType.putAll(myParser.getMethodReturnTypes());
		JavaSemanticInfo.getVariableDataTypes().putAll(myParser.getVariableDataTypes());
	}

	public void addToClassScopeMap(JavaStatement s, Set<String> scope) {
		JavaSemanticInfo.classScopeMap.put(s.getStmtId(),scope);
	}



	public void addToMethodScopeMap(JavaStatement s, Set<String> scope) {
		JavaSemanticInfo.methodScopeMap.put(s.getStmtId(),scope);
	}

	public boolean scopeCheckOK(JavaStatement potentiallyBuggyStmt, JavaStatement potentialFixStmt) {
		// I *believe* this is just variable names and doesn't check required
		// types, which are also collected
		// at parse time and thus could be considered here.
		Set<String> classScope = classScopeMap.get(potentiallyBuggyStmt.getStmtId());
		Set<String> methodScope = methodScopeMap.get(potentiallyBuggyStmt.getStmtId());

		Set<String> inScopeAt = new HashSet<String>(classScope);
		inScopeAt.addAll(methodScope);

		Set<String> requiredScopes = potentialFixStmt.getRequiredNames();
		for (String req : requiredScopes) {
			if (!inScopeAt.contains(req)) {
				return false;
			}
		}

		Set<String> declares = potentialFixStmt.getNamesDeclared();
		if(declares != null) {
			for(String req : declares) {
				if(methodScope.contains(req)) {
					return false;
				}
			}
		}
		return true;
	}

	public Map<String,String> getMethodReturnTypes() {
		return JavaSemanticInfo.methodReturnType;
	}

	public static HashMap<String, String> getVariableDataTypes() {
		return variableDataTypes;
	}

	public static void setVariableDataTypes(HashMap<String, String> variableDataTypes) {
		JavaSemanticInfo.variableDataTypes = variableDataTypes;
	}

	public String returnTypeOfThisMethod(String matchString) {
		if(methodReturnType.containsKey(matchString.toLowerCase())) {
			return methodReturnType.get(matchString.toLowerCase());
		}
		return null;
	}
}

