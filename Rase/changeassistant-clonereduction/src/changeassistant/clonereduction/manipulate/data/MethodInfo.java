package changeassistant.clonereduction.manipulate.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import changeassistant.changesuggestion.expression.representation.ASTExpressionTransformer;
import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.clonereduction.manipulate.refactoring.IdGeneralizer;
import changeassistant.clonereduction.manipulate.refactoring.RefactoringMetaData;
import changeassistant.multipleexample.main.Constants;
import changeassistant.multipleexample.partition.SimpleASTCreator;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.peers.SourceCodeRange;

public class MethodInfo {	
	public String abstractName;
	public String name;
	public List<ASTNode> paramASTs;
	public String paramString;
	public String content;
	public String methodSignature;
	public String returnType;
	public List<String> types;
	public List<String> names;
	public Map<String, String> paramToName;
	public List<Integer> indexes;
	public List<String> otherKeys;
	public boolean needInst;
	public String instTypeName;
	public ASTNode astNode;
	public SimpleASTNode sASTNode;
	
	
	
	public MethodInfo(String name, List<ASTNode> paramASTs,
			ASTNode astNode, String abstractName, 
			boolean needInst, String instTypeName){
		this.abstractName = abstractName;
		this.name = name;
		this.paramASTs = paramASTs;
		this.needInst = needInst;
		this.instTypeName = instTypeName;
		this.astNode = astNode;
		MethodInvocation mi = (MethodInvocation)astNode;
		this.returnType = mi.resolveTypeBinding().getName();
		methodSignature = mi.resolveMethodBinding().toString();	
		types = new ArrayList<String>();
		names = new ArrayList<String>();		
		paramToName = new HashMap<String, String>();
		SimpleName sNode = null;
		String tmpStr2 = null;
		for(ASTNode param : paramASTs){
			switch(param.getNodeType()){
			case ASTNode.FIELD_ACCESS:
				sNode = ((FieldAccess)param).getName();
				types.add(sNode.resolveTypeBinding().getName());
				tmpStr2 = sNode.toString();
				names.add(tmpStr2);
				paramToName.put(param.toString(), tmpStr2);
				break;
			case ASTNode.SIMPLE_NAME:
				sNode = (SimpleName)param;
				types.add(sNode.resolveTypeBinding().getName());
				tmpStr2 = sNode.toString();
				paramToName.put(tmpStr2, tmpStr2);
				names.add(tmpStr2);
				break;
			case ASTNode.BOOLEAN_LITERAL:
				BooleanLiteral bNode = (BooleanLiteral)param;
				types.add("bool");
				tmpStr2 = bNode.toString();
				paramToName.put(tmpStr2, tmpStr2);
				names.add(tmpStr2);
				break;
			}			
		}
		indexes = new ArrayList<Integer>();
		for(int i = 0; i < paramASTs.size(); i++){
			indexes.add(i);
		}
		SimpleASTCreator creator = new SimpleASTCreator();
		sASTNode = creator.createSimpleASTNode(astNode);		
		Queue<SimpleASTNode> queue = new LinkedList<SimpleASTNode>();
		queue.add(sASTNode);
		SimpleASTNode sTmp = null;
		while(!queue.isEmpty()){
			sTmp = queue.remove();
			if(sTmp.getNodeType() == ASTNode.METHOD_INVOCATION && needInst){
				//no invoker
				if(!((SimpleASTNode)sTmp.getChildAt(1)).getStrValue().equals(".")){				
					sTmp.insert(new SimpleASTNode(ASTNode.SIMPLE_NAME, Constants.INSTANCE, 0, 0), 0);
					sTmp.insert(new SimpleASTNode(ASTExpressionTransformer.DOT, ".", 0, 0), 1);
					sTmp.setRecalcToRoot();
				}
			}
			if(sTmp.getChildCount() > 0){
				for(int i = 0; i < sTmp.getChildCount(); i++){
					queue.add((SimpleASTNode) sTmp.getChildAt(i));
				}
			}
		}			
		updateParamAndContent(paramToName);
	}
	
	public void updateParamAndContent(Map<String, String> convertMap){
		SimpleASTNode.convert(sASTNode, convertMap);
		StringBuffer buffer = new StringBuffer();	
		updateParamString();			
		if(!this.returnType.equals(Constants.VOID)){
			buffer.append("return ");
		}							
		buffer.append(sASTNode.constructStrValue());
		this.content = buffer.toString();
	}
	
	
	private static boolean findItem(MethodInfo mInfo, String tmpStr){		
		String tmp = mInfo.content;
		if(tmp.contains("return ")){
			tmp = tmp.substring("return ".length());
		}
		boolean tmpContainsDot = tmp.contains(".");
		boolean tmpStrContainsDot = tmpStr.contains(".");
		if(tmpContainsDot && tmpStrContainsDot){
			tmp = tmp.substring(0, tmp.indexOf('.'));
			if(tmpStr.substring(0, tmpStr.indexOf('.')).equals(tmp))
				return true;
			return false;
		}else if(!tmpContainsDot && !tmpStrContainsDot){
			return true;
		}else{// tmpContainsDot = true, tmpStrContainsDot = false
			return true;
		}
	}
	
	public static boolean findItem(MethodInfo mInfo, String tmpStr,
			Map<String, MethodInfo> uTom){
		boolean findTheItem = true;
		if(findItem(mInfo, tmpStr)){
			// do nothing
		}else{			
			findTheItem = false;
			if(mInfo.otherKeys == null)
				return findTheItem;
			for(String key : mInfo.otherKeys){
				if(findItem(uTom.get(key), tmpStr)){
					findTheItem = true;
					break;
				}
			}
		}		
		return findTheItem;
	}
	
