package changeassistant.multipleexample.edit;

import java.util.List;

import changeassistant.change.group.edits.UpdateOperationTemplate;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;

public class STreeUpdateOperation extends
		UpdateOperationTemplate<SimpleTreeNode> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public STreeUpdateOperation(SimpleTreeNode nodeToUpdate,
			SimpleTreeNode newNode) {
		super(nodeToUpdate, null, null, null, newNode);
	}

	public STreeUpdateOperation(SimpleTreeNode nodeToUpdate,
			SimpleTreeNode parent, List<SimpleTreeNode> siblingsBefore,
			List<SimpleTreeNode> siblingsAfter, SimpleTreeNode newNode) {
		super(nodeToUpdate, parent, siblingsBefore, siblingsAfter, newNode);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void apply() {
		fNodeToUpdate.setNodeType(fNewNode.getNodeType());
		fNodeToUpdate.setStrValue(fNewNode.getStrValue());
	}

	@Override
	public String toString() {
		return super.toString();
	}

}
