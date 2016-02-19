package changeassistant.multipleexample.common;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import changeassistant.changesuggestion.expression.representation.ASTExpressionTransformer;
import changeassistant.changesuggestion.expression.representation.MethodNameTerm;
import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.Term.TermType;
import changeassistant.changesuggestion.expression.representation.TermsList;
import changeassistant.changesuggestion.expression.representation.TypeNameTerm;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;
import changeassistant.internal.ASTNodeFinder;
import changeassistant.multipleexample.datastructure.MapList;
import changeassistant.multipleexample.datastructure.QueueList;
import changeassistant.multipleexample.partition.MappingException;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.util.IdMapper;
import changeassistant.multipleexample.util.SameChecker;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.peers.SourceCodeRange;

public class CommonParserMulti {
	int abstractVariable = 0, abstractMethod = 0, abstractType = 0, abstractUnknown = 0;
	MapList specificToUnifiedList = null;
	List<MethodDeclaration> mdList = null;
	public CommonParserMulti(int dimension, MapList mapList){
		specificToUnifiedList = mapList;
		Collection<String> values = specificToUnifiedList.get(0).values();
		IdMapper.calcMaxAbsCounter(values);
		abstractVariable = IdMapper.abstractVariable + 1;
		abstractMethod = IdMapper.abstractMethod + 1;
		abstractType = IdMapper.abstractType + 1;
		abstractUnknown = IdMapper.abstractUnknown + 1;
	}
	public List<SimpleASTNode> getCommon(List<List<SimpleASTNode>> stmts, List<MethodDeclaration> mdList) throws Exception{
		this.mdList = mdList;
		List<SimpleASTNode> result = new ArrayList<SimpleASTNode>();
		SimpleASTNode common = null;
		List<SimpleASTNode> exprPeers = new ArrayList<SimpleASTNode>();
		QueueList queueList = null;
		Queue<SimpleASTNode> newQueue = new LinkedList<SimpleASTNode>();
		Queue<SimpleASTNode> queue0 = null;
		SimpleASTNode expr = null;
		SimpleASTNode node0 = null, node = null, newNode = null;
		List<SimpleASTNode> peerNodes = null;
		List<String> names = null;
		List<Enumeration<SimpleASTNode>> childEnums = null;
		Enumeration<SimpleASTNode> childEnum0 = null;
		String abstractName = null;
		boolean sameStrValue = false;
		boolean sameNodeType = false;
		boolean sameChildCount = false;
		for(int i = 0; i < stmts.get(0).size(); i++){
			exprPeers.clear();
			newQueue.clear();
			for(int j = 0; j < stmts.size(); j++){
				expr = stmts.get(j).get(i);
				exprPeers.add(expr);
			}
			System.out.print("");
			common = parseCommon(exprPeers);
			if(common.getNodeType() == SimpleASTNode.UNDECIDED_NODE_TYPE){
				continue;
			}
			newQueue.add(common);
			queueList = new QueueList(stmts.size());
			queueList.addElems(exprPeers);			
			queue0 = queueList.getFirst();
			while(!queue0.isEmpty()){
				peerNodes = queueList.removePeerNodes();
				newNode = newQueue.remove();
				node0 = peerNodes.get(0);		
				sameStrValue = QueueList.allHasValue(peerNodes, node0.getStrValue());
				sameNodeType = QueueList.allHasNodeType(peerNodes, node0.getNodeType());
				sameChildCount = QueueList.allHasChildCount(peerNodes, node0.getChildCount());
				if(!sameChildCount){
					List<Term> termList = new ArrayList<Term>();
					List<Integer> nodeTypeList = new ArrayList<Integer>();
					if(sameStrValue){
//						names = QueueList.constructListLiteralNames(peerNodes);
//						for(int k = 0; k < names.size(); k++){
//							nodeTypeList.add(ASTExpressionTransformer.LIST_LITERAL);
//							termList.add(new Term(-1, names.get(k)));
//						}
						throw new Exception("The number of parameters for a method call does not match");						
					}else{
						names = QueueList.getNames(peerNodes);
						for(int k = 0; k < names.size(); k++){
							nodeTypeList.add(peerNodes.get(k).getNodeType());
							termList.add(new Term(-1, names.get(k)));
						}
					}
					Term commonTerm = Term.getDefaultTerm();
					commonTerm = parseCommonTerm(commonTerm, termList, nodeTypeList, TermType.Term, null);
					abstractName = commonTerm.getName();					
					if(sameNodeType){
						node = new SimpleASTNode(node0.getNodeType(), abstractName, 0, 0);
					}else{
						node = new SimpleASTNode(SimpleASTNode.UNDECIDED_NODE_TYPE, abstractName, 0, 0);
					}
					node.setMarked();
					newNode.add(node);
					newNode.setRecalcToRoot();
					continue;
				}else if(node0.getChildCount() == 0){
					continue;
				}
				if(sameNodeType && node0.getNodeType() == ASTNode.SUPER_CONSTRUCTOR_INVOCATION){
					throw new Exception("Super constructor call cannot be extracted into a method");
				}
				childEnums = QueueList.getChildEnums(peerNodes);
				childEnum0 = childEnums.get(0);
				while(childEnum0.hasMoreElements()){
					peerNodes = QueueList.getNextChildList(childEnums);
					node = parseCommon(peerNodes);
					if(node.hasMark()){
						newNode.setRecalcToRoot();
					}
					queueList.addElems(peerNodes);
					newQueue.add(node);
					newNode.add(node);
				}				
			}
			cleanUp(common);
			result.add(common);
		}		
		return result;
	}
	
