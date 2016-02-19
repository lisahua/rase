//package changeassistant.multipleexample.partition;
//
//import java.util.ArrayList;
//import java.util.BitSet;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//
//import changeassistant.multipleexample.partition.datastructure.EditInCommonGroup;
//import changeassistant.versions.comparison.MethodModification;
//import changeassistant.versions.treematching.edits.AbstractTreeEditOperation;
//
//public class ParitionHelper1 {
//
//	private String getDigestType(List<AbstractTreeEditOperation> edits){
//		StringBuffer buffer = new StringBuffer();
//		for(AbstractTreeEditOperation edit : edits){
//			switch(edit.getOperationType()){
//			case INSERT:buffer.append("I");
//						break;
//			case DELETE:buffer.append("D");
//						break;
//			case UPDATE:buffer.append("U");
//						break;
//			case MOVE:	buffer.append("M");
//						break;
//			}
//		}
//		return buffer.toString();
//	}
//	
//	public List<EditInCommonGroup> partitionBasedOnType(List<EditInCommonGroup> groups){
//		List<EditInCommonGroup> editGroups = new ArrayList<EditInCommonGroup>();
//		List<List<AbstractTreeEditOperation>> editsList = null;
//		List<AbstractTreeEditOperation> edits = null;
//		BitSet bitSet = null;
//		String digest = null;
//		EditInCommonGroup eGroup = null;
//		Map<String, BitSet> groupMap = new HashMap<String, BitSet>();
//		for(EditInCommonGroup group : groups){
//			editsList = group.getEditsList();
//			groupMap.clear();
//			for(int i = 0; i < editsList.size(); i++){
//				edits = editsList.get(i);
//				digest = getDigestType(edits);
//				bitSet = groupMap.get(digest);
//				if(bitSet == null){
//					bitSet = new BitSet();
//					groupMap.put(digest, bitSet);
//				}
//				bitSet.set(i);
//			}
//			for(Entry<String, BitSet> entry : groupMap.entrySet()){
//				bitSet = entry.getValue();
//				if(bitSet.cardinality() >= 2){
//					eGroup = new EditInCommonGroup(bitSet, editsList, group.getMMList());
//					editGroups.add(eGroup);
//				}
//			}
//		}
//		return editGroups;
//	}
//}
