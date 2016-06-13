package clegoues.genprog4java.mut;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.main.ClassInfo;

public class JavaAppendOperation extends JavaEditOperation {
	
	public JavaAppendOperation(ClassInfo fileName, JavaStatement location,
			JavaStatement fixCode) {
		super(Mutation.APPEND, fileName, location, fixCode);
	}
	

	@Override
	public void edit(final ASTRewrite rewriter, AST ast, CompilationUnit cu) {
		ASTNode locationNode = this.getLocation().getASTNode();
		ASTNode fixCodeNode =
			 ASTNode.copySubtree(locationNode.getAST(), this
					.getFixCode().getASTNode());

		Block newNode = locationNode.getAST().newBlock(); 
		ASTNode stm1 = (Statement)locationNode;
		if(locationNode instanceof Statement){
			stm1 = ASTNode.copySubtree(locationNode.getAST(), stm1);
			newNode.statements().add(stm1);
		}
		ASTNode stm2 = (Statement)fixCodeNode;
		if(fixCodeNode instanceof Statement){
			stm2 = ASTNode.copySubtree(fixCodeNode.getAST(), stm2);
			newNode.statements().add(stm2);
		}

		rewriter.replace(locationNode, newNode, null);
	}
}
