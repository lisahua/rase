package changeassistant.clonereduction.manipulate;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IVariableBinding;

import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;
import changeassistant.classhierarchy.ProjectResource;
import changeassistant.clonedetection.util.TypeChecker;
import changeassistant.clonereduction.datastructure.FlowLabel;
import changeassistant.clonereduction.helper.ClassContextHelper;
import changeassistant.clonereduction.main.CloneReductionMain;
import changeassistant.clonereduction.manipulate.refactoring.RefactoringMetaData;
import changeassistant.clonereduction.pdg.GFlowEdge.CATEGORY;
import changeassistant.clonereduction.pdg.Graph;
import changeassistant.crystal.FieldVariable;
import changeassistant.multipleexample.datastructure.GeneralizedStmtIndexMap;
import changeassistant.multipleexample.main.CachedProjectMap;
import changeassistant.multipleexample.match.PatternUtil;
import changeassistant.multipleexample.partition.SimpleASTCreator;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.staticanalysis.AnalysisManager;
import changeassistant.multipleexample.util.IdMapper;
import changeassistant.peers.SourceCodeRange;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.comparison.ChangedMethodADT;
import changeassistant.versions.comparison.MethodModification;
import changeassistant.versions.treematching.edits.AbstractTreeEditOperation;
import edu.cmu.cs.crystal.tac.model.ThisVariable;
import edu.cmu.cs.crystal.tac.model.TypeVariable;
import edu.cmu.cs.crystal.tac.model.Variable;

public class CloneReductionPDGCreator {

	private Set<VariableTypeBindingTerm> inputTerms;
	private Set<VariableTypeBindingTerm> inputFieldTerms;
	private Set<VariableTypeBindingTerm> outputTerms;
	private Set<VariableTypeBindingTerm> outputFieldTerms;
	private Set<VariableTypeBindingTerm> localTerms;
	private Map<String, FieldVariable> fields = null;
	private List<String> exceptionTypes = null;
	private Set<Node> returnNodes = null;
	private Set<Node> throwNodes = null;
	private Set<Node> flowNodes = null;
//	private boolean pureReturn = false;

	private ProjectResource pr = null;
	private AnalysisManager aManager = null;

	public CloneReductionPDGCreator(ProjectResource pr) {
		this.pr = pr;
	}
	
