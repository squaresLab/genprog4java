package clegoues.genprog4java.mut.holes.java;

import org.eclipse.jdt.core.dom.ASTNode;

public class ASTNodeHole extends JavaHole {
	public ASTNodeHole( ASTNode hole ) {
		this.hole = hole;
	}

	@Override
	public ASTNode getCode() {
		return this.hole;
	}

	@Override
	public void setCode(ASTNode hole) {
		this.hole = hole;
	}

	private ASTNode hole;
}
