package clegoues.genprog4java.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.SimpleName;
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

	public boolean vdPossibleFinalVariable(VariableDeclarationStatement ds) {
		VariableDeclarationFragment df = (VariableDeclarationFragment) ds.fragments().get(0);
		return finalVariables.contains(df.getName().getIdentifier());
	}

	public boolean expPossibleFinalAssignment(ExpressionStatement exstat) {
		if (exstat.getExpression() instanceof Assignment) {
			Assignment assignment= (Assignment) exstat.getExpression();
			if(assignment.getLeftHandSide() instanceof SimpleName){
				SimpleName leftHand = (SimpleName) assignment.getLeftHandSide();
				return finalVariables.contains(leftHand.toString());
				}
			}
		return false;
		}

	
	public boolean scopeCheckOK(JavaStatement potentiallyBuggyStmt, JavaStatement potentialFixStmt) {
		// I *believe* this is just variable names and doesn't check required
		// types, which are also collected
		// at parse time and thus could be considered here.
		Set<String> inScopeAt = inScopeMap.get(potentiallyBuggyStmt.getStmtId());

		Set<String> requiredScopes = potentialFixStmt.getRequiredNames();
			for (String req : requiredScopes) {
				if (!inScopeAt.contains(req)) {
					return false;
				}
			}
			return true;
	}

}
