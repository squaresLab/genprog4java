package clegoues.genprog4java.localization;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;

import clegoues.genprog4java.treelm.CompleteLM;
import codemining.ast.TreeNode;
import codemining.ast.java.AbstractJavaTreeExtractor;
import codemining.lm.tsg.TSGNode;
import codemining.lm.tsg.TSGrammar;

public class TreeBabbler {
	private static Logger logger = Logger.getLogger( TreeBabbler.class );

	public TreeBabbler( TSGrammar< TSGNode > grammar ) {
		this.grammar = new CompleteLM( grammar );
		try {
			this.extractor = (AbstractJavaTreeExtractor) grammar.getTreeExtractor();
		} catch ( ClassCastException e ) {
			throw new IllegalArgumentException(
				"grammar must have a java tree extractor", e
			);
		}
	}
	
	public ASTNode babbleFrom( ASTNode root ) {
		logger.debug( "Babbling to replace code:\n" + root.toString() );
		
		TreeNode< TSGNode > tsgTree = eclipseToTreeLm( root );
		ASTNode result = treeLmToEclipse( grammar.generateRandom( tsgTree ) );

		logger.debug( "Replacement :\n" + result.toString() );
		return result;
	}
	
	public TreeNode< TSGNode > eclipseToTreeLm( ASTNode node ) {
		TreeNode< Integer > intTree = extractor.getTree( node );
		return TreeNode.create(
			new TSGNode( intTree.getData() ), intTree.nProperties()
		);
	}
	
	private ASTNode treeLmToEclipse( TreeNode< TSGNode > tree ) {
		TreeNode< Integer > intTree = TreeNode.create(
			tree.getData().nodeKey, tree.nProperties()
		);
		TSGNode.copyChildren( intTree,  tree );
		return extractor.getASTFromTree( intTree );
	}

	private final AbstractJavaTreeExtractor extractor;
	public final CompleteLM grammar;
}
