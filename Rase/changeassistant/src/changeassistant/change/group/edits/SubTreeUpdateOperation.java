package changeassistant.change.group.edits;

import java.util.List;

import changeassistant.change.group.model.SubTreeModel;

public class SubTreeUpdateOperation extends
		UpdateOperationTemplate<SubTreeModel> {

	private static final long serialVersionUID = 1L;

	public SubTreeUpdateOperation(SubTreeModel nodeToUpdate,
			SubTreeModel parent, List<SubTreeModel> siblingsBefore,
			List<SubTreeModel> siblingsAfter, SubTreeModel newNode) {
		super(nodeToUpdate, parent, siblingsBefore, siblingsAfter, newNode);
	}

	@Override
	public void apply() {
		// fNodeToUpdate.setEDITED_TYPE(EDITED_TYPE.UPDATED);
		fNodeToUpdate.setNodeType(fNewNode.getNodeType());
		fNodeToUpdate.setStrValue(fNewNode.getStrValue());
		fNodeToUpdate.setAbstractExpressions(fNewNode.getAbstractExpressions());
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("***********Update Operation************");
		buffer.append("\n");
		buffer.append("To update " + fNodeToUpdate.getStrValue() + " to "
				+ fNewNode.getStrValue());
		buffer.append("\n");
		return buffer.toString();
	}

}
