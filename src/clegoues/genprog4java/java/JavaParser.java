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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.rep.JavaRepresentation;

/**
 * Parses a single java file, and delegates to a semantic info visitor the goal
 * of collecting various types of semantic info necessary for later mutation checks.
 * @author clegoues
 *
 */
public class JavaParser
{
	/** visits all nodes while file is parsed.  Collects semantic info */
	private SemanticInfoVisitor visitor;
	
	/** compilation unit from parsed file; to be returned/collected by the parser client */
	private CompilationUnit compilationUnit;
	

	public JavaParser(ScopeInfo scopeList)
	{
		this.visitor = new SemanticInfoVisitor();
		this.visitor.setScopeList(scopeList);
	}
	
	public CompilationUnit getCompilationUnit()
	{
		return this.compilationUnit;
	}
	
	public void parse(String file, String[] libs)
	{
		int parserVersion = AST.JLS8;
		if(Configuration.sourceVersion != "1.8") {
			parserVersion = AST.JLS4;
		}
		ASTParser parser = ASTParser.newParser(parserVersion);
		parser.setEnvironment(libs, new String[] {}, null, true);
		
		Map options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(Configuration.sourceVersion, options);
		parser.setCompilerOptions(options);
		
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		// note that this bindings recovery and resolution are important for
		// checking information about types, down the line.
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.setStatementsRecovery(true);
		ParserRequestor req = new ParserRequestor(visitor);
		
		parser.createASTs(new String[]{file}, null, new String[0], req, null);
		
		this.compilationUnit = visitor.getCompilationUnit();
	}

}
