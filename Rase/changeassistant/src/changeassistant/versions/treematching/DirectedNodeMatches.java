package changeassistant.versions.treematching;

import java.util.Set;

import changeassistant.peers.comparison.Node;

public class DirectedNodeMatches {

	public static enum DIRECT{LeftToRight, RightToLeft};
	
	private Node node1;
	
	private Set<Node> matchingNodes;
	
	private DIRECT direct;
	
	public DirectedNodeMatches(Node node, Set<Node> nodes, DIRECT direct){
		this.node1 = node;
		this.matchingNodes = nodes;
		this.direct = direct;
	}
	
	public Node getNode(){
		return node1;
	}
	
	public Set<Node> getMatchingNodes(){
		return matchingNodes;
	}
	
	public DIRECT getDirection(){
		return direct;
	}
}

