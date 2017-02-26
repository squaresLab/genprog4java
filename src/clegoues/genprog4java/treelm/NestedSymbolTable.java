package clegoues.genprog4java.treelm;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A symbol table for a scope nested inside a parent scope. This symbol table
 * contains all the names available in its parent table. Any names added to this
 * table do not affect the parent. To return to the outer scope, simply resume
 * using the parent table instead of this one.
 * <p>
 * Note: The names in the parent table are not copied into this one. Instead,
 * they are looked up as needed. This means that changes to the parent table
 * will be reflected in subsequent uses of this table.
 * </p>
 * 
 * @author jonathan
 *
 */
public class NestedSymbolTable extends SimpleSymbolTable {
	private final SymbolTable parent;
	private final NestedSymbolTable alias;
	
	/**
	 * Creates a new symbol table from the parent.
	 * 
	 * @param parent the symbol table for the outer scope
	 */
	public NestedSymbolTable( SymbolTable parent ) {
		this.parent = parent;
		if ( parent instanceof NestedSymbolTable )
			this.alias = (NestedSymbolTable) parent;
		else
			this.alias = null;
	}

	/**
	 * Adds a variable to this symbol table. If a variable with the same name
	 * already exists in this or the parent table, no change is made.
	 * 
	 * @param type the type of the variable to add
	 * @param name the name of the variable to add
	 * 
	 * @return {@code true} if the variable was successfully added or
	 * {@code false} if a variable with that name already exists.
	 */
	@Override
	public boolean addVariable( String type, String name ) {
		if ( parent.inScope( name ) )
			return false;
		return super.addVariable( type, name );
	}

	/**
	 * Returns the set of names in scope in the parent or added to this table.
	 * 
	 * @return the set of names in scope in the parent or added to this table.
	 */
	@Override
	public Set< String > getInScopeNames() {
		Set< String > names = new HashSet<>();
		addInScopeNames( names );
		return Collections.unmodifiableSet( names );
	}
	
	/**
	 * Adds the in-scope names from the parent and this table to the given set.
	 * If the parent is also an instance of NestedSymbolTable, its
	 * {@code addInScopeNames()} method will be used to avoid unnecessarily
	 * creating and destroying intermediate sets.
	 * 
	 * @param accumulator the set to populate with in-scope names.
	 */
	protected void addInScopeNames( Set< String > accumulator ) {
		accumulator.addAll( allNames );
		if ( alias == null )
			accumulator.addAll( parent.getInScopeNames() );
		else
			alias.addInScopeNames( accumulator );
	}

	/**
	 * Returns the set of names with the given type that are in scope in the
	 * parent or added to this table.
	 * 
	 * @param type the type to search for
	 * 
	 * @return the set of names with the given type.
	 */
	@Override
	public Set< String > getNamesForType( String type ) {
		Set< String > names = new HashSet<>();
		addNamesForType( names, type );
		return Collections.unmodifiableSet( names );
	}
	
	/**
	 * Adds the in-scope names with the given type to the given set. If the
	 * parent is also an instance of NestedSymbolTable, its
	 * {@code addNamesForType()} method will be used to avoid unnecessarily
	 * creating and destroying intermediate sets.
	 * 
	 * @param accumulator the set to populate
	 * @param type        the type of variables to populate with
	 */
	protected void addNamesForType( Set< String > accumulator, String type ) {
		accumulator.addAll( byType.getOrDefault( type, Collections.emptySet() ) );
		if ( alias == null )
			accumulator.addAll( parent.getNamesForType( type ) );
		else
			alias.addNamesForType( accumulator, type );
	}
}
