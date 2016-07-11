package clegoues.genprog4java.mut.edits.java;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.holes.java.SubExpsHole;

public class ClassCastChecker extends JavaEditOperation {

	public ClassCastChecker(JavaLocation location, EditHole source) {
		super(location, source);
	}
	
	@Override
	public void edit(ASTRewrite rewriter) {
		SubExpsHole thisHole = (SubExpsHole) this.getHoleCode();
		ASTNode parent = thisHole.getHoleParent();
		List<ASTNode> expressionsFromThisParent = thisHole.getSubExps();

		Collections.reverse(expressionsFromThisParent);
		//Create if before the error
		IfStatement ifstmt = rewriter.getAST().newIfStatement();
		Expression everythingInTheCondition = null; 

		if(everythingInTheCondition instanceof Object) {
			Object casted = (Object) everythingInTheCondition;
		}
		// for each of the expressions that can be null
		for(ASTNode castToCheck : expressionsFromThisParent){
			CastExpression asCast = (CastExpression) castToCheck;
			InstanceofExpression expression = ifstmt.getAST().newInstanceofExpression();
			Expression newExpression = (Expression) rewriter.createCopyTarget(asCast.getExpression());
			expression.setLeftOperand(newExpression);
			Type newType = (Type) rewriter.createCopyTarget(asCast.getType());
			expression.setRightOperand(newType);
		
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
			// CLG says: this is not tested/was taken from null check template so is probably wrong!  FIXME: test before deploy.
			PrefixExpression prefix = ifstmt.getAST().newPrefixExpression();
			prefix.setOperator(PrefixExpression.Operator.NOT);
			prefix.setOperand(everythingInTheCondition);
			ifstmt.setExpression(prefix);
			ASTNode elseStmt = (Statement) parent;
			elseStmt = ASTNode.copySubtree(parent.getAST(), elseStmt); 
			ifstmt.setElseStatement((Statement) elseStmt); 
			ReturnStatement newReturn = ifstmt.getAST().newReturnStatement();
			// return a default value.
			newReturn.setExpression(ifstmt.getAST().newNullLiteral());
			ifstmt.setThenStatement((Statement) newReturn);
		} else {
			ifstmt.setExpression(everythingInTheCondition);
			Block thenBlock = parent.getAST().newBlock();
			thenBlock.statements().add(ASTNode.copySubtree(parent.getAST(), parent));
			ifstmt.setThenStatement(thenBlock);
		}
		rewriter.replace(parent, ifstmt, null);
	}
	
	@Override
	public String toString() {
		SubExpsHole thisHole = (SubExpsHole) this.getHoleCode();
		List<ASTNode> expressionsFromThisParent = thisHole.getSubExps();
		Collections.reverse(expressionsFromThisParent);
		String retval = "cc(@" + this.getLocation().getId() + ": [ ";
		String castString = null;
		for(ASTNode castToCheck : expressionsFromThisParent){
			CastExpression asCast = (CastExpression) castToCheck;
			if(castString != null) {
				retval += ", ";
			} 
			retval += asCast.toString();
		}
		return retval + " ])";
	}
}