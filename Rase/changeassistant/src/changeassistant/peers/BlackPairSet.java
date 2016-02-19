package changeassistant.peers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import changeassistant.change.group.model.SubTreeModel;

public class BlackPairSet {

	private Set<SubTreeModelPair> blackPairs;
	
	private boolean recordBlackPairs;
	
	public BlackPairSet(BlackPairSet set){
		blackPairs = new HashSet<SubTreeModelPair>(set.blackPairs);
		recordBlackPairs = true;
	}
	
	public BlackPairSet(Set<SubTreeModelPair> blackPairs){
		this.blackPairs = blackPairs;
		recordBlackPairs = true;
	}
	
	public BlackPairSet(){
		blackPairs = new HashSet<SubTreeModelPair>();
		recordBlackPairs = true;
	}
	
	public void addAll(BlackPairSet blackPairSet){
		if(recordBlackPairs){
			blackPairs.addAll(blackPairSet.blackPairs);
		}
	}
	
	public void addAll(List<SubTreeModel> list, SubTreeModel y){
		if(recordBlackPairs){
			for(SubTreeModel x : list){
				blackPairs.add(new SubTreeModelPair(x, y));
			}
		}
	}
	
	public void addAll(Set<SubTreeModelPair> set){
		if(recordBlackPairs){
			blackPairs.addAll(set);
		}
	}
	
	public void addBlackPair(SubTreeModelPair pair)
	{
		if(recordBlackPairs){
			blackPairs.add(pair);
		}
	}
	
	public boolean contains(SubTreeModelPair pair){
		boolean flag = false;
		if(recordBlackPairs){
			flag = blackPairs.contains(pair);
		}else{
			//do nothing
		}
		return flag;
	}
	
	public void clear(){
		if(recordBlackPairs){
			blackPairs.clear();
		}
	}
	
	public void disable(){
		recordBlackPairs = false;
	}
	
	public void enable(){
		recordBlackPairs = true;
	}
	
	public Set<SubTreeModelPair> getBlackPairs(){
		return blackPairs;
	}
	
	public void removeBlackPair(SubTreeModelPair pair){
		if(recordBlackPairs){
			blackPairs.remove(pair);
		}
	}
	
	public void remove(List<SubTreeModel> candidates, SubTreeModel y){
		if(recordBlackPairs){
			for(SubTreeModelPair bPair : blackPairs){
				if(bPair.getRight().equals(y))
					candidates.remove(bPair.getLeft());
			}
		}
	}
}
