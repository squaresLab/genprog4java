package clegoues.genprog4java.treelm;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimplePropertyDescriptor;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import codemining.ast.AstNodeSymbol;
import codemining.ast.TreeNode;
import codemining.ast.java.AbstractJavaTreeExtractor;
import codemining.ast.java.JavaAstPropertiesData;
import codemining.ast.java.JavaAstTreeExtractor;
import codemining.ast.java.VariableTypeJavaTreeExtractor;

public class PRVariableTypeExtractor extends VariableTypeJavaTreeExtractor {


	/**
	 * auto-generated
	 */
	private static final long serialVersionUID = -1591816848821216788L;

	private static final Logger logger = Logger.getLogger(
		PRVariableTypeExtractor.class.getName()
	);

	private AbstractJavaTreeExtractor myExtractor;
	
	public PRVariableTypeExtractor(AbstractJavaTreeExtractor extractor) {
		super();
		this.myExtractor = extractor;
	}
	

	/**
	 * Remove any template symbols in the tree.
	 *
	 * @param tree
	 * @return
	 */
	// this one is from the variable only grammar
	/*public TreeNode<Integer> detempletize(final TreeNode<Integer> fromTree) {
		if (getSymbol(fromTree.getData()).nodeType == AstNodeSymbol.TEMPLATE_NODE) {
			return detempletize(fromTree.getChild(0, 0));
		}
		final TreeNode<Integer> toTree = TreeNode.create(fromTree.getData(),
				fromTree.nProperties());
		final ArrayDeque<TreeNode<Integer>> toStack = new ArrayDeque<TreeNode<Integer>>();
		final ArrayDeque<TreeNode<Integer>> fromStack = new ArrayDeque<TreeNode<Integer>>();

		toStack.push(toTree);
		fromStack.push(fromTree);

		while (!toStack.isEmpty()) {
			final TreeNode<Integer> currentTo = toStack.pop();
			final TreeNode<Integer> currentFrom = fromStack.pop();

			final List<List<TreeNode<Integer>>> children = currentFrom
					.getChildrenByProperty();

			for (int i = 0; i < children.size(); i++) {
				final List<TreeNode<Integer>> childrenForProperty = children
						.get(i);

				for (final TreeNode<Integer> fromChild : childrenForProperty) {
					final AstNodeSymbol symbol = getSymbol(fromChild.getData());

					if (symbol.nodeType == AstNodeSymbol.TEMPLATE_NODE) {
						TreeNode<Integer> templateChild = fromChild;
						while (getSymbol(templateChild.getData()).nodeType == AstNodeSymbol.TEMPLATE_NODE) {
							checkArgument(templateChild.nProperties() == 1);

							if (templateChild.isLeaf()) {
								break;
							}
							templateChild = templateChild.getChild(0, 0);
						}
						if (templateChild.isLeaf()
								&& getSymbol(templateChild.getData()).nodeType == AstNodeSymbol.TEMPLATE_NODE) {
							continue;
						}
						final TreeNode<Integer> untempletizedCopyChild = TreeNode
								.create(templateChild.getData(),
										templateChild.nProperties());
						currentTo.addChildNode(untempletizedCopyChild, i);

					} else {
						final TreeNode<Integer> toChild = TreeNode.create(
								fromChild.getData(), fromChild.nProperties());
						currentTo.addChildNode(toChild, i);
						toStack.push(toChild);
						fromStack.push(fromChild);
					}
				}
			}
		}

		return toTree;
	}*/
	
