package changeassistant.versions.treematching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import changeassistant.model.AbstractNode.EDITED_TYPE;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.treematching.DirectedNodeMatches.DIRECT;
import changeassistant.versions.treematching.edits.DeleteOperation;
import changeassistant.versions.treematching.edits.EmptyOperation;
import changeassistant.versions.treematching.edits.ITreeEditOperation;
import changeassistant.versions.treematching.edits.InsertOperation;
import changeassistant.versions.treematching.edits.MoveOperation;
import changeassistant.versions.treematching.edits.UpdateOperation;

public class MatchEnumerator {

	private static final int UP = 1;
	private static final int LEFT = 2;
	private static final int DIAG = 3;

	private Map<Node, Node> fBasicPrimeLeftToRight, fBasicPrimeRightToLeft,
			fPrimeLeftToRight, fPrimeRightToLeft;
	private Map<Node, Set<Node>> fLeftToRightMatch, fRightToLeftMatch;

	private Set<NodePair> fMatchPrime;
	private Node left, right, leftCopy;

	private int editIndex = 0;

	private List<ITreeEditOperation> fEditScript;

	private List<Node> nodesToDelete;

	public MatchEnumerator(Map<Node, Node> basicPrimeLeftToRight,
			Map<Node, Node> basicPrimeRightToLeft,
			Map<Node, Set<Node>> leftToRightMatch,
			Map<Node, Set<Node>> rightToLeftMatch) {
		this.fBasicPrimeLeftToRight = basicPrimeLeftToRight;
		this.fBasicPrimeRightToLeft = basicPrimeRightToLeft;
		this.fLeftToRightMatch = leftToRightMatch;
		this.fRightToLeftMatch = rightToLeftMatch;
	}

	public List<ITreeEditOperation> computeBestEditScript(Node left, Node right) {
		// the resulting map list has at least one element--empty
		// System.out.print("");
		List<BiDirectionMap> biDirectionMapList = enumerateMap2();
		// Map<Node, Node> localLeftToRightPrime, localRightToLeftPrime;
		// keep record of local prime bi-direction maps

		List<ITreeEditOperation> tempEditScript, localPrimeEditScript;
		this.left = (Node) left.deepCopy();
		this.right = (Node) right.deepCopy();
		localPrimeEditScript = editScript(biDirectionMapList.get(0));
		// localLeftToRightPrime = fPrimeLeftToRight;
		// localRightToLeftPrime = fPrimeRightToLeft;
		BiDirectionMap bMap;
		for (int i = 1; i < biDirectionMapList.size(); i++) {
			bMap = biDirectionMapList.get(i);
			this.left = (Node) left.deepCopy();
			this.right = (Node) right.deepCopy();
			tempEditScript = editScript(bMap);
			if (tempEditScript.size() < localPrimeEditScript.size()) {
				localPrimeEditScript = tempEditScript;
				// localLeftToRightPrime = fPrimeLeftToRight;
				// localRightToLeftPrime = fPrimeRightToLeft;
			}
		}
		return localPrimeEditScript;
	}

