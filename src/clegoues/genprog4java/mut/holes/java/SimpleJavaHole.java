package clegoues.genprog4java.mut.holes.java;

import org.eclipse.jdt.core.dom.ASTNode;
import clegoues.genprog4java.mut.EditHole;

public class SimpleJavaHole implements EditHole<ASTNode> {

	private String name;
	private ASTNode holeCode;
	
	public SimpleJavaHole () { }
	
	public SimpleJavaHole(String name, ASTNode holeCode) {
		this.name = name;
		this.holeCode = holeCode;
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
}
