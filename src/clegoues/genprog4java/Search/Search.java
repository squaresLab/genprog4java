package clegoues.genprog4java.Search;

import java.util.Calendar;
import java.util.List;
import java.util.TreeSet;

import clegoues.genprog4java.Fitness.Fitness;
import clegoues.genprog4java.main.Main;
import clegoues.genprog4java.rep.History;
import clegoues.genprog4java.rep.Mutation;
import clegoues.genprog4java.rep.Representation;
import clegoues.genprog4java.rep.WeightedAtom;
import clegoues.genprog4java.util.GlobalUtils;
import clegoues.genprog4java.util.Pair;


/* let random atom_set = 
  let elts = List.map fst (WeightSet.elements atom_set) in 
  let size = List.length elts in 
    List.nth elts (Random.int size) 
 */
public class Search<G,C> {

	private int generations = 10;
	private int proMut = 1;
	private boolean continueSearch = false;
	private int generationsRun = 0;
	private double appProb = 0.33333;
	private double delProb = 0.33333;
	private double swapProb = 0.33333;
	private double repProb = 0.0;
	private String searchStrategy = "ga";
	private Fitness<G,C> fitnessEngine;

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
	void noteSuccess(Representation<G,C> rep, Representation<G,C> original, int generation) {
		List<History<C>> history = rep.getHistory();
		System.out.printf("\nRepair Found: ");
		for(History<C> histEle : history) {
			System.out.printf(" " + histEle.toString());
		}
		String name = rep.getName();
		System.out.printf("\n Repair Name: " + name);
		Calendar endTime = Calendar.getInstance(); // TODO do something with this
		// TODO: createSubDirectory("repair");
		String repairFilename = "repair/repair." + Main.config.getGlobalExtension();
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

	private boolean doWork(Representation<G,C> rep, Representation<G,C> original, Mutation mut, int first, int second) throws RepairFoundException {
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
			if(!this.continueSearch) { 
				throw new RepairFoundException(rep.getName());
			}
		}
		return false;
	}

