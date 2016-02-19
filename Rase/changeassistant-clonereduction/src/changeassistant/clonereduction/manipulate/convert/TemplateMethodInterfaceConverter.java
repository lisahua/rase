package changeassistant.clonereduction.manipulate.convert;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import changeassistant.changesuggestion.expression.representation.ASTExpressionTransformer;
import changeassistant.changesuggestion.expression.representation.MethodNameTerm;
import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.TypeNameTerm;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;
import changeassistant.classhierarchy.ProjectResource;
import changeassistant.clonereduction.main.CloneReductionException;
import changeassistant.clonereduction.main.CloneReductionMain;
import changeassistant.clonereduction.manipulate.data.ExpressionInfo;
import changeassistant.clonereduction.manipulate.data.ExpressionInfoCreator;
import changeassistant.clonereduction.manipulate.data.MethodInfo;
import changeassistant.clonereduction.manipulate.data.MethodInfoCreator;
import changeassistant.clonereduction.manipulate.data.TypeInfo;
import changeassistant.clonereduction.manipulate.data.TypeInfoCreator;
import changeassistant.clonereduction.manipulate.data.VariableInfo;
import changeassistant.clonereduction.manipulate.data.VariableInfoCreator;
import changeassistant.clonereduction.manipulate.refactoring.RefactoringMetaData;
import changeassistant.clonereduction.search.CloneReductionMatchResult;
import changeassistant.internal.ASTNodeFinder;
import changeassistant.multipleexample.datastructure.GeneralizedStmtIndexMap;
import changeassistant.multipleexample.main.Constants;
import changeassistant.multipleexample.match.PatternUtil;
import changeassistant.multipleexample.partition.SimpleASTCreator;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.peers.SourceCodeRange;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.treematching.edits.ITreeEditOperation.EDIT;
public class TemplateMethodInterfaceConverter {

	static MethodDeclaration md;
	
	public static void prepareToConvert(List<SimpleTreeNode> pNodes, 
			List<List<SimpleASTNode>> nodesList, Map<String, String> sTou, 
			Map<String, MethodInfo> uTom, Map<String, ExpressionInfo> uToe, 
			Map<String, TypeInfo> uTot, Map<String, VariableInfo> uTov,
			MethodDeclaration m) throws CloneReductionException{
		md = m;
		List<SimpleASTNode> nodes = null;
		Enumeration<SimpleTreeNode> sEnum = null;
		SimpleTreeNode sTmp = null;
		for(SimpleTreeNode pNode : pNodes){
			sEnum = pNode.breadthFirstEnumeration();
			while(sEnum.hasMoreElements()){
				sTmp = sEnum.nextElement();
				nodes = nodesList.get(sTmp.getNodeIndex() - 1);
				prepareToConvert(sTou, uTom, uToe, uTot, uTov, nodes);
			}
		}
	}
	
	/**
	 * The method invoked by option hasMultipleExamples
	 * @param generalizedStmts
	 * @param sTou
	 * @param uTom
	 * @param uToe
	 * @param uTot
	 * @param uTov
	 * @param m
	 * @throws CloneReductionException
	 */
	public static void prepareToConvert(List<List<SimpleASTNode>> generalizedStmts, Map<String, String> sTou,
			Map<String, MethodInfo> uTom, Map<String, ExpressionInfo> uToe, Map<String, TypeInfo> uTot,
			Map<String, VariableInfo> uTov, MethodDeclaration m) throws CloneReductionException{
		md = m;
		for(List<SimpleASTNode> stmt : generalizedStmts){			
			prepareToConvert(sTou, uTom, uToe, uTot, uTov, stmt);
		}
		if(RefactoringMetaData.isNeedRetObj()){
			List<String> types = RefactoringMetaData.getReturnTypes();
			for(String t : types){
				if(sTou.containsKey(t) && Term.T_Pattern.matcher(sTou.get(t)).matches()){
					throw new CloneReductionException("The return value encapsulated in ReturnObject has different types in different methods");
				}
			}
//			for(Entry<String, String> entry : sTou.entrySet()){
//				key = entry.getKey();
//				value = entry.getValue();
//				if(Term.T_Pattern.matcher(value).matches() && !uTot.containsKey(value)){
//					throw new CloneReductionException("The return value encapsulated in ReturnObject has different types in different methods");
//				}
//			}
		}				
	}
	
