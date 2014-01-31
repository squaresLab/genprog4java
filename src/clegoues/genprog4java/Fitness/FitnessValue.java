package clegoues.genprog4java.Fitness;

public class FitnessValue {
	private String testClassName = null;
	private int numberTests = 0;
	private int numTestsPassed = 0;
	private int numTestsFailed = 0;
	private boolean allPassed = false; 
	
	public FitnessValue() { }
	
	public FitnessValue(String name, int numT, int numP, int numF, boolean allPassed) {
		this.setTestClassName(name);
		this.setNumberTests(numT);
		this.setNumTestsFailed(numF);
		this.setNumTestsPassed(numP);
		this.setAllPassed(allPassed);
	}
	public String getTestClassName() {
		return testClassName;
	}
	public void setTestClassName(String testClassName) {
		this.testClassName = testClassName;
	}
	public int getNumberTests() {
		return numberTests;
	}
	public void setNumberTests(int numberTests) {
		this.numberTests = numberTests;
	}
	public int getNumTestsPassed() {
		return numTestsPassed;
	}
	public void setNumTestsPassed(int numTestsPassed) {
		this.numTestsPassed = numTestsPassed;
	}

	public boolean isAllPassed() {
		return allPassed;
	}

	public void setAllPassed(boolean allPassed) {
		this.allPassed = allPassed;
	}

	public int getNumTestsFailed() {
		return numTestsFailed;
	}

	public void setNumTestsFailed(int numTestsFailed) {
		this.numTestsFailed = numTestsFailed;
	}

	
}
