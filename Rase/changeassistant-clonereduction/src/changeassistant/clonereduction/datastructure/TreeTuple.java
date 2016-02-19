package changeassistant.clonereduction.datastructure;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.eclipse.jdt.core.dom.ASTNode;

import changeassistant.multipleexample.datastructure.NodeIndexMapList;
import changeassistant.peers.comparison.Node;

public class TreeTuple {
	public static final int NOTCARE_INDEX = -1;
	public static final int NOTCARE_CINDEX = -1;
	private int relativeIndex;
	private int relativeChildIndex;
	private int nodeIndex;
	private int nodeType;
	
	public TreeTuple(int index, int cIndex, int nodeIndex, int nodeType){
		this.relativeIndex = index;
		this.relativeChildIndex = cIndex;
		this.nodeIndex = nodeIndex;
		this.nodeType = nodeType;
	}
	
	public static boolean checkEquivalence(List<TreeTuple> tuples1, List<TreeTuple> tuples2){
		if(tuples1.size() != tuples2.size())
			return false;
		TreeTuple tuple1 = null, tuple2 = null;
		for(int i = 0; i < tuples1.size(); i++){
			tuple1 = tuples1.get(i);
			tuple2 = tuples2.get(i);
			if(!tuple1.equals(tuple2)){
				return false;
			}
		}
		return true;
	}

	public static List<TreeTuple> createTuples(List<Node> markedNodes, NodeIndexMapList nodeIndexMapList){		
		List<TreeTuple> tuples = new ArrayList<TreeTuple>();
		TreeTuple tuple = null, tmpTuple = null;
		Queue<Node> queue = null;
		Node tmp = null, cTmp = null;
		int cIndex = -1, tIndex = -1;;
		Map<Node, Integer> nodeTupleMap = new HashMap<Node, Integer>();
		Map<Node, Integer> nodeIndexMap = null;
		Enumeration<Node> cEnum = null;
		Node n = null;
		for(int i = 0; i < markedNodes.size(); i++){
			n = markedNodes.get(i);		
			nodeIndexMap = nodeIndexMapList.get(i);
			tuple = new TreeTuple(NOTCARE_INDEX, NOTCARE_CINDEX, nodeIndexMap.get(n), n.getNodeType());
			nodeTupleMap.put(n, tuples.size());
			tuples.add(tuple);			
			queue = new LinkedList<Node>();
			queue.add(n);
			while(!queue.isEmpty()){
				tmp = queue.remove();
				tIndex = nodeTupleMap.get(tmp);
				tuple = tuples.get(tIndex);
				cEnum = tmp.children();
				cIndex = -1;
				while(cEnum.hasMoreElements()){
					cTmp = cEnum.nextElement();
					cIndex++;
					tmpTuple = new TreeTuple(tIndex, cIndex, nodeIndexMap.get(cTmp), cTmp.getNodeType());
					nodeTupleMap.put(cTmp, tuples.size());
					tuples.add(tmpTuple);
					queue.add(cTmp);
				}
			}
		}
		return tuples;
	}
	
	public static List<TreeTuple> createTuplesForSingleMethod(List<Node> markedNodes, Map<Node, Integer> nodeIndexMap){
		List<TreeTuple> tuples = new ArrayList<TreeTuple>();
		TreeTuple tuple = null, tmpTuple = null;
		Queue<Node> queue = null;
		Node tmp = null, cTmp = null;
		int cIndex = -1, tIndex = -1;;
		Map<Node, Integer> nodeTupleMap = new HashMap<Node, Integer>();
		Enumeration<Node> cEnum = null;
		if(markedNodes.size() == 1 && markedNodes.get(0).getNodeType() == ASTNode.METHOD_DECLARATION){			
			Enumeration<Node> childEnum = markedNodes.get(0).children();
			markedNodes = new ArrayList<Node>();
			while(childEnum.hasMoreElements()){
				markedNodes.add(childEnum.nextElement());
			}
		}
		for(Node n : markedNodes){
			tuple = new TreeTuple(NOTCARE_INDEX, NOTCARE_CINDEX, nodeIndexMap.get(n), n.getNodeType());
			nodeTupleMap.put(n, tuples.size());
			tuples.add(tuple);			
			queue = new LinkedList<Node>();
			queue.add(n);
			while(!queue.isEmpty()){
				tmp = queue.remove();
				tIndex = nodeTupleMap.get(tmp);
				tuple = tuples.get(tIndex);
				cEnum = tmp.children();
				cIndex = -1;
				while(cEnum.hasMoreElements()){
					cTmp = cEnum.nextElement();
					cIndex++;
					tmpTuple = new TreeTuple(tIndex, cIndex, nodeIndexMap.get(cTmp), cTmp.getNodeType());
					nodeTupleMap.put(cTmp, tuples.size());
					tuples.add(tmpTuple);
					queue.add(cTmp);
				}
			}
		}
		return tuples;
	}
	
	public int getNodeIndex(){
		return nodeIndex;
	}
	
	public int getNodeType(){
		return nodeType;
	}
	
	public static List<List<TreeTuple>> split(List<TreeTuple> tuples){
		List<List<TreeTuple>> result = new ArrayList<List<TreeTuple>>();
		List<TreeTuple> tupleGroup = null;
		TreeTuple tuple = null;
		for(int i = 0; i < tuples.size(); i++){
			tuple = tuples.get(i);
			if(tuple.relativeIndex == NOTCARE_INDEX){
				tupleGroup = new ArrayList<TreeTuple>();
				result.add(tupleGroup);				
			}
			tupleGroup.add(tuple);
		}
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + nodeType;
		result = prime * result + relativeChildIndex;
		result = prime * result + relativeIndex;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TreeTuple other = (TreeTuple) obj;
		if (nodeType != other.nodeType)
			return false;
		if (relativeChildIndex != other.relativeChildIndex)
			return false;
		if (relativeIndex != other.relativeIndex)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TreeTuple [relativeIndex=" + relativeIndex
				+ ", relativeChildIndex=" + relativeChildIndex + ", nodeType="
				+ nodeType + "]";
	}
}
