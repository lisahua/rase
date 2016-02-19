package changeassistant.multipleexample.contextualize;

import java.util.ArrayList;
import java.util.Collection;
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

import changeassistant.multipleexample.partition.ChangeSummaryCreator;
import changeassistant.multipleexample.partition.CommonEditParser;
import changeassistant.multipleexample.partition.MappingException;
import changeassistant.multipleexample.partition.datastructure.BaseCluster;
import changeassistant.multipleexample.partition.datastructure.ChangeSummary;
import changeassistant.multipleexample.partition.datastructure.EditInCommonCluster;
import changeassistant.multipleexample.partition.datastructure.EditInCommonGroup;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.multipleexample.util.IdMapper;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.comparison.MethodModification;
import changeassistant.versions.treematching.edits.AbstractTreeEditOperation;
import changeassistant.versions.treematching.edits.ITreeEditOperation;
import changeassistant.versions.treematching.edits.ITreeEditOperation.EDIT;
import changeassistant.versions.treematching.edits.MoveOperation;

public class ContextualizeHelper1 {

	private EditInCommonGroup group = null;

	private EditInCommonCluster cluster = null;

	private Node updatedNode, newNode;

	public static boolean DEBUG = true;

	// map between instance and the above concerned subtrees
	private Map<Integer, List<SimpleTreeNode>> indexAndTreesMap = null;

	// map between edit groups and instances--this will help us check whether
	// the clusters are changeable
	private Map<Set<Set<Integer>>, Set<Integer>> indexSubgroupMap = null;

	public static int TOTALLY_VALID_CLUSTER = 1, PARTIALLY_VALID_CLUSTER = 2,
			NOT_VALID = 3;

	// data structures to save for later use
	private List<SimpleTreeNode> sTreeList = null;
	private List<SimpleTreeNode> sTreeList2 = null;
	private List<Node> updatedNodeList = null;
	private List<Node> newNodeList = null;
	private List<Integer> insts = null;

	// private short containUpHop = 0;
	// private short containDownHop = 0;
	// private short controlUpHop = 0;
	// private short controlDownHop = 0;
	// private short dataUpHop = 0;
	// private short dataDownHop = 0;

	public ContextualizeHelper1(EditInCommonGroup group,
			EditInCommonCluster cluster) {
		this.group = group;
		this.cluster = cluster;
	}

	public void addTrees(EditInCommonCluster clus) {
		List<SimpleTreeNode> tmpSTreeList = new ArrayList<SimpleTreeNode>(), tmpSTreeList2 = new ArrayList<SimpleTreeNode>();
		List<Node> tmpUpdatedNodeList = new ArrayList<Node>(), tmpNewNodeList = new ArrayList<Node>();
		List<Integer> tmpInsts = clus.getInstances();
		Integer tmpIndex = -1;
		for (int i = 0; i < tmpInsts.size(); i++) {
			tmpIndex = insts.indexOf(tmpInsts.get(i));
			tmpSTreeList.add(sTreeList.get(tmpIndex));
			tmpSTreeList2.add(sTreeList2.get(tmpIndex));
			tmpUpdatedNodeList.add(updatedNodeList.get(tmpIndex));
			tmpNewNodeList.add(newNodeList.get(tmpIndex));
		}
		clus.setSTree(sTreeList.get(0));
		clus.setSTreeList(tmpSTreeList);
		clus.setSTreeList2(tmpSTreeList2);
		clus.setUpdatedNodeList(tmpUpdatedNodeList);
		clus.setNewNodeList(tmpNewNodeList);
	}

