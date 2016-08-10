package clegoues.genprog4java.treelm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;

import org.apache.log4j.Logger;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.SimpleName;

import codemining.ast.AstNodeSymbol;
import codemining.ast.TreeNode;
import codemining.ast.java.VariableTypeJavaTreeExtractor;

/**
 * A JavaAstTreeExtractor that abstracts variable names with just their types.
 * This extractor requires a symbol table to retrieve in-scope names while
 * generating an AST from an extracted (or randomly generated) tree.
 * 
 * @author jonathan
 */
public class RepairableJavaTreeExtractor extends VariableTypeJavaTreeExtractor {
	protected static Logger logger =
		Logger.getLogger(RepairableJavaTreeExtractor.class);

	private static final long serialVersionUID = 20160802L;
	
	/**
	 * We distinguish between names that are uses of existing variables and
	 * names that are newly introduced into the scope.
	 */
	public static final String TEMPLETIZED_VAR_DECL_PROPERTY = "DECLARATION";

	
	/**
	 * Stupid shadow implementation of TreeNode to handle data futures. The
	 * TreeNode class requires all data to be serializable, but, of course,
	 * Suppliers are not. So we use this class to build the correct tree
	 * structure before realizing all of the futures. We only need a few methods
	 * of the TreeNode class during instantiation, so that is all we implement
	 * here.
	 * 
	 * @author jonathan
	 *
	 * @param <T> the type of data to manage
	 */
	private static class IntermediateTree< T extends Serializable > {
		/**
		 * Creates a new IntermediateTree with the given future data.
		 * 
		 * @param data        the future data to store at this node
		 * @param nProperties the number of children properties at this node
		 */
		public IntermediateTree( Supplier< T > data, int nProperties ) {
			this.data = data;
			this.properties = new ArrayList<>( nProperties );
			for ( int i = 0; i < nProperties; ++i )
				this.properties.add( new ArrayList<>() );
		}
		
		/**
		 * Creates a new IntermediateTree with the given data available now.
		 * 
		 * @param data        the data to store at this node
		 * @param nProperties the number of children properties at this node
		 */
		public IntermediateTree( final T data, int nProperties ) {
			this( () -> data, nProperties );
		}
		
		/**
		 * Adds a new child to this node. Since the child data is not actually
		 * available yet, children instantiated this way cannot have descendents.
		 * 
		 * @param childData the future data to store in the child
		 * @param i         the property to add the child to
		 */
		public void addChildNode( Supplier< T > childData, int i ) {
			properties.get( i ).add(
				new IntermediateTree< T >( childData, 0 )
			);
		}

		/**
		 * Adds a new child to this node.
		 * 
		 * @param child the new child to add
		 * @param i     the property to add the child to
		 */
		public void addChildNode( IntermediateTree< T > child, int i ) {
			properties.get( i ).add( child );
		}

		/**
		 * Returns the TreeNode tree containing all realized data.
		 * 
		 * @return the TreeNode tree containing all realized data.
		 */
		public TreeNode< T > instantiate() {
			TreeNode< T > root = TreeNode.create(
				data.get(), properties.size()
			);
			for ( int i = 0; i < properties.size(); ++i ) {
				for ( IntermediateTree< T > child : properties.get( i ) )
					root.addChildNode( child.instantiate(), i );
			}
			return root;
		}

		private final Supplier< T > data;
		private final List< List< IntermediateTree< T > > > properties;
	}

	/**
	 * Extracts a TreeNode with abstracted variables from a parsed AST. Retains
	 * information on the type of variables and whether they are new introduced
	 * names so that appropriate names may be instantiated when converting back
	 * to an AST.
	 * 
	 * @author jonathan
	 */
	public class RepairableTreeExtractor extends VariableTypeTreeExtractor {
		/**
		 * Creates a new RepairableTreeExtractor for the given AST.
		 * 
		 * @param extracted   the AST to extract a tree from
		 * @param useComments whether to include comments in the extracted tree
		 */
		public RepairableTreeExtractor(
			final ASTNode extracted, final boolean useComments
		) {
			super( extracted, useComments );
		}
		
		/**
		 * Returns the abstracted form of variable nodes.
		 * 
		 * @param node     the AST node being processed
		 * @param treeNode the non-abstracted tree node
		 */
		@Override
		protected TreeNode< Integer > getTempletizedSubtreeForNode(
			final SimpleName node, final TreeNode< Integer > treeNode
		) {
			TreeNode< Integer > result =
				super.getTempletizedSubtreeForNode( node, treeNode);
			if ( result == treeNode )
				return result;

			// it was a variable
			AstNodeSymbol newSymbol = constructTypeSymbol(
				symtab.getFullyQualifiedTypeName( node.getFullyQualifiedName() )
			);
			newSymbol.addAnnotation(
				TEMPLETIZED_VAR_DECL_PROPERTY, node.isDeclaration()
			);
			return TreeNode.create( getOrAddSymbolId( newSymbol ), 0 );
		}
	}