	private List<ITreeEditOperation> editScript(BiDirectionMap bMap) {
		fPrimeLeftToRight = bMap.getLeftToRightMap();
		for (Entry<Node, Node> entry : fBasicPrimeLeftToRight.entrySet()) {
			fPrimeLeftToRight.put(entry.getKey(), entry.getValue());
		}

		fPrimeRightToLeft = bMap.getRightToLeftMap();
		for (Entry<Node, Node> entry : fBasicPrimeRightToLeft.entrySet()) {
			fPrimeRightToLeft.put(entry.getKey(), entry.getValue());
		}

		convertToEquivalent();
		List<Node> nodeList = new ArrayList<Node>();
		Enumeration<Node> enumerator = left.postorderEnumeration();
		while (enumerator.hasMoreElements()) {
			nodeList.add(enumerator.nextElement());
		}

		editIndex = 0;
		leftCopy = (Node) left.deepCopy();
		// the reason to use copy is to avoid side effects introduced by tree
		// matching algorithm
		// 1.
		// E <- {}
		// M' <- M
		fMatchPrime = new HashSet<NodePair>();
		fEditScript = new LinkedList<ITreeEditOperation>();
		for (Entry<Node, Node> entry : fPrimeLeftToRight.entrySet()) {
			fMatchPrime.add(new NodePair(entry.getKey(), entry.getValue()));
		}

		// 1'. Do a post-order traversal of T1 (this is the delete collecting
		// phase)
		List<DeleteOperation> dels = new ArrayList<DeleteOperation>();
		nodesToDelete = new ArrayList<Node>();
		for (Enumeration postOrder = left.postorderEnumeration(); postOrder
				.hasMoreElements();) {
			// (a) Let w be the current node in the post-order traversal of T1
			Node w = (Node) postOrder.nextElement();
			// (b) If w has no partner in M'
			Node wPartner = null;
			Set<Entry<Node, Node>> entries = fPrimeLeftToRight.entrySet();
			for (Entry entry : entries) {
				if (entry.getKey().equals(w)) {
					wPartner = (Node) entry.getValue();
					break;
				}
			}
			if (wPartner == null) {
				// Append DEL(w) to E
				if (w.getStrValue().equals("try-body:")) {
					// do nothing
				} else if (w.getStrValue().startsWith("catch")
						&& w.getParent().getChildCount() == 2) {
					// do nothing
				} else {
					DeleteOperation delete = new DeleteOperation(w);
					dels.add(delete);
					nodesToDelete.add(w);
				}
			}
		}

		// 2.
		// Visit the nodes in T2 in breath-first order
		Enumeration breathFirst = right.breadthFirstEnumeration();
		// skip MethodDeclaration
		while (breathFirst.hasMoreElements()) {
			// (a)
			// Let x be the current node in the breath-first search T2
			Node /* T2 */x = (Node) breathFirst.nextElement();

			// Let y = p(x)
			Node /* T2 */y = (Node) x.getParent();

			// Let z be the partner of y in M' (*)
			Node z = fPrimeRightToLeft.get(y);
			Node w = fPrimeRightToLeft.get(x);

			// (b) If x has no partner in M'
			if (w == null) {
				// i. k <- FindPos(x)
				int k = findInsertPosition(x);

				// ii. Append INS((w, a, v(x)), z, k) to E, for a new identifier
				// w.
				w = (Node) x.clone();
				w.enableMatched();
				x.enableMatched();
				ITreeEditOperation insert = new InsertOperation(w,
						x.getASTExpressions2(), z, k);
				w.setMethodDeclaration(x.getMethodDeclaration());
				// ITreeEditOperation insertCopy = new
				// InsertOperation((Node)w.clone(),
				// (Node)leftCopy.lookforNodeBasedOnRange(z), k);
				ITreeEditOperation insertCopy = new InsertOperation(
						(Node) w.clone(), x.getASTExpressions2(),
						(Node) leftCopy.lookforNodeBasedOnPosition(z), k);
				fEditScript.add(insertCopy);// to add a brand new operation into
											// the fEditScript

				// iii. Add (w, x) to M' and apply INS((w, a, v(x)), z, k) to T1
				w.setEDITED_TYPE(EDITED_TYPE.INSERTED);
				fPrimeLeftToRight.put(w, x);
				fPrimeRightToLeft.put(x, w);

				w.enableInOrder();
				x.enableInOrder();
				leftCopy = apply(insert, left, editIndex++);

				// (c) else if x is not a root (x has a partner in M')
			} else if (!x.isRoot()) {
				// i.
				// Let w be the partner of x in M'
				/* T1 */
				// w = refineBestMatch(fRightToLeftMatchPrime, x,
				// fLeftToRightMatchPrime);
				// Let v = p(w) in T1
				Node /* T1 */v = (Node) w.getParent();

				// ii. If v(w) != v(x)
				boolean equals = true;
				equals = v(w).equals(v(x));

				// if (!v(w).equals(v(x))) {
				if (!equals) {

					// A. Append UPD(w, v(x)) to E
					ITreeEditOperation update = new UpdateOperation(w, x, v(x));
					// ITreeEditOperation updateCopy = new
					// UpdateOperation(leftCopy.lookforNodeBasedOnRange(w),
					// (Node)x.clone(), v(x));
					ITreeEditOperation updateCopy = new UpdateOperation(
							(Node) leftCopy.lookforNodeBasedOnPosition(w),
							(Node) x.clone(), v(x));
					fEditScript.add(updateCopy);

					// B. Apply UPD(w, v(x)) to T1
					// the following two enableInOrder() invocations are
					// useless, since they are already in order
					w.enableInOrder();
					x.enableInOrder();
					leftCopy = apply(update, left, editIndex++);
				}
				// iii. If (y, v) not in M'
				if (!matchContains(v, y, fMatchPrime)) {
					// A. Let z be the partner of y in M'
					// Node z /*T1*/= (Node) fRightToLeftMatchPrime.get(y);
					// already executed
					// B. k <- FindPos(x)
					int k = findInsertPosition(x);
					// C. Append MOV(w, z, k) to E
					ITreeEditOperation move = new MoveOperation(w, x, z, k);

					ITreeEditOperation moveCopy = new MoveOperation(
							(Node) leftCopy.lookforNodeBasedOnPosition(w), x,
							(Node) leftCopy.lookforNodeBasedOnPosition(z), k);
					fEditScript.add(moveCopy);
					// D. Apply MOV(w, z, k) to T1
					w.enableInOrder();
					x.enableInOrder();
					leftCopy = apply(move, left, editIndex++);
				}
			}
			// (d) AlignChildren(w, x)
			if (!w.isLeaf()) {
				alignChildren(w, x);
			}
		}

		// 4. Do all deletions at once
		for (DeleteOperation del : dels) {
			Node nodeToDelete = del.getNodeToDelete();
			if (nodeToDelete.getStrValue().equals("then:")
					|| nodeToDelete.getStrValue().equals("else:")) {
				continue;// do not care the deletion of then or else statement
							// of an if-statement
			}
			nodeToDelete = left.lookforNodeBasedOnRange(nodeToDelete);
			del.setPosition(nodeToDelete.locationInParent());
			// ITreeEditOperation deleteCopy = new DeleteOperation(
			// leftCopy.lookforNodeBasedOnRange(del.getNodeToDelete()));
			ITreeEditOperation deleteCopy = new DeleteOperation(
					(Node) leftCopy.lookforNodeBasedOnPosition(del
							.getNodeToDelete()));
			fEditScript.add(deleteCopy);
			leftCopy = apply(del, left, editIndex++);
		}
		System.out.print("");
		// the problem is that: the siblings may not follow the same
		// sequence--try to adjust the position of newly added nodes
		Enumeration<Node> breadEnum = right.breadthFirstEnumeration();
		Node leftParent, rightParent, lChild, rChild;
		Enumeration<Node> rEnum, lEnum;
		while (breadEnum.hasMoreElements()) {
			rightParent = breadEnum.nextElement();
			leftParent = fPrimeRightToLeft.get(rightParent);
			if (leftParent == null) {
				// do nothing
			} else {
				rEnum = rightParent.children();
				lEnum = leftParent.children();
				while (rEnum.hasMoreElements()) {
					rChild = rEnum.nextElement();
					lChild = fPrimeRightToLeft.get(rChild);
					if (lChild.locationInParent() != rChild.locationInParent()) {
						// the insertion happens in a wrong place
						int k = rChild.locationInParent();
						ITreeEditOperation move = new MoveOperation(lChild,
								rChild, leftParent, k);
						// ITreeEditOperation moveCopy = new
						// MoveOperation(leftCopy.lookforNodeBasedOnRange(lChild),
						// rChild, leftCopy.lookforNodeBasedOnRange(leftParent),
						// k);
						ITreeEditOperation moveCopy = new MoveOperation(
								(Node) leftCopy
										.lookforNodeBasedOnPosition(lChild),
								rChild,
								(Node) leftCopy
										.lookforNodeBasedOnPosition(leftParent),
								k);
						fEditScript.add(moveCopy);
						lChild.enableInOrder();
						rChild.enableInOrder();
						// D. Apply MOV(w, z, k) to T1
						leftCopy = apply(move, left, editIndex++);
					}
				}
			}
		}

		if (!fEditScript.isEmpty()) {
			fEditScript.add(new EmptyOperation(leftCopy));
			fEditScript.add(new EmptyOperation(right));
		}
		return fEditScript;
	}

