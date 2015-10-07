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
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.main.ClassInfo;

public class JavaEditOperation implements
		EditOperation<JavaStatement, ASTRewrite, AST> {

	private Mutation mutType;
	private JavaStatement location = null;
	private JavaStatement fixCode = null;
	private ClassInfo fileInfo = null;

	public JavaEditOperation(ClassInfo fileName, JavaStatement location, Mutation mutType) {
		this.mutType = mutType;
		this.location = location;
		this.fileInfo = fileName;
	}

	public JavaEditOperation(Mutation mutType, ClassInfo fileName, JavaStatement location,
			JavaStatement fixCode) {
		this.mutType = mutType;
		this.location = location;
		this.fixCode = fixCode;
		this.fileInfo = fileName;
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

	public ClassInfo getFileInfo() {
		return this.fileInfo;
	}
	
	public void setFileInfo(ClassInfo newFileName){
		fileInfo = newFileName;
	}

	/*protected static ListRewrite getListRewriter(ASTNode origin, ASTNode fix, ASTRewrite rewriter) {
		ASTNode parent = origin;
		
		//while (!(parent instanceof Block)) {
		//	parent = parent.getParent();
		//}
		
		
		//make a new statement with the append (probably a block), and replace the origin in the parent for this new one
		Block newNode = origin.getAST().newBlock();
		ASTNode stm1 = (Statement)origin;
		if(origin instanceof Statement){
			stm1 = ASTNode.copySubtree(origin.getAST(), stm1);
			newNode.statements().add(stm1);
		}
		ASTNode stm2 = (Statement)fix;
		if(origin instanceof Statement){
			stm2 = ASTNode.copySubtree(fix.getAST(), stm2);
			newNode.statements().add(stm2);
		}
		
		rewriter.replace(origin, newNode, null);
		
		
		return rewriter.getListRewrite(parent, Block.STATEMENTS_PROPERTY);
	}*/

	@Override
	public void edit(ASTRewrite rewriter, AST ast, CompilationUnit cu) {
		ASTNode locationNode = this.getLocation().getASTNode();

		ASTNode fixCodeNode = null;
		if (this.fixCode != null) {
			fixCodeNode = ASTNode.copySubtree(locationNode.getAST(), this
					.getFixCode().getASTNode());
		}
		switch (this.getType()) {
		case APPEND:
			
			//ASTNode newNode = ASTNode.copySubtree(locationNode.getAST(), this.getFixCode().getASTNode());
			
			//ListRewrite lrw = getListRewriter(locationNode, fixCodeNode, rewriter);
			//lrw.insertAfter(fixCodeNode, locationNode, null);
			
			
			//make a new statement with the append (probably a block), and replace the origin in the parent for this new one
			////ASTNode newStatement;
			//newStatement = ListRewrite.createCopyTarget(locationNode, fixCodeNode);
			////ASTNode[] targetNodes = {locationNode, fixCodeNode};
			////newStatement = rewriter.createGroupNode(targetNodes) ;
			////rewriter.replace(locationNode, newStatement, null);
			
			
			//ImportDeclaration id = ast.newImportDeclaration();
			//id.setName(ast.newName(new String[] {"java", "util", "Set"}));
			//ListRewrite lrw = rewriter.getListRewrite(cu, CompilationUnit.IMPORTS_PROPERTY);
			//lrw.insertAfter(id, locationNode, null);
			
			//ASTNode newNode;
			//Block newStatement = locationNode.getAST().newBlock();
			//newStatement.statements().add((Statement)locationNode);
			//newStatement.statements().add(fixCodeNode);
			//newNode = ASTNode.copySubtree(locationNode.getAST(), newStatement.getASTNode());
			//rewriter.replace(locationNode, newStatement, null);
			//locationNode.getParent().setChild();
			
			
			
			//ListRewrite lrw = rewriter.getListRewrite(locationNode, Block.STATEMENTS_PROPERTY);
			//lrw.insertAfter(fixCodeNode, locationNode, null);
			
			
			Block newNode = locationNode.getAST().newBlock(); 
			ASTNode stm1 = (Statement)locationNode;
			if(locationNode instanceof Statement){
				stm1 = ASTNode.copySubtree(locationNode.getAST(), stm1);
				newNode.statements().add(stm1);
			}
			ASTNode stm2 = (Statement)fixCodeNode;
			if(fixCodeNode instanceof Statement){
				stm2 = ASTNode.copySubtree(fixCodeNode.getAST(), stm2);
				newNode.statements().add(stm2);
			}
			
			rewriter.replace(locationNode, newNode, null);
			

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
			//This is the same as delete, what is it supposed to be?
			rewriter.remove(locationNode, null);
			break;
			
			//Implement All These
		case CASTCHECK:
			
 			break;
		case EXPADD:
			
			break;
		case EXPREM:
			
			break;
		case EXPREP:
			
			break;
		case FUNREP:
			
			break;
		case NULLCHECK:
			if(canAddNullCheck(locationNode)){
				addNullCheck(rewriter,locationNode);
			}
			
			break;
		case OBJINIT:
			
			break;
		case PARADD:
			
			break;
		case PARREM:
			
			break;
		case PARREP:
			
			break;
		case RANGECHECK:
			
			break;
		case SIZECHECK:

			break;
		default:
			break;
			
			
		}
	}
	
	private boolean canAddNullCheck(ASTNode nodeToCheck){
		
	}

	private void addNullCheck(ASTRewrite rewriter, ASTNode nodeToCheck){
		
	}



}
