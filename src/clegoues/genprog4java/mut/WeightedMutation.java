package clegoues.genprog4java.mut;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.tuple.Pair;

public class WeightedMutation extends Pair<Mutation, Double> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8093281192263809575L;
	private Mutation mut;
	private double weight;
	
	public WeightedMutation(Mutation one, Double two) {
		this.mut  = one;
		this.weight = two;
	}
	

	@Override
	public Double setValue(Double value) {
		Double oldVal = weight;
		this.weight = value;
		return oldVal;

	}

	@Override
	public Mutation getLeft() {
		return mut;
	}

	@Override
	public Double getRight() {
		return weight;
	}
}
