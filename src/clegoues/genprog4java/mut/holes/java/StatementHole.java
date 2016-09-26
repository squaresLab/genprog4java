package clegoues.genprog4java.mut.holes.java;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;

import clegoues.genprog4java.mut.EditHole;

public class StatementHole extends JavaHole {

	private Statement holeCode;
	private Statement holeSite;
	
	public StatementHole () { }
	
	public StatementHole(Statement holeCode, int codeBankId) {
		super("statementHole", codeBankId);
		this.holeCode = holeCode;
	}
	
	public StatementHole(Statement holeSite, Statement holeCode, int codeBankId) {
		super("statementHole", codeBankId);
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
