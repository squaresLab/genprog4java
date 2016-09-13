package clegoues.genprog4java.treelm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.eclipse.jdt.core.dom.ASTNode;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multiset;
import com.google.common.math.DoubleMath;

import codemining.ast.TreeNode;
import codemining.ast.java.AbstractJavaTreeExtractor;
import codemining.ast.java.JavaAstTreeExtractor;
import codemining.lm.tsg.TSGNode;
import codemining.lm.tsg.TSGrammar;
import codemining.util.StatsUtil;

public class EclipseTSG {
	public EclipseTSG( TSGrammar< TSGNode > grammar ) {
		this.grammar = grammar.getInternalGrammar();
		this.tsgExtractor = (AbstractJavaTreeExtractor) grammar.getTreeExtractor();
	}
	
	private static class EclipseASTExtractor extends JavaAstTreeExtractor {
		private static final long serialVersionUID = 20160825L;

		private class ASTExtractor extends TreeNodeExtractor {
			public ASTExtractor( boolean useComments ) {
				super( useComments );
			}
			
			@Override
			public TreeNode<Integer> postProcessNodeBeforeAdding(
				TreeNode< Integer > treeNode, ASTNode node
			) {
				mapping.putIfAbsent( node, mapping.size() );
				TreeNode< Integer > result = TreeNode.create(
					mapping.get( node ), treeNode.nProperties()
				);
				List< List< TreeNode< Integer > > > children =
					treeNode.getChildrenByProperty();
				for ( int i = 0; i < treeNode.nProperties(); ++i ) {
					for ( TreeNode< Integer > child : children.get( i ) )
						result.addChildNode( child, i );
				}
				return result;
			}
		}
		
		@Override
		public TreeNode< Integer > getTree( ASTNode node, boolean useComments ) {
			ASTExtractor extractor = new ASTExtractor( useComments );
			extractor.extractFromNode( node );
			return extractor.computedNodes.get( node );
		}

		@Override
		public ASTNode getASTFromTree( TreeNode< Integer > tree ) {
			return mapping.inverse().get( tree.getData() );
		}
		
		private final BiMap< ASTNode, Integer > mapping = HashBiMap.create();
	}
	
	private static class StartPoint {
		public StartPoint(
			TreeNode< TSGNode > tree,
			TreeNode< TSGNode > production,
			double logProb
		) {
			this.tree = tree;
			this.production = production;
			this.logProb = logProb;
		}

		public final TreeNode< TSGNode > tree;
		public final TreeNode< TSGNode > production;
		public final double logProb;
	}

	private static class ParseState {
		private static enum Status { INVALID, VALID, FOUND };
		
		private ParseState( Status status, List< StartPoint > points ) {
			this.status = status;
			this.points = points;
		}
		
		public static ParseState failure =
			new ParseState( Status.INVALID, Collections.emptyList() );
		
		public static ParseState success =
			new ParseState( Status.VALID, Collections.emptyList() );
		
		public static ParseState found(
			TreeNode< TSGNode > tree,
			TreeNode< TSGNode > production,
			double logProb
		) {
			return new ParseState( Status.FOUND, Collections.singletonList(
				new StartPoint( tree, production, logProb )
			) );
		}
		
		private ParseState join( ParseState that ) {
			ArrayList< StartPoint > newPoints = new ArrayList<>( this.points );
			newPoints.addAll( that.points );
			return new ParseState( Status.FOUND, newPoints );
		}
		
		public ParseState and( ParseState that ) {
			if ( this.status == Status.INVALID || that.status == Status.INVALID )
				return failure;
			else if ( this.status == Status.VALID )
				return that;
			else if ( that.status == Status.VALID )
				return this;
			return this.join( that );
		}
		
		public ParseState or( ParseState that ) {
			if ( this.status == Status.INVALID )
				return that;
			else if ( that.status == Status.INVALID )
				return this;
			else if ( this.status != Status.FOUND )
				return that;
			else if ( that.status != Status.FOUND )
				return this;
			return this.join( that );
		}
		
		public boolean isValid() {
			return status != Status.INVALID;
		}
		
		public boolean isFound() {
			return status == Status.FOUND;
		}
		
		public List< StartPoint > getPoints() {
			return points;
		}

		private final Status status;
		private final List< StartPoint > points;
	}

