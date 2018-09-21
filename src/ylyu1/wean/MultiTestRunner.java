package ylyu1.wean;

import clegoues.genprog4java.fitness.JUnitTestRunner;

import java.io.IOException;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class MultiTestRunner 
{
	public static final String SEPARATOR = "`";
	
	/*Only works for classes, doesn't work for method level granularity (yet)*/
	public static void main(String[] args)
	{
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run()
			{
				//Runtime.getRuntime().halt(0); //i think this sends a sigkill, so this should avoid the infinite loop of shutdown hooks
			}
		});
		
		try
		{
			String[] testsRaw = args[0].split(SEPARATOR);
			Class<?>[] clazzes = new Class<?>[testsRaw.length];
			for(int i = 0; i < testsRaw.length; i++)
			{
				try {
					clazzes[i] = Class.forName(testsRaw[i].trim());
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		        System.out.println("Should initialize");	
			Flusher.initialize();
			
			JUnitCore runner = new JUnitCore();
			Result r = runner.run(clazzes);
			
			System.out.println("[SUCCESS]:" + r.wasSuccessful());
			System.out.println("[TOTAL]:" + r.getRunCount());
			System.out.println("[FAILURE]:" + r.getFailureCount());
			
			try {
				Flusher.flushOut();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			for (Failure f : r.getFailures()) {
				System.out.println(f.toString());
				System.out.println(f.getTrace());
			}
	
			System.out.println("\n" + r.getFailures().toString());
		} catch(Throwable e)
		{
			e.printStackTrace();
		}
		finally {
			Runtime.getRuntime().exit(0);
		}
		
		
	}
}
