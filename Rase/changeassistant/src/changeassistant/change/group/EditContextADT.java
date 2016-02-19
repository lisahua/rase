//package changeassistant.change.group;
//
//import java.util.HashSet;
//import java.util.Set;
//
//import changeassistant.peers.comparison.Node;
//import changeassistant.peers.comparison.Node.EDITED_TYPE;
//
//public class EditContextADT {
//	public EDITED_TYPE et;	
//	public Set<Node> changedNodes, originalDataDependingNodes, originalControlDependingNodes,
//	          newDataDependingNodes, newControlDependingNodes;	
//	public EditContextADT(Node node, EDITED_TYPE et, Set<Node> dataDependingNodes, 
//			Set<Node> controlDependingNodes){
//		this.et = et;
//		this.changedNodes = new HashSet<Node>();
//		changedNodes.add(node);
//		switch(et){
//		case INSERTED: {
//			this.newControlDependingNodes = controlDependingNodes;
//			this.newDataDependingNodes = dataDependingNodes;
//			this.originalControlDependingNodes = new HashSet<Node>();
//			this.originalDataDependingNodes = new HashSet<Node>();
//		}break;
//		case DELETED: {
//			this.originalControlDependingNodes = controlDependingNodes;
//			this.originalDataDependingNodes = dataDependingNodes;
//			this.newControlDependingNodes = new HashSet<Node>();
//			this.newDataDependingNodes = new HashSet<Node>();
//		}break;
//		case UPDATED: {
//			this.originalControlDependingNodes = controlDependingNodes;
//			this.originalDataDependingNodes = dataDependingNodes;
//			this.newControlDependingNodes = new HashSet<Node>(controlDependingNodes);
//			this.newDataDependingNodes = new HashSet<Node>(dataDependingNodes);
//		}break;
//		case MOVED: {
//			this.originalControlDependingNodes = new HashSet<Node>();
//			this.originalDataDependingNodes = new HashSet<Node>();
//			this.newControlDependingNodes = controlDependingNodes;
//			this.newDataDependingNodes = dataDependingNodes;
//		}
//		}
//	}
//	
//	public EditContextADT(Node node, EDITED_TYPE et, Set<Node> originalDataDependingNodes,
//			Set<Node> originalControlDependingNodes, Set<Node> newDataDependingNodes,
//			Set<Node> newControlDependingNodes){
//		this.originalControlDependingNodes = originalControlDependingNodes;
//		this.originalDataDependingNodes = originalDataDependingNodes;
//		this.newControlDependingNodes = newControlDependingNodes;
//		this.newDataDependingNodes = newDataDependingNodes;
//	}
//	
//	public Set<Node> lookforChangedNodesDependingOn(){
//		Set<Node> nodes = new HashSet<Node>();
//		nodes.addAll(lookforChangedNodesDependingOn(originalControlDependingNodes));
//		nodes.addAll(lookforChangedNodesDependingOn(originalDataDependingNodes));
//		nodes.addAll(lookforChangedNodesDependingOn(newControlDependingNodes));
//		nodes.addAll(lookforChangedNodesDependingOn(newDataDependingNodes));
//		return nodes;
//	}
//	
//	public Set<Node> lookforNodesDependingOn(){
//		Set<Node> nodes = new HashSet<Node>();
//		nodes.addAll(originalControlDependingNodes);
//		nodes.addAll(originalDataDependingNodes);
//		nodes.addAll(newControlDependingNodes);
//		nodes.addAll(newDataDependingNodes);
//		return nodes;
//	}
//	
//	public Set<Node> lookforChangedNodesDependingOn(Set<Node> nodeSet){
//		Set<Node> result = new HashSet<Node>();
//		for(Node node : nodeSet){
//			if(node.getEDITED_TYPE() != Node.EDITED_TYPE.NONE_EDIT){
//				result.add(node);
//			}
//		}
//		return result;
//	}
//}
