package changeassistant.versions.treematching.edits;

import java.util.Enumeration;

import org.eclipse.jdt.core.dom.ASTNode;

import changeassistant.model.AbstractNode.EDITED_TYPE;
import changeassistant.peers.comparison.Node;

public class UpdateOperation extends AbstractTreeEditOperation{

	private Node fNodeToUpdate;
    private String fNewValue;
    private String fOldValue;
    private boolean fApplied;
    private Node fNewNode;
  
	public UpdateOperation(Node nodeToUpdate, Node newNode, String value) {
        fNodeToUpdate = nodeToUpdate;
        fNewNode = newNode;
        fOldValue = fNodeToUpdate.getStrValue();
        fParent = (Node)nodeToUpdate.getParent();
        fPosition = nodeToUpdate.locationInParent();
        fNewValue = value;
    }
	
	@Override
	public void apply() {
		if (!fApplied) {			
			fNodeToUpdate.setEDITED_TYPE(EDITED_TYPE.UPDATED);
            fNodeToUpdate.setStrValue(fNewNode.getStrValue());
            fNodeToUpdate.setNodeType(fNewNode.getNodeType());            
            fApplied = true;
        }
	}	
	
	public Node getNewNode() {
        return fNewNode;
    }
	
	public String getNewValue(){
		return fNewValue;
	}
	
	@Override
	public Node getNode(){
		return this.fNodeToUpdate;
	}
	
	public Node getNodeToUpdate() {
        return fNodeToUpdate;
    }
	
	public String getOldValue() {
        return fOldValue;
    }
	
	public EDIT getOperationType() {
        return ITreeEditOperation.EDIT.UPDATE;
    }
	
	@Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("\n--Update operation--\n");
        sb.append("Node value to update: ");
        sb.append(fOldValue);
        sb.append("\nwith value: ");
        sb.append(fNewValue);
        return sb.toString();
    }
}
