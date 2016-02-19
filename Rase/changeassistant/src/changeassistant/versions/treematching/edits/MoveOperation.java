package changeassistant.versions.treematching.edits;

import java.util.Enumeration;

import org.eclipse.jdt.core.dom.ASTNode;

import changeassistant.model.AbstractNode.EDITED_TYPE;
import changeassistant.peers.comparison.Node;


public class MoveOperation extends AbstractTreeEditOperation {

	private static final String LEFT_PARENTHESIS = " (";
    private static final String RIGHT_PARENTHESIS = ")";
    private Node fNodeToMove;
    private Node fNewParent;
    private Node fNewNode;
    private int fNewPosition = -1;
    private boolean fApplied;
	
    public MoveOperation(Node nodeToMove, Node newNode, Node parent, int position){
    	fNodeToMove = nodeToMove;
    	fParent = (Node)fNodeToMove.getParent();
    	fPosition = fNodeToMove.locationInParent();
        fNewNode = newNode;
        fNewParent = parent;
        fNewPosition = position;
    }
    
//    public MoveOperation(Node nodeToMove, Node newNode, int oldPosition, Node parent, int position){
//    	this((Node)nodeToMove.clone(), newNode, parent, position);
//    	this.fParent = (Node)nodeToMove.getParent();
//    	this.fPosition = oldPosition;
//    }
    
	public void apply() {
		if (!fApplied) {
			fNodeToMove.setEDITED_TYPE(EDITED_TYPE.MOVED);
            if (fNewParent.getChildCount() <= fNewPosition) {
            	try{
                fNewParent.add(fNodeToMove);//this is move operation by definition
            	}catch(Exception e){
            		e.printStackTrace();
            	}
            } else {
                fNewParent.insert(fNodeToMove, fNewPosition);
            }    
            fApplied = true;
		}            		
	}
	
	public Node getNewNode() {
        return fNewNode;
    }
	
	public Node getNewParentNode(){
		return this.fNewParent;
	}
	
	public int getNewPosition(){
		return this.fNewPosition;
	}
	@Override
	public Node getNode(){
		return this.fNodeToMove;
	}
	
	public Node getNodeToMove() {
        return fNodeToMove;
    }

	@Override
	public EDIT getOperationType() {	
		return ITreeEditOperation.EDIT.MOVE;
	}
	
	@Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("\n--Move operation--\n");
        sb.append("Node value to move: ");
        sb.append(fNodeToMove.toString() + LEFT_PARENTHESIS + fNodeToMove.getNodeType() + RIGHT_PARENTHESIS);
        sb.append("\nas child of: ");
        sb.append(fNewParent.toString() + LEFT_PARENTHESIS + fNewParent.getNodeType() + RIGHT_PARENTHESIS);
        sb.append("\nat position: ");
        sb.append(fPosition);
        return sb.toString();
    }

}
