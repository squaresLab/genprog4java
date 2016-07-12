package clegoues.genprog4java.mut.edits.java;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import clegoues.genprog4java.mut.holes.java.ExpHole;
import clegoues.genprog4java.mut.holes.java.SubExpsHole;

public class ObjectInitializer extends JavaEditOperation {

	@Override
	public void edit(ASTRewrite rewriter) {
		SubExpsHole thisHole = (SubExpsHole) this.getHoleCode();
		ASTNode methodInvocation = thisHole.getHoleParent();
		List<ASTNode> parameters = thisHole.getSubExps();
		
		
		methodInvocExp.accept(new ASTVisitor() {
			public boolean visit(MethodInvocation node) {
				for(Object arg : node.arguments()) {
					Expression asExp = (Expression) arg;
					ITypeBinding binding = asExp.resolveTypeBinding();
					if(binding.isClass()) {
						argsToInit.add(asExp);
						break;
					}
				}
				}
				return true;
			}
		});
	}
	/* [Object Initializer]
			B = buggy statements
			collect object creation operators in parameters of B into collection C

			loop for all object creations in C
			{
			 insert an assignment statement that declares a new local variable and creates a new object (using an object creation's type) to the variable
			 insert a method invocation statement that calls an initialization method 
			}
			insert B after statements
*/
}
