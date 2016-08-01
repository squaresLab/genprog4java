package clegoues.genprog4java.localization;

import static clegoues.util.ConfigurationBuilder.STRING;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import clegoues.genprog4java.Search.GiveUpException;
import clegoues.genprog4java.fitness.Fitness;
import clegoues.genprog4java.java.ASTUtils;
import clegoues.genprog4java.java.JavaSemanticInfo;
import clegoues.genprog4java.main.Configuration;
import clegoues.genprog4java.mut.holes.java.JavaASTNodeLocation;
import clegoues.genprog4java.mut.holes.java.JavaLocation;
import clegoues.genprog4java.rep.Representation;
import clegoues.genprog4java.rep.UnexpectedCoverageResultException;
import clegoues.genprog4java.treelm.TreeBabbler;
import clegoues.util.ConfigurationBuilder;
import clegoues.util.GlobalUtils;
import clegoues.util.ConfigurationBuilder.LexicalCast;
import codemining.ast.TreeNode;
import codemining.lm.tsg.FormattedTSGrammar;
import codemining.lm.tsg.TSGNode;
import codemining.lm.tsg.samplers.CollapsedGibbsSampler;
import codemining.util.serialization.ISerializationStrategy.SerializationException;
import codemining.util.serialization.Serializer;

public class EntropyLocalization extends DefaultLocalization {
	protected static Logger logger = Logger.getLogger(EntropyLocalization.class);

	public static final ConfigurationBuilder.RegistryToken token =
			ConfigurationBuilder.getToken();

	public static TreeBabbler babbler = ConfigurationBuilder.of(
			new LexicalCast< TreeBabbler >() {
				public TreeBabbler parse(String value) {
					if ( value.equals( "" ) )
						return null;
					try {
						FormattedTSGrammar grammar =
								(FormattedTSGrammar) Serializer.getSerializer().deserializeFrom( value );
						return new TreeBabbler( grammar );
					} catch (SerializationException e) {
						logger.error( e.getMessage() );
						return null;
					}
				}
			}
			)
			.inGroup( "Entropy Parameters" )
			.withFlag( "grammar" )
			.withVarName( "babbler" )
			.withDefault( "" )
			.withHelp( "grammar to use for babbling repairs" )
			.build();



	public EntropyLocalization(Representation orig) throws IOException, UnexpectedCoverageResultException {
		super(orig);
	}

	@Override
	protected void computeLocalization() throws UnexpectedCoverageResultException, IOException {
		logger.info("Start Fault Localization");
		TreeSet<Integer> negativePath = getPathInfo(DefaultLocalization.negCoverageFile, Fitness.negativeTests, false);

		for (Integer i : negativePath) {
			faultLocalization.add(original.instantiateLocation(i, 1.0));
		}
	}

	@Override
	public void reduceSearchSpace() {
		// Does nothing, at least for now.
	}

	@Override
	public Location getRandomLocation(double weight) {
		JavaLocation startingStmt = (JavaLocation) GlobalUtils.chooseOneWeighted(new ArrayList(this.getFaultLocalization()), weight);
		ASTNode actualCode = startingStmt.getCodeElement();
		List<ASTNode> decomposed = ASTUtils.decomposeASTNode(actualCode);
		decomposed.add(actualCode);
		Collections.shuffle(decomposed, Configuration.randomizer);
		ASTNode selected = decomposed.get(0);
		System.err.println("SELECTED: " + selected);
		return new JavaASTNodeLocation(startingStmt, selected.getParent());
		//		double maxProb = Double.NEGATIVE_INFINITY;
		//		ASTNode biggestSoFar = actualCode;
		//		for(ASTNode node : decomposed) {
		//			TreeNode< TSGNode > asTlm = babbler.eclipseToTreeLm(node);
		//			double prob = babbler.grammar.computeRulePosteriorLog2Probability(asTlm);
		//			double entropy = -prob * Math.exp(prob);
		//			System.err.println(node);
		//			System.err.println(prob);
		//			System.err.println("entropy:" + entropy);
		//			System.err.println();
		//
		//			if(prob > maxProb) {
		//				maxProb = prob;
		//				biggestSoFar = node;
		//			}
		//		}
		//
		//		System.err.println("biggest found:");
		//		System.err.println(biggestSoFar);
		//		System.err.println(maxProb);
		//
		//		return new JavaASTNodeLocation(biggestSoFar.getParent());
	}


	private class BabbleVisitor extends ASTVisitor {
		/*  note that there are many things I'm not including in this handling.  
		 *  Some of them don't need to be dealt with explicitly (such as parenthesized expressions
		 *  or if statements).  Others might be handled, in a perfect world, such as:
		 *  labeled statements: I'm assuming that, as in C, labels should be unique (within some scope)
		 *  method, class, or field declarations: I'm hoping the babbler never does this
		 *  synchronized statements: too hard 
		 *  
		 *  This one is in the middle somewhere, in that I may need to figure it out
		 *  at some point:
		 *  SimpleType, QualifiedType, NameQualifiedType: these may be "dealt with" via their parent nodes
		 *  
		 *  
		 *  Some things I'm putting in visit methods to think about handling later, as
		 *  reminders, like:
		 *  try statements: should we ensure that whatever's being tried
		 *  can throw an exception, and that the type matches the catch? This sounds tricky
		 *  but not impossible and related exception-handling or -throwing constructs.
		 *  switch statements: matching of types?
		 */
		// note to self: many of these checks are already performed by java statement and 
		// java semantic info.  Perhaps we can avoid duplicating all that code as much as possible?
		
