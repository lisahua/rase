package changeassistant.clonereduction.manipulate.refactoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.BooleanLiteral;

import changeassistant.clonedetection.util.DeltaCodeSizeCounter;
import changeassistant.clonereduction.main.CloneReductionMain;
import changeassistant.clonereduction.manipulate.OperationCollection;
import changeassistant.clonereduction.manipulate.data.MethodInfo;
import changeassistant.clonereduction.manipulate.data.TypeInfo;
import changeassistant.multipleexample.apply.CodeGenerator;
import changeassistant.multipleexample.main.Constants;
import changeassistant.versions.treematching.edits.ITreeEditOperation.EDIT;

public class TemplateClassHierarchyCreator {
	
	Map<String, String> unifiedToDeclaredMethods = null;

	private String createConcreteTemplate(int index, Map<String, MethodInfo> uTom,  Map<String, TypeInfo> uTot){
		MethodInfo value = null;
		StringBuffer buffer = new StringBuffer();
		buffer.setLength(0);
		buffer.append("public class ").append(Constants.CONCRETE_TEMPLATE_CLASS)
			.append(Integer.toString(index)).append(" extends ").append(Constants.TEMPLATE_CLASS);
		if(RefactoringMetaData.isNeedTypeParameters()){
			buffer.append("<");
			for(Entry<String, TypeInfo> entry : uTot.entrySet()){
				buffer.append(entry.getValue().content).append(",");
			}
			buffer.setLength(buffer.length() - 1);
			buffer.append(">");
			}
		buffer.append("{\n");
		if(RefactoringMetaData.isNeedTypeParameters()){
			for(Entry<String, MethodInfo> entry : uTom.entrySet()){
				value = entry.getValue();
				buffer.append(" public ").append(createConcreteDeclaredMethod(uTot, value)).append("{\n");
				CloneReductionMain.refEdits.add(2, EDIT.INSERT, "concrete class template's concrete method definition");
				CloneReductionMain.deltaCounter.increment(2, "concrete template class' method definition");
				buffer.append(value.content).append(";\n");
				buffer.append("}\n");
			}
		}else{
			for(Entry<String, MethodInfo> entry : uTom.entrySet()){
				value = entry.getValue();
				buffer.append("  public ").append(unifiedToDeclaredMethods.get(value.name) + "{\n");				
				buffer.append(value.content + ";\n");
				CloneReductionMain.refEdits.add(2, EDIT.INSERT, "concrete class template's concrete method definition");
				CloneReductionMain.deltaCounter.increment(2, "concrete template class' method definition");
				buffer.append("  }\n");
			}
		}				
		buffer.append("}\n");
		String tmpClassStr = CodeGenerator.create(buffer.toString(), ASTParser.K_COMPILATION_UNIT).toString();
		CloneReductionMain.refEdits.add(1, EDIT.INSERT, "template concrete class declaration");
		CloneReductionMain.deltaCounter.increment("template concrete class declaration");
		return tmpClassStr;
	}
	
