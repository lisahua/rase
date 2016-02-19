package changeassistant.peers;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import changeassistant.change.group.model.SubTreeModel;
import changeassistant.changesuggestion.expression.representation.MethodNameTerm;
import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.TypeNameTerm;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;
import changeassistant.peers.comparison.Node;

public class SubTreeModelPair {
//left is candidate, right is sub tree model
	private SubTreeModel left;
	private SubTreeModel right;
	private Map<TypeNameTerm, TypeNameTerm> typeMap;
	private Map<MethodNameTerm, MethodNameTerm> methodMap;
	private Map<VariableTypeBindingTerm, VariableTypeBindingTerm> variableMap;
	
	public SubTreeModelPair(SubTreeModel left, SubTreeModel right){
		this.left = left;
		this.right = right;
	}
	
	public boolean containsKey(Term key){
		return typeMap.containsKey(key)||methodMap.containsKey(key)||variableMap.containsKey(key);
	}
	
	/**
	 * The return value indicates where there is conflict lying within the pair
	 * This method should always be called before any getXXX() method invocation
	 * @return
	 */
	public boolean doMap(){
		boolean success = true;
		if(typeMap != null){
			//do nothing, since the creation is already done before
		}else{
			typeMap = new HashMap<TypeNameTerm, TypeNameTerm>();
			methodMap = new HashMap<MethodNameTerm, MethodNameTerm>();
			variableMap = new HashMap<VariableTypeBindingTerm, VariableTypeBindingTerm>();
			if(left.getAbstractExpressions().size() != right.getAbstractExpressions().size()){
				//do nothing
			}else{
				List<List<Term>> abstractExpressions1 = left.getAbstractExpressions();
				List<List<Term>> abstractExpressions2 = right.getAbstractExpressions();
				List<Term> abstractExpression1, abstractExpression2;
				Term term1, term2;
				
				for(int i = 0; i < abstractExpressions1.size(); i++){
					success = true;
					abstractExpression1 = abstractExpressions1.get(i);
					abstractExpression2 = abstractExpressions2.get(i);
					if(abstractExpression1.size() != abstractExpression2.size())
						continue;
					for(int j = 0; j < abstractExpression1.size(); j++){
						term1 = abstractExpression1.get(j);
						term2 = abstractExpression2.get(j);
						if(!term1.getTermType().equals(term2.getTermType())){
							//should take it as correct matching
							break;
						}else{
							switch(term1.getTermType()){
							case TypeNameTerm:{
								if(MatchWorker.detectConflict(term1, term2, 
										new HashMap<Term, Term>(typeMap))){
									success = false;
									break;
								}else{
									typeMap.put((TypeNameTerm)term1, (TypeNameTerm)term2);
								}
							}break;
							case MethodNameTerm:{
								if(MatchWorker.detectConflict(term1, term2,
										new HashMap<Term, Term>(methodMap))){
									success = false;
									break;
								}else{
									methodMap.put((MethodNameTerm)term1, (MethodNameTerm)term2);
								}
							}break;
							case VariableTypeBindingTerm:{
								if(MatchWorker.detectConflict(term1, term2, 
										new HashMap<Term, Term>(variableMap))){
									success = false;
									break;
								}else{
									VariableTypeBindingTerm vTerm1 = (VariableTypeBindingTerm)term1;
									VariableTypeBindingTerm vTerm2 = (VariableTypeBindingTerm)term2;
									variableMap.put(vTerm1, vTerm2);
									TypeNameTerm tTerm1 = vTerm1.getTypeNameTerm();
									TypeNameTerm tTerm2 = vTerm2.getTypeNameTerm();
									if(MatchWorker.detectConflict(tTerm1, tTerm2, 
											new HashMap<Term, Term>(typeMap))){
										success = false;
										break;
									}else{
										typeMap.put(tTerm1, tTerm2);
									}
								}
							}break;
							case Term:{
								//do nothing, since they are the same
							}break;
							}
						}
					}
				}
			}
		}
		if(success){
			if(typeMap.isEmpty()){
				typeMap = Collections.emptyMap();
			}
			if(methodMap.isEmpty()){
				methodMap = Collections.emptyMap();
			}
			if(variableMap.isEmpty()){
				variableMap = Collections.emptyMap();
			}
		}else{//not successful
			typeMap = null;
			methodMap = null;
			variableMap = null;
		}
		
		return success;
	}
	
	public Term get(Term key){
		switch(key.getTermType()){
		case TypeNameTerm:{
			return typeMap.get(key);
		}
		case MethodNameTerm:{
			return methodMap.get(key);
		}
		case VariableTypeBindingTerm:{
			return variableMap.get(key);
		}
		}
		return null;
	}
	
	public void undoMap(){
		typeMap = null;
		methodMap = null;
		variableMap = null;
	}
	
	public boolean equals(Object obj){
		if(!(obj instanceof SubTreeModelPair)) return false;
		SubTreeModelPair other = (SubTreeModelPair)obj;
		if(!this.left.equals(other.left)) return false;
		if(!this.right.equals(other.right)) return false;
		return true;
	}
	
	public SubTreeModel getLeft(){
		return this.left;
	}
	
	public SubTreeModel getRight(){
		return this.right;
	}
	
	public Map<MethodNameTerm, MethodNameTerm> getMethodMap(){
		
		return this.methodMap;
	}
	
	public Map<TypeNameTerm, TypeNameTerm> getTypeMap(){
		return this.typeMap;
	}
	
	public Map<VariableTypeBindingTerm, VariableTypeBindingTerm>
	getVariableMap(){
		return this.variableMap;
	}
	
	public int hashCode(){
		return left.hashCode() * 1000 + right.hashCode();
	}
	
	public String toString(){
		return "left node is " + left.toString() + ", and right node is " + right.toString();
	}
}
