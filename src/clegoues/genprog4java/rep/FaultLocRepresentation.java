package clegoues.genprog4java.rep;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.TreeSet;

import clegoues.genprog4java.Fitness.Fitness;
import clegoues.genprog4java.Fitness.TestCase;
import clegoues.genprog4java.Fitness.TestType;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.mut.EditOperation;
import clegoues.genprog4java.mut.HistoryEle;
import clegoues.genprog4java.mut.JavaEditOperation;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.util.Pair;


public abstract class FaultLocRepresentation<G extends EditOperation> extends CachingRepresentation<G> {
	// FIXME: making these static is lazy of me, but whatever, I'm tired of this clone/copy debacle
	protected static ArrayList<WeightedAtom> faultLocalization = new ArrayList<WeightedAtom>();  
	protected static ArrayList<WeightedAtom> fixLocalization = new ArrayList<WeightedAtom>();

	private static double positivePathWeight = 0.1; 
	private static double negativePathWeight = 1.0;
	private static boolean allowCoverageFail = false;
	private static String posCoverageFile = "coverage.path.pos";
	private static String negCoverageFile = "coverage.path.neg";
	private static boolean regenPaths = false;
	protected boolean doingCoverage = false;

	public FaultLocRepresentation(ArrayList<HistoryEle> history,
			ArrayList<JavaEditOperation> genome2) {
		super(history,genome2);
	}

	public FaultLocRepresentation() {
		super();
	}

	public static void configure(Properties prop) {
		if(prop.getProperty("positivePathWeight") != null) {
			positivePathWeight = Double.parseDouble(prop.getProperty("positivePathWeight").trim());
		}
		if(prop.getProperty("negativePathWeight") != null) {
			negativePathWeight = Double.parseDouble(prop.getProperty("negativePathWeight").trim());
		}
		if(prop.getProperty("allowCoverageFail") != null) {
			allowCoverageFail = true;
		}
		
		if(prop.getProperty("posCoverageFile") != null)
		{
			posCoverageFile = prop.getProperty("posCoverageFile").trim();
		}
		
		if(prop.getProperty("negCoverageFile") != null)
		{
			negCoverageFile = prop.getProperty("negCoverageFile").trim();
		}
		if(prop.getProperty("regenPaths") != null) {
			regenPaths = true;
		}
		
	}
	

	/*

			  (** [deserialize] can fail if the version saved in the binary file does not
			      match the current [faultLocRep_version].  As it can call
			      [compute_localization], it may also abort there *)
			  method deserialize ?in_channel ?global_info (filename : string) = 
			    let fin = 
			      match in_channel with
			      | Some(v) -> v
			      | None -> assert(false); 
			    in 
			    let version = Marshal.from_channel fin in
			      if version <> faultlocRep_version then begin
			        debug "faultlocRep: %s has old version\n" filename ;
			        failwith "version mismatch" 
			      end ;
			      fault_localization := Marshal.from_channel fin ; 
			      fix_localization := Marshal.from_channel fin ; 
			      per_test_localization := Marshal.from_channel fin ; 
			      per_atom_covering_tests := Marshal.from_channel fin ; 

			      let gval = match global_info with Some(n) -> n | _ -> false in
			        if gval then begin
			          (* CLG isn't sure if this is quite right *)
			          let fault_scheme' = Marshal.from_channel fin in
			          let fix_scheme' = Marshal.from_channel fin in
			          let negative_path_weight' = Marshal.from_channel fin in
			          let positive_path_weight' = Marshal.from_channel fin in
			            if fault_scheme' <> !fault_scheme ||
			              fix_scheme' <> !fix_scheme ||
			              negative_path_weight' <> !negative_path_weight ||
			              positive_path_weight' <> !positive_path_weight ||
			              !regen_paths then
			              self#compute_localization()
			        end;
			        super#deserialize ?in_channel:(Some(fin)) ?global_info:global_info filename ; 
			        debug "faultlocRep: %s: loaded\n" filename ; 
			        if in_channel = None then close_in fin 

			  (***********************************)
			  (* Concrete methods implementing the interface *)
			  (***********************************)

			  method serialize ?out_channel ?global_info (filename : string) =
			    let fout = 
			      match out_channel with
			      | Some(v) -> v
			      | None -> assert(false); 
			    in 
			      Marshal.to_channel fout (faultlocRep_version) [] ; 
			      Marshal.to_channel fout (!fault_localization) [] ;
			      Marshal.to_channel fout (!fix_localization) [] ;
			      Marshal.to_channel fout (!per_test_localization) [] ;
			      Marshal.to_channel fout (!per_atom_covering_tests) [] ;
			      let gval = match global_info with Some(n) -> n | _ -> false in 
			        if gval then begin
			          Marshal.to_channel fout !fault_scheme [] ; 
			          Marshal.to_channel fout !fix_scheme [] ; 
			          Marshal.to_channel fout !negative_path_weight [] ; 
			          Marshal.to_channel fout !positive_path_weight [] ; 
			        end;
			        super#serialize ~out_channel:fout filename ;
			        debug "faultlocRep: %s: saved\n" filename ; 
			        if out_channel = None then close_out fout 
	 */
	public ArrayList<WeightedAtom> getFaultyAtoms () {
		return FaultLocRepresentation.faultLocalization; 
	}