		// FIXME: will the babbler ever do something completely bizarre, like babble a field
		// declaration inside a method?
		private JavaSemanticInfo semanticInfo = null;
		private JavaLocation location = null;
		private Stack<String> expectedTypeStack = new Stack<String>();
		private ASTNode parentNode = null;
		private Set<String> namesInScope = null;
		
		public BabbleVisitor(JavaSemanticInfo info, JavaLocation location, ASTNode parent) {
			this.semanticInfo = info;
			this.location = location;
			this.parentNode = parent;
			Set<String> classScope = JavaSemanticInfo.classScopeMap.get(location.getId());
			Set<String> methodScope = JavaSemanticInfo.methodScopeMap.get(location.getId());
			namesInScope = new HashSet<String>(methodScope);
			namesInScope.addAll(classScope);
		}
		
		// FIXME: hm.  Do I need to check that the thing being accessed is in
		// scope and also an array?  Possibly.
		public boolean visit(ArrayAccess node) {
			return true;
		}
		// FIXME: type exists.  Possibly taken care of by Assignment, not sure yet
		public boolean visit(ArrayCreation node) {
			return true;
		}
		// FIXME: possibly handled by creation/assignment
		public boolean visit(ArrayInitializer node) {
			return true;
		}
		
		// FIXME: name exists, types match (as much as possible?)
		public boolean visit(Assignment node) {
			return true;
		}

		// FIXME: ensure that it's inside a loop or switch
		public boolean visit(BreakStatement node) {
			return true;
		}
		
		// FIXME: ensure the types being cast/or checked against are is in scope/valid
		public boolean visit(CastExpression node) {
			return true;
		}
		
		public boolean visit(InstanceofExpression node) {
			return true;
		}
		
		// FIXME: constructor and super constructors may only be called inside a constructor
		// and only at the top.
		public boolean visit(ConstructorInvocation node) {
			return true;
		}
		
		public boolean visit(SuperConstructorInvocation node) {
			return true;
		}
		

		// FIXME: ensure that it's inside a loop
		public boolean	visit(ContinueStatement node) {
			return true;
		}

		// FIXME: ensure field exists
		public boolean visit(FieldAccess node) {
			return true;
		}
		
		public boolean visit(SuperFieldAccess node) {
			return true;
		}
		// FIXME: ensure the loop control variables are fresh
		public boolean visit(EnhancedForStatement node) {
			return true;
		}
		public boolean visit(ForStatement node) {
			return true;
		}

		// FIXME: ensure method exists, parameters exist (or punt to other handles?)
		public boolean visit(MethodInvocation node) {
			return true;
		}
		
		public boolean visit(SuperMethodInvocation node) {
			return true;
		}
		
		/* checks return: if void or null, should return nothing (sets expression
		 * to null); if not, and expression is null, returns a default expression.
		 * Does not presently try to fix a return that has an expression but of the incorrect type */
		public boolean visit(ReturnStatement node) {
			ASTNode enclosingMethod = ASTUtils.getEnclosingMethod(this.location.getCodeElement());
			MethodDeclaration md = (MethodDeclaration) enclosingMethod;
			Type returnType = md.getReturnType2();
			boolean shouldReturnNull = returnType == null ||
					((returnType instanceof PrimitiveType) &&
							((PrimitiveType)returnType).getPrimitiveTypeCode() == PrimitiveType.VOID); 
			if(shouldReturnNull) {
				node.setExpression(null);
				return true;
			}
			if(node.getExpression() == null) {
				Expression defaultExp = (Expression) ASTUtils.getDefaultReturn(md, location.getCodeElement().getAST());
				node.setExpression(defaultExp);
				return false;
			}
			return true;
		}
		
		// FIXME: names exist. If yes, leave alone.  If no, see if we've
		// renamed already (like in a previous var decl; will check that names are fresh in
		// declarations). Qualified names suck. 
		public boolean visit(QualifiedName node) {
			return true;
		}
		public boolean visit(SimpleName node) {
			if(!namesInScope.contains(node.getIdentifier())) {
				ArrayList<String> allNames = new ArrayList<String>(namesInScope);
				Collections.shuffle(allNames,Configuration.randomizer);
				String picked = allNames.get(0);
				node.setIdentifier(picked);
			}
			return true;
		}
		
		// FIXME: variables decled need to be unique, subject to various rules
		// and may populate a symbol table for later renaming, we'll see.
		public boolean visit(SingleVariableDeclaration node) { 
			return true;
		}
		
		public boolean visit(VariableDeclarationExpression node) {
			return true;
		}
		
		public boolean visit(VariableDeclarationFragment node) {
			return true;
		}
		
		public boolean visit(VariableDeclarationStatement node) {
			return true;
		}

		
		// FIXME: leaving as reminders, see comment at start of class
		public boolean visit(ClassInstanceCreation node) { // class exists?
			return true;
		}
		public boolean visit(SwitchCase node) {
			return true;
		}
		public boolean visit(SwitchStatement node) {
			return true;
		}
		public boolean visit(CatchClause node) {
			return true;
		}
		public boolean visit(ThrowStatement node) {
			return true;
		}
		public boolean visit(TryStatement node) {
			return true;
		}

	}
	// babbles fix code, manipulates it to reference in-scope variables
	public ASTNode babbleFixCode(JavaLocation location, JavaSemanticInfo semanticInfo) {
		ASTNode element = location.getCodeElement();
		ASTNode babbled = babbler.babbleFrom(element);
		System.err.println("babbled:" + babbled.toString());
		babbled.accept(new BabbleVisitor(semanticInfo, location, element.getParent()));
		System.err.println("replaced:" + babbled.toString());
		return babbled; 
	}

	@Override
	public Location getNextLocation() throws GiveUpException {
		Location ele = super.getNextLocation();
		// FIXME
		return ele;
	}
}
