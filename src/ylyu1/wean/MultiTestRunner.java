package ylyu1.wean;

import clegoues.genprog4java.fitness.JUnitTestRunner;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class MultiTestRunner 
{
	public static final String SEPARATOR = "`";
	
	/*
	public static void main(String[] args)
	{

		String[] tests = args[0].split(SEPARATOR);
		
		//System.err.println("Number of tests: " + tests.length);
		
		for(String s : tests)
		{
			if (s.equals(""))
				continue; //ignore the empty string
			
			//System.err.println("Now attempting to run test: " + s);
			String[] ss = new String[1];
			ss[0]=s;
			//JUnitTestRunner.mainNoHardExit(ss);
			JUnitTestRunner.foo();
		}
		
		Runtime.getRuntime().exit(0);
	}
	*/
	
	/*Only works for classes, doesn't work for method level granularity (yet)*/
	public static void main(String[] args)
	{
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
			
			JUnitCore runner = new JUnitCore();
			Result r = runner.run(clazzes);
			
			System.out.println("[SUCCESS]:" + r.wasSuccessful());
			System.out.println("[TOTAL]:" + r.getRunCount());
			System.out.println("[FAILURE]:" + r.getFailureCount());
	
			for (Failure f : r.getFailures()) {
				System.out.println(f.toString());
				System.out.println(f.getTrace());
			}
	
			System.out.println("\n" + r.getFailures().toString());
		} finally {
			Runtime.getRuntime().exit(0);
		}
	}
}
