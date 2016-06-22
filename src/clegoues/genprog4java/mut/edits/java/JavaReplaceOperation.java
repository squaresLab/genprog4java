package clegoues.genprog4java.mut.edits.java;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.StatementHole;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.EditHole;

public class JavaReplaceOperation extends JavaEditOperation {
	
	public JavaReplaceOperation(JavaLocation location,
			HashMap<String,EditHole> fixCode) {
		super(Mutation.REPLACE, location, fixCode);
		holeNames.add("singleHole");
	}
	@Override
	public void edit(final ASTRewrite rewriter) {
		ASTNode locationNode = this.getLocationNode(); 
		StatementHole fixHole = (StatementHole) this.getHoleCode("singleHole");
		ASTNode fixCodeNode =
				 ASTNode.copySubtree(locationNode.getAST(), fixHole.getCode());
		rewriter.replace(locationNode, fixCodeNode, null);
	}
	
	@Override
	public String toString() {
		StatementHole fixHole = (StatementHole) this.getHoleCode("singleHole");
		return "r(" + this.getLocation().getId() + "," + fixHole.getCodeBankId() + ")";
	}
}
