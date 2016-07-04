package clegoues.genprog4java.mut.holes.java;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.mut.EditHole;

public abstract class JavaHole implements EditHole<ASTNode> {
	private String name;
	private int codeBankId; // for debugging/ease of output, really

	public JavaHole() { } 

	public JavaHole(String name, int id) {
		this.name = name;
		this.codeBankId = id;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public String getName() {
		return this.name;
	}

	public int getCodeBankId() {
		return codeBankId;
	}

	public void setCodeBankId(int codeBankId) {
		this.codeBankId = codeBankId;
	}

	@Override
	public String toString() {
		return ((Integer) this.getCodeBankId()).toString();
	}

	public static List<EditHole> makeSubExpsHoles(Map<ASTNode, List<ASTNode>> entryMap) {
		if(entryMap != null && entryMap.size() > 0) {
			List<EditHole> retVal = new LinkedList<EditHole>();
			for(Map.Entry<ASTNode, List<ASTNode>> entry : entryMap.entrySet()) {
				retVal.add(new SubExpsHole(entry.getKey(), entry.getValue()));
			} 
			return retVal;
		} 
		return null;
	}


	public static List<EditHole> makeExpHole( Map<Expression, List<Expression>> replacableExps, JavaStatement parentStmt) {
		if(replacableExps != null && replacableExps.size() > 0) {
			List<EditHole> retVal = new LinkedList<EditHole>();
			for(Map.Entry<Expression, List<Expression>> exps : replacableExps.entrySet()) {
				for(Expression replacementExp : exps.getValue()) { 
					retVal.add(new ExpHole(exps.getKey(), replacementExp, parentStmt.getStmtId()));

				}
			}
			return retVal;
		}
		return null;
	}

}
