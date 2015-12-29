package clegoues.genprog4java.mut.edits.java;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.JavaHole;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.EditHole;


public class JavaAppendOperation extends JavaEditOperation {
	
	public JavaAppendOperation(JavaLocation location, HashMap<String,EditHole> fixCode) {
		super(Mutation.APPEND, location, fixCode);
	}

	public void edit(final ASTRewrite rewriter) {
		ASTNode locationNode = this.getLocationNode(); 
		JavaHole fixHole = (JavaHole) this.getHoleCode("singleHole");
		ASTNode fixCodeNode =
			 ASTNode.copySubtree(locationNode.getAST(), fixHole.getCode()); 

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
