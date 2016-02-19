package changeassistant.multipleexample.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import changeassistant.changesuggestion.expression.representation.ASTExpressionTransformer;
import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.Term.TermType;
import changeassistant.changesuggestion.expression.representation.TypeNameTerm;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;
import changeassistant.multipleexample.partition.MappingException;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.util.IdMapper;

/**
 * This is an enhanced version of CommonParser because it is aimed at handling
 * common parsing among multiple examples
 * 
 * @author mn8247
 * 
 */
public class CommonParser2 {

	private int abstractStmt = 0;
	private int abstractMethod = 0;
	private int abstractVariable = 0;
	private int abstractType = 0;
	private int abstractUnknown = 0;

	private Map<String, String> knownUnifiedToSpecific;
	private List<Map<String, String>> knownUnifiedToSpecificList;

	private Map<String, String> s1Tou = null;
	private List<Map<String, String>> s2Tous = null;

	private EqualCalculator<Term> termComparator = new EqualCalculator<Term>();
	private EqualCalculator<TermType> typeComparator = new EqualCalculator<TermType>();
	private EqualCalculator<String> nameComparator = new EqualCalculator<String>();
	private EqualCalculator<Integer> integerComparator = new EqualCalculator<Integer>();

	private void addEntryS2Tous(List<String> rNames, String uniName) {
		for (int j = 0; j < rNames.size(); j++) {
			s2Tous.get(j).put(rNames.get(j), uniName);
		}
	}

	private String getAbstractIdentifier(int nodeType1,
			List<Integer> nodeType2s, List<String> abstractIdentifiers) {
		StringBuffer uniName = new StringBuffer(Term.createAbstractName(
				ASTExpressionTransformer.ABSTRACT_STATEMENT, abstractStmt++));
		uniName.append(Term.createStmtSuffix(nodeType1, nodeType2s,
				abstractIdentifiers));
		return uniName.toString();
	}

	private String getAbstractIdentifier(String lName, List<String> rNames)
			throws MappingException {
		String uniName = lName;
		String luniName = s1Tou.get(lName);
		List<String> runiNames = new ArrayList<String>();
		for (int i = 0; i < s2Tous.size(); i++) {
			runiNames.add(s2Tous.get(i).get(rNames.get(i)));
		}
		if (luniName == null && nameComparator.hasAllEqualTo2(null, runiNames)) {
			return uniName;
		}
		if (luniName != null
				&& nameComparator.hasAllNotEqualTo2(null, runiNames)
				&& nameComparator.hasAllEqualTo(luniName, runiNames)) {
			return uniName;
		} else {
			StringBuffer info = new StringBuffer(
					lName
							+ " cannot be mapped to the following names since they are already mapped differently: \n");
			for (int i = 0; i < runiNames.size(); i++) {
				info.append(rNames.get(i)).append("\t");
			}
			throw new MappingException(info.toString());
		}
	}

	private String getAbstractIdentifier(String lName, List<String> rNames,
			String abstractName) throws MappingException {
		String uniName = getMappedIdentifier(lName, rNames);
		if (uniName == null) {
			if (abstractName.equals(ASTExpressionTransformer.ABSTRACT_VARIABLE)) {
				uniName = Term.createAbstractName(abstractName,
						abstractVariable++);
			} else if (abstractName
					.equals(ASTExpressionTransformer.ABSTRACT_METHOD)) {
				uniName = Term.createAbstractName(abstractName,
						abstractMethod++);
			} else if (abstractName
					.equals(ASTExpressionTransformer.ABSTRACT_TYPE)) {
				uniName = Term.createAbstractName(abstractName, abstractType++);
			} else {
				uniName = Term.createAbstractName(abstractName,
						abstractUnknown++);
			}
		}
		return uniName;
	}

