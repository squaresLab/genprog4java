package clegoues.genprog4java.Fitness;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

public class RunAllFilter extends Filter {
	@Override
	public String describe()
	{
		return "full filter";
	}

	@Override
	public boolean shouldRun(Description description)
	{
		description.getMethodName();
		return true;
	}
}
