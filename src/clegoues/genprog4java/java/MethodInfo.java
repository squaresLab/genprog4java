package clegoues.genprog4java.java;

import java.util.List;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

public class MethodInfo {
	private String name;
	private int numArgs;
	private Type returnType;
	private List<SingleVariableDeclaration> parameters;
	private MethodDeclaration node;

	MethodInfo(String name, int numArgs, Type returnType, List<SingleVariableDeclaration> parameters, MethodDeclaration actualNode) {
		this.setName(name);
		this.setNumArgs(numArgs);
		this.setReturnType(returnType);
		this.setParameters(parameters);
		this.setNode(actualNode);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNumArgs() {
		return numArgs;
	}

	public void setNumArgs(int numArgs) {
		this.numArgs = numArgs;
	}

	public Type getReturnType() {
		return returnType;
	}

	public void setReturnType(Type returnType) {
		this.returnType = returnType;
	}

	public List<SingleVariableDeclaration> getParameters() {
		return parameters;
	}

	public void setParameters(List<SingleVariableDeclaration> parameters) {
		this.parameters = parameters;
	}

	public MethodDeclaration getNode() {
		return node;
	}

	public void setNode(MethodDeclaration node) {
		this.node = node;
	}
}
