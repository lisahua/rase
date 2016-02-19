package changeassistant.versions.treematching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;

import changeassistant.peers.comparison.Node;
import changeassistant.versions.treematching.measure.INodeSimilarityCalculator;
import changeassistant.versions.treematching.measure.IStringSimilarityCalculator;

public class BestLeafTreeMatcher implements ITreeMatcher {
	private IStringSimilarityCalculator fLeafGenericStringSimilarityCalculator;
	private double fLeafGenericStringSimilarityThreshold;

	private INodeSimilarityCalculator fNodeSimilarityCalculator;
	private double fNodeSimilarityThreshold;

	private IStringSimilarityCalculator fNodeStringSimilarityCalculator;
	private double fNodeStringSimilarityThreshold;
	private final double fWeightingThreshold = 0.8;
	private final double fDynamicNodeStringThreshold = 0.8;

	private boolean fDynamicEnabled;
	private int fDynamicDepth;
	private double fDynamicThreshold;

	private Map<Node, Set<NodePair>> fLeftToRightMatchNodes,
			fRightToLeftMatchNodes;
	private Map<Node, Set<LeafPair>> fLeftToRightMatchLeafs,
			fRightToLeftMatchLeafs;
	private List<LeafPair> basicLeafPairs;

	private double DIST = Math.pow(0.1, 6);

	private Set<NodePair> fMatch;

