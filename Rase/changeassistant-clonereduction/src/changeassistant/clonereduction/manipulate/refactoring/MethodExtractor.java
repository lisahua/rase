package changeassistant.clonereduction.manipulate.refactoring;

import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;
import changeassistant.classhierarchy.ProjectResource;
import changeassistant.clonereduction.datastructure.MethodRelation;
import changeassistant.clonereduction.datastructure.Wildcard;
import changeassistant.clonereduction.main.CloneReductionException;
import changeassistant.clonereduction.manipulate.CloneReductionFilter;
import changeassistant.clonereduction.manipulate.CloneReductionPDGCreator;
import changeassistant.clonereduction.manipulate.NodeEquivalenceChecker;
import changeassistant.clonereduction.manipulate.convert.TemplateMethodInterfaceConverter;
import changeassistant.clonereduction.search.CloneReductionMatchResult;
import changeassistant.multipleexample.datastructure.GeneralizedStmtIndexMap;
import changeassistant.multipleexample.datastructure.MapList;
import changeassistant.multipleexample.match.PatternUtil;
import changeassistant.multipleexample.partition.datastructure.ChangeSummary;
import changeassistant.multipleexample.partition.datastructure.EditInCommonCluster;
import changeassistant.multipleexample.partition.datastructure.EditInCommonGroup;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.comparison.ChangedMethodADT;
import changeassistant.versions.comparison.MethodModification;
import changeassistant.versions.treematching.edits.ITreeEditOperation.EDIT;

public class MethodExtractor {
	private String newMethodString;
	private ProjectResource pr;
	private static List<MethodDeclaration> mdList;
	private MethodHeaderCreator mhCreator;

	public MethodExtractor(ProjectResource pr) {
		this.pr = pr;
	}
	
	public static void setMethodDeclarationList(List<MethodDeclaration> list){
		mdList = list;
	}
	
	public MethodHeaderCreator getMethodHeaderCreator(){
		return mhCreator;
	}

	protected SimpleTreeNode concretize(SimpleTreeNode sTree,
			EditInCommonCluster cluster) {
		List<ChangeSummary> csList = cluster.getConChgSum();
		List<List<List<SimpleASTNode>>> exprsLists = cluster
				.getSimpleExprsLists();
		Map<String, String> uTos = cluster.getUnifiedToSpecificList().get(0);
		List<List<SimpleASTNode>> sNodesList = null;
		List<SimpleASTNode> sNodes = null;
		Enumeration<SimpleTreeNode> sEnum = sTree.breadthFirstEnumeration();
		SimpleTreeNode sTmp = null;
		Map<Integer, Integer> editAndRoleType = null;
		while (sEnum.hasMoreElements()) {
			sTmp = sEnum.nextElement();
			if (Term.Abs_And_Exact_Pattern.matcher(sTmp.getStrValue())
					.matches()) {
				editAndRoleType = sTmp.getEditAndRoletype();
				if (!editAndRoleType.isEmpty()) {
					if (editAndRoleType.size() == 1) {
						Entry<Integer, Integer> editAndRole = editAndRoleType
								.entrySet().iterator().next();
						Integer role = editAndRole.getValue();
						Integer index = editAndRole.getKey();
						if (role == SimpleTreeNode.EDITED) {
							EDIT edit = csList.get(index).editType;
							if (edit.equals(EDIT.INSERT)
									|| edit.equals(EDIT.UPDATE)) {
								sNodesList = exprsLists.get(editAndRole
										.getKey());
								sNodes = sNodesList.get(sNodesList.size() - 1);
								sNodes = SimpleASTNode.concretizeSingleNode(
										uTos, sNodes);
								sTmp.setStrValue(PatternUtil.createStrValue(
										sTmp.getNodeType(), sTmp.getStrValue(),
										sNodes));
							}
						}
					}
				}
			}
		}
		return sTree;
	}

	public void extractMethod(EditInCommonGroup group,
			EditInCommonCluster cluster, CloneReductionFilter crFilter,
			String extractMethodName, MethodRelation rel, 
			Wildcard wildcard, List<CloneReductionMatchResult> matchResults)
			throws CloneReductionException {
		MethodExtractorHelper meHelper = new MethodExtractorHelper(pr,
				wildcard, rel);
		meHelper.checkNeedTemplateClass();
		
		CloneReductionPDGCreator creator = new CloneReductionPDGCreator(pr);
		creator.drawPDG(group.getMMList().get(0),
				crFilter.getRangesList().get(0), crFilter.getContexts().get(0));
		TemplateMethodInterfaceConverter.mergeInfo(pr);		
		mhCreator = new MethodHeaderCreator();
		String header = mhCreator.createHeader(extractMethodName, crFilter.getSpecificToUnifiedList().get(0),
				crFilter.getPartialSNodes().get(0), crFilter.getCustomizedNodesLists().get(0),
				crFilter.getMarkedNodes().get(0).getMethodDeclaration(), matchResults);
		MethodBodyCreator mbCreator = new MethodBodyCreator();
		newMethodString = mbCreator.createBody(header, crFilter.getpSNodes(),
				crFilter.getMarkedNodes().get(0),
				crFilter.getCustomizedNodesLists().get(0));		
	}
	
	public void extractMethod(CloneReductionFilter crFilter, MethodRelation rel, Wildcard wildcard, 
			String extractMethodName, MethodModification mm)
		throws CloneReductionException{		
		NodeEquivalenceChecker neChecker = crFilter.getNeChecker();
		MapList mapList = neChecker.getMapList();
		GeneralizedStmtIndexMap generalizedStmtIndexMap = neChecker.getGeneralizedStmtIndexMap();
		MethodExtractorHelper meHelper = new MethodExtractorHelper(pr, wildcard, rel);
		meHelper.checkNeedTemplateClass();
		CloneReductionPDGCreator creator = new CloneReductionPDGCreator(pr);
//		System.out.print("");
		Node mNode = crFilter.getMarkedNodes().get(0);
		creator.drawPDG(mm, mNode, generalizedStmtIndexMap.getIndexes(0), 
				generalizedStmtIndexMap.getIndexStmtMap(0), neChecker.getNodeIndexMapList().get(0), 
				neChecker.getMapList().get(0));
		TemplateMethodInterfaceConverter.mergeInfo(pr);
		mhCreator = new MethodHeaderCreator();
		String header = mhCreator.createHeader(extractMethodName, mapList, 
				generalizedStmtIndexMap, mdList);
		MethodBodyCreator mbCreator = new MethodBodyCreator();
		newMethodString = mbCreator.createBody(header, mNode,
				crFilter.getMarkedNodesList().get(0),
				mapList.get(0),
				mapList.getMImap(0),				
				generalizedStmtIndexMap);
	}

	public String getMethodString() {
		return newMethodString;
	}

}
