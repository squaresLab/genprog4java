package clegoues.genprog4java.mut.holes.java;

import org.eclipse.jdt.core.dom.ASTNode;

import clegoues.genprog4java.java.MethodInfo;

// this is hideous but I'm lazy
public class MethodInfoHole extends SimpleJavaHole {

	private MethodInfo replacerInfo;

	public MethodInfoHole() { } 
	public MethodInfoHole(String name, ASTNode holeParent, int codeBankId, MethodInfo holeInfo) {
		super(name,  holeParent, codeBankId);
		this.replacerInfo = holeInfo;
	}

	public MethodInfo getMethodInfo() {
		return this.replacerInfo;
	}
}

