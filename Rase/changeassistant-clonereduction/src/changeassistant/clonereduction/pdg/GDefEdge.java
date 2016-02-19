package changeassistant.clonereduction.pdg;

public class GDefEdge extends GEdge {

	String label;

	public GDefEdge(GNode source, GNode sink, String label) {
		super(source, sink);
		this.label = label;
	}
	
	public boolean equals(Object obj){
		if(!super.equals(obj))
			return false;
		if(!(obj instanceof GDefEdge)) 
			return false;
		GDefEdge dEdge = (GDefEdge)obj;
		return label.equals(dEdge.label);
	}
	
	public int hashCode(){
		return super.hashCode() * 31 + label.hashCode();
	}

	@Override
	public String toString() {
		return "GDefEdge [label=" + label + "]";
	}
		
}
