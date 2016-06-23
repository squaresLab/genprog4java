package clegoues.genprog4java.mut.holes.java;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IMethodBinding;

public class MethodInfoHole extends JavaHole  {

	private IMethodBinding replacerInfo;
	private ASTNode holeParent;
	
	public MethodInfoHole() { } 
	public MethodInfoHole(ASTNode holeParent, int codeBankId, IMethodBinding holeInfo) {
		super("MethodInfoHole", codeBankId);
		this.replacerInfo = holeInfo;
		this.holeParent = holeParent;
	}

	public IMethodBinding getMethodInfo() {
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

