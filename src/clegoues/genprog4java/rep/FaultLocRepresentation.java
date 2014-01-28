package clegoues.genprog4java.rep;

public abstract class FaultLocRepresentation<G,C> extends CachingRepresentation<G,C> {
	private List<AtomPair> faultLocalization;
	private List<AtomPair> fixLocalization;

			  (***********************************)
			  (* Methods that must be provided by a subclass.  *)
			  (***********************************)

			  (** instruments this variant for fault localization and writes it to disk.
			      Does not compile or run the instrumented variant.

			      @param coverage_source_name filename for the instrumented source code
			      @param coverage_exe_name executable name for when it's compiled
			      @param coverage_data_out_name path file to which to write the coverage
			      information.*)
			  method virtual private instrument_fault_localization : 
			      string -> string -> string -> unit

			  (** used for line-based localization: 

			      @param filename name of source file
			      @param lineno line number on which we are looking for an atom
			      @return atom id associated with/closest lineno in filename *)
			  method virtual private atom_id_of_source_line : string -> int -> atom_id 

			  method internal_copy () : 'self_type = 
			    ({< fault_localization = ref !fault_localization ; 
			        fix_localization = ref !fix_localization ;
			     >})


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
			  (**/**)
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

			  method debug_info () =
			    debug "one\n";
			    let fix_local = self#get_fix_source_atoms() in
			      debug "two\n";
			    let fault_local = self#get_faulty_atoms() in
			      debug "fault path length: %d, fix path length: %d\n" 
			        (llen fault_local) (llen fix_local);
			      debug "fault weight: %g\n" 
			        (lfoldl (fun accum -> fun (_,g) -> accum +. g) 0.0 fault_local);
			      debug "fix weight: %g\n"  
			        (lfoldl (fun accum -> fun (_,g) -> accum +. g) 0.0 fix_local);
			      let fout = open_out "fault_path.weights" in
			        liter 
			          (fun (id,w) -> output_string fout (Printf.sprintf "%d,%g\n" id w)) 
			          fault_local;
			        close_out fout; 
			        let fout = open_out "fix_path.weights" in
			          liter (fun (id,w) -> output_string fout (Printf.sprintf "%d,%g\n" id w)) 
			            fix_local;
			          close_out fout

			  method get_faulty_atoms () = !fault_localization

			  method get_fix_source_atoms () = !fix_localization

			  (* particular representations, such as Cilrep, can override this
			   * method to reduce the fix space *) 
			  method reduce_fix_space () = () 

			  method reduce_search_space split_fun do_uniq =
			    (* there's no reason this can't do something to fix localization as well but
			       for now I'm only implementing the stuff we currently need *)
			    let fault_localization' = 
			      if do_uniq then uniq !fault_localization
			      else !fault_localization 
			    in
			      fault_localization := (lfilt split_fun fault_localization')


			  val mutations = ref []
			  val mutation_cache = hcreate 10 

			  method register_mutations muts =
			    liter
			      (fun (mutation,prob) ->
			        if prob > 0.0 then
			          mutations := (mutation,prob) :: !mutations
			      ) muts 

			  (* Returns the set of tests that visit (cover) the given atoms. To take
			   * advantage of this, use --coverage-per-test. *)
			  method tests_visiting_atoms (atomset : AtomSet.t) : TestSet.t = 
			    if AtomMap.is_empty !per_atom_covering_tests then begin
			      if not !coverage_per_test_warning_printed then begin 
			        debug "rep: WARNING: test_visiting_atoms: no data available\n\ttry using --coverage-per-test and/or --regen-paths\n\tdefaulting to 'all tests'\n" ;
			        coverage_per_test_warning_printed := true ; 
			      end ; 
			      if TestSet.is_empty !set_of_all_tests then begin
			        let answer = ref TestSet.empty in
			        for i = 1 to !pos_tests do
			          answer := TestSet.add (Positive i) !answer ;
			        done ; 
			        for i = 1 to !neg_tests do
			          answer := TestSet.add (Negative i) !answer ;
			        done ;
			        set_of_all_tests := !answer ;
			        !answer 
			      end else begin
			        !set_of_all_tests 
			      end 

			    end else begin
			      AtomSet.fold (fun atom acc ->
			        TestSet.union acc 
			          (try (AtomMap.find atom !per_atom_covering_tests)
			           with _ -> TestSet.empty ) 
			      ) atomset (TestSet.empty) 
			    end  

			  (* Using the information associated with --coverage-per-test, we can
			   * compute this special common case of impact analysis: 
			   * which tests must I run given my edits? *) 
			  method tests_visiting_edited_atoms () : TestSet.t = 
			    let atoms = atoms_visited_by_edit_history (self#get_history ()) in 
			    self#tests_visiting_atoms atoms 

			  (* available_mutations can fail if template_mutations are enabled because
			     Claire has not finished implementing that yet *)
			  method available_mutations mut_id = 
			    ht_find mutation_cache mut_id
			      (fun _ ->
			        lfilt
			          (fun (mutation,prob) ->
			            match mutation with
			              Delete_mut -> true
			            | Append_mut -> 
			              (* CLG FIXME/thought: cache the sources list? *)
			              (WeightSet.cardinal (self#append_sources mut_id)) > 0
			            | Swap_mut ->
			              (WeightSet.cardinal (self#swap_sources mut_id)) > 0
			            | Replace_mut ->
			              (WeightSet.cardinal (self#replace_sources mut_id)) > 0
			            | Template_mut(s) -> (llen (self#template_available_mutations s mut_id)) > 0
			          ) !mutations
			      )

			  (***********************************)
			  (* no templates (subclasses can override) *)
			  (***********************************)
			  val templates = ref false
			  val template_cache = hcreate 10

			  method load_templates template_file = templates := true
			  method template_available_mutations str location_id =  [] 

			  method append_sources x = 
			    lfoldl
			      (fun weightset ->
			        fun (i,w) ->
			          WeightSet.add (i,w) weightset) (WeightSet.empty) !fix_localization

			  method swap_sources x = 
			    lfoldl
			      (fun weightset ->
			        fun (i,w) ->
			          WeightSet.add (i,w) weightset)
			      (WeightSet.empty) (lfilt (fun (i,w) -> i <> x) !fault_localization)

			  method replace_sources x =
			    lfoldl
			      (fun weightset ->
			        fun (i,w) ->
			          WeightSet.add (i,w) weightset)
			      (WeightSet.empty) (lfilt (fun (i,w) -> i <> x) !fix_localization)
			  (**/**)      

