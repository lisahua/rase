package changeassistant.changesuggestion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;

import changeassistant.change.group.edits.AbstractTreeEditOperation2;
import changeassistant.change.group.edits.SubTreeDeleteOperation;
import changeassistant.change.group.edits.SubTreeInsertOperation;
import changeassistant.change.group.edits.SubTreeMoveOperation;
import changeassistant.change.group.edits.SubTreeUpdateOperation;
import changeassistant.change.group.model.MatchingInfo;
import changeassistant.change.group.model.SubTreeModel;
import changeassistant.changesuggestion.astrewrite.ASTRewriteBasedManipulator2;
import changeassistant.changesuggestion.astrewrite.RewriteException;
import changeassistant.changesuggestion.expression.representation.MethodNameTerm;
import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.TypeNameTerm;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;
import changeassistant.classhierarchy.ProjectResource;
import changeassistant.model.TransformationRule;
import changeassistant.peers.SubTreeModelPair;
import changeassistant.versions.comparison.ChangedMethodADT;
import changeassistant.versions.comparison.MethodModification;
import changeassistant.versions.treematching.edits.ITreeEditOperation.EDIT;

public class ChangeSuggestion2 {

	private ProjectResource prLeft, prRight;

	private List<MethodModification> methodModifications;

	private Map<SubTreeModel, List<TransformationRule>> subTreeMaps;

	private Map<SubTreeModel, Set<ChangedMethodADT>> map;

	private ASTRewriteBasedManipulator2 aManipulator;

	private List<String> primitiveTypes;

	private List<Integer> literalTypes;

	public ChangeSuggestion2(ProjectResource prLeft) {
		this.prLeft = prLeft;
		this.aManipulator = new ASTRewriteBasedManipulator2();
		String[] ss = new String[] { "byte", "short", "char", "int", "long",
				"float", "double", "boolean" };
		this.primitiveTypes = Arrays.asList(ss);
		this.literalTypes = Arrays.asList(new Integer[] {// primitive literal
				ASTNode.BOOLEAN_LITERAL, ASTNode.CHARACTER_LITERAL,
						ASTNode.NUMBER_LITERAL, ASTNode.NULL_LITERAL });
	}

	public ChangeSuggestion2(ProjectResource prLeft, ProjectResource prRight,
			List<MethodModification> methodModifications,
			Map<SubTreeModel, List<TransformationRule>> subTreeMaps,
			Map<SubTreeModel, Set<ChangedMethodADT>> map) {
		this.prLeft = prLeft;
		this.prRight = prRight;
		this.methodModifications = methodModifications;
		this.subTreeMaps = subTreeMaps;
		this.map = map;
		this.aManipulator = new ASTRewriteBasedManipulator2();
	}

	/*
	 * public void suggestChanges(){ // String methodSignature =
	 * "org.eclipse.compare.BufferedContent  removeContentChangeListener(IContentChangeListener)"
	 * ; String methodSignature =
	 * "org.eclipse.compare.internal.patch.PreviewPatchPage2  guessFuzzFactor(WorkspacePatcher)"
	 * ; Set<SubTreeModel> subTreeKeys =
	 * lookforConcernedSubTreeModel(methodSignature);
	 * if(!subTreeKeys.isEmpty()){ for(SubTreeModel subTreeKey : subTreeKeys){
	 * List<TransformationRule> trs =
	 * lookforConcernedTransformationRule(subTreeKey, methodSignature);
	 * Set<ChangedMethodADT> peers = map.get(subTreeKey); if(trs != null){
	 * for(TransformationRule tr : trs){ if(subTreeKey.equals(new
	 * SubTreeModel(tr.subTreeModel))){ //do nothing }else{ System.out.println(
	 * "The subtree key is different from the subTreeModel kept in the transformation rule"
	 * ); } for(ChangedMethodADT peer : peers){
	 * if(peer.toString().contains(methodSignature)){ //do nothing }else{
	 * apply(peer, tr); } } } } } }else{ System.err.println(
	 * "The sub tree corresponds to the concerned method is not found!"); } }
	 */
	public boolean apply(ChangedMethodADT peer, TransformationRule tr,
			MatchingInfo matchingInfo) {
		boolean flag = true;
		List<AbstractTreeEditOperation2<SubTreeModel>> editScriptOnCandidate = null;
		editScriptOnCandidate = createEditScriptsOnCandidateNode(tr.editScript,
				matchingInfo, matchingInfo.getSubTree(),
				matchingInfo.getCandidate());
		if (editScriptOnCandidate == null)// not applicable due to term
											// replacement
			return false;
		System.out.print("");
		try {
			aManipulator.manipulate(prLeft, prRight, editScriptOnCandidate,
					peer);
		} catch (RewriteException e) {
			// e.printStackTrace();
			flag = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!flag) {
			// to replace the changed file with the backup file
			aManipulator.rollBack(prLeft, prRight, editScriptOnCandidate, peer);
		}
		editScriptOnCandidate = null;
		matchingInfo = null;
		return flag;
	}

