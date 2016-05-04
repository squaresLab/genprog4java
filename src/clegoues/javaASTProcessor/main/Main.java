package clegoues.javaASTProcessor.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.InfixExpression;

import clegoues.javaASTProcessor.ASTPrinter.ASTDumper;
import clegoues.javaASTProcessor.ASTPrinter.IASTPrinter;
import clegoues.javaASTProcessor.ASTPrinter.JSONStyleASTPrinter;
import clegoues.javaASTProcessor.ASTPrinter.SimpleTextASTPrinter;
import clegoues.javaASTProcessor.main.Configuration;
import clegoues.util.ConfigurationBuilder;



public class Main {
	private static ASTDumper dumper = null;

	protected static List<CompilationUnit> parseCompilationUnit(String file, String[] libs) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		String fileName = "./" + Configuration.sourceDir + "/" + file; //"./" + file.replace('.', '/') + ".java";
		File f = new File(fileName);
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
		
		return result;
	}
	
	protected static void printCompilationUnits(List<CompilationUnit> cus) {
		for(CompilationUnit cu : cus) {
		cu.accept(new ASTVisitor() {
			// Eclipse AST optimize deeply nested expressions of the form L op R
			// op R2
			// op R3... where the same operator appears between
			// all the operands. This
			// function disable such optimization back to tree view.
			@Override
			public boolean visit(final InfixExpression node) {
				if (node.hasExtendedOperands()) {
					@SuppressWarnings("unchecked")
					List<Expression> operands = new ArrayList<>(node.extendedOperands());
					Collections.reverse(operands);
					operands.add(node.getRightOperand());
					final Expression firstOperand = operands.remove(0);
					firstOperand.delete(); // remove node from its parent
					node.setRightOperand(firstOperand);
					InfixExpression last = node;
					for (final Expression expr : operands) {
						InfixExpression infixExpression = node.getAST().newInfixExpression();
						infixExpression.setOperator(node.getOperator());
						expr.delete();
						infixExpression.setRightOperand(expr);
						final Expression left = last.getLeftOperand();
						last.setLeftOperand(infixExpression);
						infixExpression.setLeftOperand(left);
						last = infixExpression;
					}
				}

				return super.visit(node);
			}
		});
		for (final Object comment : cu.getCommentList()) {
			((Comment) comment).delete();
		}
		dumper.dump(cu);
	}
	}
	
	public static void main(String[] args) {
		BasicConfigurator.configure();
		ConfigurationBuilder.register( Configuration.token );
		ConfigurationBuilder.parseArgs( args );
		ConfigurationBuilder.storeProperties();
		IASTPrinter myPrinter = null;
		switch(Configuration.outputFormat.trim().toLowerCase()) {
		case "json" : myPrinter = new JSONStyleASTPrinter(System.out);
			break;
		case "simple" : 
		default: myPrinter = new SimpleTextASTPrinter(System.out);
			break;
		}
		
		dumper = new ASTDumper(myPrinter, null);
		for(String fname : Configuration.targetClassNames) {
			List<CompilationUnit> cus = parseCompilationUnit(fname, Configuration.libs.split(File.pathSeparator));
			printCompilationUnits(cus);
		}
		
	}

}
