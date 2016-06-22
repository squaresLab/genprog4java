package clegoues.genprog4java.mut.holes.java;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;

public class ExpHole  extends JavaHole {

	private Expression holeCode;
	private ASTNode holeParent;
	
	public ExpHole () { }
	
	public ExpHole(String name, Expression holeCode, int codeBankId) {
		super(name, codeBankId);
		this.holeCode = holeCode;
	}

	public ExpHole(String name, ASTNode holeParent, Expression holeCode, int codeBankId) {
		super(name, codeBankId);
		this.holeCode = holeCode;
		this.holeParent = holeParent;
	}

	@Override
	public ASTNode getCode() {
		return this.holeCode;
	}

	@Override
	public void setCode(ASTNode code) {
		this.holeCode = (Expression) code;		
	}

	public ASTNode getHoleParent() {
		return holeParent;
	}

	public void setHoleParent(ASTNode holeSite) {
		this.holeParent = holeSite;
	}


}
