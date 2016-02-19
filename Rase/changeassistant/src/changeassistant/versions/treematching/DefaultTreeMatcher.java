package changeassistant.versions.treematching;


import java.util.Enumeration;
import java.util.Set;

import changeassistant.peers.comparison.Node;
import changeassistant.versions.treematching.measure.INodeSimilarityCalculator;
import changeassistant.versions.treematching.measure.IStringSimilarityCalculator;

public class DefaultTreeMatcher implements ITreeMatcher{
	private IStringSimilarityCalculator fLeafStringSimilarityCalculator;
	private double fLeafStringSimilarityThreshold;

	private INodeSimilarityCalculator fNodeSimilarityCalculator;
	private double fNodeSimilarityThreshold;
	private IStringSimilarityCalculator fNodeStringSimilarityCalculator;
	private double fNodeStringSimilarityThreshold;

	private boolean fDynamicEnabled;
	private int fDynamicDepth;
	private double fDynamicThreshold;

	private Set<NodePair> fMatch;

	/**
	 * {@inheritDoc}
	 */
	public void init(IStringSimilarityCalculator leafStringSimCalc,
			double leafStringSimThreshold,
			INodeSimilarityCalculator nodeSimCalc, double nodeSimThreshold) {
		fLeafStringSimilarityCalculator = leafStringSimCalc;
		fLeafStringSimilarityThreshold = leafStringSimThreshold;
		fNodeStringSimilarityCalculator = leafStringSimCalc;
		fNodeStringSimilarityThreshold = leafStringSimThreshold;
		fNodeSimilarityCalculator = nodeSimCalc;
		fNodeSimilarityThreshold = nodeSimThreshold;
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(IStringSimilarityCalculator leafStringSimCalc,
			double leafStringSimThreshold,
			IStringSimilarityCalculator nodeStringSimCalc,
			double nodeStringSimThreshold,
			INodeSimilarityCalculator nodeSimCalc, double nodeSimThreshold) {
		init(leafStringSimCalc, leafStringSimThreshold, nodeSimCalc,
				nodeSimThreshold);
		fNodeStringSimilarityCalculator = nodeStringSimCalc;
		fNodeStringSimilarityThreshold = nodeStringSimThreshold;
	}

	/**
	 * {@inheritDoc}
	 */
	public void enableDynamicThreshold(int depth, double threshold) {
		fDynamicDepth = depth;
		fDynamicThreshold = threshold;
		fDynamicEnabled = true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void disableDynamicThreshold() {
		fDynamicEnabled = false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMatchingSet(Set<NodePair> matchingSet) {
		fMatch = matchingSet;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void match(Node left, Node right) {
		// 1. M <- {} : in init

		// 2. Mark all nodes of T1 and T2 "unmatched" this is done during build
		// of the trees

		// 3. Proceed bottom-up on tree T1
		for (Enumeration enumerate = left.postorderEnumeration(); enumerate
				.hasMoreElements();) {
			Node x = (Node) enumerate.nextElement();
			// For each unmatched node x in T1
			if (!x.isMatched()) {
				for (Enumeration e = right.postorderEnumeration(); e
						.hasMoreElements()
						&& !x.isMatched();) {
					Node y = (Node) e.nextElement();
					// if there is an unmatched node y in T2
					if (!x.isMatched() && !y.isMatched()) {
						if (equal(x, y)) {
							// i. Add (x, y) to M
							fMatch.add(new NodePair(x, y));

							// ii. Mark x and y "matched"
							x.enableMatched();
							y.enableMatched();
						}
					}
				}
			}
		}
	}

	private boolean equal(Node x, Node y) {
		// leaves
		if (x.isLeaf() && y.isLeaf()) {
			if (x.getNodeType() == y.getNodeType()) {
				return fLeafStringSimilarityCalculator.calculateSimilarity(x
						.getStrValue(), y.getStrValue()) >= fLeafStringSimilarityThreshold;
			}

			// inner nodes
		} else if ((!x.isLeaf() && !y.isLeaf()) || (x.isRoot() && y.isRoot())) {
			if (x.getNodeType() == y.getNodeType()) {
				// little heuristic: root nodes must not be compared by
				// INodeSimilarityCalculator
//				if (x.getLabel() == EntityType.ROOT_NODE) {
//					return x.getValue().equals(x.getValue());
//				} else {
					double t = fNodeSimilarityThreshold;
					if (fDynamicEnabled && (x.getLeafCount() < fDynamicDepth)
							&& (y.getLeafCount() < fDynamicDepth)) {
						t = fDynamicThreshold;
					}
					double simNode = fNodeSimilarityCalculator
							.calculateSimilarity(x, y);
					double simString = fNodeStringSimilarityCalculator
							.calculateSimilarity(x.getStrValue(), y.getStrValue());
					return (simNode >= t)
							&& (simString >= fNodeStringSimilarityThreshold);
//				}
			}
		}
		return false;
	}
}