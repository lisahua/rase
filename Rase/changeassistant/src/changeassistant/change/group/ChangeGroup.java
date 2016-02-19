package changeassistant.change.group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;

import changeassistant.change.group.edits.AbstractTreeEditOperation2;
import changeassistant.change.group.edits.SubTreeDeleteOperation;
import changeassistant.change.group.edits.SubTreeInsertOperation;
import changeassistant.change.group.edits.SubTreeMoveOperation;
import changeassistant.change.group.edits.SubTreeUpdateOperation;
import changeassistant.change.group.model.SubTreeModel;
import changeassistant.changesuggestion.expression.representation.AbstractExpressionRepresentationGenerator;
import changeassistant.main.ChangeAssistantMain;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.treematching.edits.AbstractTreeEditOperation;
import changeassistant.versions.treematching.edits.DeleteOperation;
import changeassistant.versions.treematching.edits.ITreeEditOperation.EDIT;
import changeassistant.versions.treematching.edits.InsertOperation;
import changeassistant.versions.treematching.edits.MoveOperation;
import changeassistant.versions.treematching.edits.UpdateOperation;

public class ChangeGroup {
	private List<Integer> editsInvolved;
	public Node subTree;
	public SubTreeModel subTreeModel;
	public List<AbstractTreeEditOperation2<SubTreeModel>> editScriptOnSubTree;
	public Set<Node> knownGroup;
	public Set<Node> knownNodesInOriginalRevision, knownNodesInNewRevision;

	public boolean isValid;
	private GroupManager gm;

	public ChangeGroup(GroupManager gm, Set<Node> knownGroup) {
		this.gm = gm;
		this.isValid = true;
		this.knownGroup = knownGroup;
		Iterator<Node> iter = knownGroup.iterator();
		Set<Integer> edits = new HashSet<Integer>();
		while (iter.hasNext()) {
			edits.addAll(iter.next().getEditIndexes());
		}
		editsInvolved = new ArrayList<Integer>(edits);
		Collections.sort(editsInvolved);
	}

	public SubTreeModel apply(AbstractTreeEditOperation edit1,
			AbstractTreeEditOperation2 edit2, SubTreeModel subTreeModel) {
		edit1.apply();
		edit2.apply();
		SubTreeModel result = (SubTreeModel) subTreeModel.deepCopy();
		subTreeModel = null;
		return result;
	}

	public void collectKnownNodesInNewMethod() {
		knownNodesInNewRevision = new HashSet<Node>();
		Node newNode = gm.getNewNode();
		Node updatedOldNode = gm.getUpdatedOldNode();
		Node tempNode = null;
		for (Node node : knownGroup) {
			tempNode = updatedOldNode.lookforNodeBasedOnRange(node);
			if (tempNode != null && tempNode.equals(node)) {
				tempNode = (Node) newNode.lookforNodeBasedOnPosition(tempNode);
				knownNodesInNewRevision.add(tempNode);
			}
		}
	}

	public void collectKnownNodesInOldMethod() {
		knownNodesInOriginalRevision = new HashSet<Node>();
		Node oldNode = gm.getOldNode();
		Node tempNode = null;
		for (Node node : knownGroup) {
			tempNode = oldNode.lookforNodeBasedOnRange(node);
			if (tempNode != null && tempNode.equals(node)) {// oldNode may be
															// not ancestor of
															// the given node
															// since they are
															// from two trees
				knownNodesInOriginalRevision.add(node);
			}
		}
	}

