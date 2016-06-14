package clegoues.genprog4java.mut.holes.java;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

import clegoues.genprog4java.mut.EditHole;

public class SubExpsHole extends SimpleJavaHole implements EditHole<ASTNode> {
	private ASTNode holeParent;
	private List<ASTNode> subExps;
	
	public SubExpsHole(String name, ASTNode holeParent, List<ASTNode> subExps) {
		this.setHoleParent(holeParent);
		this.setSubExps(subExps);
	}

	public ASTNode getHoleParent() {
		return holeParent;
	}

	public void setHoleParent(ASTNode holeParent) {
		this.holeParent = holeParent;
	}

	public List<ASTNode> getSubExps() {
		return subExps;
	}

	public void setSubExps(List<ASTNode> subExps) {
		this.subExps = subExps;
	}

}
