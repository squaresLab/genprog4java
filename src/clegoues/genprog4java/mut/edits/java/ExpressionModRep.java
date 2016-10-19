package clegoues.genprog4java.mut.edits.java;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.Expression;

import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.ExpHole;
import clegoues.genprog4java.mut.holes.java.JavaLocation;

public class ExpressionModRep extends ExpressionReplacer {
	
	public ExpressionModRep(JavaLocation location, EditHole source) {
		super(location, source);
	}
	@Override
	public String toString() {
		ExpHole thisHole = (ExpHole) this.getHoleCode();
		Expression locationExp = (Expression) thisHole.getLocationExp();
		Expression newExpCode = (Expression) thisHole.getCode();

		String retval = "er(" + this.getLocation().getId() + ": ";
		retval += "(" + locationExp.toString() + ") -->";
		retval +=  "(" + newExpCode.toString() + "))";
		return retval;
	}
	
}
