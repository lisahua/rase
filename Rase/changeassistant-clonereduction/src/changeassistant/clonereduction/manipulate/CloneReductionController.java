package changeassistant.clonereduction.manipulate;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import changeassistant.change.group.edits.AbstractTreeEditOperation2;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;
import changeassistant.classhierarchy.ClassContext;
import changeassistant.classhierarchy.ProjectResource;
import changeassistant.clonereduction.datastructure.MethodRelation;
import changeassistant.clonereduction.datastructure.Wildcard;
import changeassistant.clonereduction.helper.MethodRelationHelper;
import changeassistant.clonereduction.main.CloneReductionException;
import changeassistant.clonereduction.main.CloneReductionMain;
import changeassistant.clonereduction.manipulate.refactoring.MethodExtractor;
import changeassistant.clonereduction.manipulate.refactoring.ParamCreator;
import changeassistant.clonereduction.manipulate.refactoring.RefactoringMetaData;
import changeassistant.clonereduction.manipulate.refactoring.RetObjInterpretionCreator;
import changeassistant.clonereduction.manipulate.util.CloneReductionApplier;
import changeassistant.clonereduction.manipulate.util.CloneReductionUtil;
import changeassistant.clonereduction.search.CloneReductionMatchResult;
import changeassistant.clonereduction.search.CloneReductionSearchController;
import changeassistant.internal.ASTNodeFinder;
import changeassistant.multipleexample.datastructure.GeneralizedStmtIndexMap;
import changeassistant.multipleexample.edit.STreeEditScript;
import changeassistant.multipleexample.edit.STreeInsertOperation;
import changeassistant.multipleexample.main.Constants;
import changeassistant.multipleexample.main.Oracle;
import changeassistant.multipleexample.match.MatchResult;
import changeassistant.multipleexample.match.PatternUtil;
import changeassistant.multipleexample.partition.SimpleASTCreator;
import changeassistant.multipleexample.partition.datastructure.EditInCommonCluster;
import changeassistant.multipleexample.partition.datastructure.EditInCommonGroup;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.peers.SourceCodeRange;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.comparison.ChangedMethodADT;
import changeassistant.versions.comparison.MethodModification;
import changeassistant.versions.treematching.edits.ITreeEditOperation.EDIT;

public class CloneReductionController {

	public List<ChangedMethodADT> newADTs;
	public List<ChangedMethodADT> oldADTs;

	public MethodRelation rel;

	private List<SimpleTreeNode> modifiedTrees = null;
	private CloneReductionFilter crFilter;
	private CloneReductionTemplateCreator crtCreator;
	private EditInCommonGroup group;
	private EditInCommonCluster cluster;
	private ProjectResource prOld, prNew;
	private Oracle oracle;
	private MethodExtractor extractor;
	private List<CloneReductionMatchResult> matchResults;
	private MethodRelationHelper mHelper;
	private MethodDeclaration md;

	public CloneReductionController(ProjectResource prOld, ProjectResource prNew) {
		this.prOld = prOld;
		this.prNew = prNew;
	}
	
	private void init(EditInCommonGroup group, Oracle oracle) throws Exception{
		this.group = group;
		this.cluster = group.getClusters().get(0);
		if(!CloneReductionMain.hasMultiExamples){
			this.oldADTs = CloneReductionUtil.getOldADTs(group);
			this.newADTs = CloneReductionUtil.getNewADTs(group);
			crFilter = new CloneReductionFilter(prNew); 
			mHelper = new MethodRelationHelper(prNew);
			rel = mHelper.inferRelation(newADTs);	
			crtCreator = new CloneReductionTemplateCreator();				
			crFilter.prepareForExtraction(cluster, group, this);
			crtCreator.createTemplate(cluster, group, this);
		}else{
			List<MethodModification> mmList = group.getMMList();
			List<Integer> instances = cluster.getInstances();	
			if(CloneReductionMain.refactoringOld){
				oldADTs = new ArrayList<ChangedMethodADT>();
				for(Integer inst : instances){
					oldADTs.add(mmList.get(inst).originalMethod);
				}
				crFilter = new CloneReductionFilter(prOld);
				mHelper = new MethodRelationHelper(prOld);
				rel = mHelper.inferRelation(oldADTs);
				crtCreator = new CloneReductionTemplateCreator();
				crFilter.prepareForExtraction(cluster, group, this);
				crtCreator.createTemplate(cluster, group, this);
			}else{
				newADTs = new ArrayList<ChangedMethodADT>();
				for(Integer inst : instances){
					newADTs.add(mmList.get(inst).newMethod);
				}
				crFilter = new CloneReductionFilter(prNew); 
				mHelper = new MethodRelationHelper(prNew);
				rel = mHelper.inferRelation(newADTs);	
				crtCreator = new CloneReductionTemplateCreator();				
				crFilter.prepareForExtraction(cluster, group, this);
				crtCreator.createTemplate(cluster, group, this);
			}
		}		
		this.oracle = oracle;
		List<EditInCommonCluster> clusters = group.getClusters();
		if (clusters == null) {
			throw new CloneReductionException(
					"There is no cluster extracted from the given examples");
		}		
	}
	
