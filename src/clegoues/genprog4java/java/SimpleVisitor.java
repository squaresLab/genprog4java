package clegoues.genprog4java.java;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class SimpleVisitor extends ASTVisitor {
	private CompilationUnit cu;
	public CompilationUnit getCompilationUnit() {
		return cu;
	}

	public void setCompilationUnit(CompilationUnit cu) {
		this.cu = cu;
	}
	
	public void finalizeVisit() {
		// do nothing, subclasses may override
	}
	
	public List<ASTNode> getStatements() {
		return null; // hack so that javaparser is usable for both genprog and treelm code
	}
}
