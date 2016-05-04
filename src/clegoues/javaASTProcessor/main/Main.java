package clegoues.javaASTProcessor.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;



public class Main {


	protected static void parseCompilationUnit(String file, String[] libs) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setEnvironment(libs, new String[] {}, null, true);
		Map options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
		parser.setCompilerOptions(options);

		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.setStatementsRecovery(true);
		final List<CompilationUnit> result = new ArrayList<CompilationUnit>();
		parser.createASTs(
				new String[] {file}, 
				null, 
				new String[0], 
				new FileASTRequestor(){
					public void acceptAST(String sourceFilePath, CompilationUnit ast) {
						result.add(ast);
					}
				}, 
				null
		);
	}
	public static void main(String[] args) {
		String fileName = args[1];
		
		
		
	}

}
