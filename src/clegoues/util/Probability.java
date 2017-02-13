package clegoues.util;

import com.google.common.math.DoubleMath;

import codemining.util.StatsUtil;

/**
 * Simple class for computing with immutable probabilities. Probabilities are
 * internally computed as log-probabilities to maximize precision. Mostly this
 * class exists to enable thinking in terms of "and" and "or" rather than
 * "multiplication" and "addition" and keeping track of logarithms.
 * 
 * @author jonathan
 */
public class Probability {
	/**
	 * Creates a Probability representing the given value between 0 and 1.
	 * 
	 * @param p the probability value to represent
	 * 
	 * @return the requested Probability
	 */
	public static Probability raw( double p ) {
		return new Probability( DoubleMath.log2( p ) );
	}
	
	/**
	 * Creates a Probability representing 2<sup>p</sup>.
	 * 
	 * @param p log base 2 of the probability to represent
	 * 
	 * @return the requested probability
	 */
	public static Probability logProb( double p ) {
		return new Probability( p );
	}
	
	/**
	 * Probability 1.0.
	 */
	public static final Probability TRUE = logProb( 0 );

	/**
	 * Probability 0.0.
	 */
	public static final Probability FALSE = logProb( Double.NEGATIVE_INFINITY );
	
	/**
	 * Creates a probability representing 2<sup>logProb</sup>
	 * 
	 * @param logProb log base 2 of the probability
	 */
	private Probability( double logProb ) {
		disjuncts = new double[] { logProb };
	}
	
	/**
	 * Creates probability representing the disjunction of several
	 * log-probabilities.
	 * 
	 * @param disjuncts the log-probabilities of the clauses in the disjunction
	 */
	private Probability( double[] disjuncts ) {
		this.disjuncts = disjuncts;
	}
	
	/**
	 * Computes and caches the cumulative probability of the disjunction.
	 */
	private void collapse() {
		if ( disjuncts.length == 1 )
			return;
		disjuncts = new double[] {
			StatsUtil.log2SumOfExponentials( disjuncts )
		};
	}

	/**
	 * Returns the value of this probability.
	 * 
	 * @return the value of this probability.
	 */
	public double get() {
		collapse();
		return Math.pow( 2, disjuncts[ 0 ] );
	}
	
	/**
	 * Returns the base-2 log of this probability.
	 * 
	 * @return the base-2 log of this probability.
	 */
	public double getLog() {
		collapse();
		return disjuncts[ 0 ];
	}

	/**
	 * Returns the probability that two independent events both occur.
	 * 
	 * @param that the probability of an independent event to co-occur with this
	 * one.
	 * 
	 * @return the probability that two independent events both occur.
	 */
	public Probability and( Probability that ) {
		this.collapse();
		that.collapse();
		return new Probability( this.disjuncts[ 0 ] + that.disjuncts[ 0 ] );
	}
	
	/**
	 * Returns the probability that one of two mutually exclusive events occur.
	 * 
	 * @param that the probability of an event mutually exclusive with this one.
	 * 
	 * @return the probability that one of two mutually exclusive events occur.
	 */
	public Probability or( Probability that ) {
		int n1 = this.disjuncts.length;
		int n2 = that.disjuncts.length;
		double[] tmp = new double[ n1 + n2 ];
		System.arraycopy( this.disjuncts, 0, tmp, 0, n1 );
		System.arraycopy( that.disjuncts, 0, tmp, n1, n2 );
		return new Probability( tmp );
	}

	private double[] disjuncts;
}
