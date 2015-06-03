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

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import clegoues.genprog4java.java.JavaStatement;

public class JavaEditOperation implements
		EditOperation<JavaStatement, ASTRewrite, AST> {

	private Mutation mutType;
	private JavaStatement location = null;
	private JavaStatement fixCode = null;

	public JavaEditOperation(JavaStatement location, Mutation mutType) {
		this.mutType = mutType;
		this.location = location;
	}

	public JavaEditOperation(Mutation mutType, JavaStatement location,
			JavaStatement fixCode) {
		this.mutType = mutType;
		this.location = location;
		this.fixCode = fixCode;
	}

	@Override
	public Mutation getType() {
		return this.mutType;
	}

	@Override
	public void setType(Mutation type) {
		this.mutType = type;
	}

	public JavaStatement getLocation() {
		return this.location;
	}

	public void setLocation(JavaStatement location) {
		this.location = location;
	}

	public void setFixCode(JavaStatement fixCode) {
		this.fixCode = fixCode;
	}

	public JavaStatement getFixCode() {
		return this.fixCode;
	}

	protected static ListRewrite getListRewriter(ASTNode origin,
			ASTRewrite rewriter) {
		ASTNode parent = origin;
		while (!(parent instanceof Block)) {
			parent = parent.getParent();
		}

		return rewriter.getListRewrite(parent, Block.STATEMENTS_PROPERTY);
	}

	@Override
	public void edit(ASTRewrite rewriter, AST ast) {
		ASTNode locationNode = this.getLocation().getASTNode();

		ASTNode fixCodeNode = null;
		if (this.fixCode != null) {
			fixCodeNode = ASTNode.copySubtree(locationNode.getAST(), this
					.getFixCode().getASTNode());
		}
		switch (this.getType()) {
		case APPEND:
			ListRewrite lrw = getListRewriter(locationNode, rewriter);
			lrw.insertAfter(fixCodeNode, locationNode, null);
			break;
		case REPLACE:
			rewriter.replace(locationNode, fixCodeNode, null);
			break;
		case SWAP:
			rewriter.replace(locationNode, fixCodeNode, null);
			rewriter.replace(this.getFixCode().getASTNode(), ASTNode
					.copySubtree(locationNode.getAST(), this.getLocation()
							.getASTNode()), null);
			break;
		case DELETE:
			rewriter.remove(locationNode, null);
			break;
		case NULLINSERT:
			// TODO:Have to figure this out
			rewriter.remove(locationNode, null);
			break;
		}
	}

}
