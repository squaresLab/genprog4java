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

import static clegoues.util.ConfigurationBuilder.DOUBLE;
import static clegoues.util.ConfigurationBuilder.INT;
import static clegoues.util.ConfigurationBuilder.STRING;
import static clegoues.util.ConfigurationBuilder.BOOLEAN;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.mut.EditOperation;
import clegoues.genprog4java.rep.Representation;
import clegoues.util.ConfigurationBuilder;
import clegoues.util.GlobalUtils;

public class Population<G extends EditOperation> implements Iterable<Representation<G>>{

	protected static Logger logger = Logger.getLogger(Fitness.class);
	
	public static final ConfigurationBuilder.RegistryToken token =
		ConfigurationBuilder.getToken();
	
	private static int popsize = ConfigurationBuilder.of( INT )
		.withVarName( "popsize" )
		.withDefault( "40" )
		.withHelp( "size of the population" )
		.inGroup( "Population Parameters" )
		.withCast( new ConfigurationBuilder.LexicalCast< Integer >(){
			public Integer parse(String value) {
				int size = Integer.parseInt( value );
				tournamentK = size / 5;
				return size;
			}
		} )
		.build();
	//private static double crossp = 0.5; 
	private static double crossp = ConfigurationBuilder.of( DOUBLE )
		.withVarName( "crossp" )
		.withDefault( "0.5" )
		.withHelp( "probability of crossover" )
		.inGroup( "Population Parameters" )
		.build();
	//tournament size, 20% of the population, set when popsize is updated
	private static int tournamentK;
	private double tournamentP = 1.0; //tournament probability
	//private static String crossover = "onepoint";
	private static String crossover = ConfigurationBuilder.of( STRING )
		.withVarName( "crossover" )
		.withDefault( "onepoint" )
		.withHelp( "crossover algorithm" )
		.inGroup( "Population Parameters" )
		.build();
	private static Boolean multiObjectiveFitness = ConfigurationBuilder.of( BOOLEAN )
		.withVarName( "multiObjectiveFitness" )
		.withDefault( "false" )
		.withHelp( "true if multi objective fitness is desired" )
		.inGroup( "Population Parameters" )
		.build();
	private static double diversityContribution = ConfigurationBuilder.of( DOUBLE )
		.withVarName( "diversityContribution" )
		.withDefault( "0.5" )
		.withHelp( "the percentage that the diversity score contributes to fitness" )
		.inGroup( "Population Parameters" )
		.build();
	private static double correctnessContribution = ConfigurationBuilder.of( DOUBLE )
		.withVarName( "correctnessContribution" )
		.withDefault( "0.5" )
		.withHelp( "the percentage that the correctness score contributes to fitness" )
		.inGroup( "Population Parameters" )
		.build();
	private ArrayList<Representation<G>> population = new ArrayList<Representation<G>>(this.popsize);

	public Population() {

	}

	public Population(ArrayList<Representation<G>> smallerPop) {
		this.population = smallerPop;
	}

	protected ArrayList<Representation<G>> getPopulation() {
		return this.population;
	}

	public int getPopsize() {
		return Population.popsize;
	}

	/* {b serialize} serializes a population to disk.  The first variant is
	      optionally instructed to print out the global information necessary for a
	      collection of representations.  The remaining variants print out only
	      their variant-specific local information */
	void serialize() {
		throw new UnsupportedOperationException();
		/*
			  let serialize ?out_channel (population : ('a,'b) t) (filename : string) =
			    match !output_format with
			      "bin" | "binary" ->
			        let fout = 
			          match out_channel with
			            Some(v) -> v
			          | None -> open_out_bin filename 
			        in
			          Marshal.to_channel fout (population_version) [] ;
			          liter (fun variant -> variant#serialize ?out_channel:(Some(fout)) ?global_info:(Some(false)) filename) population;
			          if out_channel = None then close_out fout
			    | "txt" ->
			      debug "serializing population to txt; ?out_channel ignored\n";
			      let fout = open_out filename in 
			        liter (fun variant -> 
			          let name = variant#name () in
			            output_string fout (name^"\n"))
			          population;
			        if out_channel = None then close_out fout
		 */
	}


