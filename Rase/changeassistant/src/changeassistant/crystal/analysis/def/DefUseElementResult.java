package changeassistant.crystal.analysis.def;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import changeassistant.crystal.FieldVariable;
import changeassistant.crystal.analysis.ElementResult;
import changeassistant.internal.ASTNodeFinder;
import changeassistant.internal.ASTNodeIncludedCollector;
import changeassistant.peers.SourceCodeRange;
import changeassistant.peers.comparison.Node;
import edu.cmu.cs.crystal.cfg.eclipse.EclipseCFG;
import edu.cmu.cs.crystal.simple.TupleLatticeElement;
import edu.cmu.cs.crystal.tac.TACFlowAnalysis;
import edu.cmu.cs.crystal.tac.eclipse.CompilationUnitTACs;
import edu.cmu.cs.crystal.tac.eclipse.EclipseInstructionSequence;
import edu.cmu.cs.crystal.tac.eclipse.EclipseTAC;
import edu.cmu.cs.crystal.tac.model.SourceVariable;
import edu.cmu.cs.crystal.tac.model.TACInstruction;
import edu.cmu.cs.crystal.tac.model.Variable;
import edu.cmu.cs.crystal.util.Utilities;

/**
 * This class does not only contain analysis result of DefUseAnalysis, but also
 * the MethodDeclaration used to get the analysis result
 * 
 * @author ibm
 * 
 */
public class DefUseElementResult extends ElementResult {

	public static boolean UPSTREAM_DATA_DEPENDENCE = true;

	public static boolean DOWNSTREAM_DATA_DEPENDENCE = false;

	public static boolean UPSTREAM_DEF_DEF = false;

	public static boolean DOWNSTREAM_DEF_DEF = false;

	public static int DEF_USE_ITERATION = 1;

	public static int DEF_DEF_ITERATION = 1;

	private ASTNodeIncludedCollector collector;
	private MethodDeclaration d;
	private TACFlowAnalysis<TupleLatticeElement<Integer, DefUseLE>> analysis;
	private CompilationUnitTACs cTac;
	private List<ASTNode> astNodes;
	private List<SourceCodeRange> astNodeRanges;

	private List<Variable> vars;
	private List<List<DefUse>> defuseLists;
	private List<Set<SourceCodeRange>> defRangesList;

	private Map<SourceCodeRange, List<TupleLatticeElement<Integer, DefUseLE>>> resultsAfterRangeCache;

	private Map<Node, WeakReference<Map<Variable, Set<SourceCodeRange>>>> defFactCache;
	// private Map<Node, WeakReference<Map<Variable, Set<SourceCodeRange>>>>
	// useFactCache;
	private Map<SourceCodeRange, WeakReference<List<TupleLatticeElement<Integer, DefUseLE>>>> resultsAfterCache;

	private EclipseTAC tac;

	public Set<Node> fUpstreamSeeds = new HashSet<Node>();
	public Set<Node> fDownstreamSeeds = new HashSet<Node>();

	public Set<Node> fUpstreamSeeds2 = new HashSet<Node>();// the upstream nodes
															// whose defs are
															// killed by current
															// def
	public Set<Node> fDownstreamSeeds2 = new HashSet<Node>();// the downstream
																// nodes whose
																// defs kill the
																// current def
	public Map<String, FieldVariable> fieldMap = null;

	public DefUseElementResult(MethodDeclaration d,
			TACFlowAnalysis<TupleLatticeElement<Integer, DefUseLE>> analysis,
			CompilationUnitTACs tac, Map<String, FieldVariable> fieldMap,
			List<Variable> vars) {
		this.d = d;
		this.analysis = analysis;
		this.analysis.getEndResults(d);
		this.cTac = tac;
		this.tac = tac.getMethodTAC(d);

		this.astNodes = new ArrayList<ASTNode>();
		this.astNodeRanges = new ArrayList<SourceCodeRange>();
		// EclipseCFG(d).getNodeMap() may return some FieldDeclaration which is
		// not inside the method boundary
		Set<ASTNode> astNodeSet = new EclipseCFG(d).getNodeMap().keySet();
		SourceCodeRange mRange = new SourceCodeRange(d.getStartPosition(),
				d.getLength());
		SourceCodeRange tmpRange = null;
		for (ASTNode tmpNode : astNodeSet) {
			tmpRange = new SourceCodeRange(tmpNode.getStartPosition(),
					tmpNode.getLength());
			if (tmpRange.isInside(mRange)) {
				astNodes.add(tmpNode);
				astNodeRanges.add(tmpRange);
			}
		}
		this.collector = new ASTNodeIncludedCollector(d);
		this.defFactCache = new HashMap<Node, WeakReference<Map<Variable, Set<SourceCodeRange>>>>();
		this.resultsAfterCache = new HashMap<SourceCodeRange, WeakReference<List<TupleLatticeElement<Integer, DefUseLE>>>>();
		this.fieldMap = fieldMap;
		this.vars = vars;
		setDefUses();
	}

	public Map<String, FieldVariable> getFieldMap() {
		return fieldMap;
	}

