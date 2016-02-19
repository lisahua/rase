package changeassistant.model;

import java.io.Serializable;
import java.util.List;

import changeassistant.change.group.edits.AbstractTreeEditOperation2;
import changeassistant.change.group.model.SubTreeModel;
import changeassistant.versions.comparison.ChangedMethodADT;
import changeassistant.versions.comparison.MethodModification;
import changeassistant.versions.treematching.edits.AbstractTreeEditOperation;

public class TransformationRule implements Serializable {

	private static final long serialVersionUID = 1L;
	public SubTreeModel subTreeModel;
	public List<AbstractTreeEditOperation2<SubTreeModel>> editScript;
	// public MethodModification mm;
	public ChangedMethodADT originalMethod;
	public ChangedMethodADT newMethod;
	public String oldMDStr;
	public String newMDStr;

	public TransformationRule(SubTreeModel subTreeModel,
			List<AbstractTreeEditOperation2<SubTreeModel>> editScript,
			MethodModification mm) {
		// this.patternNode = patternNode;
		this.subTreeModel = subTreeModel;
		this.editScript = editScript;
		this.originalMethod = mm.originalMethod;
		this.newMethod = mm.newMethod;
		List<AbstractTreeEditOperation> edits = mm.getEdits();
		this.oldMDStr = edits.get(0).getParentNode().getMethodDeclaration()
				.toString();
		this.newMDStr = edits.get(edits.size() - 1).getNode()
				.getMethodDeclaration().toString();
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(originalMethod.toString()
				+ " experiences a list of edits");
		buffer.append("\n");
		for (AbstractTreeEditOperation2<SubTreeModel> edit : editScript) {
			buffer.append(edit.toString());
			buffer.append("\n");
		}
		return buffer.toString();
	}
}
