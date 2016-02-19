package changeassistant.multipleexample.contextualize;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import changeassistant.multipleexample.partition.datastructure.AbstractCluster;
import changeassistant.multipleexample.partition.datastructure.EditInCommonCluster;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.peers.comparison.Node;

public class CHelper4Merger {

	public static void merge(EditInCommonCluster high_cluster) {
		System.out.print("");
		List<AbstractCluster> incomings = high_cluster.getIncomings();
		int firstValidIndex = 0;
		EditInCommonCluster interCluster = (EditInCommonCluster) incomings
				.get(firstValidIndex);
		while (!interCluster.getApplicable()
				&& firstValidIndex < incomings.size() - 1) {
			interCluster = (EditInCommonCluster) incomings
					.get(++firstValidIndex);
		}
		if (firstValidIndex == incomings.size() - 1) {
			high_cluster.setApplicable(false);
			return;
		}
		interCluster = (EditInCommonCluster) interCluster.clone();

		Queue<EditInCommonCluster> queue = new LinkedList<EditInCommonCluster>();
		EditInCommonCluster eClus = null;
		List<List<List<SimpleASTNode>>> simpleASTNodesLists = null;
		for (int i = ++firstValidIndex; i < incomings.size(); i++) {
			queue.add((EditInCommonCluster) incomings.get(i));
		}
		Set<Integer> tmpSet1 = null, tmpSet2 = null;
		List<List<Integer>> indexesList = null;
		List<Integer> insts = null;
		Integer tmpInst = -1;
		int tmpIndex = -1;
		while (!queue.isEmpty()) {
			insts = new ArrayList<Integer>();
			indexesList = new ArrayList<List<Integer>>();
			tmpSet1 = new HashSet<Integer>(interCluster.getInstances());
			eClus = queue.remove();
			if (!eClus.getApplicable()) {
				insts.addAll(interCluster.getInstances());
				continue;
			}
			tmpSet2 = new HashSet<Integer>(eClus.getInstances());
			tmpSet1.retainAll(tmpSet2);

			if (!tmpSet1.isEmpty()) {
				if (tmpSet1.size() != tmpSet2.size()) {
					tmpInst = tmpSet1.iterator().next();
					// System.out.print("");
					// tmpInst is the instance used to align two clusters
					simpleASTNodesLists = CHelper4Aligner
							.alignCommonBasedOnSameInstance(interCluster,
									eClus, indexesList, insts, tmpInst);
					if (!CHelper4Unifier.unifyCommon(high_cluster,
							interCluster, eClus, indexesList, insts, tmpInst,
							simpleASTNodesLists)) {
						high_cluster.setApplicable(false);
						break;
					}
				} else {
					insts = interCluster.getInstances();
				}
			}
			if (interCluster.getInstances().size() == high_cluster
					.getInstances().size())
				break;
		}
		if (interCluster.getInstances().size() == high_cluster.getInstances()
				.size()) {
			high_cluster.setSequenceList(interCluster.getSequenceList());
			high_cluster.setSequence(interCluster.getSequenceList().get(0));
			high_cluster.setSimpleASTNodesList(interCluster
					.getSimpleASTNodesList());
			if (high_cluster.getForests() != null)
				high_cluster.enableForestOrder();
			high_cluster.setSpecificToUnifiedList(interCluster
					.getSpecificToUnifiedList());
			high_cluster.setUnifiedToSpecificList(interCluster
					.getUnifiedToSpecificList());
			high_cluster.setSimpleASTNodesLists(null);
			high_cluster.setExprsLists(interCluster.getSimpleExprsLists());
			List<Integer> originalInsts = high_cluster.getInstances();
			if (insts == null || insts.equals(originalInsts))
				return;// the merge processing is done
			/*
			 * If the order of insts is different from that of originalInsts,
			 * reorder them
			 */
			List<SimpleTreeNode> newSTrees = new ArrayList<SimpleTreeNode>();
			List<SimpleTreeNode> newSTrees2 = new ArrayList<SimpleTreeNode>();
			List<List<Integer>> newIndexesList = new ArrayList<List<Integer>>();
			List<Node> newUpdatedNodes = new ArrayList<Node>();
			List<Node> newNewNodes = new ArrayList<Node>();
			for (Integer inst : insts) {
				tmpIndex = originalInsts.indexOf(inst);
				newSTrees.add(high_cluster.getSTrees().get(tmpIndex));
				newSTrees2.add(high_cluster.getSTrees2().get(tmpIndex));
				newIndexesList.add(high_cluster.getIndexesList().get(tmpIndex));
				newUpdatedNodes.add(high_cluster.getUpdatedNodeList().get(
						tmpIndex));
				newNewNodes.add(high_cluster.getNewNodeList().get(tmpIndex));
			}
			high_cluster.setInstances(insts);
			high_cluster.setSTree(newSTrees.get(0));
			high_cluster.setSTreeList(newSTrees);
			high_cluster.setSTreeList2(newSTrees2);
			high_cluster.setIndexesList(newIndexesList);
			high_cluster.setUpdatedNodeList(newUpdatedNodes);
			high_cluster.setNewNodeList(newNewNodes);
		} else {
			high_cluster.setApplicable(false);
		}
	}
}
