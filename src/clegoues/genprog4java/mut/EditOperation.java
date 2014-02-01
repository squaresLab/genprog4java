package clegoues.genprog4java.mut;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;


public interface EditOperation<G,R,S> { // FIXME: this is a hack to see if I can get rewriting to work at all

	public Mutation getType();

	public void setType(Mutation type);
	
	
	public G getFixCode();
	
	public void setFixCode(G target);
	public G getLocation();
	public void setLocation(G location);
	
	public void edit(R rewriter, S ast);

}