	/**
	 * If all instances share the same amount of forests, we can go to the next
	 * step. Otherwise, try to refine the edits shared between them. Algorithm:
	 * If two forests intersect with each other, record the # of intersection
	 * they have and compute the largest intersection
	 * 
	 * @param cluster
	 * @return
	 */
	public int checkCluster(EditInCommonCluster cluster) {
		if (indexSubgroupMap.size() == 1)
			return TOTALLY_VALID_CLUSTER;
		List<Set<Set<Integer>>> indexSetList = new ArrayList<Set<Set<Integer>>>(
				indexSubgroupMap.keySet());
		List<Set<Set<Integer>>> rIndexSetList = new ArrayList<Set<Set<Integer>>>();

		rIndexSetList.add(indexSetList.get(0));
		Set<Set<Integer>> indexSet = null;
		// the more forests included, the earlier the indexSet is put
		for (int i = 1; i < indexSetList.size(); i++) {// reorder the indexSet
														// based on num of
														// forests contained
			int j = 0;
			indexSet = indexSetList.get(i);
			for (; j < rIndexSetList.size(); j++) {
				if (rIndexSetList.get(j).size() > indexSet.size()) {
					break;
				}
			}
			if (j == rIndexSetList.size()) {// indexSet.size() >= the size of
											// any rIndexSetList
				rIndexSetList.add(indexSet);
			} else {
				rIndexSetList.add(j, indexSet);
			}
		}
		indexSetList = rIndexSetList;
		Set<Set<Integer>> bss1 = indexSetList.get(0);

		Set<Set<Integer>> bss2 = null;
		Set<Integer> commonBS = new HashSet<Integer>();
		Set<Integer> sectSet = null;
		Set<Integer> records = new HashSet<Integer>();
		List<Set<Integer>> subindexes = new ArrayList<Set<Integer>>();
		List<Set<Integer>> rSubindexes = new ArrayList<Set<Integer>>();
		Set<Integer> tmpBitSet = new HashSet<Integer>();
		Set<Integer> commonBitSet = new HashSet<Integer>();
		int numOfEdits = -1, numOfCommonEdits = -1, sumOfCommonEdits = 0;
		for (Set<Integer> bs : bss1) {// find the largest common grouped edits
			records.clear();
			subindexes.clear();
			for (int i = 1; i < indexSetList.size(); i++) {
				bss2 = indexSetList.get(i);
				commonBitSet.clear();
				numOfCommonEdits = 0;
				for (Set<Integer> bs2 : bss2) {
					sectSet = new HashSet<Integer>(bs2);
					sectSet.retainAll(bs);
					if (!sectSet.isEmpty()) {
						tmpBitSet.clear();
						tmpBitSet.addAll(sectSet);
						numOfEdits = tmpBitSet.size();
						if (numOfEdits > numOfCommonEdits) {
							commonBitSet.clear();
							commonBitSet.addAll(tmpBitSet);
							numOfCommonEdits = commonBitSet.size();
						}
					}
				}
				bs = new HashSet<Integer>();
				bs.addAll(commonBitSet);
			}
			if (numOfCommonEdits != 0) {
				rSubindexes.add(bs);
				sumOfCommonEdits += numOfCommonEdits;
			}
		}
		if (sumOfCommonEdits < CommonEditParser.THRESHOLD_FOR_NUMBER_OF_EDITS) {
			cluster.setApplicable(false);
			return NOT_VALID;
		}
		for (int i = 0; i < rSubindexes.size(); i++) {
			commonBS.addAll(rSubindexes.get(i));
		}
		List<List<Integer>> indexesList = cluster.getIndexesList();
		List<Integer> oIndexes = null, nIndexes = null;
		for (int k = 0; k < indexesList.size(); k++) {
			oIndexes = indexesList.get(k);
			nIndexes = new ArrayList<Integer>();
			for (Integer common : commonBS) {
				nIndexes.add(oIndexes.get(common));
			}
			indexesList.set(k, nIndexes);
		}
		List<ChangeSummary> oChangeSummary = cluster.getConChgSum();
		List<String> oChgSumStr = cluster.getChgSumStr();
		List<String> oAbsChgSumStr = cluster.getAbsChgSumStr();
		List<ChangeSummary> nChangeSummary = new ArrayList<ChangeSummary>();
		List<List<List<SimpleASTNode>>> nSimpleExprsLists = new ArrayList<List<List<SimpleASTNode>>>();
		List<List<List<SimpleASTNode>>> oSimpleExprsLists = cluster
				.getSimpleExprsLists();
		List<String> nChgSumStr = new ArrayList<String>(), nAbsChgSumStr = new ArrayList<String>();
		for (Integer common : commonBS) {
			nChangeSummary.add(oChangeSummary.get(common));
			nChgSumStr.add(oChgSumStr.get(common));
			nAbsChgSumStr.add(oAbsChgSumStr.get(common));
			nSimpleExprsLists.add(oSimpleExprsLists.get(common));
		}
		cluster.setConChgSum(nChangeSummary);
		cluster.setChgSumStr(nChgSumStr);
		cluster.setAbsChgSumStr(nAbsChgSumStr);
		cluster.setExprsLists(nSimpleExprsLists);
		return PARTIALLY_VALID_CLUSTER;

	}

	protected boolean checkCluster2(EditInCommonCluster cluster) {
		if (indexSubgroupMap.size() == 1)
			return true;
		List<Set<Set<Integer>>> indexSetList = new ArrayList<Set<Set<Integer>>>(
				indexSubgroupMap.keySet());
		List<Set<Set<Integer>>> rIndexSetList = new ArrayList<Set<Set<Integer>>>();

		rIndexSetList.add(indexSetList.get(0));
		Set<Set<Integer>> indexSet = null;
		// the more forests included, the earlier the indexSet is put
		for (int i = 1; i < indexSetList.size(); i++) {// reorder the indexSet
														// based on num of
														// forests contained
			int j = 0;
			indexSet = indexSetList.get(i);
			for (; j < rIndexSetList.size(); j++) {
				if (rIndexSetList.get(j).size() > indexSet.size()) {
					break;
				}
			}
			if (j == rIndexSetList.size()) {// indexSet.size() >= the size of
											// any rIndexSetList
				rIndexSetList.add(indexSet);
			} else {
				rIndexSetList.add(j, indexSet);
			}
		}
		indexSetList = rIndexSetList;
		Set<Set<Integer>> bss1 = indexSetList.get(0);

		Set<Set<Integer>> bss2 = null;
		Set<Integer> sectSet = null;
		Set<Integer> records = new HashSet<Integer>();
		List<Set<Integer>> subindexes = new ArrayList<Set<Integer>>();
		List<Set<Integer>> rSubindexes = new ArrayList<Set<Integer>>();
		Set<Integer> tmpBitSet = new HashSet<Integer>();
		Set<Integer> commonBitSet = new HashSet<Integer>();
		int numOfEdits = -1, numOfCommonEdits = -1, sumOfCommonEdits = 0;
		for (Set<Integer> bs : bss1) {// find the largest common grouped edits
			records.clear();
			subindexes.clear();
			for (int i = 1; i < indexSetList.size(); i++) {
				bss2 = indexSetList.get(i);
				commonBitSet.clear();
				numOfCommonEdits = 0;
				for (Set<Integer> bs2 : bss2) {
					sectSet = new HashSet<Integer>(bs2);
					sectSet.retainAll(bs);
					if (!sectSet.isEmpty()) {
						tmpBitSet.clear();
						tmpBitSet.addAll(sectSet);
						numOfEdits = tmpBitSet.size();
						if (numOfEdits > numOfCommonEdits) {
							commonBitSet.clear();
							commonBitSet.addAll(tmpBitSet);
							numOfCommonEdits = commonBitSet.size();
						}
					}
				}
				bs = new HashSet<Integer>();
				bs.addAll(commonBitSet);
			}
			if (numOfCommonEdits != 0) {
				rSubindexes.add(bs);
				sumOfCommonEdits += numOfCommonEdits;
			}
		}
		if (sumOfCommonEdits < CommonEditParser.THRESHOLD_FOR_NUMBER_OF_EDITS) {
			return false;
		}
		return true;
	}

