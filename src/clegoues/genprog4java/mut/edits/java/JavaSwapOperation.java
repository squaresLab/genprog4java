package clegoues.genprog4java.mut.edits.java;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;


import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.StatementHole;
import clegoues.genprog4java.mut.holes.java.JavaLocation;

public class JavaSwapOperation extends JavaEditOperation {
	
	public JavaSwapOperation(JavaLocation location, EditHole source) {
		super(location, source);
	}
	@Override
	public void edit(final ASTRewrite rewriter) {
		ASTNode locationNode = ((JavaLocation) this.getLocation()).getCodeElement(); 
		StatementHole fixCode = (StatementHole) this.getHoleCode(); 
		ASTNode fixCodeNode =
			 ASTNode.copySubtree(rewriter.getAST(), fixCode.getCode()); 
		rewriter.replace(locationNode, fixCodeNode, null);
		rewriter.replace(fixCodeNode, ASTNode
				.copySubtree(locationNode.getAST(), locationNode), null); 
	}
	
	@Override
	public String toString() {
		StatementHole fixHole = (StatementHole) this.getHoleCode();
		return "s(" + this.getLocation().getId() + "," + fixHole.getCodeBankId() + ")";
	}
}
