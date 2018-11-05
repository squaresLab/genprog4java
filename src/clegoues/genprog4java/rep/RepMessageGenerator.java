package clegoues.genprog4java.rep;

import clegoues.genprog4java.mut.EditOperation;

public interface RepMessageGenerator<G extends EditOperation> {
	String getMessage(Representation<G> rep);
}
