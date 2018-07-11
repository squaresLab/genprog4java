package ylyu1.wean;

import clegoues.genprog4java.fitness.JUnitTestRunner;

public class MultiTestRunner 
{
	public static void main(String [] args)
	{
		for(String s : args)
		{
			String[] ss = {s};
			JUnitTestRunner.main(ss);
		}
	}
}
