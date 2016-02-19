package changeassistant.clonereduction.manipulate.convert;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;

import changeassistant.changesuggestion.expression.representation.ASTExpressionTransformer;
import changeassistant.clonereduction.main.CloneReductionMain;
import changeassistant.clonereduction.manipulate.refactoring.RefactoringMetaData;
import changeassistant.internal.ASTNodeFinder;
import changeassistant.multipleexample.datastructure.GeneralizedStmtIndexMap;
import changeassistant.multipleexample.main.Constants;
import changeassistant.multipleexample.match.PatternUtil;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.peers.SourceCodeRange;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.treematching.edits.ITreeEditOperation.EDIT;

public class MethodInvocationConverter {
	
	private static List<List<SimpleASTNode>> nodesList = null;
	private static List<SimpleTreeNode> sNodes = null;
	private static MethodDeclaration md = null;
	
	public static void convert(List<SimpleTreeNode> sNodes2, Node mNode, 
			List<List<SimpleASTNode>> pCusNodesList){
		sNodes = sNodes2;
		nodesList = pCusNodesList;
		md = mNode.getMethodDeclaration();
		SourceCodeRange scr = null;
		SimpleTreeNode sTmp = null;
		SimpleASTNode sATmp = null;
		Enumeration<SimpleTreeNode> sEnum = null;
		List<SimpleASTNode> nodes = null;
		ASTNodeFinder finder = new ASTNodeFinder();
		MethodInvocation mi = null;
		IMethodBinding binding = null;
		Expression invoker = null;
		boolean isNonStatic = false;
		Queue<SimpleASTNode> queue = new LinkedList<SimpleASTNode>();
		for(SimpleTreeNode s : sNodes){
			sEnum = s.breadthFirstEnumeration();
			while(sEnum.hasMoreElements()){
				sTmp = sEnum.nextElement();
				nodes = nodesList.get(sTmp.getNodeIndex() - 1);
				for(SimpleASTNode sNode : nodes){
					queue.add(sNode);
					while(!queue.isEmpty()){
						sATmp = queue.remove();
						if(sATmp.getNodeType() == ASTNode.METHOD_INVOCATION){
							scr = sATmp.getScr();
							mi = (MethodInvocation)finder.lookforASTNode(md, scr);
							binding = mi.resolveMethodBinding();
							invoker = (Expression)mi.getExpression();
							isNonStatic = ((binding.getModifiers() & Modifier.STATIC) == 0);
							if(isNonStatic && invoker == null && RefactoringMetaData.isNeedTemplateClass()){
								sATmp.insert(new SimpleASTNode(ASTNode.SIMPLE_NAME, Constants.INSTANCE, 0, 0), 0);
								sATmp.insert(new SimpleASTNode(ASTExpressionTransformer.DOT, ".", 0, 0), 1);
								sATmp.setRecalcToRoot();
							}
						}
					}
					sNode.constructStrValue();
					if(sATmp.getChildCount() > 0){
						queue.addAll(sATmp.getChildren());
					}
				}
				String newString = PatternUtil.createStrValue(sTmp.getNodeType(), sTmp.getStrValue(), nodes);
				sTmp.setStrValue(newString);
			}
		}
	}
	
	public static void convert(List<SimpleTreeNode> sNodes2, Node mNode, Map<String, SourceCodeRange> miMap, Map<Integer, List<SimpleASTNode>> indexStmtMap){
		sNodes = sNodes2;
		md = mNode.getMethodDeclaration();
		ASTNodeFinder finder = new ASTNodeFinder();
		MethodInvocation mi = null;
		Queue<SimpleASTNode> queue = new LinkedList<SimpleASTNode>();
		Enumeration<SimpleTreeNode> sEnum = null;
		SimpleTreeNode sTmp = null;
		List<SimpleASTNode> nodes = null;
		SimpleASTNode sASTmp = null;
		SourceCodeRange scr = null;
		System.out.print("");
		for(SimpleTreeNode s : sNodes){
			sEnum = s.breadthFirstEnumeration();
			while(sEnum.hasMoreElements()){
				sTmp = sEnum.nextElement();
				nodes = indexStmtMap.get(sTmp.getNodeIndex());
				for(SimpleASTNode sASTNode : nodes){
					queue.add(sASTNode);
					while(!queue.isEmpty()){
						sASTmp = queue.remove();
						if(sASTmp.getNodeType() == ASTNode.METHOD_INVOCATION){
							scr = sASTmp.getScr();
							if(miMap.values().contains(scr)){
								mi = (MethodInvocation)finder.lookforASTNode(md, scr);
								if(mi.getExpression() == null){
									IMethodBinding binding = mi.resolveMethodBinding();
									int modifiers = binding.getModifiers();
									boolean isNonStatic = ((modifiers & Modifier.STATIC) == 0);
									if(!isNonStatic){
										sASTmp.insert(new SimpleASTNode(ASTNode.SIMPLE_TYPE, binding.getDeclaringClass().getQualifiedName(), 0, 0), 0);
										sASTmp.insert(new SimpleASTNode(ASTExpressionTransformer.DOT, ".", 0, 0), 1);
										sASTmp.setRecalcToRoot();
									}
								}
								/*
								if(mi.getExpression() == null){
									sASTmp.insert(new SimpleASTNode(ASTNode.SIMPLE_NAME, Constants.INSTANCE, 0, 0), 0);
									sASTmp.insert(new SimpleASTNode(ASTExpressionTransformer.DOT, ".", 0, 0), 1);
									sASTmp.setRecalcToRoot();
								}
								*/
							}
						}
						if(sASTmp.getChildCount() > 0){
							queue.addAll(sASTmp.getChildren());
						}
					}
					sASTNode.constructStrValue();					
				}
				String newString = PatternUtil.createStrValue(sTmp.getNodeType(), sTmp.getStrValue(), nodes);
				sTmp.setStrValue(newString);
			}
		}
	}
}
