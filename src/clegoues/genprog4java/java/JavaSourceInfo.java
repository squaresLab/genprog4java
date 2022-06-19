package clegoues.genprog4java.java;

import clegoues.genprog4java.mut.holes.java.JavaLocation;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.util.ArrayList;
import java.util.HashMap;

public class JavaSourceInfo {

	private static HashMap<ClassInfo, String> originalSource = new HashMap<ClassInfo, String>();
	private static HashMap<Integer, JavaStatement> codeBank = new HashMap<Integer, JavaStatement>();
	private static HashMap<Integer, JavaStatement> base = new HashMap<Integer, JavaStatement>();
	private static HashMap<ClassInfo, CompilationUnit> baseCompilationUnits = new HashMap<ClassInfo, CompilationUnit>();
	
	private static HashMap<ClassInfo, HashMap<Integer, ArrayList<Integer>>> lineNoToAtomIDMap = new HashMap<>();
	private static HashMap<Integer, ClassInfo> stmtToFile = new HashMap<Integer, ClassInfo>();
	
	private static HashMap<Integer,JavaLocation> locationInformation = new HashMap<Integer,JavaLocation>();

	public HashMap<ClassInfo, String> getOriginalSource() {
		return originalSource;
	}
	public void setOriginalSource(HashMap<ClassInfo, String> originalSource) {
		JavaSourceInfo.originalSource = originalSource;
	}
	public HashMap<Integer, JavaStatement> getCodeBank() {
		return codeBank;
	}
	public void setCodeBank(HashMap<Integer, JavaStatement> codeBank) {
		JavaSourceInfo.codeBank = codeBank;
	}
	public HashMap<Integer, JavaStatement> getBase() {
		return base;
	}
	public void setBase(HashMap<Integer, JavaStatement> base) {
		JavaSourceInfo.base = base;
	}
	public HashMap<ClassInfo, CompilationUnit> getBaseCompilationUnits() {
		return baseCompilationUnits;
	}
	public void setBaseCompilationUnits(HashMap<ClassInfo, CompilationUnit> baseCompilationUnits) {
		JavaSourceInfo.baseCompilationUnits = baseCompilationUnits;
	}
	public void addToOriginalSource(ClassInfo pair, String source) {
		JavaSourceInfo.originalSource.put(pair, source);
	}
	public void addToBaseCompilationUnits(ClassInfo pair, CompilationUnit compilationUnit) {
		JavaSourceInfo.baseCompilationUnits.put(pair, compilationUnit);
	}
	public void addToBase(int stmtId, JavaStatement s) {
		JavaSourceInfo.base.put(stmtId, s);
	}
	public void addToCodeBank(int stmtId, JavaStatement s) {
		JavaSourceInfo.codeBank.put(stmtId, s);
	}
	
	
	public ArrayList<Integer> atomIDofSourceLine(ClassInfo cls, int lineno) {
		return lineNoToAtomIDMap.get(cls).get(lineno);
	}
	public void augmentLineInfo(ClassInfo cls, int atomId, ASTNode node) {
		HashMap<Integer, ArrayList<Integer>> clsMap = lineNoToAtomIDMap.get(cls);
		if (clsMap == null) {
			clsMap = new HashMap<>();
			lineNoToAtomIDMap.put(cls, clsMap);
		}
		int lineNo = ASTUtils.getLineNumber(node);
		ArrayList<Integer> lineNoList = null;
		if (clsMap.containsKey(lineNo)) {
			lineNoList = clsMap.get(lineNo);
		} else {
			lineNoList = new ArrayList<Integer>();
		}
		lineNoList.add(atomId);
		clsMap.put(lineNo, lineNoList);
	}
	
	public void storeStmtInfo(JavaStatement s, ClassInfo pair) {
		this.addToBase(s.getStmtId(), s);
		this.addToCodeBank(s.getStmtId(), s);
		stmtToFile.put(s.getStmtId(),pair);		
	}
	
	public ClassInfo getFileFromStmt(int location) {
		return JavaSourceInfo.stmtToFile.get(location);
	}
	public HashMap<Integer,JavaLocation> getLocationInformation() {
		return locationInformation;
	}
	public void setLocationInformation(HashMap<Integer,JavaLocation> locationInformation) {
		JavaSourceInfo.locationInformation = locationInformation;
	}
}
