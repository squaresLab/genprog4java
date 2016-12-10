package clegoues.genprog4java.mut.edits.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.java.ASTUtils;
import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.holes.java.SubExpsHole;

public class RangeCheckOperation extends JavaEditOperation {


	public RangeCheckOperation(JavaLocation location, EditHole source) {
		super(location, source);
	}

	/*[Range Checker]
			B = buggy statements
			collect array accesses of B into collection C
			insert a if statement before B

			loop for all index variables in C
			{
			 insert a conditional expression that checks whether an index variable is within upper and lower bound
			}
			concatenate conditions using AND

			if B include return statement
			{
			 negate the concatenated the conditional expression
			 insert a return statement that returns a default value into THEN section of the if statement
			 insert B after the if statement
			} else {
			 insert B into THEN section of the if statement
			}*/

	@Override
	public void edit(final ASTRewrite rewriter) {
		SubExpsHole thisHole = (SubExpsHole) this.getHoleCode();
		ASTNode parent = thisHole.getHoleParent();
		List<ASTNode> arrays = thisHole.getSubExps();
		ASTNode newNode = null;

		// new if statement that will contain the range check
		// expressions concatenated using AND
		IfStatement newIfStatement = rewriter.getAST().newIfStatement();
		
		InfixExpression allAccessesCheck = null; 
		for (ASTNode array : arrays) {

			Expression index = ((ArrayAccess) array).getIndex();
			
			InfixExpression thisAccessCheck = rewriter.getAST().newInfixExpression();
			thisAccessCheck.setOperator(Operator.CONDITIONAL_AND);

			InfixExpression upperBoundCheck = rewriter.getAST().newInfixExpression();
			upperBoundCheck.setLeftOperand((Expression) rewriter.createCopyTarget(index));
			upperBoundCheck.setOperator(Operator.LESS);

			SimpleName uqualifier = rewriter.getAST().newSimpleName(
					((ArrayAccess) array).getArray().toString());
			SimpleName uname = rewriter.getAST().newSimpleName(
					"length");
			upperBoundCheck.setRightOperand(rewriter.getAST()
					.newQualifiedName(uqualifier, uname));			

			InfixExpression lowerBoundCheck = rewriter.getAST().newInfixExpression();
			lowerBoundCheck.setLeftOperand((Expression) rewriter.createCopyTarget(index));
			lowerBoundCheck.setOperator(Operator.GREATER_EQUALS);
			lowerBoundCheck.setRightOperand(rewriter.getAST().newNumberLiteral("0"));

			thisAccessCheck.setLeftOperand(lowerBoundCheck);
			thisAccessCheck.setRightOperand(upperBoundCheck);

			if(allAccessesCheck == null) {
				allAccessesCheck = thisAccessCheck;
			} else {
				Expression tempExpression = allAccessesCheck;
				allAccessesCheck = rewriter.getAST().newInfixExpression();
				allAccessesCheck.setOperator(Operator.CONDITIONAL_AND);
				allAccessesCheck.setLeftOperand(thisAccessCheck);
				allAccessesCheck.setRightOperand(tempExpression);
			}
		}

		
		if(parent instanceof ReturnStatement) {
			newNode = rewriter.getAST().newBlock(); 
			// create a prefix expression = NOT(finalandconditions)
			
			PrefixExpression negationExp = rewriter.getAST()
					.newPrefixExpression();

			ParenthesizedExpression parexp = null;
			parexp = parent.getAST().newParenthesizedExpression();
			parexp.setExpression(allAccessesCheck);

			negationExp.setOperand(parexp);
			negationExp
			.setOperator(org.eclipse.jdt.core.dom.PrefixExpression.Operator.NOT);
			// set the ifstatement expression
			newIfStatement.setExpression(negationExp);

			ReturnStatement rstmt = rewriter.getAST().newReturnStatement();
			
			ASTNode returnValue = ASTUtils.getDefaultReturn(parent, rewriter.getAST());
			if(returnValue != null) 
				rstmt.setExpression((Expression) returnValue);
			newIfStatement.setThenStatement(rstmt);

			// add the if statement followed by remaining content of
			// the parent node to new node
			
			((Block) newNode).statements().add(newIfStatement);
			ASTNode stmt = parent;
			stmt = ASTNode.copySubtree(rewriter.getAST(),  parent);
			((Block) newNode).statements().add(stmt);
			
		} else if(parent instanceof ForStatement) { 
			newNode = parent;
			// get the expressions of for statement
			Expression forexp = ((ForStatement) parent).getExpression();
			forexp = (Expression) ((ForStatement) parent)
					.getExpression().copySubtree(
							((ForStatement) parent).getExpression()
							.getAST(), (ASTNode) forexp);

			// create infix expression to AND the range check
			// expressions and for statement expressions
			InfixExpression forexpression = null;
			forexpression = rewriter.getAST().newInfixExpression();
			forexpression.setOperator(Operator.CONDITIONAL_AND);
			forexpression.setLeftOperand(allAccessesCheck);
			forexpression.setRightOperand(forexp);

			// update the for statement expressions
			newNode = ASTNode.copySubtree(
					rewriter.getAST(), newNode);
			((ForStatement) newNode).setExpression(forexpression);
		} else {
			newNode = rewriter.getAST().newBlock(); 
			newIfStatement.setExpression(allAccessesCheck);
			Block thenBlock = rewriter.getAST().newBlock();
			thenBlock.statements().add((ASTNode) rewriter.createCopyTarget(parent));
			newIfStatement.setThenStatement(thenBlock);
			((Block) newNode).statements().add(newIfStatement);

		}

		rewriter.replace(parent, newNode, null);
	}

	@Override
	public String toString() {
		// FIXME: this is lazy
		return "RangeChecker(" + this.getLocation().getId() + ")";
	}

}