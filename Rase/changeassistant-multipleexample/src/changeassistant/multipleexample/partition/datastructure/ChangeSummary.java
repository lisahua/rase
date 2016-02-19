package changeassistant.multipleexample.partition.datastructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import changeassistant.changesuggestion.expression.representation.MethodNameTerm;
import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.TypeNameTerm;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;
import changeassistant.versions.treematching.edits.ITreeEditOperation.EDIT;

public class ChangeSummary implements Cloneable {

	public EDIT editType;
	public List<NodeSummary> nodeSummaries;
	public List<String> nodeTypes;
	
	public ChangeSummary(){
	}
	
	public ChangeSummary(EDIT editType){
		this.editType = editType;
		nodeSummaries = Collections.EMPTY_LIST;
		nodeTypes = Collections.EMPTY_LIST;
	}
	
	public ChangeSummary(EDIT editType, List<NodeSummary> nodeSummaries,
			List<String> nodeTypes){
		this();
		this.editType = editType;
		this.nodeSummaries = nodeSummaries;
		this.nodeTypes = nodeTypes;
	}
	
	@Override
	public Object clone(){
		ChangeSummary obj = null;
		try {
			obj = (ChangeSummary)super.clone();
			List<NodeSummary> newNodeSummaries = new ArrayList<NodeSummary>();
			List<String> newNodeTypes = new ArrayList<String>(nodeTypes);
			for(int i = 0; i < nodeSummaries.size(); i++){
				newNodeSummaries.add((NodeSummary)nodeSummaries.get(i).clone());
			}
			obj.nodeSummaries = newNodeSummaries;
			obj.nodeTypes = newNodeTypes;
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return obj;
	}
	
	public boolean equals(Object obj){
		if(!(obj instanceof ChangeSummary)) return false;
		ChangeSummary other = (ChangeSummary)obj;
		if(!this.editType.equals(other.editType)) return false;
//		if(!this.abstractSummaryString.equals(other.abstractSummaryString)) return false;
		if(!this.nodeSummaries.equals(other.nodeSummaries)) return false;
		return true;
	}
	
	public int hashCode(){
		return this.editType.hashCode() * 1000000 + 
		/* this.abstractSummaryString.hashCode() * 10000 */ +
		this.nodeSummaries.hashCode() * 100;
	}
	
	public String getAbstractSummaryString(){
			StringBuffer buffer = new StringBuffer();
			String temp = null;
			buffer.append(editType.toString());
			for(int i = 0; i < nodeSummaries.size(); i ++){
				temp = nodeSummaries.get(i).toAbstractString2();
				buffer.append(' ' + temp);
			}
		return buffer.toString();
	}
	
	public String getAbstractSummaryString2(){
		StringBuffer buffer = new StringBuffer();
		String temp;
		buffer.append(editType.toString());
		
		for(int i = 0; i < nodeSummaries.size(); i ++){
			temp = nodeTypes.get(i) + " " + nodeSummaries.get(i).toAbstractString2();
			buffer.append(' ' + temp);
		}
		return buffer.toString();
	}
	
	public static void merge(final ChangeSummary cs1, ChangeSummary inOutCs2){
		List<NodeSummary> nodeSummaries1 = cs1.nodeSummaries;
		List<NodeSummary> nodeSummaries2 = inOutCs2.nodeSummaries;
		List<List<Term>> termsList1, termsList2;
		List<Term> terms1, terms2;
		Term term1, term2;
		for(int i = 0; i < nodeSummaries2.size(); i++){
			termsList1 = nodeSummaries1.get(i).expressions;
			termsList2 = nodeSummaries2.get(i).expressions;
			for(int j = 0; j < termsList2.size(); j++){
				terms1 = termsList1.get(j);
				terms2 = termsList2.get(j);
				for(int k = 0; k < terms2.size(); k++){
					term1 = terms1.get(k);
					term2 = terms2.get(k);
					switch(term2.getTermType()){
					case TypeNameTerm:{
						TypeNameTerm tTerm1 = (TypeNameTerm)term1;
						TypeNameTerm tTerm2 = (TypeNameTerm)term2;
						if(!tTerm2.getName().equals(tTerm1.getName())){
							tTerm2.setName("T_x");
						}
					}break;
					case MethodNameTerm:{
						MethodNameTerm mTerm1 = (MethodNameTerm)term1;
						MethodNameTerm mTerm2 = (MethodNameTerm)term2;
						if(!mTerm2.getName().equals(mTerm1.getName()))
							mTerm2.setName("M_x");
					}break;
					case VariableTypeBindingTerm: {
						VariableTypeBindingTerm vTerm1 = (VariableTypeBindingTerm)term1;
						VariableTypeBindingTerm vTerm2 = (VariableTypeBindingTerm)term2;
						if(!vTerm2.getName().equals(vTerm1.getName()))
							vTerm2.setName("V_x");
						TypeNameTerm tTerm1 = (TypeNameTerm)vTerm1.getTypeNameTerm();
						TypeNameTerm tTerm2 = (TypeNameTerm)vTerm2.getTypeNameTerm();
						if(!tTerm2.getName().equals(tTerm1.getName()))
							tTerm2.setName("T_x");
					}break;
					}
				}
			}
		}
	}
	
	@Override
	public String toString(){
		return this.getAbstractSummaryString();
	}
}
