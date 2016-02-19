package changeassistant.change.group.edits;

import java.util.List;

import changeassistant.change.group.model.SubTreeModel;

public class SubTreeDeleteOperation extends
		DeleteOperationTemplate<SubTreeModel> {

	private static final long serialVersionUID = 1L;

	public SubTreeDeleteOperation(SubTreeModel nodeToDelete,
			SubTreeModel parent, List<SubTreeModel> siblingsBefore,
			List<SubTreeModel> siblingsAfter) {
		super(nodeToDelete, parent, siblingsBefore, siblingsAfter);
	}

	public SubTreeDeleteOperation(SubTreeModel nodeToDelete,
			SubTreeModel parent, List<SubTreeModel> siblingsBefore,
			List<SubTreeModel> siblingsAfter, int position) {
		super(nodeToDelete, parent, siblingsBefore, siblingsAfter, position);
	}

	@Override
	public void apply() {
		try {
			// fParent.remove(this.siblingsBefore.size());
			// fNodeToDelete.setEDITED_TYPE(EDITED_TYPE.DELETED);
			fParent.remove(location);
			fParent.updateChildren();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
