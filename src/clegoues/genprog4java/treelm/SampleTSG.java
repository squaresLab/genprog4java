package clegoues.genprog4java.treelm;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import codemining.ast.TreeNode;
import codemining.ast.java.AbstractJavaTreeExtractor;
import codemining.ast.java.BinaryJavaAstTreeExtractor;
import codemining.ast.java.JavaAstTreeExtractor;
import codemining.java.tokenizers.JavaTokenizer;
import codemining.lm.tsg.FormattedTSGrammar;
import codemining.lm.tsg.TSGNode;
import codemining.lm.tsg.samplers.blocked.BlockCollapsedGibbsSampler;
import codemining.lm.tsg.samplers.blocked.TreeCorpusFilter;
import codemining.util.serialization.ISerializationStrategy.SerializationException;
import codemining.util.serialization.Serializer;
import clegoues.genprog4java.java.JavaParser;
import clegoues.genprog4java.java.ScopeInfo;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.main.Main;
import clegoues.util.ConfigurationBuilder;
import clegoues.util.ShutdownDelay;

import static clegoues.util.ConfigurationBuilder.BOOLEAN;
import static clegoues.util.ConfigurationBuilder.DOUBLE;
import static clegoues.util.ConfigurationBuilder.INT;
import static clegoues.util.ConfigurationBuilder.PATH;
import static clegoues.util.ConfigurationBuilder.STRING;

public class SampleTSG {
	protected static Logger logger = Logger.getLogger(Main.class);

	private static final ConfigurationBuilder.RegistryToken token =
		ConfigurationBuilder.getToken();
	
	private static String[] sources = ConfigurationBuilder.of( PATH )
		.inGroup( "Training Parameters" )
		.withVarName( "sources" )
		.withDefault( "" )
		.withHelp( "directories containing sources to train on" )
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

	private static BlockCollapsedGibbsSampler getSamplerForSources() {
		AbstractJavaTreeExtractor format = new JavaAstTreeExtractor();
		if ( binarize )
			format = new BinaryJavaAstTreeExtractor( format );
		BlockCollapsedGibbsSampler sampler = new BlockCollapsedGibbsSampler(
			100, concentration,
			new FormattedTSGrammar(format), new FormattedTSGrammar(format)
		);
		
        final TreeCorpusFilter filter = new TreeCorpusFilter(format, 0);
        int nFiles = 0;
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
				JavaParser parser = new JavaParser( new ScopeInfo() );
				parser.parse(
					f.getPath(), Configuration.libs.split( File.pathSeparator )
				);
				CompilationUnit root = parser.getCompilationUnit();
				GrammarUtils.prepareAST( root );
				for ( ASTNode node : GrammarUtils.getForest( root ) ) {
					TreeNode< TSGNode > tree =
						TSGNode.convertTree( format.getTree( node ), 0.9 );
					nNodes += tree.getTreeSize();
					filter.addTree( tree );
				}

				nFiles += 1;
			}
		}
		logger.info(
			"Loaded " + nFiles + " files containing " + nNodes + " nodes"
		);
		for ( TreeNode< TSGNode > tree : filter.getFilteredTrees() )
			sampler.addTree( tree );
		sampler.lockSamplerData();
		return sampler;
	}

	public static void main( String[] args ) {
		ConfigurationBuilder.register( token );
		ConfigurationBuilder.register( Configuration.token );
		ConfigurationBuilder.parseArgs( args );
		
		if ( ( sources.length == 0 ) == checkpoint.isEmpty() ) {
			System.err.println(
				"ERROR: you must provide exactly one of --sources and --checkpoint"
			);
			System.err.println( "sources.length = " + sources.length );
			System.err.println( "checkpoint.isEmpty() = " + checkpoint.isEmpty() );
			return;
		}
		
		BlockCollapsedGibbsSampler sampler;
		if ( checkpoint.isEmpty() ) {
			sampler = getSamplerForSources();
		} else {
			Object deserialized;
			try {
				deserialized = Serializer.getSerializer().deserializeFrom( checkpoint );
			} catch ( SerializationException e ) {
				System.err.println( e.getMessage() );
				return;
			}
			sampler = (BlockCollapsedGibbsSampler) deserialized;
		}
		
		try ( ShutdownDelay delay = new ShutdownDelay( 500000 ) ) {
			int nCompleted = sampler.performSampling( iters );
			FormattedTSGrammar grammar;
			if ( nCompleted < iters ) {
				logger.warn( "Sampling not complete. Outputting sample grammar" );
				grammar = (FormattedTSGrammar) sampler.getSampleGrammar();
			} else {
				logger.info( "Sampling complete. Outputing grammar..." );
				grammar = (FormattedTSGrammar) sampler.getBurnInGrammar();
			}

			try {
				Serializer.getSerializer().serialize( grammar, output );
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
