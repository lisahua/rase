package changeassistant.clonereduction.manipulate.refactoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;

import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;
import changeassistant.clonereduction.manipulate.data.ExpressionInfo;
import changeassistant.clonereduction.manipulate.data.MethodInfo;
import changeassistant.clonereduction.manipulate.data.TypeInfo;
import changeassistant.clonereduction.manipulate.data.VariableInfo;
import changeassistant.multipleexample.main.Constants;
import changeassistant.peers.comparison.Node;

public class RefactoringMetaData {
	private static boolean needExitFlags = false;
	private static boolean needRetObj = false;
	private static boolean hasRetVal = false;
	private static boolean hasImplicitThis = false;
	private static boolean needTemplateClass = false;
	private static boolean needTemplateHierarchy = false;
	private static boolean needTypeParameters = false;
	private static boolean needVariableDeclaration = false;
	private static boolean needExprParams = false;
	private static boolean needVariableParameters = false;
	private static Set<VariableTypeBindingTerm> inputTerms = null;
	private static Set<VariableTypeBindingTerm> inputFieldTerms = null;
	private static Set<VariableTypeBindingTerm> outputTerms = null;
	private static Set<VariableTypeBindingTerm> outputFieldTerms = null;
	private static Set<VariableTypeBindingTerm> localTerms = null;
	private static VariableTypeBindingTerm termToReturn = null;
	private static List<String> exceptionTypes = null;
	private static Set<Node> returnNodes = null;
	private static Set<Node> throwNodes = null;
	private static Set<Node> flowNodes = null;
	//returnType can be RetObj--the actual return type of the invoked extracted method
	private static String returnType = null;
	//returnValType--one of the return values from the extracted method 
	private static String returnValType = null;
	private static List<String> basicParamList = null;
	private static List<String> basicParamTypeList = null;
	private static List<String> extendedParamList = null;
	private static List<String> extendedParamTypeList = null;
	private static List<String> exprParamList = null;
	private static List<String> exprParamTypeList = null;
	private static List<Map<String, MethodInfo>> unifiedToMethodList = new ArrayList<Map<String, MethodInfo>>();
	private static List<Map<String, ExpressionInfo>> unifiedToExprList = new ArrayList<Map<String, ExpressionInfo>>();
	private static List<Map<String, TypeInfo>> unifiedToTypeList = new ArrayList<Map<String, TypeInfo>>();
	private static List<Map<String, VariableInfo>> unifiedToVariableList = new ArrayList<Map<String, VariableInfo>>();
	//used for interpreting RetObj
	private static List<String> returnNames;
	private static List<String> returnTypes;
	private static List<Boolean> needDeclForInterFlags;
	private static Map<Integer, Set<Integer>> unifiedToMethodRelations;
	private static Map<Integer, Set<Integer>> unifiedToTypeRelations;
	
	public static void clear(){
		needExitFlags = false;
		needRetObj = false;
		hasRetVal = false;
		hasImplicitThis = false;
		needTemplateClass = false;
		needTemplateHierarchy = false;
		needTypeParameters = false;
		needVariableDeclaration = false;
		needVariableParameters = false;
		inputTerms = null;
		inputFieldTerms = null;
		outputTerms = null;
		outputFieldTerms = null;
		termToReturn = null;
		exceptionTypes = null;
		returnNodes = null;
		throwNodes = null;
		flowNodes = null;
		returnType = null;
		returnValType = null;
		basicParamList = null;
		basicParamTypeList = null;
		extendedParamList = null;
		extendedParamTypeList = null;
		exprParamList = null;
		exprParamTypeList = null;
		unifiedToMethodList.clear();
		unifiedToExprList.clear();
		unifiedToTypeList.clear();
		unifiedToVariableList.clear();
		returnNames = null;
		returnTypes = null;
		needDeclForInterFlags = null;
	}
	
	public static void setNeedExitFlags(){
		needExitFlags = true;
	}
	
	public static void setNeedRetObj(){
		needRetObj = true;
	}
	
	public static void setNeedTemplateClass(){
		needTemplateClass = true;
	}
	
	public static void setHasRetVal(){
		hasRetVal = true;
	}
	
