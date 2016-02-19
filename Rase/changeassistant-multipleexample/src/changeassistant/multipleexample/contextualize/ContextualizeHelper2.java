package changeassistant.multipleexample.contextualize;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import changeassistant.multipleexample.contextualize.datastructure.Sequence;
import changeassistant.multipleexample.main.CachedProjectMap;
import changeassistant.multipleexample.partition.datastructure.ClusterHelper;
import changeassistant.multipleexample.partition.datastructure.EditInCommonCluster;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.multipleexample.staticanalysis.AnalysisManager;
import changeassistant.peers.SourceCodeRange;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.comparison.MethodModification;

public class ContextualizeHelper2 {

	private List<EditInCommonCluster> clusters = null;
	private List<MethodModification> mmList = null;

	// private List<List<Set<Node>>> controlNodesLists = null;
	// private List<List<Set<Node>>> dataNodesLists = null;

	public ContextualizeHelper2(List<EditInCommonCluster> clusters,
			List<MethodModification> mmList) {
		this.clusters = clusters;
		this.mmList = mmList;
	}

	public void refineCommonContext() {
		// controlNodesLists = new ArrayList<List<Set<Node>>>();
		// dataNodesLists = new ArrayList<List<Set<Node>>>();
		List<EditInCommonCluster> level_1_clusters = ClusterHelper
				.getLevel_1_clusters(clusters);
		Queue<EditInCommonCluster> queue = new LinkedList<EditInCommonCluster>();
		Set<EditInCommonCluster> processed = new HashSet<EditInCommonCluster>();

		queue.addAll(level_1_clusters);
		EditInCommonCluster cluster = null;

		System.out.print("");
		while (!queue.isEmpty()) {
			cluster = queue.remove();
			if (processed.add(cluster)) {// the cluster has not been processed
				if (cluster.getApplicable() && cluster.checkForestOrder()) {
					refineNodeIndexes(cluster);
				}
				if (cluster.getOutgoings() != null) {
					queue.addAll(cluster.getOutgoings());
				}
			}
		}
	}

