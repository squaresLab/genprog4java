package clegoues.genprog4java.main;

import java.io.IOException;

import org.apache.log4j.Logger;

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