	public ASTRewriteBasedManipulator2 getManipulator() {
		return this.aManipulator;
	}

	/**
	 * The binding information is collected within the method node If no binding
	 * is available for a specific type name, simply use the orginal variable
	 * name
	 * 
	 * @param candidate
	 * @return
	 */
	// private Map<String, Set<Term>> collectBindings(SubTreeModel candidate){
	// Map<String, Set<Term>> type_variable_bindings = new HashMap<String,
	// Set<Term>>();
	// Enumeration<SubTreeModel> enumeration =
	// candidate.breadthFirstEnumeration();
	// SubTreeModel temp;
	// List<List<Term>> tempAbstractExpressions;
	// List<Term> tempAbstractExpression;
	// Term tempTerm;
	// String typeName;
	// Set<Term> termSet;
	// while(enumeration.hasMoreElements()){
	// temp = enumeration.nextElement();
	// tempAbstractExpressions = temp.getAbstractExpressions();
	// for(int i = 0; i < tempAbstractExpressions.size(); i ++){
	// tempAbstractExpression = tempAbstractExpressions.get(i);
	// for(int j = 0; j < tempAbstractExpression.size(); j ++){
	// tempTerm = tempAbstractExpression.get(j);
	// if(tempTerm instanceof VariableTypeBindingTerm){
	// typeName = ((VariableTypeBindingTerm)
	// tempTerm).getTypeNameTerm().getAbstractName();
	// if(typeName.equals("type_name") || typeName.equals("method_name")){
	// //do nothing, the mapping will be set up later
	// continue;
	// }
	// if(type_variable_bindings.containsKey(typeName)){
	// type_variable_bindings.get(typeName).add(tempTerm);
	// }else{
	// termSet = new HashSet<Term>();
	// termSet.add(tempTerm);
	// type_variable_bindings.put(typeName, termSet);
	// }
	// }
	// }
	// }
	// }
	// return type_variable_bindings;
	// }

