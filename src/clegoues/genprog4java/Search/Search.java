package clegoues.genprog4java.Search;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;

import clegoues.genprog4java.Fitness.Fitness;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.mut.EditOperation;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.rep.Representation;
import clegoues.genprog4java.rep.WeightedAtom;
import clegoues.genprog4java.util.GlobalUtils;
import clegoues.genprog4java.util.Pair;


/* let random atom_set = 
  let elts = List.map fst (WeightSet.elements atom_set) in 
  let size = List.length elts in 
    List.nth elts (Random.int size) 
 */


public class Search<G extends EditOperation> {

	private static int generations = 10;
	private static double promut = 1;
	private static boolean continueSearch = false;
	private static double appProb = 0.33333;
	private static double delProb = 0.33333;
	private static double swapProb = 0.33333;
	private static double repProb = 0.0;
	private static int maxEvals = 0;
	private static String startingGenome = "";

	public static void configure(Properties props) {
		if(props.getProperty("generations") != null) {
			Search.generations = Integer.parseInt(props.getProperty("generations").trim());
		}
		if(props.getProperty("oracleGenome") != null) {
			Search.startingGenome = props.getProperty("startingGenome").trim();
		}
		if(props.getProperty("promut") != null) {
			Search.promut = Double.parseDouble(props.getProperty("pMutation").trim());
		}	
		if(props.getProperty("continue") != null) {
			Search.continueSearch = true;
		}
		if(props.getProperty("max") != null) {
			Search.maxEvals = Integer.parseInt(props.getProperty("max").trim());
		}
	}
	private Fitness<G> fitnessEngine;
	private int generationsRun = 0;

	/* CLG is not convinced that the responsibility for writing out the successful
		   repair should lie in search, but she does think it's better to have it here
		   than in fitness, where it was before */


	/* Different strategies and representation types can do different
    things when a repair is found.  This at least stores information about the
    successful variant and may write it to disk or otherwise dispatch to the
    successful variant itself (potentially leading to minimization, for example,
    depending on the representation and command-line arguments). 

    @param rep successful variant
    @param orig original variant
    @param generation generation in which the repair was found */
	void noteSuccess(Representation<G> rep, Representation<G> original, int generation) throws RepairFoundException {
		// FIXME: print out edit list somehow
		System.out.printf("\nRepair Found: " + rep.getName());

		Calendar endTime = Calendar.getInstance(); // TODO do something with this
		// TODO: createSubDirectory("repair");
		String repairFilename = "repair/repair." + Configuration.globalExtension;
		rep.outputSource(repairFilename);
		rep.noteSuccess();

	}

	private TreeSet<WeightedAtom> rescaleAtomPairs(TreeSet<WeightedAtom> items) {
		double fullSum = 0.0; 
		TreeSet<WeightedAtom> retVal = new TreeSet<WeightedAtom>();
		for(Pair<?,Double> item : items) {
			fullSum += item.getSecond();
		}
		double scale = 1.0/fullSum;
		for(WeightedAtom item : items) { 
			WeightedAtom newItem = new WeightedAtom(item.getAtom(), item.getWeight() * scale);
			retVal.add(newItem);
		}
		return retVal; 
	}

	private boolean doWork(Representation<G> rep, Representation<G> original, Mutation mut, int first, int second) throws RepairFoundException {
		switch(mut) {
		case DELETE: rep.delete(first);
		break;	
		case APPEND: 
			rep.append(first, second);
			break;
		case REPLACE:
			rep.replace(first,second);
			break;
		case SWAP:
			rep.swap(first,second);
			break;
		}

		if(fitnessEngine.testToFirstFailure(rep)) {
			this.noteSuccess(rep,original,1);
			if(!Search.continueSearch) { 
				throw new RepairFoundException(rep.getName());
			}
		}
		return false;
	}

