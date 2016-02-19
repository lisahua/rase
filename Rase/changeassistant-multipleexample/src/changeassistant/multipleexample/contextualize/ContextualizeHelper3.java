//package changeassistant.multipleexample.contextualize;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.Enumeration;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.Queue;
//import java.util.Set;
//import java.util.Stack;
//
//import org.eclipse.ui.dialogs.ListSelectionDialog;
//
//import changeassistant.changesuggestion.expression.representation.ASTExpressionTransformer;
//import changeassistant.multipleexample.common.CommonParser;
//import changeassistant.multipleexample.contextualize.datastructure.Sequence;
//import changeassistant.multipleexample.contextualize.datastructure.SequenceComparator;
//import changeassistant.multipleexample.datastructure.Pair;
//import changeassistant.multipleexample.partition.datastructure.AbstractCluster;
//import changeassistant.multipleexample.partition.datastructure.BaseCluster;
//import changeassistant.multipleexample.partition.datastructure.ClusterHelper;
//import changeassistant.multipleexample.partition.datastructure.EditInCommonCluster;
//import changeassistant.multipleexample.partition.datastructure.EditInCommonGroup;
//import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
//import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
//import changeassistant.versions.comparison.MethodModification;
//
//public class ContextualizeHelper3 {
//	
//	private EditInCommonGroup group = null;
//	private List<EditInCommonCluster> clusters = null;
//	private List<Integer> involvedInstances = null;
//	private List<List<Integer>> encodedSequences = null;
//	
//	private Map<Integer, Integer> matchSet = null;
//	
//	private Map<String, Pair<Sequence>> dictionary = null;
//	private double[][] matchMatrix = null;
//	private List<List<SimpleASTNode>>[][] commonPartMatrix = null;
//	
//	private List<MethodModification> mmList;
//
//	public ContextualizeHelper3(EditInCommonGroup group, List<EditInCommonCluster> clusters,
//			List<MethodModification> mmList){
//		this.group = group;
//		this.clusters = clusters;
//		this.mmList = mmList;
//	}
//	
//	// the context is parsed based on bottom-up, breadth-first order 
//	public void parseCommonContext(){
//		List<EditInCommonCluster> level_1_clusters = ClusterHelper.getLevel_1_clusters(clusters);
//		BaseCluster bClus = null;
//		Set<Integer> processed = new HashSet<Integer>();
//		List<SimpleTreeNode> sTrees = null;
//		int tmpIndex = -1;
//		for(EditInCommonCluster level_1_cluster : level_1_clusters){
//			sTrees = level_1_cluster.getSTrees();
//			for(AbstractCluster aClus : level_1_cluster.getIncomings()){
//				tmpIndex = aClus.getIndex();
//				if(aClus instanceof BaseCluster && processed.add(tmpIndex)){
//					bClus = (BaseCluster)aClus;
//					bClus.setSTree(sTrees.get(level_1_cluster.getInstances().indexOf(tmpIndex)));
//					bClus.encodeSequence();
//					bClus.createSimpleASTNodesList();
//				}
//			}
//		}
//
//		List<List<SimpleASTNode>> simpleASTNodesList = null;
//		List<Sequence> sequenceList = null;
//		Sequence sequence = null;
//		List<EditInCommonCluster> high_clusters = level_1_clusters;
//		Set<EditInCommonCluster> new_high_clusters = new HashSet<EditInCommonCluster>();
//		AbstractCluster tmpClus = null;
//		List<AbstractCluster> tmpSubClusters = null;
//		
//		while(!high_clusters.isEmpty()){
//			for(EditInCommonCluster high_cluster : high_clusters){
//				if(high_cluster.allSameInstances()){
//					tmpClus = high_cluster.getIncomings().get(0);
//					
//					high_cluster.setSequence(tmpClus.getSequence());
//					
//					sequenceList = new ArrayList<Sequence>();
//					for(AbstractCluster aClus : high_cluster.getIncomings()){
//						sequenceList.add(aClus.getSequence());
//					}
//					high_cluster.setSequenceList(sequenceList);
//					high_cluster.setSimpleASTNodesList(tmpClus.getSimpleASTNodesList());
//					if(high_cluster.getOutgoings() != null)
//						new_high_clusters.addAll(high_cluster.getOutgoings());
//					continue;
//				}
//				high_cluster.customize();
//				simpleASTNodesList = new ArrayList<List<SimpleASTNode>>();
//				for(int i = 0; i < high_cluster.getSimpleASTNodesList(0).size(); i++){
//					simpleASTNodesList.add(new ArrayList<SimpleASTNode>());
//				}
//				sequenceList = new ArrayList<Sequence>();
//				tmpSubClusters = new ArrayList<AbstractCluster>();
//				for(AbstractCluster incoming : high_cluster.getIncomings()){
//					tmpSubClusters.add(incoming);
//				}
//				AbstractCluster bClus1 = tmpSubClusters.get(0);
//				AbstractCluster bClus2 = tmpSubClusters.get(1);
//				
//				System.out.print("");
//				sequence = bClus1.getSequence();
//				if(sequence != null){
//					sequence = 
//						match(bClus1.getSequence(), bClus2.getSequence(),
//							high_cluster.getSimpleASTNodesList(0), 
//							high_cluster.getSimpleASTNodesList(1),
//							high_cluster.getSpecificToUnifiedList().get(0),
//							high_cluster.getSpecificToUnifiedList().get(1),
//							high_cluster.getUnifiedToSpecificList().get(1),
//							sequenceList, simpleASTNodesList,
//							high_cluster.getForests().get(0),
//							high_cluster.getForests().get(1));
//					
//					for(int i = 2; sequence != null && i < tmpSubClusters.size(); i++){
//						bClus2 = tmpSubClusters.get(i);
//						sequence = match(sequence, bClus2.getSequence(),
//								simpleASTNodesList, bClus2.getSimpleASTNodesList(),
//								high_cluster.getSpecificToUnifiedList().get(0),
//								high_cluster.getSpecificToUnifiedList().get(i),
//								high_cluster.getUnifiedToSpecificList().get(i),
//								sequenceList, simpleASTNodesList,
//								high_cluster.getForests().get(0),
//								high_cluster.getForests().get(i));
//					}
//				}
//				high_cluster.setSequence(sequence);
//				if(sequence == null){
//					high_cluster.setApplicable(false);//because no pattern can be parsed out 
//				}else if(high_cluster.getOutgoings() != null){
//					high_cluster.setSequenceList(sequenceList);
//					new_high_clusters.addAll(high_cluster.getOutgoings());
//				}
//				high_cluster.setSimpleASTNodesList(simpleASTNodesList);
//			}
//			high_clusters = new ArrayList<EditInCommonCluster>(new_high_clusters);
//			new_high_clusters.clear();
//		}
//	}
//	
//	private double calcSimilarity(List<SimpleASTNode> nodes1, List<SimpleASTNode> nodes2,
//			Map<String, String> specificToUnified, Map<String, String> unifiedToSpecific){
//		CommonParser parser = new CommonParser();
//		SimpleASTNode commonNode = null;
//		int min = Math.min(nodes1.size(), nodes2.size());
//		/*if(min == 0){
//			if(nodes1.size() == 0 && nodes2.size() == 0)
//				return 1;
//			return 0;
//		}*/
//		SimpleASTNode node1 = null, node2 = null;
//		int commonCounter = 0;
//		int allCounter = 0;
//		Enumeration<SimpleASTNode> sEnum = null;
//		String gStr = null;
//		SimpleASTNode sNode = null;
//		for(int i = 0; i < min; i++){
//			node1 = nodes1.get(i);
//			node2 = nodes2.get(i);
//			if(node1.getStrValue().equals(node2.getStrValue()))
//				return 1;
//			System.out.print("");
//			commonNode = parser.getCommon(node1, node2);
//			if(commonNode == null)
//				return 0;
//			sEnum = commonNode.depthFirstEnumeration();
//			while(sEnum.hasMoreElements()){
//				sNode = sEnum.nextElement();
//				if(sNode.hasGeneral()){
//					gStr = sNode.getStrValue();
//					if(!gStr.startsWith(ASTExpressionTransformer.ABSTRACT_UNKNOWN))
//						commonCounter++;
////					if(specificToUnified.containsValue(sNode.getStrValue())){
////						commonCounter++;
////					}
//				}else{
//					commonCounter++;
//				}
//				allCounter++;
//			}
//		}
//		return commonCounter * 1.0/allCounter;
//	}
//	
///*	
//	private Sequence computeLCS(Sequence s1, Sequence s2, Sequence result1, Sequence result2){
//		List<Sequence> subseqs1 = getSubsequences(s1);
//		List<Sequence> subseqs2 = getSubsequences(s2);
//		Sequence seq1 = null, seq2 = null, 
//				 tmpHead1 = null, tmpHead2 = null,
//				 tmpTail1 = null, tmpTail2 = null,
//				 tmpHT1 = null, tmpHT2 = null;
//		int indHead1 = -1, indHead2 = -1,
//			indTail1 = -1, indTail2 = -1,
//			indHT1 = -1, indHT2 = -1;
//		int m = subseqs1.size();
//		int n = subseqs2.size();
//		int[][] c = new int[m+1][n+1];
//		int[] headArr1 = new int[m+1];
//		int[] tailArr1 = new int[m+1];
//		int[] htArr1 = new int[m+1];
//		for(int i= 0; i <= m; i++){
//			headArr1[i] = -1;
//			tailArr1[i] = -1;
//			htArr1[i] = -1;
//		}
//		int[] headArr2 = new int[n+1];
//		int[] tailArr2 = new int[n+1];
//		int[] htArr2 = new int[n+1];
//		for(int i = 0; i <= n; i++){
//			headArr2[i] = -1;
//			tailArr2[i] = -1;
//			htArr2[i] = -1;
//		}
//		int max = -1;
//		
//		for(int i = 0; i <=m; i++){
//			c[i][0] = 0;
//		}
//		for(int i = 0; i <=n; i++){
//			c[0][i] = 0;
//		}
//		
//		int len = -1;
//		int lcs[] = new int[3];
//		for(int i = 1; i <=m; i++){
//			seq1 = subseqs1.get(i -1);
//			
//			tmpHead1 = seq1.head();
//			indHead1 = subseqs1.indexOf(tmpHead1);//if empty, ind1 = -1
//			
//			tmpTail1 = seq1.tail();
//			indTail1 = subseqs1.indexOf(tmpTail1);
//			
//			tmpHT1 = tmpHead1.concate(tmpTail1);
//			indHT1 = subseqs1.indexOf(tmpHT1);
//			
//			headArr1[i] = indHead1;
//			tailArr1[i] = indTail1;
//			htArr1[i] = indHT1;
//			for(int j = 1; j <= n; j++){
//				for(int k = 0; k < 3; k++){
//					lcs[k] = 0;
//				}
//				seq2 = subseqs2.get(j - 1);
//				indHead2 = headArr2[j];
//				indTail2 = tailArr2[j];
//				indHT2 = htArr2[j];
//				if(matchMatrix[seq1.get(0) - 1][seq2.get(0) -1] == 1){
//					if((matchSet.containsKey(seq1.get(0)) && matchSet.get(seq1.get(0)) == seq2.get(0))
//							//if the two are matched as required
//							|| !matchSet.containsKey(seq1.get(0)) && !matchSet.containsValue(seq2.get(0))){ 
//						     // or none of them are matched as required		
//						if(indHead2 == -1){
//							tmpHead2 = seq2.head();
//							indHead2 = subseqs2.indexOf(tmpHead2);
//							tmpTail2 = seq2.tail();
//							indTail2 = subseqs2.indexOf(tmpTail2);
//							headArr2[j] = indHead2;
//							tailArr2[j] = indTail2;
//						}
//						
//						len = c[indHead1 + 1][indHead2 + 1];
//						len++;
//						
//						len += c[indTail1 + 1][indTail2 + 1];
//						lcs[0] = len;
//					}
//				}
//				// s1.head().concate(s1.tail()), s2
//				lcs[1] = c[indHT1][j];
//				// s1, s2.head().concate(s2.tail())
//				if(indHT2 == -1){
//					tmpHT2 = seq2.head().concate(seq2.tail());
//					indHT2 = subseqs2.indexOf(tmpHT2);
//					htArr2[j] = indHT2;
//				}
//				lcs[2] = c[i][indHT2];
//				
//				max = lcs[0];
//				for(int k = 1; k < 3; k++){
//					if(lcs[k] > max){
//						max = lcs[k];
//						//maxIndex = i;
//					}
//				}
//				c[i][j] = max;
//			}
//		}
//		return extractLCS(c, subseqs1, subseqs2, s1, s2, 
//				headArr1, headArr2, tailArr1, tailArr2, htArr1, htArr2);
//	}
//	
//	private Sequence extractLCS(int[][] c, List<Sequence> subseqs1, List<Sequence> subseqs2,
//			Sequence s1, Sequence s2, int[] headArr1, int[] headArr2, int[] tailArr1, int[] tailArr2,
//			int[] htArr1, int[]htArr2){
//		Pair<Sequence> result = null;
//		Sequence tmpS1 = s1, tmpS2 = s2;
//		Stack<Pair<Integer>> joints = new Stack<Pair<Integer>>();
//		Stack<Integer> nextDecisions = new Stack<Integer>();
//		int len = -1;
//		int lenU = -1, lenL = -1, lenD = -1;
//		int size2 = subseqs2.size();
//		int indHead1 = -1, indHead2 = headArr2[size2 - 1], 
//			indTail1 = -1, indTail2 = tailArr2[size2 - 1],
//			indHT1 = -1, indHT2 = htArr2[size2 - 1];
//		int[] lcs = new int[3];
//		int ind1 = -1, ind2 = -1;
//		Sequence seq1 = null, seq2 = null;
//		for(int i = subseqs1.size() - 1; i >=0; i--){
//			ind1 = i;
//			ind2 = size2;
//			len = c[ind1 + 1][ind2 + 1];
//			seq1 = subseqs1.get(i);
//			seq2 = subseqs2.get(size2 - 1);
//			result = new Pair<Sequence>(new Sequence(new ArrayList<Integer>()), 
//										new Sequence(new ArrayList<Integer>()));
//			while(len > 0){
//				indHead1 = headArr1[ind1];
//				indTail1 = tailArr1[ind1];
//				indHT1 = htArr1[ind1];
//				indHead2 = headArr2[ind2];
//				indTail2 = tailArr2[ind2];
//				indHT2 = htArr2[ind2];
//				lcs[0] = c[indHead1 + 1][indHead2 + 1] + c[indTail1 + 1][indTail2 + 1];
//				lcs[1] = c[indHT1 + 1][size2];
//				lcs[2] = c[i + 1][indHT2];
//				if(matchMatrix[seq1.get(0) - 1][seq2.get(0) -1] == 1 &&
//						((matchSet.containsKey(seq1.get(0)) && matchSet.get(seq1.get(0)) == seq2.get(0))
//						//if the two are matched as required
//						|| !matchSet.containsKey(seq1.get(0)) && !matchSet.containsValue(seq2.get(0)))){
//					lcs[0]++;
//				}
//				int index = 0;
//				for(index = 0; index < 3; index++){
//					if(lcs[index] == len)
//						break;
//				}
//				boolean hasNext = false;
//				int index2 = 0;
//				for(index2 = index + 1; index2 < 3; index2++){
//					if(lcs[index2] == len){
//						hasNext = true;
//						break;
//					}
//				}
//				if(hasNext){
//					joints.push(new Pair<Integer>(ind1, ind2));
//					nextDecisions.add(index2);
//				}
//				if(index == 0){
//					computeLCS(subseqs1.get(indHead1), subseqs2.get(indHead2), result1, result2);
//					result1.	
//				}
//			}
//		}
//		return result;
//	}
//	
//	private List<Sequence> getSubsequences(Sequence s){
//		List<Sequence> result = new ArrayList<Sequence>();
//		result.add(s);
//		Queue<Sequence> queue = new LinkedList<Sequence>();
//		Set<Sequence> processed = new HashSet<Sequence>();
//		queue.add(s);
//		Sequence tmp = null, subseq = null;
//		Sequence head = null, tail = null, joint = null;
//		boolean isEmpty = true;
//		while(!queue.isEmpty()){
//			isEmpty = true;
//			tmp = queue.remove();
//			head = tmp.head();
//			tail = tmp.tail();
//			joint = tmp.joint();
//			if(!head.isEmpty() && !processed.contains(head)){
//				queue.add(head);
//				result.add(head);
//				isEmpty = false;
//			}
//			if(!tail.isEmpty() && !processed.contains(tail)){
//				queue.add(tail);
//				result.add(tail);
//				isEmpty = false;
//			}
//			if(!isEmpty){
//				subseq = head.concate(tail);
//				if(!processed.contains(subseq)){
//					result.add(subseq);
//					queue.add(subseq);	
//				}
//			}
//			result.add(joint);
//		}
//		Collections.sort(result, new SequenceComparator());
//		return result;
//	}
//	*/
//	
//	
//	private int computeLCS(Sequence s1, Sequence s2, Sequence part1, Sequence part2){
//		int max = 0;
//		String digest = s1.toString() + "--" + s2.toString();
//		Pair<Sequence> pair = null;
//
//		if(dictionary.containsKey(digest)){
//			pair = dictionary.get(digest);
//			part1.add(pair.getLeft());
//			part2.add(pair.getRight());
//			max = pair.getLeft().size();
//		}else if(s1.isEmpty() || s2.isEmpty()){
//			//do nothing
//		}else{
//			if(s1.size() == 1 && s2.size() == 1 && matchMatrix[s1.get(0) - 1][s2.get(0) - 1] == 1){
//				max = 1; 
//				part1.add(s1);
//				part2.add(s2);
//				pair = new Pair<Sequence>(s1, s2);	
//			}else{
//				List<Pair<Sequence>> pairResults = new ArrayList<Pair<Sequence>>();
//				Sequence result1, result2;
//				for(int i = 0; i < 3; i++){
//					result1 = new Sequence(new ArrayList<Integer>());
//					result2 = new Sequence(new ArrayList<Integer>());
//					pairResults.add(new Pair<Sequence>(result1, result2));
//				}
//				int[] lcs = new int[3];
//				for(int i = 0; i < 3; i++)
//					lcs[i] = 0;
//				if(matchMatrix[s1.get(0) - 1][s2.get(0) -1] == 1){
//					if((matchSet.containsKey(s1.get(0)) && matchSet.get(s1.get(0)) == s2.get(0))//if the two are matched as required
//							|| !matchSet.containsValue(s2.get(0))){ // or none of them are matched as required
//						result1 = pairResults.get(0).getLeft();
//						result2 = pairResults.get(0).getRight();
//						lcs[0] = computeLCS(s1.head(), s2.head(), result1, result2);
//						result1.append(s1.get(0));
//						result2.append(s2.get(0));
//						lcs[0]++;
//						lcs[0] += computeLCS(s1.tail(), s2.tail(), result1, result2);
//					}
//				}
//				lcs[1] = computeLCS(s1.head().concate(s1.tail()), s2, pairResults.get(1).getLeft(), pairResults.get(1).getRight());
//				lcs[2] = computeLCS(s1, s2.head().concate(s2.tail()), pairResults.get(2).getLeft(), pairResults.get(2).getRight());
//				max = lcs[0];
//				int maxIndex = 0;
//				for(int i = 1; i < 3; i++){
//					if(lcs[i] > max){
//						max = lcs[i];
//						maxIndex = i;
//					}
//				}
//				pair = pairResults.get(maxIndex);
//				part1.add(pair.getLeft());
//				part2.add(pair.getRight());
//			}
//			dictionary.put(digest, pair);
//		}
//		return max;
//	}
//	
//	
//	private void initMatchMatrix(Sequence s1, Sequence s2,
//			List<List<SimpleASTNode>> simpleASTNodesList1,
//			List<List<SimpleASTNode>> simpleASTNodesList2,
//			Map<String, String> specificToUnified,
//			Map<String, String> unifiedToSpecific, List<SimpleTreeNode> forest1,
//			List<SimpleTreeNode> forest2){
//		matchSet = new HashMap<Integer, Integer>();
//		Enumeration<SimpleTreeNode> sEnum1 = null, sEnum2 = null;
//		SimpleTreeNode sNode1 = null, sNode2 = null;
//		SimpleTreeNode tree1 = null, tree2 = null;
//		for(int i = 0; i < forest1.size(); i++){
//			tree1 = forest1.get(i);
//			tree2 = forest2.get(i);
//			sEnum1 = tree1.depthFirstEnumeration();
//			sEnum2 = tree2.depthFirstEnumeration();
//			while(sEnum1.hasMoreElements()){
//				sNode1 = sEnum1.nextElement();
//				sNode2 = sEnum2.nextElement();
//				if(!sNode1.getTypes().get(SimpleTreeNode.INSERTED_CONTEXTUAL))
//					matchSet.put(sNode1.getNodeIndex(), sNode2.getNodeIndex());
//			}
//		}
//		matchMatrix = new double[simpleASTNodesList1.size()][simpleASTNodesList2.size()];
//		for(int i = 0; i < simpleASTNodesList1.size(); i++){
//			for(int j = 0; j < simpleASTNodesList2.size(); j++){
//				matchMatrix[i][j] = -1;
//			}
//		}
//		int index1 = -1, index2 = -1;
//		int seqInd1 = -1, seqInd2 = -1;
//		int nodeInd1 = -1, nodeInd2 = -1;
//		for(Entry<Integer, Integer> match : matchSet.entrySet()){
//			index1 = match.getKey();
//			index2 = match.getValue();
//			seqInd1 = s1.indexOf(index1);
//			seqInd2 = s2.indexOf(index2);
//			for(int i = seqInd1; i < s1.getNodeIndexes().size(); i++){
//				nodeInd1 = s1.get(i);
//				if(nodeInd1 < 0)
//					continue;
//				for(int j = 0; j <= seqInd2; j++){
//					nodeInd2 = s2.get(j);
//					if(nodeInd2 < 0)
//						continue;
//					matchMatrix[nodeInd1 - 1][nodeInd2 - 1] = 0;
//				}
//			}
//			for(int i = 0; i <= seqInd1; i++){
//				nodeInd1 = s1.get(i);
//				if(nodeInd1 < 0)
//					continue;
//				for(int j = seqInd2; j < s2.getNodeIndexes().size(); j++){
//					nodeInd2 = s2.get(j);
//					if(nodeInd2 < 0)
//						continue;
//					matchMatrix[nodeInd1 - 1][nodeInd2 - 1] = 0;
//				}
//			}
//			matchMatrix[index1 - 1][index2 - 1] = 1;
//		}
//		for(int i = 0; i < simpleASTNodesList1.size(); i++){
//			for(int j = 0; j < simpleASTNodesList2.size(); j++){
//				if(matchMatrix[i][j] == -1){
//					double tmp = calcSimilarity(simpleASTNodesList1.get(i), 
//							simpleASTNodesList2.get(j), specificToUnified, unifiedToSpecific);
//					if(tmp > 0.9)
//						matchMatrix[i][j] = 1;
//					else
//						matchMatrix[i][j] = 0;
//				}
//			}
//		}
//	}
//
//	private Sequence match(Sequence s1, Sequence s2, 
//			List<List<SimpleASTNode>> simpleASTNodesList1,
//			List<List<SimpleASTNode>> simpleASTNodesList2,
//			Map<String, String> specificToUnified1,
//			Map<String, String> specificToUnified2, 
//			Map<String, String> unifiedToSpecific,
//			List<Sequence> sequenceList, List<List<SimpleASTNode>> simpleASTNodesList,
//			List<SimpleTreeNode> forest1,
//			List<SimpleTreeNode> forest2){
//		initMatchMatrix(s1, s2, simpleASTNodesList1, simpleASTNodesList2,
//				specificToUnified1, unifiedToSpecific, forest1, forest2);
//		dictionary = new HashMap<String, Pair<Sequence>>();
//		Sequence result = new Sequence(new ArrayList<Integer>());
//		Sequence result1 = new Sequence(new ArrayList<Integer>());
//		Sequence result2 = new Sequence(new ArrayList<Integer>());
//		computeLCS(s1, s2, result1, result2);
//		List<Integer> indexes1 = new ArrayList<Integer>();
//		List<Integer> indexes2 = new ArrayList<Integer>();
//		List<List<SimpleASTNode>> tmpNodesList1 = new ArrayList<List<SimpleASTNode>>();
//		List<List<SimpleASTNode>> tmpNodesList2 = new ArrayList<List<SimpleASTNode>>();
//		boolean newMatchAdded = false;;
//		int tmpIndex = -1;
//		String key = null, value = null;
//		CommonParser parser = new CommonParser();
//		List<List<SimpleASTNode>> commonNodesList = null;
//		Map<String, String> localsTou1 = new HashMap<String, String>(specificToUnified1),
//							localsTou2 = new HashMap<String, String>(specificToUnified2);
//		parser.setMap(localsTou1, localsTou2);
//		
//		for(int j = 0; j < result1.getNodeIndexes().size(); j++){
//			if(result1.get(j) > 0 && !matchSet.containsKey(result1.get(j))){
//				tmpIndex = result1.get(j);
//				tmpNodesList1.add(simpleASTNodesList1.get(tmpIndex - 1));
//				tmpIndex = result2.get(j);
//				tmpNodesList2.add(simpleASTNodesList2.get(tmpIndex - 1));
//			}
//		}
//		commonNodesList = parser.getCommon(tmpNodesList1, tmpNodesList2);
//		newMatchAdded = false;
//		if(commonNodesList != null){
//			for(Entry<String, String> entry : localsTou1.entrySet()){
//				key = entry.getKey();
//				if(!specificToUnified1.containsKey(key)){
//					specificToUnified1.put(key, entry.getValue());
//					newMatchAdded = true;
//				}
//			}
//			for(Entry<String, String> entry : localsTou2.entrySet()){
//				key = entry.getKey();
//				if(!specificToUnified2.containsKey(key)){
//					specificToUnified2.put(key, entry.getValue());
//				}
//			}
//		}
//		
//		for(int j = 0; j < result1.getNodeIndexes().size(); j++){
//			if(result1.get(j) > 0){
//				tmpIndex = result1.get(j);
//				indexes1.add(tmpIndex);
//				tmpNodesList1.add(simpleASTNodesList1.get(tmpIndex - 1));
//				/*if(isEmpt && !simpleASTNodesList1.get(result1.get(j) - 1).isEmpty()){
//					isEmpty = false;
//				}*/
//			}
//		}
//		
//		for(int j = 0; j < result2.getNodeIndexes().size(); j++){
//			if(result2.get(j) > 0){
//				tmpIndex = result2.get(j);
//				indexes2.add(tmpIndex);
//				tmpNodesList2.add(simpleASTNodesList2.get(tmpIndex - 1));
//			}
//		}
//		
//		sequenceList.add(result1);
//		sequenceList.add(result2);
//		
//		List<List<SimpleASTNode>> tmpNodesList = parser.getCommon(tmpNodesList1, tmpNodesList2);
//		if(tmpNodesList == null || tmpNodesList.size() == 0 || 
//				(tmpNodesList.size() == 1 && tmpNodesList.get(0).size() == 0)){
//			return null;
//		}
//
//		//check inclusion of all forest matching pairs
//		boolean includeAll = true;
//		int i1 = -1, i2 = -1;
//		for(Entry<Integer, Integer> match : matchSet.entrySet()){
//			i1 = indexes1.indexOf(match.getKey());
//			i2 = indexes2.indexOf(match.getValue());
//			if(i1 != -1 && i1 == i2){
//				continue;
//			}else{
//				includeAll = false;
//				break;
//			}
//		}
//		
//		if(!includeAll){
//			return null;
//		}
//		
//		for(int i = 0; i < simpleASTNodesList.size(); i++){
//			simpleASTNodesList.get(i).clear();
//		}
//		
//		int counter = 0;
//		for(int j = 0; j < result1.getNodeIndexes().size(); j++){
//			if(result1.get(j) > 0){
//				simpleASTNodesList.set(result1.get(j) - 1, tmpNodesList.get(counter++));
//			}
//		}
//		result.add(result1);
//		return result;
//	}
//	
//	private void unify(EditInCommonCluster cluster, int index1, int index2, 
//			Sequence result1, Sequence result2, Sequence result, 
//			List<List<SimpleASTNode>> simpleASTNodesList1, 
//			List<List<SimpleASTNode>> simpleASTNodesList2,
//			List<List<SimpleASTNode>> simpleASTNodesList){
//		List<List<SimpleASTNode>> newSimpleASTNodesList1 = null;
//		List<List<SimpleASTNode>> newSimpleASTNodesList2 = null;
//		if(simpleASTNodesList1.size() == result1.size())
//			newSimpleASTNodesList1 = simpleASTNodesList1;
//		else{
//			newSimpleASTNodesList1 = new ArrayList<List<SimpleASTNode>>();
//			List<Integer> nodeIndexes = result1.getNodeIndexes();
//			Integer nodeIndex = null;
//			for(int i = 0; i < nodeIndexes.size(); i++){
//				nodeIndex = nodeIndexes.get(i);
//				if(nodeIndex > 0)
//					newSimpleASTNodesList1.add(simpleASTNodesList1.get(nodeIndex - 1));
//			}
//		}
//		
//		if(simpleASTNodesList2.size() == result2.size()){
//			newSimpleASTNodesList2 = simpleASTNodesList2;
//		}else{
//			newSimpleASTNodesList2 = new ArrayList<List<SimpleASTNode>>();
//			List<Integer> nodeIndexes = result2.getNodeIndexes();
//			Integer nodeIndex = null;
//			for(int i = 0 ; i < nodeIndexes.size(); i++){
//				nodeIndex = nodeIndexes.get(i);
//				if(nodeIndex > 0)
//					newSimpleASTNodesList2.add(simpleASTNodesList2.get(nodeIndex - 1));
//			}
//		}
//		
//		CommonParser parser = new CommonParser();
//		simpleASTNodesList.addAll(parser.getCommon(newSimpleASTNodesList1, newSimpleASTNodesList2));
//	}
//}
