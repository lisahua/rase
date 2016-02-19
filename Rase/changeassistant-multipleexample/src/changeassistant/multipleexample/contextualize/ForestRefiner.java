package changeassistant.multipleexample.contextualize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.TypeNameTerm;
import changeassistant.multipleexample.contextualize.datastructure.Sequence;
import changeassistant.multipleexample.partition.MappingException;
import changeassistant.multipleexample.partition.SimpleASTNodeConverter;
import changeassistant.multipleexample.partition.datastructure.BaseCluster;
import changeassistant.multipleexample.partition.datastructure.EditInCommonCluster;
import changeassistant.multipleexample.partition.datastructure.EditInCommonGroup;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.multipleexample.util.LCSSequence;

public class ForestRefiner extends LCSSequence {

	private Set<Integer> checkMisorderTrees(EditInCommonCluster cluster) {
		List<List<SimpleTreeNode>> forests = cluster.getForests();
		if (forests == null)
			return null;
		Set<Integer> tmpEditIndexesToRemove = new HashSet<Integer>();
		List<SimpleTreeNode> sTreeList = cluster.getSTrees();
		List<Sequence> traverseSequences = new ArrayList<Sequence>();

		for (int i = 0; i < forests.size(); i++) {
			traverseSequences.add(refineTraverseSequence(forests.get(i),
					sTreeList.get(i)));
		}
		Sequence seq0 = traverseSequences.get(0);
		Sequence result = new Sequence(new ArrayList<Integer>(
				seq0.getNodeIndexes()));
		Sequence newResult = new Sequence(new ArrayList<Integer>());
		for (int i = 1; i < traverseSequences.size(); i++) {
			dictionary2 = new HashMap<String, Sequence>();
			computeLCSSequence(result, traverseSequences.get(i), newResult);
			result = newResult;
			newResult = new Sequence(new ArrayList<Integer>());
		}

		if (result.size() < seq0.size()) {
			// there is redundant trees in the forests
			// 1. find all redundant trees
			List<Integer> rIndexes = result.getNodeIndexes();
			List<Integer> sIndexes = seq0.getNodeIndexes();
			Set<Integer> redundantTrees = new HashSet<Integer>();
			for (Integer index : sIndexes) {
				if (!rIndexes.contains(index) && index >= 0) {
					redundantTrees.add(index);
				}
			}
			Set<SimpleTreeNode> redundantNodes = new HashSet<SimpleTreeNode>();
			// 2. remove all edit indexes relevant to these trees
			tmpEditIndexesToRemove.addAll(removeRedundancy(redundantTrees,
					redundantNodes, cluster));
			reorderForests(result, cluster, redundantTrees, redundantNodes);
		}
		return tmpEditIndexesToRemove;
	}

