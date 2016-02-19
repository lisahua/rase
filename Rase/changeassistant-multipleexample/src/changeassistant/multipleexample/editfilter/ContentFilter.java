package changeassistant.multipleexample.editfilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.multipleexample.partition.datastructure.ChangeSummary;
import changeassistant.multipleexample.partition.datastructure.NodeSummary;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;

public class ContentFilter {

	public static double termThres = 0.2;

	public static double nonemptyNodeThres = 0.2;

	public static List<String> stopWords = Arrays
			.asList(new String[] { "null" });

	/**
	 * When the change summary should be filtered out, the return value is true
	 * 
	 * @param chgSums
	 * @return
	 */
	public static boolean filterOut(List<ChangeSummary> chgSums,
			List<String> chgSumStr, List<String> absChgSumStr,
			List<Integer> leftCSIndexes, List<Integer> rightCSIndexes,
			List<List<List<SimpleASTNode>>> commonExprsLists) {
		boolean flag = true;
		NodeSummary ns = null;
		List<Term> terms = null;
		Term term = null;
		int specCounter = 0;
		int allCounter = 0;// include general terms together with specific V T M
							// terms
		int nonemptyNodeCounter = 0;
		int allNodeCounter = 0;

		String termName;
		List<Integer> indexes = new ArrayList<Integer>();
		ChangeSummary chgSum = null;
		for (int index = 0; index < chgSums.size(); index++) {
			chgSum = chgSums.get(index);
			if (chgSum.nodeSummaries.isEmpty()) {
				indexes.add(index);
				continue;
			}
			ns = chgSum.nodeSummaries.get(0);
			if (ns.expressions.size() != 0) {
				nonemptyNodeCounter++;
			}
			allNodeCounter++;
			System.out.print("");
			for (int i = 0; i < ns.expressions.size(); i++) {
				terms = ns.expressions.get(0);
				for (int j = 0; j < terms.size(); j++) {
					term = terms.get(j);
					termName = term.getName();
					if (term.getTermType().equals(Term.TermType.Term)
							&& !Term.U_Pattern.matcher(termName).matches()) {
						continue;
					}
					allCounter++;
					if (Term.ExactAbsPattern.matcher(termName).matches()
							|| stopWords.contains(termName)) {
						// do nothing
					} else {
						// specific term
						specCounter++;
					}
				}
			}
		}
		if (specCounter * (nonemptyNodeCounter * 1.0 / allNodeCounter)
				/ allCounter >= termThres
				&& nonemptyNodeCounter * 1.0 / allNodeCounter >= nonemptyNodeThres) {
			flag = false;
			if (!indexes.isEmpty()) {
				// System.out.print("");
				List<ChangeSummary> tmpChgSums = new ArrayList<ChangeSummary>(
						chgSums);
				List<String> tmpChgSumStr = new ArrayList<String>(chgSumStr);
				List<String> tmpAbsChgSumStr = new ArrayList<String>(
						absChgSumStr);
				List<Integer> tmpLeftCSIndexes = new ArrayList<Integer>(
						leftCSIndexes);
				List<Integer> tmpRightCSIndexes = new ArrayList<Integer>(
						rightCSIndexes);
				List<List<List<SimpleASTNode>>> tmpCommonExprsLists = new ArrayList<List<List<SimpleASTNode>>>(
						commonExprsLists);

				chgSums.clear();
				chgSumStr.clear();
				absChgSumStr.clear();
				leftCSIndexes.clear();
				rightCSIndexes.clear();
				commonExprsLists.clear();
				for (int i = 0; i < tmpChgSums.size(); i++) {
					if (!indexes.contains(i)) {
						chgSums.add(tmpChgSums.get(i));
						chgSumStr.add(tmpChgSumStr.get(i));
						absChgSumStr.add(tmpAbsChgSumStr.get(i));
						leftCSIndexes.add(tmpLeftCSIndexes.get(i));
						rightCSIndexes.add(tmpRightCSIndexes.get(i));
						commonExprsLists.add(tmpCommonExprsLists.get(i));
					}
				}
			}
		}
		return flag;
	}
}
