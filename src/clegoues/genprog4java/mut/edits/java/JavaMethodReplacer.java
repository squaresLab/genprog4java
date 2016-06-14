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

public class JavaMethodReplacer extends JavaEditOperation {

	public JavaMethodReplacer(JavaLocation location,  HashMap<String, EditHole> sources) {
		super(Mutation.METHODREPLACE, location, sources);

	}

	@Override
	public void edit(final ASTRewrite rewriter) {
		JavaStatement locationStmt = (JavaStatement) (this.getLocation().getLocation());
		ASTNode locationNode = locationStmt.getASTNode();
		Map<ASTNode, List<MethodInfo>> candidateReplacements = locationStmt.getCandidateMethodReplacements();
		List<ASTNode> optionsToBeReplaced = new ArrayList<ASTNode>(candidateReplacements.keySet());
		Collections.shuffle(optionsToBeReplaced, Configuration.randomizer);
		ASTNode toReplace = optionsToBeReplaced.get(0);
		List<MethodInfo> options = candidateReplacements.get(toReplace);
		Collections.shuffle(options, Configuration.randomizer);
		MethodInfo replaceWith = options.get(0);
		
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
}
