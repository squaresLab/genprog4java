package clegoues.javaASTProcessor.main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

import clegoues.javaASTProcessor.main.Configuration;
import clegoues.util.ConfigurationBuilder;



public class Main {


	protected static void parseCompilationUnit(String file, String[] libs) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		String fileName = "./" + Configuration.sourceDir + "/" + file; //"./" + file.replace('.', '/') + ".java";
		File f = new File(fileName);
		if(f.exists()) {
			System.out.println(fileName + "Exists!");
		} else {
			System.out.println(fileName + "Doesn't exists!");

		}
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
				new String[] {fileName}, 
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
		BasicConfigurator.configure();

		ConfigurationBuilder.register( Configuration.token );
		ConfigurationBuilder.parseArgs( args );
		ConfigurationBuilder.storeProperties();
		for(String fname : Configuration.targetClassNames) {
			parseCompilationUnit(fname, Configuration.libs.split(File.pathSeparator));
		}
		
		
	}

}
