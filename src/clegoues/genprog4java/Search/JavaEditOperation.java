package clegoues.genprog4java.Search;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.mut.EditOperation;
import clegoues.genprog4java.mut.Mutation;

public class JavaEditOperation implements EditOperation<JavaStatement,ASTRewrite> {

	private Mutation mutType;
	private JavaStatement location = null;
	private JavaStatement fixCode = null;

	public JavaEditOperation(JavaStatement location) {
		this.mutType = Mutation.DELETE;
		this.location = location;
	}
	public JavaEditOperation(Mutation mutType, JavaStatement location, JavaStatement fixCode) {
		this.mutType = mutType;
		this.location = location;
		this.fixCode = fixCode;
	}
	@Override
	public Mutation getType() {
		return this.mutType;
	}

	@Override
	public void setType(Mutation type) {
		this.mutType = type;
	}

	public JavaStatement getLocation() {
		return this.location;
	}
	public void setLocation(JavaStatement location) {
		this.location = location;
	}
	public void setFixCode(JavaStatement fixCode) {
		this.fixCode = fixCode;
	}
	public JavaStatement getFixCode() {
		return this.fixCode;
	}

	protected static ListRewrite getListRewriter(ASTNode origin, ASTRewrite rewriter)
	{
		ASTNode parent = origin.getParent();

		while(!(parent instanceof Block))
		{
			parent = parent.getParent(); // FIXME: need to understand why this is a Thing
		}

		return rewriter.getListRewrite(parent, Block.STATEMENTS_PROPERTY);
	}

	@Override
	public void edit(ASTRewrite rewriter) {
		ListRewrite lrw = getListRewriter(this.getLocation().getASTNode(), rewriter);
		ASTNode locationNode = this.getLocation().getASTNode();
		switch(this.getType())
		{
		case APPEND:
			lrw.insertAfter(locationNode, this.getFixCode().getASTNode(), null); 
			break;
		case REPLACE:
			lrw.replace(locationNode, this.getFixCode().getASTNode(), null); 
			break;
		case SWAP: throw new UnsupportedOperationException() ; // FIXME
		case DELETE:
			lrw.remove(locationNode, null);
			break;
		}
	}

}
