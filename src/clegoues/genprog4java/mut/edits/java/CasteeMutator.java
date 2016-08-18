package clegoues.genprog4java.mut.edits.java;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.holes.java.ExpChoiceHole;
import clegoues.genprog4java.mut.holes.java.ExpHole;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.holes.java.MethodInfoHole;
import clegoues.genprog4java.mut.holes.java.StatementHole;

public class CasteeMutator extends JavaEditOperation {

	public CasteeMutator(JavaLocation location, EditHole source) {
		super(location, source);
	}

	@Override
	public void edit(ASTRewrite rewriter) {
		//ASTNode locationNode = ((JavaLocation) this.getLocation()).getCodeElement(); 
		ExpHole thisHole = (ExpHole) this.getHoleCode();
		ASTNode toReplace = (ASTNode) thisHole.getCode();
		ASTNode replaceWith = ASTNode.copySubtree(rewriter.getAST(), thisHole.getLocationExp());
		rewriter.replace(toReplace, replaceWith, null);

		/*
		ASTNode locationNode = ((JavaLocation) this.getLocation()).getCodeElement(); 

		if(this.getHoleCode().getName().equalsIgnoreCase("MethodInvocation")){
			MethodInfoHole thisHole = (MethodInfoHole) this.getHoleCode();
			ASTNode toReplace = (ASTNode) thisHole.getCode();
			IMethodBinding replaceWith = thisHole.getMethodInfo();
			//Expression newNode = rewriter.getAST().newExpression();
			MethodInvocation newNode = rewriter.getAST().newMethodInvocation();
			//Class replaceWith = ASTNode.nodeClassForType(typeNumber);
			//SimpleType type = new SimpleType(replaceWith.getTypeName());//rewriter.getAST().newSimpleName(replaceWith.getName());
			SimpleName newMethodName = rewriter.getAST().newSimpleName(replaceWith.getName());
			//newNode.setType(type);
			newNode.setName(newMethodName);
			rewriter.replace(toReplace, newNode, null); 	
		}else if(this.getHoleCode().getName().equalsIgnoreCase("Name")){
			ExpHole thisHole = (ExpHole) this.getHoleCode();
			ASTNode toReplace = (ASTNode) thisHole.getCode();
			ASTNode replaceWith = thisHole.getCode();
			SimpleName newNode = rewriter.getAST().newSimpleName(replaceWith.toString());
			//MethodInvocation newNode = rewriter.getAST().newMethodInvocation();
			//Class replaceWith = ASTNode.nodeClassForType(typeNumber);
			//SimpleType type = new SimpleType(replaceWith.getTypeName());//rewriter.getAST().newSimpleName(replaceWith.getName());
			//SimpleName newMethodName = rewriter.getAST().newSimpleName(replaceWith.getName());
			//newNode.setType(type);
			//newNode.setName(newNode);
			rewriter.replace(toReplace, newNode, null); 	
		}else if(this.getHoleCode().getName().equalsIgnoreCase("ArrayAccess")){
			ExpHole thisHole = (ExpHole) this.getHoleCode();
			ASTNode toReplace = (ASTNode) thisHole.getCode();
			ASTNode replaceWith = thisHole.getCode();
			ArrayAccess newNode = rewriter.getAST().newArrayAccess();//(replaceWith.toString());
			newNode.setArray((Expression) replaceWith);
			rewriter.replace(toReplace, newNode, null); 	
		}
		*/
	}
	
	public String toString() {
		ExpHole thisHole = (ExpHole) this.getHoleCode();
		String retval = "csteeM(" + this.getLocation().getId() + ": ";
		retval += "(" + thisHole.getCode() + ") replaced with ";
		retval +=  "(" + thisHole.getLocationExp() + "))";
		return retval;
	}

}