	// private void collectMethodAndTypeNameMap(List<SubTreeModelPair> pairs,
	// Map<MethodNameTerm, MethodNameTerm> methodNameMap,
	// Map<TypeNameTerm, TypeNameTerm> typeNameMap,
	// Map<VariableTypeBindingTerm, VariableTypeBindingTerm> variableNameMap){
	// SubTreeModel left, right;
	// List<List<Term>> abstractRepresentationsLeft,
	// abstractRepresentationsRight;
	// List<Term> termListLeft, termListRight;
	// Term termLeft, termRight;
	// int size;
	// for(SubTreeModelPair pair : pairs){
	// left = pair.getLeft();
	// right = pair.getRight();
	// abstractRepresentationsLeft = left.getAbstractExpressions();
	// abstractRepresentationsRight = right.getAbstractExpressions();
	// if(abstractRepresentationsLeft.size() !=
	// abstractRepresentationsRight.size()){
	// System.out.println("The two abstract representations do not have the same length!");
	// return;
	// }
	// size = abstractRepresentationsLeft.size();
	// for(int i = 0; i < size; i++){
	// termListLeft = abstractRepresentationsLeft.get(i);
	// termListRight = abstractRepresentationsRight.get(i);
	// if(termListLeft.size() != termListRight.size()){
	// System.out.println("The two term lists do not have the same length!");
	// return;
	// }
	//
	// for(int j = 0; j < termListLeft.size(); j++){
	// termLeft = termListLeft.get(j);
	// termRight = termListRight.get(j);
	// if(termLeft instanceof VariableTypeBindingTerm){
	// VariableTypeBindingTerm vTerm = (VariableTypeBindingTerm)termLeft;
	// variableNameMap.put(vTerm, (VariableTypeBindingTerm)termRight);
	// // if(vTerm.getTypeName().equals(METHOD_NAME)){
	// // methodNameMap.put(termRight, termLeft); //since the following will use
	// it in this way
	// // }else if(vTerm.getTypeName().equals(TYPE_NAME)){
	// // typeNameMap.put(termRight, termLeft);
	// // }
	// }else if(termLeft instanceof TypeNameTerm){
	// TypeNameTerm tTerm = (TypeNameTerm)termLeft;
	// typeNameMap.put(tTerm, (TypeNameTerm)termRight);
	// }else if(termLeft instanceof MethodNameTerm){
	// MethodNameTerm mTerm = (MethodNameTerm)termLeft;
	// methodNameMap.put(mTerm, (MethodNameTerm)termRight);
	// }
	// }
	// }
	// }
	// }