	/**
	 * Assume there are only two instances in the cluster
	 * 
	 * @param cluster
	 * @throws MappingException
	 */
	private void checkConflictMapTrees(Set<Integer> tmpEditIndexesToRemove,
			EditInCommonGroup group, EditInCommonCluster cluster)
			throws MappingException {
		Set<Integer> redundantTrees = new HashSet<Integer>();
		Set<SimpleTreeNode> redundantNodes = new HashSet<SimpleTreeNode>();
		List<List<SimpleTreeNode>> forests = cluster.getForests();
		List<SimpleTreeNode> forest0 = forests.get(0);
		List<SimpleTreeNode> forest1 = forests.get(1);
		BaseCluster bClus1 = (BaseCluster) cluster.getIncomings().get(0);
		BaseCluster bClus2 = ((BaseCluster) cluster.getIncomings().get(1));
		List<List<SimpleASTNode>> simpleASTNodesList1 = bClus1
				.getSimpleASTNodesList();
		List<List<SimpleASTNode>> simpleASTNodesList2 = bClus2
				.getSimpleASTNodesList();
		List<SimpleASTNode> sNodes1 = null;
		List<SimpleASTNode> sNodes2 = null;
		List<List<Term>> termsList1 = null;
		List<List<Term>> termsList2 = null;
		Map<String, Set<TypeNameTerm>> typeTermMap1 = bClus1.getTypeTermMap();
		Map<String, Set<TypeNameTerm>> typeTermMap2 = bClus2.getTypeTermMap();
		Map<String, String> specificToUnified1 = new HashMap<String, String>(
				cluster.getSpecificToUnifiedList().get(0));
		Map<String, String> specificToUnified2 = new HashMap<String, String>(
				cluster.getSpecificToUnifiedList().get(1));
		Map<String, String> unifiedToSpecific = new HashMap<String, String>(
				cluster.getUnifiedToSpecificList().get(1));
		Map<String, String> basicMap = new HashMap<String, String>();
		Map<String, String> basicMap2 = new HashMap<String, String>();
		for (Entry<String, String> entry : specificToUnified1.entrySet()) {
			basicMap.put(entry.getKey(),
					unifiedToSpecific.get(entry.getValue()));
			basicMap2.put(unifiedToSpecific.get(entry.getValue()),
					entry.getKey());
		}
		List<Term> leftTerms = new ArrayList<Term>();
		List<Term> rightTerms = new ArrayList<Term>();
		List<Set<Integer>> supportingInsts = new ArrayList<Set<Integer>>();
		Map<String, Set<String>> blackIdentifierMap = new HashMap<String, Set<String>>();
		Map<String, Set<String>> blackIdentifierMap2 = new HashMap<String, Set<String>>();
		SimpleTreeNode tmpTree = null, tmpTree2 = null;
		SimpleTreeNode sTmp1 = null, sTmp2 = null;
		Enumeration<SimpleTreeNode> sEnum1 = null, sEnum2 = null;
		 System.out.print("");		
		// 4. find mapping conflict trees and remove those if any
		for (int i = 0; i < forest0.size(); i++) {
			tmpTree = forest0.get(i);
			tmpTree2 = forest1.get(i);
			if(tmpTree.countNodes() != tmpTree2.countNodes())
				continue;
			sEnum1 = tmpTree.breadthFirstEnumeration();
			sEnum2 = tmpTree2.breadthFirstEnumeration();
			while (sEnum1.hasMoreElements()) {
				sTmp1 = sEnum1.nextElement();
				sTmp2 = sEnum2.nextElement();				
				if (sTmp1.getEditAndRoletype().values()
						.contains(SimpleTreeNode.EDITED)) {
					continue;// the edited nodes have already been
								// processed
				} else {
					sNodes1 = simpleASTNodesList1.get(sTmp1.getNodeIndex() - 1);
					sNodes2 = simpleASTNodesList2.get(sTmp2.getNodeIndex() - 1);
					termsList1 = SimpleASTNodeConverter
							.convertToTermsList(sNodes1);
					termsList2 = SimpleASTNodeConverter
							.convertToTermsList(sNodes2);
					if (termsList1.isEmpty() && termsList2.isEmpty()) {
						Set<Integer> tmpSet = new HashSet<Integer>();
						tmpSet.add(sTmp1.getNodeIndex());
						supportingInsts.add(tmpSet);
					}
					if (BlackMapChecker.isBlackMap(termsList1, termsList2,
							sNodes1, sNodes2, specificToUnified1,
							unifiedToSpecific, specificToUnified2, basicMap,
							basicMap2, typeTermMap1, typeTermMap2, leftTerms,
							rightTerms, sTmp1.getNodeIndex(), supportingInsts,
							blackIdentifierMap, blackIdentifierMap2)) {
						if (sTmp1.isRoot()) {
							redundantTrees.add(i);
							break;
						}
						redundantNodes.add(sTmp1);
					}
				}
			}
		}
		BlackMapChecker.filterConflict(leftTerms, rightTerms, supportingInsts);
		Set<Integer> validIndexes = new HashSet<Integer>();
		for (Set<Integer> insts : supportingInsts) {
			validIndexes.addAll(insts);
		}

		for (int i = 0; i < forest0.size(); i++) {
			if (redundantTrees.contains(i))
				continue;
			tmpTree = forest0.get(i);
			sEnum1 = tmpTree.breadthFirstEnumeration();
			while (sEnum1.hasMoreElements()) {
				sTmp1 = sEnum1.nextElement();
				if (sTmp1.getEditAndRoletype().values()
						.contains(SimpleTreeNode.EDITED)
						|| validIndexes.contains(sTmp1.getNodeIndex())) {
					continue;
				} else {
					if (sTmp1.isRoot()) {
						redundantTrees.add(i);
					} else {
						redundantNodes.add(sTmp1);
					}
					break;
				}
			}
		}
		// 2. remove all edit indexes relevant to these trees
		if (!redundantTrees.isEmpty() || !redundantNodes.isEmpty())
			tmpEditIndexesToRemove.addAll(removeRedundancy(redundantTrees,
					redundantNodes, cluster));
		if (redundantNodes.isEmpty()) {
			if (!redundantTrees.isEmpty())
				reduceForests(cluster, redundantTrees);
		} else {
			ContextualizeHelper1 ch1 = new ContextualizeHelper1(group, cluster);
			ch1.createTrees(cluster);
			ch1.createForests(cluster);
		}
	}

