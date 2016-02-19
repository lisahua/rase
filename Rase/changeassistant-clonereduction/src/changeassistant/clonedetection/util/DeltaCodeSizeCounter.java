package changeassistant.clonedetection.util;

import java.util.ArrayList;
import java.util.List;

public class DeltaCodeSizeCounter {

	private List<Integer> countList;
	private List<String> infoList;
	
	public DeltaCodeSizeCounter(){
		countList = new ArrayList<Integer>();
		infoList = new ArrayList<String>();
	}
	public void increment(int size, String desc){
		countList.add(size);
		infoList.add(desc);
	}
	public void decrement(int size, String desc){
		countList.add(-size);
		infoList.add(desc);
	}
	public void increment(String desc){
		countList.add(1);
		infoList.add(desc);
	}
	public void decrement(String desc){
		countList.add(-1);
		infoList.add(desc);
	}
	
	@Override
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i < countList.size(); i++){
			buffer.append(countList.get(i)).append(" ").append(infoList.get(i)).append("\n");
		}
		return buffer.toString();
	}
	
	public int print(){
		int counter = 0;
		int tmpCounter = 0;
		int editCount = 0;
		for(int i = 0; i < countList.size(); i++){
			tmpCounter = countList.get(i);
			System.out.println(tmpCounter + " " + infoList.get(i));
			counter+= tmpCounter;
			editCount += Math.abs(tmpCounter);
		}
		System.out.println("Total delta code size is " + counter);
		System.out.println("Total edit operation number is " + editCount);
		return counter;
	}
}
