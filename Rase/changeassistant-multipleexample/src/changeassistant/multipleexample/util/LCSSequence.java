package changeassistant.multipleexample.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import changeassistant.multipleexample.contextualize.datastructure.Sequence;
import changeassistant.multipleexample.datastructure.Pair;

public class LCSSequence {

	protected static Map<String, Pair<Sequence>> dictionary = null;
	protected static Map<String, Sequence> dictionary2 = null;

	protected static double[][] matchMatrix = null;

	// protected static boolean checkMatched(Map<Integer, Integer> matchSet,
	// Sequence s1, Sequence s2) {
	// return (matchMatrix[s1.get(0) - 1][s2.get(0) - 1] == 1)
	// && ((matchSet.containsKey(s1.get(0)) && matchSet.get(s1.get(0)) == s2
	// .get(0)) || !matchSet.containsValue(s2.get(0)));
	// }

	public static int computeLCSSequence(Sequence s1, Sequence s2, Sequence part) {
		int max = 0;
		String digest = s1.toString() + "--" + s2.toString();
		if (dictionary2.containsKey(digest)) {
			part.add(dictionary2.get(digest));
			max = part.size();
		} else {
			if (s1.isEmpty() || s2.isEmpty()) {
				// do nothing
			} else if (s1.size() == 1 && s2.size() == 1
					&& s1.get(0) == s2.get(0)) {
				max = 1;
				part.add(s1);
			} else {
				int[] lcs = new int[3];
				List<Sequence> results = new ArrayList<Sequence>();
				Sequence result = null;
				for (int i = 0; i < 3; i++) {
					lcs[i] = 0;
					results.add(new Sequence(new ArrayList<Integer>()));
				}
				if (s1.get(0) == s2.get(0)) {
					result = results.get(0);
					lcs[0] = computeLCSSequence(s1.head(), s2.head(), result);
					result.append(s1.get(0));
					lcs[0]++;
					lcs[0] += computeLCSSequence(s1.tail(), s2.tail(), result);
				}
				lcs[1] = computeLCSSequence(s1.head().concate(s1.tail()), s2,
						results.get(1));
				lcs[2] = computeLCSSequence(s1, s2.head().concate(s2.tail()),
						results.get(2));
				max = lcs[0];
				int maxIndex = 0;
				for (int i = 1; i < 3; i++) {
					if (lcs[i] > max) {
						max = lcs[i];
						maxIndex = i;
					}
				}
				result = results.get(maxIndex);
				part.add(result);
				dictionary2.put(digest, result);
			}
		}
		return max;
	}

	public static int computeLCSSequence(Sequence s1, Sequence s2,
			Sequence part1, Sequence part2, Map<Integer, Integer> matchSet) {
		int max = 0;
		String digest = s1.toString() + "--" + s2.toString();
		Pair<Sequence> pair = null;

		System.out.print("");
		if (dictionary.containsKey(digest)) {
			pair = dictionary.get(digest);
			part1.add(pair.getLeft());
			part2.add(pair.getRight());
			max = pair.getLeft().size();
		} else if (s1.isEmpty() || s2.isEmpty()) {
			// do nothing
		} else {
			if (s1.size() == 1 && s2.size() == 1
					&& matchMatrix[s1.get(0) - 1][s2.get(0) - 1] == 1) {
				max = 1;
				part1.add(s1);
				part2.add(s2);
				pair = new Pair<Sequence>(s1, s2);
			} else {
				List<Pair<Sequence>> pairResults = new ArrayList<Pair<Sequence>>();
				Sequence result1, result2;
				for (int i = 0; i < 3; i++) {
					result1 = new Sequence(new ArrayList<Integer>());
					result2 = new Sequence(new ArrayList<Integer>());
					pairResults.add(new Pair<Sequence>(result1, result2));
				}
				int[] lcs = new int[3];
				for (int i = 0; i < 3; i++)
					lcs[i] = 0;
				if ((matchMatrix[s1.get(0) - 1][s2.get(0) - 1] == 1)
						&& ((matchSet.containsKey(s1.get(0)) && matchSet.get(s1
								.get(0)) == s2.get(0)) || !matchSet
								.containsValue(s2.get(0)))) {

					result1 = pairResults.get(0).getLeft();
					result2 = pairResults.get(0).getRight();
					lcs[0] = computeLCSSequence(s1.head(), s2.head(), result1,
							result2, matchSet);
					result1.append(s1.get(0));
					result2.append(s2.get(0));
					lcs[0]++;
					lcs[0] += computeLCSSequence(s1.tail(), s2.tail(), result1,
							result2, matchSet);
				}
				lcs[1] = computeLCSSequence(s1.head().concate(s1.tail()), s2,
						pairResults.get(1).getLeft(), pairResults.get(1)
								.getRight(), matchSet);
				lcs[2] = computeLCSSequence(s1, s2.head().concate(s2.tail()),
						pairResults.get(2).getLeft(), pairResults.get(2)
								.getRight(), matchSet);
				max = lcs[0];
				int maxIndex = 0;
				for (int i = 1; i < 3; i++) {
					if (lcs[i] > max) {
						max = lcs[i];
						maxIndex = i;
					}
				}
				pair = pairResults.get(maxIndex);
				part1.add(pair.getLeft());
				part2.add(pair.getRight());
			}
			dictionary.put(digest, pair);
		}
		return max;
	}
}
