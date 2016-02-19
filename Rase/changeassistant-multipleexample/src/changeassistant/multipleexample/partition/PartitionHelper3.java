//package changeassistant.multipleexample.partition;
//
//import java.util.ArrayList;
//import java.util.Collections;
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
//import org.eclipse.jdt.core.dom.ASTNode;
//
//import changeassistant.changesuggestion.expression.representation.ASTExpressionTransformer;
//import changeassistant.multipleexample.partition.datastructure.EditInCommonGroup;
//import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
//import changeassistant.versions.comparison.MethodModification;
//import changeassistant.versions.treematching.edits.AbstractTreeEditOperation;
//import changeassistant.versions.treematching.edits.InsertOperation;
//
//public class PartitionHelper3 {
//
//	public List<EditInCommonGroup> generalizeEdits(List<EditInCommonGroup> groups){
//		List<EditInCommonGroup> editGroups = new ArrayList<EditInCommonGroup>();
//		List<EditInCommonGroup> tempGroups = null;
//		for(EditInCommonGroup group : groups){
//			tempGroups = generalize(group);
//			for(EditInCommonGroup tempGroup : tempGroups){
//				if(tempGroup.parseConstraints())
//					editGroups.add(tempGroup);
//			}
//		}
//		return editGroups;
//	}
//	
//	private List<EditInCommonGroup> generalize(EditInCommonGroup group){
//		List<EditInCommonGroup> editGroups = new ArrayList<EditInCommonGroup>();
//		editGroups.add(group);
//		List<List<AbstractTreeEditOperation>> editsList = group.getEditsList();
//		
//		int numOfEdits = editsList.size();
//		for(int i = 0; i < numOfEdits; i++){
//			editGroups = groupBasedOnSingleEdit(i, numOfEdits, editGroups);
//		}
//		return editGroups;
//	}
//	
//	private List<SimpleASTNode> getCommon(List<ASTNode> exprs1, List<ASTNode> exprs2){
//		List<SimpleASTNode> sASTnodes = new ArrayList<SimpleASTNode>();
//		SimpleASTNode sASTNode = null;
//		for(int i = 0; i < exprs1.size(); i++){
//			sASTNode = getCommon(exprs1.get(i), exprs2.get(i));
//			if(sASTNode == null)
//				return null;
//			sASTnodes.add(sASTNode);
//		}
//		return sASTnodes;
//	}
//	
//	private List<SimpleASTNode> getCommon2(List<SimpleASTNode> sExprs1, List<SimpleASTNode> sExprs2){
//		List<SimpleASTNode> sASTnodes = new ArrayList<SimpleASTNode>();
//		SimpleASTNode sASTNode = null;
//		for(int i = 0; i < sExprs1.size(); i++){
//			sASTNode = getCommon(sExprs1.get(i), sExprs2.get(i));
//			if(sASTNode == null)
//				return null;
//			sASTnodes.add(sASTNode);
//		}
//		return sASTnodes;
//	}
//	
//	private SimpleASTNode getCommon(SimpleASTNode s1, SimpleASTNode s2){
//		SimpleASTNode commonPart = null;
//		SimpleASTNode commonTree = null;
//		Enumeration<SimpleASTNode> childEnum1, childEnum2;
//		SimpleASTNode node3Parent = null;
//		SimpleASTNode node1 = null, node2 = null, node3 = null, child1, child2;
//		commonTree = parseCommon(s1, s2);
//		if(commonTree.equals(s1)){
//			return new SimpleASTNode(s1);
//		}
//		if(!sameType(commonTree)){
//			return null;
//		}
//		Queue<SimpleASTNode> queue1 = new LinkedList<SimpleASTNode>();
//		Queue<SimpleASTNode> queue2 = new LinkedList<SimpleASTNode>();
//		Queue<SimpleASTNode> queue3 = new LinkedList<SimpleASTNode>();
//		queue1.add(s1);
//		queue2.add(s2);
//		queue3.add(commonTree);
//		while(!queue1.isEmpty()){
//			node1 = queue1.remove();
//			node2 = queue2.remove();
//			node3 = queue3.remove();
//			if(node1.getChildCount() != node2.getChildCount()){
//				continue; //this subtree cannot be parsed further for common part
//			}
//			childEnum1 = node1.children();
//			childEnum2 = node2.children();
//			while(childEnum1.hasMoreElements()){
//				child1 = childEnum1.nextElement();
//				child2 = childEnum2.nextElement();
//				if(child1.equals(child2) && !child1.getStrValue().equals(SimpleASTNode.LIST_LITERAL)){
//					commonPart = new SimpleASTNode(child1);
//				}else{
//					commonPart = parseCommon(child1, child2);
//					if(sameType(commonPart)){
//						queue1.add(child1);
//						queue2.add(child2);
//						queue3.add(commonPart);
//					}
//				}
//				node3.add(commonPart);
//			}
//		}
//		
//		Enumeration<SimpleASTNode> dEnum = commonTree.depthFirstEnumeration();
//		Set<SimpleASTNode> cached = new HashSet<SimpleASTNode>();
//		StringBuffer buffer = null;
//		while(dEnum.hasMoreElements()){
//			node3 = dEnum.nextElement();
//			if(node3.hasMark()){
//				node3Parent = (SimpleASTNode)node3.getParent();
//				if(node3Parent != null && cached.add(node3Parent)
//						&& !node3Parent.getStrValue().equals(SimpleASTNode.LIST_LITERAL)){
//					node3Parent.setStrValue(node3Parent.constructStrValue());
//				}
//				node3.clearMarked();
//			}
//		}
//
//		return commonTree;
//	}
//	
//	private SimpleASTNode getCommon(ASTNode expr1, ASTNode expr2){
//		SimpleASTNode commonTree = null;
//		
//		SimpleASTCreator creator = new SimpleASTCreator();
//	
//		if(expr1.toString().equals(expr2.toString())){
//			commonTree = creator.createSimpleASTNode(expr1);
//			return commonTree;
//		}
//			
//		SimpleASTNode left = creator.createSimpleASTNode(expr1);
//		SimpleASTNode right = creator.createSimpleASTNode(expr2);
//		
//		return getCommon(left, right);
//	}
//	
//	private List<ASTNode> getExprs(AbstractTreeEditOperation edit){
//		if(edit instanceof InsertOperation){
//			return ((InsertOperation)edit).getASTNodesToInsert();
//		}
//		return edit.getNode().getASTExpressions2();
//	}
//	
//	private List<EditInCommonGroup> groupBasedOnSingleEdit(int i, int numOfEdits, 
//			List<EditInCommonGroup> editGroups){
//		List<EditInCommonGroup> newGroups = new ArrayList<EditInCommonGroup>();
//		Map<List<SimpleASTNode>, Set<Integer>> gEditsMap = new HashMap<List<SimpleASTNode>, Set<Integer>>();
//		List<List<AbstractTreeEditOperation>> editsList = null;
//		List<ASTNode> exprs1 = null, exprs2 = null;
//		List<SimpleASTNode> commonExprs = null;
//		List<SimpleASTNode> keyList1= null, keyList2 = null;
//		Set<Integer> instances = null;
//		List<MethodModification> mmList = null;
//		boolean flag = false;
//		for(EditInCommonGroup group : editGroups){
//			gEditsMap.clear();
//			editsList = group.getEditsList();
//			mmList = group.getMMList();
//			for(int j = 0; j < group.size() - 1; j++){
//				exprs1 = getExprs(editsList.get(j).get(i));
//				for(int k = j + 1; k < group.size(); k++){
//					exprs2 = getExprs(editsList.get(k).get(i));
//					commonExprs = getCommon(exprs1, exprs2);
//					if(commonExprs == null)
//						continue;
//					instances = gEditsMap.get(commonExprs);
//					if(instances == null){
//						instances = new HashSet<Integer>();
//						gEditsMap.put(commonExprs, instances);
//					}
//					instances.add(j);
//					instances.add(k);
//				}
//			}
//			flag = false;
//			while(gEditsMap.size() > 1){
//				flag = false;
//				List<List<SimpleASTNode>> keyLists = new ArrayList<List<SimpleASTNode>>(gEditsMap.keySet());
//				for(int j = 0; j < keyLists.size() - 1; j++){
//					keyList1 = keyLists.get(j);
//					for(int k = j + 1; k < keyLists.size(); k++){
//						keyList2 = keyLists.get(k);
//						commonExprs = getCommon2(keyList1, keyList2);
//						if(commonExprs != null){
//							if(gEditsMap.get(commonExprs) != null){
//								gEditsMap.get(commonExprs).addAll(gEditsMap.get(keyList1));
//								gEditsMap.get(commonExprs).addAll(gEditsMap.get(keyList2));
//								gEditsMap.remove(keyList1);
//								gEditsMap.remove(keyList2);
//							}else{
//								gEditsMap.put(commonExprs, gEditsMap.get(keyList1));
//								gEditsMap.get(commonExprs).addAll(gEditsMap.get(keyList2));
//							}
//							flag = true;
//							break;
//						}
//					}
//					if(flag){
//						break;
//					}
//				}
//				if(!flag)
//					break;
//			}
//			if(gEditsMap.size() > 1){
//				Set<Integer> value = null;
//				for(Entry<List<SimpleASTNode>, Set<Integer>> entry : gEditsMap.entrySet()){
//					value = entry.getValue();
//					List<List<AbstractTreeEditOperation>> newEditsList = new ArrayList<List<AbstractTreeEditOperation>>();
//					List<MethodModification> newMMList = new ArrayList<MethodModification>();
//					for(Integer index : value){
//						newEditsList.add(editsList.get(index));
//						newMMList.add(mmList.get(index));
//					}
//					EditInCommonGroup newGroup = new EditInCommonGroup(newEditsList, newMMList);
//					newGroup.addCommonTrees(entry.getKey());
//				}
//			}else{
//				group.addCommonTrees(gEditsMap.keySet().iterator().next());
//				newGroups.add(group);
//			}
//		}
//		return newGroups;
//	}
//	
//	
//	private boolean sameType(SimpleASTNode commonTree){
//		return commonTree.getNodeType() != SimpleASTNode.UNDECIDED_NODE_TYPE;
//	}
//	
//	private SimpleASTNode parseCommon(SimpleASTNode left, SimpleASTNode right){
//		SimpleASTNode commonPart = new SimpleASTNode(
//				left.getNodeType(), left.getStrValue(), left.getScr().startPosition, left.getScr().length);
//		if(right.getNodeType() != left.getNodeType()){
//			commonPart.setNodeType(SimpleASTNode.UNDECIDED_NODE_TYPE);
//		}
//		if(!right.getStrValue().equals(left.getStrValue())){
//			commonPart.setStrValue(SimpleASTNode.UNDECIDED_STR_VALUE);
//			commonPart.setGeneral();
//			commonPart.setMarked();
//		}
//		return commonPart;
//	}
//}