	// need processing for generalized identifiers
	public void drawPDG(MethodModification mm, Node mNode, 
			List<Integer> indexes, Map<Integer, List<SimpleASTNode>> indexStmtMap, 
			Map<Node, Integer> nodeIndexMap, Map<String, String> sTou){
		if(CloneReductionMain.refactoringOld){
			aManager = new AnalysisManager(pr, CachedProjectMap.get(mm.newMethod.getProjectName()));
			aManager.setMethodModification(mm);
			fields = new HashMap<String, FieldVariable>(aManager.getOldDefUseElementResult().getFieldMap());
		}else{
			aManager = new AnalysisManager(CachedProjectMap.get(mm.originalMethod.getProjectName()), pr);
			aManager.setMethodModification(mm);
			fields = new HashMap<String, FieldVariable>(aManager.getNewDefUseElementResult().getFieldMap());
		}		
		removePublicStaticFields();
		Graph graph = new Graph(fields);
		Map<String, Set<Node>> varNodesMap = null;
		Set<Node> controlNodes = null;
		Map<FlowLabel, Node> labelNodeMap = null;
		returnNodes = new HashSet<Node>();
		throwNodes = new HashSet<Node>();
		flowNodes = new HashSet<Node>();
		Enumeration<Node> bEnum = mNode.breadthFirstEnumeration();
		Node tmp = null;
		int index = 1;
		Set<Integer> includedIndexes = new HashSet<Integer>(indexes);
		Set<SourceCodeRange> includedRanges = new HashSet<SourceCodeRange>();
		Set<Node> context = new HashSet<Node>(); 
		while(bEnum.hasMoreElements()){
			tmp = bEnum.nextElement();
			if(includedIndexes.contains(index)){
				context.add(tmp);
				includedRanges.add(tmp.getSourceCodeRange());
			}
			index++;
		}
		if(CloneReductionMain.refactoringOld){
			for(Node n : context){
				if(n.equals(mNode))
					continue;
				varNodesMap = aManager.findOldDataDependingNodes(n);
				graph.addDataEdges(varNodesMap, n);
				varNodesMap = aManager.findOldDataDependedNodes(n);
				graph.addDataEdges(n, varNodesMap);
				controlNodes = aManager.findOldControlDependingNodes(n);
				graph.addControlEdges(controlNodes, n);
				labelNodeMap = createLabelNodeMap(n, aManager.findOldFlowSinks(n, includedRanges), includedRanges);
				graph.addFlowEdges(n, labelNodeMap);				
			}
		}else{
			for(Node n : context){			
				if(n.equals(mNode))
					continue;
				varNodesMap = aManager.findNewDataDependingNodes(n);
				graph.addDataEdges(varNodesMap, n);
				varNodesMap = aManager.findNewDataDependedNodes(n);
				graph.addDataEdges(n, varNodesMap);
				controlNodes = aManager.findNewControlDependingNodes(n);
				graph.addControlEdges(controlNodes, n);
				labelNodeMap = createLabelNodeMap(n, aManager.findNewFlowSinks(n, includedRanges), includedRanges);
				graph.addFlowEdges(n, labelNodeMap);
			}
		}
		

		List<SimpleASTNode> simpleASTNodes = null;
		SimpleASTCreator creator = new SimpleASTCreator();
		Set<Term> terms = null;
		Set<String> qualifiedNames = null;
		Set<VariableTypeBindingTerm> vTerms = null;
		Set<VariableTypeBindingTerm> gvTerms = null;
		Map<String, VariableTypeBindingTerm> gvTermMap = null;
		Set<VariableTypeBindingTerm> allVTerms = null;
		inputTerms = new HashSet<VariableTypeBindingTerm>();
		inputFieldTerms = new HashSet<VariableTypeBindingTerm>();
		outputTerms = new HashSet<VariableTypeBindingTerm>();
		outputFieldTerms = new HashSet<VariableTypeBindingTerm>();
		localTerms = new HashSet<VariableTypeBindingTerm>();
		exceptionTypes = new ArrayList<String>();
		
		Set<String> definedVars = new HashSet<String>();
		Set<String> usedVars = new HashSet<String>();

		qualifiedNames = new HashSet<String>();
		allVTerms = new HashSet<VariableTypeBindingTerm>();
		List<SimpleASTNode> gStmt = null;
		Set<Term> gTerms = null;
		for (Node node : context) {
			List<ASTNode> exprs = node.getASTExpressions2();
			simpleASTNodes = new ArrayList<SimpleASTNode>();
			terms = new HashSet<Term>();
			vTerms = new HashSet<VariableTypeBindingTerm>();
			for (ASTNode expr : exprs) {
				simpleASTNodes.add(creator.createSimpleASTNode(expr));
			}
			PatternUtil.collectTerms(terms, simpleASTNodes);			
			PatternUtil.collectQualifiedNames(qualifiedNames, simpleASTNodes);
			gStmt = indexStmtMap.get(nodeIndexMap.get(node));
			gTerms = new HashSet<Term>();
			PatternUtil.collectTerms(gTerms, gStmt);	
			vTerms = Term.getVTerms(terms);
			gvTerms = Term.getVTerms(gTerms);
			gvTermMap = Term.getVTermMap(Term.getVTerms(gTerms));			
			if (!vTerms.isEmpty()) {
				collectInputs(includedRanges, graph, vTerms, gvTermMap, node, sTou, qualifiedNames);
				collectOutputs(includedRanges, graph, vTerms, gvTermMap, node, sTou);
				collectLocals(includedRanges, graph, vTerms, gvTerms, node, definedVars, usedVars, sTou);
			}
			graph.processLabels(node, exceptionTypes, throwNodes, returnNodes,
					flowNodes);
			allVTerms.addAll(vTerms);
		}
		processLocals(definedVars, usedVars, allVTerms, sTou);		
		removeRedundantExceptions();
//		checkPureReturns(context);
		removeInvalidInputTerms(qualifiedNames, sTou);
		RefactoringMetaData.setInputTerms(inputTerms);
		RefactoringMetaData.setInputFieldTerms(inputFieldTerms);
		RefactoringMetaData.setOutputTerms(outputTerms);
		RefactoringMetaData.setOutputFieldTerms(outputFieldTerms);
		RefactoringMetaData.setLocalTerms(localTerms);
		RefactoringMetaData.setReturnNodes(returnNodes);
		RefactoringMetaData.setFlowNodes(flowNodes);
		RefactoringMetaData.setThrowNodes(throwNodes);
		RefactoringMetaData.setExceptionTypes(exceptionTypes);
		RefactoringMetaData.setRetRelatedFlags();
	}
	
