package clegoues.genprog4java.rep;

public class History<C> {
	private C location;
	private C secondItem;
	private Mutation mutationType;
	
	public History(C historyItem, Mutation mutationType) {
		this.location = historyItem;
		this.mutationType = mutationType;
	}
	public History(C location, C secondItem, Mutation mutationType) {
		this.location = location;
		this.secondItem = secondItem;
		this.mutationType = mutationType;
	}
	public String toString() {
		throw new UnsupportedOperationException();
	}

}