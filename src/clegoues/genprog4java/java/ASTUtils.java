package clegoues.genprog4java.java;

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


import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.tools.SimpleJavaFileObject;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
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
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
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
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;

/** provides static utils for manipulating java ASTs.  Used to be much longer
 * before I refactored semantic check info; it may be possible to refactor this away
 * at some point.
 * @author clegoues
 *
 */
public class ASTUtils {

	public static ASTNode getEnclosingMethod(ASTNode node) {
		ASTNode parent = node.getParent();
		while(parent != null && !(parent instanceof MethodDeclaration)){
			parent = parent.getParent();
		}
		return parent;
	}

	/** 
	 * @param node ASTNode of interest
	 * @return line number corresponding to the first line of the node in its CU. 
	 * Note that ASTNodes can span multiple lines; this returns the first.
	 */
	public static int getLineNumber(ASTNode node) { 
		ASTNode root = node.getRoot();
		int lineno = -1;
		if (root instanceof CompilationUnit) {
			CompilationUnit cu = (CompilationUnit) root;
			lineno = cu.getLineNumber(node.getStartPosition());
		}

		return lineno;
	}


	/**
	 * parses/creates java code from a string, rather than a file on disk.
	 * @param progName
	 * @param code 
	 * @return collection of objects containing the java code. 
	 */
	public static Iterable<JavaSourceFromString> getJavaSourceFromString(
			String progName, List<Pair<ClassInfo, String>> code) {

		ArrayList<JavaSourceFromString> jsfs = new ArrayList<JavaSourceFromString>();
		for (Pair<ClassInfo, String> ele : code) {

			JavaSourceFromString oneSource = new JavaSourceFromString(progName,
					ele.getLeft(), ele.getRight());
			jsfs.add(oneSource);

		}
		return jsfs;

	}

	/** create a new Type object from a typeBinding, a hilariously
	 * difficult thing to do.
	 * @param ast
	 * @param typeBinding
	 * @return
	 */
	public static Type typeFromBinding(AST ast, ITypeBinding typeBinding) {
		if( ast == null ) 
			throw new NullPointerException("ast is null");
		if( typeBinding == null )
			throw new NullPointerException("typeBinding is null");

		if( typeBinding.isPrimitive() ) {
			return ast.newPrimitiveType(
					PrimitiveType.toCode(typeBinding.getName()));
		}

		if( typeBinding.isCapture() ) {
			ITypeBinding wildCard = typeBinding.getWildcard();
			WildcardType capType = ast.newWildcardType();
			ITypeBinding bound = wildCard.getBound();
			if( bound != null ) {
				capType.setBound(typeFromBinding(ast, bound),
						wildCard.isUpperbound());
			}
			return capType;
		}

		if( typeBinding.isArray() ) {
			Type elType = typeFromBinding(ast, typeBinding.getElementType());
			return ast.newArrayType(elType, typeBinding.getDimensions());
		}

		if( typeBinding.isParameterizedType() ) {
			ParameterizedType type = ast.newParameterizedType(
					typeFromBinding(ast, typeBinding.getErasure()));

			@SuppressWarnings("unchecked")
			List<Type> newTypeArgs = type.typeArguments();
			for( ITypeBinding typeArg : typeBinding.getTypeArguments() ) {
				newTypeArgs.add(typeFromBinding(ast, typeArg));
			}

			return type;
		}

		// simple or raw type
		String qualName = typeBinding.getQualifiedName();
		if( "".equals(qualName) ) {
			throw new IllegalArgumentException("No name for type binding.");
		}
		return ast.newSimpleType(ast.newName(qualName));
	}


	private static MethodDeclaration getMethodDeclaration(ASTNode node) {
		while(node != null && node.getParent() != null && !(node instanceof MethodDeclaration)) {
			node = node.getParent();
		}
		return (MethodDeclaration) node;
	}

	public static ASTNode getDefaultReturn(MethodDeclaration md, AST hostAST) {
		if(md == null) 
			return null;
		Type returnType = md.getReturnType2();
		if(returnType.isPrimitiveType()) {
			PrimitiveType casted = (PrimitiveType) returnType;
			PrimitiveType.Code tc = casted.getPrimitiveTypeCode();
			if(tc == PrimitiveType.BOOLEAN) {
				return hostAST.newBooleanLiteral(false);
			}
			if(tc == PrimitiveType.CHAR || tc == PrimitiveType.BYTE || tc == PrimitiveType.INT || tc == PrimitiveType.SHORT)
				return hostAST.newNumberLiteral("0");
			if(tc == PrimitiveType.DOUBLE || tc == PrimitiveType.FLOAT || tc == PrimitiveType.LONG)
				return hostAST.newNumberLiteral("0.0");
		} 
		return hostAST.newNullLiteral();
	}
	public static ASTNode getDefaultReturn(ASTNode node, AST hostAST) {
		MethodDeclaration md = getMethodDeclaration(node);
		return getDefaultReturn(md, hostAST);
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

/** helper class for compiling from string */
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