	/**
	 * Relevant RefactoringMetaData is also set here
	 * @param mm
	 * @param includedRanges
	 * @param context
	 */
	public void drawPDG(MethodModification mm,
			Set<SourceCodeRange> includedRanges, Set<Node> context) {
		/*
		if(CloneReductionMain.refactoringOld){
			aManager = new AnalysisManager(pr, CachedProjectMap.get(mm.newMethod.getProjectName()));
			aManager.setMethodModification(mm);
			fields = new HashMap<String, FieldVariable>(aManager.getOldDefUseElementResult().getFieldMap());
		}else{
			aManager = new AnalysisManager(CachedProjectMap.get(mm.originalMethod
					.getProjectName()), pr);
			aManager.setMethodModification(mm);
			fields = new HashMap<String, FieldVariable>(aManager.getNewDefUseElementResult().getFieldMap());
		}				
		removePublicStaticFields();
		Graph graph = new Graph(fields);
		Map<String, Set<Node>> varNodesMap = null;
		Set<Node> controlNodes = null;
		Map<FlowLabel, Node> labelNodeMap = null;
		returnNodes = new HashSet<Node>();
		throwNodes = new HashSet<Node>();
		flowNodes = new HashSet<Node>();
		for (Node node : context) {
			if (node.equals(node.getRoot()))// the node is the MethodDeclaration
											// itself
				continue;
			if(CloneReductionMain.refactoringOld){
				varNodesMap = aManager.findOldDataDependingNodes(node);
				graph.addDataEdges(varNodesMap, node);
				varNodesMap = aManager.findOldDataDependedNodes(node);
				graph.addDataEdges(node, varNodesMap);
				controlNodes = aManager.findOldControlDependingNodes(node);
				graph.addControlEdges(controlNodes, node);
				labelNodeMap = createLabelNodeMap(node, aManager.findOldFlowSinks(node, includedRanges), includedRanges);
				graph.addFlowEdges(node, labelNodeMap);
			}else{
				varNodesMap = aManager.findNewDataDependingNodes(node);
				graph.addDataEdges(varNodesMap, node);
				varNodesMap = aManager.findNewDataDependedNodes(node);
				graph.addDataEdges(node, varNodesMap);
				controlNodes = aManager.findNewControlDependingNodes(node);
				graph.addControlEdges(controlNodes, node);
				labelNodeMap = createLabelNodeMap(node,
						aManager.findNewFlowSinks(node, includedRanges), includedRanges);
				graph.addFlowEdges(node, labelNodeMap);
			}			
		}

		List<SimpleASTNode> simpleASTNodes = null;
		SimpleASTCreator creator = new SimpleASTCreator();
		Set<Term> terms = null;
		Set<String> qualifiedNames = null;
		Set<VariableTypeBindingTerm> vTerms = null;
		Set<VariableTypeBindingTerm> allVTerms = null;
		inputTerms = new HashSet<VariableTypeBindingTerm>();
		inputFieldTerms = new HashSet<VariableTypeBindingTerm>();
		outputTerms = new HashSet<VariableTypeBindingTerm>();
		outputFieldTerms = new HashSet<VariableTypeBindingTerm>();
		localTerms = new HashSet<VariableTypeBindingTerm>();
		exceptionTypes = new ArrayList<String>();
		
		Set<String> definedVars = new HashSet<String>();
		Set<String> usedVars = new HashSet<String>();

		qualifiedNames = new HashSet<String>();
		allVTerms = new HashSet<VariableTypeBindingTerm>();
		for (Node node : context) {
			List<ASTNode> exprs = node.getASTExpressions2();
			simpleASTNodes = new ArrayList<SimpleASTNode>();
			terms = new HashSet<Term>();
			vTerms = new HashSet<VariableTypeBindingTerm>();
			for (ASTNode expr : exprs) {
				simpleASTNodes.add(creator.createSimpleASTNode(expr));
			}
			PatternUtil.collectTerms(terms, simpleASTNodes);
			PatternUtil.collectQualifiedNames(qualifiedNames, simpleASTNodes);
			for (Term t : terms) {
				if (t.getTermType().equals(
						Term.TermType.VariableTypeBindingTerm)) {
					vTerms.add((VariableTypeBindingTerm) t);
				}
			}
			if (!vTerms.isEmpty()) {
				collectInputs(includedRanges, graph, vTerms, null, node, null);
				collectOutputs(includedRanges, graph, vTerms, null, node, null);
				collectLocals(includedRanges, graph, vTerms, null, node, definedVars, usedVars, null);
			}
			graph.processLabels(node, exceptionTypes, throwNodes, returnNodes,
					flowNodes);
			allVTerms.addAll(vTerms);
		}
		processLocals(definedVars, usedVars, allVTerms, null);		
		removeRedundantExceptions();
//		checkPureReturns(context);
		removeInvalidInputTerms(qualifiedNames);
		RefactoringMetaData.setInputTerms(inputTerms);
		RefactoringMetaData.setInputFieldTerms(inputFieldTerms);
		RefactoringMetaData.setOutputTerms(outputTerms);
		RefactoringMetaData.setOutputFieldTerms(outputFieldTerms);
		RefactoringMetaData.setLocalTerms(localTerms);
		RefactoringMetaData.setReturnNodes(returnNodes);
		RefactoringMetaData.setFlowNodes(flowNodes);
		RefactoringMetaData.setThrowNodes(throwNodes);
		RefactoringMetaData.setExceptionTypes(exceptionTypes);
		RefactoringMetaData.setRetRelatedFlags();
		*/
	}

