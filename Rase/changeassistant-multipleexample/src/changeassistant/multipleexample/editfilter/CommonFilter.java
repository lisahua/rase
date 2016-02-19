package changeassistant.multipleexample.editfilter;

import java.util.Enumeration;
import java.util.List;

import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.multipleexample.partition.datastructure.EditInCommonCluster;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;

public class CommonFilter {

	public static double termThresInContext = 0.2;

	public static double nodeThresInContext = 0.2;

	private static int specTermCounter = 0;
	private static int allTermCounter = 0;

	/**
	 * Return true when the percentage of specific term/stmt is above a certain
	 * threshold
	 * 
	 * @param eClus
	 * @return
	 */
	public static boolean hasValidContexts(EditInCommonCluster eClus) {
		return true;
		// specTermCounter = 0;
		// allTermCounter = 0;
		// int specStmtCounter = 0;
		// int allStmtCounter = 0;
		//
		// List<List<SimpleASTNode>> simpleASTNodesList = eClus
		// .getSimpleASTNodesList();
		// for (List<SimpleASTNode> simpleASTNodes : simpleASTNodesList) {
		// if (simpleASTNodes.isEmpty())
		// continue;
		// allStmtCounter++;
		// if (Term.S_Pattern.matcher(simpleASTNodes.get(0).getStrValue())
		// .matches()) {
		// continue;
		// }
		// specStmtCounter++;
		// countTerms(simpleASTNodes);
		// }
		// if (specStmtCounter * 1.0 / allStmtCounter >= nodeThresInContext
		// && specTermCounter * 1.0 / allTermCounter >= termThresInContext)
		// return true;
		// return false;
	}

	/**
	 * return true the speciality is above a certain threshold
	 * 
	 * @param eClus
	 * @return
	 */
	public static boolean hasValidEdits(EditInCommonCluster eClus) {
		// specTermCounter = 0;
		// allTermCounter = 0;
		//
		// System.out.print("");
		// List<List<List<SimpleASTNode>>> simpleASTNodesLists = eClus
		// .getSimpleExprsLists();
		//
		// for (List<List<SimpleASTNode>> simpleASTNodesList :
		// simpleASTNodesLists) {
		// for (List<SimpleASTNode> simpleASTNodes : simpleASTNodesList) {
		// countTerms(simpleASTNodes);
		// }
		// }
		// if (specTermCounter * 1.0 / allTermCounter >= termThresInContext)
		// return true;
		// return false;
		return true;
	}

	private static void countTerms(List<SimpleASTNode> simpleASTNodes) {
		SimpleASTNode sTmp = null;
		Term term = null;
		String name = null;
		Enumeration<SimpleASTNode> sEnum = null;
		for (SimpleASTNode sNode : simpleASTNodes) {
			sEnum = sNode.breadthFirstEnumeration();
			while (sEnum.hasMoreElements()) {
				sTmp = sEnum.nextElement();
				term = sTmp.getTerm();
				if (term != null) {
					name = term.getName();
					if (term.getTermType().equals(Term.TermType.Term)
							&& !Term.U_Pattern.matcher(name).matches())
						continue;
					allTermCounter++;
					if (!Term.ExactAbsPattern.matcher(name).matches()) {
						specTermCounter++;
					}
				}
			}
		}
	}
}
