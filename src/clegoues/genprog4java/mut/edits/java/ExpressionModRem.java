package clegoues.genprog4java.mut.edits.java;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.java.MethodInfo;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.ExpChoiceHole;
import clegoues.genprog4java.mut.holes.java.ExpChoiceHole.Which;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.holes.java.MethodInfoHole;

public class ExpressionModRem extends JavaEditOperation {

	public ExpressionModRem(JavaLocation location,  HashMap<String, EditHole> sources) {
		super(Mutation.EXPREM, location, sources);
		this.holeNames.add("condExpRem");

	}

	@Override
	public void edit(final ASTRewrite rewriter) {
		// possibly I can just rewrite the expression, no?
		JavaStatement locationStmt = (JavaStatement) (this.getLocation().getLocation());
		// possible FIXME: perhaps I'm not using the locationStmt properly?  Or maybe I am, hm.
		ASTNode locationNode = locationStmt.getASTNode();
		ExpChoiceHole thisHole = (ExpChoiceHole) this.getHoleCode("condExpRem");
		Expression oldExp = (Expression) thisHole.getCode();
		Which whichSide = thisHole.getWhich();
		while(oldExp instanceof ParenthesizedExpression) {
			oldExp = ((ParenthesizedExpression) oldExp).getExpression();	
		}
		InfixExpression realOldExp = (InfixExpression) oldExp;
		Expression newCondition;
		switch(whichSide) {
		case LEFT: newCondition = (Expression) rewriter.createCopyTarget(realOldExp.getLeftOperand());
		break;
		case RIGHT:
		default:
			newCondition = (Expression) rewriter.createCopyTarget(realOldExp.getRightOperand());
			break;
		}
		rewriter.replace(oldExp, newCondition, null);
	}
}

