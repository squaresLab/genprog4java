package clegoues.genprog4java.mut.edits.java;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.java.ASTUtils;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.holes.java.ExpHole;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.holes.java.SubExpsHole;

public class ObjectInitializer extends JavaEditOperation {

	// world's hugest hack
	private String varBase = "clgUNIQUEPLZ";
	private int count = 0;

	// by all rights this should probably be in globalUtils somewhere


	public ObjectInitializer(JavaLocation location, EditHole source) {
		super(location, source);
	}




	@Override
	public void edit(final ASTRewrite rewriter) {
		final AST myAST = rewriter.getAST();
		ExpHole thisHole = (ExpHole) this.getHoleCode();
		Expression methodInvocExp = thisHole.getLocationExp();

		final List<Pair<Expression,Expression>> argsToInit = new LinkedList<Pair<Expression,Expression>>();
		final List<Statement> newDeclarations = new LinkedList<Statement>();
		ASTNode parentStmt = methodInvocExp.getParent();
		while(parentStmt != null && !(parentStmt instanceof Statement)) {
			parentStmt = parentStmt.getParent();
		}
		assert(parentStmt != null);

		methodInvocExp.accept(new ASTVisitor() {
			
			public boolean visit(MethodInvocation node) {
				for(Object arg : node.arguments()) {
					if(arg instanceof SimpleName) {
					SimpleName asExp = (SimpleName) arg;
					ITypeBinding binding = asExp.resolveTypeBinding();

					if(binding != null && binding.isClass()) {
						SimpleName newVarName = myAST.newSimpleName(((SimpleName) arg).getIdentifier());
						Assignment newAssignment = myAST.newAssignment();
						newAssignment.setLeftHandSide(newVarName);
						Type declaringType1 = ASTUtils.typeFromBinding(myAST, binding);

						ClassInstanceCreation initializer = myAST.newClassInstanceCreation();
						initializer.setType(declaringType1);
						newAssignment.setRightHandSide(initializer);
						newAssignment.setOperator(Assignment.Operator.ASSIGN);
					
						ExpressionStatement es = myAST.newExpressionStatement(newAssignment);
						Pair toAdd = Pair.of(asExp, newVarName);
						argsToInit.add(toAdd);
						newDeclarations.add(es);
						break;
					}
				}
				}
					
				return true;
			}
		});
		for(Pair<Expression,Expression> repExp : argsToInit) {
			rewriter.replace(repExp.getLeft(), repExp.getRight(), null);
		}

		Block newBlock = myAST.newBlock();
		newBlock.statements().addAll(newDeclarations);
		newBlock.statements().add(rewriter.createCopyTarget(parentStmt));
		rewriter.replace(parentStmt, newBlock, null);
	}
	/* [Object Initializer]
			B = buggy statements
			collect object creation operators in parameters of B into collection C

			loop for all object creations in C
			{
			 insert an assignment statement that declares a new local variable and creates a new object (using an object creation's type) to the variable
			 insert a method invocation statement that calls an initialization method 
			}
			insert B after statements
	 */
	@Override
	public String toString() {
		return "Object initializer";
	}
}
