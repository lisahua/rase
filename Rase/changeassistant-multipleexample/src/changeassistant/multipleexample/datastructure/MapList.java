package changeassistant.multipleexample.datastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.MethodInvocation;

import changeassistant.peers.SourceCodeRange;

public class MapList {

	List<Map<String, String>> specificToUnifiedList = null;
	Map<String, List<SourceCodeRange>> methodInvocationMap = null;
	int size;
	
	public MapList(int size){
		this.size = size;
		specificToUnifiedList = new ArrayList<Map<String, String>>();
		for(int i = 0; i < size; i++){
			specificToUnifiedList.add(new HashMap<String, String>());
		}
		methodInvocationMap = new HashMap<String, List<SourceCodeRange>>();
	}
	
	public int size(){
		return size;
	}
	
	public MapList(List<Map<String, String>> sTouList){
		this(sTouList.size());
		for(int i = 0; i < sTouList.size(); i++){
			specificToUnifiedList.get(i).putAll(sTouList.get(i));
		}
		methodInvocationMap = new HashMap<String, List<SourceCodeRange>>();
	}
	
	public void appendMethodInvocationMap(String mName, List<SourceCodeRange> scrs){
		methodInvocationMap.put(mName, scrs);
	}
	public Map<String, List<SourceCodeRange>> getMethodInvocationMap(){
		return methodInvocationMap;
	}
	
	public Map<String, String> get(int i){
		return specificToUnifiedList.get(i);
	}
	
	public MapList deepCopy(){
		MapList mapList = new MapList(size);
		List<Map<String, String>> newMapList = new ArrayList<Map<String, String>>();		
		for(int i = 0; i < size; i++){
			newMapList.add(new HashMap<String, String>(specificToUnifiedList.get(i)));
		}
		mapList.specificToUnifiedList = newMapList;
		Map<String, List<SourceCodeRange>> newMiMap = new HashMap<String, List<SourceCodeRange>>();
		for(Entry<String, List<SourceCodeRange>> entry : methodInvocationMap.entrySet()){
			newMiMap.put(entry.getKey(), new ArrayList<SourceCodeRange>(entry.getValue()));
		}
		mapList.methodInvocationMap = newMiMap;
		return mapList;
	}
	
	public void putMultiOneMap(List<String> specificList, String unified){
		for(int i = 0; i < specificToUnifiedList.size(); i++){
			specificToUnifiedList.get(i).put(specificList.get(i), unified);
		}
	}
	
	public void removeEntriesByValue(String value){
		for(Map<String, String> map : specificToUnifiedList){
			for(Entry<String, String> entry : map.entrySet()){
				if(entry.getValue().equals(value)){
					map.remove(entry.getKey());
					break;
				}
			}
		}
	}
	
	public Map<String, SourceCodeRange> getMImap(int i){
		Map<String, SourceCodeRange> map = new HashMap<String, SourceCodeRange>();
		for(Entry<String, List<SourceCodeRange>> entry : methodInvocationMap.entrySet()){
			map.put(entry.getKey(), entry.getValue().get(i));
		}
		return map;
	}
	
	@Override
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		for(Map<String, String> map : specificToUnifiedList){
			buffer.append("[");
			for(Entry<String, String> entry : map.entrySet()){
				buffer.append(entry.getKey()).append("--").append(entry.getValue()).append(",");
			}
			buffer.append("]");
		}
		return buffer.toString();
	}
}
