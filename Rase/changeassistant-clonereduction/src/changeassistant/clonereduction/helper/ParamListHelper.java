package changeassistant.clonereduction.helper;

import java.util.List;
import java.util.Map;
import java.util.Set;

import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;
import changeassistant.clonereduction.manipulate.refactoring.IdGeneralizer;

public class ParamListHelper {

	public static void modifyParam(int index, String name, String type, List<String> typeList, List<String> paramList){
		typeList.set(index, type);
		paramList.set(index, name);
	}
	
	public static void addParam(String name, String type, List<String> typeList, List<String> paramList){
		typeList.add(type);
		paramList.add(name);
	}
	
	public static void addGeneralizedParams(Set<VariableTypeBindingTerm> terms, List<String> typeList, List<String> paramList, Map<String, String> sTou){
		String originalName = null;
		String name = null;
		String type = null;
		for(VariableTypeBindingTerm term : terms){
			originalName = term.getName();
			name = IdGeneralizer.generalize(originalName, sTou, Term.TermType.VariableTypeBindingTerm);
			if(!name.equals(originalName)){
				type = IdGeneralizer.generalize(term.getTypeNameTerm().getName(), sTou, Term.TermType.TypeNameTerm);
				typeList.add(type);
				paramList.add(name);
			}
		}
	}
	
	public static void addParams(Set<VariableTypeBindingTerm> terms, List<String> typeList, List<String> paramList, 
			Map<String, String> sTou){
		String type = null, name = null;
		for (VariableTypeBindingTerm term : terms) {
			type = term.getTypeNameTerm().getName();
			name = term.getName();
			name = IdGeneralizer.generalize(name, sTou, Term.TermType.VariableTypeBindingTerm);
			type = IdGeneralizer.generalize(type, sTou, Term.TermType.TypeNameTerm);
			typeList.add(type);
			paramList.add(name);			
		}
	}
	
	public static void addVars(Set<VariableTypeBindingTerm> terms, List<String> typeList, List<String> paramList){
		String type = null, name = null;
		for (VariableTypeBindingTerm term : terms) {
			type = term.getTypeNameTerm().getName();
			name = term.getName();
			typeList.add(type);
			paramList.add(name);			
		}
	}

}
