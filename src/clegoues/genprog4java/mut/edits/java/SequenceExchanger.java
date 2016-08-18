package clegoues.genprog4java.mut.edits.java;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.holes.java.ExpHole;
import clegoues.genprog4java.mut.holes.java.JavaLocation;

public class SequenceExchanger  extends JavaEditOperation {

	public SequenceExchanger(JavaLocation location, EditHole source) {
		super(location, source);
	}
	@Override
	public void edit(ASTRewrite rewriter) {
		ExpHole thisHole = (ExpHole) this.getHoleCode();
		ASTNode toReplace = (ASTNode) thisHole.getCode();
		ASTNode replaceWith = ASTNode.copySubtree(rewriter.getAST(), thisHole.getLocationExp());
		rewriter.replace(toReplace, replaceWith, null);
	}
	
	public String toString() {
		ExpHole thisHole = (ExpHole) this.getHoleCode();
		String retval = "sqncEx(" + this.getLocation().getId() + ": ";
		retval += "(" + thisHole.getCode() + ") replaced with ";
		retval +=  "(" + thisHole.getLocationExp() + "))";
		return retval;
	}

}
