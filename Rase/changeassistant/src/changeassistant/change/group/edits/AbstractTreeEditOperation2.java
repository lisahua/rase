package changeassistant.change.group.edits;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import changeassistant.versions.treematching.edits.ITreeEditOperation;

public abstract class AbstractTreeEditOperation2<T extends DefaultMutableTreeNode>
		implements ITreeEditOperation<T> {

	protected static final long serialVersionUID = 1L;

	protected T fParent;

	protected int location;

	protected List<T> fSiblingsAfter;

	protected List<T> fSiblingsBefore;

	public AbstractTreeEditOperation2() {
	}

	public AbstractTreeEditOperation2(T parent, List<T> siblingsBefore,
			List<T> siblingsAfter) {
		fParent = parent;
		fSiblingsBefore = siblingsBefore;
		fSiblingsAfter = siblingsAfter;
		if (fSiblingsBefore != null)
			location = siblingsBefore.size();
	}

	public AbstractTreeEditOperation2(T parent, List<T> siblingsBefore,
			List<T> siblingsAfter, int position) {
		fParent = parent;
		fSiblingsBefore = siblingsBefore;
		fSiblingsAfter = siblingsAfter;
		location = position;
	}

	@Override
	abstract public void apply();

	@Override
	public void apply(int index) {
	}

	@Override
	abstract public T getNode();

	@Override
	abstract public EDIT getOperationType();

	@Override
	public T getParentNode() {
		return this.fParent;
	}

	public int getLocation() {
		return location;
	}

	public List<T> getSiblingsBefore() {
		return fSiblingsBefore;
	}

	public List<T> getSiblingsAfter() {
		return fSiblingsAfter;
	}
}
