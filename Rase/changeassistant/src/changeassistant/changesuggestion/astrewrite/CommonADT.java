package changeassistant.changesuggestion.astrewrite;

public class CommonADT <T>{

	public T commonElement;
	public int leftIndex;
	public int rightIndex;
	
	public CommonADT(T commonElement, int left, int right){
		this.commonElement = commonElement;
		this.leftIndex = left;
		this.rightIndex = right;
	}
}
