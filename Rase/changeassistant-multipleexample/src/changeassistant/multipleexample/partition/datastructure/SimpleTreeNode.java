package changeassistant.multipleexample.partition.datastructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import changeassistant.model.AbstractNode;
import changeassistant.peers.SourceCodeRange;
import changeassistant.peers.comparison.Node;

public class SimpleTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 1L;

	private int nodeIndex;// nodeIndex starting from 1

	// See ContextualizeHelper4.java.initMatchMatrix for use
	private boolean isGeneral = false; // this is only used as a mark for the
										// root of a tree to indicate whether
										// the tree serves as a mother for an
										// extracted context

	private boolean selected = false; // this is true only when the node is
										// selected as part of extracted context

	// private BitSet editIndexes;

	// private BitSet types;

	private int astNodeType;

	public static final int EDITED = 0;
	public static final int CONTEXTUAL = 1;
	public static final int INSERTED = 2;
	public static final int INSERTED_CONTEXTUAL = 3;
	public static final int CONTAIN_UP_RELEVANT = 12;
	public static final int CONTAIN_DOWN_RELEVANT = 13;
	public static final int CONTROL_UP_RELEVANT = 4;
	public static final int CONTROL_DOWN_RELEVANT = 5;
	public static final int DEF_UP_RELEVANT = 6;
	public static final int DEF_DOWN_RELEVANT = 7;
	public static final int USE_UP_RELEVANT = 8;
	public static final int USE_DOWN_RELEVANT = 9;
	public static final int DEF_UP2_RELEVANT = 10;
	public static final int RELEVANT = 11;
	public static final int NONE = -1;

	private Map<Integer, Integer> editAndRoletype;

	// public static BitSet all;
	// public static BitSet editedAndContextual;
	// static {
	// all = new BitSet();
	// // 0--EDITED, 1--CONTEXTUAL, 2--CONTAIN_UP_RELEVANT,
	// // 3--CONTAIN_DOWN_RELEVANT, 4--CONTROL_UP_RELEVANT,
	// // 5--CONTROL_DOWN_RELEVANT
	// // 6--DATA_UP_RELEVANT, 7--DATA_DOWN_RELEVANT
	// all.set(0, 14, true);
	// editedAndContextual = new BitSet();
	// editedAndContextual.set(0, 4, true);
	// }

	/**
	 * The depth can grow positively or negatively
	 */
	private short containHop = 0;
	private short controlHop = 0;
	private short dataHop = 0;

	private SourceCodeRange scr;

	private String strValue;

	public SimpleTreeNode(Node node, boolean hierarchy, int index) {
		this(node.getNodeType(), node.getSourceCodeRange(), node.getStrValue(),
				index);
		if (hierarchy) {// the index is 0;
			Queue<Node> queue = new LinkedList<Node>();
			Queue<SimpleTreeNode> sQueue = new LinkedList<SimpleTreeNode>();
			queue.add(node);
			sQueue.add(this);
			Node temp = null;
			SimpleTreeNode sChild = null;
			SimpleTreeNode sTemp = null;
			Node child = null;
			while (!queue.isEmpty()) {
				temp = queue.remove();
				sTemp = sQueue.remove();
				Enumeration<Node> childEnum = temp.children();
				while (childEnum.hasMoreElements()) {
					child = childEnum.nextElement();
					sChild = new SimpleTreeNode(child, false, ++index);
					sTemp.add(sChild);
					queue.add(child);
					sQueue.add(sChild);
				}
			}
		}
	}

	public SimpleTreeNode(int nodeType, SourceCodeRange scr, String strValue,
			int index) {
		// this(nodeType, scr, strValue, new BitSet(0), new BitSet(0), index);
		this(nodeType, scr, strValue, Collections.EMPTY_MAP, index);
	}

	public Object deepClone() {
		SimpleTreeNode newNode = new SimpleTreeNode(this.astNodeType, this.scr,
				this.strValue, this.getNodeIndex());
		Queue<SimpleTreeNode> queue = new LinkedList<SimpleTreeNode>();
		Queue<SimpleTreeNode> queue2 = new LinkedList<SimpleTreeNode>();
		queue.add(this);
		queue2.add(newNode);
		SimpleTreeNode sTmp = null, sTmp2 = null, newTmp = null;
		Vector<SimpleTreeNode> children = null;
		while (!queue.isEmpty()) {
			sTmp = queue.remove();
			sTmp2 = queue2.remove();
			children = sTmp.children;
			if (children == null)
				continue;
			for (SimpleTreeNode c : children) {
				newTmp = new SimpleTreeNode(c.astNodeType, c.scr, c.strValue,
						c.getNodeIndex());
				sTmp2.add(newTmp);
				queue2.add(newTmp);
			}
			queue.addAll(children);
		}
		return newNode;
	}

	public void copyEditedAndRoleType(SimpleTreeNode tree) {
		Enumeration<SimpleTreeNode> curEnum = this.breadthFirstEnumeration();
		Enumeration<SimpleTreeNode> treEnum = tree.breadthFirstEnumeration();
		while (curEnum.hasMoreElements()) {
			curEnum.nextElement().editAndRoletype = new HashMap<Integer, Integer>(
					treEnum.nextElement().editAndRoletype);
		}
	}

	private SimpleTreeNode(int nodeType, SourceCodeRange scr, String strValue,
			Map<Integer, Integer> editAndRoletype, int index) {
		this.scr = scr;
		this.strValue = strValue;
		this.editAndRoletype = new HashMap<Integer, Integer>(editAndRoletype);
		this.nodeIndex = index;
		this.astNodeType = nodeType;
	}

	public SimpleTreeNode(SimpleTreeNode sTree, int defaultIndex) {
		this(sTree.getNodeType(), sTree.getSourceCodeRange(), sTree
				.getStrValue(), sTree.getEditAndRoletype(), defaultIndex);
		Queue<SimpleTreeNode> queue1 = new LinkedList<SimpleTreeNode>();
		Queue<SimpleTreeNode> queue2 = new LinkedList<SimpleTreeNode>();
		queue1.add(sTree);
		queue2.add(this);
		SimpleTreeNode sNode1, sNode2, child1, child2;
		Enumeration<SimpleTreeNode> childEnum = null;
		while (!queue1.isEmpty()) {
			sNode1 = queue1.remove();
			sNode2 = queue2.remove();
			childEnum = sNode1.children();
			while (childEnum.hasMoreElements()) {
				child1 = childEnum.nextElement();
				child2 = new SimpleTreeNode(child1.getNodeType(),
						child1.getSourceCodeRange(), child1.getStrValue(),
						child1.getEditAndRoletype(), defaultIndex);
				sNode2.add(child2);
				queue1.add(child1);
				queue2.add(child2);
			}
		}
	}

	public SimpleTreeNode(SimpleTreeNode sTree) {
		this(sTree.getNodeType(), sTree.getSourceCodeRange(), sTree
				.getStrValue(), sTree.getEditAndRoletype(), sTree.nodeIndex);
		Queue<SimpleTreeNode> queue1 = new LinkedList<SimpleTreeNode>();
		Queue<SimpleTreeNode> queue2 = new LinkedList<SimpleTreeNode>();
		queue1.add(sTree);
		queue2.add(this);
		SimpleTreeNode sNode1, sNode2, child1, child2;
		Enumeration<SimpleTreeNode> childEnum = null;
		while (!queue1.isEmpty()) {
			sNode1 = queue1.remove();
			sNode2 = queue2.remove();
			childEnum = sNode1.children();
			while (childEnum.hasMoreElements()) {
				child1 = childEnum.nextElement();
				child2 = new SimpleTreeNode(child1.getNodeType(),
						child1.getSourceCodeRange(), child1.getStrValue(),
						child1.getEditAndRoletype(), child1.nodeIndex);
				sNode2.add(child2);
				queue1.add(child1);
				queue2.add(child2);
			}
		}
	}

	public void addEntryToEditAndRoletype(Integer editIndex, Integer roleType) {
		editAndRoletype.put(editIndex, roleType);
	}

	public int countNodes() {
		int counter = 1;
		Queue<SimpleTreeNode> queue = new LinkedList<SimpleTreeNode>();
		SimpleTreeNode temp = null;
		queue.add(this);
		while (!queue.isEmpty()) {
			temp = queue.remove();
			if (temp.getChildCount() > 0) {
				counter += temp.getChildCount();
				queue.addAll(temp.children);
			}
		}
		return counter;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof SimpleTreeNode))
			return false;
		SimpleTreeNode other = (SimpleTreeNode) obj;
		if (!this.scr.equals(other.scr))
			return false;
		if (!this.strValue.equals(other.strValue))
			return false;
		if (this.nodeIndex != other.nodeIndex)
			return false;
		return true;
	}

	public short getContainHop() {
		return containHop;
	}

	public short getControlHop() {
		return controlHop;
	}

	public short getDataHop() {
		return dataHop;
	}

	public Map<Integer, Integer> getEditAndRoletype() {
		return editAndRoletype;
	}

	public Set<Integer> getEditIndexes() {
		return editAndRoletype.keySet();
	}

	public boolean getGeneralMark() {
		return isGeneral;
	}

	public int getNodeIndex() {
		return nodeIndex;
	}

	public int getNodeType() {
		return astNodeType;
	}

	public boolean getSelectedMark() {
		return selected;
	}

	public SourceCodeRange getSourceCodeRange() {
		return scr;
	}

	public String getStrValue() {
		return strValue;
	}

	public Collection<Integer> getTypes() {
		return editAndRoletype.values();
	}

	public int hashCode() {
		return this.nodeIndex * 10000 + this.scr.hashCode() * 100
				+ this.strValue.hashCode();
	}

	public SimpleTreeNode lookforNodeBasedOnEditIndex(int editIndex) {
		Enumeration<SimpleTreeNode> sEnum = this.breadthFirstEnumeration();
		SimpleTreeNode sTmp = null;
		while (sEnum.hasMoreElements()) {
			sTmp = sEnum.nextElement();
			if (sTmp.getEditIndexes().contains(editIndex))
				return sTmp;
		}
		return null;
	}

	public SimpleTreeNode lookforNodeBasedOnIndex(int nodeIndex) {
		Enumeration<SimpleTreeNode> sEnum = this.breadthFirstEnumeration();
		SimpleTreeNode sTmp = null;
		while (sEnum.hasMoreElements()) {
			sTmp = sEnum.nextElement();
			if (sTmp.getNodeIndex() == nodeIndex)
				return sTmp;
		}
		return null;
	}

	public static Node lookforNodeBasedOnRange(Node tree, SimpleTreeNode sNode) {
		List<Node> nodes = tree.lookforNodeBasedOnRange(sNode.scr);
		for (Node n : nodes) {
			if (n.getStrValue().equals(sNode.getStrValue())) {
				return n;
			}
		}
		return null;
	}

	public SimpleTreeNode lookforNodeBasedOnRange(AbstractNode node) {
		List<SimpleTreeNode> sNodes = lookforNodesBasedOnRange(node
				.getSourceCodeRange());
		for (SimpleTreeNode sNode : sNodes) {
			if (sNode.getStrValue().equals(node.getStrValue())) {
				return sNode;
			}
		}
		return null;
	}

	private List<SimpleTreeNode> lookforNodesBasedOnRange(SourceCodeRange scr) {
		List<SimpleTreeNode> result = new ArrayList<SimpleTreeNode>();
		Enumeration<SimpleTreeNode> depthFirstEnumeration = this
				.depthFirstEnumeration();
		SimpleTreeNode temp = null;
		while (depthFirstEnumeration.hasMoreElements()) {
			temp = depthFirstEnumeration.nextElement();
			if (temp.getSourceCodeRange().equals(scr)) {
				result.add(temp);
			}
		}
		return result;
	}

	public void setContainHop(short hop) {
		containHop = hop;
	}

	public void setControlHop(short hop) {
		controlHop = hop;
	}

	public void setDataHop(short hop) {
		dataHop = hop;
	}

	public void disableGeneral() {
		isGeneral = false;
	}

	public void disableSelected() {
		selected = false;
	}

	public void enableGeneral() {
		isGeneral = true;
	}

	public void enableSelected() {
		selected = true;
	}

	/**
	 * Assumption: All relevant nodes construct a subtree
	 * 
	 * @return
	 */
	public List<SimpleTreeNode> pruneRelevant() {
		SimpleTreeNode newTree = new SimpleTreeNode(this.getNodeType(),
				this.getSourceCodeRange(), this.getStrValue(),
				this.getEditAndRoletype(), 1);
		Enumeration<SimpleTreeNode> sEnum = breadthFirstEnumeration();
		SimpleTreeNode tmp = null, tmp2 = null, tmpParent = null;
		Map<SimpleTreeNode, SimpleTreeNode> map = new HashMap<SimpleTreeNode, SimpleTreeNode>();
		map.put(this, newTree);
		while (sEnum.hasMoreElements()) {
			tmp = sEnum.nextElement();
			if (!tmp.getEditAndRoletype().isEmpty() || tmp.getSelectedMark()) {
				tmpParent = (SimpleTreeNode) tmp.getParent();
				if (tmpParent == null) {// this is the method node
					continue;
				}
				tmp2 = new SimpleTreeNode(tmp.getNodeType(),
						tmp.getSourceCodeRange(), tmp.getStrValue(),
						tmp.getEditAndRoletype(), tmp.getNodeIndex());
				if (tmpParent != null) {
					while (!map.containsKey(tmpParent)) {
						tmpParent = (SimpleTreeNode) tmpParent.getParent();
					}
					map.get(tmpParent).add(tmp2);
				}
				map.put(tmp, tmp2);
			}
		}
		List<SimpleTreeNode> result = new ArrayList<SimpleTreeNode>();
		for (int i = 0; i < newTree.getChildCount(); i++) {
			result.add((SimpleTreeNode) newTree.getChildAt(i));
		}
		return result;
	}

	public void setNodeIndex(int index) {
		this.nodeIndex = index;
	}

	public void setNodeType(int nodeType) {
		astNodeType = nodeType;
	}

	public void setStrValue(String str) {
		this.strValue = str;
	}

	// public void setType(int type) {
	// this.types.set(type);
	// }

	@Override
	public String toString() {
		return strValue + scr.toString();
	}
}
