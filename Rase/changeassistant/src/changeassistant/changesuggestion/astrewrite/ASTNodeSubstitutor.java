//package changeassistant.changesuggestion.astrewrite;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Stack;
//
//import org.eclipse.jdt.core.dom.AST;
//import org.eclipse.jdt.core.dom.ASTNode;
//import org.eclipse.jdt.core.dom.ASTParser;
//import org.eclipse.jdt.core.dom.ASTVisitor;
//import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
//import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
//import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
//import org.eclipse.jdt.core.dom.ArrayAccess;
//import org.eclipse.jdt.core.dom.ArrayCreation;
//import org.eclipse.jdt.core.dom.ArrayInitializer;
//import org.eclipse.jdt.core.dom.ArrayType;
//import org.eclipse.jdt.core.dom.AssertStatement;
//import org.eclipse.jdt.core.dom.Assignment;
//import org.eclipse.jdt.core.dom.Block;
//import org.eclipse.jdt.core.dom.BooleanLiteral;
//import org.eclipse.jdt.core.dom.BreakStatement;
//import org.eclipse.jdt.core.dom.CastExpression;
//import org.eclipse.jdt.core.dom.CatchClause;
//import org.eclipse.jdt.core.dom.CharacterLiteral;
//import org.eclipse.jdt.core.dom.ClassInstanceCreation;
//import org.eclipse.jdt.core.dom.CompilationUnit;
//import org.eclipse.jdt.core.dom.ConditionalExpression;
//import org.eclipse.jdt.core.dom.ConstructorInvocation;
//import org.eclipse.jdt.core.dom.ContinueStatement;
//import org.eclipse.jdt.core.dom.DoStatement;
//import org.eclipse.jdt.core.dom.EnhancedForStatement;
//import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
//import org.eclipse.jdt.core.dom.Expression;
//import org.eclipse.jdt.core.dom.ExpressionStatement;
//import org.eclipse.jdt.core.dom.FieldAccess;
//import org.eclipse.jdt.core.dom.FieldDeclaration;
//import org.eclipse.jdt.core.dom.ForStatement;
//import org.eclipse.jdt.core.dom.IfStatement;
//import org.eclipse.jdt.core.dom.InfixExpression;
//import org.eclipse.jdt.core.dom.Initializer;
//import org.eclipse.jdt.core.dom.InstanceofExpression;
//import org.eclipse.jdt.core.dom.LabeledStatement;
//import org.eclipse.jdt.core.dom.MemberRef;
//import org.eclipse.jdt.core.dom.MemberValuePair;
//import org.eclipse.jdt.core.dom.MethodDeclaration;
//import org.eclipse.jdt.core.dom.MethodInvocation;
//import org.eclipse.jdt.core.dom.MethodRef;
//import org.eclipse.jdt.core.dom.MethodRefParameter;
//import org.eclipse.jdt.core.dom.Name;
//import org.eclipse.jdt.core.dom.NullLiteral;
//import org.eclipse.jdt.core.dom.NumberLiteral;
//import org.eclipse.jdt.core.dom.ParameterizedType;
//import org.eclipse.jdt.core.dom.ParenthesizedExpression;
//import org.eclipse.jdt.core.dom.PostfixExpression;
//import org.eclipse.jdt.core.dom.PrefixExpression;
//import org.eclipse.jdt.core.dom.PrimitiveType;
//import org.eclipse.jdt.core.dom.QualifiedName;
//import org.eclipse.jdt.core.dom.QualifiedType;
//import org.eclipse.jdt.core.dom.ReturnStatement;
//import org.eclipse.jdt.core.dom.SimpleName;
//import org.eclipse.jdt.core.dom.SimpleType;
//import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
//import org.eclipse.jdt.core.dom.StringLiteral;
//import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
//import org.eclipse.jdt.core.dom.SuperFieldAccess;
//import org.eclipse.jdt.core.dom.SuperMethodInvocation;
//import org.eclipse.jdt.core.dom.SwitchCase;
//import org.eclipse.jdt.core.dom.SwitchStatement;
//import org.eclipse.jdt.core.dom.SynchronizedStatement;
//import org.eclipse.jdt.core.dom.ThisExpression;
//import org.eclipse.jdt.core.dom.ThrowStatement;
//import org.eclipse.jdt.core.dom.TryStatement;
//import org.eclipse.jdt.core.dom.TypeDeclaration;
//import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
//import org.eclipse.jdt.core.dom.TypeLiteral;
//import org.eclipse.jdt.core.dom.TypeParameter;
//import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
//import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
//import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
//import org.eclipse.jdt.core.dom.WhileStatement;
//import org.eclipse.jdt.core.dom.WildcardType;
//
//import changeassistant.peers.MethodPair;
//
//public class ASTNodeSubstitutor extends ASTVisitor{
//	private List<ASTNode> method1Specific;
//	private List<ASTNode> method2Specific;
//	private AST ast;
//	private Stack<ASTNode> stack ;
//	
//	public ASTNodeSubstitutor(MethodPair mp, AST ast){
//		this.method1Specific = mp.method1Specific;
//		this.method2Specific = mp.method2Specific;
//		this.ast = ast; 
//		this.stack = new Stack<ASTNode>();
//	}
//	
//	public ASTNode getComposedASTNode(){
//		return this.stack.peek();
//	}
//	
//	@Override
//	public boolean visit(AnnotationTypeDeclaration node){
//		return false;
//	}
//	@Override
//	public boolean visit(AnnotationTypeMemberDeclaration node){
//		return false;
//	}
//	@Override
//	public boolean visit(AnonymousClassDeclaration node){
//		return false;
//	}
//	
//	public boolean visit(ArrayAccess node){
//		return false;
//	}
//	
//	public boolean visit(ArrayCreation node){
//		return false;
//	}
//	
//	public boolean visit(ArrayInitializer node){
//		return false;
//	}
//	
//	public boolean visit(ArrayType node){
//		return false;
//	}
//	
//	public boolean visit(AssertStatement node){
//		return false;
//	}
//	
//	public boolean visit(Assignment node){
//		return false;
//	}
//	
//	public boolean visit(Block node){
//		return false;
//	}
//	
//	public boolean visit(BooleanLiteral node){
//		simpleNodeProcess(node);
//		return false;
//	}
//	
//	public boolean visit(BreakStatement node){
//		return false;
//	}
//	
//	public boolean visit(CastExpression node){
//		return false;
//	}
//	
//	public boolean visit(CatchClause node){
//		return false;
//	}
//	
//	public boolean visit(CharacterLiteral node){
//		simpleNodeProcess(node);
//		return false;
//	}
//	
//	public boolean visit(ClassInstanceCreation node){
//		return false;
//	}
//	
//	public boolean visit(CompilationUnit node){
//		return false;
//	}
//	
//	public boolean visit(ConditionalExpression node){
//		return false;
//	}
//	
//	public boolean visit(ConstructorInvocation node){
//		ASTNode astNode = findRelatedNode(node);		
//		if(astNode == null){
//			if(stack.isEmpty()){		
//				astNode = ast.newConstructorInvocation();
//				stack.push(astNode);				
//				
//				visitList(node.arguments());
//				int size = node.arguments().size();
//				List<ASTNode> newNodes = new ArrayList<ASTNode>(size);				
//				for(int i = 0; i < size; i ++){
//					newNodes.add(0, stack.pop()); //to keep the original parameter order
//				}			
//				((ConstructorInvocation)astNode).arguments().addAll(newNodes);
//				
//				visitList(node.typeArguments());
//				size = node.typeArguments().size();
//				newNodes = new ArrayList<ASTNode>(size);
//				for(int i = 0; i < size; i ++){
//					newNodes.add(0, stack.pop());
//				}
//				((ConstructorInvocation)astNode).typeArguments().addAll(newNodes);								
//			}else{
//				astNode = ASTNode.copySubtree(ast, node);
//				stack.push(astNode);
//			}				
//		}else{
//			astNode = ASTNode.copySubtree(ast, astNode);
//			stack.push(astNode);	
//		}			
//		return false;
//	}
//	
//	public boolean visit(ContinueStatement node){
//		return false;
//	}
//	
//	public boolean visit(DoStatement node){
//		return false;
//	}
//	
//	public boolean visit(EnhancedForStatement node){
//		return false;
//	}
//	public boolean visit(EnumConstantDeclaration node){
//		return false;
//	}
//	
//	public boolean visit(ExpressionStatement node){
//		return false;
//	}
//	
//	public boolean visit(FieldAccess node){
//		return false;
//	}
//	
//	public boolean visit(FieldDeclaration node){
//		return false;
//	}
//	
//	public boolean visit(ForStatement node){
//		return false;
//	}
//	
//	public boolean visit(IfStatement node){
//		return false;
//	}
//	
//	public boolean visit(InfixExpression node){		
//		ASTNode astNode = findRelatedNode(node);		
//		if(astNode == null){
//			if(stack.isEmpty()){				
//				astNode = ast.newInfixExpression();	
//				stack.push(astNode);
//				node.getLeftOperand().accept(this);
//				((InfixExpression)astNode).setLeftOperand((Expression)stack.pop());
//				((InfixExpression)astNode).setOperator(node.getOperator());
//				node.getRightOperand().accept(this);
//				((InfixExpression)astNode).setRightOperand((Expression)stack.pop());	
//			}else{
//				astNode = ASTNode.copySubtree(ast, node);
//				stack.push(astNode);
//			}				
//		}else{
//			astNode = ASTNode.copySubtree(ast, astNode);
//			stack.push(astNode);	
//		}			
//		return false;
//	}
//	
//	public boolean visit(Initializer node){
//		return false;
//	}
//	
//	public boolean visit(InstanceofExpression node){
//		return false;
//	}
//	
//	public boolean visit(LabeledStatement node){
//		return false;
//	}
//	
//	public boolean visit(MemberRef node){
//		return false;
//	}
//	
//	public boolean visit(MemberValuePair node){
//		return false;
//	}
//	
//	public boolean visit(MethodDeclaration node){
//		return false;
//	}
//	
//	public boolean visit(MethodInvocation node){
//		ASTNode astNode = findRelatedNode(node);		
//		if(astNode == null){
//			if(stack.isEmpty()){		
//				astNode = ast.newMethodInvocation();
//				stack.push(astNode);
//				
//				node.getExpression().accept(this);
//				((MethodInvocation)astNode).setExpression((Expression)stack.pop());
//				
//				node.getName().accept(this);
//				((MethodInvocation)astNode).setName((SimpleName)stack.pop());
//				
//				visitList(node.arguments());
//				int size = node.arguments().size();
//				List<ASTNode> newNodes = new ArrayList<ASTNode>(size);				
//				for(int i = 0; i < size; i ++){
//					newNodes.add(0, stack.pop()); //to keep the original parameter order
//				}			
//				((MethodInvocation)astNode).arguments().addAll(newNodes);
//				
//				visitList(node.typeArguments());
//				size = node.typeArguments().size();
//				newNodes = new ArrayList<ASTNode>(size);
//				for(int i = 0; i < size; i ++){
//					newNodes.add(0, stack.pop());
//				}
//				((MethodInvocation)astNode).typeArguments().addAll(newNodes);								
//			}else{
//				astNode = ASTNode.copySubtree(ast, node);
//				stack.push(astNode);
//			}				
//		}else{
//			astNode = ASTNode.copySubtree(ast, astNode);
//			stack.push(astNode);	
//		}			
//		return false;
//	}
//	
//	public boolean visit(MethodRef node){
//		return false;
//	}
//	
//	public boolean visit(MethodRefParameter node){
//		return false;
//	}
//	
//	public boolean visit(NullLiteral node){
//		simpleNodeProcess(node);
//		return false;
//	}
//	
//	public boolean visit(NumberLiteral node){
//		simpleNodeProcess(node);
//		return false;
//	}
//	
//	public boolean visit(ParameterizedType node){
//		return false;
//	}
//	public boolean visit(ParenthesizedExpression node){
//		return false;
//	}
//	
//	public boolean visit(PostfixExpression node){
//		ASTNode astNode = findRelatedNode(node);		
//		if(astNode == null){
//			if(stack.isEmpty()){				
//				astNode = ast.newInfixExpression();	
//				stack.push(astNode);
//				
//				((PostfixExpression)astNode).setOperator(node.getOperator());
//				
//				node.getOperand().accept(this);
//				((PostfixExpression)astNode).setOperand((Expression)stack.pop());				
//			}else{
//				astNode = ASTNode.copySubtree(ast, node);
//				stack.push(astNode);
//			}				
//		}else{
//			astNode = ASTNode.copySubtree(ast, astNode);
//			stack.push(astNode);	
//		}					
//		return false;
//	}
//	
//	public boolean visit(PrefixExpression node){
//		ASTNode astNode = findRelatedNode(node);		
//		if(astNode == null){
//			if(stack.isEmpty()){				
//				astNode = ast.newInfixExpression();	
//				stack.push(astNode);
//				
//				((PrefixExpression)astNode).setOperator(node.getOperator());
//				
//				node.getOperand().accept(this);
//				((PrefixExpression)astNode).setOperand((Expression)stack.pop());				
//			}else{
//				astNode = ASTNode.copySubtree(ast, node);
//				stack.push(astNode);
//			}				
//		}else{
//			astNode = ASTNode.copySubtree(ast, astNode);
//			stack.push(astNode);	
//		}					
//		return false;
//	}
//	
//	public boolean visit(PrimitiveType node){
//		return false;
//	}
//	
//	public boolean visit(QualifiedName node){
//		ASTNode astNode = findRelatedNode(node);		
//		if(astNode == null){
//			if(stack.isEmpty()){		
//				ASTNode qualifier = findRelatedNode(node.getQualifier());
//				if(qualifier == null)
//					qualifier = node.getQualifier();
//				ASTNode name = findRelatedNode(node.getName());
//				if(name == null)
//					name = node.getName();
//				
//				astNode = ast.newQualifiedName((Name)qualifier, (SimpleName) name);							
//				stack.push(astNode);			
//			}else{
//				astNode = ASTNode.copySubtree(ast, node);
//				stack.push(astNode);
//			}				
//		}else{
//			astNode = ASTNode.copySubtree(ast, astNode);
//			stack.push(astNode);	
//		}					
//		return false;
//	}
//	
//	public boolean visit(QualifiedType node){
//		return false;
//	}
//	
//	public boolean visit(ReturnStatement node){
//		return false;
//	}
//	
//	public boolean visit(SimpleName node){
//		simpleNodeProcess(node);					
//		return false;
//	}
//	
//	public boolean visit(SimpleType node){
//		return false;
//	}
//	
//	public boolean visit(SingleVariableDeclaration node){
//		return false;
//	}
//	
//	public boolean visit(StringLiteral node){
//		simpleNodeProcess(node);					
//		return false;
//	}
//	
//	public boolean visit(SuperConstructorInvocation node){
//		ASTNode astNode = findRelatedNode(node);		
//		if(astNode == null){
//			if(stack.isEmpty()){		
//				astNode = ast.newSuperConstructorInvocation();
//				stack.push(astNode);
//				
//				node.getExpression().accept(this);
//				((SuperConstructorInvocation)astNode).setExpression((Expression)stack.pop());
//							
//				visitList(node.arguments());
//				int size = node.arguments().size();
//				List<ASTNode> newNodes = new ArrayList<ASTNode>(size);				
//				for(int i = 0; i < size; i ++){
//					newNodes.add(0, stack.pop()); //to keep the original parameter order
//				}			
//				((SuperConstructorInvocation)astNode).arguments().addAll(newNodes);
//				
//				visitList(node.typeArguments());
//				size = node.typeArguments().size();
//				newNodes = new ArrayList<ASTNode>(size);
//				for(int i = 0; i < size; i ++){
//					newNodes.add(0, stack.pop());
//				}
//				((SuperConstructorInvocation)astNode).typeArguments().addAll(newNodes);								
//			}else{
//				astNode = ASTNode.copySubtree(ast, node);
//				stack.push(astNode);
//			}				
//		}else{
//			astNode = ASTNode.copySubtree(ast, astNode);
//			stack.push(astNode);	
//		}			
//		return false;
//	}
//	
//	public boolean visit(SuperFieldAccess node){
//		return false;
//	}
//	
//	public boolean visit(SuperMethodInvocation node){
//		ASTNode astNode = findRelatedNode(node);		
//		if(astNode == null){
//			if(stack.isEmpty()){		
//				astNode = ast.newSuperMethodInvocation();
//				stack.push(astNode);
//				
//				node.getQualifier().accept(this);
//				((SuperMethodInvocation)astNode).setQualifier((Name) stack.pop());
//				
//				node.getName().accept(this);
//				((SuperMethodInvocation)astNode).setName((SimpleName)stack.pop());
//				
//				visitList(node.arguments());
//				int size = node.arguments().size();
//				List<ASTNode> newNodes = new ArrayList<ASTNode>(size);				
//				for(int i = 0; i < size; i ++){
//					newNodes.add(0, stack.pop()); //to keep the original parameter order
//				}			
//				((SuperMethodInvocation)astNode).arguments().addAll(newNodes);
//				
//				visitList(node.typeArguments());
//				size = node.typeArguments().size();
//				newNodes = new ArrayList<ASTNode>(size);
//				for(int i = 0; i < size; i ++){
//					newNodes.add(0, stack.pop());
//				}
//				((SuperMethodInvocation)astNode).typeArguments().addAll(newNodes);								
//			}else{
//				astNode = ASTNode.copySubtree(ast, node);
//				stack.push(astNode);
//			}				
//		}else{
//			astNode = ASTNode.copySubtree(ast, astNode);
//			stack.push(astNode);	
//		}			
//		return false;
//	}
//	
//	public boolean visit(SwitchCase node){
//		return false;
//	}
//	
//	public boolean visit(SwitchStatement node){
//		return false;
//	}
//	
//	public boolean visit(SynchronizedStatement node){
//		return false;
//	}
//	
//	public boolean visit(ThisExpression node){
//		return false;
//	}
//	public boolean visit(ThrowStatement node){
//		return false;
//	}
//	
//	public boolean visit(TryStatement node){
//		return false;
//	}
//	
//	public boolean visit(TypeDeclaration node){
//		return false;
//	}
//	
//	public boolean visit(TypeDeclarationStatement node){
//		return false;
//	}
//	
//	public boolean visit(TypeLiteral node){
//		simpleNodeProcess(node);
//		return false;
//	}
//	
//	public boolean visit(TypeParameter node){
//		return false;
//	}
//	
//	public boolean visit(VariableDeclarationExpression node){
//		return false;
//	}
//	
//	public boolean visit(VariableDeclarationFragment node){
//		return false;
//	}
//	
//	public boolean visit(VariableDeclarationStatement node){
//		return false;
//	}
//	
//	public boolean visit(WhileStatement node){
//		return false;
//	}
//	
//	public boolean visit(WildcardType node){
//		return false;
//	}
//	
//	private void visitList(List<ASTNode> nodes){
//		for(int i = 0; i < nodes.size(); i ++){
//			nodes.get(i).accept(this);
//		}
//	}
//	
//	private ASTNode findRelatedNode(ASTNode node){
//		ASTNode temp = null;
//		ASTNode result = null;
//		for(int i = 0; i < method1Specific.size(); i ++){
//			temp = method1Specific.get(i);
//			if(temp.getNodeType() == node.getNodeType() && temp.toString().equals(node.toString())){
//				result = method2Specific.get(i);
//				break;
//			}
//		}
//		return result;
//	}
//	
//	private void simpleNodeProcess(ASTNode node){
//		ASTNode astNode = findRelatedNode(node);		
//		if(astNode == null){
//			astNode = ASTNode.copySubtree(ast, node);
//			stack.push(astNode);					
//		}else{
//			astNode = ASTNode.copySubtree(ast, astNode);
//			stack.push(astNode);	
//		}		
//	}
//}
