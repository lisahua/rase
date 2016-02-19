package changeassistant.multipleexample.apply;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import changeassistant.classhierarchy.ClassContext;
import changeassistant.classhierarchy.ProjectResource;
import changeassistant.crystal.analysis.DefUseAnalysisFactory;
import changeassistant.crystal.analysis.PostDominateAnalysisFactory;
import changeassistant.crystal.analysis.def.DefUseElementResult;
import changeassistant.crystal.analysis.postdominate.PostDominateElementResult;
import changeassistant.internal.ASTMethodFinder;
import changeassistant.multipleexample.match.PatternUtil;
import changeassistant.multipleexample.partition.CommonEditParser;
import changeassistant.multipleexample.partition.SimpleASTCreator;
import changeassistant.multipleexample.partition.datastructure.ChangeSummary;
import changeassistant.multipleexample.partition.datastructure.EditInCommonCluster;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.peers.SourceCodeRange;
import changeassistant.peers.comparison.ASTMethodBodyTransformer;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.comparison.ChangedMethodADT;
import changeassistant.versions.treematching.measure.IStringSimilarityCalculator;
import changeassistant.versions.treematching.measure.NGramsCalculator;

public class ExtraEditApplier {

	private IStringSimilarityCalculator nodeStringCalc = null;

	private ProjectResource pr = null;

	private PostDominateElementResult postDominateElementResult = null;

	private DefUseElementResult methodAnalysisResult = null;

	private List<List<SimpleTreeNode>> controlNodes = null;

	private List<List<List<SimpleASTNode>>> controlExprsLists = null;

	private List<List<SimpleTreeNode>> dataNodes = null;

	private List<List<List<SimpleASTNode>>> dataExprsLists = null;

	private List<ChangeSummary> chgSums = null;

	private SimpleTreeNode sTree = null;

	private int counter = 0;

	private Map<SimpleTreeNode, Node> nodeMap = null;

	public ExtraEditApplier(ProjectResource pr) {
		nodeStringCalc = new NGramsCalculator();
		((NGramsCalculator) nodeStringCalc).setN(CommonEditParser.N);
		this.pr = pr;
	}