	private void alignChildren(Node w, Node x) {
		if (w.isLeaf() || x.isLeaf()) {
			return;
		}

		// 1. Mark all children of w and all children f x "out of order"
		markChildrenOutOfOrder(w);
		markChildrenOutOfOrder(x);

		// 2.
		// Let S1 be the sequence of children of w whose partners are children
		// of x
		List<Node> sOne = createChildrenSequence(w, x, fPrimeLeftToRight);
		// Let S2 be the sequence of children of x whose partners are children
		// of w
		List<Node> sTwo = createChildrenSequence(x, w, fPrimeRightToLeft);

		// 3. Define the function equal(a, b) to be true if and only if (a, b)
		// in M'
		// 4. Let S <- LCS(S1, S2, equal)
		HashSet<NodePair> s = longestCommonSubsequence(sOne, sTwo);

		// 5. For each (a, b) in S, mark nodes a and b "in order"
		for (NodePair p : s) {
			p.getLeft().enableInOrder();
			p.getRight().enableInOrder();
		}
		// Node leftCopy = (Node) ((Node)w.getRoot()).deepCopy();
		// 6. For each a in S1, b in S2 such that (a, b) in M but (a, b) not in
		// S
		for (Node a : sOne) {
			if (!a.isInOrder()) { // a not in S
				for (Node b : sTwo) {
					if (!b.isInOrder() && matchContains(a, b, fMatchPrime)) { // b
																				// not
																				// in
																				// S
																				// and
																				// (a,
																				// b)
																				// in
																				// M
						// System.out.print("");
						// (a) k <- FindPos(b)
						int k = findInsertPosition(b);
						// (b)
						// Append MOV(a, w, k) to E
						ITreeEditOperation move = new MoveOperation(a, b, w, k);
						ITreeEditOperation moveCopy = new MoveOperation(
								leftCopy.lookforNodeBasedOnRange(a), b,
								leftCopy.lookforNodeBasedOnRange((Node) a
										.getParent()), k);
						fEditScript.add(moveCopy);
						a.enableInOrder();
						b.enableInOrder();
						// D. Apply MOV(w, z, k) to T1
						leftCopy = apply(move, (Node) a.getRoot(), editIndex++);
					}
				}
			}
		}
	}

