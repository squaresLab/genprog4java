package clegoues.genprog4java.treelm;

import java.util.Set;

/**
 * The symbol-table functionality required to get variable names and types.
 * 
 * @author jonathan
 */
public interface SymbolTable {
	/**
	 * Add a new variable into the symbol table (optional operation). The new
	 * variable has the given type and name. If a variable with that name
	 * already exists in the table, do nothing and return {@code false}.
	 * <p>
	 * If the implementation does not support this operation, it may either
	 * return false or throw an {@code UnsupportedOperationException} if the
	 * name is already present. If the name is not present, it should always
	 * throw an exception.
	 * </p>
	 * 
	 * @param type the type of the variable to add
	 * @param name the name of the variable to add
	 * 
	 * @return {@code true} if the variable was successfully added,
	 * {@code false} otherwise.
	 * 
	 * @throws UnsupportedOperationException if this symbol table does not
	 * support adding new variables.
	 */
	public boolean addVariable( String type, String name );
	
	/**
	 * Returns the set of all names currently in the table.
	 * 
	 * @return the set of all names currently in the table.
	 */
	public Set< String > getInScopeNames();
	
	/**
	 * Returns the set of all names with the given type in the table.
	 * 
	 * @param type the type of variables to retrieve
	 * 
	 * @return the set of all names with the given type in the table.
	 */
	public Set< String > getNamesForType( String type );

	/**
	 * Checks whether a variable with the given name is in the table. The
	 * default implementation simply checks the set returned by
	 * {@link #getInScopeNames()}. This method should be overridden if it can
	 * be implemented more efficiently directly.
	 * 
	 * @param name the name of the variable to check
	 * 
	 * @return {@code true} if a variable with the given name is in the table.
	 */
	public default boolean inScope( String name ) {
		return getInScopeNames().contains( name );
	}
}