	private Map<FlowLabel, Node> createLabelNodeMap(Node source,
			Map<String, Node> contentNodeMap,
			Set<SourceCodeRange> includedRanges) {
		Map<FlowLabel, Node> map = new HashMap<FlowLabel, Node>();
		int nodeType = source.getNodeType();
		CATEGORY category = getCategory(nodeType);
		Node node = null;
		if(nodeType == ASTNode.RETURN_STATEMENT){
			for (Entry<String, Node> entry : contentNodeMap.entrySet()) {
				node = entry.getValue();
				if(category.equals(CATEGORY.NONE) && node.getNodeType() == ASTNode.METHOD_DECLARATION){
					category = CATEGORY.RETURN;
				}
				map.put(new FlowLabel(category, entry.getKey()), entry.getValue());
			}
		}else{
			for (Entry<String, Node> entry : contentNodeMap.entrySet()) {
				node = entry.getValue();
				if (!includedRanges.contains(node.getSourceCodeRange())) {
					map.put(new FlowLabel(category, entry.getKey()),
							entry.getValue());
				}
			}
		}		
		return map;
	}

	private CATEGORY getCategory(int nodeType) {
		switch (nodeType) {
		case ASTNode.THROW_STATEMENT:
			return CATEGORY.THROW;
		case ASTNode.RETURN_STATEMENT:
			return CATEGORY.RETURN;
		}
		return CATEGORY.NONE;
	}
	
	private void removeInvalidTerms(Set<String> qualifiedNames, Set<VariableTypeBindingTerm> vTerms){
		Set<VariableTypeBindingTerm> invalidTerms = new HashSet<VariableTypeBindingTerm>();
		char ch;
		for (VariableTypeBindingTerm term : vTerms) {
			String tName = term.getName();
			ch = tName.charAt(0);
			if(ch <= 'z' && ch >= 'a'){
				// do nothing
			}else{
				for (String qName : qualifiedNames) {
					if (qName.contains(tName)
							&& (qName.contains(tName + ".") || qName.contains("."
									+ tName))) {
						invalidTerms.add(term);
						break;
					}
				}
			}			
		}
		vTerms.removeAll(invalidTerms);
	}
	
