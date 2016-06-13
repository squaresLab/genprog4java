package clegoues.genprog4java.mut.edits.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.mut.holes.java.JavaLocation;

public class JavaRangeCheckOperation extends JavaEditOperation {

	public JavaRangeCheckOperation(JavaLocation location,  HashMap<String, EditHole> sources) {
		super(Mutation.RANGECHECK, location, sources);
	}

	@Override
	public void edit(final ASTRewrite rewriter) {
		ASTNode locationNode = ((JavaStatement) this.getLocation()).getASTNode();
		// FIXME: should lowerbound be called lowerbound in the range check operator?
		Block newNode = locationNode.getAST().newBlock(); 

		final Map<ASTNode, List<ASTNode>> nodestmts =  ((JavaStatement) this.getLocation()).getArrayAccesses(); 
		Set<ASTNode> parentnodes = nodestmts.keySet();

	parentnodes = nodestmts.keySet();
	// for each parent node which may have multiple array access
	// instances
	for (ASTNode parent : parentnodes) {
		// create a new node
		newNode = parent.getAST().newBlock();

		// if the parent is for statement then create a new for
		// statement. this is special case
		ForStatement newForStmt = null;

		List<ASTNode> arrays = nodestmts.get(parent); // get all the arrays of the parent
		boolean returnflag = false; // to check is parent node has a return statement
		int counter = 0; // to keep track of the #range check conditions
		boolean isforstmt = false; // to check if parent is of type ForStatement

		if (parent.toString().contains("return")) {
			returnflag = true;
		}

		if (parent instanceof ForStatement) {
			isforstmt = true;
			newForStmt = (ForStatement) parent;
		}

		// new if statement that will contain the range check
		// expressions concatenated using AND
		IfStatement rangechkstmt = parent.getAST().newIfStatement();

		InfixExpression finalandexpression = null;
		finalandexpression = parent.getAST().newInfixExpression();
		finalandexpression.setOperator(Operator.CONDITIONAL_AND);

		// for each of the array access instances
		for (ASTNode array : arrays) {
			// get the array index
			Expression index = ((ArrayAccess) array).getIndex();
			String arrayindex;
			if (!(index instanceof NumberLiteral)) {
				arrayindex = index.toString();
				arrayindex = arrayindex.replace("++", "");
				arrayindex = arrayindex.replace("--", "");

				// create infix expression to check lowerbound and
				// upperbound of array index
				InfixExpression andexpression = null;
				andexpression = parent.getAST().newInfixExpression();
				andexpression.setOperator(Operator.CONDITIONAL_AND);

				// create infix expression to check lowerbound
				InfixExpression checklboundexpression = null;
				checklboundexpression = parent.getAST()
						.newInfixExpression();
				checklboundexpression.setLeftOperand(parent.getAST()
						.newSimpleName(arrayindex));
				checklboundexpression
				.setOperator(Operator.GREATER_EQUALS);
				checklboundexpression.setRightOperand(parent.getAST()
						.newNumberLiteral("0"));

				// create infix expression to check upper bound
				InfixExpression checkuboundexpression = null;
				checkuboundexpression = parent.getAST()
						.newInfixExpression();
				checkuboundexpression.setLeftOperand(parent.getAST()
						.newSimpleName(arrayindex));
				checkuboundexpression.setOperator(Operator.LESS);

				SimpleName uqualifier = parent.getAST().newSimpleName(
						((ArrayAccess) array).getArray().toString());
				SimpleName uname = parent.getAST().newSimpleName(
						"length");
				checkuboundexpression.setRightOperand(parent.getAST()
						.newQualifiedName(uqualifier, uname));

				andexpression.setLeftOperand(checklboundexpression);
				andexpression.setRightOperand(checkuboundexpression);

				if (counter == 0) { // only one array access is there in
					// parent node
					finalandexpression = andexpression;
					counter++;
				} else { // if more than one array access are there then
					// keep creating and concatenating
					// expressions
					// into "finalandexpression"
					InfixExpression tmpandexpression = null;
					tmpandexpression = parent.getAST()
							.newInfixExpression();
					tmpandexpression
					.setOperator(Operator.CONDITIONAL_AND);
					tmpandexpression.setLeftOperand(finalandexpression);
					tmpandexpression.setRightOperand(andexpression);
					finalandexpression = tmpandexpression;
					counter++;
				}
			}
		}

		if (isforstmt == false) { // if the parent node is NOT
			// ForStatement
			if (returnflag == false) { // if parent node DOES NOT
				// contain return statement
				rangechkstmt.setExpression(finalandexpression);
				ASTNode stmt = (Statement) parent;
				stmt = ASTNode.copySubtree(parent.getAST(), stmt);
				rangechkstmt.setThenStatement((Statement) stmt);
				newNode.statements().add(rangechkstmt);
			} else { // if parent node contains return statement

				// create a prefix expression = NOT(finalandconditions)
				PrefixExpression notfinalandexpression = null;
				notfinalandexpression = parent.getAST()
						.newPrefixExpression();

				ParenthesizedExpression parexp = null;
				parexp = parent.getAST().newParenthesizedExpression();
				parexp.setExpression(finalandexpression);

				notfinalandexpression.setOperand(parexp);
				notfinalandexpression
				.setOperator(org.eclipse.jdt.core.dom.PrefixExpression.Operator.NOT);
				// set the ifstatement expression
				rangechkstmt.setExpression(notfinalandexpression);

				// set the then part as return default. We shall have to
				// declare RETURN_DEFAULT constant in the target
				// program.
				ReturnStatement rstmt = parent.getAST()
						.newReturnStatement();
				SimpleName defaultreturnvalue = parent.getAST()
						.newSimpleName("RETURN_DEFAULT");
				rstmt.setExpression(defaultreturnvalue);
				rangechkstmt.setThenStatement(rstmt);

				// add the if statement followed by remaining content of
				// the parent node to new node
				newNode.statements().add(rangechkstmt);
				ASTNode stmt = (Statement) parent;
				stmt = ASTNode.copySubtree(parent.getAST(), stmt);
				newNode.statements().add(stmt);
			}
		} else { // if the parent node is of type ForStatement.

			// get the expressions of for statement
			Expression forexp = ((ForStatement) parent).getExpression();
			forexp = (Expression) ((ForStatement) parent)
					.getExpression().copySubtree(
							((ForStatement) parent).getExpression()
							.getAST(), (ASTNode) forexp);

			// create infix expression to AND the range check
			// expressions and for statement expressions
			InfixExpression forexpression = null;
			forexpression = parent.getAST().newInfixExpression();
			forexpression.setOperator(Operator.CONDITIONAL_AND);
			forexpression.setLeftOperand(finalandexpression);
			forexpression.setRightOperand(forexp);

			// update the for statement expressions
			newForStmt = (ForStatement) ASTNode.copySubtree(
					parent.getAST(), newForStmt);
			newForStmt.setExpression(forexpression);
		}

		// replace parent node with new node (or new for statement)
		if (isforstmt == false) {
			rewriter.replace(parent, newNode, null);
		} else {
			rewriter.replace(parent, newForStmt, null);
		}
	}

	}
}