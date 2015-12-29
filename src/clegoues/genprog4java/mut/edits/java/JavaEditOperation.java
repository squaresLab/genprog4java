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

package clegoues.genprog4java.mut.edits.java;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.EditOperation;
import clegoues.genprog4java.mut.Location;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.JavaLocation;

public abstract class JavaEditOperation implements
EditOperation<ASTRewrite> {

	private Mutation mutType;
	// I don't think the location abstraction is strictly necessary functionally, but conceptually it makes
	// sense to me to keep it (because edits apply to a location; it's important).
	// Alternatively, make it a hole? 
	private Location<JavaStatement> location = null;
	private ArrayList<String> holeNames = null;
	private HashMap<String,EditHole> holeCode = new HashMap<String,EditHole>();

	public JavaEditOperation(Mutation mutType, JavaLocation location) {
		this.mutType = mutType;
		this.location = location;
	}
	
	public Location getLocation() {
		return this.location;
	}

	protected JavaEditOperation(Mutation mutType, JavaLocation location, List<EditHole> sources) {
		this.mutType = mutType;
		this.location = location;
		for(EditHole source : sources) {
			this.holeCode.put(source.getName(), source);
			this.holeNames.add(source.getName()) ;
		}
	}

	@Override
	public List<String> getHoles() {
		return this.holeNames;
	}
	
	@Override
	public Mutation getType() {
		return this.mutType;
	}

	@Override
	public void setType(Mutation type) {
		this.mutType = type;
	}

	protected ASTNode getLocationNode() {
		JavaStatement actualLocation = this.location.getLocation();
		return actualLocation.getASTNode();
	}

	public void setHoleCode(String name, EditHole fixCode) {
		this.holeCode.put(name, fixCode);
	}

	public EditHole getHoleCode(String name) {
		return this.holeCode.get(name);
	}

}
