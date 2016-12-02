package clegoues.genprog4java.treelm;

import java.util.function.Supplier;

/**
 * An empty SymbolTable placeholder. This symbol table always contains only the
 * variable {@code x} and always allows treats {@code x} as the next free
 * variable name.
 * 
 * @author jonathan
 */
public enum EmptySymbolTable implements SymbolTable {
	INSTANCE;

	/**
	 * Ignored, since the in scope variables never change.
	 */
	@Override
	public void enter( int nodeType ) { }

	/**
	 * Ignored, since the in scope variables never change.
	 */
	@Override
	public void leave( int nodeType ) { }

	/**
	 * Always allocates {@code x}.
	 */
	@Override
	public Supplier< String > allocFreeName( String type ) {
		return () -> "x";
	}

	/**
	 * Always returns {@code x}.
	 */
	@Override
	public Supplier< String > getNameForType( String type ) {
		return () -> "x";
	}

}
