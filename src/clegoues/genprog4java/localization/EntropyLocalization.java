package clegoues.genprog4java.localization;

import static clegoues.util.ConfigurationBuilder.STRING;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import org.eclipse.jdt.core.dom.ASTNode;

import clegoues.genprog4java.Search.GiveUpException;
import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.java.ASTUtils;
import clegoues.genprog4java.java.JavaLMSymbolTable;
import clegoues.genprog4java.java.JavaSemanticInfo;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.mut.holes.java.JavaASTNodeLocation;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.rep.Representation;
import clegoues.genprog4java.treelm.EclipseTSG;
import clegoues.genprog4java.treelm.EclipseTSG.ParseException;
import clegoues.util.ConfigurationBuilder;
import clegoues.util.GlobalUtils;
import clegoues.util.ConfigurationBuilder.LexicalCast;
import codemining.lm.tsg.FormattedTSGrammar;
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
						FormattedTSGrammar grammar =
								(FormattedTSGrammar) Serializer.getSerializer().deserializeFrom( value );
						return new EclipseTSG( grammar );
					} catch (SerializationException e) {
						logger.error( e.getMessage() );
						return null;
					}
				}
			}
			)
			.inGroup( "Entropy Parameters" )
			.withFlag( "grammar" )
			.withVarName( "babbler" )
			.withDefault( "" )
			.withHelp( "grammar to use for babbling repairs" )
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
		JavaLocation startingStmt = (JavaLocation) GlobalUtils.chooseOneWeighted(new ArrayList(this.getFaultLocalization()), weight);
		ASTNode actualCode = startingStmt.getCodeElement();
		List<ASTNode> decomposed = ASTUtils.decomposeASTNode(actualCode);
		decomposed.add(actualCode);
		Collections.shuffle(decomposed, Configuration.randomizer);
		ASTNode selected = decomposed.get(0);
		System.err.println("SELECTED: " + selected);
		return new JavaASTNodeLocation(startingStmt, selected.getParent());
		//		double maxProb = Double.NEGATIVE_INFINITY;
		//		ASTNode biggestSoFar = actualCode;
		//		for(ASTNode node : decomposed) {
		//			TreeNode< TSGNode > asTlm = babbler.eclipseToTreeLm(node);
		//			double prob = babbler.grammar.computeRulePosteriorLog2Probability(asTlm);
		//			double entropy = -prob * Math.exp(prob);
		//			System.err.println(node);
		//			System.err.println(prob);
		//			System.err.println("entropy:" + entropy);
		//			System.err.println();
		//
		//			if(prob > maxProb) {
		//				maxProb = prob;
		//				biggestSoFar = node;
		//			}
		//		}
		//
		//		System.err.println("biggest found:");
		//		System.err.println(biggestSoFar);
		//		System.err.println(maxProb);
		//
		//		return new JavaASTNodeLocation(biggestSoFar.getParent());
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
