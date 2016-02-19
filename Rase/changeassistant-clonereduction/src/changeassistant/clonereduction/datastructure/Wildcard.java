package changeassistant.clonereduction.datastructure;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.clonereduction.search.CloneReductionMatchResult;
import changeassistant.multipleexample.contextualize.datastructure.Sequence;
import changeassistant.multipleexample.datastructure.MapList;
import changeassistant.multipleexample.match.MatchResult;
import changeassistant.multipleexample.match.PatternUtil;
import changeassistant.multipleexample.partition.datastructure.ChangeSummary;
import changeassistant.multipleexample.partition.datastructure.EditInCommonCluster;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.versions.comparison.ChangedMethodADT;

public class Wildcard {

	public static int NEW = 1;
	public static int OLD = 0;
	
	public Set<String> oldWildVars, oldWildMethods, oldWildTypes, oldUnknown;
	public Set<String> newWildVars, newWildMethods, newWildTypes, newUnknown;
	public Set<String> editedWildVars, editedWildMethods, editedWildTypes,
			editedUnknown;

	public Wildcard(){
		
	}
	/**
	 * Especially designed for multiple-example scenario
	 * @param sTouList
	 */
	public Wildcard(MapList sTouList, int flag){
		Map<String, String> map = sTouList.get(0);			
		editedWildVars = editedWildMethods = editedWildTypes = editedUnknown = Collections.EMPTY_SET;
		if(flag == NEW){
			newWildVars = new HashSet<String>();
			newWildMethods = new HashSet<String>();
			newWildTypes = new HashSet<String>();
			newUnknown = new HashSet<String>();
			oldWildVars = oldWildMethods = oldWildTypes = oldUnknown = Collections.EMPTY_SET;			
			init(newWildVars, newWildMethods, newWildTypes, newUnknown, map);
		}else{
			oldWildVars = new HashSet<String>();
			oldWildMethods = new HashSet<String>();
			oldWildTypes = new HashSet<String>();
			oldUnknown = new HashSet<String>();
			newWildVars = newWildMethods = newWildTypes = newUnknown = Collections.EMPTY_SET;
			init(oldWildVars, oldWildMethods, oldWildTypes, oldUnknown, map);
		}		
	}
	
	private void init(Set<String> wildVars, Set<String> wildMethods, Set<String> wildTypes, Set<String> unknown, Map<String, String> map){
		String value = null;
		for(Entry<String, String> entry : map.entrySet()){
			value = entry.getValue();			
			if(Term.ExactAbsPattern.matcher(value).matches()){
				if(Term.V_Pattern.matcher(value).matches()){
					wildVars.add(value);
				}else if(Term.M_Pattern.matcher(value).matches()){
					wildMethods.add(value);
				}else if(Term.T_Pattern.matcher(value).matches()){
					wildTypes.add(value);
				}else{
					unknown.add(value);
				}
			}
		}
	}
	
	public void collectNewWildcards(
			List<CloneReductionMatchResult> matchResults, ChangedMethodADT adt) {
		newWildVars = new HashSet<String>();
		newWildMethods = new HashSet<String>();
		newWildTypes = new HashSet<String>();
		newUnknown = new HashSet<String>();
		CloneReductionMatchResult mResult = CloneReductionMatchResult.find(
				matchResults, adt);
		try{
		collectWildcards(newWildVars, newWildMethods, newWildTypes, newUnknown,
				mResult.getuToc().keySet());
		}catch(Exception e){
		}
	}

	public void collectOldWildcards(List<MatchResult> matchResults,
			ChangedMethodADT adt) {
		oldWildVars = new HashSet<String>();
		oldWildMethods = new HashSet<String>();
		oldWildTypes = new HashSet<String>();
		oldUnknown = new HashSet<String>();
		MatchResult mResult = MatchResult.find(matchResults, adt);
		try{
		collectWildcards(oldWildVars, oldWildMethods, oldWildTypes, oldUnknown,
				mResult.getUtoC().keySet());
		}catch(Exception e){			
		}
	}

	public void collectEditedWildcards(EditInCommonCluster cluster) {
		editedWildVars = new HashSet<String>();
		editedWildMethods = new HashSet<String>();
		editedWildTypes = new HashSet<String>();
		editedUnknown = new HashSet<String>();

		List<Integer> nodeIndexes = Sequence.parsePositiveIndexes(cluster
				.getCodePattern().getSequence().getNodeIndexes());
		List<List<SimpleASTNode>> nodesList = cluster.getSimpleASTNodesList(0);
		Set<String> alphabetSet = new HashSet<String>();
		for (Integer nodeIndex : nodeIndexes) {
			PatternUtil.collectAllIdentifiers(alphabetSet,
					nodesList.get(nodeIndex - 1));
		}
		List<List<List<SimpleASTNode>>> exprsLists = cluster
				.getSimpleExprsLists();
		List<ChangeSummary> summaries = cluster.getConChgSum();
		for (int i = 0; i < summaries.size(); i++) {
			switch (summaries.get(i).editType) {
			case UPDATE:
				PatternUtil.collectAllIdentifiers(alphabetSet, exprsLists
						.get(i).get(1));
				break;
			case INSERT:
				PatternUtil.collectAllIdentifiers(alphabetSet, exprsLists
						.get(i).get(0));
				break;
			}
		}
		collectWildcards(editedWildVars, editedWildMethods, editedWildTypes,
				editedUnknown, alphabetSet);
	}

	private void collectWildcards(Set<String> wildVars,
			Set<String> wildMethods, Set<String> wildTypes,
			Set<String> unknowns, Set<String> keys) {
		for (String key : keys) {
			if (Term.ExactAbsPattern.matcher(key).find()) {
				if (Term.V_Pattern.matcher(key).matches()) {
					wildVars.add(key);
				} else if (Term.M_Pattern.matcher(key).matches()) {
					wildMethods.add(key);
				} else if (Term.T_Pattern.matcher(key).matches()) {
					wildTypes.add(key);
				} else {// U_Pattern
					unknowns.add(key);
				}
			}
		}
	}
}
