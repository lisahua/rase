package changeassistant.model;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import javax.swing.tree.DefaultMutableTreeNode;

import changeassistant.peers.SourceCodeRange;
import changeassistant.versions.treematching.edits.ITreeEditOperation.EDIT;

public abstract class AbstractNode extends DefaultMutableTreeNode {

	protected static final long serialVersionUID = 1L;
	public static final int MARKER_PROPERTY = 1;
	public static final int NOT_EXIST = -1;
	// the next index to use for marking purpose
	public static final int MAX_MARKER_INDEX = 0;

	public static enum EDITED_TYPE {
		INSERTED, DELETED, MOVED, UPDATED, NONE_EDIT
	};

	protected int role = -1;

	protected EDITED_TYPE et;

	protected Set<EDIT> editTypes;

	protected SourceCodeRange range;

	protected String strValue;

	protected Map<Integer, Integer> properties = null;

	protected int astNodeType;

	public int getProperty(int property) {
		if (properties == null || !properties.containsKey(property))
			return NOT_EXIST;
		return properties.get(property);
	}

	/**
	 * return false if the property has been set
	 * 
	 * @param property
	 * @param value
	 * @return
	 */
	public boolean setProperty(int property, int value) {
		if (properties == null)
			properties = new HashMap<Integer, Integer>();
		if (properties.containsKey(property))
			return false;
		properties.put(property, value);
		return true;
	}

	public Object clone() {
		Object obj = super.clone();
		AbstractNode aNode = (AbstractNode) obj;
		if (this.properties != null) {
			aNode.properties = new HashMap<Integer, Integer>(this.properties);
		}
		return obj;
	}

