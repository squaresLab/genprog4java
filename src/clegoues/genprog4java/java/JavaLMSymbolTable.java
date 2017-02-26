package clegoues.genprog4java.java;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import clegoues.genprog4java.localization.Location;
import clegoues.genprog4java.treelm.SymbolTable;

import static clegoues.genprog4java.java.JavaSemanticInfo.classScopeMap;
import static clegoues.genprog4java.java.JavaSemanticInfo.inverseVarDataTypeMap;
import static clegoues.genprog4java.java.JavaSemanticInfo.methodScopeMap;
import static clegoues.genprog4java.java.JavaSemanticInfo.variableDataTypes;

public class JavaLMSymbolTable implements SymbolTable {
	private transient Set< String > allNames;
	private final int id;
	
	/**
	 * Create a new symbol table including everything in scope at the given
	 * location.
	 * 
	 * @param startingPoint the location to import in-scope variables from
	 */
	public JavaLMSymbolTable (Location< ? > startingPoint) {
		allNames = null;
		id = startingPoint.getId();
	}
	
	@Override
	public boolean addVariable( String type, String name ) {
		throw new UnsupportedOperationException( "addVariable(type,name)" );
	}

	@Override
	public Set< String > getInScopeNames() {
		if ( allNames == null ) {
			allNames = new java.util.TreeSet<>();
			allNames.addAll( classScopeMap.get( id ) );
			allNames.addAll( methodScopeMap.get( id ) );
			allNames.retainAll( variableDataTypes.keySet() );
			allNames = Collections.unmodifiableSet( allNames );
		}
		return allNames;
	}

	@Override
	public Set< String > getNamesForType( String type ) {
		Set< String > names = new HashSet<>( getInScopeNames() );
		names.retainAll(
			inverseVarDataTypeMap.getOrDefault( type, Collections.emptySet() )
		);
		return Collections.unmodifiableSet( names );
	}
	
}
