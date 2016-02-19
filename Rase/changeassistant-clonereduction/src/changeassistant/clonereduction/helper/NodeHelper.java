package changeassistant.clonereduction.helper;

import org.eclipse.jdt.core.dom.ASTNode;

import changeassistant.peers.comparison.Node;

public class NodeHelper {

	public boolean isIf(Node node) {
		return node.getNodeType() == ASTNode.IF_STATEMENT
				&& node.getStrValue().startsWith("if:");
	}
}
