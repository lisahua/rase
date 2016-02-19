package changeassistant.clonereduction.manipulate.convert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;

import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;
import changeassistant.changesuggestion.expression.representation.Term.TermType;
import changeassistant.clonereduction.helper.ParamListHelper;
import changeassistant.clonereduction.main.CloneReductionMain;
import changeassistant.clonereduction.manipulate.data.MethodInfo;
import changeassistant.clonereduction.manipulate.data.VariableInfo;
import changeassistant.clonereduction.manipulate.refactoring.IdGeneralizer;
import changeassistant.clonereduction.manipulate.refactoring.RefactoringMetaData;
import changeassistant.multipleexample.main.Constants;
import changeassistant.multipleexample.match.PatternUtil;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.multipleexample.util.NodeUtil;
import changeassistant.peers.SourceCodeRange;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.treematching.edits.ITreeEditOperation.EDIT;

public class ReturnStatementConverter {

	private static Set<Node> returnNodes = null;
	private static Set<Node> flowNodes = null;
	private static Set<SimpleTreeNode> sRetNodes = null;
	private static Set<SimpleTreeNode> sFlowNodes = null;
	private static List<List<SimpleASTNode>> nodesList = null;
	private static List<SimpleTreeNode> sNodes = null;
	private static Map<Integer, List<SimpleASTNode>> indexStmtMap = null;
	private static int maxIndex = -1;
	
	private static void modifyReturnExpr(){
		SimpleTreeNode sTmp = null;
		String tmpStr = null;
		StringBuffer buffer = null;
		boolean hasRetVal = RefactoringMetaData.isHasRetVal();
		for(SimpleTreeNode r : sRetNodes){
			sTmp = lookforNode(r.getNodeIndex());
			tmpStr = sTmp.getStrValue().trim();
			if(tmpStr.contains(Constants.RETURN)){
				if(hasRetVal){
					tmpStr = tmpStr.substring("return".length() + 1, tmpStr.length() - 1);
					List<String> returnNames = new ArrayList<String>(RefactoringMetaData.getReturnNames());
					if(returnNames.size() == 1){
						r.setStrValue(tmpStr);
					}else{
						StringBuffer tmpBuffer = new StringBuffer();
						System.out.print("");
						for(String retName : returnNames){
							if(retName.equals(Constants.RET_VAR)){
								tmpBuffer.append(tmpStr).append(",");
							}else if(retName.equals(Constants.FLAG_VAR)){
								tmpBuffer.append(Constants.FLAG_RET).append(",");
							}else{
								tmpBuffer.append(retName).append(",");
							}
						}
						if(tmpBuffer.length() > 0){
							tmpBuffer.setLength(tmpBuffer.length() - 1);
						}
						if(RefactoringMetaData.isNeedExitFlags() && !tmpBuffer.toString().contains(Constants.FLAG_RET)){
							r.setStrValue("return new RetObj("+ Constants.FLAG_RET + "," + tmpBuffer.toString() + ");");
						}else{							
							r.setStrValue("return new RetObj(" + tmpBuffer.toString() + ");");
						}	
					}									
				}else{
					if(RefactoringMetaData.isNeedRetObj()){
						buffer = new StringBuffer();
						buffer.append("return new RetObj(").append(Constants.FLAG_RET).append(",");
						List<String> returnTypes = RefactoringMetaData.getReturnTypes();
						for(int i = 1; i < returnTypes.size(); i++){
							buffer.append(MethodInfo.getDefaultValue(returnTypes.get(i))).append(",");
						}
						buffer.setLength(buffer.length() - 1);
						buffer.append(");");
						tmpStr = buffer.toString();
					}else{
						tmpStr = "return " + Constants.FLAG_RET;
					}					
					r.setStrValue(tmpStr);
				}
			}
		}	
	}
	
	private static void addReturnStmt(int nodeType, String strValue){
		List<SimpleASTNode> exprs = new ArrayList<SimpleASTNode>();
		exprs.add(new SimpleASTNode(nodeType, strValue, 0, 0));
		nodesList.add(exprs);
		sNodes.add(new SimpleTreeNode(ASTNode.RETURN_STATEMENT, SourceCodeRange.DefaultRange, 
				PatternUtil.createStrValue(ASTNode.RETURN_STATEMENT, "", exprs), nodesList.size()));
	}
	
	private static void addReturnStmt2(int nodeType, String strValue){
		List<SimpleASTNode> exprs = new ArrayList<SimpleASTNode>();
		exprs.add(new SimpleASTNode(nodeType, strValue, 0, 0));
		++maxIndex;
		indexStmtMap.put(maxIndex, exprs);
		sNodes.add(new SimpleTreeNode(ASTNode.RETURN_STATEMENT, SourceCodeRange.DefaultRange, 
				PatternUtil.createStrValue(ASTNode.RETURN_STATEMENT, "", exprs), maxIndex));
	}
	
	public static void initialize(List<SimpleTreeNode> sNodes2, Node mNode){
		sNodes = sNodes2;
		returnNodes = RefactoringMetaData.getReturnNodes();	
		flowNodes = RefactoringMetaData.getFlowNodes();
		NodeUtil util = new NodeUtil();
		Map<Node, Integer> map = util.createNodeIndexMap(mNode);		
		sRetNodes = lookforSNodes(map, sNodes, returnNodes);
		sFlowNodes = lookforSNodes(map, sNodes, flowNodes);
	}
	
