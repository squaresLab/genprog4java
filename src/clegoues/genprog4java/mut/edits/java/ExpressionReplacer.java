package clegoues.genprog4java.mut.edits.java;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.ExpHole;
import clegoues.genprog4java.mut.holes.java.JavaLocation;

public abstract class ExpressionReplacer extends JavaEditOperation {

	public ExpressionReplacer(JavaLocation location, EditHole source) {
		super(location, source);
	}
	
	protected void replaceExp(ASTRewrite rewriter, Expression replaceWith) {
		rewriter.replace(((ExpHole) this.getHoleCode()).getLocationExp(), replaceWith, null); 

	}
	@Override
	public void edit(ASTRewrite rewriter) {
		ExpHole thisHole = (ExpHole) this.getHoleCode();
		replaceExp(rewriter, (Expression) thisHole.getCode()); 
	}
	

}
