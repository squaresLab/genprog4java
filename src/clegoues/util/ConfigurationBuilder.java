/*
 * Copyright (c) 2014-2016, 
 *  Claire Le Goues     <clegoues@cs.cmu.edu>
 *  Jonathan Dorn       <dorn@virginia.edu>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. The names of the contributors may not be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package clegoues.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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

/**
 * Allows the construction of configuration parameters that may be set by the
 * user on the command-line, in a properties file, or both. This implementation
 * assumes that all parameter values are ultimately stored as static fields of
 * a class. The instance methods allow the parameter to be configured
 * appropriately before it is instantiated. The static methods provide support
 * for parsing the arguments, generating help text, and loading properties files
 * in accordance with the parameters built through this class.
 * <p>
 * Typical usage, assuming appropriate static imports for brevity, may look
 * like:
 * <pre>
 * public static double myDouble = ConfigurationBuilder.of(DOUBLE)
 *     .withVarName("myDouble")
 *     .withDefault("1.0")
 *     .withHelp("my double value to use everywhere")
 *     .build();
 * </pre>
 * Note that the field name must be specified using {@link
 * #withVarName(String)}. Also note that default values must be specified as a
 * string, since they will be interpreted the same way as user arguments.
 * <p>
 * By default, all options appear in a single group with
 * no heading; options may be assigned to groups using {@link #inGroup(String)}.
 * The built-in {@link LexicalCast} instances handle most uses of primitive
 * types. However, custom implementations may be assigned with {@link
 * #withCast(LexicalCast)} or {@link #of(LexicalCast)} to allow user-defined
 * data types or side-effects.
 * <p>
 * Every class that uses parameters built using this mechanism should also have
 * a {@link RegistryToken}. This token should be registered (via {@link
 * #register(RegistryToken)}) by the {@code * main()} method before parsing
 * the arguments. This ensures that the registered class is fully loaded and the
 * option data structures are configured properly.
 * <pre>
 * public static final RegistryToken token = ConfigurationBuilder.getToken();
 * </pre>
 * 
 * @author jonathan
 *
 * @param <T> the type of value stored in the field.
 */
public class ConfigurationBuilder< T > {
	protected static Logger logger =
		Logger.getLogger( ConfigurationBuilder.class );

	// FIXME: char?
	/**
	 * Generates an option that takes no arguments and sets the field to
	 * {@code true} when it is used. Similarly, any appearance of the option
	 * name in a properties file will assign {@code true} to the field.
	 */
	public static final LexicalCast< Boolean > BOOLEAN = Boolean::valueOf;
	
	/**
	 * Generates an option that takes an argument and assigns a boolean. Values
	 * that match {@code 1}, {@code true}, or {@code yes} (ignoring case) will
	 * be considered true. Values that match {@code 0}, {@code false}, or
	 * {@code no} will be considered false.
	 */
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
		
	/**
	 * Interprets option arguments as {@code byte} values.
	 */
	public static final LexicalCast< Byte > BYTE    = Byte::valueOf;
	
	/**
	 * Interprets option arguments as {@code double} values.
	 */
	public static final LexicalCast< Double > DOUBLE  = Double::valueOf;
	
	/**
	 * Interprets option arguments as {@code float} values.
	 */
	public static final LexicalCast< Float > FLOAT   = Float::valueOf;
	
	/**
	 * Interprets option arguments as {@code int} values.
	 */
	public static final LexicalCast< Integer > INT     = Integer::valueOf;
	
	/**
	 * Interprets option arguments as {@code long} values.
	 */
	public static final LexicalCast< Long > LONG    = Long::valueOf;
	
	/**
	 * Interprets option arguments as {@code short} values.
	 */
	public static final LexicalCast< Short > SHORT   = Short::valueOf;
	
	/**
	 * Interprets option arguments as {@code String}s. That is, the arguments
	 * are passed to the field unchanged.
	 */
	public static final LexicalCast< String > STRING  = (String v) -> v;