	private void reduceForests(EditInCommonCluster cluster,
			Set<Integer> redundantTrees) {
		List<List<SimpleTreeNode>> forests = cluster.getForests();
		List<SimpleTreeNode> oldForest = null;
		List<SimpleTreeNode> newForest = null;
		for (int i = 0; i < forests.size(); i++) {
			oldForest = forests.get(i);
			newForest = new ArrayList<SimpleTreeNode>();
			for (int j = 0; j < oldForest.size(); j++) {
				if (redundantTrees.contains(j))
					continue;
				newForest.add(oldForest.get(j));
			}
			forests.set(i, newForest);
		}
	}

	/**
	 * cluster is a level_1 cluster containing only two baseClusters
	 * 
	 * @param cluster
	 * @return
	 * @throws MappingException
	 */
	public List<Integer> refineForests(EditInCommonGroup group,
			EditInCommonCluster cluster) throws MappingException {
		Set<Integer> tmpEditIndexesToRemove = checkMisorderTrees(cluster);
		if (tmpEditIndexesToRemove == null)
			return Collections.emptyList();
		if (cluster.getIncomings().get(0) instanceof BaseCluster) {
			checkConflictMapTrees(tmpEditIndexesToRemove, group, cluster);
		}
		cluster.enableForestOrder();
		return new ArrayList<Integer>(tmpEditIndexesToRemove);
	}

	public Sequence refineTraverseSequence(List<SimpleTreeNode> forest,
			SimpleTreeNode sTree) {
		List<Integer> indexes = new ArrayList<Integer>();
		int index = -1;
		Set<SimpleTreeNode> visited = new HashSet<SimpleTreeNode>();
		Stack<SimpleTreeNode> stack = new Stack<SimpleTreeNode>();
		List<SimpleTreeNode> reverseChildren = new ArrayList<SimpleTreeNode>();
		Enumeration<SimpleTreeNode> sEnum = null;
		SimpleTreeNode tmp = null;
		SimpleTreeNode tmp2 = null;
		stack.push(sTree);
		while (!stack.isEmpty()) {
			tmp = stack.pop();
			index = forest.indexOf(tmp);
			if (index != -1 && indexes.contains(index)) {
				indexes.add(-index);
			} else {
				if (index != -1) {
					indexes.add(index);
				}
				if (visited.contains(tmp))
					continue;
				stack.push(tmp);
				visited.add(tmp);
				if (tmp.getChildCount() > 0) {
					sEnum = tmp.children();
					reverseChildren.clear();
					while (sEnum.hasMoreElements()) {
						reverseChildren.add(sEnum.nextElement());
					}
					for (int i = reverseChildren.size() - 1; i >= 0; i--) {
						tmp2 = reverseChildren.get(i);
						stack.push(tmp2);
					}
				}
			}
		}
		return new Sequence(indexes);
	}

