package clegoues.genprog4java.treelm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.math.DoubleMath;

import clegoues.genprog4java.main.Configuration;

import codemining.ast.TreeNode;
import codemining.lm.tsg.FormattedTSGrammar;
import codemining.lm.tsg.TSGNode;
import codemining.lm.tsg.TSGrammar;
import codemining.math.random.SampleUtils;
import codemining.util.StatsUtil;

public class CompleteLM  extends FormattedTSGrammar {
	private static final long serialVersionUID = 20160707L;

	private static final Logger logger = Logger.getLogger(
		CompleteLM.class.getName()
	);

	public CompleteLM( TSGrammar< TSGNode > trained ) {
		super( trained.getTreeExtractor() );
		SampleUtils.setRandomizer(Configuration.randomizer);
		internalMap = new HashMap< TSGNode, Multiset< TreeNode< TSGNode > > >();
		parseCache = new HashMap<>();
		modified = false;
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
		return computeRulePosteriorLog2Probability( tree, false );
	}

	@Override
	public double computeRulePosteriorLog2Probability(
		TreeNode< TSGNode > tree, boolean remove
	) {
		List< ParseTree > parses = parse( tree, true );

		List< Double > probs = new ArrayList<>( parses.size() );
		for ( ParseTree parse : parses ) {
			probs.add( parse.getTreeLog2Prob() );
		}
		return StatsUtil.log2SumOfExponentials( probs );
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
	
	private Multiset< TreeNode< TSGNode > > getInitialProductions(
		TreeNode< TSGNode > tree
	) {
		Multiset< TreeNode< TSGNode > > productions;

		// We don't know if we're starting at the root of a grammar production
		// or not. So create a new node (so we can change its isRoot status) and
		// check for productions with both root and non-root.
		
		TSGNode data = new TSGNode( tree.getData().nodeKey );
		productions = internalMap.getOrDefault(
			data, HashMultiset.< TreeNode< TSGNode > >create()
		);
		data.isRoot = true;
		if ( grammar.containsKey( data ) )
			productions.addAll( grammar.get( data ) );
		
		return productions;
	}

	@Override
	public TreeNode< TSGNode > generateRandom( TreeNode< TSGNode > root ) {
		rebuildInternalMap();
		
		Multiset< TreeNode< TSGNode > > productions =
			getInitialProductions( root );
		
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
	
	private static class ParseTree {
		public ParseTree( double logProb, TreeNode< TSGNode > production ) {
			this.logProb    = logProb;
			this.production = production;
			this.children   = new ArrayList<>();
		}
		
		public ParseTree withChild( ParseTree child ) {
			ParseTree copy = new ParseTree( logProb, production );
			copy.children.addAll( children );
			copy.children.add( child );
			return copy;
		}

		public double getProductionLog2Prob() {
			return logProb;
		}
		
		public double getTreeLog2Prob() {
			double prob = logProb;
			for ( ParseTree child : children )
				prob += child.getTreeLog2Prob();
			return prob;
		}
		
		public TreeNode< TSGNode > getProduction() {
			return production;
		}

		private final double logProb;
		private final TreeNode< TSGNode > production;
		private final List< ParseTree > children;
	}
	
	private List< ParseTree > parse(
		TreeNode< TSGNode > tree, boolean considerInternalProductions
	) {
		TSGNode data = new TSGNode( tree.getData().nodeKey );

		Multiset< TreeNode< TSGNode > > productions =
			HashMultiset.< TreeNode< TSGNode > >create();
		if ( considerInternalProductions && internalMap.containsKey( data ) )
			productions.addAll( internalMap.get( data ) );
		data.isRoot = true;
		if ( grammar.containsKey( data ) )
			productions.addAll( grammar.get( data ) );

		if ( ! parseCache.containsKey( tree ) )
			parseCache.clear();
		
		double weight = - DoubleMath.log2( productions.size() );
		return tryProductions( productions, tree, weight, !considerInternalProductions );
	}
	
	private List< ParseTree > tryProductions(
		Multiset< TreeNode< TSGNode > > productions,
		TreeNode< TSGNode > tree,
		double weight,
		boolean useCache
	) {
		if ( useCache && parseCache.containsKey( tree ) )
			return parseCache.get( tree );
		List< ParseTree > trees = new ArrayList<>();
		for (Multiset.Entry<TreeNode<TSGNode>> entry : productions.entrySet()) {
			TreeNode< TSGNode > production = entry.getElement();
			trees.addAll( tryParse(
				production, tree, weight + DoubleMath.log2( entry.getCount() )
			) );
		}
		if ( useCache )
			parseCache.put( tree, trees );
		return trees;
	}

	private List< ParseTree > tryParse(
		TreeNode< TSGNode > production, TreeNode< TSGNode > tree, double weight
	) {
		List< List< ParseTree > > children = new ArrayList<>();
		Deque< Pair< TreeNode< TSGNode >, TreeNode< TSGNode > > > pending =
			new ArrayDeque<>();
		pending.addFirst( Pair.of( production, tree ) );
		while ( ! pending.isEmpty() ) {
			Pair< TreeNode< TSGNode >, TreeNode< TSGNode > > next =
				pending.removeFirst();
			TreeNode< TSGNode > prod = next.getLeft();
			TreeNode< TSGNode > node = next.getRight();
			
			if ( prod.getData().nodeKey != node.getData().nodeKey )
				return Collections.emptyList();
			if ( prod.nProperties() != node.nProperties() )
				return Collections.emptyList();
			
			if ( prod.isLeaf() && ! node.isLeaf() ) {
				// we've reached another production: gather the possible parses
				// for the subtree as children of this one
				
				Multiset< TreeNode< TSGNode > > subProductions =
					grammar.get( prod.getData() );
				if ( subProductions == null )
					return Collections.emptyList();
				double subWeight = - DoubleMath.log2( subProductions.size() );
				
				List< ParseTree > tmp =
					tryProductions( subProductions, node, subWeight, true );
				if ( tmp.isEmpty() )
					return Collections.emptyList();
				children.add( tmp );
			} else {
				// this is not a new production: every child of the production
				// must match a child in the tree
				List< List< TreeNode< TSGNode > > > leftChildren =
					prod.getChildrenByProperty();
				List< List< TreeNode< TSGNode > > > rightChildren =
					node.getChildrenByProperty();
				for ( int i = 0; i < leftChildren.size(); ++i ) {
					List< TreeNode< TSGNode > > left = leftChildren.get( i );
					List< TreeNode< TSGNode > > right = rightChildren.get( i );
					if ( left.size() != right.size() )
						return Collections.emptyList();
					for ( int j = left.size() - 1; j >= 0; --j )
						pending.addFirst(
							Pair.of( left.get( j ), right.get( j ) )
						);
				}
			}
		}
		
		List< ParseTree > parses = Collections.singletonList(
			new ParseTree( weight, production )
		);
		for ( List< ParseTree > options : children ) {
			List< ParseTree > tmp =
				new ArrayList<>( parses.size() * options.size() );
			for ( ParseTree parse : parses ) {
				for ( ParseTree child : options )
					tmp.add( parse.withChild( child ) );
			}
			parses = tmp;
		}
		return parses;
	}

	private final Map< TSGNode, Multiset< TreeNode< TSGNode > > > internalMap;
	private final Map< TreeNode< TSGNode >, List< ParseTree > > parseCache;
	private boolean modified;
}
