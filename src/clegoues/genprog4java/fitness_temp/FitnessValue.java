/*
 * Copyright (c) 2014-2015, 
 *  Claire Le Goues     <clegoues@cs.cmu.edu>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. The names of the contributors may not be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package clegoues.genprog4java.fitness_temp;

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
