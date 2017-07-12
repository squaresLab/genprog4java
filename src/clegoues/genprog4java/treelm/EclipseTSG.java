package clegoues.genprog4java.treelm;

import java.io.IOException;
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

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multiset;

import codemining.ast.AstNodeSymbol;
import codemining.ast.TreeNode;
import codemining.ast.java.AbstractJavaTreeExtractor;
import codemining.lm.tsg.ITsgPosteriorProbabilityComputer;
import codemining.lm.tsg.TSGNode;
import codemining.lm.tsg.TSGrammar;
import codemining.math.random.SampleUtils;
import codemining.util.serialization.Serializer;
import clegoues.util.ConfigurationBuilder;
import clegoues.util.Probability;

import static clegoues.util.ConfigurationBuilder.STRING;
	
public class EclipseTSG {
	private static final Logger logger = Logger.getLogger( EclipseTSG.class );
	
	public static final ConfigurationBuilder.RegistryToken token =
		ConfigurationBuilder.getToken( VariableAbstractor.token );
	
	private static String dottyFile = ConfigurationBuilder.of( STRING )
		.inGroup( "Grammar-Based Insertions" )
		.withFlag( "graphviz-file" )
		.withVarName( "dottyFile" )
		.withHelp( "write the production graph as a GraphViz graph" )
		.withDefault( "" )
		.build();

	public EclipseTSG( TSGrammar< TSGNode > grammar ) {
		this.grammar = grammar.getInternalGrammar();
		this.posterior = grammar.getPosteriorComputer();
		ChainedJavaTreeExtractor ex =
			(ChainedJavaTreeExtractor) grammar.getTreeExtractor();
		this.tsgExtractor = new ChainedJavaTreeExtractor( ex );
	}
	
	private static class TreeNote {
		public ASTNode node = null;
		public TreeNode< Integer > target = null;
		public ParseState parseResult = null;
	}

