package clegoues.genprog4java.mut.edits.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.ThrowStatement;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.java.MethodInfo;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Location;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.holes.java.MethodInfoHole;
import clegoues.genprog4java.mut.holes.java.SimpleJavaHole;
import clegoues.genprog4java.mut.holes.java.SubExpsHole;
import clegoues.genprog4java.rep.JavaRepresentation;
import clegoues.genprog4java.rep.WeightedAtom;

@SuppressWarnings("rawtypes")
public class JavaEditFactory {

	private static HashMap<Location, TreeSet<WeightedAtom>> scopeSafeAtomMap = new HashMap<Location, TreeSet<WeightedAtom>>();

	protected Logger logger = Logger.getLogger(JavaEditOperation.class);

	public JavaEditOperation makeEdit(Mutation edit, Location dst, HashMap<String,EditHole> sources) {
		switch(edit) {
		case DELETE: 
			return new JavaDeleteOperation((JavaLocation) dst);
		case OFFBYONE:
			return new OffByOneOperation((JavaLocation) dst, sources);
		case APPEND: return new JavaAppendOperation((JavaLocation) dst, sources);
		case REPLACE: return new JavaReplaceOperation((JavaLocation) dst, sources);
		case SWAP: return new JavaSwapOperation((JavaLocation) dst, sources);
		case PARREP:
			return new MethodParameterReplacer((JavaLocation) dst, sources);
		case FUNREP:
			return new MethodReplacer((JavaLocation) dst, sources);	
		case LBOUNDSET:
			return new LowerBoundSetOperation((JavaLocation) dst, sources);
		case UBOUNDSET:
			return new UpperBoundSetOperation((JavaLocation) dst, sources);
		case RANGECHECK:
			return new RangeCheckOperation((JavaLocation) dst, sources);
		case NULLCHECK:
			return new NullCheckOperation((JavaLocation) dst, sources);
		case CASTCHECK:
			return new ClassCastChecker((JavaLocation) dst, sources);
		default: logger.fatal("unhandled edit template type in JavaEditFactory; this should be impossible (famous last words...)");
		}		return null;
	}

