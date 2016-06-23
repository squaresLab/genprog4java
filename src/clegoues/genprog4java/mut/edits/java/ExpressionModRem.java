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

public class ExpressionModRem extends JavaEditOperation {

	public ExpressionModRem(JavaLocation location,  HashMap<String, EditHole> sources) {
		super(Mutation.EXPREM, location, sources);
		this.holeNames.add("condExpRem");

	}

	@Override
	public void edit(final ASTRewrite rewriter) {
		ExpChoiceHole thisHole = (ExpChoiceHole) this.getHoleCode("condExpRem");
		Expression oldExp = (Expression) thisHole.getCode();
		int whichSide = thisHole.getChoice();
		while(oldExp instanceof ParenthesizedExpression) {
			oldExp = ((ParenthesizedExpression) oldExp).getExpression();	
		}
		InfixExpression realOldExp = (InfixExpression) oldExp;
		Expression newCondition;
		switch(whichSide) {
		case 0: newCondition = (Expression) rewriter.createCopyTarget(realOldExp.getLeftOperand());
		break;
		case 1:
		default:
			newCondition = (Expression) rewriter.createCopyTarget(realOldExp.getRightOperand());
			break;
		}
		rewriter.replace(oldExp, newCondition, null);
	}
	
	@Override
	public String toString() {
		// FIXME: this is lazy
		return "erm(" + this.getLocation().getId() + ")";
	}
	
}

