package clegoues.genprog4java.mut.holes.java;

import org.eclipse.jdt.core.dom.ASTNode;
import clegoues.genprog4java.mut.EditHole;

public class JavaHole implements EditHole<ASTNode> {

	private String name;
	private ASTNode holeCode;
	private ASTNode holeParent;
	
	public JavaHole(String name, ASTNode holeCode, ASTNode holeParent) {
		this.name = name;
		this.holeCode = holeCode;
		this.setHoleParent(holeParent);
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

	public ASTNode getHoleParent() {
		return holeParent;
	}

	public void setHoleParent(ASTNode holeParent) {
		this.holeParent = holeParent;
	}

	@Override
	public double getWeight() {
		// FIXME: terrible hack
		return 1.0;
	}
	@Override
	public void setWeight(double weight) {
		// FIXME: terrible hack
	}
}
