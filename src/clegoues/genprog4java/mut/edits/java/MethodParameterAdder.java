package clegoues.genprog4java.mut.edits.java;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.JavaLocation;

public class MethodParameterAdder extends JavaEditOperation {
	public MethodParameterAdder(JavaLocation location,  HashMap<String, EditHole> sources) {
		super(Mutation.PARADD, location, sources);
		this.holeNames.add("addParameter");
	}

	@Override
	public void edit(ASTRewrite rewriter) {
		// TODO Auto-generated method stub
		
	}

}

