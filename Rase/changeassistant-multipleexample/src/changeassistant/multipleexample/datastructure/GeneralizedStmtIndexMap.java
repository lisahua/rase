package changeassistant.multipleexample.datastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;

public class GeneralizedStmtIndexMap {

	List<List<SimpleASTNode>> generalizedStmts = null;
	List<List<Integer>> generalizedIndexesList = null;
	
	public GeneralizedStmtIndexMap(){
		generalizedStmts = new ArrayList<List<SimpleASTNode>>();
    	generalizedIndexesList = new ArrayList<List<Integer>>();
	}
	
	public GeneralizedStmtIndexMap deepCopy(){
		GeneralizedStmtIndexMap result = new GeneralizedStmtIndexMap();
		result.generalizedIndexesList.addAll(generalizedIndexesList);
		result.generalizedStmts.addAll(generalizedStmts);
		return result;
	}
	
	public List<Integer> getPeers(int i){
		return generalizedIndexesList.get(i);
	}
	
	/**
	 * Get the first dimension of the indexes coming from the same method declaration
	 * @param i
	 * @return
	 */
	public List<Integer> getIndexes(int i){
		List<Integer> indexes = new ArrayList<Integer>();
		for(List<Integer> tmpIndexes : generalizedIndexesList){
			indexes.add(tmpIndexes.get(i));
		}
		return indexes;
	}
	
	public Map<Integer, List<SimpleASTNode>> getIndexStmtMap(int index){
		Map<Integer, List<SimpleASTNode>> map = new HashMap<Integer, List<SimpleASTNode>>();
		for(int i = 0; i < generalizedIndexesList.size(); i++){
			map.put(generalizedIndexesList.get(i).get(index), generalizedStmts.get(i));
		}
		return map;
	}
	
	public List<List<SimpleASTNode>> getGeneralizedStmts(){
		return generalizedStmts;
	}
	
	public void put(List<Integer> indexes, List<SimpleASTNode> stmts){
		generalizedStmts.add(stmts);
		generalizedIndexesList.add(indexes);
	}
	
	public void putAll(GeneralizedStmtIndexMap map){
		generalizedStmts.addAll(map.generalizedStmts);
		generalizedIndexesList.addAll(map.generalizedIndexesList);
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		List<Integer> indexes = null;
		List<SimpleASTNode> sNodes = null;
		for(int i = 0; i < generalizedStmts.size(); i++){
			indexes = generalizedIndexesList.get(i);
			sNodes = generalizedStmts.get(i);
			buffer.append("\n" + i + "---\n");
			buffer.append("[");
			for(int j = 0; j < indexes.size(); j++){
				buffer.append(indexes.get(j)).append(",");
			}
			buffer.append("][");
			for(int j = 0; j < sNodes.size(); j++){
				buffer.append(sNodes.get(j));
			}
			buffer.append("]");
		}
		return buffer.toString();
	}
	
	public boolean isEmpty(){
		return generalizedStmts.isEmpty();
	}
}
