package clegoues.genprog4java.Fitness;


public class TestCase {
	private TestType posOrNeg;
	private int testNum;
	public TestCase(TestType t, int num) {
		this.posOrNeg = t;
		this.testNum = num;
	}
	public String toString () {
		if(posOrNeg == TestType.POSITIVE) {
			return "p" + this.testNum;
		} else {
			return "n" + this.testNum;
		}
	}
	
}
