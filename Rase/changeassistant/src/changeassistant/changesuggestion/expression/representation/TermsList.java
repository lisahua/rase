package changeassistant.changesuggestion.expression.representation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import changeassistant.changesuggestion.expression.representation.Term.TermType;
import changeassistant.classhierarchy.ClassContext;
import changeassistant.classhierarchy.ProjectResource;

public class TermsList {

	public static int LEVEL_HIGH = 2;

	public static int LEVEL_LOW = 1;

	public static int LEVEL_NONE = 0;

	private static Set<VariableTypeBindingTerm> variableSet;

	public static ProjectResource prLeft = null, prRight = null;

	private static void addBlackMap(
			Map<String, Set<String>> blackIdentifierMap,
			Map<String, Set<String>> blackIdentifierMap2, String name1,
			String name2) {
		Set<String> tmpSet = null;
		tmpSet = blackIdentifierMap.get(name1);
		if (tmpSet == null) {
			tmpSet = new HashSet<String>();
			blackIdentifierMap.put(name1, tmpSet);
		}
		tmpSet.add(name2);
		tmpSet = blackIdentifierMap.get(name2);
		if (tmpSet == null) {
			tmpSet = new HashSet<String>();
			blackIdentifierMap2.put(name2, tmpSet);
		}
		tmpSet.add(name1);
	}

	private static void addSupportingInstance(List<Term> tmpLeftTerms,
			List<Term> tmpRightTerms, Term term1, Term term2,
			List<Set<Integer>> tmpSupportingInsts, Integer inst) {
		int tmpIndex1 = -1;
		Term tmpTerm = null;
		Set<Integer> insts = null;
		for (int k = 0; k < tmpLeftTerms.size(); k++) {
			tmpTerm = tmpLeftTerms.get(k);
			if (tmpTerm.equals(term1) && tmpRightTerms.get(k).equals(term2)) {
				tmpIndex1 = k;
				break;
			}
		}
		if (tmpIndex1 != -1) {
			tmpSupportingInsts.get(tmpIndex1).add(inst);
		} else {
			tmpLeftTerms.add(term1);
			tmpRightTerms.add(term2);
			insts = new HashSet<Integer>();
			insts.add(inst);
			tmpSupportingInsts.add(insts);
		}
	}

	private static void addSupportingInstance(List<Term> tmpLeftTerms,
			List<Term> tmpRightTerms, Term term1, Term term2,
			List<List<Integer>> tmpSupportingInsts1, Integer inst1,
			List<List<Integer>> tmpSupportingInsts2, Integer inst2) {
		int tmpIndex1 = -1;
		Term tmpTerm = null;
		List<Integer> insts = null;
		for (int k = 0; k < tmpLeftTerms.size(); k++) {
			tmpTerm = tmpLeftTerms.get(k);
			if (tmpTerm.equals(term1) && tmpRightTerms.get(k).equals(term2)) {
				tmpIndex1 = k;
				break;
			}
		}
		if (tmpIndex1 != -1) {
			insts = tmpSupportingInsts1.get(tmpIndex1);
			if (!insts.contains(inst1)) {
				tmpSupportingInsts1.get(tmpIndex1).add(inst1);
				tmpSupportingInsts2.get(tmpIndex1).add(inst2);
			}
		} else {
			tmpLeftTerms.add(term1);
			tmpRightTerms.add(term2);
			insts = new ArrayList<Integer>();
			insts.add(inst1);
			tmpSupportingInsts1.add(insts);
			insts = new ArrayList<Integer>();
			insts.add(inst2);
			tmpSupportingInsts2.add(insts);
		}
	}