	/* {b deserialize} deserializes a population from disk, to be used as
	      incoming_pop.  The incoming variant is assumed to have loaded the global
	      state (which CLG doesn't love so she might change it).  Remaining variants
	      are read in individually, using only their own local information */
	/* deserialize can fail if the file does not conform to the expected format
	     for Marshal or if there is a version mismatch between the population module
	     that wrote the binary file and this one (that is loading it). */
	void deserialize(String filename) {
		throw new UnsupportedOperationException();
		/*
			  let deserialize ?in_channel filename original = 
			    (* the original should have loaded the global state *)
			    let fin = 
			      match in_channel with
			        Some(v) -> v
			      | None -> open_in_bin filename in
			    let pop = ref [original] in
			      try
			        if !output_format = "txt" then 
			          failwith "txt format, skipping binary attempt";
			        let version = Marshal.from_channel fin in
			          if version <> population_version then begin
			            debug "population: %s has old version: %s\n" filename version;
			            failwith "version mismatch" 
			          end ;
			          let attempt = ref 1 in
			          try
			            while true do
			              debug "attempt %d\n" !attempt; incr attempt;
			              let rep' = original#copy () in
			                rep'#deserialize ?in_channel:(Some(fin)) ?global_info:(None) filename;
			                pop := rep'::!pop
			            done; !pop
			          with End_of_file -> !pop
			      with _ -> begin
			        close_in fin;
			        pop := [original];
			        try
			          let individuals = get_lines filename in 
			            liter
			              (fun genome ->
			                let copy = original#copy() in
			                  copy#load_genome_from_string genome;
			                  pop := copy :: !pop
			              ) individuals; !pop
			        with End_of_file -> !pop
			      end
		 */
	}

	/* {b tournament_selection} variant_comparison_function population
    desired_pop_size uses tournament selction to select desired_pop_size
    variants from population using variant_comparison_function to compare
    individuals, if specified, and variant fitness if not.  Returns a subset
    of the population.  */
	private Representation<G> selectOne() {
		Collections.shuffle(population);
		List<Representation<G>> pool = population.subList(0, tournamentK);
		Comparator<Representation<G>> myComp = new Comparator<Representation<G>>() {
			@Override
			public int compare(Representation<G> one, Representation<G> two) { 
				return new Double(two.getFitness()).compareTo(new Double(one.getFitness())); 
			}
		}; // we sort in descending order by fitness
		TreeSet<Representation<G>> sorted = new TreeSet<Representation<G>>(myComp);
		sorted.addAll(pool);
		double step = 0.0;
		for(Representation<G> indiv : sorted) {
			boolean taken = false;
			if(this.tournamentP >= 1.0) {
				taken = true;
			} else {
				double requiredProb = this.tournamentP * Math.pow((1.0 - this.tournamentP), step);
				double random = Configuration.randomizer.nextDouble();
				if(random <= requiredProb) {
					taken = true;
				}
			}
			if(taken) {
				return indiv.copy();	
			} else {
				step += 1.0;
			}
		}
		return population.get(0).copy(); // FIXME: this should never happen, right?
	}
	private ArrayList<Representation<G>> tournamentSelection(int desired) {
		assert(desired >= 0);
		assert(tournamentK >= 1);
		assert(this.tournamentP >= 0.0);
		assert(this.tournamentP <= 1.0) ;
		assert(population.size() >= 0);
		ArrayList<Representation<G>> result = new ArrayList<Representation<G>>();

		//remove the uncompiling ones from the population
		for(int i = 0; i< population.size(); ++i) {
			Representation<G> indiv = population.get(i);
			boolean successfullyCompiled = indiv.compile(indiv.getName(), indiv.getVariantFolder());
			if(!successfullyCompiled){
				//replace that variant with the original
				population.remove(indiv);
				//first element of the population should be the original, which should always compile
				Representation<G> toInsert = population.get(0).copy();
				population.add(toInsert);
			}
		}

		for(int i = 0 ; i < desired; i++) {
			result.add(selectOne());
		}
		return result; 
	}


