//package changeassistant.changesuggestion.astrewrite.editcheckers;
//
//import java.util.List;
//
//import org.eclipse.jdt.core.dom.ASTNode;
//import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
//
//import changeassistant.changesuggestion.expression.representation.ASTExpressionMatcher;
//import changeassistant.changesuggestion.expression.representation.AbstractExpressionRepresentationGenerator;
//import changeassistant.peers.comparison.Node;
//import changeassistant.versions.treematching.edits.DeleteOperation;
//
//public class DeleteChecker implements EditChecker<DeleteOperation>{
//
//	@Override
//	public boolean check(DeleteOperation delete, Node currentParent) {
//		boolean flag = true;
////		AbstractExpressionRepresentationGenerator aerg = new AbstractExpressionRepresentationGenerator();
//		Node nodeToDelete = delete.getNodeToDelete();
//		Node currentNodeToDelete = (Node)currentParent.getChildAt(delete.getPosition());
//		if(nodeToDelete.getStrValue().equals(currentNodeToDelete.getStrValue())){
//			return flag;
//		}		
//		switch(nodeToDelete.getNodeType()){
//		case ASTNode.EXPRESSION_STATEMENT: 
//		case ASTNode.IF_STATEMENT:        {
//			flag = ((ASTNode)nodeToDelete.getFirstExpression()).subtreeMatch(new ASTExpressionMatcher(), 
//					(ASTNode)currentNodeToDelete.getFirstExpression());
//			
//		}break;
//		case ASTNode.VARIABLE_DECLARATION_STATEMENT:{
//			VariableDeclarationStatement declOriginal = (VariableDeclarationStatement)
//												nodeToDelete.getExpressions().get(0);
//			VariableDeclarationStatement declNew = (VariableDeclarationStatement)
//												currentNodeToDelete.getExpressions().get(0);			
//			String typeNameO, typeNameN;
//			typeNameO = declOriginal.getType().toString();
//			typeNameN = declNew.getType().toString();
////			try{
////			    typeNameO = declOriginal.getType().resolveBinding().getQualifiedName();
////			}catch(Exception e){
////				typeNameO = declOriginal.getType().toString();
////			}
////			VariableDeclarationStatement declNew = (VariableDeclarationStatement)
////											currentNodeToDelete.getExpressions().get(0);
////			try{
////				typeNameN = declNew.getType().resolveBinding().getQualifiedName();
////			}catch(Exception e){
////				typeNameN = declNew.getType().toString();
////			}
//			if(!typeNameO.equals(typeNameN)){
//				flag = false;
//				break;
//			}
//		}break;
//		}		
//		return flag;
//	}
//
//}