	private static class EclipseASTProcess
		implements ChainedJavaTreeExtractor.PostProcess,
			Supplier< EclipseASTProcess >,
			Serializable
	{
		private static final long serialVersionUID = 20160930L;

		public EclipseASTProcess(
			Map< TreeNode< Integer >, TreeNote > notes
		) {
			this.notes = notes;
		}

		@Override
		public TreeNode< Integer > encode(
			TreeNode< Integer > tree, ASTNode node,
			Function< AstNodeSymbol, Integer > getOrAddSymbol,
			Function< Integer, AstNodeSymbol > getSymbol
		) {
			notes.computeIfAbsent( tree, ( t ) -> new TreeNote() ).node = node;
			return tree;
		}

		@Override
		public TreeNode< Integer > decode(
			TreeNode< Integer > tree,
			Function< AstNodeSymbol, Integer > getOrAddSymbolId,
			Function< Integer, AstNodeSymbol > getSymbol,
			SymbolTable st
		) {
			return tree;
		}
		
		@Override
		public EclipseASTProcess get() {
			return this;
		}
		
		private final Map< TreeNode< Integer >, TreeNote > notes;
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

	private ParseState parse( ASTNode root, ASTNode target )
		throws ParseException
	{
		// Set up notes for every node in the tree. This way we can associate
		// data with each node.
		
		Map< TreeNode< Integer >, TreeNote > notes = new IdentityHashMap<>();
		ChainedJavaTreeExtractor astExtractor =
			new ChainedJavaTreeExtractor( tsgExtractor );
		astExtractor.addPostProcessFactory( new EclipseASTProcess( notes ) );
		TreeNode< Integer > source = astExtractor.getTree( root );

		// Walk the tree to find the target node. Add each node to the current
		// path in the preOrder visit, then remove it in the postOrder visit.
		// When we find the target node, the current path is the path to the
		// target.
		
		Deque< TreeNode< Integer > > path = new ArrayDeque<>();
		TreeNodeUtils.visit( source, ( t ) -> {
			path.addLast( t );
			TreeNote note =
				notes.computeIfAbsent( t, ( s ) -> new TreeNote() );
			if ( note.node == target ) {
				for ( TreeNode< Integer > node : path )
					notes.get( node ).target = t;
			}
			return TreeNodeUtils.VisitResult.CONTINUE;
		}, ( t ) -> {
			path.removeLast();
			return TreeNodeUtils.VisitResult.CONTINUE;
		} );
		if ( notes.get( source ).target == null )
			throw new ParseException( "could not find target node to replace" );	
		
		// Create a one-node "production" representing the root of the tree.

		TSGNode starter = new TSGNode( source.getData() );
		starter.isRoot = true;
		TreeNode< TSGNode > production =
			TreeNode.create( starter, source.nProperties() );
		
		ParseState result = tryProduction( source, production, notes );
		return result.with( notes.get( source ).target );
	}

	private static class ParseState {
		public ParseState(
			List< Pair< TreeNode< TSGNode >, Probability > > prods
		) {
			Probability net = Probability.FALSE;
			for ( Pair< TreeNode< TSGNode >, Probability > prod : prods )
				net = net.or( prod.getRight() );

			this.prods = Collections.unmodifiableList( prods );
			this.p = net;
			this.subtree = null;
		}
		
		public ParseState( Probability p ) {
			this.prods = Collections.emptyList();
			this.p = p;
			this.subtree = null;
		}
		
		private ParseState(
			List< Pair< TreeNode< TSGNode >, Probability > > prods,
			Probability p,
			TreeNode< Integer > subtree
		) {
			this.prods = prods;
			this.p = p;
			this.subtree = subtree;
		}

		public ParseState with( TreeNode< Integer > subtree ) {
			return new ParseState( prods, p, subtree );
		}
		
		public List< Pair< TreeNode< TSGNode >, Probability > > getProductions() {
			return prods;
		}
		
		public Probability getProbability() {
			return p;
		}
		
		public TreeNode< Integer > getSubtree() {
			return subtree;
		}
		
		private final List< Pair< TreeNode< TSGNode >, Probability > > prods;
		private final Probability p;
		private final TreeNode< Integer > subtree;
	}

	private static class ParseStateBuilder {
		// invariant: probability != Probability.TRUE => productions.isEmpty()

		public static ParseStateBuilder leafCollector() {
			return new ParseStateBuilder( Probability.TRUE );
		}
		
		public static ParseStateBuilder ruleCollector() {
			return new ParseStateBuilder( Probability.FALSE );
		}

		private ParseStateBuilder( Probability initial ) {
			this.productions = new HashMap<>();
			this.probability = initial;
		}
		
		public ParseState build() {
			if ( productions.isEmpty() )
				return new ParseState( probability );

			List< Pair< TreeNode< TSGNode >, Probability > > prods =
				new ArrayList<>( productions.size() );
			for ( Map.Entry< TreeNode< TSGNode >, Probability > prod
					: productions.entrySet() )
				prods.add( Pair.of( prod.getKey(), prod.getValue() ) );
			return new ParseState( prods );
		}

		public void and( ParseState state ) {
			Preconditions.checkArgument(
				productions.isEmpty() || state.getProductions().isEmpty(),
				"target node is not unique"
			);

			// Failure "and" anything is failure.
			
			if ( probability == Probability.FALSE )
				return;
			
			// If this has productions, then we know state does not and vice
			// versa. Thus only one of these two loops will do anything.
			
			for ( Map.Entry< TreeNode< TSGNode >, Probability > prod
					: productions.entrySet() )
				prod.setValue( prod.getValue().and( state.getProbability() ) );
			for ( Pair< TreeNode< TSGNode >, Probability > prod
					: state.getProductions() )
				productions.put( prod.getKey(), prod.getValue().and( probability ) );
			
			// Maintain the invariant
			
			if ( productions.isEmpty() )
				probability = probability.and( state.getProbability() );
			else
				probability = Probability.TRUE;
		}
		
		public void or( Probability p, ParseState state ) {
			// Add every production from state after "anding" its probability
			// with p. If we already have the production, just "or" the new and
			// old values together.
			
			for ( Pair< TreeNode< TSGNode >, Probability > prod
					: state.getProductions() ) {
				Probability newp = prod.getValue().and( p ).or(
					productions.getOrDefault( prod.getKey(), Probability.FALSE )
				);
				productions.put( prod.getKey(), newp );
			}

			if ( productions.isEmpty() )
				// Keep track of the probability if we still have no productions
				probability = probability.or( p.and( state.getProbability() ) );
			else
				// If we do have productions, those are the only probabilities
				// that matter. We ignore a failure to match on one possibility
				// as soon as another one succeeds. This maintains the invariant
				probability = Probability.TRUE;
		}

		public void addTarget( TreeNode< TSGNode > production ) {
			if ( probability == Probability.FALSE )
				return;
			productions.put( production, probability );
			probability = Probability.TRUE;
		}
		
		public void noMatch() {
			probability = Probability.FALSE;
			productions.clear();
		}
		
		private Map< TreeNode< TSGNode >, Probability > productions;
		private Probability probability;
	}

	private ParseState tryProduction(
		TreeNode< Integer > sourceTree,
		TreeNode< TSGNode > production,
		Map< TreeNode< Integer >, TreeNote > notes
	) {
		ParseStateBuilder result = ParseStateBuilder.leafCollector();
		try {
			TreeNodeUtils.visit2( production, sourceTree, ( prod, node ) -> {
				TreeNote note = notes.get( node );
				if ( note.target == node ) {
					// We've reached the target node. This node and its subtree
					// always match so that it can be replaced with different
					// productions. SKIP the rest of this subtree but keep
					// matching the rest.
					result.addTarget( prod );
					return TreeNodeUtils.VisitResult.SKIP;
				} else if ( prod.getData().nodeKey != node.getData() ) {
					// NOT A MATCH: the production specifies a different type of
					// node here. Just abort here: this failure supersedes any
					// productions or probabilities from other branches.
					result.noMatch();
					return TreeNodeUtils.VisitResult.ABORT;
				} else if ( prod.isLeaf() && ! node.isLeaf() ) {
					// We've reached another production. SKIP the rest of this
					// subtree and rely on branchProductions() to parse it instead.
					result.and( branchProductions( node, notes ) );
					return TreeNodeUtils.VisitResult.SKIP;
				}
				return TreeNodeUtils.VisitResult.CONTINUE;
			}, null );
		} catch ( TreeNodeUtils.TreeStructureException e ) {
			// NOT A MATCH: The production and source trees did not match.
			result.noMatch();
		}
		return result.build();
	}
	
	private ParseState branchProductions(
		TreeNode< Integer > sourceTree,
		Map< TreeNode< Integer >, TreeNote > notes
	) {
		// If we have already parsed the subtree rooted at this node, we do not
		// need to parse it again.
		
		TreeNote note = notes.get( sourceTree );
		if ( note.parseResult != null )
			return note.parseResult;
		
		ParseStateBuilder result = ParseStateBuilder.ruleCollector();
		
		// Get the set of grammar productions with this root.
		
		TSGNode key = new TSGNode( sourceTree.getData() );
		key.isRoot = true;
		Multiset< TreeNode< TSGNode > > productions = grammar.get( key );

		// If we found some productions, we need to try each one to see if it
		// matches the source tree.

		if ( productions != null ) {
			List< Multiset.Entry< TreeNode< TSGNode > > > entries =
				new ArrayList<>( productions.entrySet() );
			for ( int i = 0; i < entries.size(); ++i ) {
				TreeNode< TSGNode > production = entries.get( i ).getElement();
				result.or(
					Probability.logProb(
						posterior.computeLog2PosteriorProbabilityOfRule( production, false )
					), tryProduction( sourceTree, production, notes )
				);
			}
		}

		// If we had no productions to try or if none of our productions matched
		// then we just assign a probability for the production generating the
		// whole subtree.

		ParseState state = result.build();
		if ( productions == null ||
				state.getProbability().getLog() == Double.NEGATIVE_INFINITY ) {
			Probability p = Probability.logProb(
				posterior.computeLog2PosteriorProbabilityOfRule(
					TSGNode.convertTree( sourceTree, 0 ), false
				)
			);
			state = new ParseState( p );
		}

		note.parseResult = state;
		return state;
	}

	private TreeNode< TSGNode > generateRandom(
		TreeNode< TSGNode > root, TreeNode< TSGNode > production
	) {
		TreeToGraphViz< TSGNode > dotty = null;
		if ( ! dottyFile.isEmpty() ) {
			dotty = new TreeToGraphViz<>( tsgExtractor, (t) -> t.getData().nodeKey );
			dotty.addGroup( production );
		}

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
				TreeNode< TSGNode > newProd =
					SampleUtils.getRandomElement( productions );
				if ( ! dottyFile.isEmpty() ) {
					// Make sure that each production is separate in the graph,
					// even if we reuse the same actual production.
					newProd = newProd.deepCopy();
					dotty.addGroup( newProd );
					dotty.addEdge( prod, newProd );
				}
				prod = newProd;
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
		if ( ! dottyFile.isEmpty() )
			try {
				dotty.writeFile( dottyFile );
			} catch ( IOException e ) {
				logger.warn(
					"could not create GraphViz file: " + e.getMessage()
				);
			}
		return root;
	}

	public double getLogProb( ASTNode target )
		throws ParseException
	{
		ASTNode root = GrammarUtils.getStartNode( target );
		ParseState state = parse( root, target );

		Map< TreeNode< Integer >, TreeNote > notes = new IdentityHashMap<>();
		TreeNodeUtils.visit( state.getSubtree(), ( t ) -> {
			notes.put( t, new TreeNote() );
			return TreeNodeUtils.VisitResult.CONTINUE;
		}, null );

		ParseStateBuilder builder = ParseStateBuilder.ruleCollector();
		Probability denom = Probability.FALSE;
		for ( Pair< TreeNode< TSGNode >, Probability > prod
				: state.getProductions() ) {
			ParseState tmp = tryProduction(
				state.getSubtree(), prod.getLeft(), notes
			);
			builder.or( prod.getRight(), tmp );
			denom = denom.or( prod.getRight() );
		}
		
		double numer = builder.build().getProbability().getLog();
		if ( Double.isFinite( numer ) )
			return numer - denom.getLog();
		else
			return posterior.computeLog2PosteriorProbabilityOfRule(
				TSGNode.convertTree( state.getSubtree(), 0 ), false
			);
	}
	
	public ASTNode babbleFrom( ASTNode target, SymbolTable table )
		throws ParseException
	{
		ASTNode root = GrammarUtils.getStartNode( target );
		ParseState state = parse( root, target );
		
		double[] logProbs = new double[ state.getProductions().size() ];
		for ( int i = 0; i < logProbs.length; ++i )
			logProbs[ i ] = state.getProductions().get( i ).getRight().getLog();
		
		// Currently we have no way to avoid babbling code that requires
		// variables of types for which there are no in-scope examples. Instead,
		// we detect the failure after babbling, when decoding into an AST
		// fails. So the following loop repeats until we can successfully decode
		
		for ( int attempt = 1; true; ++attempt ) {
			int i = SampleUtils.getRandomIndex( logProbs );
			TreeNode< TSGNode > prod = state.getProductions().get( i ).getLeft();
			TreeNode< TSGNode > tsgTree = TreeNode.create(
				new TSGNode( prod.getData().nodeKey ), prod.nProperties()
			);
			tsgTree = generateRandom( tsgTree, prod );
			TreeNode< Integer > intTree = TreeNode.create(
				tsgTree.getData().nodeKey, tsgTree.nProperties()
			);
			TSGNode.copyChildren( intTree, tsgTree );
			tsgExtractor.setSymbolTable( table );

			try {
				return tsgExtractor.getASTFromTree( intTree );
			} catch ( CodeGenerationException e ) {
				// ignore -- we'll just try again
				logger.error(
					"babbling attempt " + attempt + " failed: " + e.getMessage()
				);
			}
		}
	}

	public final Map< TSGNode, ? extends Multiset< TreeNode< TSGNode > > > grammar;
	private final ChainedJavaTreeExtractor tsgExtractor;
	private final ITsgPosteriorProbabilityComputer< TSGNode > posterior;

}
