package clegoues.genprog4java.treelm;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;

import clegoues.genprog4java.treelm.ChainedJavaTreeExtractor.PostProcess;
import codemining.ast.AstNodeSymbol;
import codemining.ast.TreeNode;
import codemining.math.random.SampleUtils;

public class VariableAbstractor implements
	Supplier< ChainedJavaTreeExtractor.PostProcess >,
	ChainedJavaTreeExtractor.PostProcess,
	Serializable
{
	private static final long serialVersionUID = 20161006L;

	@Override
	public TreeNode< Integer > encode(
		TreeNode< Integer > tree, ASTNode node,
		Function< AstNodeSymbol, Integer > getSymbolId
	) {
		if ( node.getNodeType() != ASTNode.SIMPLE_NAME )
			return tree;
		IBinding binding = ( (SimpleName) node ).resolveBinding();
		if ( binding == null || binding.getKind() != IBinding.VARIABLE )
			return tree;
		
		IVariableBinding var = (IVariableBinding) binding;

		AstNodeSymbol symbol = new AstNodeSymbol( AstNodeSymbol.TEMPLATE_NODE );
		symbol.addAnnotation( "TYPE", var.getType().getQualifiedName() );
		Integer id = getSymbolId.apply( symbol );
		if ( ! varNodes.containsKey( id ) )
			varNodes.put( id, ConcurrentHashMultiset.create() );
		varNodes.get( id ).add( tree );
		
		return TreeNode.create( id, 0 );
	}

	@Override
	public TreeNode< Integer > decode(
		TreeNode< Integer > tree, Function< Integer, AstNodeSymbol > getSymbol
	) {
		Integer id = tree.getData();
		if ( varNodes.containsKey( id ) )
			return SampleUtils.getRandomElement( varNodes.get( id ) );
		else
			return tree;
	}

	@Override
	public PostProcess get() {
		return this;
	}
	
	private final Map< Integer, Multiset< TreeNode< Integer > > > varNodes =
		new ConcurrentHashMap<>();
}