	public static Map<String, Set<TypeNameTerm>> createTypeTermMap(
			List<List<List<Term>>> termsListList) {
		Map<String, Set<TypeNameTerm>> map = new HashMap<String, Set<TypeNameTerm>>();
		TypeNameTerm tTerm = null;
		Set<TypeNameTerm> tTerms = null;
		String name = null;
		for (List<List<Term>> termsList : termsListList) {
			for (List<Term> terms : termsList) {
				for (Term term : terms) {
					if (term instanceof TypeNameTerm
							|| term instanceof VariableTypeBindingTerm) {
						if (term instanceof TypeNameTerm) {
							name = term.getName();
							tTerm = (TypeNameTerm) term;
						} else if (term instanceof VariableTypeBindingTerm) {
							tTerm = ((VariableTypeBindingTerm) term)
									.getTypeNameTerm();
							name = tTerm.getName();
						}
						if (map.containsKey(name)) {
							tTerms = map.get(name);
							boolean isFound = false;
							for (Term tmpTerm : tTerms) {
								if (tmpTerm.getName().equals(tTerm.getName())) {
									isFound = true;
									break;
								}
							}
							if (!isFound)
								tTerms.add(tTerm);
						} else {
							tTerms = new HashSet<TypeNameTerm>();
							tTerms.add(tTerm);
							map.put(name, tTerms);
						}
					}
				}
			}
		}
		return map;
	}

	/**
	 * Assumption: termsList1.size() == termsList2.size()
	 * 
	 * @param termsList1
	 * @param termsList2
	 * @param basicMap
	 * @param basicMap2
	 * @param typeTermMap1
	 * @param typeTermMap2
	 * @param leftTerms
	 * @param rightTerms
	 * @param inst
	 * @param supportingInsts
	 * @param blackIdentifierMap
	 * @param blackIdentifierMap2
	 * @return
	 */
	public static boolean doMap(List<List<Term>> termsList1,
			List<List<Term>> termsList2, Map<String, String> basicMap,
			Map<String, String> basicMap2,
			Map<String, Set<TypeNameTerm>> typeTermMap1,
			Map<String, Set<TypeNameTerm>> typeTermMap2, List<Term> leftTerms,
			List<Term> rightTerms, int inst,
			List<Set<Integer>> supportingInsts,
			Map<String, Set<String>> blackIdentifierMap,
			Map<String, Set<String>> blackIdentifierMap2) {
		boolean success = true;
		List<Term> terms1 = null, terms2 = null;
		List<Term> tmpLeftTerms = new ArrayList<Term>(leftTerms), tmpRightTerms = new ArrayList<Term>(
				rightTerms);
		List<Set<Integer>> tmpSupportingInsts = new ArrayList<Set<Integer>>();
		for (Set<Integer> insts : supportingInsts) {
			tmpSupportingInsts.add(new HashSet<Integer>(insts));
		}
		String name1 = null, name2 = null, tName1 = null, tName2 = null;
		Term term1 = null, term2 = null, tTerm1 = null, tTerm2 = null;
		TermType t1 = null;
		for (int i = 0; i < termsList1.size(); i++) {
			terms1 = termsList1.get(i);
			terms2 = termsList2.get(i);
			if (terms1.size() != terms2.size()) {// when the terms' sizes are
													// different, do not try to
													// map
				continue;
			}
			for (int j = 0; j < terms1.size(); j++) {
				term1 = terms1.get(j);
				term2 = terms2.get(j);
				if (!term1.getTermType().equals(term2.getTermType())) {
					success = false;
					break;// the two terms have different term types
				}
				t1 = term1.getTermType();
				if (t1.equals(Term.TermType.Term))
					continue; // do not record the mapping between operators
				name1 = term1.getName();
				name2 = term2.getName();
				if (basicMap.containsKey(name1)
						&& !basicMap.get(name1).equals(name2)
						|| basicMap2.containsKey(name2)
						&& !basicMap2.get(name2).equals(name1)) {
					// the second map may have dynamic binding and static
					// binding at the same time
					if (term1.getTermType().equals(Term.TermType.TypeNameTerm)) {
						if (isConflictWithTypeMap(basicMap, basicMap2, name1,
								name2, typeTermMap1, typeTermMap2, term1, term2)) {
							addBlackMap(blackIdentifierMap,
									blackIdentifierMap2, name1, name2);
							success = false;
							break;
						} else {// More process is needed
							continue;
						}
					} else {
						addBlackMap(blackIdentifierMap, blackIdentifierMap2,
								name1, name2);
						success = false;
						break;
					}
				}
				if (t1.equals(Term.TermType.VariableTypeBindingTerm)) {
					tTerm1 = ((VariableTypeBindingTerm) term1)
							.getTypeNameTerm();
					tTerm2 = ((VariableTypeBindingTerm) term2)
							.getTypeNameTerm();
					tName1 = tTerm1.getName();
					tName2 = tTerm2.getName();
					if (basicMap.containsKey(tName1)
							&& !basicMap.get(tName1).equals(tName2)
							|| basicMap2.containsKey(tName2)
							&& !basicMap2.get(tName2).equals(tName1)) {
						if (isConflictWithTypeMap(basicMap, basicMap2, tName1,
								tName2, typeTermMap1, typeTermMap2, tTerm1,
								tTerm2)) {
							addBlackMap(blackIdentifierMap,
									blackIdentifierMap2, name1, name2);
							success = false;
							break;
						} else {
							continue;
						}
					}
					addSupportingInstance(tmpLeftTerms, tmpRightTerms, tTerm1,
							tTerm2, tmpSupportingInsts, inst);
				}
				addSupportingInstance(tmpLeftTerms, tmpRightTerms, term1,
						term2, tmpSupportingInsts, inst);
			}
			if (!success)
				break;
		}
		if (success) {// filter out conflicting mappings
			leftTerms.clear();
			leftTerms.addAll(tmpLeftTerms);
			rightTerms.clear();
			rightTerms.addAll(tmpRightTerms);
			supportingInsts.clear();
			supportingInsts.addAll(tmpSupportingInsts);
		}
		return success;
	}

