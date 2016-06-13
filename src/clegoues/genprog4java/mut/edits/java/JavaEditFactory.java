package clegoues.genprog4java.mut.edits.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Location;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.JavaHole;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.rep.JavaRepresentation;
import clegoues.genprog4java.rep.WeightedAtom;
import clegoues.util.Pair;

public class JavaEditFactory {

	// FIXME: handling of holes for templates is all wrong!
	private static HashMap<Location, TreeSet<WeightedAtom>> scopeSafeAtomMap = new HashMap<Location, TreeSet<WeightedAtom>>();

	protected Logger logger = Logger.getLogger(JavaEditOperation.class);

	public JavaEditOperation makeEdit(Mutation edit, Location dst, HashMap<String,EditHole> sources) {
		switch(edit) {
		case DELETE: 
			return new JavaDeleteOperation((JavaLocation) dst);
		case OFFBYONE:
			return new JavaOffByOneOperation((JavaLocation) dst, sources);
		case APPEND: return new JavaAppendOperation((JavaLocation) dst, sources);
		case REPLACE: return new JavaReplaceOperation((JavaLocation) dst, sources);
		case SWAP: return new JavaSwapOperation((JavaLocation) dst, sources);
		case PARREP:
			return new JavaMethodParameterReplacer((JavaLocation) dst, sources);
		case FUNREP:
			return new JavaMethodReplacer((JavaLocation) dst, sources);	
		case LBOUNDSET:
			return new JavaLowerBoundSetOperation((JavaLocation) dst, sources);
		case UBOUNDSET:
			return new JavaUpperBoundSetOperation((JavaLocation) dst, sources);
		case RANGECHECK:
			return new JavaRangeCheckOperation((JavaLocation) dst, sources);
		case NULLCHECK:
			return new JavaNullCheckOperation((JavaLocation) dst, sources);
		default: logger.fatal("unhandled edit template type in JavaEditFactory; this should be impossible (famous last words...)");
		}		return null;
	}

	private Pair<String,String> mrtContainsMethodName(String matchString){
		for (Pair<String,String> p : JavaRepresentation.getMethodReturns()) {
			if(p.getFirst().equalsIgnoreCase(matchString)){
				return p;
			}
		}
		return null;
	}

