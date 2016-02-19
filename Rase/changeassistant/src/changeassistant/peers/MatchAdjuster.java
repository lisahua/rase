package changeassistant.peers;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;

import changeassistant.change.group.model.SubTreeModel;
import changeassistant.changesuggestion.expression.representation.MethodNameTerm;
import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.TypeNameTerm;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;

public class MatchAdjuster {

	private Map<TypeNameTerm, TypeNameTerm> primeTypeMap;
	private Map<MethodNameTerm, MethodNameTerm> primeMethodMap;
	private Map<VariableTypeBindingTerm, VariableTypeBindingTerm> primeVariableMap;
	private Set<SubTreeModelPair> matchedLeaves;
	private Set<SubTreeModelPair> matched;
	
	private BlackPairSet blackPairs;//the pairs definitely should not be considered
	
	private void init(
			Set<SubTreeModelPair> matchedLeaves, Set<SubTreeModelPair> matched,
			Map<TypeNameTerm, TypeNameTerm> pTMap,
	        Map<MethodNameTerm, MethodNameTerm> pMMap,
	        Map<VariableTypeBindingTerm, VariableTypeBindingTerm> pVMap,
	        BlackPairSet blackPairs){
		this.matchedLeaves = matchedLeaves;
		this.matched = matched;
		this.primeTypeMap = pTMap;
		this.primeMethodMap = pMMap;
		this.primeVariableMap = pVMap;
		this.blackPairs = blackPairs;
	}
	
	private boolean adjust(SubTreeModel left, SubTreeModel right){
		boolean success = false;
		SubTreeModel x = null, y = null;
		SubTreeModel parent;
		List<SubTreeModel> candidates = new ArrayList<SubTreeModel>();
		Set<SubTreeModelPair> pairs;
			
		for(Enumeration<SubTreeModel> rightNodes = right.postorderEnumeration(); rightNodes.hasMoreElements();){
			y = rightNodes.nextElement();
			if(!y.isMatched()){
				candidates.clear();
				pairs = MatchWorker.getMatchedSubTreeModelPair(y, matched);
				if(!pairs.isEmpty()){
					System.out.println("There are still some node whose mapping cannot be decided");
				}
				parent = (SubTreeModel)y.getParent();
				if(parent == null){
					continue;
				}else{
					candidates = MatchWorker.searchEquivalent(y, matched, false);
				}
				if(candidates.size() == 0){//there is no possible match to use
					for(Enumeration<SubTreeModel> leftNodes = left.postorderEnumeration(); leftNodes.hasMoreElements();){
						x = leftNodes.nextElement();
						if(!x.isMatched() && MatchWorker.isEquivalentNode(x, y, false)){
							candidates.add(x);
						}
					}
				}
				blackPairs.remove(candidates, y);
				if(candidates.size() == 0){
					continue;
				}
				for(SubTreeModel candi : candidates){
					matchedLeaves.add(new SubTreeModelPair(candi, y));
					success = true;
				}
			}
		}
		return success;
	}
	
	public boolean adjust(SubTreeModel left, SubTreeModel right, 
			Set<SubTreeModelPair> matchedLeaves, Set<SubTreeModelPair> matched,
			Map<TypeNameTerm, TypeNameTerm> pTMap,
	        Map<MethodNameTerm, MethodNameTerm> pMMap,
			Map<VariableTypeBindingTerm, VariableTypeBindingTerm> pVMap,
			BlackPairSet blackPairs){
		init(matchedLeaves, matched, pTMap, pMMap, pVMap, blackPairs);
		return adjust(left, right);
	}
}
