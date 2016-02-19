package changeassistant.clonereduction.manipulate.refactoring;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
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
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.Term.TermType;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;
import changeassistant.clonereduction.helper.ParamListHelper;
import changeassistant.clonereduction.main.CloneReductionException;
import changeassistant.clonereduction.manipulate.data.ExpressionInfo;
import changeassistant.clonereduction.manipulate.data.MethodInfo;
import changeassistant.clonereduction.manipulate.data.TypeInfo;
import changeassistant.clonereduction.manipulate.data.TypeInfoCreator;
import changeassistant.clonereduction.manipulate.data.VariableInfo;
import changeassistant.clonereduction.search.CloneReductionMatchResult;
import changeassistant.internal.ASTNodeFinder;
import changeassistant.multipleexample.datastructure.GeneralizedStmtIndexMap;
import changeassistant.multipleexample.datastructure.MapList;
import changeassistant.multipleexample.main.Constants;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.multipleexample.util.IdMapper;
import changeassistant.multipleexample.util.SameChecker;
import changeassistant.peers.SourceCodeRange;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.comparison.ChangedMethodADT;

public class MethodHeaderCreator {
	
	private List<String> paramList;
	private List<String> typeList;
	private Map<String, String> cTou;
	private List<SimpleTreeNode> pSNodes;
	private MethodDeclaration md;
	private List<List<SimpleASTNode>> customizedNodesList;
	private List<CloneReductionMatchResult> matchResults;
	
	public List<String> getParamList(){
		return paramList;
	}
	
	private StringBuffer initialize(String methodName, Map<String, String> sTou){
		//the string starts from the return type
		StringBuffer buffer = new StringBuffer(IdGeneralizer.generalize(RefactoringMetaData.getReturnType(), sTou, TermType.TypeNameTerm));
		buffer.append(" ").append(methodName).append("(");
		Set<VariableTypeBindingTerm> inputTerms = RefactoringMetaData.getInputTerms();
		Set<VariableTypeBindingTerm> inputFieldTerms = RefactoringMetaData.getInputFieldTerms();
		paramList = new ArrayList<String>();
		typeList = new ArrayList<String>();
		
		ParamListHelper.addParams(inputTerms, typeList, paramList, sTou);
		if (RefactoringMetaData.isNeedTemplateClass()) {
			ParamListHelper.addParams(inputFieldTerms, typeList, paramList, sTou);
		}else{
			ParamListHelper.addGeneralizedParams(inputFieldTerms, typeList, paramList, sTou);
		}
		return buffer;
	}
	
	
	public String createHeader(String methodName, MapList mapList, GeneralizedStmtIndexMap generalizedStmtIndexMap,
			List<MethodDeclaration> mdList) throws CloneReductionException{
		this.cTou = mapList.get(0);
		StringBuffer buffer = initialize(methodName, cTou);
		if(RefactoringMetaData.isNeedTemplateClass())
			processMI2(mdList, generalizedStmtIndexMap, mapList.getMethodInvocationMap());
		processOther(buffer);
		return buffer.toString();
	}
	
