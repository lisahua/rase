package changeassistant.clonereduction.helper;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;
import changeassistant.clonereduction.datastructure.MethodRelation;
import changeassistant.clonereduction.datastructure.Wildcard;
import changeassistant.clonereduction.main.CloneReductionException;
import changeassistant.clonereduction.manipulate.CloneReductionFilter;
import changeassistant.clonereduction.manipulate.OperationCollection;
import changeassistant.clonereduction.search.CloneReductionMatchResult;
import changeassistant.multipleexample.match.MatchResult;
import changeassistant.multipleexample.partition.datastructure.ChangeSummary;
import changeassistant.multipleexample.partition.datastructure.EditInCommonCluster;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.treematching.edits.AbstractTreeEditOperation;
import changeassistant.versions.treematching.edits.ITreeEditOperation.EDIT;

public class BenefitEstimateHelper {

	public static int BIG_NUM = 1000;
	private OperationCollection refEdits;
	private OperationCollection sysEdits;

	private Wildcard wData;

	private List<CloneReductionMatchResult> nResults;
	private List<MatchResult> oResults;

	public BenefitEstimateHelper(Wildcard wData, List<MatchResult> oResults,
			List<CloneReductionMatchResult> nResults) {
		this.wData = wData;
		this.oResults = oResults;
		this.nResults = nResults;
	}
	
	private void countEditsForOutputObject(int numOfOutput){
		// 1--create a data structure class OutputObject
		/*
		 * public class OutputClass{
		 *  public Field field1;
		 *  public Field field2;
		 *  ...
		 *  public OutputClass(Field f1, Field f2, ...){
		 *  field1 = f1;
		 *  field2 = f2;
		 *  ...
		 *  }
		 * }
		 */
		refEdits.add(1, EDIT.INSERT_OUTPUT_CLASS);
		// numOfOutput--create a field for each output and declare it as
		// public
		refEdits.add(numOfOutput, EDIT.INSERT_FIELD);
		// 1--create a constructor for the object
		refEdits.add(1, EDIT.INSERT_METHOD);
		// numOfOutput--statements in the constructor
		refEdits.add(numOfOutput, EDIT.INSERT);
		// numOfOutput--statements to interpret the OutputObject as several
		// values used in each method caller
		/*
		 * At the callsite side 
		 * 
		 * localVar1 = outputClass.field1; 
		 * localVar2 = outputClass.field2; ...
		 */
		refEdits.addForEachMethod(numOfOutput, EDIT.INSERT);
	}
	
	private void countEditsForWildcards(int counterForChunk, MethodRelation rel){
		if (!rel.inSameClass && !rel.hasSameSuperClass) {
			// create a TemplateClass to place the extracted method
			refEdits.add(1, EDIT.INSERT_CLASS);
		} else if (rel.inSameClass) {
			// do nothing
		}
		if (!wData.newWildMethods.isEmpty() || !wData.newWildTypes.isEmpty()
				|| !wData.newUnknown.isEmpty()) {
			// create an abstract TemplateClass
			if (!refEdits.containsEdit(EDIT.INSERT_CLASS)) {
				refEdits.add(1, EDIT.INSERT_ABSTRACT_CLASS);				
			}
			refEdits.addForEachMethod(1, EDIT.INSERT_CONCRETE_CLASS);
			//if(!wData.newWildTypes.isEmpty()){
				//make a concrete copy of the extracted method for each method
			//}
			if(!wData.newWildMethods.isEmpty()){
				//declare a method for each $m in abstract class
				int numOfDeclaredMethods = wData.newWildMethods.size();				
				refEdits.add(numOfDeclaredMethods, EDIT.INSERT_METHOD);
				//override the methods for $m in concrete class
				refEdits.addForEachMethod(numOfDeclaredMethods, EDIT.INSERT_METHOD);
				//fill in contents of the methods
				refEdits.addForEachMethod(numOfDeclaredMethods, EDIT.INSERT);
			}
			if(!wData.newUnknown.isEmpty()){
				for(String unknown : wData.newUnknown){
					if(!Term.U_List_Literal_Pattern.matcher(unknown).matches()){
						//declare a method for each $u in abstract class
						refEdits.add(1, EDIT.INSERT_METHOD);
						//declare a method in each concrete class
						refEdits.addForEachMethod(1, EDIT.INSERT_METHOD);
						//fill in content
						refEdits.addForEachMethod(1, EDIT.INSERT);
					}
				}
			}
		}
	}
	
