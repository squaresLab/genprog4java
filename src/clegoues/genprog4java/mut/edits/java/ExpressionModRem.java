package clegoues.genprog4java.mut.edits.java;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.java.JavaStatement;
import clegoues.genprog4java.java.MethodInfo;
import clegoues.genprog4java.mut.EditHole;
import clegoues.genprog4java.mut.Mutation;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.mut.holes.java.MethodInfoHole;

public class ExpressionModRem extends JavaEditOperation {
		
		public ExpressionModRem(JavaLocation location,  HashMap<String, EditHole> sources) {
			super(Mutation.EXPREM, location, sources);
			this.holeNames.add("condExpRem");

		}

		@Override
		public void edit(final ASTRewrite rewriter) {
			JavaStatement locationStmt = (JavaStatement) (this.getLocation().getLocation());
			ASTNode locationNode = locationStmt.getASTNode();
			MethodInfoHole thisHole = (MethodInfoHole) this.getHoleCode("condExpRem");
			ASTNode toReplace = thisHole.getCode();
			MethodInfo replaceWith = thisHole.getMethodInfo();

			MethodInvocation newNode = locationNode.getAST().newMethodInvocation();
			SimpleName newMethodName = locationNode.getAST().newSimpleName(replaceWith.getName());
			newNode.setName(newMethodName);
			
			List<ASTNode> paramNodes = ((MethodInvocation) toReplace).arguments();
			for(ASTNode param : paramNodes) {
				ASTNode newParam = rewriter.createCopyTarget(param);
				newNode.arguments().add(newParam);
			}		
			rewriter.replace(toReplace, newNode, null);
		}
	}