	public static MethodInfo getMethodInfo(MethodInfo mInfo, String tmpStr, 
			Map<String, MethodInfo> uTom){
		if(findItem(mInfo, tmpStr)){
			return mInfo;
		}
		if(mInfo.otherKeys != null){
			for(String key : mInfo.otherKeys){
				mInfo = uTom.get(key);
				if(findItem(mInfo, tmpStr)){
					return mInfo;
				}
			}
		}		
		return mInfo;
	}
	
	public static String getDefaultValue(String type){
		if(Constants.NUM_TYPES.contains(type)){
			return Constants.DEFAULT_NUM;
		}
		return Constants.DEFAULT_OBJ;
	}
	
	public void updateParamString(){
		StringBuffer buffer = new StringBuffer();
		String name = null;
		for(int i = 0; i < names.size(); i++){
			name = names.get(i);
			if(paramASTs.get(i).getNodeType() != ASTNode.BOOLEAN_LITERAL)
				buffer.append(name).append(",");
		}
		if(!names.isEmpty()){
			buffer.setLength(buffer.length() - 1);	
		}	
		paramString = buffer.toString();
	}
	
	public static void merge(MethodInfo mInfo, MethodInfo mInfo2){
		List<String> types = new ArrayList<String>(mInfo.types);
		List<String> names = new ArrayList<String>(mInfo.names);
		List<String> types1 = new ArrayList<String>(mInfo.types);
		List<String> types2 = new ArrayList<String>(mInfo2.types);
		if(types1.size() != types2.size()){
			types.addAll(types2);
			names.addAll(mInfo2.names);			
		}
		if(!types1.equals(types)){
			mInfo.types = types;
			mInfo.names = names;
			mInfo.updateParamString();
			mInfo2.types = types;
			mInfo2.names = names;
			mInfo2.paramString = mInfo.paramString;
			List<Integer> indexes = mInfo2.indexes;
			indexes.clear();
			int types1Size = types1.size();
			for(int i = 0; i < types2.size(); i++){
				indexes.add(types1Size + i);
			}
		}
		if(!mInfo.returnType.equals(mInfo2.returnType)){			
			TypeInfo value = null;
			boolean hasReplaced = false;
			for(Map<String, TypeInfo> uTot : RefactoringMetaData.getUnifiedToTypeList()){
				for(Entry<String, TypeInfo> entry : uTot.entrySet()){
					value = entry.getValue();
					if(value.content.equals(mInfo.returnType) || value.content.equals(mInfo2.returnType)){
						mInfo.returnType = mInfo2.returnType = value.name;
						hasReplaced = true;
						break;
					}
				}
				if(hasReplaced)
					break;
			}			
		}
//		if(mInfo.otherKeys != null){
//			mInfo2.otherKeys = mInfo.otherKeys;
//		}else if(mInfo2.otherKeys != null){
//			mInfo.otherKeys = mInfo2.otherKeys;
//		}
	}
	
	@Override
	public String toString(){
		StringBuffer buffer = new StringBuffer(name + "(");
		buffer.append(content).append(")(");
		for(String name : names){
			buffer.append(name).append(",");
		}
		if(!names.isEmpty()){
			buffer.setLength(buffer.length() - 1);
		}
		buffer.append(")");
		return buffer.toString();
	}
	
	public void addForward(String otherKey){
		if(otherKeys == null){
			otherKeys = new ArrayList<String>();
		}
		otherKeys.add(otherKey);
	}
	
	public static String createNewMTerm(Collection<String> abstractNames, Set<String> knownKeys){
		int maxIndex = -1;
		int index = -1;
		for(String aName : abstractNames){
			if(Term.M_Pattern.matcher(aName).matches()){
				index = Term.parseInt(aName);
				if(index > maxIndex){
					maxIndex = index;
				}
			}			
		}
		for(String key : knownKeys){
			index = Term.parseInt(key);
			if(index > maxIndex){
				maxIndex = index;
			}
		}
		return "m$_" + Integer.toString(maxIndex + 1) + "_";
	}
	
	public String createDeclaredMethodSignature(Map<String, TypeInfo> uTot){
		StringBuffer buffer = new StringBuffer(returnType + " " + name + "(");
		String name = null;
		String type = null;
		for(Integer index : indexes){
			name = names.get(index);
			if(paramASTs.get(index).getNodeType() == ASTNode.BOOLEAN_LITERAL){
				//do nothing
			}else{
				type = types.get(index);
				type = IdGeneralizer.generalizeType(type, uTot);
				buffer.append(type).append(" ").append(name).append(",");
			}			
		}
		if(needInst){
			String instType = IdGeneralizer.generalizeType(instTypeName, uTot);
			buffer.append(instType).append(" ").append("inst");
		}
		if(buffer.charAt(buffer.length() - 1) == ',')
			buffer.setLength(buffer.length() - 1);
		buffer.append(")");
		return buffer.toString();
	}
	
/*	
	public String createMethodCall(Map<String, String> sTou){
		if(needInst)
			return name + "(" + paramString + ", inst)";
		else{
//			if(sParent.getNodeType() == ASTNode.METHOD_INVOCATION && sParent.getIndex(sTmp) != 0){
//				StringBuffer prefix = new StringBuffer();
//				for(int i = 0; i < sParent.getIndex(sTmp); i++){
//					prefix.append(sParent.getChildAt(i));
//				}
//				if(prefix.charAt(prefix.length() - 1) == '.'){
//					prefix.setLength(prefix.length() - 1);
//				}
//				return name + "(" + paramString + ", " + prefix.toString() + ")";
//			}else{
//				return name + "(" + paramString + ")";
//			}	
			return name + "(" + paramString + ")";
		}
	}
	*/
}
