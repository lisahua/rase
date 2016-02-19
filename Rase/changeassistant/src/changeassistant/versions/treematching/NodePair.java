package changeassistant.versions.treematching;

import changeassistant.peers.comparison.Node;

public class NodePair{

	private Node leftNode;
	private Node rightNode;
	
	public NodePair(Node left, Node right){
		leftNode = left;
		rightNode = right;
	}
	
	public Node getLeft(){
		return leftNode;
	}
	
	public Node getRight(){
		return rightNode;
	}
	
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer("");
		sb.append("left inner node: " + this.leftNode + ", ");
		sb.append("right inner node: " + this.rightNode + "\n");
		return sb.toString();
	}
}
