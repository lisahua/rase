package changeassistant.util;

import java.util.Enumeration;

import changeassistant.change.group.model.MatchingInfo;
import changeassistant.change.group.model.SubTreeModel;
import changeassistant.peers.SubTreeModelPair;

public class DebugPrinter {

	public static void printPath(MatchingInfo matchingInfo){
		//match subTreeModel with the collector
		SubTreeModel left = null;
		Enumeration<SubTreeModel> pEnumeration = null;
		SubTreeModel pathNode = null;
		int counter = 0;
		for(SubTreeModelPair singlePair : matchingInfo.getMatchedList()){
			left = singlePair.getRight();
			System.out.print(counter++ + "::" + left.getMatchingIndex());
			pEnumeration = left.pathFromAncestorEnumeration(left.getRoot());
			while(pEnumeration.hasMoreElements()){
				pathNode = pEnumeration.nextElement();
				System.out.print("->" + pathNode);
			}
			System.out.println();
		}
	}
	public static void printPath(SubTreeModel subTreeModel){
		Enumeration<SubTreeModel> sEnumeration = subTreeModel.breadthFirstEnumeration();
		SubTreeModel sNode = null;
		while(sEnumeration.hasMoreElements()){
			sNode = sEnumeration.nextElement();
			System.out.println(sNode.getStrValue() + "--matching index :" + sNode.getMatchingIndex());
		}
	}
}
