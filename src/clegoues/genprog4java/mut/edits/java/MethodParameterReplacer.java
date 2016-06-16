package clegoues.genprog4java.mut.edits.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.java.MethodInfo;
import clegoues.genprog4java.main.ClassInfo;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.holes.java.SimpleJavaHole;
import clegoues.genprog4java.mut.holes.java.SubExpsHole;

public class MethodParameterReplacer extends JavaEditOperation {


	public MethodParameterReplacer(JavaLocation location,  HashMap<String, EditHole> sources) {
		super(Mutation.PARREP, location, sources);
		this.holeNames.add("replaceParameter");
	}

	@Override
	public void edit(ASTRewrite rewriter) {
		JavaStatement locationStmt = (JavaStatement) (this.getLocation().getLocation());
		SimpleJavaHole thisHole = (SimpleJavaHole) this.getHoleCode("replaceParameter");
		rewriter.replace(thisHole.getHoleSite(), thisHole.getCode(), null); 
	}
}
/*
 * [Parameter Replacer]
P = program
B = fault location

<AST Analysis> 
M <- collect a method call of B in P 

<Context Check>
if there is any parameter in M -> continue
otherwise -> stop 

<Program Editing>
TargetParam <- select a parameter in M

I <- collect all method calls in the same scope of TargetParam in P
I_selected <- select a method call which has at least one parameter whose type is compatible with                            TargetParam

SourceParam <- select a parameter of I_selected, which has a compatible type with TargetParam

replace TargetParam by SourceParam
 */