	// question/understanding TODO: why is detempletize different between metavariable and variable-based grammars?
	// this one is from the TempletizedJavaTreeExtractor
	public TreeNode<Integer> detempletize(final TreeNode<Integer> fromTree) {
		if (getSymbol(fromTree.getData()).nodeType == AstNodeSymbol.TEMPLATE_NODE) {
			return detempletize(fromTree.getChild(0, 0));
		}
		final TreeNode<Integer> toTree = TreeNode.create(fromTree.getData(),
				fromTree.nProperties());
		final ArrayDeque<TreeNode<Integer>> toStack = new ArrayDeque<TreeNode<Integer>>();
		final ArrayDeque<TreeNode<Integer>> fromStack = new ArrayDeque<TreeNode<Integer>>();

		toStack.push(toTree);
		fromStack.push(fromTree);

		while (!toStack.isEmpty()) {
			final TreeNode<Integer> currentTo = toStack.pop();
			final TreeNode<Integer> currentFrom = fromStack.pop();

			final List<List<TreeNode<Integer>>> children = currentFrom
					.getChildrenByProperty();

			for (int i = 0; i < children.size(); i++) {
				final List<TreeNode<Integer>> childrenForProperty = children
						.get(i);

				for (final TreeNode<Integer> fromChild : childrenForProperty) {
					final AstNodeSymbol symbol = getSymbol(fromChild.getData());

					if (symbol.nodeType == AstNodeSymbol.TEMPLATE_NODE) {

						checkArgument(fromChild.nProperties() == 1);

						if (fromChild.isLeaf()) {
							continue;
						}
						final TreeNode<Integer> untempletizedChild = fromChild
								.getChild(0, 0);
						final TreeNode<Integer> untempletizedCopyChild = TreeNode
								.create(untempletizedChild.getData(),
										untempletizedChild.nProperties());
						currentTo.addChildNode(untempletizedCopyChild, i);

					} else {
						final TreeNode<Integer> toChild = TreeNode.create(
								fromChild.getData(), fromChild.nProperties());
						currentTo.addChildNode(toChild, i);
						toStack.push(toChild);
						fromStack.push(fromChild);
					}
				}
			}
		}

		return toTree;
	}