	/**
	 * To count the number of edits included in the bitSets
	 * 
	 * @param bitSets
	 * @return
	 */
	private int countEdits(Set<Set<Integer>> bitSets) {
		int counter = 0;
		for (Set<Integer> bitSet : bitSets) {
			counter += bitSet.size();
		}
		return counter;
	}

	/**
	 * The forests created have not been ordered based on preorder traversal
	 * order
	 * 
	 * @param cluster
	 */
	public void createForests(EditInCommonCluster cluster) {
		List<List<SimpleTreeNode>> forests = null;
		List<SimpleTreeNode> forest = null, forest2 = null;
		// check whether the existing clusters conflict with structure-based
		// clustering
		List<Integer> tmpInsts = null;
		List<Integer> treeEdits = null;
		Set<Integer> processedIndexes = null;
		Map<Integer, Integer> tmpEditAndRoletype1 = null;
		Integer tmpInst = null;

		int tmpIndex = -1;

		Collection<Set<Integer>> subgroups = indexSubgroupMap.values();
		List<Map<Integer, Integer>> orderedEditAndRoletype = new ArrayList<Map<Integer, Integer>>();
		tmpInsts = cluster.getInstances();
		if (subgroups.iterator().next().containsAll(tmpInsts)) {
			forests = new ArrayList<List<SimpleTreeNode>>();
			tmpInst = tmpInsts.get(0);
			forest2 = indexAndTreesMap.get(tmpInst);
			if (forest2 == null) {// no forest can be formed up
				cluster.setApplicable(false);
				forests.clear();
			} else {
				forests.add(forest2);
				orderedEditAndRoletype.clear();
				for (SimpleTreeNode t : forest2) {
					orderedEditAndRoletype.add(t.getEditAndRoletype());
				}
				for (int i = 1; i < tmpInsts.size(); i++) {
					tmpInst = tmpInsts.get(i);
					forest = indexAndTreesMap.get(tmpInst);
					treeEdits = new ArrayList<Integer>();
					System.out.print("");
					processedIndexes = new HashSet<Integer>();
					for (int j = 0; j < forest.size(); j++) {
						tmpEditAndRoletype1 = forest.get(j)
								.getEditAndRoletype();
						tmpIndex = orderedEditAndRoletype
								.indexOf(tmpEditAndRoletype1);
						if (tmpIndex == -1) {
							cluster.setApplicable(false);
							forests.clear();
							treeEdits.clear();
							break;
						}
						processedIndexes.add(tmpIndex);
						treeEdits.add(tmpIndex);
					}
					forest2 = new ArrayList<SimpleTreeNode>();
					for (int j = 0; j < forest.size(); j++) {
						forest2.add(null);
					}
					for (int j = 0; j < treeEdits.size(); j++) {
						forest2.set(treeEdits.get(j), forest.get(j));
					}
					if (DEBUG) {
						for (int j = 0; j < forest.size(); j++) {
							if (forest2.get(j) == null) {
								System.out
										.println("The tree was not mapped correctly");
							}
						}
					}
					forests.add(forest2);
				}
			}
			cluster.setForests(forests);
		}
	}

	/**
	 * create trees without considering the inserted context which is inserted
	 * by edits not included
	 * 
	 * @param cluster
	 */
	public List<Integer> createTrees(EditInCommonCluster cluster) {
		List<Integer> tmpInsts = cluster.getInstances();
		Integer tmpInst = null;

		List<List<Integer>> indexesList = cluster.getIndexesList();
		List<Integer> indexes = null;

		Map<Set<Integer>, SimpleTreeNode> tmpMap = null;
		List<SimpleTreeNode> sTreeList = cluster.getSTrees();

		Set<Set<Integer>> indexGroups = null;
		List<SimpleTreeNode> trees = null;

		SimpleTreeNode sTree = null;
		List<AbstractTreeEditOperation> edits = null;

		indexAndTreesMap.clear();
		indexSubgroupMap.clear();

		for (int i = 0; i < tmpInsts.size(); i++) {
			tmpInst = tmpInsts.get(i);
			indexes = indexesList.get(i);
			edits = group.getMMList().get(tmpInst).getEdits();
			sTree = new SimpleTreeNode(sTreeList.get(i));

			updatedNode = edits.get(edits.size() - 2).getNode();

			newNode = edits.get(edits.size() - 1).getNode();

			for (Integer index : indexes) {
				labelTree(edits.get(index), sTree, indexes.indexOf(index),
						indexes, edits);
			}
			tmpMap = trim(sTree);
			trees = new ArrayList<SimpleTreeNode>(tmpMap.values());
			indexAndTreesMap.put(tmpInst, trees);
			indexGroups = tmpMap.keySet();
			if (!indexSubgroupMap.containsKey(indexGroups)) {
				Set<Integer> subgroup = new HashSet<Integer>();
				indexSubgroupMap.put(indexGroups, subgroup);
			}
			indexSubgroupMap.get(indexGroups).add(tmpInst);
		}
		Set<Integer> includedEditIndexes = new HashSet<Integer>();
		List<Integer> redundantIndexes = new ArrayList<Integer>();
		for (Set<Integer> tmpIndexes : tmpMap.keySet()) {
			includedEditIndexes.addAll(tmpIndexes);
		}
		for (int i = 0; i < indexesList.get(0).size(); i++) {
			if (!includedEditIndexes.contains(i)) {
				redundantIndexes.add(i);
			}
		}
		return redundantIndexes;
	}

