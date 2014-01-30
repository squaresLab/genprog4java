package clegoues.genprog4java.main;

public class Configuration {
	public static final String packageName = null; // FIXME hmmm
	public static final String outputDir = null;
	// FIXME: rename these, and also figure out what they mean
	public static String libs;
	public static String sourceVersion;
	public static String targetVersion;
	private int numPositiveTests = 5;
	private int numNegativeTests = 1;
	private String sanityFilename = "repair.sanity.java";
	private String sanityExename = "repair.sanity";
	private String globalExtension = ".java";
	private String searchStrategy = "ga";

	public  int getNumPositiveTests() { return this.numPositiveTests; }
	public int getNumNegativeTests() { return this.numNegativeTests; }
	public Configuration() {};
	public Configuration(int numPos, int numNeg) {
		this.numPositiveTests = numPos;
		this.numNegativeTests = numNeg;
	}
	public Configuration(String configFile) {
		throw new UnsupportedOperationException() ; // TODO: load options for run from file
	}
	public String getSanityFilename() {
		return this.sanityFilename;
	}
	public String getGlobalExtension() {
		return this.globalExtension; 
	}
	public String getSanityExename() {
		return this.sanityExename;
	}
}
