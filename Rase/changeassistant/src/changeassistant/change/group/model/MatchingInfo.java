package changeassistant.change.group.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.Set;

import changeassistant.changesuggestion.expression.representation.MethodNameTerm;
import changeassistant.changesuggestion.expression.representation.TypeNameTerm;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;
import changeassistant.peers.SubTreeModelPair;
import changeassistant.util.DebugPrinter;

public class MatchingInfo implements Cloneable{
	
	public SubTreeModel subTree;
	public SubTreeModel candidate;
	private List<SubTreeModelPair> matchedList;
	private Map<TypeNameTerm, TypeNameTerm> primeTypeMap;
	private Map<MethodNameTerm, MethodNameTerm> primeMethodMap;
	private Map<VariableTypeBindingTerm, VariableTypeBindingTerm> primeVariableMap;
	
	public MatchingInfo(){
	}
	
	public MatchingInfo(SubTreeModel subTree, SubTreeModel candidate,
			Set<SubTreeModelPair> matchedSet, Map<TypeNameTerm, TypeNameTerm> primeTypeMap,
			Map<MethodNameTerm, MethodNameTerm> primeMethodMap,
			Map<VariableTypeBindingTerm, VariableTypeBindingTerm> primeVariableMap){
			this.subTree = subTree;
			this.candidate = candidate;
			this.matchedList = new ArrayList<SubTreeModelPair>(matchedSet);
			this.primeTypeMap = primeTypeMap;
			this.primeMethodMap = primeMethodMap;
			this.primeVariableMap = primeVariableMap;
			this.indexPairs();
	}
	
	public Object clone(){
		MatchingInfo other = new MatchingInfo();
		other.subTree = (SubTreeModel)this.subTree.deepCopy();
		other.candidate = (SubTreeModel)this.candidate.deepCopy();
		other.primeTypeMap = new HashMap<TypeNameTerm, TypeNameTerm>(this.primeTypeMap);
		other.primeMethodMap = new HashMap<MethodNameTerm, MethodNameTerm>(this.primeMethodMap);
		other.primeVariableMap = 
			new HashMap<VariableTypeBindingTerm, VariableTypeBindingTerm>(this.primeVariableMap);
		other.matchedList = new ArrayList<SubTreeModelPair>();
		SubTreeModel left, right;
		for(SubTreeModelPair pair : matchedList){
			left = (SubTreeModel)other.candidate.lookforNodeBasedOnPosition(pair.getLeft());
			right = (SubTreeModel)other.subTree.lookforNodeBasedOnPosition(pair.getRight());
			other.matchedList.add(new SubTreeModelPair(left, right));
		}
		return other;
	}
	
	public SubTreeModel getCandidate(){
		return this.candidate;
	}
	
	public List<SubTreeModelPair> getMatchedList(){
		return this.matchedList;
	}
	
	public SubTreeModel getSubTree(){
		return this.subTree;
	}
	
	public Map<TypeNameTerm, TypeNameTerm> getTypeMap(){
		return this.primeTypeMap;
	}
	
	public Map<MethodNameTerm, MethodNameTerm> getMethodMap(){
		return this.primeMethodMap;
	}
	
	public Map<VariableTypeBindingTerm, VariableTypeBindingTerm> getVariableMap(){
		return this.primeVariableMap;
	}
	
	public boolean isEmpty(){
		return this.matchedList.isEmpty();
	}
	
	public void indexPairs(){
		SubTreeModelPair pair;
		for(int i = 0; i < matchedList.size(); i ++){
			pair = matchedList.get(i);
			pair.getLeft().setMatchingIndex(i);
			pair.getRight().setMatchingIndex(i);
//			System.out.println("\n");
//			DebugPrinter.printPath((SubTreeModel)pair.getRight().getRoot());
		}
	}
}
