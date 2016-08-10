package clegoues.genprog4java.mut.holes.java;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.jdt.core.dom.ASTNode;

import clegoues.genprog4java.java.ClassInfo;
import clegoues.genprog4java.localization.Location;

public abstract class JavaLocation<G> extends Location<G> {

	public JavaLocation(G location, Double weight) {
		super(location, weight);
		
	}

	private ClassInfo classInfo = null; 
	// this is a bit tricky, because I may want to use *either* the id or this node depending and that
	// feels like a bad idea.  But let's try it.
	private ASTNode codeElement = null;
	

	public ClassInfo getClassInfo() {
		return this.classInfo;
	}
	
	public void setClassInfo(ClassInfo ci) {
		this.classInfo = ci;
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

	public ASTNode getCodeElement() {
		return codeElement;
	}

	public void setCodeElement(ASTNode codeElement) {
		this.codeElement = codeElement;
	}

	public String toString() {
		return ((Integer) this.getId()).toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof JavaLocation) {
			JavaLocation other = (JavaLocation) obj;
			if(this.codeElement != null && other.getCodeElement() != null) {
			return new EqualsBuilder()
					.append(this.classInfo, other.getClassInfo())
					.append(this.codeElement.toString(), other.getCodeElement().toString())
					.append(this.getId(), other.getId())
					.isEquals();
			} 
			return false;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(this.classInfo)
				.append(this.codeElement.toString())
				.append(this.getId())
				.toHashCode();
	}
}
