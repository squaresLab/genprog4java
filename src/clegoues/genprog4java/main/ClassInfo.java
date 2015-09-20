package clegoues.genprog4java.main;


public class ClassInfo implements Comparable<ClassInfo> {
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
	public String getPackage() {
		return packageName;
	}
	public void setPackage(String packageName) {
		this.packageName = packageName;
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