	protected void refineNodeIndexes(EditInCommonCluster cluster) {
		List<Integer> stanNodeIndexes = null;
		List<Integer> tmpNodeIndexes = null;
		List<Integer> positiveNodeIndexes = null;
		List<Integer> positiveNodeIndexesBackup = null;
		List<Integer> relevantNodeIndexes = null;
		List<Integer> nodeIndexesToRemove = null;
		List<List<Integer>> otherNodeIndexes = null;
		List<List<Integer>> originalNodeIndexes = new ArrayList<List<Integer>>();
		boolean allRemoved = false;
		List<Sequence> sequenceList = cluster.getSequenceList();
		if (sequenceList.isEmpty()) {
			cluster.setApplicable(false);
			return;
		}
		MethodModification mm = null;
		AnalysisManager aManager = null;
		int indexOfNodeIndex = 0;
		int indexOfMinusNodeIndex = 0;
		int numOfInsts = cluster.getInstances().size();
		List<Integer> nodeIndexes = sequenceList.get(0).getNodeIndexes();
		positiveNodeIndexesBackup = new ArrayList<Integer>();
		for (Integer nodeIndex : nodeIndexes) {
			if (nodeIndex > 0)
				positiveNodeIndexesBackup.add(nodeIndex);
		}
		// to back up the original node indexes list
		for (int i = 0; i < numOfInsts; i++) {
			originalNodeIndexes.add(new ArrayList<Integer>(sequenceList.get(i)
					.getNodeIndexes()));
		}
		for (int i = 0; i < numOfInsts; i++) {
			otherNodeIndexes = new ArrayList<List<Integer>>();
			for (int j = 0; j < numOfInsts; j++) {
				if (j == i) {
					// enumerate the relevant node set in each method to get the
					// smallest common context
					stanNodeIndexes = sequenceList.get(j).getNodeIndexes();
				} else {
					otherNodeIndexes.add(sequenceList.get(j).getNodeIndexes());
				}
			}
			mm = mmList.get(cluster.getInstances().get(i));
			aManager = new AnalysisManager(
					CachedProjectMap.get(mm.originalMethod.getProjectName()),
					CachedProjectMap.get(mm.newMethod.getProjectName()));
			aManager.setMethodModification(mm);

			positiveNodeIndexes = new ArrayList<Integer>();
			for (Integer nodeIndex : stanNodeIndexes) {
				if (nodeIndex > 0) {
					positiveNodeIndexes.add(nodeIndex);
				}
			}
			System.out.print("");
			relevantNodeIndexes = getRelevantNodeIndexes(aManager,
					new SimpleTreeNode(cluster.getSTrees().get(i)), cluster
							.getIndexesList().get(i));
			nodeIndexesToRemove = new ArrayList<Integer>();
			for (Integer nodeIndex : stanNodeIndexes) {
				if (nodeIndex > 0 && !relevantNodeIndexes.contains(nodeIndex)) {
					nodeIndexesToRemove.add(nodeIndex);
				}
			}
			if (nodeIndexesToRemove.size() == positiveNodeIndexes.size()) {
				allRemoved = true;
				break;
			} else {
				for (Integer nodeIndex : nodeIndexesToRemove) {
					positiveNodeIndexes.remove(nodeIndex);
					indexOfNodeIndex = stanNodeIndexes.indexOf(nodeIndex);
					stanNodeIndexes.remove(indexOfNodeIndex);
					indexOfMinusNodeIndex = stanNodeIndexes.indexOf(-nodeIndex);
					if (indexOfMinusNodeIndex != -1) {
						stanNodeIndexes.remove(indexOfMinusNodeIndex);
					}
					for (int k = 0; k < otherNodeIndexes.size(); k++) {
						tmpNodeIndexes = otherNodeIndexes.get(k);
						tmpNodeIndexes.remove(indexOfNodeIndex);
						if (indexOfMinusNodeIndex != -1)
							tmpNodeIndexes.remove(indexOfMinusNodeIndex);
					}
				}
			}
		}
		if (allRemoved) {// roll back to the original context
			for (int i = 0; i < numOfInsts; i++) {
				sequenceList.set(i, new Sequence(originalNodeIndexes.get(i)));
			}
		} else if (positiveNodeIndexes.size() == 1) {
			int index = positiveNodeIndexesBackup.indexOf(cluster.getSequence()
					.get(0));
			if (cluster.getSimpleASTNodesList().get(index).isEmpty()) {
				for (int i = 0; i < numOfInsts; i++) {
					sequenceList.set(i,
							new Sequence(originalNodeIndexes.get(i)));
				}
			}
		}
		cluster.setSequence(sequenceList.get(0));
	}

	protected List<Integer> getRelevantNodeIndexes(AnalysisManager aManager,
			SimpleTreeNode sTree, List<Integer> indexes) {
		Set<Node> relevantNodes = aManager.findRelevantNodes(indexes);
		// addDependenceConstraints(aManager, indexes);
		Map<SourceCodeRange, List<Node>> tmpRangeNodesMap = new HashMap<SourceCodeRange, List<Node>>();
		SourceCodeRange range = null;
		SimpleTreeNode sTmp = null;
		List<Node> nodes = null;
		Enumeration<SimpleTreeNode> sEnum = null;
		List<Integer> relevantNodeIndexes = new ArrayList<Integer>();
		System.out.print("");
		for (Node rel : relevantNodes) {
			range = rel.getSourceCodeRange();
			if (tmpRangeNodesMap.get(range) == null) {
				nodes = new ArrayList<Node>();
				tmpRangeNodesMap.put(range, nodes);
			}
			tmpRangeNodesMap.get(range).add(rel);
		}
		sEnum = sTree.breadthFirstEnumeration();
		while (sEnum.hasMoreElements()) {
			sTmp = sEnum.nextElement();
			nodes = tmpRangeNodesMap.get(sTmp.getSourceCodeRange());
			if (nodes != null) {
				for (Node node : nodes) {
					if (node.getStrValue().equals(sTmp.getStrValue())) {
						relevantNodeIndexes.add(sTmp.getNodeIndex());
					}
				}
			}
		}
		return relevantNodeIndexes;
	}
}
