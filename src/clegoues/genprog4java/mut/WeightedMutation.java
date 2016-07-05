package clegoues.genprog4java.mut;

import clegoues.util.Pair;

public class WeightedMutation extends Pair<Mutation, Double> implements Comparable {

	
	public WeightedMutation(Mutation one, Double two) {
		super();
		this.setFirst(one);
		this.setSecond(two);
	}
	
	@Override
	public int compareTo(Object o) {
		WeightedMutation otherMut = (WeightedMutation) o;
		if(this.getFirst() == otherMut.getFirst()) {
			return this.getSecond().compareTo(otherMut.getSecond());
		}
		return this.getFirst().compareTo(otherMut.getFirst());
	}

}
