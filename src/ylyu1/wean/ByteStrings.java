package ylyu1.wean;

import java.util.ArrayList;

import clegoues.genprog4java.fitness.Fitness;

public class ByteStrings {
	public ArrayList<byte[]> grid = new ArrayList<byte[]>();
	//public String[] names = new String[1000];
	public int currloc = 0;
	public int maxSize = 0;
	public void insert(byte[] b)
	{
		int size = b.length;
		if(size>maxSize)
		{
			maxSize=size;
		}
		grid.add(b);
		currloc++;
	}
	public void resizeAll()
	{
		for(int i = 0; i < currloc; i++)
		{
			byte[] newb = new byte[maxSize];
			for(int j = 0; j < maxSize; j++)
			{
				if(j < grid.get(i).length) newb[j]=grid.get(i)[j];
				else newb[j]=2;
			}
			grid.set(i, newb);
		}
	}
	public byte[] extend(byte[] b, int tosize)
	{
		byte[] ret = new byte[tosize];
		for(int i = 0; i < tosize; i++)
		{
			if(i < tosize)ret[i]=b[i];
			else ret[i]=2;
		}
		return ret;
	}
	public double[] getScores() 
	{
		int[] raw = Fitness.getStringDiffScore(grid);
	    double max = 0;
	    for(int r : raw)
	    {
	    	if(r>max)max=r;
	    }
	    double[] ret = new double[raw.length];
	    for(int i = 0; i < ret.length; i++)
	    {
	    	if(max==0)
	    	{
	    		ret[i]=0.0;
	    	}
	    	else
	    	{
	    		ret[i]=((double)raw[i])/max;
	    	}
	    }
	    return ret;
	}
	public void doubleUp()
	{
		ArrayList<byte[]> arr = new ArrayList<byte[]>();
		for(int i = 0; i < grid.size(); i += 2)
		{
			byte[] b = new byte[maxSize*2];
			for(int j = 0; j < maxSize; j++)
			{
				b[2*j] = grid.get(i)[j];
				b[2*j+1]= grid.get(i+1)[j];
			}
			arr.add(b);
		}
		grid = arr;
	}
}