	public static boolean doMap(List<List<Term>> termsList1,
			List<List<Term>> termsList2, Map<String, String> basicMap,
			Map<String, String> basicMap2,
			Map<String, Set<TypeNameTerm>> typeTermMap1,
			Map<String, Set<TypeNameTerm>> typeTermMap2, List<Term> leftTerms,
			List<Term> rightTerms, int inst1, int inst2,
			List<List<Integer>> supportingInsts1,
			List<List<Integer>> supportingInsts2,
			Map<String, Set<String>> blackIdentifierMap,
			Map<String, Set<String>> blackIdentifierMap2) {
		boolean success = true;
		List<Term> terms1 = null, terms2 = null;
		List<Term> tmpLeftTerms = new ArrayList<Term>(leftTerms), tmpRightTerms = new ArrayList<Term>(
				rightTerms);
		List<List<Integer>> tmpSupportingInsts1 = new ArrayList<List<Integer>>();
		List<List<Integer>> tmpSupportingInsts2 = new ArrayList<List<Integer>>();
		for (int i = 0; i < supportingInsts1.size(); i++) {
			tmpSupportingInsts1.add(new ArrayList<Integer>(supportingInsts1
					.get(i)));
			tmpSupportingInsts2.add(new ArrayList<Integer>(supportingInsts2
					.get(i)));
		}
		String name1 = null, name2 = null, tName1 = null, tName2 = null;
		Term term1 = null, term2 = null, tTerm1 = null, tTerm2 = null;
		TermType t1 = null;
		for (int i = 0; i < termsList1.size(); i++) {
			terms1 = termsList1.get(i);
			terms2 = termsList2.get(i);
			if (terms1.size() != terms2.size()) {// when the terms' sizes are
													// different, do not try to
													// map
				continue;
			}
			for (int j = 0; j < terms1.size(); j++) {
				term1 = terms1.get(j);
				term2 = terms2.get(j);
				if (!term1.getTermType().equals(term2.getTermType())) {
					success = false;
					break;// the two terms have different term types
				}
				t1 = term1.getTermType();
				if (t1.equals(Term.TermType.Term))
					continue; // do not record the mapping between operators
				name1 = term1.getName();
				name2 = term2.getName();
				if (basicMap.containsKey(name1)
						&& !basicMap.get(name1).equals(name2)
						|| basicMap2.containsKey(name2)
						&& !basicMap2.get(name2).equals(name1)) {
					// the second map may have dynamic binding and static
					// binding at the same time
					if (term1.getTermType().equals(Term.TermType.TypeNameTerm)) {
						if (isConflictWithTypeMap(basicMap, basicMap2, name1,
								name2, typeTermMap1, typeTermMap2, term1, term2)) {
							addBlackMap(blackIdentifierMap,
									blackIdentifierMap2, name1, name2);
							success = false;
							break;
						} else {// More process is needed
							continue;
						}
					} else {
						addBlackMap(blackIdentifierMap, blackIdentifierMap2,
								name1, name2);
						success = false;
						break;
					}
				} else {
					// an abstract term is mapped with a concrete term
					// or two different abstract terms are mapped together
					boolean isAbs1 = Term.ExactAbsPattern.matcher(name1)
							.matches();
					boolean isAbs2 = Term.ExactAbsPattern.matcher(name2)
							.matches();
					if (isAbs1 && !isAbs2 || !isAbs1 && isAbs2 || isAbs1
							&& isAbs2 && !name1.equals(name2)) {
						success = false;
						break;
					}
				}
				if (t1.equals(Term.TermType.VariableTypeBindingTerm)) {
					tTerm1 = ((VariableTypeBindingTerm) term1)
							.getTypeNameTerm();
					tTerm2 = ((VariableTypeBindingTerm) term2)
							.getTypeNameTerm();
					tName1 = tTerm1.getName();
					tName2 = tTerm2.getName();
					if (basicMap.containsKey(tName1)
							&& !basicMap.get(tName1).equals(tName2)
							|| basicMap2.containsKey(tName2)
							&& !basicMap2.get(tName2).equals(tName1)) {
						if (isConflictWithTypeMap(basicMap, basicMap2, tName1,
								tName2, typeTermMap1, typeTermMap2, tTerm1,
								tTerm2)) {
							addBlackMap(blackIdentifierMap,
									blackIdentifierMap2, name1, name2);
							success = false;
							break;
						} else {
							continue;
						}
					}
					addSupportingInstance(tmpLeftTerms, tmpRightTerms, tTerm1,
							tTerm2, tmpSupportingInsts1, inst1,
							tmpSupportingInsts2, inst2);
				}
				addSupportingInstance(tmpLeftTerms, tmpRightTerms, term1,
						term2, tmpSupportingInsts1, inst1, tmpSupportingInsts2,
						inst2);
			}
			if (!success)
				break;
		}
		if (success) {// filter out conflicting mappings
			leftTerms.clear();
			leftTerms.addAll(tmpLeftTerms);
			rightTerms.clear();
			rightTerms.addAll(tmpRightTerms);
			supportingInsts1.clear();
			supportingInsts1.addAll(tmpSupportingInsts1);
			supportingInsts2.clear();
			supportingInsts2.addAll(tmpSupportingInsts2);
		}
		return success;
	}

