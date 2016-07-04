package clegoues.genprog4java.localization;

import static clegoues.util.ConfigurationBuilder.STRING;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;

import clegoues.genprog4java.Search.GiveUpException;
import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.java.ASTUtils;
import clegoues.genprog4java.mut.Location;
import clegoues.genprog4java.mut.holes.java.JavaASTNodeLocation;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.rep.Representation;
import clegoues.genprog4java.rep.UnexpectedCoverageResultException;
import clegoues.util.ConfigurationBuilder;
import clegoues.util.GlobalUtils;
import clegoues.util.ConfigurationBuilder.LexicalCast;
import codemining.ast.TreeNode;
import codemining.lm.tsg.FormattedTSGrammar;
import codemining.lm.tsg.TSGNode;
import codemining.lm.tsg.samplers.CollapsedGibbsSampler;
import codemining.util.serialization.ISerializationStrategy.SerializationException;
import codemining.util.serialization.Serializer;

public class EntropyLocalization extends DefaultLocalization {
	protected static Logger logger = Logger.getLogger(EntropyLocalization.class);

	public static final ConfigurationBuilder.RegistryToken token =
			ConfigurationBuilder.getToken();

	public static TreeBabbler babbler = ConfigurationBuilder.of(
			new LexicalCast< TreeBabbler >() {
				public TreeBabbler parse(String value) {
					if ( value.equals( "" ) )
						return null;
					try {
						FormattedTSGrammar grammar =
							(FormattedTSGrammar) Serializer.getSerializer().deserializeFrom( value );
						return new TreeBabbler( grammar );
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
		double maxProb = Double.NEGATIVE_INFINITY;
		ASTNode biggestSoFar = actualCode;
		for(ASTNode node : decomposed) {
			TreeNode< TSGNode > asTlm = babbler.eclipseToTreeLm(node);
			double prob = babbler.grammar.computeRulePosteriorLog2Probability(asTlm);
			double entropy = -prob * Math.exp(prob);
			System.err.println(node);
			System.err.println(prob);
			System.err.println("entropy:" + entropy);
			System.err.println();
			
			if(prob > maxProb) {
				maxProb = prob;
				biggestSoFar = node;
			}
		}
		
		System.err.println("biggest found:");
		System.err.println(biggestSoFar);
		System.err.println(maxProb);
		return new JavaASTNodeLocation(biggestSoFar);
	}
	
	@Override
	public Location getNextLocation() throws GiveUpException {
		Location ele = super.getNextLocation();
		// FIXME
		return ele;
	}
}
