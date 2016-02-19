package changeassistant.versions.treematching;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import changeassistant.peers.LineRange;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.treematching.edits.ITreeEditOperation;

public class BestLeafMatchDifferencer implements ITreeDifferencer {

	// private static final int UP = 1;
	// private static final int LEFT = 2;
	// private static final int DIAG = 3;

	private Set<NodePair> fMatch;
	private HashMap<Node, Set<Node>> fLeftToRightMatch;
	private HashMap<Node, Set<Node>> fRightToLeftMatch;

	// private HashSet<NodePair> fMatchPrime;
	// private HashMap<Node, Node> fLeftToRightMatchPrime;
	// private HashMap<Node, Node> fRightToLeftMatchPrime;
	// private Node leftCopy;

	private HashMap<Node, Node> /* tempPrimeMap1, tempPrimeMap2, */
	basicPrimeLeftToRight, basicPrimeRightToLeft;

	private List<ITreeEditOperation> fEditScript;

	// private int editIndex = 0;
	/**
	 * Calculates the edit script of {@link ITreeEditOperation} between the left
	 * and the right {@link Node} trees.
	 * 
	 * @param left
	 *            tree to calculate the edit script for
	 * @param right
	 *            tree to calculate the edit script for
	 */
	@Override
	public void calculateEditScript(final Node left, final Node right) {
		fMatch = new HashSet<NodePair>();
		ITreeMatcher dnm = MatchFactory.getMatcher(fMatch);
		try {
			dnm.match(left, right);
		} catch (Exception e) {
			e.printStackTrace();
		}

		initialMatchMap();

		MatchEnumerator enumerator = new MatchEnumerator(basicPrimeLeftToRight,
				basicPrimeRightToLeft, fLeftToRightMatch, fRightToLeftMatch);

		clearMatchMap();
		fEditScript = enumerator.computeBestEditScript(left, right);
	}

	@Override
	public void calculateEditScript(Node left, Node right,
			Map<LineRange, LineRange> noChangeRangePairs,
			Map<LineRange, LineRange> updateRangePairs, CompilationUnit leftCU,
			CompilationUnit rightCU) {
		fMatch = new HashSet<NodePair>();

		initMatch(left, right, noChangeRangePairs, updateRangePairs, leftCU,
				rightCU);

		ITreeMatcher dnm = MatchFactory.getMatcher(fMatch);
		try {
			dnm.match(left, right);
		} catch (Exception e) {
			e.printStackTrace();
		}

		initialMatchMap();
		MatchEnumerator enumerator = new MatchEnumerator(basicPrimeLeftToRight,
				basicPrimeRightToLeft, fLeftToRightMatch, fRightToLeftMatch);

		clearMatchMap();
		fEditScript = enumerator.computeBestEditScript(left, right);
	}

	private Map<Integer, List<Node>> createLineNodesMap(Node root,
			CompilationUnit cu) {
		Map<Integer, List<Node>> lineNodeMap = new HashMap<Integer, List<Node>>();
		Enumeration<Node> nEnum = root.depthFirstEnumeration();
		Node tmpNode = null;
		int lineNumber = 0;
		List<Node> nodeList = null;
		int baseLine = cu
				.getLineNumber(root.getSourceCodeRange().startPosition);
		while (nEnum.hasMoreElements()) {
			tmpNode = nEnum.nextElement();
			lineNumber = cu
					.getLineNumber(tmpNode.getSourceCodeRange().startPosition)
					- baseLine;
			if (!lineNodeMap.containsKey(lineNumber)) {
				nodeList = new ArrayList<Node>();
				lineNodeMap.put(lineNumber, nodeList);
			}
			lineNodeMap.get(lineNumber).add(tmpNode);
		}
		return lineNodeMap;
	}

