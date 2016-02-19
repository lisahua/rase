package changeassistant.multipleexample.match;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import changeassistant.change.group.model.SubTreeModel;
import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.multipleexample.contextualize.ContextualizationUtil;
import changeassistant.multipleexample.contextualize.datastructure.Sequence;
import changeassistant.multipleexample.partition.datastructure.EditInCommonCluster;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;

public class CodePattern implements Cloneable {

	private EditInCommonCluster callback;

	private Pattern packageNamingPattern;
	private Pattern classNamingPattern;
	private Pattern methodNamingPattern;
	private Pattern fileNamingPattern;
	private Set<String> usedFields;

//	private SubTreeModel matchingContext;
	SimpleTreeNode sTree;

	// when doing clone(), these items are not copied deeply since they are read
	// only
	protected Set<Term> alphabetSet;
	protected Set<String> stmtSet;
	protected List<List<SimpleASTNode>> simpleASTNodesList;
	protected Sequence sequence;

	public CodePattern() {
		alphabetSet = new HashSet<Term>();
		stmtSet = new HashSet<String>();
	}

	public CodePattern(EditInCommonCluster cluster) {
		this();
		callback = cluster;
		sTree = new SimpleTreeNode(cluster.getSTree());
		simpleASTNodesList = ContextualizationUtil.getCopy(cluster
				.getSimpleASTNodesList());
		sequence = new Sequence(cluster.getSequence().getNodeIndexes());
	}

	@Override
	public Object clone() {
		CodePattern pat = null;
		try {
			pat = (CodePattern) super.clone();
//			if (matchingContext != null) {
//				pat.matchingContext = matchingContext.createCopy();
//			}
			pat.sTree = new SimpleTreeNode(sTree);
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pat;
	}

	public Set<Term> getAlphabetSet() {
		return alphabetSet;
	}

	public Pattern getClassNamingPattern() {
		return classNamingPattern;
	}

	public EditInCommonCluster getCluster() {
		return callback;
	}

	public Pattern getFileNamingPattern() {
		return fileNamingPattern;
	}

	public Pattern getMethodNamingPattern() {
		return methodNamingPattern;
	}

	public Pattern getPackageNamingPattern() {
		return packageNamingPattern;
	}

	public Sequence getSequence() {
		return sequence;
	}

	public List<List<SimpleASTNode>> getSimpleASTNodesList() {
		return simpleASTNodesList;
	}

	public Set<String> getStmtSet() {
		return stmtSet;
	}

	public void setClassNamingPattern(Pattern pat) {
		classNamingPattern = pat;
	}

	public void setFileNamingPattern(Pattern pat) {
		fileNamingPattern = pat;
	}

	public void setMethodNamingPattern(Pattern pat) {
		methodNamingPattern = pat;
	}

	public void setPackageNamingPattern(Pattern pat) {
		packageNamingPattern = pat;
	}

	public SimpleTreeNode getSTree() {
		return sTree;
	}
	
	public Set<String> getUsedFields() {
		return usedFields;
	}

	public void setsTree(SimpleTreeNode sTree) {
		this.sTree = sTree;
	}

	public void setSimpleASTNodesList(List<List<SimpleASTNode>> simpleASTNodesList) {
		this.simpleASTNodesList = simpleASTNodesList;
	}
	
	public void setUsedFields(Set<String> usedFields){
		this.usedFields = usedFields;
	}

	
}
