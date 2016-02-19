package changeassistant.multipleexample.edit;

import java.util.List;

import changeassistant.change.group.edits.DeleteOperationTemplate;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;

public class STreeDeleteOperation extends
		DeleteOperationTemplate<SimpleTreeNode> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8425750916087196410L;

	public STreeDeleteOperation(SimpleTreeNode nodeToDelete,
			SimpleTreeNode parent, List<SimpleTreeNode> siblingsBefore,
			List<SimpleTreeNode> siblingsAfter) {
		super(nodeToDelete, parent, siblingsBefore, siblingsAfter);
		// TODO Auto-generated constructor stub
	}

	public STreeDeleteOperation(SimpleTreeNode nodeToDelete) {
		super(nodeToDelete, null, null, null);
	}

	@Override
	public String toString() {
		return super.toString();
	}

}
