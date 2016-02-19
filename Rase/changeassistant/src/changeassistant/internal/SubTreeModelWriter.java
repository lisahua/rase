package changeassistant.internal;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import changeassistant.change.group.model.SubTreeModel;
import changeassistant.model.TransformationRule;
import changeassistant.versions.comparison.ChangedMethodADT;

public class SubTreeModelWriter {

	public static void writeToFile(Map<SubTreeModel, List<TransformationRule>> subTreeMaps, String path){
		Map<SubTreeModel, Set<ChangedMethodADT>> map = new HashMap<SubTreeModel, Set<ChangedMethodADT>>();
		Iterator<SubTreeModel> iter = subTreeMaps.keySet().iterator();
		List<TransformationRule> trList = null;
		Set<ChangedMethodADT> changedSet = null;
		SubTreeModel temp = null;
		while(iter.hasNext()){
			temp = iter.next();
			trList = subTreeMaps.get(temp);
			changedSet = new HashSet<ChangedMethodADT>();
			for(TransformationRule tr : trList){
				changedSet.add(tr.originalMethod);
			}
			map.put(temp, changedSet);
		}
		try {
			FileOutputStream ostream = new FileOutputStream(path);
			ObjectOutputStream output = new ObjectOutputStream(ostream);
			output.writeObject(map);
			output.flush();
			ostream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void writeToFile2(Map<SubTreeModel, List<TransformationRule>> subTreeMaps, String path){
		try {
			FileOutputStream ostream = new FileOutputStream(path);
			ObjectOutputStream output = new ObjectOutputStream(ostream);
			output.writeObject(subTreeMaps);
			output.flush();
			ostream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
