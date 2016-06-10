package clegoues.genprog4java.Search;

import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.mut.EditOperation;
import clegoues.genprog4java.rep.Representation;

public class RandomSingleEdit<G extends EditOperation> extends Search<G>{

	public RandomSingleEdit(Fitness<G> engine) {
		super(engine);
		
	}

	@Override
	protected Population<G> initialize(Representation<G> original, Population<G> incomingPopulation)
			throws RepairFoundException {
		// FIXME: register just the basic mutations?
		return null;
	}

	@Override
	protected void runAlgorithm(Representation<G> original, Population<G> initialPopulation)
			throws RepairFoundException {
		// TODO Auto-generated method stub
		
	}



}
