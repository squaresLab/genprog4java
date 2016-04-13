package clegoues.genprog4java.Search;

import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.mut.EditOperation;
import clegoues.genprog4java.rep.Representation;

public class RSRepair<G extends EditOperation> extends Search<G>{

	public RSRepair(Fitness<G> engine) {
		super(engine);
	}

	@Override
	protected Population<G> initialize(Representation<G> original, Population<G> incomingPopulation)
			throws RepairFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void runAlgorithm(int gen, int maxGen, Population<G> initialPopulation, Representation<G> original)
			throws RepairFoundException {
		// TODO Auto-generated method stub
		
	}

}
