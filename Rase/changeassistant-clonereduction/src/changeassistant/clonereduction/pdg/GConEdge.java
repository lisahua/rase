package changeassistant.clonereduction.pdg;

import java.util.List;

public class GConEdge extends GEdge {

	public GConEdge(GNode source, GNode sink) {
		super(source, sink);
	}
	public boolean equals(Object obj){
		if(!super.equals(obj))
			return false;
		if(!(obj instanceof GConEdge))
			return false;
		return true;
	}
}
