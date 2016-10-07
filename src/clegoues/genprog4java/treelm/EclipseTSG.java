package clegoues.genprog4java.treelm;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.core.dom.ASTNode;

import com.google.common.collect.Multiset;
import com.google.common.math.DoubleMath;

import codemining.ast.AstNodeSymbol;
import codemining.ast.TreeNode;
import codemining.lm.tsg.TSGNode;
import codemining.lm.tsg.TSGrammar;
import codemining.math.random.SampleUtils;
import codemining.util.StatsUtil;

public class EclipseTSG {
	public EclipseTSG( TSGrammar< TSGNode > grammar ) {
		this.grammar = grammar.getInternalGrammar();
		ChainedJavaTreeExtractor ex =
			(ChainedJavaTreeExtractor) grammar.getTreeExtractor();
		this.tsgExtractor = new ChainedJavaTreeExtractor( ex );
	}
	
	private static class EclipseASTProcess
		implements ChainedJavaTreeExtractor.PostProcess,
			Supplier< EclipseASTProcess >,
			Serializable
	{
		private static final long serialVersionUID = 20160930L;

		public EclipseASTProcess () {
			this.nodeTable = new IdentityHashMap<>();
		}

		@Override
		public TreeNode< Integer > encode(
			TreeNode< Integer > tree, ASTNode node,
			Function< AstNodeSymbol, Integer > getOrAddSymbol
		) {
			nodeTable.putIfAbsent( tree, node );
			return tree;
		}

		@Override
		public TreeNode< Integer > decode(
			TreeNode< Integer > tree, Function< Integer,
			AstNodeSymbol > getSymbol
		) {
			return tree;
		}
		
		@Override
		public EclipseASTProcess get() {
			return this;
		}
		
		public ASTNode getASTNode( TreeNode< Integer > tree ) {
			return nodeTable.get( tree );
		}

		private final IdentityHashMap< TreeNode< Integer >, ASTNode > nodeTable;
	}

	private static class StartPoint {
		public StartPoint(
			TreeNode< Integer > tree,
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

		public final TreeNode< Integer > tree;
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
		
		public static ParseState failure = new ParseState(
			Status.INVALID, Collections.emptyList(), Double.NEGATIVE_INFINITY
		);
		
		public static ParseState success( double logProb ) {
			return new ParseState(
				Status.VALID, Collections.emptyList(), logProb
			);
		}
		
		public static ParseState found(
			TreeNode< Integer > tree,
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

	private <T> T parse(
		ASTNode root, ASTNode target, Function< List< StartPoint >, T > parser
	)
		throws ParseException
	{
		EclipseASTProcess nodeMapper = new EclipseASTProcess();
		ChainedJavaTreeExtractor astExtractor =
			new ChainedJavaTreeExtractor( tsgExtractor );
		astExtractor.addPostProcessFactory( nodeMapper );
		TreeNode< Integer > tsgTree = astExtractor.getTree( root );
		
		TSGNode starter = new TSGNode( tsgTree.getData() );
		starter.isRoot = true;
		TreeNode< TSGNode > production =
			TreeNode.create( starter, tsgTree.nProperties() );
		
		final Map< ASTNode, ParseState > searchCache = new HashMap<>();
		ParseState matches = findTarget(
			nodeMapper, tsgTree, production, target, searchCache
		);
		if ( ! matches.isValid() )
			throw new ParseException( "could not parse AST" );
		if ( ! matches.isFound() )
			throw new ParseException( "could not find target node to replace" );	

		return parser.apply( matches.getPoints() );
	}

	private ParseState findTarget(
		EclipseASTProcess nodeMapper,
		TreeNode< Integer > tsgTree,
		TreeNode< TSGNode > production,
		ASTNode target,
		Map< ASTNode, ParseState > searchCache
	) {
		ParseState result = ParseState.success( 0.0 );
		
		Deque< Pair< TreeNode< Integer >, TreeNode< TSGNode > > > pending =
			new ArrayDeque<>();
		pending.addFirst( Pair.of( tsgTree, production ) );
		while ( ! pending.isEmpty() ) {
			TreeNode< Integer > node = pending.peekFirst().getLeft();
			TreeNode< TSGNode > prod = pending.peekFirst().getRight();
			pending.removeFirst();
			
			ASTNode current = nodeMapper.getASTNode( node );
			if ( current == target ) {
				// If we've reached the target node, do not match it. Doing
				// so would only allow productions from the identical
				// TSGNode. Instead, save the parse state here. If the rest
				// of the parse succeeds, we have a match and can return it
				// outside the loop.
				result = result.and( ParseState.found( node, prod, 0.0 ) );
				continue;
			}

			if ( prod.getData().nodeKey != node.getData() )
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
					nodeMapper, node, target, searchCache
				) );
			} else {
				// This is not a new production: every child of the production
				// must match a child in the tree
				
				List< List< TreeNode< Integer > > > leftChildren =
					node.getChildrenByProperty();
				List< List< TreeNode< TSGNode > > > rightChildren =
					prod.getChildrenByProperty();
				for ( int i = leftChildren.size() - 1; i >= 0; --i ) {
					List< TreeNode< Integer > > left = leftChildren.get( i );
					List< TreeNode< TSGNode > > right = rightChildren.get( i );
					if ( left.size() != right.size() )
						return ParseState.failure;
					for ( int j = left.size() - 1; j >= 0; --j )
						pending.addFirst( Pair.of(
							left.get( j ), right.get( j )
						) );
				}
			}
		}
		
