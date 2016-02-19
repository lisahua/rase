package changeassistant.versions.treematching;

import java.util.Set;

import changeassistant.peers.comparison.Node;
import changeassistant.versions.treematching.measure.INodeSimilarityCalculator;
import changeassistant.versions.treematching.measure.IStringSimilarityCalculator;

public interface ITreeMatcher {

    public void init(IStringSimilarityCalculator leafCalc, double lTh,
			IStringSimilarityCalculator nodeStringCalc, double nStTh,
			INodeSimilarityCalculator nodeCalc, double nTh);

    public void enableDynamicThreshold(int depth, double threshold);
    
    public void disableDynamicThreshold();
    
    public void setMatchingSet(Set<NodePair> matchingSet);
    
    public void match(Node left, Node right);
}
