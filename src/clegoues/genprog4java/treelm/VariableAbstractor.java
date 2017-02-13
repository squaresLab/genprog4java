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

public class VariableAbstractor implements
	Supplier< ChainedJavaTreeExtractor.PostProcess >,
	ChainedJavaTreeExtractor.PostProcess,
	Serializable
{
	private static final long serialVersionUID = 20161006L;

	@Override
	public TreeNode< Integer > encode(
		TreeNode< Integer > tree, ASTNode node,
		Function< AstNodeSymbol, Integer > getSymbolId,
		Function< Integer, AstNodeSymbol > getSymbol
	) {
		if ( node.getNodeType() != ASTNode.SIMPLE_NAME )
			return tree;
		IBinding binding = ( (SimpleName) node ).resolveBinding();
		if ( binding == null || binding.getKind() != IBinding.VARIABLE )
			return tree;
		
		IVariableBinding var = (IVariableBinding) binding;
		String typeName = var.getType().getQualifiedName();

		AstNodeSymbol symbol = new AstNodeSymbol( AstNodeSymbol.TEMPLATE_NODE );
		symbol.addAnnotation( "TYPE", typeName );
		Integer id = getSymbolId.apply( symbol );
		varNodes.putIfAbsent( id, ConcurrentHashMultiset.create() );
		varNodes.get( id ).add( tree );
		
		return TreeNode.create( id, 0 );
	}

	@Override
	public TreeNode< Integer > decode(
		TreeNode< Integer > tree,
		Function< AstNodeSymbol, Integer > getOrAddSymbolId,
		Function< Integer, AstNodeSymbol > getSymbol,
		SymbolTable symbols
	) {
		Integer id = tree.getData();
		if ( varNodes.containsKey( id ) ) {
			String type = (String) getSymbol.apply( id ).getAnnotation("TYPE");
			String name = symbols.getNameForType( type ).get();
			AstNodeSymbol symbol = new AstNodeSymbol( ASTNode.SIMPLE_NAME );
			symbol.addSimpleProperty(
				SimpleName.IDENTIFIER_PROPERTY.getId(), name
			);
			Integer varId = getOrAddSymbolId.apply( symbol );

			return TreeNode.create( varId, 0 );
		} else
			return tree;
	}

	@Override
	public PostProcess get() {
		return this;
	}
	
	private final Map< Integer, Multiset< TreeNode< Integer > > > varNodes =
		new ConcurrentHashMap<>();
}
