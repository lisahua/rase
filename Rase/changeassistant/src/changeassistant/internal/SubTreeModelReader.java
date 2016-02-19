package changeassistant.internal;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import changeassistant.change.group.model.SubTreeModel;
import changeassistant.model.TransformationRule;
import changeassistant.versions.comparison.ChangedMethodADT;

public class SubTreeModelReader {

	public static Map<SubTreeModel, Set<ChangedMethodADT>> readFromFile(String path){
		try {
			FileInputStream in = new FileInputStream(path);
			ObjectInputStream input = new ObjectInputStream(in);
			Map<SubTreeModel, Set<ChangedMethodADT>> map = (Map<SubTreeModel, Set<ChangedMethodADT>>)input.readObject();
			return map;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return null;
	}
	
	public static Map<SubTreeModel, List<TransformationRule>> readFromFile2(String path){
		try{
			FileInputStream in = new FileInputStream(path);
			ObjectInputStream input = new ObjectInputStream(in);
			Map<SubTreeModel, List<TransformationRule>> map = (Map<SubTreeModel, List<TransformationRule>>)input.readObject();
			return map;
		}catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return null;
	}
}
