package clegoues.genprog4java.mut.edits.java;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.ExpHole;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.holes.java.MethodInfoHole;

public class MethodReplacer extends JavaEditOperation {

	public MethodReplacer(JavaLocation location, EditHole source) {
		super(location, source);
	}
	

	@Override
	public void edit(final ASTRewrite rewriter) {
		ASTNode locationNode = ((JavaLocation) this.getLocation()).getCodeElement(); 
		MethodInfoHole thisHole = (MethodInfoHole) this.getHoleCode();
		ASTNode toReplace = thisHole.getCode();
		IMethodBinding replaceWith = thisHole.getMethodInfo();

		MethodInvocation newNode = rewriter.getAST().newMethodInvocation();
		SimpleName newMethodName = rewriter.getAST().newSimpleName(replaceWith.getName());
		newNode.setName(newMethodName);
		
		List<ASTNode> paramNodes = ((MethodInvocation) toReplace).arguments();
		for(ASTNode param : paramNodes) {
			ASTNode newParam = rewriter.createCopyTarget(param);
			newNode.arguments().add(newParam);
		}		
		
		rewriter.replace(toReplace, newNode, null); 
	}
	
	@Override
	public String toString() {
		// FIXME: this is lazy
		return "MethodReplacer(" + this.getLocation().getId() + ")";
	}
	
	
}

