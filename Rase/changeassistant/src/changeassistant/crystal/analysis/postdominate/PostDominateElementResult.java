package changeassistant.crystal.analysis.postdominate;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import changeassistant.crystal.analysis.DominateLE;
import changeassistant.crystal.analysis.ElementResult;
import changeassistant.internal.ASTNodeIncludedCollector;
import changeassistant.peers.SourceCodeRange;
import changeassistant.peers.comparison.Node;
import edu.cmu.cs.crystal.cfg.eclipse.EclipseCFG;
import edu.cmu.cs.crystal.cfg.eclipse.EclipseCFGEdge;
import edu.cmu.cs.crystal.cfg.eclipse.EclipseCFGNode;
import edu.cmu.cs.crystal.simple.TupleLatticeElement;
import edu.cmu.cs.crystal.tac.TACFlowAnalysis;
import edu.cmu.cs.crystal.tac.eclipse.CompilationUnitTACs;
import edu.cmu.cs.crystal.tac.model.Variable;

public class PostDominateElementResult extends ElementResult {

	public static boolean UPSTREAM_CONTROL_DEPENDENCE = true;

	public static boolean DOWNSTREAM_CONTROL_DEPENDENCE = false;

	public static String ERROR = "(error)";
	public static String THROWS = "(throws)";

	public static int K = 1;

	private MethodDeclaration d;
	// private TACFlowAnalysis<SetLatticeElement<DominateLE>> analysis;
	// private EclipseCFG cfg;
	// private Map<ASTNode, EclipseCFGNode> map;
	// private Map<ASTNode, Node> astNodeMap;
	// private ASTElementSearcher searcher;
	private List<SourceCodeRange> astNodeRanges;
	private List<EclipseCFGNode> cfgNodes;
	private List<Set<SourceCodeRange>> dominatorsList;// given a node, all of
														// the nodes dominating
														// it
	private List<Set<SourceCodeRange>> dominateesList;// given a node, all of
														// the nodes dominated
														// by it

	private Map<Node, WeakReference<List<SourceCodeRange>>> scrIncludedCache = null;

	public List<Node> fUpstreamSeeds = new ArrayList<Node>();
	public List<Node> fDownstreamSeeds = new ArrayList<Node>();

	public PostDominateElementResult(
			MethodDeclaration d,
			TACFlowAnalysis<TupleLatticeElement<Variable, DominateLE>> analysis,
			CompilationUnitTACs tac) {
		this.d = d;
		this.scrIncludedCache = new HashMap<Node, WeakReference<List<SourceCodeRange>>>();
		analysis.getEndResults(d);
		Map<ASTNode, EclipseCFGNode> astTOcfg = new EclipseCFG(d).getNodeMap();
		astNodeRanges = new ArrayList<SourceCodeRange>();// to represent all ast
															// nodes
		cfgNodes = new ArrayList<EclipseCFGNode>(); // to represent all
													// corresponding cfg nodes
		ASTNode astNode = null;
		EclipseCFGNode cfgNode = null;
		for (Entry<ASTNode, EclipseCFGNode> entry : astTOcfg.entrySet()) {
			astNode = entry.getKey();
			cfgNode = entry.getValue();
			astNodeRanges.add(new SourceCodeRange(astNode.getStartPosition(),
					astNode.getLength()));
			cfgNodes.add(cfgNode);
		}
		dominatorsList = new ArrayList<Set<SourceCodeRange>>();
		d.accept(new ResultASTVisitor(analysis, dominatorsList, astNodeRanges));
		setDominatees();
		// this.searcher = new ASTElementSearcher(d);
	}

	private Set<SourceCodeRange> getDownstreamControlDependence(Node node) {
		Set<SourceCodeRange> controlDependingNodes = new HashSet<SourceCodeRange>(
				getDownstreamControlDependence2(node));
		return controlDependingNodes;
	}

