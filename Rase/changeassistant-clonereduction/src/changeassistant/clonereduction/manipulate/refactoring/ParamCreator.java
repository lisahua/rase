package changeassistant.clonereduction.manipulate.refactoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.SimpleName;

import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;
import changeassistant.clonereduction.manipulate.CloneReductionFilter;
import changeassistant.clonereduction.manipulate.data.ExpressionInfo;
import changeassistant.clonereduction.manipulate.data.MethodInfo;
import changeassistant.clonereduction.manipulate.data.VariableInfo;
import changeassistant.clonereduction.search.CloneReductionMatchResult;
import changeassistant.multipleexample.datastructure.MapList;
import changeassistant.multipleexample.main.Constants;
import changeassistant.multipleexample.util.IdMapper;
import changeassistant.versions.comparison.ChangedMethodADT;

public class ParamCreator {

	private ChangedMethodADT knownADT;
	private List<CloneReductionMatchResult> results;
	private List<String> paramStrings;
	private List<String> outputReceivers;
	private List<List<String>> outputTermReceivers;
	private Map<String, String> specificToUnified = null;
	private int size = 0;
	private MapList mapList = null;
	private String termName = null;
	private List<String> paramList = null;
	
	private void createParams(){
		List<Map<String, ExpressionInfo>> unifiedToExprList = RefactoringMetaData.getUnifiedToExprList();
		List<Map<String, VariableInfo>> unifiedToVariableList = RefactoringMetaData.getUnifiedToVariableList();
		StringBuffer buffer = new StringBuffer();
		Map<String, ExpressionInfo> uToe = unifiedToExprList.get(0);
		Map<String, VariableInfo> uTov = unifiedToVariableList.get(0);
		VariableInfo vInfo = null;
		ExpressionInfo eInfo = null;
		Map<String, String> paramToAbstract = new HashMap<String, String>();
		String tmpKey = null;
		for(String param : paramList){
			if(Term.V_Pattern2.matcher(param).matches()){
				tmpKey = "v$_" + param.substring(1) + "_";
				paramToAbstract.put(param, tmpKey);
			}else if(Term.U_Pattern2.matcher(param).matches()){
				for(Entry<String, ExpressionInfo> entry : uToe.entrySet()){
					if(entry.getValue().name.equals(param)){
						paramToAbstract.put(param, entry.getKey());
						break;
					}
				}
			}
		}
		for(int i = 0; i < size; i++){
			uToe = unifiedToExprList.get(i);
			uTov = unifiedToVariableList.get(i);
			buffer.setLength(0);
			for(int j = 0; j < paramList.size(); j++){
				tmpKey = paramList.get(j);
				if(paramToAbstract.containsKey(tmpKey)){
					if(tmpKey.startsWith("v")){
						vInfo = uTov.get(paramToAbstract.get(tmpKey));
						buffer.append(vInfo.content).append(",");
					}else{
						eInfo = uToe.get(paramToAbstract.get(tmpKey));
						buffer.append(eInfo.content).append(",");
					}
				}else if(tmpKey.equals(Constants.INSTANCE)){
					buffer.append("this").append(",");
				}else{
					buffer.append(tmpKey).append(",");
				}
			}
			if(buffer.length() > 0){
				buffer.setLength(buffer.length() - 1);
			}
			paramStrings.add(buffer.toString());
		}		
	}
	
	private boolean checkHasSameReceiver(StringBuffer buffer){
		outputReceivers = new ArrayList<String>();
		boolean hasSameReceiver = true;
		String returnType = RefactoringMetaData.getReturnType();		
		if(returnType.equals(Constants.VOID)){
			//do nothing
		}else if(returnType.equals(Constants.FLAG)){
			buffer.append(Constants.FLAG).append(" flag = ");
		}else if(returnType.equals(Constants.RETOBJ)){
			buffer.append(Constants.RETOBJ).append(" retObj = ");
		}else{
			if(RefactoringMetaData.isHasRetVal()){
				buffer.append("return ");
			}else if(RefactoringMetaData.isNeedVariableDeclaration()){
				VariableTypeBindingTerm termToReturn = RefactoringMetaData.getTermToReturn();
				buffer.append(returnType).append(" ").append(termToReturn.getName()).append(" = ");
			}else{//don't need variable declaration
				termName = RefactoringMetaData.getTermToReturn().getName();
				if(specificToUnified.containsKey(termName))
					hasSameReceiver = false;
			}
		}
		return hasSameReceiver;
	}
	
