package changeassistant.versions.comparison;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import changeassistant.change.group.model.SubTreeModel;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.treematching.edits.AbstractTreeEditOperation;
import changeassistant.versions.treematching.edits.ITreeEditOperation;

public class MethodModification implements Serializable{

	public ChangedMethodADT originalMethod;
	public ChangedMethodADT newMethod;
	List<AbstractTreeEditOperation> edits;
	SubTreeModel subTree = null;
		
	public MethodModification(ChangedMethodADT originalMethod, ChangedMethodADT newMethod, 
			List<AbstractTreeEditOperation> edits /*, Map<Node, Node> leftToRightMatchPrime, 
			Map<Node, Node> rightToLeftMatchPrime*/){
		this.originalMethod = originalMethod;
		this.newMethod = newMethod;
		this.edits = edits;
//		this.leftToRightMatchPrime = leftToRightMatchPrime;
//		this.rightToLeftMatchPrime = rightToLeftMatchPrime;
	}
	
	public boolean equals(Object obj){
		if(!(obj instanceof MethodModification))
			return false;
		MethodModification other = (MethodModification)obj;
		if(!this.originalMethod.equals(other.originalMethod))
			return false;
		return true;
	}
	
	public List<AbstractTreeEditOperation> getEdits(){
		return this.edits;
	}

	public SubTreeModel getSubTree(){
		return this.subTree;
	}
	
	public int hashCode(){
		return this.originalMethod.hashCode();
	}
	
	public void setSubTree(SubTreeModel subTree){
		this.subTree = subTree;
	}
	
	@Override
	public String toString(){
		return this.originalMethod.toString();
	}
}
