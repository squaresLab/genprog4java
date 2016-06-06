package clegoues.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

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
		{01,02,03,04,05,06,07,8,9,010,011,012,013,014,015,016,017,18,19,020,021,022}, 
		{101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,118,119,120,121,122}, 
		{201,202,203,204,205,206,207,208,209,210,211,212,213,214,215,216,217,218,219,220,221,222}, 
		{301,302,303,304,305,306,307,308,309,310,311,312,313,314,315,316,317,318,319,320,321,322},
		{401,402,403,404,405,406,407,408,409,410,411,412,413,414,415,416,417,418,419,420,421,422},
		{501,502,503,504,505,506,507,508,509,510,511,512,513,514,515,516,517,518,519,520,521,522},
		{601,602,603,604,605,606,607,608,609,610,611,612,613,614,615,616,617,618,619,620,621,622},
		{701,702,703,704,705,706,707,708,709,710,711,712,713,714,715,716,717,718,719,720,721,722},
		{804,802,803,801,805,806,807,808,809,810,811,812,813,814,815,816,817,818,819,820,821,822},
		{901,902,903,904,905,906,907,908,909,910,911,912,913,914,915,916,917,918,919,920,921,922},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}, 
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}, 
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}, 
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}, 
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}, 
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}, 
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}, 
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}, 
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}, 
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}, 
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}, 
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
	};
	
	public ArrayList<Integer>  getRanking(int rowNumber){
		SortedSet<Integer> rankingValues = new TreeSet<Integer>(Collections.reverseOrder());
		ArrayList<Integer> rankingStmtNumbers = new ArrayList<Integer>();
		for (int i = 0; i < replacementModel[rowNumber].length; i++) {
			//TreeSet sorts the values as they get added. It is a set, so no repeated values
			rankingValues.add(replacementModel[rowNumber][i]);
		}
		for (Integer valueInMatrix : rankingValues) {
			//it goes through all the row, so it doesnt matter if the values in the treeset are not repeated, it will find them even if a repetaed value is only once in the treeset
			for (int i = 0; i < replacementModel[rowNumber].length; i++) {
				if(replacementModel[rowNumber][i]==valueInMatrix){
					rankingStmtNumbers.add(i);
				}
			}
		}
		
		return rankingStmtNumbers;
	}
	

	
	
}
