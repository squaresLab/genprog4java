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

package clegoues.genprog4java.mut;

import java.util.List;

public class HistoryEle {
	private Mutation mtype = null;
	private Location location;
	private List<EditHole> fixCode;
	public HistoryEle() { }
	public HistoryEle(Mutation m, Location location) {
		this.mtype = m;
		this.location = location;
	}
	public HistoryEle(Mutation m, Location loc, List<EditHole> fixes) {
		this.mtype = m;
		this.location = loc;
		this.fixCode = fixes;
	}
	public Mutation getMtype() {
		return mtype;
	}
	public void setMtype(Mutation mtype) {
		this.mtype = mtype;
	}
	public Location getLocation() {
		return location;
	}
	public void setLocation(Location location) {
		this.location = location;
	}
	public List<EditHole> getFixCode() {
		return fixCode;
	}
	public void setFixCode(List<EditHole> fixCode) {
		this.fixCode = fixCode;
	}
	public String toString() { // FIXME: this won't work for lists of edit code!
		String result = "";
		switch(this.mtype) {
		case APPEND:
			result += "a(" + this.location + "," + this.fixCode + ")";
			break;
		case DELETE:     
			result += "d(" + this.location + ")";
			break;
		case SWAP:       
			result += "s(" + this.location + "," + this.fixCode + ")";
			break;
		case REPLACE:    
			result += "r(" + this.location + "," + this.fixCode + ")";
			break;
		case NULLINSERT: 
			result += "n(" + this.location + ")";
			break;
		case FUNREP: 	 
			result += "fr(" + this.location + ")";
			break;
		case CASTCHECK:  
			result += "cc(" + this.location + ")";
			break;
		case EXPADD:     
			result += "ea(" + this.location + ")";
			break;
		case EXPREM:     
			result += "erm(" + this.location + ")";
			break;
		case EXPREP:     
			result += "erp(" + this.location + ")";
			break;
		case NULLCHECK:  
			result += "nc(" + this.location + ")";
			break;
		case OBJINIT:	 
			result += "oi(" + this.location + ")";
			break;
		case PARADD:	 
			result += "pa(" + this.location + ")";
			break;
		case PARREM:	 
			result += "prm(" + this.location + ")";
			break;
		case PARREP:	 
			result += "prp(" + this.location + ")";
			break;
		case RANGECHECK: 
			result += "rc(" + this.location + ")";
			break;
		case SIZECHECK:	 
			result += "sc(" + this.location + ")";
			break;
		case LBOUNDSET:	 
			result += "lbs(" + this.location + ")";
			break;
		case UBOUNDSET:	 
			result += "ubs(" + this.location + ")";
			break;	
		case OFFBYONE:
			result += "obo(" + this.location + ")";
			break;
		default:
			break;
		}
		return result;
	}

}
