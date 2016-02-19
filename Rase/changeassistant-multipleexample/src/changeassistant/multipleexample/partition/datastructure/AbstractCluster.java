package changeassistant.multipleexample.partition.datastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.TermsList;
import changeassistant.changesuggestion.expression.representation.TypeNameTerm;
import changeassistant.multipleexample.contextualize.ContextualizeHelper1;
import changeassistant.multipleexample.contextualize.datastructure.Sequence;
import changeassistant.multipleexample.partition.CommonEditParser;
import changeassistant.multipleexample.partition.SimpleASTNodeConverter;

public abstract class AbstractCluster implements Cloneable,
		Comparable<AbstractCluster> {

	/**
	 * used for context match
	 */
	protected SimpleTreeNode sTree; // this is an updated SimpleTreeNode after
									// invoking getContextCode()
	protected SimpleTreeNode sTree2;
	/**
	 * used for edit script generation
	 */
	protected SimpleTreeNode sEditedTree;
	protected List<ChangeSummary> conChgSum;
	protected List<String> conChgSumStr;
	protected List<String> conAbsChgSumStr;
	/**
	 * used for context match
	 */
	protected Sequence sequence;
	protected List<List<List<SimpleASTNode>>> simpleExprsLists;
	protected List<EditInCommonCluster> outgoings;
	protected List<List<SimpleASTNode>> simpleASTNodesList;// edit content
	protected Map<String, Set<TypeNameTerm>> typeTermMap;

	public AbstractCluster() {
		sEditedTree = null;
	}

	public AbstractCluster(List<ChangeSummary> conChgSum,
			List<String> conChgSumStr, List<String> conAbsChgSumStr) {
		this.conChgSum = conChgSum;
		this.conChgSumStr = conChgSumStr;
		this.conAbsChgSumStr = conAbsChgSumStr;
	}

	public void addOutgoing(EditInCommonCluster parent) {
		if (this.outgoings == null)
			outgoings = new ArrayList<EditInCommonCluster>();
		if (!this.outgoings.contains(parent)) {
			this.outgoings.add(parent);
		}
	}

	@Override
	public Object clone() {
		AbstractCluster clus = null;
		try {
			clus = (AbstractCluster) super.clone();
			clus.sTree = new SimpleTreeNode(sTree);
			if (clus.sEditedTree != null)
				clus.sEditedTree = new SimpleTreeNode(clus.sEditedTree);
			if (sequence != null)
				clus.sequence = new Sequence(sequence.getNodeIndexes());
			clus.simpleExprsLists = new ArrayList<List<List<SimpleASTNode>>>();
			List<List<SimpleASTNode>> newSimpleExprsList = null;
			List<SimpleASTNode> newSimpleExprs = null;
			if (simpleExprsLists != null) {
				for (List<List<SimpleASTNode>> simpleExprsList : simpleExprsLists) {
					newSimpleExprsList = new ArrayList<List<SimpleASTNode>>();
					for (List<SimpleASTNode> simpleExprs : simpleExprsList) {
						newSimpleExprs = new ArrayList<SimpleASTNode>(
								simpleExprs);
						newSimpleExprsList.add(newSimpleExprs);
					}
					clus.simpleExprsLists.add(newSimpleExprsList);
				}
			}
			if (simpleASTNodesList != null) {
				clus.simpleASTNodesList = new ArrayList<List<SimpleASTNode>>();
				for (List<SimpleASTNode> simpleASTNodes : simpleASTNodesList) {
					clus.simpleASTNodesList.add(new ArrayList<SimpleASTNode>(
							simpleASTNodes));
				}
			}
			if (typeTermMap != null) {
				clus.typeTermMap = new HashMap<String, Set<TypeNameTerm>>();
				for (Entry<String, Set<TypeNameTerm>> entry : typeTermMap
						.entrySet()) {
					clus.typeTermMap.put(entry.getKey(),
							new HashSet<TypeNameTerm>(entry.getValue()));
				}
			}
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return clus;
	}

	public int compareTo(AbstractCluster o) {
		AbstractCluster other = (AbstractCluster) o;
		return this.getIndex() - other.getIndex();
	}

	public boolean containsOutgoing(EditInCommonCluster outgoing) {
		return outgoings.contains(outgoing);
	}

	abstract public boolean contains(Integer index);

	public boolean equals(Object obj) {
		if (!(obj instanceof AbstractCluster))
			return false;
		AbstractCluster other = (AbstractCluster) obj;
		if (other.getIndex() != this.getIndex())
			return false;
		return true;
	}

	public List<ChangeSummary> getConChgSum() {
		return conChgSum;
	}

	public List<String> getChgSumStr() {
		return conChgSumStr;
	}

	public List<String> getAbsChgSumStr() {
		return conAbsChgSumStr;
	}

	abstract public List<Integer> getInstances();

	abstract public int getIndex();

	public List<EditInCommonCluster> getOutgoings() {
		return this.outgoings;
	}

	public Sequence getSequence() {
		return sequence;
	}

	public SimpleTreeNode getSEditedTree() {
		return sEditedTree;
	}

	public List<List<SimpleASTNode>> getSimpleASTNodesList() {
		return simpleASTNodesList;
	}

	public List<List<List<SimpleASTNode>>> getSimpleExprsLists() {
		return simpleExprsLists;
	}

	public SimpleTreeNode getSTree() {
		return sTree;
	}

	public SimpleTreeNode getSTree2() {
		return sTree2;
	}

	public Map<String, Set<TypeNameTerm>> getTypeTermMap() {
		if (typeTermMap == null) {
			List<List<List<Term>>> termsListList = new ArrayList<List<List<Term>>>();
			for (List<SimpleASTNode> simpleASTNodes : simpleASTNodesList) {
				termsListList.add(SimpleASTNodeConverter
						.convertToTermsList(simpleASTNodes));
			}
			List<List<SimpleASTNode>> simpleExprsList = null;
			for (int i = 0; i < simpleExprsLists.size(); i++) {
				simpleExprsList = simpleExprsLists.get(i);
				for (List<SimpleASTNode> simpleASTNodes : simpleExprsList) {
					termsListList.add(SimpleASTNodeConverter
							.convertToTermsList(simpleASTNodes));
				}
			}
			typeTermMap = TermsList.createTypeTermMap(termsListList);
		}
		return typeTermMap;
	}

	public int hashCode() {
		return this.getIndex();
	}

	public AbstractCluster intersectWithoutContext(AbstractCluster other, EditInCommonGroup group){
		EditInCommonCluster result = null;
		CommonEditParser parser = new CommonEditParser();
		result = parser.intersect(this, other, this.conChgSum, other.conChgSum, this.conChgSumStr, other.conChgSumStr, 
				this.conAbsChgSumStr, other.conAbsChgSumStr, this.simpleExprsLists, other.simpleExprsLists, group);
		if(result != null){
			this.addOutgoing(result);
			other.addOutgoing(result);
			ContextualizeHelper1 ch = new ContextualizeHelper1(group, result);
			ch.initTrees(result);
			ch.addTrees(result);
		}
		return result;
	}
	
	public AbstractCluster intersect(AbstractCluster other,
			EditInCommonGroup group) {
		EditInCommonCluster result = null;
		CommonEditParser parser = new CommonEditParser();
		result = parser.intersect(this, other, this.conChgSum, other.conChgSum,
				this.conChgSumStr, other.conChgSumStr, this.conAbsChgSumStr,
				other.conAbsChgSumStr, this.simpleExprsLists,
				other.simpleExprsLists, group);
		// List<Integer> leftCSIndexes = parser.getLeftCSIndexes();
		if (result != null) {
			ContextualizeHelper1 ch1 = new ContextualizeHelper1(group, result);
			result = ch1.parseMinContext();
			if (result == null) {
				return null;
			}
			if (result.getSpecificToUnifiedList().get(0).size() != result
					.getSpecificToUnifiedList().get(1).size())
				System.out.println("More process is needed");
			// }
			this.addOutgoing(result);
			other.addOutgoing(result);
		}
		return result;
	}

	public void removeOutgoing(EditInCommonCluster clus) {
		outgoings.remove(clus);
	}

	public void setConChgSum(List<ChangeSummary> conChgSum) {
		this.conChgSum = conChgSum;
	}

	public void setChgSumStr(List<String> chgSumStr) {
		this.conChgSumStr = chgSumStr;
	}

	public void setAbsChgSumStr(List<String> absChgSumStr) {
		this.conAbsChgSumStr = absChgSumStr;
	}

	public void setSequence(Sequence s) {
		this.sequence = s;
	}

	public void setSEditedTree(SimpleTreeNode s) {
		this.sEditedTree = s;
	}

	public void setSimpleASTNodesList(List<List<SimpleASTNode>> nodesList) {
		this.simpleASTNodesList = nodesList;
	}

	public void setSTree(SimpleTreeNode sTree) {
		this.sTree = sTree;
	}

	public void setSTree2(SimpleTreeNode sTree2) {
		this.sTree2 = sTree2;
	}

}
