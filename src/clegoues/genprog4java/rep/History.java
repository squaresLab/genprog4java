package clegoues.genprog4java.rep;

import clegoues.genprog4java.mut.Mutation;

public class History {
	private int location; // FIXME: possibly: right now using integers to index all code instead of parameterizing things
	private int secondItem;
	private Mutation mutationType;
	
	public History(int historyItem, Mutation mutationType) {
		this.location = historyItem;
		this.mutationType = mutationType;
	}
	public History(int location, int secondItem, Mutation mutationType) {
		this.location = location;
		this.secondItem = secondItem;
		this.mutationType = mutationType;
	}
	public String toString() {
		throw new UnsupportedOperationException();
	}

}