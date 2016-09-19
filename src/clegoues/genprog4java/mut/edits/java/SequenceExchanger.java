package clegoues.genprog4java.mut.edits.java;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.holes.java.ExpHole;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.holes.java.StatementHole;

public class SequenceExchanger  extends JavaEditOperation {

	public SequenceExchanger(JavaLocation location, EditHole source) {
		super(location, source);
	}
	@Override
	public void edit(ASTRewrite rewriter) {
		
		ASTNode locationNode = ((JavaLocation) this.getLocation()).getCodeElement(); 
		StatementHole fixHole = (StatementHole) this.getHoleCode();
		ASTNode fixCodeNode =
				 ASTNode.copySubtree(rewriter.getAST(), fixHole.getCode());
		rewriter.replace(locationNode, fixCodeNode, null);
	}
	
	public String toString() {
		StatementHole fixHole = (StatementHole) this.getHoleCode();
		return "SequenceExchanger(" + this.getLocation().getId() + "," + fixHole.getCodeBankId() + ")";
	}

}
