package changeassistant.clonereduction.manipulate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import changeassistant.clonereduction.main.CloneReductionMain;
import changeassistant.model.AbstractNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.multipleexample.staticanalysis.AnalysisManager;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.comparison.MethodModification;
import changeassistant.versions.treematching.edits.AbstractTreeEditOperation;
import changeassistant.versions.treematching.edits.DeleteOperation;
import changeassistant.versions.treematching.edits.InsertOperation;
import changeassistant.versions.treematching.edits.MoveOperation;
import changeassistant.versions.treematching.edits.UpdateOperation;

public class EditedNodeMarker {
	
	public static void markEditedNodeInOld(List<AbstractTreeEditOperation> edits, Node mNode, List<Integer> indexes, AnalysisManager aManager){
		Node tmp = null;
		AbstractTreeEditOperation edit = null;
		int markerIndex = 0;
		for (Integer index : indexes) {
			edit = edits.get(index);
			switch (edit.getOperationType()) {
			case UPDATE:
				UpdateOperation update = (UpdateOperation) edit;
				tmp = mNode.lookforNodeBasedOnRange(update.getNode());
				tmp.setRole(SimpleTreeNode.EDITED);			
				markerIndex = MarkerUtility.setMarkerProperty(tmp, markerIndex);
				if(CloneReductionMain.onlyHasOneSubtree){
					for(Node tmpNode : aManager.findNewControlDependingNodeList(tmp)){
						tmp = mNode.lookforNodeBasedOnRange(tmpNode);
						tmp.setRole(SimpleTreeNode.CONTEXTUAL);
						markerIndex = MarkerUtility.setMarkerProperty(tmp, markerIndex);
					}
				}
				break;
			case DELETE:
				DeleteOperation delete = (DeleteOperation)edit;
				tmp = mNode.lookforNodeBasedOnRange(delete.getNode());
				tmp.setRole(SimpleTreeNode.EDITED);
				markerIndex = MarkerUtility.setMarkerProperty(tmp, markerIndex);
				break;
			case MOVE:
				MoveOperation move = (MoveOperation) edit;
				tmp = mNode.lookforNodeBasedOnRange(move.getNode());
				if(tmp != null){
					tmp.setRole(SimpleTreeNode.EDITED);
					markerIndex = MarkerUtility.setMarkerProperty(tmp, markerIndex);
					if(CloneReductionMain.onlyHasOneSubtree){
						for(Node tmpNode : aManager.findNewControlDependingNodes(tmp)){
							tmp = mNode.lookforNodeBasedOnRange(tmpNode);
							tmp.setRole(SimpleTreeNode.CONTEXTUAL);
							markerIndex = MarkerUtility.setMarkerProperty(tmp, markerIndex);
						}
					}
				}
				break;
			default:
				break;
			}
		}
		mNode.setProperty(AbstractNode.MAX_MARKER_INDEX, markerIndex);
	}

	public static void markEditedNodeInNew(List<AbstractTreeEditOperation> edits, Node mNode, List<Integer> indexes,
			AnalysisManager aManager){
		Node tmp = null;		
		AbstractTreeEditOperation edit = null;
		int markerIndex = 0;
		for (Integer index : indexes) {
			edit = edits.get(index);
			switch (edit.getOperationType()) {
			case UPDATE:
				UpdateOperation update = (UpdateOperation) edit;
				tmp = mNode.lookforNodeBasedOnRange(update.getNewNode());
				tmp.setRole(SimpleTreeNode.EDITED);			
				markerIndex = MarkerUtility.setMarkerProperty(tmp, markerIndex);
				if(CloneReductionMain.onlyHasOneSubtree){
					for(Node tmpNode : aManager.findNewControlDependingNodeList(tmp)){
						tmp = mNode.lookforNodeBasedOnRange(tmpNode);
						tmp.setRole(SimpleTreeNode.CONTEXTUAL);
						markerIndex = MarkerUtility.setMarkerProperty(tmp, markerIndex);
					}
				}
				break;
			case INSERT:
				InsertOperation insert = (InsertOperation) edit;
				tmp = mNode.lookforNodeBasedOnRange(insert
						.getNodeToInsert());
				tmp.setRole(SimpleTreeNode.EDITED);
				markerIndex = MarkerUtility.setMarkerProperty(tmp, markerIndex);
				if(CloneReductionMain.onlyHasOneSubtree){
					for(Node tmpNode : aManager.findNewControlDependingNodeList(tmp)){
						tmp = mNode.lookforNodeBasedOnRange(tmpNode);
						tmp.setRole(SimpleTreeNode.CONTEXTUAL);
						markerIndex = MarkerUtility.setMarkerProperty(tmp, markerIndex);
					}
				}
				// tmp = (Node) tmp.getParent();
				// if (tmp.getRole() != SimpleTreeNode.EDITED)
				// tmp.setRole(SimpleTreeNode.CONTEXTUAL);
				break;
			case MOVE:
				MoveOperation move = (MoveOperation) edit;
				tmp = mNode.lookforNodeBasedOnRange(move.getNewNode());
				tmp.setRole(SimpleTreeNode.EDITED);
				markerIndex = MarkerUtility.setMarkerProperty(tmp, markerIndex);
				if(CloneReductionMain.onlyHasOneSubtree){
					for(Node tmpNode : aManager.findNewControlDependingNodes(tmp)){
						tmp = mNode.lookforNodeBasedOnRange(tmpNode);
						tmp.setRole(SimpleTreeNode.CONTEXTUAL);
						markerIndex = MarkerUtility.setMarkerProperty(tmp, markerIndex);
					}
				}
				// tmp = (Node) tmp.getParent();
				// if (tmp.getRole() != SimpleTreeNode.EDITED)
				// tmp.setRole(SimpleTreeNode.CONTEXTUAL);
				break;
			default:
				break;
			}
		}
		mNode.setProperty(AbstractNode.MAX_MARKER_INDEX, markerIndex);
	}
}