	private void processOther(StringBuffer buffer){
		processV();				
		RefactoringMetaData.setBasicParamList(new ArrayList<String>(paramList));
		RefactoringMetaData.setBasicParamTypeList(new ArrayList<String>(typeList));
		int tmpSize = paramList.size(); 
		processM();		
		RefactoringMetaData.setExtendedParamList(new ArrayList<String>(paramList.subList(tmpSize, paramList.size())));
		RefactoringMetaData.setExtendedParamTypeList(new ArrayList<String>(typeList.subList(tmpSize, typeList.size())));
		
		tmpSize = paramList.size(); 		
		processU();
		RefactoringMetaData.setExprParamList(new ArrayList<String>(paramList.subList(tmpSize, paramList.size())));
		RefactoringMetaData.setExprParamTypeList(new ArrayList<String>(typeList.subList(tmpSize, typeList.size())));
		for(int i = 0; i < paramList.size(); i++){
			buffer.append(typeList.get(i)).append(" ").append(paramList.get(i)).append(",");
		}
		if(buffer.charAt(buffer.length() - 1) == ','){
			buffer.setLength(buffer.length() - 1);
		}
		buffer.append(")");
		List<String> exceptionTypes = RefactoringMetaData.getExceptionTypes();
		if (!exceptionTypes.isEmpty()) {
			buffer.append(" throws ");
			for (String e : exceptionTypes) {
				buffer.append(e).append(", ");
			}
			buffer.setLength(buffer.length() - 2);
		}
		if(paramList.contains(Constants.INSTANCE)){
			List<Map<String, VariableInfo>> uTovList = RefactoringMetaData.getUnifiedToVariableList();
			for(Map<String, VariableInfo> uTov : uTovList){
				uTov.put("inst", new VariableInfo("this", "this", "this", "null"));
			}
		}
	}
	
	public String createHeader(String methodName, Map<String, String> cTou,
			List<SimpleTreeNode> pSNodes,
			List<List<SimpleASTNode>> customizedNodesList,
			MethodDeclaration md, List<CloneReductionMatchResult> matchResults) throws CloneReductionException {
		this.cTou = cTou;
		this.pSNodes = pSNodes;
		this.md = md;
		this.customizedNodesList = customizedNodesList;
		this.matchResults = matchResults;
		StringBuffer buffer = initialize(methodName, cTou);
		// for method invocations on this pointer which are invoked by some extracted templates
		processMI();
		processOther(buffer);
		return buffer.toString();
	}
	
	private void processMI() throws CloneReductionException{
		Enumeration<SimpleTreeNode> sEnum = null;
		Enumeration<SimpleASTNode> sAEnum = null;
		SimpleTreeNode sTmp = null;
		SimpleASTNode sATmp = null;
		List<SimpleASTNode> nodes = null;
		SourceCodeRange scr = null;
		MethodInvocation mi = null;
		Expression invoker = null;
		boolean isNonStatic = false;
		boolean isPublic = false;
		int modifiers = -1;
		String invokerTypeName = null;
		ASTNodeFinder finder = new ASTNodeFinder();
		for(SimpleTreeNode s : pSNodes){
			sEnum = s.breadthFirstEnumeration();
			while(sEnum.hasMoreElements()){
				sTmp = sEnum.nextElement();
				nodes = customizedNodesList.get(sTmp.getNodeIndex() - 1);
				for(SimpleASTNode sNode : nodes){
					sAEnum = sNode.breadthFirstEnumeration();
					while(sAEnum.hasMoreElements()){
						sATmp = sAEnum.nextElement();
						if(sATmp.getNodeType() == ASTNode.METHOD_INVOCATION){
							scr = sATmp.getScr();
							mi = (MethodInvocation)finder.lookforASTNode(md, scr);
							IMethodBinding binding = mi.resolveMethodBinding();
							invoker = (Expression)mi.getExpression();		
							modifiers = binding.getModifiers();
							isNonStatic = ((modifiers & Modifier.STATIC) == 0);
							isPublic = ((modifiers & Modifier.PUBLIC) != 0);
							if(isNonStatic && invoker == null && RefactoringMetaData.isNeedTemplateClass()){
								if(isPublic){
									invokerTypeName = binding.getDeclaringClass().getName();
									break;
								}else{
									throw new CloneReductionException("Certain invoked method " +
											"is not callable by an extracted method in a template " + mi.toString());
								}								
							}
						}
					}
				}
				if(invokerTypeName != null){
					break;
				}
			}
		}
		if(invokerTypeName != null){
			ChangedMethodADT adt2 = matchResults.get(1).getAdt();
			String other = adt2.classname;
 			if(other.equals(invokerTypeName)){
 				paramList.add(Constants.INSTANCE);
 				typeList.add(invokerTypeName);
 			} else {
 				throw new CloneReductionException("The extracted method contains certain $m call on $t");
 			}
			RefactoringMetaData.setHasImplicitThis();
		}
	}
	
