package changeassistant.changesuggestion.astrewrite;

import changeassistant.changesuggestion.astrewrite.EditOperation.EditType;

public class StringEditOperation extends EditOperation<String> {

	public StringEditOperation(EditType type, int anchor, String elem) {
		super(type, anchor, elem);
		// TODO Auto-generated constructor stub
	}

	public StringEditOperation(EditType type, int i,
			String oElem, String nElem) {
		super(type, i, oElem, nElem);
	}
}