	public ArrayList<WeightedAtom> getFixSourceAtoms() {
		return FaultLocRepresentation.fixLocalization;
	}


	/*

			  method reduce_search_space split_fun do_uniq =
			    (* there's no reason this can't do something to fix localization as well but
			       for now I'm only implementing the stuff we currently need *)
			    let fault_localization' = 
			      if do_uniq then uniq !fault_localization
			      else !fault_localization 
			    in
			      fault_localization := (lfilt split_fun fault_localization')
	 */

	public TreeSet<Pair<Mutation, Double>> availableMutations(int atomId) {
		TreeSet<Pair<Mutation,Double>> retVal = new TreeSet<Pair<Mutation,Double>>();
		for(Pair<Mutation,Double> mutation : Representation.mutations){
			boolean addToSet = false;
			switch(mutation.getFirst()) {
			case DELETE: addToSet = true; 
			break;
			case APPEND:
				addToSet = this.appendSources(atomId).size() > 0;
				break;
			case REPLACE:
				addToSet = this.replaceSources(atomId).size() > 0;
				break;
			case SWAP:
				addToSet = this.swapSources(atomId).size() > 0;
				break;
			}
			if(addToSet) {
				retVal.add(mutation);
			}
		}
		return retVal;
	}

	// you probably want to override these for semantic legality check
	public TreeSet<WeightedAtom> appendSources(int stmtId) {
		TreeSet<WeightedAtom> retVal = new TreeSet<WeightedAtom>();
		for(WeightedAtom item : FaultLocRepresentation.fixLocalization) {
			retVal.add(item);
		}
		return retVal;
	}
	public TreeSet<WeightedAtom> swapSources(int stmtId) {
		TreeSet<WeightedAtom> retVal = new TreeSet<WeightedAtom>();
		for(WeightedAtom item : FaultLocRepresentation.fixLocalization) {
			retVal.add(item);
		}
		return retVal;
	}
	public TreeSet<WeightedAtom> replaceSources(int stmtId) {
		TreeSet<WeightedAtom> retVal = new TreeSet<WeightedAtom>();
		for(WeightedAtom item : FaultLocRepresentation.fixLocalization) {
			retVal.add(item);
		}
		return retVal;
	}


	/*

			  (** run the instrumented code to attain coverage information.  Writes the
			      generated paths to disk (the fault and fix path files respectively) but
			      does not otherwise return.

			      If the calls to [Unix.unlink] fail, they will do so silently.

			      @param coverage_sourcename instrumented source code on disk
			      @param coverage_exename compiled executable
			      @param coverage_outname on disk path file name 
			      @raise Fail("abort") if variant produces produces unexpected behavior on
			      either positive or negative test cases and [--allow-coverage-fail] is not on.
			     get_coverage will abort if allow_coverage_fail is not toggled and the variant
	 *)
	 *
	 *Traditional "weighted path" or "set difference" or Reiss-Renieris fault
	 * localization involves finding all of the statements visited while
	 * executing the negative test case(s) and removing/down-weighting
	 * statements visited while executing the positive test case(s). 
	 **/
	private void cleanCoverage()
	{
		File coverageRaw = new File("jacoco.exec"); // FIXME: likely/possibly a mistake to put this in this class 

		if(coverageRaw.exists())
		{
			coverageRaw.delete();
		}
	}

	protected abstract ArrayList<Integer> atomIDofSourceLine(int lineno);

	private TreeSet<Integer> runTestsCoverage(String pathFile, TestType testT, ArrayList<String> tests, boolean expectedResult, String wd) throws IOException, UnexpectedCoverageResultException {
		TreeSet<Integer> atoms = new TreeSet<Integer>();
		for(String test : tests)  {
			this.cleanCoverage();
			TestCase newTest = new TestCase(testT, test);


			if(this.testCase(newTest) != expectedResult && !FaultLocRepresentation.allowCoverageFail) {
				throw new UnexpectedCoverageResultException("FaultLocRep: unexpected coverage result: " + newTest.toString());
			}
			TreeSet<Integer> thisTestResult = this.getCoverageInfo();
			atoms.addAll(thisTestResult);
		}

		BufferedWriter out = new BufferedWriter(new FileWriter(new File(pathFile)));

		for(int atom : atoms)
		{
			out.write(""+atom+"\n");
		}

		out.flush();
		out.close();

		return atoms;
	}