	private static void prepareToConvert(Map<String, String> sTou, Map<String, MethodInfo> uTom, 
			Map<String, ExpressionInfo> uToe, Map<String, TypeInfo> uTot,
			Map<String, VariableInfo> uTov, List<SimpleASTNode> nodes) throws CloneReductionException{
		System.out.print("");
		SimpleASTNode node = null;
		Queue<SimpleASTNode> sQueue1 = new LinkedList<SimpleASTNode>();
		SimpleASTNode sTmp = null, sTmp2 = null;
		String tmpStr = null;
		SourceCodeRange scr = null;
		ASTNode astNode = null;
		ASTNodeFinder finder = new ASTNodeFinder();
		MethodInfo mInfo = null;
		ExpressionInfo eInfo = null;
		TypeInfo tInfo = null;
		VariableInfo vInfo = null;
		int tmpNodeType = -1;
		MethodInfoCreator mCreator = new MethodInfoCreator();
		ExpressionInfoCreator eCreator = new ExpressionInfoCreator();
		TypeInfoCreator tCreator = new TypeInfoCreator();
		VariableInfoCreator vCreator = new VariableInfoCreator();
		for(int j = 0; j < nodes.size(); j++){
			node = nodes.get(j);
			sQueue1.add(node);
			while(!sQueue1.isEmpty()){
				sTmp = sQueue1.remove();
				tmpStr = sTmp.getStrValue();
				if(Term.U_Pattern.matcher(tmpStr).matches()){
					if(!uToe.containsKey(tmpStr)){
						if(Term.U_List_Literal_Pattern.matcher(tmpStr).matches()){
							throw new CloneReductionException("The parameters of different method calls do not match");
						}else{
							scr = sTmp.getScr();
							astNode = finder.lookforASTNode(md, scr);
							eInfo = eCreator.createForU(astNode, tmpStr);
							uToe.put(tmpStr, eInfo);
						}						
					}
				}else if(Term.M_Pattern.matcher(tmpStr).matches()){					
					if(!uTom.containsKey(tmpStr)){					
						sTmp = (SimpleASTNode)sTmp.getParent();
						scr = sTmp.getScr();
						astNode = finder.lookforASTNode(md, scr);
						mInfo = mCreator.createForM(astNode, tmpStr);
						uTom.put(tmpStr, mInfo);
					}else{
						sTmp2 = (SimpleASTNode)sTmp.getParent();
						mInfo = uTom.get(tmpStr);
						if(!MethodInfo.findItem(mInfo, sTmp2.getStrValue(), uTom)){
							scr = sTmp2.getScr();
							astNode = finder.lookforASTNode(md, scr);
							tmpStr = MethodInfo.createNewMTerm(sTou.values(), uTom.keySet());
							MethodInfo newMethodInfo = mCreator.createForM(astNode, tmpStr);
							mInfo.addForward(tmpStr);
							uTom.put(tmpStr, newMethodInfo);
						}
					}	
					System.out.print("");
					String invokerType = sTou.get(mInfo.instTypeName);
					if(invokerType != null && Term.T_Pattern.matcher(invokerType).matches()){
						if(!uTot.containsKey(invokerType)){
							tInfo = tCreator.createForT(invokerType, sTou);
							uTot.put(invokerType, tInfo);
						}else{
							tInfo = uTot.get(invokerType);
						}
					}
					continue;
				}else if(Term.T_Pattern.matcher(tmpStr).matches()){
					sTmp2 = (SimpleASTNode)sTmp.getParent();
					tmpNodeType = sTmp2.getNodeType();
					if(tmpNodeType == ASTNode.CLASS_INSTANCE_CREATION || tmpNodeType == ASTNode.INSTANCEOF_EXPRESSION
							|| tmpNodeType == ASTNode.TYPE_LITERAL
							|| tmpNodeType == ASTNode.QUALIFIED_NAME){
						throw new CloneReductionException("The parameterized type cannot be used to do instantiation " +
								", type check, type literal access or qualify other variables (T.v)");
					}
					if(!uTot.containsKey(tmpStr)){
						scr = sTmp.getScr();
						if(scr.equals(SourceCodeRange.DefaultRange)){
							tInfo = tCreator.createForT(tmpStr, sTou);
						}else{
							astNode = finder.lookforASTNode(md, scr);
							tInfo = tCreator.createForT(astNode, tmpStr);
						}						
						uTot.put(tmpStr, tInfo);
					}
				}else if(Term.V_Pattern.matcher(tmpStr).matches()){
					sTmp2 = (SimpleASTNode)sTmp.getParent();
					if(sTmp2 != null && sTmp2.getNodeType() == ASTNode.QUALIFIED_NAME){
						sTmp = sTmp2;
					}
					if(!uTov.containsKey(tmpStr)){
						scr = sTmp.getScr();
						if(!scr.equals(SourceCodeRange.DefaultRange)){
							astNode = finder.lookforASTNode(md, scr);
							vInfo = vCreator.createForV(astNode, tmpStr);
							String type = vInfo.type;
							if(sTou.containsKey(type))
								type = sTou.get(type);
							if(Term.T_Pattern.matcher(type).matches()){
								if(!uTot.containsKey(type)){
									tInfo = tCreator.createForT(vInfo.type, type);
									uTot.put(type, tInfo);
								}
								vInfo.type = uTot.get(type).name;
							}
							uTov.put(tmpStr, vInfo);
						}													
					}
					if(sTmp.equals(sTmp2)){
						continue;
					}
				}else{
					System.out.print("");
					Term term = sTmp.getTerm();
					if(term != null){
						if(term instanceof VariableTypeBindingTerm){
							TypeNameTerm tTerm =((VariableTypeBindingTerm)term).getTypeNameTerm(); 
							String type = tTerm.getName();
							if(Term.T_Pattern.matcher(type).matches() && !uTot.containsKey(type)){
								scr = sTmp.getScr();
								astNode = finder.lookforASTNode(md, scr);
								vInfo = vCreator.createForV(astNode, term.getName());
								tInfo = tCreator.createForT(vInfo.type, type);
								uTot.put(type, tInfo);
							}
						}else if(term instanceof MethodNameTerm){
							MethodNameTerm mTerm = (MethodNameTerm)term;
							TypeNameTerm tTerm = mTerm.getTypeNameTerm();
							String tTName = null;
							if(tTerm != null){
								tTName = tTerm.getName();
								tTName = sTou.get(tTName);
								if(Term.T_Pattern.matcher(tTName).matches() && sTou.containsValue(tTName) &&
								!uTot.containsKey(tTName)){									
									tInfo = tCreator.createForT(tTName, sTou);
									uTot.put(tTName, tInfo);
								}
							}
						}
					}					
				}
				if(sTmp.getChildCount() > 0){
					sQueue1.addAll(sTmp.getChildren());
				}
			}
		}
	}
	
