package clegoues.genprog4java.mut.holes.java;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;

import clegoues.genprog4java.java.ASTUtils;
import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.WeightedHole;

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

	/** note that this assumes that all returned SubExpsHoles should have equal weight
	 * (I wrote it for the bounds/range checking, null checking, cast checking, and off-by-one templates, 
	 * for which this assumption holds) 
	 * @param entryMap
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<WeightedHole> makeSubExpsHoles(Map<ASTNode, List<ASTNode>> entryMap) {
		if(entryMap != null && entryMap.size() > 0) {
			List<WeightedHole> retVal = new LinkedList<WeightedHole>();
			for(Map.Entry<ASTNode, List<ASTNode>> entry : entryMap.entrySet()) {
				SubExpsHole thisHole = new SubExpsHole(entry.getKey(), entry.getValue());
				retVal.add(new WeightedHole(thisHole));
			} 
			return retVal;
		} 
		return null;
	}


	/** this assumes that all returned ExpHoles should be weighted by distance 
	 * (by line number) from the parent Expression; this is true for parameter replacer, 
	 * expression adder, and expression replacer
	 * @param replacableExps
	 * @param parentStmt
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<WeightedHole> makeExpHole( Map<Expression, List<Expression>> replacableExps, JavaStatement parentStmt) {
		if(replacableExps != null && replacableExps.size() > 0) {
			List<WeightedHole> retVal = new LinkedList<WeightedHole>();
			for(Map.Entry<Expression, List<Expression>> exps : replacableExps.entrySet()) {
				Expression toBeReplaced = exps.getKey();
				int locationLineNumber = ASTUtils.getLineNumber(toBeReplaced);
				for(Expression replacementExp : exps.getValue()) { 
					ExpHole thisHole = new ExpHole(toBeReplaced, replacementExp, parentStmt.getStmtId());
					int replacementLineNumber = ASTUtils.getLineNumber(replacementExp);
					int lineDistance = Math.abs(locationLineNumber - replacementLineNumber);
					double weight = lineDistance != 0 ? 1.0 / lineDistance : 1.0;
					retVal.add(new WeightedHole(thisHole, weight));
				}
			}
			return retVal;
		}
		return null;
	}

}
