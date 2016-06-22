package clegoues.genprog4java.mut.holes.java;

import org.eclipse.jdt.core.dom.ASTNode;

import clegoues.genprog4java.java.MethodInfo;

public class MethodInfoHole extends JavaHole  {

	private MethodInfo replacerInfo;
	private ASTNode holeParent;
	
	public MethodInfoHole() { } 
	public MethodInfoHole(String name, ASTNode holeParent, int codeBankId, MethodInfo holeInfo) {
		super(name, codeBankId);
		this.replacerInfo = holeInfo;
		this.holeParent = holeParent;
	}

	public MethodInfo getMethodInfo() {
		return this.replacerInfo;
	}
	

	@Override
	public ASTNode getCode() {
		return this.holeParent;
	}

	@Override
	public void setCode(ASTNode code) {
		this.holeParent = code;		
	}

	public ASTNode getHoleParent() {
		return holeParent;
	}

	public void setHoleParent(ASTNode holeSite) {
		this.holeParent = holeSite;
	}



}

