package changeassistant.multipleexample.edit;

import java.util.List;

import changeassistant.change.group.edits.InsertOperationTemplate;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;

public class STreeInsertOperation extends
		InsertOperationTemplate<SimpleTreeNode> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public STreeInsertOperation(SimpleTreeNode nodeToInsert,
			SimpleTreeNode parent, int location) {
		super(nodeToInsert, parent, null, null, location);
	}

	public STreeInsertOperation(SimpleTreeNode nodeToInsert,
			SimpleTreeNode parent, List<SimpleTreeNode> siblingsBefore,
			List<SimpleTreeNode> siblingsAfter) {
		super(nodeToInsert, parent, siblingsBefore, siblingsAfter);
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
