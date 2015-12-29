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

import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.JavaLocation;

public class JavaLowerBoundSetOperation extends JavaEditOperation {

	public JavaLowerBoundSetOperation(JavaLocation location) {
		super(Mutation.LBOUNDSET, location);
	}
	@Override
	public void edit(final ASTRewrite rewriter) {
		ASTNode locationNode = this.getLocationNode(); 
		final Map<ASTNode, List<ASTNode>> nodestmts = new HashMap<ASTNode, List<ASTNode>>();	// to track the parent nodes of array access nodes
		Set<ASTNode> parentnodes = null; 
		final Map<ASTNode, String> lowerbound = new HashMap<ASTNode, String>();			// to set the lower-bound values of array. currently set to arrayname.length

		locationNode.accept(new ASTVisitor() {

			// method to visit all ArrayAccess nodes in locationNode and store their parents
			public boolean visit(ArrayAccess node) {
				lowerbound.put(node, "0");
				ASTNode parent = getParent(node);
				if(!nodestmts.containsKey(parent)){
					List<ASTNode> arraynodes = new ArrayList<ASTNode>();
					arraynodes.add(node);
					nodestmts.put(parent, arraynodes);		
				}else{
					List<ASTNode> arraynodes = (List<ASTNode>) nodestmts.get(parent);
					if(!arraynodes.contains(node))
						arraynodes.add(node);
					nodestmts.put(parent, arraynodes);	
				}
				return true;
			}

			// method to get the parent of ArrayAccess node. We traverse the ast upwards until the parent node is an instance of statement
			// if statement is(are) added to this parent node
			private ASTNode getParent(ArrayAccess node) {
				ASTNode parent = node.getParent();
				while(!(parent instanceof Statement)){
					parent = parent.getParent();
				}
				return parent;
			}
		});

		parentnodes = nodestmts.keySet();
		// for each parent node which may have multiple array access instances
		for(ASTNode parent: parentnodes){
			// create a newnode
			Block newnode = parent.getAST().newBlock();
			List<ASTNode> arrays = nodestmts.get(parent);

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
					expression.setRightOperand(parent.getAST().newNumberLiteral(lowerbound.get(array)));
					stmt.setExpression(expression);

					// and then part as "index = 0"
					Assignment thenexpression = null;
					thenexpression = parent.getAST().newAssignment();
					thenexpression.setLeftHandSide(parent.getAST().newSimpleName(arrayindex));
					thenexpression.setOperator(Assignment.Operator.ASSIGN);
					thenexpression.setRightHandSide(parent.getAST().newNumberLiteral(lowerbound.get(array)));
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

}
