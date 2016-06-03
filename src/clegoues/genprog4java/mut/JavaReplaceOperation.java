package clegoues.genprog4java.mut;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.main.ClassInfo;

public class JavaReplaceOperation extends JavaEditOperation {
	public JavaReplaceOperation(ClassInfo fileName, JavaStatement location,
			JavaStatement fixCode) {
		super(Mutation.REPLACE, fileName, location, fixCode);
	}
	@Override
	public void edit(final ASTRewrite rewriter, AST ast, CompilationUnit cu) {
		ASTNode locationNode = this.getLocation().getASTNode();
		ASTNode fixCodeNode =
				 ASTNode.copySubtree(locationNode.getAST(), this
						.getFixCode().getASTNode());
		rewriter.replace(locationNode, fixCodeNode, null);

	}
}
