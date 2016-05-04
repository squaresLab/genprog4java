package clegoues.javaASTProcessor.ASTPrinter;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;

public class ASTPrinterVisitor extends ASTVisitor {

	private final PrintWriter printer;

	public ASTPrinterVisitor(OutputStream destination) {
		this.printer = new PrintWriter(destination);
	}
	public void endVisit() {
		printer.flush();
		printer.close();
	}

	
//	FIXME: boolean visit(AnonymousClassDeclaration node)  {
//        return true;
//    }
//	
	public boolean visit(ArrayAccess node)  {
		node.getArray().accept(this);
		printer.print(" [ ");
		node.getIndex().accept(this);
		printer.print(" ] ");
        return false;
    }
	

//	FIXME: boolean visit(ArrayCreation node)  {
//        return true;
//    }
//	
//	boolean	visit(ArrayInitializer node)  {
//        return true;
//    }
//	
//FIXME: boolean visit(ArrayType node)  {
//        return true;
//    }
	
	public boolean visit(AssertStatement node)  {
		// FIXME: ignoring message
		printer.print("assert ");
		node.getExpression().accept(this);
        return false;
    }
	
	public boolean visit(Assignment node)  {
		node.getLeftHandSide().accept(this);
		printer.print(node.getOperator().toString() + " ");
		node.getRightHandSide().accept(this);
        return false;
    }

	@SuppressWarnings("unchecked")
	public boolean visit(Block node) {
		List<Statement> stmts = node.statements();
		for(Statement s : stmts) {
			s.accept(this);
			printer.println();
		}
		return false;
	}
	
	public boolean visit(BooleanLiteral node)  {
		printer.print(node.toString());
        return false;
    }
	public boolean visit(BreakStatement node)  {
		printer.print("break ");
        return true;
    }
	
	public boolean visit(CastExpression node)  {
		printer.print("(" + node.getType().toString() + ") ");
		node.getExpression().accept(this);
		return false;
    }

	public boolean	visit(CharacterLiteral node)  {
		printer.print(node.toString());
        return false;
    }
	
	@SuppressWarnings("unchecked")
	public boolean	visit(ClassInstanceCreation node)  {
	if(node.getExpression() != null) {
		node.getExpression().accept(this); // omitting ".".  FIXME: should I?
	}
	printer.print("new ");
	this.printTypeArguments(node.typeArguments());
	node.getType().accept(this);
	this.printArguments(node.arguments());
	if(node.getAnonymousClassDeclaration() != null) {
		node.getAnonymousClassDeclaration().accept(this);
	}
        return false;
    }
	
	public boolean visit(ConditionalExpression node)  {
		node.getExpression().accept(this);
		printer.print(" ? ");
		node.getThenExpression().accept(this);
		printer.print(" : ");
		node.getElseExpression().accept(this);
        return false;
    }
	@SuppressWarnings("unchecked")
	public boolean visit(ConstructorInvocation node)  {
		this.printTypeArguments(node.typeArguments());
		printer.print("this ");
		this.printArguments(node.arguments());
        return false;
    }
	public boolean	visit(ContinueStatement node)  {
		printer.println("continue");
        return false;
    }
	
//	JLS 8: boolean visit(CreationReference node)  {
//        return true;
//    }
//	
//	boolean	visit(Dimension node)  {
//        return true;
//    }
	
	public boolean visit(DoStatement node)  {
		printer.println("do ");
		node.getBody().accept(this);
		printer.print("while ");
		node.getExpression().accept(this);
		printer.println();
        return false;
    }
	
	public boolean visit(EnhancedForStatement node)  {
		printer.print("for ");
		node.getParameter().accept(this); // omitting the ':'
		node.getExpression().accept(this);
		node.getBody().accept(this);
        return false;
    }
	

// FIXME: boolean	visit(EnumDeclaration node)  {
//        return true;
//    }
	
//	JLS8 : boolean visit(ExpressionMethodReference node)  {
//        return true;
//    }
	

	public boolean visit(FieldAccess node)  {
		node.getExpression().accept(this);
		printer.print(". ");
		printer.print(node.getName());
        return false;
    }
	
	@SuppressWarnings("unchecked")
	public boolean visit(FieldDeclaration node)  {
		this.printModifiers(node.modifiers());
		printer.print(node.getType().toString());
		List<VariableDeclarationFragment> fragments = node.fragments();
		for(VariableDeclarationFragment f : fragments) {
			f.accept(this);
		}
        return true;
    }
	
