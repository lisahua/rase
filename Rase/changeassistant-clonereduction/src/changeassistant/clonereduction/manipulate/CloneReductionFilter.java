package changeassistant.clonereduction.manipulate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.classhierarchy.ProjectResource;
import changeassistant.clonedetection.ScopeEnlarger;
import changeassistant.clonereduction.datastructure.MethodRelation;
import changeassistant.clonereduction.datastructure.TreeTuple;
import changeassistant.clonereduction.main.CloneReductionException;
import changeassistant.clonereduction.main.CloneReductionMain;
import changeassistant.clonereduction.manipulate.convert.TemplateMethodInterfaceConverter;
import changeassistant.clonereduction.manipulate.data.ExpressionInfo;
import changeassistant.clonereduction.manipulate.data.MethodInfo;
import changeassistant.clonereduction.manipulate.data.TypeInfo;
import changeassistant.clonereduction.manipulate.data.TypeInfoCreator;
import changeassistant.clonereduction.manipulate.data.VariableInfo;
import changeassistant.clonereduction.manipulate.refactoring.MethodExtractor;
import changeassistant.clonereduction.manipulate.refactoring.RefactoringMetaData;
import changeassistant.model.AbstractNode;
import changeassistant.multipleexample.apply.CodeGenerator;
import changeassistant.multipleexample.common.CommonParser;
import changeassistant.multipleexample.common.CommonParserMulti;
import changeassistant.multipleexample.datastructure.GeneralizedStmtIndexMap;
import changeassistant.multipleexample.datastructure.MapList;
import changeassistant.multipleexample.datastructure.NodeIndexMapList;
import changeassistant.multipleexample.main.CachedProjectMap;
import changeassistant.multipleexample.main.Constants;
import changeassistant.multipleexample.match.PatternUtil;
import changeassistant.multipleexample.partition.MappingException;
import changeassistant.multipleexample.partition.SimpleASTCreator;
import changeassistant.multipleexample.partition.datastructure.EditInCommonCluster;
import changeassistant.multipleexample.partition.datastructure.EditInCommonGroup;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.multipleexample.staticanalysis.AnalysisManager;
import changeassistant.peers.SourceCodeRange;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.comparison.MethodModification;
import changeassistant.versions.treematching.edits.AbstractTreeEditOperation;

public class CloneReductionFilter {

	private List<Node> markedNodes = null;
	
	private NodeEquivalenceChecker neChecker = null;
	
	private List<List<Node>> orderedForestList = null;
	private List<List<Node>> markedNodesList = null;
	private List<List<Node>> lastMarkedNodesList = null;

	private List<Set<Node>> contexts = null;

	private List<Set<SourceCodeRange>> rangesList = null;

	private List<String> codeStringsToExtract = null;

	private List<SimpleTreeNode> originalSNodes = null;

	private List<List<SimpleTreeNode>> partialSNodes = null;
	
	private List<List<List<SimpleASTNode>>> partiallyCustomizedNodesLists = null;

	private List<SimpleTreeNode> pSNodes = null;

	private List<List<List<SimpleASTNode>>> customizedNodesLists = null;
	private MapList specificToUnifiedList = null;
	private GeneralizedStmtIndexMap gMap = null;
	private List<MethodDeclaration> mdList = null;
	private ProjectResource pr;

	public CloneReductionFilter(ProjectResource pr) {
		this.pr = pr;
	}

	private void checkCloneReduction(MethodRelation rel)
			throws CloneReductionException {
		for (SimpleTreeNode pSNode : pSNodes) {
			Enumeration<SimpleTreeNode> sEnum = pSNode
					.breadthFirstEnumeration();
			SimpleTreeNode sTmp = null;
			String str = null;
			while (sEnum.hasMoreElements()) {
				sTmp = sEnum.nextElement();
				str = sTmp.getStrValue();
				if (Term.ExactAbsPattern.matcher(str).find()) {
					if (Term.U_Pattern.matcher(str).find()) {
						throw new CloneReductionException(
								"There is U_identifier in the extracted code fragment");
					} else if (Term.M_Pattern.matcher(str).find()) {
						if (rel.inSameClass) {
							throw new CloneReductionException(
									"There is M_identifier while the two methods are declared in the same class");
						}
					} else if (Term.T_Pattern.matcher(str).find()) {
						throw new CloneReductionException(
								"There is T_identifier in the extracted code fragments");
					}
				}
			}
		}
	}

