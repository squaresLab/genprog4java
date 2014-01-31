package clegoues.genprog4java.rep;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import clegoues.genprog4java.Fitness.FitnessValue;
import clegoues.genprog4java.Fitness.TestCase;
import clegoues.genprog4java.Fitness.TestType;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.mut.EditOperation;
import clegoues.genprog4java.util.Pair;

public abstract class CachingRepresentation<G extends EditOperation> extends Representation<G> {

	private HashMap<String,FitnessValue> fitness = new HashMap<String,FitnessValue>(); // in repair, this is a hashtable mapping fitness keys to values, for
	// multi-parameter searches.  Here, for java, I'm mapping test class names to values, but you can do what you like
	// (including the original behavior)
	
	/*  (** cached file contents from [internal_compute_source_buffers]; avoid
      recomputing/reserializing *)
  val already_source_buffers = ref None */

	@Override
	
	public HashMap<String,FitnessValue> getFitness() { return this.fitness; }
	private ArrayList<String> alreadySourced = new ArrayList<String>(); // initialize to empty
	// TODO: private List<Digest> alreadyDigest; // Digest.t in OCaml
	private Pair<Boolean,String> alreadyCompiled = null; 

	public CachingRepresentation<G> clone() throws CloneNotSupportedException {
		CachingRepresentation<G> clone = (CachingRepresentation<G>) super.clone();
		clone.updated();
		return clone;
	}

	public boolean getVariableLength() { return true; }
	
	public void noteSuccess() { } // default does nothing.  OCaml version takes the original representation here.  Probably should do same 


	public void load(String base) throws IOException { 
		// FIXME: try deserialize first
		this.fromSource(base); 
		if(Configuration.doSanity){
			try {
				assert(this.sanityCheck());
			} catch(SanityCheckException e) {
				System.err.println("cachingRep: Sanity check failed");
				Runtime.getRuntime().exit(1);
			}
			// FIXME: serialize
		}
		/*

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
		// TODO: createSubDirectory("sanity");
		String sanityFilename = "sanity/" + Configuration.sanityFilename + Configuration.globalExtension; 
		String sanityExename = "sanity/" + Configuration.sanityExename;
		this.outputSource(sanityFilename);
		if(!this.compile(sanityFilename,sanityExename))
		{
			throw new SanityCheckException("sanity: " + sanityFilename + " does not compile.");
		}
		for(int i = 1; i <= Configuration.numPositiveTests; i++) {
			TestCase thisTest = new TestCase(TestType.POSITIVE, i);
			if(!this.internalTestCase(sanityExename,sanityFilename, thisTest)) {
				throw new SanityCheckException("sanity: " + sanityFilename + " failed " + thisTest.toString());
			}
		}
		for(int i = 1; i <= Configuration.numNegativeTests; i++) {
			TestCase thisTest = new TestCase(TestType.NEGATIVE, i);
			if(this.internalTestCase(sanityExename,sanityFilename, thisTest)) {
				throw new SanityCheckException("sanity: " + sanityFilename + " passed " + thisTest.toString());
			}
		}
		// TODO: printout endTime - startTime.
		this.cleanup();
		this.updated();
		return true;
		// debug "cachingRepresentation: sanity checking passed (time_taken = %g)\n" (time_now -. time_start) ; 
	}

	// compile assumes that the source has already been serialized to disk.
	// FIXME: add compile to do the generic thing it does in the OCaml, but
	// I think for here, it's best to put it down in Java representation

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

	protected abstract Iterable<?> computeSourceBuffers();

	protected abstract boolean internalTestCase(String sanityExename, String sanityFilename, TestCase thisTest);

	@Override
	public ArrayList<String> sourceName() { return this.alreadySourced; }



	public void cleanup() {
		throw new UnsupportedOperationException();
		// TODO: remove source code from disk
		// TODO: remove compiled binary from disk
		// TODO: remove applicable subdirectories from disk
	}

	@Override
	public void setFitness(String key, FitnessValue fitness) {
		this.fitness.put(key, fitness);  
	}

	// TODO: OK, as above, I think compiling Java programs is different from our 
	// usual MO.  So while the OCaml implementation does compile in CachingRepresentation
	// assuming that it's always a call to an external script, I'm leaving that off from here for the 
	// time being.  Remember to save already_compiled when applicable.  Perhaps do that here
	// as the superclass thing?

	public boolean compile(String sourceName, String exeName) {
		// assuming that the subclass has done something (see above); here we just
		// cache
		boolean result = this.internalCompile(sourceName,exeName);
		this.alreadyCompiled = new Pair<Boolean,String>(result,exeName);
		return result;
	}


	protected abstract boolean internalCompile(String sourceName, String exeName);

	/*public boolean testCase(TestCase test) { // FIXME: add caching to testing
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
			    end 	*//*
		throw new UnsupportedOperationException();
	}*/

	// FIXME: unique name thing, I guess we'll deal with that in the subclasses?

	// TODO: ignoring available crossover points for now

	// TODO:			  method hash () = Hashtbl.hash (self#get_history ()) 

	// TODO: many internal methods for getting commands that are used in compiling and testing, which may
	// or may not be necessary for Java, we'll see
/* indicates that cached information based on our AST structure is no longer valid*/
	void updated() {
		/*
					  method private updated () = 
					    already_source_buffers := None ; 
					    already_digest := None ; 
					  */
		alreadySourced = new ArrayList<String>();
		alreadyCompiled = null;
		fitness = new HashMap<String,FitnessValue>();
	}

	public void reduceSearchSpace() {
	} // subclasses can override as desired

}
