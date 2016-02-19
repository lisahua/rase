package changeassistant.multipleexample.edit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import changeassistant.change.group.edits.AbstractTreeEditOperation2;
import changeassistant.multipleexample.apply.NodeIndexMaintainer;
import changeassistant.multipleexample.contextualize.datastructure.Sequence;
import changeassistant.multipleexample.match.EmbeddedTreeCreator;
import changeassistant.multipleexample.match.PatternUtil;
import changeassistant.multipleexample.partition.datastructure.ChangeSummary;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.multipleexample.util.LongestCommonSubsequence;
import changeassistant.peers.SourceCodeRange;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.treematching.edits.AbstractTreeEditOperation;
import changeassistant.versions.treematching.edits.ITreeEditOperation.EDIT;
import changeassistant.versions.treematching.edits.InsertOperation;
import changeassistant.versions.treematching.edits.MoveOperation;
import changeassistant.versions.treematching.edits.UpdateOperation;

public class STreeEditCreator {

	public Map<Integer, STreeDeleteOperation> deleteMap = null;
	public Map<Integer, AbstractTreeEditOperation2<SimpleTreeNode>> updateInsertMoveMap = null;
	public SimpleTreeNode newSTree;

	protected SimpleTreeNode createInsertEdit(InsertOperation insert,
			int index, Map<Node, Integer> nodeIndexMap,
			SimpleTreeNode newSTree, SimpleTreeNode sTree2,
			List<Integer> nodeIndexes, List<Integer> newNodeIndexes,
			List<List<SimpleASTNode>> sNodesList) {
		Node parent = null;
		Node edited = null;
		SimpleTreeNode newSTreeCopy = null;
		SimpleTreeNode parentNode = null;
		SimpleTreeNode editedNode = null;
		SimpleTreeNode embeddedParentNode = null;
		STreeInsertOperation sInsert = null;
		int newLocation = 0;
		int insertAfterAnchor = 0;
		int newNodeIndex = 0;
		int parentIndex = 0;
		List<Integer> preIndexes = null;
		List<Integer> leftIndexes = null;
		List<Integer> commonIndexes = null;
		LongestCommonSubsequence<Integer> lcs = new LongestCommonSubsequence<Integer>();
		SimpleTreeNode embeddedTree = EmbeddedTreeCreator.createEmbeddedTree(
				nodeIndexes, newSTree, sNodesList);
		parent = insert.getParentNode();
		edited = insert.getNodeToInsert();
		newNodeIndex = nodeIndexMap.get(edited);
		if (nodeIndexMap.get(parent) == null) {
			System.out.println(parent.toString());
			for (Entry<Node, Integer> entry : nodeIndexMap.entrySet()) {
				if (entry.getKey().toString().equals(parent.toString())) {
					System.out
							.println("The two nodes should have been matched");
				}
			}
		}
		parentIndex = nodeIndexMap.get(parent);
		parentNode = newSTree.lookforNodeBasedOnIndex(parentIndex);
		editedNode = sTree2.lookforNodeBasedOnIndex(newNodeIndex);
		editedNode = new SimpleTreeNode(editedNode.getNodeType(),
				editedNode.getSourceCodeRange(), editedNode.getStrValue(),
				editedNode.getNodeIndex());
		// compute new location in newSTree
		preIndexes = newNodeIndexes.subList(
				newNodeIndexes.indexOf(parentIndex),
				newNodeIndexes.indexOf(newNodeIndex));
		lcs.getLCS(nodeIndexes, preIndexes);
		// to update nodeIndexes
		leftIndexes = lcs.getLeftCSIndexes();
		commonIndexes = new ArrayList<Integer>();
		for (Integer leftIndex : leftIndexes) {
			commonIndexes.add(nodeIndexes.get(leftIndex));
		}
		if (commonIndexes.size() != 0)
			insertAfterAnchor = nodeIndexes.indexOf(commonIndexes
					.get(commonIndexes.size() - 1));
		NodeIndexMaintainer.changeIndexesForInsert(insertAfterAnchor,
				newNodeIndex, nodeIndexes);
		newLocation = getNewLocation(parentNode, commonIndexes);
		if (editedNode.getStrValue().equals("else") && newLocation == 0) {
			newLocation = 1;
		}
		newSTreeCopy = new SimpleTreeNode(newSTree);
		parentNode = newSTreeCopy.lookforNodeBasedOnIndex(parentIndex);
		sInsert = new STreeInsertOperation(editedNode, parentNode, newLocation);
		// compute new location in embeddedTree
		embeddedParentNode = getParentInEmbeddedTree(parentNode, embeddedTree);
		// System.out.print("");
		newLocation = getNewLocation2(embeddedParentNode, commonIndexes,
				nodeIndexes);
		updateInsertMoveMap.put(
				index,
				new STreeInsertOperation(new SimpleTreeNode(editedNode
						.getNodeType(), editedNode.getSourceCodeRange(),
						editedNode.getStrValue(), editedNode.getNodeIndex()),
						embeddedParentNode, newLocation));
		editedNode.getEditAndRoletype().put(index, SimpleTreeNode.EDITED);
		sInsert.apply();
		return newSTreeCopy;
	}