	public static boolean isNeedExitFlags(){
		return needExitFlags;
	}
	
	public static boolean isNeedRetObj(){
		return needRetObj;
	}
	
	public static boolean isNeedTemplateClass(){
		return needTemplateClass;
	}
	
	public static boolean isNeedVariableParameters(){
		return needVariableParameters;
	}
	
	public static boolean isHasRetVal(){
		return hasRetVal;
	}

	public static Set<VariableTypeBindingTerm> getInputTerms() {
		return inputTerms;
	}

	public static void setInputTerms(Set<VariableTypeBindingTerm> inputTerms) {
		RefactoringMetaData.inputTerms = inputTerms;
	}

	public static Set<VariableTypeBindingTerm> getInputFieldTerms() {
		return inputFieldTerms;
	}

	public static void setInputFieldTerms(
			Set<VariableTypeBindingTerm> inputFieldTerms) {
		RefactoringMetaData.inputFieldTerms = inputFieldTerms;
	}
	
	public static Set<VariableTypeBindingTerm> getOutputTerms(){
		return outputTerms;
	}

	public static void setOutputTerms(Set<VariableTypeBindingTerm> outputTerms) {
		RefactoringMetaData.outputTerms = outputTerms;
	}
	
	public static Set<VariableTypeBindingTerm> getOutputFieldTerms(){
		return outputFieldTerms;
	}

	public static void setOutputFieldTerms(
			Set<VariableTypeBindingTerm> outputFieldTerms) {
		RefactoringMetaData.outputFieldTerms = outputFieldTerms;
	}

	public static List<String> getExceptionTypes() {
		return exceptionTypes;
	}

	public static void setExceptionTypes(List<String> exceptionTypes) {
		RefactoringMetaData.exceptionTypes = exceptionTypes;
	}

	public static Set<Node> getReturnNodes() {
		return returnNodes;
	}

	public static void setReturnNodes(Set<Node> rNodes) {
		returnNodes = rNodes;
	}

	public static Set<Node> getThrowNodes() {
		return throwNodes;
	}

	public static void setThrowNodes(Set<Node> throwNodes) {
		RefactoringMetaData.throwNodes = throwNodes;
	}

	public static Set<Node> getFlowNodes() {
		return flowNodes;
	}

	public static void setFlowNodes(Set<Node> flowNodes) {
		RefactoringMetaData.flowNodes = flowNodes;
	}
	
