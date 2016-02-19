//package changeassistant.changesuggestion.astrewrite.editcheckers;
//
//import java.util.List;
//
//import org.eclipse.jdt.core.dom.ASTNode;
//import org.eclipse.jdt.core.dom.Expression;
//
//import changeassistant.changesuggestion.expression.representation.ASTExpressionMatcher;
//import changeassistant.changesuggestion.expression.representation.AbstractExpressionRepresentationGenerator;
//import changeassistant.peers.comparison.Node;
//import changeassistant.versions.treematching.edits.UpdateOperation;
//
//public class UpdateChecker implements EditChecker<UpdateOperation>{
//
//	public boolean checkDirectApply(UpdateOperation update, Node currentNodeToUpdate) {
//		boolean flag = true;
//		String expr1 = update.getNodeToUpdate().getStrValue(); 		
//		String expr2 = currentNodeToUpdate.getStrValue();
//		if(!expr1.equals(expr2)){
//			flag = false;
//		}
//		return flag;
//	}
//	
//	public boolean checkIndirectApply(UpdateOperation update, Node currentNodeToUpdate){
//		boolean flag = true;
//		Node nodeToUpdate = update.getNodeToUpdate();
//		List<Object> exprs1 = nodeToUpdate.getExpressions();
//		List<Object> exprs2 = currentNodeToUpdate.getExpressions();
//		if(exprs1.size() != exprs2.size()){
//			flag = false;			
//		}else{
//			Expression expr1 = null;
//			Expression expr2 = null;
//			for(int i = 0; i < exprs1.size(); i ++){
//				Object obj = exprs1.get(i);
//				if(obj instanceof Expression){
//					expr1 = (Expression)obj;
//				}
//				obj = exprs2.get(i);
//				if(obj instanceof Expression){
//					expr2 = (Expression)obj;
//				}
//				if(expr1 == null || expr2 == null){
//					flag = false;
//					break;
//				}else{
//					flag = expr1.subtreeMatch(new ASTExpressionMatcher(), expr2);
//					if(!flag){
//						break;
//					}
//				}
//			}
//		}
//		
////		AbstractExpressionRepresentationGenerator aerg = new AbstractExpressionRepresentationGenerator();
////		List<String> abstractExprsOriginal = aerg.generateAbstractRepresentation(
////				nodeToUpdate.getExpressions());
////		List<String> abstractExprsNew = aerg.generateAbstractRepresentation(
////				currentNodeToUpdate.getExpressions());
////		if(abstractExprsOriginal.size() != abstractExprsNew.size()){
////			flag = false;
////		}else{
////			for(int i = 0; i < abstractExprsOriginal.size(); i ++){		
////				if(!abstractExprsOriginal.get(i).equals(abstractExprsNew.get(i))){
////					flag = false;
////					break;
////				}
////			}
////		}
//		return flag;
//	}
//
//	@Override
//	public boolean check(UpdateOperation editOperation, Node node) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//}
