package changeassistant.multipleexample.apply;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;

import changeassistant.change.group.edits.AbstractTreeEditOperation2;
import changeassistant.classhierarchy.ProjectResource;
import changeassistant.multipleexample.edit.STreeDeleteOperation;
import changeassistant.multipleexample.edit.STreeEditScript;
import changeassistant.multipleexample.edit.STreeInsertOperation;
import changeassistant.multipleexample.edit.STreeMoveOperation;
import changeassistant.multipleexample.edit.STreeUpdateOperation;
import changeassistant.multipleexample.main.CachedProjectMap;
import changeassistant.multipleexample.main.EnhancedChangeAssistantMain;
import changeassistant.multipleexample.match.MatchResult;
import changeassistant.multipleexample.match.PatternUtil;
import changeassistant.multipleexample.match.ProgramTransformationGenerator;
import changeassistant.multipleexample.partition.SimpleASTCreator;
import changeassistant.multipleexample.partition.datastructure.ChangeSummary;
import changeassistant.multipleexample.partition.datastructure.EditInCommonCluster;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.multipleexample.util.PathUtil;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.comparison.ChangedMethodADT;

public class EditScriptApplier {

	protected Map<Integer, Integer> indexMap = null;

	public SimpleTreeNode apply(List<MatchResult> mResults, int counter,
			int tmpCounter, EditInCommonCluster cluster) {
		STreeEditScript abstractEditScript = cluster.getAbstractEditScript();
		List<ChangeSummary> chgSums = cluster.getConChgSum();
		List<List<List<SimpleASTNode>>> simpleExprsLists = cluster
				.getSimpleExprsLists();
		List<List<List<SimpleASTNode>>> concretizedSimpleExprsLists = null;
		ChangeSummary cs = null;
		List<Integer> knownNodeIndexes = cluster.getSequence().getNodeIndexes();
		List<Integer> editedNodeIndexes = null;
		List<Integer> tmpNodeIndexes = null;

		// List<List<SimpleASTNode>> concretizedSNodesList = null;
		List<SimpleASTNode> sNodes = null;
		List<List<SimpleASTNode>> tmpSNodesList = null;
		Map<Integer, STreeDeleteOperation> deletes = abstractEditScript
				.getDeletes();
		Map<Integer, AbstractTreeEditOperation2<SimpleTreeNode>> updateInsertMoves = abstractEditScript
				.getUpdateInsertMoves();
		SimpleTreeNode previousSibling = null;
		Integer tmpIndex1 = null;
		Integer tmpIndex2 = null;
		int insertLocation = -1;
		int customizedInsertLocation = -1;
		int insertAfterNodeIndex = -1;
		STreeUpdateOperation update = null;
		STreeInsertOperation insert = null;
		STreeMoveOperation move = null;

		MatchResult mResult = null;
		ChangedMethodADT adt = null;
		ProjectResource pr = null;
		// ExtraEditApplier eeApplier = new ExtraEditApplier(pr);
		Node methodNode = null;
		SimpleTreeNode sTree2 = null;
		SimpleTreeNode sTreeNode1 = null;
		SimpleTreeNode sTreeNode2 = null;
		SimpleTreeNode sTreeParent1 = null;
		SimpleTreeNode sTreeParent2 = null;
		// System.out.print("");

		if (mResults.size() == 0)
			return null;
		String path = PathUtil.createPath(counter, tmpCounter, mResults.get(0)
				.getADT().getProjectName());

		File f = new File(path);
		BufferedWriter output = null;
		int counterOfValidSuggestion = 0;
		try {
			output = new BufferedWriter(new FileWriter(f));
			Map<String, String> uToc = null;
			indexMap = new HashMap<Integer, Integer>();
			SimpleASTCreator creator = new SimpleASTCreator();

			for (int i = 0; i < mResults.size(); i++) {
				editedNodeIndexes = new ArrayList<Integer>(knownNodeIndexes);
				mResult = mResults.get(i);
				uToc = mResult.getUtoC();
				adt = mResult.getADT();
				System.out.println(adt.toString());
				System.out.print("");
				pr = CachedProjectMap.get(adt.getProjectName());
				output.write(pr.projectName + "\t" + adt.toString() + "\n");

				methodNode = pr.findClassContext(adt.classname).getMethodNode(
						adt.methodSignature);
				sTree2 = new SimpleTreeNode(methodNode, true, 1);
				concretizedSimpleExprsLists = new ArrayList<List<List<SimpleASTNode>>>();
				for (List<List<SimpleASTNode>> simpleExprsList : simpleExprsLists) {
					concretizedSimpleExprsLists.add(SimpleASTNode.concretize(
							uToc, simpleExprsList));
				}
				// concretizedSNodesList = SimpleASTNode.concretize(uToc,
				// sNodesList);
				if (EnhancedChangeAssistantMain.ABSTRACT_ALL) {
					List<List<List<SimpleASTNode>>> newConcretizedSimpleExprsLists = new ArrayList<List<List<SimpleASTNode>>>();
					// List<List<SimpleASTNode>> newConcretizedSNodesList = new
					// ArrayList<List<SimpleASTNode>>();
					Map<String, String> unifiedToSpecific = cluster
							.getUnifiedToSpecificList().get(0);
					for (List<List<SimpleASTNode>> simpleExprsList : concretizedSimpleExprsLists) {
						newConcretizedSimpleExprsLists
								.add(SimpleASTNode.concretize(
										unifiedToSpecific, simpleExprsList));
					}
					concretizedSimpleExprsLists = newConcretizedSimpleExprsLists;
				}
				tmpSNodesList = creator.createSimpleASTNodesList(methodNode);
				sTree2 = ProgramTransformationGenerator.normalizeTree(sTree2,
						tmpSNodesList);
				tmpNodeIndexes = new ArrayList<Integer>(mResult.getSequence()
						.getNodeIndexes());
				for (int j = 0; j < tmpNodeIndexes.size(); j++) {
					indexMap.put(knownNodeIndexes.get(j), tmpNodeIndexes.get(j));
				}
				for (int j = 0; j < chgSums.size(); j++) {
					cs = chgSums.get(j);
					System.out.print("");
					// the first phase: to apply insert, update, and move until
					// reaching a delete
					switch (cs.editType) {
					case DELETE:
						if (deletes.get(j) != null) {
							tmpIndex1 = deletes.get(j).getNode().getNodeIndex();
							tmpIndex2 = indexMap.get(tmpIndex1);
							NodeIndexMaintainer.changeIndexesForDelete(
									tmpIndex1, editedNodeIndexes);
							NodeIndexMaintainer.changeIndexesForDelete(
									tmpIndex2, tmpNodeIndexes);
							sTree2.lookforNodeBasedOnIndex(
									indexMap.get(deletes.get(j).getNode()
											.getNodeIndex()))
									.removeFromParent();
							indexMap.remove(tmpIndex1);
						}
						break;
					case UPDATE:
						update = (STreeUpdateOperation) updateInsertMoves
								.get(j);
						sTreeNode2 = sTree2.lookforNodeBasedOnIndex(indexMap
								.get(update.getNode().getNodeIndex()));
						sTreeNode1 = update.getNewNode();
						sNodes = concretizedSimpleExprsLists.get(j).get(1);
						tmpSNodesList
								.set(sTreeNode2.getNodeIndex() - 1, sNodes);
						sTreeNode2.setStrValue(PatternUtil.createStrValue(
								sTreeNode1.getNodeType(),
								sTreeNode1.getStrValue(), sNodes));
						sTreeNode2.setNodeType(sTreeNode1.getNodeType());
						sTreeNode2.getEditAndRoletype().put(j,
								SimpleTreeNode.EDITED);
						break;
					case MOVE:
						move = (STreeMoveOperation) updateInsertMoves.get(j);
						sTreeNode1 = move.getNode();
						sTreeNode2 = sTree2.lookforNodeBasedOnIndex(indexMap
								.get(sTreeNode1.getNodeIndex()));
						sTreeParent1 = move.getNewParent();
						sTreeParent2 = sTree2.lookforNodeBasedOnIndex(indexMap
								.get(sTreeParent1.getNodeIndex()));
						insertLocation = move.getLocation();
						if (insertLocation == 0) {
							insertAfterNodeIndex = sTreeParent1.getNodeIndex();
						} else {
							previousSibling = (SimpleTreeNode) sTreeParent1
									.getChildAt(insertLocation - 1);
							insertAfterNodeIndex = -previousSibling
									.getNodeIndex();
						}
						NodeIndexMaintainer.changeIndexesForMove(
								insertAfterNodeIndex,
								sTreeNode1.getNodeIndex(), editedNodeIndexes);
						if (insertLocation == 0) {
							insertAfterNodeIndex = sTreeParent2.getNodeIndex();
							customizedInsertLocation = 0;
						} else {
							previousSibling = sTree2
									.lookforNodeBasedOnIndex(indexMap
											.get(previousSibling.getNodeIndex()));
							insertAfterNodeIndex = -previousSibling
									.getNodeIndex();
							customizedInsertLocation = previousSibling
									.getParent().getIndex(previousSibling) + 1;
							if (!sTreeParent2.equals(previousSibling
									.getParent())) {
								customizedInsertLocation = getLocationWRTParent(
										(SimpleTreeNode) previousSibling
												.getParent(),
										sTreeParent2, customizedInsertLocation);
							}
						}
						System.out.print("");
						NodeIndexMaintainer.changeIndexesForMove(
								insertAfterNodeIndex,
								sTreeNode2.getNodeIndex(), tmpNodeIndexes);
						sTreeParent2.insert(sTreeNode2,
								customizedInsertLocation);
						sTreeNode2.getEditAndRoletype().put(j,
								SimpleTreeNode.EDITED);
						break;
					case INSERT:
						insert = (STreeInsertOperation) updateInsertMoves
								.get(j);
						sTreeNode1 = insert.getNode();
						sTreeParent1 = insert.getParentNode();
						sTreeParent2 = sTree2.lookforNodeBasedOnIndex(indexMap
								.get(sTreeParent1.getNodeIndex()));
						// update node indexes
						insertLocation = insert.getLocation();
						if (insertLocation == 0) {
							insertAfterNodeIndex = sTreeParent1.getNodeIndex();
						} else {
							previousSibling = (SimpleTreeNode) sTreeParent1
									.getChildAt(insert.getLocation() - 1);
							insertAfterNodeIndex = -previousSibling
									.getNodeIndex();
						}
						NodeIndexMaintainer
								.changeIndexesForInsert(editedNodeIndexes
										.indexOf(insertAfterNodeIndex),
										sTreeNode1.getNodeIndex(),
										editedNodeIndexes);
						if (insertLocation == 0) {
							insertAfterNodeIndex = sTreeParent2.getNodeIndex();
							customizedInsertLocation = 0;
						} else {
							previousSibling = sTree2
									.lookforNodeBasedOnIndex(indexMap
											.get(previousSibling.getNodeIndex()));
							insertAfterNodeIndex = -previousSibling
									.getNodeIndex();
							customizedInsertLocation = previousSibling
									.getParent().getIndex(previousSibling) + 1;
							if (!sTreeParent2.equals(previousSibling
									.getParent())) {
								customizedInsertLocation = getLocationWRTParent(
										(SimpleTreeNode) previousSibling
												.getParent(),
										sTreeParent2, customizedInsertLocation);
							}
						}
						int newNodeIndex = tmpSNodesList.size() + 1;
						NodeIndexMaintainer.changeIndexesForInsert(
								tmpNodeIndexes.indexOf(insertAfterNodeIndex),
								newNodeIndex, tmpNodeIndexes);
						sNodes = concretizedSimpleExprsLists.get(j).get(0);
						// change tree structure
						sTreeNode2 = new SimpleTreeNode(
								sTreeNode1.getNodeType(),
								sTreeNode1.getSourceCodeRange(),
								PatternUtil.createStrValue(
										sTreeNode1.getNodeType(),
										sTreeNode1.getStrValue(), sNodes),
								newNodeIndex);
						sTreeParent2.insert(sTreeNode2,
								customizedInsertLocation);
						// update indexMap
						indexMap.put(sTreeNode1.getNodeIndex(), newNodeIndex);
						// update tmpSNodesList
						tmpSNodesList.add(sNodes);
						sTreeNode2.getEditAndRoletype().put(j,
								SimpleTreeNode.EDITED);
						break;
					}
				}
				// inserted to allow clone reduction
				mResult.setAfterSTreeNode(sTree2);
				String newMethodString = createMethodDeclarationString(sTree2,
						methodNode.getMethodDeclaration().toString());
				// eeApplier.apply(uToc, newMethodString, adt, sTree2, cluster);
				if (newMethodString != null) {
					counterOfValidSuggestion++;
					output.write(newMethodString + "\n");
					mResult.setMethodString(newMethodString);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				output.write("There are " + counterOfValidSuggestion
						+ " methods found and changed" + "\n");
				output.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return sTreeNode2;
	}
	
	public static String removeUselessChars(String string, char c){
		if(!string.contains(Character.toString(c)))
			return string;
		StringBuffer buffer = new StringBuffer();
		char tmpChar;
		for(int i = 0; i < string.length(); i++){
			tmpChar = string.charAt(i);
			if(tmpChar != c){
				buffer.append(tmpChar);
			}
		}
		return buffer.toString();
	}

	public static String createMethodDeclarationString(SimpleTreeNode sTree2,
			String oldString) {		
		Block block = (Block) CodeGenerator.create(sTree2,
				ASTParser.K_STATEMENTS);
		String headOfMethodString = null;
		String tmpCommentString = null;
		oldString = removeUselessChars(oldString, '\r').trim();
		while(oldString.startsWith("/*") || oldString.startsWith("//")){
			if(oldString.startsWith("/*")){
				int endOfComment = oldString.indexOf("*/") + 2;
				tmpCommentString = oldString.substring(0, endOfComment);
				oldString = oldString.substring(endOfComment).trim();
			}else{
				oldString = oldString.substring(oldString.indexOf("\n")).trim();
			}
		}		
		if (oldString.contains("{")){
			headOfMethodString = oldString.substring(0,
					oldString.indexOf('{'));
			if(tmpCommentString != null)
				headOfMethodString = tmpCommentString + "\n" + headOfMethodString;
		}
		else
			return null;
		if (!block.statements().isEmpty()) {
			String newMethodString = headOfMethodString + ""
					+ block.statements().get(0).toString();
//			System.out.println(newMethodString);
			return newMethodString;
		}
		return null;
	}

	private int getLocationWRTParent(SimpleTreeNode tmpParent,
			SimpleTreeNode sTreeParent2, int customizedInsertLocation) {
		int currentInsertLocation = customizedInsertLocation;
		while (tmpParent != null && !tmpParent.equals(sTreeParent2)) {
			currentInsertLocation = tmpParent.getParent().getIndex(tmpParent) + 1;
			tmpParent = (SimpleTreeNode) tmpParent.getParent();
		}
		customizedInsertLocation = currentInsertLocation;
		return customizedInsertLocation;
	}
}
