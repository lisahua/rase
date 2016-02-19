package changeassistant.clonereduction.search;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import changeassistant.clonereduction.main.CloneReductionException;
import changeassistant.clonereduction.manipulate.convert.TemplateMethodInterfaceConverter;
import changeassistant.clonereduction.manipulate.data.ExpressionInfo;
import changeassistant.clonereduction.manipulate.data.MethodInfo;
import changeassistant.clonereduction.manipulate.data.TypeInfo;
import changeassistant.clonereduction.manipulate.data.VariableInfo;
import changeassistant.clonereduction.manipulate.refactoring.RefactoringMetaData;
import changeassistant.multipleexample.match.MatchResult;
import changeassistant.multipleexample.partition.SimpleASTCreator;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.versions.comparison.ChangedMethodADT;

public class CloneReductionMatchResult {

	//oNode corresponds to the original method in the old version
	private SimpleTreeNode oNode;
	//pNodes correspond to the extracted parts out of oNode
	private List<SimpleTreeNode> pNodes;
	private Map<String, String> cTou;
	private Map<String, String> uToc;	
	private ChangedMethodADT adt;

	public static CloneReductionMatchResult find(
			List<CloneReductionMatchResult> mResults, ChangedMethodADT adt) {
		for (CloneReductionMatchResult mResult : mResults) {
			if (mResult.adt.equals(adt)) {
				return mResult;
			}
		}
		return null;
	}

	public CloneReductionMatchResult(ChangedMethodADT adt,
			SimpleTreeNode oNode, 
			List<SimpleTreeNode> pNodes,
			List<List<SimpleASTNode>> simpleASTNodesList,
			Map<String, String> cTou, 
			Map<String, String> uToc, 
			MethodDeclaration md) throws CloneReductionException {
		this.oNode = oNode;
		this.pNodes = pNodes;
		this.cTou = cTou;
		this.uToc = uToc;
		this.adt = adt;
		createUnifiedToConcrete(simpleASTNodesList, md);
	}

	public CloneReductionMatchResult(MatchResult mResult, MatchResult oMatchResult, SimpleTreeNode sTree,
			List<List<SimpleASTNode>> simpleASTNodesList, MethodDeclaration md) 
		throws CloneReductionException {
		oNode = sTree;
		List<Integer> indexes = mResult.getSequence().getNodeIndexes();
		Enumeration<SimpleTreeNode> sEnum = sTree.breadthFirstEnumeration();
		SimpleTreeNode sTmp = null;
		while (sEnum.hasMoreElements()) {
			sTmp = sEnum.nextElement();
			if (indexes.contains(sTmp.getNodeIndex())) {
				sTmp.getEditAndRoletype().put(-1, SimpleTreeNode.RELEVANT);
			}
		}
		// System.out.print("");
		pNodes = sTree.pruneRelevant();
		cTou = mResult.getCtoU();
		uToc = mResult.getUtoC();
		if(cTou.isEmpty()){
			cTou = oMatchResult.getCtoU();
			uToc = oMatchResult.getUtoC();
		}
		adt = mResult.getADT();		
		createUnifiedToConcrete(simpleASTNodesList, md);
	}
	
	public void appendUnifiedToConcrete(int index, List<SimpleTreeNode> psNodes, List<List<SimpleASTNode>>pCusNodesList, MethodDeclaration md) 
		throws CloneReductionException{
		System.out.print("");
		Map<String, MethodInfo> unifiedToMethod = new HashMap<String, MethodInfo>();
		Map<String, ExpressionInfo> unifiedToExpression = new HashMap<String, ExpressionInfo>();
		Map<String, TypeInfo> unifiedToType = new HashMap<String, TypeInfo>();
		Map<String, VariableInfo> unifiedToVariable = new HashMap<String, VariableInfo>();
		TemplateMethodInterfaceConverter.prepareToConvert(psNodes, pCusNodesList, cTou, 
				unifiedToMethod, unifiedToExpression, unifiedToType, unifiedToVariable, md);
		RefactoringMetaData.appendUnifiedToMethod(index, unifiedToMethod);
		RefactoringMetaData.appendUnifiedToExpr(index, unifiedToExpression);
		RefactoringMetaData.appendUnifiedToType(index, unifiedToType);
		RefactoringMetaData.appendUnifiedToVariable(index, unifiedToVariable);
	}
	
	public void createUnifiedToConcrete(List<List<SimpleASTNode>> customizedNodesList, MethodDeclaration md) 
		throws CloneReductionException{
		Map<String, MethodInfo> unifiedToMethod = new HashMap<String, MethodInfo>();
		Map<String, ExpressionInfo> unifiedToExpression = new HashMap<String, ExpressionInfo>();
		Map<String, TypeInfo> unifiedToType = new HashMap<String, TypeInfo>();
		Map<String, VariableInfo> unifiedToVariable = new HashMap<String, VariableInfo>();
		TemplateMethodInterfaceConverter.prepareToConvert(pNodes, customizedNodesList, cTou, unifiedToMethod, 
				unifiedToExpression, unifiedToType, unifiedToVariable, md);		
		RefactoringMetaData.addUnifiedToMethod(unifiedToMethod);
		RefactoringMetaData.addUnifiedToExpr(unifiedToExpression);
		RefactoringMetaData.addUnifiedToType(unifiedToType);
		RefactoringMetaData.addUnifiedToVariable(unifiedToVariable);
	}

	public SimpleTreeNode getoNode() {
		return oNode;
	}

	public List<SimpleTreeNode> getpNodes() {
		return pNodes;
	}

	public Map<String, String> getcTou() {
		return cTou;
	}

	public Map<String, String> getuToc() {
		return uToc;
	}

	public ChangedMethodADT getAdt() {
		return adt;
	}

}