	private ASTNode findElement(SourceCodeRange scr) {
		int index = astNodeRanges.indexOf(scr);
		if (index == -1)
			return null;
		return astNodes.get(index);
	}

	/**
	 * For a given def, find all uses
	 * 
	 * @param node
	 * @return
	 */
	public Set<SourceCodeRange> getDownstreamDataDependence(Node node) {
		Set<SourceCodeRange> resultSet = new HashSet<SourceCodeRange>();
		Variable v = null;
		Set<SourceCodeRange> knownDefs = null;
		// 1. get all defs in the node
		Map<Variable, Set<SourceCodeRange>> defFact = getDefFact(node);
		for (Entry<Variable, Set<SourceCodeRange>> entry : defFact.entrySet()) {
			// 2. for each defined var, find all its defs
			v = entry.getKey();
			knownDefs = entry.getValue();
			for (DefUse du : defuseLists.get(vars.indexOf(v))) {
				// 3. only focus on the defs concerned
				if (knownDefs.contains(du.getDef())) {
					resultSet.addAll(du.getUses());
				}
			}
		}
		// resultSet = filter(resultSet);
		return resultSet;
	}

	public List<SourceCodeRange> getRangesIncluded(Node seed) {
		return seed.getASTExpressions();
	}

	/**
	 * for a given use, find its upstream defs
	 * 
	 * @param node
	 * @return
	 */
	public Set<SourceCodeRange> getUpstreamDataDependence(Node node) {
		Map<Variable, Set<SourceCodeRange>> map = getUseFact(node);
		Set<SourceCodeRange> resultSet = new HashSet<SourceCodeRange>();
		for (Set<SourceCodeRange> scrs : map.values()) {
			resultSet.addAll(scrs);
		}
		// resultSet = filter(resultSet);
		return resultSet;
	}

	public Set<Node> getDefSinkNodes(Node node) {
		return searchForRelevantDataNodes(getDownstreamDataDependence(node));
	}

	public Set<Node> getDefSourceNodes(Node node) {
		return searchForRelevantDataNodes(getUpstreamDataDependence(node));
	}

	public Set<Node> searchForRelevantDataNodes(
			Set<SourceCodeRange> astNodeRanges) {
		Set<Node> set = searchForRelevantNodes2(new ArrayList<SourceCodeRange>(
				astNodeRanges));
		Set<Node> result = new HashSet<Node>();
		for (Node n : set) {
			if (n.getASTExpressions().size() != 0) {
				result.add(n);
			}
		}
		return result;
	}

	/**
	 * To find nodes killing the defs in the node
	 * 
	 * @param node
	 * @return
	 */
	private Set<SourceCodeRange> getDownstreamDef(Node node) {
		Set<SourceCodeRange> resultSet = new HashSet<SourceCodeRange>();
		// 1. get all variables defined in this node
		Map<Variable, Set<SourceCodeRange>> knownDefs = getDefFact(node);
		if (knownDefs.isEmpty())
			return resultSet;// this is not a def
		Set<SourceCodeRange> tmpRanges = null;
		Set<SourceCodeRange> defRanges = null;
		Set<SourceCodeRange> beforeDefs = null;
		TupleLatticeElement<Integer, DefUseLE> beforeValue = null;
		ASTNode astNode = null;
		Variable v = null;

		for (Entry<Variable, Set<SourceCodeRange>> entry : knownDefs.entrySet()) {
			v = entry.getKey();
			// 2. find all nodes defining the concerned vars
			defRanges = entry.getValue();
			tmpRanges = DefUse.getDefs(defuseLists.get(vars.indexOf(v)));
			for (SourceCodeRange tmpRange : tmpRanges) {
				astNode = astNodes.get(astNodeRanges.indexOf(tmpRange));
				// 3. if some concerned def(s) flow to a certain def D, D should
				// be included
				beforeValue = this.getValueBeforeASTNode(astNode);
				beforeDefs = new HashSet<SourceCodeRange>(beforeValue.get(vars
						.indexOf(v)).defs);
				beforeDefs.retainAll(defRanges);
				if (!beforeDefs.isEmpty()) {
					resultSet.add(tmpRange);
				}
			}
		}
		// resultSet = filter(resultSet);
		return resultSet;
	}

	/**
	 * To get all defs killed by the known node For a given def, find upstream
	 * def(s)
	 * 
	 * @param node
	 * @return
	 */
	private Set<SourceCodeRange> getUpstreamDef(Node node) {
		Set<SourceCodeRange> resultSet = new HashSet<SourceCodeRange>();
		// 1. find all vars defined in the current node
		Map<Variable, Set<SourceCodeRange>> defFact = getDefFact(node);
		Variable v = null;
		Set<SourceCodeRange> knownDefs = null, tempDefs = null;
		TupleLatticeElement<Integer, DefUseLE> beforeValue = null;
		// 2. check each v to see whether its current def kills any previous def
		for (Entry<Variable, Set<SourceCodeRange>> entry : defFact.entrySet()) {
			v = entry.getKey();
			knownDefs = entry.getValue();
			for (SourceCodeRange def : knownDefs) {
				beforeValue = this.getValueBeforeASTNode(astNodes
						.get(astNodeRanges.indexOf(def)));
				if (beforeValue == null)
					continue;
				tempDefs = beforeValue.get(vars.indexOf(v)).defs;
				// 3. if so, add these defs into the resultSet
				if (!tempDefs.isEmpty())
					resultSet.addAll(tempDefs);
			}
		}
		// resultSet = filter(resultSet);
		return resultSet;
	}