	private void registerMutations(Representation<G> variant) {
		TreeSet<Pair<Mutation,Double>> availableMutations = new TreeSet<Pair<Mutation,Double>>();
		availableMutations.add(new Pair<Mutation,Double>(Mutation.DELETE,Search.delProb));
		availableMutations.add(new Pair<Mutation,Double>(Mutation.APPEND,Search.appProb));
		availableMutations.add(new Pair<Mutation,Double>(Mutation.SWAP,Search.swapProb));
		availableMutations.add(new Pair<Mutation,Double>(Mutation.REPLACE,Search.repProb));

		variant.registerMutations(availableMutations);
	}
	public void bruteForceOne(Representation<G> original) throws RepairFoundException, CloneNotSupportedException {

		original.reduceFixSpace();
		registerMutations(original);

		int count = 0;
		TreeSet<WeightedAtom> allFaultyAtoms = new TreeSet<WeightedAtom>(original.getFaultyAtoms());

		for(WeightedAtom faultyAtom : allFaultyAtoms) {
			int faultyLocation = faultyAtom.getAtom();
			if(Search.delProb > 0.0) {
				count++;
			}
			if(Search.appProb > 0.0) {
				count += original.appendSources(faultyLocation).size();
			}
			if(Search.repProb > 0.0) {
				count += original.replaceSources(faultyLocation).size();
			}
			if(Search.swapProb > 0.0) {
				count += original.swapSources(faultyLocation).size();
			}
		}
		System.out.print("search: bruteForce: " + count + " mutants in search space\n");

		int wins = 0;
		int sofar = 1;

		TreeSet<WeightedAtom> rescaledAtoms = rescaleAtomPairs(allFaultyAtoms); 


		for(WeightedAtom faultyAtom : rescaledAtoms) {
			int stmt = faultyAtom.getAtom();
			double weight = faultyAtom.getWeight();

			//  wouldn't real polymorphism be the actual legitimate best right here?
			TreeSet<Pair<Mutation,Double>> availableMutations = original.availableMutations(stmt);
			TreeSet<Pair<Mutation,Double>> rescaledMutations = new TreeSet<Pair<Mutation,Double>>();
			double sumMutScale = 0.0;
			for(Pair<Mutation,Double> item : availableMutations) {
				sumMutScale += item.getSecond();
			}
			double mutScale = 1 / sumMutScale;
			for(Pair<Mutation,Double> item : availableMutations) {
				rescaledMutations.add(new Pair<Mutation,Double>(item.getFirst(), item.getSecond() * mutScale));
			}

			// rescaled Mutations gives us the mutation,weight pairs available at this atom
			// which itself has its own weight
			for(Pair<Mutation,Double> mutation : rescaledMutations) {
				Mutation mut = mutation.getFirst();
				double prob = mutation.getSecond();
				System.out.printf("%3g %3g", weight, prob);

				if(mut == Mutation.DELETE) {
					Representation<G> rep = (Representation<G>) original.clone();
					if(this.doWork(rep, original, mut, stmt, stmt)) {
						wins++;
					}
				} else if (mut == Mutation.APPEND) {
					TreeSet<WeightedAtom> appendSources = this.rescaleAtomPairs(original.appendSources(stmt));
					// FIXME: source in DESCENDING order by weight!
					for(WeightedAtom append : appendSources) {
						Representation<G> rep = (Representation<G>) original.clone();
						if(this.doWork(rep, original, mut, stmt, append.getAtom())) {
							wins++;
						}
					}
				} else if (mut == Mutation.REPLACE) {
					TreeSet<WeightedAtom> replaceSources = this.rescaleAtomPairs(original.replaceSources(stmt));
					// FIXME: source in DESCENDING order by weight!
					for(WeightedAtom replace : replaceSources) {
						Representation<G> rep = (Representation<G>) original.clone();
						if(this.doWork(rep, original, mut, stmt, replace.getAtom())) {
							wins++;
						}
					}

				} else if (mut == Mutation.SWAP ) {
					TreeSet<WeightedAtom> swapSources = this.rescaleAtomPairs(original.swapSources(stmt));
					// FIXME: source in DESCENDING order by weight!
					for(WeightedAtom swap : swapSources) {
						Representation<G> rep = original.clone();
						if(this.doWork(rep, original, mut, stmt, swap.getAtom())) {
							wins++;
						}
					}
				}
				// FIXME: debug output System.out.printf("\t variant " + wins + "/" + sofar + "/" + count + "(w: " + probs +")" + rep.getName());
				sofar++;
			}
		}
		System.out.printf("search: brute_force_1 ends\n");
	}
	/*

			  Basic Genetic Algorithm
	 */