	private double parse( ASTNode root, ASTNode target, double logProb ) {
		EclipseASTExtractor astExtractor = new EclipseASTExtractor();
		TreeNode< Integer > astTree = astExtractor.getTree( root );
		TreeNode< TSGNode > tsgTree = TSGNode.convertTree( tsgExtractor.getTree( root ), 0 );
		
		TSGNode starter = new TSGNode( tsgTree.getData().nodeKey );
		starter.isRoot = true;
		TreeNode< TSGNode > production =
			TreeNode.create( starter, tsgTree.nProperties() );
		
		final Map< TreeNode< Integer >, ParseState > searchCache =
			new HashMap<>();
		ParseState matches = findTarget(
			astExtractor, astTree, tsgTree, production, target,
			logProb, searchCache
		);
		if ( ! matches.isValid() )
			return Double.NEGATIVE_INFINITY;
		if ( ! matches.isFound() )
			return 0.0;
		
		Map< TreeNode< TSGNode >, List< Double > > parseCache = new HashMap<>();
		double weight = Double.NEGATIVE_INFINITY;
		double probability = Double.NEGATIVE_INFINITY;
		for ( StartPoint start : matches.getPoints() ) {
			weight = StatsUtil.log2SumOfExponentials( weight, start.logProb );
			List< Double > probs =
				doParse( start.tree, start.production, start.logProb, parseCache );
			if ( probs.isEmpty() )
				continue;

			for ( double d : probs )
				probability = StatsUtil.log2SumOfExponentials( probability, d );
		}

		if ( Double.isInfinite( probability ) )
			return probability;
		return probability - weight;
	}

	private ParseState findTarget(
		EclipseASTExtractor ae,
		TreeNode< Integer > astTree,
		TreeNode< TSGNode > tsgTree,
		TreeNode< TSGNode > production,
		ASTNode target,
		double logProb,
		Map< TreeNode< Integer >, ParseState > searchCache
	) {
		ParseState result = ParseState.success;
		
		Deque< Triple<
			TreeNode< Integer >,
			TreeNode< TSGNode >,
			TreeNode< TSGNode >
		> > pending = new ArrayDeque<>();
		pending.addFirst( Triple.of( astTree, tsgTree, production ) );
		while ( ! pending.isEmpty() ) {
			TreeNode< Integer > curr = pending.peekFirst().getLeft();
			TreeNode< TSGNode > node = pending.peekFirst().getMiddle();
			TreeNode< TSGNode > prod = pending.peekFirst().getRight();
			pending.removeFirst();
			
			ASTNode current = ae.getASTFromTree( curr );
			if ( current == target ) {
				// If we've reached the target node, do not match it. Doing so
				// would only allow productions from the identical TSGNode.
				// Instead, save the parse state here. If the rest of the parse
				// succeeds, we have a match and can return it outside the loop.
				result = result.and( ParseState.found( node, prod, logProb ) );
				continue;
			}

			int astType = current.getNodeType();
			int tsgType = tsgExtractor.getSymbol( node.getData().nodeKey ).nodeType;
			assert astType == tsgType :
				"node type mismatch: " + astType + " != " + tsgType;
			assert curr.nProperties() == node.nProperties() :
				"properties mismatch: " + curr.nProperties() + " != " + node.nProperties();
			
			if ( prod.getData().nodeKey != node.getData().nodeKey )
				return ParseState.failure;
			if ( prod.nProperties() != node.nProperties() )
				return ParseState.failure;
			
			if ( prod.isLeaf() && ! node.isLeaf() ) {
				// We've reached another production. If that production is
				// is invalid, then so is this one. Otherwise, if that
				// production contains the target, then we save it and wait for
				// the rest of the parse to succeed before returning. Note that
				// at most one production will contain the target, so reset will
				// only be updated at most once.
				result = result.and( branchProductions(
					ae, curr, node, target, logProb, searchCache
				) );
			} else {
				// This is not a new production: every child of the production
				// must match a child in the tree
				
				List< List< TreeNode< Integer > > > leftChildren =
					curr.getChildrenByProperty();
				List< List< TreeNode< TSGNode > > > midChildren =
					node.getChildrenByProperty();
				List< List< TreeNode< TSGNode > > > rightChildren =
					prod.getChildrenByProperty();
				for ( int i = leftChildren.size() - 1; i >= 0; --i ) {
					List< TreeNode< Integer > > left = leftChildren.get( i );
					List< TreeNode< TSGNode > > mid = midChildren.get( i );
					List< TreeNode< TSGNode > > right = rightChildren.get( i );
					assert left.size() == mid.size() :
						"children mismatch: " + left.size() + " != " + mid.size();
					if ( mid.size() != right.size() ) {
						return ParseState.failure;
					}
					for ( int j = left.size() - 1; j >= 0; --j )
						pending.addFirst( Triple.of(
							left.get( j ), mid.get( j ), right.get( j )
						) );
				}
			}
		}
		
		return result;
	}
	
