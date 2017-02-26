package clegoues.genprog4java.java;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import clegoues.genprog4java.localization.Location;
import clegoues.genprog4java.treelm.SymbolTable;

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
			allNames.addAll( JavaSemanticInfo.classScopeMap.get( id ) );
			allNames.addAll( JavaSemanticInfo.methodScopeMap.get( id ) );
			allNames.retainAll( JavaSemanticInfo.variableDataTypes.keySet() );
			allNames = Collections.unmodifiableSet( allNames );
		}
		return allNames;
	}

	@Override
	public Set< String > getNamesForType( String type ) {
		Set< String > names = new HashSet<>( getInScopeNames() );
		names.retainAll( JavaSemanticInfo.inverseVarDataTypeMap.get( type ) );
		return Collections.unmodifiableSet( names );
	}
	
}
