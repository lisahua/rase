package changeassistant.multipleexample.apply;

import java.util.List;
import java.util.Set;

import changeassistant.multipleexample.partition.CommonEditParser;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.treematching.measure.IStringSimilarityCalculator;
import changeassistant.versions.treematching.measure.NGramsCalculator;

public class BestMatchFinder {

	private static IStringSimilarityCalculator nodeStringCalc = null;

	static {
		nodeStringCalc = new NGramsCalculator();
		((NGramsCalculator) nodeStringCalc).setN(CommonEditParser.N);
	}

	public static Node findBestMatchNode(Set<Node> tmp2, Node tmpNode) {
		double maxSim = -1;
		double tmpSim = -1;
		Node bestNode = null;
		for (Node tmpNode2 : tmp2) {
			if (tmpNode2.getNodeType() == tmpNode.getNodeType()) {
				tmpSim = nodeStringCalc.calculateSimilarity(
						tmpNode2.getStrValue(), tmpNode.getStrValue());
				if (tmpSim > maxSim) {
					maxSim = tmpSim;
					bestNode = tmpNode2;
				}
			}
		}
		return bestNode;
	}

	public static SimpleTreeNode findBestMatchNode(List<SimpleTreeNode> tmp2,
			SimpleTreeNode tmpNode) {
		double maxSim = -1;
		double tmpSim = -1;
		SimpleTreeNode bestNode = null;
		for (SimpleTreeNode tmpNode2 : tmp2) {
			if (tmpNode2.getNodeType() == tmpNode.getNodeType()) {
				tmpSim = nodeStringCalc.calculateSimilarity(
						tmpNode2.getStrValue(), tmpNode.getStrValue());
				if (tmpSim > maxSim) {
					maxSim = tmpSim;
					bestNode = tmpNode2;
				}
			}
		}
		return bestNode;
	}
}