	/**
	 * The function is similar to getDownstreamControlDependence(...) but only
	 * different in return type. The result may be redundant.
	 * 
	 * @param node
	 * @return
	 */
	public List<SourceCodeRange> getDownstreamControlDependence2(Node node) {
		List<SourceCodeRange> controlDependingNodes = new ArrayList<SourceCodeRange>();
		Set<ASTNode> astNodes = collectASTNodesIncluded(node);
		Set<SourceCodeRange> sourcePostDominators = new HashSet<SourceCodeRange>();
		Set<SourceCodeRange> sinkPostDominators = null;
		SourceCodeRange scr = null;
		Set<SourceCodeRange> dominators = null;
		int index = -1;
		for (ASTNode astNode : astNodes) {
			scr = new SourceCodeRange(astNode.getStartPosition(),
					astNode.getLength());
			index = astNodeRanges.indexOf(scr);
			if (index != -1) {
				dominators = dominatorsList.get(index);
				if (dominators != null) {
					sourcePostDominators.addAll(dominators);
				}
			}
		}
		if (sourcePostDominators.isEmpty())
			return controlDependingNodes;
		// 1. look for all sink nodes of current nodes
		Set<EclipseCFGEdge> cfgEdges;
		EclipseCFGNode sink, source;
		for (ASTNode astNode : astNodes) {
			index = astNodeRanges.indexOf(new SourceCodeRange(astNode
					.getStartPosition(), astNode.getLength()));
			if (index == -1)
				continue;
			source = cfgNodes.get(index);
			cfgEdges = source.getOutputs();
			// 2. for each sink node, find its post dominators,
			for (EclipseCFGEdge cfgEdge : cfgEdges) {
				sink = cfgEdge.getSink();
				index = cfgNodes.indexOf(sink);
				if (index == -1) {
					Set<EclipseCFGEdge> cfgEdges2 = sink.getOutputs();
					for (EclipseCFGEdge cfgEdge2 : cfgEdges2) {
						sink = cfgEdge2.getSink();
						index = cfgNodes.indexOf(sink);
						sinkPostDominators = getCopy(dominatorsList.get(index));
						if (sinkPostDominators == null) {
							controlDependingNodes.add(astNodeRanges.get(index));
						} else {
							sinkPostDominators.removeAll(sourcePostDominators);
							controlDependingNodes.addAll(sinkPostDominators);
						}
					}
					continue;
				}
				sinkPostDominators = getCopy(dominatorsList.get(index));
				if (sinkPostDominators == null) {
					controlDependingNodes.add(astNodeRanges.get(index));
				} else {
					// 3. see whether the source's post dominators do not
					// contain
					// the sink's post dominators
					sinkPostDominators.removeAll(sourcePostDominators);
					controlDependingNodes.addAll(sinkPostDominators);
				}
			}
		}
		return controlDependingNodes;
	}

	private Set<SourceCodeRange> getUpstreamControlDependence(Node node) {
		// 1. look for all source nodes post dominated by current node, while
		// their sink nodes are not
		// post dominated by the current node
		// System.out.print("")
		// Set<ASTNode> astNodes = getASTNodesPostDominating(node);
		Set<SourceCodeRange> controlDependingNodes = new HashSet<SourceCodeRange>(
				getUpstreamControlDependence2(node));
		return controlDependingNodes;
	}

	/**
	 * Especially for Throw Statement Specially designed for
	 * CloneReductionPDGCreation
	 * 
	 * @param node
	 * @return
	 */
	public Map<String, Node> getFlowSinks(Node node,
			Set<SourceCodeRange> includedRanges) {
		Map<String, Node> result = new HashMap<String, Node>();
		List<SourceCodeRange> exprRanges = node.getASTExpressions();
		SourceCodeRange nodeRange = node.getSourceCodeRange();
		SourceCodeRange exprRange = null;
		EclipseCFGNode sink = null;
		ASTNode sinkASTNode = null;
		Node sinkNode = null;
		int index = -1;
		SourceCodeRange sinkScr = null;
		Stack<SourceCodeRange> stack = new Stack<SourceCodeRange>();
		stack.addAll(exprRanges);
		if(exprRanges.isEmpty() && node.getNodeType() != ASTNode.BLOCK){
			stack.add(nodeRange);
		}
		Set<SourceCodeRange> processed = new HashSet<SourceCodeRange>();
		while (!stack.isEmpty()) {
			exprRange = stack.pop();
			if (!processed.add(exprRange))
				continue;
			index = astNodeRanges.indexOf(exprRange);
			if (index != -1) {
				EclipseCFGNode cNode = cfgNodes.get(index);
				for (EclipseCFGEdge cEdge : cNode.getOutputs()) {
					sink = cEdge.getSink();
					if (sink.getASTNode() != null) {
						sinkASTNode = sink.getASTNode();
						sinkNode = searchForRelevantNode(new SourceCodeRange(
								sinkASTNode.getStartPosition(),
								sinkASTNode.getLength()));
						if (sinkNode == null)
							continue;
						sinkScr = sinkNode.getSourceCodeRange();
						if (!nodeRange.isInside(sinkScr)) {
							result.put(cEdge.getLabel().getLabel(), sinkNode);
						} else {// assume sinkNode != null
							stack.add(sinkScr);
						}
					} else if (sink.getName().equals("(uber-return)")) {
						// sinkNode == null
						result.put(cEdge.getLabel().getLabel(), (Node)node.getRoot());
					}
				}
			}
		}
		return result;
	}

