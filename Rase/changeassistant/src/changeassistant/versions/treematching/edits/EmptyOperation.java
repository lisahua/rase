package changeassistant.versions.treematching.edits;

import changeassistant.peers.comparison.Node;

public class EmptyOperation extends AbstractTreeEditOperation {

	private Node node;
	
	public EmptyOperation(Node node){
		this.node = node;
	}
	
	@Override
	public void apply() {
		//do nothing
	}

	@Override
	public EDIT getOperationType() {
		// TODO Auto-generated method stub
		return EDIT.EMPTY;
	}

	@Override
	public Node getNode() {
		// TODO Auto-generated method stub
		return this.node;
	}

}
