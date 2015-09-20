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
import clegoues.genprog4java.util.Pair;

public class Configuration {
	protected static Logger logger = Logger.getLogger(Configuration.class);

	public static String sourceDir = "./";
	public static String outputDir = "./";
	public static String libs;
	public static String sourceVersion = "1.6";
	public static String targetVersion = "1.6";
	public static String globalExtension = ".java";
	public static ArrayList<Pair<String,String>> targetClassNames = new ArrayList<Pair<String,String>>();
	public static String javaRuntime = "";
	public static String javaVM;
	public static String jacocoPath = "";
	public static long seed;
	public static boolean doSanity = true;
	public static String workingDir = "./";
	public static Random randomizer = null;
	public static String compileCommand = "";

	public Configuration() {
	}

	public Configuration(String configFile) {
		Configuration.setProperties(configFile);
	}

	public static void setProperties(String name) {
		Properties prop = new Properties();
		try {
			prop.load(new FileReader(new File(name)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (prop.getProperty("outputDir") != null) {
			outputDir = prop.getProperty("outputDir").trim();
		}
		javaVM = prop.getProperty("javaVM").trim();
		if (prop.getProperty("sourceDir") != null) {
			sourceDir = prop.getProperty("sourceDir").trim();
		}
		javaRuntime = Runtime.getRuntime().toString();
		libs = prop.getProperty("libs").trim();

		if (prop.getProperty("sourceVersion") != null) {
			sourceVersion = prop.getProperty("sourceVersion").trim();
		}
		if (prop.getProperty("targetVersion") != null) {
			targetVersion = prop.getProperty("targetVersion").trim();
		}
		if (prop.getProperty("jacocoPath") != null) {
			jacocoPath = prop.getProperty("jacocoPath").trim();
		}
		if (prop.getProperty("sanity") != null) {
			String sanity = prop.getProperty("sanity").trim();
			if (sanity.equals("no")) {
				doSanity = false;
			}
		}
		if (prop.getProperty("seed") != null) {
			seed = (long) Integer.parseInt(prop.getProperty("seed").trim());
		} else {
			seed = System.currentTimeMillis();
		}
		randomizer = new Random(seed);


		if (prop.getProperty("workingDir") != null) {
			workingDir = prop.getProperty("workingDir").trim();
		}


		try {
			targetClassNames.addAll(getClasses(prop.getProperty(
					"targetClassName").trim()));
		} catch (Exception e) {
			// FIXME handle exception
			e.printStackTrace();
		}

		Search.configure(prop);
		Population.configure(prop);
		Fitness.configure(prop);
		JavaRepresentation.configure(prop);
		FaultLocRepresentation.configure(prop);
		CachingRepresentation.configure(prop);
	}

	public static Pair<String,String> getClassAndPackage(String fullName) {
		String s = fullName.trim();
		int startOfClass = s.lastIndexOf('.');
		String justClass = s.substring(startOfClass + 1, s.length());
		String packagePath = s.substring(0,startOfClass).replace('.', '/');
		return new Pair<String,String>(justClass,packagePath );
	}
	
	public static ArrayList<Pair<String,String>> getClasses(String filename)
			throws IOException, FileNotFoundException {
		ArrayList<Pair<String,String>> returnValue = new ArrayList<Pair<String,String>>();
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
		
		String original = Configuration.workingDir + File.separatorChar + Configuration.outputDir  + File.separatorChar + "original" + File.separatorChar;

		//copy the target classes to an "original" folder; we will work from there.
		File createFile = new File(original);
		createFile.mkdirs();

		String sourceDirPath =  Configuration.workingDir + File.separatorChar + Configuration.sourceDir + File.separatorChar;
		
		for( Pair<String,String> fileInfo : Configuration.targetClassNames ){
			String className = fileInfo.getFirst();
			String packagePath = fileInfo.getSecond();
			String pathToFile = "";
					
			String topLevel = sourceDirPath + className + ".java";
			
			File classFile = new File(topLevel);
			if(classFile.exists()) {
				pathToFile = topLevel;
			} else {
				pathToFile = sourceDirPath + packagePath;
			}
			String cmd = "cp " + pathToFile + " " + original + packagePath + File.separatorChar;
			Utils.runCommand(cmd);
		}	
	}
}