	protected abstract TreeSet<Integer> getCoverageInfo() throws FileNotFoundException, IOException;

	private TreeSet<Integer> readPathFile(String pathFile) {
		System.out.println("reading from " + pathFile);
		TreeSet<Integer> retVal = new TreeSet<Integer>();
		Scanner reader = null;
		try {
			reader = new Scanner(new FileInputStream(pathFile));
			while(reader.hasNextInt()) {
				int i = reader.nextInt();
				retVal.add(i);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			System.err.println("coverage file " + pathFile + " not found");
			e.printStackTrace();
		} finally {
			if(reader != null) reader.close();
			return retVal;
		}


	}
	@Override

	public void computeLocalization() throws IOException, UnexpectedCoverageResultException {
		// FIXME: THIS ONLY DOES STANDARD PATH FILE localization
		/*
		* Default "ICSE'09"-style fault and fix localization from path files.  The
		 * weighted path fault localization is a list of <atom,weight> pairs. The fix
		 * weights are a hash table mapping atom_ids to weights.
		 */
		this.doingCoverage = true;
		TreeSet<Integer> positivePath = null;
		TreeSet<Integer> negativePath = null;
		File positivePathFile = new File(FaultLocRepresentation.posCoverageFile);
		// OK, we don't instrument Java programs, rather, use java library that computes coverage for us.
		// which means either instrumentFaultLocalization should still exist and change the commands used for test case execution
		// or we don't pretend this is trying to match OCaml exactly?
		this.instrumentForFaultLocalization();
		File covDir = new File("coverage/");
		if(!covDir.exists())
			covDir.mkdir();
		if(!this.compile("coverage", "coverage/coverage.out")) {
			System.err.println("faultLocRep: Coverage failed to compile");
			throw new UnexpectedCoverageResultException("compilation failure");
		}
		if(positivePathFile.exists() && !FaultLocRepresentation.regenPaths) {
			positivePath = readPathFile(FaultLocRepresentation.posCoverageFile);
		} else {
			positivePath = runTestsCoverage(FaultLocRepresentation.posCoverageFile, TestType.POSITIVE, Fitness.positiveTests, true, "coverage/"); 
		}
		File negativePathFile = new File(FaultLocRepresentation.negCoverageFile);

		if(negativePathFile.exists() && !FaultLocRepresentation.regenPaths) {
			negativePath = readPathFile(FaultLocRepresentation.negCoverageFile);

		} else {
			negativePath = runTestsCoverage(FaultLocRepresentation.negCoverageFile, TestType.NEGATIVE, Fitness.negativeTests, false, "coverage/");
		}
		HashMap<Integer,Double> fw = new HashMap<Integer,Double>(); 
		TreeSet<Integer> negHt = new TreeSet<Integer>();
		TreeSet<Integer> posHt = new TreeSet<Integer> ();

		for(Integer i : positivePath) {// FIXME: this is negative path in the OCaml code and I think that may be wrong. 
			fw.put(i,FaultLocRepresentation.positivePathWeight);
		}
		for(Integer i : positivePath) {
			posHt.add(i);
			fw.put(i, 0.5);
		}
		for(Integer i : negativePath) {
			if(!negHt.contains(i)) {
				double negWeight = FaultLocRepresentation.negativePathWeight;
				if(posHt.contains(i)) {
					negWeight = FaultLocRepresentation.positivePathWeight;
				}
				negHt.add(i);
				fw.put(i,  0.5);
				faultLocalization.add(new WeightedAtom(i,negWeight));
			}
		} 					for(Map.Entry<Integer,Double> entry : fw.entrySet()) {
			Integer key = entry.getKey();
			Double value = entry.getValue();
			fixLocalization.add(new WeightedAtom(key,value));
		}
		assert(faultLocalization.size() > 0);
		assert(fixLocalization.size() > 0);
		this.doingCoverage = false;
	}

	protected abstract void instrumentForFaultLocalization();

	@Override
	public void load(String fname) throws IOException {
		super.load(fname); // calling super so that the code is loaded and the sanity check happens before localization is computed
		try {
		this.computeLocalization();
		} catch (UnexpectedCoverageResultException e) {
			System.err.println("FaultLocRep: UnexpectedCoverageResult");
			Runtime.getRuntime().exit(1);
		}
	}


}
