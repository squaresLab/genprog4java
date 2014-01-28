package clegoues.genprog4java.util;

public class Pair<A,B> {
	private A one;
	private B two;
	public Pair() {
		
	}
	public Pair(A one, B two) {
		this.one = one;
		this.two = two;
	}
	public A getFirst() { return one; }
	public B getSecond () { return two; }
	public void setFirst(A first) { this.one = first; }
	public void setSecond(B second) { this.two = second; }

	
}