	private void convertToEquivalent() {
		Node tempL, tempR, key, value;
		Enumeration<Node> leftNodes = left.postorderEnumeration();
		while (leftNodes.hasMoreElements()) {
			tempL = leftNodes.nextElement();
			for (Entry<Node, Node> entry : fPrimeLeftToRight.entrySet()) {
				key = entry.getKey();
				if (tempL.equals(key)) {
					fPrimeLeftToRight.remove(key);
					fPrimeLeftToRight.put(tempL, entry.getValue());
					break;
				}
			}
			for (Entry<Node, Node> entry : fPrimeRightToLeft.entrySet()) {
				value = entry.getValue();
				key = entry.getKey();
				if (tempL.equals(value)) {
					fPrimeRightToLeft.remove(key);
					fPrimeRightToLeft.put(entry.getKey(), tempL);
					break;
				}
			}
		}
		Enumeration<Node> rightNodes = right.postorderEnumeration();
		while (rightNodes.hasMoreElements()) {
			tempR = rightNodes.nextElement();
			for (Entry<Node, Node> entry : fPrimeLeftToRight.entrySet()) {
				value = entry.getValue();
				key = entry.getKey();
				if (tempR.equals(value)) {
					fPrimeLeftToRight.remove(key);
					fPrimeLeftToRight.put(key, tempR);
					break;
				}
			}
			for (Entry<Node, Node> entry : fPrimeRightToLeft.entrySet()) {
				key = entry.getKey();
				if (tempR.equals(key)) {
					fPrimeRightToLeft.remove(key);
					fPrimeRightToLeft.put(tempR, entry.getValue());
					break;
				}
			}
		}
	}