	private TreeSet<WeightedAtom> scopeHelper(Location stmtId, JavaRepresentation variant) {
		if (JavaEditFactory.scopeSafeAtomMap.containsKey(stmtId)) {
			return JavaEditFactory.scopeSafeAtomMap.get(stmtId);
		}

		JavaStatement potentiallyBuggyStmt = (JavaStatement) stmtId.getLocation();
		ASTNode faultAST = potentiallyBuggyStmt.getASTNode();
		TreeSet<WeightedAtom> retVal = new TreeSet<WeightedAtom>();

		for (WeightedAtom potentialFixAtom : variant.getFixSourceAtoms()) {
			int index = potentialFixAtom.getAtom();
			JavaStatement potentialFixStmt = variant.getFromCodeBank(index);
			ASTNode fixAST = potentialFixStmt.getASTNode();

			// I *believe* this is just variable names and doesn't check required
			// types, which are also collected
			// at parse time and thus could be considered here.
			if(!variant.semanticInfo.scopeCheckOK(potentiallyBuggyStmt, potentialFixStmt)) {
				continue;
			}

			//Heuristic: Don’t replace or swap (or append) an stmt with one just like it
			// CLG killed the stmt ID equivalence check because it's possible for a statement 
			// in a location to have been modified previously such that the statement in the code bank is
			// different from what is now at that location.
			// this comes down to our having overloaded statement IDs to mean both location and statement ID
			// which is a problem I keep meaning to solve.
			if(faultAST.equals(fixAST)) {
				continue;
			}

			//Heuristic: Do not insert a return statement on a func whose return type is void
			//Heuristic: Do not insert a return statement in a constructor
			if(fixAST instanceof ReturnStatement){
				if(potentiallyBuggyStmt.parentMethodReturnsVoid() ||
						potentiallyBuggyStmt.isLikelyAConstructor())
					continue;

				//Heuristic: Swapping, Appending or Replacing a return stmt to the middle of a block will make the code after it unreachable
				ASTNode parentBlock = potentiallyBuggyStmt.blockThatContainsThisStatement();
				if(parentBlock != null && parentBlock instanceof Block) {
					List<ASTNode> statementsInBlock = ((Block)parentBlock).statements();
					ASTNode lastStmtInTheBlock = statementsInBlock.get(statementsInBlock.size()-1);
					if(!lastStmtInTheBlock.equals(faultAST)){
						continue;
					}
				} else {
					continue;
				}

				//If we move a return statement into a function, the parameter in the return must match the function’s return type
				ASTNode enclosingMethod = potentiallyBuggyStmt.getEnclosingMethod();

				if (enclosingMethod instanceof MethodDeclaration) {
					String returnType = variant.returnTypeOfThisMethod(((MethodDeclaration)enclosingMethod).getName().toString());
					if(returnType != null){
						ReturnStatement potFix = (ReturnStatement) fixAST;
						if(potFix.getExpression() instanceof SimpleName){
							String variableType = variant.semanticInfo.getVariableDataTypes().get(potFix.getExpression().toString());
							if( !returnType.equalsIgnoreCase(variableType)){
								continue;
							}
						}
					}
				}
			}

			//Heuristic: Inserting methods like this() or super() somewhere that is not the First Stmt in the constructor, is wrong
			if(fixAST instanceof ConstructorInvocation || 
					fixAST instanceof SuperConstructorInvocation){
				ASTNode enclosingMethod = potentiallyBuggyStmt.getEnclosingMethod();

				if (enclosingMethod != null && 
						enclosingMethod instanceof MethodDeclaration && 
						((MethodDeclaration) enclosingMethod).isConstructor()) {
					StructuralPropertyDescriptor locationPotBuggy = faultAST.getLocationInParent();
					List<ASTNode> statementsInBlock = ((MethodDeclaration) enclosingMethod).getBody().statements();
					ASTNode firstStmtInTheBlock = statementsInBlock.get(0);
					StructuralPropertyDescriptor locationFirstInBlock = firstStmtInTheBlock.getLocationInParent();
					//This will catch replacements and swaps, but it will append after the first stmt, so append will still create a non compiling variant
					if(!locationFirstInBlock.equals(locationPotBuggy)){
						continue;
					}
				} else {
					continue;
				}
			}

			//Heuristic: Don't allow to move breaks outside of switch stmts
			// OR loops!
			// TODO: check for continues as well
			if(fixAST instanceof BreakStatement && 
					!potentiallyBuggyStmt.isWithinLoopOrCase()){
				continue;
			}

			//Heuristic: Don't replace/swap returns within functions that have only one return statement
			// (unless the replacer is also a return statement); could also check if it's a block or
			// other sequence of statements with a return within it, but I'm lazy
			if((!(fixAST instanceof ReturnStatement)) && 
					faultAST instanceof ReturnStatement) {
				ASTNode parent = potentiallyBuggyStmt.getEnclosingMethod();
				if(parent instanceof MethodDeclaration && 
						!JavaStatement.hasMoreThanOneReturn((MethodDeclaration)parent)) {
					continue;
				}
			}
			// if we made it this far without continuing, we're good to go.

			retVal.add(potentialFixAtom);
		}
		JavaEditFactory.scopeSafeAtomMap.put(stmtId, retVal);
		return retVal;
	}

