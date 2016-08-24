package clegoues.genprog4java.localization;

import static clegoues.util.ConfigurationBuilder.BOOL_ARG;
import static clegoues.util.ConfigurationBuilder.STRING;

import java.io.IOException;
import java.util.ArrayList;

import clegoues.genprog4java.Search.GiveUpException;
import clegoues.genprog4java.rep.UnexpectedCoverageResultException;
import clegoues.genprog4java.rep.WeightedAtom;
import clegoues.util.ConfigurationBuilder;

public abstract class Localization {

	
	public static final ConfigurationBuilder.RegistryToken token =
			ConfigurationBuilder.getToken();

	public static String fixStrategy = ConfigurationBuilder.of ( STRING )
			.withVarName("fixStrategy")
			.withHelp("Fix source strategy")
			.withDefault("classScope")
			.inGroup( "Localization Parameters" )
			.build();

	public static String faultStrategy = ConfigurationBuilder.of ( STRING )
			.withVarName("faultStrategy")
			.withHelp("Fault localization strategy")
			.withDefault("classScope")
			.inGroup( "Localization Parameters" )
			.build();
	
	public static boolean justTestingFaultLoc = ConfigurationBuilder.of( BOOL_ARG )
			.withVarName( "justTestingFaultLoc" )
			.withDefault( "false" )
			.withHelp( "boolean to be turned true if the purpose is to test that fault loc is performed correctly" )
			.inGroup( "FaultLocRepresentation Parameters" )
			.build();
	
	public abstract ArrayList<Location> getFaultLocalization();
	public abstract void reduceSearchSpace() throws GiveUpException;
	public abstract Location getRandomLocation(double weight);
	public abstract Location getNextLocation() throws GiveUpException;
	
	public abstract ArrayList<WeightedAtom> getFixSourceAtoms();
	public abstract void setAllPossibleStmtsToFixLocalization();

	protected abstract void computeLocalization() throws IOException, UnexpectedCoverageResultException;
}
