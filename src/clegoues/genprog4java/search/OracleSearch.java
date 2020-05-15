package clegoues.genprog4java.search;

import static clegoues.util.ConfigurationBuilder.STRING;

import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.mut.EditOperation;
import clegoues.genprog4java.rep.Representation;
import clegoues.util.ConfigurationBuilder;

public class OracleSearch<G extends EditOperation> extends Search<G> {
	public OracleSearch(Fitness engine) {
		super(engine);
	}
	
	public static final ConfigurationBuilder.RegistryToken token =
			ConfigurationBuilder.getToken();
	
	protected static String oracleGenome = ConfigurationBuilder.of( STRING )
			.withVarName( "oracleGenome" )
			.withDefault( "" )
			.withHelp( "oracle genome" )
			.inGroup( "Search Parameters" )
			.build();

	@Override
	protected Population<G> initialize(Representation<G> original, Population<G> incomingPopulation)
			throws RepairFoundException {
		return null;
	}
	@Override
	protected void runAlgorithm(Representation<G> original, Population<G> initialPopulation)
			throws RepairFoundException {
		Representation<G> theRepair = original.copy();
		theRepair.loadGenomeFromString(OracleSearch.oracleGenome);
		assert (fitnessEngine.testToFirstFailure(theRepair, false));
		this.noteSuccess(theRepair, original, 1);		
	}
}
