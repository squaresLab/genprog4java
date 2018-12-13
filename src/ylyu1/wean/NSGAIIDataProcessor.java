package ylyu1.wean;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.rep.CachingRepresentation;

public class NSGAIIDataProcessor extends AbstractDataProcessor {
	public ArrayList<ArrayList<Pair<Integer, Double>>> nsgaiiFitnesses = new ArrayList<>(); //nsgaii, left elem of Pair is domination rank, right elem of Pair is crowding distance
	public ArrayList<ArrayList<Map<Class<?>, Double>>> objectiveValues = new ArrayList<>(); //nsgaii, maps Objective class -> Objective value
	public ArrayList<ArrayList<Integer>> divscores; //collected even if diversity is not an objective

	@Override
	public void storeError(String err) {
		store(new NSGAIIDataStorer(false,false,0,err,null,null,null));
	}

	@Override
	public void storeNormal() {
		store(new NSGAIIDataStorer(true, super.repair, CachingRepresentation.sequence, null, divscores, nsgaiiFitnesses, objectiveValues));
	}
	
	private void store(NSGAIIDataStorer ds)
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