	public static void adjustUnifiedToExpressions(){
		Map<String, ExpressionInfo> uToe0 = unifiedToExprList.get(0);
		Map<String, ExpressionInfo> uToe = null;
		String key = null;
		ExpressionInfo eInfo0 = null, eInfo = null;
		Set<String> values = null;
		String oldType = null, newType = null;
		for(Entry<String, ExpressionInfo> entry : uToe0.entrySet()){
			key = entry.getKey();
			eInfo0 = entry.getValue();
			values = new HashSet<String>();
			values.add(eInfo0.type);
			for(int i = 1; i < unifiedToExprList.size(); i++){
				uToe = unifiedToExprList.get(i);
				values.add(uToe.get(key).type);
			}
			if(values.contains("null")){
				values.remove("null");
			}
			newType = values.iterator().next();
			for(int i = 0; i < unifiedToExprList.size(); i++){
				uToe = unifiedToExprList.get(i);
				eInfo = uToe.get(key);
				oldType = eInfo.type;
				if(oldType.equals("null")){
					eInfo.type = newType;
				}
			}
		}
		
	}
	
	
	public static void setRetRelatedFlags(){
		returnNames = new ArrayList<String>();
		returnTypes = new ArrayList<String>();
		needDeclForInterFlags = new ArrayList<Boolean>();
		List<ASTNode> exprs = null;
		if(!returnNodes.isEmpty()){
			if(!flowNodes.isEmpty()){
				setNeedExitFlags();
				returnNames.add(Constants.FLAG_VAR);
				returnTypes.add(Constants.FLAG);
				needDeclForInterFlags.add(false);
			}
			exprs = returnNodes.iterator().next().getASTExpressions2();
			if(!exprs.isEmpty()){
				setHasRetVal();
			}
		}
		int retValNum = outputTerms.size();
		if(isNeedTemplateClass()){
			retValNum += outputFieldTerms.size();
		}
		if(isHasRetVal()){			
			for(Node rNode : returnNodes){
				returnValType = ((Expression)rNode.getASTExpressions2().get(0))
					.resolveTypeBinding().getName();
				if(returnValType.equals("null")){
					continue;
				}
				if(isNeedTypeParameters()){
					boolean foundAbstractType = false;
					for(Map<String, TypeInfo> map : unifiedToTypeList){
						TypeInfo value = null;
						for(Entry<String, TypeInfo> entry : map.entrySet()){
							value = entry.getValue();
							if(value.content.equals(returnValType)){
								returnValType = value.name;
								foundAbstractType = true;
								break;
							}
						}
						if(foundAbstractType)
							break;
					}
				}else{
					break;
				}
			}
			if(returnValType != null && returnValType.equals("null")){
				hasRetVal = false;
			}
			if(isHasRetVal())
				retValNum++;
		}		
		if(isNeedExitFlags()){
			retValNum++;
		}		
		if(retValNum > 1){
			setNeedRetObj();
			if(isHasRetVal()){
				returnNames.add(Constants.RET_VAR);
				returnTypes.add(returnValType);
				needDeclForInterFlags.add(false);
			}
			for(VariableTypeBindingTerm term : outputTerms){
				returnNames.add(term.getName());
				returnTypes.add(term.getTypeNameTerm().getName());
				if(!inputTerms.contains(term))
					needDeclForInterFlags.add(true);
				else
					needDeclForInterFlags.add(false);
			}
			if(isNeedTemplateClass()){
				for(VariableTypeBindingTerm term : outputFieldTerms){
					returnNames.add(term.getName());
					returnTypes.add(term.getTypeNameTerm().getName());
					needDeclForInterFlags.add(false);
				}
			}
			returnType = Constants.RETOBJ;
			setNeedVariableDeclaration();		
		}else if(retValNum == 1){
			if(isHasRetVal()){
				returnType = returnValType;		
			}else{				
				if(isNeedExitFlags()){
					returnType = Constants.FLAG;
					setNeedVariableDeclaration();
				}else{
					if(!outputTerms.isEmpty()){
						termToReturn = outputTerms.iterator().next();
						returnType = termToReturn.getTypeNameTerm().getName();
						if(!inputTerms.contains(termToReturn)){
							setNeedVariableDeclaration();
						}
					}else{//!outputFieldTerms.isEmpty()
						termToReturn = outputFieldTerms.iterator().next();
						returnType = termToReturn.getTypeNameTerm().getName();
						if(!inputFieldTerms.contains(termToReturn) && !outputFieldTerms.contains(termToReturn)){
							setNeedVariableDeclaration();
						}
					}
				}					
			}
		}else{
			returnType = Constants.VOID;
		}
	}
	
	public static String getReturnType(){
		return returnType;
	}

	public static List<String> getBasicParamList() {
		return basicParamList;
	}

	public static void setBasicParamList(List<String> basicParamList) {
		RefactoringMetaData.basicParamList = basicParamList;
	}

	public static List<String> getBasicParamTypeList() {
		return basicParamTypeList;
	}

	public static void setBasicParamTypeList(List<String> basicTypeList) {
		basicParamTypeList = basicTypeList;
	}

	public static List<String> getExtendedParamList() {
		return extendedParamList;
	}

	public static void setExtendedParamList(List<String> extendedParamList) {
		RefactoringMetaData.extendedParamList = extendedParamList;
	}

	public static List<String> getExtendedParamTypeList() {
		return extendedParamTypeList;
	}

	public static void setExtendedParamTypeList(List<String> extendedParamTypeList) {
		RefactoringMetaData.extendedParamTypeList = extendedParamTypeList;
	}
	
	public static List<String> getExprParamList() {
		return exprParamList;
	}

	public static void setExprParamList(List<String> exprParamList) {
		RefactoringMetaData.exprParamList = exprParamList;
	}

	public static List<String> getExprParamTypeList() {
		return exprParamTypeList;
	}

