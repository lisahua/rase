package changeassistant.multipleexample.contextualize;

import java.util.ArrayList;
import java.util.List;

import changeassistant.multipleexample.contextualize.datastructure.Sequence;
import changeassistant.multipleexample.partition.datastructure.EditInCommonCluster;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;

public class CHelper4Aligner {

	/**
	 * Side effects: indexesList, insts
	 * 
	 * @param interCluster
	 *            --the cluster which is used to merge insts contained in eClus
	 * @param eClus
	 * @param inst
	 *            --the instance shared between the two clusters
	 */
	public static List<List<List<SimpleASTNode>>> alignCommonBasedOnSameInstance(
			EditInCommonCluster interCluster, EditInCommonCluster eClus,
			List<List<Integer>> indexesList, List<Integer> insts, Integer inst) {
		List<Sequence> sequenceList = null;
		List<Integer> leftIndexes = interCluster.getSequenceList()
				.get(interCluster.getInstances().indexOf(inst))
				.getNodeIndexes();
		List<Integer> rightIndexes = eClus.getSequenceList()
				.get(eClus.getInstances().indexOf(inst)).getNodeIndexes();
		List<Integer> contextNodeIndexes = new ArrayList<Integer>(leftIndexes);
		contextNodeIndexes.retainAll(rightIndexes);

		List<List<List<SimpleASTNode>>> simpleASTNodesLists = new ArrayList<List<List<SimpleASTNode>>>();

		Integer tmpInst = null;
		List<Integer> tmpInsts = interCluster.getInstances();
		sequenceList = interCluster.getSequenceList();

		for (int i = 0; i < tmpInsts.size(); i++) {
			tmpInst = tmpInsts.get(i);
			if (!insts.contains(tmpInst)) {
				indexesList.add(trimNodeIndexes(contextNodeIndexes,
						leftIndexes, sequenceList.get(i).getNodeIndexes()));
				insts.add(tmpInst);
			}
		}
		simpleASTNodesLists.add(trimSimpleASTNodesList(contextNodeIndexes,
				leftIndexes, interCluster.getSimpleASTNodesList()));

		tmpInsts = eClus.getInstances();
		sequenceList = eClus.getSequenceList();

		for (int i = 0; i < tmpInsts.size(); i++) {
			tmpInst = tmpInsts.get(i);
			if (!insts.contains(tmpInst)) {
				indexesList.add(trimNodeIndexes(contextNodeIndexes,
						rightIndexes, sequenceList.get(i).getNodeIndexes()));
				insts.add(tmpInst);
			}
		}
		simpleASTNodesLists.add(trimSimpleASTNodesList(contextNodeIndexes,
				rightIndexes, eClus.getSimpleASTNodesList()));
		return simpleASTNodesLists;
	}

	/**
	 * Side effect: high_cluster's sequenceList is set
	 * 
	 * @param clusters
	 * @param high_cluster
	 * @param indexLists
	 * @param insts
	 */
	public static void alignCommonBasedOnSameInstance(
			List<EditInCommonCluster> clusters,
			EditInCommonCluster high_cluster, List<List<Integer>> indexLists,
			List<Integer> insts) {
		if (clusters.get(0).getSequence() == null)
			return;
		if (clusters.size() != 1) {
			List<Integer> contextSequenceIndexes = new ArrayList<Integer>(
					clusters.get(0).getSequence().getNodeIndexes());
			for (int i = 1; i < clusters.size(); i++) {
				contextSequenceIndexes.retainAll(clusters.get(i).getSequence()
						.getNodeIndexes());
			}
			EditInCommonCluster cluster = null;

			indexLists.add(contextSequenceIndexes);
			insts.add(clusters.get(0).getInstances().get(0));
			// find the sequence of all methods based on their alignment with
			// the
			// 1st method
			for (int i = 0; i < clusters.size(); i++) {
				cluster = clusters.get(i);
				indexLists.add(trimNodeIndexes(contextSequenceIndexes, cluster
						.getSequenceList().get(0).getNodeIndexes(), cluster
						.getSequenceList().get(1).getNodeIndexes()));
				insts.add(cluster.getInstances().get(1));
			}
		} else {
			indexLists.add(new ArrayList<Integer>(clusters.get(0)
					.getSequenceList().get(0).getNodeIndexes()));
			indexLists.add(new ArrayList<Integer>(clusters.get(0)
					.getSequenceList().get(1).getNodeIndexes()));
		}
		high_cluster.setSequenceList2(indexLists);
		high_cluster.setSequence(high_cluster.getSequenceList().get(0));
	}

	/**
	 * ASSUMPTION: the two sequences have the same length and they are
	 * corresponding to each other. This method has nothing to do with
	 * trimNodeIndexesList(...). By default, sequenceList should has two
	 * instances, one is the standard method, the other is the one to align with
	 * it and the aligned result should be returned.
	 * 
	 * @param contextNodeIndexes
	 * @param sequenceList
	 * @return
	 */
	public static List<Integer> trimNodeIndexes(
			List<Integer> contextNodeIndexes, List<Integer> knownIndexes,
			List<Integer> unknownIndexes) {
		List<Integer> result = new ArrayList<Integer>();
		int tmpIndex = -1;
		for (int i = 0; i < contextNodeIndexes.size(); i++) {
			tmpIndex = knownIndexes.indexOf(contextNodeIndexes.get(i));
			result.add(unknownIndexes.get(tmpIndex));
		}
		return result;
	}

	/**
	 * trim a list of node indexes based on their correspondence with a certain
	 * standard node index list
	 * 
	 * @param tmpNodeIndexList
	 * @param knownNodeIndexList
	 * @param nodeIndexLists
	 * @return
	 */
	private static List<List<Integer>> trimNodeIndexesList(
			List<Integer> tmpNodeIndexList, List<Integer> knownNodeIndexList,
			List<List<Integer>> nodeIndexLists) {
		List<List<Integer>> result = new ArrayList<List<Integer>>();
		List<Integer> indexes = new ArrayList<Integer>();
		for (int i = 0; i < tmpNodeIndexList.size(); i++) {
			indexes.add(knownNodeIndexList.indexOf(tmpNodeIndexList.get(i)));
		}
		List<Integer> oldNodeIndexList = null;
		List<Integer> newNodeIndexList = null;
		for (int i = 0; i < nodeIndexLists.size(); i++) {
			oldNodeIndexList = nodeIndexLists.get(i);
			if (oldNodeIndexList.equals(knownNodeIndexList)) {
				result.add(tmpNodeIndexList);
			} else {
				newNodeIndexList = new ArrayList<Integer>();
				for (Integer index : indexes) {
					newNodeIndexList.add(oldNodeIndexList.get(index));
				}
				result.add(newNodeIndexList);
			}
		}

		return result;
	}

	private static List<List<SimpleASTNode>> trimSimpleASTNodesList(
			List<Integer> contextNodeIndexes, List<Integer> concreteIndexes,
			List<List<SimpleASTNode>> simpleASTNodesList) {
		List<List<SimpleASTNode>> tmp = ContextualizationUtil
				.getCopy(simpleASTNodesList);
		List<List<SimpleASTNode>> result = new ArrayList<List<SimpleASTNode>>();
		List<Integer> contextPositiveIndexes = Sequence
				.parsePositiveIndexes(contextNodeIndexes);
		List<Integer> concretePositiveIndexes = Sequence
				.parsePositiveIndexes(concreteIndexes);
		for (Integer contextIndex : contextPositiveIndexes) {
			result.add(tmp.get(concretePositiveIndexes.indexOf(contextIndex)));
		}
		return result;

	}

}
