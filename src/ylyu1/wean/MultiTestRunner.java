package ylyu1.wean;

import clegoues.genprog4java.fitness.JUnitTestRunner;

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
			List<Class<?>> clazzes = new ArrayList<Class<?>>();
			for(int i = 0; i < testsRaw.length; i++)
			{
				try {
					Class<?> clazzz = Class.forName(testsRaw[i].trim());
					clazzes.add(clazzz);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExceptionInInitializerError e){
					e.printStackTrace();
				}
			}
			Class<?>[] clazzess = new Class<?>[clazzes.size()];
			for(int i = 0; i < clazzes.size(); i++){
				clazzess[i]=clazzes.get(i);
			} 
			
			JUnitCore runner = new JUnitCore();
			Result r = runner.run(clazzess);
			
			System.out.println("[SUCCESS]:" + r.wasSuccessful());
			System.out.println("[TOTAL]:" + r.getRunCount());
			System.out.println("[FAILURE]:" + r.getFailureCount());
			
			try {
				Flusher.flushOut();
			} catch (IOException e) {
				e.printStackTrace();
			}
	
			System.out.println("\n" + r.getFailures().toString());
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
