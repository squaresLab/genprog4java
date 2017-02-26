package clegoues.genprog4java.treelm;

import java.util.Set;

import codemining.math.random.SampleUtils;

/**
 * Selects in-scope names randomly and creates new names by appending a unique
 * integer to an abbreviation of the type name.
 * 
 * @author jonathan
 */
public enum NaiveNamingStrategy implements NamingStrategy {
	INSTANCE;
	
	@Override
	public String getUniqueName( SymbolTable st, String type ) {
		String base;
		// String.matches() requires the pattern to consume the whole string
		if ( type.matches( ".*?\\p{IsUppercase}.*" ) )
			// If there are upper case letters, collect them and make them lower
			base = type.replaceAll( "\\P{IsUppercase}", "" ).toLowerCase();
		else
			// Otherwise, collect the first char and all chars after underscores
			base = type.substring( 0, 1 ) + type.replaceAll( "(?<!_).", "" );

		String name = base + id++;
		while ( st.inScope( name ) )
			name = base + id++;
		System.out.printf( "DEBUG: allocating %s of type %s\n", name, type );
		return name;
	}

	@Override
	public String getInScopeName( SymbolTable st, String type ) {
		Set< String > available = st.getNamesForType( type );
		if ( available.isEmpty() )
			throw new IllegalStateException(
				"No in-scope variables with type " + type
			);
		String name = SampleUtils.getRandomElement( available );
		System.out.printf( "DEBUG: retrieving %s of type %s\n", name, type );
		return name;
	}

	protected int id = 0;
}
