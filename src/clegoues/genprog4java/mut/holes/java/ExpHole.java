package clegoues.genprog4java.mut.holes.java;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;

public class ExpHole  extends JavaHole {

	private Expression holeCode;
	private Expression locationExp;
	
	public ExpHole(Expression holeParent, Expression holeCode, int codeBankId) {
		super("ExpHole", codeBankId);
		this.holeCode = holeCode;
		this.locationExp = holeParent;
	}

	protected ExpHole(String name, Expression holeParent, Expression holeCode, int codeBankId) {
		super(name, codeBankId);
		this.holeCode = holeCode;
		this.locationExp = holeParent;
	}
	@Override
	public ASTNode getCode() {
		return this.holeCode;
	}

	@Override
	public void setCode(ASTNode code) {
		this.holeCode = (Expression) code;		
	}

	public Expression getLocationExp() {
		return locationExp;
	}

	public void setLocationExp(Expression holeSite) {
		this.locationExp = holeSite;
	}


}
