package clegoues.genprog4java.util;

public class Pair<A,B> {
	private A one;
	private B two;
	public Pair(A one, B two) {
		this.one = one;
		this.two = two;
	}
	public A getFirst() { return one; }
	public B getSecond () { return two; }
}
