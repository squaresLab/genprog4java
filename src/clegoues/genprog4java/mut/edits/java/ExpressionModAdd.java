package clegoues.genprog4java.mut.edits.java;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.ExpChoiceHole;
import clegoues.genprog4java.mut.holes.java.ExpChoiceHole.Which;
import clegoues.genprog4java.mut.holes.java.JavaLocation;

public class ExpressionModAdd extends JavaEditOperation {

	public ExpressionModAdd(JavaLocation location,  HashMap<String, EditHole> sources) {
		super(Mutation.EXPADD, location, sources);
		this.holeNames.add("condExpAdd");
	}


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

	@Override
	public String toString() {
		ExpChoiceHole thisHole = (ExpChoiceHole) this.getHoleCode("condExpAdd");
		Expression parentExp = (Expression) thisHole.getHoleParent();
		Expression newExpCode = (Expression) thisHole.getCode();


		String retval = "ea(" + this.getLocation().getId() + ": ";
		retval += "(" + parentExp.toString() + ")";
		if(thisHole.getWhich() == Which.LEFT) 
			retval += " && ";
		else 
			retval += " || ";
		retval +=  "(" + newExpCode.toString() + "))";
		return retval;
	}
}
