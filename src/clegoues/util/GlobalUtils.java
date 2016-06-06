/*
 * Copyright (c) 2014-2015, 
 *  Claire Le Goues     <clegoues@cs.cmu.edu>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. The names of the contributors may not be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package clegoues.util;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jdt.core.dom.ASTNode;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.rep.JavaRepresentation;
import clegoues.genprog4java.rep.Representation;
import clegoues.genprog4java.rep.WeightedAtom;

public class GlobalUtils {
	
	static ArrayList<Pair> alreadyReplaced = new ArrayList<Pair>();

	// range is inclusive!
	public static ArrayList<Integer> range(int start, int end) {
		ArrayList<Integer> returnVal = new ArrayList<Integer>();
		for(int i=start; i<=end; i++) {
			returnVal.add(i);
		}
		return returnVal;

	}

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
		if(counter>=2){
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

	private static int stmtKindOfJavaStmt(JavaStatement stmt){
		ASTNode node = stmt.getASTNode();
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

	public static Pair<?,Double> chooseOneWeighted(ArrayList<Pair<?,Double>> atoms) {
		assert(atoms.size() > 0);
		double totalWeight = 0.0;
		for(Pair<?,Double> atom : atoms) {
			totalWeight += atom.getSecond();
		}
		assert(totalWeight > 0.0) ;
		double wanted = Configuration.randomizer.nextDouble() * totalWeight;
		double sofar = 0.0;
		for(Pair<?,Double> atom : atoms) {
			double here = sofar + atom.getSecond();
			if(here >= wanted) {
				return atom;
			}
			sofar = here;
		}
		return null;
	}

	public static boolean probability(double p) {
		if(p < 0.0) return false;
		if(p > 1.0) return true;
		return Configuration.randomizer.nextDouble() <= p;
	}
}
