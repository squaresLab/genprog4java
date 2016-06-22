package clegoues.genprog4java.mut.edits.java;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.java.MethodInfo;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.ExpChoiceHole;
import clegoues.genprog4java.mut.holes.java.ExpHole;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.holes.java.MethodInfoHole;
import clegoues.genprog4java.mut.holes.java.ExpChoiceHole.Which;

public class ExpressionModAdd extends JavaEditOperation {
	
	public ExpressionModAdd(JavaLocation location,  HashMap<String, EditHole> sources) {
		super(Mutation.EXPADD, location, sources);
		this.holeNames.add("condExpAdd");

	}

	@Override
	public void edit(final ASTRewrite rewriter) {
//		JavaStatement locationStmt = (JavaStatement) (this.getLocation().getLocation());
//		// possible FIXME: perhaps I'm not using the locationStmt properly?  Or maybe I am, hm.
//		ASTNode locationNode = locationStmt.getASTNode();
//		ExpChoiceHole thisHole = (ExpChoiceHole) this.getHoleCode("condExpAdd");
//		ASTNode parent = thisHole.getHoleParent();
//		if(parent instanceof IfStatement) {
//			IfStatement parentIf = (IfStatement) parent;
//			IfStatement newIfStmt = parentIf.getAST().newIfStatement();
//			Expression oldExp = (Expression) thisHole.getCode();
//			Which whichSide = thisHole.getWhich();
//			while(oldExp instanceof ParenthesizedExpression) {
//				oldExp = ((ParenthesizedExpression) oldExp).getExpression();	
//			}
//			InfixExpression realOldExp = (InfixExpression) oldExp;
//			InfixExpression newCondition = newIfStmt.getAST().newInfixExpression();
//			newCondition.setLeftOperand( (Expression) rewriter.createCopyTarget(realOldExp));
//			Operator op;
//			switch(whichSide) {
//			case LEFT: op = Operator.CONDITIONAL_AND;
//			break;
//			case RIGHT:
//			default:
//				op = Operator.CONDITIONAL_OR;
//				break;
//			}
//			newCondition.setOperator(op);
//			newIfStmt.setExpression(newCondition);
//			Statement thenStatement = (Statement) rewriter.createCopyTarget(parentIf.getThenStatement());
//			newIfStmt.setThenStatement(thenStatement);
//			if(parentIf.getElseStatement() != null) {
//				Statement elseStatement = (Statement) rewriter.createCopyTarget(parentIf.getElseStatement());
//				newIfStmt.setElseStatement(elseStatement);
//			}
//			rewriter.replace(parentIf, newIfStmt, null);
//		} else { // instance of conditional expression; FIXME: test this
//			ConditionalExpression parentExp = (ConditionalExpression) parent;
//			ConditionalExpression newCondExp = parentExp.getAST().newConditionalExpression();
//			Expression oldExp = (Expression) thisHole.getCode();
//			Which whichSide = thisHole.getWhich();
//			while(oldExp instanceof ParenthesizedExpression) {
//				oldExp = ((ParenthesizedExpression) oldExp).getExpression();	
//			}
//			InfixExpression realOldExp = (InfixExpression) oldExp;
//			Expression newCondition;
//			switch(whichSide) {
//			case LEFT: newCondition = (Expression) rewriter.createCopyTarget(realOldExp.getLeftOperand());
//			break;
//			case RIGHT:
//			default:
//				newCondition = (Expression) rewriter.createCopyTarget(realOldExp.getRightOperand());
//				break;
//			}
//			newCondExp.setExpression(newCondition);
//			Expression thenExp = (Expression) rewriter.createCopyTarget(parentExp.getThenExpression());
//			newCondExp.setThenExpression(thenExp);
//			if(parentExp.getElseExpression() != null) {
//				Expression elseExp = (Expression) rewriter.createCopyTarget(parentExp.getElseExpression());
//				newCondExp.setElseExpression(elseExp);
//			}
//			rewriter.replace(parentExp, newCondExp, null);
//		}
//	}
//		ASTNode toReplace = thisHole.getCode();
		
	}
}
