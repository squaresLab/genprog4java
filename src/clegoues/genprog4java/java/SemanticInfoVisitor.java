package clegoues.genprog4java.java;


import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import clegoues.genprog4java.rep.JavaRepresentation;

// FIXME: still too PAR-y for my taste, come back to it.

public class SemanticInfoVisitor extends ASTVisitor
{
	
	private String sourcePath;
	
	private List<ASTNode> nodeSet;
	private ScopeInfo scopes;
	
	private TreeSet<String> fieldName;
	private TreeSet<String> currentMethodScope;
	
	
// FIXME possibly: for the time being, we number *after* parsing, and not here
	// unlike in the OCaml implementation, this only collects the statements and the
	// semantic information.  It doesn't number.
	private CompilationUnit cu;

	
	public void init(String p)
	{
		this.sourcePath = p;
	}
	
	public SemanticInfoVisitor()
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


	
	// there was a preVisit method here that initially set up the statements we were considering for repair and put their info
	// somewhere.  Let's do that post-parse/numvisit/semantic collection setup.
	
	public void preVisit(ASTNode node)
	{	

			if(JavaRepresentation.canRepair(node))
			{				
				// add scope information
				TreeSet<String> newScope = new TreeSet<String>();
				//newScope.addAll(this.fieldName);
				newScope.addAll(this.currentMethodScope);
				this.scopes.addScope4Stmt(node, newScope); // FIXME: possibly we only need this info for faulty statements, but whatever
				this.nodeSet.add(node);
			}
		
		
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