	private void init2(EditInCommonGroup group, Oracle oracle) throws Exception{
		this.group = group;
		this.oldADTs = CloneReductionUtil.getOldADTs(group);
		this.newADTs = CloneReductionUtil.getNewADTs(group);
		this.oracle = oracle;
		List<EditInCommonCluster> clusters = group.getClusters();
		if (clusters == null) {
			throw new CloneReductionException(
					"There is no cluster extracted from the given examples");
		}
		this.cluster = clusters.get(0);	
		crFilter = new CloneReductionFilter(prOld); 
		mHelper = new MethodRelationHelper(prOld);
		rel = mHelper.inferRelation(oldADTs);	
		crtCreator = new CloneReductionTemplateCreator();				
		crFilter.prepareForExtraction(cluster, group, this);
		crtCreator.createTemplate(cluster, group, this);
	}
	
	private void appendUnifiedToMethodOrExpr() throws CloneReductionException{		
		List<List<List<SimpleASTNode>>> pCusNodesLists = crFilter.getCustomizedNodesLists();
		List<List<SimpleTreeNode>> pSNodesList = crFilter.getPartialSNodes();
		List<List<SimpleASTNode>> pCusNodesList = null;
		List<SimpleTreeNode> pSNodes = null;
		ChangedMethodADT adt = null;
		CloneReductionMatchResult tmpResult = null;
		ClassContext cc = null;
		MethodDeclaration md = null;
		for(int i = 0; i < newADTs.size(); i++){
			adt = newADTs.get(i);
			cc = prNew.findClassContext(adt.classname);
			md = (MethodDeclaration)cc.getMethodAST(adt.methodSignature);			
			int index = -1;
			ChangedMethodADT oldADT = oldADTs.get(i);
			for(int j = 0; j < matchResults.size(); j++){
				tmpResult = matchResults.get(j);
				if(tmpResult.getAdt().equals(oldADT)){
					index = j;
					pCusNodesList = pCusNodesLists.get(i);
					pSNodes = pSNodesList.get(i);
					break;
				}
			}
			tmpResult.appendUnifiedToConcrete(index, pSNodes, pCusNodesList, md);
		}		
	}
	
