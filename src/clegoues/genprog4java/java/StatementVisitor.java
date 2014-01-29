package clegoues.genprog4java.java;


import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

// FIXME: grabbed from PAR

public class StatementVisitor extends ASTVisitor
{
	
	private String sourcePath;
	
	private List<ASTNode> nodeSet;
	private HashMap<Integer, ASTNode> buggyStmt;
	private ScopeInfo scopes;
	
	private TreeSet<String> fieldName;
	private TreeSet<String> currentMethodScope;
	
	private boolean isExp = true;
	
	private int count = 0;

	private CompilationUnit cu;

	
	public void init(String p)
	{
		this.sourcePath = p;
	}
	
	public StatementVisitor()
	{
		this.fieldName = new TreeSet<String>();
		this.fieldName.add("this");
	}
	
	public Set<String> getFieldSet()
	{
		return this.fieldName;
	}
	
	public void setNodeSet( List<ASTNode> o )
	{
		this.nodeSet = o;
	}
	
	public List<ASTNode> getNodeSet()
	{
		return this.nodeSet;
	}
	
	public void setScopeList(ScopeInfo scopeList)
	{
		this.scopes = scopeList;
	}
	
	@Override
	public boolean visit(FieldDeclaration node)
	{
		for(Object o : node.fragments())
		{
			if(o instanceof VariableDeclarationFragment)
			{
				VariableDeclarationFragment v = (VariableDeclarationFragment)o;
				this.fieldName.add(v.getName().getIdentifier());
			}
		}
		return super.visit(node);
	}
	
	
	
	
	@Override
	public boolean visit(MethodDeclaration node)
	{
		this.currentMethodScope = new TreeSet<String>();
		
		for(Object o : node.parameters())
		{
			if(o instanceof SingleVariableDeclaration)
			{
				SingleVariableDeclaration v = (SingleVariableDeclaration)o;
				this.currentMethodScope.add(v.getName().getIdentifier());
			}
		}
		
		return super.visit(node);
	}
	
	

	@Override
	public boolean visit(Initializer node)
	{
		List mods = node.modifiers();

		for(Object o : mods)
		{
			if(o instanceof Modifier)
			{
				if(((Modifier) o).isStatic())
				{
					this.currentMethodScope = new TreeSet<String>();
				}
			}
		}
			
		return super.visit(node);
	}

	
	
	@Override
	public void endVisit(Initializer node)
	{
		super.endVisit(node);
	}

	@Override
	public void endVisit(MethodDeclaration node)
	{
		super.endVisit(node);
	}

	
	
	@Override
	public boolean visit(VariableDeclarationStatement node)
	{
		for(Object o : node.fragments())
		{
			if(o instanceof VariableDeclarationFragment)
			{
				VariableDeclarationFragment v = (VariableDeclarationFragment)o;
				//logger.info(v.toString() + "[" + ASTUtils.getStatementLineNo(v)+"]");
				this.currentMethodScope.add(v.getName().getIdentifier());
			}
		}
		return super.visit(node);
	}

	public void setBuggySet(HashMap<Integer, ASTNode> buggyStmt)
	{
		this.buggyStmt = buggyStmt;
	}

	
	@Override
	public void preVisit(ASTNode node)
	{	
		for(int bugPos : Constants.buggy)
		{
			if(ASTUtils.getStatementLineNo(node) == bugPos && node instanceof Statement && !(node instanceof Block))
			{
				this.buggyStmt.put(bugPos, node);
				logger.info("Buggy Node: " + node.toString() + "\n");
				
				// add scope information
				TreeSet<String> newScope = new TreeSet<String>();
				//newScope.addAll(this.fieldName);
				newScope.addAll(this.currentMethodScope);
				this.scopes.addScope4Stmt(node, newScope);
			}
		}
	
		if(node instanceof ExpressionStatement || node instanceof AssertStatement
				|| node instanceof BreakStatement || node instanceof ContinueStatement
				|| node instanceof LabeledStatement || node instanceof ReturnStatement
				|| node instanceof ThrowStatement || node instanceof VariableDeclarationStatement
				|| node instanceof IfStatement)
			this.nodeSet.add(node);
		/*else if (node instanceof Statement)
		{
			this.nodeSet.add(node.getAST().newIfStatement());
		}*/
			
		
		
		
		super.preVisit(node);
	}

	public void setCompilationUnit(CompilationUnit ast)
	{
		this.cu = ast;
	}
	
	public CompilationUnit getCompilationUnit()
	{
		return this.cu;
	}
}
