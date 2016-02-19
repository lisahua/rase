package changeassistant.clonereduction.pdg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import changeassistant.clonereduction.datastructure.FlowLabel;
import changeassistant.clonereduction.pdg.GFlowEdge.CATEGORY;
import changeassistant.crystal.FieldVariable;
import changeassistant.multipleexample.main.Constants;
import changeassistant.peers.SourceCodeRange;
import changeassistant.peers.comparison.Node;

public class Graph {

	private Map<String, FieldVariable> fields = null;
	
	public static enum EDGE_TYPE {
		DEF, CON
	};

	Map<SourceCodeRange, GNode> nodes;

	public Graph(Map<String, FieldVariable> fields) {
		nodes = new HashMap<SourceCodeRange, GNode>();
		this.fields = fields;
	}

	public void addDataEdges(Map<String, Set<Node>> varNodesMap, Node node) {
		String var = null;
		Set<Node> tempNodes = null;
		GNode source = null;
		GNode sink = getOrCreateNode(node.getSourceCodeRange());
		for (Entry<String, Set<Node>> entry : varNodesMap.entrySet()) {
			var = entry.getKey();
			tempNodes = entry.getValue();
			if(tempNodes.isEmpty()){
				sink.addUsedField(var);
//				if(fields.containsKey(var))
//					sink.addUsedField(var);
			}
			for (Node n : tempNodes) {
				source = getOrCreateNode(n.getSourceCodeRange());
				addDefEdge(source, sink, var);
			}
		}
	}

	public void addDataEdges(Node node, Map<String, Set<Node>> varNodesMap) {
		String var = null;
		Set<Node> tempNodes = null;
		GNode source = getOrCreateNode(node.getSourceCodeRange());
		GNode sink = null;
		for (Entry<String, Set<Node>> entry : varNodesMap.entrySet()) {
			var = entry.getKey();
			tempNodes = entry.getValue();
			if(tempNodes.isEmpty() && fields.containsKey(var)){
				source.defLabels.add(var);
			}
			for (Node n : tempNodes) {
				sink = getOrCreateNode(n.getSourceCodeRange());
				addDefEdge(source, sink, var);
			}
		}
	}

	public void addControlEdges(Set<Node> controlNodes, Node node) {
		GNode source = null;
		GNode sink = getOrCreateNode(node.getSourceCodeRange());
		for (Node n : controlNodes) {
			source = getOrCreateNode(n.getSourceCodeRange());
			addConEdge(source, sink);
		}
	}

	public void addFlowEdges(Node node, Map<FlowLabel, Node> labelNodeMap) {
		GNode source = getOrCreateNode(node.getSourceCodeRange());
		GNode sink = null;
		for (Entry<FlowLabel, Node> entry : labelNodeMap.entrySet()) {
			sink = getOrCreateNode(entry.getValue().getSourceCodeRange());
			addFlowEdge(source, sink, entry.getKey());
		}
	}

	private GNode getOrCreateNode(SourceCodeRange range) {
		GNode gnode = nodes.get(range);
		if (gnode == null) {
			gnode = new GNode(range);
			nodes.put(range, gnode);
		}
		return gnode;
	}
	