	public String getCommonTypeName(List<Term> typeTerms) throws Exception{
		if(typeTerms.isEmpty())
			return null;
		Term commonTerm = (Term) typeTerms.get(0).clone();
		commonTerm = parseCommonTerm(commonTerm, typeTerms, null, TermType.TypeNameTerm, null);
		return commonTerm.getName();
	}
	
	private String getAbstractUnknown(List<String> termNameList, List<Integer> nodeTypeList) throws Exception{
		String commonTermName = getMappedIdentifier(termNameList);
		if(commonTermName == null){
			commonTermName = Term.createAbstractName(ASTExpressionTransformer.ABSTRACT_UNKNOWN, abstractUnknown++);
			commonTermName = commonTermName + Term.createExprSuffix(nodeTypeList);					
		}	
		return commonTermName;
	}
	
	private void cleanUp(SimpleASTNode common){
		SimpleASTNode tmp = null;
		if(common.hasRecalc()){
			common.constructStrValue(specificToUnifiedList);
			Enumeration<SimpleASTNode> dEnum = common.depthFirstEnumeration();
			while(dEnum.hasMoreElements()){
				tmp = dEnum.nextElement();
				if(tmp.hasMark()){
					tmp.clearMarked();
				}
			}
		}
	}
	
	private SimpleASTNode parseCommon(List<SimpleASTNode> exprPeers) throws Exception{
		SimpleASTNode expr0 = exprPeers.get(0);	
		SimpleASTNode common = (SimpleASTNode)expr0.clone();		
		int nodeType0 = expr0.getNodeType();
		String strValue0 = expr0.getStrValue();
		if(strValue0.equals(SimpleASTNode.LIST_LITERAL) && !QueueList.allHasValue(exprPeers, SimpleASTNode.LIST_LITERAL)){
			return common;
		}
		if(!QueueList.allHasNodeType(exprPeers, nodeType0)){
			common.setNodeType(SimpleASTNode.UNDECIDED_NODE_TYPE);
		}
		List<Term> termList = new ArrayList<Term>();
		List<TermType> termTypeList = new ArrayList<TermType>();
		boolean allNotNull = true;
		TermType termType = TermType.Term;
		Term term = null;
		Term commonTerm = null;
		String commonTermName = null;
		List<Integer> nodeTypeList = new ArrayList<Integer>();
		int nodeType = -1;
		for(int i = 0; i < exprPeers.size(); i++){
			term = exprPeers.get(i).getTerm();
			termList.add(term);
			if(term == null){
				allNotNull = false;
				termType = null;
				nodeType = exprPeers.get(i).getNodeType();				
			}else{
				termType = term.getTermType();
				nodeType = term.getNodeType();
			}
			termTypeList.add(termType);
			nodeTypeList.add(nodeType);
		}
		termType = termTypeList.get(0);
		boolean sameTermType = new SameChecker<TermType>().areSame(termTypeList); 		
		commonTerm = common.getTerm();
		if(allNotNull){
			if(!sameTermType){
				throw new MappingException("Different types of terms cannot be mapped together");
			}
			String termName0 = termList.get(0).getName();					
			commonTerm = parseCommonTerm(commonTerm, termList, nodeTypeList, termType, (SimpleASTNode)expr0.getParent());
			commonTermName = commonTerm.getName();			
			common.setStrValue(commonTermName);						
			if(commonTermName == null && termName0 != null){
				common.setMarked();
			}else if(commonTermName != null && !commonTermName.equals(termName0)){
				common.setMarked();
			}
		}else{//at least one expr have null term
			if(new SameChecker<Term>().areSame(termList) && termList.get(0) == null){
				commonTermName = null;
			}else{
				termList.clear();
				for(int i = 0; i < exprPeers.size(); i++){
					termList.add(new Term(-1, exprPeers.get(i).getStrValue()));					
				}	
				commonTerm = parseCommonTerm(commonTerm, termList, nodeTypeList, TermType.Term, null);
				commonTermName = commonTerm.getName();
				if(commonTermName != null){
					common.setStrValue(commonTermName);
					common.setMarked();	
				}		
			}				
		}
		if(termType != null && termType.equals(TermType.MethodNameTerm) && sameTermType){
			collectMappedMIs(termList, exprPeers, commonTermName, commonTerm);
		}
		common.setTerm(commonTerm);
		common.setGeneral();
		return common;
	}
	
