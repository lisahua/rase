package changeassistant.clonereduction.manipulate;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import changeassistant.changesuggestion.astrewrite.ASTNodeGenerator2;
import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.TypeNameTerm;
import changeassistant.clonereduction.datastructure.TreeTuple;
import changeassistant.multipleexample.common.CommonParserMulti;
import changeassistant.multipleexample.datastructure.GeneralizedStmtIndexMap;
import changeassistant.multipleexample.datastructure.MapList;
import changeassistant.multipleexample.datastructure.NodeIndexMapList;
import changeassistant.multipleexample.datastructure.SimpleASTNodesListForMethods;
import changeassistant.multipleexample.partition.MappingException;
import changeassistant.multipleexample.partition.SimpleASTCreator;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.util.SameChecker;
import changeassistant.peers.comparison.Node;

public class NodeEquivalenceChecker {
	SimpleASTNodesListForMethods simpleASTNodesListForMethods = null;
	NodeIndexMapList nodeIndexMapList = null;
	MapList mapList = null;
	GeneralizedStmtIndexMap generalizedStmtIndexMap = null;
    public NodeEquivalenceChecker(List<Node> nodes){
    	simpleASTNodesListForMethods = new SimpleASTNodesListForMethods(nodes);
    	nodeIndexMapList = new NodeIndexMapList(nodes);    
    	generalizedStmtIndexMap = new GeneralizedStmtIndexMap();
    }
    
    public SimpleASTNodesListForMethods getSimpleASTNodesListForMethods(){
    	return simpleASTNodesListForMethods;
    }
    
	public boolean checkEquivalence(List<List<Node>> incrementalNodesList, MapList knownMapList, 
			List<MethodDeclaration> mdList) throws Exception {		
		List<TreeTuple> other = null;
		mapList = knownMapList.deepCopy();
		System.out.print("");
		boolean result = true;
		int nodeIndex = -1;
		List<List<TreeTuple>> tuplesList = new ArrayList<List<TreeTuple>>();
		List<TreeTuple> tuples = TreeTuple.createTuplesForSingleMethod(incrementalNodesList.get(0), nodeIndexMapList.get(0));
		CommonParserMulti mParser = new CommonParserMulti(incrementalNodesList.size(), mapList);
		tuplesList.add(tuples);
		for(int i = 1; i < incrementalNodesList.size(); i++){
			other = TreeTuple.createTuplesForSingleMethod(incrementalNodesList.get(i), nodeIndexMapList.get(i));
			tuplesList.add(other);
			if(!TreeTuple.checkEquivalence(tuples, other)){
				result = false;
				break;
			}
		}		
		if(result){
			List<List<SimpleASTNode>> tmpStmts = new ArrayList<List<SimpleASTNode>>();
			GeneralizedStmtIndexMap generalizedSIMap = new GeneralizedStmtIndexMap();
			int nodeIndex0 = -1;
			int nodeType0 = -1;
			List<Integer> generalizedIndexes = generalizedStmtIndexMap.getIndexes(0);
			List<Integer> tmpIndexes = new ArrayList<Integer>();
			List<SimpleASTNode> gStmt = null;
			TreeTuple tuple0 = null, tuple = null;
			boolean isReturnStmt = false;
				for(int i = 0; i < tuples.size(); i++){
					tmpStmts.clear();
					tmpIndexes = new ArrayList<Integer>();
					tuple0 = tuplesList.get(0).get(i);
					nodeIndex0 = tuple0.getNodeIndex();
					nodeType0 = tuple0.getNodeType();
					if(nodeType0 == ASTNode.RETURN_STATEMENT){
						isReturnStmt = true;
					}else{
						isReturnStmt = false;
					}
					if(generalizedIndexes.contains(nodeIndex0))
						continue;
					List<ASTNode> exprs = new ArrayList<ASTNode>();
					List<ASTNode> tmpExprs = null;
					Expression expr = null;
					for(int j = 0; j < tuplesList.size(); j++){
						tuple = tuplesList.get(j).get(i);
						nodeIndex = tuple.getNodeIndex();
						tmpIndexes.add(nodeIndex);
						tmpStmts.add(simpleASTNodesListForMethods.getExprs(j, nodeIndex));
						if(isReturnStmt){
							tmpExprs = nodeIndexMapList.getNode(j, nodeIndex).getASTExpressions2();
							if(!tmpExprs.isEmpty()){
								exprs.add(tmpExprs.get(0));
							}
						}
					}
//					System.out.print("");
					gStmt = mParser.getCommon(tmpStmts, mdList);
					if(tmpStmts.get(0).isEmpty() && gStmt.isEmpty() || !tmpStmts.get(0).isEmpty() && !gStmt.isEmpty()){
						generalizedSIMap.put(tmpIndexes, gStmt);
						if(isReturnStmt){
							List<Term> typeTerms = new ArrayList<Term>();
							List<String> typeNames = new ArrayList<String>();
							String typeName = null;
							for(int j = 0; j < exprs.size(); j++){
								expr = (Expression)exprs.get(j);
								ITypeBinding binding = expr.resolveTypeBinding();
								typeName = binding.getName();
								typeNames.add(typeName);
								typeTerms.add(new TypeNameTerm(-1, typeName, binding.getQualifiedName()));							
							}
							if(boxTypes(typeNames)){
								
							}else{
								mParser.getCommonTypeName(typeTerms);
							}
							
						}
					}else{
						result = false;
						break;
					}				
				}	
				if(result)
					generalizedStmtIndexMap.putAll(generalizedSIMap);
		}		
		return result;
	}
	
	private boolean boxTypes(List<String> typeNames){
		String primitiveType = null;
		String boxType = null;
		for(String typeName : typeNames){
			if(ASTNodeGenerator2.primitiveTypeStrings.contains(typeName)){
				primitiveType = typeName;
				break;
			}
		}
		if(primitiveType != null){
			if(primitiveType.equals("boolean")){
				boxType = "Boolean";
			}else if(primitiveType.equals("float")){
				boxType = "Float";
			}else if(primitiveType.equals("double")){
				boxType = "Double";
			}else if(primitiveType.equals("int")){
				boxType = "Integer";
			}else if(primitiveType.equals("long")){
				boxType = "Long";
			}else if(primitiveType.equals("char")){
				boxType = "Character";
			}else if(primitiveType.equals("short")){
				boxType = "Short";
			}
			for(String typeName : typeNames){
				if(!typeName.equals(boxType) && !typeName.equals(primitiveType)){
					return false;
				}
			}
			return true;
		}else{
			return false;
		}
	}
	
	public TreeTuple getTuple(int index, int cIndex, Node parent, int i){
		int stmtIndex = nodeIndexMapList.get(i).get(parent);
		return new TreeTuple(index, cIndex, stmtIndex, parent.getNodeType());
	}
	
	public MapList getMapList(){
		return mapList;
	}
	
	public NodeIndexMapList getNodeIndexMapList(){
		return nodeIndexMapList;
	}
	
	public GeneralizedStmtIndexMap getGeneralizedStmtIndexMap(){
		return generalizedStmtIndexMap;
	}
	
	public void setGeneralizedStmtIndexMap(GeneralizedStmtIndexMap other){
		generalizedStmtIndexMap = other;
	}
}
