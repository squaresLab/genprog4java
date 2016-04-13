package clegoues.genprog4java.Search;

import java.util.ArrayList;
import java.util.Properties;
import java.util.TreeSet;

import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.mut.EditOperation;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.rep.Representation;
import clegoues.genprog4java.rep.WeightedAtom;
import clegoues.genprog4java.util.GlobalUtils;
import clegoues.genprog4java.util.Pair;

public class GeneticProgramming<G extends EditOperation> extends Search<G>{
	//The proportional mutation rate, which controls the probability that a genome is mutated in the mutation step in terms of the number of genes within it should be modified.
	private static double promut = 1; 
	private static int generations = 10;
	private int generationsRun = 0;

	public GeneticProgramming(Fitness<G> engine) {
		super(engine);
	}
	
	public static void configure(Properties props) {
		if (props.getProperty("promut") != null) {
			GeneticProgramming.promut = Double.parseDouble(props.getProperty("pMutation")
					.trim());
		}
		if (props.getProperty("generations") != null) {
			GeneticProgramming.generations = Integer.parseInt(props.getProperty(
					"generations").trim());
		}

	}
	/*
	 * 
	 * (** randomly chooses an atomic mutation operator, instantiates it as
	 * necessary (selecting an insertion source, for example), and applies it to
	 * some variant. These choices are guided by certain probabilities, such as
	 * the node weights or the probabilities associated with each operator. If
	 * applicable for the given experiment/representation, may use subatom
	 * mutation.
	 * 
	 * @param test optional; force a mutation on every atom of the variant
	 * 
	 * @param variant individual to mutate
	 * 
	 * @return variant' modified/potentially mutated variant
	 */
	public void mutate(Representation<G> variant) {
		ArrayList faultyAtoms = variant.getFaultyAtoms();
		ArrayList<WeightedAtom> proMutList = new ArrayList<WeightedAtom>();
		boolean foundMutationThatCanApplyToAtom = false;
		while(!foundMutationThatCanApplyToAtom){
			//promut default is 1
			for (int i = 0; i < GeneticProgramming.promut; i++) {
				WeightedAtom wa = null;
				boolean alreadyOnList = false;
				//only adds the random atom if it is different from the others already added
				do{
					//chooses a random atom
					wa = (WeightedAtom) GlobalUtils.chooseOneWeighted(faultyAtoms);
					alreadyOnList = proMutList.contains(wa);
				}while(alreadyOnList);
				proMutList.add(wa);
			}
			for (WeightedAtom atom : proMutList) {
				int stmtid = atom.getAtom();
				//the available mutations for this stmt
				TreeSet<Pair<Mutation, Double>> availableMutations = variant.availableMutations(stmtid);
				if(availableMutations.isEmpty()){
					continue; 
				}else{
					foundMutationThatCanApplyToAtom = true;
				}
				//choose one at random
				Pair<Mutation, Double> chosenMutation = (Pair<Mutation, Double>) GlobalUtils.chooseOneWeighted(new ArrayList(availableMutations));
				Mutation mut = chosenMutation.getFirst();
				switch (mut) {
				case LBOUNDSET:
				case NULLCHECK:
				case DELETE:
				case UBOUNDSET:
				case RANGECHECK:
				case NULLINSERT:
				case FUNREP:
					// FIXME: this -1 hack is pretty gross; note to self, CLG should fix it
					variant.performEdit(mut, stmtid, (-1));
					break;
				case APPEND:
					TreeSet<WeightedAtom> allowedA = variant.editSources(stmtid,mut);
					WeightedAtom afterA = (WeightedAtom) GlobalUtils
							.chooseOneWeighted(new ArrayList(allowedA));
					variant.performEdit(mut, stmtid,  afterA.getAtom()); 
					break;
				case SWAP:
					TreeSet<WeightedAtom> allowedS = variant.editSources(stmtid,mut);
					WeightedAtom afterS = (WeightedAtom) GlobalUtils
							.chooseOneWeighted(new ArrayList(allowedS));
					variant.performEdit(mut, stmtid,  afterS.getAtom()); 
					break;
				case REPLACE: 
					TreeSet<WeightedAtom> allowedR = variant.editSources(stmtid,mut);
					WeightedAtom afterR = (WeightedAtom) GlobalUtils
							.chooseOneWeighted(new ArrayList(allowedR));
					variant.performEdit(mut, stmtid,  afterR.getAtom()); 
					break;
				case OFFBYONE:
					TreeSet<WeightedAtom> allowedO = variant.editSources(stmtid,mut);
					WeightedAtom afterO = (WeightedAtom) GlobalUtils
							.chooseOneWeighted(new ArrayList(allowedO));
					variant.performEdit(mut, stmtid,  afterO.getAtom()); 
					break;
				default: 
					logger.fatal("Unhandled template type in search.mutate; add handling and try again!");
					break;

				}
			}
		}
	}
	/*
	 * prepares for GA by registering available mutations (including templates
	 * if applicable) and reducing the search space, and then generates the
	 * initial population, using [incoming_pop] if non-empty, or by randomly
	 * mutating the [original]. The resulting population is evaluated for
	 * fitness before being returned. This may terminate early if a repair is
	 * found in the initial population (by [calculate_fitness]).
	 * 
	 * @param original original variant
	 * 
	 * @param incoming_pop possibly empty, incoming population
	 * 
	 * @return initial_population generated by mutating the original
	 */
	protected Population<G> initialize(Representation<G> original,
			Population<G> incomingPopulation) throws RepairFoundException {
		original.reduceSearchSpace();

		Population<G> initialPopulation = incomingPopulation;

		if (incomingPopulation != null
				&& incomingPopulation.size() > incomingPopulation.getPopsize()) {
			initialPopulation = incomingPopulation.firstN(incomingPopulation
					.getPopsize());
		} 
		int stillNeed = initialPopulation.getPopsize()
				- initialPopulation.size();
		if (stillNeed > 0) {
			initialPopulation.add(original.copy());
			stillNeed--;
		}
		for (int i = 0; i < stillNeed; i++) {
			Representation<G> newItem = original.copy();
			this.mutate(newItem);
			initialPopulation.add(newItem);
		}

		for (Representation<G> item : initialPopulation) {
			if (fitnessEngine.testFitness(0, item)) {
				this.noteSuccess(item, original, 0);
				if(!continueSearch) {
					throw new RepairFoundException();
				}
			}
		}
		return initialPopulation;
	}
	
