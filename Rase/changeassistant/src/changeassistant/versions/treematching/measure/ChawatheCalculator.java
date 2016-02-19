
package changeassistant.versions.treematching.measure;


import java.util.Set;

import changeassistant.peers.comparison.Node;
import changeassistant.versions.treematching.NodePair;

public class ChawatheCalculator implements INodeSimilarityCalculator {
	private Set<? extends NodePair> fLeafMatchSet;

	@Override
	public double calculateSimilarity(Node left, Node right) {
		int common = 0;
        // common(x, y) = {(w, z) in M | x contains w, and y contains z}
        // |common|
        for (NodePair p : fLeafMatchSet) {
            Node l = p.getLeft();
            Node r = p.getRight();
            if (left.isNodeDescendant(l) && l.isLeaf() && right.isNodeDescendant(r) && r.isLeaf()) {
                common++;
            }
        }
        int max = maxLeafStatements(left, right);
        return (double) common / (double) max;
	}
	
	private int maxLeafStatements(Node left, Node right) {
        int leftLeafStatements = left.getLeafCount();
        int rightLeafStatements = right.getLeafCount();

        return Math.max(leftLeafStatements, rightLeafStatements);
    }

	@Override
	public void setLeafMatchSet(Set<? extends NodePair> leafMatchSet) {
		//the leafMatchSet doesn't only contain matched leafs, but also matched inner nodes
		 fLeafMatchSet = leafMatchSet;		
	}

}
