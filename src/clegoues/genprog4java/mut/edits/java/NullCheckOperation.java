package clegoues.genprog4java.mut.edits.java;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.java.ASTUtils;
import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.holes.java.SubExpsHole;

public class NullCheckOperation extends JavaEditOperation {

	// for future reference: I *think* that the technical Par description would
	// have us add a null check for every reference within a location, whereas we 
	// pick a sub-location parent and check everything within it.
	// this increases our fault space a bit because every location actually
	// consists of multiple internal locations
	// however, it decreases the number of variants that don't compile because
	// we don't try to null check dereferences that apply to
	// variables declared *within* a location.
	// I think the tradeoff is worth it, and besides, this is *much* easier to implement
	public NullCheckOperation(JavaLocation location, EditHole source) {
		super(location, source);
	}

	@Override
	public void edit(final ASTRewrite rewriter) {
		ASTNode locationNode =  ((JavaLocation) this.getLocation()).getCodeElement();
		AST myAST = rewriter.getAST();
		
		SubExpsHole thisHole = (SubExpsHole) this.getHoleCode();
		ASTNode parent = thisHole.getHoleParent();
		List<ASTNode> expressionsFromThisParent = thisHole.getSubExps();

		Collections.reverse(expressionsFromThisParent);

		// Create if before the error
		IfStatement ifstmt = myAST.newIfStatement();
		InfixExpression everythingInTheCondition = null; 

		// for each of the expressions that can be null
		for(ASTNode expressionToCheckIfNull : expressionsFromThisParent){
			InfixExpression expression = myAST.newInfixExpression();
			Expression newExpression = (Expression) rewriter.createCopyTarget(expressionToCheckIfNull);
			expression.setLeftOperand(newExpression);
			expression.setOperator(Operator.NOT_EQUALS);
			expression.setRightOperand(myAST.newNullLiteral());
			if(everythingInTheCondition == null) {
				everythingInTheCondition = expression;
			} else {
				InfixExpression newInfix = myAST.newInfixExpression();
				newInfix.setOperator(Operator.CONDITIONAL_AND);
				newInfix.setLeftOperand(everythingInTheCondition);
				newInfix.setRightOperand(expression);
				everythingInTheCondition = newInfix;
			}
		}
		if(parent instanceof ReturnStatement) {
			ParenthesizedExpression parenthesized = myAST.newParenthesizedExpression();
			parenthesized.setExpression(everythingInTheCondition);
			
			PrefixExpression prefix = ifstmt.getAST().newPrefixExpression();
			prefix.setOperator(PrefixExpression.Operator.NOT);
			prefix.setOperand(parenthesized);
			
			ifstmt.setExpression(prefix);
			
			ReturnStatement newReturn = myAST.newReturnStatement();
			// return a default value.
			ASTNode newValue = ASTUtils.getDefaultReturn(locationNode, myAST);
			if(newValue != null) 
				newReturn.setExpression((Expression) newValue);
			Block newThenStatement = myAST.newBlock();
			newThenStatement.statements().add(newReturn);
			ifstmt.setThenStatement(newThenStatement);
			
			Block elseStmt = myAST.newBlock();
			elseStmt.statements().add(rewriter.createCopyTarget(parent));
			ifstmt.setElseStatement(elseStmt); 
		
		} else {
			ifstmt.setExpression(everythingInTheCondition);
			Block newThenStatement = rewriter.getAST().newBlock();
			newThenStatement.statements().add(rewriter.createCopyTarget(parent));
			ifstmt.setThenStatement(newThenStatement);
		}
		
		rewriter.replace(parent, ifstmt, null);

	}

	@Override
	public String toString() {
		// FIXME: this is lazy
		return "nc(" + this.getLocation().getId() + ")";
	}

}