	private Set<Integer> removeRedundancy(Set<Integer> redundantTrees,
			Set<SimpleTreeNode> redundantNodes, EditInCommonCluster cluster) {
		List<SimpleTreeNode> forest0 = cluster.getForests().get(0);

		Set<Integer> tmpEditIndexesToRemove = new HashSet<Integer>();
		Enumeration<SimpleTreeNode> sEnum = null;
		Enumeration<SimpleTreeNode> sEnum2 = null;
		SimpleTreeNode sTmp = null;
		SimpleTreeNode sTmp2 = null;
		SimpleTreeNode sParent = null;
		SimpleTreeNode redundantTree = null;
		List<Integer> tmpEditIndexes = null;
		Set<Integer> sEditIndexes = null;
		for (Integer index : redundantTrees) {
			redundantTree = forest0.get(index);
			tmpEditIndexesToRemove.addAll(redundantTree.getEditIndexes());
		}
		boolean isChanged = true;
		while (isChanged) {
			isChanged = false;
			for (int i = 0; i < forest0.size(); i++) {
				SimpleTreeNode tmpTree = forest0.get(i);
				if (redundantTrees.contains(i))
					continue;
				// check each tree to see whether it contains edits to remove
				tmpEditIndexes = new ArrayList<Integer>(
						tmpTree.getEditIndexes());
				tmpEditIndexes.retainAll(tmpEditIndexesToRemove);
				if (!tmpEditIndexes.isEmpty()) {
					// if so, locate the node which is relevant to the edits
					sEnum = tmpTree.depthFirstEnumeration();
					while (sEnum.hasMoreElements()) {
						sTmp = sEnum.nextElement();
						sEditIndexes = new HashSet<Integer>(
								sTmp.getEditIndexes());
						sEditIndexes.retainAll(tmpEditIndexes);
						if (!sEditIndexes.isEmpty()) {
							if (sTmp.isRoot()) {
								redundantTrees.add(forest0.indexOf(tmpTree));
								break;
							} else {
								sEnum2 = sTmp.breadthFirstEnumeration();
								while (sEnum2.hasMoreElements()) {
									sTmp2 = sEnum2.nextElement();
									if (redundantNodes.add(sTmp2)
											&& tmpEditIndexesToRemove
													.addAll(sTmp2
															.getEditIndexes()))
										isChanged = true;
								}
								sParent = (SimpleTreeNode) sTmp.getParent();
								Set<Integer> keySet = null;
								while (sParent != null) {
									keySet = new HashSet<Integer>(
											sParent.getEditIndexes());
									keySet.retainAll(sEditIndexes);
									for (Integer key : keySet) {
										sParent.getEditAndRoletype()
												.remove(key);
									}
									sParent = (SimpleTreeNode) sParent
											.getParent();
								}
							}
						}
					}
				}
			}
		}
		// check overlap between redundantNodeIndexes and redundantTrees
		Set<SimpleTreeNode> newRedundantNodes = new HashSet<SimpleTreeNode>();
		for (SimpleTreeNode rNode : redundantNodes) {
			if (!redundantTrees.contains(forest0
					.indexOf(((SimpleTreeNode) rNode.getRoot())))) {
				newRedundantNodes.add(rNode);
			}
		}
		redundantNodes.clear();
		redundantNodes.addAll(newRedundantNodes);
		return tmpEditIndexesToRemove;
	}

	private void reorderForests(Sequence result, EditInCommonCluster cluster,
			Set<Integer> redundantTrees, Set<SimpleTreeNode> redundantNodes) {
		List<List<SimpleTreeNode>> forests = cluster.getForests();
		// 3. reorder the forests
		List<SimpleTreeNode> oldForest = null;
		List<SimpleTreeNode> newForest = null;
		List<Integer> resultIndexes = result.getNodeIndexes();
		for (int i = 0; i < forests.size(); i++) {
			oldForest = forests.get(i);
			newForest = new ArrayList<SimpleTreeNode>();
			for (Integer index : resultIndexes) {
				if (index < 0)
					continue;
				newForest.add(oldForest.get(index));
			}
			forests.set(i, newForest);
		}
		trimRedundantNodes(forests, redundantTrees, redundantNodes);
	}

	private void trimRedundantNodes(List<List<SimpleTreeNode>> forests,
			Set<Integer> redundantTrees, Set<SimpleTreeNode> redundantNodes) {
		List<SimpleTreeNode> nodesToRemove = null;
		if (!redundantNodes.isEmpty()) {
			List<SimpleTreeNode> forest = null;
			SimpleTreeNode tmpTree = null;
			SimpleTreeNode tmpTree2 = null;
			SimpleTreeNode sTmp = null;
			SimpleTreeNode sTmp2 = null;
			Enumeration<SimpleTreeNode> sEnum = null;
			Enumeration<SimpleTreeNode> sEnum2 = null;
			for (int i = 1; i < forests.size(); i++) {
				nodesToRemove = new ArrayList<SimpleTreeNode>();
				forest = forests.get(i);
				for (int j = 0; j < forest.size(); j++) {
					tmpTree = forests.get(0).get(j);
					if (redundantTrees.contains(j))
						continue;
					tmpTree2 = forests.get(i).get(j);
					sEnum = tmpTree.depthFirstEnumeration();
					sEnum2 = tmpTree2.depthFirstEnumeration();
					while (sEnum.hasMoreElements()) {
						sTmp = sEnum.nextElement();
						sTmp2 = sEnum2.nextElement();
						if (redundantNodes.contains(sTmp)) {
							nodesToRemove.add(sTmp2);
						}
					}
				}
				for (SimpleTreeNode nodeToRemove : nodesToRemove) {
					nodeToRemove.removeFromParent();
				}
			}
			for (SimpleTreeNode rNode : redundantNodes) {
				rNode.removeFromParent();
			}
		}
	}
}
