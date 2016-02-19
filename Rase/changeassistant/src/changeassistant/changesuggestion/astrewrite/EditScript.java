package changeassistant.changesuggestion.astrewrite;

import java.util.ArrayList;
import java.util.List;

public class EditScript<T> {

	public ArrayList<EditOperation<T>> editOperations;
	public List<CommonADT<T>> commonSubElements;
	
	public EditScript(){
		this.editOperations = new ArrayList<EditOperation<T>>();
	}
	
	public void add(EditOperation<T> eo){
		this.editOperations.add(eo);
	}
}
