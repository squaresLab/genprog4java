package clegoues.genprog4java.treelm;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * Central location for design decisions regarding how to process a corpus of
 * java files for a tree substitution grammar. By implementing these decisions
 * here, we can more easily change them later without breaking unexpected
 * methods elsewhere.
 * <p>
 * Currently, we operate on the top-level method declarations of top-level
 * classes. Thus, our grammar cannot represent import statements, top-level
 * classes, or static declarations inside top-level classes. However, it may
 * represent local classes.
 * 
 * @author jonathan
 */
public class GrammarUtils {
	/**
	 * Returns the list of subtrees of a CompilationUnit that are described by
	 * the tree substitution grammar. For example, we may want a grammar for
	 * methods, but not import statements.
	 * 
	 * @param root the compilation unit to retrieve subtrees from
	 * 
	 * @return the list of subtrees represented by the tree substitution grammar
	 */
	public static List< ASTNode > getForest( CompilationUnit root ) {
		List< ASTNode > methods = new ArrayList<>();
		root.accept( new ASTVisitor() {
			@Override
			public boolean visit( MethodDeclaration node ) {
				methods.add( node );
				return false;
			}
		} );
		return methods;
	}
	
	/**
	 * Returns the ancestor of the given node that corresponds to the start
	 * production of the grammar. Currently, finds the top-level method
	 * declaration in the top-level class that contains the given node.
	 * 
	 * @param node the node to find the start production ancestor for
	 * 
	 * @return the ancestor of the given node corresponding to the grammar start
	 * production
	 */
	public static ASTNode getStartNode( ASTNode node ) {
		List< ASTNode > pathToRoot = new ArrayList<>();
		pathToRoot.add( node );
		while ( node.getParent() != null ) {
			node = node.getParent();
			pathToRoot.add( node );
		}
		for ( int i = pathToRoot.size() - 1; i >= 0; --i ) {
			if ( pathToRoot.get( i ).getNodeType() == ASTNode.METHOD_DECLARATION )
				return pathToRoot.get( i );
		}
		return null;
	}
	
	/**
	 * Pre-processes an AST to prepare it to be passed to the tree substitution
	 * grammar.
	 * 
	 * @param node the node to prepare
	 */
	public static void prepareAST( ASTNode node ) {
		node.accept( new ASTVisitor() {
			@Override
			public void endVisit( Javadoc node ) {
				node.delete();
			}
		} );
	}

	private GrammarUtils() {}
}
