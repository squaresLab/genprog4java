package clegoues.genprog4java.treelm;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

import codemining.ast.TreeNode;

public class TreeNodeUtils {
	public static < T extends Serializable >
	TreeNode< T > visit (
		final TreeNode< T > fromTree,
		final Function< ? super TreeNode< T >, ? extends TreeNode< T > > preOrder
	) {
		return visit( fromTree, preOrder, (t) -> t );
	}

	public static < T extends Serializable >
	TreeNode< T > visit(
		final TreeNode< T > fromTree,
		final Function< ? super TreeNode< T >, ? extends TreeNode< T > > preOrder,
		final Function< ? super TreeNode< T >, ? extends TreeNode< T > > postOrder
	) {
		checkNotNull( postOrder );

		TreeNode< T > intermediate = fromTree;
		if ( preOrder != null )
			intermediate = preOrder.apply( fromTree );
		List< List< TreeNode< T > > > properties =
			intermediate.getChildrenByProperty();
		List< List< TreeNode< T > > > processed = new ArrayList<>();

		boolean createNew = false;
		for ( List< TreeNode< T > > children : properties ) {
			List< TreeNode< T > > results = new ArrayList<>();
			for ( TreeNode< T > child : children ) {
				TreeNode< T > result = visit( child, preOrder, postOrder );
				results.add( result );
				if ( result != child )
					createNew = true;
			}
			processed.add( results );
		}
		
		if ( createNew ) {
			TreeNode< T > tmpTree = TreeNode.create(
				intermediate.getData(), intermediate.nProperties()
			);
			for ( int i = 0; i < processed.size(); ++i )
				for ( int j = 0; j < processed.get( i ).size(); ++j )
					tmpTree.addChildNode( processed.get( i ).get( j ), i );
			intermediate = tmpTree;
		}
		return postOrder.apply( intermediate );
	}

	public static < T extends Serializable > void walk(
		final TreeNode< T > tree,
		final Consumer< ? super TreeNode< T > > preOrder,
		final Consumer< ? super TreeNode< T > > postOrder
	) {
		visit( tree, (t) -> {
			preOrder.accept( t );
			return t;
		}, (t) -> {
			postOrder.accept( t );
			return t;
		} );
	}

	private TreeNodeUtils() {}
}