	protected SimpleTreeNode createMoveEdit(MoveOperation move, int index,
			Map<Node, Integer> nodeIndexMap, SimpleTreeNode newSTree,
			SimpleTreeNode sTree2, List<Integer> nodeIndexes,
			List<Integer> newNodeIndexes, List<List<SimpleASTNode>> sNodesList) {
		SimpleTreeNode embeddedTree = EmbeddedTreeCreator.createEmbeddedTree(
				nodeIndexes, newSTree, sNodesList);
		// System.out.println(sTree2.lookforNodeBasedOnIndex(84));
		STreeMoveOperation sMove = null;
		Node parent = null;
		Node edited = null;
		SimpleTreeNode newSTreeCopy = null;
		SimpleTreeNode parentNode = null;
		SimpleTreeNode editedNode = null;
		SimpleTreeNode embeddedParentNode = null;
		int newLocation = 0;
		int nodeIndex = 0;
		int parentIndex = 0;
		List<Integer> preIndexes = null;
		List<Integer> leftIndexes = null;
		List<Integer> commonIndexes = null;
		LongestCommonSubsequence<Integer> lcs = new LongestCommonSubsequence<Integer>();

		parent = move.getNewParentNode();
		edited = move.getNode();
		nodeIndex = nodeIndexMap.get(edited);
		parentIndex = nodeIndexMap.get(parent);
		parentNode = sTree2.lookforNodeBasedOnIndex(parentIndex);
		editedNode = newSTree.lookforNodeBasedOnIndex(nodeIndex);
		preIndexes = newNodeIndexes.subList(
				newNodeIndexes.indexOf(parentIndex),
				newNodeIndexes.indexOf(nodeIndex));
		lcs.getLCS(nodeIndexes, preIndexes);
		leftIndexes = lcs.getLeftCSIndexes();
		commonIndexes = new ArrayList<Integer>();
		for (Integer leftIndex : leftIndexes) {
			commonIndexes.add(nodeIndexes.get(leftIndex));
		}
		// to update nodeIndexes
		NodeIndexMaintainer.changeIndexesForMove(
				commonIndexes.get(commonIndexes.size() - 1), nodeIndex,
				nodeIndexes);
		System.out.print("");
		newLocation = getNewLocation(parentNode, commonIndexes);
		newSTreeCopy = new SimpleTreeNode(newSTree);
		parentNode = newSTreeCopy.lookforNodeBasedOnIndex(parentIndex);
		editedNode = newSTreeCopy.lookforNodeBasedOnIndex(nodeIndex);
		sMove = new STreeMoveOperation(editedNode, parentNode, newLocation);

		embeddedParentNode = getParentInEmbeddedTree(parentNode, embeddedTree);
		updateInsertMoveMap.put(
				index,
				new STreeMoveOperation(embeddedTree
						.lookforNodeBasedOnIndex(nodeIndex),
						embeddedParentNode, getNewLocation2(embeddedParentNode,
								commonIndexes, nodeIndexes)));
		sMove.apply();
		return newSTreeCopy;
	}