	private void removeInvalidTerms(Set<String> qualifiedNames, Set<VariableTypeBindingTerm> vTerms, Map<String, String> sTou){
		Set<VariableTypeBindingTerm> invalidTerms = new HashSet<VariableTypeBindingTerm>();
		char ch;
		for (VariableTypeBindingTerm term : vTerms) {
			String tName = term.getName();
			ch = tName.charAt(0);
			if(ch <= 'z' && ch >= 'a'){
				// do nothing
			}else{
				for (String qName : qualifiedNames) {
					if (qName.contains(tName)
							&& (qName.contains(tName + ".") || qName.contains("."
									+ tName))) {
						if(sTou.containsKey(tName) && Term.ExactAbsPattern.matcher(sTou.get(tName)).matches()){
							break;
						}else{
							invalidTerms.add(term);
							break;
						}
					}
				}
			}			
		}
		vTerms.removeAll(invalidTerms);
	}

	private void removeInvalidInputTerms(Set<String> qualifiedNames, Map<String, String> sTou) {
		removeInvalidTerms(qualifiedNames, inputTerms, sTou);
		removeInvalidTerms(qualifiedNames, inputFieldTerms);
	}

	private void removeRedundantExceptions() {
		ClassContextHelper ccHelper = new ClassContextHelper();
		Set<String> typeSet = new HashSet<String>(exceptionTypes);
		exceptionTypes = ccHelper.mergeExceptions(pr,
				typeSet.toArray(new String[typeSet.size()]));
	}

	private void removePublicStaticFields(){
		int modifiers = -1;
		FieldVariable fv = null;
		Variable v = null;
		Set<String> keysToRemove = new HashSet<String>();
		for(Entry<String, FieldVariable> entry : fields.entrySet()){
			fv = entry.getValue();
			v = fv.getObjectAccessed();
			if (v instanceof ThisVariable)
				continue;
			IVariableBinding binding = fv.getBinding();
			if(binding != null){
				modifiers = binding.getModifiers();
				if((modifiers & Modifier.PUBLIC) != 0 && (modifiers & Modifier.STATIC) != 0){
					keysToRemove.add(entry.getKey());
				}
			}			
		}		
		for(String key : keysToRemove){
			fields.remove(key);
		}
	}
	
	private void collectLocals(Set<SourceCodeRange> includedRanges, Graph graph, 
			Set<VariableTypeBindingTerm> vTerms, Set<VariableTypeBindingTerm> gvTerms, Node node,
			Set<String> definedVars, Set<String> usedVars, Map<String, String> sTou){	
		Set<String> defined = graph.getDefWithoutUseOutsideVars(node.getSourceCodeRange(), includedRanges);
		Set<String> used = graph.getUseWithDefOutsideVars(node.getSourceCodeRange(), includedRanges);
		for(String var : defined){
			if(sTou.containsKey(var)){
				definedVars.add(sTou.get(var));
			}else{
				definedVars.add(var);
			}
		}
		for(String var : used){
			if(sTou.containsKey(var)){
				usedVars.add(sTou.get(var));
			}else{
				usedVars.add(var);
			}
		}		
	}
	
	private void processLocals(Set<String> definedVars, Set<String> usedVars, Set<VariableTypeBindingTerm> vTerms,
			Map<String, String> sTou){
		definedVars.removeAll(usedVars);
		String var = null;
		String counterpart = null;
		//find local def without outside use
		for(VariableTypeBindingTerm vTerm : vTerms){
			var = vTerm.getName();
			counterpart = getCounterpartField(var);
			if(counterpart == null){
				if(definedVars.contains(var)){
					localTerms.add(vTerm);
				}else if(sTou.containsKey(var) && definedVars.contains(sTou.get(var))){
					localTerms.add(vTerm);
				}				
			}			
		}
	}

	private void collectOutputs(Set<SourceCodeRange> includedRanges,
			Graph graph, Set<VariableTypeBindingTerm> vTerms, 
			Map<String, VariableTypeBindingTerm> gvTermMap, Node node, Map<String, String> sTou) {
		String var = null;
		// find local def no use
		Set<String> vars = graph.getDefWithUseOutsideVars(node.getSourceCodeRange(),
				includedRanges);
		String counterpart = null;
		for (VariableTypeBindingTerm vTerm : vTerms) {
			var = vTerm.getName();
			counterpart = getCounterpartField(var);
			if(counterpart != null && vars.contains(counterpart)){
				if (inputFieldTerms.contains(vTerm)
						&& !TypeChecker.isPassByValueType(vTerm
								.getTypeNameTerm().getName())) {
					// do nothing
				} else {
					appendTerm(outputFieldTerms, vTerm, var, sTou);
//					outputFieldTerms.add(vTerm);
				}
			}else if(vars.contains(var)){
				if (inputTerms.contains(vTerm)
						&& !TypeChecker.isPassByValueType(vTerm
								.getTypeNameTerm().getName())) {
					// do nothing
				} else {
					appendTerm(outputTerms, vTerm, var, sTou);
//					outputTerms.add(vTerm);
				}
			}
		}
	}
	