	public static List<SimpleTreeNode> convert(List<SimpleTreeNode> pSNodes, List<List<SimpleASTNode>> nodesList,
			MethodDeclaration m){
		md = m;
		List<Map<String, MethodInfo>> unifiedToMethodList = RefactoringMetaData.getUnifiedToMethodList();
		List<Map<String, ExpressionInfo>> unifiedToExprList = RefactoringMetaData.getUnifiedToExprList();
		List<Map<String, TypeInfo>> unifiedToTypeList = RefactoringMetaData.getUnifiedToTypeList();
		List<Map<String, VariableInfo>> unifiedToVariableList = RefactoringMetaData.getUnifiedToVariableList();
		Map<String, MethodInfo> uTom = unifiedToMethodList.get(0);
		Map<String, ExpressionInfo> uToe = unifiedToExprList.get(0);
		Map<String, TypeInfo> uTot = unifiedToTypeList.get(0);
		Map<String, VariableInfo> uTov = unifiedToVariableList.get(0);
		List<SimpleTreeNode> result = new ArrayList<SimpleTreeNode>();
		Enumeration<SimpleTreeNode> sEnum = null;
		SimpleTreeNode sTmp = null;
		for(SimpleTreeNode sNode : pSNodes){
			sNode = (SimpleTreeNode)sNode.deepClone();
			result.add(sNode);
			sEnum = sNode.breadthFirstEnumeration();
			while(sEnum.hasMoreElements()){
				sTmp = sEnum.nextElement();
				convert(uTom, uToe, uTot, uTov, sTmp, nodesList.get(sTmp.getNodeIndex() - 1), null);
			}			
		}
		return result;
	}
	
