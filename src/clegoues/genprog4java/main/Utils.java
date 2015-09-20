package clegoues.genprog4java.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import clegoues.genprog4java.rep.JavaRepresentation;

public class Utils {
	public static boolean runCommand(String commandToRun){
		Logger logger = Logger.getLogger(JavaRepresentation.class);
		String s = null;
	
        try {
        	
            // using the Runtime exec method:
            Process p = Runtime.getRuntime().exec(commandToRun);
             
            BufferedReader stdInput = new BufferedReader(new
                 InputStreamReader(p.getInputStream()));
 
            BufferedReader stdError = new BufferedReader(new
                 InputStreamReader(p.getErrorStream()));
 
            // read the output from the command

            logger.info("Here is the standard output of the command:\n");
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }
             
            // read any errors from the attempted                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm          mmmmm,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,                      mn     command
            logger.info("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) 
        	{
            	logger.error(s);
            }
            
            int retValue = 0;
            try {
            	retValue = p.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
            
            if(retValue != 0)
            {
            	logger.error("Exit value of the command was different from 0");
            	return false;
            }
             
            //System.exit(0);
        }
        catch (IOException e) {
            logger.error("Exception happened with the command: ");
            logger.error(e.getStackTrace());
            return false;
            //System.exit(-1);
        }	
        return true;
	}
}
