package changeassistant.clonereduction.manipulate;

import java.util.ArrayList;
import java.util.List;

import changeassistant.versions.treematching.edits.ITreeEditOperation.EDIT;

public class OperationCollection {
	List<EDIT> editList;
	List<Integer> countList;
	List<String> infoList;
	
	int size;

	public void setNumOfMethods(int size){
		this.size = size;
	}
	
	public void addForEachMethod(int num, EDIT type){
		editList.add(type);
		countList.add(num * size);		
	}
	
	public OperationCollection() {
		editList = new ArrayList<EDIT>();
		countList = new ArrayList<Integer>();
		infoList = new ArrayList<String>();
	}

	public boolean containsEdit(EDIT editType) {
		return editList.contains(editType);
	}

	public void add(int count, EDIT type) {
		add(count, type, "");
	}
	
	public void add(int count, EDIT type, String message){
		editList.add(type);
		countList.add(count);
		infoList.add(message);
	}

	public int getCount() {
		int count = 0;
		for (int i = 0; i < countList.size(); i++) {
			count += countList.get(i);
		}
		return count;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < countList.size(); i++) {
			buffer.append(" ").append(countList.get(i)).append(" ")
					.append(editList.get(i));
		}
		return buffer.toString();
	}
	
	public void print(){
		int counter = 0;
		int tmpCounter =0;
		for(int i = 0; i < countList.size(); i++){
			tmpCounter = countList.get(i);
			System.out.println(tmpCounter + " " + editList.get(i) + "--" + infoList.get(i));
			counter += tmpCounter;
		}
		System.out.println("Total number of edit operations is " + counter);
	}
}