	public TreeSet<EditHole> editSources(JavaRepresentation variant, Location location, Mutation editType,
			String holeName) { // I notice that I'm really not using hole name, here, hm...maybe I can get rid of it?
		JavaStatement locationStmt = (JavaStatement) location.getLocation();
		TreeSet<EditHole> retVal = new TreeSet<EditHole>();

		switch(editType) {
		case APPEND: 	
		case REPLACE:
			//If it is a return statement, nothing should be appended after it, since it would be dead code
			if(!(locationStmt.getASTNode() instanceof ReturnStatement || locationStmt.getASTNode() instanceof ThrowStatement )){
				TreeSet<WeightedAtom> fixStmts = this.scopeHelper(location, variant);
				for(WeightedAtom fixStmt : fixStmts) {
					JavaStatement potentialFixStmt = variant.getFromCodeBank(fixStmt.getFirst());
					ASTNode fixAST = potentialFixStmt.getASTNode();
					retVal.add(new SimpleJavaHole(holeName, fixAST, potentialFixStmt.getStmtId()));
				}
			} 
			break;
		case DELETE: 
			break;
		case SWAP:
			for (WeightedAtom item : this.scopeHelper(location, variant)) {
				int atom = item.getAtom();
				TreeSet<WeightedAtom> inScopeThere = this.scopeHelper(variant.instantiateLocation(atom, item.getSecond()), variant);
				for (WeightedAtom there : inScopeThere) {
					if (there.getAtom() == location.getId()) {
						JavaStatement potentialFixStmt = variant.getFromCodeBank(there.getAtom());
						ASTNode fixAST = potentialFixStmt.getASTNode();
						retVal.add(new SimpleJavaHole(holeName, fixAST, potentialFixStmt.getStmtId()));
						break;
					}
				}
			}
			break;
		case LBOUNDSET:
		case UBOUNDSET:
		case RANGECHECK:
			Map<ASTNode, List<ASTNode>> arrayAccesses = locationStmt.getArrayAccesses();
			if(arrayAccesses.size() > 0) {
				for(Map.Entry<ASTNode, List<ASTNode>> entry : arrayAccesses.entrySet()) {
					retVal.add(new SubExpsHole(holeName, entry.getKey(), entry.getValue()));
				} 
			}
			break;
		case OFFBYONE:  
			arrayAccesses = locationStmt.getArrayAccesses();
			if(arrayAccesses.size() > 0) {
				for(Map.Entry<ASTNode, List<ASTNode>> entry : arrayAccesses.entrySet()) {
					for(ASTNode arrayAccess : entry.getValue()) {
						retVal.add(new SimpleJavaHole(holeName, arrayAccess, locationStmt.getStmtId()));
					}
				} 
			}
			break;
		case NULLCHECK:
			Map<ASTNode, List<ASTNode>> nullCheckables = locationStmt.getNullCheckables();
			if(nullCheckables.size() > 0) {
				for(Map.Entry<ASTNode, List<ASTNode>> entry : nullCheckables.entrySet()) {
					ASTNode parent = entry.getKey();
					List<ASTNode> possibleReplacements = entry.getValue();
					retVal.add(new SubExpsHole(holeName,parent, possibleReplacements));
				}
			} 
			break;
		case FUNREP:
			Map<ASTNode, List<ASTNode>> methodReplacements = locationStmt.getReplacableMethods(variant.semanticInfo.getMethodDecls());
			for(Map.Entry<ASTNode,List<ASTNode>> entry : methodReplacements.entrySet()) {
				List<ASTNode> replacableMethods = entry.getValue();
				for(ASTNode replacableMethod : replacableMethods) {
					List<MethodInfo> possibleReplacements = 
							locationStmt.getCandidateMethodReplacements(replacableMethod);
					for(MethodInfo possibleReplacement : possibleReplacements) {
						retVal.add(new MethodInfoHole(holeName,replacableMethod, locationStmt.getStmtId(), possibleReplacement));
					}
				}
			}
			break;
		case PARREP:
			Map<ASTNode, Map<ASTNode, List<ASTNode>>> replacableParameters = locationStmt.getReplacableMethodParameters(variant.semanticInfo);
			for(Map.Entry<ASTNode, Map<ASTNode,List<ASTNode>>> funsite : replacableParameters.entrySet()) {
				for(Map.Entry<ASTNode, List<ASTNode>> exps : funsite.getValue().entrySet()) {
					for(ASTNode replacementExp : exps.getValue()) { // simple hole here is terrible.  
					retVal.add(new SimpleJavaHole(holeName, exps.getKey(), replacementExp, locationStmt.getStmtId()));
					}
				}
			}
			break;
		case CASTCHECK:
			Map<ASTNode, List<ASTNode>> casts = locationStmt.getCasts();
			for(Map.Entry<ASTNode, List<ASTNode>> entry : casts.entrySet()) {
				retVal.add(new SubExpsHole(holeName, entry.getKey(), entry.getValue()));
			}
			break;
		case PARADD:
		case PARREM:
		case EXPREP:
		case EXPADD:
		case EXPREM:
		case OBJINIT:
		case SIZECHECK:
		default:
			// IMPORTANT FIXME FOR MANISH AND MAU: you must add handling here to check legality for templates as you add them.
			// if a template always applies, then you can move the template type to the DELETE case, above.
			// however, I don't think most templates always apply; it doesn't make sense to null check a statement in which nothing
			// could conceivably be null, for example.
			logger.fatal("Unhandled template type in editSources!  Fix code in JavaRepresentation to do this properly.");
			return null;
		}
		return retVal;
	}

