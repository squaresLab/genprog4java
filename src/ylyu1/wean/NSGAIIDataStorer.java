package ylyu1.wean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

public class NSGAIIDataStorer implements Serializable {
	public boolean repair;
	public boolean good;
	public int variant;
	public String errorMessage;
	public static ArrayList<ArrayList<Integer>> divscores; //collected even if diversity is not an objective
	public static ArrayList<ArrayList<Pair<Integer, Double>>> nsgaiiFitnesses = new ArrayList<>(); //left elem of Pair is domination rank, right elem of Pair is crowding distance
	public static ArrayList<ArrayList<Map<Class<?>, Double>>> objectiveValues = new ArrayList<>(); //maps Objective class -> Objective value
	
	public NSGAIIDataStorer(boolean good1, boolean repair1, int variant1, String errorMessage1, ArrayList<ArrayList<Integer>> divscores1,
			ArrayList<ArrayList<Pair<Integer, Double>>> nsgaiiFits, ArrayList<ArrayList<Map<Class<?>, Double>>> objectiveVals)
	{
		repair = repair1;
		good = good1;
		variant = variant1;
		errorMessage = errorMessage1;
		divscores = divscores1;
		nsgaiiFitnesses = nsgaiiFits;
		objectiveValues = objectiveVals;
	}
}