	private void enlargeScope() throws CloneReductionException {
		contexts = new ArrayList<Set<Node>>();
		rangesList = new ArrayList<Set<SourceCodeRange>>();
		orderedForestList = new ArrayList<List<Node>>();
		mdList = new ArrayList<MethodDeclaration>();
		ScopeEnlarger enlarger = new ScopeEnlarger();
		Set<Node> context = null;
		neChecker = new NodeEquivalenceChecker(markedNodes);
		for (Node markedNode : markedNodes) {
			context = new HashSet<Node>();
			contexts.add(context);
			rangesList.add(enlarger.enlargeScope(markedNode, context));
			orderedForestList.add(enlarger.getOrderedForest());
			mdList.add(markedNode.getMethodDeclaration());
		}
		MethodExtractor.setMethodDeclarationList(mdList);
	}

	private List<String> customize() throws CloneReductionException {
		Node mNode = null;
		codeStringsToExtract = new ArrayList<String>();
		SimpleTreeNode customizedSNode = null;
		SimpleTreeNode specificSNode = null;
		SimpleASTCreator creator = new SimpleASTCreator();
		customizedNodesLists = new ArrayList<List<List<SimpleASTNode>>>();
		List<List<SimpleASTNode>> customizedNodesList = null;
		partiallyCustomizedNodesLists = new ArrayList<List<List<SimpleASTNode>>>();
		List<List<SimpleASTNode>> partiallyCustomizedNodesList = null;
		List<List<SimpleASTNode>> specificNodesList = null;
		Map<String, String> specificToUnified = null;
		Map<String, String> partiallySToU = null;
		String key = null, value = null;
		originalSNodes = new ArrayList<SimpleTreeNode>();
		partialSNodes = new ArrayList<List<SimpleTreeNode>>();
		List<SimpleTreeNode> pNodes = null;
		List<String> customizedCodeStrings = new ArrayList<String>();
		for (int i = 0; i < markedNodes.size(); i++) {
			mNode = markedNodes.get(i);
			customizedSNode = new SimpleTreeNode(mNode, true, 1);
			specificSNode = new SimpleTreeNode(mNode, true, 1);
			specificToUnified = specificToUnifiedList.get(i);
			specificNodesList = creator.createSimpleASTNodesList(mNode);
			customizedNodesList = SimpleASTNode.customize(
					specificToUnifiedList.get(i), specificNodesList);
			partiallySToU = new HashMap<String, String>();
			for(Entry<String, String> entry : specificToUnifiedList.get(i).entrySet()){
				key = entry.getKey();
				value = entry.getValue();
				if(Term.V_Pattern.matcher(value).matches()){
					partiallySToU.put(key, key);
				}else{
					partiallySToU.put(key, value);
				}				
			}
			partiallyCustomizedNodesList = SimpleASTNode.customize(partiallySToU, specificNodesList);
			partiallyCustomizedNodesLists.add(partiallyCustomizedNodesList);
			customizedNodesLists.add(customizedNodesList);
			Enumeration<SimpleTreeNode> sEnum = customizedSNode
					.breadthFirstEnumeration();
			Enumeration<SimpleTreeNode> sEnum2 = specificSNode
					.breadthFirstEnumeration();
			Enumeration<Node> nEnum = mNode.breadthFirstEnumeration();
			SimpleTreeNode sTmp = null;
			SimpleTreeNode sTmp2 = null;
			Node nTmp = null;
			String sTmpStr = null, sTmpStr2 = null;
			while (nEnum.hasMoreElements()) {
				nTmp = nEnum.nextElement();
				sTmp = sEnum.nextElement();
				sTmp2 = sEnum2.nextElement();
				switch (nTmp.getRole()) {
				case SimpleTreeNode.EDITED:
					sTmp.getEditAndRoletype().put(0, SimpleTreeNode.EDITED);
					sTmp2.getEditAndRoletype().put(0, SimpleTreeNode.EDITED);
					break;
				case SimpleTreeNode.CONTEXTUAL:
					sTmp.getEditAndRoletype().put(0, SimpleTreeNode.CONTEXTUAL);
					sTmp2.getEditAndRoletype()
							.put(0, SimpleTreeNode.CONTEXTUAL);
					break;
				case SimpleTreeNode.CONTAIN_DOWN_RELEVANT:
					sTmp.getEditAndRoletype().put(0,
							SimpleTreeNode.CONTAIN_DOWN_RELEVANT);
					sTmp2.getEditAndRoletype().put(0,
							SimpleTreeNode.CONTAIN_DOWN_RELEVANT);
					break;
				}
				sTmpStr = PatternUtil.createStrValue(sTmp.getNodeType(), sTmp.getStrValue(), 
						customizedNodesList.get(sTmp.getNodeIndex() - 1));
				sTmpStr2 = PatternUtil.createStrValue(sTmp2.getNodeType(), sTmp2.getStrValue(), 
						specificNodesList.get(sTmp2.getNodeIndex() - 1));
				sTmp.setStrValue(sTmpStr);
				sTmp2.setStrValue(sTmpStr2);
			}
			originalSNodes.add(specificSNode);
			pNodes = customizedSNode.pruneRelevant();
			partialSNodes.add(pNodes);
			String codeString = CodeGenerator.createCodeString(specificSNode.pruneRelevant());
			if(codeString.contains(Constants.SUPER + "(")){
				throw new CloneReductionException("super constructor cannot be placed into any extracted method");
			}else{
				codeStringsToExtract.add(((Block)CodeGenerator.create(codeString, ASTParser.K_STATEMENTS)).toString());
			}
			codeString = CodeGenerator.createCodeString(pNodes);
			if(codeString.contains(Constants.SUPER + "(")){
				customizedCodeStrings.add("{\n" + codeString + "}\n");
			}else{
				customizedCodeStrings.add(((Block)CodeGenerator.create(codeString, ASTParser.K_STATEMENTS)).toString());
			}
		}
		return customizedCodeStrings;
	}

