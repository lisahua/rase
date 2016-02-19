package changeassistant.change.group.edits;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

public class DeleteOperationTemplate<T extends DefaultMutableTreeNode> extends
		AbstractTreeEditOperation2<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected T fNodeToDelete;

	public DeleteOperationTemplate(T nodeToDelete, T parent,
			List<T> siblingsBefore, List<T> siblingsAfter) {
		super(parent, siblingsBefore, siblingsAfter);
		fNodeToDelete = nodeToDelete;
	}

	public DeleteOperationTemplate(T nodeToDelete, T parent,
			List<T> siblingsBefore, List<T> siblingsAfter, int position) {
		this(nodeToDelete, parent, siblingsBefore, siblingsAfter);
		location = position;
	}

	@Override
	public void apply() {
		fNodeToDelete.removeFromParent();
	}

	@Override
	public T getNode() {
		// TODO Auto-generated method stub
		return fNodeToDelete;
	}

	@Override
	public changeassistant.versions.treematching.edits.ITreeEditOperation.EDIT getOperationType() {
		// TODO Auto-generated method stub
		return EDIT.DELETE;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("***********Delete Operation************");
		buffer.append("\n");
		buffer.append("To delete " + this.fNodeToDelete);
		buffer.append("\n");
		return buffer.toString();
	}

}
