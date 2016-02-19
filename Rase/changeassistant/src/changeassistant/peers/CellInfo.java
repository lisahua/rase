package changeassistant.peers;

import java.util.List;

import changeassistant.changesuggestion.expression.representation.Term;

public class CellInfo{
	int i,j;
	SubTreeModelPair pair;
	public CellInfo(int i, int j, SubTreeModelPair pair){
		this.i = i;
		this.j = j;
		this.pair = pair;
	}
	
	public CellInfo(){
		
	}
	
	public double computeSimilarity(SubTreeModelPair pair){
		this.pair = pair;
		return computeSimilarity();
	}
	
	public double computeSimilarity(){
		List<List<Term>> left = pair.getLeft().getAbstractExpressions();
		List<Term> leftTerms, rightTerms;
		Term term1, term2;
		double temp;
		double result = 0;
		List<List<Term>> right = pair.getRight().getAbstractExpressions();
		if(left.size() != right.size()) return 0;
		if(left.size() == 0){//both of them do not contain expressions
			if(pair.getLeft().getStrValue().equals(pair.getRight().getStrValue()))
				return 1;
			else
				return 0;
		}
		double[] similarities = new double[left.size()];
		int size = 0;
		for(int i = 0; i < left.size(); i ++){
			leftTerms = left.get(i);
			rightTerms = right.get(i);
			size = Math.min(leftTerms.size(), rightTerms.size());
			temp = 0;
			for(int j = 0; j < size; j++){
				term1 = leftTerms.get(j);
				term2 = rightTerms.get(j);
				if(term1.getName().equals(term2.getName())){
					temp += 1;
				}
			}
			similarities[i] = temp/rightTerms.size();
//			if(leftTerms.size() != rightTerms.size()){
//				similarities[i] = 0;
//				break;
//			}else{
//			}
		}
		for(int i = 0; i < similarities.length; i++){
			result += similarities[i];
		}
		return result/similarities.length;
	}
	
	public boolean equals(Object obj){
		if(!(obj instanceof CellInfo)) return false;
		CellInfo other = (CellInfo)obj;
		if(this.i != other.i) return false;
		if(this.j != other.j) return false;
		if(!this.pair.equals(other.pair)) return false;
		return true;
	}
	
	public int hashCode(){
		return this.i * 10000 + this.j * 100 + this.pair.hashCode();
	}
	
	public String toString(){
		return "i = " + i + " j = " + j + ": " + pair.toString();
	}
}
