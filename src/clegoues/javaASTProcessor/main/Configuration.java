package clegoues.javaASTProcessor.main;

import static clegoues.util.ConfigurationBuilder.BOOL_ARG;
import static clegoues.util.ConfigurationBuilder.LONG;
import static clegoues.util.ConfigurationBuilder.STRING;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import clegoues.util.ConfigurationBuilder;

public class Configuration {
	protected static Logger logger = Logger.getLogger(Configuration.class);

	public static final ConfigurationBuilder.RegistryToken token =
		ConfigurationBuilder.getToken();

	//public static String sourceDir = "./";
	public static String sourceDir = ConfigurationBuilder.of( STRING )
		.withVarName( "sourceDir" )
		.withDefault( "./" )
		.withHelp( "directory containing the source files" )
		.build();
	
	public static String libs = ConfigurationBuilder.of( STRING )
		.withVarName( "libs" )
		.withHelp( "classpath to compile the project" )
		.build();
	
	public static ArrayList<String> targetClassNames =
		new ConfigurationBuilder< ArrayList< String > >()
			.withVarName( "targetClassNames" )
			.withFlag( "targetClassName" )
			.withDefault( "" )
			.withHelp( "the class(es) to process" )
			.withCast( new ConfigurationBuilder.LexicalCast< ArrayList< String > >(){
				public ArrayList<String> parse( String value ) {
					if ( ! value.isEmpty() ) {
						try {
							return getClasses( value );
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					return new ArrayList< String >();
				}
				
			} )
			.build();

	public static ArrayList<String> getClasses(String filename)
			throws IOException, FileNotFoundException {
		ArrayList<String> returnValue = new ArrayList<String>();
		String ext = FilenameUtils.getExtension(filename);
		if (ext.equals("txt")) {
			FileInputStream fis;
			fis = new FileInputStream(filename);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String line;
			while(((line = br.readLine()) != null) && !line.isEmpty()) {
				returnValue.add(line.trim());
			}
			br.close();
		} else {
			returnValue.add(filename);
		}

		return returnValue;
	}

}
