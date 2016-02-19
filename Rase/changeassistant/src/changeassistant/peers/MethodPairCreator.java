//package changeassistant.peers;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import changeassistant.classhierarchy.ProjectResource;
//import changeassistant.versions.comparison.MethodModification;
//
//public class MethodPairCreator {
//	//this is the map between a methodModification and multiple method_pairs' indexes in the list
//	private Map<MethodModification, Set<MethodPair>> methodPairMap;
//	
//	private PeerFinder peerFinder;
//
//	private MethodPairComparator mpComparator;
//	
//	public Map<MethodModification, Set<MethodPair>> create(List<MethodModification> methodModifications,
//			List<MethodPair> methodPairs, ProjectResource prLeft, ProjectResource prRight){
//		methodPairMap = new HashMap<MethodModification, Set<MethodPair>>();
//		mpComparator = new MethodPairComparator();
//		peerFinder = new PeerFinder(prLeft);
//		for(MethodModification mm : methodModifications){
//			//for each method modification mm
//			//1. look for potential peers within the original project
//			try{
//			Set<PeerMethodADT> potentialMethodPeers = peerFinder.findPotentialPeers(mm.originalMethod);
//			Set<MethodPair> mps = mpComparator.compare(new PeerMethodADT(mm.originalMethod), potentialMethodPeers, prLeft);
//			if(mps.size() > 0){
//				methodPairMap.put(mm, mps);
//			}
//			}catch(Exception e){
//				e.printStackTrace();
//			}
//		}
//		return methodPairMap;
//	}
//}