	public static Set<VariableTypeBindingTerm> getVariableSet() {
		return variableSet;
	}

	public static boolean isConflictWithTypeMap(Map<String, String> basicMap,
			Map<String, String> basicMap2, String name1, String name2,
			Map<String, Set<TypeNameTerm>> typeTermMap1,
			Map<String, Set<TypeNameTerm>> typeTermMap2, Term term1, Term term2) {
		boolean isConflict = false;
		String qNameNew = null, qKnown = null;
		List<TypeNameTerm> tKnowns = null;
		if (basicMap.containsKey(name1) && !basicMap.get(name1).equals(name2)) {
			qNameNew = ((TypeNameTerm) term2).getQualifiedName();
			tKnowns = new ArrayList<TypeNameTerm>(typeTermMap2.get(basicMap
					.get(name1)));
		} else {
			qNameNew = ((TypeNameTerm) term1).getQualifiedName();
			Set<TypeNameTerm> tKnownSet = typeTermMap1
					.get(basicMap2.get(name2));
			if (tKnownSet == null) {
				isConflict = true;
				return isConflict;
			}
			tKnowns = new ArrayList<TypeNameTerm>(typeTermMap1.get(basicMap2
					.get(name2)));
		}
		boolean isSubClass = isSubClass(qNameNew, tKnowns);
		if (!isSubClass) {
			isConflict = true;
		}
		return isConflict;
	}

