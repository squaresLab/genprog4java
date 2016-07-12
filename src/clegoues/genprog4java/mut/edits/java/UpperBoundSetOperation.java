package clegoues.genprog4java.mut.edits.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.holes.java.SubExpsHole;

public class UpperBoundSetOperation extends JavaEditOperation {

	public UpperBoundSetOperation(JavaLocation location, EditHole source) {
		super(location, source);
	}

	/*
	[Upper Bound Setter]
			B = buggy statements
			collect array accesses of B into collection C

			loop for all index variables in C
			{
			 insert a if statement before statements having an index variable
			 insert a conditional expression that checks whether an index variable is larger than upper bound
			 insert an assignment statement that gives the upper bound value to the index variable into THEN section of the if statement
			}
			insert B after all if statements*/

	@Override
	public void edit(final ASTRewrite rewriter) {
		SubExpsHole thisHole = (SubExpsHole) this.getHoleCode();
		ASTNode parent = thisHole.getHoleParent();
		List<ASTNode> arrays = thisHole.getSubExps();

		Block newnode = rewriter.getAST().newBlock();


		// for each of the array access instances
		// we can assume all indices are simplenames
		
		for( ASTNode  array : arrays){
			IfStatement ifStmt = rewriter.getAST().newIfStatement();

			Expression index = ((ArrayAccess) array).getIndex();
			InfixExpression check = rewriter.getAST().newInfixExpression();
			check.setLeftOperand((Expression) rewriter.createCopyTarget(index));
			check.setOperator(Operator.GREATER_EQUALS);

			SimpleName qualifier = rewriter.getAST().newSimpleName(((ArrayAccess)array).getArray().toString());
			SimpleName name = rewriter.getAST().newSimpleName("length");
			check.setRightOperand(rewriter.getAST().newQualifiedName(qualifier, name));
			ifStmt.setExpression(check);

			// and then part as "index = arrayname.length - 1"

			Assignment thenexpression = rewriter.getAST().newAssignment();
			thenexpression.setLeftHandSide((Expression) rewriter.createCopyTarget(index));
			thenexpression.setOperator(Assignment.Operator.ASSIGN);

			InfixExpression setupperboundexpression = rewriter.getAST().newInfixExpression();
			SimpleName qualifier1 = rewriter.getAST().newSimpleName(((ArrayAccess)array).getArray().toString());
			SimpleName name1 = rewriter.getAST().newSimpleName("length");
			setupperboundexpression.setLeftOperand(rewriter.getAST().newQualifiedName(qualifier1, name1));
			setupperboundexpression.setOperator(Operator.MINUS);
			setupperboundexpression.setRightOperand(rewriter.getAST().newNumberLiteral("1"));

			thenexpression.setRightHandSide(setupperboundexpression);

			ExpressionStatement thenstmt = rewriter.getAST().newExpressionStatement(thenexpression);
			ifStmt.setThenStatement(thenstmt);

			// add if statement to newnode
			newnode.statements().add(ifStmt);
		}
		// append the existing content of parent node to newnode
		ASTNode stmt = (Statement)parent;
		stmt = ASTNode.copySubtree(rewriter.getAST(), stmt);
		newnode.statements().add(stmt);
		rewriter.replace(parent, newnode, null);
}	

@Override
public String toString() {
	// FIXME: this is lazy
	return "ubs(" + this.getLocation().getId() + ")";
}

}
