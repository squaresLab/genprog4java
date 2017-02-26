package clegoues.genprog4java.treelm;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A simple SymbolTable implementation.
 * 
 * @author jonathan
 */
public class SimpleSymbolTable implements SymbolTable {
	protected final Map< String, Set< String > > byType;
	protected final Set< String > allNames;

	public SimpleSymbolTable() {
		byType = new HashMap<>();
		allNames = new HashSet<>();
	}

	@Override
	public boolean addVariable( String type, String name ) {
		if ( ! allNames.add( name ) )
			return false;
		byType.computeIfAbsent( type, ( n ) -> new HashSet<>() ).add( name );
		return true;
	}

	@Override
	public Set< String > getInScopeNames() {
		return Collections.unmodifiableSet( allNames );
	}

	@Override
	public Set< String > getNamesForType( String type ) {
		return byType.getOrDefault( type, Collections.emptySet() );
	}
	
	@Override
	public boolean inScope( String name ) {
		return allNames.contains( name );
	}
}