	/*

			(** randomly chooses an atomic mutation operator,
			    instantiates it as necessary (selecting an insertion source, for example),
			    and applies it to some variant.  These choices are guided by certain
			    probabilities, such as the node weights or the probabilities associated with
			    each operator. If applicable for the given experiment/representation, may
			    use subatom mutation. 
			    @param test optional; force a mutation on every atom of the variant
			    @param variant individual to mutate
			    @return variant' modified/potentially mutated variant
	 */
	void mutate(Representation<G> variant) { // FIXME: don't need to return, right? 
		List<WeightedAtom> faultyAtoms = variant.getFaultyAtoms();
		ArrayList<WeightedAtom> proMutList = new ArrayList<WeightedAtom>();
		for(int i = 0; i < Search.promut; i++) {
			proMutList.add(GlobalUtils.chooseOneWeighted(faultyAtoms));

		}
		for(WeightedAtom atom : proMutList) {	
			int stmtid = atom.getAtom();
			TreeSet<Pair<Mutation,Double>> availableMutations = variant.availableMutations(stmtid);
			Pair<Mutation,Double> chosenMutation = null; // FIXME = GlobalUtils.chooseOneWeighted(new List(availableMutations));
			Mutation mut = chosenMutation.getFirst();
			// FIXME: make sure the mutation list isn't empty before choosing?
			switch(mut) {
			case DELETE: variant.delete(stmtid);
			break;
			case APPEND:
				TreeSet<WeightedAtom> allowed = variant.appendSources(stmtid);
				WeightedAtom after = GlobalUtils.chooseOneWeighted((List<WeightedAtom>) allowed);
				variant.append(stmtid,  after.getAtom());
				break;
			case SWAP:
				TreeSet<WeightedAtom> swapAllowed = variant.swapSources(stmtid);
				WeightedAtom swapWith = GlobalUtils.chooseOneWeighted((List<WeightedAtom>) swapAllowed);
				variant.swap(stmtid, swapWith.getAtom());
				break;
			case REPLACE:
				TreeSet<WeightedAtom> replaceAllowed = variant.replaceSources(stmtid);
				WeightedAtom replaceWith = GlobalUtils.chooseOneWeighted((List<WeightedAtom>) replaceAllowed);
				variant.replace(stmtid, replaceWith.getAtom());
				break;
			}
		}
	}
	/* computes the fitness of a variant by dispatching to the {b Fitness}
    module. If the variant has maximal fitness, calls [note_success], which may
    terminate the search.

    @param generation current generation
    @param orig original variant
    @param variant individual being tested
    @return variant post-fitness-testing, which means it should know its fitness
    (assuming the [Fitness] module behaved as it should)
    @raise Maximum_evals if max_evals is less than infinity and is reached. */
	void calculateFitness(int generation, Representation<G> original, Representation<G> variant) throws MaximumEvalsException, RepairFoundException
	{ // FIXME: I think this should go strictly into Fitness
		int evals = original.num_test_evals_ignore_cache(); // FIXME: this needs to go elsewhere
		if(Search.maxEvals > 0 && evals > Search.maxEvals) {
			throw new MaximumEvalsException();
		}
		if(fitnessEngine.testFitness(generation, variant)) {
			this.noteSuccess(variant,original,generation);
		} 
	}
	/*  prepares for GA by registering available mutations (including templates if
			    applicable) and reducing the search space, and then generates the initial
			    population, using [incoming_pop] if non-empty, or by randomly mutating the
			    [original]. The resulting population is evaluated for fitness before being
			    returned.  This may terminate early if a repair is found in the initial
			    population (by [calculate_fitness]).

			    @param original original variant
			    @param incoming_pop possibly empty, incoming population
			    @return initial_population generated by mutating the original */
	private Population<G> initializeGa(Representation<G> original, Population<G> incomingPopulation) throws MaximumEvalsException, RepairFoundException, CloneNotSupportedException {
		original.reduceSearchSpace(); // FIXME: this had arguments originally
		this.registerMutations(original);

		Population<G> initialPopulation = incomingPopulation;
		if(incomingPopulation.size() > incomingPopulation.getPopsize()) {
			initialPopulation = incomingPopulation.firstN(incomingPopulation.getPopsize());
		} // FIXME: this is too functional I think. 
		int stillNeed = initialPopulation.getPopsize() - initialPopulation.size();
		if(stillNeed > 0) {
			initialPopulation.add(original.clone());
		}
		/*
		 *  FIXME
		 *     let remainder = !popsize - (llen incoming_pop) in

			      (* initialize the population to a bunch of random mutants *)
			      pop :=
			        GPPopulation.generate !pop  (fun () -> mutate original) !popsize;
			      debug ~force_gui:true 
			        "search: initial population (sizeof one variant = %g MB)\n"
			        (debug_size_in_mb (List.hd !pop));
			      (* compute the fitness of the initial population *)
			      GPPopulation.map !pop (calculate_fitness 0 original) FIXME: not done
		 */
		for(Representation<G> item : initialPopulation) {
			this.calculateFitness(0, original, item);
		}
		return initialPopulation;
	}

