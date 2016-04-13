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
public abstract class Search<G extends EditOperation> {
	protected Logger logger = Logger.getLogger(Search.class);

	protected static boolean continueSearch = false;

	//20 mutations 1/20 = 0.05
	public static HashMap<Mutation,Double> availableMutations = new HashMap<Mutation,Double>();

	public static String searchStrategy = "ga";
	protected Fitness<G> fitnessEngine = null;

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
