package changeassistant.clonereduction.manipulate.refactoring;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.internal.core.util.MethodInfo;

import changeassistant.changesuggestion.expression.representation.Term.TermType;
import changeassistant.clonereduction.manipulate.data.ExpressionInfo;
import changeassistant.clonereduction.manipulate.data.TypeInfo;
import changeassistant.clonereduction.manipulate.data.VariableInfo;

public class IdGeneralizer {
	
	public static String generalizeType(String concreteName, Map<String, TypeInfo> uTot){
		String typeName = concreteName;
		TypeInfo value = null;
		for(Entry<String, TypeInfo> entry : uTot.entrySet()){
			value = entry.getValue();					
			if(value.content.equals(typeName)){
				typeName = value.name;
				break;
			}
		}
		return typeName;
	}

	public static String generalize(String concreteName, Map<String, String> sTou, TermType termType){
		String result = concreteName;
		if(sTou.containsKey(concreteName)){
			result = sTou.get(concreteName);
		}
		switch(termType){
		case VariableTypeBindingTerm:
			Map<String, VariableInfo> uTov = RefactoringMetaData.getUnifiedToVariableList().get(0);
			if(uTov.containsKey(result)){
				result = uTov.get(result).name;
			}else{
				Map<String, ExpressionInfo> uToe = RefactoringMetaData.getUnifiedToExprList().get(0);
				if(uToe.containsKey(result)){
					result = uToe.get(result).name;
				}
			}
			break;
		case MethodNameTerm:
			break;
		case TypeNameTerm:
			Map<String, TypeInfo> uTot = RefactoringMetaData.getUnifiedToTypeList().get(0);
			if(uTot.containsKey(result)){
				result = uTot.get(result).name;
			}
			break;
		case Term:
			Map<String, ExpressionInfo> uToe = RefactoringMetaData.getUnifiedToExprList().get(0);
			if(uToe.containsKey(result)){
				result = uToe.get(result).name;
			}
			break;
		}
		return result;
	}
}