	private TreeSet<WeightedAtom> scopeHelper(Location stmtId, JavaRepresentation variant) {
		if (JavaEditFactory.scopeSafeAtomMap.containsKey(stmtId)) {
			return JavaEditFactory.scopeSafeAtomMap.get(stmtId);
		}

		JavaStatement potentiallyBuggyStmt = (JavaStatement) stmtId.getLocation();

		// I *believe* this is just variable names and doesn't check required
		// types, which are also collected
		// at parse time and thus could be considered here.
		Set<String> inScopeAt = JavaRepresentation.inScopeMap.get(potentiallyBuggyStmt.getStmtId());
		TreeSet<WeightedAtom> retVal = new TreeSet<WeightedAtom>();
		ASTNode faultAST = potentiallyBuggyStmt.getASTNode();

		for (WeightedAtom potentialFixAtom : variant.getFixSourceAtoms()) {
			int index = potentialFixAtom.getAtom();
			JavaStatement potentialFixStmt = variant.getFromCodeBank(index);
			ASTNode fixAST = potentialFixStmt.getASTNode();

			Set<String> requiredScopes = potentialFixStmt.getRequiredNames();


			{ // scoping the ok variable
				boolean ok = true;
				for (String req : requiredScopes) {
					if (!inScopeAt.contains(req)) {
						ok = false;
						break;
					}
				}
				if(!ok)
					continue;
			}

			//Heuristic: Don’t replace or swap (or append) an stmt with one just like it
			if(faultAST.equals(fixAST) || potentiallyBuggyStmt.getStmtId()==potentialFixStmt.getStmtId()){
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
					String returnType = JavaRepresentation.returnTypeOfThisMethod(((MethodDeclaration)enclosingMethod).getName().toString());
					if(returnType != null){
						ReturnStatement potFix = (ReturnStatement) fixAST;
						if(potFix.getExpression() instanceof SimpleName){
							String variableType = JavaRepresentation.variableDataTypes.get(potFix.getExpression().toString());
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
			String holeName) {
		switch(editType) {
		case APPEND: 	
		case REPLACE:
			JavaStatement locationStmt = (JavaStatement) location.getLocation();
			//If it is a return statement, nothing should be appended after it, since it would be dead code
			// FIXME: replace?
			if(!(locationStmt.getASTNode() instanceof ReturnStatement || locationStmt.getASTNode() instanceof ThrowStatement )){
				TreeSet<WeightedAtom> fixStmts = this.scopeHelper(location, variant);
				TreeSet<EditHole> retVal = new TreeSet<EditHole>();
				for(WeightedAtom fixStmt : fixStmts) {
					JavaStatement potentialFixStmt = variant.getFromCodeBank(fixStmt.getFirst());
					ASTNode fixAST = potentialFixStmt.getASTNode();
					retVal.add(new JavaHole(holeName, fixAST, null));
				}
				return retVal;
			}else{
				return null;
			}
		case DELETE: return null;
		case SWAP:
			TreeSet<EditHole> retVal = new TreeSet<EditHole>();
			for (WeightedAtom item : this.scopeHelper(location, variant)) {
				int atom = item.getAtom();
				TreeSet<WeightedAtom> inScopeThere = this.scopeHelper(variant.instantiateLocation(atom, item.getSecond()), variant);
				for (WeightedAtom there : inScopeThere) {
					if (there.getAtom() == location.getId()) {
						JavaStatement potentialFixStmt = variant.getFromCodeBank(there.getAtom());
						ASTNode fixAST = potentialFixStmt.getASTNode();
						retVal.add(new JavaHole(holeName, fixAST, null));
						retVal.add(new JavaHole(holeName, fixAST, null));
						break;
					}
				}
			}
			return retVal;
		case FUNREP:
		case PARREP:
		case PARADD:
		case PARREM:
		case EXPREP:
		case EXPADD:
		case EXPREM:
		case NULLCHECK:
		case OBJINIT:
		case RANGECHECK:
		case SIZECHECK:
		case CASTCHECK:
		case LBOUNDSET:
		case UBOUNDSET:
		case OFFBYONE:
		return null;
		default:
			// IMPORTANT FIXME FOR MANISH AND MAU: you must add handling here to check legality for templates as you add them.
			// if a template always applies, then you can move the template type to the DELETE case, above.
			// however, I don't think most templates always apply; it doesn't make sense to null check a statement in which nothing
			// could conceivably be null, for example.
			logger.fatal("Unhandled template type in editSources!  Fix code in JavaRepresentation to do this properly.");
			return new TreeSet<EditHole>();
		}
	}

	@SuppressWarnings("rawtypes")
	public Boolean doesEditApply(JavaRepresentation variant, Location location, Mutation editType) {
		JavaStatement locationStmt = (JavaStatement) location.getLocation();
		switch(editType) {
		case APPEND: 
		case REPLACE:
		case SWAP: // FIXME: a bit hacky
			return this.editSources(variant, location,  editType, "singleHole").size() > 0;
		case DELETE: 			
			return locationStmt.canBeDeleted();
		case OFFBYONE:  
		case UBOUNDSET:
		case LBOUNDSET:
		case RANGECHECK:
			return locationStmt.containsArrayAccesses();
		case FUNREP: 
			return locationStmt.methodReplacerApplies(JavaRepresentation.methodDecls);
		case PARREP:
			return locationStmt.methodParamReplacerApplies(/* expressions/vars in scope? */);

		case NULLCHECK: 
			return locationStmt.nullCheckApplies();
		default:
			logger.fatal("Unhandled edit type in DoesEditApply.  Handle it in JavaRepresentation and try again.");
			break;

		}
		return false;
	}


	public List<String> holesForMutation(Mutation mut) { // this info should probably be static, and it's weird
		// to have it split between this factory and the individual edits
		switch(mut) {
		case APPEND:
		case REPLACE: 
		case SWAP: 
			ArrayList<String> singleHole = new ArrayList<String>();
			singleHole.add("singleHole"); 
			return singleHole;
		case DELETE: return null;
		case FUNREP:
		case PARREP:
		case PARADD:
		case PARREM:
		case EXPREP:
		case EXPADD:
		case EXPREM: 
		case NULLCHECK: 
		case OBJINIT:
		case RANGECHECK: 
		case SIZECHECK:
		case CASTCHECK:
		case LBOUNDSET:
		case UBOUNDSET:
		case OFFBYONE:
			logger.fatal("Unhandled edit type in holesForMutation.  Handle it in JavaEditFactory and try again.");
			return null;
		}
		return null;
	}
}
