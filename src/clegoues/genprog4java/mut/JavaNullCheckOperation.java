package clegoues.genprog4java.mut;

import java.util.ArrayList;
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

	private Expression createExpression(ASTNode expressionToCheckIfNull) {
		MethodInvocation mi = expressionToCheckIfNull.getAST().newMethodInvocation();		
		Expression beforeTheDot = ((MethodInvocation) expressionToCheckIfNull).getExpression();
		while(beforeTheDot instanceof MethodInvocation){	
			beforeTheDot = ((MethodInvocation) beforeTheDot).getExpression();
		}
		String expIdentifier = ((SimpleName)beforeTheDot).getIdentifier();
		return expressionToCheckIfNull.getAST().newSimpleName(expIdentifier);
	}


	@Override
	public void edit(final ASTRewrite rewriter, AST ast, CompilationUnit cu) {
		ASTNode locationNode = this.getLocation().getASTNode();
		 Map<ASTNode, List<ASTNode>> nodestmts = this.getLocation().getNullCheckables();
		Set<ASTNode> parentnodes = nodestmts.keySet();
 
	for(ASTNode parent: parentnodes){
		// create a newnode
		List<ASTNode> expressionsFromThisParent = nodestmts.get(parent);
		//Create if before the error
		IfStatement ifstmt = locationNode.getAST().newIfStatement();
		InfixExpression everythingInTheCondition = locationNode.getAST().newInfixExpression(); 

		// for each of the expressions that can be null
		for(ASTNode  expressionToCheckIfNull : expressionsFromThisParent){
			InfixExpression expression = expressionToCheckIfNull.getAST().newInfixExpression();
			if(expressionToCheckIfNull instanceof MethodInvocation)
				expression.setLeftOperand(createExpression(expressionToCheckIfNull));			
			if(expressionToCheckIfNull instanceof FieldAccess)
				expression.setLeftOperand(((FieldAccess) expressionToCheckIfNull).getExpression());
			if(expressionToCheckIfNull instanceof QualifiedName)
				expression.setLeftOperand(((QualifiedName) expressionToCheckIfNull).getName());

			expression.setOperator(Operator.NOT_EQUALS);
			expression.setRightOperand(expressionToCheckIfNull.getAST().newNullLiteral());
				everythingInTheCondition = expression;
		}
		ifstmt.setExpression(everythingInTheCondition);
				
		ASTNode thenStmt = (Statement) parent;
		thenStmt = ASTNode.copySubtree(parent.getAST(), thenStmt);
		ifstmt.setThenStatement((Statement) thenStmt);
		rewriter.replace(parent, ifstmt, null);
	}	
	}
}
