package changeassistant.multipleexample.edit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import changeassistant.change.group.edits.AbstractTreeEditOperation2;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;

public class STreeEditScript {
	Map<Integer, AbstractTreeEditOperation2<SimpleTreeNode>> updateInsertMoveMap = null;
	Map<Integer, STreeDeleteOperation> deleteMap = null;
	List<Integer> nodeIndexes = null;
	List<List<SimpleASTNode>> sNodesList = null;
	SimpleTreeNode finalTree;

	public STreeEditScript(
			Map<Integer, AbstractTreeEditOperation2<SimpleTreeNode>> updateInsertMove,
			Map<Integer, STreeDeleteOperation> deletes,
			List<Integer> nodeIndexes, List<List<SimpleASTNode>> sNodesList) {
		if (updateInsertMove == null) {
			updateInsertMoveMap = new HashMap<Integer, AbstractTreeEditOperation2<SimpleTreeNode>>();
		} else {
			updateInsertMoveMap = updateInsertMove;
		}
		if (deletes == null) {
			deleteMap = new HashMap<Integer, STreeDeleteOperation>();
		} else {
			deleteMap = deletes;
		}
		this.nodeIndexes = nodeIndexes;
		this.sNodesList = sNodesList;
	}

	public Map<Integer, STreeDeleteOperation> getDeletes() {
		return deleteMap;
	}

	public List<Integer> getNodeIndexes() {
		return nodeIndexes;
	}

	public SimpleTreeNode getFinalTree() {
		return finalTree;
	}

	public List<List<SimpleASTNode>> getSNodesList() {
		return sNodesList;
	}

	public Map<Integer, AbstractTreeEditOperation2<SimpleTreeNode>> getUpdateInsertMoves() {
		return updateInsertMoveMap;
	}
}
