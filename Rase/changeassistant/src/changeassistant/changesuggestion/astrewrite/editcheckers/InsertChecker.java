//package changeassistant.changesuggestion.astrewrite.editcheckers;
//
//import java.util.List;
//
//import org.eclipse.jdt.core.dom.ASTNode;
//
//import changeassistant.changesuggestion.expression.representation.AbstractExpressionRepresentationGenerator;
//import changeassistant.peers.comparison.Node;
//import changeassistant.versions.treematching.edits.InsertOperation;
//
//public class InsertChecker implements EditChecker<InsertOperation> {
//
//	@Override
//	public boolean check(InsertOperation insert, Node currentParent) {
//		AbstractExpressionRepresentationGenerator aerg = new AbstractExpressionRepresentationGenerator();
//		Node parent = insert.getParentNode();
//		Node nodeToInsert = insert.getNodeToInsert();
//		boolean flag = true;
//		switch(nodeToInsert.getNodeType()){
//		case ASTNode.CATCH_CLAUSE:{//judge whether the try body can throw such exception
//			//if not, the insert can not happen
//			Node tryBlock = (Node) parent.getChildAt(0);
//			List<Object> expressions = tryBlock.getAllExpressionContained();
//			List<String> abstractExprs = aerg.generateAbstractRepresentation(expressions);			
//			
//			Node tryBlock2 = (Node)currentParent.getChildAt(0);
//			List<Object> expressions2 = tryBlock2.getAllExpressionContained();
//			List<String> abstractExprs2 = aerg.generateAbstractRepresentation(expressions2);
//			
//			for(int i = 0; i < abstractExprs.size(); i++){
//				if(!abstractExprs2.contains(abstractExprs.get(i))){
//					flag = false;//do not contain all expressions in the original try block
//					break;
//				}
//			}			
//			//if tryBlock2 contains all possible statements mentioned in tryBlock1, the the catch clause should be added 
//		}break;
//		}
//		return flag;
//	}
//	
//}