			  (***********************************)
			  (* No Subatoms (subclasses can override) *)
			  (***********************************)

			  (** the subatoms functions fail by default, unless a subclass implements
			      them *)

			  method subatoms = false

			  (** @raise Fail("get_subatoms") not supported by default *)
			  method get_subatoms = failwith "get_subatoms" 

			  (** @raise Fail("replace_subatom") if subatoms not supported *)
			  method replace_subatom stmt_id subatom_id atom = begin
			    if not self#subatoms then
			      failwith "replace_subatom" 
			    else begin
			      self#updated () ;
			      self#add_history (Replace_Subatom(stmt_id,subatom_id,atom));
			    end
			  end

			  (** @raise Fail("replace_subatom_with_constant") not supported by default *)
			  method replace_subatom_with_constant = failwith "replace_subatom_with_constant" 

			      
			  (***********************************)
			  (* Compute the fault localization information. *)
			  (***********************************)

			  (** helper function for localization; you probably want to override this. 
			      @param atom_id id we're looking for
			      @return line number on which the id is found *)
			  method private source_line_of_atom_id id = id

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
			  method private get_coverage coverage_sourcename coverage_exename coverage_outname = 
			    let fix_path = Filename.concat (Unix.getcwd()) !fix_path in
			    let fault_path = Filename.concat (Unix.getcwd()) !fault_path in
			    (* Traditional "weighted path" or "set difference" or Reiss-Renieris fault
			     * localization involves finding all of the statements visited while
			     * executing the negative test case(s) and removing/down-weighting
			     * statements visited while executing the positive test case(s).  *)
			    let run_tests test_maker max_test out_path expected =
			      let stmts = 
			        lfoldl
			          (fun stmts test ->
			            debug "\ttest: %d\n" test;
			            let _ = 
			              try Unix.unlink coverage_outname with _ -> ()
			            in
			            let cmd = Printf.sprintf "touch %s\n" coverage_outname in
			            let _ = ignore(Unix.system cmd) in
			            let actual_test = test_maker test in 
			            let res, _ = 
			              self#internal_test_case coverage_exename coverage_sourcename 
			                actual_test
			            in 
			              if res <> expected then begin 
			                if not !allow_coverage_fail then 
			                  abort "Rep: unexpected coverage result on %s\n" 
			                    (test_name actual_test)
			              end ;
			              let stmts' = ref [] in
			              let fin = Pervasives.open_in coverage_outname in 
			              (try
			                 while true do
			                   let line = input_line fin in
			                   let num = my_int_of_string line in 
			                     if not (List.mem num !stmts') then
			                       stmts' := num :: !stmts'
			                 done
			               with End_of_file -> close_in fin);
			              (* If you specify --coverage-per-test, we retain this
			               * information and remember that this particular test
			               * visited this set of atoms. 
			               *
			               * Otherwise, we just union up all of the atoms visited
			               * by all of the positive tests. *) 
			              if !coverage_per_test then begin
			                let visited_atom_set = List.fold_left (fun acc elt ->
			                  AtomSet.add elt acc
			                ) (AtomSet.empty) !stmts' in
			                debug "\t\tcovers %d atoms\n" 
			                  (AtomSet.cardinal visited_atom_set) ;
			                AtomSet.iter (fun atom -> 
			                  let other_tests_visiting_this_atom =
			                    try
			                      AtomMap.find atom !per_atom_covering_tests
			                    with _ -> TestSet.empty
			                  in
			                  per_atom_covering_tests := AtomMap.add atom
			                    (TestSet.add actual_test other_tests_visiting_this_atom)
			                    !per_atom_covering_tests ;
			                ) visited_atom_set ; 
			                per_test_localization := TestMap.add 
			                  actual_test visited_atom_set !per_test_localization ;
			              end ; 
			              uniq (!stmts'@stmts)
			          )  [] (1 -- max_test) 
			      in
			      let fout = open_out out_path in
			        liter
			          (fun stmt ->
			            let str = Printf.sprintf "%d\n" stmt in
			              output_string fout str) stmts;
			        close_out fout; stmts
			    in
			      debug "coverage negative:\n";
			      ignore(run_tests (fun t -> Negative t) !neg_tests fault_path false);
			      debug "coverage positive:\n";
			      ignore(run_tests (fun t -> Positive t) !pos_tests fix_path true) ;
			      if !coverage_per_test then begin
			        let total_tests = ref 0 in 
			        let total_seen = ref 0 in 
			        AtomMap.iter (fun a ts -> 
			          incr total_seen ;
			          total_tests := (TestSet.cardinal ts) + !total_tests ; 
			        (* debugging *) 
			        (* 
			          debug "Atom %4d:" a ;
			          TestSet.iter (fun t -> 
			            debug " %s" (test_name t) 
			          ) ts ;
			          debug "\n" ;
			        *) 
			        ) !per_atom_covering_tests ;
			        debug "coverage: average tests per atom: %g / %d\n" 
			          (float_of_int !total_tests /. float_of_int !total_seen) 
			          (!neg_tests + !pos_tests) 
			          ; 
			      end ; 
			      () 

			  (* now we have a positive path and a negative path *) 


			  (** @raise Fail("load_oracle not supported on this implementation") not
			      supported by default; subclasses can override. *)
			  method private load_oracle (fname : string) : unit = 
			    failwith "load_oracle not supported on this implementation"
			  (* there are a number of ways compute_localization can fail.  Will abort
			  *)
			 
			  (** produces fault and fix localization sets for use by later mutation
			      operators. This is typically done by running the program to find the atom
			      coverage on the positive and negative test cases, but there are other
			      schemes:

			      {ul
			      {- path:     default 'weight path' localization}
			      {- uniform:  all atoms in the program have uniform 1.0 weight}
			      {- line: an external file specifies a list of source-code line numbers;
			      the corresponding atoms are used}
			      {- weight: an external file specifies a weighted list of atoms}
			      {- oracle: for fix localization, an external file specifies source code
			      (e.g., repair templates, human-written repairs) that is used as a source
			      of possible fixes}}

			      There are a number of ways this function can fail.  

			      @raise Fail("general confusion") this function will fail if either the fault or
			      the fix scheme is unrecognized, if the oracle scheme is specified without
			      an [oracle_file], if coverage info must be generated but the result does
			      not compile, or if the scheme is [line] or [weight] and the input file is
			      malformed
			  *)
			  method compute_localization () =
			    debug "faultLocRep: compute_localization: fault_scheme: %s, fix_scheme: %s\n" 
			      !fault_scheme !fix_scheme;
			    
			    (*********************************)
			    (* localization utilities *)
			    (*********************************)
			    
			    let fix_weights_to_lst ht = hfold (fun k v acc -> (k,v) :: acc) ht [] in
			    let uniform lst = 
			      lfoldl 
			        (fun acc atom -> ((self#source_line_of_atom_id atom),1.0) :: acc)
			        [] (1 -- self#max_atom())
			    in
			    (* Default "ICSE'09"-style fault and fix localization from path files.  The
			     * weighted path fault localization is a list of <atom,weight> pairs. The fix
			     * weights are a hash table mapping atom_ids to weights.  *)
			    let compute_localization_from_path_files () = 
			      let fw = Hashtbl.create 10 in
			        liter 
			          (fun (i,_) -> Hashtbl.replace fw i !positive_path_weight) 
			          !fault_localization;
			        let neg_ht = Hashtbl.create 255 in 
			        let pos_ht = Hashtbl.create 255 in 
			          iter_lines !fix_path
			            (fun line ->
			              Hashtbl.replace pos_ht line () ;
			              Hashtbl.replace fw (my_int_of_string line) 0.5);
			          lfoldl
			            (fun (wp,fw) line ->
			              if not (Hashtbl.mem neg_ht line) then
			                begin 
			                  let neg_weight = if Hashtbl.mem pos_ht line 
			                    then !positive_path_weight 
			                    else !negative_path_weight 
			                  in 
			                    Hashtbl.replace neg_ht line () ; 
			                    Hashtbl.replace fw (my_int_of_string line) 0.5 ; 
			                    (my_int_of_string line,neg_weight) :: wp, fw
			                end
			              else wp,fw) ([],fw)
			            (get_lines !fault_path)
			    in

			    (* Process a special user-provided file to obtain a list of <atom,weight>
			     * pairs. The input format is a list of "file,stmtid,weight" tuples. You can
			     * separate with commas and/or whitespace. If you leave off the weight, we
			     * assume 1.0. You can leave off the file as well.  *)
			    let process_line_or_weight_file fname scheme =
			      let regexp = Str.regexp "[ ,\t]" in 
			      let fix_weights = Hashtbl.create 10 in 
			        liter 
			          (fun (i,_) -> Hashtbl.replace fix_weights i !positive_path_weight) 
			          !fix_localization;
			        let fault_localization = ref [] in 
			          liter 
			            (fun line -> 
			              let stmt, weight, file = 
			                match Str.split regexp line with
			                | [stmt] -> my_int_of_string stmt, !negative_path_weight, ""
			                | [stmt ; weight] -> begin
			                  try
			                    my_int_of_string stmt, float_of_string weight, ""
			                  with _ -> my_int_of_string weight,!negative_path_weight,stmt
			                end
			                | [file ; stmt ; weight] -> 
			                  my_int_of_string stmt, float_of_string weight, file
			                | _ -> 
			                  abort ("ERROR: faultLocRep: compute_localization: %s: malformed line:\n%s\n"
			                  ) !fault_file line
			              in 
			              (* In the "line" scheme, the file uses source code line numbers
			               * (rather than atom-ids). In such a case, we must convert them to
			               * atom-ids. *)
			              let stmt = if scheme = "line" then 
			                  self#atom_id_of_source_line file stmt 
			                else stmt
			              in
			                if stmt >= 1 then begin 
			                  Hashtbl.replace fix_weights stmt 0.5; 
			                  fault_localization := (stmt,weight) :: !fault_localization
			                end 
			            ) (get_lines fname);
			          lrev !fault_localization, fix_weights
			    in
			    let set_fault wp = fault_localization := wp in
			    let set_fix lst = fix_localization := lst in

			    let _ = 
			      (* sanity/legality checking on the command line options *)
			      (match !fault_scheme with 
			        "path" | "uniform" | "line" | "weight" -> ()
			      | "default" -> fault_scheme := "path" 
			      | _ -> 
			        abort "faultLocRep: Unrecognized fault localization scheme: %s\n" 
			          !fault_scheme);
			      if !fix_oracle_file <> "" then fix_scheme := "oracle";
			      match !fix_scheme with
			        "path" | "uniform" | "line" | "weight" | "default" -> ()
			      | "oracle" -> assert(!fix_oracle_file <> "" && !fix_file <> "")
			      | _ -> 
			        abort  "faultLocRep: Unrecognized fix localization scheme: %s\n" 
			          !fix_scheme
			    in
			    let _ =
			      (* if we need the path files and they are either missing or we've been
			       * asked to regenerate them, generate them *)
			      match !fault_scheme,!fix_scheme with
			        "path",_  | _,"path"| _,"default" 
			          when !regen_paths ||
			            (not ((Sys.file_exists !fault_path) && (Sys.file_exists !fix_path))) ->
			              let subdir = add_subdir (Some("coverage")) in 
			              let coverage_sourcename = Filename.concat subdir 
			                (coverage_sourcename ^ if (!Global.extension <> "")
			                  then !Global.extension
			                  else "") 
			              in 
			              let coverage_exename = Filename.concat subdir coverage_exename in 
			              let coverage_outname = Filename.concat subdir "coverage.path" in
			                debug "Rep: coverage_sourcename: %s\n" coverage_sourcename;
			                self#instrument_fault_localization 
			                  coverage_sourcename coverage_exename coverage_outname ;
			                if not (self#compile coverage_sourcename coverage_exename) then 
			                  abort "ERROR: faultLocRep: compute_localization: cannot compile %s\n" 
			                    coverage_sourcename ;
			                self#get_coverage coverage_sourcename coverage_exename coverage_outname
			      | _,_ -> ()
			    in
			      (* that setup all aside, actually compute the localization *)
			      if !fault_scheme = "path" || !fix_scheme = "path" || !fix_scheme =
			        "default" then begin
			          let wp, fw = compute_localization_from_path_files () in
			            if !fault_scheme = "path" then set_fault (lrev wp);
			            if !fix_scheme = "path" || !fix_scheme = "default" then 
			              set_fix (fix_weights_to_lst fw)
			        end; (* end of: "path" fault or fix *) 
			      
			      (* Handle "uniform" fault or fix localization *) 
			      if !fault_scheme = "uniform" then set_fault (uniform ());
			      if !fix_scheme = "uniform" then set_fix (uniform ());

			      (* Handle "line" or "weight" fault localization *) 
			      if !fault_scheme = "line" || !fault_scheme = "weight" then begin
			        let wp,fw = process_line_or_weight_file !fault_file !fault_scheme in 
			          set_fault wp;
			          if !fix_scheme = "default" then 
			            set_fix (fix_weights_to_lst fw)
			      end;
			      
			      (* Handle "line" or "weight" fix localization *) 
			      if !fix_scheme = "line" || !fix_scheme = "weight" then 
			        set_fix (fst (process_line_or_weight_file !fix_file !fix_scheme))
			          
			      (* Handle "oracle" fix localization *) 
			      else if !fix_scheme = "oracle" then begin
			        self#load_oracle !fix_oracle_file;
			        set_fix (fst (process_line_or_weight_file !fix_file "line"));
			      end;


			      (* print debug/converage info if specified *)
			      if !coverage_info <> "" then begin
			        let pos_stmts = lmap fst !fix_localization in 
			        let perc = 
			          (float_of_int (llen pos_stmts)) /. (float_of_int (self#max_atom())) 
			        in
			          debug "COVERAGE: %d unique stmts visited by pos test suite (%d/%d: %g%%)\n"
			            (llen pos_stmts) (llen pos_stmts) (self#max_atom()) perc;
			          let fout = open_out !coverage_info in 
			            liter
			              (fun stmt ->
			                let str = Printf.sprintf "%d\n" stmt in
			                  output_string fout str) pos_stmts;
			            liter
			              (fun stmt ->
			                let str = Printf.sprintf "%d\n" stmt in
			                  output_string fout str) pos_stmts;
			            close_out fout
			      end
			end 

}
