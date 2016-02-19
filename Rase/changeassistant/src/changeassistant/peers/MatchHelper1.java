package changeassistant.peers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import changeassistant.change.group.model.SubTreeModel;
import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.Term.TermType;
import changeassistant.internal.ConsoleInputReader;

/**
 * To finish the first two steps of matching
 * @author mn8247
 *
 */
public class MatchHelper1 {
	
	private void addPair(Set<SubTreeModelPair> pairs, SubTreeModelPair addedOne){
		if(pairs.contains(addedOne)){
			//do nothing
		}else{
			pairs.add(addedOne);
		}
	}
		/**
		 * match leaves only according to its own information.
		 * This is the first step to match as many leaves as possible
		 * @param left
		 * @param right
		 * @param 
		 */
	public Set<SubTreeModelPair> matchLeavesAndEditedInners(SubTreeModel left, SubTreeModel right){
		Set<SubTreeModelPair> mLeaves = new HashSet<SubTreeModelPair>();
			SubTreeModel x = null, y = null;
			boolean found = false;
			for(Enumeration<SubTreeModel> rightNodes = right.postorderEnumeration(); rightNodes.hasMoreElements();){
				y = rightNodes.nextElement();//if one of the right node is not matched, then quit	
				if(y.isLeaf()){
					found = false;
					for(Enumeration<SubTreeModel> leftNodes = left.postorderEnumeration(); leftNodes.hasMoreElements();){
						x = leftNodes.nextElement();
						if(MatchWorker.isEquivalentNode(x, y, true)){
							addPair(mLeaves, new SubTreeModelPair(x, y));
							found = true;
						}
					}
					if(!found && y.getEDITset() != null){
						return null;
					}
				}else if(y.getEDITset() != null){
					found = false;
					for(Enumeration<SubTreeModel> leftNodes = left.postorderEnumeration(); leftNodes.hasMoreElements();){
						x = leftNodes.nextElement();
						if(MatchWorker.isEquivalentNode(x, y, true)){//exact match for the edited node
							found = true;
						}
					}
					if(!found){
						return null;
					}
				}
		}
		return mLeaves;
	}
	
	public boolean readOracle(SubTreeModel left, SubTreeModel right, Set<SubTreeModelPair> matchedLeaves){
		List<SubTreeModel> leftNodes = new ArrayList<SubTreeModel>();
		List<SubTreeModel> rightNodes = new ArrayList<SubTreeModel>();
		SubTreeModel temp;
		boolean success = false;
		Enumeration<SubTreeModel> leftEnumeration = left.postorderEnumeration();
		while(leftEnumeration.hasMoreElements()){
			temp = leftEnumeration.nextElement();
			if(!temp.isMatched()){
				leftNodes.add(temp);
			}
		}
		Enumeration<SubTreeModel> rightEnumeration = right.postorderEnumeration();
		while(rightEnumeration.hasMoreElements()){
			temp = rightEnumeration.nextElement();
			if(!temp.isMatched()){
				rightNodes.add(temp);
			}
		}
		System.out.println("There are following nodes waiting to be matched:");
		System.out.println("Left nodes:");
		for(int i = 0; i < leftNodes.size(); i++){
			System.out.println(i + "---" + leftNodes.get(i).toConcreteString() + "---" + leftNodes.get(i));
		}
		System.out.println("Right nodes:");
		for(int i = 0; i < rightNodes.size(); i++){
			System.out.println(i + "---" + rightNodes.get(i).toConcreteString() + "---" + rightNodes.get(i));
		}
		int leftIndex, rightIndex;
		String oracle;
		ConsoleInputReader reader = new ConsoleInputReader();
		while(true){
			System.out.println("Please give your oracle!");
			oracle = reader.readOracle();
			if(oracle.charAt(0) >= 'a' && oracle.charAt(0) <= 'z' ||
					oracle.charAt(0)>= 'A' && oracle.charAt(0) <='Z')
				break;
			StringTokenizer st = new StringTokenizer(oracle, " ");
			SubTreeModelPair pair;
			while(st.hasMoreElements()){
				leftIndex = Integer.parseInt((String) st.nextElement());
				rightIndex = Integer.parseInt((String) st.nextElement());
				pair = new SubTreeModelPair(leftNodes.get(leftIndex), rightNodes.get(rightIndex));
				if(matchedLeaves.contains(pair)){
					// do nothing
				}else{
					matchedLeaves.add(pair);
					success = true;
				}
			}
		}
		return success;
	}
}
