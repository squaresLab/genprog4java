package clegoues.genprog4java.main;
import java.io.File;
import java.io.IOException;

public class Main {
	public static void main(String[] args) throws IOException
	{
		StringBuffer buf = new StringBuffer();
		buf.append(Main.class.getName()+"\n");
		
		for(String str : args)
		{
			buf.append(str+"\n");
		}
		
		PropertyConfigurator.configure("log4j.properties");
		
		if( args.length > 0 )
		{
			Constants.setProperties(args[0]);
		}
		else
		{
			Constants.setProperties(PROPERTIES_FILE);
		}
		
		File tmp = new File(Constants.outputDir);
		if(!(tmp.exists() && tmp.isDirectory()))
		{
			boolean result = tmp.mkdirs();
			if(!result)
			{
				logger.error("Unable to create temporary directories.");
				System.err.println("Unable to create temporary directories.");
				Runtime.getRuntime().exit(1);
			}
		}
		
		logger.info("Configuration file succefully loaded.");
		
		int iteration = 1;
		
		if(args.length > 1)
			iteration = Integer.parseInt(args[1]);
		
		logger.info("Total: "+ iteration + " runs.");
		
		long startTime = System.currentTimeMillis();
		
		try
		{
			for(int i = 0; i < iteration; i++)
			{
				logger.info("Iteration: " + i);
				GPProcessor gpp = new GPProcessor();
				
				gpp.doGP();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			buf.append(e.getMessage()+"\n");
			success = "Failure";
		}
		finally
		{
			try
			{
				int elapsed = getElapsedTime(startTime);
				buf.append("\nTotal elapsed Time: " + elapsed + "\n");
				
				/*SMTPClient.send("put smtp server here", 25, "sender", 
						"destination", "username", "password",
						"message", MailSecurity.TLS);*/
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			Runtime.getRuntime().exit(0);
		}
		Runtime.getRuntime().exit(0);
	}

	private static int getElapsedTime(long start)
	{
		return (int) ( System.currentTimeMillis() - start ) / 1000;
	}
}
}
