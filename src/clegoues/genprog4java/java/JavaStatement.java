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

package clegoues.genprog4java.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;

public class JavaStatement {

	private ASTNode astNode;
	private int lineno;
	private int stmtId; // unique
	private Set<String> names;
	private Set<String> types;

	private Set<String> mustBeInScope;

	public void setStmtId(int id) {
		this.stmtId = id;
	}

	public int getStmtId() {
		return this.stmtId;
	}

	public ASTNode getASTNode() {
		return astNode;
	}

	public void setASTNode(ASTNode node) {
		this.astNode = node;
	}

	public int getLineno() {
		return lineno;
	}

	public void setLineno(int lineno) {
		this.lineno = lineno;
	}

	public Set<String> getNames() {
		return names;
	}

	public void setNames(Set<String> names) {
		this.names = names;
	}

	public Set<String> getTypes() {
		return types;
	}

	public void setTypes(Set<String> types) {
		this.types = types;
	}

	public Set<String> getRequiredNames() {
		return mustBeInScope;
	}

	public void setRequiredNames(Set<String> scopes) {
		this.mustBeInScope = scopes;
	}

	public String toString() {
		if (astNode != null)
			return this.astNode.toString();
		else
			return "null";
	}
	// method to get the parent of an ASTnode. We traverse the ast upwards until the parent node is an instance of statement
	// if statement is(are) added to this parent node
	private ASTNode getParent(ASTNode node) {
		ASTNode parent = node.getParent();
		while(!(parent instanceof Statement)){
			parent = parent.getParent();
		}
		return parent;
	}

	/******* Cached information for applicability of various mutations/templates ******/
	private Map<ASTNode, List<ASTNode>> arrayAccesses = null; // to track the parent nodes of array access nodes

	public boolean containsArrayAccesses() {
		if(arrayAccesses == null) {
			arrayAccesses =  new HashMap<ASTNode, List<ASTNode>>();
			this.getASTNode().accept(new ASTVisitor() {
				// method to visit all ArrayAccess nodes in locationNode and store their parents
				public boolean visit(ArrayAccess node) {
					ASTNode parent = getParent(node);
					if(!arrayAccesses.containsKey(parent)){
						List<ASTNode> arraynodes = new ArrayList<ASTNode>();
						arraynodes.add(node);
						arrayAccesses.put(parent, arraynodes);		
					}else{
						List<ASTNode> arraynodes = (List<ASTNode>) arrayAccesses.get(parent);
						if(!arraynodes.contains(node))
							arraynodes.add(node);
						arrayAccesses.put(parent, arraynodes);	
					}

					return true;
				}
			});

		}
		return this.arrayAccesses.size() > 0;
	}

	// DOES NOT CHECK that it isn't null; precondition is that the previous function was called!
	public Map<ASTNode, List<ASTNode>> getArrayAccesses() {
		return this.arrayAccesses;
	}


	private Map<ASTNode, List<ASTNode>> nullCheckable = null;

	public boolean nullCheckApplies() {
		if(nullCheckable == null) {
			nullCheckable = new HashMap<ASTNode, List<ASTNode>>();
			if(this.getASTNode() instanceof MethodInvocation 
					|| this.getASTNode() instanceof ExpressionStatement 
					|| this.getASTNode() instanceof ReturnStatement ){

				this.getASTNode().accept(new ASTVisitor() {
					// method to visit all Expressions relevant for this in locationNode and
					// store their parents
					public boolean visit(MethodInvocation node) {
						saveDataOfTheExpression(node);

						return true;
					}
					public boolean visit(FieldAccess node) {
						saveDataOfTheExpression(node);
						return true;
					}

					public boolean visit(QualifiedName node) {
						saveDataOfTheExpression(node);
						return true;
					}

					public void saveDataOfTheExpression(ASTNode node){
						ASTNode parent = getParent(node);
						if (!nullCheckable.containsKey(parent)) {
							List<ASTNode> thisList = new ArrayList<ASTNode>();
							thisList.add(node);
							nullCheckable.put(parent, thisList);
						} else {
							List<ASTNode> thisList = (List<ASTNode>) nullCheckable
									.get(parent);
							if (!thisList.contains(node))
								thisList.add(node);
							nullCheckable.put(parent, thisList);
						}
					}
				});

			}
		}
		return nullCheckable.size() > 0;
	}
	// DOES NOT CHECK that it isn't null; precondition is that the previous function was called!
	public Map<ASTNode, List<ASTNode>> getNullCheckables() {
		return this.nullCheckable;
	}


}