	private void appendTerm(Set<VariableTypeBindingTerm> terms, VariableTypeBindingTerm vTerm,
			String var, Map<String, String> sTou){
		if(sTou.containsKey(var)){
			terms.add(vTerm);
		}
	}

	private void collectInputs(Set<SourceCodeRange> includedRanges,
			Graph graph, Set<VariableTypeBindingTerm> vTerms, Map<String, VariableTypeBindingTerm> gvTermMap, 
			Node node, Map<String, String> sTou, Set<String> qualifiedNames) {		
		Set<String> vars = graph.getUseWithDefOutsideVars(
				node.getSourceCodeRange(), includedRanges);		
		String var = null;
		String counterpart = null;
		// find local use no def
		
		for (VariableTypeBindingTerm vTerm : vTerms) {
			var = vTerm.getName();
			counterpart = getCounterpartField(var);
//			tmpTerm = gvTermMap.get(sTou.get(var));
			if(counterpart != null && vars.contains(counterpart) && !inputFieldTerms.contains(vTerm)){
				appendTerm(inputFieldTerms, vTerm, var, sTou);
//				inputFieldTerms.add(vTerm);
			}else if(vars.contains(var)){
				// This is actually a type term
				if (var.equals(vTerm.getTypeNameTerm().getName())) {
					continue;
				}
				int nodeType = vTerm.getNodeType();
				if (Term.IndexToExpr.containsKey(nodeType)
						&& Term.IndexToExpr.get(nodeType).endsWith(
								"Literal")) {
					// do nothing
				} else {
					appendTerm(inputTerms, vTerm, var, sTou);
//					inputTerms.add(vTerm);
				}
			}
		}
		for(String qName : qualifiedNames){
			for(String tmpVar : vars){
				if(tmpVar.equals(qName) || tmpVar.endsWith(qName)){
					String[] nameSegs = qName.split("\\.");
					for(String nameSeg: nameSegs){
						if(sTou.containsKey(nameSeg) && Term.V_Pattern.matcher(sTou.get(nameSeg)).matches()){
							for(VariableTypeBindingTerm vTerm : vTerms){
								if(vTerm.getName().equals(nameSeg)){
									appendTerm(inputTerms, vTerm, nameSeg, sTou);
								}
							}
						}
					}
					break;
				}
			}
		}
	}
	
	private String getCounterpartField(String var){
		String key = null;
		FieldVariable fv = null;
		Variable v = null;
		for (Entry<String, FieldVariable> entry : fields.entrySet()) {
			key = entry.getKey();
			fv = entry.getValue();
			v = fv.getObjectAccessed();
			if (v instanceof ThisVariable) {
				if (key.substring("this.".length()).equals(var)) {
					return key;
				}
			}else if(v instanceof TypeVariable){
				if(key.substring(v.toString().length() + 1).equals(var)){
					return key;
				}
			}
		}
		return null;
	}

	private boolean isField(String var) {
		if (fields.containsKey(var))
			return true;
		FieldVariable fv = null;
		String key = null;
		Variable v = null;
		for (Entry<String, FieldVariable> entry : fields.entrySet()) {
			key = entry.getKey();
			fv = entry.getValue();
			v = fv.getObjectAccessed();
			if (v instanceof ThisVariable) {
				if (key.substring("this.".length()).equals(var)) {
					return true;
				}
			}
		}
		return false;
	}

//	public boolean isPureReturn() {
//		return pureReturn;
//	}

//	public Set<Node> getReturnRelevantNodes() {
//		Set<Node> result = new HashSet<Node>();
//		Enumeration<Node> nEnum = null;
//		for (Node node : returnNodes) {
//			result.addAll(aManager
//					.findNewControlDependingNodesTransitively(node));
//			nEnum = node.pathFromAncestorEnumeration(node.getRoot());
//			while (nEnum.hasMoreElements()) {
//				result.add(nEnum.nextElement());
//			}
//		}
//		return result;
//	}
}
