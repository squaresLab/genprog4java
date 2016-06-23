package clegoues.genprog4java.mut.edits.java;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.ExpChoiceHole;
import clegoues.genprog4java.mut.holes.java.JavaLocation;

public class MethodParameterRemover extends JavaEditOperation {

	protected MethodParameterRemover(JavaLocation location, HashMap<String, EditHole> sources) {
		super(Mutation.PARREM, location, sources);
		this.holeNames.add("remParameter");
	}

	@Override

	public void edit(ASTRewrite rewriter) {
		JavaStatement locationStmt = (JavaStatement) (this.getLocation().getLocation());
		ExpChoiceHole thisHole = (ExpChoiceHole) this.getHoleCode("remParameter");
		Statement parentExp = (Statement) locationStmt.getASTNode();
		int numRemove = thisHole.getChoice();
		MethodInvocation methodInvocation = (MethodInvocation) thisHole.getCode();
		MethodInvocation newMethodInvocation = parentExp.getAST().newMethodInvocation();
		SimpleName name = methodInvocation.getName();
		SimpleName newMethodName = locationStmt.getASTNode().getAST().newSimpleName(name.getIdentifier());

		newMethodInvocation.setName(newMethodName);
		newMethodInvocation.setExpression(methodInvocation.getExpression());
		List<Expression> oldArgs= methodInvocation.arguments();
		int max = oldArgs.size() - numRemove;
		for(int i = 0; i < oldArgs.size() - numRemove; i++) {
			ASTNode newParam = rewriter.createCopyTarget(oldArgs.get(i));
			newMethodInvocation.arguments().add(newParam);
		}
		rewriter.replace(thisHole.getHoleParent(), newMethodInvocation, null); 
	}

	@Override
	public String toString() {	
		JavaStatement locationStmt = (JavaStatement) (this.getLocation().getLocation());

		// FIXME: is it possible to get the method call for this?  Would be nice for debug
		String retval = "prm(" + this.getLocation().getId() + ": ";
		Statement parentExp = (Statement) locationStmt.getASTNode();
		retval += "(" + parentExp.toString() + "))";
		return retval;
	}



}
