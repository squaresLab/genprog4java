package clegoues.genprog4java.rep;
import java.util.Calendar;
import java.util.List;

import clegoues.genprog4java.Fitness.TestCase;
import clegoues.genprog4java.Fitness.TestType;
import clegoues.genprog4java.util.Pair;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.main.Main;

public abstract class CachingRepresentation<G,C> implements Representation<G, C> {
	
	private double fitness; // in repair, this is a hashtable, for when we have multiple fitnesses.  For now let's KISS	
	/*  (** cached file contents from [internal_compute_source_buffers]; avoid
      recomputing/reserializing *)
  val already_source_buffers = ref None */


	private List<String> alreadySourced; // initialize to empty
	  // TODO: private List<Digest> alreadyDigest; // Digest.t in OCaml
	  private String alreadyCompiled; // initialized to ref None in ocaml
	  private List<History<C>> history;

	  // unlike in the OCaml representation, this shouldn't return None for the first element if
	  // it's a single-file representation b/c that's stupid.
	  protected abstract List<Pair<String,String>>internalComputeSourceBuffers();

	  public boolean getVariableLength() { return true; }

	  public void loadGenomeFromString(String genome) {
		throw new UnsupportedOperationException();
	  }

	public void noteSuccess() { } // default does nothing.  OCaml version takes the original representation here.  Probably should do same 
// OCaml has a copy method here.  Do we need?
	

	public void load(String base) { 
		throw new UnsupportedOperationException();
/*
			    let cache_file = if !rep_cache_file = "" then (base^".cache") else !rep_cache_file in
			    let success = 
			      try 
			        if !no_rep_cache then begin
			          if !sanity = "default" then sanity := "yes";
			          false 
			        end else begin
			          self#deserialize ?in_channel:None ~global_info:true cache_file; 
			          if !sanity = "default" then sanity := "no";
			          true
			        end
			      with e -> 
			        begin
			          debug "Exception in loading: %s\n" (Printexc.to_string e);
			          (if !sanity = "default" then sanity := "yes"); false 
			        end
			    in
			      if not success then begin
			        self#from_source !program_to_repair;
			      end;
			      if !sanity = "yes" then 
			        Stats2.time "sanity_check" self#sanity_check () ; 
			      if (not success) || !regen_paths then begin
			        self#compute_localization ();
			      end;
			      self#serialize ~global_info:true cache_file
			  end

			  method serialize ?out_channel ?global_info (filename : string) = 
			    let fout = 
			      match out_channel with
			      | Some(v) -> v
			      | None -> open_out_bin filename 
			    in 
			      Marshal.to_channel fout (cachingRep_version) [] ; 
			      debug "cachingRep: %s: saved\n" filename ; 
			      if out_channel = None then close_out fout 
			 */
	}
	
	public boolean deserialize(String filename) {
		throw new UnsupportedOperationException();
	}
// did not implement the version thing
/*
			  (** @raise Fail("version mismatch") if the version of the binary being read
			      does not match the current [cachingRep_version]  *)
			  method deserialize ?in_channel ?global_info (filename : string) = begin
			    let fin = 
			      match in_channel with
			      | Some(v) -> v
			      | None -> open_in_bin filename 
			    in 
			    let version = Marshal.from_channel fin in
			      if version <> cachingRep_version then begin
			        debug "cachingRep: %s has old version\n" filename ;
			        failwith "version mismatch" 
			      end ;
			      debug "cachingRep: %s: loaded\n" filename ; 
			      if in_channel = None then close_in fin ;
			  end 
*/

	public boolean sanityCheck() throws SanityCheckException {
		Calendar startTime = Calendar.getInstance();
		// TODO: createSubDirectory("sanity");
		String sanityFilename = "sanity/" + Main.config.getSanityFilename() + Main.config.getGlobalExtension();
		String sanityExename = "sanity/" + Main.config.getSanityExename();
		this.outputSource(sanityFilename);
		if(!this.compile(sanityFilename,sanityExename))
		{
			throw new SanityCheckException("sanity: " + sanityFilename + " does not compile.");
		}
		for(int i = 1; i <= Main.config.getNumNegativeTests(); i++) {
			TestCase thisTest = new TestCase(TestType.POSITIVE, i);
			if(!this.internalTestCase(sanityExename,sanityFilename, thisTest)) {
				throw new SanityCheckException("sanity: " + sanityFilename + " failed " + thisTest.toString());
			}
		}
		for(int i = 1; i <= Main.config.getNumNegativeTests(); i++) {
			TestCase thisTest = new TestCase(TestType.NEGATIVE, i);
			if(this.internalTestCase(sanityExename,sanityFilename, thisTest)) {
				throw new SanityCheckException("sanity: " + sanityFilename + " passed " + thisTest.toString());
			}
		}
		Calendar endTime = Calendar.getInstance();
		// TODO: printout endTime - startTime.
		this.cleanup();
		this.updated();
		return true;
// debug "cachingRepresentation: sanity checking passed (time_taken = %g)\n" (time_now -. time_start) ; 
}

