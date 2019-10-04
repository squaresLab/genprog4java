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
import java.util.*;

import javax.tools.SimpleJavaFileObject;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.core.dom.*;

/**
 * provides static utils for manipulating java ASTs.  Used to be much longer
 * before I refactored semantic check info; it may be possible to refactor this away
 * at some point.
 *
 * @author clegoues
 */
public class ASTUtils {

	public static ASTNode getEnclosing(Class<? extends ASTNode> desiredParent, ASTNode node) {
		ASTNode parent = node.getParent();
		while (parent != null && !desiredParent.isInstance(parent)) {
			parent = parent.getParent();
		}
		return parent;
	}

	public static ASTNode getEnclosingMethod(ASTNode node) {
		return getEnclosing(MethodDeclaration.class, node);
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
	 *
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

	/**
	 * create a new Type object from a typeBinding, a hilariously
	 * difficult thing to do.
	 *
	 * @param ast
	 * @param typeBinding
	 * @return
	 */
	public static Type typeFromBinding(AST ast, ITypeBinding typeBinding) {
		if (ast == null)
			throw new NullPointerException("ast is null");
		if (typeBinding == null)
			throw new NullPointerException("typeBinding is null");

		if (typeBinding.isPrimitive()) {
			return ast.newPrimitiveType(
					PrimitiveType.toCode(typeBinding.getName()));
		}

		if (typeBinding.isCapture()) {
			ITypeBinding wildCard = typeBinding.getWildcard();
			WildcardType capType = ast.newWildcardType();
			ITypeBinding bound = wildCard.getBound();
			if (bound != null) {
				capType.setBound(typeFromBinding(ast, bound),
						wildCard.isUpperbound());
			}
			return capType;
		}

		if (typeBinding.isArray()) {
			Type elType = typeFromBinding(ast, typeBinding.getElementType());
			return ast.newArrayType(elType, typeBinding.getDimensions());
		}

		if (typeBinding.isParameterizedType()) {
			ParameterizedType type = ast.newParameterizedType(
					typeFromBinding(ast, typeBinding.getErasure()));

			@SuppressWarnings("unchecked")
			List<Type> newTypeArgs = type.typeArguments();
			for (ITypeBinding typeArg : typeBinding.getTypeArguments()) {
				newTypeArgs.add(typeFromBinding(ast, typeArg));
			}

			return type;
		}

		// simple or raw type
		String qualName = typeBinding.getQualifiedName();
		if ("".equals(qualName)) {
			throw new IllegalArgumentException("No name for type binding.");
		}
		return ast.newSimpleType(ast.newName(qualName));
	}


	private static MethodDeclaration getMethodDeclaration(ASTNode node) {
		while (node != null && node.getParent() != null && !(node instanceof MethodDeclaration)) {
			node = node.getParent();
		}
		return (MethodDeclaration) node;
	}

	public static ASTNode getDefaultReturn(ASTNode node, AST hostAST) {
		MethodDeclaration md = getMethodDeclaration(node);
		if (md == null)
			return null;
		Type returnType = md.getReturnType2();
		if (returnType.isPrimitiveType()) {
			PrimitiveType casted = (PrimitiveType) returnType;
			PrimitiveType.Code tc = casted.getPrimitiveTypeCode();
			if (tc == PrimitiveType.BOOLEAN) {
				return hostAST.newBooleanLiteral(false);
			}
			if (tc == PrimitiveType.CHAR || tc == PrimitiveType.BYTE || tc == PrimitiveType.INT || tc == PrimitiveType.SHORT)
				return hostAST.newNumberLiteral("0");
			if (tc == PrimitiveType.DOUBLE || tc == PrimitiveType.FLOAT || tc == PrimitiveType.LONG)
				return hostAST.newNumberLiteral("0.0");
		}
		return hostAST.newNullLiteral();
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