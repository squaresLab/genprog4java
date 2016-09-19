package clegoues.genprog4java.mut.edits.java;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEdit;

import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.holes.java.StatementHole;

public class JavaDeleteOperation extends JavaEditOperation {
	

	public JavaDeleteOperation(JavaLocation location) {
		super(location);
	}
	@Override
	public void edit(final ASTRewrite rewriter) {
		ASTNode locationNode = ((JavaLocation) this.getLocation()).getCodeElement(); 
		
		  Block emptyBlock = (Block) rewriter.getAST().createInstance(Block.class);

	        /* Replace the faulty statement with the empty Block. */
	        rewriter.replace(locationNode, emptyBlock, null);
	        
			
	}
	
	@Override
	public String toString() {
		return "StmtDelete(" + this.getLocation().getId() + ")";
	}
}

