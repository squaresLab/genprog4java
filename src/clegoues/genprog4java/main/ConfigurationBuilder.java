package clegoues.genprog4java.main;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

public class ConfigurationBuilder< T > {
	protected static Logger logger =
		Logger.getLogger( ConfigurationBuilder.class );

	// FIXME: char?
	public static final LexicalCast< Boolean > BOOLEAN = Boolean::valueOf;
	public static final LexicalCast< Boolean > BOOL_ARG =
		new LexicalCast< Boolean >() {
			public Boolean parse(String value) {
				if ( trueValues == null ) {
					trueValues = new TreeSet< String >( String.CASE_INSENSITIVE_ORDER );
					trueValues.add( "1" );
					trueValues.add( "true" );
					trueValues.add( "yes" );
				}
				if ( falseValues == null ) {
					falseValues = new TreeSet< String >( String.CASE_INSENSITIVE_ORDER );
					falseValues.add( "0" );
					falseValues.add( "false" );
					falseValues.add( "no" );
				}
				if ( trueValues.contains( value ) )
					return true;
				if ( falseValues.contains( value ) )
					return false;
				throw new IllegalArgumentException( value );
			}
			
			private Set< String > trueValues = null;
			private Set< String > falseValues = null;
		};
	public static final LexicalCast< Byte >    BYTE    = Byte::valueOf;
	public static final LexicalCast< Double >  DOUBLE  = Double::valueOf;
	public static final LexicalCast< Float >   FLOAT   = Float::valueOf;
	public static final LexicalCast< Integer > INT     = Integer::valueOf;
	public static final LexicalCast< Long >    LONG    = Long::valueOf;
	public static final LexicalCast< Short >   SHORT   = Short::valueOf;
	public static final LexicalCast< String >  STRING  = (String v) -> v;

	public static < T > ConfigurationBuilder< T > of( LexicalCast< T > cast ) {
		return new ConfigurationBuilder< T >().withCast( cast );
	}

	public static interface LexicalCast< T > {
		public T parse( String value );
	}
	
	public static interface RegistryToken {
		public Class< ? > getOwnerClass();
	}
	
	public ConfigurationBuilder() {
		this( getCallingClass() );
	}

	public ConfigurationBuilder( Class< ? > owner ) {
		this.optbuilder = OptionBuilder.hasArg();
		this.varName    = null;
		this.flagName   = null;
		this.cast       = null;
		this.dflt       = null;
		this.owner      = owner;
	}
	
	@SuppressWarnings("static-access")
	public ConfigurationBuilder< T > withCast( LexicalCast< T > cast ) {
		this.cast = cast;
		if ( cast == BOOLEAN )
			this.optbuilder.hasArg( false );
		else
			this.optbuilder.hasArg( true );
		return this;
	}
	
	public ConfigurationBuilder< T > withDefault( String dflt ) {
		this.dflt = dflt;
		return this;
	}
	
	@SuppressWarnings( "static-access" )
	public ConfigurationBuilder< T > withFlag( String flagName ) {
		optbuilder.withLongOpt( flagName );
		this.flagName = flagName;
		return this;
	}
	
	@SuppressWarnings( "static-access" )
	public ConfigurationBuilder< T > withHelp( String helpText ) {
		optbuilder.withDescription( helpText );
		return this;
	}
	
	@SuppressWarnings("static-access")
	public ConfigurationBuilder< T > withVarName( String var ) {
		this.varName = var;
		if ( this.flagName == null )
			optbuilder.withLongOpt( varName );
		return this;
	}
	
	@SuppressWarnings("static-access")
	public T build() {
		if ( registryGate && !registry.contains( owner ) ) {
			logger.warn(
				"class " + owner.getName() + " was not registered before "
				+ "building. Please register to ensure help text is accurate."
			);
			register( () -> owner );
		}

		Option opt = optbuilder.create();
		String fieldName = varName == null ? opt.getLongOpt() : varName;
		String fqn = owner.getName() + "." + fieldName;
		String value =
			opt.hasArg() ? props.getProperty(opt.getLongOpt(), dflt) : "false";

		T result = null;
		try {
			Field f = owner.getDeclaredField( fieldName );
			f.setAccessible( true );
			FieldAccess< T > accessor = new FieldAccess< T >( f, cast );
			if ( value != null )
				result = accessor.set( value );
			accessors.put( opt.getLongOpt(), accessor );
		} catch (NoSuchFieldException | SecurityException e) {
			logger.error( "cannot access field: " + fqn );
			return value == null ? null : cast.parse( value );
		} catch (IllegalArgumentException | IllegalAccessException e) {
			logger.error(
				"cannot assign default value (" + value + ") to field " + fqn
				+ ": " + e.getMessage()
			);
			return value == null ? null : cast.parse( value );
		}

		if ( value != null )
			props.setProperty( opt.getLongOpt(), value );
		if ( dflt == null && opt.hasArg() )
			required.add( opt.getLongOpt() );
		options.addOption( opt );
		return result;
	}
	
