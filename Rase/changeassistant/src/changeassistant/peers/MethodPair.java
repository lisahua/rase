//package changeassistant.peers;
//
//import java.util.ArrayList;
//import java.util.Enumeration;
//import java.util.List;
//import java.util.Set;
//import java.util.Stack;
//
//import org.eclipse.jdt.core.dom.ASTMatcher;
//import org.eclipse.jdt.core.dom.ASTNode;
//import org.eclipse.jdt.core.dom.Javadoc;
//import org.eclipse.jdt.core.dom.LineComment;
//import org.eclipse.jdt.core.dom.TagElement;
//
//import changeassistant.changesuggestion.expression.representation.ASTNodeCollector;
//import changeassistant.changesuggestion.expression.representation.ASTStructureMatcher;
//import changeassistant.peers.comparison.Node;
//
//public class MethodPair {
//
//	public PeerMethodADT method1;
//	public PeerMethodADT method2;
//	
//	public boolean isSame;
//	
//	public List<ASTNode> method1Specific;
//	public List<ASTNode> method2Specific;
//	
//	
//	public Node method1Node;
//	public Node method2Node;
//	
//	public ASTNode method1AST;
//	public ASTNode method2AST;
//	
//	private Node commonTree;
//	
//	public MethodPair(PeerMethodADT method1, PeerMethodADT method2, Node node1, Node node2,
//				List<ASTNode> leftMethodSpecific, List<ASTNode> rightMethodSpecific,
//				ASTNode leftAST, ASTNode rightAST){
//		this.method1 = method1;
//		this.method2 = method2;
//		this.method1Node = node1;
//		this.method2Node = node2;
//		this.method1Specific = leftMethodSpecific;
//		this.method2Specific = rightMethodSpecific;
//		this.method1AST = leftAST;
//		this.method2AST = rightAST;		
//		if(method1Specific.isEmpty()){
//			isSame = true;
//		}else{
//			isSame = false;
//		}
//	}
//	
//	public MethodPair(PeerMethodADT method1, PeerMethodADT method2, 
//			Node node1, Node node2){
//		this.method1 = method1;
//		this.method2 = method2;
//		this.method1Specific = new ArrayList<ASTNode>();
//		this.method2Specific = new ArrayList<ASTNode>();
//		parseCommon(node1, node2);
//		if(method1Specific.isEmpty()){
//			isSame = true;
//		}else{
//			isSame = false;
//		}
//	}
//	
//	public Node getCommonTree(){
//		return this.commonTree;
//	}
//	
//	public List<ASTNode> getMethod1Specific(){
//		return this.method1Specific;
//	}
//	
//	public List<ASTNode> getMethod2Specific(){
//		return this.method2Specific;
//	}	
//	
//	private void parseCommon(Node node1, Node node2){		
//		int nodeCounter = 0;
//		if(node1.getNodeType() == ASTNode.METHOD_DECLARATION){			
//			Enumeration<Node> lEnumeration = node1.breadthFirstEnumeration();			
//			Enumeration<Node> rEnumeration = node2.breadthFirstEnumeration();			
//			while(lEnumeration.hasMoreElements()){				
//				Node lNode = lEnumeration.nextElement();
//				Node rNode = rEnumeration.nextElement();
//				nodeCounter++;
//				List<Object> lExpressions = lNode.getExpressions();
//				List<Object> rExpressions = rNode.getExpressions();
//				if(lExpressions.size() != rExpressions.size())//once they are different, refuse to create the common tree
//					return;
//			}			
//			if(lEnumeration.hasMoreElements() || rEnumeration.hasMoreElements()){
//				return; // since these two trees are not quite similar
//			}else if(nodeCounter <= 1){
//				return; //since these two trees are all empty
//			}else if(nodeCounter == 2){//both the two methods only have one statement
//				Node child1 = (Node)node1.children().nextElement();
//				Node child2 = (Node)node2.children().nextElement();
//				if(!child1.isEquivalentTo(child2))
//					return;
//			}
//			{//they have similar structure, so it is possible to construct commonTree for them
//				int counter = 0;
//				commonTree = node1.shallowCopy();
//				Stack<Node> stack1 = new Stack<Node>();
//				Stack<Node> stack2 = new Stack<Node>();
//				Stack<Node> commonStack = new Stack<Node>();
//				stack1.push(node1);
//				stack2.push(node2);
//				commonStack.push(commonTree);
//				while(!stack1.isEmpty()){
//					Node lNode = stack1.pop();
//					Node rNode = stack2.pop();			
//					Node commonNode = commonStack.pop();//to parse out common expressions in two trees
//					List<Object> lExpressions = lNode.getExpressions();
//					List<Object> rExpressions = rNode.getExpressions();
//					List<Object> commonExpressions = commonNode.getExpressions();
//					for(int i = 0; i < lExpressions.size(); i ++){
//						ASTNode lEx = (ASTNode)lExpressions.get(i);
//						ASTNode rEx = (ASTNode)rExpressions.get(i);
//						if(!lEx.subtreeMatch(new ASTMatcher(), rEx)){							
//							commonExpressions.add(counter);
//							method1Specific.add(lEx);
//							method2Specific.add(rEx);
//							counter++;
//						}else{
//							commonExpressions.add(lEx);
//						}
//					}
//					//the two nodes match perfect
//					Enumeration<Node> children1 = lNode.children();
//					Enumeration<Node> children2 = rNode.children();
//					while(children1.hasMoreElements()){
//						Node child1 = children1.nextElement();
//						Node child2 = children2.nextElement();
//						Node childCopy = child1.shallowCopy();
//						commonNode.add(childCopy);//to construct a similar tree structure
//						stack1.push(child1);
//						stack2.push(child2);
//						commonStack.push(childCopy);
//					}
//				}				
//			}
//		}
//	}
//}
