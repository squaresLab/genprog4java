package clegoues.genprog4java.treelm;

import java.util.Collections;
import java.util.Set;

/**
 * An empty SymbolTable placeholder. This symbol table never contains any
 * variables. Attempting to add a variable always fails.
 * 
 * @author jonathan
 */
public enum EmptySymbolTable implements SymbolTable {
	INSTANCE;

	/**
	 * Never succeeds.
	 * 
	 * @param type  ignored
	 * @param name  ignored
	 * 
	 * @return {@code false}.
	 */
	@Override
	public boolean addVariable( String type, String name ) {
		return false;
	}

	/**
	 * Returns the empty set.
	 * 
	 * @return the empty set.
	 */
	@Override
	public Set< String > getInScopeNames() {
		return Collections.emptySet();
	}

	/**
	 * Returns the empty set.
	 * 
	 * @param type  ignored
	 * 
	 * @return the empty set.
	 */
	@Override
	public Set< String > getNamesForType( String type ) {
		return Collections.emptySet();
	}

}
