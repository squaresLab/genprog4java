package ylyu1.wean;

import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;

import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.rep.CachingRepresentation;




public class DataProcessor 
{
	public static ArrayList<ArrayList<Double>> fitscores = new ArrayList<ArrayList<Double>>();
	public static boolean repair = false;
	public static void storeError(String err)
	{
		store(new DataStorer(false,false,0,err,null));
	}
	public static void storeNormal()
	{
		store(new DataStorer(true, repair, CachingRepresentation.sequence, null, fitscores));
	}
	public static void store(DataStorer ds)
	{
		try {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("ResultOfSeed"+Configuration.seed+".results"));
		System.out.println("Hi Hi");
		oos.writeObject(ds);
		oos.flush();
		oos.close();
		}
		catch(Exception e) {System.out.println("Weird error occuring in data storage\n"+e.getMessage());e.printStackTrace();}
	}
	public static void main(String[] args) throws Exception
	{
		String dataset = args[0];
		int bugnum = Integer.parseInt(args[1]);
		int modenum = Integer.parseInt(args[2]);
		int seednum = Integer.parseInt(args[3]);
		String pathToBugs = args[4];
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(pathToBugs+modenum+"/"+dataset+bugnum+"Buggy/ResultOfSeed"+seednum+".results"));
		DataStorer result = (DataStorer) ois.readObject();
		ois.close();
		//plateau score: higher better, lower means more plateau
		double plateau = 0;
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
				plateau+=((double)score)/((double)valid);
			}
			plateau/=result.fitscores.size();
			System.out.println(dataset+" "+bugnum+" "+modenum+" "+seednum+" "+result.repair+" "+result.variant+" "+plateau);
		}
		else
		{
			System.out.println(dataset+" "+bugnum+" "+modenum+" "+seednum+" "+result.errorMessage);		    
		}
		
	}

}
