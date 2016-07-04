package clegoues.genprog4java.mut.edits.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import clegoues.genprog4java.localization.Localization;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Location;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.ExpChoiceHole;
import clegoues.genprog4java.mut.holes.java.ExpHole;
import clegoues.genprog4java.mut.holes.java.JavaHole;
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

	public JavaEditOperation makeEdit(Mutation edit, Location dst, EditHole sources) {
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

		Localization localization = variant.getLocalization();
		
		JavaStatement potentiallyBuggyStmt = (JavaStatement) stmtId.getLocation();
		ASTNode faultAST = potentiallyBuggyStmt.getASTNode();
		TreeSet<WeightedAtom> retVal = new TreeSet<WeightedAtom>();

		for (WeightedAtom potentialFixAtom : localization.getFixSourceAtoms()) {
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



	public TreeSet<EditHole> editSources(JavaRepresentation variant, Location location, Mutation editType) {
		JavaStatement locationStmt = (JavaStatement) location.getLocation();
		TreeSet<EditHole> retVal = new TreeSet<EditHole>();

		switch(editType) {
		case DELETE: retVal.add(new StatementHole((Statement) locationStmt.getASTNode(), locationStmt.getStmtId()));
		return retVal;
		case APPEND: 	
		case REPLACE:
			TreeSet<WeightedAtom> fixStmts = this.scopeHelper(location, variant);
			for(WeightedAtom fixStmt : fixStmts) {
				JavaStatement potentialFixStmt = variant.getFromCodeBank(fixStmt.getFirst());
				ASTNode fixAST = potentialFixStmt.getASTNode();
				retVal.add(new StatementHole((Statement) fixAST, potentialFixStmt.getStmtId()));
			}
			break;
		case SWAP:
			for (WeightedAtom item : this.scopeHelper(location, variant)) {
				int atom = item.getAtom();
				TreeSet<WeightedAtom> inScopeThere = this.scopeHelper(variant.instantiateLocation(atom, item.getSecond()), variant);
				for (WeightedAtom there : inScopeThere) {
					if (there.getAtom() == location.getId()) { // FIXME: this check looks weird to me.  Test swap.
						JavaStatement potentialFixStmt = variant.getFromCodeBank(there.getAtom());
						ASTNode fixAST = potentialFixStmt.getASTNode();
						retVal.add(new StatementHole((Statement) fixAST, potentialFixStmt.getStmtId()));
						break;
					}
				}
			}
			break;
		case LBOUNDSET:
		case UBOUNDSET:
		case RANGECHECK:
		case OFFBYONE:  
			return JavaHole.makeSubExpsHoles(locationStmt.getArrayAccesses()); 
		case NULLCHECK:
			return JavaHole.makeSubExpsHoles(locationStmt.getNullCheckables());
		case CASTCHECK:
			return JavaHole.makeSubExpsHoles(locationStmt.getCasts());
		case FUNREP:
			Map<ASTNode, List<IMethodBinding>> methodReplacements = locationStmt.getCandidateMethodReplacements();
			for(Map.Entry<ASTNode,List<IMethodBinding>> entry : methodReplacements.entrySet()) {
				ASTNode replacableMethod = entry.getKey();
				List<IMethodBinding> possibleReplacements = entry.getValue();
				for(IMethodBinding possibleReplacement : possibleReplacements) {
					retVal.add(new MethodInfoHole(replacableMethod, locationStmt.getStmtId(), possibleReplacement));
				}
			}
			break;
		case PARREP:
			return JavaHole.makeExpHole(locationStmt.getReplacableMethodParameters(variant.semanticInfo), locationStmt);
		case EXPREP:
			return JavaHole.makeExpHole(locationStmt.getConditionalExpressions(variant.semanticInfo), locationStmt);


		case EXPADD:
			Map<Expression, List<Expression>> extendableExpressions = locationStmt.getConditionalExpressions(variant.semanticInfo);
			for(Entry<Expression, List<Expression>> entries : extendableExpressions.entrySet()) { 
				for(Expression exp : entries.getValue()) {
					EditHole shrinkableExpHole1 = new ExpChoiceHole(entries.getKey(), exp, locationStmt.getStmtId(), 0);
					EditHole shrinkableExpHole2 = new ExpChoiceHole( entries.getKey(), exp, locationStmt.getStmtId(), 1);
					retVal.add(shrinkableExpHole1);
					retVal.add(shrinkableExpHole2);
				}
			}
			return retVal;
		case PARREM:
			Map<ASTNode,List<Integer>> options = locationStmt.getShrinkableParameterMethods();
			for(Map.Entry<ASTNode, List<Integer>> nodeOption : options.entrySet()) {
				for(Integer option : nodeOption.getValue()) { // probably wrong
					EditHole shrinkableParamHole = new ExpChoiceHole((Expression) nodeOption.getKey(), (Expression) nodeOption.getKey(), locationStmt.getStmtId(), option);
					retVal.add(shrinkableParamHole);
				}
			}
			return retVal;
		case EXPREM:
			Map<Expression, List<Expression>> shrinkableExpressions = locationStmt.getShrinkableConditionalExpressions();
			for(Entry<Expression, List<Expression>> entries : shrinkableExpressions.entrySet()) {
				for(Expression exp : entries.getValue()) { 
					EditHole shrinkableExpHole1 = new ExpChoiceHole(exp, exp, locationStmt.getStmtId(), 0);
					EditHole shrinkableExpHole2 = new ExpChoiceHole(exp, exp, locationStmt.getStmtId(), 1);
					retVal.add(shrinkableExpHole1);
					retVal.add(shrinkableExpHole2);
				}
			}
			return retVal;
		case PARADD:
			Map<ASTNode, List<List<ASTNode>>> extensionOptions = locationStmt.getExtendableParameterMethods(variant.semanticInfo);
			for(Entry<ASTNode, List<List<ASTNode>>> nodeOption : extensionOptions.entrySet()) {
				List<List<ASTNode>> paramOptions =  nodeOption.getValue();
				LinkedList<List<ASTNode>> res = new LinkedList<List<ASTNode>>();
				permutations(paramOptions, res, 0, new LinkedList<ASTNode>());
				for(List<ASTNode> oneList : res) {
					EditHole fillIn = new SubExpsHole(nodeOption.getKey(), oneList);
					retVal.add(fillIn);
				}
			}
			return retVal;
		case SIZECHECK:
			EditHole hole = new SubExpsHole(locationStmt.getASTNode(), locationStmt.getIndexedCollectionObjects());
			retVal.add(hole);
			return retVal;
		case OBJINIT:
			logger.fatal("Unhandled template type in editSources!  Fix code in JavaEditFactory to do this properly.");
			return null;
		}
		return retVal;
	}

	void permutations(List<List<ASTNode>> original, List<List<ASTNode>> result, int d, List<ASTNode> current) {
		// if depth equals number of original collections, final reached, add and return
		if (d == original.size()) {
			result.add(current);
			return;
		}

		// iterate from current collection and copy 'current' element N times, one for each element
		List<ASTNode> currentCollection = original.get(d);
		for (ASTNode element : currentCollection) {
			List<ASTNode> copy = new ArrayList<ASTNode>(current);
			copy.add(element);
			permutations(original, result, d + 1, copy);
		}
	}

	public boolean doesEditApply(JavaRepresentation variant, Location location, Mutation editType) {
		JavaStatement locationStmt = (JavaStatement) location.getLocation();
		switch(editType) {
		case APPEND: 
		case REPLACE:
			//If it is a return statement, nothing should be appended after it, since it would be dead code
			if(!(locationStmt.getASTNode() instanceof ReturnStatement || locationStmt.getASTNode() instanceof ThrowStatement )){
				return this.editSources(variant, location,  editType).size() > 0;
			}
			return false;
		case SWAP: 
			return this.editSources(variant, location,  editType).size() > 0;
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
			Map<Expression, List<Expression>> methodParams = locationStmt.getReplacableMethodParameters(variant.semanticInfo);
			for(Entry<Expression, List<Expression>> entry : methodParams.entrySet()) {
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
		case PARADD:
			return locationStmt.getExtendableParameterMethods(variant.semanticInfo).size() > 0;
		case EXPREP:
		case EXPADD:
			return locationStmt.getConditionalExpressions(variant.semanticInfo).size() > 0;
		case EXPREM:
			return locationStmt.getShrinkableConditionalExpressions().size() > 0;
		case SIZECHECK:
			return locationStmt.getIndexedCollectionObjects().size() > 0;
		default:
			logger.fatal("Unhandled edit type in DoesEditApply.  Handle it in JavaRepresentation and try again.");
			break;

		}
		return false;
	}

}
