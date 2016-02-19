package changeassistant.multipleexample.match;

import java.util.List;
import java.util.regex.Pattern;

import changeassistant.multipleexample.util.LongestCommonSubsequence;

public class LCSStatements extends LongestCommonSubsequence<String> {

	private List<Pattern> pats;

	public LCSStatements(List<Pattern> pats) {
		this.pats = pats;
	}

	@Override
	protected boolean equivalent(String s1, String s2, int i, int j) {
		Pattern pat = Pattern.compile(s2);
		if (pat.matcher(s1).matches()) {
			return true;
		}
		return false;
	}

}
