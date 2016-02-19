package changeassistant.multipleexample.edit;

import java.util.List;

import changeassistant.change.group.edits.MoveOperationTemplate;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;

public class STreeMoveOperation extends MoveOperationTemplate<SimpleTreeNode> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3625657212530448614L;

	public STreeMoveOperation(SimpleTreeNode nodeToMove, SimpleTreeNode parent,
			List<SimpleTreeNode> siblingsBefore,
			List<SimpleTreeNode> siblingsAfter, SimpleTreeNode newParent,
			List<SimpleTreeNode> newSiblingsBefore,
			List<SimpleTreeNode> newSiblingsAfter) {
		super(nodeToMove, parent, siblingsBefore, siblingsAfter, newParent,
				newSiblingsBefore, newSiblingsAfter);
		// TODO Auto-generated constructor stub
	}

	public STreeMoveOperation(SimpleTreeNode nodeToMove,
			SimpleTreeNode newParent, int location) {
		super(nodeToMove, null, null, null, newParent, null, null, location);
	}

	@Override
	public String toString() {
		return super.toString();
	}

}
