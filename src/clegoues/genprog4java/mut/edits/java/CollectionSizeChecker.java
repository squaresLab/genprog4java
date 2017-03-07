package clegoues.genprog4java.mut.edits.java;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.java.ASTUtils;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.holes.java.SubExpsHole;

public class CollectionSizeChecker extends JavaEditOperation {

	public CollectionSizeChecker(JavaLocation location, EditHole source) {
		super(location, source);
	}
	
	/* [Collection Size Checker]
B = buggy statements
collect method invocations of (@\textbf{[collection objects]}@) in B and put them into collection C
insert a if statement before B

loop for all method invocation in C
{
 if a method invocation has an index parameter
 {
 insert a conditional expression that checks whether the index parameter is smaller than the size of its collection object
 }
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
	@Override
	public void edit(ASTRewrite rewriter) {
		SubExpsHole thisHole = (SubExpsHole) this.getHoleCode();
		ASTNode parent = thisHole.getHoleParent();
		List<ASTNode> expressionsFromThisParent = thisHole.getSubExps();
		/*	void	add(int index, E element)
		boolean	addAll(int index, Collection<? extends E> c)
		abstract E	get(int index)
		E	remove(int index)
		protected void	removeRange(int fromIndex, int toIndex)
		E	set(int index, E element)
		List<E>	subList(int fromIndex, int toIndex) */

		IfStatement ifstmt = rewriter.getAST().newIfStatement();
		Expression everythingInTheCondition = null; 

		Collections.reverse(expressionsFromThisParent);
		for(ASTNode node : expressionsFromThisParent) {
			MethodInvocation mi = (MethodInvocation) node;
			Expression methodCall = (Expression) rewriter.createCopyTarget(mi.getExpression());
			SimpleName methodName = mi.getName();
			int numIndices = 1;
			switch(methodName.getIdentifier()) {
			case "removeRange":
			case "subList":
				numIndices = 2;
				break;
				default: break;
			}
			if(numIndices == 1) {
				Expression indexVar = (Expression) rewriter.createCopyTarget((Expression) mi.arguments().get(0));
				InfixExpression overallExp = rewriter.getAST().newInfixExpression();
				overallExp.setOperator(InfixExpression.Operator.CONDITIONAL_AND);
			
				InfixExpression lessThan = rewriter.getAST().newInfixExpression();
				lessThan.setOperator(InfixExpression.Operator.LESS);

				lessThan.setLeftOperand(indexVar);
				
				MethodInvocation sizeExp = rewriter.getAST().newMethodInvocation();
				
				sizeExp.setName(rewriter.getAST().newSimpleName("size"));
				sizeExp.setExpression(methodCall);
				lessThan.setRightOperand(sizeExp);
				
				
				InfixExpression greaterThan = rewriter.getAST().newInfixExpression();
				greaterThan.setOperator(InfixExpression.Operator.GREATER_EQUALS);
				greaterThan.setLeftOperand((Expression) rewriter.createCopyTarget((Expression) mi.arguments().get(0)));
				greaterThan.setRightOperand(rewriter.getAST().newNumberLiteral("0"));

				overallExp.setLeftOperand(lessThan);
				overallExp.setRightOperand(greaterThan);
				
				if(everythingInTheCondition == null)
					everythingInTheCondition = overallExp;
				else {
					InfixExpression newInfix = rewriter.getAST().newInfixExpression();
					newInfix.setOperator(Operator.CONDITIONAL_AND);
					newInfix.setLeftOperand(everythingInTheCondition);
					newInfix.setRightOperand(overallExp);
				}
				
			}
		}
		
		if(parent instanceof ReturnStatement) {
			PrefixExpression prefix = ifstmt.getAST().newPrefixExpression();
			ParenthesizedExpression parenthesized = rewriter.getAST().newParenthesizedExpression();
			parenthesized.setExpression(everythingInTheCondition);
			prefix.setOperator(PrefixExpression.Operator.NOT);
			prefix.setOperand(parenthesized);
			ifstmt.setExpression(prefix);
			ASTNode elseStmt = (Statement) parent;
			elseStmt = ASTNode.copySubtree(parent.getAST(), elseStmt); 
			ifstmt.setElseStatement((Statement) elseStmt); 
			ReturnStatement newReturn = ifstmt.getAST().newReturnStatement();
			// return a default value.
			
			ASTNode newValue = ASTUtils.getDefaultReturn(parent, rewriter.getAST());
			if(newValue != null) 
				newReturn.setExpression((Expression) newValue);
			ifstmt.setThenStatement((Statement) newReturn);
		} else {
			ifstmt.setExpression(everythingInTheCondition);
			Block thenBlock = parent.getAST().newBlock();
			thenBlock.statements().add(ASTNode.copySubtree(parent.getAST(), parent));
			ifstmt.setThenStatement(thenBlock);
		}
		rewriter.replace(parent, ifstmt, null);
		
	}
	
	@Override
	public String toString() {
		return "CollectionSizeChecker";
	}

}
