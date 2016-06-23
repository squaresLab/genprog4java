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
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.ThrowStatement;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Location;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.ExpChoiceHole;
import clegoues.genprog4java.mut.holes.java.ExpHole;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.holes.java.MethodInfoHole;
import clegoues.genprog4java.mut.holes.java.StatementHole;
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
		case EXPREP:
			return new ExpressionModRep((JavaLocation) dst, sources);
		case PARADD:
			return new MethodParameterAdder((JavaLocation) dst, sources);
		case EXPADD: 
			return new ExpressionModAdd((JavaLocation) dst, sources);
		case EXPREM: 
			return new ExpressionModRem((JavaLocation) dst, sources);
		case PARREM:
			return new MethodParameterRemover((JavaLocation) dst, sources);
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

	private TreeSet<EditHole> makeSubExpsHoles(String holeName, Map<ASTNode, List<ASTNode>> entryMap) {
		if(entryMap != null && entryMap.size() > 0) {
			TreeSet<EditHole> retVal = new TreeSet<EditHole>();
			for(Map.Entry<ASTNode, List<ASTNode>> entry : entryMap.entrySet()) {
				retVal.add(new SubExpsHole(holeName, entry.getKey(), entry.getValue()));
			} 
			return retVal;
		} 
		return null;
	}
	
	private TreeSet<EditHole> makeExpHole(String holeName, Map<ASTNode, Map<ASTNode, List<ASTNode>>> replacableExps, JavaStatement parentStmt) {
		if(replacableExps != null && replacableExps.size() > 0) {
			TreeSet<EditHole> retVal = new TreeSet<EditHole>();
		for(Map.Entry<ASTNode, Map<ASTNode,List<ASTNode>>> funsite : replacableExps.entrySet()) {
			for(Map.Entry<ASTNode, List<ASTNode>> exps : funsite.getValue().entrySet()) {
				for(ASTNode replacementExp : exps.getValue()) { 
					retVal.add(new ExpHole(holeName, exps.getKey(), (Expression) replacementExp, parentStmt.getStmtId()));
				}
			}
		}
		return retVal;
		}
		return null;
	}
	public TreeSet<EditHole> editSources(JavaRepresentation variant, Location location, Mutation editType,
			String holeName) { // I notice that I'm really not using hole name, here, hm...maybe I can get rid of it?
		JavaStatement locationStmt = (JavaStatement) location.getLocation();
		TreeSet<EditHole> retVal = new TreeSet<EditHole>();

		switch(editType) {
		case DELETE: 
			break;
		case APPEND: 	
		case REPLACE:
				TreeSet<WeightedAtom> fixStmts = this.scopeHelper(location, variant);
				for(WeightedAtom fixStmt : fixStmts) {
					JavaStatement potentialFixStmt = variant.getFromCodeBank(fixStmt.getFirst());
					ASTNode fixAST = potentialFixStmt.getASTNode();
					retVal.add(new StatementHole(holeName, (Statement) fixAST, potentialFixStmt.getStmtId()));
				}
			break;
		case SWAP:
			for (WeightedAtom item : this.scopeHelper(location, variant)) {
				int atom = item.getAtom();
				TreeSet<WeightedAtom> inScopeThere = this.scopeHelper(variant.instantiateLocation(atom, item.getSecond()), variant);
				for (WeightedAtom there : inScopeThere) {
					if (there.getAtom() == location.getId()) {
						JavaStatement potentialFixStmt = variant.getFromCodeBank(there.getAtom());
						ASTNode fixAST = potentialFixStmt.getASTNode();
						retVal.add(new StatementHole(holeName, (Statement) fixAST, potentialFixStmt.getStmtId()));
						break;
					}
				}
			}
			break;
		case LBOUNDSET:
		case UBOUNDSET:
		case RANGECHECK:
		case OFFBYONE:  
			return makeSubExpsHoles(holeName, locationStmt.getArrayAccesses()); 
		case NULLCHECK:
			return makeSubExpsHoles(holeName, locationStmt.getNullCheckables());
		case FUNREP:
			Map<ASTNode, List<IMethodBinding>> methodReplacements = locationStmt.getCandidateMethodReplacements();
			for(Map.Entry<ASTNode,List<IMethodBinding>> entry : methodReplacements.entrySet()) {
				ASTNode replacableMethod = entry.getKey();
				List<IMethodBinding> possibleReplacements = entry.getValue();
					for(IMethodBinding possibleReplacement : possibleReplacements) {
						retVal.add(new MethodInfoHole(holeName,replacableMethod, locationStmt.getStmtId(), possibleReplacement));
					}
				}
			break;
		case PARREP:
			return makeExpHole(holeName, locationStmt.getReplacableMethodParameters(variant.semanticInfo), locationStmt);
		case CASTCHECK:
			return makeSubExpsHoles(holeName, locationStmt.getCasts());
		case EXPREP:
			return makeExpHole(holeName, locationStmt.getConditionalExpressions(variant.semanticInfo), locationStmt);
		case EXPADD:
			Map<ASTNode, Map<ASTNode,List<ASTNode>>> extendableExpressions = locationStmt.getConditionalExpressions(variant.semanticInfo);
			for(Map.Entry<ASTNode, Map<ASTNode,List<ASTNode>>> parents : extendableExpressions.entrySet()) {
				for(Map.Entry<ASTNode,List<ASTNode>> entries : parents.getValue().entrySet()) { 
					for(ASTNode exp : entries.getValue()) {
					EditHole shrinkableExpHole1 = new ExpChoiceHole(holeName, entries.getKey(), (Expression) exp, locationStmt.getStmtId(), 0);
					EditHole shrinkableExpHole2 = new ExpChoiceHole(holeName, entries.getKey(), (Expression) exp, locationStmt.getStmtId(), 1);
					retVal.add(shrinkableExpHole1);
					retVal.add(shrinkableExpHole2);
					}
				}
			}
			return retVal;
		case EXPREM:
			Map<ASTNode, List<ASTNode>> shrinkableExpressions = locationStmt.getShrinkableConditionalExpressions();
			for(Map.Entry<ASTNode, List<ASTNode>> entries : shrinkableExpressions.entrySet()) {
				for(ASTNode exp : entries.getValue()) {
					EditHole shrinkableExpHole1 = new ExpChoiceHole(holeName, entries.getKey(), (Expression) exp, locationStmt.getStmtId(), 0);
					EditHole shrinkableExpHole2 = new ExpChoiceHole(holeName, entries.getKey(), (Expression) exp, locationStmt.getStmtId(), 1);
					retVal.add(shrinkableExpHole1);
					retVal.add(shrinkableExpHole2);
				}
			}
			return retVal;
		case PARREM:
			Map<ASTNode,List<Integer>> options = locationStmt.getShrinkableParameterMethods();
			for(Map.Entry<ASTNode, List<Integer>> nodeOption : options.entrySet()) {
				for(Integer option : nodeOption.getValue()) {
				EditHole shrinkableParamHole = new ExpChoiceHole(holeName, nodeOption.getKey(), (Expression) nodeOption.getKey(), locationStmt.getStmtId(), option);
				retVal.add(shrinkableParamHole);
				}
			}
			return retVal;
		case PARADD:
			Map<ASTNode,List<Integer>> extensionOptions = locationStmt.getExtendableParameterMethods();
			for(Map.Entry<ASTNode, List<Integer>> nodeOption : extensionOptions.entrySet()) {
				for(Integer option : nodeOption.getValue()) {
				EditHole shrinkableParamHole = new ExpChoiceHole(holeName, nodeOption.getKey(), (Expression) nodeOption.getKey(), locationStmt.getStmtId(), option);
				retVal.add(shrinkableParamHole);
				}
			}
			return retVal;
		case OBJINIT:
		case SIZECHECK:
		default:
			logger.fatal("Unhandled template type in editSources!  Fix code in JavaEditFactory to do this properly.");
			return null;
		}
		return retVal;
	}

	public Boolean doesEditApply(JavaRepresentation variant, Location location, Mutation editType) {
		JavaStatement locationStmt = (JavaStatement) location.getLocation();
		switch(editType) {
		case APPEND: 
		case REPLACE:
			//If it is a return statement, nothing should be appended after it, since it would be dead code
			if(!(locationStmt.getASTNode() instanceof ReturnStatement || locationStmt.getASTNode() instanceof ThrowStatement )){
				return this.editSources(variant, location,  editType, "singleHole").size() > 0;
			}
			return false;
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
			return locationStmt.getCandidateMethodReplacements().size() > 0; 
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
		case PARREM:
			return locationStmt.getShrinkableParameterMethods().size() > 0;
		case EXPREP:
		case EXPADD:
			return locationStmt.getConditionalExpressions(variant.semanticInfo).size() > 0;
		case EXPREM:
			return locationStmt.getShrinkableConditionalExpressions().size() > 0;
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
			retVal.add("offByOne");
			return retVal;
		case RANGECHECK: 
			retVal.add("rangeCheck");
			return retVal;
		case FUNREP:
			retVal.add("replaceMethod");
			return retVal;
		case PARREP:
		case EXPREP:
			retVal.add("replaceParameter");
			return retVal;
		case CASTCHECK:
			retVal.add("classCast");
			return retVal;
		case PARADD:
			retVal.add("addParameter");
			return retVal;
		case EXPADD:
			retVal.add("condExpAdd");
			return retVal;
		case EXPREM:
			retVal.add("condExpRem");
			return retVal;
		case PARREM:
			retVal.add("remParameter");
			return retVal;
		case OBJINIT:
		case SIZECHECK:
			logger.fatal("Unhandled edit type in holesForMutation.  Handle it in JavaEditFactory and try again.");
			return null;
		}
		return null;
	}
}