	/**
	 * To create relevant forests for edits parsed out from each instance
	 * contained in the cluster WHEN NECESSARY
	 * 
	 * @param cluster
	 * @return whether the known edit set should be extended based on whether
	 *         there is a tree rooting at some inserted node
	 */
	private boolean createTrees2(EditInCommonCluster cluster) {
		// System.out.print("");
		boolean mayExtend = false;
		List<Integer> tmpInsts = cluster.getInstances();
		Integer tmpInst = null;

		List<List<Integer>> indexesList = cluster.getIndexesList();
		List<Integer> indexes = null;

		// map between concerned edit indexes and corresponding subtrees
		Map<Set<Integer>, SimpleTreeNode> tmpMap = null;
		// data structures to save for later use
		List<SimpleTreeNode> sTreeList = cluster.getSTrees();
		List<SimpleTreeNode> sTreeList2 = cluster.getSTrees2();

		Set<Set<Integer>> indexGroups = null;
		List<SimpleTreeNode> trees = null;
		// partition instances based on edit groups
		SimpleTreeNode sTree = null, sTree2 = null;
		List<AbstractTreeEditOperation> edits = null;

		indexAndTreesMap.clear();
		indexSubgroupMap.clear();

		for (int i = 0; i < tmpInsts.size(); i++) {
			tmpInst = tmpInsts.get(i);
			indexes = indexesList.get(i);
			edits = group.getMMList().get(tmpInst).getEdits();
			sTree = new SimpleTreeNode(sTreeList.get(i));

			updatedNode = edits.get(edits.size() - 2).getNode();

			newNode = edits.get(edits.size() - 1).getNode();
			sTree2 = new SimpleTreeNode(sTreeList2.get(i));

			for (Integer index : indexes) {
				labelTree2(edits.get(index), sTree, sTree2,
						indexes.indexOf(index), indexes, edits);
			}
			tmpMap = trim(sTree);
			int editCount = countEdits(tmpMap.keySet());
			if (editCount < indexes.size()) {
				tmpMap.putAll(trim(sTree2));
				mayExtend = true;
			}
			trees = new ArrayList<SimpleTreeNode>(tmpMap.values());
			indexAndTreesMap.put(tmpInst, trees);
			indexGroups = tmpMap.keySet();
			if (!indexSubgroupMap.containsKey(indexGroups)) {
				Set<Integer> subgroup = new HashSet<Integer>();
				indexSubgroupMap.put(indexGroups, subgroup);
			}
			indexSubgroupMap.get(indexGroups).add(tmpInst);
		}
		return mayExtend;
	}

