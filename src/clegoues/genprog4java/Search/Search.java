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

// oneday FIXME: lowercase the package name because it annoys me...
import static clegoues.util.ConfigurationBuilder.BOOLEAN;
import static clegoues.util.ConfigurationBuilder.DOUBLE;
import static clegoues.util.ConfigurationBuilder.INT;
import static clegoues.util.ConfigurationBuilder.STRING;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.EditOperation;
import clegoues.genprog4java.mut.Location;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.WeightedHole;
import clegoues.genprog4java.mut.WeightedMutation;
import clegoues.genprog4java.rep.Representation;
import clegoues.util.ConfigurationBuilder;
import clegoues.util.GlobalUtils;
import clegoues.util.Pair;

@SuppressWarnings("rawtypes")
public abstract class Search<G extends EditOperation> {
	protected Logger logger = Logger.getLogger(Search.class);

	ReplacementModel rm;

	public static final ConfigurationBuilder.RegistryToken token =
			ConfigurationBuilder.getToken();

	public static Boolean checkSpace = ConfigurationBuilder.of(BOOLEAN ) 
			.withVarName("checkSpace")
			.withDefault("true")
			.withHelp("whether to do search space size check")
			.inGroup("Search Parameters")
			.build();

	protected static String model = ConfigurationBuilder.of( STRING )
			.withVarName( "model" )
			.withDefault( "default" )
			.withHelp( "model chosen to pick the fix atom from the pool of possible fix atoms with respect to the buggy atom" )
			.inGroup( "Search Parameters" )
			.build();
	protected static String modelPath = ConfigurationBuilder.of( STRING )
			.withVarName( "modelPath" )
			.withDefault( "OVERALLModel.txt" )
			.withHelp( "path of the model" )
			.inGroup( "Search Parameters" )
			.build();
	//private static int generations = 10;
	protected static int generations = ConfigurationBuilder.of( INT )
			.withVarName( "generations" )
			.withDefault( "10" )
			.withHelp( "number of search generations to run" )
			.inGroup( "Search Parameters" )
			.build();
	//The proportional mutation rate, which controls the probability that a genome is mutated in the mutation step in terms of the number of genes within it should be modified.
	//private static double promut = 1; 
	protected static double promut = ConfigurationBuilder.of( DOUBLE )
			.withVarName( "promut" )
			.withFlag( "pMutation" )
			.withDefault( "1" )
			.withHelp( "the proportional mutation rate = number of genes to modify" )
			.inGroup( "Search Parameters" )
			.build();
	//private static boolean continueSearch = false;
	static boolean continueSearch = ConfigurationBuilder.of( BOOLEAN )
			.withVarName( "continueSearch" )
			.withFlag( "continue" )
			.withHelp( "continue searching after finding a repair" )
			.inGroup( "Search Parameters" )
			.build();

	//20 mutations 1/20 = 0.05
	//public static HashMap<Mutation,Double> availableMutations = new HashMap<Mutation,Double>();
	public static Map< Mutation, Double > availableMutations =
			new ConfigurationBuilder< Map< Mutation, Double > >()
			.withVarName( "availableMutations" )
			.withFlag( "edits" )
			.withDefault( "append;replace;delete" )
			.withHelp( "mutations to use in search, with optional weights" )
			.inGroup( "Search Parameters" )
			.withCast( new ConfigurationBuilder.LexicalCast< Map< Mutation, Double > >() {
				public Map<Mutation, Double> parse(String value) {
					String[] values = value.toLowerCase().split( ";" );
					for ( int i = 0; i < values.length; ++i )
						values[ i ] = values[ i ].trim();
					return parseEdits(
							values, new HashMap< Mutation, Double >()
							);
				}
			})
			.build();


	//public static String searchStrategy = "ga";
	public static String searchStrategy = ConfigurationBuilder.of( STRING )
			.withVarName( "searchStrategy" )
			.withFlag( "search" )
			.withDefault( "ga" )
			.withHelp( "the search strategy to employ" )
			.inGroup( "Search Parameters" )
			.build();
	protected Fitness fitnessEngine = null;

	public Search(Fitness engine) {
		this.fitnessEngine = engine;
		if(Search.model.equalsIgnoreCase("probabilistic")){
			rm = new ReplacementModel();
			rm.populateModel(modelPath);
		}
	}

