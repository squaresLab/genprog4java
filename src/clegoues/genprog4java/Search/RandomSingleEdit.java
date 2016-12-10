package clegoues.genprog4java.Search;

import static clegoues.util.ConfigurationBuilder.INT;

import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.localization.Localization;
import clegoues.genprog4java.mut.EditOperation;
import clegoues.genprog4java.rep.Representation;
import clegoues.util.ConfigurationBuilder;

public class RandomSingleEdit<G extends EditOperation> extends Search<G>{


	public static final ConfigurationBuilder.RegistryToken token =
			ConfigurationBuilder.getToken();

	protected static int maxVariants = ConfigurationBuilder.of( INT )
			.withVarName( "maxVariants" )
			.withDefault( "50" )
			.withHelp( "maximum number of variants to consider" )
			.inGroup( "Search Parameters" )
			.build();

	public RandomSingleEdit(Fitness engine) {
		super(engine);
		engine.initializeModel();
	}

	@Override
	protected Population<G> initialize(Representation<G> original, Population<G> incomingPopulation)
			throws RepairFoundException, GiveUpException {
		original.getLocalization().reduceSearchSpace();
		return null;
	}

	@Override
	protected void runAlgorithm(Representation<G> original, Population<G> initialPopulation)
			throws RepairFoundException, GiveUpException {
		int numVariantsConsidered = 0;
		while(numVariantsConsidered < RandomSingleEdit.maxVariants) {
			Representation<G> variant = original.copy();
			mutate(variant);
			if (fitnessEngine.testToFirstFailure(variant, true)) { 
				this.noteSuccess(variant, original, 0);
				if(!continueSearch) 
					return;
			}
			numVariantsConsidered++;
		}
	}



}
