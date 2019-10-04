package clegoues.genprog4java.mut.edits.java;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.StatementHole;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.EditHole;


public class JavaAppendOperation extends JavaEditOperation {


	public JavaAppendOperation(JavaLocation location, EditHole source) {
		super(location,source);
	}
	
	public void edit(final ASTRewrite rewriter) {
		ASTNode locationNode = ((JavaLocation) this.getLocation()).getCodeElement(); 
		StatementHole fixHole = (StatementHole) this.getHoleCode();
		ASTNode fixCodeNode =
			 ASTNode.copySubtree(rewriter.getAST(), fixHole.getCode()); 

		Block newNode = locationNode.getAST().newBlock(); 
		if(locationNode instanceof Statement && fixCodeNode instanceof Statement){
			ASTNode stm1 = locationNode;
			ASTNode stm2 = fixCodeNode;

			stm1 = ASTNode.copySubtree(locationNode.getAST(), stm1);
			stm2 = ASTNode.copySubtree(fixCodeNode.getAST(), stm2);

			newNode.statements().add(stm1);
			newNode.statements().add(stm2);
			rewriter.replace(locationNode, newNode, null);
		}

	}
	
	@Override
	public String toString() {
		StatementHole fixHole = (StatementHole) this.getHoleCode();
		return "StmtAppend(" + this.getLocation().getId() + "," + fixHole.getCodeBankId() + ")";
	}
}
