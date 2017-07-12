package clegoues.genprog4java.localization;

import static clegoues.util.ConfigurationBuilder.STRING;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import org.eclipse.jdt.core.dom.ASTNode;

import com.google.common.collect.Multiset;

import clegoues.genprog4java.Search.GiveUpException;
import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.java.ASTUtils;
import clegoues.genprog4java.java.JavaLMSymbolTable;
import clegoues.genprog4java.java.JavaSemanticInfo;
import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.mut.holes.java.JavaASTNodeLocation;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.holes.java.JavaStatementLocation;
import clegoues.genprog4java.rep.Representation;
import clegoues.genprog4java.treelm.EclipseTSG;
import clegoues.genprog4java.treelm.EclipseTSG.ParseException;
import clegoues.util.ConfigurationBuilder;
import clegoues.util.GlobalUtils;
import clegoues.util.ConfigurationBuilder.LexicalCast;
import codemining.ast.TreeNode;
import codemining.ast.java.AbstractJavaTreeExtractor;
import codemining.lm.tsg.FormattedTSGrammar;
import codemining.lm.tsg.TSGNode;
import codemining.lm.tsg.TSGrammar;
import codemining.lm.tsg.TreeProbabilityComputer;
import codemining.util.serialization.ISerializationStrategy.SerializationException;
import codemining.util.serialization.Serializer;

public class EntropyLocalization extends DefaultLocalization {
	protected static Logger logger = Logger.getLogger(EntropyLocalization.class);

	public static final ConfigurationBuilder.RegistryToken token =
			ConfigurationBuilder.getToken(EclipseTSG.token);

	public static EclipseTSG babbler = ConfigurationBuilder.of(
			new LexicalCast< EclipseTSG >() {
				public EclipseTSG parse(String value) {
					if ( value.equals( "" ) )
						return null;
					try {
						TSGrammar<TSGNode> model =
								(TSGrammar<TSGNode>) Serializer.getSerializer().deserializeFrom( value );
						return new EclipseTSG( model );
					} catch (SerializationException e) {
						logger.error( e.getMessage() );
						return null;
					}
				}
			}
			)
			.inGroup( "Entropy Parameters" )
			.withFlag( "model" )
			.withVarName( "babbler" )
			.withDefault( "" )
			.withHelp( "model to use for babbling repairs" )
			.build();



	public EntropyLocalization(Representation orig) throws IOException, UnexpectedCoverageResultException {
		super(orig);
	}

	@Override
	protected void computeLocalization() throws UnexpectedCoverageResultException, IOException {
		logger.info("Start Fault Localization");
		TreeSet<Integer> negativePath = getPathInfo(DefaultLocalization.negCoverageFile, Fitness.negativeTests, false);

		for (Integer i : negativePath) {
			faultLocalization.add(original.instantiateLocation(i, 1.0));
		}
	}

	@Override
	public void reduceSearchSpace() {
		// Does nothing, at least for now.
	}

	@Override
	public Location getRandomLocation(double weight) {
		ArrayList fault = new ArrayList(this.getFaultLocalization());
		try {
			TSGrammar<TSGNode> model =
					(TSGrammar<TSGNode>) Serializer.getSerializer().deserializeFrom(Configuration.grammarPath);
			final TreeProbabilityComputer<TSGNode> probabilityComputer = 
					new TreeProbabilityComputer<TSGNode>(model, false, TreeProbabilityComputer.TSGNODE_MATCHER);
			//Creates sorted list of exp. by prob
			TreeMap<Double,ASTNode> rankedFaults = new TreeMap<Double, ASTNode>();
			
			for(int i=0; i<fault.size();i++) {
				JavaLocation locate = (JavaLocation) fault.get(i);
				List<ASTNode> faults = ASTUtils.decomposeASTNode(locate.getCodeElement());
				//Iterates all subtrees. Original tree in included in decomposeASTNode
				for(int sub=0;sub<faults.size();sub++){
					final TreeNode<TSGNode> tsgTree = TSGNode.convertTree(((AbstractJavaTreeExtractor) model.getTreeExtractor()).getTree(faults.get(sub)), 0);
					double prob = probabilityComputer.getLog2ProbabilityOf(tsgTree);
					
					//System.err.println("Subtree: "  + sub);
					//System.err.println("Exp: " + faults.get(sub));
					//System.err.println("Subtree Prob: " + "" + prob);
					
					rankedFaults.put(prob, faults.get(sub));
				}
			}
			return new JavaASTNodeLocation(rankedFaults.firstEntry().getValue());
			
		} catch (SerializationException e) {
			new Throwable().getStackTrace();
		}
		return null;	
	}


	
	// babbles fix code, manipulates it to reference in-scope variables
	public ASTNode babbleFixCode(JavaLocation location, JavaLMSymbolTable info) {
		ASTNode element = location.getCodeElement();
		ASTNode babbled;
		try {
			babbled = babbler.babbleFrom(element, info);
		}
		catch ( ParseException e ) {
			StringWriter message = new StringWriter();
			e.printStackTrace( new PrintWriter( message ) );
			logger.fatal( message.toString() );
			throw new RuntimeException( e );
		}
		System.err.println("babbled:" + babbled.toString());
		return babbled; 
	}

	@Override
	public Location getNextLocation() throws GiveUpException {
		Location ele = super.getNextLocation();
		// FIXME
		return ele;
	}
}