	protected SimpleTreeNode createUpdateEdit(UpdateOperation update,
			int index, Map<Node, Integer> nodeIndexMap,
			SimpleTreeNode newSTree,
			List<List<List<SimpleASTNode>>> simpleExprsLists, ChangeSummary cs,
			List<Integer> nodeIndexes, List<List<SimpleASTNode>> sNodesList) {
		SimpleTreeNode embeddedTree = EmbeddedTreeCreator.createEmbeddedTree(
				nodeIndexes, newSTree, sNodesList);
		STreeUpdateOperation sUpdate = null;
		STreeUpdateOperation updateOnSTree = null;
		Node edited = null;
		Node newNode = null;
		SimpleTreeNode newSTreeCopy = null;
		SimpleTreeNode newNodeOnSTree = null;
		int nodeIndex = 0;
		int nodeType = 0;
		List<List<SimpleASTNode>> simpleExprsList = null;
		edited = update.getNode();
		nodeIndex = nodeIndexMap.get(edited);

		simpleExprsList = simpleExprsLists.get(index);
		newNode = update.getNewNode();
		nodeType = newNode.getNodeType();
		newNodeOnSTree = new SimpleTreeNode(nodeType,
				new SourceCodeRange(0, 0), PatternUtil.createStrValue(nodeType,
						newNode.toString(), simpleExprsList.get(1)), -1);

		newSTreeCopy = new SimpleTreeNode(newSTree);
		updateOnSTree = new STreeUpdateOperation(
				newSTreeCopy.lookforNodeBasedOnIndex(nodeIndex), newNodeOnSTree);

		sUpdate = new STreeUpdateOperation(
				embeddedTree.lookforNodeBasedOnIndex(nodeIndex), newNodeOnSTree);
		updateInsertMoveMap.put(index, sUpdate);

		updateOnSTree.apply();
		sNodesList.set(nodeIndex - 1, simpleExprsList.get(1));
		for (Node node : nodeIndexMap.keySet()) {
			if (node.equals(edited)) {
				nodeIndexMap.remove(node);
				node.setNodeType(newNode.getNodeType());
				node.setStrValue(newNode.getStrValue());
				nodeIndexMap.put(node, nodeIndex);
				break;
			}
		}
		return newSTreeCopy;
	}

	/**
	 * Apply Order: random. Here, some deletions are condensed together to
	 * optimize the performance
	 * 
	 * side effects: update nodeIndexes as well
	 * 
	 * @param newSTree
	 * @param chgSums
	 * @return
	 */
	public SimpleTreeNode deletes(SimpleTreeNode newSTree,
			List<ChangeSummary> chgSums, List<Integer> nodeIndexes) {
		SimpleTreeNode sTmp = null;
		Enumeration<SimpleTreeNode> cEnum = null;
		boolean isDeleted = false;
		Set<Integer> editIndexes = null;

		deleteMap = new HashMap<Integer, STreeDeleteOperation>();
		this.newSTree = new SimpleTreeNode(newSTree);
		Queue<SimpleTreeNode> queue = new LinkedList<SimpleTreeNode>();
		queue.add(this.newSTree);

		while (!queue.isEmpty()) {
			sTmp = queue.remove();
			if (sTmp.getNodeIndex() != -1) {
				isDeleted = false;
				if (sTmp.getTypes().contains(SimpleTreeNode.EDITED)) {
					editIndexes = sTmp.getEditIndexes();
					for (Integer editIndex : editIndexes) {
						if (chgSums.get(editIndex).editType.equals(EDIT.DELETE)) {
							deleteMap.put(editIndex, new STreeDeleteOperation(
									sTmp));
							NodeIndexMaintainer.changeIndexesForDelete(
									sTmp.getNodeIndex(), nodeIndexes);
							isDeleted = true;
						}
					}
				}
				if (!isDeleted) {
					cEnum = sTmp.children();
					while (cEnum.hasMoreElements()) {
						queue.add(cEnum.nextElement());
					}
				}
			} else {
				cEnum = sTmp.children();
				while (cEnum.hasMoreElements()) {
					queue.add(cEnum.nextElement());
				}
			}
		}

		for (STreeDeleteOperation delete : deleteMap.values()) {
			delete.apply();
		}
		if (!chgSums.get(chgSums.size() - 1).editType.equals(EDIT.DELETE)) {
			return this.newSTree;
		}
		return null;
	}