	// CLG hates having to duplicate this because it's easy/stupid/could even be static,
	// but she cannot be blamed for the choices of the treelm implementers.
	/**
	 * @param treeNode
	 * @param ast
	 * @param node
	 * @param symbol
	 * @return
	 * @throws Exception
	 */
	private ASTNode createASTNodeObject(final TreeNode<Integer> treeNode,
			final AST ast, final AstNodeSymbol symbol) throws Exception {
		switch (symbol.nodeType) {
		case ASTNode.ANNOTATION_TYPE_DECLARATION:
			return ast.newAnnotationTypeDeclaration();
		case ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION:
			return ast.newAnnotationTypeMemberDeclaration();
		case ASTNode.ANONYMOUS_CLASS_DECLARATION:
			return ast.newAnonymousClassDeclaration();
		case ASTNode.ARRAY_ACCESS:
			return ast.newArrayAccess();
		case ASTNode.ARRAY_CREATION:
			return ast.newArrayCreation();
		case ASTNode.ARRAY_INITIALIZER:
			return ast.newArrayInitializer();
		case ASTNode.ARRAY_TYPE:
			return ast.newArrayType(ast.newSimpleType(ast
					.newSimpleName("DUMMYTYPE")));
		case ASTNode.ASSERT_STATEMENT:
			return ast.newAssertStatement();
		case ASTNode.ASSIGNMENT:
			return ast.newAssignment();
		case ASTNode.BLOCK:
			return ast.newBlock();
		case ASTNode.BLOCK_COMMENT:
			return ast.newBlockComment();
		case ASTNode.BOOLEAN_LITERAL:
			return ast.newBooleanLiteral(false);
		case ASTNode.BREAK_STATEMENT:
			return ast.newBreakStatement();
		case ASTNode.CAST_EXPRESSION:
			return ast.newCastExpression();
		case ASTNode.CATCH_CLAUSE:
			return ast.newCatchClause();
		case ASTNode.CHARACTER_LITERAL:
			return ast.newCharacterLiteral();
		case ASTNode.CLASS_INSTANCE_CREATION:
			return ast.newClassInstanceCreation();
		case ASTNode.COMPILATION_UNIT:
			return ast.newCompilationUnit();
		case ASTNode.CONDITIONAL_EXPRESSION:
			return ast.newConditionalExpression();
		case ASTNode.CONSTRUCTOR_INVOCATION:
			return ast.newConstructorInvocation();
		case ASTNode.CONTINUE_STATEMENT:
			return ast.newContinueStatement();
		case ASTNode.CREATION_REFERENCE:
			return ast.newCreationReference();
		case ASTNode.DIMENSION:
			return ast.newDimension();
		case ASTNode.DO_STATEMENT:
			return ast.newDoStatement();
		case ASTNode.EMPTY_STATEMENT:
			return ast.newEmptyStatement();
		case ASTNode.ENHANCED_FOR_STATEMENT:
			return ast.newEnhancedForStatement();
		case ASTNode.ENUM_CONSTANT_DECLARATION:
			return ast.newEnumConstantDeclaration();
		case ASTNode.ENUM_DECLARATION:
			return ast.newEnumDeclaration();
		case ASTNode.EXPRESSION_METHOD_REFERENCE:
			return ast.newExpressionMethodReference();
		case ASTNode.EXPRESSION_STATEMENT:
			return ast.newExpressionStatement(ast.newCharacterLiteral());
		case ASTNode.FIELD_ACCESS:
			return ast.newFieldAccess();
		case ASTNode.FIELD_DECLARATION:
			return ast
					.newFieldDeclaration(ast.newVariableDeclarationFragment());
		case ASTNode.FOR_STATEMENT:
			return ast.newForStatement();
		case ASTNode.IF_STATEMENT:
			return ast.newIfStatement();
		case ASTNode.IMPORT_DECLARATION:
			return ast.newImportDeclaration();
		case ASTNode.INFIX_EXPRESSION:
			return ast.newInfixExpression();
		case ASTNode.INITIALIZER:
			return ast.newInitializer();
		case ASTNode.INSTANCEOF_EXPRESSION:
			return ast.newInstanceofExpression();
		case ASTNode.INTERSECTION_TYPE:
			return ast.newIntersectionType();
		case ASTNode.JAVADOC:
			return ast.newJavadoc();
		case ASTNode.LABELED_STATEMENT:
			return ast.newLabeledStatement();
		case ASTNode.LAMBDA_EXPRESSION:
			return ast.newLambdaExpression();
		case ASTNode.LINE_COMMENT:
			return ast.newLineComment();
		case ASTNode.MARKER_ANNOTATION:
			return ast.newMarkerAnnotation();
		case ASTNode.MEMBER_REF:
			return ast.newMemberRef();
		case ASTNode.MEMBER_VALUE_PAIR:
			return ast.newMemberValuePair();
		case ASTNode.METHOD_DECLARATION:
			return ast.newMethodDeclaration();
		case ASTNode.METHOD_INVOCATION:
			return ast.newMethodInvocation();
		case ASTNode.METHOD_REF:
			return ast.newMethodRef();
		case ASTNode.METHOD_REF_PARAMETER:
			return ast.newMethodRefParameter();
		case ASTNode.MODIFIER:
			return ast.newModifier(ModifierKeyword.PRIVATE_KEYWORD);
		case ASTNode.NAME_QUALIFIED_TYPE:
			return ast.newNameQualifiedType(ast.newName("DUMMY_QNAME"),
					ast.newSimpleName("DUMMY"));
		case ASTNode.NORMAL_ANNOTATION:
			return ast.newNormalAnnotation();
		case ASTNode.NULL_LITERAL:
			return ast.newNullLiteral();
		case ASTNode.NUMBER_LITERAL:
			return ast.newNumberLiteral("0");
		case ASTNode.PACKAGE_DECLARATION:
			return ast.newPackageDeclaration();
		case ASTNode.PARAMETERIZED_TYPE:
			return ast.newParameterizedType(ast.newSimpleType(ast
					.newSimpleName("DUMMY")));
		case ASTNode.PARENTHESIZED_EXPRESSION:
			return ast.newParenthesizedExpression();
		case ASTNode.POSTFIX_EXPRESSION:
			return ast.newPostfixExpression();
		case ASTNode.PREFIX_EXPRESSION:
			return ast.newPrefixExpression();
		case ASTNode.PRIMITIVE_TYPE:
			return ast.newPrimitiveType(PrimitiveType.CHAR);
		case ASTNode.QUALIFIED_NAME:
			return ast.newQualifiedName(ast.newName("DUMMY_QNAME"),
					ast.newSimpleName("DUMMY"));
		case ASTNode.QUALIFIED_TYPE:
			return ast.newQualifiedType(
					ast.newSimpleType(ast.newSimpleName("DUMMY_TYPE")),
					ast.newSimpleName("DUMMY"));
		case ASTNode.RETURN_STATEMENT:
			return ast.newReturnStatement();
		case ASTNode.SIMPLE_NAME:
			return ast.newSimpleName("DUMMY");
		case ASTNode.SIMPLE_TYPE:
			return ast.newSimpleType(ast.newName("DUMMY_NAME"));
		case ASTNode.SINGLE_MEMBER_ANNOTATION:
			return ast.newSingleMemberAnnotation();
		case ASTNode.SINGLE_VARIABLE_DECLARATION:
			return ast.newSingleVariableDeclaration();
		case ASTNode.STRING_LITERAL:
			return ast.newStringLiteral();
		case ASTNode.SUPER_CONSTRUCTOR_INVOCATION:
			return ast.newSuperConstructorInvocation();
		case ASTNode.SUPER_FIELD_ACCESS:
			return ast.newSuperFieldAccess();
		case ASTNode.SUPER_METHOD_INVOCATION:
			return ast.newSuperMethodInvocation();
		case ASTNode.SUPER_METHOD_REFERENCE:
			return ast.newSuperMethodReference();
		case ASTNode.SWITCH_CASE:
			return ast.newSwitchCase();
		case ASTNode.SWITCH_STATEMENT:
			return ast.newSwitchStatement();
		case ASTNode.SYNCHRONIZED_STATEMENT:
			return ast.newSynchronizedStatement();
		case ASTNode.TAG_ELEMENT:
			return ast.newTagElement();
		case ASTNode.TEXT_ELEMENT:
			return ast.newTextElement();
		case ASTNode.THIS_EXPRESSION:
			return ast.newThisExpression();
		case ASTNode.THROW_STATEMENT:
			return ast.newThrowStatement();
		case ASTNode.TRY_STATEMENT:
			return ast.newTryStatement();
		case ASTNode.TYPE_DECLARATION:
			return ast.newTypeDeclaration();
		case ASTNode.TYPE_DECLARATION_STATEMENT:
			return ast.newTypeDeclarationStatement(ast.newTypeDeclaration());
		case ASTNode.TYPE_METHOD_REFERENCE:
			return ast.newTypeMethodReference();
		case ASTNode.TYPE_LITERAL:
			return ast.newTypeLiteral();
		case ASTNode.TYPE_PARAMETER:
			return ast.newTypeParameter();
		case ASTNode.VARIABLE_DECLARATION_EXPRESSION:
			return ast.newVariableDeclarationExpression(ast
					.newVariableDeclarationFragment());
		case ASTNode.VARIABLE_DECLARATION_FRAGMENT:
			return ast.newVariableDeclarationFragment();
		case ASTNode.VARIABLE_DECLARATION_STATEMENT:
			return ast.newVariableDeclarationStatement(ast
					.newVariableDeclarationFragment());
		case ASTNode.WHILE_STATEMENT:
			return ast.newWhileStatement();
		case ASTNode.WILDCARD_TYPE:
			return ast.newWildcardType();
		default:
			logger.severe("Failed to find node for code " + treeNode.getData());
			throw new Exception("Unkown type of ASTNode");
		}
	}
	protected static void addSimplePropertyToASTNode(final ASTNode node,
			final SimplePropertyDescriptor sp, final Object spValue)
					throws Exception {
		checkNotNull(sp, "sp should not be null");
		checkNotNull(spValue);
		if (node instanceof Modifier) {
			final Modifier.ModifierKeyword mod = Modifier.ModifierKeyword
					.toKeyword((String) spValue);
			node.setStructuralProperty(sp, mod);
		} else if (node instanceof PrimitiveType) {
			final String type = (String) spValue;
			final PrimitiveType.Code typeCode;
			if (spValue.equals("boolean")) {
				typeCode = PrimitiveType.BOOLEAN;
			} else if (spValue.equals("byte")) {
				typeCode = PrimitiveType.BYTE;
			} else if (spValue.equals("char")) {
				typeCode = PrimitiveType.CHAR;
			} else if (spValue.equals("double")) {
				typeCode = PrimitiveType.DOUBLE;
			} else if (spValue.equals("float")) {
				typeCode = PrimitiveType.FLOAT;
			} else if (spValue.equals("int")) {
				typeCode = PrimitiveType.INT;
			} else if (spValue.equals("long")) {
				typeCode = PrimitiveType.LONG;
			} else if (spValue.equals("short")) {
				typeCode = PrimitiveType.SHORT;
			} else if (spValue.equals("void")) {
				typeCode = PrimitiveType.VOID;
			} else {
				logger.severe("could not find primitive type " + type);
				throw new Exception("could not find primitive type " + type);
			}
			node.setStructuralProperty(sp, typeCode);
		} else if (node instanceof Assignment) {
			final Assignment.Operator op = Assignment.Operator
					.toOperator((String) spValue);
			node.setStructuralProperty(sp, op);
		} else if (node instanceof InfixExpression) {
			final InfixExpression.Operator op = InfixExpression.Operator
					.toOperator((String) spValue);
			node.setStructuralProperty(sp, op);
		} else if (node instanceof PrefixExpression) {
			final PrefixExpression.Operator op = PrefixExpression.Operator
					.toOperator((String) spValue);
			node.setStructuralProperty(sp, op);
		} else if (node instanceof PostfixExpression) {
			final PostfixExpression.Operator op = PostfixExpression.Operator
					.toOperator((String) spValue);
			node.setStructuralProperty(sp, op);
		} else {
			node.setStructuralProperty(sp, spValue);
		}
	}

	

