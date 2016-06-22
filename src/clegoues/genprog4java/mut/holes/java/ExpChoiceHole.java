package clegoues.genprog4java.mut.holes.java;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;

public class ExpChoiceHole extends ExpHole {

	public enum Which { LEFT, RIGHT }

	Which which;
	public ExpChoiceHole(String name, ASTNode holeParent, Expression holeCode, int codeBankId, int choice) {
		super(name,holeParent,holeCode,codeBankId);
		if(choice == 0) {
			which = Which.LEFT;
		} else {
			which = Which.RIGHT;
		}
	}
	
	public Which getWhich() {
		return this.which;
	}
	

}
