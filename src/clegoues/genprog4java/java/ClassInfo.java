package clegoues.genprog4java.java;

import java.io.File;

/** 
 * information about a java file (class, package) and helper utilities for managing
 * paths.  Comparable because we use them in sets/lists/etc, and also because they're
 * basically just pairs of strings.
 * @author clegoues
 *
 */
public class ClassInfo implements Comparable<ClassInfo> {
	private String className;
	private String packageName;
	
	public ClassInfo(String className, String packageName) {
		this.setClassName(className);
		this.setPackage(packageName);
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getPackage() {
		return packageName;
	}
	public void setPackage(String packageName) {
		this.packageName = packageName;
	}
	
	public String pathToJavaFile() {
		return this.packageName + File.separatorChar + this.className + ".java";
	}
	
	public String pathToClassFile() {
		return this.packageName + File.separatorChar + this.className + ".class";
	}
	@Override
	public int compareTo(ClassInfo o) {
		if(this.packageName.compareTo(o.getPackage()) == 0) {
			return this.className.compareTo(o.className);
		} else {
			return this.packageName.compareTo(o.getPackage());
		}
	}
	
}
