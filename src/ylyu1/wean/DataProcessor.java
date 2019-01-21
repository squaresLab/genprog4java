package ylyu1.wean;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;


public class DataProcessor {
	static final double ERR = 0.00000001;
	//TODO: make this conform with refactor
	public static void main(String[] args) throws Exception
	{
		/*
		 * Compute anti-plateau score
		 * Compute average diversity
		 */
		/*
		String dataset = args[0];
		int bugnum = Integer.parseInt(args[1]);
		int modenum = Integer.parseInt(args[2]);
		int seednum = Integer.parseInt(args[3]);
		String pathToBugs = args[4];
		*/
		if(args.length != 2)
		{
			System.out.println("Usage: java -cp GP4J_HOME/target/classes:GP4J_HOME/lib/commons-lang3-3.8.1.jar ylyu1.wean.DataProcessor bugDir seed");
			System.exit(1);
		}
		String bugDir = args[0];
		int seed = Integer.parseInt(args[1]);
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(bugDir+"/ResultOfSeed"+seed+".results"));
		Object resultObj = ois.readObject();
		ois.close();
		//plateau score: higher better, lower means more plateau
		double plateau = 0;
		if (resultObj instanceof GPDataStorer)
		{
			GPDataStorer result = (GPDataStorer) resultObj;
			if(result.good)
			{
				for(ArrayList<Double> fitscores: result.fitscores)
				{
					int score = 0;
					int valid = 0;
					for(int i = 0; i < fitscores.size(); i++)
					{
						if(fitscores.get(i)<ERR)continue;
						boolean hasequal = false;
						for(int j = 0; j < i; j++)
						{
							if(Math.abs(fitscores.get(i)-fitscores.get(j))<ERR)
							{
								hasequal=true;
							}
						}
						if(hasequal)
						{
							valid++;
						}
						else
						{
							score++;
							valid++;
						}
					}
					plateau+=((double)score)/((double)valid+ERR);
					//TODO: adding ERR to valid doesn't look right. if this is to stop division by zero, just check for it.
				}
				double total = 0;
				double totalcount = 0;
				for(ArrayList<Integer> divscores: result.divscores) {
					for(Integer i : divscores) {
						total += i;
					}
					totalcount += divscores.size();
				}
				plateau/=result.fitscores.size();
				System.out.println(bugDir+" "+seed+" "+result.repair+" "+result.variant+" "+plateau+" "+(total/totalcount));
			}
			else
			{
				System.out.println(bugDir+" "+seed+" "+result.errorMessage);		    
			}
		}
		else if (resultObj instanceof NSGAIIDataStorer)
		{
			NSGAIIDataStorer result = (NSGAIIDataStorer) resultObj;
			if(result.good)
			{
				double total = 0;
				double totalcount = 0;
				final int numGenerations = result.nsgaiiFitnesses.size();
				for(int g = 0; g < numGenerations; g++)
				{
					ArrayList<Pair<Integer, Double>> fitscores = result.nsgaiiFitnesses.get(g);
					ArrayList<Map<Class<?>, Double>> objvals = result.objectiveValues.get(g);
					int score = 0;
					int valid = 0;
					for(int i = 0; i < fitscores.size(); i++)
					{
						if( ! nsgaIsValid(objvals.get(i))) continue;
						valid++;
						boolean hasequal = false;
						for(int j = 0; j < i; j++)
						{
							if(nsgaFitnessesAreEqual(fitscores.get(i), fitscores.get(j)))
								hasequal = true;
						}
						if( ! hasequal)
							score++;
					}
					if(valid != 0)
						plateau += (double) score / (double) valid;
					
					ArrayList<Integer> divscores = result.divscores.get(g);
					for(Integer i : divscores)
						total += i;
					totalcount += divscores.size();
				}
				plateau /= numGenerations;
				System.out.println(bugDir+" "+seed+" "+result.repair+" "+result.variant+" "+plateau+" "+(total/totalcount));
			}
			else
			{
				System.out.println(bugDir+" "+seed+" "+result.errorMessage);
			}
		}
		
	}
	
	private static boolean nsgaFitnessesAreEqual(Pair<Integer, Double> f1, Pair<Integer, Double> f2)
	{
		return f1.getLeft().equals(f2.getLeft()) && f1.getRight().equals(f2.getRight());
	}
	
	private static boolean nsgaIsValid(Map<Class<?>, Double> o)
	{
		for(Double score : o.values())
			if( ! score.equals(0)) return true;
		//else if all objective scores equal zero
		return false;
	}
}