	/**
	 * This function is pretty similar to getUpstreamControlDependence(...) but
	 * only different in the return type List<SourceCodeRange> instead of
	 * Set<SourceCodeRange> In the list, there may be some data redundancy
	 * 
	 * @param node
	 * @return
	 */
	public List<SourceCodeRange> getUpstreamControlDependence2(Node node) {
		// 1. look for all source nodes post dominated by current node, while
		// their sink nodes are not
		// post dominated by the current node
		List<SourceCodeRange> scrs = new ArrayList<SourceCodeRange>();
		List<SourceCodeRange> astExpressions = new ArrayList<SourceCodeRange>(
				node.getASTExpressions());
		EclipseCFGNode sink, source;

		ASTNode sourceASTNode;
		Set<EclipseCFGEdge> cfgEdges;
		Set<EclipseCFGEdge> cfgEdges2 = null;
		SourceCodeRange sourceRange;
		SourceCodeRange sinkRange;
		Set<SourceCodeRange> knownRanges = null;
		if (node.getStrValue().equals("then:")
				|| node.getStrValue().equals("else:")) {
			assert node.getParent() != null;
			scrs.addAll(astExpressions);
			scrs.add(((Node) node.getParent()).getSourceCodeRange());
		} else {
			knownRanges = getASTNodesPostDominating(node);

			// 2. for each post dominated node A, find all its source nodes
			for (SourceCodeRange scr : knownRanges) {
				sink = cfgNodes.get(astNodeRanges.indexOf(scr));
				// for each dominated node, look for all input edges
				assert sink != null;
				cfgEdges = sink.getInputs();
				sinkRange = new SourceCodeRange(sink.getASTNode()
						.getStartPosition(), sink.getASTNode().getLength());
				// 3. for each source node, see whether it is also post
				// dominated by current node
				for (EclipseCFGEdge cfgEdge : cfgEdges) {
					source = cfgEdge.getSource();// find source for each input
													// edge
					sourceASTNode = source.getASTNode();
					if (sourceASTNode == null) {
						cfgEdges2 = source.getInputs();
						for (EclipseCFGEdge cfgEdge2 : cfgEdges2) {
							source = cfgEdge2.getSource();
							sourceASTNode = source.getASTNode();
							if (sourceASTNode == null)
								continue;
							sourceRange = new SourceCodeRange(
									sourceASTNode.getStartPosition(),
									sourceASTNode.getLength());
							if (!knownRanges.contains(sourceRange)
									|| (knownRanges.contains(sourceRange) && astExpressions
											.contains(sourceRange))) {
								scrs.add(sourceRange);
							}
						}
						continue;// it is possible that a cfgNode does not
						// correspond to an AST node
					}

					sourceRange = new SourceCodeRange(
							sourceASTNode.getStartPosition(),
							sourceASTNode.getLength());
					// 4. if a source node is not post dominated by current
					// node, it is control depended on by current node
					if (!knownRanges.contains(sourceRange)
							|| (knownRanges.contains(sourceRange) && astExpressions
									.contains(sourceRange))) {
						scrs.add(sourceRange);
						scrs.add(sinkRange);
					}
				}
			}
		}
		return scrs;
	}

