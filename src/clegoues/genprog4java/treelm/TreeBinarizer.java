package clegoues.genprog4java.treelm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.ASTNode;

import codemining.ast.AstNodeSymbol;
import codemining.ast.TreeNode;

/**
 * Transforms the properties of each node in the tree so that instead of
 * containing a list of children, they contain a degenerate binary tree of
 * children.
 * <p>
 * For example, this post-processor would transform the following node with
 * three properties and five children:
 * </p>
 * <pre>
 *   node
 *   /
 * prop1--prop2--prop3
 *  /             /
 * n1-n2-n3      n4-n5
 * </pre>
 * into a tree with three properties and three children (newly created nodes
 * are enclosed in brackets):
 * <pre>
 *       node
 *       /
 *     prop1--prop2--prop3
 *      /      /      /
 *  [10001]  [0]  [10003]
 *   /             /
 *  p1----p2      p1----p2
 *  /     /       /     /
 * n1 [10001]    n4 [10003]
 *      /             /
 *     p1----p2      p1----p2
 *     /     /       /     /
 *    n2 [10001]    n5   [0]
 *        /
 *       p1----p2
 *       /     /
 *      n3   [0]
 * </pre>
 * <p>
 * This will hopefully allow the training process to identify useful fragments
 * that consist of consecutive children in a single property of the original
 * tree. For example, consecutive statements within a block.
 * </p>
 * 
 * @author jonathan
 */
