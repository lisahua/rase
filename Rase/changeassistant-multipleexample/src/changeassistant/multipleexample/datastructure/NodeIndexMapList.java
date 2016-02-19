package changeassistant.multipleexample.datastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import changeassistant.multipleexample.util.NodeUtil;
import changeassistant.peers.comparison.Node;

public class NodeIndexMapList {
	List<Map<Node, Integer>> nodeIndexMapList = null;
	int size = 0;
	public NodeIndexMapList(List<Node> nodes){
		this.size = nodes.size();
		nodeIndexMapList = new ArrayList<Map<Node, Integer>>();
		NodeUtil nUtil = new NodeUtil();
		for(Node n : nodes){
			nodeIndexMapList.add(nUtil.createNodeIndexMap(n));
		}
	}
	
	public Map<Node, Integer> get(int index){
		return nodeIndexMapList.get(index);
	}
	
	public Node getNode(int index, int nodeIndex){
		Map<Node, Integer> map = nodeIndexMapList.get(index);
		for(Entry<Node, Integer> entry : map.entrySet()){
			if(entry.getValue().equals(nodeIndex))
				return entry.getKey();
		}
		return null;
	}
	
	
	public List<Integer> getIndexes(List<Node> nodes){
		List<Integer> result = new ArrayList<Integer>();
		for(int i = 0; i < size; i++){
			result.add(nodeIndexMapList.get(i).get(nodes.get(i)));
		}
		return result;
	}
}
