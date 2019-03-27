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

		    System.out.println("Should initialize");	
			Flusher.initialize();
			String[] testsRaw = args[0].split(SEPARATOR);
			//List<Class<?>> clazzes = new ArrayList<Class<?>>();
			for(int i = 0; i < testsRaw.length; i++)
			{
				if(testsRaw[i].length()<3)continue;
				//if(clazzName.contains("::")) {
					String[] intermed = testsRaw[i].split("::");
					String clazzName = intermed[0];
					String methodName = intermed[1];
				//}	
					Class<?> clazzz = null;
				try {
					clazzz = Class.forName(clazzName);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					continue;
				} catch (ExceptionInInitializerError e){
					e.printStackTrace();
					continue;
				}
				
				Request request = Request.method(clazzz, methodName);
			
			//Class<?>[] clazzess = new Class<?>[clazzes.size()];
			//for(int i = 0; i < clazzes.size(); i++){
				//clazzess[i]=clazzes.get(i);
			//} 
			
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
	
			//.args.System.out.println("\n" + r.getFailures().toString());
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

