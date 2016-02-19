package changeassistant.multipleexample.contextualize.datastructure;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;

import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;

public class Sequence {

	List<Integer> nodeIndexSequence = null;

	int right = 0;

	public Sequence(List<Integer> indexSequence) {
		nodeIndexSequence = indexSequence;
		init();
	}

	/**
	 * To create index sequence from a tree. It does not produce a sequence
	 * containing index == 0
	 * 
	 * @param sTree
	 */
	public Sequence(SimpleTreeNode sTree) {
		System.out.print("");
		nodeIndexSequence = new ArrayList<Integer>();
		Enumeration<SimpleTreeNode> sEnum = null;
		List<SimpleTreeNode> reverseChildren = new ArrayList<SimpleTreeNode>();
		Stack<SimpleTreeNode> stack = new Stack<SimpleTreeNode>();
		stack.push(sTree);
		int start = sTree.getNodeIndex();
		// nodeIndexSequence.add(sTree.getNodeIndex());
		SimpleTreeNode tmp = null, tmp2 = null;
		while (!stack.isEmpty()) {
			tmp = stack.pop();
			if (tmp.getNodeIndex() > 0) {
				if (tmp.getChildCount() > 0) {
					nodeIndexSequence.add(tmp.getNodeIndex());
					tmp.setNodeIndex(-tmp.getNodeIndex());
					stack.push(tmp);
					sEnum = tmp.children();
					reverseChildren.clear();
					while (sEnum.hasMoreElements()) {
						reverseChildren.add(sEnum.nextElement());
					}
					for (int i = reverseChildren.size() - 1; i >= 0; i--) {
						tmp2 = reverseChildren.get(i);
						stack.push(tmp2);
					}
				} else {
					nodeIndexSequence.add(tmp.getNodeIndex());
					nodeIndexSequence.add(-tmp.getNodeIndex());
				}
			} else if (tmp.getNodeIndex() < 0) {
				if (tmp.getNodeIndex() == -start)
					right = nodeIndexSequence.size();
				nodeIndexSequence.add(tmp.getNodeIndex());
				tmp.setNodeIndex(-tmp.getNodeIndex());
			}
		}
		init();
	}

	public void add(Sequence s1) {
		nodeIndexSequence.addAll(s1.nodeIndexSequence);
		if (nodeIndexSequence.size() == s1.nodeIndexSequence.size()) {
			// the original sequence is empty
			right = s1.right;
		}
	}

	public void append(int index) {
		nodeIndexSequence.add(0, index);
		nodeIndexSequence.add(-index);
		right = nodeIndexSequence.size() - 1;
	}

	public Sequence concate(Sequence s2) {
		List<Integer> result = new ArrayList<Integer>(nodeIndexSequence);
		result.addAll(s2.nodeIndexSequence);
		return new Sequence(result);
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Sequence))
			return false;
		Sequence other = (Sequence) obj;
		if (!nodeIndexSequence.equals(other.nodeIndexSequence))
			return false;
		return true;
	}

	public List<Integer> getNodeIndexes() {
		return nodeIndexSequence;
	}

	public Integer get(int index) {
		return nodeIndexSequence.get(index);
	}

	public int hashCode() {
		return nodeIndexSequence.hashCode();
	}

	public Sequence head() {
		List<Integer> result = new ArrayList<Integer>();
		for (int i = 1; i < right; i++) {
			result.add(nodeIndexSequence.get(i));
		}
		return new Sequence(result);
	}

	public void init() { // to calculate the value of "right"
		if (nodeIndexSequence.size() >= 2) {
			int start = nodeIndexSequence.get(0);
			for (int i = 1; i < nodeIndexSequence.size(); i++) {
				if (nodeIndexSequence.get(i) == -start)
					right = i;
			}
		}
	}

	public int indexOf(int nodeIndex) {
		return nodeIndexSequence.indexOf(nodeIndex);
	}

	public Sequence joint() {
		List<Integer> result = new ArrayList<Integer>();
		result.add(nodeIndexSequence.get(0));
		result.add(-nodeIndexSequence.get(0));
		return new Sequence(result);
	}

	public boolean isEmpty() {
		return nodeIndexSequence.isEmpty();
	}

	public static List<Integer> parsePositiveIndexes(List<Integer> indexes) {
		List<Integer> result = new ArrayList<Integer>();
		for (Integer index : indexes) {
			if (index > 0) {
				result.add(index);
			}
		}
		return result;
	}

	public int size() {
		return nodeIndexSequence.size() / 2;
	}

	public Sequence tail() {
		List<Integer> result = new ArrayList<Integer>();
		for (int i = right + 1; i < nodeIndexSequence.size(); i++) {
			result.add(nodeIndexSequence.get(i));
		}
		return new Sequence(result);
	}

	public String toString() {
		return nodeIndexSequence.toString();
	}
}
