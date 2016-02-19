//package changeassistant.changesuggestion.astrewrite;
//
//import java.util.List;
//
//import org.eclipse.jdt.core.dom.CompilationUnit;
//
//import changeassistant.classhierarchy.ProjectResource;
//import changeassistant.peers.MethodPair;
//import changeassistant.peers.PeerMethodADT;
//import changeassistant.peers.comparison.Node;
//import changeassistant.versions.comparison.MethodModification;
//import changeassistant.versions.treematching.edits.ITreeEditOperation;
//import changeassistant.versions.treematching.edits.InsertOperation;
//import changeassistant.versions.treematching.edits.UpdateOperation;
//
//public class PreconditionChecker {
//
//	public static boolean check(ProjectResource prLeft, ProjectResource prRight, MethodPair mp,
//			MethodModification mm){
//		PeerMethodADT method1 = mp.method1;//the known changed method
//		PeerMethodADT method2 = mp.method2;//the method to change
//		List<ITreeEditOperation> edits = mm.getEdits();
//		
//		CompilationUnit changedCu = prLeft.findClassContext(method1.classname).cu;
//		CompilationUnit cuToChange = prLeft.findClassContext(method2.classname).cu;
//		
//		try{
//			for(ITreeEditOperation edit : edits){
//				
//			}
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//		return false;
//	}
//	
//	private Node search(ITreeEditOperation edit, MethodPair mp){
//		Node commonTree = mp.getCommonTree();
//		Node foundNode = null;
//		switch(edit.getOperationType()){
//		case UPDATE:{
//			UpdateOperation uo = (UpdateOperation)edit;
//			Node parentNode = uo.getParentNode();
//			Node commonNode = commonTree.lookforNodeBasedOnPosition(parentNode);
//			foundNode = (Node)commonNode.getChildAt(uo.getPosition());
//		}
//			break;
//		}
//		return foundNode;
//	}
//}