	public List<ASTNode> getASTNodes() {
		return astNodes;
	}

	public List<SourceCodeRange> getASTNodeRanges() {
		return astNodeRanges;
	}

	public TACFlowAnalysis<TupleLatticeElement<Integer, DefUseLE>> getAnalysis() {
		return analysis;
	}

	public Set<Variable> getDefinedVars(Set<SourceCodeRange> ranges) {
		Set<Variable> result = new HashSet<Variable>();
		Set<SourceCodeRange> defRanges = null;
		Variable var = null;
		for (int i = 0; i < defRangesList.size(); i++) {
			defRanges = defRangesList.get(i);
			for (SourceCodeRange scr : ranges) {
				if (defRanges.contains(scr)) {
					var = vars.get(i);
					if (var instanceof SourceVariable) {
						result.add((SourceVariable) var);
						break;
					}
				}
			}
		}
		return result;
	}

	/**
	 * to get down def for current def/use
	 * 
	 * @param node
	 * @return
	 */
	public Set<SourceCodeRange> getDownDef2(Node node) {
		Set<SourceCodeRange> resultSet = getDownstreamDef(node);
		// 1. get all defs used by current node
		Map<Variable, Set<SourceCodeRange>> useFact = getUseFact(node);
		Variable var = null;
		Set<SourceCodeRange> knownDefs = null, tmpDefs = null, tmpDefs2 = null;
		TupleLatticeElement<Integer, DefUseLE> beforeValue = null;
		ASTNode tmpASTNode = null;
		for (Entry<Variable, Set<SourceCodeRange>> entry : useFact.entrySet()) {
			// 2. for each used def, find all defs killing it, regarding it as
			// an approximation of downstream defs
			var = entry.getKey();
			knownDefs = entry.getValue();
			tmpDefs = DefUse.getDefs(defuseLists.get(vars.indexOf(var)));
			for (SourceCodeRange tmpDef : tmpDefs) {
				tmpASTNode = astNodes.get(astNodeRanges.indexOf(tmpDef));
				beforeValue = this.getValueBeforeASTNode(tmpASTNode);
				// 3. the concerned defs is killed by the new def
				tmpDefs2 = new HashSet<SourceCodeRange>(beforeValue.get(vars
						.indexOf(var)).defs);
				tmpDefs2.retainAll(knownDefs);
				if (!tmpDefs2.isEmpty()) {
					resultSet.add(tmpDef);
				}
			}
		}
		// resultSet = filter(resultSet);
		return resultSet;
	}

	/**
	 * find down use for a given def/use
	 * 
	 * @param node
	 * @return
	 */
	public Set<SourceCodeRange> getDownUse2(Node node) {
		Set<SourceCodeRange> resultSet = getDownstreamDataDependence(node);// this
																			// is
																			// only
																			// for
																			// current
																			// def
		Map<Variable, Set<SourceCodeRange>> useFact = getUseFact(node);
		Variable var = null;
		List<DefUse> duList = null;
		Set<SourceCodeRange> knownDefs = null;
		List<SourceCodeRange> tmpUses = null;
		List<SourceCodeRange> uses = null;
		// 1. get all ranges using some defs
		for (Entry<Variable, Set<SourceCodeRange>> entry : useFact.entrySet()) {
			// 2. for each using range, find all uses coming after it
			var = entry.getKey();
			knownDefs = entry.getValue();
			duList = defuseLists.get(vars.indexOf(var));
			for (DefUse du : duList) {
				if (knownDefs.contains(du.def)) {
					uses = du.getUses();
					tmpUses = new ArrayList<SourceCodeRange>(uses);
					tmpUses.removeAll(knownDefs);
					for (int i = 1; i <= tmpUses.size(); i++) {
						if (tmpUses.get(tmpUses.size() - i).equals(
								uses.get(uses.size() - i))) {
							resultSet.add(tmpUses.get(tmpUses.size() - i));
						} else {// end comparison once meet with the first
								// differnt range pair
							break;
						}
					}
				}
			}
		}
		return resultSet;
	}

	/**
	 * To get def for current use/def
	 * 
	 * @param node
	 * @return
	 */
	public Set<SourceCodeRange> getUpDef2(Node node) {
		Set<SourceCodeRange> resultSet = getUpstreamDef(node);
		resultSet.addAll(getUpstreamDataDependence(node));
		return resultSet;
	}

