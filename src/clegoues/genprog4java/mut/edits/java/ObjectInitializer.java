package clegoues.genprog4java.mut.edits.java;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.holes.java.ExpHole;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.util.Pair;

public class ObjectInitializer extends JavaEditOperation {

	public ObjectInitializer(JavaLocation location, EditHole source) {
		super(location, source);
	}

	  private static String nextString() {
	    return new BigInteger(130, Configuration.randomizer).toString(32);
	  }

	  
	  private ASTNode findCU(ASTNode node) {
		  ASTNode parent = node.getParent();
		  while(parent != null && !(parent instanceof CompilationUnit)) {
			  parent = parent.getParent();
		  }
		  return parent;
	  }
	@Override
	public void edit(final ASTRewrite rewriter) {
		final AST myAST = rewriter.getAST();
		ExpHole thisHole = (ExpHole) this.getHoleCode();
		Expression methodInvocExp = thisHole.getLocationExp();
		final CompilationUnit cu = (CompilationUnit) findCU(methodInvocExp);

		final List<Pair<Expression,Expression>> argsToInit = new LinkedList<Pair<Expression,Expression>>();
		final List<Statement> newDeclarations = new LinkedList<Statement>();
		ASTNode parentStmt = methodInvocExp.getParent();
		while(parentStmt != null && !(parentStmt instanceof Statement)) {
			parentStmt = parentStmt.getParent();
		}
		assert(parentStmt != null && cu != null);

		methodInvocExp.accept(new ASTVisitor() {
			public boolean visit(MethodInvocation node) {
				for(Object arg : node.arguments()) {
					Expression asExp = (Expression) arg;
					ITypeBinding binding = asExp.resolveTypeBinding();


					if(binding.isClass()) {
						String identifier = ObjectInitializer.nextString();
						SimpleName newVarName = myAST.newSimpleName(identifier);
						VariableDeclarationFragment fragment = myAST.newVariableDeclarationFragment();
						Type declaringType = (Type) cu.findDeclaringNode(binding);
						
						ClassInstanceCreation initializer = myAST.newClassInstanceCreation();
						initializer.setType(declaringType);
						
						fragment.setName(newVarName);
						fragment.setInitializer(initializer);
						
						VariableDeclarationStatement newStmt = myAST.newVariableDeclarationStatement(fragment);
						argsToInit.add(new Pair(asExp, newVarName));
						newDeclarations.add(newStmt);
						break;
					}
				}
				return true;
			}
		});
		for(Pair<Expression,Expression> repExp : argsToInit) {
			rewriter.replace(repExp.getFirst(), repExp.getSecond(), null);
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
}
