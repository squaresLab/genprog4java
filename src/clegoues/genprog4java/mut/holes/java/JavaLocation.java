package clegoues.genprog4java.mut.holes.java;

import org.eclipse.jdt.core.dom.ASTNode;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.main.ClassInfo;
import clegoues.genprog4java.mut.Location;
import clegoues.util.Pair;

public class JavaLocation implements Location<JavaStatement> {

	private ClassInfo classInfo = null; 
	private JavaStatement location = null;
	private Double weight;
	private int id;
	// this is a bit tricky, because I may want to use *either* the id or this node depending and that
	// feels like a bad idea.  But let's try it.
	private ASTNode codeElement = null;
	
	public JavaLocation(JavaStatement location, Double weight) {
		this.location = location;
		this.weight = weight;
		this.id = location.getStmtId();
	}
	
	@Override
	public JavaStatement getLocation() {
		return this.location;
	}
	
	public ClassInfo getClassInfo() {
		return this.classInfo;
	}

	
	public void setClassInfo(ClassInfo ci) {
		this.classInfo = ci;
	}

	@Override
	public void setLocation(JavaStatement location) {
		this.location = location;	
	}

	@Override
	public Double getWeight() {
		return this.weight;
	}

	@Override
	public void setWeight(Double weight) {
		this.weight = weight;
	}

	@Override
	public Pair<JavaStatement, Double> asPair() {
		return new Pair<JavaStatement,Double>(this.location, this.weight);
	}
	
	@Override
	public Object clone() {
		JavaLocation clone = null;
		try {
			clone = (JavaLocation) super.clone();
		} catch (CloneNotSupportedException e) {
			// This should never happen
		}
		clone.setLocation(this.getLocation());
		clone.setWeight(this.getWeight());
		return (Object) clone;
	}

	@Override
	public int getId() {
		return this.id;
	}

	public ASTNode getCodeElement() {
		return codeElement;
	}

	public void setCodeElement(ASTNode codeElement) {
		this.codeElement = codeElement;
	}

}