	public boolean check(EditInCommonGroup group, Wildcard wildcard, List<MatchResult> mResults, Oracle oracle) throws Exception{		
		try {
			init(group, oracle);
			// search for places which can invoke the extracted method
			CloneReductionSearchController crsc = new CloneReductionSearchController();
			//find all matchResults
//			matchResults = crsc.findCandidates(crFilter, prOld, oldADTs, mResults, crtCreator.getTemplate());		
			if(!CloneReductionMain.hasMultiExamples){
				matchResults = crsc.findCandidates(crFilter, prNew, newADTs);				
				if (matchResults.size() > 2) {
					rel = mHelper.inferRelation2(matchResults);
					System.out.println("Need more process to appendUnifiedToMethodOrExpr() for the unknown methods");
				}else{
//					appendUnifiedToMethodOrExpr();
				}
				wildcard.collectNewWildcards(matchResults, oldADTs.get(0));			
				// create extracted method							
				// it has a side effect to create a parameter string for each method
				// to call the extracted one
				extractor.extractMethod(group, cluster, crFilter,
						Constants.EXTRACT_METHOD, rel, wildcard, matchResults);
				// prepare parameters and output receivers for each matching place
				ParamCreator pCreator = new ParamCreator();
				pCreator.createInterfaceParams(newADTs.get(0), matchResults);
				// modify each method based matchResults, and parameters/outputs
				applyToEachMethod(pCreator.getParameterStrings(), pCreator.getOutputReceivers(), 
						pCreator.getOutputTermReceivers(), mResults, rel);		
			}else{
				//infer relation between clones
				if(CloneReductionMain.refactoringOld){
					extractor = new MethodExtractor(prOld);
					rel = mHelper.inferRelation(oldADTs);
					wildcard = new Wildcard(crFilter.getSpecificToUnifiedList(), Wildcard.OLD);
				}else{
					extractor = new MethodExtractor(prNew);	
					rel = mHelper.inferRelation(newADTs);			
					wildcard = new Wildcard(crFilter.getSpecificToUnifiedList(), Wildcard.NEW);		
				}							
				
				extractor.extractMethod(crFilter, rel, wildcard, Constants.EXTRACT_METHOD, group.getMMList().get(group.getClusters().get(0).getInstances().get(0)));
				ParamCreator pCreator = new ParamCreator();
				pCreator.createInterfaceParams(crFilter, extractor.getMethodHeaderCreator());
				applyToEachMethod(pCreator.getParameterStrings(), pCreator.getOutputReceivers(), 
						pCreator.getOutputTermReceivers(), rel);
			}
		} catch (CloneReductionException e) {
			System.out.println(e.getMessage());
			return false;
		}
		return true;
	}
	
	public boolean check2(EditInCommonGroup group, Wildcard wildcard, List<MatchResult> mResults, Oracle oracle) throws Exception{
		init2(group, oracle);
		CloneReductionSearchController crsc = new CloneReductionSearchController();
		matchResults = crsc.findCandidates(crFilter, prOld, oldADTs);
		extractor = new MethodExtractor(prOld);	
		extractor.extractMethod(group, cluster, crFilter,
				Constants.EXTRACT_METHOD, rel, wildcard, matchResults);
		ParamCreator pCreator = new ParamCreator();
		pCreator.createInterfaceParams(oldADTs.get(0), matchResults);
		// modify each method based matchResults, and parameters/outputs
		applyToEachMethod(pCreator.getParameterStrings(), pCreator.getOutputReceivers(), 
				pCreator.getOutputTermReceivers(), mResults, rel);	
		return true;
	}

	public void apply(Wildcard wData) throws Exception {
		// modify java files
		CloneReductionApplier applier = null;
		if(CloneReductionMain.refactoringOld){
			applier = new CloneReductionApplier(prOld, wData);
		}else{
			applier = new CloneReductionApplier(prNew, wData);
		}			
		List<SimpleTreeNode> oSNodes = new ArrayList<SimpleTreeNode>();
		List<List<SimpleTreeNode>> pSNodesList = new ArrayList<List<SimpleTreeNode>>();
		if(!CloneReductionMain.hasMultiExamples){
			if (matchResults.size() > 2) {
				List<ChangedMethodADT> adts = new ArrayList<ChangedMethodADT>();
				for (CloneReductionMatchResult r : matchResults) {
					adts.add(r.getAdt());
					oSNodes.add(r.getoNode());
					pSNodesList.add(r.getpNodes());
				}
				rel = mHelper.inferRelation(adts);
				applier.manipulate(oSNodes, adts, rel,
						extractor.getMethodString(), oracle);
			} else {
				applier.manipulate(crFilter.getOriginalSNodes(),
						newADTs, rel,
						extractor.getMethodString(), oracle);
			}
		}else{
			if(CloneReductionMain.refactoringOld){
				applier.manipulate(modifiedTrees, oldADTs, rel, extractor.getMethodString(), oracle);
			}else{
				applier.manipulate(modifiedTrees, newADTs, rel, extractor.getMethodString(), oracle);
			}			
		}		
	}