	/**
	 * Can capture both upstream and downstream control dependence nodes
	 * 
	 * @param node
	 * @return
	 */
	public Set<Node> getNodeControlDependence(Node node) {
		// Map<ASTNode, Node> astNodeMap = NodeASTMapping.createASTNodeMap(d,
		// (Node)node.getRoot());
		this.fUpstreamSeeds.clear();
		this.fDownstreamSeeds.clear();
		Set<Node> resultSet = new HashSet<Node>();
		Set<Node> upstreamSeeds = new HashSet<Node>();
		Set<Node> downstreamSeeds = new HashSet<Node>();

		upstreamSeeds.add(node);
		downstreamSeeds.add(node);

		Set<Node> tempSet = new HashSet<Node>();
		Set<Node> tempUpstreamResult = new HashSet<Node>();
		Set<Node> tempDownstreamResult = new HashSet<Node>();
		for (int i = 0; i < K; i++) {
			if (UPSTREAM_CONTROL_DEPENDENCE) {
				tempUpstreamResult.clear();
				for (Node seed : upstreamSeeds) {
					System.out.print("");
					// tempSet =
					// NodeASTMapping.searchforRelevantNodes(astNodeMap,
					// new
					// ArrayList<SourceCodeRange>(getUpstreamControlDependence(seed)));
					tempSet = searchForRelevantNodes(getUpstreamControlDependence(seed));
					tempUpstreamResult.addAll(tempSet);
					resultSet.addAll(tempSet);
				}
				tempUpstreamResult.removeAll(fUpstreamSeeds);
				upstreamSeeds.clear();
				upstreamSeeds.addAll(tempUpstreamResult);
				fUpstreamSeeds.addAll(tempUpstreamResult);
			}
			if (DOWNSTREAM_CONTROL_DEPENDENCE) {
				try {
					tempDownstreamResult.clear();
					for (Node seed : downstreamSeeds) {
						// tempSet =
						// NodeASTMapping.searchforRelevantNodes(astNodeMap,
						// new
						// ArrayList<SourceCodeRange>(getDownstreamControlDependence(seed)));
						tempSet = searchForRelevantNodes(new ArrayList<SourceCodeRange>(
								getDownstreamControlDependence(seed)));
						tempDownstreamResult.addAll(tempSet);
						resultSet.addAll(tempSet);
					}
					tempDownstreamResult.removeAll(fDownstreamSeeds);
					downstreamSeeds.clear();
					downstreamSeeds.addAll(tempDownstreamResult);
					fDownstreamSeeds.addAll(tempDownstreamResult);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		fUpstreamSeeds.remove(node);
		fDownstreamSeeds.remove(node);
		resultSet.remove(node);
		return resultSet;
	}

	public Set<Node> getTransitiveControlDependence(Node node) {
		Set<Node> tmpSet = null;
		Set<Node> resultSet = new HashSet<Node>();
		Set<Node> upstreamResultSet = new HashSet<Node>();
		// Set<Node> downstreamResultSet = new HashSet<Node>();
		Set<Node> upstreamSeeds = new HashSet<Node>();
		// Set<Node> downstreamSeeds = new HashSet<Node>();

		upstreamSeeds.add(node);
		// downstreamSeeds.add(node);
		System.out.print("");
		while (!upstreamSeeds.isEmpty()) {
			tmpSet = new HashSet<Node>();
			for (Node tmpNode : upstreamSeeds) {
				tmpSet.addAll(searchForRelevantNodes(getUpstreamControlDependence(tmpNode)));
			}
			upstreamResultSet.addAll(upstreamSeeds);
			tmpSet.removeAll(upstreamResultSet);
			upstreamSeeds = tmpSet;
		}
		// while (!downstreamSeeds.isEmpty()) {
		// tmpSet = new HashSet<Node>();
		// for (Node tmpNode : downstreamSeeds) {
		// tmpSet.addAll(searchForRelevantNodes(new ArrayList<SourceCodeRange>(
		// getDownstreamControlDependence(tmpNode))));
		// }
		// downstreamResultSet.addAll(downstreamSeeds);
		// tmpSet.removeAll(downstreamResultSet);
		// downstreamSeeds = tmpSet;
		// }
		resultSet.addAll(upstreamResultSet);
		resultSet.remove(node);
		// resultSet.addAll(downstreamResultSet);
		return resultSet;
	}

	private Set<ASTNode> collectASTNodesIncluded(Node node) {
		Set<ASTNode> astNodesPostDominating = new HashSet<ASTNode>(
				collectASTNodesIncluded2(node));
		return astNodesPostDominating;
	}

	private List<ASTNode> collectASTNodesIncluded2(Node node) {
		ASTNodeIncludedCollector collector = new ASTNodeIncludedCollector(d);
		List<ASTNode> astNodesPostDominating = new ArrayList<ASTNode>();
		Set<ASTNode> tmpNodes = null;
		List<SourceCodeRange> asts = new ArrayList<SourceCodeRange>(
				node.getASTExpressions());
		if (node.getNodeType() == ASTNode.RETURN_STATEMENT) {
			asts.add(node.getSourceCodeRange());
		}
		for (SourceCodeRange astExpr : asts) {
			tmpNodes = collector.collectASTNodesWithin(astExpr);
			for (ASTNode tmpNode : tmpNodes) {
				if (!astNodesPostDominating.contains(tmpNode))
					astNodesPostDominating.add(tmpNode);
			}
		}
		return astNodesPostDominating;
	}

	private/* Set<ASTNode> */Set<SourceCodeRange> getASTNodesPostDominating(
			Node node) {
		// all possible AST nodes are covered in the astExpressions list
		Set<ASTNode> astNodesPostDominating = collectASTNodesIncluded(node);

		Set<SourceCodeRange> knownRanges = new HashSet<SourceCodeRange>();
		for (ASTNode astNode : astNodesPostDominating) {
			knownRanges.add(new SourceCodeRange(astNode.getStartPosition(),
					astNode.getLength()));
		}
		Set<SourceCodeRange> dominatees = new HashSet<SourceCodeRange>();
		int index = -1;
		for (SourceCodeRange scr : knownRanges) {
			index = astNodeRanges.indexOf(scr);
			if (index != -1)
				dominatees.addAll(dominateesList.get(index));
		}
		// ASTElementSearcher searcher = new ASTElementSearcher(d);
		// ASTNode tmp = null;
		// for (SourceCodeRange scr : dominatees) {
		// tmp = searcher.findElement(scr);
		// System.out.println(tmp);
		// astNodesPostDominating.add(tmp);
		// }
		// return astNodesPostDominating;
		return dominatees;
	}

	public Set<SourceCodeRange> getCopy(Set<SourceCodeRange> original) {
		if (original != null)
			return new HashSet<SourceCodeRange>(original);
		return null;
	}

	public void setDominatees() {// for a given node, find all nodes dominated
									// by it
		dominateesList = new ArrayList<Set<SourceCodeRange>>();
		for (int i = 0; i < astNodeRanges.size(); i++) {
			dominateesList.add(new HashSet<SourceCodeRange>());
		}
		Set<Integer> indexesForNullDominators = new HashSet<Integer>();
		for (int i = 0; i < dominatorsList.size(); i++) {
			Set<SourceCodeRange> dominators = dominatorsList.get(i);
			if (dominators == null) {
				indexesForNullDominators.add(i);
				continue;
			}
			SourceCodeRange dominatee = astNodeRanges.get(i);
			for (SourceCodeRange scr : dominators) {
				dominateesList.get(astNodeRanges.indexOf(scr)).add(dominatee);
			}
		}

		Set<SourceCodeRange> emptyDominatees = Collections.emptySet();
		for (Integer index : indexesForNullDominators) {
			dominateesList.set(index, emptyDominatees);
		}
	}
}

class ResultASTVisitor extends ASTVisitor {
	TACFlowAnalysis<TupleLatticeElement<Variable, DominateLE>> analysis;
	List<Set<SourceCodeRange>> dominatorsList;
	TupleLatticeElement<Variable, DominateLE> temp = null;
	List<SourceCodeRange> astNodeRanges = null;

	ResultASTVisitor(
			TACFlowAnalysis<TupleLatticeElement<Variable, DominateLE>> analysis,
			List<Set<SourceCodeRange>> dominatorsList,// pass by reference
			List<SourceCodeRange> astNodeRanges) {
		this.analysis = analysis;
		this.astNodeRanges = astNodeRanges;
		this.dominatorsList = dominatorsList;
		for (int i = 0; i < astNodeRanges.size(); i++) {
			dominatorsList.add(null);// to take up the space for further setting
										// operations
		}
	}

	@Override
	public void preVisit(ASTNode node) {
		if (node instanceof Javadoc)
			return;
		try {
			temp = analysis.getResultsBefore(node);
		} catch (Exception e) {
			System.out.println(e);
			// just ignore
		}
		SourceCodeRange scr = null;
		scr = new SourceCodeRange(node.getStartPosition(), node.getLength());
		int index = astNodeRanges.indexOf(scr);
		if (index != -1) {
			if (temp != null && temp.getElements() != null) {
				Set<SourceCodeRange> dominators = new HashSet<SourceCodeRange>();
				// There is a single element, so the for loop should be executed
				// only once
				for (DominateLE le : temp.getElements().values()) {
					dominators.addAll(le.ranges);
				}
				dominatorsList.set(index, dominators);
			} else {
				// still null;
			}
		}
	}
}

class ClassInstanceCreationVisitor extends ASTVisitor {
	String typeName = null;

	@Override
	public boolean visit(ClassInstanceCreation node) {
		typeName = node.getType().toString();
		return false;
	}

	public String getTypeName() {
		return typeName;
	}
}
