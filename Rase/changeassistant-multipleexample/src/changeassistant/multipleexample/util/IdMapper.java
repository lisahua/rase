package changeassistant.multipleexample.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;

import changeassistant.changesuggestion.expression.representation.ASTExpressionTransformer;
import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;

public class IdMapper {

	public static int LEFT = 1;
	public static int RIGHT = 2;

	public static int abstractMethod = -1, abstractType = -1,
			abstractVariable = -1, abstractUnknown = -1;

	public static void calcMaxAbsCounter(Collection<String> names) {
		int tmpIndex = -1;
		abstractMethod = abstractType = abstractVariable = abstractUnknown = -1;
		System.out.print("");
		for (String uniName : names) {
			if (Term.ExactAbsPattern.matcher(uniName).matches()) {
				tmpIndex = Term.parseInt(uniName);
				if (uniName
						.startsWith(ASTExpressionTransformer.ABSTRACT_VARIABLE)) {
					if (tmpIndex > abstractVariable)
						abstractVariable = tmpIndex;
				} else if (uniName
						.startsWith(ASTExpressionTransformer.ABSTRACT_METHOD)) {
					if (tmpIndex > abstractMethod)
						abstractMethod = tmpIndex;
				} else if (uniName
						.startsWith(ASTExpressionTransformer.ABSTRACT_TYPE)) {
					if (tmpIndex > abstractType)
						abstractType = tmpIndex;
				} else {// abstract unknown
					if (tmpIndex > abstractUnknown)
						abstractUnknown = tmpIndex;
				}
			}
		}
	}

	/**
	 * @param leftAnchor
	 * @param rightAnchor
	 * @param leftMapList
	 * @param rightMapList
	 */
	public static void integrate(int left, int right, int anchor,
			List<Integer> lInsts, List<Integer> rInsts, List<Integer> insts,
			List<Map<String, String>> leftMapList,
			List<Map<String, String>> rightMapList,
			List<Map<String, String>> mapList) {
		// for the same instance, find the difference between them
		Map<String, String> leftMap = leftMapList.get(left);
		Map<String, String> rightMap = rightMapList.get(right);
		if (leftMap.equals(rightMap))// if the two maps are the same, there is
										// no need to integrate them any more
			return;
		String key1 = null, value1 = null, key2 = null, value2 = null;
		Set<String> tmpSet1 = null, tmpSet2 = null, tmpSet = null;

		Map<String, String> map = mapList.get(anchor);
		Map<String, String> u_entries = new HashMap<String, String>();
		for (Entry<String, String> entry : map.entrySet()) {
			value1 = entry.getValue();
			if (Term.U_Pattern.matcher(value1).matches()) {
				u_entries.put(entry.getKey(), value1);
			}
		}

		// to record all entries to insert in the smaller map
		Map<String, String> entriesToInsert1 = new HashMap<String, String>();// left
																				// entries
																				// to
																				// update
																				// by
																				// learning
																				// from
																				// right
		// to record all entries to insert in the larger map
		Map<String, String> entriesToInsert2 = new HashMap<String, String>();// right
																				// entries
																				// to
																				// update
																				// by
																				// learning
																				// from
																				// left
		// to record all entries to update in both map
		List<String> namesToUpdate1 = new ArrayList<String>();
		List<String> namesToUpdate2 = new ArrayList<String>();
		List<String> namesToUpdate = null;
		List<String> newNames = new ArrayList<String>();

		for (Entry<String, String> entry : leftMap.entrySet()) {
			key1 = entry.getKey();
			value1 = entry.getValue();
			value2 = map.get(key1);
			if (value2 == null) {// the smaller map does not contain a
									// corresponding entry for the larger one
				addEntry(entriesToInsert1, key1, value1, u_entries);
			} else if (!value1.equals(value2)) {// value1 and value2 at least
												// have one abstract
				namesToUpdate1.add(value1);
				namesToUpdate2.add(value2);
				if (Term.ExactAbsPattern.matcher(value1).matches()) {
					newNames.add(value1);
				} else {
					newNames.add(value2);
				}
			}
		}
		for (Entry<String, String> entry : rightMap.entrySet()) {
			key1 = entry.getKey();
			value1 = entry.getValue();
			value2 = map.get(key1);
			if (value2 == null) {
				if (leftMap.containsKey(key1))
					continue;
				addEntry(entriesToInsert2, key1, value1, u_entries);
			}
		}

		if (!entriesToInsert1.isEmpty() || !entriesToInsert2.isEmpty()
				|| !namesToUpdate1.isEmpty()) {
			calcMaxAbsCounter(map.values());
			abstractMethod++;
			abstractType++;
			abstractVariable++;
			abstractUnknown++;

			updateEntries(entriesToInsert1);
			updateEntries(entriesToInsert2);
			updateEntries(newNames);

			if (!entriesToInsert1.isEmpty()) {
				for (int i = 0; i < rInsts.size(); i++) {
					map = mapList.get(insts.indexOf(rInsts.get(i)));
					if (map.containsKey(entriesToInsert1.keySet().iterator()
							.next())) {
						continue;// this map already contains all entries we
									// want to insert
					} else {
						for (Entry<String, String> entry : entriesToInsert1
								.entrySet()) {
							map.put(entry.getKey(), entry.getValue());
						}
					}
				}
			}
			if (!entriesToInsert2.isEmpty()) {
				for (int i = 0; i < lInsts.size(); i++) {
					map = mapList.get(insts.indexOf(lInsts.get(i)));
					if (map.containsKey(entriesToInsert2.keySet().iterator()
							.next())) {
						continue;// this map already contains all entries we
									// want to insert
					} else {
						for (Entry<String, String> entry : entriesToInsert2
								.entrySet()) {
							map.put(entry.getKey(), entry.getValue());
						}
					}
				}
			}
			if (!namesToUpdate1.isEmpty()) {
				int counter = 0;
				int index = -1;
				for (int i = 0; i < insts.size(); i++) {
					map = mapList.get(i);
					tmpSet = new HashSet<String>(map.values());
					tmpSet.retainAll(namesToUpdate1);
					if (tmpSet.isEmpty()) {
						namesToUpdate = namesToUpdate2;
					} else {
						namesToUpdate = namesToUpdate1;
					}
					counter = 0;
					for (Entry<String, String> entry : map.entrySet()) {
						index = namesToUpdate.indexOf(entry.getValue());
						if (index != -1) {
							entry.setValue(newNames.get(index));
							counter++;
							if (counter == namesToUpdate.size())
								break;
						}
					}
				}
			}
		}
	}

