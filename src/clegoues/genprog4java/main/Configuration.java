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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;

import clegoues.genprog4java.Fitness.Fitness.Fitness;
import clegoues.genprog4java.Search.Population;
import clegoues.genprog4java.Search.Search;
import clegoues.genprog4java.rep.CachingRepresentation;
import clegoues.genprog4java.rep.FaultLocRepresentation;
import clegoues.genprog4java.rep.JavaRepresentation;
import clegoues.genprog4java.rep.Representation;

public class Configuration {
	public static String sourceDir = "./";
	public static String outputDir = "./";
	public static String libs;
	public static String sourceVersion = "1.5";
	public static String targetVersion = "1.5";
	public static String globalExtension = ".java";
	public static String targetClassName = "";
	public static String searchStrategy = "ga";
	public static String javaRuntime = "";
	public static String javaVM;
	public static long seed;
	public static boolean doSanity = true;
	public static String packageName;
	public static Random randomizer = null;
	
	public Configuration() {}

	public Configuration(String configFile) {
		Configuration.setProperties(configFile);
	}


	public static void setProperties(String name)
	{
		Properties prop = new Properties();
		try
		{
			prop.load(new FileReader(new File(name)));
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		if(prop.getProperty("outputDir") != null) {
			outputDir = prop.getProperty("outputDir").trim();
		}
		if(prop.getProperty("search") != null) {
			searchStrategy = prop.getProperty("search").trim();
		}
		packageName = prop.getProperty("packageName").trim();
		javaVM = prop.getProperty("javaVM").trim();
		if(prop.getProperty("sourceDir") != null) {
		sourceDir = prop.getProperty("sourceDir").trim();
		}
		javaRuntime = Runtime.getRuntime().toString(); 
		libs = prop.getProperty("libs").trim();
		
		if(prop.getProperty("sourceVersion") != null) {
		sourceVersion = prop.getProperty("sourceVersion").trim();
		}
		if(prop.getProperty("targetVersion") != null) {
		targetVersion = prop.getProperty("targetVersion").trim();
		}
		if(prop.getProperty("sanity") != null) {
			String sanity = prop.getProperty("sanity").trim();
			if(sanity.equals("no")) { 
				doSanity = false;
			}
		}
		if(prop.getProperty("seed") != null) {
			seed = (long) Integer.parseInt(prop.getProperty("seed").trim());
		} else {
			seed = System.currentTimeMillis();
		}
		randomizer = new Random(seed);
		
		targetClassName = prop.getProperty("targetClassName").trim();

		Search.configure(prop);
		Population.configure(prop);
		Fitness.configure(prop);
		JavaRepresentation.configure(prop);
		FaultLocRepresentation.configure(prop); // FIXME probably there's a better way to do this?
		CachingRepresentation.configure(prop);

	}
}
