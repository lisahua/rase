package changeassistant.change.group.edits;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

public class MoveOperationTemplate<T extends DefaultMutableTreeNode> extends
		AbstractTreeEditOperation2<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected T fNodeToMove;
	protected T fNewParent;
	protected List<T> fNewSiblingsBefore;
	protected List<T> fNewSiblingsAfter;

	public MoveOperationTemplate(T nodeToMove, T parent,
			List<T> siblingsBefore, List<T> siblingsAfter, T newParent,
			List<T> newSiblingsBefore, List<T> newSiblingsAfter) {
		super(parent, siblingsBefore, siblingsAfter);
		fNewSiblingsBefore = newSiblingsBefore;
		fNewSiblingsAfter = newSiblingsAfter;
		fNewParent = newParent;
		fNodeToMove = nodeToMove;
	}

	public MoveOperationTemplate(T nodeToMove, T parent,
			List<T> siblingsBefore, List<T> siblingsAfter, T newParent,
			List<T> newSiblingsBefore, List<T> newSiblingsAfter, int position) {
		this(nodeToMove, parent, siblingsBefore, siblingsAfter, newParent,
				newSiblingsBefore, newSiblingsAfter);
		this.location = position;
	}

	@Override
	public void apply() {
		try {
			if (fNewParent.getChildCount() <= location) {
				fNewParent.add(fNodeToMove);
			} else {
				fNewParent.insert(fNodeToMove, location);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public T getNewParent() {
		return fNewParent;
	}

	public List<T> getNewSiblingsBefore() {
		return fNewSiblingsBefore;
	}

	public List<T> getNewSiblingsAfter() {
		return fNewSiblingsAfter;
	}

	@Override
	public T getNode() {
		return fNodeToMove;
	}

	@Override
	public changeassistant.versions.treematching.edits.ITreeEditOperation.EDIT getOperationType() {
		// TODO Auto-generated method stub
		return EDIT.MOVE;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("***********Move Operation************");
		buffer.append("\n");
		buffer.append("To move " + this.fNodeToMove + " from the position "
				+ fSiblingsBefore.size() + " under parent " + this.fParent
				+ " to the position " + fNewSiblingsBefore.size()
				+ " under parent " + this.fNewParent);
		buffer.append("\n");
		return buffer.toString();
	}
}
