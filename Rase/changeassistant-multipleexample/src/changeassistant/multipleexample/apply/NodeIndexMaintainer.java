package changeassistant.multipleexample.apply;

import java.util.ArrayList;
import java.util.List;

public class NodeIndexMaintainer {

	public static void changeIndexesForDelete(int nodeIndex,
			List<Integer> nodeIndexes) {
		List<Integer> result = new ArrayList<Integer>(nodeIndexes.subList(0,
				nodeIndexes.indexOf(nodeIndex)));
		result.addAll(nodeIndexes.subList(nodeIndexes.indexOf(-nodeIndex) + 1,
				nodeIndexes.size()));
		nodeIndexes.clear();
		nodeIndexes.addAll(result);
	}

	public static void changeIndexesForInsert(int insertAfterAnchor,
			int newNodeIndex, List<Integer> nodeIndexes) {
		nodeIndexes.add(insertAfterAnchor + 1, newNodeIndex);
		nodeIndexes.add(insertAfterAnchor + 2, -newNodeIndex);
	}

	/**
	 * 
	 * @param insertAfterNodeIndex
	 *            the index of the target location's previous sibling
	 * @param nodeIndex
	 *            the index of the node to move
	 * @param nodeIndexes
	 *            input and output
	 * @return
	 */
	public static void changeIndexesForMove(int insertAfterNodeIndex,
			int nodeIndex, List<Integer> nodeIndexes) {
		List<Integer> tmpIndexes = nodeIndexes.subList(
				nodeIndexes.indexOf(nodeIndex),
				nodeIndexes.indexOf(-nodeIndex) + 1);
		List<Integer> tmpNodeIndexes = new ArrayList<Integer>(nodeIndexes);
		tmpNodeIndexes.removeAll(tmpIndexes);
		List<Integer> preIndexes = new ArrayList<Integer>(
				tmpNodeIndexes.subList(0,
						tmpNodeIndexes.indexOf(insertAfterNodeIndex) + 1));
		preIndexes.addAll(tmpIndexes);
		preIndexes.addAll(tmpNodeIndexes.subList(
				tmpNodeIndexes.indexOf(insertAfterNodeIndex) + 1,
				tmpNodeIndexes.size()));
		nodeIndexes.clear();
		nodeIndexes.addAll(preIndexes);
	}
}
