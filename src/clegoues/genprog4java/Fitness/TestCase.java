package clegoues.genprog4java.Fitness;


enum TestType { POSITIVE, NEGATIVE };

public class TestCase {
	private TestType posOrNeg;
	private int testNum;
	public String toString () {
		if(posOrNeg == TestType.POSITIVE) {
			return "p" + this.testNum;
		} else {
			return "n" + this.testNum;
		}
	}
	
}
