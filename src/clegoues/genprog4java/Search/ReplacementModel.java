package clegoues.genprog4java.Search;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import org.eclipse.jdt.core.dom.ASTNode;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Location;
import clegoues.genprog4java.mut.WeightedHole;
import clegoues.genprog4java.rep.JavaRepresentation;
import clegoues.genprog4java.rep.Representation;
import clegoues.util.Pair;

public class ReplacementModel {

	final int AssertStatement = 0;
	final int Block = 1;
	final int BreakStatement = 2;
	final int ConstructorInvocation = 3;
	final int ContinueStatement = 4;
	final int DoStatement = 5;
	final int EmptyStatement = 6;
	final int EnhancedForStatement = 7;
	final int ExpressionStatement = 8;
	final int ForStatement = 9;
	final int IfStatement = 10;
	final int LabeledStatement = 11;
	final int ReturnStatement = 12;
	final int SuperConstructorInvocation = 13;
	final int SwitchCase = 14;
	final int SwitchStatement = 15;
	final int SynchronizedStatement = 16;
	final int ThrowStatement = 17;
	final int TryStatement = 18;
	final int TypeDeclarationStatement = 19;
	final int VariableDeclarationStatement = 20;
	final int WhileStatement = 21;
/*
	double replacementModel[][] = {
			{0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0}, 
			{0,0,1,0,0,0,0,0,10,0,27,0,16,0,0,0,0,0,0,0,7,0}, 
			{0,1,0,0,0,0,0,0,15,0,0,0,0,0,2,0,0,0,0,0,0,0}, 
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, 
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, 
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, 
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, 
			{0,0,0,0,0,0,0,0,2,1,4,0,0,0,0,0,2,0,1,0,4,0}, 
			{0,8,21,0,0,0,0,0,0,1,71,0,5,0,0,0,1,0,9,0,42,0}, 
			{0,0,0,0,0,0,0,1,2,0,2,0,0,0,0,0,0,0,0,0,1,0}, 
			{0,16,1,0,0,0,0,0,12,0,0,0,0,0,0,0,3,0,3,0,7,0}, 
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, 
			{0,4,0,0,0,0,0,0,3,0,0,0,0,0,0,0,0,0,3,0,2,0}, 
			{0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0}, 
			{0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, 
			{0,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0}, 
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, 
			{0,0,1,0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0}, 
			{0,0,0,0,0,1,0,0,0,0,3,0,0,0,0,0,1,0,0,0,0,0}, 
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, 
			{0,2,0,0,0,0,0,0,44,1,16,0,1,0,0,1,0,1,0,0,0,0}, 
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}
	};
	*/
	