	public void bruteForceOne(Representation<G,C> original) throws RepairFoundException {

		original.reduceFixSpace();
		// FIXME: are we registering mutations?  Maybe we should, no?
		int count = 0;
		TreeSet<WeightedAtom> allFaultyAtoms = new TreeSet<WeightedAtom>(original.getFaultyAtoms());

		for(WeightedAtom faultyAtom : allFaultyAtoms) {
			int faultyLocation = faultyAtom.getAtom();
			if(this.delProb > 0.0) {
				count++;
			}
			if(this.appProb > 0.0) {
				count += original.appendSources(faultyLocation).size();
			}
			if(this.repProb > 0.0) {
				count += original.replaceSources(faultyLocation).size();
			}
			if(this.swapProb > 0.0) {
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
					Representation<G,C> rep = original.copy();
					if(this.doWork(rep, original, mut, stmt, stmt)) {
						wins++;
					}
				} else if (mut == Mutation.APPEND) {
					TreeSet<WeightedAtom> appendSources = this.rescaleAtomPairs(original.appendSources(stmt));
					// FIXME: source in DESCENDING order by weight!
					for(WeightedAtom append : appendSources) {
						Representation<G,C> rep = original.copy();
						if(this.doWork(rep, original, mut, stmt, append.getAtom())) {
							wins++;
						}
					}
				} else if (mut == Mutation.REPLACE) {
					TreeSet<WeightedAtom> replaceSources = this.rescaleAtomPairs(original.replaceSources(stmt));
					// FIXME: source in DESCENDING order by weight!
					for(WeightedAtom replace : replaceSources) {
						Representation<G,C> rep = original.copy();
						if(this.doWork(rep, original, mut, stmt, replace.getAtom())) {
							wins++;
						}
					}

				} else if (mut == Mutation.SWAP ) {
					TreeSet<WeightedAtom> swapSources = this.rescaleAtomPairs(original.swapSources(stmt));
					// FIXME: source in DESCENDING order by weight!
					for(WeightedAtom swap : swapSources) {
						Representation<G,C> rep = original.copy();
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
	void mutate(Representation<G,C> variant) { // FIXME: don't need to return, right? 
		List<WeightedAtom> faultyAtoms = variant.getFaultyAtoms();
		List<WeightedAtom> proMutList;
		for(int i = 0; i < this.proMut; i++) {
			proMutList.add(GlobalUtils.chooseOneWeighted(faultyAtoms));

		}
		for(WeightedAtom atom : proMutList) {	
			int stmtid = atom.getAtom();
			TreeSet<Pair<Mutation,Double>> availableMutations = variant.availableMutations(stmtid);
			Pair<Mutation,Double> chosenMutation = GlobalUtils.chooseOneWeighted(new List(availableMutations));
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
	/*
			(** computes the fitness of a variant by dispatching to the {b Fitness}
			    module. If the variant has maximal fitness, calls [note_success], which may
			    terminate the search.

			    @param generation current generation
			    @param orig original variant
			    @param variant individual being tested
			    @return variant post-fitness-testing, which means it should know its fitness
			    (assuming the [Fitness] module behaved as it should)
			    @raise Maximum_evals if max_evals is less than infinity and is reached. *)
			let calculate_fitness generation orig variant =
			  let evals = Rep.num_test_evals_ignore_cache() in
			    if !tweet then Unix.sleep 1;
			    if !max_evals > 0 && evals > !max_evals then 
			        raise (Maximum_evals(evals));
			      if test_fitness generation variant then
			        note_success variant orig generation;
			      variant

			(** prepares for GA by registering available mutations (including templates if
			    applicable) and reducing the search space, and then generates the initial
			    population, using [incoming_pop] if non-empty, or by randomly mutating the
			    [original]. The resulting population is evaluated for fitness before being
			    returned.  This may terminate early if a repair is found in the initial
			    population (by [calculate_fitness]).

			    @param original original variant
			    @param incoming_pop possibly empty, incoming population
			    @return initial_population generated by mutating the original *)
			let initialize_ga (original : ('a,'b) Rep.representation) 
			    (incoming_pop: ('a,'b) GPPopulation.t) : ('a,'b) GPPopulation.t =

			  (* prepare the original/base representation for search by modifying the
			     search space and registering all available mutations.*)
			  original#reduce_search_space (fun _ -> true) (not (!promut <= 0));
			  original#register_mutations 
			    [(Delete_mut,!del_prob); (Append_mut,!app_prob); 
			     (Swap_mut,!swap_prob); (Replace_mut,!rep_prob)];
			  if !templates <> "" then 
			    original#load_templates !templates;
			  let pop = ref incoming_pop in
			    if (llen incoming_pop) > !popsize then
			      pop := first_nth incoming_pop !popsize; 

			    let remainder = !popsize - (llen incoming_pop) in
			      (* include the original in the starting population *)
			      if remainder > 0 then pop := (original#copy ()) :: !pop ;

			      (* initialize the population to a bunch of random mutants *)
			      pop :=
			        GPPopulation.generate !pop  (fun () -> mutate original) !popsize;
			      debug ~force_gui:true 
			        "search: initial population (sizeof one variant = %g MB)\n"
			        (debug_size_in_mb (List.hd !pop));
			      (* compute the fitness of the initial population *)
			      GPPopulation.map !pop (calculate_fitness 0 original)

			(** runs the genetic algorithm for a certain number of iterations, given the
			    most recent/previous generation as input.  Returns the last generation, unless it
			    is killed early by the search strategy/fitness evaluation.  The optional
			    parameters are set to the obvious defaults if omitted. 

			    @param start_gen optional; generation to start on (defaults to 1) 
			    @param num_gens optional; number of generations to run (defaults to
			    [generations]) 
			    @param incoming_population population produced by the previous iteration 
			    @raise Found_Repair if a repair is found
			    @raise Max_evals if the maximum fitness evaluation count is reached
			    @return population produced by this iteration *)
			let run_ga ?start_gen:(start_gen=1) ?num_gens:(num_gens = (!generations))
			    (incoming_population : ('a,'b) GPPopulation.t)
			    (original : ('a,'b) Rep.representation) : ('a,'b) GPPopulation.t =

			  (* the bulk of run_ga is performed by the recursive inner helper
			     function, which Claire modeled off the MatLab code sent to her by the
			     UNM team *)
			  let rec iterate_generations gen incoming_population =
			    if gen < (start_gen + num_gens) then begin
			      debug ~force_gui:true 
			        "search: generation %d (sizeof one variant = %g MB)\n" 
			        gen (debug_size_in_mb (List.hd incoming_population));
			      incr gens_run;
			      (* Step 1: selection *)
			      let selected = GPPopulation.selection incoming_population !popsize in
			      (* Step 2: crossover *)
			      let crossed = GPPopulation.crossover selected original in
			      (* Step 3: mutation *)
			      let mutated = GPPopulation.map crossed (fun one -> mutate one) in
			      (* Step 4. Calculate fitness. *)
			      let pop' = 
			        GPPopulation.map mutated (calculate_fitness gen original) 
			      in
			        (* iterate *)
			        iterate_generations (gen + 1) pop'
			    end else incoming_population
			  in
			    iterate_generations start_gen incoming_population

			(** {b genetic_algorithm } is parametric with respect to a number of choices
			    (e.g., population size, selection method, fitness function, fault localization,
			    many of which are set at the command line or at the representation level.
			    May exit early if exceptions are thrown in fitness evalution ([Max_Evals])
			    or a repair is found [Found_Repair].

			    @param original original variant
			    @param incoming_pop incoming population, possibly empty
			    @raise Found_Repair if a repair is found
			    @raise Max_evals if the maximum fitness evaluation count is set and then reached *)
			let genetic_algorithm (original : ('a,'b) Rep.representation) incoming_pop =
			  debug "search: genetic algorithm begins (|original| = %g MB)\n"
			    (debug_size_in_mb original);
			  assert(!generations >= 0);
			  if !popsize > 0 then begin
			  try begin
			    let initial_population = initialize_ga original incoming_pop in
			      incr gens_run;
			      try 
			        ignore(run_ga initial_population original);
			        debug "search: genetic algorithm ends\n" ;
			      with Maximum_evals(evals) -> 
			        debug "reached maximum evals (%d)\n" evals
			  end with Maximum_evals(evals) -> begin
			    debug "reached maximum evals (%d) during population initialization\n" evals;
			  end
			end

			(***********************************************************************)
			(** constructs a representation out of the genome as specified at the command
			    line and tests to first failure.  This assumes that the oracle genome
			    corresponds to a maximally fit variant.

			    @param original individual representation
			    @param starting_genome string; either a filename (binary representation) or
			    as a string representation of the genome (like the history; this is the more
			    likely use-case)
	 *)
			let oracle_search (orig : ('a,'b) Rep.representation) (starting_genome : string) = 
			  let the_repair = orig#copy () in
			    if Sys.file_exists starting_genome then
			      the_repair#deserialize starting_genome
			    else 
			      the_repair#load_genome_from_string starting_genome;
			    assert(test_to_first_failure the_repair);
			    note_success the_repair orig (1)

			(***********************************************************************)


	 */

}
