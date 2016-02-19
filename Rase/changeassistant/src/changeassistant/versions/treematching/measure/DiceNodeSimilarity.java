package changeassistant.versions.treematching.measure;

import java.util.Enumeration;
import java.util.Set;

import changeassistant.peers.comparison.Node;
import changeassistant.versions.treematching.NodePair;

public class DiceNodeSimilarity implements INodeSimilarityCalculator{
	
	private Set<? extends NodePair> fLeafMatchSet;
	private IStringSimilarityCalculator fStringSimilarity;
	private double fStringThreshold;

	public DiceNodeSimilarity(IStringSimilarityCalculator stringSimilarity, double stringSimilarityThreshold) {
        fStringSimilarity = stringSimilarity;
        fStringThreshold = stringSimilarityThreshold;
    }
	
	@Override
	public double calculateSimilarity(Node left, Node right) {
		int intersection = 0;
		for(NodePair p : fLeafMatchSet){
			Node l = p.getLeft();
			Node r = p.getRight();
			if(left.isNodeDescendant(l) && right.isNodeDescendant(r)){//to get matched descendants' number
				intersection++;
			}
		}
		if((left.getNodeType() == right.getNodeType())//the current node is compared with String Similarity algorithm
				&& fStringSimilarity.calculateSimilarity(left.getStrValue(), right.getStrValue()) >= fStringThreshold){
			intersection++;
		}
		int union = countNodes(left) + countNodes(right);
		return (double)2*intersection/union;
	}

	@Override
	public void setLeafMatchSet(Set<? extends NodePair> leafMatchSet) {
		fLeafMatchSet = leafMatchSet;		
	}

    private int countNodes(Node node) {
        if (node.isLeaf()) {
            return 1;
        } else {
            int count = 1;
            for (Enumeration e = node.children(); e.hasMoreElements();) {
                count += countNodes((Node) e.nextElement());
            }
            return count;
        }
    }
}