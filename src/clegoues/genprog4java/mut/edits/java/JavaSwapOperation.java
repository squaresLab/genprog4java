package clegoues.genprog4java.mut.edits.java;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;


import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.JavaHole;
import clegoues.genprog4java.mut.holes.java.JavaLocation;

public class JavaSwapOperation extends JavaEditOperation {

	public JavaSwapOperation(JavaLocation location, List<EditHole> fixCode) {
		super(Mutation.SWAP, location, fixCode);
	}
	
	@Override
	public void edit(final ASTRewrite rewriter) {
		ASTNode locationNode = this.getLocationNode();
		JavaHole fixCode = (JavaHole) this.getHoleCode("singleHole"); 
		ASTNode fixCodeNode =
			 ASTNode.copySubtree(locationNode.getAST(), fixCode.getCode()); 
		rewriter.replace(locationNode, fixCodeNode, null);
		rewriter.replace(fixCode.getCode(), ASTNode
				.copySubtree(locationNode.getAST(), this.getLocationNode()), null); 
	}
}