	private ParseState branchProductions(
		EclipseASTExtractor astExtractor,
		TreeNode< Integer > astTree,
		TreeNode< TSGNode > tsgTree,
		ASTNode target,
		double logProb,
		Map< TreeNode< Integer >, ParseState > searchCache
	) {
		if ( searchCache.containsKey( astTree ) )
			return searchCache.get( astTree );
		
		TSGNode key = new TSGNode( tsgTree.getData() );
		key.isRoot = true;
		Multiset< TreeNode< TSGNode > > productions = grammar.get( key );
		if ( productions == null )
			return ParseState.failure;
		
		List< Multiset.Entry< TreeNode< TSGNode > > > entries =
			new ArrayList<>( productions.entrySet() );
			
		logProb -= DoubleMath.log2( productions.size() );
		ParseState result = ParseState.failure;
		for ( int i = 0; i < entries.size(); ++i ) {
			TreeNode< TSGNode > production = entries.get( i ).getElement();
			result = result.or( findTarget(
				astExtractor, astTree, tsgTree,
				production, target,
				logProb + DoubleMath.log2( entries.get( i ).getCount() ),
				searchCache
			) );
		}
		searchCache.put( astTree, result );
		return result;
	}

	private List< Double > doParse(
		TreeNode< TSGNode > tsgTree,
		TreeNode< TSGNode > production,
		double logProb,
		Map< TreeNode< TSGNode >, List< Double > > parseCache
	) {
		List< List< Double > > children = new ArrayList<>();
		Deque< Pair< TreeNode< TSGNode >, TreeNode< TSGNode > > > pending =
			new ArrayDeque<>();
		pending.addFirst( Pair.of( tsgTree, production ) );
		while ( ! pending.isEmpty() ) {
			Pair< TreeNode< TSGNode >, TreeNode< TSGNode > > pair =
				pending.removeFirst();
			TreeNode< TSGNode > node = pair.getLeft();
			TreeNode< TSGNode > prod = pair.getRight();
			
			if ( prod.getData().nodeKey != node.getData().nodeKey )
				return Collections.emptyList();
			if ( prod.nProperties() != node.nProperties() )
				return Collections.emptyList();
			
			if ( prod.isLeaf() && ! node.isLeaf() ) {
				// we've reached another production: gather the possible parses
				// for the subtree as children of this one
				
				List< Double > tmp = doProductions( node, logProb, parseCache );
				if ( tmp.isEmpty() )
					return Collections.emptyList();
				children.add( tmp );
			} else {
				// this is not a new production: every child of the production
				// must match a child in the tree

				List< List< TreeNode< TSGNode > > > leftChildren =
					node.getChildrenByProperty();
				List< List< TreeNode< TSGNode > > > rightChildren =
					prod.getChildrenByProperty();
				for ( int i = leftChildren.size() - 1; i >= 0; --i ) {
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
		
		// probability of this parse is based on getting here (logProb) AND
		// selecting one parse (via OR) for each child

		for ( List< Double > options : children )
			logProb += StatsUtil.log2SumOfExponentials( options );
		return Collections.singletonList( logProb );
	}
	
	private List< Double > doProductions(
		TreeNode< TSGNode > tsgTree,
		double logProb,
		Map< TreeNode< TSGNode >, List< Double > > parseCache
	) {
		if ( parseCache.containsKey( tsgTree ) )
			return parseCache.get( tsgTree );

		TSGNode key = new TSGNode( tsgTree.getData() );
		key.isRoot = true;
		Multiset< TreeNode< TSGNode > > productions = grammar.get( key );
		if ( productions == null )
			return Collections.emptyList();

		List< Multiset.Entry< TreeNode< TSGNode > > > entries =
			new ArrayList<>( productions.entrySet() );
		
		logProb -= DoubleMath.log2( productions.size() );
		List< Double > results = new ArrayList<>();
		for ( int i = 0; i < entries.size(); ++i ) {
			TreeNode< TSGNode > production = entries.get( i ).getElement();
			results.addAll( doParse(
				tsgTree, production,
				logProb + DoubleMath.log2( entries.get( i ).getCount() ),
				parseCache
			) );
		}
		if ( results.isEmpty() )
			return results;
		results = Collections.singletonList( StatsUtil.log2SumOfExponentials( results ) );
		parseCache.put( tsgTree, results );
		return results;	
	}

	public double getLogProb( ASTNode root, ASTNode target ) {
		return parse( root, target, 0.0 );
	}

	private final Map< TSGNode, ? extends Multiset< TreeNode< TSGNode > > > grammar;
	private final AbstractJavaTreeExtractor tsgExtractor;
}