	private void initMatch(Node left, Node right,
			Map<LineRange, LineRange> noChangeRangePairs,
			Map<LineRange, LineRange> updateRangePairs, CompilationUnit leftCU,
			CompilationUnit rightCU) {
		LineRange leftRange = null, rightRange = null;
		int leftStart = 0, rightStart = 0;
		int leftLength = 0, rightLength = 0;
		Node tempLeft = null, tempRight = null;
		if (noChangeRangePairs.isEmpty() && updateRangePairs.isEmpty())
			return;
		Map<Integer, List<Node>> leftLineNodesMap = createLineNodesMap(left,
				leftCU);
		Map<Integer, List<Node>> rightLineNodesMap = createLineNodesMap(right,
				rightCU);

		List<Node> lNodes = null, rNodes = null;
		for (Entry<LineRange, LineRange> pair : noChangeRangePairs.entrySet()) {
			leftRange = pair.getKey();
			rightRange = pair.getValue();
			for (int i = 0, leftLine = leftRange.startLine, rightLine = rightRange.startLine; i < Math
					.min(leftRange.lineNum, rightRange.lineNum); i++, leftLine++, rightLine++) {
				leftStart = leftRange.docLineComparator.getTokenStart(leftLine);
				leftLength = leftRange.docLineComparator
						.getTokenLength(leftLine);
				rightStart = rightRange.docLineComparator
						.getTokenStart(rightLine);
				rightLength = rightRange.docLineComparator
						.getTokenLength(rightLine);
				lNodes = leftLineNodesMap.get(leftLine);
				rNodes = rightLineNodesMap.get(rightLine);
				if (lNodes == null || rNodes == null) {
					continue;
				}
				if (lNodes.size() != rNodes.size()) {
					tempLeft = left.lookforNodeByAmbiguousRange(leftStart,
							leftLength, lNodes);
					tempRight = right.lookforNodeByAmbiguousRange(rightStart,
							rightLength, rNodes);
					if (tempLeft.getStrValue().equals(tempRight.getStrValue())
							&& !tempLeft.isMatched() && !tempRight.isMatched()) {
						tempLeft.enableMatched();
						tempRight.enableMatched();
						fMatch.add(new NodePair(tempLeft, tempRight));
					}
				} else {
					for (int j = 0; j < lNodes.size(); j++) {
						tempLeft = lNodes.get(j);
						tempRight = rNodes.get(j);
						if (tempLeft.getStrValue().equals(
								tempRight.getStrValue())
								&& !tempLeft.isMatched()
								&& !tempRight.isMatched()) {
							tempLeft.enableMatched();
							tempRight.enableMatched();
							fMatch.add(new NodePair(tempLeft, tempRight));
						}
					}
				}
			}
		}

		for (Entry<LineRange, LineRange> pair : updateRangePairs.entrySet()) {
			leftRange = pair.getKey();
			rightRange = pair.getValue();
			int leftLine = leftRange.startLine;
			int rightLine = rightRange.startLine;
			leftStart = leftRange.docLineComparator.getTokenStart(leftLine);
			leftLength = leftRange.docLineComparator.getTokenLength(leftLine);
			rightStart = rightRange.docLineComparator.getTokenStart(rightLine);
			rightLength = rightRange.docLineComparator
					.getTokenLength(rightLine);
			lNodes = leftLineNodesMap.get(leftLine);
			rNodes = rightLineNodesMap.get(rightLine);
			if (lNodes == null || rNodes == null) {
				continue;
			}
			if (lNodes.size() != rNodes.size()) {
				// tempLeft = left.lookforNodeByAmbiguousRange(leftStart,
				// leftLength, lNodes);
				// tempRight = right.lookforNodeByAmbiguousRange(rightStart,
				// rightLength, rNodes);
				continue;
			} else {
				boolean isEquivalent = true;
				for (int j = 0; j < lNodes.size(); j++) {
					tempLeft = lNodes.get(j);
					tempRight = rNodes.get(j);
					isEquivalent = isEquivalent
							&& (!tempLeft.isMatched() && !tempRight.isMatched() && checkMapPossibility(
									tempLeft, tempRight));
				}
				if (isEquivalent) {
					tempLeft.enableMatched();
					tempRight.enableMatched();
					fMatch.add(new NodePair(tempLeft, tempRight));
				}
			}
		}
	}

	public static boolean checkMapPossibility(Node tempLeft, Node tempRight) {
		Set<Integer> nodeTypes = new HashSet<Integer>();
		nodeTypes.add(ASTNode.TRY_STATEMENT);
		nodeTypes.add(ASTNode.CATCH_CLAUSE);
		nodeTypes.add(ASTNode.SWITCH_CASE);
		nodeTypes.add(ASTNode.SWITCH_STATEMENT);
		nodeTypes.add(ASTNode.SYNCHRONIZED_STATEMENT);
		nodeTypes.add(ASTNode.THROW_STATEMENT);
		if (tempLeft.getNodeType() != tempRight.getNodeType()
				&& (nodeTypes.contains(tempLeft.getNodeType()) || nodeTypes
						.contains(tempRight.getNodeType())))
			return false;
		return true;
	}

	private void clearMatchMap() {
		basicPrimeLeftToRight = basicPrimeRightToLeft = null;
		fLeftToRightMatch = fRightToLeftMatch = null;
		fMatch = null;
	}

	/**
	 * Returns the edit script calculated between the two {@link Node} trees.
	 * 
	 * @return the edit script calculated between the two trees
	 */
	public List<ITreeEditOperation> getEditScript() {
		return fEditScript;
	}

	private Node apply(ITreeEditOperation edit, Node node, int editIndex) {
		edit.apply(editIndex);
		return (Node) node.deepCopy();// the deepCopy is the copy of the node
										// which experienced the change
	}

	private void initialMatchMap() {
		fLeftToRightMatch = new HashMap<Node, Set<Node>>();
		fRightToLeftMatch = new HashMap<Node, Set<Node>>();
		Node tempL, tempR;
		Set<Node> tempSet;
		for (NodePair p : fMatch) {
			tempL = p.getLeft();
			tempR = p.getRight();
			if (fLeftToRightMatch.containsKey(tempL)) {
				fLeftToRightMatch.get(tempL).add(tempR);
			} else {
				tempSet = new HashSet<Node>();
				tempSet.add(tempR);
				fLeftToRightMatch.put(tempL, tempSet);
			}
			if (fRightToLeftMatch.containsKey(tempR)) {
				fRightToLeftMatch.get(tempR).add(tempL);
			} else {
				tempSet = new HashSet<Node>();
				tempSet.add(tempL);
				fRightToLeftMatch.put(tempR, tempSet);
			}
		}

		// the basic prime will exist in any possible enumerated match pair list
		basicPrimeLeftToRight = new HashMap<Node, Node>();
		basicPrimeRightToLeft = new HashMap<Node, Node>();
		for (Node key : fLeftToRightMatch.keySet()) {
			tempSet = fLeftToRightMatch.get(key);
			if (tempSet.size() == 1) {
				tempR = tempSet.iterator().next();
				if (fRightToLeftMatch.get(tempR).size() == 1) {
					basicPrimeLeftToRight.put(key, tempR);
					basicPrimeRightToLeft.put(tempR, key);
				}
			}
		}

		// to optimize the space used by map and set data-structures by removing
		// basic prime match pairs
		for (Node key : basicPrimeLeftToRight.keySet()) {
			if (fLeftToRightMatch.containsKey(key)) {
				fLeftToRightMatch.remove(key);
			}
		}
		for (Node key : basicPrimeRightToLeft.keySet()) {
			if (fRightToLeftMatch.containsKey(key)) {
				fRightToLeftMatch.remove(key);
			}
		}
	}
}