	private List<AbstractTreeEditOperation2<SubTreeModel>> createEditScriptsOnCandidateNode(
			List<AbstractTreeEditOperation2<SubTreeModel>> editScript,
			MatchingInfo matchingInfo, SubTreeModel subTree,
			SubTreeModel candidate) {
		Map<VariableTypeBindingTerm, VariableTypeBindingTerm> variableMap = matchingInfo
				.getVariableMap();
		Map<MethodNameTerm, MethodNameTerm> methodMap = matchingInfo
				.getMethodMap();
		Map<TypeNameTerm, TypeNameTerm> typeMap = matchingInfo.getTypeMap();
		List<SubTreeModelPair> pairs = new ArrayList<SubTreeModelPair>(
				matchingInfo.getMatchedList());
		System.out.print("");
		List<AbstractTreeEditOperation2<SubTreeModel>> editScriptOnCandidate = new ArrayList<AbstractTreeEditOperation2<SubTreeModel>>();
		SubTreeModel knownEditedNode, knownParent;
		List<SubTreeModel> knownSiblingsBefore = null, knownSiblingsAfter = null, sKnownSiblingsBefore = null, sKnownSiblingsAfter = null, cKnownSiblingsBefore = null, cKnownSiblingsAfter = null;
		SubTreeModel sEditedNode = null, sParent = null;
		SubTreeModel cEditedNode = null, cParent = null, candidateCopy = null;
		AbstractTreeEditOperation2<SubTreeModel> editOnCandidate = null, editCopyOnCandidate = null, editOnSubTree = null;
		candidateCopy = (SubTreeModel) candidate.deepCopy();
		int matchingIndex = -1;
		for (AbstractTreeEditOperation2<SubTreeModel> edit2 : editScript) {
			knownEditedNode = edit2.getNode();
			if (!edit2.getOperationType().equals(EDIT.INSERT)) {
				sEditedNode = (SubTreeModel) subTree
						.lookforNodeBasedOnPosition(knownEditedNode);
			}

			knownSiblingsBefore = edit2.getSiblingsBefore();
			knownSiblingsAfter = edit2.getSiblingsAfter();
			sKnownSiblingsBefore = subTree
					.lookforNodeBasedOnPositions(knownSiblingsBefore);
			sKnownSiblingsAfter = subTree
					.lookforNodeBasedOnPositions(knownSiblingsBefore);

			if (sEditedNode != null) {
				matchingIndex = sEditedNode.getMatchingIndex();
				cEditedNode = pairs.get(matchingIndex).getLeft();
				cKnownSiblingsBefore = getCandidateSiblingsBefore(cEditedNode);// edit
																				// type
																				// is
																				// INSERT
				cKnownSiblingsAfter = getCandidateSiblingsAfter(cEditedNode);
			} else {
				cKnownSiblingsBefore = getCandidateSiblingsBeforeForInsert(
						sKnownSiblingsBefore, pairs);
				cKnownSiblingsAfter = getCandidateSiblingsAfterForInsert(
						sKnownSiblingsAfter, pairs);
			}

			knownParent = edit2.getParentNode();
			if (knownParent == null) {
				// do nothing
			} else {
				sParent = (SubTreeModel) subTree
						.lookforNodeBasedOnPosition(knownParent);
				cParent = pairs.get(sParent.getMatchingIndex()).getLeft();
			}

			switch (edit2.getOperationType()) {
			case INSERT: {
				// System.out.print("");
				SubTreeInsertOperation insert = (SubTreeInsertOperation) edit2;
				SubTreeModel sNewNode = (SubTreeModel) insert.getNode().clone();
				if (!customize(sNewNode, methodMap, typeMap, variableMap)) {
					return null;
				}
				editOnSubTree = new SubTreeInsertOperation(sNewNode, sParent,
						sKnownSiblingsBefore, sKnownSiblingsAfter);
				cEditedNode = (SubTreeModel) sNewNode.clone();
				int position = computePosition(cKnownSiblingsBefore,
						cEditedNode);
				editOnCandidate = new SubTreeInsertOperation(cEditedNode,
						cParent, cKnownSiblingsBefore, cKnownSiblingsAfter,
						position);
				editCopyOnCandidate = new SubTreeInsertOperation(
						(SubTreeModel) cEditedNode.clone(),
						(SubTreeModel) candidateCopy
								.lookforNodeBasedOnPosition(cParent),
						cKnownSiblingsBefore, cKnownSiblingsAfter, position);
				editScriptOnCandidate.add(editCopyOnCandidate);
				candidateCopy = apply(editOnSubTree, editOnCandidate, candidate);

				matchingIndex = pairs.size();
				cEditedNode.setMatchingIndex(matchingIndex);
				sNewNode.setMatchingIndex(matchingIndex);
				pairs.add(new SubTreeModelPair(cEditedNode, sNewNode));
			}
				break;
			case DELETE: {
				editOnSubTree = new SubTreeDeleteOperation(sEditedNode,
						sParent, sKnownSiblingsBefore, sKnownSiblingsAfter);
				int position = computePosition(cKnownSiblingsBefore,
						cEditedNode);
				editOnCandidate = new SubTreeDeleteOperation(cEditedNode,
						cParent, cKnownSiblingsBefore, cKnownSiblingsAfter,
						position);
				editCopyOnCandidate = new SubTreeDeleteOperation(
						(SubTreeModel) candidateCopy
								.lookforNodeBasedOnPosition(cEditedNode),
						(SubTreeModel) candidateCopy
								.lookforNodeBasedOnPosition(cParent),
						cKnownSiblingsBefore, cKnownSiblingsAfter, position);
				editScriptOnCandidate.add(editCopyOnCandidate);
				candidateCopy = apply(editOnSubTree, editOnCandidate, candidate);
			}
				break;
			case MOVE: {
				SubTreeMoveOperation move = (SubTreeMoveOperation) edit2;
				knownParent = move.getNewParent();
				SubTreeModel sNewParent = (SubTreeModel) subTree
						.lookforNodeBasedOnPosition(knownParent);
				SubTreeModel cNewParent = pairs.get(
						sNewParent.getMatchingIndex()).getLeft();

				knownSiblingsBefore = move.getNewSiblingsBefore();
				knownSiblingsAfter = move.getNewSiblingsAfter();
				List<SubTreeModel> sNewSiblingsBefore = subTree
						.lookforNodeBasedOnPositions(knownSiblingsBefore);
				List<SubTreeModel> sNewSiblingsAfter = subTree
						.lookforNodeBasedOnPositions(knownSiblingsAfter);
				editOnSubTree = new SubTreeMoveOperation(sEditedNode, sParent,
						sKnownSiblingsBefore, sKnownSiblingsAfter, sNewParent,
						sNewSiblingsBefore, sNewSiblingsAfter);
				List<SubTreeModel> cNewSiblingsBefore = getCandidateSiblingsBeforeForInsert(
						sNewSiblingsBefore, pairs);
				List<SubTreeModel> cNewSiblingsAfter = getCandidateSiblingsAfterForInsert(
						sNewSiblingsAfter, pairs);
				int newPosition = computePosition(cNewSiblingsBefore,
						cEditedNode);
				editOnCandidate = new SubTreeMoveOperation(cEditedNode,
						cParent, cKnownSiblingsBefore, cKnownSiblingsAfter,
						cNewParent, cNewSiblingsBefore, cNewSiblingsAfter,
						newPosition);
				editCopyOnCandidate = new SubTreeMoveOperation(
						(SubTreeModel) candidateCopy
								.lookforNodeBasedOnPosition(cEditedNode),
						(SubTreeModel) candidateCopy
								.lookforNodeBasedOnPosition(cParent),
						cKnownSiblingsBefore, cKnownSiblingsAfter,
						(SubTreeModel) candidateCopy
								.lookforNodeBasedOnPosition(cNewParent),
						cNewSiblingsBefore, cNewSiblingsAfter, newPosition);
				editScriptOnCandidate.add(editCopyOnCandidate);
				candidateCopy = apply(editOnSubTree, editOnCandidate, candidate);
			}
				break;
			case UPDATE: {
				SubTreeUpdateOperation update = (SubTreeUpdateOperation) edit2;
				SubTreeModel sNewNode = (SubTreeModel) update.getNewNode()
						.clone();
				if (!customize(sNewNode, methodMap, typeMap, variableMap))
					return null;
				// to customize the standard update in order to be adjusted to
				// the new context
				editOnSubTree = new SubTreeUpdateOperation(sEditedNode,
						sParent, sKnownSiblingsBefore, sKnownSiblingsAfter,
						sNewNode);
				editOnCandidate = new SubTreeUpdateOperation(cEditedNode,
						cParent, cKnownSiblingsBefore, cKnownSiblingsAfter,
						(SubTreeModel) sNewNode.clone());
				editCopyOnCandidate = new SubTreeUpdateOperation(
						(SubTreeModel) candidateCopy
								.lookforNodeBasedOnPosition(cEditedNode),
						(SubTreeModel) candidateCopy
								.lookforNodeBasedOnPosition(cParent),
						cKnownSiblingsBefore, cKnownSiblingsAfter,
						(SubTreeModel) sNewNode.clone());
				editScriptOnCandidate.add(editCopyOnCandidate);
				candidateCopy = apply(editOnSubTree, editOnCandidate, candidate);
			}
				break;
			}
			knownEditedNode = knownParent = null;
			knownSiblingsBefore = knownSiblingsAfter = null;
			sKnownSiblingsBefore = sKnownSiblingsAfter = null;
			cKnownSiblingsBefore = cKnownSiblingsAfter = null;
			sEditedNode = sParent = null;
			cEditedNode = cParent = null;
			editOnCandidate = editCopyOnCandidate = editOnSubTree = null;
		}
		variableMap = null;
		methodMap = null;
		typeMap = null;
		pairs = null;
		candidateCopy = null;
		editScript = null;
		return editScriptOnCandidate;
	}

