package changeassistant.multipleexample.contextualize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.TermsList;
import changeassistant.changesuggestion.expression.representation.TypeNameTerm;
import changeassistant.multipleexample.common.CommonParser;
import changeassistant.multipleexample.partition.MappingException;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;

public class BlackMapChecker {

	public static void filterConflict(List<Term> leftTerms,
			List<Term> rightTerms, List<Set<Integer>> supportingInsts) {
		int i = 0;
		Term tmpTerm = null, term1 = null, term2 = null;
		List<Term> terms1 = null, terms2 = null;
		List<Set<Integer>> supportingInsts2 = null;
		Set<Integer> insts = null;
		Set<Integer> uselessInsts = new HashSet<Integer>();
		while (i < leftTerms.size()) {
			tmpTerm = leftTerms.get(i);
			if (leftTerms.subList(i + 1, leftTerms.size()).contains(tmpTerm)) {// tmpTerm
																				// is
																				// a
																				// repetitive
																				// term
				Map<Integer, Integer> candisInsts = new HashMap<Integer, Integer>();
				candisInsts.put(i, supportingInsts.get(i).size());
				for (int j = i + 1; j < leftTerms.size(); j++) {
					term1 = leftTerms.get(j);
					if (term1.equals(tmpTerm)) {
						candisInsts.put(j, supportingInsts.get(j).size());
					}
				}
				int maxValue = -1, maxKey = -1, key = -1, value = -1;
				for (Entry<Integer, Integer> entry : candisInsts.entrySet()) {
					key = entry.getKey();
					value = entry.getValue();
					if (value > maxValue) {
						maxKey = key;
						maxValue = value;
					}
				}
				candisInsts.remove(maxKey);
				terms1 = new ArrayList<Term>();
				terms2 = new ArrayList<Term>();
				supportingInsts2 = new ArrayList<Set<Integer>>();
				for (int k = 0; k < leftTerms.size(); k++) {
					if (!candisInsts.containsKey(k)) {
						terms1.add(leftTerms.get(k));
						supportingInsts2.add(supportingInsts.get(k));
					} else {
						uselessInsts.addAll(supportingInsts.get(k));
					}
				}
				for (int k = 0; k < supportingInsts2.size(); k++) {
					insts = supportingInsts2.get(k);
					insts.removeAll(uselessInsts);
				}
				leftTerms = terms1;
				rightTerms = terms2;
				supportingInsts = supportingInsts2;
			} else {
				i++;
			}
		}
	}

	public static boolean isBlackMap(List<List<Term>> termsList1,
			List<List<Term>> termsList2, List<SimpleASTNode> sNodes1,
			List<SimpleASTNode> sNodes2,
			Map<String, String> specificToUnified1,
			Map<String, String> unifiedToSpecific,
			Map<String, String> specificToUnified2,
			Map<String, String> basicMap, Map<String, String> basicMap2,
			Map<String, Set<TypeNameTerm>> typeTermMap1,
			Map<String, Set<TypeNameTerm>> typeTermMap2, List<Term> leftTerms,
			List<Term> rightTerms, int inst,
			List<Set<Integer>> supportingInsts,
			Map<String, Set<String>> blackIdentifierMap,
			Map<String, Set<String>> blackIdentifierMap2)
			throws MappingException {
		CommonParser parser = new CommonParser();
		boolean hasEqualSize = true;
		if (termsList1.size() != termsList2.size())
			hasEqualSize = false;
		if (hasEqualSize) {
			for (int j = 0; j < termsList1.size(); j++) {
				if (termsList1.get(j).size() != termsList2.get(j).size()) {
					hasEqualSize = false;
					break;
				}
			}
		}
		if (!hasEqualSize) {
			Map<String, String> interSpecificToUnified1 = new HashMap<String, String>(
					specificToUnified1);
			Map<String, String> interSpecificToUnified2 = new HashMap<String, String>(
					specificToUnified2);
			parser.setMap(interSpecificToUnified1, interSpecificToUnified2);

			if (sNodes1.size() == sNodes2.size()) {
				for (int j = 0; j < sNodes1.size(); j++) {
					parser.getCommon(sNodes1.get(j), sNodes2.get(j));
				}
			}
			if (!interSpecificToUnified2.equals(specificToUnified2)) {
				for (Entry<String, String> entry : interSpecificToUnified2
						.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();
					if (!specificToUnified2.containsKey(key)) {
						specificToUnified2.put(key, value);
						unifiedToSpecific.put(value, key);
					}
				}
			}
		} else if (!TermsList.doMap(termsList1, termsList2, basicMap,
				basicMap2, typeTermMap1, typeTermMap2, leftTerms, rightTerms,
				inst, supportingInsts, blackIdentifierMap, blackIdentifierMap2)) {
			return true;
		}
		return false;
	}
}
