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

public class MethodParameterRemover extends ExpressionReplacer {
	

	public MethodParameterRemover(JavaLocation location, EditHole source) {
		super(location, source);
	}
	
	@Override
	public void edit(ASTRewrite rewriter) {
		ASTNode locationNode = ((JavaLocation) this.getLocation()).getCodeElement(); 
		ExpChoiceHole thisHole = (ExpChoiceHole) this.getHoleCode();
		Statement parentExp = (Statement) locationNode;
		int numRemove = thisHole.getChoice();
		MethodInvocation methodInvocation = (MethodInvocation) thisHole.getCode();
		MethodInvocation newMethodInvocation = parentExp.getAST().newMethodInvocation();
		SimpleName name = methodInvocation.getName();
		SimpleName newMethodName = locationNode.getAST().newSimpleName(name.getIdentifier());

		newMethodInvocation.setName(newMethodName);
		newMethodInvocation.setExpression(methodInvocation.getExpression());
		List<Expression> oldArgs= methodInvocation.arguments();
		int max = oldArgs.size() - numRemove;
		for(int i = 0; i < oldArgs.size() - numRemove; i++) {
			ASTNode newParam = rewriter.createCopyTarget(oldArgs.get(i));
			newMethodInvocation.arguments().add(newParam);
		}
		this.replaceExp(rewriter, newMethodInvocation);
	}

	@Override
	public String toString() {	
		// FIXME: is it possible to get the method call for this?  Would be nice for debug
		String retval = "prm(" + this.getLocation().getId() + ": ";
		Statement parentExp = (Statement) ((JavaLocation) this.getLocation()).getCodeElement(); 
;
		retval += "(" + parentExp.toString() + "))";
		return retval;
	}



}
