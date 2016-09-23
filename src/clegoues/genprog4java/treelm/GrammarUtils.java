package clegoues.genprog4java.treelm;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class GrammarUtils {
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
