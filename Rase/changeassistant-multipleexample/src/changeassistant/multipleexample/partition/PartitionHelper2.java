//package changeassistant.multipleexample.partition;
//
//import java.util.ArrayList;
//import java.util.BitSet;
//import java.util.Collection;
//import java.util.Enumeration;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.Queue;
//import java.util.Set;
//
//import changeassistant.multipleexample.partition.datastructure.EditInCommonGroup;
//import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
//import changeassistant.peers.comparison.Node;
//import changeassistant.versions.comparison.MethodModification;
//import changeassistant.versions.treematching.edits.AbstractTreeEditOperation;
//
//public class PartitionHelper2 {
//	
//	private BitSet editedAndContextual;
//	
//	private BitSet all;
//	
//	public PartitionHelper2(){
//		editedAndContextual = new BitSet();
//		editedAndContextual.set(1, 2, true);
//		all = new BitSet();
//		all.set(0, 2, true);
//	}
//	
//	public List<EditInCommonGroup> partitionBasedOnStructure(List<EditInCommonGroup> groups){
//		List<EditInCommonGroup> editGroups = new ArrayList<EditInCommonGroup>();
//		for(EditInCommonGroup group : groups){
//			editGroups.addAll(partition(group));
//		}
//		return editGroups;
//	}
//	
//	private List<EditInCommonGroup> partition(EditInCommonGroup group){
//		List<EditInCommonGroup> editGroups = new ArrayList<EditInCommonGroup>();
//		List<List<AbstractTreeEditOperation>> newEditsList = null;
//		
//		List<MethodModification> mmList = group.getMMList();
//		List<List<AbstractTreeEditOperation>> editsList = group.getEditsList();
//		
//		Map<List<BitSet>, List<MethodModification>> map = 
//			mapIndexesWithMMs(mmList, editsList);
//		
//		for(List<MethodModification> mms : map.values()){
//			//to judge whether this is a systematic change based on the threshold 2 for supporting instances
//			if(mms.size() < 2)
//				continue;
//			newEditsList = new ArrayList<List<AbstractTreeEditOperation>>();
//			for(MethodModification mm : mms){
//				newEditsList.add(editsList.get(mmList.indexOf(mm)));
//			}
//			editGroups.add(new EditInCommonGroup(newEditsList, mms));
//		}
//		
//		return editGroups;
//	}
//	
//	private void labelTree(AbstractTreeEditOperation op, 
//			SimpleTreeNode sTree, int j){
//		SimpleTreeNode sNode = null;
//		switch(op.getOperationType()){
//		case INSERT: sNode = sTree.lookforNodeBasedOnRange(op.getParentNode());
//					 if(sNode != null){
//						 sNode.setType(SimpleTreeNode.CONTEXTUAL);
//						 sNode.getEditIndexes().set(j);
//					 }else{
//						 List<Integer> parentEditIndexes = op.getParentNode().getEditIndexes();
//						 labelDescendantInsert(sTree, parentEditIndexes, j);
//					 }
//			break;
//		case DELETE: 
//		case UPDATE:
//		case MOVE:	 sNode = sTree.lookforNodeBasedOnRange(op.getNode());
//					 if(sNode != null){
//						 sNode.setType(SimpleTreeNode.EDITED);
//						 sNode.getEditIndexes().set(j);
//					 }
//			break;
//		}
//	}
//	
//	private void labelDescendantInsert(SimpleTreeNode sTree,
//			List<Integer> parentEditIndexes, int j){
//		Enumeration<SimpleTreeNode> enumeration = sTree.breadthFirstEnumeration();
//		BitSet parentBitset = new BitSet();
//		for(Integer index : parentEditIndexes){
//			parentBitset.set(index);
//		}
//		SimpleTreeNode sNode = null;
//		while(enumeration.hasMoreElements()){
//			sNode = enumeration.nextElement();
//			if(sNode.getEditIndexes().intersects(parentBitset)){
//				sNode.getEditIndexes().set(j);//to append the insertion
//				sNode.setType(SimpleTreeNode.CONTEXTUAL);
//			}
//		}
//	}
//	
//	private Map<List<BitSet>, List<MethodModification>> mapIndexesWithMMs(
//			List<MethodModification> mmList, List<List<AbstractTreeEditOperation>> editsList){
//		List<AbstractTreeEditOperation> edits = null;
//		SimpleTreeNode sTree = null;
//		MethodModification mm = null;
//		List<MethodModification> knownMMs = null;
//		List<BitSet> indexGroups = null;
//		
//		Map<List<BitSet>, List<MethodModification>> map = 
//			new HashMap<List<BitSet>, List<MethodModification>>();
//		for(int i = 0; i < mmList.size(); i++){
//			mm = mmList.get(i);
//			sTree = new SimpleTreeNode(
//						(Node)mm.getEdits().get(0).getParentNode().getRoot(), true);
//			edits = editsList.get(i);
//			for(int j = 0; j < edits.size(); j++){
//				labelTree(edits.get(j), sTree, j);
//			}
//			indexGroups = trim(sTree);
//			knownMMs = map.get(indexGroups);
//			if(knownMMs == null){
//				knownMMs = new ArrayList<MethodModification>();
//				map.put(indexGroups, knownMMs);
//			}
//			knownMMs.add(mm);
//		}
//		return map;
//	}
//	
//	private List<BitSet> trim(SimpleTreeNode sTree){
//		SimpleTreeNode result = trimDownstream(sTree);
//		Map<SimpleTreeNode, BitSet> map = trimUpstream(result);
////		Map<BitSet, SimpleTreeNode> resultMap = new HashMap<BitSet, SimpleTreeNode>();
////		for(Entry<SimpleTreeNode, BitSet> entry : map.entrySet()){
////			resultMap.put(entry.getValue(), entry.getKey());
////		}
//		return new ArrayList(map.values());
//	}
//	
//	private SimpleTreeNode trimDownstream(SimpleTreeNode sTree){
//		SimpleTreeNode result = new SimpleTreeNode(sTree);
//		Enumeration<SimpleTreeNode> enumeration = result.depthFirstEnumeration();
//
//		SimpleTreeNode sNode = null;
//		SimpleTreeNode sTemp = null;
//		
//		//to trim all irrelevant downstream nodes until reaching a contextual or edited node
//		while(enumeration.hasMoreElements()){
//			sNode = enumeration.nextElement();
//			if(!sNode.getTypes().intersects(all)){
//				if(sNode.getParent() != null){
//					sNode.removeFromParent();
//				}
//			}else{
//				sTemp = (SimpleTreeNode)sNode.getParent();
//				while(sTemp != null){
//					if(sTemp.getTypes().intersects(all)){
//						break;
//					}else{
//						sTemp.getTypes().set(SimpleTreeNode.RELEVANT);
//					}
//					sTemp = (SimpleTreeNode)sTemp.getParent();
//				}
//			}
//		}
//		return result;
//	}
//	
//	private Map<SimpleTreeNode, BitSet> trimUpstream(SimpleTreeNode sTree){
//		Map<SimpleTreeNode, BitSet> map = new HashMap<SimpleTreeNode, BitSet>();
//		Enumeration<SimpleTreeNode> childEnumeration = null;
//		Queue<SimpleTreeNode> queue = new LinkedList<SimpleTreeNode>();
//		SimpleTreeNode sRoot = null;
//		SimpleTreeNode sTemp = null;
//		SimpleTreeNode child = null;
//		int editedChildCount = 0;
//		BitSet tempBitSet = null;
//		Set<SimpleTreeNode> relevantChildSet = null;
//		Set<SimpleTreeNode> childSet = null;
//		queue.add(sTree);
//		while(!queue.isEmpty()){
//			sTemp = queue.remove();
//			if(!sTemp.getTypes().intersects(editedAndContextual)){//try to partition if this is a relevant node
//				childEnumeration = sTemp.children();
//				editedChildCount = 0;
//				relevantChildSet = new HashSet<SimpleTreeNode>();
//				childSet = new HashSet<SimpleTreeNode>();
//				while(childEnumeration.hasMoreElements()){
//					child = childEnumeration.nextElement();
//					if(child.getTypes().intersects(editedAndContextual)){
//						editedChildCount++;
//					}else{//relevant node
//						relevantChildSet.add(child);
//					}
//					childSet.add(child);
//				}
//				if(editedChildCount < 2){
//					for(SimpleTreeNode temp : childSet){
//						temp.setParent(null);
//						queue.add(temp);
//					}
//				}else{
//					queue.addAll(childSet);
//					for(SimpleTreeNode temp : relevantChildSet){
//						temp.setParent(null);
//					}
//				}
//			}else{
//				sRoot = (SimpleTreeNode)sTemp.getRoot();
//				tempBitSet = map.get(sRoot);
//				if(tempBitSet == null){
//					tempBitSet = new BitSet();
//					map.put(sRoot, tempBitSet);
//				}
//				tempBitSet.or(sTemp.getEditIndexes());
//			}
//		}
//		return map;
//	}
//}
