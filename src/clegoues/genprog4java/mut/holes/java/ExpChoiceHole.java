package clegoues.genprog4java.mut.holes.java;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;

public class ExpChoiceHole extends ExpHole {


	int choice;
	public ExpChoiceHole(String name, ASTNode holeParent, Expression holeCode, int codeBankId, int choice) {
		super(name,holeParent,holeCode,codeBankId);
		this.choice = choice;

	}
	
	public int getChoice() {
		return this.choice;
	}
	

}
