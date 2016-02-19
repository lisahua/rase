package changeassistant.clonereduction.manipulate.data;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.ASTNode;

import changeassistant.changesuggestion.astrewrite.ASTNodeGenerator2;
import changeassistant.changesuggestion.expression.representation.Term;

public class TypeInfoCreator {
	
	List<String> pStrings = null;
	
	public TypeInfoCreator(){
		pStrings = ASTNodeGenerator2.primitiveTypeStrings;		
	}
	
	public TypeInfo createForT(String abstractName, Map<String, String> sTou){
		String concrete = null;
		for(Entry<String, String> entry : sTou.entrySet()){
			if(entry.getValue().equals(abstractName)){
				concrete = entry.getKey();
				break;
			}				
		}
		return createForT(concrete, abstractName);
	}

	public TypeInfo createForT(ASTNode astNode, String abstractName){				
		int index = Term.parseInt(abstractName);
		return new TypeInfo("T"+index, astNode, abstractName);
	}
	
	public TypeInfo createForT(String type, String abstractName){
		int index = Term.parseInt(abstractName);
		return new TypeInfo("T"+index, type, abstractName);
	}
}