	public void apply(Map<String, String> uToc, String methodString,
			ChangedMethodADT adt, SimpleTreeNode originalSTree,
			EditInCommonCluster cluster) {
		sTree = originalSTree;
		ClassContext cc = pr.findClassContext(adt.classname);
		ICompilationUnit icu = JavaCore.createCompilationUnitFrom(pr
				.getFile(cc.relativeFilePath));
		controlNodes = cluster.getControlNodes();
		controlExprsLists = cluster.getControlExprsLists();
		dataNodes = cluster.getDataNodes();
		dataExprsLists = cluster.getDataExprsLists();
		chgSums = cluster.getConChgSum();
		counter = 0;
		System.out.print("");
		ASTMethodBodyTransformer transformer = new ASTMethodBodyTransformer();
		CompilationUnit cu = null;
		MethodDeclaration md = null;
		Node newNode = null;
		ASTMethodFinder finder = new ASTMethodFinder();
		Enumeration<Node> nEnum = null;
		Enumeration<SimpleTreeNode> sEnum = null;
		boolean isApplied = false;
		int startPos = -1;
		// ASTRewrite rewrite = null;
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		// AST parentAST = null;
		// ASTNode newMd = null;
		// ListRewrite listRewrite = null;
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		try {
			icu = icu.getWorkingCopy(null);
		} catch (JavaModelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			while (true) {
				parser.setSource(icu);
				parser.setResolveBindings(true);
				cu = (CompilationUnit) parser.createAST(null);
				md = finder.lookforMethod(cu, adt);
				startPos = md.getStartPosition();

				IType[] allTypes = icu.getTypes();
				isApplied = false;
				for (IType type : allTypes) {
					IMethod[] methods = type.getMethods();
					for (IMethod method : methods) {
						if (method.getSourceRange().getOffset() == startPos) {
							method.delete(false, null);
							type.createMethod(methodString, null, false, null);
							isApplied = true;
							break;
						}
						if (isApplied)
							break;
					}
				}
				if (isApplied) {
					parser.setSource(icu);
					parser.setResolveBindings(true);
					cu = (CompilationUnit) parser.createAST(null);
					md = finder.lookforMethod(cu, adt);
					newNode = transformer.createMethodBodyTree(md);
					// prepare for mapping between new created node tree and
					// known
					// sTree
					nodeMap = new HashMap<SimpleTreeNode, Node>();
					sEnum = sTree.breadthFirstEnumeration();
					nEnum = newNode.breadthFirstEnumeration();
					while (sEnum.hasMoreElements()) {
						nodeMap.put(sEnum.nextElement(), nEnum.nextElement());
					}
					System.out.print("");
					postDominateElementResult = PostDominateAnalysisFactory
							.getInstance().getAnalysisResultForMethod(icu, md);
					postDominateElementResult.init(newNode);
					methodAnalysisResult = DefUseAnalysisFactory.getInstance()
							.getAnalysisResultForMethod(icu, md);
					methodAnalysisResult.init(newNode);
					if (extendExtraEdits(uToc)) {
						methodString = EditScriptApplier
								.createMethodDeclarationString(sTree,
										methodString);
						continue;
					} else {
						break;
					}
				}
			}

			icu.discardWorkingCopy();
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	protected boolean applySupplementaryEdits(Set<Node> tmpNodes,
			SimpleTreeNode sNode, Node node, Map<String, String> uToc,
			List<List<SimpleTreeNode>> standardNodes) {
		SimpleTreeNode newSNode = null;
		List<ASTNode> exprs = null;
		List<SimpleTreeNode> newTmpNodes = null;
		List<SimpleTreeNode> nodes = null;
		List<SimpleASTNode> sASTNodes = null;
		SimpleASTCreator creator = new SimpleASTCreator();

		newTmpNodes = new ArrayList<SimpleTreeNode>();
		for (Node tmpNode : tmpNodes) {
			exprs = tmpNode.getASTExpressions2();
			sASTNodes = new ArrayList<SimpleASTNode>();
			for (ASTNode expr : exprs) {
				sASTNodes.add(creator.createSimpleASTNode(expr));
			}
			sASTNodes = SimpleASTNode.concretizeSingleNode(uToc, sASTNodes);
			newSNode = new SimpleTreeNode(node.getNodeType(),
					SourceCodeRange.getDefaultScr(),
					PatternUtil.createStrValue(node.getNodeType(),
							node.getStrValue(), sASTNodes), 0);
			newTmpNodes.add(newSNode);
		}
		nodes = standardNodes.get(counter);
		int insertAt = -1;
		if (!nodes.isEmpty()) {
			SimpleTreeNode bestNode = null;
			for (SimpleTreeNode standardNode : nodes) {
				bestNode = BestMatchFinder.findBestMatchNode(newTmpNodes,
						standardNode);
				if (bestNode == null) {
					SimpleTreeNode sParent = null;
					SimpleTreeNode sNewNode = null;
					sParent = (SimpleTreeNode) sNode.getParent();
					insertAt = sParent.getIndex(sNode);
					sNewNode = new SimpleTreeNode(standardNode.getNodeType(),
							standardNode.getSourceCodeRange(),
							standardNode.getStrValue(),
							standardNode.getNodeIndex());
					if (insertAt != 0) {
						insertAt--;
					}
					sParent.insert(sNewNode, insertAt);
					return true;
				}
			}
		}
		counter++;
		return false;
	}

	protected boolean extendExtraEdits(Map<String, String> uToc) {
		Node node = null;
		ChangeSummary chgSum = null;
		SimpleTreeNode sNode;
		for (int i = 0; i < chgSums.size(); i++) {
			chgSum = chgSums.get(i);
			switch (chgSum.editType) {
			case DELETE:
				break;
			default:// insert, update, move
				sNode = sTree.lookforNodeBasedOnEditIndex(i);
				node = nodeMap.get(sNode);
				if (applySupplementaryEdits(
						methodAnalysisResult.getNodeDataDependence(node),
						sNode, node, uToc, dataNodes)) {
					return true;
				}
				break;
			}
		}
		return false;
	}
}
