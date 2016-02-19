package changeassistant.multipleexample.match;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.TypeNameTerm;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;
import changeassistant.multipleexample.contextualize.datastructure.Sequence;
import changeassistant.multipleexample.partition.MappingException;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.versions.comparison.ChangedMethodADT;

public class MatchResult {

	private Sequence sequence;
	private Map<String, String> cTou = null;// concrete identifier to unified
											// identifier
	private Map<String, String> uToc = null;// unified identifier to concrete
											// identifier
	private ChangedMethodADT adt = null;
	private SimpleTreeNode afterSTreeNode;
	private String methodString;

	public static MatchResult find(List<MatchResult> mResults, ChangedMethodADT adt){
		for(MatchResult mResult : mResults){
				if(mResult.adt.equals(adt)){
					return mResult;
				}
			}
		return null;
	}
	public MatchResult(Sequence result1, Sequence result2,
			List<List<SimpleASTNode>> simpleASTNodesListLeft,
			List<List<SimpleASTNode>> simpleASTNodesListRight) {
		sequence = result2;
		findIdMap(result1.getNodeIndexes(), result2.getNodeIndexes(),
				simpleASTNodesListLeft, simpleASTNodesListRight);
	}

	private void addMap(String str1, String str2) throws MappingException {
		String mappedU = cTou.get(str1);
		String mappedC = uToc.get(str2);
		if (mappedU == null && mappedC == null) {
			cTou.put(str2, str1);
			uToc.put(str1, str2);
		} else if (mappedU != null && mappedC == null || mappedU == null
				&& mappedC != null || !mappedU.equals(str2)
				|| !mappedC.equals(str1)) {
			if (Term.U_Pattern.matcher(str1).matches()) {
				uToc.put(str1, str2);
			} else {
				throw new MappingException(str1 + " cannot be mapped to "
						+ str2 + " since " + str1 + " is mapped to " + mappedU
						+ " and " + str2 + " is mapped to " + mappedC);
			}
		} else {
			// do nothing, since str1 and str2 have already been mapped together
		}
	}

	private void findIdMap(List<Integer> nodeIndexes1,
			List<Integer> nodeIndexes2, List<List<SimpleASTNode>> sNodesList1,
			List<List<SimpleASTNode>> sNodesList2) {
		cTou = new HashMap<String, String>();
		uToc = new HashMap<String, String>();
		List<SimpleASTNode> sNodes1 = null;
		List<SimpleASTNode> sNodes2 = null;
		SimpleASTNode sNode1 = null;
		SimpleASTNode sNode2 = null;
		String str1 = null;
		String str2 = null;
		Term tmpTerm1 = null;
		Term tmpTerm2 = null;
		TypeNameTerm tTerm1 = null;
		TypeNameTerm tTerm2 = null;
		Enumeration<SimpleASTNode> cEnum1 = null;
		Enumeration<SimpleASTNode> cEnum2 = null;
		Queue<SimpleASTNode> queue1 = new LinkedList<SimpleASTNode>();
		Queue<SimpleASTNode> queue2 = new LinkedList<SimpleASTNode>();
		try {
			for (int i = 0; i < nodeIndexes1.size(); i++) {
				if (nodeIndexes1.get(i) < 0)
					continue;
				sNodes1 = sNodesList1.get(nodeIndexes1.get(i) - 1);
				sNodes2 = sNodesList2.get(nodeIndexes2.get(i) - 1);
				for (int j = 0; j < sNodes1.size(); j++) {
					sNode1 = sNodes1.get(j);
					sNode2 = sNodes2.get(j);
					queue1.add(sNode1);
					queue2.add(sNode2);
					while (!queue1.isEmpty()) {
						sNode1 = queue1.remove();
						sNode2 = queue2.remove();
						if (sNode1.getChildCount() != 0) {
							cEnum1 = sNode1.children();
							cEnum2 = sNode2.children();
							if (sNode1.getChildCount() != sNode2
									.getChildCount()
									&& sNode1.getStrValue().equals(
											SimpleASTNode.LIST_LITERAL)
									&& sNode2.getStrValue().equals(
											SimpleASTNode.LIST_LITERAL)) {
								if (sNode1.getChildCount() == 1
										&& Term.U_List_Literal_Pattern.matcher(
												((SimpleASTNode) sNode1
														.getChildAt(0))
														.getStrValue())
												.matches()) {
									String uLiteral = ((SimpleASTNode) sNode1
											.getChildAt(0)).getStrValue();
									if (sNode2.getChildCount() == 0) {
										addMap(uLiteral, "");
									} else {
										addMap(uLiteral,
												sNode2.constructStrValue());
									}
								}
								continue;
							}
							while (cEnum1.hasMoreElements()) {
								queue1.add(cEnum1.nextElement());
								queue2.add(cEnum2.nextElement());
							}
						} else {// matching process only happen for two leaves
							str1 = sNode1.getStrValue();
							str2 = sNode2.getStrValue();
							if (Term.U_Pattern.matcher(str1).matches()) {
								addMap(str1, str2);
							} else if (sNode1.getTerm() != null) {
								tmpTerm1 = sNode1.getTerm();
								if (sNode2.getChildCount() == 1) {
									SimpleASTNode tmpSNode = (SimpleASTNode) sNode2
											.getChildAt(0);
									while (tmpSNode.getTerm() == null
											&& tmpSNode.getChildCount() == 1) {
										tmpSNode = (SimpleASTNode) tmpSNode
												.getChildAt(0);
									}
									if (tmpSNode.getTerm() == null) {
										System.out
												.println("More process is needed");
									} else {
										tmpTerm2 = tmpSNode.getTerm();
									}
								} else {
									tmpTerm2 = sNode2.getTerm();
								}
								addMap(tmpTerm1.getName(), tmpTerm2.getName());
								if (tmpTerm1.getTermType().equals(
										Term.TermType.VariableTypeBindingTerm)) {
									tTerm1 = ((VariableTypeBindingTerm) tmpTerm1)
											.getTypeNameTerm();
									tTerm2 = ((VariableTypeBindingTerm) tmpTerm2)
											.getTypeNameTerm();
									addMap(tTerm1.getName(), tTerm2.getName());
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Sequence getSequence() {
		return sequence;
	}

	public ChangedMethodADT getADT() {
		return adt;
	}

	public Map<String, String> getCtoU() {
		return cTou;
	}

	public Map<String, String> getUtoC() {
		return uToc;
	}

	public SimpleTreeNode getAfterSTreeNode() {
		return afterSTreeNode;
	}

	public String getMethodString() {
		return methodString;
	}

	public void setADT(ChangedMethodADT adt) {
		this.adt = adt;
	}

	public void setAfterSTreeNode(SimpleTreeNode n) {
		afterSTreeNode = n;
	}

	public void setMethodString(String str) {
		methodString = str;
	}
	
	@Override
	public String toString(){
		return adt.toString();
	}
}
