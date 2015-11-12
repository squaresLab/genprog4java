/*
 * Copyright (c) 2014-2015, 
 *  Claire Le Goues     <clegoues@cs.cmu.edu>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. The names of the contributors may not be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package clegoues.genprog4java.mut;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;


import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.objectweb.asm.tree.MethodInsnNode;
import org.eclipse.jdt.core.dom.QualifiedName;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.main.ClassInfo;

public class JavaEditOperation implements
EditOperation<JavaStatement, ASTRewrite, AST> {

	private Mutation mutType;
	private JavaStatement location = null;
	private JavaStatement fixCode = null;
	private ClassInfo fileInfo = null;

	public JavaEditOperation(ClassInfo fileName, JavaStatement location, Mutation mutType) {
		this.mutType = mutType;
		this.location = location;
		this.fileInfo = fileName;
	}

	public JavaEditOperation(Mutation mutType, ClassInfo fileName, JavaStatement location,
			JavaStatement fixCode) {
		this.mutType = mutType;
		this.location = location;
		this.fixCode = fixCode;
		this.fileInfo = fileName;
	}

	@Override
	public Mutation getType() {
		return this.mutType;
	}

	@Override
	public void setType(Mutation type) {
		this.mutType = type;
	}

	public JavaStatement getLocation() {
		return this.location;
	}

	public void setLocation(JavaStatement location) {
		this.location = location;
	}

	public void setFixCode(JavaStatement fixCode) {
		this.fixCode = fixCode;
	}

	public JavaStatement getFixCode() {
		return this.fixCode;
	}

	public ClassInfo getFileInfo() {
		return this.fileInfo;
	}

	public void setFileInfo(ClassInfo newFileName){
		fileInfo = newFileName;
	}

	/*protected static ListRewrite getListRewriter(ASTNode origin, ASTNode fix, ASTRewrite rewriter) {
		ASTNode parent = origin;

		//while (!(parent instanceof Block)) {
		//	parent = parent.getParent();
		//}


		//make a new statement with the append (probably a block), and replace the origin in the parent for this new one
		Block newNode = origin.getAST().newBlock();
		ASTNode stm1 = (Statement)origin;
		if(origin instanceof Statement){
			stm1 = ASTNode.copySubtree(origin.getAST(), stm1);
			newNode.statements().add(stm1);
		}
		ASTNode stm2 = (Statement)fix;
		if(origin instanceof Statement){
			stm2 = ASTNode.copySubtree(fix.getAST(), stm2);
			newNode.statements().add(stm2);
		}

		rewriter.replace(origin, newNode, null);


		return rewriter.getListRewrite(parent, Block.STATEMENTS_PROPERTY);
	}*/

	@Override
	public void edit(final ASTRewrite rewriter, AST ast, CompilationUnit cu) {
		ASTNode locationNode = this.getLocation().getASTNode();

		// these are used for array access related checks for implementing PAR templates
		final Map<ASTNode, List<ASTNode>> nodestmts = new HashMap<ASTNode, List<ASTNode>>();	// to track the parent nodes of array access nodes
		final Map<ASTNode, String> lowerbound = new HashMap<ASTNode, String>();			// to set the lower-bound values of array. currently set to arrayname.length
		final Map<ASTNode, String> upperbound = new HashMap<ASTNode, String>();			// to set the upper-bound values of array. currently set to arrayname.length
		Set<ASTNode> parentnodes = nodestmts.keySet();

		ASTNode fixCodeNode = null;
		if (this.fixCode != null) {
			fixCodeNode = ASTNode.copySubtree(locationNode.getAST(), this
					.getFixCode().getASTNode());
		}
		switch (this.getType()) {
		case APPEND:

			Block newNode = locationNode.getAST().newBlock(); 
			ASTNode stm1 = (Statement)locationNode;
			if(locationNode instanceof Statement){
				stm1 = ASTNode.copySubtree(locationNode.getAST(), stm1);
				newNode.statements().add(stm1);
			}
			ASTNode stm2 = (Statement)fixCodeNode;
			if(fixCodeNode instanceof Statement){
				stm2 = ASTNode.copySubtree(fixCodeNode.getAST(), stm2);
				newNode.statements().add(stm2);
			}

			rewriter.replace(locationNode, newNode, null);


			break;
		case REPLACE:
			rewriter.replace(locationNode, fixCodeNode, null);
			break;
		case SWAP:
			rewriter.replace(locationNode, fixCodeNode, null);
			rewriter.replace(this.getFixCode().getASTNode(), ASTNode
					.copySubtree(locationNode.getAST(), this.getLocation()
							.getASTNode()), null);
			break;
		case DELETE:
			rewriter.remove(locationNode, null);
			break;
		case NULLINSERT:
			// TODO:Have to figure this out
			//This is the same as delete, what is it supposed to be?
			rewriter.remove(locationNode, null);
			break;

			//Implement All These
		case CASTCHECK:

			break;
		case EXPADD:

			break;
		case EXPREM:

			break;
		case EXPREP:

			break;
		case FUNREP:

			break;
		case NULLCHECK:
			List<String> listToCheckNulls = new ArrayList<String>();

			if(locationNode instanceof MethodInvocation){
				listToCheckNulls.add(((MethodInvocation) locationNode).getName().toString());
			}

			if(locationNode instanceof FieldAccess){
				listToCheckNulls.add(((FieldAccess) locationNode).getExpression().toString());
			}

			if(locationNode instanceof QualifiedName){
				listToCheckNulls.add(((FieldAccess) locationNode).getName().toString());
			}

			if(listToCheckNulls.size() > 0){

				//Create if before the error
				IfStatement ifstmt = locationNode.getAST().newIfStatement();
				Block newNode1 = locationNode.getAST().newBlock(); 
				InfixExpression everythingInTheCondition = locationNode.getAST().newInfixExpression(); 
				InfixExpression keepForNextRound = locationNode.getAST().newInfixExpression(); 

				if(listToCheckNulls.size()==1){
					InfixExpression expression = null;
					expression = locationNode.getAST().newInfixExpression();
					expression.setLeftOperand(locationNode.getAST().newSimpleName(listToCheckNulls.get(0).toString()));
					expression.setOperator(Operator.EQUALS);
					expression.setRightOperand(locationNode.getAST().newSimpleName("null")); //NOT SURE IF THIS IS THE RIGHT WAY TO GET NULL
					ifstmt.setExpression(expression);
				}else{
					for(String sn : listToCheckNulls){
						// with expression "x==null" 
						InfixExpression expression = null;
						expression = locationNode.getAST().newInfixExpression();
						expression.setLeftOperand(locationNode.getAST().newSimpleName(sn));
						expression.setOperator(Operator.EQUALS);
						expression.setRightOperand(locationNode.getAST().newSimpleName("null")); //NOT SURE IF THIS IS THE RIGHT WAY TO GET NULL

						if(!sn.equals(listToCheckNulls.get(0))) {
							everythingInTheCondition = locationNode.getAST().newInfixExpression();
							everythingInTheCondition.setLeftOperand(keepForNextRound);
							everythingInTheCondition.setOperator(Operator.AND);
							everythingInTheCondition.setRightOperand(expression);
						}
						keepForNextRound = expression;
					}
					ifstmt.setExpression(everythingInTheCondition);
				}

				//add then statement
				//Block thenexpression = null;
				//thenexpression = locationNode.getAST().newBlock();
				//ASTNode copyLocationNode = (Statement)locationNode;
				//copyLocationNode = ASTNode.copySubtree(locationNode.getAST(), copyLocationNode);
				ifstmt.setThenStatement((Statement) locationNode);
				newNode1.statements().add(ifstmt);

				rewriter.replace(locationNode, newNode1, null);
			}

			break;
		case OBJINIT:

			break;
		case PARADD:

			break;
		case PARREM:

			break;
		case PARREP:

			break;
		case RANGECHECK:
			locationNode.accept(new ASTVisitor() {

				// method to visit all ArrayAccess nodes in locationNode and
				// store their parents
				public boolean visit(ArrayAccess node) {
					lowerbound.put(node, "0");
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
					return true;
				}

				// method to get the parent of ArrayAccess node. We traverse the
				// ast upwards until the parent node is an instance of statement
				// if statement is(are) added to this parent node
				private ASTNode getParent(ArrayAccess node) {
					ASTNode parent = node.getParent();
					while (!(parent instanceof Statement)) {
						parent = parent.getParent();
					}
					return parent;
				}
			});

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
								.newNumberLiteral(lowerbound.get(array)));

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

			break;
		case SIZECHECK:

			break;
		case LBOUNDSET:
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

			break;
		case UBOUNDSET:				
			locationNode.accept(new ASTVisitor() {

				// method to visit all ArrayAccess nodes in locationNode and store their parents
				public boolean visit(ArrayAccess node) {
					upperbound.put(node, node.getArray().toString().concat(".length"));
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

						// with expression "index > arrayname.length" 
						InfixExpression expression = null;
						expression = parent.getAST().newInfixExpression();
						expression.setLeftOperand(parent.getAST().newSimpleName(arrayindex));
						expression.setOperator(Operator.GREATER_EQUALS);

						// and then part as "index = arrayname.length - 1"
						SimpleName qualifier = parent.getAST().newSimpleName(((ArrayAccess)array).getArray().toString());
						SimpleName name = parent.getAST().newSimpleName("length");
						expression.setRightOperand(parent.getAST().newQualifiedName(qualifier, name));
						stmt.setExpression(expression);

						Assignment thenexpression = null;
						thenexpression = parent.getAST().newAssignment();
						thenexpression.setLeftHandSide(parent.getAST().newSimpleName(arrayindex));
						thenexpression.setOperator(Assignment.Operator.ASSIGN);

						InfixExpression setupperboundexpression = null;
						setupperboundexpression = parent.getAST().newInfixExpression();
						SimpleName qualifier1 = parent.getAST().newSimpleName(((ArrayAccess)array).getArray().toString());
						SimpleName name1 = parent.getAST().newSimpleName("length");
						setupperboundexpression.setLeftOperand(parent.getAST().newQualifiedName(qualifier1, name1));
						setupperboundexpression.setOperator(Operator.MINUS);
						setupperboundexpression.setRightOperand(parent.getAST().newNumberLiteral("1"));

						thenexpression.setRightHandSide(setupperboundexpression);

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

			break;	
		case OFFBYONE:
			locationNode.accept(new ASTVisitor() {
				int mutationtype;	// used to randomly put + or - operator while mutating array index
				// method to visit all ArrayAccess nodes modify array index by 1
				public boolean visit(ArrayAccess node) {

					// using random numbers (even or odd) to increase or decrease the index by 1
					Random rand = new Random();
					int randomNum = rand.nextInt(11);
					if(randomNum%2==0){
						mutationtype = 0;
					}else{
						mutationtype = 1;
					}
					Expression arrayindex = node.getIndex(); // original index
					Expression mutatedindex = mutateIndex(arrayindex, 1); // method call to get mutated index
					rewriter.replace(arrayindex, mutatedindex, null);	// replacing original index with mutated index
					return true;
				}

				// recursive method to mutate array index. (increase or decrease the index by 1)
				private Expression mutateIndex(Expression arrayindex, int mutateflag) { // arrayindex is the index to be mutated, mutateflag is used to check if mutation is to be performed.

					if (arrayindex instanceof SimpleName) {  // if index is simple variable name
						SimpleName name = arrayindex.getAST().newSimpleName(arrayindex.toString());	// fetch the name
						if (mutateflag == 0) {	// if no mutation is to be performed then return the index
							return name;
						}
						// create infix expression with index +/- 1
						InfixExpression mutatedindex = null;
						mutatedindex = arrayindex.getAST().newInfixExpression();
						mutatedindex.setLeftOperand(name);
						if (mutationtype == 0) {
							mutatedindex.setOperator(Operator.MINUS);
							mutationtype = 1;
						} else {
							mutatedindex.setOperator(Operator.PLUS);
							mutationtype = 0;
						}
						mutatedindex.setRightOperand(arrayindex.getAST().newNumberLiteral("1"));
						// return mutated index
						return mutatedindex;
					} else if (arrayindex instanceof NumberLiteral) { // if index is number
						NumberLiteral number = arrayindex.getAST().newNumberLiteral(arrayindex.toString());
						if (mutateflag == 0) { // if no mutation is to be performed then return the index
							return number;
						}
						// create infix expression with index +/- 1
						InfixExpression mutatedindex = null;
						mutatedindex = arrayindex.getAST().newInfixExpression();
						mutatedindex.setLeftOperand(number);
						if (mutationtype == 0) {
							mutatedindex.setOperator(Operator.MINUS);
							mutationtype = 1;
						} else {
							mutatedindex.setOperator(Operator.PLUS);
							mutationtype = 0;
						}
						mutatedindex.setRightOperand(arrayindex.getAST().newNumberLiteral("1"));
						// return mutated index
						return mutatedindex;
					} else if (arrayindex instanceof PostfixExpression && (arrayindex.toString().contains("++") || arrayindex.toString().contains("--"))) { // if index postfix expression
						PostfixExpression pexp = arrayindex.getAST().newPostfixExpression();
						String indexname = ((PostfixExpression) arrayindex).getOperand().toString();
						pexp.setOperand(arrayindex.getAST().newSimpleName(indexname));

						if (arrayindex.toString().contains("++")) {
							pexp.setOperator(org.eclipse.jdt.core.dom.PostfixExpression.Operator.INCREMENT);
						} else if (arrayindex.toString().contains("--")) {
							pexp.setOperator(org.eclipse.jdt.core.dom.PostfixExpression.Operator.DECREMENT);
						}

						if (mutateflag == 0) { // if no mutation is to be performed then return the index
							return pexp;
						}
						// create infix expression with index +/- 1
						InfixExpression mutatedindex = null;
						mutatedindex = arrayindex.getAST().newInfixExpression();
						mutatedindex.setLeftOperand(pexp);

						if (mutationtype == 0) {
							mutatedindex.setOperator(Operator.MINUS);
							mutationtype = 1;
						} else {
							mutatedindex.setOperator(Operator.PLUS);
							mutationtype = 0;
						}

						mutatedindex.setRightOperand(arrayindex.getAST().newNumberLiteral("1"));
						// return mutated index
						return mutatedindex;

					} else if (arrayindex instanceof PrefixExpression && (arrayindex.toString().contains("++") || arrayindex.toString().contains("--"))) { // if index is prefix expression
						PrefixExpression pexp = arrayindex.getAST().newPrefixExpression();
						String indexname = ((PrefixExpression) arrayindex).getOperand().toString();
						pexp.setOperand(arrayindex.getAST().newSimpleName(indexname));

						if (arrayindex.toString().contains("++")) {
							pexp.setOperator(org.eclipse.jdt.core.dom.PrefixExpression.Operator.INCREMENT);
						} else if (arrayindex.toString().contains("--")) {
							pexp.setOperator(org.eclipse.jdt.core.dom.PrefixExpression.Operator.DECREMENT);
						}

						if (mutateflag == 0) { // if no mutation is to be performed then return the index
							return pexp;
						}
						// create infix expression with index +/- 1
						InfixExpression mutatedindex = null;
						mutatedindex = arrayindex.getAST().newInfixExpression();
						mutatedindex.setLeftOperand(pexp);
						if (mutationtype == 0) {
							mutatedindex.setOperator(Operator.MINUS);
							mutationtype = 1;
						} else {
							mutatedindex.setOperator(Operator.PLUS);
							mutationtype = 0;
						}
						mutatedindex.setRightOperand(arrayindex.getAST().newNumberLiteral("1"));
						// return mutated index
						return mutatedindex;
					} else if (arrayindex instanceof InfixExpression) {// if index is infix expression
						InfixExpression iexp = arrayindex.getAST().newInfixExpression();
						Expression loperand = ((InfixExpression) arrayindex).getLeftOperand();
						if (loperand != null) {
							iexp.setLeftOperand(mutateIndex(((InfixExpression) arrayindex).getLeftOperand(), 0));
						}

						Operator ioperator = ((InfixExpression) arrayindex).getOperator();
						iexp.setOperator(ioperator);

						Expression roperand = ((InfixExpression) arrayindex).getRightOperand();
						if (roperand != null) {
							iexp.setRightOperand(mutateIndex(((InfixExpression) arrayindex).getRightOperand(), 0));
						}
						// create infix expression with index +/- 1
						InfixExpression mutatedindex = null;
						mutatedindex = arrayindex.getAST().newInfixExpression();
						mutatedindex.setLeftOperand(iexp);
						if (mutationtype == 0) {
							mutatedindex.setOperator(Operator.MINUS);
							mutationtype = 1;
						} else {
							mutatedindex.setOperator(Operator.PLUS);
							mutationtype = 0;
						}
						mutatedindex.setRightOperand(arrayindex.getAST().newNumberLiteral("1"));
						// return mutated index
						return mutatedindex;
					}
					return arrayindex;
				}
			});

			break;

		default:
			break;

		}
	}



}
