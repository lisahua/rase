package changeassistant.versions.treematching.measure;

import java.util.Set;

import changeassistant.peers.comparison.Node;
import changeassistant.versions.treematching.NodePair;

public interface INodeSimilarityCalculator {

	double calculateSimilarity(Node left, Node right);
	
	void setLeafMatchSet(Set<? extends NodePair> leafMatchSet);
}
