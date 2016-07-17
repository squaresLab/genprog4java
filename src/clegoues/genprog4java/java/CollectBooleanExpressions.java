package clegoues.genprog4java.java;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

/** collect all expressions that may be checking a boolean value
 * from an ASTNode.  Used in JavaSemanticInfo to collect possible expressions
 * to use to extend an if condition (for associated template) as well as in
 * JavaStatement to collect expressions in a potentially-extendable condition to
 * exclude redundant additions.
 * @author clegoues
 *
 */
public class CollectBooleanExpressions extends ASTVisitor {

	private List<Expression> expressionsInScope;
	public CollectBooleanExpressions(List<Expression> expressionsInScope) {
		this.expressionsInScope = expressionsInScope;
	}

	private void tryAdd(Expression node) {
		ITypeBinding tb = node.resolveTypeBinding();
		if(tb != null) {
			if(tb.getName().equals("boolean")) {
				expressionsInScope.add(node);
			}
		}
	}
	public boolean visit(PrefixExpression node) {
		if(node.getOperator() == PrefixExpression.Operator.NOT) {
			expressionsInScope.add(node);
		}
		return true;
	}
	public boolean visit(ConditionalExpression node) {
		expressionsInScope.add(node);
		return true;
	}
	public boolean visit(FieldAccess node) {
		tryAdd(node);
		return true;
	}
	public boolean visit(InfixExpression node) {
		InfixExpression.Operator op = node.getOperator();
		if(op ==  InfixExpression.Operator.LESS ||   
				op == InfixExpression.Operator.GREATER ||
				op == InfixExpression.Operator.LESS_EQUALS ||
				op == InfixExpression.Operator.GREATER_EQUALS ||
				op == InfixExpression.Operator.EQUALS || 
				op == InfixExpression.Operator.NOT_EQUALS ||
				op == InfixExpression.Operator.CONDITIONAL_AND ||
				op == InfixExpression.Operator.CONDITIONAL_OR) {
			expressionsInScope.add(node);
		}
		return true;
	} 
	public boolean visit(InstanceofExpression node) {
		expressionsInScope.add(node);
		return true;
	} 
	public boolean visit(MethodInvocation node) {
		tryAdd(node);
		return true;
	} 
	public boolean visit(SimpleName node) {
		tryAdd(node);
		return true;
	} 
	public boolean visit(QualifiedName node) {
		tryAdd(node);
		return true;
	}
	public boolean visit(SuperFieldAccess node) {
		tryAdd(node);
		return true;
	}
	public boolean visit(SuperMethodInvocation node) {
		tryAdd(node);
		return true;
	}

}
