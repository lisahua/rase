package changeassistant.changesuggestion.astrewrite;

public class EditOperation<T> {

	public static enum EditType{SUBSTITUTE_BEFORE, SUBSTITUTE_AFTER};
	public EditType type;
	public T elem;
	public int anchor;
	public T nElem;
	
	public EditOperation(EditType type, int anchor, T elem){
		this.anchor = anchor;
		this.type = type;
		this.elem = elem;
	}
	
	public EditOperation(EditType type, int anchor, T oElem, T nElem){
		this(type, anchor, oElem);
		this.nElem = nElem;
	}
}