	public boolean isReturnNode(SourceCodeRange range){
		GNode gnode = nodes.get(range);
		assert gnode != null;
		List<GEdge> outgoings = gnode.outgoings;
		GFlowEdge fEdge = null;
		for (GEdge outgoing : outgoings) {
			if (outgoing instanceof GFlowEdge) {
				fEdge = (GFlowEdge) outgoing;
				if (fEdge.label.category.equals(CATEGORY.RETURN)) {
						return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * throwNodes, returnNodes, and flowNodes are three types of boundary nodes of the code chunk to extract
	 * @param range
	 * @param exceptionTypes
	 * @param throwNodes
	 * @param returnNodes
	 * @param flowNodes
	 */
	public void processLabels(Node node, List<String> exceptionTypes, Set<Node> throwNodes, Set<Node> returnNodes, Set<Node> flowNodes){
		GNode gnode = nodes.get(node.getSourceCodeRange());
		assert gnode != null;
		List<GEdge> outgoings = gnode.outgoings;
		GFlowEdge fEdge = null;
		CATEGORY category = null;
		for (GEdge outgoing : outgoings) {
			if (outgoing instanceof GFlowEdge) {
				fEdge = (GFlowEdge) outgoing;
				category = fEdge.label.category;
				switch(category){
				case THROW:
					exceptionTypes.add(fEdge.label.content);
					throwNodes.add(node);
					break;
				case RETURN:	
					if(node.getStrValue().contains(Constants.RETURN))
						returnNodes.add(node);
					break;
				case NONE:
					flowNodes.add(node);
					break;
				}
			}
		}
	}

	public List<String> getUnhandledExceptions(SourceCodeRange range) {
		GNode gnode = nodes.get(range);
		assert gnode != null;
		List<String> result = new ArrayList<String>();
		List<GEdge> outgoings = gnode.outgoings;
		GFlowEdge fEdge = null;
//		GNode sink = null;
		for (GEdge outgoing : outgoings) {
			if (outgoing instanceof GFlowEdge) {
				fEdge = (GFlowEdge) outgoing;
				if (fEdge.label.category.equals(CATEGORY.THROW)) {
//					sink = fEdge.sink;
//					if (!includedRanges.contains(sink.range)) {
						result.add(fEdge.label.content);
//					}
				}
			}
		}
		return result;
	}

	/**
	 * Algorithm to decide input Terms: if the used term cannot find any local
	 * def.
	 * 
	 * @param range
	 * @param includedRanges
	 * @return
	 */
	public Set<String> getUseWithDefOutsideVars(SourceCodeRange range,
			Set<SourceCodeRange> includedRanges) {
		GNode gnode = nodes.get(range);
		if (gnode == null)
			return null;
		Set<String> result = new HashSet<String>();
		List<GEdge> incomings = gnode.incomings;
		GNode source = null;
		GDefEdge dEdge = null;
		for (GEdge incoming : incomings) {
			if (incoming instanceof GDefEdge) {
				dEdge = (GDefEdge) incoming;
				source = dEdge.source;
				if (!includedRanges.contains(source.range)) {
					result.add(dEdge.label);
				}
			}
		}
		result.addAll(gnode.usedFields);
		return result;
		// Set<String> result = new HashSet<String>();
		// List<GEdge> edges = gnode.incomings;
		// GDefEdge dEdge = null;
		// for (GEdge edge : edges) {
		// if (edge instanceof GDefEdge) {
		// dEdge = (GDefEdge) edge;
		// if (includedRanges.contains(dEdge.source.range))
		// result.add(dEdge.label);
		// }
		// }
		// return result;
	}

	public Set<String> getDefWithoutUseOutsideVars(SourceCodeRange range, 
			Set<SourceCodeRange> includedRanges){
		GNode gnode = nodes.get(range);
		if(gnode == null)
			return null;
		List<GEdge> edges = gnode.outgoings;
		Set<String> result = new HashSet<String>();
		boolean isInside = false;
		SourceCodeRange tmpRange = null;
		for (GEdge edge : edges) {
			isInside = false;
			if (edge instanceof GDefEdge) {
				tmpRange = edge.sink.range;				
				for (SourceCodeRange iRange : includedRanges) {
					if (tmpRange.isInside(iRange)) {
						isInside = true;
						break;
					}
				}
				if(isInside){
					result.add(((GDefEdge)edge).label);
				}
			}
		}
		
		return result;
	}
	
	
	/**
	 * Algorithm to decide output Terms: if defined term find use which is not
	 * local def
	 * 
	 * @param range
	 * @param includedRanges
	 * @return
	 */
	public Set<String> getDefWithUseOutsideVars(SourceCodeRange range,
			Set<SourceCodeRange> includedRanges) {
		GNode gnode = nodes.get(range);
		if (gnode == null)
			return null;
		List<GEdge> edges = gnode.outgoings;
		Set<String> result = new HashSet<String>();
		boolean isInside = false;
		SourceCodeRange tmpRange = null;
		for (GEdge edge : edges) {
			if (edge instanceof GDefEdge) {
				tmpRange = edge.sink.range;
				isInside = false;
				for (SourceCodeRange iRange : includedRanges) {
					if (tmpRange.isInside(iRange)) {
						isInside = true;
						break;
					}
				}
				if (!isInside) {
					result.add(((GDefEdge) edge).label);
				}
			}
		}
		for(String defLabel : gnode.defLabels){
			if(fields.containsKey(defLabel)){
				result.add(defLabel);
			}
		}
		return result;
	}

	private void addConEdge(GNode source, GNode sink) {
		GConEdge edge = new GConEdge(source, sink);
		if (!source.hasOutgoingEdge(edge)) {
			source.outgoings.add(edge);
			sink.incomings.add(edge);
		}
	}

	private void addDefEdge(GNode source, GNode sink, String var) {
		GDefEdge edge = new GDefEdge(source, sink, var);
		if (!source.hasOutgoingEdge(edge)) {
			source.outgoings.add(edge);
			sink.incomings.add(edge);
			source.defLabels.add(var);
		}
	}

	private void addFlowEdge(GNode source, GNode sink, FlowLabel label) {
		GFlowEdge edge = new GFlowEdge(source, sink, label);
		if (!source.hasOutgoingEdge(edge)) {
			source.outgoings.add(edge);
			sink.incomings.add(edge);
		}
	}
}