	private boolean extendIndexes(List<List<Integer>> indexesList) {
		boolean isExtended = false;
		Integer tmpInst = null;
		Integer anchorEditIndex = null;
		int indexToExtend = -1;
		List<SimpleTreeNode> trees = null;
		List<SimpleTreeNode> treesToExtend = null;
		SimpleTreeNode sTree = null;
		SimpleTreeNode sTreeParent = null;
		Set<Integer> processed = new HashSet<Integer>();
		Set<Integer> tmpSet = null;
		String knownStr = null;
		List<Integer> tmpInstList = null;
		List<Integer> tmpIndexes = null;
		List<MethodModification> mmList = group.getMMList();
		List<AbstractTreeEditOperation> edits = null;
		AbstractTreeEditOperation edit = null;
		Node tmpNode = null;

		boolean allSame = false;
		for (Entry<Integer, List<SimpleTreeNode>> entry : indexAndTreesMap
				.entrySet()) {
			tmpInst = entry.getKey();
			if (processed.contains(tmpInst))
				continue;
			trees = entry.getValue();
			// find the tree whose edits may be extended
			for (int i = 0; i < trees.size(); i++) {
				sTree = trees.get(i);
				if (sTree.getTypes().contains(
						SimpleTreeNode.INSERTED_CONTEXTUAL)) {
					// find the inst groups in which all insts have
					// corresponding to the known tree
					for (Entry<Set<Set<Integer>>, Set<Integer>> entry2 : indexSubgroupMap
							.entrySet()) {
						tmpSet = entry2.getValue();
						tmpInstList = new ArrayList<Integer>();
						if (tmpSet.contains(tmpInst)) {
							treesToExtend = new ArrayList<SimpleTreeNode>();
							for (Integer tmp : tmpSet) {
								treesToExtend.add(indexAndTreesMap.get(tmp)
										.get(i));
								tmpInstList.add(tmp);
								processed.add(tmp);
							}
							if (treesToExtend.size() < 2) {
								continue;
							}
							allSame = true;
							knownStr = treesToExtend.get(0).getStrValue();
							for (int j = 1; j < treesToExtend.size(); j++) {
								if (!knownStr.equals(treesToExtend.get(j)
										.getStrValue())) {
									allSame = false;
								}
							}
							if (allSame) {
								isExtended = true;
								for (int j = 0; j < treesToExtend.size(); j++) {
									sTreeParent = treesToExtend.get(j);
									anchorEditIndex = Collections
											.min(sTreeParent.getEditIndexes());
									tmpIndexes = indexesList.get(j);
									anchorEditIndex = tmpIndexes
											.get(anchorEditIndex);
									edits = mmList.get(tmpInstList.get(j))
											.getEdits();
									for (int k = 0; k < anchorEditIndex; k++) {
										if (tmpIndexes.contains(k))
											continue;
										edit = edits.get(k);
										if (edit.getOperationType().equals(
												EDIT.INSERT)) {
											tmpNode = edit.getNode();
											if (tmpNode
													.getSourceCodeRange()
													.equals(sTreeParent
															.getSourceCodeRange())
													&& tmpNode
															.getStrValue()
															.equals(sTreeParent
																	.getStrValue())) {
												indexToExtend = k;
												break;
											}
										}
									}
									tmpIndexes
											.add(tmpIndexes
													.indexOf(anchorEditIndex),
													indexToExtend);
								}
							}
							break;
						}
					}
				}
			}
		}
		return isExtended;
	}

	/**
	 * To initiate fields sTreeList, sTreeList2... of the current class using
	 * information of all instances involved in the current cluster
	 * 
	 * @param cluster
	 */
	public void initTrees(EditInCommonCluster cluster) {
		sTreeList = new ArrayList<SimpleTreeNode>();
		sTreeList2 = new ArrayList<SimpleTreeNode>();
		updatedNodeList = new ArrayList<Node>();
		newNodeList = new ArrayList<Node>();
		List<Integer> tmpInsts = cluster.getInstances();
		insts = new ArrayList<Integer>(tmpInsts);
		Integer tmpInst = null;
		// partition instances based on edit groups
		SimpleTreeNode sTree = null, sTree2 = null;
		List<AbstractTreeEditOperation> edits = null;
		for (int i = 0; i < tmpInsts.size(); i++) {
			tmpInst = tmpInsts.get(i);
			edits = group.getMMList().get(tmpInst).getEdits();
			sTree = new SimpleTreeNode((Node) edits.get(0).getParentNode()
					.getRoot(), true, 1);
			newNode = edits.get(edits.size() - 1).getNode();
			updatedNode = edits.get(edits.size() - 2).getNode();
			sTree2 = new SimpleTreeNode(newNode, true, 1);
			sTreeList.add(sTree);
			sTreeList2.add(sTree2);
			updatedNodeList.add(updatedNode);
			newNodeList.add(newNode);
		}
	}

	private boolean labelDescendant(SimpleTreeNode sTree,
			List<Integer> indexes, List<Integer> parentEditIndexes, int j) {
		Enumeration<SimpleTreeNode> enumeration = sTree
				.breadthFirstEnumeration();
		Set<Integer> parentSet = new HashSet<Integer>();
		Set<Integer> tmpSet = null;
		for (Integer index : parentEditIndexes) {
			parentSet.add(indexes.indexOf(index));
		}
		SimpleTreeNode sNode = null;
		while (enumeration.hasMoreElements()) {
			sNode = enumeration.nextElement();
			tmpSet = new HashSet<Integer>(sNode.getEditIndexes());
			tmpSet.retainAll(parentSet);
			if (!tmpSet.isEmpty()) {
				sNode.addEntryToEditAndRoletype(j, SimpleTreeNode.CONTEXTUAL);
				return true;
			}
		}
		return false;
	}