	/**
	 * check method invocations made by the extracted method
	 * @param mdList
	 * @param siMap
	 * @param methodInvocationMap
	 * @throws CloneReductionException
	 */
	private void processMI2(List<MethodDeclaration> mdList, GeneralizedStmtIndexMap siMap,
			Map<String, List<SourceCodeRange>> methodInvocationMap) throws CloneReductionException{
		MethodDeclaration md0 = mdList.get(0);
		ASTNodeFinder finder = new ASTNodeFinder();
		String invokerTypeName = null;
		List<String> invokerTypeNames = new ArrayList<String>();
		List<SourceCodeRange> list = null;
		MethodInvocation mi = null;
		IMethodBinding binding = null;
		Expression invoker = null;
		int modifiers = 0;
		boolean isNonStatic = false;
		boolean isPublic = false;
		for(Entry<String, List<SourceCodeRange>> entry : methodInvocationMap.entrySet()){
			list = entry.getValue();
			mi = (MethodInvocation)finder.lookforASTNode(md0, list.get(0));
			invoker = (Expression)mi.getExpression();
			binding = mi.resolveMethodBinding();
			invokerTypeName = binding.getDeclaringClass().getName();
			if(binding != null){
				modifiers = binding.getModifiers();				
				isNonStatic = ((modifiers & Modifier.STATIC) == 0);
				isPublic = ((modifiers & Modifier.PUBLIC) != 0);	
			}else{
				throw new CloneReductionException("The binding of the invoked method " + mi.toString() + " is not resolved");
			}								
			if(!isNonStatic){
				continue;				
			}				
			if(isNonStatic && !isPublic && RefactoringMetaData.isNeedTemplateClass()){
				throw new CloneReductionException("Certain invoked method " + 
						"is not callable by an extracted method in a template " + mi.toString());
			}		
			invokerTypeNames.clear();			
			for(int i = 0; i < list.size(); i++){
				mi = (MethodInvocation)finder.lookforASTNode(mdList.get(i), list.get(i));
				binding = mi.resolveMethodBinding();
				modifiers = binding.getModifiers();
				invokerTypeNames.add(binding.getDeclaringClass().getName());
				isNonStatic = ((modifiers & Modifier.STATIC) == 0);
				isPublic = ((modifiers & Modifier.PUBLIC) != 0);
			}
			if(!new SameChecker<String>().areSame(invokerTypeNames)){
				if(invoker == null || invoker != null && invoker.toString().equals("this")){
					if(!invokerTypeName.isEmpty() && !paramList.contains(Constants.INSTANCE)){
						paramList.add(Constants.INSTANCE);
						if(cTou.containsKey(invokerTypeName)){
							String typeName = cTou.get(invokerTypeName);
							if(Term.T_Pattern.matcher(typeName).matches()){
								typeList.add(RefactoringMetaData.getUnifiedToTypeList().get(0).get(typeName).name);								
							}else{
								typeList.add(typeName);
							}
						}else{
							throw new CloneReductionException("The information contained in the systematic edit is not enough to deal with $m on $t");
						}						
					}
					if(invoker == null && !invokerTypeName.isEmpty()){
						RefactoringMetaData.setHasImplicitThis();
					}
				}
//				throw new CloneReductionException("The extracted method contains certain $m call on $t");
			}else if(isNonStatic){
				if(invoker == null || invoker!= null && invoker.toString().equals("this")){
					if(!invokerTypeName.isEmpty() && !paramList.contains(Constants.INSTANCE)){
						paramList.add(Constants.INSTANCE);
						typeList.add(invokerTypeName);
					}
					if(invoker == null && !invokerTypeName.isEmpty()){
						RefactoringMetaData.setHasImplicitThis();
					}
				}
			}
		}	
	}
	