public class TreeBinarizer implements
	Supplier< ChainedJavaTreeExtractor.PostProcess >,
	ChainedJavaTreeExtractor.PostProcess,
	Serializable
{
	private static final long serialVersionUID = 20170210;

	public TreeBinarizer() {
		containers = new ConcurrentHashMap<>();
		queues = null;
	}
	
	@Override
	public TreeBinarizer get() {
		return this;
	}
	
	private static final class Counts
		implements Comparable< Counts >, Serializable
	{
		private static final long serialVersionUID = 20170210L;

		public int numProperties = Integer.MAX_VALUE;
		public int count = 0;
		public Integer symbolId = null;

		@Override
		public int compareTo( Counts that ) {
			// We want to prioritize symbols with the fewest properties ...
			if ( this.numProperties != that.numProperties )
				return this.numProperties - that.numProperties;
			// ... followed by the largest count ...
			if ( this.count != that.count )
				return that.count - this.count;
			// ... followed by arbitrary tie-breaking for consistency with
			// equals()
			return that.symbolId - this.symbolId;
		}
		
		@Override
		public boolean equals( Object that ) {
			if ( this == that )
				return true;
			if ( ! ( that instanceof Counts ) )
				return false;
			Counts c = ( Counts ) that;
			return numProperties == c.numProperties
				&& count == c.count
				&& symbolId == c.symbolId;
		}
	}

	private void rebuildQueue() {
		if ( queues != null )
			return;
		queues = new HashMap<>();
		for ( Map.Entry< String, Map< Integer, Counts > > map
				: containers.entrySet() ) {
			for ( Counts n : map.getValue().values() ) {
				PriorityQueue< Counts > queue = queues.computeIfAbsent(
					map.getKey(), ( x ) -> new PriorityQueue<>()
				);
				queue.add( n );
			}
		}
	}

	private AstNodeSymbol getBinarySymbol( String propname ) {
		AstNodeSymbol binary = new AstNodeSymbol( AstNodeSymbol.MULTI_NODE );
		binary.addChildProperty( "Current" );
		binary.addChildProperty( "Next" );
		binary.addAnnotation( "property", propname );
		return binary;
	}

	@Override
	public TreeNode< Integer > encode(
		TreeNode< Integer > tree, ASTNode node,
		Function< AstNodeSymbol, Integer > getOrAddSymbol,
		Function< Integer, AstNodeSymbol > getSymbol
	) {
		AstNodeSymbol treeSymbol = getSymbol.apply( tree.getData() );
		TreeNode< Integer > copy =
			TreeNode.create( tree.getData(), tree.nProperties() );
		
		int termID = getOrAddSymbol.apply(
			new AstNodeSymbol( AstNodeSymbol.MULTI_NODE )
		);

		// binarize each property separately
		
		for ( int i = 0; i < tree.nProperties(); ++i ) {
			// Record this tree as having this property
			
			String propname = treeSymbol.getChildProperty( i );
			Counts n = containers
				.computeIfAbsent( propname, ( x ) -> new HashMap<>() )
				.computeIfAbsent( tree.getData(), ( x ) -> new Counts() );
			n.count += 1;
			n.numProperties = Math.min( n.numProperties, tree.nProperties() );
			n.symbolId = tree.getData();
			if ( queues != null )
				queues = null;

			// Get an ID for binary nodes that fit under the current property
			// of this tree.
			
			int id = getOrAddSymbol.apply( getBinarySymbol( propname ) );

			// Create the terminator node first. We will build the chain in a
			// bottom-up fashion. Note that this produces the side-effect that
			// every property of the parent node will contain at least the
			// terminal node.
			
			TreeNode< Integer > current = TreeNode.create( termID, 0 );

			// For each child of this property (starting with the last), create
			// a new node with two properties. Insert the current child as the
			// only child of the first property and the current binarized tree
			// as the only child of the second property. We do not need to copy
			// the child node; since nodes are processed in post-order, the
			// child has already been encoded.
			
			List< TreeNode< Integer > > children =
				tree.getChildrenByProperty().get( i );
			for ( int j = children.size() - 1; j >=0; --j ) {
				TreeNode< Integer > binary = TreeNode.create( id, 2 );
				binary.addChildNode( children.get( j ), 0 );
				binary.addChildNode( current, 1 );
				current = binary;
			}
			copy.addChildNode( current, i );
		}
		return copy;
	}
	
	private TreeNode< Integer > getChild(
		TreeNode< Integer > parent, int i, int propertyId
	) {
		List< List< TreeNode< Integer > > > properties =
			parent.getChildrenByProperty();
		if ( properties.size() <= propertyId )
			return null;
		
		List< TreeNode< Integer > > children = properties.get( propertyId );
		if ( children.size() <= i )
			return null;
		return children.get( i );
	}

	private List< TreeNode< Integer > > decodePropList(
		TreeNode< Integer > tree,
		Function< Integer, AstNodeSymbol > getSymbol
	) {
		List< TreeNode< Integer > > children = new ArrayList<>();

		// Assume we were given the first multi-node of the property. We can
		// just walk down the tree, collecting the left children into the list.
		// Since nodes are processed in pre-order for decoding, we do not need
		// to process the children; they will be processed later in the walk.
		//
		// To support de-binarizing fragments, we must deal with the possibility
		// that the chain may not contain the left children or may terminate
		// early.
		
		while ( tree != null && tree.nProperties() != 0 ) {
			AstNodeSymbol symbol = getSymbol.apply( tree.getData() );
			assert symbol.nodeType == AstNodeSymbol.MULTI_NODE :
				"cannot debinarize non-binarized tree";

			TreeNode< Integer > child = getChild( tree, 0, 0 );
			if ( child != null )
				// FIXME: just skip missing children? Or should we come up with
				// a placeholder?
				children.add( child );
			tree = getChild( tree, 0, 1 );
		}

		return children;
	}

	@Override
	public TreeNode< Integer > decode(
		TreeNode< Integer > tree,
		Function< AstNodeSymbol, Integer > getOrAddSymbolId,
		Function< Integer, AstNodeSymbol > getSymbol,
		SymbolTable st
	) {
		TreeNode< Integer > copy;
		
		// Training may have identified fragments that start and stop at
		// arbitrary nodes in the binarized tree. To support de-binarizing
		// fragments, we need to detect for each node whether it is one of our
		// MULTI_NODEs or one from the underlying tree.
		
		AstNodeSymbol symbol = getSymbol.apply( tree.getData() );
		if ( symbol.nodeType == AstNodeSymbol.MULTI_NODE ) {
			// If we start with a MULTI_NODE, we will need to invent a container
			// to hold this tree after we collapse it back into a list.
			//
			// FIXME: this is a hack and includes some probability of failure,
			// since not every node in some property list X can appear in every
			// property list X.

			if ( ! symbol.hasAnnotation( "property" ) )
				throw new RuntimeException(
					"cannot de-binarize terminal multi-node"
				);
			rebuildQueue();
			
			String propname = (String) symbol.getAnnotation( "property" );
			int containerId = queues.get( propname ).peek().symbolId;
			AstNodeSymbol containerSymbol = getSymbol.apply( containerId );

			copy = TreeNode.create(
				containerId, containerSymbol.nChildProperties()
			);
			for ( int i = 0; i < containerSymbol.nChildProperties(); ++i )
				if ( containerSymbol.getChildProperty( i ).equals( propname ) )
					for ( TreeNode< Integer > child
							: decodePropList( tree, getSymbol ) )
						copy.addChildNode( child, i );
		} else {
			// If we start with a normal node, we just de-binarize each of its
			// properties in turn.

			copy = TreeNode.create( tree.getData(), tree.nProperties() );
			for ( int i = 0; i < tree.nProperties(); ++i ) {
				int nChildren = tree.getChildrenByProperty().get( i ).size();
				if ( nChildren == 0 )
					continue;
				for ( TreeNode< Integer > child
						: decodePropList( tree.getChild( 0, i ), getSymbol ) )
					copy.addChildNode( child, i );
			}
		}
		return copy;
	}
	
	private final Map< String, Map< Integer, Counts > > containers;
	private transient Map< String, PriorityQueue< Counts > > queues;
}