	private static Class< ? > getCallingClass() {
		StackTraceElement[] frames = Thread.currentThread().getStackTrace();
		// skip frame 0, since that will be Thread.getStackTrace()
		for ( int i = 1; i < frames.length; ++i ) {
			if ( frames[ i ].getClassName() != ConfigurationBuilder.class.getName() ) {
				try {
					return Class.forName( frames[ i ].getClassName() );
				} catch (ClassNotFoundException e) {
					// somehow java cannot load the class that called us ... ?
					logger.error( e.getMessage() );
				}
			}
		}
		return null;
	}

	public static RegistryToken getToken() {
		final Class< ? > caller = getCallingClass();
		return () -> caller;
	}

	public static void register( RegistryToken token ) {
		registry.add( token.getOwnerClass() );
	}

	public static void showHelp() {
		registryGate = true;
		StringBuilder usage = new StringBuilder();
		StackTraceElement[] frames = Thread.currentThread().getStackTrace();
		for ( int i = frames.length - 1; i >= 0; --i ) {
			if ( frames[ i ].getMethodName().equals( "main" ) ) {
				usage.append( "java " );
				usage.append( frames[ i ].getClassName() );
				usage.append( " [configuration...] [options]" );
				break;
			}
		}
		new HelpFormatter().printHelp( usage.toString(), options );
	}
	
	public static void parseArgs( String[] args ) {
		registryGate = true;
		
		CommandLineParser parser = new GnuParser();
		CommandLine cl = null;
		try {
			cl = parser.parse( options, args );
		} catch ( ParseException e ) {
			logger.error( e.getMessage() );
			showHelp();
			System.exit( 1 );
		}
		if ( cl.hasOption( "help" ) ) {
			showHelp();
			System.exit( 0 );
		}

		for ( String configfile : cl.getArgs() )
			loadProperties( configfile );

		for ( Option opt : cl.getOptions() ) {
			String name = opt.getLongOpt();
			if ( cl.hasOption( name ) ) {
				String value;
				if ( options.getOption( name ).hasArg() )
					value = cl.getOptionValue( name );
				else
					value = "true";
				props.setProperty( name, value );
				try {
					accessors.get( name ).set( value );
				} catch ( IllegalAccessException e ) {
					// This error should have been caught back when build()
					// was called...
					logger.error( e.getMessage() );
				}
			}
		}
		
		int missing = 0;
		for ( String name : required ) {
			if ( ! props.containsKey( name ) ) {
				logger.error( "missing required property " + name );
				missing += 1;
			}
		}
		if ( missing > 0 )
			System.exit( 1 );
	}

	public static void loadProperties( String fname ) {
		registryGate = true;
		Properties newProps = new Properties();
		try {
			newProps.load( new FileReader( new File( fname ) ) );
		} catch (IOException e) {
			StringWriter buffer = new StringWriter();
			PrintWriter writer = new PrintWriter( buffer );
			e.printStackTrace( new PrintWriter( writer ) );
			logger.error( buffer.toString() );
			return;
		}
		
		for ( Map.Entry< String, FieldAccess<?> > me : accessors.entrySet() ) {
			String key = me.getKey();
			String value = null;
			if ( options.getOption( key ).hasArg() ) {
				value = newProps.getProperty( key );
			} else if ( props.getProperty( key ) != null ) {
				value = "true";
				props.setProperty( key, value );
			}
			if ( value != null ) {
				try {
					me.getValue().set( value.trim() );
				} catch (IllegalAccessException e) {
					// These error should have been caught back when build()
					// was called...
					logger.error( e.getMessage() );
				}
			}
		}
		
		props.putAll( newProps );
	}
	
	public static void storeProperties() {
		registryGate = true;
		Properties tmp = new Properties();
		for ( Map.Entry< String, FieldAccess<?> > me : accessors.entrySet() )
			tmp.setProperty( me.getKey(), props.getProperty( me.getKey() ) );
		try {
			tmp.store( System.out, null );
		} catch (IOException e) {
			logger.warn( e.getMessage() );
		}
	}
	
	private static class FieldAccess< T > {
		public FieldAccess( Field f, LexicalCast< T > cast ) {
			this.cast  = cast;
			this.field = f;
		}
		
		public T set( String value )
			throws IllegalArgumentException, IllegalAccessException
		{
			T t = cast.parse( value );
			field.set( null, t );
			return t;
		}

		private LexicalCast< T > cast;
		private Field field;
	}
	
	private static Options options;
	private static Properties props = new Properties();
	private static Set< String > required = new HashSet< String >();
	private static boolean registryGate = false;
	private static Set< Class< ? > > registry = new HashSet< Class< ? > >();
	private static Map< String, FieldAccess< ? > > accessors
		= new HashMap< String, FieldAccess< ? > >();

	static {
		options = new Options();
		options.addOption(
			new Option( "h", "help", false, "show this help and exit" )
		);
	}
	
	private OptionBuilder optbuilder;
	private Class< ? > owner;
	private LexicalCast< T > cast;
	private String varName;
	private String flagName;
	private String dflt;
}
