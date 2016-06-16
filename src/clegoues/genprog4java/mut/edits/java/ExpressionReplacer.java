package clegoues.genprog4java.mut.edits.java;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.JavaLocation;

public class ExpressionReplacer extends JavaEditOperation {

	
	public ExpressionReplacer(JavaLocation location,  HashMap<String, EditHole> sources) {
		super(Mutation.EXPREP, location, sources);
		this.holeNames.add("expReplace");
	}

	@Override
	public void edit(ASTRewrite rewriter) {
		// TODO Auto-generated method stub
		
	}

}