	/**
	 * Currently, getMappedIdentifier do not filter conflicting maps because
	 * some pre-process, like addMatchPairs(), has already checked conflict.
	 * 
	 * @param lName
	 * @param rNames
	 * @return
	 * @throws MappingException
	 */
	private String getMappedIdentifier(String lName, List<String> rNames)
			throws MappingException {
		String luniName = null;
		String uniName = null;
		List<String> runiNames = new ArrayList<String>();
		// List<String> tmpSpecNames = null;
		// Map<String, String> tmpMap = null;
		String rName = null;
		String runiName = null;
		for (int i = 0; i < rNames.size(); i++) {
			rName = rNames.get(i);
			runiName = s2Tous.get(i).get(rName);
			runiNames.add(runiName);
		}
		if (lName != null && s1Tou.get(lName) != null) {
			luniName = s1Tou.get(lName);
		}
		if (luniName != null
				&& nameComparator.hasAllNotEqualTo2(null, runiNames)) {
			if (nameComparator.hasAllEqualTo(luniName, runiNames)) {
				uniName = luniName;
			} else {
				StringBuffer info = new StringBuffer(
						"The following identifiers have been mapped differently already: ");
				info.append("\n");
				info.append(lName).append("\t");
				for (int i = 0; i < runiNames.size(); i++) {
					info.append(rNames.get(i)).append("\t");
				}
				throw new MappingException(info.toString());
			}
		} else if (luniName != null
				&& nameComparator.hasSomeEqualTo2(null, runiNames)
				|| luniName == null
				&& nameComparator.hasSomeNotEqualTo2(null, runiNames)) {
			if (luniName != null) {
				uniName = luniName;
				// tmpSpecNames = new ArrayList<String>();
				// for(int i= 0; i < s2Tous.size(); i++){
				// tmpMap = s2Tous.get(i);
				// for(Entry<String, String> entry : tmpMap.entrySet()){
				// if(entry.getValue().equals(uniName)){
				// tmpSpecNames.add(entry.getKey());
				// break;
				// }
				// }
				// }
				// StringBuffer info = new StringBuffer(lName +
				// " cannot be mapped to the following names: \n");
				// for(int i = 0; i < rNames.size(); i++){
				// info.append(rNames.get(i)).append("\t");
				// }
				// info.append("\n since it has been already mapped to the following names: \n");
				// for(int i = 0; i < tmpSpecNames.size(); i++){
				// info.append(tmpSpecNames.get(i)).append("\t");
				// }
				// throw new MappingException(info.toString());
			} else {
				for (String tmpName : runiNames) {
					if (tmpName != null) {
						uniName = tmpName;
						break;
					}
				}
				// tmpSpecNames = new ArrayList<String>();
				// StringBuffer info = new StringBuffer(lName +
				// " cannot be mapped to the following names: \n");
				// for(int i = 0; i < runiNames.size(); i++){
				// runiName = runiNames.get(i);
				// if(runiName != null){
				// tmpMap = s2Tous.get(i);
				// for(Entry<String, String> entry : tmpMap.entrySet()){
				// if(entry.getValue().equals(runiName)){
				// tmpSpecNames.add(entry.getKey());
				// break;
				// }
				// }
				// }
				// }
				// info.append("\n since some of them have already been mapped to other names");
			}
		}
		return uniName;
	}

	private String getAbstractSpecialIdentifier(int lType,
			List<Integer> rNodeTypes, String lName, List<String> rNames,
			String abstractName) throws MappingException {
		String uniName = null;
		uniName = getMappedIdentifier(lName, rNames);
		if (uniName == null) {
			if (abstractName.equals(ASTExpressionTransformer.ABSTRACT_VARIABLE)) {
				uniName = Term.createAbstractName(abstractName,
						abstractVariable++);
			} else if (abstractName
					.equals(ASTExpressionTransformer.ABSTRACT_METHOD)) {
				uniName = Term.createAbstractName(abstractName,
						abstractMethod++);
			} else if (abstractName
					.equals(ASTExpressionTransformer.ABSTRACT_TYPE)) {
				uniName = Term.createAbstractName(abstractName, abstractType++);
			} else {// for the operators, we will give different prefix
					// for others, we will append something special
				uniName = Term.createAbstractName(abstractName,
						abstractUnknown++);
				if (lType == ASTExpressionTransformer.OPERATOR
						&& integerComparator.hasAllEqualTo(
								ASTExpressionTransformer.OPERATOR, rNodeTypes)) {
					uniName = uniName + Term.createOpSuffix(lName, rNames);
				} else {
					uniName = uniName
							+ Term.createExprSuffix(lType, rNodeTypes, lName,
									rNames);
				}
			}
		}
		return uniName;
	}

