package changeassistant.change.group.edits;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

public class InsertOperationTemplate<T extends DefaultMutableTreeNode> extends
		AbstractTreeEditOperation2<T> {

	private static final long serialVersionUID = 1L;
	protected T fNodeToInsert;

	public InsertOperationTemplate(T nodeToInsert, T parent,
			List<T> siblingsBefore, List<T> siblingsAfter) {
		super(parent, siblingsBefore, siblingsAfter);
		fNodeToInsert = nodeToInsert;
	}

	public InsertOperationTemplate(T nodeToInsert, T parent,
			List<T> siblingsBefore, List<T> siblingsAfter, int position) {
		this(nodeToInsert, parent, siblingsBefore, siblingsAfter);
		location = position;
	}

	@Override
	public void apply() {
		if (fParent.getChildCount() <= location) {
			fParent.add(fNodeToInsert);
		} else {
			fParent.insert(fNodeToInsert, location);
		}
	}

	@Override
	public T getNode() {
		// TODO Auto-generated method stub
		return fNodeToInsert;
	}

	@Override
	public changeassistant.versions.treematching.edits.ITreeEditOperation.EDIT getOperationType() {
		return EDIT.INSERT;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("***********Insert Operation************");
		buffer.append("\n");
		buffer.append("To insert " + this.fNodeToInsert + " to the position "
				+ location + " under parent " + this.fParent);
		buffer.append("\n");
		return buffer.toString();
	}

}
