package clegoues.genprog4java.mut.holes.java;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.mut.Location;

public class JavaLocation implements Location<JavaStatement> {

	private JavaStatement location = null;
	
	public JavaLocation(JavaStatement location) {
		this.location = location;
	}
	
	@Override
	public JavaStatement getLocation() {
		return this.location;
	}

	@Override
	public void setLocation(JavaStatement location) {
		this.location = location;	
	}

}