	public int estimateCost() {
		return 0;
	}
	
	public String createConcreteCaller(int index, Map<Integer, Set<Integer>> map){
		StringBuffer methodToCall = new StringBuffer();
		if(map == null || map.containsKey(index))
			methodToCall.append("new ").append(Constants.CONCRETE_TEMPLATE_CLASS)
				.append(Integer.toString(index)).append("()").append(".");
		else{
			for(Entry<Integer, Set<Integer>> entry : map.entrySet()){
				if(entry.getValue().contains(index)){
					methodToCall.append("new ").append(Constants.CONCRETE_TEMPLATE_CLASS)
					.append(Integer.toString(entry.getKey())).append("()").append(".");
				}
			}
		}
		return methodToCall.toString();
	}

	private String createMethodCall(int index, MethodRelation rel) {
		StringBuffer methodToCall = new StringBuffer();
		if(RefactoringMetaData.isNeedTemplateHierarchy()){
			Map<Integer, Set<Integer>> unifiedToMethodRelations = RefactoringMetaData.getUnifiedToMethodRelations();
			Map<Integer, Set<Integer>> unifiedToTypeRelations = RefactoringMetaData.getUnifiedToTypeRelations();
			int sizeForuTomRelations = unifiedToMethodRelations.size(); 
			int sizeForuTotRelations = unifiedToTypeRelations.size(); 
			if(sizeForuTomRelations != 1 && sizeForuTotRelations == 1){
				methodToCall.append(createConcreteCaller(index, unifiedToMethodRelations));				
			}else if(sizeForuTomRelations == 1 && sizeForuTotRelations != 1){
				methodToCall.append(createConcreteCaller(index, unifiedToTypeRelations));
			}else{
				methodToCall.append(createConcreteCaller(index, null));
			}
		}else if(RefactoringMetaData.isNeedTemplateClass()){
			methodToCall.append(Constants.TEMPLATE_CLASS).append(".");
		}
		methodToCall.append(Constants.EXTRACT_METHOD);
		return methodToCall.toString();
	}
	
	protected void applyToEachMethod(List<String> parameters, List<String> outputReceivers, List<List<String>> outputTermReceivers, 
			MethodRelation rel){
		List<Node> mNodes = crFilter.getMarkedNodes();
		GeneralizedStmtIndexMap siMap = crFilter.getNeChecker().generalizedStmtIndexMap;
		Node mNode = null;
		RetObjInterpretionCreator roiCreator = new RetObjInterpretionCreator();
		SimpleTreeNode sNode = null;
		modifiedTrees = new ArrayList<SimpleTreeNode>();
		List<Integer> indexes = null;
		Enumeration<SimpleTreeNode> cEnum = null;
		SimpleTreeNode sTmp = null, cTmp = null, newNode = null, parentNode = null;
		int insertIndex = -1;
		int index = 0;
		Set<SimpleTreeNode> nodesToRemove = new HashSet<SimpleTreeNode>();
		List<List<SimpleASTNode>> stmtList = null;
		SimpleASTCreator creator = new SimpleASTCreator();
		Queue<SimpleTreeNode> queue = null;
		System.out.print("");
		for(int i = 0; i < mNodes.size(); i++){
			parentNode = null;
			insertIndex = -1;
			nodesToRemove.clear();
			mNode = mNodes.get(i);
			sNode = new SimpleTreeNode(mNode, true, 1);
			stmtList = creator.createSimpleASTNodesList(mNode);
			sNode = PatternUtil.createStringValues(sNode, stmtList, new HashSet<String>());
			modifiedTrees.add(sNode);
			indexes = siMap.getIndexes(i);
			queue = new LinkedList<SimpleTreeNode>();
			queue.add(sNode);
			while(!queue.isEmpty()){
				sTmp = queue.remove();
				index = sTmp.getNodeIndex();
				if(indexes.contains(index)){
					nodesToRemove.add(sTmp);
					if(parentNode == null){
						if(sTmp.getNodeType() == ASTNode.METHOD_DECLARATION){
							parentNode = sTmp;
							insertIndex = 0;
						}else{
							parentNode = (SimpleTreeNode)sTmp.getParent();
							insertIndex = parentNode.getIndex(sTmp);
						}																		
					}
				}else{
					cEnum = sTmp.children();
					while(cEnum.hasMoreElements()){
						cTmp = cEnum.nextElement();
						queue.add(cTmp);
					}
				}
			}
			for(SimpleTreeNode s : nodesToRemove){
				s.removeFromParent();
				CloneReductionMain.deltaCounter.decrement(s.countNodes(), "remove from old version of the method");
			}			
			String callString = createMethodCall(i, rel);
			CloneReductionMain.deltaCounter.increment(1, "add call to the extracted method");
			
			boolean needInterpreter = false;
			if(outputReceivers.isEmpty()){
				newNode = createMethodCall(parameters.get(i), callString);
			}else if(RefactoringMetaData.getReturnType() == Constants.FLAG){
				if(parentNode.getChildCount() == insertIndex){
					newNode = createMethodCall(parameters.get(i), callString);
				}else{
					needInterpreter = true;
					newNode = createAssignment(parameters.get(i), outputReceivers.get(i), callString);
				}
			}else{
				newNode = createAssignment(parameters.get(i), outputReceivers.get(i), callString);
			}
			parentNode.insert(newNode, insertIndex);
			if(RefactoringMetaData.isNeedRetObj() || needInterpreter){
				roiCreator.interpret(parentNode, insertIndex, outputTermReceivers.get(i), mNode.getMethodDeclaration());
			}	
		}
	}

