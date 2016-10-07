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

public class ChainedJavaTreeExtractor extends JavaAstTreeExtractor {
	private static final long serialVersionUID = 20160923L;

	@SuppressWarnings( "unused" )
	private static final Logger logger =
		Logger.getLogger( ChainedJavaTreeExtractor.class );

	public ChainedJavaTreeExtractor() {
		this.factories = new ArrayList<>();
		this.startNodes = new HashSet<>();
	}
	
	public ChainedJavaTreeExtractor( ChainedJavaTreeExtractor that ) {
		super( that.nodeAlphabet );
		this.factories = new ArrayList<>( that.factories );
		this.startNodes = new HashSet<>();
	}

	public static interface PostProcess {
		public TreeNode< Integer > encode(
			TreeNode< Integer > tree,
			ASTNode node,
			Function< AstNodeSymbol, Integer > getOrAddSymbol
		);

		public TreeNode< Integer > decode(
			TreeNode< Integer > tree,
			Function< Integer, AstNodeSymbol > getSymbol
		);
	}
	
	public
	< T extends Supplier< ? extends PostProcess > & Serializable >
	void addPostProcessFactory( T factory ) {
		this.factories.add( factory );
	}

	protected class TreeNodeExtractorAdapter extends TreeNodeExtractor {
		public TreeNodeExtractorAdapter( boolean useComments ) {
			super( useComments );
			postProcessors = new ArrayList<>( factories.size() );
			for ( int i = 0; i < factories.size(); ++i )
				postProcessors.add( factories.get( i ).get() );
		}
		
		@Override
		public TreeNode< Integer > postProcessNodeBeforeAdding(
			TreeNode< Integer > treeNode, ASTNode node
		) {
			for ( int i = 0; i < postProcessors.size(); ++i )
				treeNode = postProcessors.get( i ).encode(
					treeNode, node,
					ChainedJavaTreeExtractor.this::getOrAddSymbolId
				);
			return treeNode;
		}

		private final List< PostProcess > postProcessors;
	}
	
	// Methods inherited from JavaAstTreeExtractor
	
	@Override
	public ASTNode getASTFromTree( TreeNode< Integer > tree ) {
		List< PostProcess > postProcessors = new ArrayList<>();
		for ( Supplier< ? extends PostProcess > factory : factories )
			postProcessors.add( factory.get() );
		tree = TreeNodeUtils.visit( tree, (t) -> {
			for ( int i = postProcessors.size() - 1; i >=0; --i )
				t = postProcessors.get( i ).decode(
					t, ChainedJavaTreeExtractor.this::getSymbol
				);
			return t;
		} );
		return super.getASTFromTree( tree );
	}

	@Override
	public TreeNode< Integer > getTree( ASTNode node ) {
		return getTree( node, false );
	}
	
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
	
	@Override
	public TreeNode< Integer > getKeyForCompilationUnit() {
		if ( startNodes.size() == 1 )
			return startNodes.iterator().next();
		throw new NoSuchElementException(
			"grammar has " + startNodes.size() + " start nodes"
		);
	}

	@Override
	public TreeNode< Integer > getTree( File f ) {
		throw new UnsupportedOperationException();
	}

	// Methods inherited from AbstractTreeExtractor
	
	// New methods in this class
	
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

	protected final List< Supplier< ? extends PostProcess > > factories;
	protected final Set< TreeNode< Integer > > startNodes;
}
