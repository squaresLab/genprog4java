package clegoues.genprog4java.mut.edits.java;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.Expression;

import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.ExpHole;
import clegoues.genprog4java.mut.holes.java.JavaLocation;

public class ExpressionModRep extends MethodParameterReplacer {

	
	public ExpressionModRep(JavaLocation location,  HashMap<String, EditHole> sources) {
		super(Mutation.EXPREP, location, sources);
	}
	
	@Override
	public String toString() {
		ExpHole thisHole = (ExpHole) this.getHoleCode("replaceParameter");
		Expression parentExp = (Expression) thisHole.getHoleParent();
		Expression newExpCode = (Expression) thisHole.getCode();

		String retval = "er(" + this.getLocation().getId() + ": ";
		retval += "(" + parentExp.toString() + ") -->";
		retval +=  "(" + newExpCode.toString() + "))";
		return retval;
	}
	
}
