package changeassistant.multipleexample.contextualize;

import java.util.List;

import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.TermsList;
import changeassistant.multipleexample.util.LongestCommonSubsequence;

public class LCSTerms extends LongestCommonSubsequence<List<List<Term>>> {

	@Override
	protected boolean equivalent(List<List<Term>> termsList1,
			List<List<Term>> termsList2, int i, int j) {
		if (termsList1.isEmpty() || termsList2.isEmpty())
			return false;
		return TermsList.isEquivalent(termsList1, termsList2);
	}
}