	private int computePosition(List<SubTreeModel> cKnownSiblingsBefore,
			SubTreeModel cEditedNode) {
		int position = cKnownSiblingsBefore.size();
		if (position == 0 && cEditedNode.getStrValue().equals("else:")) {
			position = 1;
		} else if (cEditedNode.getStrValue().startsWith("catch:")) {
			boolean hasTry = false;
			for (SubTreeModel s : cKnownSiblingsBefore) {
				if (s.getStrValue().equals("try-body:")) {
					hasTry = true;
					break;
				}
			}
			if (!hasTry) {
				position++;
			}
		}
		return position;
	}

	private boolean customize(
			SubTreeModel sNode,
			Map<MethodNameTerm, MethodNameTerm> methodNameMap,
			Map<TypeNameTerm, TypeNameTerm> typeNameMap,
			Map<VariableTypeBindingTerm, VariableTypeBindingTerm> variableNameMap) {
		List<List<Term>> abstractExpressions = sNode.getAbstractExpressions();
		List<Term> abstractExpression;
		Term temp, beforeTerm, afterTerm;
		boolean success = true;
		Set<String> strOfReferent = new HashSet<String>();
		Set<Integer> nodeTypeOfReferent = new HashSet<Integer>();
		for (int i = 0; i < abstractExpressions.size(); i++) {
			strOfReferent.clear();
			nodeTypeOfReferent.clear();
			abstractExpression = abstractExpressions.get(i);
			for (int j = 0; j < abstractExpression.size(); j++) {
				temp = abstractExpression.get(j);
				switch (temp.getTermType()) {
				case VariableTypeBindingTerm: {
					for (Entry<VariableTypeBindingTerm, VariableTypeBindingTerm> entry : variableNameMap
							.entrySet()) {
						if (entry.getValue().equals(temp)) {
							temp = entry.getKey();
							break;
						}
					}
				}
					break;
				case TypeNameTerm: {
					for (Entry<TypeNameTerm, TypeNameTerm> entry : typeNameMap
							.entrySet()) {
						if (entry.getValue().equals(temp)) {
							temp = entry.getKey();
							break;
						}
					}
				}
					break;
				case MethodNameTerm: {
					for (Entry<MethodNameTerm, MethodNameTerm> entry : methodNameMap
							.entrySet()) {
						if (entry.getValue().equals(temp)) {
							temp = entry.getKey();
							break;
						}
					}
				}
					break;
				case Term: {
					if (temp.getName().equals(".")) {
						assert (j > 0 && j < abstractExpression.size() - 1);
						beforeTerm = abstractExpression.get(j - 1);
						afterTerm = abstractExpression.get(j + 1);
						if (beforeTerm instanceof VariableTypeBindingTerm
								&& afterTerm instanceof VariableTypeBindingTerm) {
							strOfReferent
									.add(((VariableTypeBindingTerm) beforeTerm)
											.getTypeNameTerm().getName());
							nodeTypeOfReferent.add(beforeTerm.getNodeType());
						}
					}
				}
					break;
				}
				// check whether primitive type is referred to
				strOfReferent.retainAll(primitiveTypes);
				nodeTypeOfReferent.retainAll(literalTypes);
				if (!strOfReferent.isEmpty() || !nodeTypeOfReferent.isEmpty()) {
					success = false;
					return success;
				}
			}
		}
		return success;
	}

