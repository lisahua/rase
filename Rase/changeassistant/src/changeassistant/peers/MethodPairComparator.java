//package changeassistant.peers;
//
//import java.util.ArrayList;
//import java.util.Enumeration;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Set;
//
//import org.eclipse.jdt.core.dom.ASTNode;
//import org.eclipse.jdt.core.dom.MethodDeclaration;
//
//import changeassistant.changesuggestion.expression.representation.ASTStructureMatcher;
//import changeassistant.classhierarchy.ClassContext;
//import changeassistant.classhierarchy.ProjectResource;
//import changeassistant.internal.MethodADT;
//import changeassistant.peers.comparison.Node;
//
//public class MethodPairComparator {
//	
//	private List<ASTNode> leftMethodSpecific = new ArrayList<ASTNode>();
//	private List<ASTNode> rightMethodSpecific = new ArrayList<ASTNode>();
//
////	public void compare(List<Set<PeerMethodADT>> potentialPeers, ProjectResource projectResource){
////		for(Set<PeerMethodADT> peerSet : potentialPeers){
////			//compare any two methods within the set
////			List<PeerMethodADT> peerList = new ArrayList<PeerMethodADT>(peerSet);
////			for(int i = 0; i < peerList.size() - 1; i ++){				
////				Node left = projectResource.findClassContext(peerList.get(i).classname)
////								.getMethodNode(peerList.get(i).methodSignature);			
////				for(int j = i + 1; j < peerList.size(); j ++){
////					Node right = projectResource.findClassContext(peerList.get(j).classname)
////									.getMethodNode(peerList.get(j).methodSignature);						
////					if(isSimilar(left, right)){
////						System.out.println("the two nodes are similar to each other");
////					}
////				}
////			}
////		}
////	}
//	
//	public Set<MethodPair> compare(PeerMethodADT pmADT, Set<PeerMethodADT> potentialPeers, ProjectResource projectResource){
//		Set<MethodPair> mps = new HashSet<MethodPair>();
//		List<PeerMethodADT> peerList = new ArrayList<PeerMethodADT>(potentialPeers);
//		ClassContext ccLeft = projectResource.findClassContext(pmADT.classname);
//		ASTNode leftAST = ccLeft.getMethodAST(pmADT.methodSignature);
//		Node left = ccLeft.getMethodNode(pmADT.methodSignature);		
//		
//		for(int i = 0; i < peerList.size(); i ++){
//			PeerMethodADT rightPeer = peerList.get(i);
//			ClassContext ccRight = projectResource.findClassContext(rightPeer.classname);
//			Node right = ccRight.getMethodNode(rightPeer.methodSignature);
//			ASTNode rightAST = ccRight.getMethodAST(rightPeer.methodSignature);
//			System.out.println("comparing " + pmADT.classname + "." + pmADT.methodSignature + " and " +
//					rightPeer.classname + "." + rightPeer.methodSignature);
//			
//			try{
//			if(isSimilar(left, right)){//the most coarse comparison
//				MethodPair mp = new MethodPair(pmADT, rightPeer, left, right,
//						leftMethodSpecific, rightMethodSpecific, leftAST, rightAST);
//				mps.add(mp);							
//			}
//			}catch(Exception e){
//				e.printStackTrace();
//			}
//		}		
//		if(mps.size() > 0){
//			System.out.println("Found some peers!");
//		}
//		return mps;
//	}
//	/**
//	 * Judge whether two trees are similar to each other according to node types of high level nodes.
//	 * @param left
//	 * @param right
//	 * @return
//	 */
////	private static boolean isSimilar(Node left, Node right){
////		Enumeration<Node> lEnumeration = left.breadthFirstEnumeration();
////		Enumeration<Node> rEnumeration = right.breadthFirstEnumeration();
////		while(lEnumeration.hasMoreElements() && rEnumeration.hasMoreElements()){
////			Node lNode = lEnumeration.nextElement();
////			Node rNode = rEnumeration.nextElement();
////			if(lNode.getNodeType() != rNode.getNodeType())
////				return false;
////		}
////		if(lEnumeration.hasMoreElements() | rEnumeration.hasMoreElements())
////			return false;
////		return true;
////	}
//	
//	private boolean isSimilar(Node left, Node right){		
//		ASTStructureMatcher astMatcher = new ASTStructureMatcher();		
//		boolean flag = true;
//		Enumeration<Node> lEnumeration = left.breadthFirstEnumeration();
//		Enumeration<Node> rEnumeration = right.breadthFirstEnumeration();
//		int counter = 0;
//		while(lEnumeration.hasMoreElements() && rEnumeration.hasMoreElements()){
//			Node lNode = lEnumeration.nextElement();
//			Node rNode = rEnumeration.nextElement();
//			if(lNode.getNodeType() != rNode.getNodeType()){
//				flag = false;
//				break;
//			}else{		
//				counter++;
//				try{
//				astMatcher.safeSubtreeListMatch(convertToASTNodeList(lNode.getExpressions()), 
//						convertToASTNodeList(rNode.getExpressions()));
//				}catch(Exception e){
//					e.printStackTrace();
//				}
//			}
//		}
//		if(lEnumeration.hasMoreElements() || rEnumeration.hasMoreElements()){
//			flag = false;
//		}else if(counter == 1){
//			flag = false;
//		}else if(counter == 2 && !astMatcher.getMethodSpecific(1).isEmpty()){
//			flag = false;
//		}else if(flag){//if the two nodes are matched			
//			this.leftMethodSpecific = astMatcher.getMethodSpecific(1);
//			this.rightMethodSpecific = astMatcher.getMethodSpecific(2);			
//		}		
//		return flag;
//	}
//	
//	private List<ASTNode> convertToASTNodeList(List<Object> objs){
//		List<ASTNode> list = new ArrayList<ASTNode>();
//		for(Object obj : objs){
//			list.add((ASTNode)obj);
//		}
//		return list;
//	}
//}
