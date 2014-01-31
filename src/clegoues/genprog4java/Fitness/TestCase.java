package clegoues.genprog4java.Fitness;


public class TestCase {
	private TestType posOrNeg;
	private int testNum = -1;
	private String testName = null;
	public TestCase(TestType t, int num) {
		this.posOrNeg = t;
		this.testNum = num;
	}
	public TestCase(TestType t, String name) {
		this.posOrNeg = t;
		this.testName = name; 
	}
	public String toString () {
		if(this.testName != null)
			return this.testName;
		if(posOrNeg == TestType.POSITIVE) {
			return "p" + this.testNum;
		} else {
			return "n" + this.testNum;
		}
	}
	
}
