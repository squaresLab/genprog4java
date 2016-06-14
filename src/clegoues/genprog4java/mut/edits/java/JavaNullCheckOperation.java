package clegoues.genprog4java.mut.edits.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
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
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.holes.java.SubExpsHole;

public class JavaNullCheckOperation extends JavaEditOperation {

	public JavaNullCheckOperation(JavaLocation location,  HashMap<String, EditHole> sources) {
		super(Mutation.NULLCHECK, location, sources);
		this.holeNames.add("checkForNull");
	}

	@Override
	public void edit(final ASTRewrite rewriter) {
		ASTNode locationNode = ((JavaStatement) (this.getLocation().getLocation())).getASTNode();
		SubExpsHole thisHole = (SubExpsHole) this.getHoleCode("checkForNull");
		ASTNode parent = thisHole.getHoleParent();
		List<ASTNode> expressionsFromThisParent = thisHole.getSubExps();

		Collections.reverse(expressionsFromThisParent);
		//Create if before the error
		IfStatement ifstmt = locationNode.getAST().newIfStatement();
		InfixExpression everythingInTheCondition = null; 

		// for each of the expressions that can be null
		for(ASTNode expressionToCheckIfNull : expressionsFromThisParent){
			InfixExpression expression = ifstmt.getAST().newInfixExpression();
			if(expressionToCheckIfNull instanceof MethodInvocation) {
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
				expression.setLeftOperand(((QualifiedName) expressionToCheckIfNull).getName());

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
			// CLG says: this is not tested!  FIXME: test before deploy.
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
}