	/**
	 * Create a new ASTNode for the given symbol, setting only the simple
	 * properties.
	 *
	 * @param symbol
	 * @param ast
	 * @return
	 * @throws Exception
	 */
	private final ASTNode getASTNodeForTreeNode(
			final TreeNode<Integer> treeNode, final AST ast,
			final Map<TreeNode<Integer>, ASTNode> createdASTNodes)
					throws Exception {
		final AstNodeSymbol symbol = getSymbol(treeNode.getData());
		if(symbol.nodeType == AstNodeSymbol.TEMPLATE_NODE) {
			System.err.println("converting a symbol.");
		}
		final ASTNode node = createASTNodeObject(treeNode, ast, symbol);

		// Set children properties
		final List<StructuralPropertyDescriptor> descriptors = JavaAstPropertiesData
				.getChildProperties(symbol.nodeType);
		checkArgument(descriptors.size() == treeNode.nProperties());
		for (int i = 0; i < descriptors.size(); i++) {
			if (treeNode.getChildrenByProperty().get(i).isEmpty()) {
				continue; // Nothing to do.
			}

			if (descriptors.get(i) instanceof ChildPropertyDescriptor) {
				final TreeNode<Integer> child = treeNode
						.getChildrenByProperty().get(i).get(0);
				node.setStructuralProperty(descriptors.get(i),
						checkNotNull(createdASTNodes.get(child)));
			} else {
				checkArgument(descriptors.get(i) instanceof ChildListPropertyDescriptor);
				final List<ASTNode> nodesChildren = (List<ASTNode>) node
						.getStructuralProperty(descriptors.get(i));
				nodesChildren.clear();
				for (final TreeNode<Integer> childNode : treeNode
						.getChildrenByProperty().get(i)) {
					final ASTNode childAst = checkNotNull(createdASTNodes
							.get(childNode));
					nodesChildren.add(childAst);
				}
			}
		}

		// Set simple properties
		for (final SimplePropertyDescriptor sp : JavaAstPropertiesData
				.getSimpleProperties(symbol.nodeType)) {
			final Object simplePropertyValue = symbol.getSimpleProperty(sp
					.getId());
			if (simplePropertyValue == null) {
				continue;
			}
			addSimplePropertyToASTNode(node, sp, simplePropertyValue);
		}

		createdASTNodes.put(treeNode, checkNotNull(node));
		System.err.println("Symbol is: " + node.toString());

		return node;
	}

