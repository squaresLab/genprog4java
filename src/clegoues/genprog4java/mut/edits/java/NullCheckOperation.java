package clegoues.genprog4java.mut.edits.java;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
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

	public NullCheckOperation(JavaLocation location, EditHole source) {
		super(location, source);
	}

	@Override
	public void edit(final ASTRewrite rewriter) {
		ASTNode locationNode =  ((JavaLocation) this.getLocation()).getCodeElement();
		SubExpsHole thisHole = (SubExpsHole) this.getHoleCode();
		ASTNode parent = thisHole.getHoleParent();
		List<ASTNode> expressionsFromThisParent = thisHole.getSubExps();

		Collections.reverse(expressionsFromThisParent);
		//Create if before the error
		IfStatement ifstmt = rewriter.getAST().newIfStatement();
		InfixExpression everythingInTheCondition = null; 

		// for each of the expressions that can be null
		for(ASTNode expressionToCheckIfNull : expressionsFromThisParent){
			InfixExpression expression = ifstmt.getAST().newInfixExpression();
			if(expressionToCheckIfNull instanceof MethodInvocation) {
				ASTNode exp = ((MethodInvocation) expressionToCheckIfNull).getExpression();
				Expression newExpression = (Expression) rewriter.createCopyTarget(((MethodInvocation) expressionToCheckIfNull).getExpression());
				expression.setLeftOperand(newExpression);
			}
			if(expressionToCheckIfNull instanceof SimpleName) {
				String name = ((SimpleName) expressionToCheckIfNull).getIdentifier();
				Expression newSimpleName = ifstmt.getAST().newSimpleName(name);
				expression.setLeftOperand(newSimpleName);
			}
			if(expressionToCheckIfNull instanceof FieldAccess) {
				Expression newExpression = (Expression) rewriter.createCopyTarget(((FieldAccess) expressionToCheckIfNull).getExpression());

				expression.setLeftOperand(newExpression);
			}
			if(expressionToCheckIfNull instanceof QualifiedName)
				expression.setLeftOperand((Expression) rewriter.createCopyTarget(((QualifiedName) expressionToCheckIfNull).getName()));

			expression.setOperator(Operator.NOT_EQUALS);
			expression.setRightOperand(expressionToCheckIfNull.getAST().newNullLiteral());
			if(everythingInTheCondition == null)
				everythingInTheCondition = expression;
			else {
				InfixExpression newInfix = ifstmt.getAST().newInfixExpression();
				newInfix.setOperator(Operator.CONDITIONAL_AND);
				newInfix.setLeftOperand(everythingInTheCondition);
				newInfix.setRightOperand(expression);
				everythingInTheCondition = newInfix;
			}
		}
		if(parent instanceof ReturnStatement) {
			PrefixExpression prefix = ifstmt.getAST().newPrefixExpression();
			prefix.setOperator(PrefixExpression.Operator.NOT);
			ParenthesizedExpression parenthesized = rewriter.getAST().newParenthesizedExpression();
			parenthesized.setExpression(everythingInTheCondition);
			prefix.setOperand(parenthesized);
			ifstmt.setExpression(prefix);
			ASTNode elseStmt = (Statement) parent;
			elseStmt = ASTNode.copySubtree(parent.getAST(), elseStmt); 
			ifstmt.setElseStatement((Statement) elseStmt); 
			ReturnStatement newReturn = ifstmt.getAST().newReturnStatement();
			// return a default value.
			ASTNode newValue = ASTUtils.getDefaultReturn(locationNode, rewriter.getAST());
			if(newValue != null) 
				newReturn.setExpression((Expression) newValue);
			ifstmt.setThenStatement((Statement) newReturn);
		} else {
			ifstmt.setExpression(everythingInTheCondition);
			ASTNode thenStmt = (Statement) parent;
			thenStmt = ASTNode.copySubtree(rewriter.getAST(), thenStmt);
			ifstmt.setThenStatement((Statement) thenStmt);
		}
		rewriter.replace(parent, ifstmt, null);

	}

	@Override
	public String toString() {
		// FIXME: this is lazy
		return "nc(" + this.getLocation().getId() + ")";
	}

}
