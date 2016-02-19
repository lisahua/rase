package changeassistant.change.group.edits;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

public class UpdateOperationTemplate<T extends DefaultMutableTreeNode> extends
		AbstractTreeEditOperation2<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected T fNodeToUpdate;
	protected T fNewNode;

	public UpdateOperationTemplate(T nodeToUpdate, T parent,
			List<T> siblingsBefore, List<T> siblingsAfter, T newNode) {
		super(parent, siblingsBefore, siblingsAfter);
		fNodeToUpdate = nodeToUpdate;
		fNewNode = newNode;
	}

	@Override
	public void apply() {

	}

	public T getNewNode() {
		return fNewNode;
	}

	@Override
	public T getNode() {
		return fNodeToUpdate;
	}

	@Override
	public changeassistant.versions.treematching.edits.ITreeEditOperation.EDIT getOperationType() {
		return EDIT.UPDATE;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("***********Update Operation************");
		buffer.append("\n");
		buffer.append("To update " + fNodeToUpdate + " to " + fNewNode);
		buffer.append("\n");
		return buffer.toString();
	}
}
