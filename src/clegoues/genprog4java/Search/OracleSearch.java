package clegoues.genprog4java.Search;

import java.util.Properties;

import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.mut.EditOperation;
import clegoues.genprog4java.rep.Representation;

public class OracleSearch<G extends EditOperation> extends Search<G> {
	public OracleSearch(Fitness<G> engine) {
		super(engine);
	}
	
	private static String startingGenome = "";
	public static void configure(Properties props) {
		if (props.getProperty("oracleGenome") != null) {
			OracleSearch.startingGenome = props.getProperty("startingGenome").trim();
		}

	}

	@Override
	protected Population<G> initialize(Representation<G> original, Population<G> incomingPopulation)
			throws RepairFoundException {
		return null;
	}
	@Override
	protected void runAlgorithm(int gen, int maxGen, Population<G> initialPopulation, Representation<G> original)
			throws RepairFoundException {
		Representation<G> theRepair = original.copy();
		theRepair.loadGenomeFromString(OracleSearch.startingGenome);
		assert (fitnessEngine.testToFirstFailure(theRepair));
		this.noteSuccess(theRepair, original, 1);		
	}
}
