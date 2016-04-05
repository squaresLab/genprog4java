package clegoues.genprog4java.mut;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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

public class JavaMethodReplacer extends JavaEditOperation {

	public JavaMethodReplacer(ClassInfo fileName, JavaStatement location) {
		super(Mutation.METHODREPLACE, fileName, location);
	}

	@Override
	public void edit(final ASTRewrite rewriter, AST ast, final CompilationUnit cu) {
		JavaStatement locationStmt = this.getLocation();
		ASTNode locationNode = locationStmt.getASTNode();
		Map<ASTNode, List<MethodInfo>> candidateReplacements = locationStmt.getCandidateReplacements();
		List<MethodInfo> options = candidateReplacements.get(locationNode);
		Collections.shuffle(options, Configuration.randomizer);
		MethodInfo toReplace = options.get(0);
		MethodInvocation newNode = locationNode.getAST().newMethodInvocation();
		SimpleName newMethodName = locationNode.getAST().newSimpleName(toReplace.getName());
		newNode.setName(newMethodName);
		List<ASTNode> paramNodes = ((MethodInvocation) locationNode).arguments();
		for(ASTNode param : paramNodes) {
			ASTNode newParam = rewriter.createCopyTarget(param);
			newNode.arguments().add(newParam);

		}			
		rewriter.replace(locationNode, newNode, null);
	}
}
