package clegoues.genprog4java.Search;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.jdt.core.dom.ASTNode;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.localization.Location;
import clegoues.genprog4java.mut.EditHole;
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

	double replacementModel[][] = new double[22][22];
	

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
	public List<Pair<?,Double>> rescaleBasedOnModel(ArrayList<Pair<?,Double>> atoms, Representation<?> variant, int stmtIdBuggy) {
		assert(atoms.size() > 0);

		List retVal = new LinkedList();
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