	/**
	 * To get use before current use, the result should be ordered
	 * 
	 * @param node
	 * @return
	 */
	public List<SourceCodeRange> getUpUse2(Node node) {
		Set<SourceCodeRange> resultSet = new HashSet<SourceCodeRange>();
		Map<Variable, Set<SourceCodeRange>> useFact = getUseFact(node);
		List<SourceCodeRange> tmpRanges = null, useRanges = null;
		Variable var = null;
		Set<SourceCodeRange> knownDefs = null;
		SourceCodeRange r1 = null;
		List<DefUse> duList = null;
		for (Entry<Variable, Set<SourceCodeRange>> entry : useFact.entrySet()) {
			// 1. for each var used in the node
			var = entry.getKey();
			// 2. find the defs it is using
			knownDefs = entry.getValue();
			duList = defuseLists.get(vars.indexOf(var));
			// 3. for each used def, check all uses before the earliest current
			// use for that def
			for (DefUse du : duList) {
				if (knownDefs.contains(du.getDef())) {
					useRanges = du.getUses();
					tmpRanges = new ArrayList<SourceCodeRange>(useRanges);
					for (int i = 0; i < tmpRanges.size(); i++) {
						r1 = tmpRanges.get(i);
						if (r1.equals(useRanges.get(i))) {
							resultSet.add(r1);
						} else {// end comparison once find the first different
								// range pair
							break;
						}
					}
				}
			}
		}
		List<SourceCodeRange> orderedResults = new ArrayList<SourceCodeRange>(
				resultSet);
		Collections.sort(orderedResults);
		return orderedResults;
	}

	public Set<Node> getNodeDataDependence(Node node) {
		this.fUpstreamSeeds.clear();
		this.fDownstreamSeeds.clear();
		this.fUpstreamSeeds2.clear();
		this.fDownstreamSeeds2.clear();

		Set<Node> resultSet = new HashSet<Node>();
		Set<Node> upstreamSeeds = new HashSet<Node>();
		Set<Node> downstreamSeeds = new HashSet<Node>();

		upstreamSeeds.add(node);
		downstreamSeeds.add(node);
		System.out.print("");
		Set<Node> tempSet = new HashSet<Node>();
		Set<Node> tempUpstreamResult = new HashSet<Node>();
		Set<Node> tempDownstreamResult = new HashSet<Node>();
		for (int i = 0; i < DEF_USE_ITERATION; i++) {
			if (UPSTREAM_DATA_DEPENDENCE) {
				tempUpstreamResult.clear();
				for (Node seed : upstreamSeeds) {
					tempSet = searchForRelevantDataNodes(getUpstreamDataDependence(seed));
					tempUpstreamResult.addAll(tempSet);
					resultSet.addAll(tempSet);
				}
				tempUpstreamResult.removeAll(fUpstreamSeeds);
				upstreamSeeds.clear();
				upstreamSeeds.addAll(tempUpstreamResult);
				fUpstreamSeeds.addAll(tempUpstreamResult);
			}
			if (DOWNSTREAM_DATA_DEPENDENCE) {
				tempDownstreamResult.clear();
				for (Node seed : downstreamSeeds) {
					tempSet = searchForRelevantDataNodes(getDownstreamDataDependence(seed));
					tempDownstreamResult.addAll(tempSet);
					resultSet.addAll(tempSet);
				}
				tempDownstreamResult.removeAll(fDownstreamSeeds);
				downstreamSeeds.clear();
				downstreamSeeds.addAll(tempDownstreamResult);
				fDownstreamSeeds.addAll(tempDownstreamResult);
			}
		}

		// fUpstreamSeeds.addAll(upstreamSeeds);
		upstreamSeeds.clear();
		upstreamSeeds.add(node);

		// fDownstreamSeeds.addAll(downstreamSeeds);
		downstreamSeeds.clear();
		downstreamSeeds.add(node);
		for (int i = 0; i < DEF_DEF_ITERATION; i++) {
			if (UPSTREAM_DEF_DEF) {
				tempUpstreamResult.clear();
				for (Node seed : upstreamSeeds) {
					tempSet = searchForRelevantNodes(new ArrayList<SourceCodeRange>(
							getUpstreamDef(seed)));
					tempUpstreamResult.addAll(tempSet);
					resultSet.addAll(tempSet);
				}
				// to remove the nodes which have already served as seeds to
				// avoid repetitive analysis
				tempUpstreamResult.removeAll(fUpstreamSeeds2);
				upstreamSeeds.clear();
				upstreamSeeds.addAll(tempUpstreamResult);
				fUpstreamSeeds2.addAll(upstreamSeeds);
			}
			if (DOWNSTREAM_DEF_DEF) {
				tempDownstreamResult.clear();
				for (Node seed : downstreamSeeds) {
					tempSet = searchForRelevantNodes(new ArrayList<SourceCodeRange>(
							getDownstreamDef(seed)));
					tempDownstreamResult.addAll(tempSet);
					resultSet.addAll(tempSet);
				}
				// to remove the nodes which have already served as seeds to
				// avoid repetitive analysis
				tempDownstreamResult.removeAll(fDownstreamSeeds2);
				downstreamSeeds.clear();
				downstreamSeeds.addAll(tempDownstreamResult);
				fDownstreamSeeds2.addAll(downstreamSeeds);
			}
		}

		fUpstreamSeeds.remove(node);
		fDownstreamSeeds.remove(node);
		fUpstreamSeeds2.remove(node);
		fDownstreamSeeds2.remove(node);
		resultSet.remove(node);
		return resultSet;
	}

