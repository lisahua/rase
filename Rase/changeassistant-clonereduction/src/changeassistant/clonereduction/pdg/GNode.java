package changeassistant.clonereduction.pdg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import changeassistant.peers.SourceCodeRange;

public class GNode {

	Set<String> defLabels;	
	Set<String> usedFields;
	SourceCodeRange range;
	List<GEdge> incomings;
	List<GEdge> outgoings;

	public GNode(SourceCodeRange range) {
		this.range = range;
		init();
	}
	
	public boolean hasOutgoingEdge(GEdge edge){
		boolean contained = false;
		for(GEdge outEdge : outgoings){
			if(edge.equals(outEdge)){
				contained = true;
				break;
			}
		}
		return contained;
	}

	private void init() {
		this.incomings = new ArrayList<GEdge>();
		this.outgoings = new ArrayList<GEdge>();
		this.defLabels = new HashSet<String>();
		this.usedFields = new HashSet<String>();
	}

	public void addUsedField(String f){
		usedFields.add(f);
	}
	
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof GNode))
			return false;
		GNode other = (GNode)obj;
		return this.range.equals(other.range);
	}
	
	public int hashCode(){
		return range.hashCode();
	}

	@Override
	public String toString() {
		return range.toString();
	}
}
