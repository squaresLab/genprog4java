package clegoues.genprog4java.treelm;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.core.dom.ASTNode;

import codemining.ast.AstNodeSymbol;
import codemining.ast.TreeNode;

public class TreeBinarizer implements
	Supplier< ChainedJavaTreeExtractor.PostProcess >,
	ChainedJavaTreeExtractor.PostProcess,
	Serializable
{
	private static final long serialVersionUID = 20161006L;

	public TreeBinarizer() {
		contextId = new HashMap<>();
	}
	
	@Override
	public TreeBinarizer get() {
		return this;
	}

	@Override
	public TreeNode< Integer > encode(
		TreeNode< Integer > tree, ASTNode node,
		Function< AstNodeSymbol, Integer > getOrAddSymbol
	) {
		TreeNode< Integer > copy =
		TreeNode.create( tree.getData(), tree.nProperties() );
		for ( int i = 0; i < tree.nProperties(); ++i ) {
			List< TreeNode< Integer > > children =
				tree.getChildrenByProperty().get( i );
			TreeNode< Integer > current = TreeNode.create( 0, 0 );
			for ( int j = children.size() - 1; j >=0; --j ) {
				Pair< Integer, Integer > key = Pair.of( tree.getData(), i );
				contextId.putIfAbsent( key, contextId.size() + 10000 );
				int id = contextId.get( key );

				TreeNode< Integer > binary = TreeNode.create( id, 2 );
				binary.addChildNode( children.get( j ), 0 );
				binary.addChildNode( current, 1 );
				current = binary;
			}
			copy.addChildNode( current, i );
		}
		return copy;
	}

	@Override
	public TreeNode< Integer > decode(
		TreeNode< Integer > tree, Function< Integer, AstNodeSymbol > getSymbol
	) {
		TreeNode< Integer > copy =
			TreeNode.create( tree.getData(), tree.nProperties() );
		for ( int i = 0; i < tree.nProperties(); ++i ) {
			TreeNode< Integer > child =
				tree.getChildrenByProperty().get( i ).get( 0 );
			while ( child.getData() != 0 ) {
				copy.addChildNode( child.getChild( 0, 0 ), i );
				child = child.getChild( 0, 1 );
			}
		}
		return copy;
	}
	
	private final Map< Pair< Integer, Integer >, Integer > contextId;
	
	private static String nextToken(
		Deque< String > processed, Deque< String > pending
	) {
		String token = pending.removeFirst();
		processed.addLast( token );
		return token;
	}
	
	private static TreeNode< Integer > buildTree(
		Deque< String > processed, Deque< String > pending
	) {
		String token = nextToken( processed, pending );
		if ( ! token.equals( "(" ) ) {
			int key = Integer.parseInt( token );
			return TreeNode.create( key, 0 );
		}
		
		token = nextToken( processed, pending );
		int key = Integer.parseInt( token );
		List< List< TreeNode< Integer > > > properties = new ArrayList<>();
		while ( true ) {
			// looking for the start of a property or end of this tree...
			token = nextToken( processed, pending );
			if ( token.equals( ")" ) )
				break;
			if ( token.equals( "[" ) ) {
				List< TreeNode< Integer > > children = new ArrayList<>();
				while ( ! token.equals( "]" ) ) {
					children.add( buildTree( processed, pending ) );
					token = pending.getFirst();
				}
				nextToken( processed, pending );
				properties.add( children );
			} else {
				processed.removeLast();
				pending.addFirst( token );
				properties.add(
					Collections.singletonList( buildTree( processed, pending ) )
				);
			}
		}
		TreeNode< Integer > result = TreeNode.create( key, properties.size() );
		for ( int i = 0; i < properties.size(); ++i )
			for ( int j = 0; j < properties.get( i ).size(); ++j )
				result.addChildNode( properties.get( i ).get( j ), i );
		return result;
	}

	private static void printTree( TreeNode< Integer > tree ) {
		StringBuilder indent = new StringBuilder();
		TreeNodeUtils.walk( tree,
			(t) -> {
				System.out.printf( "%s%d\n", indent, t.getData() );
				indent.append( "  " );
			},
			(t) -> {
				indent.delete( indent.length() - 2, indent.length() );
			}
		);
	}
	
	private static void printTokens( Deque< String > tokens ) {
		for ( String token : tokens ) {
			System.err.print( " " );
			System.err.print( token );
		}
		System.err.println();
	}

	public static void main( String[] args ) {
		if ( args.length < 1 ) {
			System.err.println(
				"Usage: java clegoues.genprog4java.treelm.TreeBinarizerFactory tree"
			);
			return;
		}
		
		Deque< String > tokens = new ArrayDeque<>(
			Arrays.asList( args[ 0 ].split( "\\s+"  ) )
		);
		if ( ! tokens.getFirst().equals( "(" ) ) {
			tokens.addFirst( "(" );
			tokens.addLast( ")" );
		}
		Deque< String > processed = new ArrayDeque<>();
		TreeNode< Integer > tree;
		try {
			tree = buildTree( processed, tokens );
		} catch ( NoSuchElementException e ) {
			System.err.print( "ERROR: expected more tokens at end of input:" );
			printTokens( processed );
			System.exit( 1 );
			return;
		} catch ( NumberFormatException e ) {
			System.err.print( "ERROR: expected integer:" );
			printTokens( processed );
			System.exit( 1 );
			return;
		}
		if ( ! tokens.isEmpty() ) {
			System.err.print( "ERROR: unprocessed tokens:" );
			printTokens( tokens );
			System.exit( 1 );
		}

		TreeBinarizer inst = new TreeBinarizer();
		
		printTree( tree );
		System.out.println( "=====" );
		tree = TreeNodeUtils.visit( tree, null, (t) -> {
			return inst.encode( t, null, null );
		} );
		printTree( tree );
		System.out.println( "=====" );
		tree = TreeNodeUtils.visit( tree, (t) -> {
			return inst.decode( t, null );
		} );
		printTree( tree );
	}
}