	@SuppressWarnings("unchecked")
	public boolean visit(ForStatement node)  { 
		printer.print("for ");
		List<Expression> initializers = node.initializers();
		for(Expression initializer : initializers) {
			initializer.accept(this);
			printer.print(" ");
		}
		Expression exp = node.getExpression();
		if(exp != null) {
			exp.accept(this);
			printer.print(' ');
		}
		List<Expression> updaters = node.updaters();
		for(Expression updater : updaters) {
			updater.accept(this);
			printer.print(' ');
		}
		node.getBody().accept(this);
        return false;
    }
	
	public boolean visit(IfStatement node)  {
		printer.print("if ");
		node.getExpression().accept(this);
		printer.println("");
		printer.println("then ");
		node.getThenStatement().accept(this);
		printer.println();
		if(node.getElseStatement() != null) {
		printer.print("else ");
		node.getElseStatement().accept(this);
		}
        return false;
    }
	
	public boolean visit(ImportDeclaration node)  {
		printer.println("import " + node.getName());
        return false;
    }
	
	public boolean visit(InfixExpression node)  {
		node.getLeftOperand().accept(this);
		printer.print(node.getOperator().toString());
		node.getRightOperand().accept(this);
        return false;
    }
	
	
	public boolean visit(InstanceofExpression node)  {
		node.getLeftOperand().accept(this);
		printer.print("instanceof ");
		node.getRightOperand().accept(this);
        return false;
    }
	
// JLS 8 boolean visit(IntersectionType node)  {
//        return true;
//    }

	public boolean visit(LabeledStatement node)  {
		printer.print(node.getLabel() + ": ");
		node.getBody().accept(this);
        return false;
    }

	
	private void printTypeParameters(List<TypeParameter> tParams) {
		if(tParams.size() > 0) {
			printer.print("<");
			for(TypeParameter t : tParams) { // FIXME trailing comma
				printer.print(t.toString() + ",");
			}
			printer.print(">");
		}
	}
	
	private void printTypeArguments(List<Type> tParams) {
		if(tParams.size() > 0) {
			printer.print("<");
			for(Type t : tParams) { // FIXME trailing comma
				printer.print(t.toString() + ",");
			}
			printer.print(">");
		}
	}
	
	private void printArguments(List<Expression> args) {
		for(Expression e : args) {
			e.accept(this);
		}
	}
	
	private void printModifiers(List<Modifier> modifiers) {
		for(Modifier mod : modifiers) {
			printer.print(mod.toString() + " ");
		}
	}
	@SuppressWarnings("unchecked")
	public boolean visit(MethodDeclaration node)  {
		// am omitting extraDimensions, receiverQualifier, and receiverType properties for now
		this.printModifiers(node.modifiers());
		if(!node.isConstructor()) {
			Type returnType = node.getReturnType2();
			printer.print(returnType.toString() + " ");
		}
		printer.print(node.getName());
		List<TypeParameter> typeParameters = node.typeParameters();
		this.printTypeParameters(typeParameters);
		printer.print(" ");
		
		node.isVarargs();
		node.parameters();
		List<Type> thrownExceptions = node.thrownExceptionTypes();
		if(thrownExceptions.size() > 0) {
			printer.print(" throws ");
			for(Type t : thrownExceptions) {
				printer.print(t.toString() + " ");
			}
		}
		node.getBody().accept(this);
		printer.println();
        return false;
    }
	

	@SuppressWarnings("unchecked")
	public boolean	visit(MethodInvocation node)  {
		if(node.getExpression() != null) {
			node.getExpression().accept(this);
			printer.print(". ");
		}
		this.printTypeArguments(node.typeArguments());
		printer.print(node.getName() + " ");
		this.printArguments(node.arguments());
        return true;
    }

//	FIXME: boolean visit(NameQualifiedType node)  {
//        return true;
//    }

	public boolean visit(NullLiteral node)  {
		printer.print("NULL ");
        return false;
    }
	
	public boolean visit(NumberLiteral node)  {
		printer.print(node.toString() + " ");
        return false;
    }
	
	public boolean visit(PackageDeclaration node)  {
		printer.println("package " + node.getName().toString());
        return false;
    }

	public boolean visit(PostfixExpression node)  {
		node.getOperand().accept(this);
		printer.print(node.getOperator() + " ");
        return false;
    }
	
	public boolean visit(PrefixExpression node)  {
		printer.print(node.getOperator() + " ");
		node.getOperand().accept(this);
        return false;
    }
	
	public boolean visit(PrimitiveType node)  {
		printer.print(node.toString());
        return false;
    }
	
// FIXME: do I need these? boolean visit(QualifiedName node)  {
//        return true;
//    }
//	
//	boolean	visit(QualifiedType node)  {
//        return true;
//    }
	
	public boolean visit(ReturnStatement node)  {
		printer.print("return ");
		Expression retval = node.getExpression();
		if(retval != null) {
			retval.accept(this);
		}
		return false;
    }
	
