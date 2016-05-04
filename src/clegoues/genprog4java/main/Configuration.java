/*
 * Copyright (c) 2014-2015, 
 *  Claire Le Goues     <clegoues@cs.cmu.edu>
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

package clegoues.genprog4java.main;

import static clegoues.util.ConfigurationBuilder.BOOL_ARG;
import static clegoues.util.ConfigurationBuilder.LONG;
import static clegoues.util.ConfigurationBuilder.STRING;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import clegoues.genprog4java.Search.Population;
import clegoues.genprog4java.Search.Search;
import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.rep.CachingRepresentation;
import clegoues.genprog4java.rep.FaultLocRepresentation;
import clegoues.genprog4java.rep.JavaRepresentation;
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
	//public static String outputDir = "./tmp/";
	public static String outputDir = ConfigurationBuilder.of( STRING )
		.withVarName( "outputDir" )
		.withDefault( "./tmp/" )
		.withHelp( "directory to contain generated files" )
		.build();
	public static String libs = ConfigurationBuilder.of( STRING )
		.withVarName( "libs" )
		.withHelp( "classpath to compile the project" )
		.build();
	//public static String sourceVersion = "1.6";
	public static String sourceVersion = ConfigurationBuilder.of( STRING )
		.withVarName( "sourceVersion" )
		.withDefault( "1.6" )
		.withHelp( "Java version of the source code" )
		.build();
	//public static String targetVersion = "1.6";
	public static String targetVersion = ConfigurationBuilder.of( STRING )
		.withVarName( "targetVersion" )
		.withDefault( "1.6" )
		.withHelp( "Java version of the generated classes" )
		.build();
	//public static String globalExtension = ".java";
	public static String globalExtension = ConfigurationBuilder.of( STRING )
		.withVarName( "globalExtension" )
		.withDefault( ".java" )
		.withHelp( "source file extension" )
		.build();
	//public static ArrayList<ClassInfo> targetClassNames = new ArrayList<ClassInfo>();
	public static ArrayList<ClassInfo> targetClassNames =
		new ConfigurationBuilder< ArrayList< ClassInfo > >()
			.withVarName( "targetClassNames" )
			.withFlag( "targetClassName" )
			.withDefault( "" )
			.withHelp( "the class(es) to repair" )
			.withCast( new ConfigurationBuilder.LexicalCast< ArrayList< ClassInfo > >(){
				public ArrayList<ClassInfo> parse( String value ) {
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
					return new ArrayList< ClassInfo >();
				}
				
			} )
			.build();
	//public static String javaRuntime = "";
	public static String javaRuntime = ConfigurationBuilder.of( STRING )
		.withVarName( "javaRuntime" )
		.withDefault( Runtime.getRuntime().toString() )
		.withHelp( "the java runtime to use" )
		.build();
	public static String javaVM = ConfigurationBuilder.of( STRING )
		.withVarName( "javaVM" )
		.withHelp( "path to Java" )
		.build();
	//public static String testClassPath = "";
	public static String testClassPath = ConfigurationBuilder.of( STRING )
		.withVarName( "testClassPath" )
		.withDefault( "" )
		.withHelp( "classpath to run the tests" )
		.build();
	//public static String srcClassPath = "";
	public static String srcClassPath = ConfigurationBuilder.of( STRING )
		.withVarName( "srcClassPath" )
		.withDefault( "" )
		.withHelp( "another classpath" )
		.build();
	//public static String jacocoPath = "";
	public static String jacocoPath = ConfigurationBuilder.of( STRING )
		.withVarName( "jacocoPath" )
		.withDefault( "" )
		.withHelp( "path to javaagent JAR file" )
		.build();
	public static Random randomizer;
	public static long seed = ConfigurationBuilder.of( LONG )
		.withVarName( "seed" )
		.withDefault( Long.toString( System.currentTimeMillis() ) )
		.withHelp( "seed to initialize random number generator" )
		.withCast( new ConfigurationBuilder.LexicalCast< Long >() {
			public Long parse(String value) {
				long seed = Long.valueOf( value );
				randomizer = new Random( seed );
				return seed;
			}
		} )
		.build();
	//public static boolean doSanity = true;
	public static boolean doSanity = ConfigurationBuilder.of( BOOL_ARG )
		.withVarName( "doSanity" )
		.withFlag( "sanity" )
		.withDefault( "true" )
		.withHelp( "indicates whether to run sanity check" )
		.build();
	//public static String workingDir = "./";
	public static String workingDir = ConfigurationBuilder.of( STRING )
		.withVarName( "workingDir" )
		.withDefault( "./" )
		.withHelp( "directory containing the source directory" )
		.build();
	//public static String classSourceFolder = "";
	public static String classSourceFolder = ConfigurationBuilder.of( STRING )
		.withVarName( "classSourceFolder" )
		.withDefault( "" )
		.withHelp( "directory to contain compiled classes" )
		.build();
	//public static String classTestFolder = "";
	public static String classTestFolder = ConfigurationBuilder.of( STRING )
		.withVarName( "classTestFolder" )
		.withDefault( "" )
		.withHelp( "unused" )
		.build();
	//public static String compileCommand = "";
	public static String compileCommand = ConfigurationBuilder.of( STRING )
		.withVarName( "compileCommand" )
		.withDefault( "" )
		.withHelp( "command for compiling the program" )
		.build();

	private Configuration() {}

	public static void configure() {
		Fitness.configure();
		
		//Save original target file to an outside folder if it is the first run. Or load it if it is not.
		saveOrLoadTargetFiles();
	}
	
	public static void saveOrLoadTargetFiles(){
		
		String safeFolder = Configuration.outputDir  + File.separatorChar + "original" + File.separatorChar;
		
		//If there is a variant already created in the output folder then it is not the first run
		File variant0Folder = new File(Configuration.outputDir  + File.separatorChar + "variant0"  + File.separatorChar );
		if (variant0Folder.exists()){
			
			for( ClassInfo s : Configuration.targetClassNames ){
				//overwrite the targetClass with the one saved before
				Utils.runCommand("cp " + safeFolder + s.pathToJavaFile() + " " + Configuration.workingDir + Configuration.sourceDir + File.separatorChar + s.getPackage());
			}
			
		//else 	it is the first run
		}else{
			//create safe folder and save the original target class
			saveTargetFiles();
		}
		
	}

	public static ClassInfo getClassAndPackage(String fullName) {
		String s = fullName.trim();
		int startOfClass = s.lastIndexOf('.');
		String justClass = s.substring(startOfClass + 1, s.length());
		String packagePath = s.substring(0,startOfClass).replace('.', '/');
		return new ClassInfo(justClass,packagePath );
	}
	
	public static ArrayList<ClassInfo> getClasses(String filename)
			throws IOException, FileNotFoundException {
		ArrayList<ClassInfo> returnValue = new ArrayList<ClassInfo>();
		String ext = FilenameUtils.getExtension(filename);
		if (ext.equals("txt")) {
			FileInputStream fis;
			fis = new FileInputStream(filename);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String line;
			while(((line = br.readLine()) != null) && !line.isEmpty()) {
				returnValue.add(getClassAndPackage(line));
			}
			br.close();
		} else {
			returnValue.add(getClassAndPackage(filename));
		}

		return returnValue;
	}
	
	public static void saveTargetFiles() {
		
		String original = Configuration.outputDir  + File.separatorChar + "original" + File.separatorChar;

		//copy the target classes to an "original" folder; we will work from there.
		File createFile = new File(original);
		createFile.mkdirs();

		String sourceDirPath =  Configuration.workingDir + File.separatorChar + Configuration.sourceDir + File.separatorChar;
		
		for( ClassInfo fileInfo : Configuration.targetClassNames ){
			String className = fileInfo.getClassName();
			String packagePath = fileInfo.getPackage();
			String pathToFile = "";
					
			String topLevel = sourceDirPath + className + ".java";
			
			File classFile = new File(topLevel);
			if(classFile.exists()) {
				pathToFile = topLevel;
			} else {
				pathToFile = sourceDirPath + packagePath + File.separatorChar + className + ".java";
			}
			File packagePathFile = new File(original + packagePath);
			packagePathFile.mkdirs();
			String cmd = "cp " + pathToFile + " " + original + packagePath + File.separatorChar;
			Utils.runCommand(cmd);
		}	
	}
}
