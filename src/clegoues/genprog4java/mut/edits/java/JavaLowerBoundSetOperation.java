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
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.holes.java.SimpleJavaHole;
import clegoues.genprog4java.mut.holes.java.SubExpsHole;

public class JavaLowerBoundSetOperation extends JavaEditOperation {

	public JavaLowerBoundSetOperation(JavaLocation location,  HashMap<String, EditHole> sources) {
		super(Mutation.LBOUNDSET, location, sources);
		this.holeNames.add("lowerBoundCheck");

	}
	@Override
	public void edit(final ASTRewrite rewriter) {
		ASTNode locationNode = ((JavaStatement) this.getLocation()).getASTNode(); // not used, but being completist
		SubExpsHole thisHole = (SubExpsHole) this.getHoleCode("lowerBoundCheck");
		ASTNode parent = thisHole.getHoleParent();
		List<ASTNode> arrays = thisHole.getSubExps();
		// possible FIXME: all array accesses in this location, or just the one? 
		// check against the spec

		Block newnode = parent.getAST().newBlock();

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
				IfStatement stmt = parent.getAST().newIfStatement();

				// with expression "index < 0" 
				InfixExpression expression = null;
				expression = parent.getAST().newInfixExpression();
				expression.setLeftOperand(parent.getAST().newSimpleName(arrayindex));
				expression.setOperator(Operator.LESS);
				expression.setRightOperand(parent.getAST().newNumberLiteral("0"));
				stmt.setExpression(expression);

				// and then part as "index = 0"
				Assignment thenexpression = null;
				thenexpression = parent.getAST().newAssignment();
				thenexpression.setLeftHandSide(parent.getAST().newSimpleName(arrayindex));
				thenexpression.setOperator(Assignment.Operator.ASSIGN);
				thenexpression.setRightHandSide(parent.getAST().newNumberLiteral("0"));
				ExpressionStatement thenstmt = parent.getAST().newExpressionStatement(thenexpression);
				stmt.setThenStatement(thenstmt);

				// add if statement to newnode
				newnode.statements().add(stmt);
			}
			// append the existing content of parent node to newnode
			ASTNode stmt = (Statement)parent;
			stmt = ASTNode.copySubtree(parent.getAST(), stmt);
			newnode.statements().add(stmt);
			rewriter.replace(parent, newnode, null);
		}
	}
}
