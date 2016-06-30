package clegoues.genprog4java.mut.edits.java;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.JavaLocation;

public class MethodParameterAdder extends JavaEditOperation {


	public MethodParameterAdder(JavaLocation location, EditHole source) {
		super(location, source);
	}
	
	@Override
	public void edit(ASTRewrite rewriter) {
		
	}

	@Override
	public String toString() {
		// FIXME: this is lazy
		return "pa(" + this.getLocation().getId() + ")";
	}
	
}

