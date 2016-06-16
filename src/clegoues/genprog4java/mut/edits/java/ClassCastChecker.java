package clegoues.genprog4java.mut.edits.java;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.holes.java.SubExpsHole;

public class ClassCastChecker extends JavaEditOperation {
	
	public ClassCastChecker(JavaLocation location,  HashMap<String, EditHole> sources) {
		super(Mutation.CASTCHECK, location, sources);
		this.holeNames.add("classCast");
	}

	@Override
	public void edit(ASTRewrite rewriter) {
		ASTNode locationNode = ((JavaStatement) (this.getLocation().getLocation())).getASTNode();
		SubExpsHole thisHole = (SubExpsHole) this.getHoleCode("classCast");
		ASTNode parent = thisHole.getHoleParent();
		List<ASTNode> expressionsFromThisParent = thisHole.getSubExps();

		Collections.reverse(expressionsFromThisParent);
		//Create if before the error
		IfStatement ifstmt = locationNode.getAST().newIfStatement();
		Expression everythingInTheCondition = null; 

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
			ASTNode thenStmt = (Statement) parent;
			thenStmt = ASTNode.copySubtree(parent.getAST(), thenStmt);
			ifstmt.setThenStatement((Statement) thenStmt);
		}
		rewriter.replace(parent, ifstmt, null);
	}

/*	[Class Cast Checker]
			B = buggy statements
			collect class-casting operators of B into collection C
			insert a if statement before B

			loop for all castees in C
			{
			 insert a conditional expression that checks whether a castee implements its casting type
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
}
