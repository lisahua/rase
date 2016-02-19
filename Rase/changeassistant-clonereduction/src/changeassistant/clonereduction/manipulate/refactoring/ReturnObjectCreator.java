package changeassistant.clonereduction.manipulate.refactoring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;

import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;
import changeassistant.clonereduction.main.CloneReductionMain;
import changeassistant.clonereduction.manipulate.util.CloneReductionApplier;
import changeassistant.multipleexample.main.Oracle;
import changeassistant.versions.treematching.edits.ITreeEditOperation.EDIT;

public class ReturnObjectCreator {
	public void createReturnObject(Oracle oracle) throws Exception{
		StringBuffer buffer = new StringBuffer();
		if(RefactoringMetaData.isNeedExitFlags()){
			buffer.append("public enum Flag{RETURN, FALLTHRU};\n");
			CloneReductionMain.refEdits.add(1, EDIT.INSERT, "enum type declaration");
			CloneReductionMain.deltaCounter.increment("created enum");
		}			
		List<String> returnNames = RefactoringMetaData.getReturnNames();
		List<String> returnTypes = RefactoringMetaData.getReturnTypes();
		if(returnNames.size() >= 2){
			buffer.append("public class RetObj {\n");
			CloneReductionMain.refEdits.add(1, EDIT.INSERT, "RetObj class declaration");
			CloneReductionMain.deltaCounter.increment("RetObj class declaration");
			String type = null, name = null;	
			// create field declarations
			for(int i = 0; i < returnNames.size(); i++){
				type = returnTypes.get(i);
				name = returnNames.get(i);
				addFieldDeclaration(buffer, type, name);
			}
			// create constructor
			buffer.append("  public RetObj(");
			for(int i = 0; i < returnNames.size(); i++){
				buffer.append(returnTypes.get(i)).append(" ").append(returnNames.get(i)).append(",");
			}
			buffer.setLength(buffer.length() - 1);
			CloneReductionMain.refEdits.add(1, EDIT.INSERT, "RetObj constructor declaration");
			CloneReductionMain.deltaCounter.increment("RetObj constructor declaration");
			buffer.append("){\n");
			for(int i = 0; i < returnNames.size(); i++){
				name = returnNames.get(i);
				buffer.append("    this.").append(name).append(" = ").append(name).append(";\n");
				CloneReductionMain.refEdits.add(1, EDIT.INSERT, "assignment inside RetObj constructor");
				CloneReductionMain.deltaCounter.increment("Statement inside RetObj constructor");
			}		
			buffer.append("  }\n");
			buffer.append("}\n");
		}				
		String result = buffer.toString();
		oracle.checkReturnObject(result);
		System.out.println(result);
	}
	
	private void addFieldDeclaration(StringBuffer buffer, String type, String name){
		buffer.append("  public ").append(type).append(" ").append(name).append(";\n");
		CloneReductionMain.refEdits.add(1, EDIT.INSERT, "field declaration in RetObj");
		CloneReductionMain.deltaCounter.increment("field declaration in RetObj");
	}
}
