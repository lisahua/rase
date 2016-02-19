package changeassistant.versions.treematching;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import changeassistant.peers.comparison.Node;
import changeassistant.versions.treematching.DirectedNodeMatches.DIRECT;

public class BiDirectionMap {

	private Map<Node, Node> leftToRight;
	
	private Map<Node, Node> rightToLeft;
	
	private int size;
	
	public BiDirectionMap(Node node1, Node node2, DIRECT direct){
		this();
		add(node1, node2, direct);
	}
	
	public BiDirectionMap(){
		this.leftToRight = new HashMap<Node, Node>();
		this.rightToLeft = new HashMap<Node, Node>();
		size = 0;
	}
	
	public BiDirectionMap(BiDirectionMap origin){
		this.leftToRight = new HashMap<Node, Node>(origin.leftToRight);
		this.rightToLeft = new HashMap<Node, Node>(origin.rightToLeft);
		this.size = origin.size;
	}
	
	public void add(Node node1, Node node2, DIRECT direction){
		switch(direction){
		case LeftToRight:{
			leftToRight.put(node1, node2);
			rightToLeft.put(node2, node1);
		}break;
		case RightToLeft:{
			leftToRight.put(node2, node1);
			rightToLeft.put(node1, node2);
		}break;
		}
		size++;
	}
	
	public void addAll(Map<Node, Node> map, DIRECT direct){
		for(Entry<Node, Node> entry : map.entrySet()){
			add(entry.getKey(), entry.getValue(), direct);
		}
	}

	public Set<Node> getLeftNodes(){
		return leftToRight.keySet();
	}
	
	public Map<Node, Node> getLeftToRightMap(){
		return this.leftToRight;
	}
	
	public Set<Node> getRightNodes(){
		return rightToLeft.keySet();
	}
	
	public Map<Node, Node> getRightToLeftMap(){
		return this.rightToLeft;
	}
	
	public int getSize(){
		return this.size;
	}
}
