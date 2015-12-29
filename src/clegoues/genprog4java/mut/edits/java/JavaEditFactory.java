package clegoues.genprog4java.mut.edits.java;

import java.util.HashMap;

import org.apache.log4j.Logger;


import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Location;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.JavaLocation;

public class JavaEditFactory {
	

	protected Logger logger = Logger.getLogger(JavaEditOperation.class);

	public JavaEditOperation makeEdit(Mutation edit, Location dst, HashMap<String,EditHole> sources) {
		switch(edit) {
		case DELETE: 
			return new JavaDeleteOperation((JavaLocation) dst);
		case OFFBYONE:
			return new JavaOffByOneOperation((JavaLocation) dst, sources);
		case APPEND: return new JavaAppendOperation((JavaLocation) dst, sources);
		case REPLACE: return new JavaReplaceOperation((JavaLocation) dst, sources);
		case SWAP: return new JavaSwapOperation((JavaLocation) dst, sources);
		default: logger.fatal("unhandled edit template type in JavaEditFactory; this should be impossible (famous last words...)");
		}		return null;
	}
}
