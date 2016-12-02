package clegoues.genprog4java.treelm;

import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * The symbol-table functionality required to get variable names and types.
 * <p>
 * The {@link #enter(int)} and {@link #leave(int)} methods allow the symbol
 * table to keep track of the current scope. They will be called with the
 * result of {@link ASTNode#getNodeType()} before each node is visited and
 * after all of the nodes children are visited, respectively. This is
 * intended to indicate both the current block and any name qualifications.
 * </p><p>
 * The {@link #getFullyQualifiedTypeName(String)} method is used while
 * extracting a tree (e.g., prior to training a model) to determine the
 * names of types so that the correct type may be retrieved even in
 * randomly generated trees.
 * </p><p>
 * Finally, {@link #allocFreeName(String)} and
 * {@link #getNameForType(String)} are used when converting a tree back to
 * an AST, to retrieve newly declared or existing names, respectively.
 * </p><p>
 * <b>Note that these methods returns a future String (via a Supplier)
 * instead of a bare String.</b> This is because Java allows fields
 * (e.g., in an anonymous class) to be defined after the methods that
 * use them. Instantiating an AST occurs in two passes. In the first, the
 * {@code enter()} and {@code leave()} methods provide context for the 
 * {@code allocFreeName()} and {@code getNameForType}. In the second pass,
 * the Suppliers are invoked to get actual names.
 * 
 * @author jonathan
 */
public interface SymbolTable {
	/**
	 * Called immediately before the tree extractor begins processing a
	 * subtree. The corresponding {@link #leave(int)} will be called after
	 * all descendants have been processed.
	 * 
	 * @param nodeType the type of the root node of the subtree
	 */
	public void enter( int nodeType );
	
	/**
	 * Called immediately after the tree extractor has finished processing
	 * all descendants of a subtree.
	 * 
	 * @param nodeType the type of the root node of the subtree
	 */
	public void leave( int nodeType );
	
	/**
	 * Called when a new name is required to instantiate an AST. The
	 * returned name should be added to the appropriate scope as a possible
	 * result of future calls to {@link #getNameForType(String)}.
	 * 
	 * @param type the type of variable to get a name for
	 * 
	 * @return a name for the variable that does not conflict with existing
	 * in-scope names.
	 */
	public Supplier< String > allocFreeName( String type );
	
	/**
	 * Called when an existing name is required to instantiate an AST.
	 * 
	 * @param type the type of variable to get a name for
	 * 
	 * @return the name of an in-scope variable with the requested type.
	 */
	public Supplier< String > getNameForType( String type );
}

