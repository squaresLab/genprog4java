package clegoues.genprog4java.treelm;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;


import codemining.ast.java.JavaAstTreeExtractor;
import codemining.ast.TreeNode;
import codemining.java.tokenizers.JavaTokenizer;
import codemining.lm.tsg.FormattedTSGrammar;
import codemining.lm.tsg.TSGNode;
import codemining.lm.tsg.samplers.AbstractTSGSampler;
import codemining.lm.tsg.samplers.blocked.JavaFilteredBlockCollapsedGibbsSampler;
import codemining.lm.tsg.samplers.blocked.TreeCorpusFilter;
import codemining.util.serialization.ISerializationStrategy.SerializationException;
import codemining.util.serialization.Serializer;
import clegoues.genprog4java.java.JavaParser;
import clegoues.genprog4java.main.Configuration;
import clegoues.util.ConfigurationBuilder;
import clegoues.util.ShutdownDelay;

import static clegoues.util.ConfigurationBuilder.BOOLEAN;
import static clegoues.util.ConfigurationBuilder.DOUBLE;
import static clegoues.util.ConfigurationBuilder.INT;
import static clegoues.util.ConfigurationBuilder.PATH;
import static clegoues.util.ConfigurationBuilder.STRING;

public class TrainTSG {
	protected static Logger logger = Logger.getLogger(TrainTSG.class);

	private static final ConfigurationBuilder.RegistryToken token =
		ConfigurationBuilder.getToken();
	
	private static String[] sources = ConfigurationBuilder.of( PATH )
		.inGroup( "Training Parameters" )
		.withVarName( "sources" )
		.withDefault( "" )
		.withHelp( "directories containing sources to train on" )
		.build();
	
	private static String makeCheckpoint = ConfigurationBuilder.of( STRING )
		.inGroup( "Training Parameters" )
		.withVarName( "makeCheckpoint" )
		.withFlag( "make-checkpoint" )
		.withDefault( "" )
		.withHelp( "save checkpoint to named file" )
		.build();

	private static String checkpoint = ConfigurationBuilder.of( STRING )
		.inGroup( "Training Parameters" )
		.withVarName( "checkpoint" )
		.withDefault( "" )
		.withHelp( "sampler checkpoint to resume from" )
		.build();
			
	private static String output = ConfigurationBuilder.of( STRING )
		.inGroup( "Training Parameters" )
		.withVarName( "output" )
		.withDefault( "tsg.ser" )
		.withHelp( "file to write learned grammar to" )
		.build();
	
	private static int iters = ConfigurationBuilder.of( INT )
		.inGroup( "Training Parameters" )
		.withVarName( "iters" )
		.withDefault( "1000" )
		.withHelp( "number of training iterations to conduct" )
		.build();
	
	private static double concentration = ConfigurationBuilder.of( DOUBLE )
		.inGroup( "Training Parameters" )
		.withVarName( "concentration" )
		.withFlag( "alpha" )
		.withDefault( "1" )
		.withHelp( "concentration parameter for updating posterior probabilities" )
		.build();

	private static boolean binarize = ConfigurationBuilder.of( BOOLEAN )
		.inGroup( "Training Parameters" )
		.withVarName( "binarize" )
		.withHelp( "reshape extracted tree to be binary tree" )
		.build();

	private static boolean varAbstraction = ConfigurationBuilder.of( BOOLEAN )
		.inGroup( "Training Parameters" )
		.withVarName( "varAbstraction" )
		.withFlag( "variables" )
		.withHelp( "make variables abstract" )
		.build();
	
	// The point of checkpointing is to accelerate a subsequent training run by
	// reusing the results of training earlier. We enable this piecemeal by
	// matching individual trees of the forest: for any trees that match, we
	// can reuse the training data while allowing any new trees to be randomly
	// initialized. Since the integer codes for each tree node must be kept in
	// sync with the tree extractor object, this Checkpoint class manages both
	// the extractor and the forest of trees.
	
	private static class Checkpoint {
		/**
		 * Loads the extractor and forest of trained trees from the given file.
		 * 
		 * @param filename the name of the checkpoint file to read
		 * 
		 * @throws SerializationException if the checkpoint file cannot be read
		 * for any reason.
		 */
		public Checkpoint( String filename ) throws SerializationException {
			AbstractTSGSampler sampler = (AbstractTSGSampler)
				Serializer.getSerializer().deserializeFrom( filename );

			extractor = (JavaAstTreeExtractor)
				sampler.getSampleGrammar().getTreeExtractor();

			corpus = new HashMap<>();
			for ( TreeNode< TSGNode > tree : sampler.getTreeCorpus() ) {
				corpus.computeIfAbsent(
					tree.getTreeSize(), ( i ) -> new ArrayList<>()
				).add( tree );
			}
		}
		
		/**
		 * Represents an empty or missing checkpoint. That is, it creates a new
		 * extractor and maintains an empty corpus of trained trees.
		 */
		public Checkpoint() {
			ChainedJavaTreeExtractor tmp = new ChainedJavaTreeExtractor();
			if ( varAbstraction )
				tmp.addPostProcessFactory( new VariableAbstractor() );
			if ( binarize )
				tmp.addPostProcessFactory( new TreeBinarizer() );
			extractor = tmp;
			
			corpus = Collections.emptyMap();
		}

		/**
		 * Returns the tree extractor for this checkpoint.
		 * 
		 * @return the tree extractor for this checkpoint.
		 */
		public JavaAstTreeExtractor getExtractor() {
			return extractor;
		}
		
