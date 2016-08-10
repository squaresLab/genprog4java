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

package clegoues.genprog4java.fitness;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/** 
 * Tracks basic information about a test case; primarily useful
 * for when we're using a test model, otherwise we could probably
 * get away with just using strings to track test class names
 * (but since it makes clegoues crazy when strings are used as 
 * generic data, we probably wouldn't).
 * @author clegoues
 *
 */
public class TestCase implements Comparable<TestCase>, Serializable {

	private static final long serialVersionUID = 6781231573789815806L;
	private final TestType posOrNeg;
	private final String testName;
	private int numPatchesKilled = 0;

	public enum TestType {
		POSITIVE, NEGATIVE
	}
	
	public TestCase(TestType t, String name) {
		this.posOrNeg = t;
		this.testName = name; 
		this.numPatchesKilled = 0;
	}
	public String getTestName() {
		return testName;
	}
	
	public String toString () {
		return this.testName;
	}

	public void incrementPatchesKilled() {
		this.numPatchesKilled++;
	}

	public TestType getPosOrNeg() {
		return posOrNeg;
	}

	@Override
	public int compareTo(TestCase o) {
		if(this.numPatchesKilled == o.numPatchesKilled) {
			return this.testName.compareTo(o.testName);
		} // FIXME: possibly also consider type?
		return this.numPatchesKilled - o.numPatchesKilled;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(testName).toHashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof TestCase) {
			TestCase other = (TestCase) obj;
			return new EqualsBuilder().append(this.testName, other.getTestName()).isEquals();
		}
		return false;
	}

}
