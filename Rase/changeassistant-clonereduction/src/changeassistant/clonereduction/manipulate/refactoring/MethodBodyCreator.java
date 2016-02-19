package changeassistant.clonereduction.manipulate.refactoring;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;
import changeassistant.clonereduction.main.CloneReductionMain;
import changeassistant.clonereduction.manipulate.convert.AssignmentConverter;
import changeassistant.clonereduction.manipulate.convert.MethodInvocationConverter;
import changeassistant.clonereduction.manipulate.convert.ReturnStatementConverter;
import changeassistant.clonereduction.manipulate.convert.TemplateMethodInterfaceConverter;
import changeassistant.multipleexample.apply.CodeGenerator;
import changeassistant.multipleexample.datastructure.GeneralizedStmtIndexMap;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.peers.SourceCodeRange;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.treematching.edits.ITreeEditOperation.EDIT;

public class MethodBodyCreator {

	public String createBody(String header, List<SimpleTreeNode> pSNodes,
			Node mNode, 
			List<List<SimpleASTNode>> pCusNodesList) {
		System.out.print("");
		StringBuffer buffer = new StringBuffer();
		//convert $u, $m, $t, $v
		List<SimpleTreeNode> newNodes = TemplateMethodInterfaceConverter
			.convert(pSNodes, pCusNodesList, mNode.getMethodDeclaration());
		//convert method invocations with implicit this pointer
		if(RefactoringMetaData.isHasImplicitThis()){
			MethodInvocationConverter.convert(newNodes, mNode, pCusNodesList);	
		}	
		//convert return stmt, and add some if needed
		ReturnStatementConverter.convert(newNodes, mNode, pCusNodesList);
//		System.out.print("");
		int counter = 1;
		for(SimpleTreeNode nNode : newNodes){
			counter += nNode.countNodes();
		}
		CloneReductionMain.refEdits.add(counter, EDIT.INSERT, "extracted method relevant");
		CloneReductionMain.deltaCounter.increment(counter, "extracted method relevant");
		buffer.append(header).append(((Block)CodeGenerator.create(newNodes, ASTParser.K_STATEMENTS)).toString());
		return buffer.toString();
	}
	
	public String createBody(String header, Node mNode,
			List<Node> markedNodes,
			Map<String, String> sTou, 
			Map<String, SourceCodeRange> miMap,
			GeneralizedStmtIndexMap generalizedStmtIndexMap){		
		StringBuffer buffer = new StringBuffer();
//		System.out.print("");
		Map<Integer, List<SimpleASTNode>> indexStmtMap = generalizedStmtIndexMap.getIndexStmtMap(0);
		List<SimpleTreeNode> newNodes = TemplateMethodInterfaceConverter.convert(markedNodes,indexStmtMap, mNode, sTou);
		//convert method invocations with implict this pointer
		if(RefactoringMetaData.isNeedTemplateClass()){
			MethodInvocationConverter.convert(newNodes, mNode, miMap, indexStmtMap);
		}
		Set<VariableTypeBindingTerm> outputFieldTerms = RefactoringMetaData.getOutputFieldTerms();
		if(!outputFieldTerms.isEmpty() && RefactoringMetaData.isNeedTemplateClass()){
			AssignmentConverter.convert(newNodes, mNode, indexStmtMap, outputFieldTerms, sTou);
		}
		ReturnStatementConverter.convert(newNodes, mNode, indexStmtMap, sTou);		
		int counter = 1;
		for(SimpleTreeNode nNode : newNodes){
			counter += nNode.countNodes();
		}
		CloneReductionMain.refEdits.add(counter, EDIT.INSERT, "extracted method relevant");
		CloneReductionMain.deltaCounter.increment(counter, "extracted method relevant");
		buffer.append(header).append(((Block)CodeGenerator.create(newNodes, ASTParser.K_STATEMENTS)).toString());	
		return buffer.toString();
 	}
}