	/* runs the genetic algorithm for a certain number of iterations, given the
			    most recent/previous generation as input.  Returns the last generation, unless it
			    is killed early by the search strategy/fitness evaluation.  The optional
			    parameters are set to the obvious defaults if omitted. 

			    @param start_gen optional; generation to start on (defaults to 1) 
			    @param num_gens optional; number of generations to run (defaults to
			    [generations]) 
			    @param incoming_population population produced by the previous iteration 
			    @raise Found_Repair if a repair is found
			    @raise Max_evals if the maximum fitness evaluation count is reached
			    @return population produced by this iteration *)*/
	private void runGa(int startGen, int numGens, Population<G> incomingPopulation, Representation<G> original) throws MaximumEvalsException, RepairFoundException {
		/*
		 * the bulk of run_ga is performed by the recursive inner helper
			     function, which Claire modeled off the MatLab code sent to her by the
			     UNM team */	
		int gen = startGen;
		while(gen < startGen + numGens) { // FIXME: gensRun vs. generationsRun?
			System.out.printf("search: generation" + gen); // FIXME: (sizeof one variant = %g MB) (debug_size_in_mb (List.hd incoming_population));
			generationsRun++;
			// Step 1: selection
			incomingPopulation.selection(incomingPopulation.getPopsize());
			// step 2: crossover
			incomingPopulation.crossover(original);
			// step 3: mutation
			for(Representation<G> item : incomingPopulation) {
				this.mutate(item);
			}
			// step 4: fitness
			for(Representation<G> item : incomingPopulation) {
				this.calculateFitness(gen,  original, item);
			}
			gen++;
		}
	}

	/* {b genetic_algorithm } is parametric with respect to a number of choices
	(e.g., population size, selection method, fitness function, fault localization,
	many of which are set at the command line or at the representation level.
			    May exit early if exceptions are thrown in fitness evaluation ([Max_Evals])
			    or a repair is found [Found_Repair].

			    @param original original variant
			    @param incoming_pop incoming population, possibly empty
			    @raise Found_Repair if a repair is found
			    @raise Max_evals if the maximum fitness evaluation count is set and then reached */
	public void geneticAlgorithm(Representation<G> original, Population<G> incomingPopulation) throws RepairFoundException, CloneNotSupportedException {
		System.out.printf("search: genetic algorithm begins (|original| = \n"); // %g MB)\n" (debug_size_in_mb original);
		assert(Search.generations >= 0);
		try {
			Population<G> initialPopulation = this.initializeGa(original, incomingPopulation);
			generationsRun++;
			try {
				this.runGa(1, Search.generations, initialPopulation, original);
			} catch(MaximumEvalsException e) {
				System.out.printf("reached maximum evals (%d)\n", Search.maxEvals);
			}
		} catch(MaximumEvalsException e) {
			System.out.printf("reached maximum evals (%d) during population initialization\n", Search.maxEvals);
		}
	}

	/*	 constructs a representation out of the genome as specified at the command
    line and tests to first failure.  This assumes that the oracle genome
    corresponds to a maximally fit variant.

    @param original individual representation
    @param starting_genome string; a string representation of the genome 
	 */
	public void oracleSearch(Representation<G> original) throws RepairFoundException, CloneNotSupportedException {
		Representation<G> theRepair = original.clone();
		theRepair.loadGenomeFromString(Search.startingGenome);
		assert(fitnessEngine.testToFirstFailure(theRepair));
		this.noteSuccess(theRepair, original, 1);
	}

}
