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

package clegoues.genprog4java.mut;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.main.ClassInfo;

public class JavaEditOperation implements
EditOperation<JavaStatement, ASTRewrite, AST> {

	private Mutation mutType;
	private JavaStatement location = null;
	private JavaStatement fixCode = null;
	private ClassInfo fileInfo = null;

	protected JavaEditOperation(Mutation mutType, ClassInfo fileName, JavaStatement location) {
		this.mutType = mutType;
		this.location = location;
		this.fileInfo = fileName;
	}


	protected JavaEditOperation(Mutation mutType, ClassInfo fileName, JavaStatement location,
			JavaStatement fixCode) {
		this.mutType = mutType;
		this.location = location;
		this.fixCode = fixCode;
		this.fileInfo = fileName;
	}


	@Override
	public Mutation getType() {
		return this.mutType;
	}

	@Override
	public void setType(Mutation type) {
		this.mutType = type;
	}

	public JavaStatement getLocation() {
		return this.location;
	}

	public void setLocation(JavaStatement location) {
		this.location = location;
	}

	public void setFixCode(JavaStatement fixCode) {
		this.fixCode = fixCode;
	}

	public JavaStatement getFixCode() {
		return this.fixCode;
	}

	public ClassInfo getFileInfo() {
		return this.fileInfo;
	}

	public void setFileInfo(ClassInfo newFileName){
		fileInfo = newFileName;
	}

	@Override
	public void edit(final ASTRewrite rewriter, AST ast, CompilationUnit cu) {
		ASTNode locationNode = this.getLocation().getASTNode();

		// these are used for array access related checks for implementing PAR templates
		final Map<ASTNode, List<ASTNode>> nodestmts = new HashMap<ASTNode, List<ASTNode>>();	// to track the parent nodes of array access nodes

		switch (this.getType()) {

		case NULLINSERT:
			// TODO:Have to figure this out
			//This is the same as delete, what is it supposed to be?
			rewriter.remove(locationNode, null);

		case OFFBYONE:
			locationNode.accept(new ASTVisitor() {
				int mutationtype;	// used to randomly put + or - operator while mutating array index
				// method to visit all ArrayAccess nodes modify array index by 1
				public boolean visit(ArrayAccess node) {

					// using random numbers (even or odd) to increase or decrease the index by 1
					Random rand = new Random();
					int randomNum = rand.nextInt(11);
					if(randomNum%2==0){
						mutationtype = 0;
					}else{
						mutationtype = 1;
					}
					Expression arrayindex = node.getIndex(); // original index
					Expression mutatedindex = mutateIndex(arrayindex, 1); // method call to get mutated index
					rewriter.replace(arrayindex, mutatedindex, null);	// replacing original index with mutated index
					return true;
				}
				
				

				// recursive method to mutate array index. (increase or decrease the index by 1)
				private Expression mutateIndex(Expression arrayindex, int mutateflag) { // arrayindex is the index to be mutated, mutateflag is used to check if mutation is to be performed.

					if (arrayindex instanceof SimpleName) {  // if index is simple variable name
						SimpleName name = arrayindex.getAST().newSimpleName(arrayindex.toString());	// fetch the name
						if (mutateflag == 0) {	// if no mutation is to be performed then return the index
							return name;
						}
						// create infix expression with index +/- 1
						InfixExpression mutatedindex = null;
						mutatedindex = arrayindex.getAST().newInfixExpression();
						mutatedindex.setLeftOperand(name);
						if (mutationtype == 0) {
							mutatedindex.setOperator(Operator.MINUS);
							mutationtype = 1;
						} else {
							mutatedindex.setOperator(Operator.PLUS);
							mutationtype = 0;
						}
						mutatedindex.setRightOperand(arrayindex.getAST().newNumberLiteral("1"));
						// return mutated index
						return mutatedindex;
					} else if (arrayindex instanceof NumberLiteral) { // if index is number
						NumberLiteral number = arrayindex.getAST().newNumberLiteral(arrayindex.toString());
						if (mutateflag == 0) { // if no mutation is to be performed then return the index
							return number;
						}
						// create infix expression with index +/- 1
						InfixExpression mutatedindex = null;
						mutatedindex = arrayindex.getAST().newInfixExpression();
						mutatedindex.setLeftOperand(number);
						if (mutationtype == 0) {
							mutatedindex.setOperator(Operator.MINUS);
							mutationtype = 1;
						} else {
							mutatedindex.setOperator(Operator.PLUS);
							mutationtype = 0;
						}
						mutatedindex.setRightOperand(arrayindex.getAST().newNumberLiteral("1"));
						// return mutated index
						return mutatedindex;
					} else if (arrayindex instanceof PostfixExpression && (arrayindex.toString().contains("++") || arrayindex.toString().contains("--"))) { // if index postfix expression
						PostfixExpression pexp = arrayindex.getAST().newPostfixExpression();
						String indexname = ((PostfixExpression) arrayindex).getOperand().toString();
						pexp.setOperand(arrayindex.getAST().newSimpleName(indexname));

						if (arrayindex.toString().contains("++")) {
							pexp.setOperator(org.eclipse.jdt.core.dom.PostfixExpression.Operator.INCREMENT);
						} else if (arrayindex.toString().contains("--")) {
							pexp.setOperator(org.eclipse.jdt.core.dom.PostfixExpression.Operator.DECREMENT);
						}

						if (mutateflag == 0) { // if no mutation is to be performed then return the index
							return pexp;
						}
						// create infix expression with index +/- 1
						InfixExpression mutatedindex = null;
						mutatedindex = arrayindex.getAST().newInfixExpression();
						mutatedindex.setLeftOperand(pexp);

						if (mutationtype == 0) {
							mutatedindex.setOperator(Operator.MINUS);
							mutationtype = 1;
						} else {
							mutatedindex.setOperator(Operator.PLUS);
							mutationtype = 0;
						}

						mutatedindex.setRightOperand(arrayindex.getAST().newNumberLiteral("1"));
						// return mutated index
						return mutatedindex;

					} else if (arrayindex instanceof PrefixExpression && (arrayindex.toString().contains("++") || arrayindex.toString().contains("--"))) { // if index is prefix expression
						PrefixExpression pexp = arrayindex.getAST().newPrefixExpression();
						String indexname = ((PrefixExpression) arrayindex).getOperand().toString();
						pexp.setOperand(arrayindex.getAST().newSimpleName(indexname));

						if (arrayindex.toString().contains("++")) {
							pexp.setOperator(org.eclipse.jdt.core.dom.PrefixExpression.Operator.INCREMENT);
						} else if (arrayindex.toString().contains("--")) {
							pexp.setOperator(org.eclipse.jdt.core.dom.PrefixExpression.Operator.DECREMENT);
						}

						if (mutateflag == 0) { // if no mutation is to be performed then return the index
							return pexp;
						}
						// create infix expression with index +/- 1
						InfixExpression mutatedindex = null;
						mutatedindex = arrayindex.getAST().newInfixExpression();
						mutatedindex.setLeftOperand(pexp);
						if (mutationtype == 0) {
							mutatedindex.setOperator(Operator.MINUS);
							mutationtype = 1;
						} else {
							mutatedindex.setOperator(Operator.PLUS);
							mutationtype = 0;
						}
						mutatedindex.setRightOperand(arrayindex.getAST().newNumberLiteral("1"));
						// return mutated index
						return mutatedindex;
					} else if (arrayindex instanceof InfixExpression) {// if index is infix expression
						InfixExpression iexp = arrayindex.getAST().newInfixExpression();
						Expression loperand = ((InfixExpression) arrayindex).getLeftOperand();
						if (loperand != null) {
							iexp.setLeftOperand(mutateIndex(((InfixExpression) arrayindex).getLeftOperand(), 0));
						}

						Operator ioperator = ((InfixExpression) arrayindex).getOperator();
						iexp.setOperator(ioperator);

						Expression roperand = ((InfixExpression) arrayindex).getRightOperand();
						if (roperand != null) {
							iexp.setRightOperand(mutateIndex(((InfixExpression) arrayindex).getRightOperand(), 0));
						}
						// create infix expression with index +/- 1
						InfixExpression mutatedindex = null;
						mutatedindex = arrayindex.getAST().newInfixExpression();
						mutatedindex.setLeftOperand(iexp);
						if (mutationtype == 0) {
							mutatedindex.setOperator(Operator.MINUS);
							mutationtype = 1;
						} else {
							mutatedindex.setOperator(Operator.PLUS);
							mutationtype = 0;
						}
						mutatedindex.setRightOperand(arrayindex.getAST().newNumberLiteral("1"));
						// return mutated index
						return mutatedindex;
					}
					return arrayindex;
				}
			});

			break;

		default:
			break;

		}
	}

	


}
