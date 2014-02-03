package clegoues.genprog4java.Fitness;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class JUnitTestRunner
{
	public static void main(String[] args)
	{
		try {
			String clazzName = args[0].trim();
			System.err.println("Test Class: " + clazzName);
			Class<?>[] testClazz = new Class[1];
			testClazz[0] = Class.forName(clazzName);

			Request testRequest = Request.classes(testClazz);
			System.out.println("Requested #: " + testRequest.getRunner().testCount());

			JUnitCore runner = new JUnitCore();
			Result r = runner.run(testRequest);

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
			e.printStackTrace();
		}	
		Runtime.getRuntime().exit(0);
	}

}
// FIXME: do I need the listener?
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

