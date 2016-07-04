package clegoues.genprog4java.mut.holes.java;

import clegoues.genprog4java.java.JavaStatement;

public class JavaStatementLocation extends JavaLocation<JavaStatement> {
	private int id;
	
	public JavaStatementLocation(JavaStatement location, Double weight) {
		super(location,weight);
		this.id = location.getStmtId();
	}

	@Override
	public int getId() {
		return this.id;
	}
	
}