	private void labelTree(AbstractTreeEditOperation op, SimpleTreeNode sTree,
			int j, List<Integer> indexes, List<AbstractTreeEditOperation> edits) {
		SimpleTreeNode sNode = null;
		Node pnode = null;
		switch (op.getOperationType()) {
		case INSERT:
			pnode = op.getParentNode();
			sNode = sTree.lookforNodeBasedOnRange(pnode);
			if (sNode != null) {
				sNode.addEntryToEditAndRoletype(j, SimpleTreeNode.CONTEXTUAL);
				labelIfParent(sNode, j);	
			} else {
				List<Integer> knownParentEditIndexes = new ArrayList<Integer>(
						pnode.getEditIndexes());
				knownParentEditIndexes.retainAll(indexes);
				if (!knownParentEditIndexes.isEmpty()) {// the edit is within
					// the knownParentEditIndexes
					// first try to label the old tree; if fail, then try to
					// label the new tree
					labelDescendant(sTree, indexes, knownParentEditIndexes, j);
				}
			}
			break;
		case DELETE:
		case UPDATE:
			sNode = sTree.lookforNodeBasedOnRange(op.getNode());
			if (sNode != null) {
				sNode.addEntryToEditAndRoletype(j, SimpleTreeNode.EDITED);
			}
			break;
		case MOVE:
			sNode = sTree.lookforNodeBasedOnRange(op.getNode());
			if (sNode == null) {
				Node node = updatedNode.lookforNodeBasedOnRange(op.getNode());
				if (node != null) {
					List<Integer> knownEditIndexes = node.getEditIndexes();
					for (Integer knownEditIndex : knownEditIndexes) {
						if (knownEditIndex < j
								&& edits.get(knownEditIndex).getOperationType()
										.equals(EDIT.UPDATE)) {
							sNode = sTree.lookforNodeBasedOnRange(edits.get(
									knownEditIndex).getNode());
							break;
						}
					}
				}
			}
			if (sNode != null) {
				Node node = updatedNode
						.lookforNodeBasedOnRange(((MoveOperation) op)
								.getNewParentNode());
				SimpleTreeNode sParent = sTree.lookforNodeBasedOnRange(node);
				if (sParent != null) {
					sParent.addEntryToEditAndRoletype(j,
							SimpleTreeNode.CONTEXTUAL);
					sNode.addEntryToEditAndRoletype(j, SimpleTreeNode.EDITED);
				} else {// this node has been updated or inserted
					List<Integer> knownParentEditIndexes = new ArrayList<Integer>(
							node.getEditIndexes());
					knownParentEditIndexes.retainAll(indexes);
					if (!knownParentEditIndexes.isEmpty()) {
						if (labelDescendant(sTree, indexes,
								knownParentEditIndexes, j)) {
							sNode.addEntryToEditAndRoletype(j,
									SimpleTreeNode.EDITED);
						}
					}
				}
			}
		}
	}
	
	private void labelIfParent(SimpleTreeNode sNode, int j){
		if(sNode.getStrValue().equals("then:") || sNode.getStrValue().equals("else:")){
			sNode = (SimpleTreeNode)sNode.getParent();
			sNode.addEntryToEditAndRoletype(j, SimpleTreeNode.CONTEXTUAL);
		}
	}

	/**
	 * 
	 * @param op
	 * @param sTree
	 *            the tree labeled with edit indexes
	 * @param sTree2
	 *            the tree without any labeling
	 * @param j
	 * @param indexes
	 * @return
	 */
	private boolean labelTree2(AbstractTreeEditOperation op,
			SimpleTreeNode sTree, SimpleTreeNode sTree2, int j,
			List<Integer> indexes, List<AbstractTreeEditOperation> edits) {
		SimpleTreeNode sNode = null;
		Node pnode = null;
		System.out.print("");
		switch (op.getOperationType()) {
		case INSERT:
			pnode = op.getParentNode();
			sNode = sTree.lookforNodeBasedOnRange(pnode);
			if (sNode != null) {
				sNode.addEntryToEditAndRoletype(j, SimpleTreeNode.CONTEXTUAL);
				labelIfParent(sNode, j);				
			} else {
				// this is a newly inserted node which does not exist in the old
				// version
				List<Integer> knownParentEditIndexes = new ArrayList<Integer>(
						pnode.getEditIndexes());
				knownParentEditIndexes.retainAll(indexes);
				if (!knownParentEditIndexes.isEmpty()) {// the edit is within
														// the
														// knownParentEditIndexes
					// first try to label the old tree; if fail, then try to
					// label the new tree
					if (!labelDescendant(sTree, indexes,
							knownParentEditIndexes, j))
						labelDescendant(sTree2, indexes,
								knownParentEditIndexes, j);
				} else {
					sNode = sTree2.lookforNodeBasedOnRange(pnode);
					if (sNode != null) {// the parent is inserted, although it
										// is not included as common edit
						sNode.addEntryToEditAndRoletype(j,
								SimpleTreeNode.INSERTED_CONTEXTUAL);
					}
				}
			}
			break;
		case DELETE:
		case UPDATE:
		case MOVE:
			sNode = sTree.lookforNodeBasedOnRange(op.getNode());
			if (sNode != null) {
				sNode.addEntryToEditAndRoletype(j, SimpleTreeNode.EDITED);
			}
			if (op.getOperationType().equals(EDIT.MOVE)) {
				Node node = updatedNode
						.lookforNodeBasedOnRange(((MoveOperation) op)
								.getNewParentNode());
				sNode = sTree.lookforNodeBasedOnRange(node);
				if (sNode != null) {
					sNode.addEntryToEditAndRoletype(j,
							SimpleTreeNode.CONTEXTUAL);
				} else {// this node has been updated or inserted
					List<Integer> knownParentEditIndexes = new ArrayList<Integer>(
							node.getEditIndexes());
					knownParentEditIndexes.retainAll(indexes);
					if (!knownParentEditIndexes.isEmpty()) {
						labelDescendant(sTree2, indexes,
								knownParentEditIndexes, j);
					} else {
						sNode = sTree2.lookforNodeBasedOnRange(node);
						if (sNode != null)
							sNode.addEntryToEditAndRoletype(j,
									SimpleTreeNode.INSERTED_CONTEXTUAL);
						else {// this node has been updated by some edit
								// which is not common
							AbstractTreeEditOperation tmpOp = null;
							List<Integer> relevantEditIndexes = node
									.getEditIndexes();
							for (Integer index : relevantEditIndexes) {
								tmpOp = edits.get(index);
								if (tmpOp.getOperationType().equals(
										ITreeEditOperation.EDIT.UPDATE)) {
									sNode = sTree.lookforNodeBasedOnRange(tmpOp
											.getNode());
									if (sNode != null) {
										sNode.addEntryToEditAndRoletype(j,
												SimpleTreeNode.CONTEXTUAL);
									} else {
										System.out
												.println("More process is needed");
									}
								} else if (tmpOp.getOperationType().equals(
										ITreeEditOperation.EDIT.MOVE)) {
									sNode = sTree.lookforNodeBasedOnRange(tmpOp
											.getNode());
									if (sNode != null) {
										sNode.addEntryToEditAndRoletype(j,
												SimpleTreeNode.CONTEXTUAL);
									}
								}
							}
						}
					}
				}
			}
			break;
		}
		return true;
	}

