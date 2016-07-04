package clegoues.genprog4java.mut.edits.java;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.mut.holes.java.ASTNodeHole;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.EditHole;

public class JavaReplaceASTOperation extends JavaEditOperation {
	
	public JavaReplaceASTOperation(JavaLocation location, EditHole source) {
		super(location, source);
	}
	
	@Override
	public void edit(final ASTRewrite rewriter) {
		ASTNode locationNode = this.getLocationNode(); 
		ASTNodeHole fixHole = (ASTNodeHole) this.getHoleCode();
		ASTNode fixCodeNode =
			 ASTNode.copySubtree(locationNode.getAST(), fixHole.getCode());
		rewriter.replace(locationNode, fixCodeNode, null);
	}
	
	@Override
	public String toString() {
		return "n(" + this.getLocation().getId() + ",???)";
	}
}