	public Set<Node> getTransitiveDataDependence(Node node) {
		Set<Node> result = new HashSet<Node>();
		Set<Node> tmpSet = null;
		Set<Node> upstreamRs = new HashSet<Node>();
		// Set<Node> downstreamRs = new HashSet<Node>();
		Set<Node> upstreamSeeds = new HashSet<Node>();
		// Set<Node> downstreamSeeds = new HashSet<Node>();

		upstreamSeeds.add(node);
		// downstreamSeeds.add(node);

		while (!upstreamSeeds.isEmpty()) {
			tmpSet = new HashSet<Node>();
			for (Node tmpNode : upstreamSeeds) {
				tmpSet = searchForRelevantDataNodes(getUpstreamDataDependence(tmpNode));
			}
			upstreamRs.addAll(upstreamSeeds);
			tmpSet.removeAll(upstreamRs);
			upstreamSeeds = tmpSet;
		}

		result.addAll(upstreamRs);
		// result.addAll(downstreamRs);
		return result;
	}

	public Set<FieldVariable> getOutputFields(Node node) {
		Set<FieldVariable> result = new HashSet<FieldVariable>();
		SourceCodeRange range = node.getSourceCodeRange();
		Set<SourceCodeRange> defRanges = null;
		Variable var = null;
		for (int i = 0; i < defRangesList.size(); i++) {
			defRanges = defRangesList.get(i);
			for (SourceCodeRange scr : defRanges) {
				if (scr.isInside(range)) {
					var = vars.get(i);
					if (var instanceof FieldVariable) {
						result.add((FieldVariable) var);
						break;
					}
				}
			}
		}
		return result;
	}

	public Set<SourceVariable> getOutputVars(Node node) {
		Set<SourceVariable> result = new HashSet<SourceVariable>();
		SourceCodeRange range = node.getSourceCodeRange();
		Set<SourceCodeRange> defRanges = null;
		Variable var = null;
		for (int i = 0; i < defRangesList.size(); i++) {
			defRanges = defRangesList.get(i);
			for (SourceCodeRange scr : defRanges) {
				if (scr.isInside(range)) {
					var = vars.get(i);
					if (var instanceof SourceVariable) {
						result.add((SourceVariable) var);
						break;
					}
				}
			}
		}
		return result;
	}

	public MethodDeclaration getMethodDeclaration() {
		return this.d;
	}

	private Set<SourceCodeRange> filter(Set<SourceCodeRange> resultSet) {
		// filter out the expressions within the current node
		ASTNode element;
		Set<SourceCodeRange> filteredResultSet = new HashSet<SourceCodeRange>();
		for (SourceCodeRange result : resultSet) {
			element = findElement(result);
			if (element != null) {
				switch (element.getNodeType()) {
				case ASTNode.BOOLEAN_LITERAL:
				case ASTNode.CHARACTER_LITERAL:
				case ASTNode.NULL_LITERAL:
				case ASTNode.NUMBER_LITERAL:
				case ASTNode.STRING_LITERAL:
				case ASTNode.TYPE_LITERAL:
					break; // do nothing
				default:
					filteredResultSet.add(result);
					break;
				}
			}else{
				filteredResultSet.add(result);
			}
		}
		resultSet = filteredResultSet;
		return resultSet;
	}

	/**
	 * find all defs in the given astExpressions it is possible that a variable
	 * is defined multiple times
	 * 
	 * @param astExpressions
	 * @return
	 */
	public Map<Variable, Set<SourceCodeRange>> getDefFact(Node seed) {
		if (defFactCache.containsKey(seed)) {
			Map<Variable, Set<SourceCodeRange>> tmpMap = defFactCache.get(seed)
					.get();
			if (tmpMap != null)
				return tmpMap;
		}
		List<SourceCodeRange> ranges = getRangesIncluded(seed);
		Map<Variable, Set<SourceCodeRange>> result = new HashMap<Variable, Set<SourceCodeRange>>();
		Set<SourceCodeRange> defRanges = null;
		Set<SourceCodeRange> tmpRanges = null;
		Variable v = null;

		for (int i = 0; i < defRangesList.size(); i++) {
			defRanges = new HashSet<SourceCodeRange>(defRangesList.get(i));
			for (SourceCodeRange defRange : defRanges) {
				for (SourceCodeRange r : ranges) {
					if (defRange.isInside(r)) {
						v = vars.get(i);
						tmpRanges = result.get(v);
						if (tmpRanges == null) {
							tmpRanges = new HashSet<SourceCodeRange>();
							result.put(v, tmpRanges);
						}
						tmpRanges.add(defRange);
						break;
					}
				}
			}
		}
		for (Entry<Variable, Set<SourceCodeRange>> entry : result.entrySet()) {
			entry.setValue(filter(entry.getValue()));
		}
		defFactCache.put(seed, new WeakReference(result));
		return result;
	}