	private void countNumOfOperations(MethodRelation rel,
			Set<VariableTypeBindingTerm> outputTerms, Set<Node> returnNodes,
			Set<Node> flowNodes, List<SimpleTreeNode> pSNodes) {
		int counterForChunk = 0;
		int tmpCounter = 0;
		for (SimpleTreeNode pNode : pSNodes) {
			tmpCounter = pNode.countNodes();
			counterForChunk += tmpCounter;
		}
		// insert a method call for each callsite
		/*
		 * (var = )extractMethod(paramList...);
		 */
		refEdits.addForEachMethod(1, EDIT.INSERT);
		// create an extracted method
		/*
		 * returnType extractMethod(Type1 p1, Type2 p2, ...){
		 *  code_chunk
		 * }
		 */ 
		refEdits.add(1, EDIT.INSERT_METHOD);
		// fill content of the method
		refEdits.add(counterForChunk, EDIT.INSERT);		
		
		int numOfOutput = outputTerms.size();
		if (returnNodes.size() > 0) {
			if (!flowNodes.isEmpty()) {
				numOfOutput++;// add a flag to decide whether return or not
			}
			for(Node rNode : returnNodes){
				if(rNode.getStrValue().startsWith("return:") && !rNode.getASTExpressions().isEmpty()){
					numOfOutput++;
					break;
				}
			}
		}
		if (numOfOutput > 1) {
			countEditsForOutputObject(numOfOutput);			
		}

		if (numOfOutput >= 1) {// add return stmt
			if (returnNodes.isEmpty()) {
				//create an extra statement inside the extractMethod()
				// return a created object/returnVar for output
				/*
				 * return Object/returnVar;
				 */
				refEdits.add(1, EDIT.INSERT);			
			} else if (flowNodes.isEmpty()) {//!returnNodes.isEmpty()
				// do nothing
			} else {// !returnNodes.isEmpty() && !flowNodes.isEmpty()
				refEdits.add(flowNodes.size(), EDIT.INSERT);
				//insert flag check logic at each callsite
				/*
				 * if(flag == RETURN){
				 *     return returnVar;
				 * }
				 */
				refEdits.addForEachMethod(3, EDIT.INSERT);
			}
		}
		countEditsForWildcards(counterForChunk, rel);
	}

	public int worthMethodExtraction(CloneReductionFilter crFilter,
			List<AbstractTreeEditOperation> edits, MethodRelation rel,
			Set<VariableTypeBindingTerm> outputTerms, Set<Node> returnNodes,
			Set<Node> flowNodes) throws CloneReductionException {
		refEdits = new OperationCollection();
		refEdits.setNumOfMethods(nResults.size());
		Node mNode = crFilter.getMarkedNodes().get(0);
		countEditOperations(mNode, edits);
		countNumOfOperations(rel, outputTerms, returnNodes, flowNodes, crFilter.getpSNodes());
		return refEdits.getCount();
	}

