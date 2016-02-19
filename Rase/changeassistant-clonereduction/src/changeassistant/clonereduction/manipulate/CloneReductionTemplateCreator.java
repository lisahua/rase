package changeassistant.clonereduction.manipulate;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import changeassistant.clonereduction.main.CloneReductionException;
import changeassistant.clonereduction.main.CloneReductionMain;
import changeassistant.clonereduction.pattern.MethodExtractionPattern;
import changeassistant.multipleexample.apply.CodeGenerator;
import changeassistant.multipleexample.datastructure.MapList;
import changeassistant.multipleexample.match.PatternUtil;
import changeassistant.multipleexample.partition.SimpleASTCreator;
import changeassistant.multipleexample.partition.datastructure.EditInCommonCluster;
import changeassistant.multipleexample.partition.datastructure.EditInCommonGroup;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.comparison.MethodModification;
import changeassistant.versions.treematching.edits.AbstractTreeEditOperation;
import changeassistant.versions.treematching.edits.ITreeEditOperation.EDIT;

public class CloneReductionTemplateCreator {
	
	private List<List<List<SimpleASTNode>>> customizedNodesLists = null;
	
	private MapList specificToUnifiedList;

	private List<List<SimpleTreeNode>> pSNodesOldList;
	
	private List<List<SimpleTreeNode>> pSNodesNewList;
	
	private List<String> customizedCodeStrings;
	
	private List<Node> oldNodes;
	
	private List<Node> newNodes;
	
	private EditInCommonCluster cluster;
	
	private MethodExtractionPattern pat = null;
	
	public void createTemplate(EditInCommonCluster cluster,
			EditInCommonGroup group, CloneReductionController crc) throws CloneReductionException{
		if(!CloneReductionMain.hasMultiExamples){
			// The original implementation before 06/29 in which psNodesList is converted from new version to old version	 
			//1. convert pSNodesList in new version to pSNodesList in old version
				convert(cluster, group.getMMList());
			//2. extract common representation from pSNodesList, which will be used to double check clone reduction's opportunity
				extractCommon();
			//3. check extractability
				checkExtractable();
		} else{
			
		}
	}
	
	public MethodExtractionPattern getTemplate(){
		return pat;
	}
	/**
	 * Convert psNodesListNew in crFilter to psNodesListOld
	 * @param mmList
	 * @param crFilter
	 */
	private void convert(EditInCommonCluster cluster, List<MethodModification> mmList){
		System.out.print("");
		this.cluster = cluster;
		MethodModification mm = null;
		List<AbstractTreeEditOperation> edits = null;
		AbstractTreeEditOperation edit = null;
		Node updatedNode = null, oldNode = null, newNode = null, nTmp = null;
		SimpleTreeNode sOldNode = null, sNewNode = null, sTmp = null;
		int tmpIndex = -1;
		List<SimpleTreeNode> pSNodesNew = null;
		List<Integer> instances = cluster.getInstances();
		List<List<Integer>> indexesList = cluster.getIndexesList();
		pSNodesOldList = new ArrayList<List<SimpleTreeNode>>();
		oldNodes = new ArrayList<Node>();
		newNodes = new ArrayList<Node>();
		List<Integer> indexes = null;
		Enumeration<SimpleTreeNode> sEnum = null;
		for(int i = 0; i < mmList.size(); i++){
			mm = mmList.get(i);
			if(!instances.contains(i))
				continue;
			tmpIndex = instances.indexOf(i);
			edits = mm.getEdits();
			oldNode = (Node)edits.get(0).getParentNode().getRoot();
			oldNodes.add(oldNode);
			sOldNode = new SimpleTreeNode(oldNode, true, 1);
			updatedNode = edits.get(edits.size() - 2).getNode();
			newNode = edits.get(edits.size() - 1).getNode();
			newNodes.add(newNode);
			sNewNode = new SimpleTreeNode(newNode, true, 1);
			pSNodesNew = pSNodesNewList.get(tmpIndex);
			for(SimpleTreeNode pSNodeNew : pSNodesNew){
				sEnum = pSNodeNew.breadthFirstEnumeration();
				while(sEnum.hasMoreElements()){
					sTmp = sEnum.nextElement();
					if(CloneReductionMain.refactoringOld){
						nTmp = SimpleTreeNode.lookforNodeBasedOnRange(oldNode, sOldNode.lookforNodeBasedOnIndex(sTmp.getNodeIndex()));
					}else{
						nTmp = SimpleTreeNode.lookforNodeBasedOnRange(newNode, sNewNode.lookforNodeBasedOnIndex(sTmp.getNodeIndex()));
						nTmp = (Node)updatedNode.lookforNodeBasedOnPosition(nTmp);
					}
					nTmp = oldNode.lookforNodeBasedOnRange(nTmp);
					if(nTmp != null){
						sTmp = sOldNode.lookforNodeBasedOnRange(nTmp);
						sTmp.getEditAndRoletype().put(0, SimpleTreeNode.EDITED);
					}
				}								
			}
			indexes = indexesList.get(tmpIndex);
			for(Integer index : indexes){
				edit = edits.get(index);
				if(edit.getOperationType().equals(EDIT.DELETE) ||edit.getOperationType().equals(EDIT.UPDATE)){
					sTmp = sOldNode.lookforNodeBasedOnRange(edit.getNode());
					sTmp.getEditAndRoletype().put(0, SimpleTreeNode.EDITED);
				}
			}
			pSNodesOldList.add(sOldNode.pruneRelevant());			
		}
	}
	
