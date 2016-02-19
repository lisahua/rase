package changeassistant.multipleexample.contextualize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;

public class ContextualizationUtil {

	/**
	 * deep copy for each SimpleASTNode
	 * 
	 * @param nodesList
	 * @return
	 */
	public static List<List<SimpleASTNode>> getCopy(
			List<List<SimpleASTNode>> nodesList) {
		List<List<SimpleASTNode>> result = new ArrayList<List<SimpleASTNode>>();
		List<SimpleASTNode> resultNodes = null;
		for (List<SimpleASTNode> nodes : nodesList) {
			resultNodes = new ArrayList<SimpleASTNode>();
			for (SimpleASTNode node : nodes) {
				resultNodes.add(new SimpleASTNode(node));
			}
			result.add(resultNodes);
		}
		return result;
	}

	public static List<Map<String, String>> getCopiedMapList(
			List<Map<String, String>> mapList) {
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		for (Map<String, String> map : mapList) {
			result.add(new HashMap<String, String>(map));
		}
		return result;
	}

	// public static List<SimpleTreeNode> getCopySTrees(List<SimpleTreeNode>
	// sTrees) {
	// List<SimpleTreeNode> result = new ArrayList<SimpleTreeNode>();
	// for (SimpleTreeNode sTree : sTrees) {
	// result.add(new SimpleTreeNode(sTree));
	// }
	// return result;
	// }
}
