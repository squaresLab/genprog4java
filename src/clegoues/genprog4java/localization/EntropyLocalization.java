package clegoues.genprog4java.localization;

import static clegoues.util.ConfigurationBuilder.STRING;

import java.io.IOException;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.rep.Representation;
import clegoues.genprog4java.rep.UnexpectedCoverageResultException;
import clegoues.util.ConfigurationBuilder;
import codemining.lm.tsg.samplers.CollapsedGibbsSampler;
import codemining.util.serialization.ISerializationStrategy.SerializationException;
import codemining.util.serialization.Serializer;

public class EntropyLocalization extends DefaultLocalization {
	protected Logger logger = Logger.getLogger(EntropyLocalization.class);

	public static final ConfigurationBuilder.RegistryToken token =
			ConfigurationBuilder.getToken();

	public static String languageModel = ConfigurationBuilder.of ( STRING )
			.withVarName("languageModel")
			.withDefault("/Users/clegoues/research/lm-repair/tsg.ser")
			.withHelp("File with serialized language model")
			.inGroup( "EntropyLocalization Parameters" )
			.build();

	private CollapsedGibbsSampler sampler;

	public EntropyLocalization(Representation orig) throws IOException, UnexpectedCoverageResultException {
		super(orig);
	}

	@Override
	protected void computeLocalization() throws UnexpectedCoverageResultException, IOException {
		logger.info("Start Fault Localization");
		TreeSet<Integer> negativePath = getPathInfo(DefaultLocalization.negCoverageFile, Fitness.negativeTests, false);

		for (Integer i : negativePath) {
			faultLocalization.add(original.instantiateLocation(i, 1.0));
			// FIXME: better weighting scheme for entropy?
		}
		try {
			sampler = (CollapsedGibbsSampler) Serializer.getSerializer()
					.deserializeFrom(EntropyLocalization.languageModel);
		} catch (SerializationException e) {
			throw new UnexpectedCoverageResultException("Failure in deserialization in EntropyLocalization, giving up.");
		}
	}

	@Override
	public void reduceSearchSpace() {
		// Does nothing, at least for now.
	}
}