	/**
	 * Creates a new RepairableJavaTreeExtractor. Uses the given symbol table
	 * to retrieve or instantiate variables when necessary.
	 * 
	 * @param table the table tracking in-scope variables
	 */
	public RepairableJavaTreeExtractor( SymbolTable table ) {
		this.symtab = table;
	}
	
	/**
	 * Identifies templatized tree nodes.
	 * 
	 * @param tree the TreeNode to check
	 * 
	 * @return <i>true</i> if the tree is templatized.
	 */
	private boolean isTemplate( TreeNode< Integer > tree ) {
		int nodeType = getSymbol( tree.getData() ).nodeType;
		return nodeType == AstNodeSymbol.TEMPLATE_NODE;
	}

	/**
	 * Removes any template symbols in the tree, instantiating variables from
	 * the symbol table when necessary.
	 * 
	 * @param fromTree the tree to detemplatize
	 */
	@Override
	public TreeNode<Integer> detempletize( final TreeNode<Integer> fromTree ) {
		if ( isTemplate( fromTree ) )
			return detempletize( fromTree.getChild( 0, 0 ) );

		IntermediateTree< Integer > toTree =
			new IntermediateTree<>( fromTree.getData(), fromTree.nProperties() );
		detempletize( fromTree, toTree );
		return toTree.instantiate();
	}
	
	/**
	 * Removes template nodes in the tree, replacing variable nodes with
	 * variable futures.
	 * 
	 * @param fromTree the tree to detemplatize
	 * @param toTree   the tree to insert detemplatized nodes into
	 */
	private void detempletize(
		final TreeNode< Integer > fromTree,
		final IntermediateTree< Integer > toTree
	) {
		symtab.enter( getSymbol( fromTree.getData() ).nodeType );
		final List< List< TreeNode< Integer > > > children =
			fromTree.getChildrenByProperty();
		for ( int i = 0; i < children.size(); ++i ) {
			final List< TreeNode< Integer > > childrenForProperty =
				children.get( i );
			for ( final TreeNode< Integer > fromChild : childrenForProperty ) {
				if ( isTemplate( fromChild ) ) {
					TreeNode< Integer > templateChild = fromChild;
					while ( !templateChild.isLeaf() && isTemplate( templateChild ) ) {
						checkArgument( templateChild.nProperties() == 1 );
						templateChild = templateChild.getChild( 0, 0 );
					}
					if ( templateChild.isLeaf() && isTemplate( templateChild ) ) {
						// this should be a variable node....
						AstNodeSymbol symbol = getSymbol( fromChild.getData() );
						String type = (String)
							symbol.getAnnotation( TEMPLETIZED_VAR_TYPE_PROPERTY );
						boolean is_decl = (Boolean)
							symbol.getAnnotation( TEMPLETIZED_VAR_DECL_PROPERTY );
						
						final Supplier< String > name;
						if ( is_decl )
							name = symtab.allocFreeName( type );
						else
							name = symtab.getNameForType( type );
						
						toTree.addChildNode(
							() -> {
								AST ast = AST.newAST( AST.JLS8 );
								ASTNode v = ast.newSimpleName( name.get() );
								return getTree( v ).getData();
							},
						i );
						continue;
					}
					final IntermediateTree< Integer > untemplatizedCopyChild =
						new IntermediateTree<>(
							templateChild.getData(), templateChild.nProperties()
						);
					toTree.addChildNode( untemplatizedCopyChild, i );
				} else {
					final IntermediateTree< Integer > toChild =
						new IntermediateTree<>(
							fromChild.getData(), fromChild.nProperties()
						);
					toTree.addChildNode( toChild, i );
					detempletize( fromChild, toChild );
				}
			}
		}
		symtab.leave( getSymbol( fromTree.getData() ).nodeType );
	}

	/**
	 * Constructs a new node extractor. This method may be overridden by
	 * subclasses so that {@link #getTree(ASTNode)} and
	 * {@link #getTreeMap(ASTNode)} do not need to be.
	 * 
	 * @param node        the AST node to extract
	 * @param useComments whether to include comments in the extracted tree
	 * 
	 * @return an extractor for the given tree.
	 */
	protected TreeNodeExtractor getNodeExtractor(
		final ASTNode node, final boolean useComments
	) {
		return new RepairableTreeExtractor( node, useComments );
	}
	
	/**
	 * Extracts a language model tree for the given AST.
	 * 
	 * @param node        the AST node to extract a tree from
	 * @param useComments whether to include comments in the extracted tree
	 */
	@Override
	public TreeNode< Integer > getTree(
		final ASTNode node, final boolean useComments
	) {
		final TreeNodeExtractor ex = getNodeExtractor( node, useComments );
		ex.extractFromNode( node );
		return ex.computedNodes.get( node );
	}
	
	/**
	 * Returns a map between AST nodes and the corresponding language model
	 * tree nodes.
	 * 
	 * @param node the root of the AST nodes to get a mapping for
	 */
	@Override
	public Map< ASTNode, TreeNode< Integer > > getTreeMap( final ASTNode node ) {
		final TreeNodeExtractor ex = getNodeExtractor( node, false );
		ex.extractFromNode( node );
		return ex.computedNodes;
	}

	private SymbolTable symtab;
}
