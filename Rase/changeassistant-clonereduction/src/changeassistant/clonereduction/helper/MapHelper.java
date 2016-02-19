package changeassistant.clonereduction.helper;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import changeassistant.changesuggestion.expression.representation.Term;

public class MapHelper {

	public Map<String, String> getRefined(Map<String, String> sTou) {
		Map<String, String> refined = new HashMap<String, String>();
		for (Entry<String, String> entry : sTou.entrySet()) {
			if (Term.V_Pattern.matcher(entry.getValue()).matches()) {
				refined.put(entry.getKey(), entry.getValue());
			}
		}
		return refined;
	}
}
