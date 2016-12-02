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
		TreeNode< Integer > tree,
		Function< AstNodeSymbol, Integer > getOrAddSymbolId,
		Function< Integer, AstNodeSymbol > getSymbol,
		SymbolTable st
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
}
