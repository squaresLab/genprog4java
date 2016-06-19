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
<<<<<<< local
=======
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Location;
>>>>>>> other
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

	int replacementModel[][] = {
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

	/*


	double replacementModel[][] = {
	{5.00,5.00,7.48,5.00,3.76,0.53,5.00,5.00,5.00,8.30,23.05,0.31,20.04,5.00,4.90,4.62,1.30,13.50,7.23,0.03,5.00,4.95},
	{5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00},
	{1.00,5.00,5.00,5.00,4.08,0.60,5.00,5.00,5.00,9.93,26.03,0.13,25.39,5.00,2.48,1.57,1.79,8.39,11.73,0.10,5.00,6.77},
	{5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00},
	{1.74,5.00,9.42,5.00,5.00,1.28,5.00,5.00,5.00,11.39,18.25,0.35,22.60,5.00,3.80,2.85,2.17,8.98,9.42,0.11,5.00,7.63},
	{0.81,5.00,5.26,5.00,6.60,5.00,5.00,5.00,5.00,9.44,14.21,0.18,15.86,5.00,3.73,1.67,1.97,5.88,6.39,0.03,5.00,27.98},
	{5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00},
	{5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00},
	{5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00},
	{0.86,5.00,6.28,5.00,3.19,0.79,5.00,5.00,5.00,5.00,22.89,0.09,21.08,5.01,5.00,3.34,1.87,10.01,10.71,0.08,5.00,13.79},
	{1.64,5.00,8.43,5.00,2.87,0.60,5.00,5.00,5.00,13.49,5.00,0.24,26.46,7.45,5.00,4.80,2.85,9.89,15.11,0.08,5.00,6.11},
	{1.30,5.00,8.33,5.00,7.86,1.11,5.00,5.00,5.00,5.18,22.85,5.00,15.17,3.05,5.00,2.04,14.62,10.45,4.16,0.09,5.00,3.79},
	{1.13,5.00,9.41,5.00,3.11,0.49,5.00,5.00,5.00,13.33,27.24,0.24,5.00,5.59,5.00,3.65,2.55,14.91,12.61,0.12,5.00,5.61},
	{5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00},
	{0.78,5.00,2.84,5.00,2.84,0.39,5.00,5.00,5.00,10.27,31.79,0.16,22.40,5.00,5.00,0.46,2.07,7.37,11.69,0.08,5.00,6.87},
	{1.14,5.00,2.72,5.00,3.80,0.55,5.00,5.00,5.00,11.07,34.14,0.13,21.86,5.00,0.75,5.00,1.53,8.65,9.02,0.05,5.00,4.58},
	{0.80,5.00,6.57,5.00,2.28,0.43,5.00,5.00,5.00,10.21,24.18,0.05,19.77,5.00,6.35,2.07,5.00,9.16,12.16,0.04,5.00,5.93},
	{2.11,5.00,6.57,5.00,2.58,0.48,5.00,5.00,5.00,11.87,18.84,0.17,32.28,5.00,4.64,3.30,2.74,5.00,10.08,0.07,5.00,4.27},
	{0.71,5.00,7.41,5.00,3.02,0.66,5.00,5.00,5.00,11.73,27.75,0.11,23.24,5.00,5.63,2.65,2.58,8.99,5.00,0.09,5.00,5.42},
	{0.00,5.00,4.51,5.00,7.52,1.00,5.00,5.00,5.00,10.28,21.05,0.50,17.79,5.00,6.02,1.75,2.01,9.27,11.53,5.00,5.00,6.77},
	{5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00,5.00},
	{0.72,5.00,8.02,5.00,3.82,1.96,5.00,5.00,5.00,23.16,19.78,0.12,16.48,5.00,6.56,3.09,1.64,6.81,7.80,0.04,5.00,5.00}
	};

	 */

	public void populateModel(String path){

		// This will reference one line at a time
		String line = null;

		try {
			// FileReader reads text files in the default encoding.
			FileReader fileReader = new FileReader(path);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			int row = 0;
			while((line = bufferedReader.readLine()) != null) {
				//FIXME: separate the values between the spaces and assign them to the array 
				++row;
				replacementModel[row]=divideLineToAnArray(line);
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