	protected int getEmbeddedNewLocation(SimpleTreeNode parentNode,
			List<Integer> commonIndexes) {
		Enumeration<SimpleTreeNode> cEnum = parentNode.children();
		SimpleTreeNode sTmp = null;
		int newLocation = 0;
		int tmpIndex = -1;
		while (cEnum.hasMoreElements()) {
			sTmp = cEnum.nextElement();
			tmpIndex = sTmp.getNodeIndex();
			if (tmpIndex > 0 && commonIndexes.contains(tmpIndex)) {
				newLocation++;
			} else if (!commonIndexes.contains(tmpIndex)) {
				break;
			}
		}
		return newLocation;
	}

	/**
	 * get new location in newSTree
	 * 
	 * @param parentNode
	 * @param commonIndexes
	 * @return
	 */
	protected int getNewLocation(SimpleTreeNode parentNode,
			List<Integer> commonIndexes) {
		Enumeration<SimpleTreeNode> cEnum = parentNode.children();
		SimpleTreeNode sTmp = null;
		int newLocation = 0;
		int locationCounter = 0;
		int tmpIndex = -1;
		while (cEnum.hasMoreElements()) {
			sTmp = cEnum.nextElement();
			tmpIndex = sTmp.getNodeIndex();
			if (tmpIndex > 0 && commonIndexes.contains(tmpIndex)) {
				newLocation = locationCounter + 1;
			}
			locationCounter++;
		}
		return newLocation;
	}

	/**
	 * get new location in embeddedTree
	 * 
	 * @param parentNode
	 * @param commonIndexes
	 * @return
	 */
	protected int getNewLocation2(SimpleTreeNode parentNode,
			List<Integer> commonIndexes, List<Integer> nodeIndexes) {
		Enumeration<SimpleTreeNode> cEnum = parentNode.children();
		int newLocation = 0;
		int tmpIndex = -1;
		int locationCounter = 0;
		while (cEnum.hasMoreElements()) {
			tmpIndex = cEnum.nextElement().getNodeIndex();
			if (tmpIndex > 0 && commonIndexes.contains(tmpIndex)
					&& nodeIndexes.contains(tmpIndex)) {
				newLocation = locationCounter + 1;
			}
			locationCounter++;
		}
		return newLocation;
	}

	/**
	 * if the given parentIndex can find a node in the embeddedTree, return it;
	 * otherwise, return some ancestor of the parent node which is in the
	 * embeddedTree, although this may cause problems.
	 * 
	 * @param parentIndex
	 * @param embeddedTree
	 * @return This should not been NULL
	 */
	protected SimpleTreeNode getParentInEmbeddedTree(
			SimpleTreeNode tmpParentNode, SimpleTreeNode embeddedTree) {
		SimpleTreeNode result = null;
		while (tmpParentNode != null) {
			result = embeddedTree.lookforNodeBasedOnIndex(tmpParentNode
					.getNodeIndex());
			if (result == null) {
				tmpParentNode = (SimpleTreeNode) tmpParentNode.getParent();
			} else {
				break;
			}
		}
		if (result == null) {
			System.out
					.println("No parent is found although some should be found!");
		} else if (!result.equals(tmpParentNode)) {
			System.out
					.println("Be cautions about the operations on the ancestor");
		}
		return result;
	}