	public static Map<Mutation,Double> parseEdits(String[] editList, Map<Mutation,Double> mutations) {
		for(String oneItem : editList) {
			String edit = "";
			Double weight = 1.0;
			if ( oneItem.contains( "," ) ) {
				String[] editAndWeight = oneItem.split(",");
				edit = editAndWeight[0];
				weight = Double.parseDouble(editAndWeight[1]);
			} else {
				edit = oneItem;
			}
			
			//;parrep;paradd;parrem;exprep;expadd;exprem;nullcheck;rangecheck;sizecheck;castcheck;lbset;offbyone;ubset
			switch(edit.toLowerCase()) {
			case "append": mutations.put(Mutation.APPEND, weight); break;
			case "swap":  mutations.put(Mutation.SWAP, weight); break;
			case "delete":  mutations.put(Mutation.DELETE, weight); break;
			case "replace":  mutations.put(Mutation.REPLACE, weight); break;
			case "funrep":  mutations.put(Mutation.FUNREP, weight); break;
			case "parrep":  mutations.put(Mutation.PARREP, weight); break;
			case "paradd":  mutations.put(Mutation.PARADD, weight); break;
			case "parrem":  mutations.put(Mutation.PARREM, weight); break;
			case "exprep":  mutations.put(Mutation.EXPREP, weight); break;
			case "expadd":  mutations.put(Mutation.EXPADD, weight); break;
			case "exprem":  mutations.put(Mutation.EXPREM, weight); break;
			case "nullcheck":  mutations.put(Mutation.NULLCHECK, weight); break;
			case "objinit":  mutations.put(Mutation.OBJINIT, weight); break;
			case "rangecheck":  mutations.put(Mutation.RANGECHECK, weight); break;
			case "sizecheck":  mutations.put(Mutation.SIZECHECK, weight); break;
			case "castcheck":  mutations.put(Mutation.CASTCHECK, weight); break;
			case "lbset":  mutations.put(Mutation.LBOUNDSET, weight); break;
			case "ubset":  mutations.put(Mutation.UBOUNDSET, weight); break;
			case "offbyone":  mutations.put(Mutation.OFFBYONE, weight); break;
			}
		}
		return mutations;
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
	protected abstract Population<G> initialize(Representation<G> original,
			Population<G> incomingPopulation) throws RepairFoundException, GiveUpException;

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
		ArrayList<Location> faultyAtoms = variant.getFaultyLocations();
		ArrayList<Location> proMutList = new ArrayList<Location>();
		boolean foundMutationThatCanApplyToAtom = false;
		while(!foundMutationThatCanApplyToAtom){
			//promut default is 1 // promut stands for proportional mutation rate, which controls the probability that a genome is mutated in the mutation step in terms of the number of genes within it should be modified.
			for (int i = 0; i < Search.promut; i++) {
				//chooses a random location
				Pair<?, Double> wa = null;
				boolean alreadyOnList = false;
				//If it already picked all the fix atoms from current FixLocalization, then start picking from the ones that remain
				if(proMutList.size()>=faultyAtoms.size()){ 
					variant.setAllPossibleStmtsToFixLocalization();				}
				//only adds the random atom if it is different from the others already added
				do {
					//chooses a random faulty atom from the subset of faulty atoms
					wa = GlobalUtils.chooseOneWeighted(new ArrayList(faultyAtoms));
					// insert a check to see if this location has any valid mutations?  If not, look again
					// if not, somehow tell the variant to remove that location from the list of faulty atoms
					alreadyOnList = proMutList.contains(wa);
				} while(alreadyOnList);
				proMutList.add((Location)wa);
			}
			for (Location location : proMutList) {
				// FIXME: deal with the case where there are no edits that apply ever, because infinite loop
				//the available mutations for this stmt
				Set<WeightedMutation> availableMutations = variant.availableMutations(location);
				if(availableMutations.isEmpty()){
					continue; 
				}else{
					foundMutationThatCanApplyToAtom = true;
				}
				//choose a mutation at random
				Pair<Mutation, Double> chosenMutation = (Pair<Mutation, Double>) GlobalUtils.chooseOneWeighted(new ArrayList(availableMutations));
				Mutation mut = chosenMutation.getFirst();
				List<WeightedHole> allowed = variant.editSources(location, mut);
				allowed = rescaleAllowed(mut,allowed, variant,location.getId());
				WeightedHole selected = (WeightedHole) GlobalUtils
						.chooseOneWeighted(new ArrayList(allowed));
				variant.performEdit(mut, location, selected.getHole());

			}
		}
	}

	private List rescaleAllowed(Mutation mut, List<WeightedHole> allowed, Representation variant, int stmtid) {
		if(mut != Mutation.REPLACE || Search.model.equalsIgnoreCase("default")){
			return allowed;
		}else if(Search.model.equalsIgnoreCase("probabilistic")){
			return rm.rescaleBasedOnModel(new ArrayList(allowed), variant, stmtid);
		}
		return null;
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
	public void doSearch(Representation<G> original,
			Population<G> incomingPopulation) throws
	CloneNotSupportedException {

		try {
			this.runAlgorithm(original, incomingPopulation);
		} catch(RepairFoundException e) {
			return;
		} catch (GiveUpException e) {
			return;
		}
	}

	protected abstract void runAlgorithm(Representation<G> original, Population<G> initialPopulation) throws RepairFoundException, GiveUpException;


}
