package clegoues.genprog4java.mut.edits.java;

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
import clegoues.genprog4java.mut.Mutation;

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
	public void edit(final ASTRewrite rewriter) {
		ASTNode locationNode = this.getLocation().getASTNode();
		final Map<ASTNode, List<ASTNode>> nodestmts = new HashMap<ASTNode, List<ASTNode>>();	// to track the parent nodes of array access nodes
		Set<ASTNode> parentnodes = null; 

	locationNode.accept(new ASTVisitor() {

		// method to visit all Expressions relevant for this in locationNode and
		// store their parents
		public boolean visit(MethodInvocation node) {
			saveDataOfTheExpression(node);
			saveDataOfTheExpression(((MethodInvocation)node).getExpression());
			return true;
		}

		public boolean visit(FieldAccess node) {
			saveDataOfTheExpression(node);
			saveDataOfTheExpression(((FieldAccess)node).getExpression());
			return true;
		}

		public boolean visit(QualifiedName node) {
			saveDataOfTheExpression(node);
			saveDataOfTheExpression(((QualifiedName)node).getName());
			return true;
		}

		public void saveDataOfTheExpression(ASTNode node){
			//expressions.put(node, "0");
			ASTNode parent = getParent(node);
			if (!nodestmts.containsKey(parent)) {
				List<ASTNode> arraynodes = new ArrayList<ASTNode>();
				arraynodes.add(node);
				nodestmts.put(parent, arraynodes);
			} else {
				List<ASTNode> arraynodes = (List<ASTNode>) nodestmts
						.get(parent);
				if (!arraynodes.contains(node))
					arraynodes.add(node);
				nodestmts.put(parent, arraynodes);
			}
		}

		// method to get the parent of ArrayAccess node. We traverse the
		// ast upwards until the parent node is an instance of statement
		// if statement is(are) added to this parent node
		private ASTNode getParent(ASTNode node) {
			ASTNode parent = node.getParent();
			while (!(parent instanceof Statement)) {
				parent = parent.getParent();
			}
			return parent;
		}
	});

	parentnodes = nodestmts.keySet();
	// for each parent node which may have multiple'   
	for(ASTNode parent: parentnodes){
		// create a newnode
		//Block newnode = parent.getAST().newBlock();
		List<ASTNode> expressionsFromThisParent = nodestmts.get(parent);

		//Create if before the error
		IfStatement ifstmt = locationNode.getAST().newIfStatement();
		//Block newNode1 = locationNode.getAST().newBlock(); 
		InfixExpression everythingInTheCondition = locationNode.getAST().newInfixExpression(); 
		InfixExpression keepForNextRound = locationNode.getAST().newInfixExpression(); 

		ASTNode lastExpInTheList = expressionsFromThisParent.get(expressionsFromThisParent.size()-1);
		// for each of the expressions that can be null
		for(ASTNode  expressionToCheckIfNull : expressionsFromThisParent){

			//String leftOperand=null;
			
			

			InfixExpression expression = null;
			expression = expressionToCheckIfNull.getAST().newInfixExpression();
			//ASTNode toCheckIfNullNode = ASTNode.copySubtree(expressionToCheckIfNull.getAST(), expressionToCheckIfNull);
			
			if(expressionToCheckIfNull instanceof MethodInvocation){
				
				expression.setLeftOperand(createExpression(expressionToCheckIfNull));
				//expression.setLeftOperand((Expression)expressionToCheckIfNull);
			
			}
			if(expressionToCheckIfNull instanceof FieldAccess)
				expression.setLeftOperand(((FieldAccess) expressionToCheckIfNull).getExpression());
			if(expressionToCheckIfNull instanceof QualifiedName)
				expression.setLeftOperand(((QualifiedName) expressionToCheckIfNull).getName());
			
			
			//expression.setLeftOperand((Expression)toCheckIfNullNode);
			expression.setOperator(Operator.EQUALS);
			expression.setRightOperand(expressionToCheckIfNull.getAST().newNullLiteral());

		/*	if(expressionsFromThisParent.size() != 1) {
				everythingInTheCondition = expressionToCheckIfNull.getAST().newInfixExpression();
				everythingInTheCondition.setLeftOperand(keepForNextRound);
				everythingInTheCondition.setOperator(Operator.AND);
				everythingInTheCondition.setRightOperand(expression);
				keepForNextRound = expression;
			}else{*/
				everythingInTheCondition = expression;
//			}
			

		}
		ifstmt.setExpression(everythingInTheCondition);
				
		ASTNode thenStmt = (Statement) parent;
		//thenStmt = locationNode.getAST().newBlock();
		thenStmt = ASTNode.copySubtree(parent.getAST(), thenStmt);
		ifstmt.setThenStatement((Statement) thenStmt);
		//newNode.statements().add(rangechkstmt);
				
		//ifstmt.setThenStatement((Statement)thenStmt);
		rewriter.replace(parent, ifstmt, null);
	}	
	}
}
