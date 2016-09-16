package clegoues.genprog4java.treelm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import codemining.math.random.SampleUtils;
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
		
		public StartPoint withLogProb( double logProb ) {
			return new StartPoint( this.tree, this.production, logProb );
		}

		public final TreeNode< TSGNode > tree;
		public final TreeNode< TSGNode > production;
		public final double logProb;
	}

	private static class ParseState {
		private static enum Status { INVALID, VALID, FOUND };
		
		private ParseState(
			Status status, List< StartPoint > points, double logProb
		) {
			this.status = status;
			this.points = Collections.unmodifiableList( points );
			this.logProb = logProb;
		}
		
		public ParseState( ParseState that ) {
			this( that.status, that.points, that.logProb );
		}
		
		public static ParseState failure = new ParseState(
			Status.INVALID, Collections.emptyList(), Double.NEGATIVE_INFINITY
		);
		
		public static ParseState success( double logProb ) {
			return new ParseState(
				Status.VALID, Collections.emptyList(), logProb
			);
		}
		
		public static ParseState found(
			TreeNode< TSGNode > tree,
			TreeNode< TSGNode > production,
			double logProb
		) {
			return new ParseState( Status.FOUND, Collections.singletonList(
				new StartPoint( tree, production, logProb )
			), 0.0 );
		}
		
		public ParseState and( ParseState that ) {
			if ( this.status == Status.INVALID || that.status == Status.INVALID )
				return failure;
			if ( this.status == Status.VALID && that.status == Status.VALID )
				return success( this.logProb + that.logProb );

			assert this.status == Status.VALID || that.status == Status.VALID;

			List< StartPoint > newPoints =
				Stream.concat( this.points.stream(), that.points.stream() ).map(
					(point) -> point.withLogProb(
						point.logProb + this.logProb + that.logProb
					)
				).collect( Collectors.toList() );
			return new ParseState( Status.FOUND, newPoints, 0.0 );
		}
		
		public ParseState or( ParseState that ) {
			if ( this.status == Status.INVALID )
				return that;
			else if ( that.status == Status.INVALID )
				return this;
			if ( this.status == Status.VALID && that.status == Status.VALID )
				return success( StatsUtil.log2SumOfExponentials(
					this.logProb, that.logProb
				) );
			
			assert this.status == Status.FOUND && that.status == Status.FOUND;

			Map< TreeNode< TSGNode >, StartPoint > pointmap = new HashMap<>();
			Stream.concat( this.points.stream(), that.points.stream() ).forEach(
				(p) -> {
					if ( pointmap.containsKey( p.production ) )
						p = p.withLogProb( StatsUtil.log2SumOfExponentials(
							p.logProb, pointmap.get( p.production ).logProb
						) );
					pointmap.put( p.production, p );
				}
			);
			return new ParseState(
				Status.FOUND, new ArrayList<>( pointmap.values() ), 0.0
			);
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
		private final double logProb;
	}

	private static interface Parser< T > {
		public T process( List< StartPoint > points );
	}
	
	public static class ParseException extends Exception {
		private static final long serialVersionUID = 20160914L;

		public ParseException() {}
		public ParseException( String message ) {
			super( message );
		}
		public ParseException( String message, Throwable cause ) {
			super( message, cause );
		}
		protected ParseException(
			String message, Throwable cause,
			boolean enableSuppression, boolean writeableStackTrace
		) {
			super( message, cause, enableSuppression, writeableStackTrace );
		}
		public ParseException( Throwable cause ) {
			super( cause );
		}
	}

	private <T> T parse( ASTNode root, ASTNode target, Parser< T > parser )
		throws ParseException
	{
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
			searchCache
		);
		if ( ! matches.isValid() )
			throw new ParseException( "could not parse AST" );
		if ( ! matches.isFound() )
			throw new ParseException( "could not find target node to replace" );	

		return parser.process( matches.getPoints() );
	}

	private ParseState findTarget(
		EclipseASTExtractor ae,
		TreeNode< Integer > astTree,
		TreeNode< TSGNode > tsgTree,
		TreeNode< TSGNode > production,
		ASTNode target,
		Map< TreeNode< Integer >, ParseState > searchCache
	) {
		ParseState result = ParseState.success( 0.0 );
		
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
				result = result.and( ParseState.found( node, prod, 0.0 ) );
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
					ae, curr, node, target, searchCache
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
		Map< TreeNode< Integer >, ParseState > searchCache
	) {
		if ( searchCache.containsKey( astTree ) )
			return new ParseState( searchCache.get( astTree ) );
		
		TSGNode key = new TSGNode( tsgTree.getData() );
		key.isRoot = true;
		Multiset< TreeNode< TSGNode > > productions = grammar.get( key );
		if ( productions == null )
			return ParseState.failure;
		
		List< Multiset.Entry< TreeNode< TSGNode > > > entries =
			new ArrayList<>( productions.entrySet() );
			
		ParseState result = ParseState.failure;
		double unit = DoubleMath.log2( productions.size() );
		for ( int i = 0; i < entries.size(); ++i ) {
			double logProb =
				DoubleMath.log2( entries.get( i ).getCount() ) - unit;
			TreeNode< TSGNode > production = entries.get( i ).getElement();
			result = result.or( ParseState.success( logProb ).and( findTarget(
				astExtractor, astTree, tsgTree,
				production, target,
				searchCache
			) ) );
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
	
	private TreeNode< TSGNode > generateRandom(
		TreeNode< TSGNode > root, TreeNode< TSGNode > production
	) {
		Deque< Pair< TreeNode< TSGNode >, TreeNode< TSGNode > > > pending =
			new ArrayDeque<>();
		pending.addFirst( Pair.of( root, production ) );
		while ( ! pending.isEmpty() ) {
			TreeNode< TSGNode > node = pending.peekFirst().getLeft();
			TreeNode< TSGNode > prod = pending.peekFirst().getRight();
			pending.removeFirst();
			if ( prod.isLeaf() && 0 < node.nProperties() ) {
				TSGNode key = new TSGNode( node.getData() );
				key.isRoot = true;
				Multiset< TreeNode< TSGNode > > productions = grammar.get( key );
				if ( productions == null )
					continue;
				prod = SampleUtils.getRandomElement( productions );
			}
			if ( ! prod.isLeaf() ) {
				for ( int i = 0; i < prod.nProperties(); ++i ) {
					List< TreeNode< TSGNode > > children =
						prod.getChildrenByProperty().get( i );
					for ( TreeNode< TSGNode > child : children ) {
						TreeNode< TSGNode > copy = TreeNode.create(
							new TSGNode( child.getData().nodeKey ),
							child.nProperties()
						);
						node.addChildNode( copy, i );
						pending.addFirst( Pair.of( copy, child ) );
					}
				}
			}
		}
		return root;
	}

	public double getLogProb( ASTNode root, ASTNode target )
		throws ParseException
	{
		return parse( root, target, ( points ) -> {
			Map< TreeNode< TSGNode >, List< Double > > parseCache = new HashMap<>();
			double weight = Double.NEGATIVE_INFINITY;
			double probability = Double.NEGATIVE_INFINITY;
			for ( StartPoint point : points ) {
				weight = StatsUtil.log2SumOfExponentials( weight, point.logProb );
				List< Double > probs =
					doParse( point.tree, point.production, point.logProb, parseCache );
				if ( probs.isEmpty() )
					continue;

				for ( double d : probs )
					probability = StatsUtil.log2SumOfExponentials( probability, d );
			}

			if ( Double.isInfinite( probability ) )
				return probability;
			return probability - weight;
		} );
	}
	
	public ASTNode babble( ASTNode root, ASTNode target )
		throws ParseException
	{
		return parse( root, target, ( points ) -> {
			double[] logProbs = new double[ points.size() ];
			for ( int i = 0; i < points.size(); ++i )
				logProbs[ i ] = points.get( i ).logProb;
			int i = SampleUtils.getRandomIndex( logProbs );
			StartPoint p = points.get( i );

			TreeNode< TSGNode > tsgTree = TreeNode.create(
				new TSGNode( p.production.getData().nodeKey ),
				p.production.nProperties()
			);
			tsgTree = generateRandom( tsgTree, p.production );
			TreeNode< Integer > intTree = TreeNode.create(
				tsgTree.getData().nodeKey, tsgTree.nProperties()
			);
			TSGNode.copyChildren( intTree, tsgTree );
			return tsgExtractor.getASTFromTree( intTree );
		} );
	}

	private final Map< TSGNode, ? extends Multiset< TreeNode< TSGNode > > > grammar;
	private final AbstractJavaTreeExtractor tsgExtractor;
}
