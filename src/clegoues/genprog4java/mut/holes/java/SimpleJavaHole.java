package clegoues.genprog4java.mut.holes.java;

import org.eclipse.jdt.core.dom.ASTNode;
import clegoues.genprog4java.mut.EditHole;

public class SimpleJavaHole implements EditHole<ASTNode> {

	private String name;
	private ASTNode holeCode;
	private int codeBankId; // for debugging/ease of output, really
	
	public SimpleJavaHole () { }
	
	public SimpleJavaHole(String name, ASTNode holeCode, int codeBankId) {
		this.name = name;
		this.holeCode = holeCode;
		this.codeBankId = codeBankId;
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public ASTNode getCode() {
		return this.holeCode;
	}

	@Override
	public void setCode(ASTNode code) {
		this.holeCode = code;		
	}
	@Override
	public int compareTo(EditHole<ASTNode> o) {
		return 1; //I think this doesn't matter?
	}
	
	public String toString() {
		return ((Integer) this.getCodeBankId()).toString();
	}

	public int getCodeBankId() {
		return codeBankId;
	}

	public void setCodeBankId(int codeBankId) {
		this.codeBankId = codeBankId;
	}
}