	public static void setExprParamTypeList(List<String> exprParamTypeList) {
		RefactoringMetaData.exprParamTypeList = exprParamTypeList;
	}

	public static void addUnifiedToExpr(Map<String, ExpressionInfo> uToe){
		if(!uToe.isEmpty()){
			needExprParams = true;
		}
		unifiedToExprList.add(uToe);
	}
	
	public static void addUnifiedToVariable(Map<String, VariableInfo> uTov){
		if(!uTov.isEmpty()){
			needVariableParameters = true;
		}
		unifiedToVariableList.add(uTov);
	}
	
	public static void appendUnifiedToExpr(int index, Map<String, ExpressionInfo> uToe){
		Map<String, ExpressionInfo> unifiedToExpr = unifiedToExprList.get(index);
		String key = null;
		boolean appendFlag = false;
		for(Entry<String, ExpressionInfo> entry : uToe.entrySet()){
			key = entry.getKey();
			if(!unifiedToExpr.containsKey(key)){
				unifiedToExpr.put(key, uToe.get(key));
				appendFlag = true;
			}
		}
		if(appendFlag && !needExprParams)
			needExprParams = true;
	}
	
	public static void addUnifiedToMethod(Map<String, MethodInfo> uTom){
		if(!uTom.isEmpty()){
			setNeedTemplateHierarchy();
		}			
		unifiedToMethodList.add(uTom);
	}
	
	public static void addUnifiedToMethodRelations(){
		unifiedToMethodRelations = new HashMap<Integer, Set<Integer>>();
		Map<String, MethodInfo> uTom = null;
		Map<String, Integer> signatureToIndexes = new HashMap<String, Integer>();
		StringBuffer buffer = new StringBuffer();
		String tmpStr = null;
		for(int i = 0; i < unifiedToMethodList.size(); i++){
			uTom = unifiedToMethodList.get(i);
			buffer.setLength(0);
			for(Entry<String, MethodInfo> entry : uTom.entrySet()){
				buffer.append(entry.getValue().methodSignature).append(",");
			}
			tmpStr = buffer.toString();
			if(signatureToIndexes.containsKey(tmpStr)){
				unifiedToMethodRelations.get(signatureToIndexes.get(tmpStr)).add(i);
			}else{
				signatureToIndexes.put(tmpStr, i);
				unifiedToMethodRelations.put(i, new HashSet<Integer>());
			}
		}
	}
	
	public static void appendUnifiedToMethod(int index, Map<String, MethodInfo> uTom){
		Map<String, MethodInfo> unifiedToMethod = unifiedToMethodList.get(index);
		String key = null;
		boolean appendFlag = false;
		MethodInfo newMInfo = null, oldMInfo = null;
		for(Entry<String, MethodInfo> entry : uTom.entrySet()){
			key = entry.getKey();
			if(!unifiedToMethod.containsKey(key)){
				unifiedToMethod.put(key, uTom.get(key));
				appendFlag = true;
			}else{
				oldMInfo = unifiedToMethod.get(key);
				newMInfo = uTom.get(key);
				if(newMInfo.content.equals(oldMInfo.content)){
					if(newMInfo.otherKeys != null && !newMInfo.otherKeys.equals(oldMInfo.otherKeys)){
						oldMInfo.otherKeys = newMInfo.otherKeys;
					}
				}else{
					unifiedToMethod.put(key, newMInfo);
				}				
			}
		}
		if(appendFlag && !needTemplateHierarchy){
			setNeedTemplateHierarchy();
		}
	}
	
	public static Map<Integer, Set<Integer>> getUnifiedToMethodRelations(){
		return unifiedToMethodRelations;
	}	

	public static void addUnifiedToType(Map<String, TypeInfo> uTot){
		if(!uTot.isEmpty()){
			setNeedTemplateHierarchy();
			setNeedTypeParameters();
		}
		unifiedToTypeList.add(uTot);
	}
	