	protected void applyToEachMethod(List<String> parameters,
			List<String> outputReceivers, 
			List<List<String>> outputTermReceivers,
			List<MatchResult> mResults, MethodRelation rel) {
		CloneReductionMatchResult result = null;
		SimpleTreeNode oNode = null, editedNode = null, parentNode = null;
		List<SimpleTreeNode> pNodes = null;
		SimpleTreeNode newNode = null;		
		int index = -1;
		AbstractTreeEditOperation2<SimpleTreeNode> op = cluster.getAbstractEditScript().getUpdateInsertMoves().get(0);
		List<Integer> knownNodeIndexes = cluster.getSequence().getNodeIndexes();
		int nodeIndex = -1;
		ChangedMethodADT adt = null;
		ClassContext cc = null;
		Integer insertIndex = -1;
		if(op.getParentNode() != null){
			nodeIndex = op.getParentNode().getNodeIndex();
			insertIndex = ((STreeInsertOperation)op).getLocation();
		}	
		RetObjInterpretionCreator roiCreator = new RetObjInterpretionCreator();
		for (int i = 0; i < matchResults.size(); i++) {
			result = matchResults.get(i);
			adt = result.getAdt();
			if(CloneReductionMain.refactoringOld){
				cc = prOld.findClassContext(adt.classname);				
			}else{
				cc = prNew.findClassContext(adt.classname);
			}			
			md = (MethodDeclaration)cc.getMethodAST(adt.methodSignature);
			oNode = result.getoNode();
			CloneReductionMain.deltaCounter.decrement(oNode.countNodes(), "old version of given method");
			pNodes = result.getpNodes();
			if(!pNodes.isEmpty()){
				editedNode = oNode.lookforNodeBasedOnIndex(pNodes.get(0)
						.getNodeIndex());
				parentNode = (SimpleTreeNode) editedNode.getParent();
				index = parentNode.getIndex(editedNode);
				if (pNodes.size() == 1) {
					parentNode.remove(index);
					CloneReductionMain.refEdits.add(pNodes.get(0).countNodes(), EDIT.DELETE, "old nodes");
				} else {
					for (SimpleTreeNode pNode : pNodes) {
						editedNode = oNode.lookforNodeBasedOnIndex(pNode
								.getNodeIndex());
						parentNode.remove(parentNode.getIndex(editedNode));
						CloneReductionMain.refEdits.add(editedNode.countNodes(), EDIT.DELETE, "old nodes");
					}
				}
			}else{//everything is insert, so remember the first insert location			
				for(MatchResult mResult : mResults){
					index = -1;
					if(mResult.getADT().equals(result.getAdt())){
						List<Integer> tmpNodeIndexes = mResult.getSequence().getNodeIndexes();
						for(int j = 0; j < tmpNodeIndexes.size(); j++){
							if(knownNodeIndexes.get(j).equals(nodeIndex)){
								parentNode = oNode.lookforNodeBasedOnIndex(tmpNodeIndexes.get(j));
								index = insertIndex;
								break;
							}
						}
					}
					if(index != -1)
						break;
				}
			}			
			String callString = createMethodCall(i, rel);
			CloneReductionMain.refEdits.add(1, EDIT.INSERT, "extracted method call");
			boolean needInterpreter = false;
			if (outputReceivers.isEmpty()) {
				newNode = createMethodCall(parameters.get(i), callString);
			} else if(RefactoringMetaData.getReturnType() == Constants.FLAG){
				if(parentNode.getChildCount() == index){
					newNode = createMethodCall(parameters.get(i), callString);
				}					
				else{
					needInterpreter = true;
					newNode = createAssignment(parameters.get(i), 
							outputReceivers.get(i), callString);
				}
			}else{
				newNode = createAssignment(parameters.get(i),
						outputReceivers.get(i), callString);
			}
			parentNode.insert(newNode, index);
			if(RefactoringMetaData.isNeedRetObj() || needInterpreter){			
				roiCreator.interpret(parentNode, index, outputTermReceivers.get(i), md);
			}
			CloneReductionMain.deltaCounter.increment(oNode.countNodes(), "new version of given method");
			if (editedNode != null && editedNode.getNodeType() == ASTNode.METHOD_DECLARATION) {
				editedNode.removeAllChildren();
				editedNode.add(createMethodCall(parameters.get(i), callString));
			} else {

			}
		}
	}

