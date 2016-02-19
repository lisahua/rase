package changeassistant.multipleexample.datastructure;

import java.util.ArrayList;
import java.util.List;

import changeassistant.multipleexample.partition.SimpleASTCreator;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.peers.comparison.Node;

public class SimpleASTNodesListForMethods {
	List<List<List<SimpleASTNode>>> simpleASTNodesListForMethods = null;
	int size = 0;
	public SimpleASTNodesListForMethods(List<Node> nodes){
		size = nodes.size();
		simpleASTNodesListForMethods = new ArrayList<List<List<SimpleASTNode>>>();
		List<List<SimpleASTNode>> simpleASTNodesList = null;
		SimpleASTCreator creator = new SimpleASTCreator();
		for(Node n : nodes){
			simpleASTNodesList = creator.createSimpleASTNodesList(n);
			simpleASTNodesListForMethods.add(simpleASTNodesList);
		}
	}
	
	public List<List<SimpleASTNode>> get(int index){
		return simpleASTNodesListForMethods.get(index);
	}
	
	public List<SimpleASTNode> getExprs(int i, int nodeIndex){
		return simpleASTNodesListForMethods.get(i).get(nodeIndex - 1);
	}
}