	/**
	 * This is different from the above function since it is only focused on the
	 * facts relevant to the ranged node.
	 * 
	 * @param range
	 * @return
	 */
	public Map<Variable, Set<SourceCodeRange>> getDefFact(SourceCodeRange range) {
		Map<Variable, Set<SourceCodeRange>> result = new HashMap<Variable, Set<SourceCodeRange>>();
		Set<SourceCodeRange> defRanges = null;
		Variable v = null;
		for (int i = 0; i < defRangesList.size(); i++) {
			defRanges = new HashSet<SourceCodeRange>(defRangesList.get(i));
			if (defRanges.contains(range)) {
				v = vars.get(i);
				if (result.get(v) == null) {
					result.put(v, defRanges);
				} else {
					result.get(v).addAll(defRanges);
				}
			}
		}
		for (Entry<Variable, Set<SourceCodeRange>> entry : result.entrySet()) {
			entry.setValue(filter(entry.getValue()));
		}
		return result;
	}

	public Set<FieldVariable> getFieldsRead(Node node) {
		List<SourceCodeRange> ranges = node.getASTExpressions();
		Set<FieldVariable> result = new HashSet<FieldVariable>();
		for (SourceCodeRange range : ranges) {
			result.addAll(getFieldsRead(range));
		}
		return result;
	}

	/**
	 * Notice!! This is different from the above method
	 * 
	 * @param range
	 * @return
	 */
	public Set<FieldVariable> getFieldsRead(SourceCodeRange range) {
		List<TupleLatticeElement<Integer, DefUseLE>> values = getValuesAfterASTNode(range);
		Set<String> fieldStrings = new HashSet<String>();
		for (TupleLatticeElement<Integer, DefUseLE> value : values) {
			for (Integer key : value.getKeySet()) {
				fieldStrings.addAll(value.get(key).fieldsAlreadyDefined);
			}
		}
		return convertFields(fieldStrings);
	}

	private Set<FieldVariable> convertFields(Set<String> fieldStrings) {
		Set<FieldVariable> result = new HashSet<FieldVariable>();
		for (String fs : fieldStrings) {
			result.add(fieldMap.get(fs));
		}
		return result;
	}

	/**
	 * This is used to find useFact of the ranged node, but does not do that for
	 * all nodes included by the range
	 * 
	 * @param range
	 * @return
	 */
	public Map<Variable, Set<SourceCodeRange>> getUseFact(SourceCodeRange range)
			throws Exception {
		Map<Variable, Set<SourceCodeRange>> result = null;
		List<TupleLatticeElement<Integer, DefUseLE>> valueList = this
				.getValuesAfterASTNode(range);
		Set<SourceCodeRange> useRanges = null;
		result = new HashMap<Variable, Set<SourceCodeRange>>();
		Variable var = null;
		for (TupleLatticeElement<Integer, DefUseLE> value : valueList) {
			for (Integer key : value.getKeySet()) {
				useRanges = new HashSet<SourceCodeRange>(value.get(key).uses);
				// 3. remove the ranges defined by current node
				useRanges.remove(range);
				var = vars.get(key);
				if (result.containsKey(var)) {
					result.get(var).addAll(useRanges);
				} else if (!useRanges.isEmpty()) {
					result.put(var, useRanges);
				}
			}
		}
		for (Entry<Variable, Set<SourceCodeRange>> entry : result.entrySet()) {
			entry.setValue(filter(entry.getValue()));
		}
		return result;
	}

	/**
	 * Find all defs used by current node
	 * 
	 * @param seed
	 * @return
	 */
	public Map<Variable, Set<SourceCodeRange>> getUseFact(Node seed) {
		Map<Variable, Set<SourceCodeRange>> result = null;
		List<TupleLatticeElement<Integer, DefUseLE>> valueList = null;

		// 1. find all ranges included
		List<SourceCodeRange> ranges = getRangesIncluded(seed);
		result = new HashMap<Variable, Set<SourceCodeRange>>();
		Set<SourceCodeRange> useRanges = null;
		Set<String> fields = null;
		Variable var = null;
		System.out.print("");
		// 2. find all uses of each range
		for (int i = 0; i < ranges.size(); i++) {
			valueList = this.getValuesAfterASTNode(ranges.get(i));
			for (TupleLatticeElement<Integer, DefUseLE> value : valueList) {
				for (Integer key : value.getKeySet()) {
					useRanges = new HashSet<SourceCodeRange>(
							value.get(key).uses);
					// 3. remove the ranges defined by current node
					useRanges.removeAll(ranges);
					if(!useRanges.isEmpty()){
						Set<SourceCodeRange> newUseRanges = new HashSet<SourceCodeRange>(useRanges);		
						for(SourceCodeRange ur : useRanges){
							for(SourceCodeRange r : ranges){
								if(ur.isInside(r)){
									newUseRanges.remove(ur);
									break;
								}
							}
						}
						useRanges = newUseRanges;
					}
					fields = value.get(key).fieldsAlreadyDefined;
					var = vars.get(key);
					if (useRanges.isEmpty()
							&& ((!(var instanceof FieldVariable)) || !(fields
									.contains(var.toString())))) {
						continue;
					}
					// useRanges is not empty or it is empty but it is for
					// certain field accessed by the node
					if (result.containsKey(var)) {
						result.get(var).addAll(useRanges);
					} else {
						result.put(var, useRanges);
					}
				}
			}
		}
		for (Entry<Variable, Set<SourceCodeRange>> entry : result.entrySet()) {
			entry.setValue(filter(entry.getValue()));
		}
		return result;
	}