	public String create(String methodString){
		System.out.print("");
		List<Map<String, MethodInfo>> unifiedToMethodList = RefactoringMetaData.getUnifiedToMethodList();
		List<Map<String, TypeInfo>> unifiedToTypeList = RefactoringMetaData.getUnifiedToTypeList();
		boolean needHierarchy = RefactoringMetaData.isNeedTemplateHierarchy();
		String tmpStr = null;
		StringBuffer resultBuffer = new StringBuffer();
		StringBuffer buffer = new StringBuffer("public ");
		if(needHierarchy){
			buffer.append("abstract ");
		}
		buffer.append("class ").append(Constants.TEMPLATE_CLASS);
		CloneReductionMain.refEdits.add(1, EDIT.INSERT, "template class declaration");
		CloneReductionMain.deltaCounter.increment("template class declaration");
		Map<String, TypeInfo> uTot = null;
		if(RefactoringMetaData.isNeedTypeParameters()){
			buffer.append("<");
			uTot = unifiedToTypeList.get(0);
			for(Entry<String, TypeInfo> entry : uTot.entrySet()){
				buffer.append(entry.getValue().name).append(",");
			}
			if(!uTot.isEmpty()){
				buffer.setLength(buffer.length() - 1);
			}
			buffer.append(">");
		}
		buffer.append("{\n");
		buffer.append(methodString);
		String declared = null;
		MethodInfo value = null;
		Map<String, MethodInfo> uTom = null;
		if(needHierarchy){
			unifiedToDeclaredMethods = new HashMap<String, String>();
			uTom = unifiedToMethodList.get(0);
			uTot = unifiedToTypeList.get(0);
			for(Entry<String, MethodInfo> entry : uTom.entrySet()){
				value = entry.getValue();
				declared = value.createDeclaredMethodSignature(uTot);
				buffer.append("  public abstract ").append(declared).append(";\n");
				unifiedToDeclaredMethods.put(value.name, declared);
				CloneReductionMain.refEdits.add(1, EDIT.INSERT, "template abstract method declaration");
				CloneReductionMain.deltaCounter.increment("template abstract method declaration");
			}
		}
		buffer.append("}\n");
		resultBuffer.append(CodeGenerator.create(buffer.toString(), ASTParser.K_COMPILATION_UNIT).toString());		
		if(needHierarchy){
			Map<Integer, Set<Integer>> uTomRelations = RefactoringMetaData.getUnifiedToMethodRelations();
			Map<Integer, Set<Integer>> uTotRelations = RefactoringMetaData.getUnifiedToTypeRelations();
			int sizeForuTomRelations = uTomRelations.size(); 
			int sizeForuTotRelations = uTotRelations.size(); 
			if(sizeForuTomRelations == 1 && sizeForuTotRelations != 1){
				for(Integer index : uTotRelations.keySet()){
					tmpStr = createConcreteTemplate(index, unifiedToMethodList.get(index), unifiedToTypeList.get(index));
					resultBuffer.append(tmpStr);
				}				
			}else if(sizeForuTotRelations == 1 && sizeForuTomRelations != 1){
				for(Integer index : uTomRelations.keySet()){
					tmpStr = createConcreteTemplate(index, unifiedToMethodList.get(index), unifiedToTypeList.get(index));
					resultBuffer.append(tmpStr);
				}
			}else{
				for(int i = 0; i < unifiedToMethodList.size(); i++){
					tmpStr = createConcreteTemplate(i, unifiedToMethodList.get(i), unifiedToTypeList.get(i));
					resultBuffer.append(tmpStr);
				}
			}
		}
		return resultBuffer.toString();
	}
	
	private Map<String, String> prepareTypeMap(Map<String, TypeInfo> uTot){
		Map<String, String> map = new HashMap<String, String>();
		TypeInfo value = null;
		for(Entry<String, TypeInfo> entry : uTot.entrySet()){
			value = entry.getValue();
			map.put(value.name, value.content);
		}
		return map;
	}
	
	private String lookforConcreteType(String key, Map<String, String> map){
		if(map.containsKey(key))
			return map.get(key);
		else
			return key;
	}
	
	public String createConcreteDeclaredMethod(Map<String, TypeInfo> uTot, MethodInfo mInfo){
		StringBuffer buffer = new StringBuffer();
		Map<String, String> map = prepareTypeMap(uTot);	
		buffer.append(lookforConcreteType(mInfo.returnType, map)).append(" ").append(mInfo.name).append("(");
		List<String> names = mInfo.names;
		List<String> types = mInfo.types;
		for(int i = 0; i < mInfo.indexes.size(); i++){
			int index = mInfo.indexes.get(i);
			ASTNode paramAST = mInfo.paramASTs.get(i);
			String name = names.get(index);
			if((paramAST instanceof BooleanLiteral) && (name.equals("true") || name.equals("false"))){
				
			}else{
				buffer.append(lookforConcreteType(types.get(index), map)).append(" ").append(names.get(index)).append(",");
			}
		}
		if(mInfo.needInst){
			buffer.append(mInfo.instTypeName).append(" ").append("inst").append(",");
		}
		if(buffer.charAt(buffer.length() - 1) == ','){
			buffer.setLength(buffer.length() - 1);
		}		
		buffer.append(")");
		return buffer.toString();
	}
}
