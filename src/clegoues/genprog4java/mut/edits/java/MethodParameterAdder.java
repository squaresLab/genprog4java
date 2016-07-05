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
import clegoues.genprog4java.mut.holes.java.ExpHole;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.holes.java.SubExpsHole;

public class MethodParameterAdder extends JavaEditOperation {


	public MethodParameterAdder(JavaLocation location, EditHole source) {
		super(location, source);
	}
	
	@Override
	public void edit(ASTRewrite rewriter) {
		SubExpsHole thisHole = (SubExpsHole) this.getHoleCode();
		ASTNode parentExp = ((JavaLocation) this.getLocation()).getCodeElement(); 
		MethodInvocation methodInvocation = (MethodInvocation) thisHole.getCode();
		MethodInvocation newMethodInvocation = parentExp.getAST().newMethodInvocation();
		SimpleName name = methodInvocation.getName();
		SimpleName newMethodName = parentExp.getAST().newSimpleName(name.getIdentifier());

		newMethodInvocation.setName(newMethodName);
		newMethodInvocation.setExpression(methodInvocation.getExpression());
		List<ASTNode> newArgs= thisHole.getSubExps();
		List<Expression> oldArgs = methodInvocation.arguments();
		int prevArgsSize = oldArgs.size();
		for(int i = 0 ; i < prevArgsSize; i++) {
			ASTNode newParam = rewriter.createCopyTarget(oldArgs.get(i));
			newMethodInvocation.arguments().add(newParam);
		}
		for(ASTNode newArg : newArgs) {
			ASTNode newParam = rewriter.createCopyTarget(newArg);
			newMethodInvocation.arguments().add(newParam);
		}
		// possible FIXME: make methodparamadder extend expressionreplacer?  Need to fix hole type problem if so
		rewriter.replace(methodInvocation, newMethodInvocation, null); 
	}

	@Override
	public String toString() {
		// FIXME: this is lazy
		return "pa(" + this.getLocation().getId() + ")";
	}
	
}

