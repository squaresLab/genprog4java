package clegoues.genprog4java.mut.edits.java;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.main.ClassInfo;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.JavaHole;

public class JavaReplaceOperation extends JavaEditOperation {
	public JavaReplaceOperation(ClassInfo fileName, JavaStatement location,
			JavaStatement fixCode) {
		super(Mutation.REPLACE, fileName, location, fixCode);
	}
	@Override
	public void edit(final ASTRewrite rewriter) {
		ASTNode locationNode = this.getLocation().getASTNode();
		JavaHole fixHole = (JavaHole) this.getHoleCode("singleHole");
		ASTNode fixCodeNode =
				 ASTNode.copySubtree(locationNode.getAST(), fixHole.getCode());
		rewriter.replace(locationNode, fixCodeNode, null);

	}
}
