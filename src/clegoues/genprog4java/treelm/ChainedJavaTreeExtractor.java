package clegoues.genprog4java.treelm;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;

import codemining.ast.AstNodeSymbol;
import codemining.ast.TreeNode;
import codemining.ast.java.JavaAstTreeExtractor;

/**
 * This {@link JavaAstExtractor} allows the resulting tree to be modified by
 * a chain of post-processing strategies. For example, variables and literals
 * might be abstracted by a post-processor or it might annotate nodes with
 * information for later use. In particular, this ensures strategies that do not
 * alter the structure of the tree do not need to "know" about the structure;
 * they can apply to any structure without extra sub-classing.
 * 
 * @author jonathan
 */
public class ChainedJavaTreeExtractor extends JavaAstTreeExtractor {
	private static final long serialVersionUID = 20161202L;

	@SuppressWarnings( "unused" )
	private static final Logger logger =
		Logger.getLogger( ChainedJavaTreeExtractor.class );

	/**
	 * Create a new ChainedJavaTreeExtractor. The basic extractor produces the
	 * same tree as its parent class. Use
	 * {@link #addPostProcessFactory(Supplier)} to modify the tree produced.
	 */
	public ChainedJavaTreeExtractor() {
		this.parent = null;
		this.factories = new ArrayList<>();
		this.startNodes = new HashSet<>();
		this.symbols = EmptySymbolTable.INSTANCE;
	}
	
	/**
	 * Creates a new ChainedJavaTreeExtractor with the same post-processor
	 * factories as the given extractor. The chains are not shared, so
	 * post-processor factories added after calling this constructor affect only
	 * one or the other extractor.
	 * <p>
	 * Note that the set of starting non-terminals are not copied by this
	 * constructor. This allows the new extractor to reflect a grammar with a
	 * different starting non-terminal than its predecessor.
	 * 
	 * @param that
	 */
	public ChainedJavaTreeExtractor( ChainedJavaTreeExtractor that ) {
		super( that.nodeAlphabet );
		this.parent = that;
		this.factories = new ArrayList<>( that.factories );
		this.startNodes = new HashSet<>();
		this.symbols = EmptySymbolTable.INSTANCE;
	}
	
	/**
	 * The interface for post-processing strategies.
	 * <p>
	 * When extracting a tree, the tree from the base extractor (or
	 * post-processors earlier in the chain) is passed to
	 * {@link #encode(TreeNode, ASTNode, Function)}, along with the ASTNode
	 * being processed. The method may modify or completely replace this tree
	 * with a new tree to be passed up the chain. The resulting tree is
	 * processed in post-order; that is, children nodes are processed before
	 * their parent nodes.
	 * </p><p>
	 * In the reverse process, the current integer tree is passed to
	 * {@link #decode(TreeNode, Function)}, which should undo any processing
	 * done by {@code encode()}, so that the resulting tree is valid for the
	 * previous processor in the chain. The integer tree is processed in
	 * pre-order; this is, parent nodes are processed before any children nodes.
	 * 
	 * @author jonathan
	 */
	public static interface PostProcess {
		/**
		 * Transforms the integer tree from an earlier stage of extraction. The
		 * result may be the original tree unmodified, modified, or completely
		 * replaced.
		 * 
		 * @param tree        the tree from the previous stage of processing
		 * @param node        the ASTNode being processed
		 * @param getSymbolId callback to get an integer representation of an
		 *                    AstNodeSymbol from the base extractor.
		 * @param getSymbol   callback to get an AstNodeSymbol for an integer
		 *                       
		 * @return the processed integer tree.
		 */
		public TreeNode< Integer > encode(
			TreeNode< Integer > tree,
			ASTNode node,
			Function< AstNodeSymbol, Integer > getSymbolId,
			Function< Integer, AstNodeSymbol > getSymbol
		);

