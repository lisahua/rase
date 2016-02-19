package changeassistant.changesuggestion.expression.representation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;

public class AbstractExpressionRepresentationGenerator {
	
	private List<VariableTypeBindingTerm> variableTypeList;
	private List<TypeNameTerm> typeBindingList;
	private List<MethodNameTerm> methodTypeList;
	
	private ASTExpressionTransformer eTransformer;
	
	public AbstractExpressionRepresentationGenerator(){
		init();
	}
	
	public void init(){
		this.variableTypeList = new ArrayList<VariableTypeBindingTerm>();
		this.typeBindingList = new ArrayList<TypeNameTerm>();
		this.methodTypeList = new ArrayList<MethodNameTerm>();
		this.eTransformer = new ASTExpressionTransformer(variableTypeList,
				typeBindingList, methodTypeList);
	}

	public static List<String> createConcreteStringList(
			List<List<Term>> termLists) {
		List<String> result = new ArrayList<String>();
		StringBuffer buffer = null;
		String temp;
		for (List<Term> termList : termLists) {
			buffer = new StringBuffer();
			for (Term term : termList) {
				switch(term.getNodeType()){
				case ASTNode.CHARACTER_LITERAL:{
					temp = "'" + term.name + "'";
				}break;
				default:{
					temp = term.name;
				}
				}
				buffer.append(temp);
			}
			result.add(buffer.toString());
		}
		return result;
	}

	public static List<String> createStringList(List<List<Term>> termLists) {
		List<String> result = new ArrayList<String>();
		StringBuffer buffer = null;
		for (List<Term> termList : termLists) {
			buffer = new StringBuffer();
			for (Term term : termList) {
				if(term instanceof MethodNameTerm ||
						term instanceof VariableTypeBindingTerm ||
						term instanceof TypeNameTerm){
					buffer.append(term.getAbstractName());
				}else{
					buffer.append(term.getName());
				}
			}
			result.add(buffer.toString());
		}
		return result;
	}
	/**
	 * To create a term list for a method declaration's name
	 * @param methodName
	 * @param nodeType
	 * @return
	 */
	public List<Term> getTokenizedRepresentation(String methodName, int nodeType){
		eTransformer.clear();
		eTransformer.pushMethodNode(nodeType, methodName);
		return new ArrayList<Term>(eTransformer.fValueList);
	}

	public List<List<Term>> getTokenizedRepresentation(List<ASTNode> astNodes) {
		List<List<Term>> result = new ArrayList<List<Term>>();
		for (ASTNode astNode : astNodes) {
			eTransformer.clear();
			astNode.accept(eTransformer);
			result.add(new ArrayList<Term>(eTransformer.fValueList));
		}
		astNodes = null;
		return result;
	}

//	public static List<String> generateTokenizedRepresentation(Object obj) {
//		List<String> result = new ArrayList<String>();
//		if (obj instanceof ASTNode) {
//			ASTExpressionTransformer eTransformer = new ASTExpressionTransformer();
//			((Expression) obj).accept(eTransformer);
//			List<Term> terms = eTransformer.fValueList;
//			for (Term term : terms) {
//				result.add(term.name);
//			}
//		} else {
//			System.out.println("The passed in object is not an ASTNode object");
//		}
//		return result;
//	}

//	public String generateAbstractRepresentation(Object obj) {
//		String result = null;
//		if (obj instanceof ASTNode) {
//			ASTExpressionTransformer eTransformer = new ASTExpressionTransformer();
//			((ASTNode) obj).accept(eTransformer);
//			List<Term> terms = eTransformer.fValueList;
//			StringBuffer sb = new StringBuffer();
//			for (Term term : terms) {
//				if (term instanceof VariableTypeBindingTerm) {
//					sb.append(((VariableTypeBindingTerm) term).getTypeName());
//				} else {
//					sb.append(term.name);
//				}
//			}
//			result = sb.toString();
//		} else {
//			System.out
//					.println("The passed in object is not an instance of Expression");
//		}
//		return result;
//	}

	/**
	 * Generate abstract representation for expression. The abstract
	 * representation will do not contain variable name; instead, type name will
	 * be used
	 * 
	 * @param expressions
	 * @return
	 */
	// public List<String> generateAbstractRepresentation(List<ASTNode>
	// expressions){
	// Set<String> results = new HashSet<String>();
	// for(Object obj : expressions){
	// results.add(this.generateAbstractRepresentation(obj));
	// }
	// return new ArrayList<String>(results);
	// }
}