	double replacementModel[][] = {
	{4.343682998, 15.77066624, 2.69991265, 1.842293338, 0.00794092, 0.214404828, 0.158818391, 4.343682998, 18.69292464, 4.105455412, 14.0077821, 0.111172874, 10.00555864, 0.00794092, 1.675534027, 1.516715636, 0.651155404, 7.972683237, 2.795203685, 3.096958628, 4.343682998, 1.635829429},
	{0.785569564, 4.347578259, 4.992719491, 1.526056969, 0.000518186, 0.220228934, 0.482430913, 4.347578259, 26.48861805, 5.555469191, 9.238733347, 0.115037232, 15.88601987, 0.000518186, 5.295858141, 3.310688617, 1.224472876, 5.473595846, 4.396805903, 0.036791187, 4.347578259, 1.927132723},
   {0.681862459, 16.33622563, 4.347435148, 1.879242908, 0.001498599, 0.263753391, 0.867688711, 4.347435148, 15.28720646, 5.63023573, 13.63125478, 0.134873893, 15.79223426, 0.001498599, 1.155419683, 0.681862459, 1.0070584, 4.680124084, 5.484871645, 0.026974779, 4.347435148, 3.413808089},
   {0.667152221, 11.2629279, 4.032046613, 9.718863802, 0.002913328, 0.643845594, 0.471959213, 9.718863802, 12.82738529, 5.561544064, 9.01383831, 0.247632921, 10.15586307, 0.002913328, 2.249089585, 1.849963583, 0.678805535, 3.574654042, 4.349599417, 0.087399854, 9.718863802, 3.163874727},
   {4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545},
   {0.568383659, 12.23801066, 3.641207815, 3.037300178, 0.017761989, 4.3339254, 0.834813499, 4.3339254, 12.77087034, 8.419182948, 11.50976909, 0.213143872, 9.644760213, 0.017761989, 1.88277087, 1.63410302, 0.781527531, 4.031971581, 5.772646536, 0.071047957, 4.3339254, 9.911190053},
   {0.204282364, 15.04880079, 5.409699629, 1.868805326, 0.007566013, 0.461526822, 4.34289173, 4.34289173, 16.55443747, 7.997276235, 10.25951426, 0.075660135, 7.013694484, 0.007566013, 4.108345313, 4.199137474, 0.817129454, 3.389574033, 7.845955966, 0.007566013, 4.34289173, 1.694787017},
   {4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545},
   {0.622257489, 20.96617768, 2.930544535, 1.151261131, 0.000339105, 0.176673652, 0.392005263, 4.347663906, 4.347663906, 6.004530441, 16.02847125, 0.085793539, 20.77966998, 0.000339105, 2.904433457, 1.575142255, 0.907783814, 6.511492265, 4.489070649, 0.029502126, 4.347663906, 1.401520546},
   {0.474111627, 12.88832559, 3.160118293, 1.423273717, 0.000938835, 0.29667183, 0.924752382, 4.347744449, 17.77120593, 4.347744449, 13.65723138, 0.052574755, 14.01117214, 0.000938835, 2.620288222, 1.738722246, 0.980143642, 5.296906539, 5.441487114, 0.038492231, 4.347744449, 6.179411351},
   {0.708861272, 10.99543942, 4.45945536, 1.228625456, 0.000505607, 0.240669019, 0.367576423, 4.347716172, 23.63258537, 6.928335238, 4.347716172, 0.124884974, 15.41545742, 0.000505607, 4.36794046, 2.887017019, 1.249860958, 4.530240366, 6.933896917, 0.042471003, 4.347716172, 2.842523587},
   {1.196473552, 13.7279597, 2.770780856, 2.896725441, 0.062972292, 0.377833753, 0.377833753, 4.345088161, 13.85390428, 3.526448363, 16.05793451, 4.345088161, 17.00251889, 0.062972292, 2.770780856, 1.385390428, 0.314861461, 5.037783375, 3.337531486, 0.125944584, 4.345088161, 2.078085642},
   {0.572765674, 15.16395946, 4.042250278, 1.413824374, 0.000469865, 0.249498419, 0.25137788, 4.347662656, 25.47750051, 5.929698769, 13.40807322, 0.077057892, 4.347662656, 0.000469865, 2.860539311, 1.718297021, 0.94959756, 7.210551293, 5.396871638, 0.033360429, 4.347662656, 2.200848577},
   {4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545},
   {0.568962884, 18.47983809, 1.601878723, 0.96609134, 0.001909271, 0.238658928, 0.975637697, 4.347411028, 16.41400642, 5.731632809, 15.34863296, 0.063005957, 11.95394837, 0.001909271, 4.347411028, 0.240568199, 1.239117153, 4.211852757, 5.315411639, 0.045822514, 4.347411028, 3.558881931},
   {0.867444589, 19.04673705, 1.645366426, 1.169969747, 0.003086991, 0.172871519, 1.725628203, 4.346483917, 14.8206458, 6.164721862, 15.97518059, 0.067913811, 12.6257949, 0.003086991, 0.388960919, 4.346483917, 0.654442181, 4.849663518, 4.047045749, 0.02160894, 4.346483917, 2.710378465},
   {0.354403122, 16.25768247, 2.718585976, 1.296487372, 0.004486115, 0.296083621, 0.726750707, 4.347045893, 15.62962631, 5.03342156, 13.7723745, 0.708806245, 12.79888744, 0.004486115, 2.521196896, 1.148445561, 4.347045893, 5.845408461, 5.715311112, 0.035888924, 4.347045893, 2.09052981},
   {0.83763544,  14.26122799, 2.901626298, 1.219213582, 0.001020262, 0.200991695, 0.273430326, 4.347338135, 22.10908646, 6.114432632, 10.88007836, 0.115289652, 17.02613912, 0.001020262, 2.042565348, 1.475299447, 0.954965617, 4.347338135, 4.533025894, 0.037749709, 4.347338135, 1.973187504},
   {0.447869059, 12.93629063, 4.047107682, 1.276426819, 0.001017884, 0.217827224, 0.923220993, 4.347383529, 16.35434586, 6.528709425, 16.58540558, 0.04580479, 14.37150738, 0.001017884, 3.23483607, 1.535987297, 1.265230093, 4.880754863, 4.347383529, 0.046822674, 4.347383529, 2.257667213},
   {0.267737617, 12.4497992, 4.6854083, 2.008032129, 0.133868809, 0.133868809, 0.267737617, 4.283801874, 14.0562249, 6.55957162, 11.11111111, 0.133868809, 18.34002677, 0.133868809, 2.81124498, 1.204819277, 0.535475234, 4.6854083, 6.157965194, 4.283801874, 4.283801874, 1.472556894},
   {4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545, 4.545454545},
   {0.571862829, 11.00218486, 4.360216586, 1.930274532, 0.001899877, 1.780184288, 0.47876888, 4.34691745, 11.47905386, 15.68728033, 12.51448656, 0.077894937, 11.93122447, 0.001899877, 3.545169564, 1.455305405, 1.151325164, 3.852949558, 5.085969412, 0.051296666, 4.34691745, 4.34691745}
	};


	

