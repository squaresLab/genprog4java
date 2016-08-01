package clegoues.genprog4java.mut.holes.java;

import org.eclipse.jdt.core.dom.ASTNode;

public class JavaASTNodeLocation extends JavaLocation<ASTNode> {

	JavaLocation origLocInfo = null;
	public JavaASTNodeLocation(ASTNode location, Double weight) {
		super(location, weight);
		this.setCodeElement(location);
	}

	public JavaASTNodeLocation(ASTNode location) {
		super(location, 1.0);
		this.setCodeElement(location);
	}
	

	public JavaASTNodeLocation(JavaLocation origLocInfo, ASTNode location) {
		super(location, 1.0);
		this.setCodeElement(location);
		this.setClassInfo(origLocInfo.getClassInfo());
		this.origLocInfo = origLocInfo;
	}
	@Override
	public int getId() {
		return origLocInfo.getId();
	}

}
