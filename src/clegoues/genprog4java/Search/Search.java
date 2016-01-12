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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.mut.EditOperation;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.rep.Representation;
import clegoues.genprog4java.rep.WeightedAtom;
import clegoues.genprog4java.util.GlobalUtils;
import clegoues.genprog4java.util.Pair;

@SuppressWarnings("rawtypes")
public class Search<G extends EditOperation> {
	protected Logger logger = Logger.getLogger(Search.class);

	private static int generations = 10;
	//The proportional mutation rate, which controls the probability that a genome is mutated in the mutation step in terms of the number of genes within it should be modified.
	private static double promut = 1; 
	private static boolean continueSearch = false;

	//20 mutations 1/20 = 0.05
	public static HashMap<Mutation,Double> availableMutations = new HashMap<Mutation,Double>();

	private static String startingGenome = "";
	public static String searchStrategy = "ga";
	private Fitness<G> fitnessEngine = null;
	private int generationsRun = 0;

	public Search(Fitness<G> engine) {
		this.fitnessEngine = engine;
	}

	public static void parseEdits(String[] editList, Boolean withWeight) {
		for(String oneItem : editList) {
			String edit = "";
			Double weight = 1.0;
			if(withWeight) {
				String[] editAndWeight = oneItem.split(",");
				edit = editAndWeight[0];
				weight = Double.parseDouble(editAndWeight[1]);
			} else {
				edit = oneItem;
			}
			switch(edit.toLowerCase()) {
			case "append": availableMutations.put(Mutation.APPEND, weight); break;
			case "swap":  availableMutations.put(Mutation.SWAP, weight); break;
			case "delete":  availableMutations.put(Mutation.DELETE, weight); break;
			case "replace":  availableMutations.put(Mutation.REPLACE, weight); break;
			case "nullinsert":  availableMutations.put(Mutation.NULLINSERT, weight); break;
			case "funrep":  availableMutations.put(Mutation.FUNREP, weight); break;
			case "parrep":  availableMutations.put(Mutation.PARREP, weight); break;
			case "paradd":  availableMutations.put(Mutation.PARADD, weight); break;
			case "parrem":  availableMutations.put(Mutation.PARREM, weight); break;
			case "exprep":  availableMutations.put(Mutation.EXPREP, weight); break;
			case "expadd":  availableMutations.put(Mutation.EXPADD, weight); break;
			case "exprem":  availableMutations.put(Mutation.EXPREM, weight); break;
			case "nullcheck":  availableMutations.put(Mutation.NULLCHECK, weight); break;
			case "objinit":  availableMutations.put(Mutation.OBJINIT, weight); break;
			case "rangecheck":  availableMutations.put(Mutation.RANGECHECK, weight); break;
			case "sizecheck":  availableMutations.put(Mutation.SIZECHECK, weight); break;
			case "castcheck":  availableMutations.put(Mutation.CASTCHECK, weight); break;
			case "lbset":  availableMutations.put(Mutation.LBOUNDSET, weight); break;
			case "ubset":  availableMutations.put(Mutation.UBOUNDSET, weight); break;
			case "offbyone":  availableMutations.put(Mutation.OFFBYONE, weight); break;
			}
		}
	}
	public static void configure(Properties props) {
		try {
			if (props.getProperty("generations") != null) {
				Search.generations = Integer.parseInt(props.getProperty(
						"generations").trim());
			}
			if (props.getProperty("oracleGenome") != null) {
				Search.startingGenome = props.getProperty("startingGenome").trim();
			}
			if (props.getProperty("promut") != null) {
				Search.promut = Double.parseDouble(props.getProperty("pMutation")
						.trim());
			}
			if (props.getProperty("continue") != null) {
				Search.continueSearch = true;
			}
			if(props.getProperty("edits") != null) {
				String edits = props.getProperty("edits");
				edits = edits.toLowerCase().trim();
				String[] editList = edits.split(";");
				parseEdits(editList, false);
			} else if (props.getProperty("editsWithWeights") != null) { 
				String edits = props.getProperty("editsWithWeights");
				edits = edits.toLowerCase().trim();
				String[] editList = edits.split(",");
				parseEdits(editList, true);
			} else { // edits set to defaults
				availableMutations.put(Mutation.APPEND, 1.0);
				availableMutations.put(Mutation.REPLACE, 1.0);
				availableMutations.put(Mutation.DELETE, 1.0); 
			}

			if (props.getProperty("search") != null) {
				Search.searchStrategy = props.getProperty("search").trim();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * Different strategies and representation types can do different things
	 * when a repair is found. This at least stores information about the
	 * successful variant and may write it to disk or otherwise dispatch to the
	 * successful variant itself (potentially leading to minimization, for
	 * example, depending on the representation and command-line arguments).
	 * 
	 * @param rep successful variant
	 * 
	 * @param orig original variant
	 * 
	 * @param generation generation in which the repair was found
	 */
	void noteSuccess(Representation<G> rep, Representation<G> original,
			int generation) {

		logger.info("\nRepair Found: " + rep.getName() + " (in " + rep.getVariantFolder() + ")\n");

		File repairDir = new File("repair/");
		if (!repairDir.exists())
			repairDir.mkdir();
		String repairFilename = "repair/repair."
				+ Configuration.globalExtension;
		rep.outputSource(repairFilename);
	}

	private TreeSet<WeightedAtom> rescaleAtomPairs(TreeSet<WeightedAtom> items) {
		double fullSum = 0.0;
		TreeSet<WeightedAtom> retVal = new TreeSet<WeightedAtom>();
		for (Pair<?, Double> item : items) {
			fullSum += item.getSecond();
		}
		double scale = 1.0 / fullSum;
		for (WeightedAtom item : items) {
			WeightedAtom newItem = new WeightedAtom(item.getAtom(),
					item.getWeight() * scale);
			retVal.add(newItem);
		}
		return retVal;
	}

	private boolean doWork(Representation<G> rep, Representation<G> original,
			Mutation mut, int first, int second) {
		rep.performEdit(mut, first, second);
		if (fitnessEngine.testToFirstFailure(rep)) {
			this.noteSuccess(rep, original, 1);
			if (!Search.continueSearch) {
				return true;
			}
		}
		return false;
	}

	public boolean bruteForceOne(Representation<G> original) {

		original.reduceFixSpace();

		int count = 0;
		TreeSet<WeightedAtom> allFaultyAtoms = new TreeSet<WeightedAtom>(
				original.getFaultyAtoms());

		for (WeightedAtom faultyAtom : allFaultyAtoms) {
			int faultyLocation = faultyAtom.getAtom();

			for(Map.Entry mutation : availableMutations.entrySet()) {
				Mutation key = (Mutation) mutation.getKey();
				Double prob = (Double) mutation.getValue();
				if(prob > 0.0) {
					count += original.editSources(faultyLocation, key).size(); 
				}
			}

		}
		logger.info("search: bruteForce: " + count
				+ " mutants in search space\n");

		int wins = 0;
		int sofar = 1;
		boolean repairFound = false;

		TreeSet<WeightedAtom> rescaledAtoms = rescaleAtomPairs(allFaultyAtoms);

		for (WeightedAtom faultyAtom : rescaledAtoms) {
			int stmt = faultyAtom.getAtom();
			double weight = faultyAtom.getWeight();
			Comparator<Pair<Mutation, Double>> descendingMutations = new Comparator<Pair<Mutation, Double>>() {
				@Override
				public int compare(Pair<Mutation, Double> one,
						Pair<Mutation, Double> two) {
					return (new Double(two.getSecond())).compareTo((new Double(
							one.getSecond())));
				}
			};
			// wouldn't real polymorphism be the actual legitimate best right
			// here?
			TreeSet<Pair<Mutation, Double>> availableMutations = original
					.availableMutations(stmt);
			TreeSet<Pair<Mutation, Double>> rescaledMutations = new TreeSet<Pair<Mutation, Double>>(
					descendingMutations);
			double sumMutScale = 0.0;
			for (Pair<Mutation, Double> item : availableMutations) {
				sumMutScale += item.getSecond();
			}
			double mutScale = 1 / sumMutScale;
			for (Pair<Mutation, Double> item : availableMutations) {
				rescaledMutations.add(new Pair<Mutation, Double>(item
						.getFirst(), item.getSecond() * mutScale));
			}

			// rescaled Mutations gives us the mutation,weight pairs available
			// at this atom
			// which itself has its own weight
			Comparator<WeightedAtom> descendingAtom = new Comparator<WeightedAtom>() {
				@Override
				public int compare(WeightedAtom one, WeightedAtom two) {
					return (new Double(two.getWeight())).compareTo((new Double(
							one.getWeight())));
				}
			};
			for (Pair<Mutation, Double> mutation : rescaledMutations) {
				Mutation mut = mutation.getFirst();
				double prob = mutation.getSecond();
				logger.info(weight + " " + prob);
				switch(mut) {
				case DELETE:
					Representation<G> delRep = original.copy();
					if (this.doWork(delRep, original, mut, stmt, stmt)) {
						wins++;
						repairFound = true;
					}
					break;
				case APPEND:
				case REPLACE:
				case OFFBYONE:
					TreeSet<WeightedAtom> sources1 = new TreeSet<WeightedAtom>(
							descendingAtom);
					sources1.addAll(this.rescaleAtomPairs(original
							.editSources(stmt, mut)));
					for (WeightedAtom append : sources1) {
						Representation<G> rep = original.copy();
						if (this.doWork(rep, original, mut, stmt,
								append.getAtom())) {
							wins++;
							repairFound = true;
						}
					}
					break;
				case SWAP:
					TreeSet<WeightedAtom> sources = new TreeSet<WeightedAtom>(
							descendingAtom);
					sources.addAll(this.rescaleAtomPairs(original
							.editSources(stmt, mut)));
					for (WeightedAtom append : sources) {
						Representation<G> rep = original.copy();
						if (this.doWork(rep, original, mut, stmt,
								append.getAtom())) {
							wins++;
							repairFound = true;
						}
					}
					break;
				default:
					logger.fatal("FATAL: unhandled template type in bruteForceOne.  Add handling (probably by adding a case either to the DELETE case or the other one); and try again");
					break;
				}


			}
			// FIXME: debug output System.out.printf("\t variant " + wins +
			// "/" + sofar + "/" + count + "(w: " + probs +")" +
			// rep.getName());
			sofar++;
			if (repairFound && !Search.continueSearch) {
				return true;
			}
		}
		logger.info("search: brute_force_1 ends\n");
		return repairFound;
	}

	/*
	 * 
	 * Basic Genetic Algorithm
	 */

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
			for (int i = 0; i < Search.promut; i++) {
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
				// FIXME: make sure the mutation list isn't empty before choosing?
				switch (mut) {
				case DELETE:
					// FIXME: this -1 hack is pretty gross; note to self, CLG should fix it
					variant.performEdit(mut, stmtid, (-1));
					break;
				case APPEND:
				case SWAP:
				case REPLACE: 
					TreeSet<WeightedAtom> allowed = variant.editSources(stmtid,mut);
					WeightedAtom after = (WeightedAtom) GlobalUtils
							.chooseOneWeighted(new ArrayList(allowed));
					variant.performEdit(mut, stmtid,  after.getAtom()); 
					break;
				case OFFBYONE:
					TreeSet<WeightedAtom> allow = variant.editSources(stmtid,mut);
					WeightedAtom aftr = (WeightedAtom) GlobalUtils
							.chooseOneWeighted(new ArrayList(allow));
					variant.performEdit(mut, stmtid,  aftr.getAtom()); 
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
	private Population<G> initializeGa(Representation<G> original,
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
		}
		stillNeed--;
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
	private void runGa(int startGen, int numGens,
			Population<G> incomingPopulation, Representation<G> original) {
		/*
		 * the bulk of run_ga is performed by the recursive inner helper
		 * function, which Claire modeled off the MatLab code sent to her by the
		 * UNM team
		 */
		int gen = startGen;
		while (gen < startGen + numGens) {
			logger.info("search: generation" + gen);
			generationsRun++;
			assert (incomingPopulation.getPopsize() > 0);
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

	/*
	 * {b genetic_algorithm } is parametric with respect to a number of choices
	 * (e.g., population size, selection method, fitness function, fault
	 * localization, many of which are set at the command line or at the
	 * representation level. May exit early if exceptions are thrown in fitness
	 * evaluation ([Max_Evals]) or a repair is found [Found_Repair].
	 * 
	 * @param original original variant
	 * 
	 * @param incoming_pop incoming population, possibly empty
	 * 
	 * @raise Found_Repair if a repair is found
	 * 
	 * @raise Max_evals if the maximum fitness evaluation count is set and then
	 * reached
	 */
	public void geneticAlgorithm(Representation<G> original,
			Population<G> incomingPopulation) throws
			CloneNotSupportedException {
		logger.info("search: genetic algorithm begins\n");
		assert (Search.generations >= 0);

		try {
			Population<G> initialPopulation = this.initializeGa(original,
					incomingPopulation);
			generationsRun++;
			this.runGa(1, Search.generations, initialPopulation, original);
		} catch(RepairFoundException e) {
			return;
		}

	}

	/*
	 * constructs a representation out of the genome as specified at the command
	 * line and tests to first failure. This assumes that the oracle genome
	 * corresponds to a maximally fit variant.
	 * 
	 * @param original individual representation
	 * 
	 * @param starting_genome string; a string representation of the genome
	 */
	public void oracleSearch(Representation<G> original){
		Representation<G> theRepair = original.copy();
		theRepair.loadGenomeFromString(Search.startingGenome);
		assert (fitnessEngine.testToFirstFailure(theRepair));
		this.noteSuccess(theRepair, original, 1);
	}

	public void ioSearch(Representation<G> original) {
		throw new UnsupportedOperationException();
	}

}