	private TACInstruction getInstr(ASTNode element) {
		TACInstruction instr = null;
		try {
			instr = tac.instruction(element);
		} catch (Exception e) {
			MethodDeclaration innerMd = Utilities.getMethodDeclaration(element);
			EclipseTAC tempTac = cTac.getMethodTAC(innerMd);
			instr = tempTac.instruction(element);
		}
		return instr;
	}

	private List<TACInstruction> getInstrIncluded(SourceCodeRange range) {
		List<TACInstruction> result = new ArrayList<TACInstruction>();
		ASTNode astNode = null;
		TACInstruction instr = null;
		for (SourceCodeRange tmpRange : astNodeRanges) {
			if (tmpRange.isInside(range)) {
				astNode = astNodes.get(astNodeRanges.indexOf(tmpRange));
				instr = getInstr(astNode);
				if (instr != null) {
					result.add(instr);
				}
			}
			/*
			 * if (tmpRange.equals(range)) { instr =
			 * getInstr(astNodes.get(astNodeRanges.indexOf(tmpRange))); if
			 * (instr != null) { result.add(instr); break; } }
			 */
		}
		return result;
	}

	/**
	 * Modify the returned value if the instr is instanceof
	 * EclipseInstructionSequence
	 * 
	 * @param instr
	 * @return
	 */
	private TupleLatticeElement<Integer, DefUseLE> getValueAfterInstr(
			TACInstruction instr) {
		/*
		 * try { if (instr instanceof EclipseInstructionSequence) {
		 * TACInstruction[] instructions = ((EclipseInstructionSequence) instr)
		 * .getInstructions(); TupleLatticeElement<Integer, DefUseLE> result =
		 * analysis .getResultsAfter(instr); TupleLatticeElement<Integer,
		 * DefUseLE> tmp = null; Set<SourceCodeRange> tmpRanges = null; for (int
		 * i = 0; i < instructions.length; i++) { tmp =
		 * analysis.getResultsAfter(instructions[i]); for (Integer key :
		 * tmp.getKeySet()) { tmpRanges = tmp.get(key).uses; if
		 * (!tmpRanges.isEmpty()) result.get(key).uses.addAll(tmpRanges); } }
		 * return result; } else { return analysis.getResultsAfter(instr); } }
		 * catch (Exception e) { return null; }
		 */try {
			return analysis.getResultsAfter(instr);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * If the ASTNode does not correspond to an instruction, do not see its
	 * relevant result
	 */
	public List<TupleLatticeElement<Integer, DefUseLE>> getValuesAfterASTNode(
			SourceCodeRange range) {
		// modified 06/04/13
		if (resultsAfterCache.containsKey(range)
				&& resultsAfterCache.get(range).get() != null) {
			return resultsAfterCache.get(range).get();
		}
		List<TupleLatticeElement<Integer, DefUseLE>> result = new ArrayList<TupleLatticeElement<Integer, DefUseLE>>();
		ASTNode astNode = null;
		TACInstruction instr = null;
		List<TACInstruction> instrs = getInstrIncluded(range);
		// the node is inside an inner method declaration
		if (instrs.isEmpty()) {
			ASTNodeFinder finder = new ASTNodeFinder();
			astNode = finder.lookforASTNode(d, range);
			MethodDeclaration innerMd = Utilities.getMethodDeclaration(astNode);
			EclipseTAC tempTac = cTac.getMethodTAC(innerMd);
			instr = tempTac.instruction(astNode);
			if (instr != null) {
				// instrs.add(instr);
				result.add(getValueAfterInstr(instr));
			} else {
				/*
				 * Set<ASTNode> astNodesInside = collector
				 * .collectASTNodesWithin(new SourceCodeRange(astNode
				 * .getStartPosition(), astNode.getLength())); for (ASTNode
				 * tmpASTNode : astNodesInside) { instr =
				 * tempTac.instruction(tmpASTNode); if (instr != null) {
				 * instrs.add(instr); } }
				 */
				result.add(this.getValueBeforeASTNode(astNode));
			}
		} else {
			for (TACInstruction tmpInstr : instrs) {
				if (tmpInstr instanceof EclipseInstructionSequence
						&& tmpInstr.getNode() instanceof VariableDeclarationFragment) {
					TACInstruction[] instructions = ((EclipseInstructionSequence) tmpInstr)
							.getInstructions();
					result.add(getValueAfterInstr(instructions[instructions.length - 1]));
				} else {
					result.add(getValueAfterInstr(tmpInstr));
				}
			}
		}
		resultsAfterCache
				.put(range,
						new WeakReference<List<TupleLatticeElement<Integer, DefUseLE>>>(
								result));
		return result;
	}

	public TupleLatticeElement<Integer, DefUseLE> getValueBeforeASTNode(
			ASTNode element) {
		return analysis.getResultsBefore(element);
		/*
		 * (modified 06/07/2013) TACInstruction instr = getInstr(element); if
		 * (instr != null) return analysis.getResultsBefore(instr); return null;
		 */
	}

	/**
	 * vars keeps track of all vars defined in the methods, may it be a local
	 * variable or a field fields keeps track of all fields touched by the
	 * method, may it be defined, used, or both
	 */
	private void setDefUses() {
		this.defuseLists = new ArrayList<List<DefUse>>(vars.size());
		defRangesList = new ArrayList<Set<SourceCodeRange>>(vars.size());
		for (int i = 0; i < vars.size(); i++) {
			defuseLists.add(new ArrayList<DefUse>());
			defRangesList.add(new HashSet<SourceCodeRange>());
		}
		SourceCodeRange curScr = null;
		List<TupleLatticeElement<Integer, DefUseLE>> values = null;
		Set<SourceCodeRange> tmpUses = null;
		Set<SourceCodeRange> tmpDefs = null;
		Set<SourceCodeRange> knownDefs = null;
		List<SourceCodeRange> uses = null;
		DefUse tmpDefUse;
		List<DefUse> defUses = null;
		for (ASTNode astNode : astNodes) {
			TACInstruction instr = getInstr(astNode);
			if (instr == null)
				continue;
			curScr = new SourceCodeRange(astNode.getStartPosition(),
					astNode.getLength());
			values = new ArrayList<TupleLatticeElement<Integer, DefUseLE>>();
			if (instr instanceof EclipseInstructionSequence) {
				TACInstruction[] instrs = ((EclipseInstructionSequence) instr)
						.getInstructions();
				for (int i = 0; i < instrs.length; i++) {
					values.add(getValueAfterInstr(instrs[i]));
				}
			} else {
				values.add(getValueAfterInstr(instr));
			}
			for (TupleLatticeElement<Integer, DefUseLE> value : values) {
				// 3. for each astNode, check whether it uses some variable
				for (Integer key : value.getKeySet()) {
					tmpUses = value.get(key).uses;
					if (!tmpUses.isEmpty()) {
						defUses = defuseLists.get(key);
						for (SourceCodeRange tmpDef : tmpUses) {
							tmpDefUse = DefUse.getDefUse(defUses, tmpDef);
							if (tmpDefUse == null) {
								uses = new ArrayList<SourceCodeRange>();
								uses.add(curScr);
								tmpDefUse = new DefUse(tmpDef, uses);
								defUses.add(tmpDefUse);
							} else {
								tmpDefUse.addUse(curScr);
							}
						}
					}
					tmpDefs = value.get(key).defs;
					if (tmpDefs.contains(curScr)) {
						knownDefs = defRangesList.get(key);
						knownDefs.add(curScr);
					}
				}
			}
		}
		DefUse.sort(defuseLists);
		// for (int i = 0; i < vars.size(); i++) {
		// defRangesList.add(DefUse.getDefs(defuseLists.get(i)));
		// }
	}
}

class DefUse implements Comparable {
	SourceCodeRange def;
	List<SourceCodeRange> uses;

	public DefUse(SourceCodeRange def, List<SourceCodeRange> uses) {
		this.def = def;
		this.uses = uses;
	}

	public void addUse(SourceCodeRange scr) {
		if (uses.contains(scr))
			return;
		uses.add(scr);
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof DefUse))
			return false;
		DefUse other = (DefUse) obj;
		if (!def.equals(other.def))
			return false;
		if (!uses.equals(other.uses))
			return false;
		return true;
	}

	public SourceCodeRange getDef() {
		return def;
	}

	public List<SourceCodeRange> getUses() {
		return uses;
	}

	public int hashCode() {
		return def.hashCode() * 1000 + uses.hashCode();
	}

	public static Set<SourceCodeRange> getDefs(List<DefUse> dus) {
		Set<SourceCodeRange> results = new HashSet<SourceCodeRange>();
		for (DefUse du : dus) {
			results.add(du.def);
		}
		return results;
	}

	public static DefUse getDefUse(List<DefUse> list, SourceCodeRange def) {
		for (DefUse du : list) {
			if (du.def.equals(def))
				return du;
		}
		return null;
	}

	public static void sort(List<List<DefUse>> list) {
		for (List<DefUse> defUses : list) {
			for (DefUse du : defUses) {
				du.sort();// the order of use sourcecoderange is an
							// approximation for use reordering
			}
		}
	}

	@Override
	public int compareTo(Object o) {
		DefUse other = (DefUse) o;
		return def.compareTo(other.def);
	}

	public void sort() {
		Collections.sort(uses);
	}
}
