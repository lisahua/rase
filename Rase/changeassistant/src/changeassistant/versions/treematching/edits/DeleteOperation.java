package changeassistant.versions.treematching.edits;

import org.eclipse.jdt.core.dom.ASTNode;

import changeassistant.model.AbstractNode.EDITED_TYPE;
import changeassistant.peers.comparison.Node;


public class DeleteOperation extends AbstractTreeEditOperation{

	private Node fNodeToDelete;
    private boolean fApplied;
    
    private ASTNode astNodeToDelete;
    
    public DeleteOperation(Node nodeToDelete){
    	fNodeToDelete = nodeToDelete;
    	fParent = (Node) fNodeToDelete.getParent();
    	fPosition = fNodeToDelete.locationInParent();
    }
    
	@Override
	public void apply() {
		if (!fApplied) {
			fNodeToDelete.setEDITED_TYPE(EDITED_TYPE.DELETED);
            fNodeToDelete.removeFromParent();
            fApplied = true;
        }		
	}

	public ASTNode getASTNodeToDelete(){
		return this.astNodeToDelete;
	}
	
	@Override
	public Node getNode(){
		return this.fNodeToDelete;
	}
	
	public Node getNodeToDelete() {
        return fNodeToDelete;
    }
	
	public EDIT getOperationType(){
		return ITreeEditOperation.EDIT.DELETE;
	}
	
	@Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("\n--Delete operation--\n");
        sb.append("Node to delete: ");
        sb.append(fNodeToDelete.toString() + " (" + fNodeToDelete.getNodeType() + ")");
        return sb.toString();
    }
}