	private void extractCommon(){
		specificToUnifiedList = new MapList(cluster.getSpecificToUnifiedList());
		customize();
	}
	
	private void extractCommon2(){
		
	}
	
	private void customize(){
		List<SimpleTreeNode> pSNodesOld = null;
		Map<String, String> uMap = null;
		List<List<SimpleASTNode>> customizedNodesList = null;
		Enumeration<SimpleTreeNode> sEnum = null;
		SimpleASTCreator creator = new SimpleASTCreator();
		SimpleTreeNode sTmp = null;
		customizedCodeStrings = new ArrayList<String>();
		customizedNodesLists = new ArrayList<List<List<SimpleASTNode>>>();
		Map<String, String> sTou = null;
		StringBuffer buffer = null;
		for(int i = 0; i < pSNodesOldList.size(); i++){
			pSNodesOld = pSNodesOldList.get(i);
			sTou = specificToUnifiedList.get(i);
			customizedNodesList = SimpleASTNode.customize(sTou, 
					creator.createSimpleASTNodesList(oldNodes.get(i)));
			for(SimpleTreeNode pSNode : pSNodesOld){
				sEnum = pSNode.breadthFirstEnumeration();
				while(sEnum.hasMoreElements()){
					sTmp = sEnum.nextElement();
					sTmp.setStrValue(PatternUtil.createStrValue(sTmp.getNodeType(), sTmp.getStrValue(), 
							customizedNodesList.get(sTmp.getNodeIndex() - 1)));
				}
			}
			buffer = new StringBuffer();
			for(SimpleTreeNode pSNode : pSNodesOld){
				CodeGenerator.createCode(pSNode, buffer);
			}
			customizedCodeStrings.add(buffer.toString());
			customizedNodesLists.add(customizedNodesList);
		}		
	}
	
	private void checkExtractable() throws CloneReductionException{
		if(!customizedCodeStrings.get(0).equals(customizedCodeStrings.get(1))){
			DiffResolver resolver = new DiffResolver();
			resolver.resolveDiff(pSNodesOldList, specificToUnifiedList, customizedNodesLists);
			customize();
		}
		pat = new MethodExtractionPattern();
		pat.setpSNodes(pSNodesOldList.get(0));
		pat.setSimpleASTNodesList(customizedNodesLists.get(0));
		pat.collectFeatures();
	}
}
