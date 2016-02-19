package changeassistant.clonereduction.manipulate;

import changeassistant.model.AbstractNode;
import changeassistant.peers.comparison.Node;

public class MarkerUtility {

	public static int setMarkerProperty(Node n, int value){
		boolean success = n.setProperty(AbstractNode.MARKER_PROPERTY, value);
		if(success)
			return value+1;
		return value;
	}
}
