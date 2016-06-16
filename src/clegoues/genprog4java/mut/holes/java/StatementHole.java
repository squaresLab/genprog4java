package clegoues.genprog4java.mut.holes.java;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;

import clegoues.genprog4java.mut.EditHole;

public class StatementHole extends JavaHole {

	private Statement holeCode;
	private Statement holeSite;
	
	public StatementHole () { }
	
	public StatementHole(String name, Statement holeCode, int codeBankId) {
		super(name, codeBankId);
		this.holeCode = holeCode;
	}
	
	public StatementHole(String name,Statement holeSite, Statement holeCode, int codeBankId) {
		super(name, codeBankId);
		this.holeCode = holeCode;
		this.holeSite = holeSite;
	}
	
	@Override
	public ASTNode getCode() {
		return this.holeCode;
	}

	@Override
	public void setCode(ASTNode code) {
		this.holeCode = (Statement) code;		
	}

	public ASTNode getHoleSite() {
		return holeSite;
	}

	public void setHoleSite(Statement holeSite) {
		this.holeSite = holeSite;
	}
}
