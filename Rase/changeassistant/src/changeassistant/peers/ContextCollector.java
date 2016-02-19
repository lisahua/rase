package changeassistant.peers;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import changeassistant.peers.comparison.Node;
import changeassistant.versions.treematching.MatchFactory;
import changeassistant.versions.treematching.NodePair;

public class ContextCollector {

//	static double lThres = 0.6;
	
//	static double nThres = 0.4;
	
	public static boolean isSimilarMethodName(String methodName1, String methodName2){
		double lThres = 0.1;
		boolean flag = false;
		Set<String> tokens1 = tokenize(methodName1);
		Set<String> tokens2 = tokenize(methodName2);
		Set<String> tokens3 = new HashSet<String>(tokens1);
		tokens3.retainAll(tokens2);
		double sim = tokens3.size() * 2.0/(tokens1.size() + tokens2.size());	
		if(sim > lThres){
			flag = true;
		}
		return flag;
	}
	
	public static boolean isSimilarMethodStructure(Node node1, Node node2){
		double nThres = 0.1;
		boolean flag = false;
		int matchCounter = 0;
		Node node1Copy = (Node)node1.deepCopy(),
			 node2Copy = (Node)node2.deepCopy();
		Set<Node> cachedLeftNodes = new HashSet<Node>(),
				  cachedRightNodes = new HashSet<Node>();
		Set<NodePair> matches = new HashSet<NodePair>();
		MatchFactory.getMatcher(matches).match(node1Copy, node2Copy);
		for(NodePair match : matches){
			if(cachedLeftNodes.contains(match.getLeft()) ||
					cachedRightNodes.contains(match.getRight())){
				//do nothing
			}else{
				matchCounter++;
				cachedLeftNodes.add(match.getLeft());
				cachedRightNodes.add(match.getRight());
			}
		}
		double sim = matchCounter * 2.0/(node1.countNodes() + node2.countNodes());
		if(sim >= nThres){
			flag = true;
		}
		return flag;
	}
	
	private static Set<String> tokenize(String name){
		StringTokenizer st = new StringTokenizer(name, "_");
		Set<String> set = new HashSet<String>();
		Set<String> result = new HashSet<String>();
		while(st.hasMoreElements()){
			set.add((String)st.nextElement());
		}
		char ch;
		int startPosition = 0;
		for(String str : set){
			startPosition = 0;
			for(int i = 0; i < str.length(); i++){
				ch = name.charAt(i);
				if(ch >= 'A' && ch <= 'Z'){
					result.add(str.substring(startPosition, i));
					startPosition = i;
				}
			}
			result.add(str.substring(startPosition, str.length()));
		}
		return result;
	}
}
