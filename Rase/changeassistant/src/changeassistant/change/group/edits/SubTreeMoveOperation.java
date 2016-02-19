package changeassistant.change.group.edits;

import java.util.List;

import changeassistant.change.group.model.SubTreeModel;

public class SubTreeMoveOperation extends MoveOperationTemplate<SubTreeModel> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SubTreeMoveOperation(SubTreeModel sNodeToMove, SubTreeModel sParent,
			List<SubTreeModel> relevantSiblings,
			List<SubTreeModel> relevantSiblings2, SubTreeModel sNewParent,
			List<SubTreeModel> newSiblings, List<SubTreeModel> newSiblings2) {
		super(sNodeToMove, sParent, relevantSiblings, relevantSiblings2,
				sNewParent, newSiblings, newSiblings2, newSiblings.size());
	}

	public SubTreeMoveOperation(SubTreeModel sNodeToMove, SubTreeModel sParent,
			List<SubTreeModel> relevantSiblings,
			List<SubTreeModel> relevantSiblings2, SubTreeModel sNewParent,
			List<SubTreeModel> newSiblings, List<SubTreeModel> newSiblings2,
			int position) {
		super(sNodeToMove, sParent, relevantSiblings, relevantSiblings2,
				sNewParent, newSiblings, newSiblings2, position);
	}

	@Override
	public void apply() {
		// fNodeToMove.setEDITED_TYPE(EDITED_TYPE.MOVED);
		try {
			if (fNewParent.getChildCount() <= location) {
				fNewParent.add(fNodeToMove);
			} else {
				fNewParent.insert(fNodeToMove, location);
				// fNewParent.insert(fNodeToMove, newSiblingsBefore.size());
			}
			fParent.updateChildren();
			if (!fParent.equals(fNewParent))
				fNewParent.updateChildren();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
