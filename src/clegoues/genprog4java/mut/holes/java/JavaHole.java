package clegoues.genprog4java.mut.holes.java;

import org.eclipse.jdt.core.dom.ASTNode;

import clegoues.genprog4java.mut.EditHole;

public abstract class JavaHole implements EditHole<ASTNode> {
	private String name;
	private int codeBankId; // for debugging/ease of output, really

	public JavaHole() { } 
	
	public JavaHole(String name, int id) {
		this.name = name;
		this.codeBankId = id;
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public String getName() {
		return this.name;
	}
	
	public int getCodeBankId() {
		return codeBankId;
	}

	public void setCodeBankId(int codeBankId) {
		this.codeBankId = codeBankId;
	}
	
	@Override
	public int compareTo(EditHole<ASTNode> o) {
		return 1; //I think this doesn't matter?
	}
	
	@Override
	public String toString() {
		return ((Integer) this.getCodeBankId()).toString();
	}

}
