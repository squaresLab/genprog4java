package clegoues.genprog4java.mut.edits.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Location;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.rep.JavaRepresentation;
import clegoues.genprog4java.rep.WeightedAtom;
import clegoues.genprog4java.util.Pair;

public class JavaEditFactory {
	
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
		
		JavaStatement locationStmt = (JavaStatement) stmtId.getLocation();
		
		// I *believe* this is just variable names and doesn't check required
		// types, which are also collected
		// at parse time and thus could be considered here.
		Set<String> inScopeAt = JavaRepresentation.inScopeMap.get(locationStmt.getStmtId());
		TreeSet<WeightedAtom> retVal = new TreeSet<WeightedAtom>();
		for (WeightedAtom atom : variant.getFixSourceAtoms()) {
			int index = atom.getAtom();
			JavaStatement stmt = variant.getFromCodeBank(index);
			Set<String> requiredScopes = stmt.getRequiredNames();

			for (String req : requiredScopes) {
				if (!inScopeAt.contains(req)) {
					break;
				}
			}
			if(stmt.getASTNode() instanceof MethodRef){
				MethodRef mr = (MethodRef) stmt.getASTNode();
				Pair<String,String> methodRefInMrt = mrtContainsMethodName(mr.getName().toString());
				if(methodRefInMrt != null){
					if( methodRefInMrt.getSecond().equalsIgnoreCase("void") || methodRefInMrt.getSecond().equalsIgnoreCase("null")){
						break;
					}
				}
			}
			
			//No need to assign a value to a final variable
			if(stmt.getASTNode() instanceof Assignment){
				if(((Assignment) stmt.getASTNode()).getLeftHandSide() instanceof SimpleName){
					SimpleName leftHand = (SimpleName) ((Assignment) stmt.getASTNode()).getLeftHandSide();
					if(JavaRepresentation.finalVariables.contains(leftHand)){
						break;
					}
				}
			}
			
			//No need to insert a declaration of a final variable
			if(stmt.getASTNode() instanceof VariableDeclarationStatement){
				VariableDeclarationStatement ds = (VariableDeclarationStatement) stmt.getASTNode();
				VariableDeclarationFragment df = (VariableDeclarationFragment) ds.fragments().get(0);
				
				if(JavaRepresentation.finalVariables.contains(df.getName().getIdentifier())){
					break;
				}
			}

			retVal.add(atom);
		}
		JavaEditFactory.scopeSafeAtomMap.put(stmtId, retVal);
		return retVal;
	}
	
	public TreeSet<WeightedAtom> editSources(JavaRepresentation variant, Location location, Mutation editType,
			String holeName) {
		switch(editType) {
		case APPEND: 	
		case REPLACE:
				JavaStatement locationStmt = (JavaStatement) location.getLocation();
				//If it is a return statement, nothing should be appended after it, since it would be dead code
				// FIXME: what about REPLACE?
				if(!(locationStmt.getASTNode() instanceof ReturnStatement)){
					return this.scopeHelper(location, variant);
				}else{
					return null;
				}
		case DELETE: return null;
		case SWAP:
				TreeSet<WeightedAtom> retVal = new TreeSet<WeightedAtom>();
				for (WeightedAtom item : this.scopeHelper(location, variant)) {
					int atom = item.getAtom();
					TreeSet<WeightedAtom> inScopeThere = this.scopeHelper(variant.instantiateLocation(atom, item.getSecond()), variant);
					for (WeightedAtom there : inScopeThere) {
						if (there.getAtom() == location.getId()) {
							retVal.add(item);
							break;
						}
					}
				}
				return retVal;
		case NULLINSERT:
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
		default:
			// IMPORTANT FIXME FOR MANISH AND MAU: you must add handling here to check legality for templates as you add them.
			// if a template always applies, then you can move the template type to the DELETE case, above.
			// however, I don't think most templates always apply; it doesn't make sense to null check a statement in which nothing
			// could conceivably be null, for example.
			logger.fatal("Unhandled template type in editSources!  Fix code in JavaRepresentation to do this properly.");
			return new TreeSet<WeightedAtom>();
		}
	}

	@SuppressWarnings("rawtypes")
	public Boolean doesEditApply(JavaRepresentation variant, Location location, Mutation editType) {
		switch(editType) {
		case APPEND: 
		case REPLACE:
		case SWAP: // FIXME: a bit hacky
			return this.editSources(variant, location,  editType, "singleHole").size() > 0;
		case NULLINSERT:
		case DELETE: return true; // possible FIXME: not always true in Java?
		case OFFBYONE: return true;
		case NULLCHECK: 
			JavaStatement locationStmt = (JavaStatement) location.getLocation();
			if(locationStmt.getASTNode() instanceof MethodInvocation || locationStmt.getASTNode() instanceof FieldAccess || locationStmt.getASTNode() instanceof QualifiedName){
				return true;
			}
			break; 
		default:
			logger.fatal("Unhandled edit type in DoesEditApply.  Handle it in JavaEditFactory and try again.");
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
		case NULLINSERT:
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