	private List<SubTreeModel> getCandidateSiblingsAfter(
			SubTreeModel cEditedNode) {
		List<SubTreeModel> cKnownSiblingsAfter = new ArrayList<SubTreeModel>();
		SubTreeModel temp = (SubTreeModel) cEditedNode.getNextSibling();
		while (temp != null) {
			cKnownSiblingsAfter.add(temp);
			temp = (SubTreeModel) temp.getNextSibling();
		}
		return cKnownSiblingsAfter;
	}

	private List<SubTreeModel> getCandidateSiblingsBefore(
			SubTreeModel cEditedNode) {
		List<SubTreeModel> cKnownSiblingsBefore = new ArrayList<SubTreeModel>();
		SubTreeModel temp = (SubTreeModel) cEditedNode.getPreviousSibling();
		while (temp != null) {
			cKnownSiblingsBefore.add(0, temp);
			temp = (SubTreeModel) temp.getPreviousSibling();
		}
		return cKnownSiblingsBefore;
	}

	private List<SubTreeModel> getCandidateSiblingsAfter(
			List<SubTreeModel> sKnownSiblingsBefore,
			List<SubTreeModelPair> pairs) {
		List<SubTreeModel> cKnownSiblingsAfter = new ArrayList<SubTreeModel>();
		int matchingIndex;
		SubTreeModel cSibling;
		if (!sKnownSiblingsBefore.isEmpty()) {
			matchingIndex = sKnownSiblingsBefore.get(
					sKnownSiblingsBefore.size() - 1).getMatchingIndex();
			cSibling = pairs.get(matchingIndex).getLeft();
			while (cSibling != null) {
				// cKnownSiblingsBefore.add(0, cSibling);
				cSibling = (SubTreeModel) cSibling.getPreviousSibling();
			}
		}
		return cKnownSiblingsAfter;
	}