	private void createOutputReceivers2(){
//		System.out.print("");
		StringBuffer buffer = new StringBuffer();
		boolean hasSameReceiver = checkHasSameReceiver(buffer);
		List<Map<String, String>> restUtoCMaps = new ArrayList<Map<String, String>>();
		Map<String, String> uToc = null, cTou = null;
		List<String> outputTermString = null;
		if(hasSameReceiver){
			String str = buffer.toString();
			for(int i = 0; i < size; i++){
				outputReceivers.add(str);
			}
		}else{
			outputReceivers.add(termName + " = ");
			for(int i = 1; i < size; i++){
				cTou = mapList.get(i);
				uToc = IdMapper.createReverseMap(cTou);
				restUtoCMaps.add(uToc);
				outputReceivers.add(uToc.get(specificToUnified.get(termName)) + " = ");
			}
		}
		List<String> returnNames = RefactoringMetaData.getReturnNames();
		String name = null;
		int outputIndex = -1;
		for(int i = 0; i < returnNames.size(); i++){
			name = returnNames.get(i);
			if(name.equals(Constants.FLAG_VAR) || name.equals(Constants.RETOBJ_VAR)){
				continue;
			}
			outputIndex = i;
			break;
		}
		if(outputIndex == -1){
			outputTermReceivers = new ArrayList<List<String>>();
			for(int i = 0; i < size; i++){
				outputTermReceivers.add(Collections.EMPTY_LIST);
			}
		}else{
			outputTermReceivers = new ArrayList<List<String>>();
			outputTermString = new ArrayList<String>();				
			for(int j = outputIndex; j < returnNames.size(); j++){
				outputTermString.add(returnNames.get(j));
			}
			outputTermReceivers.add(outputTermString);
			if(hasSameReceiver){
				for(int i = 1; i < size; i++)
					outputTermReceivers.add(outputTermString);
			}else{
				for(int i = 1; i < size; i++){
					outputTermString = new ArrayList<String>();
					uToc = restUtoCMaps.get(i);
					for(int j = outputIndex; j < returnNames.size(); j++){
						outputTermString.add(uToc.get(specificToUnified.get(returnNames.get(j))));
					}
					outputTermReceivers.add(outputTermString);
				}
			}			
		}
	}
	
	private void createOutputReceivers(){
		StringBuffer buffer = new StringBuffer();
		boolean hasSameReceiver = checkHasSameReceiver(buffer);
		CloneReductionMatchResult r = null;
		Map<String, String> unifiedToSpecific = null;
		if(hasSameReceiver){
			String str = buffer.toString();
			for(int i = 0; i < size; i++){
				outputReceivers.add(str);
			}
		}else{						
			for(int i = 0; i < size; i++){
				r = results.get(i);	
				if (r.getAdt().equals(knownADT)) {
					outputReceivers.add(termName + " = ");
				}else{
					unifiedToSpecific = r.getuToc();
					outputReceivers.add(unifiedToSpecific.get(specificToUnified.get(termName)) + " = ");
				}
			}
		}
		List<String> outputTermString = null;
		List<String> returnNames = RefactoringMetaData.getReturnNames();
		String name = null;
		int outputIndex = -1;
		for(int i = 0; i < returnNames.size(); i++){
			name = returnNames.get(i);
			if(name.equals(Constants.FLAG_VAR) || name.equals(Constants.RETOBJ_VAR)){
				continue;
			}
			outputIndex = i;
			break;
		}
		if(outputIndex == -1){
			outputTermReceivers = new ArrayList<List<String>>();
			for(int i = 0; i < results.size(); i++){
				outputTermReceivers.add(Collections.EMPTY_LIST);
			}
		}else{
			outputTermReceivers = new ArrayList<List<String>>();
			for(int i = 0; i < results.size(); i++){
				r = results.get(i);
				outputTermString = new ArrayList<String>();				
				if(r.getAdt().equals(knownADT)){							
					for(int j = outputIndex; j < returnNames.size(); j++){
						outputTermString.add(returnNames.get(j));
					}					
				}else{
					unifiedToSpecific = r.getuToc();
					for(int j = outputIndex; j < returnNames.size(); j++){
						outputTermString.add(unifiedToSpecific.get(specificToUnified.get(returnNames.get(j))));
					}
				}
				outputTermReceivers.add(outputTermString);
			}
		}		
	}

	public void createInterfaceParams(ChangedMethodADT knownADT,
			List<CloneReductionMatchResult> results) {
		this.knownADT = knownADT;
		this.results = results;
		this.size = results.size();
		paramStrings = new ArrayList<String>();	
		for (CloneReductionMatchResult r : results) {
			if (r.getAdt().equals(knownADT)) {
				specificToUnified = r.getcTou();
				break;
			}
		}
		createParams();
		createOutputReceivers();
	}

	public void createInterfaceParams(CloneReductionFilter crFilter, MethodHeaderCreator mhCreator){
		this.mapList = crFilter.getNeChecker().getMapList();
		this.size = mapList.size(); 
		paramStrings = new ArrayList<String>();	
		this.specificToUnified = mapList.get(0);		
		this.paramList = mhCreator.getParamList();
		createParams();
		createOutputReceivers2();
	}
	
	public List<String> getParameterStrings() {
		return paramStrings;
	}

	public List<String> getOutputReceivers() {
		return outputReceivers;
	}
	
	public List<List<String>> getOutputTermReceivers(){
		return outputTermReceivers;
	}
}