	/*
	 * runs the genetic algorithm for a certain number of iterations, given the
	 * most recent/previous generation as input. Returns the last generation,
	 * unless it is killed early by the search strategy/fitness evaluation. The
	 * optional parameters are set to the obvious defaults if omitted.
	 * 
	 * @param start_gen optional; generation to start on (defaults to 1)
	 * 
	 * @param num_gens optional; number of generations to run (defaults to
	 * [generations])
	 * 
	 * @param incoming_population population produced by the previous iteration
	 * 
	 * @raise Found_Repair if a repair is found
	 * 
	 * @raise Max_evals if the maximum fitness evaluation count is reached
	 * 
	 * @return population produced by this iteration *)
	 */
	protected void runAlgorithm(Representation<G> original, Population<G> initialPopulation) throws RepairFoundException {
		/*
		 * the bulk of run_ga is performed by the recursive inner helper
		 * function, which Claire modeled off the MatLab code sent to her by the
		 * UNM team
		 */
		logger.info("search: genetic algorithm begins\n");

		assert (GeneticProgramming.generations >= 0);
		Population<G> incomingPopulation = this.initialize(original,
				initialPopulation);
		int gen = 1;
		while (gen < GeneticProgramming.generations) {
			logger.info("search: generation" + gen);
			generationsRun++;
			assert (initialPopulation.getPopsize() > 0);
			// Step 1: selection
			logger.info("before selection, generation: " + gen + " incoming popsize: " + incomingPopulation.size());
			incomingPopulation.selection(incomingPopulation.getPopsize());
			logger.info("after selection, generation: " + gen + " incoming popsize: " + incomingPopulation.size());
			// step 2: crossover
			incomingPopulation.crossover(original);
			logger.info("after crossover, generation: " + gen + " incoming popsize: " + incomingPopulation.size());

			// step 3: mutation
			for (Representation<G> item : incomingPopulation) {
				this.mutate(item);
			}
			logger.info("after mutation, generation: " + gen + " incoming popsize: " + incomingPopulation.size());

			// step 4: fitness
			int count = 0;
			for (Representation<G> item : incomingPopulation) {
				count++;
				if (fitnessEngine.testFitness(gen, item)) {
					this.noteSuccess(item, original, gen);
					if(!continueSearch) 
						return;
				}
			}
			logger.info("Generation: " + gen + " I think I tested " + count + " variants.");
			gen++;
		}
	}
}
