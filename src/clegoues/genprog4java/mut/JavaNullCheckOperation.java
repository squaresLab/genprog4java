package clegoues.genprog4java.mut;

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
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.main.ClassInfo;

public class JavaNullCheckOperation extends JavaEditOperation {

	public JavaNullCheckOperation(ClassInfo fileName, JavaStatement location) {
		super(Mutation.NULLCHECK, fileName, location);
	}



	@Override
	public void edit(final ASTRewrite rewriter, AST ast, CompilationUnit cu) {
		ASTNode locationNode = this.getLocation().getASTNode();
		 Map<ASTNode, List<ASTNode>> nodestmts = this.getLocation().getNullCheckables();
		Set<ASTNode> parentnodes = nodestmts.keySet();
 
	for(ASTNode parent: parentnodes){
		// create a newnode
		List<ASTNode> expressionsFromThisParent = nodestmts.get(parent);
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
		ifstmt.setExpression(everythingInTheCondition);
				
		ASTNode thenStmt = (Statement) parent;
		thenStmt = ASTNode.copySubtree(parent.getAST(), thenStmt);
		ifstmt.setThenStatement((Statement) thenStmt);
		rewriter.replace(parent, ifstmt, null);
	}	
	}
}
