package clegoues.genprog4java.treelm;

/**
 * Indicates an unrecoverable problem during code generation.
 * 
 * @author jonathan
 */
public class CodeGenerationException extends RuntimeException {
	private static final long serialVersionUID = 20170223L;

	/**
	 * Constructs a new CodeGenerationException with no detail message.
	 */
	public CodeGenerationException() {}
	
	/**
	 * Constructs a new CodeGenerationException with the given detail message.
	 * 
	 * @param message the detail message
	 */
	public CodeGenerationException( String message ) {
		super( message );
	}
	
	/**
	 * Constructs a new CodeGenerationException with the given detail message
	 * and cause.
	 * 
	 * @param message the detail message
	 * @param cause   the cause
	 */
	public CodeGenerationException( String message, Throwable cause ) {
		super( message, cause );
	}
	
	/**
	 * Constructs a new CodeGenerationException with the given cause.
	 * 
	 * @param cause the cause
	 */
	public CodeGenerationException( Throwable cause ) {
		super( cause );
	}
}