	/**
	 * Creates a new {@code ConfigurationBuilder} instance that uses the given
	 * {@code cast}. This is a convenience method to allow the Java type checker
	 * to deduce the type parameter, which is the return type of {@link
	 * #build(}.
	 * <p>
	 * This method assumes the field containing the parameter value is in the
	 * calling class.
	 * 
	 * @param cast the cast operation indicating the field type
	 * 
	 * @return a new {@code ConfigurationBuilder} instance.
	 */
	public static < T > ConfigurationBuilder< T > of( LexicalCast< T > cast ) {
		return new ConfigurationBuilder< T >().withCast( cast );
	}

	/**
	 * Interface for interpreting arguments as values of an appropriate
	 * datatype.
	 * 
	 * @param <T> the type to interpret the argument as
	 */
	public static interface LexicalCast< T > {
		public T parse( String value );
	}
	
	/**
	 * Token used to ensure that all options and fields are properly recorded
	 * before parsing arguments. Callers should not instantiate this class
	 * themselves. Instead, use the {@link ConfigurationBuilder#getToken()}
	 * method to acquire a token.
	 */
	public static interface RegistryToken {
		public Class< ? > getOwnerClass();
	}
	
	/**
	 * Creates a new {@code ConfigurationBuilder}. This is a more verbose
	 * alternative to the {@link #of} method. Assumes the field containing the
	 * parameter value is in the calling class.
	 */
	public ConfigurationBuilder() {
		this( getCallingClass() );
	}

	/**
	 * Creates a new {@code ConfigurationBuilder}. This is a more verbose
	 * alternative to the {@link of} method. The field containing the parameter
	 * value will be found in the {@code owner} class.
	 * 
	 * @param owner class containing the field  that will contain this parameter
	 *              value
	 */
	public ConfigurationBuilder( Class< ? > owner ) {
		this.optbuilder = OptionBuilder.hasArg();
		this.varName    = null;
		this.flagName   = null;
		this.cast       = null;
		this.dflt       = null;
		this.owner      = owner;
		this.groupName  = "";
	}
	
	/**
	 * Specifies the name of the group this option should appear with in the
	 * help text.
	 * 
	 * @param groupName the name of the group
	 * 
	 * @return a reference to this builder
	 */
	public ConfigurationBuilder< T > inGroup( String groupName ) {
		this.groupName = groupName;
		return this;
	}

	/**
	 * Specifies the mechanism for interpreting the argument string as a value
	 * of the appropriate data type.
	 * 
	 * @param cast the function object that will interpret values
	 * 
	 * @return a reference to this builder
	 */
	@SuppressWarnings("static-access")
	public ConfigurationBuilder< T > withCast( LexicalCast< T > cast ) {
		this.cast = cast;
		if ( cast == BOOLEAN )
			this.optbuilder.hasArg( false );
		else
			this.optbuilder.hasArg( true );
		return this;
	}
	
	/**
	 * Specifies the default value for this option.
	 * 
	 * @param dflt the default value for this option
	 * 
	 * @return a reference to this builder
	 */
	public ConfigurationBuilder< T > withDefault( String dflt ) {
		this.dflt = dflt;
		return this;
	}
	
	/**
	 * Specifies the flag and property name for this option. If this is not
	 * specified, it defaults to the name of the field.
	 * 
	 * @param flagName the flag and property name for this option.
	 * 
	 * @return a reference to this builder
	 */
	@SuppressWarnings( "static-access" )
	public ConfigurationBuilder< T > withFlag( String flagName ) {
		optbuilder.withLongOpt( flagName );
		this.flagName = flagName;
		return this;
	}
	
	/**
	 * Specifies the description of this option for use in help text.
	 * 
	 * @param helpText the description of this option
	 *
	 * @return a reference to this builder
	 */
	@SuppressWarnings( "static-access" )
	public ConfigurationBuilder< T > withHelp( String helpText ) {
		optbuilder.withDescription( helpText );
		return this;
	}
	
