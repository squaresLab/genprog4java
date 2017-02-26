package clegoues.genprog4java.treelm;

/**
 * A strategy for choosing variable names. The naming strategy should be largely
 * stateless. In particular, there are no guarantees whether names that were
 * previously returned by the strategy will be in or out of scope at the time of
 * the next call. The strategy is not responsible for recording the names that
 * it returns, nor for adding names to the symbol table.
 * 
 * @author jonathan
 */
public interface NamingStrategy {
	/**
	 * Returns a fresh variable name for the given type. The variable name is
	 * guaranteed not to be present in the symbol table (i.e.
	 * {@code st.inScope(name)} returns {@code false}).
	 * 
	 * @param st   the symbol table to make the name unique against
	 * @param type the type of variable being allocated
	 * 
	 * @return a fresh variable name for the given type.
	 */
	public String getUniqueName( SymbolTable st, String type );
	
	/**
	 * Returns the name of an in-scope variable with the given type. That is,
	 * {@code st.inScope(name)} returns {@code true}. If no such variable names
	 * exist, throws an IllegalStateException.
	 * 
	 * @param st   the symbol table 
	 * @param type the type of variable
	 * 
	 * @return the name of an in-scope variable with the given type.
	 * 
	 * @throws IllegalStateException if no in-scope variables with the given
	 * type exist.
	 */
	public String getInScopeName( SymbolTable st, String type );
}