	private boolean checkSame(List<String> customizedCodeStrings) {
		boolean theSame = true;
		String code0 = customizedCodeStrings.get(0);
		for (int i = 1; i < customizedCodeStrings.size(); i++) {
			if (!customizedCodeStrings.get(i).equals(code0)) {
				theSame = false;
				break;
			}
		}
		return theSame;
	}

	private void extractCommon() throws CloneReductionException {
		List<String> customizedCodeStrings = customize();
		boolean theSame = checkSame(customizedCodeStrings);
		if (theSame) {
			pSNodes = partialSNodes.get(0);
		} else {
			customizedCodeStrings = resolveDiff();
			if (customizedCodeStrings != null
					&& checkSame(customizedCodeStrings)) {
				pSNodes = partialSNodes.get(0);
			} else {
				throw new CloneReductionException(
						"Cannot extract common context");
			}
		}
		// remove identifier mappings specific to old version
		removeUnusedMaps();
	}
	
	private boolean enlargeSubtreeScope() throws Exception{				
		List<Node> mNodes = null;
		Node parent = null;
		List<Node> parentNodes = null;
		List<TreeTuple> pTuples = new ArrayList<TreeTuple>();
		TreeTuple tuple1 = null, tuple2 = null;
		boolean isEquivalent = true;
		while(isEquivalent){
			//check whether every forest has a parent node
			specificToUnifiedList = neChecker.getMapList();
			parentNodes = new ArrayList<Node>();
			pTuples.clear();
			for(int i = 0; i < markedNodesList.size(); i++){
				parent = (Node)markedNodesList.get(i).get(0).getParent();
				if(parent == null){
					parentNodes = null;
					break;
				}
				parentNodes.add(parent);
				pTuples.add(neChecker.getTuple(TreeTuple.NOTCARE_INDEX, TreeTuple.NOTCARE_CINDEX, parent, i));
			}			
			if(parentNodes != null){
				tuple1 = pTuples.get(0);
				for(int j = 1; j < pTuples.size(); j++){
					tuple2 = pTuples.get(j);
					if(!tuple1.equals(tuple2)){
						parentNodes = null;
						isEquivalent = false;
						return isEquivalent;
					}
				}
			}
			if(parentNodes == null){
				break;
			}
			if(!CloneReductionMain.expandContext)
				return false;
			//enlarge by one level
			lastMarkedNodesList = new ArrayList<List<Node>>(markedNodesList);
			markedNodesList.clear();
			for(int i = 0; i < parentNodes.size(); i++){
				mNodes = new ArrayList<Node>();
				mNodes.add(parentNodes.get(i));
				markedNodesList.add(mNodes);
			}
			try{
				gMap = neChecker.getGeneralizedStmtIndexMap().deepCopy();
				isEquivalent = neChecker.checkEquivalence(markedNodesList, specificToUnifiedList, mdList);
			}catch(Exception e){
				isEquivalent = false;
				break;
			}
		}
		if(!isEquivalent){
			neChecker.setGeneralizedStmtIndexMap(gMap);
			neChecker.mapList = specificToUnifiedList;
		}
		return isEquivalent;
	}
	
