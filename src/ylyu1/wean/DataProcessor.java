package ylyu1.wean;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;


public class DataProcessor {
	//TODO: make this conform with refactor
	public static void main(String[] args) throws Exception
	{
		/*
		 * Compute anti-plateau score
		 * Compute average diversity
		 */
		String dataset = args[0];
		int bugnum = Integer.parseInt(args[1]);
		int modenum = Integer.parseInt(args[2]);
		int seednum = Integer.parseInt(args[3]);
		String pathToBugs = args[4];
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(pathToBugs+modenum+"/"+dataset+bugnum+"Buggy/ResultOfSeed"+seednum+".results"));
		Object resultObj = ois.readObject();
		ois.close();
		//plateau score: higher better, lower means more plateau
		double plateau = 0;
		if (resultObj instanceof GPDataStorer)
		{
			GPDataStorer result = (GPDataStorer) resultObj;
			if(result.good)
			{
				ArrayList<Double> plateaus = new ArrayList<Double>();
				for(ArrayList<Double> fitscore: result.fitscores)
				{
					int score = 0;
					int valid = 0;
					for(int i = 0; i < fitscore.size(); i++)
					{
						if(fitscore.get(i)<0.00000001)continue;
						boolean hasequal = false;
						for(int j = 0; j < i; j++)
						{
							if(Math.abs(fitscore.get(i)-fitscore.get(j))<0.00000001)
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
					plateau+=((double)score)/((double)valid+0.000000001);
				}
				double total = 0;
				double totalcount = 0;
				for(ArrayList<Integer> divscore: result.divscores) {
					for(Integer i : divscore) {
						total += i;
					}
					totalcount += divscore.size();
				}
				plateau/=result.fitscores.size();
				System.out.println(dataset+" "+bugnum+" "+modenum+" "+seednum+" "+result.repair+" "+result.variant+" "+plateau+" "+(total/totalcount));
			}
			else
			{
				System.out.println(dataset+" "+bugnum+" "+modenum+" "+seednum+" "+result.errorMessage);		    
			}
		}
		else if (resultObj instanceof NSGAIIDataStorer)
		{
			throw new UnsupportedOperationException("Zhen hasn't implemented this portion yet");
		}
		
	}
}
