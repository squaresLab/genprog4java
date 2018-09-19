package ylyu1.wean;

import java.io.*;
import java.util.*;

public class Aggregator
{
	public static ByteStrings bs = new ByteStrings();
	public static void main(String [] args) throws Exception
	{
		String fn = args[0];
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream("Temp.all"));
		Hashtable<Integer, Flusher> flush = (Hashtable<Integer, Flusher>) ois.readObject();
		//Hashtable<Integer,PredSerial> serials = (Hashtable<Integer,PredSerial>) ois.readObject();
		ois.close();
		//Hashtable<Integer,PredSerial> serials = Modify.allSerials;
		ObjectInputStream oiss = new ObjectInputStream(new FileInputStream(fn+".pse"));
		int max = (Integer)oiss.readObject();
		oiss.close();
		//int max = 100;
		System.out.println(flush.size());
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
		//Collection<PredSerial> coll = serials.values();
		byte[] b = new byte[max+1];
	    for(int i = 0; i < b.length; i++)
	    {
	    	b[i]=2;
	    }
	    
	    for(Integer i : flush.keySet())
	    {
	    	if(flush.get(i).passing)b[i]=1;else b[i]=0;
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
	    bs.insert(b);
	    /*
	    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fn+".tns"));
		oos.writeObject(b);
		oos.flush();
		oos.close();*/
	}
	
}

