package changeassistant.internal;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import changeassistant.peers.SourceCodeRange;
import changeassistant.peers.comparison.Node;

public class NodeASTMapping {
	
	/**
	 * The MethodDeclaration node is not included in the mapping
	 * @param md
	 * @param root
	 * @return
	 */
	public static Map<ASTNode, Node> createASTNodeMap(MethodDeclaration md, Node root){
		Map<ASTNode, Node> astNodeMap = new HashMap<ASTNode, Node>();
		Enumeration<Node> bEnum = root.breadthFirstEnumeration();
		bEnum.nextElement();//do not put the methodDeclaration itself into the mapping, since this does not make too much sense
		Node bNode = null;
		ASTElementSearcher searcher = new ASTElementSearcher(md); 			
		while(bEnum.hasMoreElements()){
			bNode = bEnum.nextElement();
			astNodeMap.put(searcher.findElement(bNode.getSourceCodeRange()), bNode);
		}
		return astNodeMap;
	}
	
	public static Map<Node, ASTNode> createNodeASTMap(MethodDeclaration md, Node root){
		Map<Node, ASTNode> nodeASTMap = new HashMap<Node, ASTNode>();
		Enumeration<Node> bEnum = root.breadthFirstEnumeration();
		bEnum.nextElement();
		Node bNode = null;
		ASTElementSearcher searcher = new ASTElementSearcher(md);
		while(bEnum.hasMoreElements()){
			bNode = bEnum.nextElement();
			nodeASTMap.put(bNode, searcher.findElement(bNode.getSourceCodeRange()));
		}
		return nodeASTMap;
	}
	
//	public static Set<Node> searchforRelevantNodes(
//			Map<ASTNode, Node> astNodeMap, 
//			List<SourceCodeRange>astNodesDependingOn){
//		Set<Node> result = new HashSet<Node>();
//		List<ASTNode> astNodeKeys = new ArrayList<ASTNode>(astNodeMap.keySet());
//		EnclosingASTFinder eASTFinder = new EnclosingASTFinder();
//		List<ASTNode> tempASTNodes = null;
//		Node temp = null;
//		for(SourceCodeRange scr : astNodesDependingOn){
//			tempASTNodes = eASTFinder.lookforEnclosingAST(astNodeKeys, scr);
//			if(tempASTNodes == null){
////				System.out.println("There is no enclosing AST " + scr);
//				//do nothing, since no ASTNode encloses it
//			}else{
//				for(ASTNode tempASTNode : tempASTNodes){//tempASTNode may be Block object--which is not modeled in the Node Tree
//					temp = astNodeMap.get(tempASTNode);
//					if(temp != null)
//						result.add(temp);
//				}
//			}
//		}
//		return result;
//	}
}