	public void add (Representation<G> newItem) {
		population.add(newItem);
	}

	/* Crossover is an operation on more than one variant, which is why it
			appears here.  We currently have one-point crossover implemented on
			variants of both stable and variable length, patch_subset_crossover, which
			is something like uniform crossover (but which works on all
			representations now, not just cilRep patch) and "ast_old_behavior", which
			Claire hasn't fixed yet.  The nitty-gritty of how to combine
			representation genomes to accomplish crossover has been mostly moved to
			the representation classes, so this implementation doesn't know much about
			particular genomes.  Crossback implements one-point between variants and
			the original. *)
			(* this implements the old AST/WP crossover behavior, typically intended to be
			used on the patch representation.  I don't like keeping it around, since
			the point of refactoring is to decouple the evolutionary behavior from the
			representation.  I'm still thinking about it *)
			(* this can fail if the edit histories contain unexpected elements, such as
			crossover, or if load_genome_from_string fails (which is likely, since it's
			not implemented across the board, which is why I'm mentioning it in this
			comment) */
	// I have not implemented crossoverPatchOldBehavior


	//  Patch Subset Crossover; works on all representations even though it was
	// originally designed just for cilrep patch
	private ArrayList<Representation<G>> crossoverPatchSubset(Representation<G> original, Representation<G> variant1, Representation<G> variant2) {
		ArrayList<G> g1 = variant1.getGenome();
		ArrayList<G> g2 = variant2.getGenome();
		ArrayList<G> g1g2 = new ArrayList<G>(g1);
		g1g2.addAll(g2);
		ArrayList<G> g2g1 = new ArrayList<G>(g2);
		g2g1.addAll(g1);

		ArrayList<G> newG1 = new ArrayList<G>();
		ArrayList<G> newG2 = new ArrayList<G>();

		for(G ele : g1g2) {
			if(GlobalUtils.probability(crossp)) {
				newG1.add(ele);
			}
		}
		for(G ele : g2g1) {
			if(GlobalUtils.probability(crossp)) {
				newG2.add(ele);
			}
		}
		Representation<G> c1 = original.copy();
		Representation<G> c2 = original.copy();
		c1.setGenome(newG1);
		c2.setGenome(newG2);
		ArrayList<Representation<G>> retval = new ArrayList<Representation<G>>();
		retval.add(c1);
		retval.add(c2);
		return retval;
	}

	private ArrayList<Representation<G>> crossoverOnePoint(Representation<G> original, Representation<G> variant1, Representation<G> variant2) {
		Representation<G> child1 = original.copy();
		Representation<G> child2 = original.copy();
		// in the OCaml, to support the flat crossover on binRep, I had a convoluted thing
		// where you had to query variants to figure out which crossover points were legal
		// as I have no plans to support binary repair in Java at the moment, I'm doing
		// the easy thing here instead.
		// the only trick is if one of the two variants is "original"
		ArrayList<G> g1 = variant1.getGenome();
		ArrayList<G> g2 = variant2.getGenome();

		ArrayList<G> newg1 = new ArrayList<G>();
		ArrayList<G> newg2 = new ArrayList<G>();
		ArrayList<Representation<G>> retval = new ArrayList<Representation<G>>();

		ArrayList<G> firstHalfG1 = new ArrayList<G>();
		ArrayList<G> secondHalfG1 = new ArrayList<G>();
		ArrayList<G> firstHalfG2 = new ArrayList<G>();
		ArrayList<G> secondHalfG2 = new ArrayList<G>();
		int point1 = 0, point2 = 0;

		if(g1.size() > 0) {
			point1 = Configuration.randomizer.nextInt(g1.size());
			firstHalfG1 = new ArrayList<G>(g1.subList(0,point1));
			secondHalfG1 = new ArrayList<G>(g1.subList(point1 + 1, g1.size()));
		} 
		if(g2.size() > 0) {
			if(original.getVariableLength() || g1.size() == 0) {
				point2 =  Configuration.randomizer.nextInt(g2.size());
			} else if(g1.size() > 0) {
				point2 = point1;
			} 
			firstHalfG2 = new ArrayList<G>(g2.subList(0, point2));
			secondHalfG2 = new ArrayList<G>(g2.subList(point2 + 1,  g2.size()));
		}

		newg1.addAll(firstHalfG1);
		newg1.addAll(secondHalfG2);
		newg2.addAll(firstHalfG2);
		newg2.addAll(secondHalfG1); 

		// FIXME: add crossover to history?
		child1.setGenome(newg1);
		child2.setGenome(newg2);

		retval.add(child1);
		retval.add(child2);
		return retval;
	}