	/**
	 * To create and apply inserts and moves Apply Order: observe the order
	 * 
	 * @param root
	 * @param chgSums
	 * @param assistStrValues
	 * @param simpleExprsLists
	 * @param sNodesList
	 */
	public SimpleTreeNode updateInsertMove(SimpleTreeNode newSTree2,
			SimpleTreeNode sTree2, List<ChangeSummary> chgSums,
			List<List<List<SimpleASTNode>>> simpleExprsLists,
			List<Integer> indexes, Map<Node, Integer> nodeIndexMap,
			List<AbstractTreeEditOperation> edits, List<Integer> nodeIndexes,
			List<List<SimpleASTNode>> sNodesList) {
		newSTree = newSTree2;
		updateInsertMoveMap = new HashMap<Integer, AbstractTreeEditOperation2<SimpleTreeNode>>();
		ChangeSummary cs = null;
		Sequence newSequence = new Sequence(sTree2);
		List<Integer> newNodeIndexes = newSequence.getNodeIndexes();
		for (int i = 0; i < chgSums.size(); i++) {
			cs = chgSums.get(i);
			switch (cs.editType) {
			case MOVE:
				try {
					newSTree = createMoveEdit(
							(MoveOperation) edits.get(indexes.get(i)), i,
							nodeIndexMap, newSTree, sTree2, nodeIndexes,
							newNodeIndexes, sNodesList);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case INSERT:
				newSTree = createInsertEdit(
						(InsertOperation) edits.get(indexes.get(i)), i,
						nodeIndexMap, newSTree, sTree2, nodeIndexes,
						newNodeIndexes, sNodesList);
				break;
			case UPDATE:
				newSTree = createUpdateEdit(
						(UpdateOperation) edits.get(indexes.get(i)), i,
						nodeIndexMap, newSTree, simpleExprsLists, cs,
						nodeIndexes, sNodesList);
				break;
			case DELETE:
				return newSTree;
			}
		}
		return null;
	}

	public SimpleTreeNode moves2(SimpleTreeNode newSTree,
			SimpleTreeNode sTree2, List<ChangeSummary> chgSums,
			List<List<List<SimpleASTNode>>> simpleExprsLists,
			List<Integer> indexes, Map<Node, Integer> nodeIndexMap,
			List<AbstractTreeEditOperation> edits, List<Integer> nodeIndexes,
			List<List<SimpleASTNode>> sNodesList) {
		Sequence newSequence = new Sequence(sTree2);
		List<Integer> newNodeIndexes = newSequence.getNodeIndexes();
		int maxChangeIndex = Collections.max(deleteMap.keySet());
		// ASSUMPTION: all left edits should be move
		for (int i = maxChangeIndex + 1; i < chgSums.size(); i++) {
			newSTree = createMoveEdit(
					(MoveOperation) edits.get(indexes.get(i)), i, nodeIndexMap,
					newSTree, sTree2, nodeIndexes, newNodeIndexes, sNodesList);
		}
		this.newSTree = newSTree;
		return null;
	}

	public List<SimpleTreeNode> lookforRelevantNodes(EDIT editType,
			int editIndex, SimpleTreeNode root) {
		List<SimpleTreeNode> result = new ArrayList<SimpleTreeNode>();
		Enumeration<SimpleTreeNode> sEnum = root.breadthFirstEnumeration();
		SimpleTreeNode sTmp = null;
		while (sEnum.hasMoreElements()) {
			sTmp = sEnum.nextElement();
			if (sTmp.getEditIndexes().contains(editIndex)) {
				result.add(sTmp);
			}
		}
		return result;
	}
}
