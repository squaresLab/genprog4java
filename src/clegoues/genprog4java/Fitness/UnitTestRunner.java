package clegoues.genprog4java.Fitness;


import java.util.ArrayList;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

//FIXME: taken from PAR

public class UnitTestRunner
{
	// first arg: test class to run
	// second arg: test filter
	public static void main(String[] args)
	{
		try {
			ArrayList<Class<?>> testClassList = new ArrayList<Class<?>>();
			
			String className = args[0].trim();
			
			Class<?> filterClazz = Class.forName(args[1]);
			Filter filter = (Filter)filterClazz.newInstance();
			System.err.println("Filter Class: "+ filter.getClass().toString());
		
				System.err.println("Test Class: " + className);
				Class<?> clazz = Class.forName(className);
				testClassList.add(clazz);

			Class<?>[] testClasses = testClassList.toArray(
					new Class[testClassList.size()]);
			
			Request total = Request.classes(testClasses);
			System.out.println("Requested #: " + total.getRunner().testCount());
			
			Request sampled = total.filterWith(filter);
			
			JUnitCore runner = new JUnitCore();
			//runner.addListener(new Listener());
			
			Result r = runner.run(sampled);
			
			System.out.println("[SUCCESS]:"+r.wasSuccessful());
			System.out.println("[TOTAL]:"+r.getRunCount());
			System.out.println("[FAILURE]:"+r.getFailureCount());
			
			for(Failure f: r.getFailures())
			{
				System.out.println(f.toString());
				System.out.println(f.getTrace());
			}
			
			System.out.println("\n"+r.getFailures().toString());
							
		} catch (Exception e) {
			//System.out.println("Sampled");
			e.printStackTrace();
		}
/*		finally
		{
			Runtime.getRuntime().exit(0);
		}
*/		
		Runtime.getRuntime().exit(0);
	}
	
}

class Listener extends RunListener {
	@Override
	public void testRunStarted(Description description) throws Exception {
		System.out.println("test started: " + description.toString());
	}

	@Override
	public void testRunFinished(Result result) throws Exception {
		System.out.println("test finished");
	}

	@Override
	public void testFinished(Description description) throws Exception {
		System.out.println("test: "+description.getDisplayName());
	}
}

