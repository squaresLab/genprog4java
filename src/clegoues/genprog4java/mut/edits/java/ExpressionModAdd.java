package clegoues.genprog4java.mut.edits.java;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.java.MethodInfo;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.ExpHole;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.holes.java.MethodInfoHole;

public class ExpressionModAdd extends JavaEditOperation {
	
	public ExpressionModAdd(JavaLocation location,  HashMap<String, EditHole> sources) {
		super(Mutation.EXPADD, location, sources);
		this.holeNames.add("condExpAdd");

	}

	@Override
	public void edit(final ASTRewrite rewriter) {
		JavaStatement locationStmt = (JavaStatement) (this.getLocation().getLocation());
		ASTNode locationNode = locationStmt.getASTNode();
		ExpHole thisHole = (ExpHole) this.getHoleCode("condExpAdd");
		ASTNode toReplace = thisHole.getCode();
		
	}
}