	private void processV(){
		String type = null, name = null, unified = null;
		VariableInfo vInfo = null;
		//for $v
		Map<String, VariableInfo> uTov = RefactoringMetaData.getUnifiedToVariableList().get(0);
		if(uTov.isEmpty())
			return;
		Map<String, TypeInfo> uTot = RefactoringMetaData.getUnifiedToTypeList().get(0);
		Map<String, Integer> alreadyParams = new HashMap<String, Integer>();
		int index = -1;
		for(int i = 0; i < paramList.size(); i++){
			name = paramList.get(i);
			if(cTou.containsKey(name)){
				unified = cTou.get(name);
				if(Term.V_Pattern.matcher(unified).matches() && uTov.containsKey(unified)){
					alreadyParams.put(unified, i);
				}
			}
		}
		/*
		boolean isLocal = false;
		boolean isOutput = false;
		Set<VariableTypeBindingTerm> localTerms = RefactoringMetaData.getLocalTerms();
		Set<VariableTypeBindingTerm> outputFieldTerms = RefactoringMetaData.getOutputFieldTerms();
		Set<VariableTypeBindingTerm> outputTerms = RefactoringMetaData.getOutputTerms();
		String vContent = null;
		
		for(Entry<String, VariableInfo> entry : uTov.entrySet()){
			name = entry.getKey();
			vInfo = entry.getValue();
			vContent = vInfo.content;
			if(!alreadyParams.containsKey(name)){
				isLocal = false;
				isOutput = false;
				for(VariableTypeBindingTerm lTerm : localTerms){
					if(lTerm.getName().equals(vContent)){
						isLocal = true;
						break;
					}
				}
				if(!isLocal){
					for(VariableTypeBindingTerm oTerm : outputTerms){
						if(oTerm.getName().equals(vContent)){
							isOutput = true;
							break;
						}
					}
					if(!isOutput){
						for(VariableTypeBindingTerm oTerm : outputFieldTerms){
							if(oTerm.getName().equals(vContent)){
								isOutput = true;
								break;
							}
						}
					}
				}				
				if(!isLocal && !isOutput)
					ParamListHelper.addParam(vInfo.name, vInfo.type, typeList, paramList);
			}else{//alreadyParams.containsKey(name)
				index = alreadyParams.get(name);
				type = typeList.get(index);
				if(uTot.containsKey(type)){
					ParamListHelper.modifyParam(index, vInfo.name, uTot.get(type).name, typeList, paramList);
				}else{
					ParamListHelper.modifyParam(alreadyParams.get(name), vInfo.name, vInfo.type, typeList, paramList);
				}				
			}
		}
		*/
	}
	
	private void processM(){
		String type = null, name = null;
		//for $m		
		Map<String, MethodInfo> unifiedToMethod = RefactoringMetaData.getUnifiedToMethodList().get(0);
		for(MethodInfo m : unifiedToMethod.values()){
			List<String> names = m.names;
			List<String> types = m.types;
			List<ASTNode> paramASTs = m.paramASTs;
			ASTNode paramAST = null;
			for(int i = 0; i < names.size(); i++){
				name = names.get(i);
				type = types.get(i);
				paramAST = paramASTs.get(i);
				if(paramList.contains(name) && typeList.get(paramList.indexOf(name)).equals(type) ||
						paramAST instanceof SimpleName || paramAST instanceof BooleanLiteral || 
						paramAST instanceof FieldAccess){
					//do nothing
				}else{
					paramList.add(name);
					typeList.add(type);
				}
			}
		}
	}
	
	private void processU(){
		String type = null, name = null;
		//for $u
		Map<String, ExpressionInfo> unifiedToExpr = RefactoringMetaData.getUnifiedToExprList().get(0);
		for(ExpressionInfo e : unifiedToExpr.values()){
			name = e.name;
			type = e.type;
			if(!paramList.contains(name)){
				paramList.add(name);
				typeList.add(type);
			}			
		}		
	}
}
