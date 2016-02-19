package changeassistant.changesuggestion.astrewrite;

import java.util.List;

import changeassistant.changesuggestion.astrewrite.EditOperation.EditType;

public class TokenEditOperation extends EditOperation<List<String>> {

	public TokenEditOperation(EditType type, int anchor, List<String> elem) {
		super(type, anchor, elem);
		// TODO Auto-generated constructor stub
	}
}
