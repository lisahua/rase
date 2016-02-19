package changeassistant.multipleexample.datastructure;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import changeassistant.changesuggestion.expression.representation.ASTExpressionTransformer;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;

public class QueueList {
	List<Queue<SimpleASTNode>> queueList = null;
	int size = 0;
	public QueueList(int size){
		this.size = size;
		queueList = new ArrayList<Queue<SimpleASTNode>>();
		for(int i = 0; i < size; i ++){
			queueList.add(new LinkedList<SimpleASTNode>());
		}
	}
	public void addElems(List<SimpleASTNode> exprPeers){
		for(int i = 0; i < size; i++){
			queueList.get(i).add(exprPeers.get(i));
		}
	}
	
	public static boolean allHasChildCount(List<SimpleASTNode> nodes, int childCount){
		boolean result = true;
		for(int i = 0; i < nodes.size(); i++){
			if(nodes.get(i).getChildCount() != childCount){
				result = false;
				break;
			}
		}
		return result;
	}
	
	public static boolean allHasNodeType(List<SimpleASTNode> nodes, int nodeType){
		boolean result = true;
		for(int i = 0; i < nodes.size(); i++){
			if(nodes.get(i).getNodeType() != nodeType){
				result = false;
				break;
			}
		}
		return result;
	}
	
	public static boolean allHasValue(List<SimpleASTNode> nodes, String strValue){
		boolean result = true;
		for(int i = 0; i < nodes.size(); i++){
			if(!nodes.get(i).getStrValue().equals(strValue)){
				result = false;
				break;
			}
		}
		return result;
	}
	
	public static List<Enumeration<SimpleASTNode>> getChildEnums(List<SimpleASTNode> nodes){
		List<Enumeration<SimpleASTNode>> enumList = new ArrayList<Enumeration<SimpleASTNode>>();
		for(SimpleASTNode n : nodes){
			enumList.add(n.children());
		}
		return enumList;
	}
	
	public static List<SimpleASTNode> getNextChildList(List<Enumeration<SimpleASTNode>> childEnums){
		List<SimpleASTNode> result = new ArrayList<SimpleASTNode>();
		for(Enumeration<SimpleASTNode> cEnum : childEnums){
			result.add(cEnum.nextElement());
		}
		return result;
	}
	
	public static List<String> getNames(List<SimpleASTNode> nodes){
		List<String> names = new ArrayList<String>();
		for(SimpleASTNode n : nodes){
			names.add(n.getStrValue());
		}
		return names;
	}
	
	public static List<Integer> getNodeTypes(List<SimpleASTNode> nodes){
		List<Integer> nodeTypes = new ArrayList<Integer>();
		for(SimpleASTNode n : nodes){
			nodeTypes.add(n.getNodeType());
		}
		return nodeTypes;
	}
	
	public static List<String> constructListLiteralNames(List<SimpleASTNode> nodes){
		List<String> names = new ArrayList<String>();
		for(SimpleASTNode n : nodes){
			StringBuffer buffer = new StringBuffer();
			buffer.append(ASTExpressionTransformer.ARGS_PRE);
			for(int i = 0; i < n.getChildCount(); i++){
				buffer.append(n.getChildAt(i));
			}
			names.add(buffer.toString());
		}
		return names;
	}
	
	public Queue<SimpleASTNode> getFirst(){
		return queueList.get(0);
	}
	public List<Queue<SimpleASTNode>> getRest(){
		return queueList.subList(1, size);
	}
	/*
	public SimpleASTNode peekFirst(){
		return queueList.get(0).peek();
	}
	*/
	public List<SimpleASTNode> removeRestNodes(){
		List<SimpleASTNode> result = new ArrayList<SimpleASTNode>();
		for(int i = 1; i < size; i++){
			result.add(queueList.get(i).remove());
		}
		return result;
	}
	
	public List<SimpleASTNode> removePeerNodes(){
		List<SimpleASTNode> result = new ArrayList<SimpleASTNode>();
		for(int i = 0; i < size; i ++){
			result.add(queueList.get(i).remove());
		}
		return result;
	}
}