	private void collectMappedMIs(List<Term> termList, List<SimpleASTNode> exprPeers, String mName,
			Term commonTerm) throws Exception{
		System.out.print("");
		SimpleASTNode expr = null;
		List<SourceCodeRange> scrs = new ArrayList<SourceCodeRange>();
		for(int i = 0; i < exprPeers.size(); i++){
			expr = exprPeers.get(i);
			while(expr.getNodeType() != ASTNode.METHOD_INVOCATION
					&& expr.getNodeType() != ASTNode.METHOD_DECLARATION && expr.getNodeType() != ASTNode.ANONYMOUS_CLASS_DECLARATION){
				expr = (SimpleASTNode)expr.getParent();
			}		
			if(expr.getNodeType() == ASTNode.METHOD_DECLARATION || expr.getNodeType() == ASTNode.ANONYMOUS_CLASS_DECLARATION){
//				scrs.add(exprPeers.get(i).getScr());// this expr should be SimpleName--the method's name
				return;
			}else{
				scrs.add(expr.getScr());
			}			
		}
		processMI(termList, scrs, commonTerm);
		specificToUnifiedList.appendMethodInvocationMap(mName, scrs);
	}
	
	private void processMI(List<Term> termList, List<SourceCodeRange> scrs,
			Term commonTerm) throws Exception{
		ASTNodeFinder finder = new ASTNodeFinder();
		MethodInvocation mi = null;
		IMethodBinding binding = null;
		List<String> invokerTypeNames = new ArrayList<String>();
		List<String> qNames = new ArrayList<String>();
		ITypeBinding tBinding = null;
		String invokerTypeName = null;
		String qName = null;
		for(int i = 0; i < scrs.size(); i++){
			mi = (MethodInvocation)finder.lookforASTNode(mdList.get(i), scrs.get(i));
			binding = mi.resolveMethodBinding();
			if(binding != null){
				tBinding = binding.getDeclaringClass();
				invokerTypeName = tBinding.getName();
				qName = tBinding.getQualifiedName();
				invokerTypeNames.add(invokerTypeName);
				qNames.add(qName);
			}else{
				invokerTypeNames.add("Object");
				qNames.add("java.io.Object");
			}			
		}
		if(!new SameChecker<String>().areSame(invokerTypeNames)){
			List<Term> typeTerms = new ArrayList<Term>();
			for(int j = 0; j < invokerTypeNames.size(); j++){
				typeTerms.add(new TypeNameTerm(-1, invokerTypeNames.get(j), qNames.get(j)));
			}
			getCommonTypeName(typeTerms);
			((MethodNameTerm)commonTerm).setTypeNameTerm((TypeNameTerm) typeTerms.get(0).clone());
		}
	}
	
