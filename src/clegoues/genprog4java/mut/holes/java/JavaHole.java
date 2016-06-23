package clegoues.genprog4java.mut.holes.java;

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
	public int compareTo(EditHole<ASTNode> o) {
		return 1; //I think this doesn't matter?
	}
	
	@Override
	public String toString() {
		return ((Integer) this.getCodeBankId()).toString();
	}

	public static TreeSet<EditHole> makeSubExpsHoles(String holeName, Map<ASTNode, List<ASTNode>> entryMap) {
		if(entryMap != null && entryMap.size() > 0) {
			TreeSet<EditHole> retVal = new TreeSet<EditHole>();
			for(Map.Entry<ASTNode, List<ASTNode>> entry : entryMap.entrySet()) {
				retVal.add(new SubExpsHole(holeName, entry.getKey(), entry.getValue()));
			} 
			return retVal;
		} 
		return null;
	}
	
	// fixme: this is with parent, and one is without
	
	public static TreeSet<EditHole> makeExpHole(String holeName, Map<ASTNode, Map<ASTNode, List<ASTNode>>> replacableExps, JavaStatement parentStmt) {
		if(replacableExps != null && replacableExps.size() > 0) {
			TreeSet<EditHole> retVal = new TreeSet<EditHole>();
		for(Map.Entry<ASTNode, Map<ASTNode,List<ASTNode>>> funsite : replacableExps.entrySet()) {
			for(Map.Entry<ASTNode, List<ASTNode>> exps : funsite.getValue().entrySet()) {
				for(ASTNode replacementExp : exps.getValue()) { 
					retVal.add(new ExpHole(holeName, (Expression) exps.getKey(), (Expression) replacementExp, parentStmt.getStmtId()));
				}
			}
		}
		return retVal;
		}
		return null;
	}
	
}
