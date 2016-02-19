package changeassistant.clonereduction.pdg;

public class GEdge {
	
	GNode source;
	GNode sink;

	public GEdge(GNode source, GNode sink) {
		this.source = source;
		this.sink = sink;
	}

	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof GEdge))
			return false;
		GEdge other = (GEdge)obj;
		return source.equals(other.source) && sink.equals(other.sink);
	}
	
	public int hashCode(){
		return source.hashCode() * 31 + sink.hashCode();
	}
}
