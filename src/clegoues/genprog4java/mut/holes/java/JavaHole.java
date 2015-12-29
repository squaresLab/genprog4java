package clegoues.genprog4java.mut.holes.java;

import org.eclipse.jdt.core.dom.ASTNode;
import clegoues.genprog4java.mut.EditHole;

public class JavaHole implements EditHole<ASTNode> {

	private String name;
	private ASTNode holeCode;
	
	public JavaHole(String name, ASTNode holeCode) {
		this.name = name;
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

}