	/**
	 * To get the smallest amount of context--including parent of insertion
	 * location, and that of a move target The traversal order of this process
	 * should be bottom up, from the first level of EditInCommonClusters
	 * (clusters composed of BaseClusters)
	 */
	public EditInCommonCluster parseMinContext() {
		initTrees(cluster);
		addTrees(cluster);
		// map between instance and the above concerned subtrees
		indexAndTreesMap = new HashMap<Integer, List<SimpleTreeNode>>();
		// map between edit groups and instances--this will help us check
		// whether the clusters are changeable
		indexSubgroupMap = new HashMap<Set<Set<Integer>>, Set<Integer>>();
		List<Integer> redundantIndexes = null;
		ForestRefiner forestRefiner = new ForestRefiner();
		// System.out.print("");
		boolean mayExtend = createTrees2(cluster);
		if (checkCluster2(cluster)) {
			if (mayExtend) {// try to extend common edit indexes when possible
				extendIndexes(cluster.getIndexesList());
				initTrees(cluster);
			}
		} else {
			return null;
		}
		indexAndTreesMap.clear();
		indexSubgroupMap.clear();
		redundantIndexes = createTrees(cluster);
		int status = checkCluster(cluster);
		if (status == PARTIALLY_VALID_CLUSTER)
			// try to remove some indexes if edit structures are different
			redundantIndexes = createTrees(cluster);
		// recreate the trees
		// based on new parsed edits overlapped
		if (status != NOT_VALID) {
			createForests(cluster);
			System.out.print("");
			// try to remove some indexes if edit structures have different
			// shape/conflicting identifier mapping
			try {
				redundantIndexes.addAll(forestRefiner.refineForests(group,
						cluster));
				if (!redundantIndexes.isEmpty())
					updateEditIndexes(redundantIndexes, cluster);
				if(cluster.getConChgSum().size() == 0){
					cluster.setApplicable(false);
				}
				// high_cluster is refined based on traverse order
				// among trees
			} catch (MappingException e) {
				// TODO Auto-generated catch block
				cluster.setApplicable(false);
			}
			// the redundantIndexes are described with respect to
			// the last inst's indexes
		}
		if (cluster.getApplicable())
			return cluster;
		return null;
	}

	private Map<Set<Integer>, SimpleTreeNode> trim(SimpleTreeNode sTree) {
		Map<Set<Integer>, SimpleTreeNode> map = new HashMap<Set<Integer>, SimpleTreeNode>();
		System.out.print("");
		List<SimpleTreeNode> list = new ArrayList<SimpleTreeNode>();
		SimpleTreeNode tmp = null;
		SimpleTreeNode tmpParent = null;
		Queue<SimpleTreeNode> queue = new LinkedList<SimpleTreeNode>();
		Enumeration<SimpleTreeNode> cEnum = null;
		Enumeration<SimpleTreeNode> dEnum = null;
		queue.add(sTree);
		while (!queue.isEmpty()) {
			tmp = queue.remove();
			tmpParent = (SimpleTreeNode) tmp.getParent();
			cEnum = tmp.children();
			while (cEnum.hasMoreElements()) {
				queue.add(cEnum.nextElement());
			}
			if (!tmp.getTypes().isEmpty()) {// the current
											// node is
											// interesting
				if (tmpParent == null) {
					list.add(tmp);
				} else if (!tmpParent.getTypes().isEmpty()) {
					// do nothing
				} else {// tmpParent is not an edited on contexual node
					tmp.removeFromParent();
					list.add(tmp);
				}
			} else {
				if (tmpParent == null) {// the current node is not interesting
					// do nothing
				} else if (!tmpParent.getTypes().isEmpty()) {
					tmp.removeFromParent();// remove it from the interesting
											// parent
				} else {
					// do nothing
				}
			}
		}
		Set<Integer> set = null;
		for (SimpleTreeNode n : list) {
			set = new HashSet<Integer>();
			dEnum = n.depthFirstEnumeration();
			while (dEnum.hasMoreElements()) {
				set.addAll(dEnum.nextElement().getEditIndexes());
			}
			map.put(set, n);
		}
		return map;
	}

