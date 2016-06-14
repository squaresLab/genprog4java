package clegoues.javaASTProcessor.ASTPrinter;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
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
import org.eclipse.jdt.core.dom.ExpressionStatement;
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
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
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
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

public class ASTPrinterVisitor extends ASTVisitor {

	private final PrintWriter printer;
	
	public ASTPrinterVisitor(OutputStream destination) {
		this.printer = new PrintWriter(destination);
	}
	public void endVisit() {
		printer.flush();
	}

	private void literal(String token) {
		printer.print(token);
		printer.print(" ");
	}
	
//	FIXME: boolean visit(AnonymousClassDeclaration node)  {
//        return true;
//    }
//	
	public boolean visit(ArrayAccess node)  {
		node.getArray().accept(this);
		this.literal("[");
		node.getIndex().accept(this);
		this.literal("]");
        return false;
    }
	

	@SuppressWarnings("unchecked")
	public boolean visit(ArrayCreation node)  {
		this.literal("new");
		node.getType().accept(this);
		// FIXME: type parameters.
		this.literal("[");
		for(Expression dim : ((List<Expression>) node.dimensions())) {
			dim.accept(this);
		}

		this.literal("]");
		if(node.getInitializer() != null)
			node.getInitializer().accept(this);
		return false;
    }
	
	@SuppressWarnings("unchecked")
	public boolean	visit(ArrayInitializer node)  {
		List<Expression> exps = node.expressions();
		for(Expression e : exps) {
			e.accept(this);
		}
        return false;
    }
//	
//FIXME: boolean visit(ArrayType node)  {
//        return true;
//    }
	
	public boolean visit(AssertStatement node)  {
		// FIXME: ignoring message
		this.literal("assert");
		node.getExpression().accept(this);
        return false;
    }
	
	public boolean visit(Assignment node)  {
		node.getLeftHandSide().accept(this);
		this.literal(node.getOperator().toString());
		node.getRightHandSide().accept(this);
        return false;
    }

	@SuppressWarnings("unchecked")
	public boolean visit(Block node) {
		List<Statement> stmts = node.statements();
		for(Statement s : stmts) {
			s.accept(this);
		}

		return false;
	}
	
	public boolean visit(BooleanLiteral node)  {
		this.literal(node.toString());
        return false;
    }
	public boolean visit(BreakStatement node)  {
		this.literal("break");
		this.literal("\n");
        return false;
    }
	
	public boolean visit(CastExpression node)  {
		this.literal("(" + node.getType().toString() + ")");
		node.getExpression().accept(this);
		return false;
    }

	public boolean	visit(CharacterLiteral node)  {
		this.literal(node.toString());
        return false;
    }
	
	@SuppressWarnings("unchecked")
	public boolean visit(ClassInstanceCreation node)  {
	if(node.getExpression() != null) {
		node.getExpression().accept(this);
		this.literal(".");
	}
	this.literal("new");
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
		this.literal("?");
		node.getThenExpression().accept(this);
		this.literal(":");
		node.getElseExpression().accept(this);
        return false;
    }
	@SuppressWarnings("unchecked")
	public boolean visit(ConstructorInvocation node)  {
		this.printTypeArguments(node.typeArguments());
		this.literal("this");
		this.printArguments(node.arguments());
        return false;
    }
	public boolean	visit(ContinueStatement node)  {
		this.literal("continue");
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
		this.literal("do");
		this.literal("\n");
		node.getBody().accept(this);
		this.literal("while");
		node.getExpression().accept(this);
        return false;
    }
	