		/**
		 * Matches a tree against the corpus of trained trees. Matching is done
		 * structurally and comparing the integer codes at each node. The
		 * boolean value at each node is ignored, since this is what encodes the
		 * result of training. If a match is found, the pre-trained tree is
		 * removed from the corpus and returned instead of the input tree.
		 * Removing the pre-trained tree form the corpus prevents it from
		 * matching more than once in case, for example, a new getter with the
		 * same tree structure was added to the corpus.
		 * 
		 * @param tree the tree to match against the corpus
		 * 
		 * @return the matching tree if a match was found, otherwise the
		 * original tree.
		 */
		public TreeNode< TSGNode > getMatch( TreeNode< TSGNode > tree ) {
			int size = tree.getTreeSize();
			if ( ! corpus.containsKey( size ) ) {
				failure += 1;
				return tree;
			}
			
			List< TreeNode< TSGNode > > forest = corpus.get( size );
			for ( int i = 0 ; i < forest.size(); ++i ) {
				TreeNode< TSGNode > candidate = forest.get( i );
				boolean[] match = { true };
				try {
					TreeNodeUtils.visit2( tree, candidate, ( t, u ) -> {
						if ( t.getData().nodeKey != u.getData().nodeKey ) {
							match[ 0 ] = false;
							return TreeNodeUtils.VisitResult.ABORT;
						}
						return TreeNodeUtils.VisitResult.CONTINUE;
					}, null );
				} catch ( TreeNodeUtils.TreeStructureException e ) {
					match[ 0 ] = false;
				}
				if ( match[ 0 ] ) {
					forest.remove( i );
					success += 1;
					return candidate;
				}
			}
			failure += 1;
			return tree;
		}
		
		public int getSuccesses() {
			return success;
		}
		
		public int getFailures() {
			return failure;
		}
		
		private final JavaAstTreeExtractor extractor;
		private final Map< Integer, List< TreeNode< TSGNode > > > corpus;
		private int success = 0;
		private int failure = 0;
	}

	/**
	 * Parses the source files and initializes a new sampler to train a grammar.
	 * 
	 * @param ckpt the checkpoint containing the correct tree extractor and
	 *             pre-trained corpus
	 * 
	 * @return the new sampler ready for training.
	 */
	private static AbstractTSGSampler getSampler( Checkpoint ckpt ) {
		JavaAstTreeExtractor format = ckpt.getExtractor();
		
        final TreeCorpusFilter filter = new TreeCorpusFilter( format, 0 );
        int nTrees = 0;
        int nNodes = 0;
		for ( String source : sources ) {
			logger.info( "loading sample trees from " + source );
			File sourceFile = new File( source );
			Collection< File > files;
			if ( sourceFile.isDirectory() )
				files = FileUtils.listFiles(
					sourceFile,
					JavaTokenizer.javaCodeFileFilter,
					DirectoryFileFilter.DIRECTORY
				);
			else
				files = Collections.singleton( sourceFile );

			for ( File f : files ) {
				JavaParser parser = new JavaParser( );
				parser.parse(
					f.getPath(), Configuration.libs.split( File.pathSeparator )
				);
				CompilationUnit root = parser.getCompilationUnit();
				GrammarUtils.prepareAST( root );
				for ( ASTNode node : GrammarUtils.getForest( root ) ) {
					TreeNode< TSGNode > tree =
						TSGNode.convertTree( format.getTree( node ), 0.9 );
					filter.addTree( tree );
					nTrees += 1;
					nNodes += tree.getTreeSize();
				}
			}
		}
		logger.info(
			"Loaded " + nTrees + " files containing " + nNodes + " nodes"
		);

		JavaFilteredBlockCollapsedGibbsSampler sampler =
			new JavaFilteredBlockCollapsedGibbsSampler(
				(double)nNodes / (double)nTrees, concentration,
				new FormattedTSGrammar(format), new FormattedTSGrammar(format)
			);

		for ( TreeNode< TSGNode > tree : filter.getFilteredTrees() )
			sampler.addTree( ckpt.getMatch( tree ) );
		sampler.lockSamplerData();
		return sampler;
	}


	public static void main( String[] args ) {
		ConfigurationBuilder.register( token );
		ConfigurationBuilder.register( Configuration.token );
		ConfigurationBuilder.parseArgs( args );
		
		Checkpoint ckpt;
		if ( checkpoint.isEmpty() ) {
			ckpt = new Checkpoint();
		} else {
			try {
				ckpt = new Checkpoint( checkpoint );
			}
			catch ( SerializationException e ) {
				logger.error(
					"failed to deserialize checkpoint: " +
						ExceptionUtils.getFullStackTrace( e )
				);
				System.exit( 1 );
				return;
			}
		}	
		
		AbstractTSGSampler sampler = getSampler( ckpt );
		System.out.printf(
			"INFO: matched %d/%d trees\n",
			ckpt.getSuccesses(),
			ckpt.getSuccesses() + ckpt.getFailures()
		);
		try ( ShutdownDelay delay = new ShutdownDelay( 500000 ) ) {
			int nCompleted = sampler.performSampling( iters );
			if ( nCompleted < iters )
				logger.warn( String.format( 
					"Sampling terminated after %d iterations.", nCompleted
				) );
			FormattedTSGrammar grammar =
				(FormattedTSGrammar) sampler.getSampleGrammar();

			try {
				Serializer.getSerializer().serialize( grammar, output );
				if ( ! makeCheckpoint.isEmpty() ) {
					// Need to serialize the whole sampler, since I was getting
					// deserialization exceptions trying to do anything else...
					Serializer.getSerializer().serialize(
						sampler, makeCheckpoint
					);
				}
			}
			catch ( SerializationException e ) {
				logger.error(
					"failed to serialize grammar: " +
					ExceptionUtils.getFullStackTrace( e )
				);
			}
		}
	}
}