		/**
		 * Restores the integer tree from an earlier stage of extraction. This
		 * should undo the processing of
		 * {@link #encode(TreeNode, ASTNode, Function)} such that the result of
		 * this method can be handled by the previous processor in the chain.
		 * 
		 * @param tree        the tree that resulted from {@code encode()}ing
		 * @param getSymbolId callback to get an integer representation of an
		 *                    AstNodeSymbol from the base extractor
		 * @param getSymbol   callback to get an AstNodeSymbol for an integer
		 * @param st          a {@link SymbolTable} to query for variable names
		 * 
		 * @return the integer tree with this post-processor's effect undone.
		 */
		public TreeNode< Integer > decode(
			TreeNode< Integer > tree,
			Function< AstNodeSymbol, Integer > getSymbolId,
			Function< Integer, AstNodeSymbol > getSymbol,
			SymbolTable st
		);
	}
	
	/**
	 * Adds a factory that generates {@link PostProcess} strategies to the chain
	 * of strategies. Using factories instead of post-processing strategies
	 * directly allows the use of strategies that are not themselves easily
	 * serializable.
	 * 
	 * @param <T> ensures that the factories can be serialized along with this
	 * extractor.
	 * 
	 * @param factory the factory to add
	 */
	public
	< T extends Supplier< ? extends PostProcess > & Serializable >
	void addPostProcessFactory( T factory ) {
		this.factories.add( factory );
	}

	/**
	 * Sets the {@link SymbolTable} to use when generating an AST. Note that
	 * this {@code SymbolTable} is simply passed to the {@link PostProcess}
	 * strategies when decoding. Thus, the {@code SymbolTable} is only used if
	 * they take advantage of it.
	 * 
	 * @param st the {@code SymbolTable} to use.
	 */
	public void setSymbolTable( SymbolTable st ) {
		symbols = st;
	}

	/**
	 * ASTNode visitor that extracts an integer tree from an ASTNode and passes
	 * the result along the chain of strategies.
	 * 
	 * @author jonathan
	 */
	protected class TreeNodeExtractorAdapter extends TreeNodeExtractor {
		/**
		 * Creates a new TreeNodeExtractorAdapter.
		 * 
		 * @param useComments whether to visit documentation comments
		 */
		public TreeNodeExtractorAdapter( boolean useComments ) {
			super( useComments );
			postProcessors = new ArrayList<>( factories.size() );
			for ( int i = 0; i < factories.size(); ++i )
				postProcessors.add( factories.get( i ).get() );
		}
		
		/**
		 * Applies the post-processors in order to the extracted TreeNode. Each
		 * node is visited in depth-first post-order.
		 * 
		 * @param treeNode the extracted node
		 * @param node     the original ASTNode being processed
		 */
		@Override
		public TreeNode< Integer > postProcessNodeBeforeAdding(
			TreeNode< Integer > treeNode, ASTNode node
		) {
			for ( int i = 0; i < postProcessors.size(); ++i )
				treeNode = postProcessors.get( i ).encode(
					treeNode, node,
					ChainedJavaTreeExtractor.this::getOrAddSymbolId,
					ChainedJavaTreeExtractor.this::getSymbol
				);
			return treeNode;
		}

		private final List< PostProcess > postProcessors;
	}
	
	// Methods inherited from JavaAstTreeExtractor
	
	/**
	 * Converts a tree of integers into an abstract syntax tree.
	 * 
	 * @param tree the tree of integers to convert
	 * 
	 * @return the corresponding abstract syntax tree.
	 * 
	 * @see {@link #getTree(ASTNode)}
	 */
	@Override
	public ASTNode getASTFromTree( TreeNode< Integer > tree ) {
		List< PostProcess > postProcessors = new ArrayList<>();
		for ( Supplier< ? extends PostProcess > factory : factories )
			postProcessors.add( factory.get() );
		tree = TreeNodeUtils.transform( tree, (t) -> {
			for ( int i = postProcessors.size() - 1; i >=0; --i )
				t = postProcessors.get( i ).decode(
					t, this::getOrAddSymbolId, this::getSymbol, symbols
				);
			symbols.enter( getSymbol( t.getData() ).nodeType );
			return t;
		}, (t) -> {
			symbols.leave( getSymbol( t.getData() ).nodeType );
			return t;
		} );
		return super.getASTFromTree( tree );
	}