	/**
	 * {@inheritDoc}
	 */
	public void init(IStringSimilarityCalculator leafStringSimCalc,
			double leafStringSimThreshold,
			INodeSimilarityCalculator nodeSimCalc, double nodeSimThreshold) {
		fLeafGenericStringSimilarityCalculator = leafStringSimCalc;// leaf
																	// string
																	// similarity
																	// calculator
		fLeafGenericStringSimilarityThreshold = leafStringSimThreshold;// leaf
																		// string
																		// similarity
																		// threshold
		fNodeStringSimilarityCalculator = leafStringSimCalc;// node string
															// similarity
															// calculator
		fNodeStringSimilarityThreshold = leafStringSimThreshold;// node string
																// similarity
																// threshold
		fNodeSimilarityCalculator = nodeSimCalc;// node similarity calculator
		fNodeSimilarityThreshold = nodeSimThreshold;// node similarity threshold
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
	 * Position matching leaves are preferred to contenting matching leaves
	 * 
	 * @param matchedLeafs
	 * @return
	 */
	public void matchLeaves(Node left, Node right) {
		List<LeafPair> leafPairs = new ArrayList<LeafPair>();
		Node x, y;
		for (Enumeration<Node> leftNodes = left.postorderEnumeration(); leftNodes
				.hasMoreElements();) {
			x = leftNodes.nextElement();
			if (x.isLeaf() && !x.isMatched()) {
				for (Enumeration<Node> rightNodes = right
						.postorderEnumeration(); rightNodes.hasMoreElements();) {
					y = rightNodes.nextElement();
					if (y.isLeaf() && !y.isMatched()) {
						if (x.getNodeType() == y.getNodeType()) {
							double similarity = fLeafGenericStringSimilarityCalculator
									.calculateSimilarity(x.getStrValue(),
											y.getStrValue());
							// Important! Otherwhise nodes that match poorly
							// will make it into final matching set,
							// if no better matches are found!
							if (similarity >= fLeafGenericStringSimilarityThreshold) {
								leafPairs.add(new LeafPair(x, y, similarity));
							}
						}
					}
				}
			}
		}
		Collections.sort(leafPairs);
		for (LeafPair pair : leafPairs) {
			x = pair.getLeft();
			y = pair.getRight();
			if (!(x.isMatched() || y.isMatched())) {
				fMatch.add(pair);
				x.enableMatched();
				y.enableMatched();
			}
		}
	}

	public void matchInnerNodes(Node left, Node right) {
		Node x, y;
		// the roots match each each
		fMatch.add(new NodePair(left, right));
		left.enableMatched();
		right.enableMatched();
		for (Enumeration<Node> leftNodes = left.postorderEnumeration(); leftNodes
				.hasMoreElements();) {
			x = leftNodes.nextElement();
			if (!x.isMatched()/* && !x.isLeaf() */&& !isSpecial(x)) {
				List<Node> candidates = new ArrayList<Node>();
				for (Enumeration<Node> rightNodes = right
						.postorderEnumeration(); rightNodes.hasMoreElements();) {
					y = rightNodes.nextElement();
					if ((!y.isMatched()) /* && !y.isLeaf() */&& !isSpecial(y)
							&& (equal(x, y) || equal2(x, y))) {// we try to
																// match an
																// inner node
																// with another
																// inner node or
																// a leaf
																// simultaneously
						candidates.add(y);
					}
				}
				if (candidates.size() == 0) {
					continue;
				} else {
					if (candidates.size() == 1) {
						y = candidates.get(0);
					} else {// to look for the possibly best matched inner node
						y = candidates.get(0);
						double maxPercent = countMatchedChild(x, y);
						for (int i = 1; i < candidates.size(); i++) {
							Node yy = candidates.get(i);
							double yyPercent = countMatchedChild(x, yy);
							if (yyPercent > maxPercent) {
								y = yy;
								maxPercent = yyPercent;
							}
						}
					}
					if (BestLeafMatchDifferencer.checkMapPossibility(x, y)) {
						x.enableMatched();
						y.enableMatched();
						fMatch.add(new NodePair(x, y));
					}
				}
			}
		}
	}

	public void matchInnerAndLeaf(Node left, Node right) {
		Node x, y;
		// match inner nodes and leaf nodes when possible
		for (Enumeration<Node> leftNodes = left.postorderEnumeration(); leftNodes
				.hasMoreElements();) {
			x = leftNodes.nextElement();
			if (!x.isMatched() && isSpecial(x)) {
				for (Enumeration<Node> rightNodes = right
						.postorderEnumeration(); rightNodes.hasMoreElements();) {
					y = rightNodes.nextElement();
					// System.out.print("");
					if (!y.isMatched() && isSpecial(y) && equal3(x, y)) {
						x.enableMatched();
						y.enableMatched();
						fMatch.add(new NodePair(x, y));
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void match(Node left, Node right) {
		// try to look for best matched leaves
		matchLeaves(left, right);

		matchInnerNodes(left, right);

		matchInnerAndLeaf(left, right);

	}

	private double countMatchedChild(Node x, Node y) {
		int totalCounter = 0;
		int counter = 0;
		if (x.isLeaf() || y.isLeaf())
			return counter;
		Enumeration<Node> enumeration = y.postorderEnumeration();
		while (enumeration.hasMoreElements()) {
			Node child = enumeration.nextElement();
			totalCounter++;
			// if the matched node of this y's child is also the child of x
			if (child.isMatched()) {
				Node left = getMatchedLeftNode(child);
				if (left != null && left.isNodeAncestor(x))
					counter++;
			}
		}
		return counter * 1.0 / totalCounter;
	}

	private boolean equal(Node x, Node y) {
		// inner nodes
		// if(x.isRoot() && y.isRoot())
		// return true;
		// if ((!x.isLeaf() && !y.isLeaf()) || (x.isRoot() && y.isRoot())) {
		if (!x.isLeaf() && !y.isLeaf()) {
			if (x.getNodeType() == ASTNode.IF_STATEMENT
					&& (y.getStrValue().equals("then:") || y.getStrValue()
							.equals("else:"))
					|| (x.getStrValue().equals("then:") || x.getStrValue()
							.equals("else:"))
					&& y.getNodeType() == ASTNode.IF_STATEMENT) {
				return false;
			}
			if (x.getNodeType() == ASTNode.WHILE_STATEMENT
					&& y.getNodeType() == ASTNode.IF_STATEMENT
					|| x.getNodeType() == ASTNode.IF_STATEMENT
					&& y.getNodeType() == ASTNode.WHILE_STATEMENT) {
				return false;
			}
			// if (x.getNodeType() == y.getNodeType()) {
			// little heuristic
			// if (x.getLabel() == EntityType.ROOT_NODE) {
			// return x.getValue().equals(x.getValue());
			// } else {
			double t = fNodeSimilarityThreshold;
			double t2 = t;
			if (fDynamicEnabled
					&& ((x.getLeafCount() < fDynamicDepth) || (y.getLeafCount() < fDynamicDepth))) {
				t = fDynamicThreshold;
				t2 = t / 2;// maybe very different in structure
			}
			double simNode = fNodeSimilarityCalculator
					.calculateSimilarity(x, y);
			double simString = fNodeStringSimilarityCalculator
					.calculateSimilarity(x.getStrValue(), y.getStrValue());
			if ((simString < fNodeStringSimilarityThreshold)
					&& (simNode >= fWeightingThreshold)) {
				// the structures are very similar, but the node strings are
				// very different, this may be a renaming operation
				return true;
			}
			if ((simString >= fNodeStringSimilarityThreshold) && (simNode >= t)) {
				// the structures are less similar, but the node strings are
				// more similar to each other
				return true;
			} else {
				// the structures are very different, but the node strings are
				// quite similar
				// this may be a big insertion
				return (simNode >= t2)
						&& (simString >= fDynamicNodeStringThreshold);
			}
			// }
			// }
		}
		return false;
	}

	private boolean equal2(Node x, Node y) {
		try {
			if (x.getNodeType() == y.getNodeType()) {// this can help match leaf
														// and inner node
				double similarity = fLeafGenericStringSimilarityCalculator
						.calculateSimilarity(x.getStrValue(), y.getStrValue());
				if (similarity >= fNodeStringSimilarityThreshold) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean equal3(Node x, Node y) {
		try {
			if (isSpecial(x) && isSpecial(y)
					&& x.getStrValue().equals(y.getStrValue())) {
				Node rightParent = (Node) y.getParent();
				Node leftParent = getMatchedLeftNode((rightParent));
				if (leftParent != null && leftParent.equals(x.getParent()))
					return true;
				return false;// for these special nodes, the following process
								// does not help since they can only return true
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	// Is it possible to have a set of nodes matched to the same node?
	private Node getMatchedLeftNode(Node node) {
		Node matchedNode = null;
		if (node == null || !node.isMatched()) {// this case should be
												// impossible
			// do nothing
		} else {
			for (NodePair np : fMatch) {
				if (np.getRight().equals(node))
					return np.getLeft();
			}
		}
		return matchedNode;
	}

	/**
	 * These special nodes are matched after their parents' matching
	 * 
	 * @param node
	 * @return
	 */
	private boolean isSpecial(Node node) {
		String strValue = node.getStrValue();
		return strValue.equals("then:") || strValue.equals("else:")
				|| strValue.equals("try-body:") || strValue.equals("finally:");
	}

}
