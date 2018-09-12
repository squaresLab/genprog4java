package ylyu1.wean;

import java.io.*;
import java.util.*;

public class Aggregator
{
	
	public static void main(String [] args) throws Exception
	{
		String fn = args[0];
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream("Temp.all"));
		Hashtable<Integer, Flusher> flush = (Hashtable<Integer, Flusher>) ois.readObject();
		//Hashtable<Integer,PredSerial> serials = (Hashtable<Integer,PredSerial>) ois.readObject();
		ois.close();
		Hashtable<Integer,PredSerial> serials = Modify.allSerials;
		//Scanner input = new Scanner(new File(fn+".tuo"));
		//int lnum = 0;
		//int lineat = -1;
		/*
		while(input.hasNextLine())
		{
			lnum++;
			Scanner inpute = new Scanner(input.nextLine());
			int ind;
			try{ind = inpute.nextInt();}catch(Exception e){continue;}
			//System.out.println("Line"+lnum);
			if(ind==417417417)
			{
				try{
					boolean b = inpute.nextBoolean();
					int i = inpute.nextInt();
					PredSerial ps = serials.get(i);
					if(ps.line==-1||ps.line==lineat)
					{
						ps.total++;
						if(b)ps.passed++;
					}
				}
				catch(Exception e){continue;}
			}
			else if(ind==124124124)
			{
				try{
					int i = inpute.nextInt();
					//System.out.println("lineat="+i);
					lineat=i;
				}
				catch(Exception e){continue;}
			}
			
		}*/
		Collection<PredSerial> coll = serials.values();
		int max = -1;
		for(PredSerial ps : coll)
		{
			if(ps.serial>max)max=ps.serial;
			//System.out.println("Class: "+ps.className+"\nMethod: "+ps.method+"\nLocation: "+ps.location+"\nLine: "+ps.line+"\nPredicate: "+ps.predicate+"\nCovered_By_Positive_Tests: "+ps.posCover+"\nCovered_By_Negative_Tests: "+ps.negCover+"\nEvaluated: "+ps.total+"\nPassed: "+ps.passed+"\n");
		}
		byte[] b = new byte[max+1];
	    for(int i = 0; i < b.length; i++)
	    {
	    	b[i]=2;
	    }
	    
	    for(PredSerial ps : coll)
	    {
	    	if(flush.containsKey(ps.serial))
	    	{
	    		if(flush.get(ps.serial).passing)
	    		{
	    			b[ps.serial]=1;
	    		}
	    		else
	    		{
	    			b[ps.serial]=0;
	    		}
	    	}
	    	/*
	    	if(ps.total!=0)
			{
				if(ps.total==ps.passed)
				{
					b[ps.serial]=1;
				}
				else b[ps.serial]=0;
			}*/
	    }
	    /*
	    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fn+".tns"));
		oos.writeObject(b);
		oos.flush();
		oos.close();*/
	}
	
}

