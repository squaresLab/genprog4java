package clegoues.genprog4java.mut.edits.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.*;

import clegoues.genprog4java.java.ASTUtils;
import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.localization.Localization;
import clegoues.genprog4java.localization.Location;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.WeightedHole;
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

	private static HashMap<Integer, List<WeightedAtom>> scopeSafeAtomMap = new HashMap<Integer, List<WeightedAtom>>();

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
		case SIZECHECK:
			return new CollectionSizeChecker((JavaLocation) dst, sources);
		case OBJINIT:
			return new ObjectInitializer((JavaLocation) dst, sources);
		case SEQEXCH:
			return new SequenceExchanger((JavaLocation) dst, sources);
		case CASTERMUT:
			return new CasterMutator((JavaLocation) dst, sources);
		case CASTEEMUT:
			return new CasteeMutator((JavaLocation) dst, sources);
		}
		return null;
	}

	private List<WeightedAtom> scopeHelper(Location stmtId, JavaRepresentation variant, Mutation mut) {
		if (JavaEditFactory.scopeSafeAtomMap.containsKey(stmtId.getId())) {
			return JavaEditFactory.scopeSafeAtomMap.get(stmtId.getId());
		}
		Localization localization = variant.getLocalization();

		JavaStatement potentiallyBuggyStmt = (JavaStatement) stmtId.getLocation();
		ASTNode faultAST = ((JavaLocation) stmtId).getCodeElement();
		List<WeightedAtom> retVal = new ArrayList<WeightedAtom>();

		for (WeightedAtom potentialFixAtom : localization.getFixSourceAtoms()) {
			int index = potentialFixAtom.getAtom();
			JavaStatement potentialFixStmt = variant.getFromCodeBank(index);
			ASTNode fixAST = potentialFixStmt.getASTNode();

			// I *believe* this is just variable names and doesn't check required
			// types, which are also collected
			// at parse time and thus could be considered here.
			if (!JavaRepresentation.semanticInfo.scopeCheckOK(potentiallyBuggyStmt, potentialFixStmt)) {
				continue;
			}

			//Heuristic: Don’t replace or swap (or append) an stmt with one just like it
			// CLG killed the stmt ID equivalence check because it's possible for a statement 
			// in a location to have been modified previously such that the statement in the code bank is
			// different from what is now at that location.
			// this comes down to our having overloaded statement IDs to mean both location and statement ID
			// which is a problem I keep meaning to solve.
			if (mut != Mutation.APPEND && faultAST.equals(fixAST)) {
				continue;
			}

			//Heuristic: Do not insert a return statement on a func whose return type is void
			//Heuristic: Do not insert a return statement in a constructor
			if (fixAST instanceof ReturnStatement){
				if (potentiallyBuggyStmt.parentMethodReturnsVoid() ||
						potentiallyBuggyStmt.isLikelyAConstructor())
					continue;

				//Heuristic: Swapping, Appending or Replacing a return stmt to the middle of a block will make the code after it unreachable
				ASTNode parentBlock = potentiallyBuggyStmt.blockThatContainsThisStatement();
				if (parentBlock != null && parentBlock instanceof Block) {
					List<ASTNode> statementsInBlock = ((Block)parentBlock).statements();
					ASTNode lastStmtInTheBlock = statementsInBlock.get(statementsInBlock.size()-1);
					if (!lastStmtInTheBlock.equals(faultAST)){
						continue;
					}
				} else {
					continue;
				}

				//If we move a return statement into a function, the parameter in the return must match the function’s return type
				ASTNode enclosingMethod = ASTUtils.getEnclosingMethod(faultAST);

				if (enclosingMethod instanceof MethodDeclaration) {
					String returnType = JavaRepresentation.semanticInfo.returnTypeOfThisMethod(((MethodDeclaration)enclosingMethod).getName().toString());
					if (returnType != null){
						ReturnStatement potFix = (ReturnStatement) fixAST;
						if (potFix.getExpression() instanceof SimpleName){
							String variableType = JavaRepresentation.semanticInfo.getVariableDataTypes().get(potFix.getExpression().toString());
							if ( !returnType.equalsIgnoreCase(variableType)){
								continue;
							}
						}
					}
				}
			}

			//Heuristic: Inserting methods like this() or super() somewhere that is not the First (or second, if super?) Stmt in the constructor, is wrong
			if (fixAST instanceof ConstructorInvocation ||
					fixAST instanceof SuperConstructorInvocation){
				if (mut == Mutation.APPEND) continue;
				ASTNode enclosingMethod = ASTUtils.getEnclosingMethod(faultAST);

				if (enclosingMethod != null && 
						enclosingMethod instanceof MethodDeclaration && 
						((MethodDeclaration) enclosingMethod).isConstructor()) {
					List<ASTNode> statementsInBlock = ((MethodDeclaration) enclosingMethod).getBody().statements();
					ASTNode firstStmtInTheBlock = statementsInBlock.get(0);
					if(!faultAST.equals(firstStmtInTheBlock)) {
						continue;
					}
				} else {
					continue;
				}
			}

			//Heuristic: Don't allow to move breaks or continues outside of switch stmts
			// OR loops!
			// and don't cause unreachable statements by moving breaks into the middle of a block
			if ((fixAST instanceof BreakStatement || fixAST instanceof ContinueStatement)){
				if (!potentiallyBuggyStmt.isWithinLoopOrCase())	continue;
				else if (!potentiallyBuggyStmt.isLastStatementInControlFlow()) continue;
			}

			//Heuristic: Don't replace/swap returns within functions that have only one return statement
			// (unless the replacer is also a return statement); could also check if it's a block or
			// other sequence of statements with a return within it, but I'm lazy
			if ((!(fixAST instanceof ReturnStatement)) &&
					faultAST instanceof ReturnStatement) {
				ASTNode parent = ASTUtils.getEnclosingMethod(faultAST);
				if(parent instanceof MethodDeclaration && 
						!JavaStatement.hasMoreThanOneReturn((MethodDeclaration)parent)) {
					continue;
				}
			}
			
			//Heuristc: Don't throw or return in the middle of blocks, leaving unreachable statements
			if (fixAST instanceof ReturnStatement || fixAST instanceof ThrowStatement) {
				if (!potentiallyBuggyStmt.isLastStatementInControlFlow()) continue;
			}

			// if we made it this far without continuing, we're good to go.
			retVal.add(potentialFixAtom);
		}
		JavaEditFactory.scopeSafeAtomMap.put(stmtId.getId(), retVal);
		return retVal;
	}


	@SuppressWarnings("unchecked")
	public List<WeightedHole> editSources(JavaRepresentation variant, Location location, Mutation editType) {
		JavaStatement locationStmt = (JavaStatement) location.getLocation();
		Localization localization = variant.getLocalization();
		switch(editType) {
		case DELETE:
		{
			List<WeightedHole> retVal = new LinkedList<WeightedHole>();
			StatementHole stmtHole = new StatementHole((Statement) locationStmt.getASTNode(), locationStmt.getStmtId());
			retVal.add(new WeightedHole(stmtHole)); // deletion has no special weight  
			return retVal;
		}
		case APPEND: 	
		case REPLACE:
		{
			List<WeightedHole> retVal = new LinkedList<WeightedHole>();
			List<WeightedAtom> fixStmts = this.scopeHelper(location, variant, editType);
			for(WeightedAtom fixStmt : fixStmts) {
				JavaStatement potentialFixStmt = variant.getFromCodeBank(fixStmt.getLeft());
				ASTNode fixAST = potentialFixStmt.getASTNode();
				StatementHole stmtHole = new StatementHole((Statement) fixAST, potentialFixStmt.getStmtId());
				retVal.add(new WeightedHole(stmtHole, fixStmt.getRight()));
			}
			return retVal;
		}
		case SWAP:
		{
			List<WeightedHole> retVal = new LinkedList<WeightedHole>();
			for (WeightedAtom item : this.scopeHelper(location, variant, editType)) {
				int atom = item.getAtom();
				List<WeightedAtom> inScopeThere = this.scopeHelper(variant.instantiateLocation(atom, item.getRight()), variant, editType);
				for (WeightedAtom there : inScopeThere) {
					if (there.getAtom() == location.getId()) {
						JavaStatement potentialFixStmt = variant.getFromCodeBank(there.getAtom());
						ASTNode fixAST = potentialFixStmt.getASTNode();
						StatementHole stmtHole = new StatementHole((Statement) fixAST, potentialFixStmt.getStmtId());
						retVal.add(new WeightedHole(stmtHole, there.getRight()));
						break;
					}
				}
			}
			return retVal;
		}
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
		{			
			List<WeightedHole> retVal = new LinkedList<WeightedHole>();
			Map<ASTNode, List<IMethodBinding>> methodReplacements = locationStmt.getCandidateMethodReplacements();
			for(Map.Entry<ASTNode,List<IMethodBinding>> entry : methodReplacements.entrySet()) {
				ASTNode replaceableMethod = entry.getKey();
				List<IMethodBinding> possibleReplacements = entry.getValue();
				for(IMethodBinding possibleReplacement : possibleReplacements) {
					MethodInfoHole thisHole = new MethodInfoHole(replaceableMethod, locationStmt.getStmtId(), possibleReplacement);
					// method replacer chooses between multiple options uniformly at random
					retVal.add(new WeightedHole(thisHole));
				}
			}
			return retVal;
		}
		// parameter replacer, expression replacer, and expression adder and removers
		// should be selected between by distance from location.
		case PARREP:
			return JavaHole.makeExpHole(locationStmt.getReplacableMethodParameters(), locationStmt);
		case PARREM:
		{
			List<WeightedHole> retVal = new LinkedList<WeightedHole>();
			Map<ASTNode,List<Integer>> options = locationStmt.getShrinkableParameterMethods();
			for(Map.Entry<ASTNode, List<Integer>> nodeOption : options.entrySet()) {
				for(Integer option : nodeOption.getValue()) { // probably wrong
					EditHole shrinkableParamHole = new ExpChoiceHole((Expression) nodeOption.getKey(), (Expression) nodeOption.getKey(), locationStmt.getStmtId(), option);
					// parameter removal selects uniformly at random.
					retVal.add(new WeightedHole(shrinkableParamHole));
				}
			}
			return retVal;
		}
		case PARADD:
		{ // selection criteria not specified in Par materials, so I give uniform weight to all options
			List<WeightedHole> retVal = new LinkedList<WeightedHole>();
			Map<ASTNode, List<List<ASTNode>>> extensionOptions = locationStmt.getExtendableParameterMethods();
			for(Entry<ASTNode, List<List<ASTNode>>> nodeOption : extensionOptions.entrySet()) {
				List<List<ASTNode>> paramOptions =  nodeOption.getValue();
				LinkedList<List<ASTNode>> res = new LinkedList<List<ASTNode>>();
				permutations(paramOptions, res, 0, new LinkedList<ASTNode>());
				for(List<ASTNode> oneList : res) {
					EditHole fillIn = new SubExpsHole(nodeOption.getKey(), oneList);
					retVal.add(new WeightedHole(fillIn));
				}
			}
			return retVal;
		}
		case EXPREP:
			return JavaHole.makeExpHole(locationStmt.getExtendableConditionalExpressions(), locationStmt);
		case EXPADD:
		{
			List<WeightedHole> retVal = new LinkedList<WeightedHole>();
			Map<Expression, List<Expression>> extendableExpressions = locationStmt.getExtendableConditionalExpressions();
			for(Entry<Expression, List<Expression>> entries : extendableExpressions.entrySet()) { 
				Expression parentExp = entries.getKey();
				int parentExpLoc = ASTUtils.getLineNumber(parentExp);
				for(Expression exp : entries.getValue()) {
					EditHole shrinkableExpHole1 = new ExpChoiceHole(entries.getKey(), exp, locationStmt.getStmtId(), 0);
					EditHole shrinkableExpHole2 = new ExpChoiceHole(entries.getKey(), exp, locationStmt.getStmtId(), 1);
					int newExpLoc = ASTUtils.getLineNumber(exp);
					int lineDistance = Math.abs(parentExpLoc - newExpLoc);
					double weight = lineDistance != 0 ? 1.0 / lineDistance : 1.0;
					retVal.add(new WeightedHole(shrinkableExpHole1, weight));
					retVal.add(new WeightedHole(shrinkableExpHole2, weight));
				}
			}
			return retVal;
		}
		case EXPREM:
		{
			List<WeightedHole> retVal = new LinkedList<WeightedHole>();
			Map<Expression, List<Expression>> shrinkableExpressions = locationStmt.getShrinkableConditionalExpressions();
			for(Entry<Expression, List<Expression>> entries : shrinkableExpressions.entrySet()) {
				for(Expression exp : entries.getValue()) { 
					EditHole shrinkableExpHole1 = new ExpChoiceHole(exp, exp, locationStmt.getStmtId(), 0);
					EditHole shrinkableExpHole2 = new ExpChoiceHole(exp, exp, locationStmt.getStmtId(), 1);
					retVal.add(new WeightedHole(shrinkableExpHole1));
					retVal.add(new WeightedHole(shrinkableExpHole2));
				}
			}
			return retVal;
		}

		case SIZECHECK:
		{ // no choice here or objinit, so weight of 1.0
			List<WeightedHole> retVal = new LinkedList<WeightedHole>();
			EditHole hole = new SubExpsHole(locationStmt.getASTNode(), locationStmt.getIndexedCollectionObjects());
			retVal.add(new WeightedHole(hole));
			return retVal;
		}
		case OBJINIT:
		{
			List<WeightedHole> retVal = new LinkedList<WeightedHole>();
			List<ASTNode> initObjects = locationStmt.getObjectsAsMethodParams();
			for(ASTNode initObject : initObjects) {
				// this is a slight misuse of ExpHole, which sort of "expects" that the second argument
				// is the "replacement" code.
				EditHole newHole = new ExpHole((Expression) initObject, null, locationStmt.getStmtId());
				retVal.add(new WeightedHole(newHole));
			}
			return retVal;
		}
		case SEQEXCH:
		{
			List<WeightedHole> retVal = new LinkedList<WeightedHole>();
			JavaStatement faultyStmt = variant.getFromCodeBank(locationStmt.getStmtId());
			for (WeightedAtom potentialFixAtom : localization.getFixSourceAtoms()) {

				JavaStatement possibleFixStmt = variant.getFromCodeBank(potentialFixAtom.getLeft());
				if(!faultyStmt.toString().equalsIgnoreCase(possibleFixStmt.toString())
						&& faultyStmt.getASTNode().getNodeType()==possibleFixStmt.getASTNode().getNodeType()){
					StatementHole stmtHole = new StatementHole((Statement) possibleFixStmt.getASTNode(), possibleFixStmt.getStmtId());
					retVal.add(new WeightedHole(stmtHole, potentialFixAtom.getRight()));
				}
			}
			return retVal;
		}
		case CASTERMUT:
		{
			List<WeightedHole> retVal = new LinkedList<WeightedHole>();
			List<Type> casterObjects = locationStmt.getCasterTypes();
			List<ASTNode> toReplaceForCasters = locationStmt.getTypesToReplaceCaster();
			for(ASTNode casterObject : casterObjects) {

				ASTNode toRemove = null;
				for(ASTNode toReplaceForCaster : toReplaceForCasters){
					if(casterObject.toString().equalsIgnoreCase(toReplaceForCaster.toString())){
						toRemove = toReplaceForCaster;
					}
				}
				if(toRemove!=null){
					toReplaceForCasters.remove(toRemove);
				}
				EditHole newHole = new SubExpsHole(casterObject, toReplaceForCasters);
				retVal.add(new WeightedHole(newHole));
				
			}
			return retVal;
		}
		case CASTEEMUT:
		{
			List<WeightedHole> retVal = new LinkedList<WeightedHole>();
			List<Expression> casteeObjects = locationStmt.getCasteeExpressions();
			List<Expression> toReplaceForCastees = locationStmt.getExpressionsToReplaceCastee();
			for(Expression casteeObject : casteeObjects) {
				for(Expression toReplaceForCastee : toReplaceForCastees) {
					if(!casteeObject.toString().equalsIgnoreCase(toReplaceForCastee.toString())){
						EditHole newHole = new ExpHole(toReplaceForCastee, casteeObject, locationStmt.getStmtId());
						retVal.add(new WeightedHole(newHole));
					}
				}
			}
			return retVal;
		}
		}
		return null;
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
		Localization localization = variant.getLocalization();
		switch(editType) {
		case APPEND: 
			if(!(locationStmt.getASTNode() instanceof ReturnStatement || locationStmt.getASTNode() instanceof ThrowStatement )){
				return this.editSources(variant, location,  editType).size() > 0;
			}
			return false;
		case REPLACE: 
		case SWAP: 
			return locationStmt.canBeDeleted() && this.editSources(variant, location,  editType).size() > 0;
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
			return locationStmt.getReplacableMethodParameters().size() > 0;
		case PARREM:
			return locationStmt.getShrinkableParameterMethods().size() > 0;
		case PARADD:
			return locationStmt.getExtendableParameterMethods().size() > 0;
		case NULLCHECK: 
			return locationStmt.getNullCheckables().size() > 0;
		case CASTCHECK:
			return locationStmt.getCasts().size() > 0;
		case EXPREP:
		case EXPADD:
			return locationStmt.getExtendableConditionalExpressions().size() > 0;
		case EXPREM:
			return locationStmt.getShrinkableConditionalExpressions().size() > 0;
		case SIZECHECK:
			return locationStmt.getIndexedCollectionObjects().size() > 0;
		case OBJINIT:
			return locationStmt.getObjectsAsMethodParams().size() > 0;
		case SEQEXCH:
			//FIXME: there might be a stronger requirement than this
			return locationStmt.canBeDeleted() && localization.getFixSourceAtoms().size() > 0;
		case CASTERMUT:
			return locationStmt.getCasterTypes().size() > 0;
		case CASTEEMUT:
			return locationStmt.getCasteeExpressions().size() > 0;
		}
		return false;
	}

}
