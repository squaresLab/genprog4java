package clegoues.genprog4java.treelm;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclaration;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSortedMap;

import clegoues.genprog4java.treelm.ChainedJavaTreeExtractor.PostProcess;
import clegoues.util.ConfigurationBuilder;
import codemining.ast.AstNodeSymbol;
import codemining.ast.TreeNode;

public class VariableAbstractor implements
	java.util.function.Supplier< ChainedJavaTreeExtractor.PostProcess >,
	ChainedJavaTreeExtractor.PostProcess,
	Serializable
{
	private static final long serialVersionUID = 20161006;
	
	public static final ConfigurationBuilder.RegistryToken token =
		ConfigurationBuilder.getToken();
	
	/**
	 * Map from command-line arguments to naming strategies.
	 */
	private static Map< String, Supplier< NamingStrategy > > strategies =
		ImmutableSortedMap.< String, Supplier< NamingStrategy > >naturalOrder()
			.put( "naive", () -> NaiveNamingStrategy.INSTANCE )
			.build();
	
	/**
	 * The naming strategy to use when decoding integer trees into ASTs.
	 */
	private static NamingStrategy naming =
		ConfigurationBuilder.< NamingStrategy >of( ( s ) -> {
			if ( strategies.containsKey( s ) )
				return strategies.get( s ).get();
			else {
				StringBuilder msg =
					new StringBuilder( "unknown naming strategy '" );
				msg.append( s );
				msg.append( "': must be one of" );

				int i = 0;
				for ( String key : strategies.keySet() ) {
					if ( i > 0 )
						msg.append( ',' );
					msg.append( ' ' );
					msg.append( key );
					i += 1;
				}
				throw new IllegalArgumentException( msg.toString() );
			}
		} )
		.inGroup( "Grammar-Based Insertions" )
		.withVarName( "naming" )
		.withFlag( "varname-strategy" )
		.withHelp( "the strategy to use for variable names in babbled code" )
		.withDefault( "naive" )
		.build();

	/**
	 * Replaces variable nodes in the tree with placeholders that record the
	 * type but no name. Non-variable nodes are not modified.
	 * 
	 * @param tree         the tree node to modify
	 * @param node         the AST node it was extracted from
	 * @param getSymbolId  callback to get an int representing a symbol
	 * @param getSymbol    callback to get the symbol represented by an int
	 * 
	 * @return the modified tree node
	 */
	@Override
	public TreeNode< Integer > encode(
		TreeNode< Integer > tree, ASTNode node,
		Function< AstNodeSymbol, Integer > getSymbolId,
		Function< Integer, AstNodeSymbol > getSymbol
	) {
		// We are only interested in SimpleName nodes that represent a variable
		// or field. Everything else we return unchanged.
		
		if ( node.getNodeType() != ASTNode.SIMPLE_NAME )
			return tree;
		IBinding binding = ( (SimpleName) node ).resolveBinding();
		if ( binding == null || binding.getKind() != IBinding.VARIABLE )
			return tree;
		
		// We will replace the variable with a TEMPLATE_NODE node annotated with
		// the fully qualified type name.
		
		IVariableBinding var = (IVariableBinding) binding;
		String typeName = var.getType().getQualifiedName();
		AstNodeSymbol symbol = new AstNodeSymbol( AstNodeSymbol.TEMPLATE_NODE );
		symbol.addAnnotation( "TYPE", typeName );
		return TreeNode.create( getSymbolId.apply( symbol ), 0 );
	}

	/**
	 * Replaces variable placeholders with real names again. Uses
	 * {@link #naming} and the {@link SymbolTable} to select in-scope names with
	 * the correct types or fresh variable names, as appropriate. Since the
	 * decoded tree may introduce new scopes and new variables in those scopes,
	 * additional symbol tables may be constructed and discarded during the
	 * course of decoding.
	 * 
	 * @param tree              the tree to modify
	 * @param getOrAddSymbolId  callback to get an int representing a symbol
	 * @param getSymbol         callback to get the symbol represented by an int
	 * @param st                symbol table with names in scope at the root of
	 *                          the tree
	 * 
	 * @return the modified tree.
	 */
	@Override
	public TreeNode< Integer > decode(
		TreeNode< Integer > tree,
		Function< AstNodeSymbol, Integer > getOrAddSymbolId,
		Function< Integer, AstNodeSymbol > getSymbol,
		SymbolTable st
	) {
		if ( names == null || ! names.containsKey( tree ) )
			initializeScopes( tree, st, getSymbol );

		if ( getSymbol.apply( tree.getData() ).nodeType == AstNodeSymbol.TEMPLATE_NODE ) {
			String name = names.get( tree ).get();
			AstNodeSymbol symbol = new AstNodeSymbol( ASTNode.SIMPLE_NAME );
			symbol.addSimpleProperty(
				SimpleName.IDENTIFIER_PROPERTY.getId(), name
			);
			Integer varId = getOrAddSymbolId.apply( symbol );
			return TreeNode.create( varId, 0 );
		} else
			return tree;
	}

	/**
	 * Bookkeeping for adding variable declarations to a scope. The visitation
	 * order processes newly declared variable names before their initializers.
	 * Since a declared variable is not in scope for its initializer, we need
	 * to swap the order, processing the initializer first, then adding the
	 * variable after we're done processing declaration node. This object
	 * contains the bookkeeping info we need to do that.
	 * 
	 * @author jonathan
	 */
	private static class Resolution {
		/**
		 * The list of variable suppliers to add the declared variable to. This
		 * will either be a list of fields or a list of variables, depending on
		 * the type of declaration node we are processing.
		 */
		public List< Supplier< String > > kind;
		
		/**
		 * The delayed processing to enqueue adding the declared variable after
		 * any variables in its initializer are resolved. Note that we are
		 * enqueuing further delayed computation. That is, the action of
		 * selecting a name is enqueued; however, the actual name will not be
		 * chosen until even later.
		 */
		public Supplier< Supplier< String > > resolve;
	}

	/**
	 * Collects a symbol table with the postponed computation needed to update
	 * it. The variables and fields declared in this scope are kept separate,
	 * since all fields are available in any daughter scopes, whether they are
	 * declared before the daughter scope or not. However, only the variables
	 * declared before the daughter scope are available in it.
	 * <p>
	 * Note that Java requires that fields be declared in different sorts of
	 * scopes than variables (e.g., class bodies vs method bodies). This means
	 * that at most one of the two lists of computations will include
	 * computations that directly allocate new names.
	 * </p>
	 * 
	 * @author jonathan
	 */
	private static class Scope {
		/**
		 * Creates a new scope, where the parent scope's variables are kept in
		 * the given symbol table.
		 * 
		 * @param parent table containing the parent scope's variables
		 */
		public Scope( SymbolTable parent ) {
			this.symbols = new NestedSymbolTable( parent );
			this.variables = new ArrayList<>();
			this.fields = new ArrayList<>();
			this.resolution = null;
		}
		
		/**
		 * Creates a new scope as a daughter of the given scope and dependent on
		 * its fields.
		 * 
		 * @param parent parent scope to depend on
		 */
		private Scope( Scope parent ) {
			this( parent.symbols );

			// Add a phony field that depends on the current parent variable
			// names and all parent field names. This way, when anything that
			// depends on our in-scope fields is evaluated, it will trigger
			// evaluation of the fields in all our parent scopes.

			int vid = parent.variables.size();
			this.fields.add( Suppliers.memoize( () -> {
				int fid = parent.fields.size();
				if ( vid > 0 )
					parent.variables.get( vid - 1 ).get();
				if ( fid > 0 )
					parent.fields.get( fid - 1 ).get();
				return (String)null;
			} ) );
		}
		
		/**
		 * Returns a subscope of the current scope.
		 * 
		 * @return a subscope of the current scope.
		 */
		public Scope subScope() {
			return new Scope( this );
		}
		
		/**
		 * Creates a delayed computation for getting the name of a variable. 
		 * Delegates the actual selection of the variable name to
		 * {@code getName}. The computation will depend on the most recent
		 * variable (if any) and the most recent field in this scopes queues.
		 * This ensures that all previously queued computations, including those
		 * that introduce new names into the symbol table, will be performed
		 * before this computation is run.
		 * 
		 * @param kind    the list of computations to extend
		 * @param getName callback to compute a variable name
		 * @param type    the type of the variable to name
		 * 
		 * @return a delayed computation to get the name
		 */
		private Supplier< String > addEntry(
			List< Supplier< String > > kind,
			BiFunction< SymbolTable, String, String > getName,
			String type
		) {
			int vid = variables.size();
			int fid = fields.size();
			Supplier< String > task = Suppliers.memoize( () -> {
				if ( vid > 0 )
					variables.get( vid - 1 ).get();
				if ( fid > 0 )
					fields.get( fid - 1 ).get();
				return getName.apply( symbols, type );
			} );
			kind.add( task );
			return task;
		}
		
		/**
		 * Returns a delayed computation to add a variable name computation to
		 * the given list. This allows the initializer for a declaration to be
		 * added to the list before the declared variable.
		 * 
		 * @param kind the list of computations to extend
		 * 
		 * @return a delayed computation to add a delayed computation
		 */
		private Runnable setDeclaration( List< Supplier< String > > kind ) {
			Resolution r = new Resolution();
			resolution = r;
			r.kind = kind;

			// r.resolve will be set when the name is requested, so we need the
			// lambda to reevaluate r.resolve when it runs, rather than
			// pre-computing it now.

			return () -> {
				if ( r.resolve == null )
					throw new CodeGenerationException(
						"declaration was never assigned a name"
					);
				r.resolve.get();
			};
		}
		
		/**
		 * Returns a delayed computation to add a new field name to the scope.
		 * The type of the field will be specified by the next call to
		 * {@link #getName(String)}.
		 * 
		 * @return a delayed computation to add a new field name.
		 */
		public Runnable setFieldDeclaration() {
			return setDeclaration( fields );
		}
		
		/**
		 * Returns a delayed computation to add a new variable name to the
		 * scope. The type of the field will be specified by the next call to
		 * {@link #getName(String)}.
		 * 
		 * @return a delayed computation to add a new variable name.
		 */
		public Runnable setVariableDeclaration() {
			return setDeclaration( variables );
		}
		
		/**
		 * Allocates a new name and adds it to the symbol table.
		 * 
		 * @param st   the symbol table of current names
		 * @param type the type of name to allocate
		 * 
		 * @return the newly allocated name
		 */
		private static String allocateName( SymbolTable st, String type ) {
			String name = naming.getUniqueName( st, type );
			if ( ! st.addVariable( type, name ) )
				throw new CodeGenerationException(
					"attempted to reallocate variable " + name
				);
			return name;
		}
		
		/**
		 * Retrieves the name of an in-scope variable with the given type.
		 * 
		 * @param st   the symbol table of current names
		 * @param type the type of name to retrieve
		 * 
		 * @return the requested name
		 */
		private static String retrieveName( SymbolTable st, String type ) {
			try {
				return naming.getInScopeName( st, type );
			} catch ( IllegalStateException e ) {
				throw new CodeGenerationException( e );
			}
		}
		
		/**
		 * Returns a delayed computation to compute a name and add it to the
		 * scope. If this is the first call to {@code getName} since the last
		 * call to either {@link #setFieldDeclaration()} or
		 * {@link #setVariableDeclaration()}, the computation will allocate a
		 * new name when it is run. Otherwise, the computation will select a
		 * name from the set of in-scope names at the time it is run.
		 * 
		 * @param type the type of variable 
		 * 
		 * @return a delayed computation to compute a name
		 */
		public Supplier< String > getName( String type ) {
			if ( resolution != null ) {
				Resolution r = resolution;
				resolution = null;
				List< Supplier< String > > target = r.kind;
				r.resolve = Suppliers.memoize( () ->
					addEntry( target, Scope::allocateName, type )
				);
				return Suppliers.memoize( () -> {
					return r.resolve.get().get();
				} );
			} else
				return addEntry( variables, Scope::retrieveName, type );
		}
		
		private final SymbolTable symbols;
		private final List< Supplier< String > > variables;
		private final List< Supplier< String > > fields;
		private Resolution resolution;
	}

	/**
	 * Nodes which introduce a new scope.
	 */
	private static final Class< ? >[] scopeNodes = {
		AbstractTypeDeclaration.class,
		AnonymousClassDeclaration.class,
		Block.class,
		CompilationUnit.class,
		EnhancedForStatement.class,
		ForStatement.class,
		MethodDeclaration.class
	};
	
	/**
	 * Nodes which introduce a new name into a scope.
	 */
	private static final Class< ? >[] declNodes = {
		EnumConstantDeclaration.class,
		FieldDeclaration.class,
		ImportDeclaration.class,
		VariableDeclaration.class
	};
	
	/**
	 * Check whether a node type is a subclass of any of the given root types.
	 * 
	 * @param nodeType  Eclipse AST node type constant to check
	 * @param rootTypes Eclipse AST node classes to check
	 * 
	 * @return true if {@code nodeType} represents a subclass of one of the
	 * given root types.
	 */
	private static boolean checkType(
		Function< Integer, AstNodeSymbol > getSymbol,
		TreeNode< Integer > node,
		Class< ? >... rootTypes
	) {
		AstNodeSymbol symbol = getSymbol.apply( node.getData() );
		try {
			Class< ? > type = ASTNode.nodeClassForType( symbol.nodeType );
			for ( Class< ? > rootType : rootTypes )
				if ( rootType.isAssignableFrom( type ) )
					return true;
		} catch ( IllegalArgumentException e ) {
			return false;
		}
		return false;
	}

	/**
	 * Initializes the {@link #names} map with callbacks to get names for the
	 * TEMPLATE_NODEs in the tree. These callbacks should be used in
	 * {@link #decode(TreeNode, Function, Function, SymbolTable)} to replace
	 * the nodes with appropriate SimpleName nodes.
	 * 
	 * @param tree      the tree to collect callbacks for
	 * @param base      the symbol table representing everything in scope at the
	 *                  root of the tree
	 * @param getSymbol callback to get a symbol given an integer
	 */
	private void initializeScopes(
		TreeNode< Integer > tree,
		SymbolTable base,
		Function< Integer, AstNodeSymbol > getSymbol
	) {
		names = new IdentityHashMap<>();
		
		Deque< Runnable > declStack = new ArrayDeque<>();
		Deque< Scope > scopeStack = new ArrayDeque<>();
		scopeStack.addLast( new Scope( base ) );

		TreeNodeUtils.visit( tree, ( t ) -> {
			// add null for every subtree, so that decode doesn't go crazy
			names.put( t, null );
			if ( checkType( getSymbol, t, scopeNodes ) ) {
				scopeStack.addLast( scopeStack.getLast().subScope() );
			} else if ( checkType( getSymbol, t, VariableDeclaration.class ) ) {
				Runnable r = scopeStack.getLast().setVariableDeclaration();
				declStack.addLast( r );
			} else if ( checkType( getSymbol, t, declNodes ) ) {
				Runnable r = scopeStack.getLast().setFieldDeclaration();
				declStack.addLast( r );
			} else {
				AstNodeSymbol symbol = getSymbol.apply( t.getData() );
				if ( symbol.nodeType == AstNodeSymbol.TEMPLATE_NODE ) {
					String type = (String)symbol.getAnnotation( "TYPE" );
					names.put( t, scopeStack.getLast().getName( type ) );
				}
			}
			return TreeNodeUtils.VisitResult.CONTINUE;
		}, ( t ) -> {
			if ( checkType( getSymbol, t, scopeNodes ) )
				scopeStack.removeLast();
			else if ( checkType( getSymbol, t, declNodes ) )
				declStack.getLast().run();
			return TreeNodeUtils.VisitResult.CONTINUE;
		} );
	}

	/**
	 * Returns this post-processor. Since this maintains no non-transient state,
	 * we do not need to construct a new object.
	 * 
	 * @return this post-processor.
	 */
	@Override
	public PostProcess get() {
		return this;
	}
	
	private transient Map< TreeNode< Integer >, Supplier< String > > names;
}
