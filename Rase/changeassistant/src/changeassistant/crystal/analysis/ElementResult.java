package changeassistant.crystal.analysis;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import changeassistant.peers.SourceCodeRange;
import changeassistant.peers.comparison.Node;

public class ElementResult {

	protected List<Node> nodes;

	public void init(Node root) {
		nodes = new ArrayList<Node>();
		Enumeration<Node> bEnum = root.breadthFirstEnumeration();
		while (bEnum.hasMoreElements()) {
			nodes.add(bEnum.nextElement());
		}
	}

	public Node searchForRelevantNode(SourceCodeRange scr) {
		double minDiff = Double.MAX_VALUE;
		double sDiff = 0;
		Node relevantNode = null;
		int start = scr.startPosition;
		int length = scr.length;
		for (Node node : nodes) {
			scr = node.getSourceCodeRange();
			if (scr.startPosition <= start
					&& scr.startPosition + scr.length >= start + length) {
				sDiff = Math.pow(scr.startPosition - start, 2)
						+ Math.pow(scr.length - length, 2);
				if (sDiff < minDiff) {
					minDiff = sDiff;
					relevantNode = node;
				}
			}
		}
		return relevantNode;
	}

	public Set<Node> searchForRelevantNodes(List<SourceCodeRange> astNodeRanges) {
		Set<Node> result = searchForRelevantNodes2(astNodeRanges);
		return result;
	}

	public Set<Node> searchForRelevantNodes(Set<SourceCodeRange> astNodeRanges) {
		return searchForRelevantNodes2(new ArrayList<SourceCodeRange>(
				astNodeRanges));
	}

	public List<Node> searchForRelevantNodeList(List<SourceCodeRange> arrayList) {
		double[] sDiffs = new double[arrayList.size()];
		double sDiff = 0;
		List<List<Node>> relevantNodes = new ArrayList<List<Node>>();
		List<Node> result = new ArrayList<Node>();
		List<Node> tmpNodes = null;
		SourceCodeRange scr = null;
		SourceCodeRange astNodeRange = null;
		int start = -1;
		int length = -1;
		for (int i = 0; i < arrayList.size(); i++) {
			sDiffs[i] = Double.MAX_VALUE;
			relevantNodes.add(null);
		}
		for (Node node : nodes) {
			scr = node.getSourceCodeRange();
			for (int i = 0; i < arrayList.size(); i++) {
				astNodeRange = arrayList.get(i);
				start = astNodeRange.startPosition;
				length = astNodeRange.length;
				if (scr.startPosition <= start
						&& scr.startPosition + scr.length >= start + length) {
					sDiff = Math.pow(scr.startPosition - start, 2)
							+ Math.pow(scr.length - length, 2);
					if (sDiff <= sDiffs[i]) {
						tmpNodes = relevantNodes.get(i);
						if (tmpNodes == null) {
							tmpNodes = new ArrayList<Node>();
							relevantNodes.set(i, tmpNodes);
						}
						if (sDiff < sDiffs[i]) {
							sDiffs[i] = sDiff;
							tmpNodes.clear();
							tmpNodes.add(node);
						} else {// sDiff == sDiffs[i]
							if (!tmpNodes.contains(node))
								tmpNodes.add(node);
						}
					}
				}
			}
		}
		assert !relevantNodes.contains(null);
		for (List<Node> r : relevantNodes) {
			if (r != null) {
				for (Node n : r) {
					if (!result.contains(n)) {
						result.add(n);
					}
				}
			}
		}
		return result;
	}

	/**
	 * This function is similar to searchForRelevantNodes(...) but only
	 * different in the return type. The argument may contain redundant data
	 * 
	 * @param arrayList
	 * @return
	 */
	public Set<Node> searchForRelevantNodes2(List<SourceCodeRange> arrayList) {
		double[] sDiffs = new double[arrayList.size()];
		double sDiff = 0;
		List<Set<Node>> relevantNodes = new ArrayList<Set<Node>>();
		Set<Node> tmpNodes = null;
		SourceCodeRange scr = null;
		SourceCodeRange astNodeRange = null;
		int start = -1;
		int length = -1;
		for (int i = 0; i < arrayList.size(); i++) {
			sDiffs[i] = Double.MAX_VALUE;
			relevantNodes.add(null);
		}
		// System.out.print("");
		for (Node node : nodes) {
			scr = node.getSourceCodeRange();
			for (int i = 0; i < arrayList.size(); i++) {
				astNodeRange = arrayList.get(i);
				start = astNodeRange.startPosition;
				length = astNodeRange.length;
				if (scr.startPosition <= start
						&& scr.startPosition + scr.length >= start + length) {
					sDiff = Math.pow(scr.startPosition - start, 2)
							+ Math.pow(scr.length - length, 2);
					if (sDiff <= sDiffs[i]) {
						tmpNodes = relevantNodes.get(i);
						if (tmpNodes == null) {
							tmpNodes = new HashSet<Node>();
							relevantNodes.set(i, tmpNodes);
						}
						if (sDiff < sDiffs[i]) {
							sDiffs[i] = sDiff;
							tmpNodes.clear();
							tmpNodes.add(node);
						} else {// sDiff == sDiffs[i]
							tmpNodes.add(node);
						}
					}
				}
			}
		}
		assert !relevantNodes.contains(null);

		Set<Node> result = new HashSet<Node>();
		for (Set<Node> r : relevantNodes) {
			if (r != null)
				result.addAll(r);
		}
		return result;
	}
}
