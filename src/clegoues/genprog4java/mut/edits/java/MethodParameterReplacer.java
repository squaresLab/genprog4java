package clegoues.genprog4java.mut.edits.java;

import org.eclipse.jdt.core.dom.Expression;

import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.holes.java.ExpHole;
import clegoues.genprog4java.mut.holes.java.JavaLocation;

public class MethodParameterReplacer extends ExpressionReplacer {

	public MethodParameterReplacer(JavaLocation location, EditHole source) {
		super(location, source);
	}
	
	@Override
	public String toString() {		
		ExpHole thisHole = (ExpHole) this.getHoleCode();
		Expression locationExp = (Expression) thisHole.getLocationExp();
		Expression newExpCode = (Expression) thisHole.getCode();
		// FIXME: is it possible to get the method call for this?  Would be nice for debug
		String retval = "ParameterReplacer(" + this.getLocation().getId() + ": ";
		retval += "(" + locationExp.toString() + ") -->";
		retval +=  "(" + newExpCode.toString() + "))";
		return retval;
	}
	
}