	/**
	 * Create an AST from a given TreeNode
	 *
	 */
	@Override
	public ASTNode getASTFromTree(final TreeNode<Integer> tree) {
		final TreeNode<Integer> detempletized = detempletize(tree);
//		return super.getASTFromTree(detempletized);
		final Map<TreeNode<Integer>, ASTNode> extractedNodes = Maps
				.newIdentityHashMap();

		// Do a pre-order visit. Topological sorting
		final Deque<TreeNode<Integer>> toVisit = new ArrayDeque<TreeNode<Integer>>();
		// It will contain the nodes in the order from the last to be converted
		// to the first (i.e topologically sorted)
		final List<TreeNode<Integer>> conversionPlan = Lists.newArrayList();

		toVisit.push(tree);
		while (!toVisit.isEmpty()) {
			final TreeNode<Integer> node = toVisit.pop();
			conversionPlan.add(node);
			for (final List<TreeNode<Integer>> childProperty : node
					.getChildrenByProperty()) {
				for (final TreeNode<Integer> child : childProperty) {
					toVisit.push(child);
				}
			}
		}

		// OK. Now back to business...
		final AST ast = AST.newAST(AST.JLS8);
		for (int i = conversionPlan.size() - 1; i >= 0; i--) {
			try {
				final TreeNode<Integer> toBeConverted = conversionPlan.get(i);
				for (final List<TreeNode<Integer>> childProperties : toBeConverted
						.getChildrenByProperty()) {
					for (final TreeNode<Integer> child : childProperties) {
						checkArgument(extractedNodes.containsKey(child));
					}
				}
				getASTNodeForTreeNode(toBeConverted, ast, extractedNodes);
			} catch (final Exception e) {
				logger.warning("Failed to get ASTNode for subtree "
						+ e.getMessage() + " "
						+ ExceptionUtils.getFullStackTrace(e));
			}
		}

		return extractedNodes.get(tree);
	}

}