	public void populateModel(String path){

		// This will reference one line at a time
		String line = null;

		try {
			// FileReader reads text files in the default encoding.
			FileReader fileReader = new FileReader(path);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			int row = 0;
			while((line = bufferedReader.readLine()) != null) {
				String[] tmp =line.split(" ");
				for (int i = 0; i < tmp.length; i++) {
					replacementModel[row][i]=Double.valueOf(tmp[i]);
				}
				++row;
			}   

			// Always close files.
			bufferedReader.close();         
		}
		catch(FileNotFoundException ex) {
			System.out.println(
					"Unable to open file '" + 
							path + "'");                
		}
		catch(IOException ex) {
			System.out.println(
					"Error reading file '" 
							+ path + "'");                  
			// Or we could just do this: 
			// ex.printStackTrace();
		}

	}

	@SuppressWarnings("rawtypes")
	public TreeSet<Pair<?,Double>> rescaleBasedOnModel(ArrayList<Pair<?,Double>> atoms, Representation<?> variant, int stmtIdBuggy) {
		assert(atoms.size() > 0);

		TreeSet retVal = new TreeSet();
		HashMap<Integer, JavaStatement> codeBank = ((JavaRepresentation)variant).getCodeBank();

		JavaStatement buggyStmt = codeBank.get(stmtIdBuggy);
		ASTNode buggyAstNode = buggyStmt.getASTNode();
		int row = stmtKindOfJavaStmt(buggyAstNode);

		for(Pair<?,Double> atom: atoms){
			ASTNode fixStmt = ((EditHole<ASTNode>)((WeightedHole)atom).getFirst()).getCode();
			int column = stmtKindOfJavaStmt(fixStmt);
			atom.setSecond(Double.valueOf(replacementModel[row][column]+1));
			retVal.add(atom);

		}
		return retVal;
	}



	private static int stmtKindOfJavaStmt(ASTNode node){
		int retVal = -1;
		switch(node.getNodeType()){
		case ASTNode.ASSERT_STATEMENT: retVal=0; break;
		case ASTNode.BLOCK: retVal=1; break;
		case ASTNode.BREAK_STATEMENT: retVal=2; break;
		case ASTNode.CONSTRUCTOR_INVOCATION: retVal=3; break;
		case ASTNode.CONTINUE_STATEMENT: retVal=4; break;
		case ASTNode.DO_STATEMENT: retVal=5; break;
		case ASTNode.EMPTY_STATEMENT: retVal=6; break;
		case ASTNode.ENHANCED_FOR_STATEMENT: retVal=7; break;
		case ASTNode.EXPRESSION_STATEMENT: retVal=8; break;
		case ASTNode.FOR_STATEMENT: retVal=9; break;
		case ASTNode.IF_STATEMENT: retVal=10; break;
		case ASTNode.LABELED_STATEMENT: retVal=11; break;
		case ASTNode.RETURN_STATEMENT: retVal=12; break;
		case ASTNode.SUPER_CONSTRUCTOR_INVOCATION: retVal=13; break;
		case ASTNode.SWITCH_CASE: retVal=14; break;
		case ASTNode.SWITCH_STATEMENT: retVal=15; break;
		case ASTNode.SYNCHRONIZED_STATEMENT: retVal=16; break;
		case ASTNode.THROW_STATEMENT: retVal=17; break;
		case ASTNode.TRY_STATEMENT: retVal=18; break;
		case ASTNode.TYPE_DECLARATION_STATEMENT: retVal=19; break;
		case ASTNode.VARIABLE_DECLARATION_STATEMENT: retVal=20; break;
		case ASTNode.WHILE_STATEMENT: retVal=21; break;

		}
		return retVal;
	}