	private List<Node> processSibs(List<Node> siblings) throws Exception{
		if(siblings == null)
			return null;
		List<List<Node>> incrementalNodesList = new ArrayList<List<Node>>();		
		List<Node> iNodes = null;
		for(int i = 0; i < siblings.size(); i++){
			iNodes = new ArrayList<Node>();
			iNodes.add(siblings.get(i));
			incrementalNodesList.add(iNodes);
		}
		try{
			if(neChecker.checkEquivalence(incrementalNodesList, specificToUnifiedList.deepCopy(), mdList)){
				specificToUnifiedList = neChecker.mapList;
				return siblings;
			}else{
				neChecker.mapList = specificToUnifiedList;
				return null;
			}
		}catch(Exception e){
			neChecker.mapList = specificToUnifiedList;
			return null;
		}
	}
	
	private void addPrefixes(List<Node> siblings, List<Node> currents) throws Exception{
		Node sibling = null;	
		int step = 0;
		while(siblings != null){
			for(Node c : currents){
				sibling = (Node)c.getPreviousSibling();
				if(sibling == null){
					siblings = null;
					break;
				}
				siblings.add(sibling);
			}
			siblings = processSibs(siblings);
			if(siblings != null){
				step++;
				currents.clear();
				currents.addAll(siblings);
				siblings.clear();
			}
		}
		Node current = null;
		if(step > 0){
			for(List<Node> mNodes : markedNodesList){
				current = mNodes.get(0);
				for(int j = 0; j < step; j++){
					current = (Node)current.getPreviousSibling();
					mNodes.add(0, current);
				}
			}
		}
	}
	
	private void addPostfixes(List<Node> siblings, List<Node> currents) throws Exception{
		Node sibling = null;	
		int step = 0;
		while(siblings != null){
			for(Node c : currents){
				sibling = (Node)c.getNextSibling();
				if(sibling == null){
					siblings = null;
					break;
				}
				siblings.add(sibling);
			}
			siblings = processSibs(siblings);
			if(siblings != null){
				step++;
				currents.clear();
				currents.addAll(siblings);
				siblings.clear();
			}
		}
		Node current = null;
		if(step > 0){
			for(List<Node> mNodes : markedNodesList){
				current = mNodes.get(mNodes.size() - 1);
				for(int j = 0; j < step; j++){
					current = (Node)current.getNextSibling();
					mNodes.add(current);
				}
			}
		}
	}
	
	private boolean enlargeSubtreesScope() throws Exception{
		if(!CloneReductionMain.expandContext)
			return false;
		//increase prefix as much as possible
		List<Node> siblings = new ArrayList<Node>();		
		List<Node> currents = new ArrayList<Node>();
		for(List<Node> mNodes : markedNodesList){
			currents.add(mNodes.get(0));
		}	
		addPrefixes(siblings, currents);
		siblings = new ArrayList<Node>();
		currents = new ArrayList<Node>();
		for(List<Node> mNodes : markedNodesList){
			currents.add(mNodes.get(mNodes.size() - 1));
		}
		addPostfixes(siblings, currents);		
		return false;
	}
	