	private Node apply(ITreeEditOperation edit, Node node, int editIndex) {
		edit.apply(editIndex);
		return (Node) node.deepCopy();// the deepCopy is the copy of the node
										// which experienced the change
	}

	@SuppressWarnings("unchecked")
	private List<Node> createChildrenSequence(Node node, Node x,
			Map<Node, Node> match) {
		LinkedList<Node> result = new LinkedList<Node>();

		for (Enumeration e = node.children(); e.hasMoreElements();) {
			Node n = (Node) e.nextElement();
			Node v = match.get(n);
			if ((v != null) && (v.getParent() == x)) {
				result.add(n);
			}
		}
		return result;
	}

	public List<BiDirectionMap> enumerateMap2() {
		// System.out.print("");
		BiDirectionMap bMap, bMap2;
		List<BiDirectionMap> startMapList, resultMapList;
		Node node1;
		Set<Node> matches, leftNodes, rightNodes;
		Map<Node, Node> tempMap = new HashMap<Node, Node>();

		resultMapList = new ArrayList<BiDirectionMap>();
		resultMapList.add(new BiDirectionMap());

		Queue<Node> queue = new LinkedList<Node>();
		Set<Node> processedNodes = new HashSet<Node>();
		Set<Node> conflictingLeftNodeSet = new HashSet<Node>(), conflictingRightNodeSet = new HashSet<Node>();
		List<Node> conflictingLeftNodeList, conflictingRightNodeList;
		int maximumCount, temp;
		boolean isEquivalent;
		for (Entry<Node, Set<Node>> entry : fLeftToRightMatch.entrySet()) {
			node1 = entry.getKey();
			if (processedNodes.contains(node1))
				continue;
			queue.add(node1);
			isEquivalent = false;
			while (!queue.isEmpty()) {
				node1 = queue.remove();
				// record node1 is processed
				processedNodes.add(node1);
				matches = fLeftToRightMatch.get(node1);
				conflictingLeftNodeSet.clear();
				// add all nodes holding conflicting matches to the set
				for (Node match : matches) {
					conflictingLeftNodeSet.addAll(fRightToLeftMatch.get(match));
				}
				conflictingRightNodeSet.clear();
				for (Node node : conflictingLeftNodeSet) {
					conflictingRightNodeSet.addAll(fLeftToRightMatch.get(node));
				}
				if (conflictingLeftNodeSet.size() == conflictingRightNodeSet
						.size()) {
					// match according to order
					isEquivalent = true;
					conflictingLeftNodeList = new ArrayList<Node>(
							conflictingLeftNodeSet);
					conflictingRightNodeList = new ArrayList<Node>(
							conflictingRightNodeSet);
					Collections.sort(conflictingLeftNodeList);
					Collections.sort(conflictingRightNodeList);
					tempMap.clear();
					for (int i = 0; i < conflictingLeftNodeList.size(); i++) {
						tempMap.put(conflictingLeftNodeList.get(i),
								conflictingRightNodeList.get(i));
					}
					for (int i = 0; i < resultMapList.size(); i++) {
						resultMapList.get(i)
								.addAll(tempMap, DIRECT.LeftToRight);
					}
					processedNodes.addAll(conflictingLeftNodeList);
				} else {
					conflictingLeftNodeSet.removeAll(processedNodes);
					conflictingLeftNodeSet.removeAll(queue);
					queue.addAll(conflictingLeftNodeSet);
					// add new matching pairs based on original matches, but do
					// not cover them
					startMapList = new ArrayList<BiDirectionMap>(resultMapList);
					for (int j = 0; j < startMapList.size(); j++) {
						bMap = startMapList.get(j);
						leftNodes = bMap.getLeftNodes();
						if (leftNodes.contains(node1))
							continue;
						rightNodes = bMap.getRightNodes();
						for (Node match : matches) {
							if (rightNodes.contains(match)) {
								continue;
							}
							bMap2 = new BiDirectionMap(bMap);
							bMap2.add(node1, match, DIRECT.LeftToRight);
							resultMapList.add(bMap2);
						}
					}
				}
			}
			if (!isEquivalent) {
				maximumCount = 0;
				for (int i = 0; i < resultMapList.size(); i++) {
					temp = resultMapList.get(i).getSize();
					if (maximumCount < temp)
						maximumCount = temp;
				}
				startMapList = new ArrayList<BiDirectionMap>();
				for (int i = 0; i < resultMapList.size(); i++) {
					bMap = resultMapList.get(i);
					if (bMap.getSize() == maximumCount) {
						startMapList.add(bMap);
					}
				}
				resultMapList = startMapList;
			}
		}
		return resultMapList;
	}