	/*
	 * static ArrayList<Pair> alreadyReplaced = new ArrayList<Pair>();
	public static int chooseReplacementBasedOnPredictingModel(ArrayList<Pair<?,Double>> atoms, Representation<?> variant, int stmtIdBuggy) {
		assert(atoms.size() > 0);
		ReplacementModel rm = new ReplacementModel();
		HashMap<Integer, JavaStatement> codeBank = ((JavaRepresentation)variant).getCodeBank();

		JavaStatement buggyStmt = codeBank.get(stmtIdBuggy);
		int stmtKindOfBuggyStmt = stmtKindOfJavaStmt(buggyStmt);
		ArrayList<Integer> ranking = rm.getRanking(stmtKindOfBuggyStmt);

		ArrayList<Pair<Integer,Integer>> possibleFixStmts = new ArrayList<Pair<Integer,Integer>>();
		for( Pair<?,Double> atom : atoms){
			JavaStatement possibleFixStmt = codeBank.get(atom.getFirst());
			int stmtKindOfPossibleFixStmt = stmtKindOfJavaStmt(possibleFixStmt);
			//this array stores the Id of this statement in the codeBank, and the kind of stmt
			possibleFixStmts.add(new Pair(atom.getFirst(),stmtKindOfPossibleFixStmt));
		}

		//ranking should always have 22 values
		for(int r :ranking){
			for(int i = 0; i < possibleFixStmts.size(); ++i){
				if(possibleFixStmts.get(i).getSecond()==r){
					if(!alreadyReplacedContains(stmtKindOfBuggyStmt,possibleFixStmts.get(i).getSecond())){
						alreadyReplaced.add(new Pair<Integer,Integer>(stmtKindOfBuggyStmt,possibleFixStmts.get(i).getSecond()));
						//If it has already picked the top five guesses for a certain buggyStmtKind, then remove the guesses for that buggyStmtKind and start over 
						if(alreadyPickedTopFiveGuessesFor(stmtKindOfBuggyStmt)){
							wipeOutAllRecordedGuessesFor(stmtKindOfBuggyStmt);
						}
						return possibleFixStmts.get(i).getFirst();

					}
				}
			}
		}

		//If it can't find any then return one at random 
		Pair<?,Double> lastResource = chooseOneWeighted(atoms);
		WeightedAtom wa = (WeightedAtom)lastResource;
		return wa.getAtom();
	}

	private static boolean alreadyPickedTopFiveGuessesFor(int buggyStmtKind){
		int counter=0;
		for (Pair<Integer,Integer> p: alreadyReplaced) {
			if(p.getFirst()==buggyStmtKind){
				counter++;
			}			
		}
		if(counter>=5){
			return true;
		}else{
			return false;
		}
	}

	private static boolean alreadyReplacedContains(int replacee, int replacer){
		for (Pair<Integer,Integer> p: alreadyReplaced) {
			if(p.getFirst()==replacee && p.getSecond()==replacer){
				return true;
			}			
		}
		return false;
	}

	private static void wipeOutAllRecordedGuessesFor(int buggyStmtKind){
		for (int j = 0; j < alreadyReplaced.size(); j++) {
			Pair<Integer,Integer> p = alreadyReplaced.get(j);
			if(p.getFirst()==buggyStmtKind){
				alreadyReplaced.remove(p);
				j--;
			}			
		}
	}
	 */


	/*
	public ArrayList<Integer>  getRanking(int rowNumber){
		SortedSet<Integer> rankingValues = new TreeSet<Integer>(Collections.reverseOrder());
		ArrayList<Integer> rankingStmtNumbers = new ArrayList<Integer>();
		for (int i = 0; i < replacementModel[rowNumber].length; i++) {
			//TreeSet sorts the values as they get added. It is a set, so no repeated values
			rankingValues.add(replacementModel[rowNumber][i]);
		}

		//In case a row has a lot of 0, then instead of giving priority to the stmt kinds for their alphabetical name, we will give them priority regarding their historical frequency of replacing others as pointed in "A Deeper Look into Bug Fixes: Patterns, Replacements, Deletions, and Additions"
		int bestReplacersHistorically[] = {12,10,8,7,9,18,17,1,2,21,14,15,4,16,0,5,11,19,20,13,3,6};
		for (Integer valueInMatrix : rankingValues) {
			//it goes through all the row, so it doesnt matter if the values in the treeset are not repeated, it will find them even if a repetaed value is only once in the treeset
			for (int i : bestReplacersHistorically) {
				if(replacementModel[rowNumber][i]==valueInMatrix){
					rankingStmtNumbers.add(i);
				}
			}
		}

		return rankingStmtNumbers;
	}
	 */




}