	/* do_cross original variant1 variant2 performs crossover on variant1 and
	variant2, producing two children [child1;child2] as a result.  Dispatches
	to the appropriate crossover function based on command-line options *)
	do_cross can fail if given an unexpected crossover option from the command
	line */
	private ArrayList<Representation<G>> doCross(Representation<G> original, Representation<G> variant1, Representation<G> variant2) {
		if(crossover.equals("one") || crossover.equals("onepoint") || crossover.equals("pstch-one-point")) {
			return crossoverOnePoint(original,variant1,variant2);
		}
		if(crossover.equals("back")) {
			return crossoverOnePoint(original, variant1, original);
		}
		if(crossover.equals("uniform")) {
			return crossoverPatchSubset(original,variant1,variant2);
		}
		throw new UnsupportedOperationException("Population: unrecognized crossover: " + crossover);

	}
	/* crossover population original_variant performs crossover over the entire
			population, returning a new population with both the old and the new
			variants */

	public void crossover(Representation<G> original) {
		Collections.shuffle(population,Configuration.randomizer);
		ArrayList<Representation<G>> output = new ArrayList<Representation<G>>(this.population);
		int half = population.size() / 2;
		for(int it = 0 ; it < half; it++) {
			Representation<G> parent1 = population.get(it); //copy?
			Representation<G> parent2 = population.get(it + half); //copy?
			if(GlobalUtils.probability(crossp)) {
				ArrayList<Representation<G>> children = this.doCross(original, parent1, parent2);
				output.addAll(children); // I *think* this is OK, because we include all the parents in output above, so we don't need to add them here
			}
		}
		this.population = output; 
	}

	public Population<G> firstN(int desiredSize) {
		List<Representation<G>> smallerPop = population.subList(0, desiredSize);
		return new Population<G>((ArrayList<Representation<G>>) smallerPop);
	}

	public int size() {
		return population.size();
	}
	@Override
	public Iterator<Representation<G>> iterator() {
		return population.iterator(); 	
	}

	public void selection(int popsize) {
		if(this.multiObjectiveFitness){
			this.population = this.multiObjectiveSelection(popsize);
		}else{
			this.population = this.tournamentSelection(popsize);
		}
	}
	
	
	private ArrayList<Representation<G>> multiObjectiveSelection(int desired) {
		assert(desired >= 0);
		assert(population.size() >= 0);
		ArrayList<Representation<G>> result = new ArrayList<Representation<G>>();
		//remove the uncompiling ones from the population
		for(int i = 0; i< population.size(); ++i) {
			Representation<G> indiv = population.get(i);
			boolean successfullyCompiled = indiv.compile(indiv.getName(), indiv.getVariantFolder());
			if(!successfullyCompiled){
				//replace that variant with the original
				population.remove(indiv);
				//first element of the population should be the original, which should always compile
				Representation<G> toInsert = population.get(0).copy();
				population.add(toInsert);
			}
		}
		for(int i = 0; i< population.size(); ++i) {
			Representation<G> indiv = population.get(i);
			createFoldersAndTS(indiv);
		}
		//for(int i = 0 ; i < desired; i++) {
		//	result.add(getMostDiverse());
		//}
		result.addAll(selectBasedOnMultiObjective(desired));
		//System.out.println("The desired number of variants is "+ desired+ " but we only have "+ result.size()+ " in result set");
		
		return result; 
	}
	
