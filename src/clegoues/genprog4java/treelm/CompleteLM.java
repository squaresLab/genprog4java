package clegoues.genprog4java.treelm;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import clegoues.genprog4java.main.Configuration;
import codemining.ast.TreeNode;
import codemining.lm.tsg.FormattedTSGrammar;
import codemining.lm.tsg.TSGNode;
import codemining.lm.tsg.TSGrammar;
import codemining.lm.tsg.samplers.ClassicTsgPosteriorComputer;
import codemining.math.random.SampleUtils;

public class CompleteLM  extends FormattedTSGrammar {
	private static final long serialVersionUID = 20160707L;

	private static final Logger logger = Logger.getLogger(
		CompleteLM.class.getName()
	);

	public CompleteLM( TSGrammar< TSGNode > trained ) {
		super( trained.getTreeExtractor() );
		SampleUtils.setRandomizer(Configuration.randomizer);
		internalMap = new HashMap< TSGNode, Multiset< TreeNode< TSGNode > > >();
		modified = false;
		setPosteriorComputer( new ClassicTsgPosteriorComputer(
			this, (ClassicTsgPosteriorComputer) trained.getPosteriorComputer()
		) );
		addAll( trained );
	}
	
	private void rebuildInternalMap() {
		if ( ! modified )
			return;
		internalMap.clear();
		
		Queue<TreeNode<TSGNode>> pending = new LinkedList<TreeNode<TSGNode>>();

		for ( Multiset< TreeNode< TSGNode > > productions : grammar.values() )
			pending.addAll( productions );
		while ( !pending.isEmpty() ) {
			TreeNode< TSGNode > node = pending.poll();
			for ( List< TreeNode< TSGNode > > production : node.getChildrenByProperty() ) {
				for ( TreeNode< TSGNode > child : production ) {
					if ( child.getData().isRoot )
						continue;
					
					Multiset< TreeNode< TSGNode > > tmp = HashMultiset.create();
					Multiset< TreeNode< TSGNode > > inSet =
						internalMap.putIfAbsent( child.getData(), tmp );
					if ( inSet == null )
						tmp.add( child );
					else
						inSet.add( child );
					pending.add( child );
				}
			}
		}
		modified = false;
	}

	@Override
	public void addAll( TSGrammar< TSGNode > other ) {
		modified = true;
		super.addAll( other );
	}

	@Override
	public void addTree( TreeNode< TSGNode > subTree ) {
		modified = true;
		super.addTree( subTree );
	}
	
	@Override
	public void addTree( TreeNode< TSGNode > tree, int count ) {
		modified = true;
		super.addTree( tree, count );
	}
	
	@Override
	public void clear() {
		modified = true;
		super.clear();
	}

	@Override
	public double computeRulePosteriorLog2Probability(
		TreeNode< TSGNode > tree
	) {
		return posteriorComputer.computeLog2PosteriorProbabilityOfRule( tree, false );
	}

	@Override
	public double computeRulePosteriorLog2Probability(
		TreeNode< TSGNode > tree, boolean remove
	) {
		return posteriorComputer.computeLog2PosteriorProbabilityOfRule( tree, remove );
	}

	@Override
	public int countTreeOccurences( TreeNode< TSGNode > root ) {
		rebuildInternalMap();
		Multiset< TreeNode< TSGNode > > productions;
		if ( root.getData().isRoot )
			productions = grammar.get( root.getData() );
		else
			productions = internalMap.get( root.getData() );
		if ( productions == null )
			return 0;
		return productions.count( root );
	}
	
	@Override
	public int countTreesWithRoot( TSGNode root ) {
		rebuildInternalMap();
		Multiset< TreeNode< TSGNode > > productions;
		if ( root.isRoot )
			productions = grammar.get( root );
		else
			productions = internalMap.get( root );
		if ( productions == null )
			return 0;
		return productions.size();
	}

	@Override
	public TreeNode< TSGNode > generateRandom( TreeNode< TSGNode > root ) {
		rebuildInternalMap();
		
		Multiset< TreeNode< TSGNode > > productions;

		// We don't know if we're starting at the root of a grammar production
		// or not. So create a new node (so we can change its isRoot status) and
		// check for productions with both root and non-root.
		
		TSGNode data = new TSGNode( root.getData().nodeKey );
		productions = internalMap.getOrDefault(
			data, HashMultiset.< TreeNode< TSGNode > >create()
		);
		data.isRoot = true;
		if ( grammar.containsKey( data ) )
			productions.addAll( grammar.get( data ) );
		
		final Queue<TreeNode<TSGNode>> pending = new LinkedList<TreeNode<TSGNode>>();

		BiConsumer< TreeNode<TSGNode>, Multiset<TreeNode<TSGNode>> > select =
			new BiConsumer< TreeNode<TSGNode>, Multiset<TreeNode<TSGNode>> >() {
				@Override
				public void accept(
					TreeNode< TSGNode > node,
					Multiset< TreeNode< TSGNode > > productions
				) {
					TreeNode< TSGNode > selected =
						SampleUtils.getRandomElement( productions );
					selected = selected.deepCopy();
					for ( int i = 0; i < selected.nProperties(); ++i ) {
						for ( TreeNode< TSGNode > child
								: selected.getChildrenByProperty().get( i ) )
						{
							node.addChildNode( child, i );
							pending.add( child );
						}
					}
				}
			};

		select.accept( root, productions );
		while ( !pending.isEmpty() ) {
			TreeNode< TSGNode > node = pending.poll();
			if ( node.getData().isRoot ) {
				super.generateRandom( node );
			} else {
				boolean isNonTerminal = node.nProperties() > 0;
				if ( node.isLeaf() && isNonTerminal ) {
					if ( node.getData().isRoot )
						productions = grammar.get( node.getData() );
					else
						productions = internalMap.get( node.getData() );
					if ( productions == null )
						continue;
					select.accept( node, productions );
				} else if ( !node.isLeaf() ){
					for ( int i = 0; i < node.getChildrenByProperty().size(); ++i ) {
						for ( TreeNode< TSGNode > child
								: node.getChildrenByProperty().get( i ) )
							pending.add( child );
					}
				}
			}
		}
		return root;
	}

	@Override
	public boolean removeTree( TreeNode< TSGNode > subTree ) {
		boolean did_it = super.removeTree( subTree );
		modified = modified || did_it;
		return did_it;
	}

	@Override
	public int removeTree( TreeNode< TSGNode > subTree, int occurences ) {
		int count = super.removeTree( subTree, occurences );
		modified = modified || ( count > 0 );
		return count;
	}
	
	@Override
	public void prune( int threshold ) {
		modified = true;
		super.prune( threshold );
	}
	
	private final Map< TSGNode, Multiset< TreeNode< TSGNode > > > internalMap;
	private boolean modified;
}