	/**
	 * The inserted node itself cannot be used to locate the position to insert
	 * 
	 * @param sKnownSiblingsBefore
	 * @param pairs
	 * @return
	 */
	private List<SubTreeModel> getCandidateSiblingsBeforeForInsert(
			List<SubTreeModel> sKnownSiblingsBefore,
			List<SubTreeModelPair> pairs) {
		List<SubTreeModel> cKnownSiblingsBefore = new ArrayList<SubTreeModel>();
		int matchingIndex;
		SubTreeModel cSibling;
		if (!sKnownSiblingsBefore.isEmpty()) {
			matchingIndex = sKnownSiblingsBefore.get(
					sKnownSiblingsBefore.size() - 1).getMatchingIndex();
			cSibling = pairs.get(matchingIndex).getLeft();
			while (cSibling != null) {
				cKnownSiblingsBefore.add(0, cSibling);
				cSibling = (SubTreeModel) cSibling.getPreviousSibling();
			}
		}
		cSibling = null;
		return cKnownSiblingsBefore;
	}

	private List<SubTreeModel> getCandidateSiblingsAfterForInsert(
			List<SubTreeModel> sKnownSiblingsAfter, List<SubTreeModelPair> pairs) {
		List<SubTreeModel> cKnownSiblingsAfter = new ArrayList<SubTreeModel>();
		int matchingIndex;
		SubTreeModel cSibling;
		if (!sKnownSiblingsAfter.isEmpty()) {
			matchingIndex = sKnownSiblingsAfter.get(0).getMatchingIndex();
			cSibling = pairs.get(matchingIndex).getLeft();
			while (cSibling != null) {
				cKnownSiblingsAfter.add(cSibling);
				cSibling = (SubTreeModel) cSibling.getNextSibling();
			}
		}
		return cKnownSiblingsAfter;
	}

	private boolean isTypeName(String name) {
		return name.charAt(0) >= 'A' && name.charAt(0) <= 'Z'
				|| primitiveTypes.contains(name);
	}

	private Set<SubTreeModel> lookforConcernedSubTreeModel(
			String methodSignature) {
		Set<SubTreeModel> subTreeKeys = new HashSet<SubTreeModel>();
		Set<Entry<SubTreeModel, Set<ChangedMethodADT>>> entries = map
				.entrySet();
		Set<ChangedMethodADT> adtSet = null;
		for (Entry<SubTreeModel, Set<ChangedMethodADT>> entry : entries) {
			adtSet = entry.getValue();
			for (ChangedMethodADT adt : adtSet) {
				if (adt.toString().contains(methodSignature)) {
					subTreeKeys.add(entry.getKey());
				}
			}
		}
		return subTreeKeys;
	}

	private List<TransformationRule> lookforConcernedTransformationRule(
			SubTreeModel key, String methodSignature) {
		Set<Entry<SubTreeModel, List<TransformationRule>>> entries = subTreeMaps
				.entrySet();
		for (Entry<SubTreeModel, List<TransformationRule>> entry : entries) {
			if (entry.getKey().isDeepCopyOf(key)) {
				Iterator<TransformationRule> iter = entry.getValue().iterator();
				TransformationRule tr = null;
				while (iter.hasNext()) {
					tr = iter.next();
					if (tr.originalMethod.toString().contains(methodSignature)) {
						return entry.getValue();
					}
				}
			}
		}
		return null;
	}

	private SubTreeModel apply(AbstractTreeEditOperation2 editOnSubTree,
			AbstractTreeEditOperation2 editOnCandidate, SubTreeModel candidate) {
		editOnSubTree.apply();
		editOnCandidate.apply();
		return (SubTreeModel) candidate.deepCopy();
	}
}