	private void extractCommonWithFlexibility() throws Exception {
		lastMarkedNodesList = null;
		markedNodesList = new ArrayList<List<Node>>(orderedForestList);
		boolean isEquivalent = neChecker.checkEquivalence(markedNodesList, specificToUnifiedList, mdList);
		GeneralizedStmtIndexMap gStmtIndexMap = neChecker.getGeneralizedStmtIndexMap();
		if(gStmtIndexMap == null || gStmtIndexMap.isEmpty()){
			throw new CloneReductionException("No commonality is extracted");
		}	
		if(!isEquivalent){			
			return;
		}
		//enlarge scope vertically
		boolean success = enlargeSubtreeScope();
		if(!success){	
			if(lastMarkedNodesList != null){//rollback
				if(markedNodesList.equals(orderedForestList)){
					// do nothing				
				}else{
					if(lastMarkedNodesList != null){
						markedNodesList = lastMarkedNodesList;
					}else{
						markedNodesList = new ArrayList<List<Node>>(orderedForestList);
					}
				}	
			}
			enlargeSubtreesScope();
		}
		List<Node> tmpMarkedNodes = markedNodesList.get(0);
		if(tmpMarkedNodes.size() == 1 && tmpMarkedNodes.get(0).getNodeType() == ASTNode.METHOD_DECLARATION){
			for(int i = 0; i < markedNodesList.size(); i++){
				tmpMarkedNodes = new ArrayList<Node>();
				Enumeration<Node> childEnum = markedNodesList.get(i).get(0).children();
				while(childEnum.hasMoreElements()){
					tmpMarkedNodes.add(childEnum.nextElement());
				}
				markedNodesList.set(i, tmpMarkedNodes);
			}
		}
	}
	
	private List<String> resolveDiff() throws CloneReductionException{
		DiffResolver resolver = new DiffResolver();
		resolver.resolveDiff(partialSNodes, specificToUnifiedList, customizedNodesLists);
		return customize();
	}

	private void removeUnusedMaps() {
		Set<String> usedValues = new HashSet<String>();
		for (List<SimpleASTNode> sNodes : customizedNodesLists.get(0)) {
			PatternUtil.collectAllIdentifiers(usedValues, sNodes);
		}
		Map<String, String> specificToUnified = specificToUnifiedList.get(0);
		Set<String> keysToRemove = new HashSet<String>();
		String value = null;
		for (Entry<String, String> entry : specificToUnified.entrySet()) {
			value = entry.getValue();
			if (!usedValues.contains(value)) {
				keysToRemove.add(entry.getKey());
			}
		}
		Set<String> valuesToRemove = new HashSet<String>();
		for (String tmpKey : keysToRemove) {
			valuesToRemove.add(specificToUnified.remove(tmpKey));
		}
		keysToRemove.clear();
		specificToUnified = specificToUnifiedList.get(1);
		for (Entry<String, String> entry : specificToUnified.entrySet()) {
			value = entry.getValue();
			if (valuesToRemove.contains(value)) {
				keysToRemove.add(entry.getKey());
			}
		}
		for (String tmpKey : keysToRemove) {
			specificToUnified.remove(tmpKey);
		}
	}

	private void markEditedNodes(List<MethodModification> mmList,
			List<Node> newNodeList, List<Integer> instances, List<List<Integer>> indexesList) {
		MethodModification mm = null;
		List<AbstractTreeEditOperation> edits = null;
		Node mNode = null;
		List<Integer> indexes = null;
		markedNodes = new ArrayList<Node>();
		AnalysisManager aManager = null;
		int tmpIndex= -1;	
		for(int i = 0; i < instances.size(); i++){
			mNode = (Node) newNodeList.get(i).deepCopy();
			markedNodes.add(mNode);
			indexes = indexesList.get(i);
			tmpIndex = instances.get(i);
			mm = mmList.get(tmpIndex);
			edits = mm.getEdits();
			aManager = new AnalysisManager(CachedProjectMap.get(mm.originalMethod.getProjectName()), pr);
			aManager.setMethodModification(mm);
			EditedNodeMarker.markEditedNodeInNew(edits, mNode, indexes, aManager);		
		}
	}
	
	private void markNodesInOld(List<MethodModification> mmList, List<Integer> instances, 
			List<List<Integer>> indexesList){
		MethodModification mm = null;
		Node mNode = null;
		markedNodes = new ArrayList<Node>();
		int tmpIndex = -1;
		List<AbstractTreeEditOperation> edits = null;
		AnalysisManager aManager = null;
		for (int i = 0; i < instances.size(); i++) {
			tmpIndex = instances.get(i);
			mm = mmList.get(tmpIndex);
			edits = mm.getEdits();
			mNode = (Node) ((Node)edits.get(0).getParentNode().getRoot()).deepCopy();
			markedNodes.add(mNode);
			aManager = new AnalysisManager(pr, CachedProjectMap.get(mm.newMethod.getProjectName()));
			EditedNodeMarker.markEditedNodeInOld(edits, mNode, indexesList.get(i), aManager);
		}
	}

