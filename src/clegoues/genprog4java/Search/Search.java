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

import static clegoues.util.ConfigurationBuilder.BOOLEAN;
import static clegoues.util.ConfigurationBuilder.DOUBLE;
import static clegoues.util.ConfigurationBuilder.INT;
import static clegoues.util.ConfigurationBuilder.STRING;

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
import clegoues.util.ConfigurationBuilder;
import clegoues.util.GlobalUtils;
import clegoues.util.Pair;

@SuppressWarnings("rawtypes")
public abstract class Search<G extends EditOperation> {
	protected Logger logger = Logger.getLogger(Search.class);

	public static final ConfigurationBuilder.RegistryToken token =
		ConfigurationBuilder.getToken();
	
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
	protected Fitness<G> fitnessEngine = null;

	public Search(Fitness<G> engine) {
		this.fitnessEngine = engine;
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
			switch(edit.toLowerCase()) {
			case "append": mutations.put(Mutation.APPEND, weight); break;
			case "swap":  mutations.put(Mutation.SWAP, weight); break;
			case "delete":  mutations.put(Mutation.DELETE, weight); break;
			case "replace":  mutations.put(Mutation.REPLACE, weight); break;
			case "nullinsert":  mutations.put(Mutation.NULLINSERT, weight); break;
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

		logger.info("\n\nREPAIR FOUND: " + rep.getName() + " (in " + rep.getVariantFolder() + ")\n\n");
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
			Population<G> incomingPopulation) throws RepairFoundException;
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
		}
	}

	protected abstract void runAlgorithm(Representation<G> original, Population<G> initialPopulation) throws RepairFoundException;


}
