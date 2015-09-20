package clegoues.genprog4java.main;

import java.io.File;

public class ClassInfo {
	private String className;
	private String packageName;
	ClassInfo(String className, String packageName) {
		this.setClassName(className);
		this.setPackage(packageName);
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getPackag() {
		return packageName;
	}
	public void setPackage(String packageName) {
		this.packageName = packageName;
	}
	
	public String getPathToJava() {
		return this.packageName + File.separatorChar + this.className;  
	}
	
	
}