	public static void addUnifiedToTypeRelations(){
		unifiedToTypeRelations = new HashMap<Integer, Set<Integer>>();
		Map<String, TypeInfo> uTot = null;
		Map<String, Integer> namesToIndexes = new HashMap<String, Integer>();
		StringBuffer buffer = new StringBuffer();
		String tmpStr = null;
		for(int i = 0; i < unifiedToTypeList.size(); i++){
			uTot = unifiedToTypeList.get(i);
			buffer.setLength(0);
			for(Entry<String, TypeInfo> entry : uTot.entrySet()){
				buffer.append(entry.getValue().content).append(",");
			}
			tmpStr = buffer.toString();
			if(namesToIndexes.containsKey(tmpStr)){
				unifiedToTypeRelations.get(namesToIndexes.get(tmpStr)).add(i);
			}else{
				namesToIndexes.put(tmpStr, i);
				unifiedToTypeRelations.put(i, new HashSet<Integer>());
			}
		}
	}
	
	public static void appendUnifiedToType(int index, Map<String, TypeInfo> uTot){
		Map<String, TypeInfo> unifiedToType = unifiedToTypeList.get(index);
		String key = null;
		boolean appendFlag = false;
		for(Entry<String, TypeInfo> entry : uTot.entrySet()){
			key = entry.getKey();
			if(!unifiedToType.containsKey(key)){
				unifiedToType.put(key, uTot.get(key));
				appendFlag = true;
			}
		}
		if(appendFlag && !needTypeParameters){
			setNeedTemplateHierarchy();
			setNeedTypeParameters();		
		}
	}
	
	public static Map<Integer, Set<Integer>> getUnifiedToTypeRelations(){
		return unifiedToTypeRelations;
	}
	
	public static List<Map<String, ExpressionInfo>> getUnifiedToExprList(){
		return unifiedToExprList;
	}

	public static List<Map<String, MethodInfo>> getUnifiedToMethodList() {
		return unifiedToMethodList;
	}

	public static List<Map<String, TypeInfo>> getUnifiedToTypeList(){
		return unifiedToTypeList;
	}
	
	public static List<Map<String, VariableInfo>> getUnifiedToVariableList() {
		return unifiedToVariableList;
	}

	public static void appendUnifiedToVariable(int index, Map<String, VariableInfo> uTov) {
		Map<String, VariableInfo> unifiedToVariable = unifiedToVariableList.get(index);
		String key = null;
		boolean appendFlag = false;
		for(Entry<String, VariableInfo> entry : uTov.entrySet()){
			key = entry.getKey();
			if(!unifiedToVariable.containsKey(key)){
				unifiedToVariable.put(key, uTov.get(key));
				appendFlag = true;
			}
		}
		if(appendFlag && !needVariableParameters){
			needVariableParameters = true;
		}
	}

	public static boolean isNeedVariableDeclaration() {
		return needVariableDeclaration;
	}

	public static void setNeedVariableDeclaration() {
		needVariableDeclaration = true;
	}

	public static VariableTypeBindingTerm getTermToReturn() {
		return termToReturn;
	}

	public static void setTermToReturn(VariableTypeBindingTerm termToReturn) {
		RefactoringMetaData.termToReturn = termToReturn;
	}

	public static List<String> getReturnNames() {
		return returnNames;
	}

	public static List<String> getReturnTypes() {
		return returnTypes;
	}
	
	public static boolean isNeedTemplateHierarchy(){
		return needTemplateHierarchy;
	}
	
	public static boolean isNeedTypeParameters(){
		return needTypeParameters;
	}
	
	public static boolean isNeedExprParams(){
		return needExprParams;
	}
	
	public static List<Boolean> getNeedDeclForInterFlags(){
		return needDeclForInterFlags;
	}

	public static boolean isHasImplicitThis() {
		return hasImplicitThis;
	}

	public static void setHasImplicitThis() {
		hasImplicitThis = true;
	}

	public static Set<VariableTypeBindingTerm> getLocalTerms() {
		return localTerms;
	}

	public static void setLocalTerms(Set<VariableTypeBindingTerm> localTerms) {
		RefactoringMetaData.localTerms = localTerms;
	}
	
	public static void setNeedTemplateHierarchy(){
		needTemplateHierarchy = true;
	}
	
	public static void setNeedTypeParameters(){
		needTypeParameters = true;
	}
}
