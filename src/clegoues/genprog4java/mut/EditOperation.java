package clegoues.genprog4java.mut;


// FIXME: grabbed exactly from PAR, fix me

public interface EditOperation<G,R> {

	public Mutation getType();

	public void setType(Mutation type);
	
	
	public G getFixCode();
	
	public void setFixCode(G target);
	public G getLocation();
	public void setLocation(G location);
	
	public void edit(R rewriter);
}