	public boolean visit(SimpleName node)  {
		printer.print(node.getIdentifier());
        return false;
    }
	
	public boolean	visit(SimpleType node)  {
		printer.print(node.getName().toString());
        return false;
    }

	@SuppressWarnings("unchecked")
	public boolean visit(SingleVariableDeclaration node)  {
		this.printModifiers(node.modifiers());
		printer.print(node.getType() + " ");
		printer.print(node.getName() + " ");
		if(node.getInitializer() != null) {
			printer.print(" = ");
			node.getInitializer().accept(this);
		}
		return false;
    }
	
	public boolean visit(StringLiteral node)  {
		printer.print(node.getLiteralValue() + " ");
        return false;
    }
	
	@SuppressWarnings("unchecked")
	public boolean visit(SuperConstructorInvocation node)  {
		Expression exp = node.getExpression();
		if(exp != null) {
			exp.accept(this);
		}
		this.printTypeArguments(node.typeArguments());
		printer.print("super ");
		List<Expression> args = node.arguments();
		for(Expression e : args) {
			e.accept(this);
		}
		printer.println();
        return false;
    }
	
	public boolean visit(SuperFieldAccess node)  {
		printer.println("super. " + node.getName());
        return false;
    }
//FIXME: bored boolean visit(SuperMethodInvocation node)  {
//        return true;
//    }

// Added in Java 8: boolean visit(SuperMethodReference node)  {
//        return true;
//    }
//	
	public boolean visit(SwitchCase node)  {
		if(node.isDefault()) {
			printer.print("default ");
		} else {
			printer.print("case ");
			node.getExpression().accept(this);
		}
		return false;
    }
	
	@SuppressWarnings("unchecked")
	public boolean visit(SwitchStatement node)  {
		printer.print("switch ");
		node.getExpression().accept(this);
		printer.println("");
		List<Statement> stmts = node.statements();
		for(Statement s: stmts) {
			s.accept(this);
		}
		printer.println();
		return false;
    }
	
//	FIXME: deal with this later
//   boolean visit(SynchronizedStatement node)  {
//        return true;
//    }

	public boolean visit(ThisExpression node)  {
		printer.print("this ");
        return false;
    }
	
	public boolean visit(ThrowStatement node)  {
		printer.print("throw ");
		node.getExpression().accept(this);
        return false;
    }
	
	@SuppressWarnings("unchecked")
	public boolean visit(TryStatement node)  {
		// FIXME: with resources not handled
		printer.println("try ");
		node.getBody().accept(this);
		List<CatchClause> catchClauses = node.catchClauses();
		for(CatchClause c : catchClauses) {
			printer.print("catch ");
			c.getException().accept(this);
			c.getBody().accept(this);
			printer.println();
		}
		Block f = node.getFinally();
		if(f != null) {
			printer.println("finally ");
			f.accept(this);
		}
		return false;
    }
	

	@SuppressWarnings("unchecked")
	public boolean visit(TypeDeclaration node)  {
		List<Modifier> modifiers = node.modifiers();
		for(Modifier mod : modifiers) {
			printer.print(mod.toString() + " ");
		}
		if(node.isInterface()) {
			List<Type> superTypes = node.superInterfaceTypes();
			printer.print("interface " + node.getName().toString());
			if(superTypes.size() > 0) {
				printer.print("extends ");
			}
			for(Type t : superTypes) { // FIXME: trailing comma
				printer.print(t.toString() + ",");
			}
		} else {
			printer.print("class " + node.getName().toString());
			Type superType = node.getSuperclassType();
			if(superType != null) {
				printer.print("extends " + superType.toString());
			}
		}
		List<TypeParameter> tParams = node.typeParameters();
		this.printTypeParameters(tParams);
		printer.print("\n");
		List<BodyDeclaration> bodyDecls = node.bodyDeclarations();
		for(BodyDeclaration b : bodyDecls) {
			b.accept(this);
		}
        return false;
    }

	public boolean visit(TypeLiteral node)  {
		printer.print(node.getType().toString());
        return false;
    }
	
// JLS 8: boolean visit(TypeMethodReference node)  {
//        return true;
//    }

//	FIXME: Bored boolean visit(UnionType node)  {
//        return true;
//    }

	public boolean visit(VariableDeclarationFragment node) {
		printer.print(node.getName() + " ");
		if(node.getInitializer() != null) {
			printer.print(" = ");
			node.getInitializer().accept(this);
		}
		return false;
	}

	 public boolean visit(WhileStatement node)  {
		 printer.print("while ");
		 node.getExpression().accept(this);
		 printer.println("");
		 node.getBody().accept(this);
        return false;
    }

 

}