	/**
	 * Specifies the name of the field to write the option value to. This may
	 * also be used as the flag and property name if they are not specified with
	 * {@link #withFlag(String)}.
	 * 
	 * @param var the name of the field to write the option value to
	 * 
	 * @return a reference to this builder
	 */
	@SuppressWarnings("static-access")
	public ConfigurationBuilder< T > withVarName( String var ) {
		this.varName = var;
		if ( this.flagName == null )
			optbuilder.withLongOpt( varName );
		return this;
	}
	
	/**
	 * Generates the command-line option and property configured by this object
	 * and returns the default value (or null if there is no default). If the
	 * field is in a class that has not been registered and the arguments have
	 * already been parsed or the help text generated, a warning will be logged.
	 * 
	 * @return the default value, parsed into the appropriate data type
	 */
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
		if ( ! groups.containsKey( groupName ) )
			groups.put( groupName, new Options() );
		groups.get( groupName ).addOption( opt );
		return result;
	}
	
	/**
	 * Helper function to determine the name of the class that called methods
	 * in this class. Used to auto-detect the class containing the option
	 * fields.
	 * 
	 * @return the class that called methods in this class
	 */
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

	/**
	 * Returns a token that can be used to {@link #register(RegistryToken)} the
	 * calling class.
	 * 
	 * @return a token that can be used to register the calling class.
	 */
	public static RegistryToken getToken() {
		final Class< ? > caller = getCallingClass();
		return () -> caller;
	}

	/**
	 * Registers the class represented by the given token. This is intended to
	 * help ensure that classes are fully loaded, including and fields
	 * corresponding to command-line options, before parsing the command-line.
	 * 
	 * @param token the token assigned to the class being registered
	 */
	public static void register( RegistryToken token ) {
		registry.add( token.getOwnerClass() );
	}

	/**
	 * Prints the help text to standard output. This is equivalent to calling
	 * {@link #showHelp(PrintWriter)} with {@link System#out}.
	 */
	public static void showHelp() {
		showHelp( new PrintWriter( System.out ) );
	}

	/**
	 * Prints the help text to the given writer. The usage information is
	 * automatically determined by walking the call stack until a method named
	 * {@code main} is encountered. If this method is not called from the main
	 * thread, it may not be able to determine the name of the top-level class.
	 * 
	 * @param pw the writer to print help text to
	 */
	public static void showHelp( PrintWriter pw ) {
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
		
		// Since we are writing each group independently, the descriptions will
		// appear ragged. To prevent this, we must pad the descriptions
		// associated with shorter flags to align with the ones for longer
		// flags.
		
		int width = 0;
		Map< String, Integer > padding = new HashMap< String, Integer >();
		for ( Map.Entry< String, Options > me : groups.entrySet() ) {
			int local = 0;
			for ( Object obj : me.getValue().getOptions() ) {
				Option opt = (Option) obj;
				int len = opt.getLongOpt().length();
				width = Math.max( width, len );
				local = Math.max( local, len );
			}
			padding.put( me.getKey(), local );
		}

		HelpFormatter formatter = new HelpFormatter();
		formatter.printUsage( pw, 80, usage.toString() );
		for ( Map.Entry< String, Options > me : groups.entrySet() ) {
			pw.println();
			if ( ! me.getKey().isEmpty() ) {
				pw.println( me.getKey() );
				pw.println();
			}
			int descPad = width - padding.get( me.getKey() );
			formatter.printOptions( pw, 80, me.getValue(), 2, descPad + 2 );
		}
		pw.flush();
	}
	
	/**
	 * Parses the given arguments and assigns values to the appropriate fields.
	 * All non-option arguments are assumed to be properties files, which are
	 * loaded first, before the command-line flags are processed.
	 * <p>
	 * This method will terminate the program if the user requests help text or
	 * if any required values (i.e., those without defaults) are missing.
	 * 
	 * @param args the arguments to parse
	 */
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

	/**
	 * Loads a properties file and assigns values to the appropriate fields.
	 * 
	 * @param fname the name of the properties file to load
	 */
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
			} else if ( newProps.getProperty( key ) != null ) {
				value = "true";
				newProps.setProperty( key, value );
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
	
	/**
	 * Writes the configured values to standard output. This is equivalent to
	 * calling {@link #storeProperties(OutputStream)} with {@link System#out}.
	 */
	public static void storeProperties() {
		storeProperties( System.out );
	}
	
	/**
	 * Writes the configured values to the given output stream. This includes
	 * the values specified in properties files or on the command-lien as well
	 * as all default values for the options that were not assigned by the user.
	 * 
	 * @param out the output stream to write to
	 */
	public static void storeProperties( OutputStream out ) {
		registryGate = true;
		Properties tmp = new Properties();
		for ( Map.Entry< String, FieldAccess<?> > me : accessors.entrySet() )
			tmp.setProperty( me.getKey(), props.getProperty( me.getKey() ) );
		try {
			tmp.store( out, null );
		} catch (IOException e) {
			logger.warn( e.getMessage() );
		}
	}
	
	/**
	 * Helper class to mediate parsing an argument and assigning it to a field.
	 * These may be stored as values in a map, allowing convenient access while
	 * parsing the command-line and properties files.
	 * <p>
	 * This approach is reasonably type-safe, since the value assigned to the
	 * field must match the return type of {@link ConfigurationBuilder#build()}.
	 * As long as the user assigns the result of {@code build()} directly to the
	 * field, the type checker will ensure that the type is valid at that point.
	 * 
	 * @param <T> the type of value to assign to the field
	 */
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
	
	/**
	 * The set of all options that may be passed on the command-line.
	 */
	private static Options options;
	
	/**
	 * Accumulated properties from all sources.
	 */
	private static Properties props = new Properties();
	
	/**
	 * The set of flag names that are not associated with default values. These
	 * flags must be specified in some way for argument parsing to succeed.
	 */
	private static Set< String > required = new HashSet< String >();
	
	/**
	 * Indicates whether the options have been used. Before the options are
	 * used, it is not a problem to see new options from an unregistered source.
	 * After they are used, however, any new options indicate a logic error that
	 * could result in an incomplete set of options being parsed.
	 */
	private static boolean registryGate = false;

	/**
	 * Simple registry of classes to help detect logic errors as noted in
	 * {@link #registryGate}.
	 */
	private static Set< Class< ? > > registry = new HashSet< Class< ? > >();
	
	/**
	 * The groups of options for use when generating the help text.
	 */
	private static Map< String, Options > groups =
		new LinkedHashMap< String, Options >();
	
	/**
	 * The {@link FieldAccess} objects used to assign values to fields when
	 * the corresponding flag or property is recognized.
	 */
	private static Map< String, FieldAccess< ? > > accessors =
		new HashMap< String, FieldAccess< ? > >();

	// Initialize the options to include a --help flag.
	static {
		Option help = 
			new Option( "h", "help", false, "show this help and exit" );
		options = new Options();
		options.addOption( help );
		groups.put( "", new Options() );
		groups.get( "" ).addOption( help );
	}
	
	/**
	 * The {@code OptionBuilder} used for constructing command-line options.
	 */
	private OptionBuilder optbuilder;
	
	/**
	 * The class containing the field to assign to.
	 */
	private Class< ? > owner;
	
	/**
	 * The function object to convert arguments into appropriate data types.
	 */
	private LexicalCast< T > cast;
	
	/**
	 * The name of the field to assign to.
	 */
	private String varName;
	
	/**
	 * The name of the flag or property to recognize.
	 */
	private String flagName;
	
	/**
	 * The default value for this field.
	 */
	private String dflt;
	
	/**
	 * The name of the group this option should appear in when the help text
	 * is written.
	 */
	private String groupName;
}
