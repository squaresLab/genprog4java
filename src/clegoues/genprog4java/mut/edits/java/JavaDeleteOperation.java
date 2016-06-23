package clegoues.genprog4java.mut.edits.java;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.holes.java.StatementHole;

public class JavaDeleteOperation extends JavaEditOperation {
	

	public JavaDeleteOperation(JavaLocation location) {
		super(location);
	}
	@Override
	public void edit(final ASTRewrite rewriter) {
		ASTNode locationNode = this.getLocationNode(); 
		rewriter.remove(locationNode, null);
	}
	
	@Override
	public String toString() {
		return "d(" + this.getLocation().getId() + ")";
	}
}

