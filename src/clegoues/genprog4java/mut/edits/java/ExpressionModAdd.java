package clegoues.genprog4java.mut.edits.java;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.ExpChoiceHole;
import clegoues.genprog4java.mut.holes.java.JavaLocation;

public class ExpressionModAdd extends ExpressionReplacer {


	public ExpressionModAdd(JavaLocation location, EditHole source) {
		super(location, source);
	}
	
	@Override
	public void edit(final ASTRewrite rewriter) {
		ExpChoiceHole thisHole = (ExpChoiceHole) this.getHoleCode();
		Expression locationExp = (Expression) thisHole.getLocationExp();
		Expression newExpCode = (Expression) thisHole.getCode();

		int whichSide = thisHole.getChoice();
		InfixExpression.Operator newOperator;
		switch(whichSide) {
		case 0: newOperator = InfixExpression.Operator.CONDITIONAL_AND;
		break;
		case 1:
		default:
			newOperator = InfixExpression.Operator.CONDITIONAL_OR;
			break;
		}
		InfixExpression newExpression = rewriter.getAST().newInfixExpression();
		newExpression.setOperator(newOperator);
		newExpression.setLeftOperand((Expression) rewriter.createCopyTarget(locationExp));
		newExpression.setRightOperand((Expression) rewriter.createCopyTarget(newExpCode));
		this.replaceExp(rewriter, newExpression);
	}

	@Override
	public String toString() {
		ExpChoiceHole thisHole = (ExpChoiceHole) this.getHoleCode();
		Expression locationExp = (Expression) thisHole.getLocationExp();
		Expression newExpCode = (Expression) thisHole.getCode();


		String retval = "ea(" + this.getLocation().getId() + ": ";
		retval += "(" + locationExp.toString() + ")";
		if(thisHole.getChoice() == 0) 
			retval += " && ";
		else 
			retval += " || ";
		retval +=  "(" + newExpCode.toString() + "))";
		return retval;
	}
}