	private void createFoldersAndTS(Representation<G> indiv){
		Runtime rt = Runtime.getRuntime();
		String varLoc = getVarLoc();
		String command = "python /home/mausoto/diversityProject/DiversityGenProg/genprog4java/src/clegoues/genprog4java/fitness/createFoldersAndTS.py "; 
		command += d4jProject() + " ";
		command += d4jBugNum() + " ";
		command += Configuration.seed + " ";
		command += varLoc + "tmp/" + indiv.getVariantFolder()+"/ "; 
		command += "/home/mausoto/defects4jJava8/defects4j/framework/lib/test_generation/generation/evosuite-1.0.6.jar ";
		command += "/home/mausoto/diversityProject/DiversityGenProg/testSuitesForDiversityScore/";
		System.out.println(command);
		String p = GlobalUtils.getOutputFromCommand(command);
		System.out.println(p);
	}
	
	
	
	
	private ArrayList<Representation<G>> selectBasedOnMultiObjective(int desired) {
		//Collections.shuffle(population);
		//List<Representation<G>> pool = population;
		//System.out.println("1population has "+ population.size()+ " elements");
		assert(correctnessContribution < 1);
		assert(diversityContribution < 1);
		//set diversity score and fitness
		for(Representation<G> indiv : population) {
			//System.out.println("Population Name:"+indiv.getName()+ " fitness:"+ indiv.getFitness() + " divScore:"+ indiv.getDiversityScore() + " corrScore:" + indiv.getCorrectnessScore());
			
			//double cS = correctnessScore(indiv);
			double cS = indiv.getCorrectnessScore();
			double dS = diversityScore(indiv);
			//indiv.setCorrectnessScore(cS);
			indiv.setDiversityScore(dS);
			double fitness =  (correctnessContribution * cS) + (diversityContribution * dS);
			
			//System.out.println("correctnessContribution is: "+ correctnessContribution);
			//System.out.println("diversityContribution is: "+ diversityContribution);
			//System.out.println("cS is: "+ cS);
			//System.out.println("dS is: "+ dS);
			System.out.println("fitness is: "+ fitness);
			
			indiv.setFitness(fitness);
		}
		Comparator<Representation<G>> myComp = new Comparator<Representation<G>>() {
			@Override
			public int compare(Representation<G> one, Representation<G> two) { 
				return new Double(two.getFitness()).compareTo(new Double(one.getFitness()));
			}
		}; // we sort in descending order by fitness
		List<Representation<G>> pool = population.subList(0, population.size());
		Collections.sort(pool,myComp);
		
		System.out.println("Pool after sorting");
		for(Representation<G> indiv : pool) {
			System.out.println("Pool Name:"+indiv.getName()+ " fitness:"+ indiv.getFitness() + " divScore:"+ indiv.getDiversityScore() + " corrScore:" + indiv.getCorrectnessScore());
		}

		ArrayList<Representation<G>> toReturn = new ArrayList<Representation<G>>();
		for(Representation<G> indiv : pool) {
			if(toReturn.size() < desired){
				toReturn.add(indiv.copy());
			}
		}
		
		//return population.get(0).copy(); // FIXME: this should never happen, right?
		//System.out.println("toReturn has "+toReturn.size()+" elements before returning");
		return toReturn;
	}
	
	/*private double correctnessScore(Representation<G> indiv){
		//If fitness here is calculated as number of passed test cases, then we are all good here. 
		System.out.println("Fitness.numPositiveTests:"+Fitness.numPositiveTests);
		System.out.println("Fitness.numNegativeTests:"+Fitness.numNegativeTests);
		System.out.println("Fitness.sample:"+Fitness.sample);
		System.out.println("indiv.getFitness():"+indiv.getFitness());
		return indiv.getFitness() / Math.round(Fitness.sample * Fitness.numPositiveTests);
		return indiv.
	}*/
	
	private double diversityScore(Representation<G> variant){
		double overallScore = 0;
		for(Representation<G> indiv : population) {
			if (indiv != variant){
				
				overallScore += (indivDiversityScore(indiv, variant) / population.size());
			}
		}
		System.out.println("Overall diversity score of variant "+ variant.getName() + ": "+overallScore);
		return overallScore;
	}
	
