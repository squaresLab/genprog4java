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
	
	// FIXME: I really need a better mechanism for printing edit templates than the statement ID approach

	@Override
	public void edit(final ASTRewrite rewriter) {
		ExpChoiceHole thisHole = (ExpChoiceHole) this.getHoleCode("condExpAdd");
		Expression parentExp = (Expression) thisHole.getHoleParent();
		Expression newExpCode = (Expression) thisHole.getCode();

		Which whichSide = thisHole.getWhich();
		while(parentExp instanceof ParenthesizedExpression) {
			parentExp = ((ParenthesizedExpression) parentExp).getExpression();	
		}
		InfixExpression.Operator newOperator;
		switch(whichSide) {
		case LEFT: newOperator = InfixExpression.Operator.CONDITIONAL_AND;
		break;
		case RIGHT:
		default:
			newOperator = InfixExpression.Operator.CONDITIONAL_OR;
			break;
		}
		InfixExpression newExpression = parentExp.getAST().newInfixExpression();
		newExpression.setOperator(newOperator);
		newExpression.setLeftOperand((Expression) rewriter.createCopyTarget(parentExp));
		newExpression.setRightOperand((Expression) rewriter.createCopyTarget(newExpCode));

		rewriter.replace(parentExp, newExpression, null);

	}
}
