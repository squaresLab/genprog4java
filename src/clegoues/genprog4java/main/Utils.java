package clegoues.genprog4java.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;


// FIXME: move this to the actual utils package, because apparently that exists.
public class Utils {
	public static boolean runCommand(String commandToRun){
		Logger logger = Logger.getLogger(Utils.class);
	
        try {
        	
            Process p = Runtime.getRuntime().exec(commandToRun);
             
            int retValue = 0;
            try {
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