	public void prepareForExtraction(EditInCommonCluster cluster,
			EditInCommonGroup group, CloneReductionController crc)
			throws Exception {
//		System.out.print("");
		List<MethodModification> mmList = group.getMMList();
		List<Node> newNodeList = cluster.getNewNodeList();
		List<Integer> instances = cluster.getInstances();
		List<Integer> reorderedInstances = new ArrayList<Integer>(instances);
		Collections.sort(reorderedInstances);
		List<List<Integer>> indexesList = cluster.getIndexesList();
		List<Map<String, String>> sTouList = cluster.getSpecificToUnifiedList();
		specificToUnifiedList = new MapList(sTouList.size());
		// 0. model trees in a standard way (convert some structures when
		// preserving semantics)
		// newNodeList = semanticsEqual(newNodeList, crc.newADTs);

		// 1. mark all changed nodes in the new version
		if(CloneReductionMain.refactoringOld){
			markNodesInOld(mmList, instances, indexesList);
		}else{
			markEditedNodes(mmList, newNodeList, instances, indexesList);
		}				
		if(!CloneReductionMain.hasMultiExamples){
			// 2. look for enclosing program structure to cover the edited part
			enlargeScope();

			// 3. check the possibility to reduce clone
			simpleCheck();

			// 4. find the common part between the enclosing program structures
			extractCommon();
		}else{
			enlargeScope();
			extractCommonWithFlexibility();
			MapList mapList = neChecker.getMapList();
			GeneralizedStmtIndexMap gStmtIndexMap = neChecker.getGeneralizedStmtIndexMap();
			Map<String, MethodInfo> unifiedToMethod0 = new HashMap<String, MethodInfo>();
			Map<String, ExpressionInfo> unifiedToExpression0 = new HashMap<String, ExpressionInfo>();
			Map<String, TypeInfo> unifiedToType0 = new HashMap<String, TypeInfo>();
			Map<String, VariableInfo> unifiedToVariable0 = new HashMap<String, VariableInfo>();
			TemplateMethodInterfaceConverter.prepareToConvert(
					gStmtIndexMap.getGeneralizedStmts(), mapList.get(0), unifiedToMethod0, 
					unifiedToExpression0, unifiedToType0, unifiedToVariable0, markedNodes.get(0).getMethodDeclaration());
			RefactoringMetaData.addUnifiedToMethod(unifiedToMethod0);
			RefactoringMetaData.addUnifiedToExpr(unifiedToExpression0);
			RefactoringMetaData.addUnifiedToType(unifiedToType0);
			RefactoringMetaData.addUnifiedToVariable(unifiedToVariable0);	
			Node mNode = null;
			List<List<SimpleASTNode>> stmtList = null;
			List<List<SimpleASTNode>> tmpStmtList = null;
			Map<String, String> sTou = null;
			Map<String, MethodInfo> unifiedToMethod = null;
			Map<String, TypeInfo> unifiedToType = null;
			Map<String, ExpressionInfo> unifiedToExpression = null;
			Map<String, VariableInfo> unifiedToVariable = null;
			SimpleASTCreator creator = new SimpleASTCreator();
			for(int i = 1; i < markedNodes.size(); i++){
				mNode = markedNodes.get(i);
				stmtList = creator.createSimpleASTNodesList(mNode);
				tmpStmtList = new ArrayList<List<SimpleASTNode>>();
				for(Integer index : gStmtIndexMap.getIndexes(i)){
					tmpStmtList.add(stmtList.get(index - 1));
				}
				sTou = mapList.get(i);
				tmpStmtList = SimpleASTNode.customize(sTou, tmpStmtList);
				unifiedToMethod = new HashMap<String, MethodInfo>();
				unifiedToExpression = new HashMap<String, ExpressionInfo>();
				unifiedToType = new HashMap<String, TypeInfo>();
				unifiedToVariable = new HashMap<String, VariableInfo>();
				TemplateMethodInterfaceConverter.prepareToConvert(tmpStmtList, sTou, unifiedToMethod, 
						unifiedToExpression, unifiedToType, unifiedToVariable, mNode.getMethodDeclaration());		
				RefactoringMetaData.addUnifiedToMethod(unifiedToMethod);
				RefactoringMetaData.addUnifiedToExpr(unifiedToExpression);
				if(unifiedToType.size() < unifiedToType0.size()){
					String key = null;
					TypeInfoCreator tCreator = new TypeInfoCreator();
					TypeInfo tInfo = null;
					for(Entry<String, TypeInfo> entry : unifiedToType0.entrySet()){
						key = entry.getKey();
						if(unifiedToType.containsKey(key))
							continue;
						tInfo = tCreator.createForT(key, sTou);
						unifiedToType.put(key, tInfo);
					}
				}
				RefactoringMetaData.addUnifiedToType(unifiedToType);
				RefactoringMetaData.addUnifiedToVariable(unifiedToVariable);
			}
			RefactoringMetaData.adjustUnifiedToExpressions();			
			RefactoringMetaData.addUnifiedToMethodRelations();
			RefactoringMetaData.addUnifiedToTypeRelations();
		}		
		// 5. decide whether extraction possibility based on general identifiers
		// for types, methods, and variables
		// checkCloneReduction(crc.rel);
	}