	//How many tests behave differently between representations
	private double indivDiversityScore(Representation<G> indiv, Representation<G> variant){
		
		Runtime rt = Runtime.getRuntime();
		String varLoc = getVarLoc();
		String command = "python /home/mausoto/diversityProject/DiversityGenProg/genprog4java/src/clegoues/genprog4java/fitness/diversityScores.py "; 
		command += d4jProject() + " ";
		command += d4jBugNum() + " ";
		command += Configuration.seed + " ";
		command += varLoc + "tmp/" + indiv.getVariantFolder()+"/ "; 
		command += varLoc + "tmp/" + variant.getVariantFolder()+"/ ";
		command += "/home/mausoto/defects4jJava8/defects4j/framework/lib/test_generation/generation/evosuite-1.0.6.jar ";
		command += "/home/mausoto/diversityProject/DiversityGenProg/testSuitesForDiversityScore/";
		System.out.println("Executed python script to obtain diversity score: "+command);
		
		//System.out.println("Command executed here");
		//Process pr = rt.exec(command); 
		String p = GlobalUtils.getOutputFromCommand(command);
		System.out.println(p);
		double pythonScriptResult = 0;
		//System.out.println("Here it is going to check if 'diversityScore' is in the string");
		if(p.contains("diversityScore")){
			//System.out.println("'diversityScore' was in the string: ");
			//System.out.println(p);
			String dsString = p.substring(p.indexOf("diversityScore:")+15, p.length());
			dsString = dsString.substring(0,dsString.indexOf("\n"));
			//System.out.println("!!!!!!!!!!!!!!!!!!!the int is just: "+ dsString);
			pythonScriptResult = Double.parseDouble(dsString);
		}else{
			System.err.println("diversityScore was not in the string");//+ p + " End of string. \n");
		}

		return pythonScriptResult;
		
		//tsIndiv = indiv.createTS();
		//tsVar = variant.createTS();
		//tsIndivNumTests = numberOfTests(tsIndiv);
		//tsVarNumTests = numberOfTests(tsVar);
		//numTestsFailedByIndiv = indiv.numFailedTestsTS(tsVar);
		//numTestsFailedByVar = variant.numFailedTestsTS(tsIndiv);
		//return (numTestsFailedByIndiv + numTestsFailedByVar) / (tsIndivNumTests + tsVarNumTests);
	}
	
	private String getProjAndNum(){
		String fullPath = GlobalUtils.getOutputFromCommand("pwd");
		String projAndNum = null;
		String[] words = fullPath.split("/");
		for(String s: words){
			if(s.contains("Buggy"))
				projAndNum = s.substring(0,s.indexOf("Buggy"));
		}
		return projAndNum;
	}	
	
	private String getVarLoc(){
		//$D4J_HOME/$BUGSFOLDER/"$LOWERCASEPACKAGE""$BUGNUMBER"Buggy/tmp/
		//System.out.println("executing getVarLoc");
		String fullPath = GlobalUtils.getOutputFromCommand("pwd");
		//System.out.println("full path: "+ fullPath);
		int lastIndex = fullPath.indexOf("Buggy")+5;
		//System.out.println("Lastindex: "+ lastIndex + " full length: "+ fullPath.length());
		String varLoc = fullPath.substring(0,lastIndex)+"/";
		return varLoc;
		
	}

	private String d4jProject(){
		//System.out.println("executing d4jProject");
		String projAndNum = getProjAndNum();
		String toCap = projAndNum.replaceAll("[0-9]","");
		return toCap.substring(0, 1).toUpperCase() + toCap.substring(1);
	}
	private String d4jBugNum(){
		//System.out.println("executing d4jBugNum");
		String projAndNum = getProjAndNum();
		return projAndNum.replaceAll("[A-Za-z]","");
	}
	
	//Returns the number of tests in the test suite indicated in the location
	//private int numberOfTests(String tsLocation){
		
	//}

}
