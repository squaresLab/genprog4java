/*
 * Copyright (c) 2014-2015, 
 *  Claire Le Goues     <clegoues@cs.cmu.edu>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. The names of the contributors may not be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package clegoues.genprog4java.Search;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Properties;
import java.util.TreeSet;

import clegoues.genprog4java.Fitness.Fitness.Fitness;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.mut.EditOperation;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.rep.Representation;
import clegoues.genprog4java.rep.WeightedAtom;
import clegoues.genprog4java.util.GlobalUtils;
import clegoues.genprog4java.util.Pair;


@SuppressWarnings("rawtypes")
public class Search<G extends EditOperation> {

	private static int generations = 10;
	private static double promut = 1;
	private static boolean continueSearch = false;
	private static double appProb = 0.33333;
	private static double delProb = 0.33333;
	private static double swapProb = 0.0;
	private static double repProb = 0.33333; 
	private static String startingGenome = "";

	private Fitness<G> fitnessEngine = null;
	private int generationsRun = 0;

	public Search(Fitness<G> engine) {
		this.fitnessEngine = engine;
	}
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
		if(props.getProperty("appp") != null) {
			Search.appProb = Double.parseDouble(props.getProperty("appp").trim());
		}
		if(props.getProperty("repp") != null) {
			Search.repProb = Double.parseDouble(props.getProperty("repp").trim());
		}
		if(props.getProperty("delp") != null) {
			Search.delProb = Double.parseDouble(props.getProperty("delp").trim());
		}
		if(props.getProperty("swapp") != null) {
			Search.swapProb = Double.parseDouble(props.getProperty("swapp").trim());
		}
	}


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
	void noteSuccess(Representation<G> rep, Representation<G> original, int generation) {
		System.out.printf("\nRepair Found: " + rep.getName() + "\n");

		Calendar endTime = Calendar.getInstance(); // TODO do something with this
		File repairDir = new File("repair/");
		if(!repairDir.exists()) 
			repairDir.mkdir();
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

	private boolean doWork(Representation<G> rep, Representation<G> original, Mutation mut, int first, int second) {
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
				return true;
			}
		}
		return false;
	}

	private void registerMutations(Representation<G> variant) {
		Comparator<Pair<Mutation,Double>> myComp = new Comparator<Pair<Mutation,Double>>() {
			@Override
			public int compare(Pair<Mutation,Double> one, Pair<Mutation,Double> two) {
				if(one.getFirst().compareTo(two.getFirst()) == 0) {
					return one.getSecond().compareTo(two.getSecond());
				} 
				return one.getFirst().compareTo(two.getFirst());
			}
		};
		TreeSet<Pair<Mutation,Double>> availableMutations = 
				new TreeSet<Pair<Mutation,Double>>(myComp);
		availableMutations.add(new Pair<Mutation,Double>(Mutation.DELETE,Search.delProb));
		availableMutations.add(new Pair<Mutation,Double>(Mutation.APPEND,Search.appProb));
		availableMutations.add(new Pair<Mutation,Double>(Mutation.SWAP,Search.swapProb));
		availableMutations.add(new Pair<Mutation,Double>(Mutation.REPLACE,Search.repProb));

		Representation.registerMutations(availableMutations);
	}
	public boolean bruteForceOne(Representation<G> original) {

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
		boolean repairFound = false;

		TreeSet<WeightedAtom> rescaledAtoms = rescaleAtomPairs(allFaultyAtoms); 


		for(WeightedAtom faultyAtom : rescaledAtoms) {
			int stmt = faultyAtom.getAtom();
			double weight = faultyAtom.getWeight();
			Comparator<Pair<Mutation,Double>> descendingMutations = new Comparator<Pair<Mutation,Double>>() {
				@Override
				public int compare(Pair<Mutation,Double> one, Pair<Mutation,Double> two) {
					return (new Double(two.getSecond())).compareTo((new Double(one.getSecond())));
				}
			};
			//  wouldn't real polymorphism be the actual legitimate best right here?
			TreeSet<Pair<Mutation,Double>> availableMutations = original.availableMutations(stmt);
			TreeSet<Pair<Mutation,Double>> rescaledMutations = new TreeSet<Pair<Mutation,Double>>(descendingMutations);
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
			Comparator<WeightedAtom> descendingAtom = new Comparator<WeightedAtom>() {
				@Override
				public int compare(WeightedAtom one, WeightedAtom two) {
					return (new Double(two.getWeight())).compareTo((new Double(one.getWeight())));
				}
			};
			for(Pair<Mutation,Double> mutation : rescaledMutations) {
				Mutation mut = mutation.getFirst();
				double prob = mutation.getSecond();
				System.out.printf("%3g %3g", weight, prob);
				if(mut == Mutation.DELETE) {
					Representation<G> rep = original.copy(); 
					if(this.doWork(rep, original, mut, stmt, stmt)) {
						wins++;
						repairFound = true;
					}
				} else if (mut == Mutation.APPEND) {
					TreeSet<WeightedAtom> appendSources = new TreeSet<WeightedAtom>(descendingAtom);
					appendSources.addAll(this.rescaleAtomPairs(original.appendSources(stmt)));
					for(WeightedAtom append : appendSources) {
						Representation<G> rep = original.copy(); 
						if(this.doWork(rep, original, mut, stmt, append.getAtom())) {
							wins++;
							repairFound = true;
						}
					}
				} else if (mut == Mutation.REPLACE) {
					TreeSet<WeightedAtom> replaceSources = new TreeSet<WeightedAtom>(descendingAtom);
					replaceSources.addAll(this.rescaleAtomPairs(original.replaceSources(stmt)));
					for(WeightedAtom replace : replaceSources) {
						Representation<G> rep = original.copy();
						if(this.doWork(rep, original, mut, stmt, replace.getAtom())) {
							wins++;
							repairFound = true;
						}
					}

				} else if (mut == Mutation.SWAP ) {
					TreeSet<WeightedAtom> swapSources = new TreeSet<WeightedAtom>(descendingAtom);
					swapSources.addAll(this.rescaleAtomPairs(original.swapSources(stmt)));
					for(WeightedAtom swap : swapSources) {
						Representation<G> rep = original.copy();
						if(this.doWork(rep, original, mut, stmt, swap.getAtom())) {
							wins++;
							repairFound = true;
						}
					}
				}
				// FIXME: debug output System.out.printf("\t variant " + wins + "/" + sofar + "/" + count + "(w: " + probs +")" + rep.getName());
				sofar++;
				if(repairFound && !Search.continueSearch){
					return true;
				}
			}
		}
		System.out.printf("search: brute_force_1 ends\n");
		return repairFound;
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
		ArrayList faultyAtoms =variant.getFaultyAtoms();
		ArrayList<WeightedAtom> proMutList = new ArrayList<WeightedAtom>();
		for(int i = 0; i < Search.promut; i++) {
			proMutList.add((WeightedAtom) GlobalUtils.chooseOneWeighted(faultyAtoms));

		}
		for(WeightedAtom atom : proMutList) {	
			int stmtid = atom.getAtom();
			TreeSet<Pair<Mutation,Double>> availableMutations = variant.availableMutations(stmtid);
			Pair<Mutation,Double> chosenMutation = (Pair<Mutation, Double>) GlobalUtils.chooseOneWeighted(new ArrayList(availableMutations));
			Mutation mut = chosenMutation.getFirst();
			// FIXME: make sure the mutation list isn't empty before choosing?
			switch(mut) {
			case DELETE: variant.delete(stmtid);
			break;
			case APPEND:
				TreeSet<WeightedAtom> allowed = variant.appendSources(stmtid);
				WeightedAtom after = (WeightedAtom) GlobalUtils.chooseOneWeighted(new ArrayList(allowed));
				variant.append(stmtid,  after.getAtom());
				break;
			case SWAP:
				TreeSet<WeightedAtom> swapAllowed = variant.swapSources(stmtid);
				WeightedAtom swapWith = (WeightedAtom) GlobalUtils.chooseOneWeighted(new ArrayList(swapAllowed));
				variant.swap(stmtid, swapWith.getAtom());
				break;
			case REPLACE:
				TreeSet<WeightedAtom> replaceAllowed = variant.replaceSources(stmtid);
				WeightedAtom replaceWith = (WeightedAtom) GlobalUtils.chooseOneWeighted(new ArrayList(replaceAllowed));
				variant.replace(stmtid, replaceWith.getAtom());
				break;
			}
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
	// FIXME hm there has got to be way to lose the RepairFoundException, hmmmmm
	private Population<G> initializeGa(Representation<G> original, Population<G> incomingPopulation) throws RepairFoundException {
		original.reduceSearchSpace(); // FIXME: this had arguments originally
		this.registerMutations(original);

		Population<G> initialPopulation = incomingPopulation;

		if(incomingPopulation != null && incomingPopulation.size() > incomingPopulation.getPopsize()) {
			initialPopulation = incomingPopulation.firstN(incomingPopulation.getPopsize());
		} // FIXME: this is too functional I think. 
		int stillNeed = initialPopulation.getPopsize() - initialPopulation.size();
		if(stillNeed > 0) {
			initialPopulation.add(original.copy());
		}
		stillNeed--;
		for(int i = 0; i < stillNeed; i++ ) {
			Representation<G> newItem = original.copy();
			this.mutate(newItem);
			initialPopulation.add(newItem);
		}

		for(Representation<G> item : initialPopulation) {
			if(fitnessEngine.testFitness(0, item)) {
				this.noteSuccess(item,original,0);
			}
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
	private void runGa(int startGen, int numGens, Population<G> incomingPopulation, Representation<G> original) throws RepairFoundException {
		/*
		 * the bulk of run_ga is performed by the recursive inner helper
			     function, which Claire modeled off the MatLab code sent to her by the
			     UNM team */	
		int gen = startGen;
		while(gen < startGen + numGens) { // FIXME: gensRun vs. generationsRun?
			System.out.println("search: generation" + gen); 
			generationsRun++;
			assert(incomingPopulation.getPopsize() > 0);
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
				if(fitnessEngine.testFitness(gen,item)) {
					this.noteSuccess(item,original,gen);
				}
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
		System.out.printf("search: genetic algorithm begins\n");
		assert(Search.generations >= 0);

		Population<G> initialPopulation = this.initializeGa(original, incomingPopulation);
		generationsRun++;
		this.runGa(1, Search.generations, initialPopulation, original);

	}

	/*	 constructs a representation out of the genome as specified at the command
    line and tests to first failure.  This assumes that the oracle genome
    corresponds to a maximally fit variant.

    @param original individual representation
    @param starting_genome string; a string representation of the genome 
	 */
	public void oracleSearch(Representation<G> original) throws RepairFoundException {
		Representation<G> theRepair = original.copy();
		theRepair.loadGenomeFromString(Search.startingGenome);
		assert(fitnessEngine.testToFirstFailure(theRepair));
		this.noteSuccess(theRepair, original, 1);
	}

}