	public static List<SimpleTreeNode> convert(List<Node> markedNodes, Map<Integer, List<SimpleASTNode>> indexStmtMap, Node mNode, 
			Map<String, String> sTou){
		md = mNode.getMethodDeclaration();
		SimpleTreeNode sNode = new SimpleTreeNode(mNode, true, 1);
		List<Map<String, MethodInfo>> unifiedToMethodList = RefactoringMetaData.getUnifiedToMethodList();
		List<Map<String, ExpressionInfo>> unifiedToExprList = RefactoringMetaData.getUnifiedToExprList();
		List<Map<String, TypeInfo>> unifiedToTypeList = RefactoringMetaData.getUnifiedToTypeList();
		List<Map<String, VariableInfo>> unifiedToVariableList = RefactoringMetaData.getUnifiedToVariableList();
		Map<String, MethodInfo> uTom = unifiedToMethodList.get(0);
		Map<String, ExpressionInfo> uToe = unifiedToExprList.get(0);
		Map<String, TypeInfo> uTot = unifiedToTypeList.get(0);
		Map<String, VariableInfo> uTov = unifiedToVariableList.get(0);
		List<SimpleTreeNode> result = new ArrayList<SimpleTreeNode>();
		SimpleTreeNode sTmp = null;
		SimpleTreeNode tmpEnumNode = null;
		for(Node tmpNode : markedNodes){
			sTmp = sNode.lookforNodeBasedOnRange(tmpNode);
			result.add(sTmp);
			Enumeration<SimpleTreeNode> sBEnum = sTmp.breadthFirstEnumeration();
			while(sBEnum.hasMoreElements()){
				tmpEnumNode = sBEnum.nextElement();
				convert(uTom, uToe, uTot, uTov, tmpEnumNode, indexStmtMap.get(tmpEnumNode.getNodeIndex()), sTou);
			}			
		}
		
		//convert interface of all invoked methods by extractedMethod()
		MethodInfo mInfo = null;
		List<Map<Integer, String>> indexToGeneralNameList = new ArrayList<Map<Integer, String>>();
		Map<Integer, String> indexToGeneralName = null;
		String abstractName = null;
		String name = null, generalName = null;
		List<String> names = null;
		for(Entry<String, MethodInfo> entry : uTom.entrySet()){
			indexToGeneralName = new HashMap<Integer, String>();
			mInfo = entry.getValue();
			names = mInfo.names;
			for(int i = 0; i < names.size(); i++){
				name = names.get(i);
				if(sTou.containsKey(name)){
					abstractName = sTou.get(name);
					if(uTov.containsKey(abstractName)){
						indexToGeneralName.put(i, uTov.get(abstractName).name);
					}
				}
			}
			indexToGeneralNameList.add(indexToGeneralName);
		}
		int listId = -1;
		int index = -1;
		Map<String, String> pTon = null;
		Map<String, String> convertMap = null;
		for(Map<String, MethodInfo> tmpUToM : unifiedToMethodList){
			listId = -1;
			for(Entry<String, MethodInfo> entry : tmpUToM.entrySet()){
				listId++;
				indexToGeneralName = indexToGeneralNameList.get(listId);
				if(!indexToGeneralName.isEmpty()){
					mInfo = entry.getValue();
					pTon = mInfo.paramToName;
					convertMap = new HashMap<String, String>();
					for(Entry<Integer, String> iTog : indexToGeneralName.entrySet()){
						index = iTog.getKey();
						generalName = iTog.getValue();
						name = mInfo.names.get(index);
						if(pTon.containsValue(name)){
							pTon.put(mInfo.paramASTs.get(index).toString(), generalName);
							convertMap.put(name, generalName);
						}
						mInfo.names.set(index, generalName);
					}
					mInfo.updateParamAndContent(convertMap);					
				}	
			}					
		}
		return result;
	}
	/**
	 * convert representation
	 * @param uTom
	 * @param sNode
	 * @param nodes
	 * @throws CloneReductionException 
	 */
	private static void convert(Map<String, MethodInfo> uTom, Map<String, ExpressionInfo> uToe, 
			Map<String, TypeInfo> uTot, Map<String, VariableInfo> uTov, SimpleTreeNode sNode, 
			List<SimpleASTNode> nodes, Map<String, String> sTou) {
		SimpleASTNode node = null;
		Queue<SimpleASTNode> sQueue = new LinkedList<SimpleASTNode>();
		SimpleASTNode sTmp = null, sTmp2 = null, sTmp3 = null;
		String tmpStr = null;
		MethodInfo mInfo = null;
		VariableInfo vInfo = null;
		TypeInfo tInfo = null;
		ExpressionInfo eInfo = null;
		SimpleASTCreator creator = new SimpleASTCreator();
//		System.out.print("");
		for (int j = 0; j < nodes.size(); j++) {
			node = nodes.get(j);
			sQueue.add(node);
			while (!sQueue.isEmpty()) {
				sTmp = sQueue.remove();
				tmpStr = sTmp.getStrValue();
				if(sTmp.getNodeType() == ASTNode.FIELD_ACCESS && tmpStr.startsWith("this.")){
					sTmp.remove(0);//remove "this"
					sTmp.remove(0);//remove "."
					sTmp.setRecalcToRoot();
				}else if(sTmp.getNodeType() == ASTNode.METHOD_INVOCATION && uTov.containsKey(Constants.INSTANCE)
						&& !((SimpleASTNode)sTmp.getChildAt(1)).getStrValue().equals(".")
						&& !uTom.containsKey(((SimpleASTNode)sTmp.getChildAt(0)).getStrValue())){//no invoker
					sTmp.insert(new SimpleASTNode(ASTNode.SIMPLE_NAME, Constants.INSTANCE, 0, 0), 0);
					sTmp.insert(new SimpleASTNode(ASTExpressionTransformer.DOT, ".", 0, 0), 1);
					sTmp.setRecalcToRoot();
				}else if(tmpStr.equals("this") && uTov.containsKey(Constants.INSTANCE)){
					sTmp.setStrValue(Constants.INSTANCE);
					sTmp2 = (SimpleASTNode)sTmp.getParent();
					if(sTmp2 != null)
						sTmp2.setRecalcToRoot();
				}else if(uTom.containsKey(tmpStr) || uToe.containsKey(tmpStr) || uTot.containsKey(tmpStr) 
						|| uTov.containsKey(tmpStr)){// M_Pattern, U_Pattern, T_Pattern
					if(uTom.containsKey(tmpStr)){
						sTmp2 = (SimpleASTNode)sTmp.getParent();	
						mInfo = uTom.get(tmpStr);
						mInfo = MethodInfo.getMethodInfo(mInfo, sTmp2.getStrValue(), uTom);
						tmpStr = mInfo.name;								
						int childIndex = sTmp2.getIndex(sTmp);
						if(childIndex != 0){
							for(int i = 0; i < childIndex;i++){
								sTmp2.remove(0);
							}														
						}
						System.out.print("");
						SimpleASTNode sTmpParamList = (SimpleASTNode) sTmp2.getChildAt(2);
						if(sTmpParamList.getChildCount() != mInfo.names.size() * 2 + 1){	
							sTmpParamList.removeAllChildren();
							String[] paramArray = mInfo.paramString.split(",");
							for(String paramElem : paramArray){
								if(paramElem.equals(""))
									continue;
								paramElem = sTou.get(paramElem);
								sTmpParamList.add(new SimpleASTNode(ASTNode.SIMPLE_NAME, paramElem, 0, 0));
								sTmpParamList.add(new SimpleASTNode(ASTExpressionTransformer.COMMA, ",", 0, 0));
							}
							if(mInfo.needInst){
								sTmpParamList.add(new SimpleASTNode(ASTNode.SIMPLE_NAME, Constants.INSTANCE, 0, 0));
								sTmpParamList.add(new SimpleASTNode(ASTExpressionTransformer.COMMA, ",", 0, 0));
							}
							/*
							for(ASTNode paramAST : mInfo.paramASTs){
								sTmp3 = creator.createSimpleASTNode(paramAST);
								SimpleASTNode.convert(sTmp3, sTou);
								sTmpParamList.add(sTmp3);
								sTmpParamList.add(new SimpleASTNode(ASTExpressionTransformer.COMMA, ",", 0, 0));
							}*/
							if(sTmpParamList.getChildCount() > 0){
								sTmpParamList.remove(sTmpParamList.getChildCount() - 1);
							}
							sTmpParamList.setRecalcToRoot();
						}
					}else if(uToe.containsKey(tmpStr)){//uToe.containsKey(tmpStr)
						eInfo = uToe.get(tmpStr);
						if(Term.U_List_Literal_Pattern.matcher(tmpStr).matches()){
							StringBuffer buffer = new StringBuffer();
							for(String otherKey : eInfo.otherKeys){
								buffer.append(uToe.get(otherKey).name).append(",");
							}
							buffer.setLength(buffer.length() - 1);
							tmpStr = buffer.toString();
						}else{
							tmpStr = eInfo.name;
						}			
						sTmp.removeAllChildren();
					}else if(uTov.containsKey(tmpStr)){
						if(tmpStr.equals(Constants.INSTANCE))
							continue;
						vInfo = uTov.get(tmpStr);						
						sTmp2 = (SimpleASTNode)sTmp.getParent();
						if(sTmp2 != null && sTmp2.getChildCount() > 1){
							if (sTmp2.getNodeType() == ASTNode.QUALIFIED_NAME ||
									sTmp2.getNodeType() == ASTNode.FIELD_ACCESS && sTmp2.getStrValue().startsWith("this.")){
								sTmp2.setStrValue(vInfo.name);
								sTmp2 = (SimpleASTNode) sTmp2.getParent();
								if(sTmp2 != null){
									sTmp2.setRecalcToRoot();
								}
								continue;
							}
						}											
						tmpStr = vInfo.name;						
					}else{//uTot.conatins(tmpStr)
						sTmp2 = (SimpleASTNode)sTmp.getParent();
						tInfo = uTot.get(tmpStr);
						tmpStr = tInfo.name;
					}
					sTmp.setStrValue(tmpStr);						
					sTmp2 = (SimpleASTNode)sTmp.getParent();
					if(sTmp2 != null)
						sTmp2.setRecalcToRoot();
				}
				if (sTmp.getChildCount() != 0) {	
					sQueue.addAll(sTmp.getChildren());
				}
			}
			node.constructStrValue();
		}
		String oldString = sNode.getStrValue();
		String newString = PatternUtil.createStrValue(sNode.getNodeType(), oldString, nodes);
		sNode.setStrValue(newString);
	}

