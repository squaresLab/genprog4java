package clegoues.genprog4java.treelm;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;

import codemining.ast.AstNodeSymbol;
import codemining.ast.TreeNode;
import codemining.ast.java.AbstractJavaTreeExtractor;

import static codemining.ast.java.AbstractJavaTreeExtractor.JAVA_NODETYPE_CONVERTER;

/**
 * Collects TSG production rules and writes them to a GraphViz file so that
 * they can be plotted as a graph.
 * 
 * @author jonathan
 *
 * @param <T> the data type of the grammar nodes
 */
public class TreeToGraphViz< T > {
	/**
	 * Creates a new graph.
	 * 
	 * @param extractor 
	 * @param reader
	 */
	public TreeToGraphViz(
		AbstractJavaTreeExtractor extractor,
		Function< TreeNode< ? extends T >, Integer > reader
	) {
		this.extractor = extractor;
		this.reader = reader;

		this.ids = new IdentityHashMap<>();
		this.names = new HashMap<>();
		this.edges = new HashMap<>();
		this.groups = new ArrayList<>();
	}
	
	/**
	 * Returns a unique identifier for the node for use in the GraphViz file.
	 *
	 * @param tree the node to get an ID for
	 * 
	 * @return a unique identifier for the node.
	 */
	private String getId( TreeNode< ? extends T > tree ) {
		return ids.computeIfAbsent(
			tree, ( x ) -> String.format( "n%d", ids.size() )
		);
	}
	
	/**
	 * Creates a directed edge between two nodes.
	 * 
	 * @param src the ID for the edge source
	 * @param dst the ID for the edge destination
	 */
	private void makeEdge( String src, String dst ) {
		edges.computeIfAbsent( src, ( x ) -> new HashSet<>() ).add( dst );
	}
	
	/**
	 * Checks whether a directed edge exists between two nodes. Does not check
	 * whether an edge exists in the other direction.
	 * 
	 * @param src the ID for the edge source
	 * @param dst the ID for the edge destination
	 * 
	 * @return {@code treu} if the edge exists.
	 */
	private boolean edgeExists( String src, String dst ) {
		return edges.containsKey( src ) && edges.get( src ).contains( dst );
	}
	
	/**
	 * Creates a new cluster of nodes representing the given tree. Edges in the
	 * graph will go from parents to children.
	 * 
	 * @param tree the tree to create nodes for
	 */
	public void addGroup( TreeNode< ? extends T > tree ) {
		List< String > group = new ArrayList<>();
		Deque< String > path = new ArrayDeque<>();
		TreeNodeUtils.visit( tree, ( t ) -> {
			String id = getId( t );
			group.add( id );

			if ( ! path.isEmpty() )
				makeEdge( path.getFirst(), id );
			path.addFirst( id );

			AstNodeSymbol symbol = extractor.getSymbol( reader.apply( t ) );
			String name;
			if ( symbol.nodeType == AstNodeSymbol.MULTI_NODE ) {
				if ( symbol.hasAnnotation( "property" ) )
					name = "\"(" + symbol.getAnnotation( "property" ) + ")\"";
				else
					name = "\"\"";
			} else if ( symbol.nodeType == AstNodeSymbol.TEMPLATE_NODE )
				name = "TEMPLATE_NODE";
			else if ( symbol.nodeType == AstNodeSymbol.UNK_SYMBOL )
				name = "UNK_SYMBOL";
			else
				name = JAVA_NODETYPE_CONVERTER.apply( symbol.nodeType );
			names.put( id, name );
			
			return TreeNodeUtils.VisitResult.CONTINUE;
		}, ( t ) -> {
			path.removeFirst();
			return TreeNodeUtils.VisitResult.CONTINUE;
		} );
		groups.add( group );
	}
	
	/**
	 * Creates an undirected edge between two nodes. These nodes do not need to
	 * be in the same cluster. By convention, a new production is indicated by
	 * connecting the leaf node of the previous production to the root node of
	 * the new production anchored at that leaf.
	 * 
	 * @param node1 one of the nodes connected by the edge
	 * @param node2 the other node connected by the edge
	 */
	public void addEdge(
		TreeNode< ? extends T > node1, TreeNode< ? extends T > node2
	) {
		String srcId = getId( node1 );
		String dstId = getId( node2 );
		makeEdge( srcId, dstId );
		makeEdge( dstId, srcId );
	}

	/**
	 * Writes the graph to the named file. The written file will be directed
	 * graph suitable as an input to GraphViz.
	 * 
	 * @param filename the name of the file to create
	 * 
	 * @throws IOException if there is a problem writing the file
	 */
	public void writeFile( String filename ) throws IOException {
		try ( FileWriter fw = new FileWriter( filename ) ) {
			Set< Pair< String, String > > biedges = new HashSet<>();

			PrintWriter out = new PrintWriter( fw );
			out.println( "digraph {" );
			for ( int i = 0; i < groups.size(); ++i ) {
				List< String > group = groups.get( i );
				if ( groups.size() > 1 )
					out.printf( "  subgraph cluster_%d {\n", i );
				for ( String id : group ) {
					if ( id.startsWith( "red_" ) )
						out.printf( "    %s [label=%s,fillcolor=red]\n", id, names.get( id ) );
					else
						out.printf( "    %s [label=%s]\n", id, names.get( id ) );
				}
				for ( String src : group ) {
					if ( ! edges.containsKey( src ) )
						continue;
					for ( String dst : edges.get( src ) )
						if ( edgeExists( dst, src ) )
							biedges.add( Pair.of( src, dst ) );
						else
							out.printf( "    %s -> %s\n", src, dst );
				}
				if ( groups.size() > 1 )
					out.println( "  }" );
			}
			for ( Pair< String, String > edge : biedges )
				if ( edge.getLeft().compareTo( edge.getRight() ) < 0 )
					out.printf( "  %s -> %s [dir=none]\n",
						edge.getLeft(), edge.getRight()
					);
			out.println( "}" );
		}
	}

	private final AbstractJavaTreeExtractor extractor;
	private final Function< TreeNode< ? extends T >, Integer > reader;
	private final Map< TreeNode< ? extends T >, String > ids;
	private final Map< String, String > names;
	private final Map< String, Set< String > > edges;
	private final List< List< String > > groups;
}
