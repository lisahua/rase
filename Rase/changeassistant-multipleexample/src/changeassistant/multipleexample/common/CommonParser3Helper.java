package changeassistant.multipleexample.common;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;

public class CommonParser3Helper {

	public static List<Map<String, String>> addMapWhenTraverse2(
			Integer[][] nodeIndexArray,
			List<List<SimpleASTNode>> tmpNodes1List,
			List<List<SimpleASTNode>> simpleASTNodes1List,
			List<List<List<SimpleASTNode>>> simpleASTNodes2ListList,
			Map<String, String> s1Tou) {
		SimpleASTNode child = null;
		SimpleASTNode tmpNode = null;
		SimpleASTNode node1 = null;
		SimpleASTNode node2 = null;

		List<Map<String, String>> tmpMap2s = new ArrayList<Map<String, String>>();

		List<SimpleASTNode> tmpNodes = null;
		List<SimpleASTNode> simpleASTNodes1 = null;

		List<List<SimpleASTNode>> simpleASTNodes2s = new ArrayList<List<SimpleASTNode>>();

		Queue<SimpleASTNode> queue = new LinkedList<SimpleASTNode>();
		Queue<SimpleASTNode> queue1 = new LinkedList<SimpleASTNode>();

		List<Queue<SimpleASTNode>> queue2s = new ArrayList<Queue<SimpleASTNode>>();
		for (int i = 0; i < simpleASTNodes2ListList.size(); i++) {
			queue2s.add(new LinkedList<SimpleASTNode>());
			tmpMap2s.add(new HashMap<String, String>());
		}
		queue2s.remove(queue2s.size() - 1);

		List<SimpleASTNode> node2s = null;

		Enumeration<SimpleASTNode> cEnum1 = null, cEnum2 = null;

		String tmpStrValue1 = null, tmpStrValue2 = null;

		Term tmpTerm1 = null, tmpTerm2 = null;

		for (int l = 0; l < nodeIndexArray.length; l++) {
			tmpNodes = tmpNodes1List.get(l);
			simpleASTNodes1 = simpleASTNodes1List.get(nodeIndexArray[l][0] - 1);
			for (int j = 1; j < simpleASTNodes2ListList.size(); j++) {
				simpleASTNodes2s.add(simpleASTNodes2ListList.get(j).get(
						nodeIndexArray[l][j] - 1));
			}
			for (int m = 0; m < simpleASTNodes1.size(); m++) {
				queue.add(tmpNodes.get(m));
				queue1.add(simpleASTNodes1.get(m));
				for (int j = 1; j < simpleASTNodes2s.size(); j++) {
					queue2s.get(j - 1).add(simpleASTNodes2s.get(j).get(m));
				}
			}
			while (!queue.isEmpty()) {
				tmpNode = queue.remove();
				node1 = queue1.remove();
				node2s = new ArrayList<SimpleASTNode>();
				for (int j = 0; j < queue2s.size(); j++) {
					node2s.add(queue2s.get(j).remove());
				}
				if (tmpNode.getChildCount() == 0) {
					if (node1.getChildCount() != 0) {
						if (node1.getChildCount() == 1) {
							child = (SimpleASTNode) node1.getChildAt(0);
							node1.setTerm(child.getTerm());
							for (int j = 0; j < node2s.size(); j++) {
								node2 = node2s.get(j);
								child = (SimpleASTNode) node2.getChildAt(0);
								node2.setTerm(child.getTerm());
							}
						}
						node1.removeAllChildren();
						for (int j = 0; j < node2s.size(); j++) {
							node2.removeAllChildren();
						}
						tmpStrValue1 = tmpNode.getStrValue();
						tmpStrValue2 = node1.getStrValue();
						if (s1Tou.containsValue(tmpStrValue1)) {
							for (int j = 0; j < node2s.size(); j++) {
								tmpMap2s.get(j).put(
										node2s.get(j).getStrValue(),
										tmpNode.getStrValue());
							}
						}
						if (Term.V_Pattern.matcher(tmpStrValue1).matches()) {
							tmpTerm1 = ((VariableTypeBindingTerm) (tmpNode
									.getTerm())).getTypeNameTerm();
							tmpTerm2 = ((VariableTypeBindingTerm) node1
									.getTerm()).getTypeNameTerm();
							tmpStrValue1 = tmpTerm1.getName();
							tmpStrValue2 = tmpTerm2.getName();
							if (s1Tou.containsValue(tmpStrValue1)) {
								for (int j = 0; j < node2s.size(); j++) {
									tmpStrValue2 = ((VariableTypeBindingTerm) node2s
											.get(j).getTerm())
											.getTypeNameTerm().getName();
									tmpMap2s.get(j).put(tmpStrValue2,
											tmpStrValue1);
								}
							}
						}
					}
				} else {
					if (tmpNode.getChildCount() != node1.getChildCount()) {
						System.out.println("More process is needed");
					} else {
						cEnum1 = tmpNode.children();
						cEnum2 = node1.children();
						while (cEnum1.hasMoreElements()) {
							queue.add(cEnum1.nextElement());
							queue1.add(cEnum2.nextElement());
						}
					}
					for (int j = 0; j < node2s.size(); j++) {
						node2 = node2s.get(j);
						if (node1.getChildCount() != node2.getChildCount()) {
							System.out.println("More process is needed");
						} else {
							cEnum2 = node2.children();
							while (cEnum2.hasMoreElements()) {
								queue2s.get(j).add(cEnum2.nextElement());
							}
						}
					}
				}
			}
		}
		return tmpMap2s;
	}
}