	/**
	 * Get the common term mapped by termList
	 * Side effect: the new mappings between the abstractName and every one in the termNameList are appended to specificToUnifiedList
	 * @param termNameList
	 * @param nodeTypeList
	 * @param termType0
	 * @return
	 * @throws Exception
	 */
	private Term parseCommonTerm(Term commonTerm, List<Term> termList, List<Integer> nodeTypeList, TermType termType0,
			SimpleASTNode parentNode) throws Exception{
		System.out.print("");
		List<String> termNameList = new ArrayList<String>();
		List<String> typeTermNameList = new ArrayList<String>();
		List<Term> typeNameTermList = new ArrayList<Term>();
		
		TypeNameTerm tTerm = null;
		for(Term term : termList){
			if(term == null){
				termNameList.add(null);
				typeNameTermList.add(null);
				typeTermNameList.add(null);
			}else{
				termNameList.add(term.getName());
				if(termType0.equals(TermType.VariableTypeBindingTerm)){
					tTerm = ((VariableTypeBindingTerm)term).getTypeNameTerm();
					typeTermNameList.add(tTerm.getName());
					typeNameTermList.add(tTerm);
				}		
			}				
		}		
		String termName0 = termNameList.get(0);
		String commonTermName = getMappedIdentifier(termNameList);
		if(commonTermName == null){
			boolean sameTermName = new SameChecker<String>().areSame(termNameList);
			boolean needAbstraction = true;
			
			if(sameTermName){
				needAbstraction = false;
				commonTermName = termName0;
//				if(commonTermName == null){
//					needAbstraction = false;
//				}
			}
		
			if(needAbstraction){
				switch(termType0){
				case VariableTypeBindingTerm:
					boolean areTypeNames = true;
					String tmpTypeName = null;
					for(int i = 0; i < termList.size(); i++){
						tmpTypeName = ((VariableTypeBindingTerm)termList.get(i)).getTypeNameTerm().getName();
						if(!tmpTypeName.equals(termNameList.get(i))){
							areTypeNames = false;
							break;
						}
					}
					if(!areTypeNames)
						commonTermName = Term.createAbstractName(ASTExpressionTransformer.ABSTRACT_VARIABLE, abstractVariable++);
					else
						commonTermName = Term.createAbstractName(ASTExpressionTransformer.ABSTRACT_TYPE, abstractType++);						
					break;
				case MethodNameTerm:												
					commonTermName = Term.createAbstractName(ASTExpressionTransformer.ABSTRACT_METHOD, abstractMethod++);
					break;
				case TypeNameTerm:						
					commonTermName = Term.createAbstractName(ASTExpressionTransformer.ABSTRACT_TYPE, abstractType++);
					break;
				case Term:
					commonTermName = Term.createAbstractName(ASTExpressionTransformer.ABSTRACT_UNKNOWN, abstractUnknown++);
					commonTermName = commonTermName + Term.createExprSuffix(nodeTypeList);
				}
			}	
		}	
		if(commonTermName != null){
			specificToUnifiedList.putMultiOneMap(termNameList, commonTermName);		
			switch(termType0){
			case VariableTypeBindingTerm:
				if(commonTermName.equals("this") && parentNode != null && parentNode.getNodeType() == ASTNode.FIELD_ACCESS){
					// do nothing
				}else{
					TypeNameTerm commonTypeTerm = (TypeNameTerm)typeNameTermList.get(0).clone();
					parseCommonTerm(commonTypeTerm, typeNameTermList, null, TermType.TypeNameTerm, null);
					((VariableTypeBindingTerm)commonTerm).getTypeNameTerm().setName(commonTypeTerm.getName());
				}	
				break;
			case TypeNameTerm:
				if(Term.T_Pattern.matcher(commonTermName).matches()){
					if(parentNode != null){
						switch(parentNode.getNodeType()){
							case ASTNode.TYPE_LITERAL:
							case ASTNode.INSTANCEOF_EXPRESSION:
							case ASTNode.CLASS_INSTANCE_CREATION:
								throw new Exception("The parameterized type cannot be used to do initialization or type check");
						}
					}
				}
				break;
			}			
		}
		if(commonTerm != null){
			commonTerm.setName(commonTermName);
		}else if(termType0.equals(TermType.Term)){
			commonTerm = new Term(-1, commonTermName);
		}
		return commonTerm;
	}
	
	/**
	 * Purpose: 1. identify already established mappings if there exist. 2. detect mapping conflict.
	 * @param termNameList
	 * @return
	 * @throws MappingException
	 */
	private String getMappedIdentifier(List<String> termNameList) throws MappingException{
		System.out.print("");
		List<String> mappedNameList = new ArrayList<String>();
		String name = null;
		for(int i = 0; i < termNameList.size(); i++){
			name = termNameList.get(i);
			if(name != null)
				mappedNameList.add(specificToUnifiedList.get(i).get(name));
			else
				mappedNameList.add(null);
		}
		String termName0 = termNameList.get(0);
		String name0 = mappedNameList.get(0);
		if(name0 == null){
			for(int i = 1; i < mappedNameList.size(); i++){
				name = mappedNameList.get(i);
				if(name != null){
					throw new MappingException(termNameList.get(i) + " in example " + i +
							" cannot get mapped to " + termName0 + " in 0 because it is already mapped to another one");
				}
			}
		}else{
			for(int i = 1; i < mappedNameList.size(); i++){
				name = mappedNameList.get(i);
				if(!name0.equals(name)){
					throw new MappingException(termNameList.get(i) + " in example " + i + 
							" cannot get mapped to " + termName0 + " in 0 because the latter is mapped differently");
				}
			}
		}
		return name0;
	}
}
