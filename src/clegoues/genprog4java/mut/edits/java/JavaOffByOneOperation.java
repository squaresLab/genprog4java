package clegoues.genprog4java.mut.edits.java;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.holes.java.SimpleJavaHole;
import clegoues.genprog4java.mut.holes.java.SubExpsHole;

public class JavaOffByOneOperation extends JavaEditOperation {
	private enum mutationType { ADD, SUBTRACT};

	// used to randomly add or subtract 1 while mutating array index
	private mutationType mutType;

	public JavaOffByOneOperation(JavaLocation location, HashMap<String, EditHole> sources) {
		super(Mutation.OFFBYONE, location, sources);
		this.holeNames.add("arrayCheck");
		int randomNum = Configuration.randomizer.nextInt(11);

		if(randomNum%2==0){
			mutType = mutationType.SUBTRACT;
		}else{
			mutType = mutationType.ADD;
		}

	}
	@Override
	public void edit(final ASTRewrite rewriter) {
		ASTNode locationNode = ((JavaStatement) this.getLocation()).getASTNode(); // not used, but being completist
		SimpleJavaHole thisHole = (SimpleJavaHole) this.getHoleCode("arrayCheck");
		ArrayAccess arrayAccess  = (ArrayAccess) thisHole.getCode();

		Expression arrayindex = arrayAccess.getIndex(); // original index
		Expression mutatedindex = mutateIndex(arrayindex, true); // method call to get mutated index
		rewriter.replace(arrayindex, mutatedindex, null);	// replacing original index with mutated index
	}


	// recursive method to mutate array index. (increase or decrease the index by one)
	private Expression mutateIndex(Expression arrayindex, Boolean mutateflag) { // arrayindex is the index to be mutated, mutateflag is used to check if mutation is to be performed.
		if (arrayindex instanceof SimpleName) {  // if index is simple variable name
			SimpleName name = arrayindex.getAST().newSimpleName(arrayindex.toString());	// fetch the name
			if (mutateflag == false) {	// if no mutation is to be performed then return the index
				return name;
			}
			// create infix expression with index +/- 1
			InfixExpression mutatedindex = null;
			mutatedindex = arrayindex.getAST().newInfixExpression();
			mutatedindex.setLeftOperand(name);
			if (mutType == mutationType.SUBTRACT) {
				mutatedindex.setOperator(Operator.MINUS);
				mutType = mutationType.ADD;
			} else {
				mutatedindex.setOperator(Operator.PLUS);
				mutType = mutationType.SUBTRACT;
			}
			mutatedindex.setRightOperand(arrayindex.getAST().newNumberLiteral("1"));
			// return mutated index
			return mutatedindex;
		} else if (arrayindex instanceof NumberLiteral) { // if index is number
			NumberLiteral number = arrayindex.getAST().newNumberLiteral(arrayindex.toString());
			if (mutateflag == false) { // if no mutation is to be performed then return the index
				return number;
			}
			// create infix expression with index +/- 1
			InfixExpression mutatedindex = null;
			mutatedindex = arrayindex.getAST().newInfixExpression();
			mutatedindex.setLeftOperand(number);
			if (mutType == mutationType.SUBTRACT) {
				mutatedindex.setOperator(Operator.MINUS);
				mutType = mutationType.ADD;
			} else {
				mutatedindex.setOperator(Operator.PLUS);
				mutType = mutationType.SUBTRACT;
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

			if (mutateflag == false) { // if no mutation is to be performed then return the index
				return pexp;
			}
			// create infix expression with index +/- 1
			InfixExpression mutatedindex = null;
			mutatedindex = arrayindex.getAST().newInfixExpression();
			mutatedindex.setLeftOperand(pexp);

			if (mutType == mutationType.SUBTRACT) {
				mutatedindex.setOperator(Operator.MINUS);
				mutType = mutationType.ADD;
			} else {
				mutatedindex.setOperator(Operator.PLUS);
				mutType = mutationType.SUBTRACT;
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

			if (mutateflag == false) { // if no mutation is to be performed then return the index
				return pexp;
			}
			// create infix expression with index +/- 1
			InfixExpression mutatedindex = null;
			mutatedindex = arrayindex.getAST().newInfixExpression();
			mutatedindex.setLeftOperand(pexp);
			if (mutType == mutationType.SUBTRACT) {
				mutatedindex.setOperator(Operator.MINUS);
				mutType = mutationType.ADD;
			} else {
				mutatedindex.setOperator(Operator.PLUS);
				mutType = mutationType.SUBTRACT;
			}
			mutatedindex.setRightOperand(arrayindex.getAST().newNumberLiteral("1"));
			// return mutated index
			return mutatedindex;
		} else if (arrayindex instanceof InfixExpression) {// if index is infix expression
			InfixExpression iexp = arrayindex.getAST().newInfixExpression();
			Expression loperand = ((InfixExpression) arrayindex).getLeftOperand();
			if (loperand != null) {
				iexp.setLeftOperand(mutateIndex(((InfixExpression) arrayindex).getLeftOperand(), false));
			}

			Operator ioperator = ((InfixExpression) arrayindex).getOperator();
			iexp.setOperator(ioperator);

			Expression roperand = ((InfixExpression) arrayindex).getRightOperand();
			if (roperand != null) {
				iexp.setRightOperand(mutateIndex(((InfixExpression) arrayindex).getRightOperand(), false));
			}
			// create infix expression with index +/- 1
			InfixExpression mutatedindex = null;
			mutatedindex = arrayindex.getAST().newInfixExpression();
			mutatedindex.setLeftOperand(iexp);
			if (mutType == mutationType.SUBTRACT) {
				mutatedindex.setOperator(Operator.MINUS);
				mutType = mutationType.ADD;
			} else {
				mutatedindex.setOperator(Operator.PLUS);
				mutType = mutationType.SUBTRACT;
			}
			mutatedindex.setRightOperand(arrayindex.getAST().newNumberLiteral("1"));
			// return mutated index
			return mutatedindex;
		}
		return arrayindex;
	}
}