	/**
	 * Two purposes: 1. when two nodes have different types, a wildcard is
	 * created to represent them both; 2. when two nodes are of the same type
	 * but only differ in identifiers used, unified identifiers are created to
	 * represent the different identifiers The function can return null if the
	 * map is not consistent
	 * 
	 * @param simpleASTNodesList
	 *            -- the 1st simpleASTNodesList
	 * @param simpleASTNodesListList
	 *            --all simpleASTNodesList except for the 1st one
	 * @param nodeIndexArray
	 * @param nodeTypeArray
	 * @return
	 */
	public List<List<SimpleASTNode>> getCommon(
			List<Map<String, String>> specificToUnifiedList,
			List<Map<String, String>> unifiedToSpecificList,
			List<List<SimpleASTNode>> simpleASTNodesList,
			List<List<List<SimpleASTNode>>> simpleASTNodesListList,
			List<List<Integer>> nodeIndexLists,
			List<List<Integer>> nodeTypeLists) {
		Integer[][] nodeIndexArray = new Integer[nodeIndexLists.get(0).size()][nodeIndexLists
				.size()];
		Integer[][] nodeTypeArray = new Integer[nodeIndexLists.get(0).size()][nodeIndexLists
				.size()];
		List<List<SimpleASTNode>> newSimpleASTNodesList = new ArrayList<List<SimpleASTNode>>();
		List<Integer> nodeIndexList = null, nodeTypeList = null;
		Map<String, String> tmpMap = null;
		System.out.print("");
		setMap(specificToUnifiedList);

		for (int i = 0; i < nodeIndexLists.size(); i++) {
			nodeIndexList = nodeIndexLists.get(i);
			for (int j = 0; j < nodeIndexList.size(); j++) {// transpose
				nodeIndexArray[j][i] = nodeIndexList.get(j);
			}
		}
		for (int i = 0; i < nodeTypeLists.size(); i++) {
			nodeTypeList = nodeTypeLists.get(i);
			for (int j = 0; j < nodeTypeList.size(); j++) {
				nodeTypeArray[j][i] = nodeTypeList.get(j);
			}
		}
		List<SimpleASTNode> simpleASTNodes1 = null, simpleASTNodes2 = null, simpleASTNodes = null;
		List<List<SimpleASTNode>> simpleASTNodes2List = new ArrayList<List<SimpleASTNode>>();
		List<List<SimpleASTNode>> list = null;
		int nodeType1 = -1, nodeType2 = -1;
		boolean isUnknownType1 = false, isUnknownType2 = false;
		List<Integer> tmpNodeTypes = null;

		for (int i = 0; i < nodeIndexArray.length; i++) {
			simpleASTNodes1 = simpleASTNodesList
					.get((int) nodeIndexArray[i][0] - 1);
			nodeType1 = nodeTypeArray[i][0];
			if (simpleASTNodes1.size() == 1
					&& simpleASTNodes1.get(0).getNodeType() == ASTExpressionTransformer.UNKNOWN_STATEMENT) {
				isUnknownType1 = true;
			} else {
				isUnknownType1 = false;
			}

			simpleASTNodes2List.clear();
			tmpNodeTypes = Arrays.asList(nodeTypeArray[i]);
			isUnknownType2 = false;
			for (int j = 0; j < simpleASTNodesListList.size(); j++) {
				list = simpleASTNodesListList.get(j);
				simpleASTNodes2 = list.get((int) nodeIndexArray[i][j + 1] - 1);
				simpleASTNodes2List.add(simpleASTNodes2);
				if (simpleASTNodes2.size() == 1
						&& simpleASTNodes2.get(0).getNodeType() == ASTExpressionTransformer.UNKNOWN_STATEMENT) {
					isUnknownType2 = true;
				}
			}

			simpleASTNodes = new ArrayList<SimpleASTNode>();
			if (!integerComparator.hasAllEqualTo(nodeType1, tmpNodeTypes)
					|| isUnknownType1 || isUnknownType2) {
				List<String> abstractIdentifiers = new ArrayList<String>();
				if (simpleASTNodes1.size() == 1
						&& simpleASTNodes1.get(0).getNodeType() == ASTExpressionTransformer.UNKNOWN_STATEMENT) {
					abstractIdentifiers.add(simpleASTNodes1.get(0).getTerm()
							.getName());
				} else if (simpleASTNodes2.size() == 1
						&& simpleASTNodes2.get(0).getNodeType() == ASTExpressionTransformer.UNKNOWN_STATEMENT) {
					abstractIdentifiers.add(simpleASTNodes2.get(0).getTerm()
							.getName());
				} else {
					abstractIdentifiers = null;
				}
				simpleASTNodes.add(new SimpleASTNode(new Term(
						ASTExpressionTransformer.UNKNOWN_STATEMENT,
						getAbstractIdentifier(nodeType1, tmpNodeTypes,
								abstractIdentifiers))));
			} else {
				// all nodes have the same type, what we need to do is just
				// unify them
				if (isConsistentMap(simpleASTNodes1, simpleASTNodes2List)) {
					simpleASTNodes = new ArrayList<SimpleASTNode>(
							simpleASTNodes1);
				} else {
					return null;
				}
			}
			newSimpleASTNodesList.add(simpleASTNodes);
		}
		simpleASTNodesList.clear();
		simpleASTNodesList.addAll(newSimpleASTNodesList);
		specificToUnifiedList.clear();
		specificToUnifiedList.add(s1Tou);
		specificToUnifiedList.addAll(s2Tous);

		unifiedToSpecificList.clear();
		for (Map<String, String> specificToUnified : specificToUnifiedList) {
			tmpMap = IdMapper.createReverseMap(specificToUnified);
			unifiedToSpecificList.add(tmpMap);
		}
		return simpleASTNodesList;
	}