	public boolean visit(EnhancedForStatement node)  {
		this.literal("for");
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
	public boolean visit(ExpressionStatement node) {
		node.getExpression().accept(this);
		this.literal("\n");
		return false;
	}

	public boolean visit(FieldAccess node)  {
		node.getExpression().accept(this);
		this.literal("."); 
		this.literal(node.getName().toString());
        return false;
    }
	
	@SuppressWarnings("unchecked")
	public boolean visit(FieldDeclaration node)  {
		this.printModifiers(node.modifiers());
		this.literal(node.getType().toString());
		List<VariableDeclarationFragment> fragments = node.fragments();
		for(VariableDeclarationFragment f : fragments) {
			f.accept(this);
		}
        return false;
    }
	
	@SuppressWarnings("unchecked")
	public boolean visit(ForStatement node)  { 
		this.literal("for");
		List<Expression> initializers = node.initializers();
		for(Expression initializer : initializers) {
			initializer.accept(this);
		}
		Expression exp = node.getExpression();
		if(exp != null) {
			exp.accept(this);
		}
		List<Expression> updaters = node.updaters();
		for(Expression updater : updaters) {
			updater.accept(this);
		}
		node.getBody().accept(this);
        return false;
    }
	
	public boolean visit(IfStatement node)  {
		this.literal("if");
		node.getExpression().accept(this);
		this.literal("\n");
		this.literal("then");
		node.getThenStatement().accept(this);
		if(node.getElseStatement() != null) {
		this.literal("else");
		node.getElseStatement().accept(this);
		}
        return false;
    }
	
	public boolean visit(ImportDeclaration node)  {
		this.literal("import");
		this.literal(node.getName().toString());
        return false;
    }
	
	public boolean visit(InfixExpression node)  {
		node.getLeftOperand().accept(this);
		this.literal(node.getOperator().toString());
		node.getRightOperand().accept(this);
        return false;
    }
	
	
	public boolean visit(InstanceofExpression node)  {
		node.getLeftOperand().accept(this);
		this.literal("instanceof");
		node.getRightOperand().accept(this);
        return false;
    }
	
// JLS 8 boolean visit(IntersectionType node)  {
//        return true;
//    }

	public boolean visit(LabeledStatement node)  {
		this.literal(node.getLabel() + ":");
		node.getBody().accept(this);
        return false;
    }

	
	private void printTypeParameters(List<TypeParameter> tParams) {
		if(tParams.size() > 0) {
			this.literal("<");
			for(TypeParameter t : tParams) {
				this.literal(t.toString());
			}
			this.literal(">");
		}
	}
	
	private void printTypeArguments(List<Type> tParams) {
		if(tParams.size() > 0) {
			this.literal("<");
			for(Type t : tParams) {
				this.literal(t.toString());
			}
			this.literal(">");
		}
	}
	
	private void printArguments(List<Expression> args) {
		for(Expression e : args) {
			e.accept(this);
		}
	}
	
	private void printModifiers(List<Modifier> modifiers) {
		for(Modifier mod : modifiers) {
			this.literal(mod.toString());
		}
	}
	@SuppressWarnings("unchecked")
	public boolean visit(MethodDeclaration node)  {

		// am omitting extraDimensions, receiverQualifier, and receiverType properties for now
		this.printModifiers(node.modifiers());
		if(!node.isConstructor()) {
			Type returnType = node.getReturnType2();
			this.literal(returnType.toString());
		}
		this.literal(node.getName().toString());
		List<TypeParameter> typeParameters = node.typeParameters();
		this.printTypeParameters(typeParameters);
		
		// FIXME: varargs are stupid node.isVarargs();
		List<SingleVariableDeclaration> params = node.parameters();
		for(SingleVariableDeclaration p : params) {
			p.accept(this);
		}
		@SuppressWarnings("deprecation")
		List<Type> thrownExceptions = node.thrownExceptions();
		if(thrownExceptions.size() > 0) {
			this.literal("throws");
			for(Type t : thrownExceptions) {
				this.literal(t.toString());
			}
		}
		this.literal("\n");
		node.getBody().accept(this);
        return false;
    }
	

	@SuppressWarnings("unchecked")
	public boolean	visit(MethodInvocation node)  {
		if(node.getExpression() != null) {
			node.getExpression().accept(this);
			this.literal("."); // FIXME: dot?
		}
		this.printTypeArguments(node.typeArguments());
		this.literal(node.getName().toString());
		this.printArguments(node.arguments());
        return false;
    }

//	FIXME: boolean visit(NameQualifiedType node)  {
//        return true;
//    }

	public boolean visit(NullLiteral node)  {
		this.literal("NULL");
        return false;
    }
	
	public boolean visit(NumberLiteral node)  {
		this.literal(node.toString());
        return false;
    }
	
	public boolean visit(PackageDeclaration node)  {
		this.literal("package"); 
		this.literal(node.getName().toString());
		this.literal("\n");
        return false;
    }

	public boolean visit(PostfixExpression node)  {
		node.getOperand().accept(this);
		this.literal(node.getOperator().toString());
        return false;
    }
	
	public boolean visit(PrefixExpression node)  {
		this.literal(node.getOperator().toString());
		node.getOperand().accept(this);
        return false;
    }
	
	public boolean visit(PrimitiveType node)  {
		this.literal(node.toString());
        return false;
    }
	
  public boolean visit(QualifiedName node)  { // FIXME: missing dots, the sad face
	 Name qualifier = node.getQualifier();
	 this.literal(qualifier.toString());
	 this.literal(node.getName().getIdentifier());
        return false;
   }
	
//	FIXME: public boolean visit(QualifiedType node)  {
//        return true;
//    }
	
	public boolean visit(ReturnStatement node)  {
		this.literal("return");
		Expression retval = node.getExpression();
		if(retval != null) {
			retval.accept(this);
		}
		return false;
    }
	
	public boolean visit(SimpleName node)  {
		this.literal(node.getIdentifier());
        return false;
    }
	
	public boolean	visit(SimpleType node)  {
		this.literal(node.getName().toString());
        return false;
    }

	@SuppressWarnings("unchecked")
	public boolean visit(SingleVariableDeclaration node)  {
		this.printModifiers(node.modifiers());
		this.literal(node.getType().toString());
		this.literal(node.getName().toString());
		if(node.getInitializer() != null) {
			this.literal("=");
			node.getInitializer().accept(this);
		}
		return false;
    }
	
	public boolean visit(StringLiteral node)  {
		this.literal(node.getLiteralValue());
        return false;
    }
	
	@SuppressWarnings("unchecked")
	public boolean visit(SuperConstructorInvocation node)  {
		Expression exp = node.getExpression();
		if(exp != null) {
			exp.accept(this);
		}
		this.printTypeArguments(node.typeArguments());
		this.literal("super");
		List<Expression> args = node.arguments();
		for(Expression e : args) {
			e.accept(this);
		}
        return false;
    }
	
	public boolean visit(SuperFieldAccess node)  {
		this.literal("super"); // FIXME: change all getName().toString() to getName.getIdentifier
		this.literal(".");
		this.literal(node.getName().toString());
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
			this.literal("default");
		} else {
			this.literal("case");
			node.getExpression().accept(this);
		}
		return false;
    }
	
