package clegoues.genprog4java.mut;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import clegoues.util.Pair;

public class WeightedMutation extends Pair<Mutation, Double> implements Comparable<Object> {

	
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

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof WeightedMutation) {
			WeightedMutation other = (WeightedMutation) obj;
			return new EqualsBuilder()
					.append(this.getFirst(), other.getFirst())
					.append(this.getSecond(), other.getSecond())
					.isEquals();
		}
		return false;
	}
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(this.getFirst())
				.append(this.getSecond())
				.toHashCode();
	}
}