	// FIXME: OK, in OCaml there's an outputSource declaration here that assumes that 
	// the way we output code is to compute the source buffers AS STRINGS and then print out one per file.
	// it's possible this is the same in Java, but unlikely, so I'm going to not implement this here yet
	// and figure out how java files are manipulated first
	// it would be nice if this, as the caching representation superclass, cached the "already sourced" info somehow, as with compile below
	/*
	void outputSource(String filename) {
		List<Pair<String,String>> sourceBuffers = this.computeSourceBuffers();
		for(Pair<String,String> element : sourceBuffers) {
			String sourcename = element.getFirst();
			String outBuffer = element.getSecond;
	 	// output to disk.
	 	 }
	 	// alreadySourced := Some(lmap (fun (sname,_) -> sname) many_files);
		}*/

			protected abstract boolean internalTestCase(String sanityExename, String sanityFilename, TestCase thisTest);
		// TODO Auto-generated method stub

			public List<String> sourceName() { return this.alreadySourced; }



			public void cleanup() {
				throw new UnsupportedOperationException();
				// TODO: remove source code from disk
				// TODO: remove compiled binary from disk
				// TODO: remove applicable subdirectories from disk
			}
		
			void setFitness(double fitness) {
				this.fitness = fitness; // not using the hashtable thing because only one fitness measure for now
			}

			// TODO: OK, as above, I think compiling Java programs is different from our 
			// usual MO.  So while the OCaml implementation does compile in CachingRepresentation
			// assuming that it's always a call to an external script, I'm leaving that off from here for the 
			// time being.  Remember to save already_compiled when applicable.  Perhaps do that here
			// as the superclass thing?
			
			public boolean compile(String sourceName, String exeName) {
				// assuming that the subclass has done something (see above); here we just
				// cache
				this.alreadyCompiled = exeName;
				return true;
			}


			public boolean testCase(TestCase test) {
				/* I need to figure out digests before I can do this
			      let tpr = self#prepare_for_test_case test in
			      let digest_list, result = 
			        match tpr with
			        | Must_Run_Test(digest_list,exe_name,source_name,test) -> 
			          let result = self#internal_test_case exe_name source_name test in
			            digest_list, result 
			        | Have_Test_Result(digest_list,result) -> 
			          digest_list, result 
			      in 
			        test_cache_add digest_list (test,!test_condition) result ;
			        Hashtbl.replace tested (digest_list,(test,!test_condition)) () ;
			        result 
			    end 	*/
				throw new UnsupportedOperationException();
			}



			    // ommitting test_cases.  I think PAR does test cases in parallel, but 
			 // that seems needlessly complicated for a first hack.

			public List<History<C>> getHistory() { return this.history; }
	
			public void addHistory(History<C> edit) {
				this.history.add(edit);
			}

		 // historyElement to string was originally in cachingRepresentation for some totally unknown reason; moved that 
		 // to the History.toString method in the appropriate class.
					  
			public String name() {
				if(history.size() == 0) {
					return "original";
				}
				String name = "";
				for(History<C> edit : this.history) {
					name = name + " " + edit.toString();
				}
				return name;
			}
// TODO: ignoring available crossover points for now
// TODO: OK delete assumes the code type is stmtId; make this type system play nice at some point
			public void delete(C stmtId) {
				this.updated();
				History<C> delete = new History(stmtId, Mutation.DELETE);
				this.addHistory(delete);
			}

			public void append(C appendAfter, C appendWhat) {
				this.updated();
				History<C> append = new History(appendAfter, appendWhat, Mutation.APPEND);
				this.addHistory(append);
			}

			public void swap(C swapWhere, C swapWhat) {
				this.updated();
				History<C> swap = new History(swapWhere, swapWhat, Mutation.SWAP);
				this.addHistory(swap);
			}

			public void replace(C repWhere, C repWhat) {
				this.updated();
				History<C> replace = new History(repWhere, repWhat, Mutation.REPLACE);
				this.addHistory(replace);
			}


// TODO:			  method hash () = Hashtbl.hash (self#get_history ()) 

	// TODO: many internal methods for getting commands that are used in compiling and testing, which may
			// or may not be necessary for Java, we'll see

			// TODO: includes compute_source_bufgfers, compute_digest, internal_test_case_command,
			// internal_test_case_postprocess, internal_test_case, prepare_for_test_case

			void updated() {
				/*
				  (** indicates that cached information based on our AST structure is no longer
					      valid *)
					  method private updated () = 
					    hclear fitness ;
					    already_compiled := None ;
					    already_source_buffers := None ; 
					    already_digest := None ; 
					    already_sourced := None ; */
				throw new UnsupportedOperationException();
			}


}
