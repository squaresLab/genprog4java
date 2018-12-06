package ylyu1.wean;

import java.io.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.rep.CachingRepresentation;




public class GPDataProcessor extends AbstractDataProcessor
{
	public ArrayList<ArrayList<Double>> fitscores = new ArrayList<ArrayList<Double>>();
	public ArrayList<ArrayList<Integer>> diversityScores = new ArrayList<ArrayList<Integer>>();
	
	public void storeError(String err)
	{
		store(new GPDataStorer(false,false,0,err,null,null));
	}
	public void storeNormal()
	{
		store(new GPDataStorer(true, super.repair, CachingRepresentation.sequence, null, fitscores ,diversityScores));
	}
	private void store(GPDataStorer ds)
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
	

}
