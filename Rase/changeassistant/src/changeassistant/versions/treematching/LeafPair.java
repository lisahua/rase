package changeassistant.versions.treematching;

import changeassistant.peers.comparison.Node;


public class LeafPair extends NodePair implements Comparable<LeafPair>{
	private double fSimilarity;
	public LeafPair(Node left, Node right) {
        super(left, right);
    }
	
	public LeafPair(Node left, Node right, Double similarity) {
        super(left, right);
        fSimilarity = similarity;
    }
	
	public double getSimilarity() {
        return fSimilarity;
    }
	
	public int compareTo(LeafPair other) {
        return -Double.compare(fSimilarity, other.getSimilarity());
    }
	
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer("");
		sb.append("left leaf: " + this.getLeft().toString() + "\n");
		sb.append("right leaf: " + this.getRight().toString() + "\n");
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof LeafPair))return false;
	    LeafPair another = (LeafPair)obj;
	    if(!this.getLeft().equals(another.getLeft()))return false;
	    if(!this.getRight().equals(another.getRight()))return false;
	    if(this.fSimilarity - another.fSimilarity > Math.pow(0.1, 6))return false;
		return true;
	}
	
	@Override
	public int hashCode(){
		return this.getLeft().hashCode() * 100 +
		       this.getRight().hashCode() * 10 +
		       Double.valueOf(fSimilarity).hashCode();
	}
}
