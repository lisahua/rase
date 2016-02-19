package changeassistant.multipleexample.match;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.peers.SourceCodeRange;

public class EmbeddedTreeCreator {

	public static SimpleTreeNode createEmbeddedTree(List<Integer> nodeIndexes,
			SimpleTreeNode sTree, List<List<SimpleASTNode>> sNodesList) {
		List<Integer> newNodeIndexes = new ArrayList<Integer>(nodeIndexes);
		Enumeration<SimpleTreeNode> sEnum = sTree.breadthFirstEnumeration();
		SimpleTreeNode sTmp = null;
		Map<Integer, SimpleTreeNode> simpleTreeNodes = new HashMap<Integer, SimpleTreeNode>();
		SimpleTreeNode sTreeNode = null;
		while (sEnum.hasMoreElements()) {
			sTmp = (SimpleTreeNode) sEnum.nextElement().clone();
			simpleTreeNodes.put(sTmp.getNodeIndex() - 1, sTmp);
		}
		SimpleTreeNode root = null;
		if (nodeIndexes.get(0) == -nodeIndexes.get(nodeIndexes.size() - 1)) {
			// the first node is the root
			root = simpleTreeNodes.get(nodeIndexes.get(0) - 1);
			// root.setStrValue(PatternUtil.createStrValue(root.getNodeType(),
			// root.getStrValue(), sNodesList.get(nodeIndexes.get(0) - 1)));
			newNodeIndexes.remove(0);
			newNodeIndexes.remove(newNodeIndexes.size() - 1);
		} else {
			root = new SimpleTreeNode(-1, new SourceCodeRange(0, 0), null, -1);
		}
		Stack<SimpleTreeNode> stack = new Stack<SimpleTreeNode>();
		stack.push(root);
		for (Integer nodeIndex : newNodeIndexes) {
			if (nodeIndex > 0) {
				sTreeNode = simpleTreeNodes.get(nodeIndex - 1);
				stack.peek().add(sTreeNode);
				stack.push(sTreeNode);
				// sNodes = sNodesList.get(nodeIndex - 1);
				// sTreeNode.setStrValue(PatternUtil.createStrValue(
				// sTreeNode.getNodeType(), sTreeNode.getStrValue(),
				// sNodes));
			} else {
				stack.pop();
			}
		}
		return root;
	}
}