	public List<BiDirectionMap> enumerateMap() {
		BiDirectionMap bMap, bMap2;
		List<BiDirectionMap> startMapList, resultMapList;
		Node node1;
		Set<Node> matches, leftNodes, rightNodes;

		resultMapList = new ArrayList<BiDirectionMap>();
		resultMapList.add(new BiDirectionMap());// start from empty match

		Queue<Node> queue = new LinkedList<Node>();
		Set<Node> processedNodes = new HashSet<Node>();
		Set<Node> conflictingNodes;
		int maximumCount, temp;
		for (Entry<Node, Set<Node>> entry : fLeftToRightMatch.entrySet()) {
			node1 = entry.getKey();
			if (processedNodes.contains(node1))
				continue;
			queue.add(node1);
			while (!queue.isEmpty()) {
				node1 = queue.remove();
				// record node1 is processed
				processedNodes.add(node1);
				matches = fLeftToRightMatch.get(node1);
				// add all unprocessed nodes holding conflicting matches to
				// queue
				for (Node match : matches) {
					conflictingNodes = new HashSet<Node>(
							fRightToLeftMatch.get(match));
					conflictingNodes.removeAll(processedNodes);// to remove all
																// nodes
																// processed
					conflictingNodes.removeAll(queue);// to remove all nodes
														// waiting for process
					queue.addAll(conflictingNodes);
				}
				// add new matching pairs based on original matches, but do not
				// cover them
				startMapList = new ArrayList<BiDirectionMap>(resultMapList);
				for (int j = 0; j < startMapList.size(); j++) {
					bMap = startMapList.get(j);
					leftNodes = bMap.getLeftNodes();
					if (leftNodes.contains(node1))
						continue;
					rightNodes = bMap.getRightNodes();
					for (Node match : matches) {
						if (rightNodes.contains(match)) {
							continue;
						}
						bMap2 = new BiDirectionMap(bMap);
						bMap2.add(node1, match, DIRECT.LeftToRight);
						resultMapList.add(bMap2);
					}
				}
			}
			maximumCount = 0;
			for (int i = 0; i < resultMapList.size(); i++) {
				temp = resultMapList.get(i).getSize();
				if (maximumCount < temp)
					maximumCount = temp;
			}
			startMapList = new ArrayList<BiDirectionMap>();
			for (int i = 0; i < resultMapList.size(); i++) {
				bMap = resultMapList.get(i);
				if (bMap.getSize() == maximumCount) {
					startMapList.add(bMap);
				}
			}
			resultMapList = startMapList;
		}
		return resultMapList;
	}

	private void extractLCS(int[][] b, List<Node> l, List<Node> r, int i,
			int j, HashSet<NodePair> lcs) {
		if ((i != 0) && (j != 0)) {
			if (b[i][j] == DIAG) {
				lcs.add(new NodePair(l.get(i - 1), r.get(j - 1)));
				extractLCS(b, l, r, i - 1, j - 1, lcs);
			} else if (b[i][j] == UP) {
				extractLCS(b, l, r, i - 1, j, lcs);
			} else {
				extractLCS(b, l, r, i, j - 1, lcs);
			}
		}
	}

