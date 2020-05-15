package clegoues.genprog4java.java;

import org.eclipse.jdt.core.dom.*;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class CFBlockInfo {
    private Set<ASTNode> lastStatementsInCFBlocks = new HashSet<>();
    private Stack<ASTNode> currentCFBlock = new Stack<>();
    ASTNode currentNode;

    public boolean isLastStatementInCFBlock(ASTNode node) {
        return lastStatementsInCFBlocks.contains(node);
    }

    public void registerNode(ASTNode node) {
        if(isCFStatement(node)) {
            currentCFBlock.push(node);
        }
        currentNode = node;
    }

    public void endOfNode(ASTNode node) {
        if (node.equals(currentCFBlock.peek())) {
            currentCFBlock.pop();
            lastStatementsInCFBlocks.add(currentNode);
        }
    }

    public static boolean isCFStatement(ASTNode node) {
        return node instanceof EnhancedForStatement ||
                node instanceof ForStatement ||
                node instanceof DoStatement ||
                node instanceof IfStatement ||
                node instanceof SwitchStatement ||
                node instanceof TryStatement ||
                node instanceof WhileStatement;
    }
}
