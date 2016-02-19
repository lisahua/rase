//package changeassistant.changesuggestion;
//
//import java.util.Iterator;
//import java.util.Map;
//import java.util.Set;
//
//import changeassistant.changesuggestion.astrewrite.ASTRewriteBasedManipulator;
//import changeassistant.changesuggestion.astrewrite.PostconditionChecker;
//import changeassistant.changesuggestion.astrewrite.PreconditionChecker;
//import changeassistant.classhierarchy.ClassContext;
//import changeassistant.classhierarchy.ProjectResource;
//import changeassistant.internal.WorkspaceUtilities;
//import changeassistant.peers.MethodPair;
//import changeassistant.peers.PeerMethodADT;
//import changeassistant.versions.comparison.MethodModification;
//
//public class ChangeSuggestion {
//	
//	ASTRewriteBasedManipulator aManipulator = new ASTRewriteBasedManipulator();
//	
//	PreconditionChecker preChecker = new PreconditionChecker();
//
//	PostconditionChecker postChecker = new PostconditionChecker();
//	
//	public void suggestChange(Map<MethodModification, Set<MethodPair>> methodPairMap, 
//			ProjectResource prLeft, ProjectResource prRight){
//		Iterator<MethodModification> mIterator = methodPairMap.keySet().iterator();
//		while(mIterator.hasNext()){
//			MethodModification mm = mIterator.next();
//			Set<MethodPair> mps = methodPairMap.get(mm);
//			for(MethodPair mp : mps){							
//				//1. manipulate the original file
//				aManipulator.manipulate(prLeft, prRight, mp, mm);		
//			}
//		}
//	}
//}
