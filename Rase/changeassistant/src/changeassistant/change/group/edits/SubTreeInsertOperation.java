package changeassistant.change.group.edits;

import java.util.List;

import changeassistant.change.group.model.SubTreeModel;

public class SubTreeInsertOperation extends
		InsertOperationTemplate<SubTreeModel> {

	// private SubTreeModel fNodeToInsert;// describe which node to insert
	public SubTreeInsertOperation(SubTreeModel nodeToInsert,
			SubTreeModel parent, List<SubTreeModel> siblingsBefore,
			List<SubTreeModel> siblingsAfter) {
		super(nodeToInsert, parent, siblingsBefore, siblingsAfter);
	}

	public SubTreeInsertOperation(SubTreeModel nodeToInsert,
			SubTreeModel parent, List<SubTreeModel> siblingsBefore,
			List<SubTreeModel> siblingsAfter, int position) {
		super(nodeToInsert, parent, siblingsBefore, siblingsAfter, position);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void apply() {// the application of this edit is simple since it is
							// applied to subTree
		// fParent.insert(fNodeToInsert, this.siblingsBefore.size());
		// if (fParent.getChildCount() <= location) {
		// fParent.add(fNodeToInsert);
		// } else {
		// fParent.insert(fNodeToInsert, location);
		// }
		super.apply();
		fParent.updateChildren();
	}
}
