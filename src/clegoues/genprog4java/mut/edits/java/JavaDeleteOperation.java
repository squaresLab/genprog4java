package clegoues.genprog4java.mut.edits.java;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.main.ClassInfo;
import clegoues.genprog4java.mut.Location;
import clegoues.genprog4java.mut.Mutation;

public class JavaDeleteOperation extends JavaEditOperation {

	public JavaDeleteOperation(ClassInfo fileName, JavaStatement location) {
		super(Mutation.DELETE, fileName,  location);
	}
	
	@Override
	public void edit(final ASTRewrite rewriter) {
		ASTNode locationNode = this.getLocationNode(); 
		rewriter.remove(locationNode, null);
	}
}

