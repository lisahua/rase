package changeassistant.peers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import changeassistant.change.group.model.MatchingInfo;
import changeassistant.change.group.model.SubTreeModel;
import changeassistant.changesuggestion.expression.representation.AbstractExpressionRepresentationGenerator;
import changeassistant.classhierarchy.ClassContext;
import changeassistant.classhierarchy.PackageResource;
import changeassistant.classhierarchy.ProjectResource;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.comparison.ChangedMethodADT;

public class PeerFinder2 {

	public static int THRESHOLD = 7; // number of instances supporting

	private ProjectResource pr;

	private SubTreeModelMatcher matcher;

	public static boolean abstractVariable = true;
	public static boolean abstractMethod = true;
	public static boolean abstractType = true;

	private Map<SubTreeModel, Set<ChangedMethodADT>> map;
	private Map<SubTreeModel, Set<ChangedMethodADT>> peers;

	public PeerFinder2() {
		this.matcher = new SubTreeModelMatcher();
	}

	public PeerFinder2(ProjectResource projectResource,
			Map<SubTreeModel, Set<ChangedMethodADT>> map) {
		this.pr = projectResource;
		this.matcher = new SubTreeModelMatcher();
		this.map = map;
		this.peers = new HashMap<SubTreeModel, Set<ChangedMethodADT>>();
	}

	public void findPeers() {
		Iterator<ClassContext> ccIter = pr.classContextIterator();
		ClassContext cc = null;

		while (ccIter.hasNext()) {
			cc = ccIter.next();
			lookforPeers(cc, peers);
		}

		Iterator<PackageResource> prIter = pr.packageResourceIterator();
		PackageResource pr;
		while (prIter.hasNext()) {
			pr = prIter.next();
			ccIter = pr.classContextIterator();
			while (ccIter.hasNext()) {
				cc = ccIter.next();
				lookforPeers(cc, peers);
			}
		}
	}

	public Map<SubTreeModel, Set<ChangedMethodADT>> getMethodPeers() {
		return this.peers;
	}

	public MatchingInfo containSubTree(final SubTreeModel candidate,
			final SubTreeModel subTree) {
		matcher.initialize();
		MatchingInfo matchingInfo = this.matcher.match(
				(SubTreeModel) candidate.deepCopy(),
				(SubTreeModel) subTree.deepCopy());
		matcher.clear();
		candidate.clearMatched(true);// clear the candidate after using it
		subTree.clearMatched(true); // clear the subtree after using it
		return matchingInfo;
	}

	// public MatchingInfo containSubTreeExactInner(final SubTreeModel
	// candidate,
	// final SubTreeModel subTree){
	// matcher.initialize();
	// MatchingInfo matchingInfo = this.matcher.matchExactInner(
	// (SubTreeModel)candidate.deepCopy(),
	// (SubTreeModel)subTree.deepCopy());
	// matcher.clear();
	// return matchingInfo;
	// }

	private void lookforPeers(ClassContext cc,
			Map<SubTreeModel, Set<ChangedMethodADT>> peers) {
		Set<SubTreeModel> subTrees = map.keySet();
		Set<SubTreeModel> unnecessarySubTrees = new HashSet<SubTreeModel>(); // to
																				// record
																				// subTrees
																				// which
																				// are
																				// too
																				// common
																				// to
																				// use
		Set<String> methods = cc.methodMap.keySet();
		Node methodNode = null;
		ChangedMethodADT cADT = null;
		Set<ChangedMethodADT> set = null;
		SubTreeModel candidate1 = null;
		for (String methodSignature : methods) {
			methodNode = cc.getMethodNode(methodSignature);
			candidate1 = new SubTreeModel(methodNode, methodNode, true,
					new AbstractExpressionRepresentationGenerator());
			// if(!methodNode.getStrValue().contains("compareInputChanged(ICompareInput"))
			// continue;
			unnecessarySubTrees.clear();
			for (SubTreeModel subTree1 : subTrees) {// each time, only use the
													// copy of the compared
													// subTree--assume all sub
													// trees are clean before
													// using it
				if (containSubTree(candidate1, subTree1) != null) {
					cADT = new ChangedMethodADT(cc.name, methodSignature,
							cc.methodMap.get(methodSignature));
					// if(map.get(subTree).contains(cADT)){
					// //do nothing
					// }else{
					set = peers.get(subTree1);// however, subTree1 is used to be
												// indexed in hashMap
					if (set == null) {
						set = new HashSet<ChangedMethodADT>();
						set.add(cADT);
						peers.put(subTree1, set);
					} else {
						if (set.size() == THRESHOLD) {
							// map.remove(subTree1);
							unnecessarySubTrees.add(subTree1);
							peers.remove(subTree1);// the subTree is so common
													// that there is no need to
													// generalize the changes
													// for one of its instances
						} else {
							set.add(cADT);
						}
					}
					if (peers.get(subTree1) != null) {
						System.out.println("The number of peers found is "
								+ peers.get(subTree1).size()
								+ ", while the number of peers known is "
								+ map.get(subTree1).size());
					}
					// }
				}
			}
			if (!unnecessarySubTrees.isEmpty()) {
				for (SubTreeModel temp : unnecessarySubTrees) {
					map.remove(temp);
				}
			}
		}
	}

	public static void setAbstractMethod(boolean v) {
		abstractMethod = v;
	}

	public static void setAbstractType(boolean v) {
		abstractType = v;
	}

	public static void setAbstractVariable(boolean v) {
		abstractVariable = v;
	}

	public static void setAbstractDefault() {
		abstractMethod = abstractType = abstractVariable = true;
	}
}
