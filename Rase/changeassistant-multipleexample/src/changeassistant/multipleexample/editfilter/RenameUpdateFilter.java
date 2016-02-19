package changeassistant.multipleexample.editfilter;

import java.util.List;

import changeassistant.multipleexample.partition.datastructure.ChangeSummary;

public class RenameUpdateFilter {

	public static boolean filterOut(List<ChangeSummary> chgSums) {
		// do not filter rename update
		return false;
		// boolean flag = true;
		// int counter = 0;
		// NodeSummary ns1 = null, ns2 = null;
		// List<Term> terms1, terms2;
		// Term term1, term2;
		// for(ChangeSummary chgSum : chgSums){
		// if(counter >=
		// CommonEditParser.THRESHOLD_FOR_NUMBER_OF_NON_EMPTY_EDITS)
		// return false;
		// if(chgSum.nodeSummaries.size() == 0)
		// continue;
		// switch(chgSum.editType){
		// case INSERT:
		// case MOVE:
		// case DELETE:if(!chgSum.nodeSummaries.get(0).expressions.isEmpty())
		// counter++;
		// flag = false;
		// break;
		// case UPDATE:if(!chgSum.nodeSummaries.get(0).expressions.isEmpty() ||
		// !chgSum.nodeSummaries.get(1).expressions.isEmpty()){
		// ns1 = chgSum.nodeSummaries.get(0);
		// ns2 = chgSum.nodeSummaries.get(1);
		// boolean isRenaming = true;
		// //renaming update is not counted in
		// if(ns1.expressions.size() == ns2.expressions.size()){
		// for(int i = 0; i < ns1.expressions.size(); i++){
		// terms1 = ns1.expressions.get(i);
		// terms2 = ns2.expressions.get(i);
		// if(terms1.size() == terms2.size()){
		// for(int j = 0; j < terms1.size(); j++){
		// term1 = terms1.get(j);
		// term2 = terms2.get(j);
		// if(!term1.equals(term2)){
		// if(!term1.getTermType().equals(term2.getTermType())){
		// isRenaming = false;
		// break;
		// }
		// }
		// }
		// if(!isRenaming)
		// break;
		// }else{
		// isRenaming = false;
		// break;
		// }
		// }
		// }else{
		// isRenaming = false;
		// }
		// if(!isRenaming)
		// counter++;
		// }
		// break;
		// }
		// }
		// if(counter <
		// CommonEditParser.THRESHOLD_FOR_NUMBER_OF_NON_EMPTY_EDITS)
		// flag = true; // the useful information is too little
		// return flag;
	}
}
