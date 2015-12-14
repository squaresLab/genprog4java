package clegoues.genprog4java.mut;

import java.util.Random;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.main.ClassInfo;

public class JavaOffByOneOperation extends JavaEditOperation {

	public JavaOffByOneOperation(ClassInfo fileName, JavaStatement location) {
		super(Mutation.OFFBYONE, fileName, location);
	}
	@Override
	public void edit(final ASTRewrite rewriter, AST ast, CompilationUnit cu) {
		ASTNode locationNode = this.getLocation().getASTNode();
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
	}

}
