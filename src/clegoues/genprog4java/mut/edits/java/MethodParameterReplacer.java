package clegoues.genprog4java.mut.edits.java;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.ExpHole;
import clegoues.genprog4java.mut.holes.java.JavaLocation;

public class MethodParameterReplacer extends ExpressionReplacer {

	public MethodParameterReplacer(JavaLocation location,  HashMap<String, EditHole> sources) {
		super(Mutation.PARREP, location, sources);
		this.holeNames.add("replaceParameter");
	}

	@Override
	public String toString() {		
		ExpHole thisHole = (ExpHole) this.getHoleCode("replaceParameter");
		Expression parentExp = (Expression) thisHole.getHoleParent();
		Expression newExpCode = (Expression) thisHole.getCode();
		// FIXME: is it possible to get the method call for this?  Would be nice for debug
		String retval = "mpr(" + this.getLocation().getId() + ": ";
		retval += "(" + parentExp.toString() + ") -->";
		retval +=  "(" + newExpCode.toString() + "))";
		return retval;
	}
	
}
/*
 * [Parameter Replacer]
P = program
B = fault location

<AST Analysis> 
M <- collect a method call of B in P 

<Context Check>
if there is any parameter in M -> continue
otherwise -> stop 

<Program Editing>
TargetParam <- select a parameter in M

I <- collect all method calls in the same scope of TargetParam in P
I_selected <- select a method call which has at least one parameter whose type is compatible with                            TargetParam

SourceParam <- select a parameter of I_selected, which has a compatible type with TargetParam

replace TargetParam by SourceParam
 */