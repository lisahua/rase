package changeassistant.clonedetection.ccfinder;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

import changeassistant.clonereduction.manipulate.NodeEquivalenceChecker;
import changeassistant.multipleexample.ccfinder.RunCCFinder;
import changeassistant.multipleexample.datastructure.SimpleASTNodesListForMethods;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.util.PathUtil;
import changeassistant.peers.comparison.Node;

public class CCFinderHelper {
	RunCCFinder runner = null;
	public CCFinderHelper(){
		runner = new RunCCFinder();
	}

	public void findClones(List<Node> nodes/*, NodeEquivalenceChecker neChecker*/){
		List<String> pathList = new ArrayList<String>();
//		SimpleASTNodesListForMethods nodesListForMethods = neChecker.getSimpleASTNodesListForMethods();
		for(int i = 0; i < nodes.size(); i++){
			pathList.add(createTmpFile(nodes.get(i), /*nodesListForMethods.get(i),*/ "tmp" + Integer.toString(i) + ".java"));
		}
		String path0 = pathList.get(0);
		String path = null;
		List<Hashtable<String, StringBuilder>> pairLists = new ArrayList<Hashtable<String, StringBuilder>>();
		for(int i = 1; i < pathList.size(); i++){
			path = pathList.get(i);
			runner.runCcfinder(path0, path);
			pairLists.add(runner.getNewPairList());
		}
	}
	
	private String createTmpFile(Node n, String fileName){
		String path = PathUtil.createPath(fileName);
		StringBuffer buffer = new StringBuffer();
		buffer.append(n.getMethodDeclaration());		
//		Enumeration<Node> nEnum = n.preorderEnumeration();
//		String tmp = null;
//		Node nTmp = null;
//		while(nEnum.hasMoreElements()){
//			nTmp = nEnum.nextElement();
//			if(nTmp.getNodeType() == ASTNode.METHOD_DECLARATION)
//				continue;
//			tmp = nTmp.getStrValue();
//			buffer.append(tmp.trim()).append("\n");
//		}
		FileWriter fstream = null;
		try {
			fstream = new FileWriter(path);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(buffer.toString());
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return path;
	}
}