	private int findInsertPosition(Node node) {
		Node v = (Node) node.getPreviousSibling();
		while ((v != null) && !v.isInOrder()) {
			v = (Node) v.getPreviousSibling();
		}

		// 1. x is the leftmost child of y that is marked "in order"
		if (v == null) {
			return 0;
		}

		// 2. Let u be the partner of v in T1 (*)
		Node u = fPrimeRightToLeft.get(v);
		if (u == null) {
			System.out.println("ERROR: partner expected (findPosition)");
		}

		// 3. Suppose u is the ith child of its parent
		// (counting from left to right) that is marked "in order"
		// return i
		Node p = (Node) u.getParent();
		int count = 0;
		for (int i = 0; i < p.getIndex(u); i++) {
			Node h = (Node) p.getChildAt(i);
			if (h.isInOrder() || nodesToDelete.contains(h)) {
				count++;
			}
		}
		return count + 1;
	}

	// private int findMovePosition(Node a, Node node){
	// Node v = (Node) node.getPreviousSibling();
	// while ((v != null) && !v.isInOrder()) {
	// v = (Node) v.getPreviousSibling();
	// }
	//
	// // 1. x is the leftmost child of y that is marked "in order"
	// if (v == null) {
	// return 0;
	// }
	//
	// // 2. Let u be the partner of v in T1 (*)
	// Node u = fPrimeRightToLeft.get(v);
	// if (u == null) {
	// System.out.println("ERROR: partner expected (findPosition)");
	// }
	//
	// // 3. Suppose u is the ith child of its parent
	// // (counting from left to right) that is marked "in order"
	// // return i
	// // System.out.print(findInsertPosition(node));
	// Node p = (Node) u.getParent();
	// int count = 0;
	// for (int i = 0; i < p.getIndex(u); i++) {
	// Node h = (Node) p.getChildAt(i);
	// System.out.print(Boolean.toString(h.isInOrder() ||
	// nodesToDelete.contains(h)));
	// if (h.isInOrder() && !h.equals(a) || nodesToDelete.contains(h)) {
	// count++;
	// }
	// }
	// return count + 1;
	// }

	private HashSet<NodePair> longestCommonSubsequence(List<Node> left,
			List<Node> right) {
		int m = left.size();
		int n = right.size();

		int[][] c = new int[m + 1][n + 1];
		int[][] b = new int[m + 1][n + 1];

		for (int i = 0; i <= m; i++) {
			c[i][0] = 0;
			b[i][0] = 0;
		}
		for (int i = 0; i <= n; i++) {
			c[0][i] = 0;
			b[0][i] = 0;
		}

		for (int i = 1; i <= m; i++) {
			for (int j = 1; j <= n; j++) {
				if (matchContains(left.get(i - 1), right.get(j - 1),
						fMatchPrime)) {
					c[i][j] = c[i - 1][j - 1] + 1;
					b[i][j] = DIAG;
				} else if (c[i - 1][j] >= c[i][j - 1]) {
					c[i][j] = c[i - 1][j];
					b[i][j] = UP;
				} else {
					c[i][j] = c[i][j - 1];
					b[i][j] = LEFT;
				}
			}
		}
		HashSet<NodePair> result = new HashSet<NodePair>();
		extractLCS(b, left, right, m, n, result);
		return result;
	}

	@SuppressWarnings("unchecked")
	private void markChildrenOutOfOrder(Node node) {
		for (Enumeration e = node.children(); e.hasMoreElements();) {
			((Node) e.nextElement()).enableOutOfOrder();
		}
	}

	private boolean matchContains(Node v, Node y, Set<NodePair> match) {
		for (NodePair p : match) {
			if (((p.getLeft().equals(v)) && (p.getRight().equals(y)))
					|| ((p.getLeft().equals(y)) && (p.getRight().equals(v)))) {
				return true;
			}
		}
		return false;
	}

	private String v(Node node) {
		return node.getStrValue();
	}
}
