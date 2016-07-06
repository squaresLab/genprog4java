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
	@Override
	public void edit(final ASTRewrite rewriter) {
		SubExpsHole thisHole = (SubExpsHole) this.getHoleCode();
		ASTNode parent = thisHole.getHoleParent();
		List<ASTNode> arrays = thisHole.getSubExps();

		Block newnode = rewriter.getAST().newBlock();


		// for each of the array access instances
		for( ASTNode  array : arrays){
			Expression index = ((ArrayAccess) array).getIndex();
			String arrayindex;
			if (!(index instanceof NumberLiteral)){
				// get the array index
				arrayindex = index.toString();
				arrayindex = arrayindex.replace("++", "");
				arrayindex = arrayindex.replace("--", "");

				// create if statement 
				IfStatement stmt = rewriter.getAST().newIfStatement();

				// with expression "index > arrayname.length" 
				InfixExpression expression = null;
				expression = rewriter.getAST().newInfixExpression();
				expression.setLeftOperand(rewriter.getAST().newSimpleName(arrayindex));
				expression.setOperator(Operator.GREATER_EQUALS);

				// and then part as "index = arrayname.length - 1"
				SimpleName qualifier = rewriter.getAST().newSimpleName(((ArrayAccess)array).getArray().toString());
				SimpleName name = rewriter.getAST().newSimpleName("length");
				expression.setRightOperand(rewriter.getAST().newQualifiedName(qualifier, name));
				stmt.setExpression(expression);

				Assignment thenexpression = null;
				thenexpression = rewriter.getAST().newAssignment();
				thenexpression.setLeftHandSide(rewriter.getAST().newSimpleName(arrayindex));
				thenexpression.setOperator(Assignment.Operator.ASSIGN);

				InfixExpression setupperboundexpression = null;
				setupperboundexpression = rewriter.getAST().newInfixExpression();
				SimpleName qualifier1 = rewriter.getAST().newSimpleName(((ArrayAccess)array).getArray().toString());
				SimpleName name1 = rewriter.getAST().newSimpleName("length");
				setupperboundexpression.setLeftOperand(rewriter.getAST().newQualifiedName(qualifier1, name1));
				setupperboundexpression.setOperator(Operator.MINUS);
				setupperboundexpression.setRightOperand(rewriter.getAST().newNumberLiteral("1"));

				thenexpression.setRightHandSide(setupperboundexpression);

				ExpressionStatement thenstmt = rewriter.getAST().newExpressionStatement(thenexpression);
				stmt.setThenStatement(thenstmt);

				// add if statement to newnode
				newnode.statements().add(stmt);
			}
			// append the existing content of parent node to newnode
			ASTNode stmt = (Statement)parent;
			stmt = ASTNode.copySubtree(rewriter.getAST(), stmt);
			newnode.statements().add(stmt);
			rewriter.replace(parent, newnode, null);
		}
	}	
	
	@Override
	public String toString() {
		// FIXME: this is lazy
		return "ubs(" + this.getLocation().getId() + ")";
	}
	
}
