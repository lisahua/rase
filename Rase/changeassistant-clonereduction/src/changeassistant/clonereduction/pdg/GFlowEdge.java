package changeassistant.clonereduction.pdg;

import changeassistant.clonereduction.datastructure.FlowLabel;

public class GFlowEdge extends GEdge {

	public enum CATEGORY {
		RETURN, THROW, NONE
	};

	FlowLabel label;

	public GFlowEdge(GNode source, GNode sink, FlowLabel label) {
		super(source, sink);
		this.label = label;
	}
	
	public boolean equals(Object obj){
		if(!super.equals(obj))
			return false;
		if(!(obj instanceof GFlowEdge))
			return false;
		return true;
	}
}