	/**
	 * Converts an abstract syntax tree into a tree of integers. Does not visit
	 * documentation comments.
	 * 
	 * @param node the abstract syntax tree to convert
	 * 
	 * @return the corresponding tree of integers
	 * 
	 * @see {@link #getASTFromTree(TreeNode)}
	 */
	@Override
	public TreeNode< Integer > getTree( ASTNode node ) {
		return getTree( node, false );
	}
	
	/**
	 * Converts an abstract syntax into a tree of integers.
	 * 
	 * @param node        the abstract syntax tree to convert
	 * @param useComments whether to process documentation comments
	 * 
	 * @return the corresponding tree of integers
	 * 
	 * @see {@link #getASTFromTree(TreeNode)}
	 */
	@Override
	public TreeNode< Integer > getTree( ASTNode node, boolean useComments ) {
		TreeNodeExtractor ex = new TreeNodeExtractorAdapter( useComments );
		ex.extractFromNode( node );
		return ex.computedNodes.get( node );
	}
	
	@Override
	public Map< ASTNode, TreeNode< Integer > > getTreeMap( ASTNode node ) {
		return getTreeMap( node, false );
	}
	
	// Methods inherited from AbstractJavaTreeExtractor
	
	/**
	 * Returns a node representing the start production for trees extracted by
	 * this extractor. If this extractor has been asked to convert ASTs to
	 * integer trees with more than one start production, throws an exception.
	 * 
	 * @return a node representing the start production for trees extracted by
	 * this extractor.
	 * 
	 * @throws NoSuchElementException if no trees have been extracted, or if
	 * more than one start production has been extracted.
	 */
	@Override
	public TreeNode< Integer > getKeyForCompilationUnit() {
		if ( startNodes.size() == 1 )
			return startNodes.iterator().next();
		throw new NoSuchElementException(
			"grammar has " + startNodes.size() + " start nodes"
		);
	}

	/**
	 * Not supported. This method always throws an exception, since the parsing
	 * done by the codemining projects is incompatible with that done by genprog.
	 * 
	 * @param f ignored
	 * 
	 * @throws UnsupportedOperationException
	 */
	@Override
	public TreeNode< Integer > getTree( File f ) {
		throw new UnsupportedOperationException();
	}

	// Methods inherited from AbstractTreeExtractor
	
	/**
	 * Return the ID of the symbol or create a new one. This method dispatches
	 * to the method from the previous extractor in the chain, if any.
	 * 
	 * @param symbol the symbol to get the ID for
	 * 
	 * @return the ID of the symbol.
	 */
	@Override
	public synchronized int getOrAddSymbolId( final AstNodeSymbol symbol ) {
		if ( parent == null )
			return super.getOrAddSymbolId( symbol );
		else
			return parent.getOrAddSymbolId( symbol );
	}
	
	// New methods in this class
	
	/**
	 * Parameterized counterpart to {@link #getTreeMap(ASTNode)}.
	 * 
	 * @param node        the node to get a map for
	 * @param useComments whether to process documentation comments
	 * 
	 * @return a map between Eclipse ASTNodes and TreeNodes.
	 */
	public Map< ASTNode, TreeNode< Integer > > getTreeMap(
		ASTNode node, boolean useComments
	) {
		final TreeNodeExtractor ex =
			new TreeNodeExtractorAdapter( useComments );
		ex.extractFromNode( node );
		TreeNode< Integer > start = ex.computedNodes.get( node );
		if ( start != null )
			startNodes.add( start );
		return ex.computedNodes;
	}

	protected final ChainedJavaTreeExtractor parent;
	protected final List< Supplier< ? extends PostProcess > > factories;
	protected final Set< TreeNode< Integer > > startNodes;
	protected SymbolTable symbols;
}
