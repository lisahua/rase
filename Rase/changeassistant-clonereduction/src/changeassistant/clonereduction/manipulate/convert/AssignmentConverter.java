package changeassistant.clonereduction.manipulate.convert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;
import changeassistant.changesuggestion.expression.representation.Term.TermType;
import changeassistant.clonereduction.manipulate.refactoring.IdGeneralizer;
import changeassistant.internal.ASTNodeFinder;
import changeassistant.multipleexample.partition.SimpleASTCreator;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.peers.SourceCodeRange;
import changeassistant.peers.comparison.Node;

public class AssignmentConverter {
	
	public static void convert(List<SimpleTreeNode> sNodes2, Node mNode, 
			Map<Integer, List<SimpleASTNode>> indexStmtMap, Set<VariableTypeBindingTerm> outputFieldTerms,
			Map<String, String> sTou){
		List<SimpleASTNode> nodes = null;
		SimpleASTCreator creator = new SimpleASTCreator();
		List<List<SimpleASTNode>> stmtList = creator.createSimpleASTNodesList(mNode);
		SimpleASTNode node = null;
		SimpleASTNode tmp = null;
		VariableTypeBindingTerm term = null;
		int index = -1;
		SimpleASTNode newTmp = null;
		List<String> fieldNames = new ArrayList<String>();
		List<String> fieldTypes = new ArrayList<String>();
		for(VariableTypeBindingTerm t : outputFieldTerms){
			fieldNames.add(t.getName());
			fieldTypes.add(t.getTypeNameTerm().getName());
		}
		for(SimpleTreeNode s : sNodes2){
			index = s.getNodeIndex();
			nodes = stmtList.get(index - 1);
			if(nodes.size() == 1){
				node = nodes.get(0);
				if(node.getNodeType() == ASTNode.ASSIGNMENT){//node is assignment
					tmp = (SimpleASTNode) node.getChildAt(0);//left expression
					switch(tmp.getNodeType()){
					case ASTNode.SIMPLE_NAME: term = (VariableTypeBindingTerm) tmp.getTerm();
											  String termName = term.getName();
											  if(fieldNames.contains(termName) && 
													  fieldTypes.get(fieldNames.indexOf(termName)).equals(term.getTypeNameTerm().getName())){												 
												  nodes.clear();
												  newTmp = new SimpleASTNode(ASTNode.VARIABLE_DECLARATION_EXPRESSION, 
														  IdGeneralizer.generalize(term.getTypeNameTerm().getName(), 
																  sTou, TermType.TypeNameTerm) + " " + indexStmtMap.get(index).get(0).getStrValue(), 0, 0);
												  nodes.add(newTmp);
												  indexStmtMap.put(index, nodes);
												  s.setNodeType(ASTNode.VARIABLE_DECLARATION_STATEMENT);
												  s.setStrValue(newTmp.getStrValue());
											  }
											  break;
					}
				}								
			}
		}
	}
}