	public Boolean doesEditApply(JavaRepresentation variant, Location location, Mutation editType) {
		JavaStatement locationStmt = (JavaStatement) location.getLocation();
		switch(editType) {
		case APPEND: 
		case REPLACE:
		case SWAP: 
			return this.editSources(variant, location,  editType, "singleHole").size() > 0;
		case DELETE: 			
			return locationStmt.canBeDeleted();
		case OFFBYONE:  
		case UBOUNDSET:
		case LBOUNDSET:
		case RANGECHECK:
			return locationStmt.getArrayAccesses().size() > 0;
		case FUNREP: 
			return locationStmt.getReplacableMethods(variant.semanticInfo.getMethodDecls()).size() > 0;
		case PARREP:
			Map<ASTNode, Map<ASTNode, List<ASTNode>>> methodParams = locationStmt.getReplacableMethodParameters(variant.semanticInfo);
			for(Map.Entry<ASTNode, Map<ASTNode, List<ASTNode>>> entry : methodParams.entrySet()) {
				if(!entry.getValue().isEmpty())
					return true;
			}
			return false;
		case NULLCHECK: 
			return locationStmt.getNullCheckables().size() > 0;
		case CASTCHECK:
			return locationStmt.getCasts().size() > 0;
		default:
			logger.fatal("Unhandled edit type in DoesEditApply.  Handle it in JavaRepresentation and try again.");
			break;

		}
		return false;
	}


	public List<String> holesForMutation(Mutation mut) { // this info should probably be static, and it's weird
		// to have it split between this factory and the individual edits (FIXME)
		ArrayList<String> retVal = new ArrayList<String>();
		switch(mut) {
		case APPEND:
		case REPLACE: 
		case SWAP: 
			retVal.add("singleHole"); 
			return retVal;
		case DELETE: return null;
		case NULLCHECK: 
			retVal.add("checkForNull");
			return retVal;
		case UBOUNDSET:
			retVal.add("upperBoundCheck");
			return retVal;
		case LBOUNDSET:
			retVal.add("lowerBoundCheck");
			return retVal;
		case OFFBYONE:
			retVal.add("arrayCheck");
			return retVal;
		case RANGECHECK: 
			retVal.add("rangeCheck");
			return retVal;
		case FUNREP:
			retVal.add("replaceMethod");
			return retVal;
		case PARREP:
			retVal.add("replaceParameter");
			return retVal;
		case CASTCHECK:
			retVal.add("classCast");
			return retVal;
		case PARADD:
		case PARREM:
		case EXPREP:
		case EXPADD:
		case EXPREM: 
		case OBJINIT:
		case SIZECHECK:
			logger.fatal("Unhandled edit type in holesForMutation.  Handle it in JavaEditFactory and try again.");
			return null;
		}
		return null;
	}
}