	/**
	 * A potential problem: this simple heuristic cannot handle complex relation
	 * between two u_values. For instance, if a u_value is pretty small and the
	 * other one is pretty large, they may have overlap literally, but they may
	 * not actually talking about the same thing.
	 * 
	 * @param entriesToInsert
	 * @param key
	 * @param value
	 * @param u_entries
	 */
	private static void addEntry(Map<String, String> entriesToInsert,
			String key, String value, Map<String, String> u_entries) {
		if (u_entries.isEmpty() || !Term.U_Pattern.matcher(value).matches()) {
			// do nothing
		} else {
			String tmpKey = null;
			for (Entry<String, String> entry : u_entries.entrySet()) {
				tmpKey = entry.getKey();
				if (tmpKey.contains(key)) {
					return; // do not insert the entry since it is overlapped by
							// some larger u_value
				}
			}
		}
		entriesToInsert.put(key, value);
	}

	public static boolean isConsistentMap(List<SimpleASTNode> simpleASTNodes1,
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

	private static void updateEntries(List<String> names) {
		for (int i = 0; i < names.size(); i++) {
			names.set(i, createNewName(names.get(i)));
		}
	}

	private static void updateEntries(Map<String, String> entries) {
		for (Entry<String, String> entry : entries.entrySet()) {
			entry.setValue(createNewName(entry.getValue()));
		}
	}

	/**
	 * create a new abstract name from an old abstract name
	 * 
	 * @param oldName
	 * @return
	 */
	private static String createNewName(String oldName) {
		Matcher matcher = Term.IndexPattern.matcher(oldName);
		StringBuffer buffer = new StringBuffer();
		if (matcher.find()) {// there should be only one index
			matcher.appendReplacement(buffer, Integer.toString(abstractType++));
		}
		matcher.appendTail(buffer);
		// if(oldName.startsWith(ASTExpressionTransformer.ABSTRACT_TYPE)){
		// newName =
		// Term.createAbstractName(ASTExpressionTransformer.ABSTRACT_TYPE,
		// abstractType++);
		// }else
		// if(oldName.startsWith(ASTExpressionTransformer.ABSTRACT_METHOD)){
		// newName =
		// Term.createAbstractName(ASTExpressionTransformer.ABSTRACT_METHOD,
		// abstractMethod++);
		// }else
		// if(oldName.startsWith(ASTExpressionTransformer.ABSTRACT_VARIABLE)){
		// newName =
		// Term.createAbstractName(ASTExpressionTransformer.ABSTRACT_VARIABLE,
		// abstractVariable++);
		// }else{
		// newName =
		// Term.createAbstractName(ASTExpressionTransformer.ABSTRACT_UNKNOWN,
		// abstractUnknown++);
		// }
		return buffer.toString();
	}

	public static Map<String, String> createReverseMap(
			Map<String, String> specificToUnified) {
		Map<String, String> map = new HashMap<String, String>();
		for (Entry<String, String> entry : specificToUnified.entrySet()) {
			map.put(entry.getValue(), entry.getKey());
		}
		return map;
	}
}