	private void countEditOperations(Node mNode,
			List<AbstractTreeEditOperation> edits) {
		Node updatedNode = (Node) edits.get(edits.size() - 2).getNode()
				.deepCopy();
		Node oldNode = (Node) ((Node) edits.get(0).getParentNode().getRoot())
				.deepCopy();
		Enumeration<Node> nEnum = mNode.breadthFirstEnumeration();
		Node tmp = null;
		Set<Node> unchangedNodes = new HashSet<Node>();
		Set<Node> updatedNodes = new HashSet<Node>();
		List<Node> mappedNodes = null;
		while (nEnum.hasMoreElements()) {
			tmp = nEnum.nextElement();
			if (tmp.getRole() == SimpleTreeNode.NONE) {
				tmp = (Node) updatedNode.lookforNodeBasedOnPosition(tmp);
				mappedNodes = oldNode.lookforNodeBasedOnRange(tmp
						.getSourceCodeRange());
				if (mappedNodes.size() == 1) {
					unchangedNodes.add(mappedNodes.iterator().next());
					continue;
				}
				String tmpStr1 = tmp.getStrValue();
				String nodeTypeStr1 = tmpStr1.substring(0, tmpStr1.indexOf(":"));
				for (Node mapped : mappedNodes) {
					String tmpStr2 = mapped.getStrValue();
					if (tmpStr2.substring(0, tmpStr2.indexOf(":")).equals(
							nodeTypeStr1)) {
						unchangedNodes.add(mapped);
						break;
					}
				}
//				if (!mappedNodes.isEmpty()) {
//					unchangedNodes.add(tmp);
//				}
			}

			else if (tmp.getRole() == SimpleTreeNode.EDITED) {
				Node tmp1 = (Node) updatedNode.lookforNodeBasedOnPosition(tmp);
				List<Node> tmpFoundNodes = oldNode.lookforNodeBasedOnRange(tmp1
						.getSourceCodeRange());
				String tmpStr1 = tmp.getStrValue();
				String nodeTypeStr1 = tmpStr1
						.substring(0, tmpStr1.indexOf(":"));
				for (Node tmpFoundNode : tmpFoundNodes) {
					String tmpStr2 = tmpFoundNode.getStrValue();
					if (tmpStr2.substring(0, tmpStr2.indexOf(":")).equals(
							nodeTypeStr1)
							&& !tmpStr2.equals(tmpStr1)) {
						updatedNodes.add(tmpFoundNode);
						break;
					}
				}
			}

		}
		nEnum = oldNode.breadthFirstEnumeration();
		Set<Node> nodesToDelete = new HashSet<Node>();
		while (nEnum.hasMoreElements()) {
			tmp = nEnum.nextElement();
			if (!unchangedNodes.contains(tmp) /* && !updatedNodes.contains(tmp) */) {
				nodesToDelete.add(tmp);
			}
		}
		nodesToDelete.remove(oldNode);
		if (!nodesToDelete.isEmpty()) {
			refEdits.addForEachMethod(nodesToDelete.size(), EDIT.DELETE);
		}
	}

	public int worthSystematicEdit(EditInCommonCluster cluster) {
		sysEdits = new OperationCollection();
		sysEdits.setNumOfMethods(oResults.size());
		List<ChangeSummary> csList = cluster.getConChgSum();
		for (ChangeSummary cs : csList) {
			sysEdits.addForEachMethod(1, cs.editType);
		}
		Set<String> newEditedVars = new HashSet<String>(wData.editedWildVars);
		newEditedVars.removeAll(wData.oldWildVars);

		Set<String> newEditedMethods = new HashSet<String>(
				wData.editedWildMethods);
		newEditedMethods.removeAll(wData.oldWildMethods);

		Set<String> newEditedTypes = new HashSet<String>(wData.editedWildTypes);
		newEditedTypes.removeAll(wData.oldWildTypes);

		Set<String> newEditedUnknowns = new HashSet<String>(wData.editedUnknown);
		newEditedUnknowns.removeAll(wData.oldUnknown);
		if (newEditedVars.size() > 0)
			sysEdits.addForEachMethod(newEditedVars.size(), EDIT.MANUAL_REPLACE_V);
		if (newEditedMethods.size() > 0)
			sysEdits.addForEachMethod(newEditedMethods.size(), EDIT.MANUAL_REPLACE_M);
		if (newEditedTypes.size() > 0)
			sysEdits.addForEachMethod(newEditedTypes.size(), EDIT.MANUAL_REPLACE_T);
		if (newEditedUnknowns.size() > 0)
			sysEdits.addForEachMethod(newEditedUnknowns.size(), EDIT.MANUAL_REPLACE_U);
		// 1-- one time to do systematic edit
		// numOfSysEditOperations = (1 + newEditedVars.size()
		// + newEditedMethods.size() + newEditedTypes.size() + newEditedUnknowns
		// .size()) * size;
		// int newLocationsFound = size - 2;
		// for each new location found, add 2 points, since each new edit
		// location requires some edit, in totall, only 1 point is added
		// return newLocationsFound * 2 - numOfSysEditOperations;
		return sysEdits.getCount();
	}
}
