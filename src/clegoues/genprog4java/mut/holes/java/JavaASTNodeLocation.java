package clegoues.genprog4java.mut.holes.java;

import org.eclipse.jdt.core.dom.ASTNode;

public class JavaASTNodeLocation extends JavaLocation<ASTNode> {

	public JavaASTNodeLocation(ASTNode location, Double weight) {
		super(location, weight);
		this.setCodeElement(location);
	}

	public JavaASTNodeLocation(ASTNode location) {
		super(location, 1.0);
		this.setCodeElement(location);
	}
	@Override
	public int getId() {
		return 0; // FIXME halp
	}

}
