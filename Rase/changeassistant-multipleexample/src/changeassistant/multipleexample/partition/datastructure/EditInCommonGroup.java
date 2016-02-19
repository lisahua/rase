package changeassistant.multipleexample.partition.datastructure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import changeassistant.multipleexample.contextualize.ContextualizeHelper2;
import changeassistant.multipleexample.contextualize.ContextualizeHelper4;
import changeassistant.multipleexample.editfilter.CommonFilter;
import changeassistant.versions.comparison.MethodModification;

public class EditInCommonGroup {

	private List<MethodModification> mmList;

	private List<List<ChangeSummary>> chgSums;

	private List<List<String>> chgSumStrs;

	private List<List<String>> absChgSumStrs;

	private List<EditInCommonCluster> clusters;

	private List<EditInCommonCluster> applicableClusters;

	public EditInCommonGroup(List<MethodModification> mmList,
			List<List<ChangeSummary>> chgSums, List<List<String>> chgSumStrs,
			List<List<String>> absChgSumStrs) {
		this.mmList = mmList;
		this.chgSums = chgSums;
		this.chgSumStrs = chgSumStrs;
		this.absChgSumStrs = absChgSumStrs;
	}

	public void contextualizeClusters() {
		List<EditInCommonCluster> newClusters = new ArrayList<EditInCommonCluster>();
		List<AbstractCluster> incomings = null;
		EditInCommonCluster eClus = null;
		for (EditInCommonCluster cluster : clusters) {
			// to remove larger clusters which are not applicable
			AbstractCluster subCluster = null;
			if (!cluster.getApplicable()) {
				Queue<EditInCommonCluster> queue = new LinkedList<EditInCommonCluster>();
				queue.add(cluster);
				EditInCommonCluster tmpCluster;
				// trace down to the largest applicable cluster, put it into
				// newClusters
				while (!queue.isEmpty()) {
					tmpCluster = queue.remove();
					incomings = tmpCluster.getIncomings();
					for (AbstractCluster aClus : incomings) {
						aClus.outgoings.remove(tmpCluster);
						if (aClus instanceof EditInCommonCluster) {
							eClus = (EditInCommonCluster) aClus;
							if (eClus.getApplicable()
									&& eClus.outgoings.size() == 0) {
								newClusters.add(eClus);
							} else if (!eClus.getApplicable()) {
								queue.add(eClus);
							}
						}
					}
				}
			} else {
				while (cluster.getIncomings().size() == 1) {
					subCluster = cluster.getIncomings().get(0);
					if (subCluster instanceof EditInCommonCluster) {
						eClus = (EditInCommonCluster) subCluster;
						if (eClus.getInstances().size() == cluster
								.getInstances().size()) {
							cluster = (EditInCommonCluster) subCluster;
							cluster.outgoings = null;
						} else {
							break;
						}
					} else {
						cluster = null;
						break;
					}
				}
				if (cluster != null)
					newClusters.add(cluster);
			}
		}
		clusters = newClusters;
		// parse context using CCFinder
		ContextualizeHelper4 cHelper4 = new ContextualizeHelper4(this,
				clusters, mmList);
		cHelper4.parseCommonContext();
		ContextualizeHelper2 cHelper2 = new ContextualizeHelper2(clusters,
				mmList);
		cHelper2.refineCommonContext();
	}

	public List<AbstractCluster> createBaseClusters() {
		List<AbstractCluster> clusters = new ArrayList<AbstractCluster>();
		for (int i = 0; i < mmList.size(); i++) {
			clusters.add(new BaseCluster(i, mmList.get(i), chgSums.get(i),
					chgSumStrs.get(i), absChgSumStrs.get(i)));
		}
		return clusters;
	}
	
	public boolean createClusterLatticeWithoutContext(){
		AbstractCluster cluster1 = null, cluster2 = null;
		EditInCommonCluster cluster3 = null;
		List<AbstractCluster> clusters = createBaseClusters();
		List<EditInCommonCluster> newClusters = new ArrayList<EditInCommonCluster>();
		List<EditInCommonCluster> conflictClusters = new ArrayList<EditInCommonCluster>();
		boolean flag = false;
		int clusterIndex = clusters.size();
		while(true){
			for (int i = 0; i < clusters.size() - 1; i++) {
				cluster1 = clusters.get(i);
				if (inProperCluster(cluster1))
					continue;
				for (int j = i + 1; j < clusters.size(); j++) {
					cluster2 = clusters.get(j);
					System.out.println("Intersect " + cluster1.getIndex()
							+ " with " + cluster2.getIndex());
					cluster3 = (EditInCommonCluster) cluster1.intersectWithoutContext(cluster2, this);
					if (cluster3 != null) {
						removeConflict(cluster1, cluster2, conflictClusters);
						flag = true;
						for (EditInCommonCluster clus : newClusters) {
							if (clus.equivalentTo(cluster3)) {
								clus.merge(cluster3);
								flag = false;
								break;
							}
						}
						if (flag) {// this is a new cluster
							cluster3.setIndex(clusterIndex++);
							newClusters.add(cluster3);
						}
					}
				}
			}

			if (newClusters.size() != 0) {
				clusters.clear();
				clusters.addAll(newClusters);
				clusters.addAll(conflictClusters);

				conflictClusters.addAll(newClusters);
				newClusters.clear();
			} else
				break;
		}
		if (clusters.size() == 0)
			return false;
		this.clusters = new ArrayList<EditInCommonCluster>();
		flag = true;
		// if there is only base clusters, none of the mm are grouped together
		for (AbstractCluster cluster : clusters) {
			if (cluster instanceof BaseCluster) {
				flag = false;
				break;
			}
			this.clusters.add((EditInCommonCluster) cluster);
		}
		if (!flag)
			this.clusters = null;
		return flag;
	}