	private List<SimpleASTNode> getCommon(List<SimpleASTNode> simpleASTNodes1,
			List<List<SimpleASTNode>> simpleASTNodes2List) {
		List<SimpleASTNode> result = new ArrayList<SimpleASTNode>();
		SimpleASTNode s1 = null, s = null, commonPart = null;
		List<SimpleASTNode> s2s = null;
		try {
			Enumeration<SimpleASTNode> childEnum1 = null;
			List<Enumeration<SimpleASTNode>> childEnum2s = null;
			SimpleASTNode child1 = null, node1 = null, node2 = null, node = null, nodeParent = null;
			List<SimpleASTNode> child2s = null, node2s = null;

			for (int j = 0; j < simpleASTNodes1.size(); j++) {
				s1 = simpleASTNodes1.get(j);
				s2s = new ArrayList<SimpleASTNode>();
				for (int k = 0; k < simpleASTNodes2List.size(); k++) {
					s2s.add(simpleASTNodes2List.get(k).get(j));
				}
				s = parseCommon(s1, s2s);
				if (!sameType(s)) {
					return null;
				}
				Queue<SimpleASTNode> queue1 = new LinkedList<SimpleASTNode>();
				List<Queue<SimpleASTNode>> queue2s = new ArrayList<Queue<SimpleASTNode>>();
				List<Integer> childCounts = null;
				List<String> nodeStrs = null;
				Queue<SimpleASTNode> queue3 = new LinkedList<SimpleASTNode>();
				Queue<SimpleASTNode> queue2 = null;
				queue1.add(s1);
				for (int k = 0; k < s2s.size(); k++) {
					queue2 = new LinkedList<SimpleASTNode>();
					queue2.add(s2s.get(k));
					queue2s.add(queue2);
				}
				queue3.add(s);
				while (!queue1.isEmpty()) {
					node1 = queue1.remove();
					node2s = new ArrayList<SimpleASTNode>();
					childCounts = new ArrayList<Integer>();
					nodeStrs = new ArrayList<String>();
					for (int k = 0; k < queue2s.size(); k++) {
						node2 = queue2s.get(k).remove();
						node2s.add(node2);
						childCounts.add(node2.getChildCount());
						nodeStrs.add(node2.getStrValue());
					}
					node = queue3.remove();
					if (!integerComparator.hasAllEqualTo(node1.getChildCount(),
							childCounts)) {
						if (node1.getStrValue().equals(
								SimpleASTNode.LIST_LITERAL)
								&& nameComparator.hasAllEqualTo(
										SimpleASTNode.LIST_LITERAL, nodeStrs)) {
							StringBuffer buffer1 = new StringBuffer(
									ASTExpressionTransformer.ARGS_PRE);
							StringBuffer buffer2 = null;
							List<String> rStrs = new ArrayList<String>();
							List<Integer> rNodeTypes = new ArrayList<Integer>();
							for (int k = 0; k < node1.getChildCount(); k++) {
								buffer1.append(node1.getChildAt(k));
							}
							for (int l = 0; l < node2s.size(); l++) {
								node2 = node2s.get(l);
								buffer2 = new StringBuffer(
										ASTExpressionTransformer.ARGS_PRE);
								for (int k = 0; k < node2.getChildCount(); k++) {
									buffer2.append(node2.getChildAt(k));
								}
								rStrs.add(buffer2.toString());
								rNodeTypes.add(node2.getNodeType());
							}
							SimpleASTNode unifiedNode = null;
							String abstractName = getAbstractSpecialIdentifier(
									ASTExpressionTransformer.LIST_LITERAL,
									rNodeTypes, buffer1.toString(), rStrs,
									ASTExpressionTransformer.ABSTRACT_UNKNOWN);
							unifiedNode = new SimpleASTNode(-1, abstractName,
									0, 0);
							s1Tou.put(buffer1.toString(), abstractName);
							for (int k = 0; k < node2s.size(); k++) {
								s2Tous.get(k).put(rStrs.get(k), abstractName);
							}
							unifiedNode.setMarked();
							node.add(unifiedNode);
							node.setRecalc();
						}
						continue;
					}
					childEnum1 = node1.children();
					childEnum2s = new ArrayList<Enumeration<SimpleASTNode>>();
					for (int k = 0; k < node2s.size(); k++) {
						childEnum2s.add(node2s.get(k).children());
					}
					while (childEnum1.hasMoreElements()) {
						child1 = childEnum1.nextElement();
						child2s = new ArrayList<SimpleASTNode>();
						for (int k = 0; k < node2s.size(); k++) {
							child2s.add(childEnum2s.get(k).nextElement());
						}
						commonPart = parseCommon(child1, child2s);
						if (sameType(commonPart)) {
							if (commonPart.getStrValue().equals(
									SimpleASTNode.LIST_LITERAL)) {
								commonPart.setMarked();
							}
							queue1.add(child1);
							for (int k = 0; k < child2s.size(); k++) {
								queue2s.get(k).add(child2s.get(k));
							}
							queue3.add(commonPart);
						}
						if (commonPart.hasMark()) {
							node.setRecalc();
						}
						node.add(commonPart);
					}
				}
				Enumeration<SimpleASTNode> dEnum = s.depthFirstEnumeration();
				Set<SimpleASTNode> cached = new HashSet<SimpleASTNode>();
				while (dEnum.hasMoreElements()) {
					node = dEnum.nextElement();
					if (node.hasMark()) {
						nodeParent = (SimpleASTNode) node.getParent();
						if (nodeParent != null
								&& cached.add(nodeParent)
								&& !nodeParent.getStrValue().equals(
										SimpleASTNode.LIST_LITERAL)) {
							nodeParent.constructStrValue(s1Tou, s2Tous);
						}
						node.clearMarked();
					}
				}
				result.add(s);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private boolean isConsistentMap(List<SimpleASTNode> simpleASTNodes1,
			List<List<SimpleASTNode>> simpleASTNodes2List) {
		SimpleASTNode sNode1 = null;
		String strValue1 = null;
		SimpleASTNode sNode2 = null;
		for (int i = 0; i < simpleASTNodes1.size(); i++) {
			sNode1 = simpleASTNodes1.get(i);
			strValue1 = sNode1.getStrValue();
			for (int j = 0; j < simpleASTNodes2List.size(); j++) {
				sNode2 = simpleASTNodes2List.get(j).get(i);
				if (!sNode2.getStrValue().equals(strValue1)) {
					return false;
				}
			}
		}
		return true;
	}

	private SimpleASTNode parseCommon(SimpleASTNode left,
			List<SimpleASTNode> rights) throws MappingException {
		SimpleASTNode right = null, commonPart = (SimpleASTNode) left.clone();
		String lName = null, uniName = null;
		List<String> rNames = new ArrayList<String>();
		List<TermType> rTermTypes = new ArrayList<TermType>();
		Term term = null;
		Term lTerm = left.getTerm();
		List<Term> rTerms = new ArrayList<Term>();
		List<Integer> rNodeTypes = new ArrayList<Integer>();
		for (int j = 0; j < rights.size(); j++) {
			right = rights.get(j);
			rTerms.add(right.getTerm());
			rNodeTypes.add(right.getNodeType());
		}
		boolean allNonnull = termComparator.hasAllNotEqualTo2(null, rTerms);
		if (allNonnull && lTerm != null) {
			lName = lTerm.getName();
			rNames = getNames(rTerms);
			rTermTypes = getTermTypes(rTerms);
			System.out.print("");
			if (typeComparator.hasAllEqualTo(lTerm.getTermType(), rTermTypes)) {
				term = (Term) lTerm.clone();
				uniName = term.getName();
				if (!nameComparator.hasAllEqualTo(lName, rNames)
						|| Term.ExactAbsPattern.matcher(uniName).matches()) {
					switch (term.getTermType()) {
					case VariableTypeBindingTerm:
						// System.out.print("");
						uniName = getAbstractIdentifier(lName, rNames,
								ASTExpressionTransformer.ABSTRACT_VARIABLE);
						break;
					case MethodNameTerm:
						uniName = getAbstractIdentifier(lName, rNames,
								ASTExpressionTransformer.ABSTRACT_METHOD);
						break;
					case TypeNameTerm:
						uniName = getAbstractIdentifier(lName, rNames,
								ASTExpressionTransformer.ABSTRACT_TYPE);
						break;
					case Term:
						uniName = getAbstractSpecialIdentifier(
								lTerm.getNodeType(), rNodeTypes, lName, rNames,
								ASTExpressionTransformer.ABSTRACT_UNKNOWN);
						break;
					}
					term.setName(uniName);
					commonPart.setGeneral();
					commonPart.setMarked();
					commonPart.setStrValue(uniName);
					s1Tou.put(lName, uniName);
					addEntryS2Tous(rNames, uniName);
				}
				if (term instanceof VariableTypeBindingTerm) {
					TypeNameTerm ltTerm = ((VariableTypeBindingTerm) lTerm)
							.getTypeNameTerm();
					rNames = new ArrayList<String>();
					TypeNameTerm rtTerm = null;
					TypeNameTerm ttTerm = ((VariableTypeBindingTerm) term)
							.getTypeNameTerm();
					for (int j = 0; j < rTerms.size(); j++) {
						rtTerm = ((VariableTypeBindingTerm) rTerms.get(j))
								.getTypeNameTerm();
						rNames.add(rtTerm.getName());
					}
					lName = ltTerm.getName();
					if (nameComparator.hasAllEqualTo(lName, rNames)) {
						getAbstractIdentifier(lName, rNames);
					} else {
						uniName = getAbstractIdentifier(lName, rNames,
								ASTExpressionTransformer.ABSTRACT_TYPE);
						s1Tou.put(lName, uniName);
						addEntryS2Tous(rNames, uniName);
						ttTerm.setName(uniName);
					}
				}
			} else {
				uniName = getAbstractIdentifier(lName, rNames,
						ASTExpressionTransformer.ABSTRACT_UNKNOWN);
				term = new Term(left.getNodeType(), getAbstractIdentifier(
						lName, rNames, uniName));
				commonPart.setGeneral();
				commonPart.setMarked();
				commonPart.setStrValue(uniName);
				commonPart.setTerm(null);
				s1Tou.put(lName, uniName);
				addEntryS2Tous(rNames, uniName);
			}
			commonPart.setTerm(term);
		} else {// one ore more of the nodes do not have terms
			lName = left.getStrValue();
			rNames = new ArrayList<String>();
			commonPart.setTerm(null);
			for (int j = 0; j < rights.size(); j++) {
				right = rights.get(j);
				rNames.add(right.getStrValue());
			}
			if (!nameComparator.hasAllEqualTo(lName, rNames)) {
				uniName = getAbstractSpecialIdentifier(left.getNodeType(),
						rNodeTypes, lName, rNames,
						ASTExpressionTransformer.ABSTRACT_UNKNOWN);
				commonPart.setGeneral();
				commonPart.setMarked();
				commonPart.setStrValue(uniName);
				s1Tou.put(lName, uniName);
				addEntryS2Tous(rNames, uniName);
			}
		}
		return commonPart;
	}

	private List<SimpleASTNode> parseCommon(
			List<SimpleASTNode> simpleASTNodes1,
			List<List<SimpleASTNode>> simpleASTNodes2List)
			throws MappingException {
		List<SimpleASTNode> result = new ArrayList<SimpleASTNode>();
		List<Integer> rNodeTypes = null;
		SimpleASTNode left = null, commonPart = null, right = null;
		String lName = null, uniName = null;
		Term term = null, lTerm = null;
		List<TermType> rTermTypes = null;
		List<Term> rTerms = null;
		List<String> rNames = null;

		for (int i = 0; i < simpleASTNodes1.size(); i++) {
			left = simpleASTNodes1.get(i);
			commonPart = (SimpleASTNode) left.clone();
			lTerm = left.getTerm();
			rTerms = new ArrayList<Term>();
			rNodeTypes = new ArrayList<Integer>();
			for (int j = 0; j < simpleASTNodes2List.size(); j++) {
				right = simpleASTNodes2List.get(j).get(i);
				rTerms.add(right.getTerm());
				rNodeTypes.add(right.getNodeType());
			}
			boolean allNonnull = termComparator.hasAllNotEqualTo2(null, rTerms);
			if (allNonnull && lTerm != null) {
				lName = lTerm.getName();
				rNames = getNames(rTerms);
				rTermTypes = getTermTypes(rTerms);
				if (typeComparator.hasAllEqualTo(lTerm.getTermType(),
						rTermTypes)) {
					term = (Term) lTerm.clone();
					uniName = term.getName();
					if (!nameComparator.hasAllEqualTo(lName, rNames)
							|| Term.ExactAbsPattern.matcher(uniName).matches()) {
						switch (term.getTermType()) {
						case VariableTypeBindingTerm:
							uniName = getAbstractIdentifier(lName, rNames,
									ASTExpressionTransformer.ABSTRACT_VARIABLE);
							break;
						case MethodNameTerm:
							uniName = getAbstractIdentifier(lName, rNames,
									ASTExpressionTransformer.ABSTRACT_METHOD);
							break;
						case TypeNameTerm:
							uniName = getAbstractIdentifier(lName, rNames,
									ASTExpressionTransformer.ABSTRACT_TYPE);
							break;
						case Term:
							uniName = getAbstractSpecialIdentifier(
									lTerm.getNodeType(), rNodeTypes, lName,
									rNames,
									ASTExpressionTransformer.ABSTRACT_UNKNOWN);
							break;
						}
						term.setName(uniName);
						commonPart.setGeneral();
						commonPart.setMarked();
						commonPart.setStrValue(uniName);
						s1Tou.put(lName, uniName);
						addEntryS2Tous(rNames, uniName);
					}
					if (term instanceof VariableTypeBindingTerm) {
						TypeNameTerm ltTerm = ((VariableTypeBindingTerm) lTerm)
								.getTypeNameTerm();
						rNames = new ArrayList<String>();
						TypeNameTerm rtTerm = null;
						TypeNameTerm ttTerm = ((VariableTypeBindingTerm) term)
								.getTypeNameTerm();
						for (int j = 0; j < rTerms.size(); j++) {
							rtTerm = ((VariableTypeBindingTerm) rTerms.get(j))
									.getTypeNameTerm();
							rNames.add(rtTerm.getName());
						}
						lName = ltTerm.getName();
						if (nameComparator.hasAllEqualTo(lName, rNames)) {
							getAbstractIdentifier(lName, rNames);
						} else {
							uniName = getAbstractIdentifier(lName, rNames,
									ASTExpressionTransformer.ABSTRACT_TYPE);
							s1Tou.put(lName, uniName);
							addEntryS2Tous(rNames, uniName);
							ttTerm.setName(uniName);
						}
					}
				} else {
					uniName = getAbstractIdentifier(lName, rNames,
							ASTExpressionTransformer.ABSTRACT_UNKNOWN);
					term = new Term(left.getNodeType(), getAbstractIdentifier(
							lName, rNames, uniName));
					commonPart.setGeneral();
					commonPart.setMarked();
					commonPart.setStrValue(uniName);
					commonPart.setTerm(null);
					s1Tou.put(lName, uniName);
					addEntryS2Tous(rNames, uniName);
				}
				commonPart.setTerm(term);
			} else {// one ore more of the nodes do not have terms
				lName = left.getStrValue();
				rNames = new ArrayList<String>();

				for (int j = 0; j < simpleASTNodes2List.size(); j++) {
					right = simpleASTNodes2List.get(j).get(i);
					rNames.add(right.getStrValue());
				}
				if (nameComparator.hasAllNotEqualTo(lName, rNames)) {
					uniName = getAbstractSpecialIdentifier(left.getNodeType(),
							rNodeTypes, lName, rNames,
							ASTExpressionTransformer.ABSTRACT_UNKNOWN);
					commonPart.setGeneral();
					commonPart.setMarked();
					commonPart.setStrValue(uniName);
					s1Tou.put(lName, uniName);
					addEntryS2Tous(rNames, uniName);
				}
			}
			result.add(commonPart);
		}
		return result;
	}

	private List<String> getNames(List<Term> rTerms) {
		List<String> result = new ArrayList<String>();
		for (Term term : rTerms) {
			result.add(term.getName());
		}
		return result;
	}

	private List<TermType> getTermTypes(List<Term> rTerms) {
		List<TermType> termTypes = new ArrayList<TermType>();
		for (Term term : rTerms) {
			termTypes.add(term.getTermType());
		}
		return termTypes;
	}

	private boolean sameType(SimpleASTNode commonTree) {
		return commonTree.getNodeType() != SimpleASTNode.UNDECIDED_NODE_TYPE;
	}

	private boolean sameType(List<SimpleASTNode> simpleASTNodes) {
		boolean hasSameType = true;
		for (int i = 0; i < simpleASTNodes.size(); i++) {
			if (simpleASTNodes.get(i).getNodeType() == SimpleASTNode.UNDECIDED_NODE_TYPE) {
				hasSameType = false;
				break;
			}
		}
		return hasSameType;
	}

	private void setMap(List<Map<String, String>> specificToUnifiedList) {
		Map<String, String> tmpMap = null;
		s1Tou = new HashMap<String, String>(specificToUnifiedList.get(0));
		s2Tous = new ArrayList<Map<String, String>>();
		for (int i = 1; i < specificToUnifiedList.size(); i++) {
			tmpMap = new HashMap<String, String>(specificToUnifiedList.get(i));
			s2Tous.add(tmpMap);
		}
		IdMapper.calcMaxAbsCounter(s1Tou.values());

		abstractMethod = IdMapper.abstractMethod + 1;
		abstractVariable = IdMapper.abstractVariable + 1;
		abstractType = IdMapper.abstractType + 1;
		abstractUnknown = IdMapper.abstractUnknown + 1;
	}

}
