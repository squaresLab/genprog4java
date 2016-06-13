package clegoues.genprog4java.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import clegoues.util.Pair;

public class JavaSemanticInfo {
	private static HashMap<Integer, Set<String>> inScopeMap = new HashMap<Integer, Set<String>>();
	private static TreeSet<Pair<String,String>> methodReturnType = new TreeSet<Pair<String,String>>();
	private static HashMap<String, String> variableDataTypes = new HashMap<String, String>();
	private static TreeSet<String> finalVariables = new TreeSet<String>();
	private static List<MethodInfo> methodDecls = new ArrayList<MethodInfo>();
	
	public void addAllSemanticInfo(JavaParser myParser) {
		JavaSemanticInfo.methodReturnType.addAll(myParser.getMethodReturnTypeSet());
		JavaSemanticInfo.variableDataTypes.putAll(myParser.getVariableDataTypes());
		JavaSemanticInfo.finalVariables.addAll(myParser.getFinalVariableSet());
		JavaSemanticInfo.methodDecls.addAll(myParser.getMethodDeclarations());		
	}

	public void addToScopeMap(JavaStatement s, Set<String> scope) {
	JavaSemanticInfo.inScopeMap.put(s.getStmtId(),scope);
	}

	public boolean possibleFinalVariable(VariableDeclarationStatement ds) {
		VariableDeclarationFragment df = (VariableDeclarationFragment) ds.fragments().get(0);
		return finalVariables.contains(df.getName().getIdentifier()));
	}

}
