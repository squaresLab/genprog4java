package clegoues.genprog4java.mut;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.main.ClassInfo;

public class JavaDeleteOperation extends JavaEditOperation {

	public JavaDeleteOperation(ClassInfo fileName, JavaStatement location) {
		super(Mutation.DELETE, fileName,  location);
	}
	
	@Override
	public void edit(final ASTRewrite rewriter, AST ast, CompilationUnit cu) {
		ASTNode locationNode = this.getLocation().getASTNode();
		rewriter.remove(locationNode, null);
	}
}