	private void updateEditIndexes(List<Integer> redundantIndexes,
			EditInCommonCluster cluster) throws MappingException {
		List<List<Integer>> originalIndexesList = cluster.getIndexesList();
		List<String> originalAbsChgSumStrs = cluster.getAbsChgSumStr();
		List<String> originalChgSumStrs = cluster.getChgSumStr();
		List<List<List<SimpleASTNode>>> originalSimpleExprsLists = cluster
				.getSimpleExprsLists();
		BaseCluster bClus1 = (BaseCluster) cluster.getIncomings().get(0);
		BaseCluster bClus2 = (BaseCluster) cluster.getIncomings().get(1);
		List<List<List<SimpleASTNode>>> oldSimpleExprsLists1 = bClus1
				.getSimpleExprsLists();
		List<List<List<SimpleASTNode>>> oldSimpleExprsLists2 = bClus2
				.getSimpleExprsLists();
		List<List<Integer>> newIndexesList = new ArrayList<List<Integer>>();
		for (int i = 0; i < originalIndexesList.size(); i++) {
			newIndexesList.add(new ArrayList<Integer>());
		}
		List<ChangeSummary> newConChgSums = new ArrayList<ChangeSummary>();
		List<String> newAbsChgSumStrs = new ArrayList<String>();
		List<String> newChgSumStrs = new ArrayList<String>();
		List<List<List<SimpleASTNode>>> newSimpleExprsLists = new ArrayList<List<List<SimpleASTNode>>>();
		List<List<SimpleASTNode>> simpleExprsList1 = null;
		List<List<SimpleASTNode>> simpleExprsList2 = null;
		List<List<SimpleASTNode>> commonExprsList = null;
		List<ChangeSummary> chgSums1 = bClus1.getConChgSum();
		ChangeSummary chgSum = null;
		ChangeSummaryCreator csCreator = new ChangeSummaryCreator();
		CommonEditParser parser = new CommonEditParser();
		Integer index1 = null, index2 = null;
		for (int i = 0; i < originalIndexesList.get(0).size(); i++) {
			if (!redundantIndexes.contains(i)) {
				index1 = originalIndexesList.get(0).get(i);
				index2 = originalIndexesList.get(1).get(i);
				newIndexesList.get(0).add(index1);
				newIndexesList.get(1).add(index2);
				simpleExprsList1 = oldSimpleExprsLists1.get(index1);
				simpleExprsList2 = oldSimpleExprsLists2.get(index2);
				commonExprsList = parser.getCommon(simpleExprsList1,
						simpleExprsList2);
				chgSum = csCreator.updateSummary(commonExprsList,
						chgSums1.get(index1));
				newConChgSums.add(chgSum);
				newAbsChgSumStrs.add(originalAbsChgSumStrs.get(i));
				newChgSumStrs.add(originalChgSumStrs.get(i));
				newSimpleExprsLists.add(commonExprsList);
			}
		}
		// parse identifier maps based on
		List<SimpleTreeNode> forest1 = cluster.getForests().get(0);
		List<SimpleTreeNode> forest2 = cluster.getForests().get(1);
		List<List<SimpleASTNode>> sNodesList1 = bClus1.getSimpleASTNodesList();
		List<List<SimpleASTNode>> sNodesList2 = bClus2.getSimpleASTNodesList();
		List<SimpleASTNode> sNodes1 = null;
		List<SimpleASTNode> sNodes2 = null;
		SimpleTreeNode tmpTree = null;
		SimpleTreeNode sTmp1 = null;
		SimpleTreeNode sTmp2 = null;
		Enumeration<SimpleTreeNode> sEnum1 = null;
		Enumeration<SimpleTreeNode> sEnum2 = null;
		for (int i = 0; i < forest1.size(); i++) {
			tmpTree = forest1.get(i);
			sEnum1 = tmpTree.breadthFirstEnumeration();
			sEnum2 = forest2.get(i).breadthFirstEnumeration();
			while (sEnum1.hasMoreElements()) {
				sTmp1 = sEnum1.nextElement();
				sTmp2 = sEnum2.nextElement();
				if (sTmp1.getEditAndRoletype().values()
						.contains(SimpleTreeNode.EDITED)) {
					continue;
				} else {
					sNodes1 = sNodesList1.get(sTmp1.getNodeIndex() - 1);
					sNodes2 = sNodesList2.get(sTmp2.getNodeIndex() - 1);
					if (sNodes1.size() != sNodes2.size()) {
						continue;
					} else {
						for (int j = 0; j < sNodes1.size(); j++) {
							parser.getCommon(sNodes1.get(j), sNodes2.get(j));
						}
					}
				}
			}
		}
		cluster.setIndexesList(newIndexesList);
		cluster.setChgSumStr(newChgSumStrs);
		cluster.setAbsChgSumStr(newAbsChgSumStrs);
		cluster.setConChgSum(newConChgSums);
		cluster.setExprsLists(newSimpleExprsLists);
		cluster.getSpecificToUnifiedList().add(parser.getLtoU());
		List<Map<String, String>> specificToUnifiedList = cluster
				.getSpecificToUnifiedList();
		specificToUnifiedList.clear();
		specificToUnifiedList.add(parser.getLtoU());
		specificToUnifiedList.add(parser.getRtoU());
		List<Map<String, String>> unifiedToSpecificList = cluster
				.getUnifiedToSpecificList();
		Map<String, String> specificToUnified = null;
		Map<String, String> unifiedToSpecific = null;
		unifiedToSpecificList.clear();
		for (int i = 0; i < specificToUnifiedList.size(); i++) {
			specificToUnified = specificToUnifiedList.get(i);
			unifiedToSpecific = IdMapper.createReverseMap(specificToUnified);
			unifiedToSpecificList.add(unifiedToSpecific);
		}
	}
}
