package changeassistant.versions.treematching.edits;

import java.util.Enumeration;

import changeassistant.peers.comparison.Node;

public abstract class AbstractTreeEditOperation implements ITreeEditOperation<Node> {

	private static final long serialVersionUID = 1L;
	protected int fPosition;
	protected Node fParent;
	 
	@Override
	abstract public void apply();

	@Override
	public void apply(int index){
		apply();
		this.getNode().setEditIndex(index);
	}
	
	@Override
	abstract public EDIT getOperationType();
	
	@Override
	public Node getParentNode(){
		return this.fParent;
	}
	
	abstract public Node getNode(); 
	
	public int getPosition() {
		return fPosition;
	} 

	public void setPosition(int position){
		this.fPosition = position;
	}
}
