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
import java.io.IOException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import clegoues.genprog4java.Search.Population;
import clegoues.genprog4java.Search.Search;
import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.mut.JavaEditOperation;
import clegoues.genprog4java.rep.CachingRepresentation;
import clegoues.genprog4java.rep.FaultLocRepresentation;
import clegoues.genprog4java.rep.JavaRepresentation;
import clegoues.genprog4java.rep.LocalizationRepresentation;
import clegoues.genprog4java.rep.Representation;
import clegoues.genprog4java.rep.UnexpectedCoverageResultException;
import clegoues.util.ConfigurationBuilder;

public class Main {
	protected static Logger logger = Logger.getLogger(Main.class);

	public static void main(String[] args) throws IOException,
			UnexpectedCoverageResultException {
		//FIXME: looks like it is not finding the properties file so it uses the default one, with just errors
		//BasicConfigurator.configure();
		//logger.debug("Sample debug message");
		//logger.info("Sample info message");
		//logger.warn("Sample warn message");
		//logger.error("Sample error message");
		//logger.fatal("Sample fatal message");
		System.out.println(System.getProperty("log4j.configuration")); 
		Search searchEngine = null;
		Representation baseRep = null;
		Fitness fitnessEngine = null;
		Population incomingPopulation = null;
		assert (args.length > 0);
		long startTime = System.currentTimeMillis();
		BasicConfigurator.configure();

		ConfigurationBuilder.register( Configuration.token );
		ConfigurationBuilder.register( Fitness.token );
		ConfigurationBuilder.register( CachingRepresentation.token );
		ConfigurationBuilder.register( FaultLocRepresentation.token );
		ConfigurationBuilder.register( JavaRepresentation.token );
		ConfigurationBuilder.register( Population.token );
		ConfigurationBuilder.register( Search.token );
		ConfigurationBuilder.parseArgs( args );
		Configuration.configure();
		ConfigurationBuilder.storeProperties();

		File workDir = new File(Configuration.outputDir);
		if (!workDir.exists())
			workDir.mkdir();
		logger.info("Configuration file loaded");
		
		if (Configuration.globalExtension == ".java") {
			if (Search.searchStrategy.equals("io")) {
				baseRep = (Representation) new LocalizationRepresentation();
			} else {
				baseRep = (Representation) new JavaRepresentation();
			}
			fitnessEngine = new Fitness<JavaEditOperation>();
			searchEngine = new Search<JavaEditOperation>(fitnessEngine);
			incomingPopulation = new Population<JavaEditOperation>(); 
		}
		// loads the class file into the representation.
		// Does the Following:
		// 1) If "yes" in sanity check in Configuration file, then does sanity
		// check.
		// 2)
		baseRep.load(Configuration.targetClassNames);

		try {
			switch (Search.searchStrategy) {
			case "ga":
				searchEngine.geneticAlgorithm(baseRep, incomingPopulation);
				break;
			case "brute":
				searchEngine.bruteForceOne(baseRep);
				break;
			case "oracle":
				searchEngine.oracleSearch(baseRep);
				break;
			case "io":
				searchEngine.ioSearch(baseRep);
				break;
			}
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		int elapsed = getElapsedTime(startTime);
		logger.info("\nTotal elapsed time: " + elapsed + "\n");
		Runtime.getRuntime().exit(0);
	}

	private static int getElapsedTime(long start) {
		return (int) (System.currentTimeMillis() - start) / 1000;
	}
}