    public static void mergeInfo(ProjectResource pr){    
    	String key = null;
    	List<Map<String, TypeInfo>> unifiedToTypeList = RefactoringMetaData.getUnifiedToTypeList();
    	Map<String, TypeInfo> uTot = unifiedToTypeList.get(0);
    	Map<String, TypeInfo> uTot2 = null;
    	TypeInfo tInfo = null;
    	for(Entry<String, TypeInfo> entry : uTot.entrySet()){
    		key = entry.getKey();
    		tInfo = entry.getValue();
    		for(int i = 1 ; i < unifiedToTypeList.size(); i++){
    			uTot2 = unifiedToTypeList.get(i);
    			TypeInfo.merge(tInfo, uTot2.get(key), pr);
    		}
    	}
    	List<Map<String, MethodInfo>> unifiedToMethodList = RefactoringMetaData.getUnifiedToMethodList();
    	Map<String, MethodInfo> uTom = unifiedToMethodList.get(0);
    	Map<String, MethodInfo> uTom2 = null;    	
    	MethodInfo mInfo = null;
    	for(Entry<String, MethodInfo> entry : uTom.entrySet()){
    		key = entry.getKey();
    		mInfo = entry.getValue();
    		for(int i = 1; i < unifiedToMethodList.size(); i++){
    			uTom2 = unifiedToMethodList.get(i);
    			MethodInfo.merge(mInfo, uTom2.get(key));
    		}
    	}
    }

}