	protected SimpleTreeNode createAssignment(String paramString,
			String outputReceiver, String methodToCall) {
		StringBuffer assignment = new StringBuffer(outputReceiver);
		assignment.append(methodToCall).append("(").append(paramString)
				.append(")");
		return new SimpleTreeNode(ASTNode.EXPRESSION_STATEMENT,
				SourceCodeRange.DefaultRange, assignment.toString(), 0);
	}

	protected SimpleTreeNode createMethodCall(String paramString,
			String methodToCall) {
		StringBuffer methodCall = new StringBuffer(methodToCall);
		methodCall.append("(").append(paramString).append(")");

		return new SimpleTreeNode(ASTNode.EXPRESSION_STATEMENT,
				SourceCodeRange.DefaultRange, methodCall.toString(), 0);
	}

//	protected void addMethodCall(Set<VariableTypeBindingTerm> inputTerms,
//			SimpleTreeNode sTree) {
//		StringBuffer methodCall = new StringBuffer();
//		methodCall.append(Constants.EXTRACT_METHOD).append("(");
//		for (VariableTypeBindingTerm t : inputTerms) {
//			methodCall.append(t.getName() + ",");
//		}
//		methodCall.setLength(methodCall.length() - 1);
//		methodCall.append(")");
//		sTree.add(new SimpleTreeNode(ASTNode.EXPRESSION_STATEMENT,
//				SourceCodeRange.DefaultRange, methodCall.toString(), 0));
//	}

	protected int getAllModifiers(EditInCommonCluster cluster,
			EditInCommonGroup group) {
		int modifierStatic = Modifier.STATIC;
		List<MethodModification> mms = group.getMMList();
		ChangedMethodADT adt = null;
		ClassContext cc = null;
		MethodDeclaration md = null;
		for (MethodModification mm : mms) {
			adt = mm.newMethod;
			cc = prNew.findClassContext(adt.classname);
			md = (MethodDeclaration) cc.getMethodAST(adt.methodSignature);
			modifierStatic &= md.getModifiers();
		}
		return modifierStatic;
	}

	public MethodRelation getMethodRelation() {
		return rel;
	}

	public CloneReductionFilter getCrFilter() {
		return crFilter;
	}

	public List<CloneReductionMatchResult> getMatchResults() {
		return matchResults;
	}
}