	@SuppressWarnings("unchecked")
	public boolean visit(SwitchStatement node)  {
		this.literal("switch");
		node.getExpression().accept(this);
		List<Statement> stmts = node.statements();
		for(Statement s: stmts) {
			s.accept(this);
		}
		return false;
    }
	
//	FIXME: deal with this later
//   boolean visit(SynchronizedStatement node)  {
//        return true;
//    }

	public boolean visit(ThisExpression node)  {
		this.literal("this");
        return false;
    }
	
	public boolean visit(ThrowStatement node)  {
		this.literal("throw");
		node.getExpression().accept(this);
		this.literal("\n");
		return false;
    }
	
	@SuppressWarnings("unchecked")
	public boolean visit(TryStatement node)  {
		// FIXME: with resources not handled
		this.literal("try");
		node.getBody().accept(this);
		List<CatchClause> catchClauses = node.catchClauses();
		for(CatchClause c : catchClauses) {
			this.literal("catch");
			c.getException().accept(this);
			c.getBody().accept(this);
		}
		Block f = node.getFinally();
		if(f != null) {
			this.literal("finally");
			f.accept(this);
		}
		return false;
    }
	

	@SuppressWarnings("unchecked")
	public boolean visit(TypeDeclaration node)  {
		this.printModifiers(node.modifiers());

		if(node.isInterface()) {
			List<Type> superTypes = node.superInterfaceTypes();
			this.literal("interface");
			this.literal(node.getName().toString());
			if(superTypes.size() > 0) {
				this.literal("extends");
			}
			for(Type t : superTypes) { 
				this.literal(t.toString());
			}
		} else {
			this.literal("class");
			this.literal(node.getName().toString());
			Type superType = node.getSuperclassType();
			if(superType != null) {
				this.literal("extends");
				this.literal(superType.toString());
			}
		}
		List<TypeParameter> tParams = node.typeParameters();
		this.printTypeParameters(tParams);
		this.literal("\n");
		List<BodyDeclaration> bodyDecls = node.bodyDeclarations();
		for(BodyDeclaration b : bodyDecls) {
			b.accept(this);
		}
        return false;
    }

	public boolean visit(TypeLiteral node)  {
		this.literal(node.getType().toString());
        return false;
    }
	
// JLS 8: boolean visit(TypeMethodReference node)  {
//        return true;
//    }

//	FIXME: Bored boolean visit(UnionType node)  {
//        return true;
//    }

	@SuppressWarnings("unchecked")
	public boolean visit(VariableDeclarationExpression node) {
		this.printModifiers(node.modifiers());
		node.getType().accept(this);
		for(VariableDeclarationFragment f : ((List<VariableDeclarationFragment>) node.fragments())) {
			f.accept(this);
		}
		return false;
	}
	@SuppressWarnings("unchecked")
	public boolean visit(VariableDeclarationStatement node) {
		this.printModifiers(node.modifiers());
		node.getType().accept(this);
		for(VariableDeclarationFragment f : ((List<VariableDeclarationFragment>) node.fragments())) {
			f.accept(this);
		}
		this.literal("\n");
		return false;
		
	}
	public boolean visit(VariableDeclarationFragment node) {
		this.literal(node.getName().toString());
		if(node.getInitializer() != null) {
			this.literal("=");
			node.getInitializer().accept(this);
		}
		return false;
	}

	 public boolean visit(WhileStatement node)  {
		 this.literal("while");
		 node.getExpression().accept(this);
		 this.literal("\n");
		 node.getBody().accept(this);
        return false;
    }

 

}
