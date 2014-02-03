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
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import javax.tools.SimpleJavaFileObject;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import clegoues.genprog4java.main.Configuration;

public class ASTUtils
{
	
	public static int getLineNumber(ASTNode node)
	{ // FIXME: I think we should be able to just get this from the CU saved in javarepresentation, right?
		ASTNode root = node.getRoot();
		int lineno = -1;
		if(root instanceof CompilationUnit)
		{
			CompilationUnit cu = (CompilationUnit)root;
			lineno = cu.getLineNumber(node.getStartPosition());
		}
	
		return lineno;
	}
	
	public static Set<String> getNames(ASTNode node)     // it does not count.
	{
		TreeSet<String> names = new TreeSet<String>();
		NameCollector visitor = new NameCollector(names);
		node.accept(visitor);
		return names;
	}
	// FIXME this feels wicked inefficient to me, but possibly that's a low-order bit
	
	public static Set<String> getTypes(ASTNode node)
	{
		TreeSet<String> types = new TreeSet<String>();
		
		TypeCollector visitor = new TypeCollector(types);
		
		node.accept(visitor);
		
		return types;
	}
	
	public static Set<String> getScope(ASTNode node)
	{
		TreeSet<String> scope = new TreeSet<String>();
		
		ScopeCollector visitor = new ScopeCollector(scope);
		
		node.accept(visitor);
		
		return scope;
	}
	
	public static Iterable<JavaSourceFromString> getJavaSourceFromString(String code)
	{
		final JavaSourceFromString jsfs;
		jsfs = new JavaSourceFromString("code", code);
		
		return new Iterable<JavaSourceFromString>()
		{
			public Iterator<JavaSourceFromString> iterator()
			{
				return new Iterator<JavaSourceFromString>()
				{
					boolean isNext = true;

					public boolean hasNext()
					{
						return isNext;
					}

					public JavaSourceFromString next()
					{
						if (!isNext)
							throw new NoSuchElementException();
						isNext = false;
						return jsfs;
					}

					public void remove()
					{
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}
}



class JavaSourceFromString extends SimpleJavaFileObject
{
	final String code;

	JavaSourceFromString(String name, String code)
	{
		super(URI.create(name.replace(".", "/")+"/"+Configuration.targetClassName+Kind.SOURCE.extension), Kind.SOURCE);
		this.code = code;
	}

	public CharSequence getCharContent(boolean ignoreEncodingErrors)
	{
		return code;
	}
}

