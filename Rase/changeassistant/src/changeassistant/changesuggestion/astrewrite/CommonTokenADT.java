package changeassistant.changesuggestion.astrewrite;

import java.util.List;

public class CommonTokenADT extends CommonADT<List<String>>{

	public CommonTokenADT(List<String> commonElement, int left, int right) {
		super(commonElement, left, right);		
	}

}
