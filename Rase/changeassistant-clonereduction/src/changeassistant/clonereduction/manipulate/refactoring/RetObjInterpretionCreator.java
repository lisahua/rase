package changeassistant.clonereduction.manipulate.refactoring;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Type;

import changeassistant.clonereduction.main.CloneReductionMain;
import changeassistant.clonereduction.manipulate.data.MethodInfo;
import changeassistant.multipleexample.main.Constants;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.peers.SourceCodeRange;
import changeassistant.versions.treematching.edits.ITreeEditOperation.EDIT;

public class RetObjInterpretionCreator {

	public void interpret(SimpleTreeNode parentNode, int index, List<String> outputTermString,
			MethodDeclaration md){
		SimpleTreeNode newNode = null;
		SimpleTreeNode tmpNode = null;
		if(RefactoringMetaData.isNeedExitFlags()){
			if(RefactoringMetaData.getReturnType().equals(Constants.FLAG)){
				newNode = createNode(ASTNode.IF_STATEMENT, "if (" + Constants.FLAG_VAR + ".equals("
						+ Constants.FLAG_RET + "))");
			}else{
				newNode = createNode(ASTNode.IF_STATEMENT, 
						"if (retObj.flag.equals(" + Constants.FLAG_RET + "))");
			}
			CloneReductionMain.refEdits.add(1, EDIT.INSERT, "retObj interpreter if-check");
			CloneReductionMain.refEdits.add(1, EDIT.INSERT, "retObj interpreter then");
			CloneReductionMain.deltaCounter.increment(2, "retObj interpreter if-then");
			int parentNodeType = parentNode.getNodeType();
			if(parentNodeType == ASTNode.IF_STATEMENT || parentNodeType == ASTNode.TRY_STATEMENT){
				List<SimpleTreeNode> currentChildren = new ArrayList<SimpleTreeNode>();								
				switch(parentNodeType){
				case ASTNode.IF_STATEMENT:
					for(int i = 0; i < parentNode.getChildCount(); i++){
						currentChildren.add((SimpleTreeNode) parentNode.getChildAt(i));
					}
					parentNode.removeAllChildren();
					parentNode.add(createNode(ASTNode.BLOCK, "then"));
					parentNode = (SimpleTreeNode) parentNode.getChildAt(0);
					break;
				case ASTNode.TRY_STATEMENT:
					for(int i = 0; i < parentNode.getChildCount(); i++){
						SimpleTreeNode tmp = (SimpleTreeNode) parentNode.getChildAt(i);
						if(tmp.getNodeType() == ASTNode.CATCH_CLAUSE){
							break;
						}else{
							currentChildren.add(tmp);
						}
					}
					for(int i = 0; i < currentChildren.size(); i++){
						parentNode.remove(0);
					}
					parentNode.insert(createNode(ASTNode.BLOCK, "try-body"), 0);
					parentNode = (SimpleTreeNode) parentNode.getChildAt(0);
					break;
				}				
				for(int i = 0; i < currentChildren.size(); i++){
					parentNode.add(currentChildren.get(i));
				}
			}
			parentNode.insert(newNode, ++index);
			if(RefactoringMetaData.isHasRetVal()){				
				tmpNode = createNode(ASTNode.RETURN_STATEMENT, "return retObj.retVal;"); 
			}else{
				if(md.getReturnType2() == null || md.getReturnType2().toString().equals(Constants.VOID)){
					tmpNode = createNode(ASTNode.RETURN_STATEMENT, "return;");		
				}else{
					tmpNode = createNode(ASTNode.RETURN_STATEMENT, "return null;");
				}						
			}
			newNode.add(tmpNode);
			CloneReductionMain.refEdits.add(1, EDIT.INSERT, "retObj interpreter return retVal");
			CloneReductionMain.deltaCounter.increment(1, "return retVal");
		}
		List<String> outputTermParams = RefactoringMetaData.getReturnNames();
		List<String> outputTermParamTypes = RefactoringMetaData.getReturnTypes();
		List<Boolean> needDecls = RefactoringMetaData.getNeedDeclForInterFlags();
		int startIndex = outputTermParams.size() - outputTermString.size();
		int tmpIndex = -1;
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i < outputTermString.size(); i++){
			buffer.setLength(0);
			tmpIndex = i + startIndex;
			if(needDecls.get(tmpIndex)){
				buffer.append(outputTermParamTypes.get(tmpIndex)).append(" "); 
			}else if(tmpIndex == 1 && RefactoringMetaData.isNeedExitFlags() && RefactoringMetaData.isHasRetVal()){
				continue;
			}
			buffer.append(outputTermString.get(i)).append(" = retObj.").append(outputTermParams.get(tmpIndex));
			tmpNode = createNode(ASTNode.EXPRESSION_STATEMENT, 
					buffer.toString());
			parentNode.insert(tmpNode, ++index);	
			CloneReductionMain.refEdits.add(1, EDIT.INSERT, "retObj interpreter value assignment");
			CloneReductionMain.deltaCounter.increment(1, "RetObj interpreter value assignment");
		}
		if(parentNode.getNodeType() == ASTNode.METHOD_DECLARATION && parentNode.getChildCount() == index + 1){
			Type t = md.getReturnType2();
			if(t != null && !t.toString().equals(Constants.VOID)){
				parentNode.add(createNode(ASTNode.RETURN_STATEMENT, "return " + 
						MethodInfo.getDefaultValue(md.getReturnType2().toString())));
				CloneReductionMain.refEdits.add(1, EDIT.INSERT, "retObj interpreter return statement");
				CloneReductionMain.deltaCounter.increment(1, "RetObj interpreter return statement");
			}
		}
	}
	
	private SimpleTreeNode createNode(int nodeType, String strValue){
		return  new SimpleTreeNode(nodeType, SourceCodeRange.DefaultRange, strValue, 0);
	}
}
