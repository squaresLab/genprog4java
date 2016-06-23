package clegoues.genprog4java.mut.edits.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.java.MethodInfo;
import clegoues.genprog4java.main.ClassInfo;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.holes.java.MethodInfoHole;
import clegoues.genprog4java.mut.holes.java.SubExpsHole;

public class MethodReplacer extends JavaEditOperation {

	public MethodReplacer(JavaLocation location,  HashMap<String, EditHole> sources) {
		super(Mutation.FUNREP, location, sources);
		this.holeNames.add("replaceMethod");
	}

	@Override
	public void edit(final ASTRewrite rewriter) {
		JavaStatement locationStmt = (JavaStatement) (this.getLocation().getLocation());
		ASTNode locationNode = locationStmt.getASTNode();
		MethodInfoHole thisHole = (MethodInfoHole) this.getHoleCode("replaceMethod");
		ASTNode toReplace = thisHole.getCode();
		IMethodBinding replaceWith = thisHole.getMethodInfo();

		MethodInvocation newNode = locationNode.getAST().newMethodInvocation();
		SimpleName newMethodName = locationNode.getAST().newSimpleName(replaceWith.getName());
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
		return "fr(" + this.getLocation().getId() + ")";
	}
	
}

