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

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.tools.SimpleJavaFileObject;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import clegoues.genprog4java.java.ClassInfo;
import clegoues.util.Pair;

public class ASTUtils {

	public static int getLineNumber(ASTNode node) { 
		ASTNode root = node.getRoot();
		int lineno = -1;
		if (root instanceof CompilationUnit) {
			CompilationUnit cu = (CompilationUnit) root;
			lineno = cu.getLineNumber(node.getStartPosition());
		}

		return lineno;
	}


	public static Iterable<JavaSourceFromString> getJavaSourceFromString(
			String progName, List<Pair<ClassInfo, String>> code) {

		ArrayList<JavaSourceFromString> jsfs = new ArrayList<JavaSourceFromString>();
		for (Pair<ClassInfo, String> ele : code) {

			JavaSourceFromString oneSource = new JavaSourceFromString(progName,
					ele.getFirst(), ele.getSecond());
			jsfs.add(oneSource);

		}
		// this originally turned off remove with an unsupported
		// operation exception;
		// do we really need that behavior?

		return jsfs;

	}

	public static List<ASTNode> decomposeASTNode(ASTNode node) {
		final List<ASTNode> decomposed = new LinkedList<ASTNode>();
		// possible FIXME: I arbitrarily removed some ASTNode types here but wasn't very
		// principled about it.
		// one thing I'm trying to do is reduce repetition, so no expression statements (for example)
		node.accept(new ASTVisitor() {
			public boolean visit(ArrayAccess node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(ArrayCreation node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(ArrayInitializer node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(Assignment node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(BooleanLiteral node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(BreakStatement node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(CastExpression node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(CatchClause node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(CharacterLiteral node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(ClassInstanceCreation node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(ConditionalExpression node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(ConstructorInvocation node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(ContinueStatement node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(Dimension node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(DoStatement node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(EmptyStatement node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(EnhancedForStatement node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(FieldAccess node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(FieldDeclaration node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(ForStatement node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(IfStatement node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(InfixExpression node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(Initializer node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(InstanceofExpression node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(MethodInvocation node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(NullLiteral node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(NumberLiteral node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(ParameterizedType node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(ParenthesizedExpression node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(PostfixExpression node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(PrefixExpression node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(PrimitiveType node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(QualifiedName node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(QualifiedType node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(ReturnStatement node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(SimpleName node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(SimpleType node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(SingleVariableDeclaration node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(StringLiteral node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(SuperConstructorInvocation node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(SuperFieldAccess node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(SuperMethodInvocation node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(SwitchCase node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(SwitchStatement node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(ThisExpression node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(ThrowStatement node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(TryStatement node) {
				decomposed.add(node);
				return true;
			}	
			public boolean visit(VariableDeclarationExpression node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(VariableDeclarationFragment node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(VariableDeclarationStatement node) {
				decomposed.add(node);
				return true;
			}
			public boolean visit(WhileStatement node) {
				decomposed.add(node);
				return true;
			}
		});
		return decomposed;
	}
}

class JavaSourceFromString extends SimpleJavaFileObject {
	final String code;

	JavaSourceFromString(String name, ClassInfo classInfo, String code) {
		super(URI.create(classInfo.pathToJavaFile()), Kind.SOURCE);
		this.code = code;
	}

	public CharSequence getCharContent(boolean ignoreEncodingErrors) {
		return code;
	}
}