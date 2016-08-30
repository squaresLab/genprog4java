package clegoues.genprog4java.mut.edits.java;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.holes.java.ExpChoiceHole;
import clegoues.genprog4java.mut.holes.java.ExpHole;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.holes.java.MethodInfoHole;
import clegoues.genprog4java.mut.holes.java.StatementHole;

public class CasteeMutator extends JavaEditOperation {

	public CasteeMutator(JavaLocation location, EditHole source) {
		super(location, source);
	}

	@Override
	public void edit(ASTRewrite rewriter) {
		//ASTNode locationNode = ((JavaLocation) this.getLocation()).getCodeElement(); 
		ExpHole thisHole = (ExpHole) this.getHoleCode();
		ASTNode toReplace = (ASTNode) thisHole.getCode();
		ASTNode replaceWith = ASTNode.copySubtree(rewriter.getAST(), thisHole.getLocationExp());
		rewriter.replace(toReplace, replaceWith, null);
	}
	
	public String toString() {
		ExpHole thisHole = (ExpHole) this.getHoleCode();
		String retval = "csteeM(" + this.getLocation().getId() + ": ";
		retval += "(" + thisHole.getCode() + ") replaced with ";
		retval +=  "(" + thisHole.getLocationExp() + "))";
		return retval;
	}

}









