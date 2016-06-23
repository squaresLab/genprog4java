package clegoues.genprog4java.mut.edits.java;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.ExpHole;
import clegoues.genprog4java.mut.holes.java.JavaLocation;

public abstract class ExpressionReplacer extends JavaEditOperation {

	protected ExpressionReplacer(Mutation mutType, JavaLocation location, HashMap<String, EditHole> sources) {
		super(mutType, location, sources);
	}
	
	@Override
	public void edit(ASTRewrite rewriter) {
		JavaStatement locationStmt = (JavaStatement) (this.getLocation().getLocation());
		ExpHole thisHole = (ExpHole) this.getHoleCode("replaceParameter");
		rewriter.replace(thisHole.getHoleParent(), thisHole.getCode(), null); 
	}
	

}
