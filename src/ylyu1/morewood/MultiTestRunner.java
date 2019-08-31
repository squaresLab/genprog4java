package ylyu1.morewood;

import clegoues.genprog4java.fitness.JUnitTestRunner;
import ylyu1.wean.Flusher;

import java.io.IOException;
import java.util.*;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class MultiTestRunner 
{
	public static final String SEPARATOR = "`";
	
	public static void main(String[] args)
	{
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run()
			{
			}
		});
		
		try
		{

		    System.out.println("Should initialize");	
			Flusher.initialize();
			String[] testsRaw = args[0].split(SEPARATOR);
			for(int i = 0; i < testsRaw.length; i++)
			{
				if(testsRaw[i].length()<3)continue;
				String[] intermed = testsRaw[i].split("::");
				String clazzName = intermed[0];
				String methodName = intermed[1];
				Class<?> clazzz = null;
				try {
					clazzz = Class.forName(clazzName);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					continue;
				} catch (ExceptionInInitializerError e){
					e.printStackTrace();
					continue;
				}
				
				Request request = Request.method(clazzz, methodName);
			
			    JUnitCore runner = new JUnitCore();
			    Result r = runner.run(request);
			
			    System.out.println("[SUCCESS]:" + r.wasSuccessful());
			    System.out.println("[TOTAL]:" + r.getRunCount());
			    System.out.println("[FAILURE]:" + r.getFailureCount());
			
			}
			
			try {
				Flusher.flushOut();
			} catch (IOException e) {
				e.printStackTrace();
			}
	
		} catch(Throwable e)
		{
			e.printStackTrace();
			Runtime.getRuntime().exit(1);
		}
		finally {
			Runtime.getRuntime().exit(0);
		}
		
		
	}
}