	public static void convert(List<SimpleTreeNode> sNodes2, Node mNode, 
			List<List<SimpleASTNode>> pCusNodesList){
		String returnType = RefactoringMetaData.getReturnType();
		if(returnType.equals(Constants.VOID))
			return;
		initialize(sNodes2, mNode);
		nodesList = pCusNodesList;
		
		//meaning that there is only a return value created to indicate flow change
		if(returnType.equals(Constants.FLAG)){
			modifyReturnExpr();
			addReturnStmt(ASTNode.FIELD_ACCESS, Constants.FLAG_FALL);
			return;
		}		
		//meaning that there is only one return value
		if(!returnType.equals(Constants.RETOBJ)){
			if(RefactoringMetaData.isHasRetVal()){
				return;//the return value is provided by returnStmt
			}else{//meaning that it must be an outputTerm/outputFieldTerm
				VariableTypeBindingTerm term = RefactoringMetaData.getTermToReturn();
				if(!returnNodes.isEmpty()){
					modifyReturnExpr();		
				}else{
					addReturnStmt(ASTNode.SIMPLE_NAME, term.getName());
				}				
			}
		}else{//returnType.equals(RefactoringMetaData.RETOBJ
			StringBuffer buffer = new StringBuffer();
			List<String> returnNames = new ArrayList<String>(RefactoringMetaData.getReturnNames());			
			if(returnNodes.isEmpty()){
				for(String rName : returnNames){
					buffer.append(rName).append(",");
				}				
			}else{//!returnNodes.isEmpty()			
				if(!flowNodes.isEmpty()){
					List<String> returnTypes = RefactoringMetaData.getReturnTypes();
					String name = null;
					for(int i = 0; i < returnNames.size(); i++){
						name = returnNames.get(i);
						if(name.equals(Constants.FLAG_VAR)){
							buffer.append(Constants.FLAG_FALL).append(",");
						}else if(name.equals(Constants.RET_VAR)){
							buffer.append(MethodInfo.getDefaultValue(returnTypes.get(i))).append(",");
						}else{
							buffer.append(returnNames.get(i)).append(",");
						}
					}
				}
				modifyReturnExpr();
			}
			buffer.setLength(buffer.length() - 1);
			addReturnStmt(ASTNode.CLASS_INSTANCE_CREATION, "new RetObj(" + buffer.toString() + ")");
		}
	}
	
	public static void convert(List<SimpleTreeNode> sNodes2, Node mNode, Map<Integer, List<SimpleASTNode>> indexStmtMap2,
			Map<String, String> sTou){
		String returnType = RefactoringMetaData.getReturnType();
		if(returnType.equals(Constants.VOID))
			return;
		initialize(sNodes2, mNode);
		indexStmtMap = indexStmtMap2;
		maxIndex = Collections.max(indexStmtMap.keySet());
		
		//meaning that there is only a return value created to indicate flow change
		if(returnType.equals(Constants.FLAG)){
			modifyReturnExpr();
			addReturnStmt2(ASTNode.FIELD_ACCESS, Constants.FLAG_FALL);
			return;
		}
		//meaning that there is only one return value
		if(!returnType.equals(Constants.RETOBJ)){
			if(RefactoringMetaData.isHasRetVal()){
				return;//the return value is provided by returnStmt
			}else{//meaning that it must be an outputTerm/outputFieldTerm
				VariableTypeBindingTerm term = RefactoringMetaData.getTermToReturn();
				if(!returnNodes.isEmpty()){
					modifyReturnExpr();		
				}else{
					addReturnStmt2(ASTNode.SIMPLE_NAME, 
							IdGeneralizer.generalize(term.getName(), sTou, TermType.VariableTypeBindingTerm));
				}				
			}
		}else{//returnType.equals(RefactoringMetaData.RETOBJ
			StringBuffer buffer = new StringBuffer();
			List<String> returnNames = new ArrayList<String>(RefactoringMetaData.getReturnNames());			
			if(returnNodes.isEmpty()){
				for(String rName : returnNames){
					buffer.append(rName).append(",");
				}				
			}else{//!returnNodes.isEmpty()			
				if(!flowNodes.isEmpty()){
					List<String> returnTypes = RefactoringMetaData.getReturnTypes();
					String name = null;
					for(int i = 0; i < returnNames.size(); i++){
						name = returnNames.get(i);
						if(name.equals(Constants.FLAG_VAR)){
							buffer.append(Constants.FLAG_FALL).append(",");
						}else if(name.equals(Constants.RET_VAR)){
							buffer.append(MethodInfo.getDefaultValue(returnTypes.get(i))).append(",");
						}else{
							buffer.append(returnNames.get(i)).append(",");
						}
					}
				}
				modifyReturnExpr();
			}
			if(buffer.length() > 0){
				buffer.setLength(buffer.length() - 1);
				addReturnStmt2(ASTNode.CLASS_INSTANCE_CREATION, "new RetObj(" + buffer.toString() + ")");
			}
		}
	}
	
	private static Set<SimpleTreeNode> lookforSNodes(Map<Node, Integer> map, List<SimpleTreeNode> sNodes, Set<Node> nodes){
		Set<SimpleTreeNode> result = new HashSet<SimpleTreeNode>();
		int index = -1;
		SimpleTreeNode sTmp = null;
		for(Node n : nodes){
			index = map.get(n);
			sTmp = lookforNode(index);
			if(sTmp != null)
				result.add(sTmp);
		}
		assert nodes.size() == result.size();
		return result;
	}
	
	private static SimpleTreeNode lookforNode(int index){
		SimpleTreeNode result = null;
		for(SimpleTreeNode sNode : sNodes){
			result = sNode.lookforNodeBasedOnIndex(index);
			if(result != null)
				return result;
		}
		return result;
	}
}
