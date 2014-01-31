package clegoues.genprog4java.Fitness;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

public class CoverageFilter extends Filter
{

	@Override
	public String describe()
	{
		return "Coverage filter";
	}

	@Override
	public boolean shouldRun(Description description)
	{
		return true;
	}

}