	public static boolean isConflictWithKnownMap(Map<String, String> lTou,
			Map<String, String> rTou, Map<String, String> specificToUnified,
			Map<String, String> unifiedToSpecific,
			Map<String, Set<String>> blackIdentifierMap,
			Map<String, Set<String>> blackIdentifierMap2) {
		boolean isConflict = false;
		if (blackIdentifierMap.isEmpty() && blackIdentifierMap2.isEmpty())
			return isConflict;

		String key = null, value = null;
		System.out.print("");
		Set<String> lInterested = new HashSet<String>(lTou.keySet());
		lInterested.retainAll(blackIdentifierMap.keySet());
		if (!lInterested.isEmpty()) {
			Map<String, String> uTor = new HashMap<String, String>();
			for (Entry<String, String> entry : rTou.entrySet()) {
				uTor.put(entry.getValue(), entry.getKey());
			}
			for (String lInte : lInterested) {
				key = lInte;
				value = uTor.get(lTou.get(key));
				if (blackIdentifierMap.get(key).contains(value)) {
					isConflict = true;
					break;
				}
			}
		}

		if (!isConflict) {
			Set<String> rInterested = new HashSet<String>(rTou.keySet());
			rInterested.retainAll(blackIdentifierMap2.keySet());
			Map<String, String> uTol = new HashMap<String, String>();
			if (!rInterested.isEmpty()) {
				for (String rInte : rInterested) {
					key = rInte;
					value = uTol.get(rTou.get(key));
					if (blackIdentifierMap2.get(key).contains(value)) {
						isConflict = true;
						break;
					}
				}
			}
		}
		return isConflict;
	}

	public static boolean isEquivalent(List<List<Term>> termsList1,
			List<List<Term>> termsList2) {
		if (termsList1.size() != termsList2.size())
			return false;
		boolean flag = true;
		List<Term> terms1, terms2;
		Term term1, term2;
		for (int i = 0; i < termsList1.size(); i++) {
			terms1 = termsList1.get(i);
			terms2 = termsList2.get(i);
			if (terms1.size() != terms2.size())
				return false;
			for (int j = 0; j < terms1.size(); j++) {
				term1 = terms1.get(j);
				term2 = terms2.get(j);
				if (!term1.isEquivalent(term2)) {
					return false;
				}
			}
		}
		return flag;
	}

	public static boolean isSubClass(String subname, List<TypeNameTerm> terms) {
		return isSubClass(subname, terms, prLeft)
				|| isSubClass(subname, terms, prRight);
	}

	protected static boolean isSubClass(String subname,
			List<TypeNameTerm> terms, ProjectResource pr) {
		boolean flag = true;
		String qKnown = null;
		ClassContext cc = pr.findClassContext(subname);
		if (cc == null) {
			return false;
		}
		for (TypeNameTerm tKnown : terms) {
			qKnown = tKnown.getQualifiedName();
			// System.out.print("");
			if (!cc.isImplementationOf(qKnown) && !cc.isSubClassOf(qKnown)) {
				flag = false;
				break;
			}
		}
		return flag;
	}

	public static int isTooGeneral(List<List<Term>> termsList) {
		variableSet = new HashSet<VariableTypeBindingTerm>();
		int level = LEVEL_HIGH;
		List<Term> terms;
		Term term;
		String name;
		for (int i = 0; i < termsList.size(); i++) {
			terms = termsList.get(i);
			for (int j = 0; j < terms.size(); j++) {
				term = terms.get(j);
				name = term.getName();
				if (term.getTermType().equals(Term.TermType.Term))
					continue;
				if (name.length() != 3
						|| (!name.equals("T_x") && !name.equals("M_x") && !name
								.equals("V_x"))) {
					level = LEVEL_NONE;
					break;
				}
				if (term instanceof VariableTypeBindingTerm) {
					variableSet.add((VariableTypeBindingTerm) term);
				}
			}
		}
		return level;
	}
}
