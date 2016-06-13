package clegoues.genprog4java.mut;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.main.ClassInfo;

public class JavaSwapOperation extends JavaEditOperation {

	public JavaSwapOperation(ClassInfo fileName, JavaStatement location,
			JavaStatement fixCode) {
		super(Mutation.SWAP, fileName, location, fixCode);
	}
	
	@Override
	public void edit(final ASTRewrite rewriter, AST ast, CompilationUnit cu) {
		ASTNode locationNode = this.getLocation().getASTNode();
		ASTNode fixCodeNode =
			 ASTNode.copySubtree(locationNode.getAST(), this
					.getFixCode().getASTNode());
		rewriter.replace(locationNode, fixCodeNode, null);
		rewriter.replace(this.getFixCode().getASTNode(), ASTNode
				.copySubtree(locationNode.getAST(), this.getLocation()
						.getASTNode()), null);
	}
}