	public void constructSubTree() {
		if (this.knownNodesInOriginalRevision.isEmpty())
			return;// do nothing, since no AST Node is known as hint for the
					// change
		subTree = (Node) gm.getOldNode().deepCopy();// branch pruning algorithm
		subTree.clearMatchedHierarchical();
		for (Node node : knownNodesInOriginalRevision) {// use isMatched to
			subTree.lookforNodeBasedOnRange(node).enableMatched();
		}
		Set<Node> parentNodes = new HashSet<Node>();
		try {
			Node tempSubTree = (Node) subTree.deepCopy();
			tempSubTree.clearMatchedHierarchical();

			Node treeToRemove = null;
			for (Node node : knownNodesInOriginalRevision) {// use isMatched to
															// mark all the
															// nodes in the
															// group
				tempSubTree.lookforNodeBasedOnRange(node).enableMatched();
			}
			Node temp = null;

			Node temp2 = null;
			Node temp3 = null;
			AbstractTreeEditOperation edit = null;
			for (Integer editIndex : editsInvolved) {
				edit = gm.edits.get(editIndex);
				temp = edit.getNode();
				temp2 = tempSubTree.lookforNodeBasedOnRange(temp);
				temp3 = subTree.lookforNodeBasedOnRange(temp);
				if (temp2 != null) {
					if (!temp2.getEditIndexes().contains(editIndex)) {
						temp2.setEditIndex(editIndex);
						temp3.setEditIndex(editIndex);
					}
					temp3.setEDIT(edit.getOperationType());
				}
				if (edit.getParentNode() != null
						&& (edit.getOperationType().equals(EDIT.INSERT) || edit
								.getOperationType().equals(EDIT.MOVE))) {
					parentNodes.add(edit.getParentNode());
				} else {
					// System.out.println("The parent node is null!");
				}
				if (edit.getOperationType().equals(EDIT.MOVE)) {
					MoveOperation move = (MoveOperation) edit;
					if (move.getNewParentNode() != null) {
						parentNodes.add(move.getNewParentNode());
					} else {
						System.out.println("The parent node is null!");
					}
				}
			}

			// use pruning to remove all the subtrees which do not contain any
			// marked node
			// it's not secure to enumerate one tree while pruning it
			Queue<Node> queue = new LinkedList<Node>();
			queue.add(tempSubTree);
			while (!queue.isEmpty()) {
				temp = queue.remove();
				if (!temp.containMatchedDescendant()) {
					treeToRemove = subTree.lookforNodeBasedOnRange(temp);
					if (treeToRemove != null
							&& treeToRemove.getParent() != null) {
						((Node) treeToRemove.getParent()).remove(treeToRemove);
					} else {
						// the tree is removed already
					}
				} else {// the node itself or its descendant is edited
					if (isMoved(temp)) {
						// the node is not visited again, since we do not prune
						// this subtree any more
					} else if (isSwitch(temp)) {
						Enumeration<Node> children = temp.children();
						List<Node> controlNodeList = new ArrayList<Node>();
						Node child = null;
						boolean needAll = false;
						while (children.hasMoreElements()) {
							child = children.nextElement();
							if (child.getNodeType() == ASTNode.SWITCH_CASE
									|| child.getNodeType() == ASTNode.BREAK_STATEMENT) {
								controlNodeList.add(child);
							}
							if (!child.getEditIndexes().isEmpty()) {
								needAll = true;
							}
						}
						if (needAll) {
							children = temp.children();
							while (children.hasMoreElements()) {
								child = children.nextElement();
								if (!controlNodeList.contains(child)) {
									Enumeration<Node> grandChildren = child
											.children();
									while (grandChildren.hasMoreElements()) {
										queue.add(grandChildren.nextElement());
									}
								}
							}
						} else {
							children = temp.children();
							while (children.hasMoreElements()) {
								queue.add(children.nextElement());
							}
						}
					} else {
						Enumeration<Node> children = temp.children();
						while (children.hasMoreElements()) {
							queue.add(children.nextElement());
						}
					}
				}
			}

			while (!subTree.isMatched()) {
				if (subTree.getChildCount() > 1
						|| parentNodes.contains(subTree)
						|| subTree.getChildCount() == 0) {
					break;
				} else {
					subTree = (Node) subTree.children().nextElement();
				}
			}
			if (ChangeAssistantMain.PRINT_INFO)
				System.out.println("# of nodes in the subtree : "
						+ subTree.countNodes());
			tempSubTree = null;
			queue = null;
			parentNodes = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void constructSubTree2() {
		if (this.knownNodesInNewRevision.isEmpty())
			return;// do nothing, since no AST Node is known as hint for the
					// change
		subTree = (Node) gm.getNewNode().deepCopy();// branch pruning algorithm
		subTree.clearMatchedHierarchical();
		for (Node node : knownNodesInNewRevision) {// use isMatched to
			subTree.lookforNodeBasedOnRange(node).enableMatched();
		}
		try {
			Node tempSubTree = (Node) subTree.deepCopy();
			tempSubTree.clearMatchedHierarchical();

			Node treeToRemove = null;
			for (Node node : knownNodesInNewRevision) {// use isMatched to
														// mark all the
														// nodes in the
														// group
				tempSubTree.lookforNodeBasedOnRange(node).enableMatched();
			}
			Node temp = null;
			// use pruning to remove all the subtrees which do not contain any
			// marked node
			// it's not secure to enumerate one tree while pruning it
			Queue<Node> queue = new LinkedList<Node>();
			queue.add(tempSubTree);
			while (!queue.isEmpty()) {
				temp = queue.remove();
				if (!temp.containMatchedDescendant()) {
					treeToRemove = subTree.lookforNodeBasedOnRange(temp);
					if (treeToRemove != null
							&& treeToRemove.getParent() != null) {
						((Node) treeToRemove.getParent()).remove(treeToRemove);
					} else {
						// the tree is removed already
					}
				} else {// the node itself or its descendant is edited
					if (isSwitch(temp)) {
						Enumeration<Node> children = temp.children();
						List<Node> controlNodeList = new ArrayList<Node>();
						Node child = null;
						boolean needAll = false;
						while (children.hasMoreElements()) {
							child = children.nextElement();
							if (child.getNodeType() == ASTNode.SWITCH_CASE
									|| child.getNodeType() == ASTNode.BREAK_STATEMENT) {
								controlNodeList.add(child);
							}
							if (!child.getEditIndexes().isEmpty()) {
								needAll = true;
							}
						}
						if (needAll) {
							children = temp.children();
							while (children.hasMoreElements()) {
								child = children.nextElement();
								if (!controlNodeList.contains(child)) {
									Enumeration<Node> grandChildren = child
											.children();
									while (grandChildren.hasMoreElements()) {
										queue.add(grandChildren.nextElement());
									}
								}
							}
						} else {
							children = temp.children();
							while (children.hasMoreElements()) {
								queue.add(children.nextElement());
							}
						}
					} else {
						Enumeration<Node> children = temp.children();
						while (children.hasMoreElements()) {
							queue.add(children.nextElement());
						}
					}
				}
			}

			while (!subTree.isMatched()) {
				if (subTree.getChildCount() > 1 || subTree.getChildCount() == 0) {
					break;
				} else {
					subTree = (Node) subTree.children().nextElement();
				}
			}
			if (ChangeAssistantMain.PRINT_INFO)
				System.out.println("# of nodes in the subtree : "
						+ subTree.countNodes());
			tempSubTree = null;
			queue = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<SubTreeModel> convertToRelativePosition(int position,
			Node parent, Node parentInSubTree, SubTreeModel sParent) {
		List<SubTreeModel> result = new ArrayList<SubTreeModel>();
		if (position == 0 || parentInSubTree == null)
			return result;// the relevant before-siblings' list is empty since
							// the
		Enumeration<Node> children = parent.children();
		Node child = null;
		int counter = 0;
		while (children.hasMoreElements()) {
			if (counter < position) {
				child = children.nextElement();
				Node childInSubTree = parentInSubTree
						.lookforNodeBasedOnRange(child);
				if (childInSubTree != null) {
					result.add((SubTreeModel) sParent
							.lookforNodeBasedOnPosition(childInSubTree));
				}
				counter++;
			} else {
				break;
			}
		}
		return result;
	}

	/**
	 * To count siblings after the node
	 * 
	 * @param position
	 * @param parent
	 * @param parentInSubTree
	 * @param sParent
	 * @return
	 */
	private List<SubTreeModel> convertToRelativePosition2(int position,
			Node parent, Node parentInSubTree, SubTreeModel sParent) {
		List<SubTreeModel> result = new ArrayList<SubTreeModel>();
		if (parentInSubTree == null)
			return result;// the relevant before-siblings' list is empty since
							// the
		Enumeration<Node> children = parent.children();
		Node child = null;
		int counter = 0;
		while (children.hasMoreElements()) {
			child = children.nextElement();
			if (counter > position) {
				Node childInSubTree = parentInSubTree
						.lookforNodeBasedOnRange(child);
				if (childInSubTree != null) {
					result.add((SubTreeModel) sParent
							.lookforNodeBasedOnPosition(childInSubTree));
				}
			} else {
				// do nothing
			}
			counter++;
		}
		return result;
	}

	/**
	 * The mapping between oldNode and subTree as well as that between subTree
	 * and abstractSubTree are both used. During the process, subTree will be
	 * changed as the edit scripts are generated
	 */
	private List<AbstractTreeEditOperation2<SubTreeModel>> createEditScriptsOnSubTree(
			AbstractExpressionRepresentationGenerator generator) {
		SubTreeModel subTreeModelCopy1 = (SubTreeModel) this.subTreeModel
				.deepCopy();
		SubTreeModel subTreeModelCopy2 = (SubTreeModel) this.subTreeModel
				.deepCopy();
		Node subTreeCopy = (Node) this.subTree.deepCopy();
		AbstractTreeEditOperation edit = null;
		List<AbstractTreeEditOperation2<SubTreeModel>> subTreeEditScript = new ArrayList<AbstractTreeEditOperation2<SubTreeModel>>();
		// System.out.print("");
		Node editedNode, editedNodeOnSubTree, parent, parentInSubTree;
		SubTreeModel sEditedNode, sParent;
		List<SubTreeModel> relevantSiblingsBefore;
		List<SubTreeModel> relevantSiblingsAfter;
		AbstractTreeEditOperation editOnSubTree = null;
		AbstractTreeEditOperation2<SubTreeModel> editOnSubTreeModel = null, editCopyOnSubTreeModel = null;
		for (Integer editIndex : editsInvolved) {
			edit = gm.edits.get(editIndex);
			editedNode = edit.getNode();
			editedNodeOnSubTree = subTreeCopy
					.lookforNodeBasedOnRange(editedNode);

			parent = edit.getParentNode();
			parentInSubTree = subTreeCopy.lookforNodeBasedOnRange(parent);
			sParent = (SubTreeModel) subTreeModelCopy1
					.lookforNodeBasedOnPosition(parentInSubTree);
			relevantSiblingsBefore = convertToRelativePosition(
					edit.getPosition(), parent, parentInSubTree, sParent);
			if (edit.getOperationType().equals(EDIT.INSERT)
					|| edit.getOperationType().equals(EDIT.MOVE)) {
				relevantSiblingsAfter = convertToRelativePosition2(
						edit.getPosition() - 1, parent, parentInSubTree,
						sParent);
			} else {
				relevantSiblingsAfter = convertToRelativePosition2(
						edit.getPosition(), parent, parentInSubTree, sParent);
			}

			switch (edit.getOperationType()) {
			case INSERT: {
				sEditedNode = new SubTreeModel((Node) editedNode.clone(),
						gm.getNewNode(), false, generator);
				// int location = parentInSubTree.getChildCount() -
				// relevantSiblingsAfter.size();
				int location = relevantSiblingsBefore.size();
				editOnSubTree = new InsertOperation((Node) editedNode.clone(),
						editedNode.getASTExpressions2(), parentInSubTree,
						location);// instead of using
									// "relevantSiblingsBefore.size()"

				editOnSubTreeModel = new SubTreeInsertOperation(sEditedNode,
						sParent, relevantSiblingsBefore, relevantSiblingsAfter);
				editCopyOnSubTreeModel = new SubTreeInsertOperation(
						(SubTreeModel) sEditedNode.clone(),
						(SubTreeModel) subTreeModelCopy2
								.lookforNodeBasedOnPosition(sParent),
						subTreeModelCopy2
								.lookforNodeBasedOnPositions(relevantSiblingsBefore),
						subTreeModelCopy2
								.lookforNodeBasedOnPositions(relevantSiblingsAfter));
			}
				break;
			case DELETE: {
				sEditedNode = (SubTreeModel) subTreeModelCopy1
						.lookforNodeBasedOnPosition(editedNodeOnSubTree);
				editOnSubTree = new DeleteOperation(editedNodeOnSubTree);

				editOnSubTreeModel = new SubTreeDeleteOperation(sEditedNode,
						sParent, relevantSiblingsBefore, relevantSiblingsAfter);
				editCopyOnSubTreeModel = new SubTreeDeleteOperation(
						(SubTreeModel) subTreeModelCopy2
								.lookforNodeBasedOnPosition(sEditedNode),
						(SubTreeModel) subTreeModelCopy2
								.lookforNodeBasedOnPosition(sParent),
						subTreeModelCopy2
								.lookforNodeBasedOnPositions(relevantSiblingsBefore),
						subTreeModelCopy2
								.lookforNodeBasedOnPositions(relevantSiblingsAfter));
			}
				break;
			case UPDATE: {
				UpdateOperation update = (UpdateOperation) edit;
				sEditedNode = (SubTreeModel) subTreeModelCopy1
						.lookforNodeBasedOnPosition(editedNodeOnSubTree);
				editOnSubTree = new UpdateOperation(editedNodeOnSubTree,
						update.getNewNode(), update.getNewValue());
				SubTreeModel sNewNode = new SubTreeModel(update.getNewNode(),
						gm.getNewNode(), false, generator);
				editOnSubTreeModel = new SubTreeUpdateOperation(sEditedNode,
						sParent, relevantSiblingsBefore, relevantSiblingsAfter,
						sNewNode);
				editCopyOnSubTreeModel = new SubTreeUpdateOperation(
						(SubTreeModel) subTreeModelCopy2
								.lookforNodeBasedOnPosition(sEditedNode),
						(SubTreeModel) subTreeModelCopy2
								.lookforNodeBasedOnPosition(sParent),
						subTreeModelCopy2
								.lookforNodeBasedOnPositions(relevantSiblingsBefore),
						subTreeModelCopy2
								.lookforNodeBasedOnPositions(relevantSiblingsAfter),
						sNewNode);
				sNewNode = null;
			}
				break;
			case MOVE: {
				MoveOperation move = (MoveOperation) edit;
				sEditedNode = (SubTreeModel) subTreeModelCopy1
						.lookforNodeBasedOnPosition(editedNodeOnSubTree);

				Node newParent = move.getNewParentNode();
				Node newParentInSubTree = subTreeCopy
						.lookforNodeBasedOnRange(newParent);
				// System.out.print("");
				SubTreeModel sNewParent = (SubTreeModel) subTreeModelCopy1
						.lookforNodeBasedOnPosition(newParentInSubTree);
				int newLocation = move.getNewPosition();
				boolean hasSameParent = false;
				if (newParent.equals(parent)
						&& editedNode.locationInParent() < newLocation) {
					hasSameParent = true;
					newLocation++;
				}
				List<SubTreeModel> newSiblingsBefore = convertToRelativePosition(
						newLocation, newParent, newParentInSubTree, sNewParent);
				List<SubTreeModel> newSiblingsAfter = convertToRelativePosition2(
						move.getNewPosition(), newParent, newParentInSubTree,
						sNewParent);
				if (hasSameParent) {
					newSiblingsBefore.remove(sEditedNode);
				}
				boolean shouldMove = false;
				if (!hasSameParent
						|| newSiblingsBefore.size() != relevantSiblingsBefore
								.size()) {
					shouldMove = true;
					editOnSubTree = new MoveOperation(editedNodeOnSubTree,
							move.getNewNode(), newParentInSubTree,
							newSiblingsBefore.size());
					editOnSubTreeModel = new SubTreeMoveOperation(sEditedNode,
							sParent, relevantSiblingsBefore,
							relevantSiblingsAfter, sNewParent,
							newSiblingsBefore, newSiblingsAfter);
					editCopyOnSubTreeModel = new SubTreeMoveOperation(
							(SubTreeModel) subTreeModelCopy2
									.lookforNodeBasedOnPosition(sEditedNode),
							(SubTreeModel) subTreeModelCopy2
									.lookforNodeBasedOnPosition(sParent),
							subTreeModelCopy2
									.lookforNodeBasedOnPositions(relevantSiblingsBefore),
							subTreeModelCopy2
									.lookforNodeBasedOnPositions(relevantSiblingsAfter),
							(SubTreeModel) subTreeModelCopy2
									.lookforNodeBasedOnPosition(sNewParent),
							subTreeModelCopy2
									.lookforNodeBasedOnPositions(newSiblingsBefore),
							subTreeModelCopy2
									.lookforNodeBasedOnPositions(newSiblingsAfter));
				}
				// int location = newParent.getChildCount() -
				// newSiblingsAfter.size();//count from the end
				newParent = null;
				newParentInSubTree = null;
				sNewParent = null;
				newSiblingsBefore = newSiblingsAfter = null;
				if (!shouldMove) {
					continue;
				}
			}
				break;
			}
			subTreeEditScript.add(editCopyOnSubTreeModel);
			subTreeModelCopy2 = apply(editOnSubTree, editOnSubTreeModel,
					subTreeModelCopy1);
			edit = editOnSubTree = null;
			editOnSubTreeModel = editCopyOnSubTreeModel = null;
			editedNode = null;
			editedNodeOnSubTree = null;
			parent = parentInSubTree = null;
			sEditedNode = sParent = null;
			relevantSiblingsBefore = relevantSiblingsAfter = null;
		}
		return subTreeEditScript;
	}

	public void groupBasedOnOldRevision() {
		// Set<NodePair> edges = new HashSet<NodePair>();
		List<AbstractTreeEditOperation> edits = gm.edits;
		Node oldNode = gm.getOldNode();
		Set<Node> nodesDependingOn = new HashSet<Node>();
		Node nodeConcerned = null;
		for (Node node : knownGroup) {
			if (node.getEditIndexes().isEmpty()) {
				// do nothing, this node does not experience change, so there is
				// no need to process it
			} else {
				// to look for all changes which have original context, parse
				// out dependency for that
				List<Integer> editIndexes = node.getEditIndexes();
				List<Node> tempList;
				for (int i = 0; i < editIndexes.size(); i++) {
					AbstractTreeEditOperation edit = edits.get(editIndexes
							.get(i));
					// add the parent node
					if (GroupManager.isParentConsidered)
						nodesDependingOn.add(edit.getParentNode());
					switch (edit.getOperationType()) {// the case INSERT is not
														// cared about
					case MOVE:
					case UPDATE: {// the moved node may be an original node in
									// the old tree, or a new inserted node
						nodeConcerned = edit.getNode();
						nodeConcerned = oldNode
								.lookforNodeBasedOnRange(nodeConcerned);
						if (nodeConcerned == null)
							break;
						tempList = gm
								.findControlDependingNodesInOldRevision(nodeConcerned);
						nodesDependingOn.addAll(tempList
						/*
						 * gm.findControlDependingNodesInOldRevision(nodeConcerned
						 * )
						 */);

						tempList = gm.findDataDependingNodes(nodeConcerned);
						nodesDependingOn.addAll(tempList/*
														 * gm
														 * .findDataDependingNodes
														 * (nodeConcerned)
														 */);
						nodesDependingOn.add(nodeConcerned);// the changed node
															// itself is also
															// included in the
															// group
					}
						break;
					}
				}
			}
		}
		knownGroup.addAll(nodesDependingOn);
		nodesDependingOn = null;
	}

	public List<Integer> getEditsInvolved() {
		return this.editsInvolved;
	}

	public boolean getStatus() {
		return this.isValid;
	}

	public boolean isMoved(Node temp) {
		List<Integer> editIndexes = temp.getEditIndexes();
		for (Integer editIndex : editIndexes) {
			if (gm.edits.get(editIndex).getOperationType().equals(EDIT.MOVE))
				return true;
		}
		return false;
	}

	public boolean isSwitch(Node temp) {
		return temp.getNodeType() == ASTNode.SWITCH_STATEMENT;
	}

	public void refineTransformationRules() {
		AbstractExpressionRepresentationGenerator generator = new AbstractExpressionRepresentationGenerator();
		subTreeModel = new SubTreeModel(subTree, gm.getOldNode(), true,
				generator);
		editScriptOnSubTree = createEditScriptsOnSubTree(generator);
	}

	public void refineTransformationRules2() {
		AbstractExpressionRepresentationGenerator generator = new AbstractExpressionRepresentationGenerator();
		subTreeModel = new SubTreeModel(subTree, gm.getNewNode(), true,
				generator);
	}

	public void setEditsInvolved(List<Integer> editsInvolved) {
		this.editsInvolved = editsInvolved;
	}

	public void setToInvalid() {
		this.isValid = false;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		int index = 0;
		Node oldNode = gm.getOldNode();
		Node temp = null;

		sb.append("Known nodes in the group: \n");
		for (Node node : knownGroup) {
			sb.append(node.getStrValue() + "  ");
		}
		return sb.toString();
	}
}
