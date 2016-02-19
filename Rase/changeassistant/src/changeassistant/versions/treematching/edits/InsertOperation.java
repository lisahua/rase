package changeassistant.versions.treematching.edits;

import java.util.Enumeration;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

import changeassistant.model.AbstractNode.EDITED_TYPE;
import changeassistant.peers.comparison.Node;

public class InsertOperation extends AbstractTreeEditOperation{

	private static final String LEFT_PARENTHESIS = " (";
    private static final String RIGHT_PARENTHESIS = ")";
    private Node fNodeToInsert;
    private List<ASTNode> astExpressions;
    private boolean fApplied;
    
    public InsertOperation(Node nodeToInsert, List<ASTNode> astExpressions, Node parent, int position){
    	fNodeToInsert = nodeToInsert;
        fParent = parent;
        fPosition = position;
        this.astExpressions = astExpressions;
    }
    
    public int getPosition(){
    	return this.fPosition;
    }
    
    public void apply() {
        if (!fApplied) {
        	if(fParent == null)
        	{
        		System.out.println("the parent is null");
        	}        
        	if(fParent.getChildCount() <= fPosition){
        		fParent.add(fNodeToInsert);
        	}else{
        		fParent.insert(fNodeToInsert, fPosition);
        	}
        	fNodeToInsert.setEDITED_TYPE(EDITED_TYPE.INSERTED);        	
            fApplied = true;
        }
    }
    
    public List<ASTNode> getASTNodesToInsert(){
    	return this.astExpressions;
    }
    
    @Override
    public Node getNode(){
    	return this.fNodeToInsert;
    }
    
    public Node getNodeToInsert() {
        return fNodeToInsert;
    }
    
    public EDIT getOperationType() {
        return ITreeEditOperation.EDIT.INSERT;
    }
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("\n--Insert operation--\n");
        sb.append("Node value to insert: ");
        sb.append(fNodeToInsert.toString() + LEFT_PARENTHESIS + fNodeToInsert.getNodeType() + RIGHT_PARENTHESIS);
        sb.append("\nas child of: ");
        sb.append(fParent.toString() + LEFT_PARENTHESIS + fParent.getNodeType() + RIGHT_PARENTHESIS);
        sb.append("\nat position: ");
        sb.append(fPosition);
        return sb.toString();
    }
}
