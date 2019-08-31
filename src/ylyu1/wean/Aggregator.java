package ylyu1.wean;

import java.io.*;
import java.util.*;

public class Aggregator
{
	public static ByteStrings bs = new ByteStrings();
	public static void clear()
	{
		bs = new ByteStrings();
	}
	public static void main(String [] args) throws Exception
	{
		String fn = args[0];
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream("Temp.all"));
		Hashtable<Integer, Flusher> flush = (Hashtable<Integer, Flusher>) ois.readObject();
		ois.close();
		ObjectInputStream oiss = new ObjectInputStream(new FileInputStream(fn+".pse"));
		int max = (Integer)oiss.readObject();
		oiss.close();
		System.out.println(flush.size());
		
		byte[] b = new byte[max+1];
	    for(int i = 0; i < b.length; i++)
	    {
	    	b[i]=2;
	    }
	    
	    for(Integer i : flush.keySet())
	    {
	    	if(flush.get(i).passing)b[i]=1;else b[i]=0;
	    }
	    bs.insert(b);
	}
	
}