	public int countNodes() {
		int counter = 1;
		Queue<AbstractNode> queue = new LinkedList<AbstractNode>();
		AbstractNode temp = null;
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

	public AbstractNode deepCopy() {
		AbstractNode copy = (AbstractNode) this.clone();
		Stack<AbstractNode> stackOriginal = new Stack<AbstractNode>();
		Stack<AbstractNode> stackNew = new Stack<AbstractNode>();
		stackOriginal.push(this);
		stackNew.push(copy);

		while (!stackOriginal.isEmpty()) {// depth traverse-first root order
			AbstractNode originalNode = stackOriginal.pop();
			AbstractNode newNode = stackNew.pop();
			Enumeration<AbstractNode> children = originalNode.children();
			while (children.hasMoreElements()) {
				AbstractNode child = children.nextElement();
				AbstractNode childCopy = (AbstractNode) child.clone();
				newNode.add(childCopy);
				stackOriginal.push(child);
				stackNew.push(childCopy);
			}
		}
		return copy;
	}

	/**
	 * The basic equals() function, whose functionality should be extended by
	 * its subclasses
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof AbstractNode))
			return false;
		AbstractNode other = (AbstractNode) obj;
		if (this.astNodeType != other.astNodeType)
			return false;
		if (this.strValue == null && other.strValue != null)
			return false;
		if (this.strValue != null && !this.strValue.equals(other.strValue))
			return false;
		return true;
	}

	public Set<EDIT> getEDITset() {
		return this.editTypes;
	}

	public EDITED_TYPE getEDITED_TYPE() {
		return this.et;
	}

	public int getNodeType() {
		return this.astNodeType;
	}

	public int getRole() {
		return role;
	}

	public SourceCodeRange getSourceCodeRange() {
		return this.range;
	}

	public String getStrValue() {
		return this.strValue;
	}

	public abstract int locationInParent();

	/**
	 * This search does not care about the content of a node, so it is possible
	 * that we may map to a totally different context with similar structure and
	 * node type or same node value of "then"
	 * 
	 * @param target
	 * @return
	 */
	public AbstractNode lookforNodeBasedOnPosition(AbstractNode target) {
		// System.out.print("");
		AbstractNode foundNode = null;
		AbstractNode node = null;
		AbstractNode node2 = null;
		int knownLocation = 0;
		if (target == null)
			return null;
		if (target.getParent() != null) {
			knownLocation = target.locationInParent();
		} else {
			knownLocation = -1;
		}
		Set<AbstractNode> potentialFoundNodes = new HashSet<AbstractNode>();
		Enumeration<AbstractNode> depthFirstEnumeration = this
				.depthFirstEnumeration();
		while (depthFirstEnumeration.hasMoreElements()) {
			AbstractNode temp = depthFirstEnumeration.nextElement();
			if ((temp.getNodeType() == target.getNodeType() || temp
					.getStrValue().equals(target.getStrValue()))) {
				if (temp.locationInParent() == knownLocation) {
					Enumeration<AbstractNode> path = target
							.pathFromAncestorEnumeration(target.getRoot());
					Enumeration<AbstractNode> path2 = temp
							.pathFromAncestorEnumeration(this.getRoot());
					while (path2.hasMoreElements() && path.hasMoreElements()) {
						node2 = path2.nextElement();
						node = path.nextElement();
						if ((node2.getNodeType() == node.getNodeType() || node2
								.getStrValue().equals(node.getStrValue()))
								&& node2.locationInParent() == node
										.locationInParent()) {// this match is
																// not suitable
																// for
																// "else"/"then"
							// because then/else are not matched due to their
							// own node type, but their if-condition
						} else {
							break;
						}
					}
					if (path2.hasMoreElements() || path.hasMoreElements()) {
						path = path2 = null;
						continue;
					} else {
						path = path2 = null;
						foundNode = temp;
						break;
					}
				} else if (knownLocation == -1) {// there is no parent
					if (temp.getParent() == null) {
						foundNode = temp;
						break;
					} else {
						potentialFoundNodes.add(temp);
					}
				}
			}
			temp = null;
		}
		if (foundNode == null && !potentialFoundNodes.isEmpty()) {
			foundNode = potentialFoundNodes.iterator().next();
		}
		potentialFoundNodes = null;
		depthFirstEnumeration = null;
		target = null;
		return foundNode;
	}

	public AbstractNode lookforNodeBasedOnPosition(AbstractNode target,
			int adjustedLocation) {
		AbstractNode foundNode = null;
		AbstractNode node = null;
		AbstractNode node2 = null;
		int knownLocation = 0;
		if (target.getParent() != null) {
			knownLocation = adjustedLocation;
		} else {
			knownLocation = 0;// if target does not have a parent, its
								// knownLocation should be 0
		}
		Enumeration<AbstractNode> depthFirstEnumeration = this
				.depthFirstEnumeration();
		while (depthFirstEnumeration.hasMoreElements()) {
			AbstractNode temp = depthFirstEnumeration.nextElement();
			if ((temp.getNodeType() == target.getNodeType() || temp
					.getStrValue().equals(target.getStrValue())) && // this is
																	// especially
																	// for
																	// "then"
					temp.locationInParent() == knownLocation) {
				Enumeration<AbstractNode> path = target
						.pathFromAncestorEnumeration(target.getRoot());
				Enumeration<AbstractNode> path2 = temp
						.pathFromAncestorEnumeration(this.getRoot());
				while (path2.hasMoreElements() && path.hasMoreElements()) {
					node2 = path2.nextElement();
					node = path.nextElement();
					if ((node2.getNodeType() == node.getNodeType() || node2
							.getStrValue().equals(node.getStrValue()))
							&& node2.locationInParent() == node
									.locationInParent()) {// this match is not
															// suitable for
															// "else"/"then"
						// because then/else are not matched due to their own
						// node type, but their if-condition
					} else {
						break;
					}
				}
				if (path2.hasMoreElements() || path.hasMoreElements()) {
					path2 = path = null;
					continue;
				} else {
					path2 = path = null;
					foundNode = temp;
					break;
				}
			} else {
				// do nothing
			}
		}
		node = node2 = null;
		depthFirstEnumeration = null;
		target = null;
		return foundNode;
	}

	public void setEDIT(EDIT et) {
		try {
			this.editTypes.add(et);
		} catch (Exception e) {
			this.editTypes = new HashSet<EDIT>();
			editTypes.add(et);
		}
	}

	public void setEDITED_TYPE(EDITED_TYPE et) {
		this.et = et;
	}

	public void setNodeType(int nodeType) {
		this.astNodeType = nodeType;
	}

	public void setRole(int role) {
		this.role = role;
	}

	public void setSourceCodeRange(SourceCodeRange scr) {
		this.range = scr;
	}

	public void setStrValue(String strValue) {
		this.strValue = strValue;
	}
}
