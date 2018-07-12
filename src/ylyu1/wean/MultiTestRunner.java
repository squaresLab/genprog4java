package ylyu1.wean;

import clegoues.genprog4java.fitness.JUnitTestRunner;

public class MultiTestRunner 
{
	public static final String SEPARATOR = "`";
	public static void main(String [] args)
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
			JUnitTestRunner.mainNoHardExit(ss);
			//System.err.println("TEST RUNNER SUCCESSFULLY EXITED!");
		}
		
		Runtime.getRuntime().exit(0);
	}
}