		return result;
	}
	
	private ParseState branchProductions(
		EclipseASTProcess nodeMapper,
		TreeNode< Integer > tsgTree,
		ASTNode target,
		Map< ASTNode, ParseState > searchCache
	) {
		ASTNode current = nodeMapper.getASTNode( tsgTree );
		if ( current != null && searchCache.containsKey( current ) )
			return searchCache.get( current );
		
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
				nodeMapper, tsgTree, production, target, searchCache
			) ) );
		}
		if ( current != null )
			searchCache.put( current, result );
		return result;
	}

	private List< Double > doParse(
		TreeNode< Integer > tsgTree,
		TreeNode< TSGNode > production,
		double logProb,
		Map< TreeNode< Integer >, List< Double > > parseCache
	) {
		List< List< Double > > children = new ArrayList<>();
		Deque< Pair< TreeNode< Integer >, TreeNode< TSGNode > > > pending =
			new ArrayDeque<>();
		pending.addFirst( Pair.of( tsgTree, production ) );
		while ( ! pending.isEmpty() ) {
			Pair< TreeNode< Integer >, TreeNode< TSGNode > > pair =
				pending.removeFirst();
			TreeNode< Integer > node = pair.getLeft();
			TreeNode< TSGNode > prod = pair.getRight();
			
			if ( prod.getData().nodeKey != node.getData() )
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

				List< List< TreeNode< Integer > > > leftChildren =
					node.getChildrenByProperty();
				List< List< TreeNode< TSGNode > > > rightChildren =
					prod.getChildrenByProperty();
				for ( int i = leftChildren.size() - 1; i >= 0; --i ) {
					List< TreeNode< Integer > > left = leftChildren.get( i );
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
		TreeNode< Integer > tsgTree,
		double logProb,
		Map< TreeNode< Integer >, List< Double > > parseCache
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

	public double getLogProb( ASTNode target )
		throws ParseException
	{
		ASTNode root = GrammarUtils.getStartNode( target );
		return parse( root, target, ( points ) -> {
			Map< TreeNode< Integer >, List< Double > > parseCache = new HashMap<>();
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
	
	public ASTNode babbleFrom( ASTNode target )
		throws ParseException
	{
		ASTNode root = GrammarUtils.getStartNode( target );
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
	private final ChainedJavaTreeExtractor tsgExtractor;
}
