package clegoues.genprog4java.treelm;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import codemining.ast.TreeNode;

public class TreeNodeUtils {
	public static enum VisitResult { CONTINUE, SKIP, ABORT };
	
	public static class TreeStructureException extends Exception {
		private static final long serialVersionUID = 20170208L;
		
		public TreeStructureException() {}
		public TreeStructureException( String s ) {
			super( s );
		}
		public TreeStructureException( String s, Throwable cause ) {
			super( s, cause );
		}
		public TreeStructureException( Throwable cause ) {
			super( cause );
		}
	}
	
	public static < T extends Serializable >
	TreeNode< T > transform (
		final TreeNode< T > fromTree,
		final Function< ? super TreeNode< T >, ? extends TreeNode< T > > preOrder
	) {
		return transform( fromTree, preOrder, (t) -> t );
	}

	public static < T extends Serializable >
	TreeNode< T > transform(
		final TreeNode< T > fromTree,
		final Function< ? super TreeNode< T >, ? extends TreeNode< T > > preOrder,
		final Function< ? super TreeNode< T >, ? extends TreeNode< T > > postOrder
	) {
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
				TreeNode< T > result = transform( child, preOrder, postOrder );
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
		if ( postOrder != null )
			return postOrder.apply( intermediate );
		else
			return intermediate;
	}
	
	public static < T extends Serializable > void visit(
		final TreeNode< T > tree,
		final Function< TreeNode< T >, VisitResult > preOrder,
		final Function< TreeNode< T >, VisitResult > postOrder
	) {
		Deque< Pair< TreeNode< T >, Boolean > > pending = new ArrayDeque<>();
		pending.addFirst( Pair.of( tree, false ) );
		while ( ! pending.isEmpty() ) {
			TreeNode< T > t = pending.peekFirst().getLeft();
			boolean visited = pending.peekFirst().getRight();
			pending.removeFirst();
			
			if ( visited ) {
				VisitResult result =
					postOrder == null ? VisitResult.CONTINUE : postOrder.apply( t );
				switch ( result ) {
				case ABORT:
					return;
				default:
					continue;
				}
			} else {
				pending.addFirst( Pair.of( t, true ) );
				VisitResult result =
					preOrder == null ? VisitResult.CONTINUE : preOrder.apply( t );
				switch ( result ) {
				case CONTINUE:
					for ( int i = t.nProperties() - 1; i >= 0; --i ) {
						int n = t.getChildrenByProperty().get( i ).size();
						for ( int j = n - 1; j >= 0; --j ) {
							pending.addFirst(
								Pair.of( t.getChild( j, i ), false )
							);
						}
					}
				case SKIP:
					continue;
				case ABORT:
					return;
				}
			}
		}
	}

	public static < T extends Serializable, U extends Serializable > void visit2(
		final TreeNode< T > left,
		final TreeNode< U > right,
		final BiFunction<
		    TreeNode< T >, TreeNode< U >, VisitResult
		> preOrder,
		final BiFunction<
		    TreeNode< T >, TreeNode< U >, VisitResult
		> postOrder
	) throws TreeStructureException {
		Deque<
			Triple< TreeNode< T >, TreeNode< U >, Boolean >
		> pending = new ArrayDeque<>();
		pending.addFirst( Triple.of( left, right, false ) );
		while ( ! pending.isEmpty() ) {
			TreeNode< T > a = pending.peekFirst().getLeft();
			TreeNode< U > b = pending.peekFirst().getMiddle();
			boolean visited = pending.peekFirst().getRight();
			pending.removeFirst();
			
			if ( visited ) {
				VisitResult result =
					postOrder == null ? VisitResult.CONTINUE : postOrder.apply( a, b );
				switch ( result ) {
				case ABORT:
					return;
				default:
					continue;
				}
			} else {
				pending.addFirst( Triple.of( a, b, true ) );

				VisitResult result =
					preOrder == null ? VisitResult.CONTINUE : preOrder.apply( a, b );
				switch ( result ) {
				case CONTINUE:
					if ( a.nProperties() != b.nProperties() )
						throw new TreeStructureException( String.format(
							"trees have different properties: %d != %d",
							a.nProperties(), b.nProperties()
						) );
					for ( int i = a.nProperties() - 1; i >= 0; --i ) {
						int n = a.getChildrenByProperty().get( i ).size();
						int m = b.getChildrenByProperty().get( i ).size();
						if ( n != m )
							throw new TreeStructureException( String.format(
								"properties have different children: %d != %d",
								n, m
							) );
						for ( int j = n - 1; j >= 0; --j )
							pending.addFirst( Triple.of(
								a.getChild( j, i ), b.getChild( j, i ), false
							) );
					}
				case SKIP:
					continue;
				case ABORT:
					return;
				}
			}
		}
	}

	private TreeNodeUtils() {}
}
