package clegoues.genprog4java.mut.edits.java;

import java.util.HashMap;


import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.JavaLocation;

public class ExpressionReplacer extends MethodParameterReplacer {

	
	public ExpressionReplacer(JavaLocation location,  HashMap<String, EditHole> sources) {
		super(Mutation.EXPREP, location, sources);
	}
}
