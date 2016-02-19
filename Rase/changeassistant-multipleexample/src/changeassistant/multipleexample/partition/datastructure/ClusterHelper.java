package changeassistant.multipleexample.partition.datastructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class ClusterHelper {
	
	private static List<EditInCommonCluster> findLevel_1_clusters(Queue<EditInCommonCluster> queue){
		List<EditInCommonCluster> level_1_clusters = new ArrayList<EditInCommonCluster>();
		boolean allBase = false;
		Set<EditInCommonCluster> knownClusters = new HashSet<EditInCommonCluster>();
		EditInCommonCluster eClus = null;
		while(!queue.isEmpty()){
			eClus = queue.remove();
			if(knownClusters.contains(eClus))
				continue;
			knownClusters.add(eClus);//to avoid process the same cluster twice
			allBase = true; // assume all of the incomings are BaseCluster
			for(AbstractCluster income : eClus.getIncomings()){
				if(income instanceof EditInCommonCluster){
					allBase = false;
					queue.add((EditInCommonCluster)income);
				}
			}
			if(allBase)
				level_1_clusters.add(eClus);//start from these level_1_clusters in the bottom-up manner
		}
		return level_1_clusters;
	}
	
	public static Map<Integer, BaseCluster> getBaseClusters(List<EditInCommonCluster> level_1_clusters){
		Set<AbstractCluster> result = new HashSet<AbstractCluster>();
		for(EditInCommonCluster cluster : level_1_clusters){
			result.addAll(cluster.getIncomings());
		}
		Map<Integer, BaseCluster> ret = new HashMap<Integer, BaseCluster>();
		for(AbstractCluster cluster : result){
			ret.put(cluster.getIndex(), (BaseCluster)cluster);
		}
		return ret;
	}
	
	public static List<EditInCommonCluster> getLevel_1_clusters(EditInCommonCluster cluster){
		Queue<EditInCommonCluster> queue = new LinkedList<EditInCommonCluster>();
		queue.add(cluster);
		return findLevel_1_clusters(queue);
	}
	
	/**
	 * 
	 * @param clusters pass-in parameter
	 * @param level_1_clusters in-and-out parameter
	 * @param baseClusters	in-and-out parameter
	 */
	public static List<EditInCommonCluster> getLevel_1_clusters(List<EditInCommonCluster> clusters){
		Queue<EditInCommonCluster> queue = new LinkedList<EditInCommonCluster>();
		queue.addAll(clusters);
		return findLevel_1_clusters(queue);
	}
}