	public boolean createClusterLattice() {
		AbstractCluster cluster1 = null, cluster2 = null;
		EditInCommonCluster cluster3 = null;
		List<AbstractCluster> clusters = createBaseClusters();
		List<EditInCommonCluster> newClusters = new ArrayList<EditInCommonCluster>();
		List<EditInCommonCluster> conflictClusters = new ArrayList<EditInCommonCluster>();

		boolean flag = false;

		int clusterIndex = clusters.size();
		while (true) {// clusterIndex is unique for each cluster
			// clusterIndex = 0;
			// System.out.print("");
			for (int i = 0; i < clusters.size() - 1; i++) {
				cluster1 = clusters.get(i);
				if (inProperCluster(cluster1))
					continue;
				for (int j = i + 1; j < clusters.size(); j++) {
					cluster2 = clusters.get(j);
					System.out.println("Intersect " + cluster1.getIndex()
							+ " with " + cluster2.getIndex());
//					System.out.print("");
					cluster3 = (EditInCommonCluster) cluster1.intersect(
							cluster2, this);
					if (cluster3 != null) {
						removeConflict(cluster1, cluster2, conflictClusters);
						flag = true;
						for (EditInCommonCluster clus : newClusters) {
							System.out.print("");
							if (clus.equivalentTo(cluster3)) {
								clus.merge(cluster3);
								// if(clus.merge(cluster3)){
								flag = false;
								break;
								// }
							}
						}
						if (flag) {// this is a new cluster
							cluster3.setIndex(clusterIndex++);
							newClusters.add(cluster3);
						}
					}
				}
			}

			if (newClusters.size() != 0) {
				clusters.clear();
				clusters.addAll(newClusters);
				clusters.addAll(conflictClusters);

				conflictClusters.addAll(newClusters);
				newClusters.clear();
			} else
				break;
		}

		if (clusters.size() == 0)
			return false;
		this.clusters = new ArrayList<EditInCommonCluster>();
		flag = true;
		// if there is only base clusters, none of the mm are grouped together
		for (AbstractCluster cluster : clusters) {
			if (cluster instanceof BaseCluster) {
				flag = false;
				break;
			}
			this.clusters.add((EditInCommonCluster) cluster);
		}
		if (!flag)
			this.clusters = null;
		return flag;
	}

	public List<EditInCommonCluster> getClusters() {
		return clusters;
	}

	public Iterator<EditInCommonCluster> getIterator() {
		if (applicableClusters == null)
			return null;
		return applicableClusters.iterator();
	}

	/**
	 * 
	 * @return maybe null when no applicable cluster is found
	 */
	public EditInCommonCluster getLargestCluster(){
		EditInCommonCluster result = null;
		int size = -1;
		int tmpSize = -1;
		for(EditInCommonCluster cluster : clusters){
			if(cluster.getApplicable()){
				tmpSize = cluster.getInstances().size(); 
				if(result == null || size < tmpSize){
					result = cluster;
					size = tmpSize;
				}
			}
		}
		return result;
	}
	
	public List<MethodModification> getMMList() {
		return mmList;
	}

	/**
	 * This method must be invoked before calling iterator() methods on the
	 * group
	 */
	public void initializeIterator() {
		applicableClusters = new ArrayList<EditInCommonCluster>();
		Queue<EditInCommonCluster> queue = new LinkedList<EditInCommonCluster>();
		Set<List<ChangeSummary>> chgSumsProcessed = new HashSet<List<ChangeSummary>>();
		List<AbstractCluster> clusters = null;
		EditInCommonCluster eClus = null;
		// System.out.print("");
		queue.addAll(this.clusters);
		while (!queue.isEmpty()) {
			eClus = queue.remove();
			if (eClus.getApplicable() && eClus.checkForestOrder()) {
				if (CommonFilter.hasValidEdits(eClus)
						&& CommonFilter.hasValidContexts(eClus))
					if (chgSumsProcessed.add(eClus.getConChgSum())) {
						applicableClusters.add(eClus);
						break;
					}
			}
			clusters = eClus.getIncomings();
			for (AbstractCluster clus : clusters) {
				if (clus instanceof EditInCommonCluster) {
					queue.add((EditInCommonCluster) clus);
				}
			}
		}
	}

	/**
	 * To judge whether the current cluster has already been put into a proper
	 * cluster. Sign: the common size of the larger cluster is the same as
	 * current cluster.
	 * 
	 * @param cluster
	 * @return
	 */
	private boolean inProperCluster(AbstractCluster cluster) {
		List<EditInCommonCluster> largerClusters = cluster.getOutgoings();
		if (largerClusters != null) {// size of the edits in current cluster
			int size = cluster.getChgSumStr().size();
			for (EditInCommonCluster clus : largerClusters) {
				if (clus.getChgSumStr().size() == size)
					return true;
			}
		}
		return false;
	}

	private void removeConflict(AbstractCluster cluster1,
			AbstractCluster cluster2, List<EditInCommonCluster> conflictClusters) {
		if (cluster1 instanceof BaseCluster || cluster2 instanceof BaseCluster)
			return;
		EditInCommonCluster eClus1 = (EditInCommonCluster) cluster1;
		EditInCommonCluster eClus2 = (EditInCommonCluster) cluster2;

		conflictClusters.remove(eClus1);
		conflictClusters.remove(eClus2);
	}

	public void setApplicableClusters(List<EditInCommonCluster> clusters) {
		this.applicableClusters = clusters;
	}

	public void setEditInCommonClusters(List<EditInCommonCluster> clusters) {
		this.clusters = clusters;
	}

	public int size() {
		return mmList.size();
	}
}
