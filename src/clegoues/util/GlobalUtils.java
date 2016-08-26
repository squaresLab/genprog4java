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

package clegoues.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import clegoues.genprog4java.main.Configuration;

public class GlobalUtils {
	// range is inclusive!
	public static ArrayList<Integer> range(int start, int end) {
		ArrayList<Integer> returnVal = new ArrayList<Integer>();
		for(int i=start; i<=end; i++) {
			returnVal.add(i);
		}
		return returnVal;
	}

	public static Pair<?,Double> chooseOneWeighted(List<Pair<?,Double>> atoms) {
		assert(atoms.size() > 0);
		double totalWeight = 0.0;
		for(Pair<?,Double> atom : atoms) {
			totalWeight += atom.getRight();
		}
		assert(totalWeight > 0.0) ;
		double wanted = Configuration.randomizer.nextDouble() * totalWeight;
		double sofar = 0.0;
		for(Pair<?,Double> atom : atoms) {
			double here = sofar + atom.getRight();
			if(here >= wanted) {
				return atom;
			}
			sofar = here;
		}
		return null;
	}

	public static boolean probability(double p) {
		if(p < 0.0) return false;
		if(p > 1.0) return true;
		return Configuration.randomizer.nextDouble() <= p;
	}
	
	public static boolean runCommand(String commandToRun){
		Logger logger = Logger.getLogger(GlobalUtils.class);
	
        try {
        	
            Process p = Runtime.getRuntime().exec(commandToRun);
             
            int retValue = 0;
            try {
            	InputStream stdin = p.getInputStream();
                InputStreamReader isr = new InputStreamReader(stdin);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                logger.info("Output when running the command:");
                while ( (line = br.readLine()) != null)
                	logger.info(line);
                retValue = p.waitFor();            
            	
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
            
            if(retValue != 0)
            {
            	logger.error("Command " + commandToRun + " exited abnormally with status " + retValue);
            	 String line;
            	 logger.error("Stdout of command:");
            	  BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            	  while ((line = input.readLine()) != null) {
            	    System.out.println(line);
            	  }
            	  logger.error("Stderr of command:");
            	  input = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            	  while ((line = input.readLine()) != null) {
              	    System.out.println(line);
              	  }
              	  return false;
            }
         }
        catch (IOException e) {
            logger.error("Exception occurred executing command: " + commandToRun); 
            logger.error(e.getStackTrace());
            return false;
        }	
        return true;
	}
}
