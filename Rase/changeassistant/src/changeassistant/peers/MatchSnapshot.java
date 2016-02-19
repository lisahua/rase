package changeassistant.peers;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import changeassistant.change.group.model.SubTreeModel;
import changeassistant.changesuggestion.expression.representation.MethodNameTerm;
import changeassistant.changesuggestion.expression.representation.TypeNameTerm;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;

public class MatchSnapshot {
	
	public SubTreeModel lRoot;
	public SubTreeModel rRoot;
	public SubTreeModel xxx;
	public SubTreeModel yyy;
	public Set<SubTreeModelPair> matched;
	public Set<SubTreeModelPair> matchedLeaves;
	public Map<TypeNameTerm, TypeNameTerm> primeTypeMap;
	public Map<MethodNameTerm, MethodNameTerm> primeMethodMap;
	public Map<VariableTypeBindingTerm, VariableTypeBindingTerm> primeVariableMap;
	
	private Set<SubTreeModelPair> copyMatches(List<SubTreeModel> leftList, 
			List<SubTreeModel> rightList, Set<SubTreeModelPair> matched){
		Set<SubTreeModelPair> result = new HashSet<SubTreeModelPair>();
		SubTreeModel left = null, right = null;
		int leftIndex, rightIndex;
		for(SubTreeModelPair pair : matched){
			left = pair.getLeft();
			right = pair.getRight();
			leftIndex = leftList.indexOf(left);
			rightIndex = rightList.indexOf(right);
			result.add(new SubTreeModelPair(leftList.get(leftIndex), rightList.get(rightIndex)));
		}
		return result;
	}
	
	private List<SubTreeModel> createList(SubTreeModel root){
		List<SubTreeModel> result = new ArrayList<SubTreeModel>();
		Enumeration<SubTreeModel> bEnum = root.breadthFirstEnumeration();
		while(bEnum.hasMoreElements()){
			result.add(bEnum.nextElement());
		}
		return result;
	}

	public MatchSnapshot(Set<SubTreeModelPair> matched, Set<SubTreeModelPair> matchedLeaves,
			Map<TypeNameTerm, TypeNameTerm> primeTypeMap, Map<MethodNameTerm, MethodNameTerm> primeMethodMap,
			Map<VariableTypeBindingTerm, VariableTypeBindingTerm> primeVariableMap, SubTreeModel right, 
			SubTreeModel y, SubTreeModel x){
		SubTreeModel originalRightRoot = right;
		SubTreeModel originalLeftRoot = (SubTreeModel)x.getRoot();
		rRoot = new SubTreeModel(originalRightRoot);
		List<SubTreeModel> rightList = createList(rRoot);
		
		lRoot = new SubTreeModel(originalLeftRoot);	
		List<SubTreeModel> leftList = createList(lRoot);
		
		xxx = leftList.get(leftList.indexOf(x));
		yyy = rightList.get(rightList.indexOf(y));
		SubTreeModelPair pair = null;
		if(!matched.isEmpty()){
			pair = matched.iterator().next();
		}else if(!matchedLeaves.isEmpty()){
			pair = matchedLeaves.iterator().next();
		}
		
		if(pair == null){
			this.matched = new HashSet<SubTreeModelPair>();
			this.matchedLeaves = new HashSet<SubTreeModelPair>();
		}else{
			
			this.matched = copyMatches(leftList, rightList, matched);
			this.matchedLeaves = copyMatches(leftList, rightList, matchedLeaves);
		}
		this.primeTypeMap = new HashMap<TypeNameTerm, TypeNameTerm>(primeTypeMap);
		this.primeMethodMap = new HashMap<MethodNameTerm, MethodNameTerm>(primeMethodMap);
		this.primeVariableMap = new HashMap<VariableTypeBindingTerm, VariableTypeBindingTerm>(primeVariableMap);
	}
	
	public static SubTreeModel switchSnapshot(MatchSnapshot snapshot, Set<SubTreeModelPair> matchedLeaves, 
			Set<SubTreeModelPair> matched, Map<TypeNameTerm, TypeNameTerm> primeTypeMap, 
			Map<MethodNameTerm, MethodNameTerm> primeMethodMap, 
			Map<VariableTypeBindingTerm, VariableTypeBindingTerm> primeVariableMap){
		matched.clear();
		matched.addAll(snapshot.matched);
		
		matchedLeaves.clear();
		matchedLeaves.addAll(snapshot.matchedLeaves);
		
		primeTypeMap.clear();
		primeTypeMap.putAll(snapshot.primeTypeMap);
		primeMethodMap.clear();
		primeMethodMap.putAll(snapshot.primeMethodMap);
		primeVariableMap.clear();
		primeVariableMap.putAll(snapshot.primeVariableMap);
		
		return snapshot.rRoot;
	}
}
