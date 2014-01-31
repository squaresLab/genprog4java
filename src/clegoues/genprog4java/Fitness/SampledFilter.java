package clegoues.genprog4java.Fitness;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

public class SampledFilter extends Filter {
		private boolean atLeastOne = false;
		
		@Override
		public String describe()
		{
			return "Random 10% Sampler";
		}

		@Override
		public boolean shouldRun(Description desc)
		{
			double rand = Math.random()*10;
			
			if(desc.getMethodName() == null)
			{
				//System.err.println("This is test class.");
				return true;
			}
			
			if(!atLeastOne)
			{
				//System.err.println("Need to run at least one test case.");
				this.atLeastOne = true;
				return true;
			}
			
			if(rand < 1.0)
			{
				//System.err.println("true.");
				return true;
			}
			else
			{
				//System.err.println("false.");
				return false;
			}
		}
	}

