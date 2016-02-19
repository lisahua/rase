package changeassistant.multipleexample.util;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.peers.comparison.Node;

public class NodeUtil {

	public Map<Node, Integer> createNodeIndexMap(Node tree) {
		Enumeration<Node> nEnum = tree.breadthFirstEnumeration();
		Map<Node, Integer> map = new HashMap<Node, Integer>();
		Node tmp = null;
		int index = 1;
		while (nEnum.hasMoreElements()) {
			tmp = nEnum.nextElement();
			map.put(tmp, index);
			index++;
		}
		return map;
	}

	public Map<Node, SimpleTreeNode> createNodeSNodeMap(Node tree,
			SimpleTreeNode sTree) {
		Enumeration<Node> nEnum = tree.breadthFirstEnumeration();
		Enumeration<SimpleTreeNode> sEnum = sTree.breadthFirstEnumeration();
		Map<Node, SimpleTreeNode> map = new HashMap<Node, SimpleTreeNode>();
		Node nTmp = null;
		SimpleTreeNode sTmp = null;
		while (nEnum.hasMoreElements()) {
			nTmp = nEnum.nextElement();
			sTmp = sEnum.nextElement();
			map.put(nTmp, sTmp);
		}
		return map;
	}
}