	protected void simpleCheck() throws CloneReductionException {
		Set<Node> context = contexts.get(0);
		int counter = 0;
		// Constraint 1: in the new version of a changed method, there should be
		// at least one
		// modified nodes
		for (Node n : context) {
			if (n.getRole() != SimpleTreeNode.NONE) {
//				if (n.getRole() == SimpleTreeNode.EDITED) {
					// Constraint 2: if the edited node is a switch case, do not
					// extract method
//					if (n.getNodeType() == ASTNode.SWITCH_CASE) {
//						throw new CloneReductionException(
//								"One of the edited node is Switch Case");
//					}
//				}
				counter++;
			}
		}
		if (counter == 0) {
			throw new CloneReductionException(
					"The number of nodes to extract is 0");
		}
		Node markedNode = markedNodes.get(0);
		Enumeration<Node> nEnum = markedNode.breadthFirstEnumeration();
		Node tmpNode = null;
		while (nEnum.hasMoreElements()) {
			tmpNode = nEnum.nextElement();
			if (tmpNode.getRole() != SimpleTreeNode.NONE)
				break;
		}
		// Constraint 3: if the marked nodes are not consecutive, do not extract
		nEnum = tmpNode.children();
		int lastMarked = -1;
		int nodeIndex = 0;
		while (nEnum.hasMoreElements()) {
			tmpNode = nEnum.nextElement();
			if (tmpNode.getRole() != SimpleTreeNode.NONE) {
				if (lastMarked == -1) {
					// do nothing
				} else if (lastMarked + 1 != nodeIndex) {
					throw new CloneReductionException(
							"The marked nodes are not consecutive");
				}
				lastMarked = nodeIndex;
			}
			nodeIndex++;
		}
	}

	public List<Set<Node>> getContexts() {
		return contexts;
	}

	public List<Set<SourceCodeRange>> getRangesList() {
		return rangesList;
	}

	public List<SimpleTreeNode> getOriginalSNodes() {
		return originalSNodes;
	}

	public List<List<SimpleTreeNode>> getPartialSNodes() {
		return partialSNodes;
	}

	public List<SimpleTreeNode> getpSNodes() {
		return pSNodes;
	}

	public void setpSNodes(List<SimpleTreeNode> pSNodes) {
		this.pSNodes = pSNodes;
	}

	public List<List<List<SimpleASTNode>>> getCustomizedNodesLists() {
		return customizedNodesLists;
	}

	public void setCustomizedNodesLists(
			List<List<List<SimpleASTNode>>> customizedNodesLists) {
		this.customizedNodesLists = customizedNodesLists;
	}

	public MapList getSpecificToUnifiedList() {
		return specificToUnifiedList;
	}

	public List<Node> getMarkedNodes() {
		return markedNodes;
	}
	
	public List<List<Node>> getMarkedNodesList(){
		return markedNodesList;
	}
	// private List<Node> semanticsEqual(List<Node> nodeList,
	// List<ChangedMethodADT> adts) {
	// List<Node> newNodeList = new ArrayList<Node>();
	// SemanticsEqualConversion converter = new SemanticsEqualConversion(pr);
	// for (int i = 0; i < adts.size(); i++) {
	// newNodeList.add(converter.convert(nodeList.get(i), adts.get(i)));
	// }
	// return newNodeList;
	// }

	public List<List<List<SimpleASTNode>>> getPartiallyCustomizedNodesLists() {
		return partiallyCustomizedNodesLists;
	}

	public NodeEquivalenceChecker getNeChecker() {
		return neChecker;
	}
	
}
