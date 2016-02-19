package changeassistant.multipleexample.partition;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import changeassistant.changesuggestion.expression.representation.ASTExpressionTransformer;
import changeassistant.changesuggestion.expression.representation.MethodNameTerm;
import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.ThisTerm;
import changeassistant.changesuggestion.expression.representation.TypeNameTerm;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.peers.comparison.Node;

public class SimpleASTCreator extends ASTVisitor {

	private Stack<SimpleASTNode> fNodeStack;

	private SimpleASTNode n;

	private ASTNode rootAST;

	Map<String, VariableTypeBindingTerm> variableTypeMap;
	// the key is the variable name + type name used

	Map<String, TypeNameTerm> typeMap;
	// the key is the type name used

	Map<String, MethodNameTerm> methodMap;
	// the key is the method name used

	int abstractVariableIndex = 0, abstractTypeIndex = 0,
			abstractMethodIndex = 0;

	public SimpleASTCreator() {
		init();
	}

	public void init() {
		this.variableTypeMap = new HashMap<String, VariableTypeBindingTerm>();
		this.typeMap = new HashMap<String, TypeNameTerm>();
		this.methodMap = new HashMap<String, MethodNameTerm>();
	}

	public List<List<SimpleASTNode>> createSimpleASTNodesList(Node methodNode) {
		List<List<SimpleASTNode>> simpleASTNodesList = new ArrayList<List<SimpleASTNode>>();
		List<SimpleASTNode> simpleASTNodes = null;
		Enumeration<Node> bEnum = methodNode.breadthFirstEnumeration();
		Node tmp = null;
		while (bEnum.hasMoreElements()) {
			tmp = bEnum.nextElement();
			List<ASTNode> exprs = tmp.getASTExpressions2();
			simpleASTNodes = new ArrayList<SimpleASTNode>();
			if (tmp.getNodeType() == ASTNode.METHOD_DECLARATION
					&& tmp.getParent() != null) {
				simpleASTNodes.add(createSimpleASTNode(tmp
						.getMethodDeclaration()));
			} else {
				for (ASTNode expr : exprs) {
					simpleASTNodes.add(createSimpleASTNode(expr));
				}
			}
			simpleASTNodesList.add(simpleASTNodes);
		}
		return simpleASTNodesList;
	}

	public SimpleASTNode createSimpleASTNode(ASTNode exprNode) {
		System.out.print("");
		rootAST = exprNode;
		fNodeStack = new Stack<SimpleASTNode>();
		n = new SimpleASTNode(exprNode.getNodeType(), exprNode.toString(),
				exprNode.getStartPosition(), exprNode.getLength());
		fNodeStack.push(n);
		exprNode.accept(this);
		fNodeStack = null;

		Enumeration<SimpleASTNode> sEnum = null;
		SimpleASTNode sTmp = null;
		sEnum = n.breadthFirstEnumeration();
		while (sEnum.hasMoreElements()) {
			sTmp = sEnum.nextElement();
			if (!sTmp.isLeaf())
				sTmp.setRecalc();
		}
		n.constructStrValue();
		// recalculate the string value of this
		// node to remove some unnecessary space
		return n;
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		push(node.getNodeType(), "anonyClass", node.getStartPosition(),
				node.getLength());
		return true;
	}

	@Override
	public void endVisit(AnonymousClassDeclaration node) {
		pop();

	}

	@Override
	public boolean visit(ArrayAccess node) {
		// format: Expression[Expression]
		push(node.getNodeType(), node.toString(), node.getStartPosition(),
				node.getLength());

		node.getArray().accept(this);

		pushValuedNode(ASTExpressionTransformer.LEFTBRACKET, "[");
		pop();

		node.getIndex().accept(this);

		pushValuedNode(ASTExpressionTransformer.RIGHTBRACKET, "]");
		pop();
		return false;
	}

	@Override
	public void endVisit(ArrayAccess node) {
		pop();
	}

	@Override
	public boolean visit(ArrayCreation node) {
		// new PrimitiveType [ Expression ] { [ Expression ] } { [ ] }
		// new TypeName [ Expression ] { [ Expression ] } { [ ] }
		// new PrimitiveType [ ] { [ ] } ArrayInitializer
		// new TypeName [ ] { [ ] } ArrayInitializer
		push(node.getNodeType(), node.toString(), node.getStartPosition(),
				node.getLength());

		pushValuedNode(ASTExpressionTransformer.NEW, "new ");
		pop();

		Type elementType = ((ArrayType) node.getType()).getElementType();
		elementType.accept(this);

		int numOfDimensions = ((ArrayType) node.getType()).getDimensions();
		List<Expression> dimensions = node.dimensions();
		for (int i = 0; i < numOfDimensions; i++) {
			pushValuedNode(ASTExpressionTransformer.LEFTBRACKET, "[");
			pop();

			if (i < dimensions.size()) {
				dimensions.get(i).accept(this);
			}

			pushValuedNode(ASTExpressionTransformer.RIGHTBRACKET, "]");
			pop();
		}

		if (node.getInitializer() != null) {
			node.getInitializer().accept(this);
		}
		return false;
	}

	@Override
	public void endVisit(ArrayCreation node) {
		pop();
	}

	@Override
	public boolean visit(ArrayInitializer node) {
		// format like: {[ Expression { , Expression} [ , ]]}
		push(node.getNodeType(), node.toString(), node.getStartPosition(),
				node.getLength());

		pushValuedNode(ASTExpressionTransformer.LEFTBRACE, "{");
		pop();

		List<ASTNode> list = node.expressions();
		visitList(list);

		pushValuedNode(ASTExpressionTransformer.RIGHTBRACE, "}");
		pop();
		return false;
	}

	@Override
	public void endVisit(ArrayInitializer node) {
		pop();
	}

	@Override
	public boolean visit(ArrayType node) {
		// format like: Type[]
		push(node.getNodeType(), node.toString(), node.getStartPosition(),
				node.getLength());

		node.getComponentType().accept(this);

		pushValuedNode(ASTExpressionTransformer.LEFTBRACKET, "[");
		pop();

		pushValuedNode(ASTExpressionTransformer.RIGHTBRACKET, "]");
		pop();
		return false;
	}

	@Override
	public void endVisit(ArrayType node) {
		pop();
	}

	@Override
	public boolean visit(AssertStatement node) {
		// format like: assert Expression [ : Expression ] ;
		if (!rootAST.equals(node)) {
			push(node.getNodeType(), node.toString(), node.getStartPosition(),
					node.getLength());
		}
		pushValuedNode(ASTExpressionTransformer.ASSERT, "assert ");
		pop();

		node.getExpression().accept(this);

		Expression message = node.getMessage();
		if (message != null) {
			pushValuedNode(ASTExpressionTransformer.COLON, ":");
			pop();

			message.accept(this);
		}

		pushValuedNode(ASTExpressionTransformer.SEMICOLON, ";");
		pop();
		return false;
	}

	@Override
	public void endVisit(AssertStatement node) {
		pop();
	}

	@Override
	public boolean visit(Assignment node) {
		if (!rootAST.equals(node)) {
			push(node.getNodeType(), node.toString(), node.getStartPosition(),
					node.getLength());
		}
		node.getLeftHandSide().accept(this);

		pushValuedNode(ASTExpressionTransformer.OPERATOR, node.getOperator()
				.toString());
		pop();

		node.getRightHandSide().accept(this);
		return false;
	}

	@Override
	public void endVisit(Assignment node) {
		pop();
	}

	@Override
	public boolean visit(Block node) {
		// format like: { { Statement } }
		if (!rootAST.equals(node)) {
			push(node.getNodeType(), node.toString(), node.getStartPosition(),
					node.getLength());
		}
		pushValuedNode(ASTExpressionTransformer.LEFTBRACE, "{");
		pop();

		List<Statement> statements = node.statements();
		for (Statement stmt : statements) {
			stmt.accept(this);
		}

		pushValuedNode(ASTExpressionTransformer.RIGHTBRACE, "}");
		pop();
		return false;
	}

	@Override
	public void endVisit(Block node) {
		pop();
	}

	@Override
	public boolean visit(BooleanLiteral node) {
		pushValueTypeNode(String.valueOf(node.booleanValue()), node
				.resolveTypeBinding().getName(), node.resolveTypeBinding()
				.getQualifiedName(), node.getNodeType());
		return false;
	}

	@Override
	public void endVisit(BooleanLiteral node) {
		pop();
	}

	@Override
	public boolean visit(BreakStatement node) {
		// format like:break [ Identifier ] ;
		if (!rootAST.equals(node))
			push(node.getNodeType(), node.toString(), node.getStartPosition(),
					node.getLength());
		pushValuedNode(ASTExpressionTransformer.BREAK, "break");
		pop();

		SimpleName label = node.getLabel();
		if (label != null) {
			pushValuedNode(ASTExpressionTransformer.LABEL,
					label.getIdentifier());
			pop();
		}

		pushValuedNode(ASTExpressionTransformer.SEMICOLON, ";");
		pop();

		return false;
	}

	@Override
	public void endVisit(BreakStatement node) {
		pop();
	}

	@Override
	public boolean visit(CastExpression node) {
		// format like: (Type) Expression
		if (!rootAST.equals(node))
			push(node.getNodeType(), node.toString(), node.getStartPosition(),
					node.getLength());

		pushValuedNode(ASTExpressionTransformer.LEFTPARENTHESIS, "(");
		pop();

		node.getType().accept(this);

		pushValuedNode(ASTExpressionTransformer.RIGHTPARENTHESIS, ")");
		pop();

		node.getExpression().accept(this);
		return false;
	}

	@Override
	public void endVisit(CastExpression node) {
		pop();
	}

	@Override
	public boolean visit(CharacterLiteral node) {
		// format like: catch ( FormalParameter ) Block
		pushValueTypeNode(node.getEscapedValue(), node.resolveTypeBinding()
				.getName(), node.resolveTypeBinding().getQualifiedName(),
				node.getNodeType());
		return false;
	}

	@Override
	public void endVisit(CharacterLiteral node) {
		pop();
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		// [ Expression . ]
       // new [ < Type { , Type } > ]
       //       Type ( [ Expression { , Expression } ] )
       //       [ AnonymousClassDeclaration ]
		// [ AnonymousClassDeclaration ]--this part is only considered as string
		try {
			if (!rootAST.equals(node))
				push(node.getNodeType(), node.toString(),
						node.getStartPosition(), node.getLength());
			if (node.getExpression() != null) {
				node.getExpression().accept(this);

				pushValuedNode(ASTExpressionTransformer.DOT, ".");
				pop();
			}

			pushValuedNode(ASTExpressionTransformer.NEW, "new ");
			pop();

			node.getType().accept(this);

			pushValuedNode(ASTExpressionTransformer.LEFTPARENTHESIS, "(");
			pop();

			List<ASTNode> list = node.arguments();
			visitList(list);

			pushValuedNode(ASTExpressionTransformer.RIGHTPARENTHESIS, ")");
			pop();

			AnonymousClassDeclaration decl = node
					.getAnonymousClassDeclaration();
			if (decl != null) {
				decl.accept(this);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void endVisit(ClassInstanceCreation node) {
		pop();
	}

	@Override
	public boolean visit(ConditionalExpression node) {
		// format like "Expression ? Expression : Expression"
		if (!rootAST.equals(node))
			push(node.getNodeType(), node.toString(), node.getStartPosition(),
					node.getLength());

		node.getExpression().accept(this);

		pushValuedNode(ASTExpressionTransformer.INTERROGATION, "? ");
		pop();

		node.getThenExpression().accept(this);

		pushValuedNode(ASTExpressionTransformer.COLON, ":");
		pop();

		node.getElseExpression().accept(this);
		return false;
	}

	@Override
	public void endVisit(ConditionalExpression node) {
		pop();
	}

	@Override
	public boolean visit(ConstructorInvocation node) {
		// format like: [ < Type { , Type } > ]
		// this ( [ Expression { , Expression } ] ) ;
		if (!rootAST.equals(node))
			push(node.getNodeType(), node.toString(), node.getStartPosition(),
					node.getLength());

		visitList(node.typeArguments());

		pushValuedNode(ASTExpressionTransformer.SPACE, " ");
		pop();

		pushMethodNode(ASTExpressionTransformer.THIS, "this");
		pop();

		pushValuedNode(ASTExpressionTransformer.LEFTPARENTHESIS, "(");
		pop();

		visitList(node.arguments());

		pushValuedNode(ASTExpressionTransformer.RIGHTPARENTHESIS, ")");
		pop();

		pushValuedNode(ASTExpressionTransformer.SEMICOLON, ";");
		pop();
		return false;
	}

	@Override
	public void endVisit(ConstructorInvocation node) {
		pop();
	}

	@Override
	public boolean visit(ContinueStatement node) {
		// format like: continue [ Identifier ] ;
		if (!rootAST.equals(node))
			push(node.getNodeType(), node.toString(), node.getStartPosition(),
					node.getLength());

		pushValuedNode(ASTExpressionTransformer.CONTINUE, "continue ");
		pop();

		SimpleName label = node.getLabel();
		if (label != null) {
			pushValuedNode(ASTExpressionTransformer.LABEL,
					label.getIdentifier());
			pop();
		}

		pushValuedNode(ASTExpressionTransformer.SEMICOLON, ";");
		pop();
		return false;
	}

	@Override
	public void endVisit(ContinueStatement node) {
		pop();
	}

	@Override
	public boolean visit(FieldAccess node) {
		// format like: Expression . Identifier
		push(node.getNodeType(), node.toString(), node.getStartPosition(),
				node.getLength());

		if (node.getExpression() != null) {
			node.getExpression().accept(this);

			pushValuedNode(ASTExpressionTransformer.DOT, ".");
			pop();
		}

		node.getName().accept(this);
		return false;
	}

	@Override
	public void endVisit(FieldAccess node) {
		pop();
	}
	
//	@Override
//	public boolean visit(IfStatement node){
//		push(node.getNodeType(), node.getExpression().toString(), node.getStartPosition(), node.getLength());
//		node.getExpression().accept(this);
//		
//		if(node.getThenStatement() != null){
//			Statement thenStmt = node.getThenStatement();
//			push(thenStmt.getNodeType(), "then", thenStmt.getStartPosition(), thenStmt.getLength());
//			thenStmt.accept(this);
//			pop();
//		}
//		if(node.getElseStatement() != null){
//			Statement elseStmt = node.getElseStatement();
//			push(elseStmt.getNodeType(), "else", elseStmt.getStartPosition(), elseStmt.getLength());
//			elseStmt.accept(this);
//			pop();
//		}
//		return false;
//	}
//	
//	@Override
//	public void endVisit(IfStatement node){
//		pop();
//	}

	@Override
	public boolean visit(InfixExpression node) {
		// format like
		// "Expression InfixOperator Expression {InfixOperation Expression}"
		push(node.getNodeType(), node.toString(), node.getStartPosition(),
				node.getLength());

		node.getLeftOperand().accept(this);

		pushValuedNode(ASTExpressionTransformer.OPERATOR, node.getOperator()
				.toString());
		pop();

		node.getRightOperand().accept(this);

		if (node.hasExtendedOperands()) {
			List<ASTNode> list = node.extendedOperands();
			for (ASTNode nodeInList : list) {
				pushValuedNode(ASTExpressionTransformer.OPERATOR, node
						.getOperator().toString());
				pop();
				nodeInList.accept(this);
			}
		}
		return false;
	}

	@Override
	public void endVisit(InfixExpression node) {
		pop();
	}

	@Override
	public boolean visit(InstanceofExpression node) {
		// format like: Expression instanceof Type
		push(node.getNodeType(), node.toString(), node.getStartPosition(),
				node.getLength());

		node.getLeftOperand().accept(this);

		pushValuedNode(ASTExpressionTransformer.INSTANCEOF, " instanceof ");
		pop();

		node.getRightOperand().accept(this);
		return false;
	}

	@Override
	public void endVisit(InstanceofExpression node) {
		pop();
	}

	@Override
	public boolean visit(LabeledStatement node) {
		// format like: Identifier : Statement
		if (!rootAST.equals(node)) {
			push(node.getNodeType(), node.toString(), node.getStartPosition(),
					node.getLength());
		}

		pushValuedNode(ASTExpressionTransformer.LABEL, node.getLabel()
				.getIdentifier());
		pop();

		pushValuedNode(ASTExpressionTransformer.COLON, ":");
		pop();

		node.getBody().accept(this);
		return false;
	}

	@Override
	public void endVisit(LabeledStatement node) {
		pop();
	}

	/**
	 * MethodDeclaration: [ Javadoc ] { ExtendedModifier } [ < TypeParameter { ,
	 * TypeParameter } > ] ( Type | void ) Identifier ( [ FormalParameter { ,
	 * FormalParameter } ] ) {[ ] } [ throws TypeName { , TypeName } ] ( Block |
	 * ; )
	 */
	@Override
	public boolean visit(MethodDeclaration node) {
		// if (!rootAST.equals(node)) {
		// push(node.getNodeType(), SubTreeModel.METHOD_DECLARATION,
		// node.getStartPosition(), node.getLength());
		// }
		visitModifiers(node.modifiers());

		visitList(node.typeParameters());

		if (node.getReturnType2() != null)
			node.getReturnType2().accept(this);

		pushValuedNode(ASTExpressionTransformer.SPACE, " ");
		pop();

		// method name
		pushMethodNode(node.getName().getNodeType(), node.getName().toString());// Name
		pop();

		// (
		pushValuedNode(ASTExpressionTransformer.LEFTPARENTHESIS, "(");
		pop();
		// System.out.print("");
		visitList(node.parameters());

		// )
		pushValuedNode(ASTExpressionTransformer.RIGHTPARENTHESIS, ")");
		pop();

		// [][]
		int extraDimensions = node.getExtraDimensions();
		for (int i = 0; i < extraDimensions; i++) {
			pushValuedNode(ASTExpressionTransformer.LEFTBRACKET, "[");
			pop();

			pushValuedNode(ASTExpressionTransformer.RIGHTBRACKET, "]");
			pop();
		}

		pushValuedNode(ASTExpressionTransformer.SPACE, " ");
		pop();

		// exceptions
		List<ASTNode> exceptions = node.thrownExceptions();
		if (exceptions != null && exceptions.size() != 0) {
			pushValuedNode(ASTExpressionTransformer.THROWS, "throws");
			pop();

			pushValuedNode(ASTExpressionTransformer.SPACE, " ");
			pop();

			visitList(exceptions);
		}

		node.getBody().accept(this);
		return false;
	}

	@Override
	public void endVisit(MethodDeclaration node) {

	}

	@Override
	public boolean visit(MethodInvocation node) {
		// format like "MethodInvocation:
		// [ Expression . ]
		// [ < Type { , Type } > ]
		// Identifier ( [ Expression { , Expression } ] )
		if (!rootAST.equals(node))
			push(node.getNodeType(), node.toString(), node.getStartPosition(),
					node.getLength());

		if (node.getExpression() != null) {
			node.getExpression().accept(this);

			pushValuedNode(ASTExpressionTransformer.DOT, ".");
			pop();
		}

		if (node.typeArguments().size() > 0) {
			pushValuedNode(ASTExpressionTransformer.LT, "<");
			pop();

			List<ASTNode> list = node.typeArguments();
			visitList(list);

			pushValuedNode(ASTExpressionTransformer.GT, ">");
			pop();
		}

		SimpleName name = node.getName();
		pushMethodNode(name.getNodeType(), name.toString());
		pop();

		pushValuedNode(ASTExpressionTransformer.LEFTPARENTHESIS, "(");
		pop();

		visitList(node.arguments());

		pushValuedNode(ASTExpressionTransformer.RIGHTPARENTHESIS, ")");
		pop();
		return false;
	}

	@Override
	public void endVisit(MethodInvocation node) {
		pop();
	}

	@Override
	public boolean visit(Modifier node) {
		push(node.getNodeType(), node.toString(), node.getStartPosition(),
				node.getLength());
		return false;
	}

	@Override
	public void endVisit(Modifier node) {
		pop();
		pushValuedNode(ASTExpressionTransformer.SPACE, " ");
		pop();
	}

	@Override
	public boolean visit(NullLiteral node) {
		push(node.getNodeType(), node.toString(), node.getStartPosition(),
				node.getLength());
		return false;
	}

	@Override
	public void endVisit(NullLiteral node) {
		pop();
	}

	@Override
	public boolean visit(NumberLiteral node) {
		pushValueTypeNode(node.getToken(), node.resolveTypeBinding().getName(),
				node.resolveTypeBinding().getQualifiedName(),
				node.getNodeType());
		return false;
	}

	@Override
	public void endVisit(NumberLiteral node) {
		pop();
	}
	
	//Type<Type{, Type}>
	@Override
	public boolean visit(ParameterizedType node){
		pushTypeNode(node.toString(), node.resolveBinding().getQualifiedName(), node.getNodeType());
		
		node.getType().accept(this);
		
		pushValuedNode(ASTExpressionTransformer.LT, "<");
		pop();
		
		visitList(node.typeArguments());
		
		pushValuedNode(ASTExpressionTransformer.GT, ">");
		pop();
		return false;
	}
	
	@Override
	public void endVisit(ParameterizedType node){
		pop();
	}

	@Override
	public boolean visit(ParenthesizedExpression node) {
		// format like: (Expression)
		push(node.getNodeType(), node.toString(), node.getStartPosition(),
				node.getLength());

		pushValuedNode(ASTExpressionTransformer.LEFTPARENTHESIS, "(");
		pop();

		node.getExpression().accept(this);

		pushValuedNode(ASTExpressionTransformer.RIGHTPARENTHESIS, ")");
		pop();
		return false;
	}

	@Override
	public void endVisit(ParenthesizedExpression node) {
		pop();
	}

	@Override
	public boolean visit(PostfixExpression node) {
		// format like: Expression PostfixOperator
		if (!rootAST.equals(node))
			push(node.getNodeType(), node.toString(), node.getStartPosition(),
					node.getLength());

		node.getOperand().accept(this);

		pushValuedNode(ASTExpressionTransformer.OPERATOR, node.getOperator()
				.toString());
		pop();
		return false;
	}

	@Override
	public void endVisit(PostfixExpression node) {
		pop();
	}

	@Override
	public boolean visit(PrefixExpression node) {
		// format like : PrefixOperator Expression
		if (!rootAST.equals(node))
			push(node.getNodeType(), node.toString(), node.getStartPosition(),
					node.getLength());

		pushValuedNode(ASTExpressionTransformer.OPERATOR, node.getOperator()
				.toString());
		pop();

		node.getOperand().accept(this);
		return false;
	}

	@Override
	public void endVisit(PrefixExpression node) {
		pop();
	}

	@Override
	public boolean visit(PrimitiveType node) {
		pushTypeNode(node.toString(), node.toString(), node.getNodeType());
		return false;
	}

	@Override
	public void endVisit(PrimitiveType node) {
		pop();
	}

	@Override
	public boolean visit(QualifiedName node) {
		push(node.getNodeType(), node.toString(), node.getStartPosition(), node.getLength());
		
		// format like: Name. SimpleName
		node.getQualifier().accept(this);

		pushValuedNode(ASTExpressionTransformer.DOT, ".");
		pop();

		node.getName().accept(this);
		return false;
	}

	@Override
	public void endVisit(QualifiedName node) {
		pop();
	}

	@Override
	public boolean visit(QualifiedType node) {
		// format like: QualifiedType:
		// Type . SimpleName
		pushTypeNode(node.toString(), node.resolveBinding().getQualifiedName(),
				node.getNodeType());

		node.getQualifier().accept(this);

		pushValuedNode(ASTExpressionTransformer.DOT, ".");
		pop();

		node.getName().accept(this);
		return false;
	}

	@Override
	public void endVisit(QualifiedType node) {
		pop();
	}

	@Override
	public boolean visit(ReturnStatement node) {
		// format like: return [ Expression ] ;
		if (!rootAST.equals(node))
			push(node.getNodeType(), node.toString(), node.getStartPosition(),
					node.getLength());

		pushValuedNode(ASTExpressionTransformer.RETURN, "return ");
		pop();

		Expression expr = node.getExpression();
		if (expr != null) {
			expr.accept(this);
		}

		pushValuedNode(ASTExpressionTransformer.SEMICOLON, ";");
		pop();
		return false;
	}

	@Override
	public void endVisit(ReturnStatement node) {
		pop();
	}

	@Override
	public boolean visit(SimpleName node) {
		String variableName = node.getIdentifier();
		String typeName = variableName;
		String qName = typeName;
		ITypeBinding binding = node.resolveTypeBinding();
		int nodeType = node.getNodeType();
		if (binding != null) {
			typeName = binding.getName();
			qName = binding.getQualifiedName();
		} else {
			System.out.println("The type name for " + node.toString()
					+ " is not resolved");
		}
		if (!rootAST.equals(node)) {
			push(node.getNodeType(), node.toString(), node.getStartPosition(),
					node.getLength());			
		}
		Term vTerm = getOrCreateVTerm(variableName, typeName, qName,
				nodeType);
		getCurrentParent().setTerm(vTerm);
		return false;
	}

	@Override
	public void endVisit(SimpleName node) {
		pop();
	}

	@Override
	public boolean visit(SimpleType node) {
		push(node.getNodeType(), node.toString(), node.getStartPosition(), node.getLength());
		TypeNameTerm tTerm = null;		
		try {
			tTerm = getOrCreateTypeNameTerm(node.toString(), node.resolveBinding().getQualifiedName(), node.getNodeType());
		} catch (Exception e) {
			tTerm = getOrCreateTypeNameTerm(node.toString(), node.toString(), node.getNodeType());
		}
		getCurrentParent().setTerm(tTerm);
		return false;
	}

	@Override
	public void endVisit(SimpleType node) {
		pop();
	}

	@Override
	public boolean visit(SingleVariableDeclaration node) {
		// format like: SingleVariableDeclaration:
		// { ExtendedModifier } Type [ ... ] Identifier { [] } [ = Expression ]
		if (!rootAST.equals(node))
			push(node.getNodeType(), node.toString(), node.getStartPosition(),
					node.getLength());

		System.out.print("");
		// Type
		node.getType().accept(this);

		// [...]
		if (node.isVarargs()) {
			pushValuedNode(ASTExpressionTransformer.ELLIPSIS, "...");
			pop();
		}

		// space
		pushValuedNode(ASTExpressionTransformer.SPACE, " ");
		pop();

		// Identifier
		node.getName().accept(this);

		// extra dimensions
		int dimensions = node.getExtraDimensions();
		for (int i = 0; i < dimensions; i++) {
			pushValuedNode(ASTExpressionTransformer.LEFTBRACKET, "[");
			pop();

			pushValuedNode(ASTExpressionTransformer.RIGHTBRACKET, "]");
			pop();
		}

		// initializer
		Expression init = node.getInitializer();
		if (init != null) {
			pushValuedNode(ASTExpressionTransformer.EQUAL, "=");
			pop();

			init.accept(this);
		}
		return false;
	}

	@Override
	public void endVisit(SingleVariableDeclaration node) {
		if (!rootAST.equals(node))
			pop();
	}

	@Override
	public boolean visit(StringLiteral node) {
		pushValueTypeNode(node.getEscapedValue(), node.resolveTypeBinding()
				.getName(), node.resolveTypeBinding().getQualifiedName(),
				node.getNodeType());
		return false;
	}

	@Override
	public void endVisit(StringLiteral node) {
		pop();
	}

	@Override
	public boolean visit(SuperConstructorInvocation node) {
		/*
		 * format like: [ Expression . ] [ < Type { , Type } > ] super ( [
		 * Expression { , Expression } ] ) ;
		 */
		if (!rootAST.equals(node)) {
			push(node.getNodeType(), node.toString(), node.getStartPosition(),
					node.getLength());
		}
		Expression expr = node.getExpression();
		if (expr != null) {
			expr.accept(this);

			pushValuedNode(ASTExpressionTransformer.DOT, ".");
			pop();
		}

		// <Type {, Type}>
		List<ASTNode> types = node.typeArguments();
		if (types != null && types.size() > 0) {
			pushValuedNode(ASTExpressionTransformer.LT, "<");
			pop();

			visitList(types);

			pushValuedNode(ASTExpressionTransformer.GT, ">");
			pop();
		}

		pushValuedNode(ASTExpressionTransformer.SUPER, "super");
		pop();

		pushValuedNode(ASTExpressionTransformer.LEFTPARENTHESIS, "(");
		pop();

		visitList(node.arguments());

		pushValuedNode(ASTExpressionTransformer.RIGHTPARENTHESIS, ")");
		pop();

		pushValuedNode(ASTExpressionTransformer.SEMICOLON, ";");
		pop();

		return false;
	}

	@Override
	public void endVisit(SuperConstructorInvocation node) {
		pop();
	}

	@Override
	public boolean visit(SuperFieldAccess node) {
		// format like:
		// SuperFieldAccess:
		// [ ClassName . ] super . Identifier
		push(node.getNodeType(), node.toString(), node.getStartPosition(),
				node.getLength());

		if (node.getQualifier() != null) {
			pushTypeNode(node.getQualifier().toString(), node.getQualifier()
					.resolveTypeBinding().getQualifiedName(),
					node.getNodeType());
			pop();

			pushValuedNode(ASTExpressionTransformer.DOT, ".");
			pop();
		}

		pushValuedNode(ASTExpressionTransformer.SUPER, "super");
		pop();

		pushValuedNode(ASTExpressionTransformer.DOT, ".");
		pop();

		if (node.getName().resolveTypeBinding() != null)
			pushValueTypeNode(node.getName().getIdentifier(), node.getName()
					.resolveTypeBinding().getName(), node.getName()
					.resolveTypeBinding().getQualifiedName(),
					node.getNodeType());
		else
			pushValueTypeNode(node.getName().getIdentifier(), node.getName()
					.getIdentifier(), node.getName().getIdentifier(),
					node.getNodeType());
		pop();
		return false;
	}

	@Override
	public void endVisit(SuperFieldAccess node) {
		pop();
	}

	@Override
	public boolean visit(SuperMethodInvocation node) {
		// format like: [ ClassName . ] super .
		// [ < Type { , Type } > ]
		// Identifier ( [ Expression { , Expression } ] )
		if (!rootAST.equals(node)) {
			push(node.getNodeType(), node.toString(), node.getStartPosition(),
					node.getLength());
		}
		if (node.getQualifier() != null) {
			node.getQualifier().accept(this);

			pushValuedNode(ASTExpressionTransformer.DOT, ".");
			pop();
		}

		pushValuedNode(ASTExpressionTransformer.SUPER, "super");
		pop();

		pushValuedNode(ASTExpressionTransformer.DOT, ".");
		pop();

		if (node.typeArguments().size() > 0) {
			pushValuedNode(ASTExpressionTransformer.LT, "<");
			pop();

			visitList(node.typeArguments());

			pushValuedNode(ASTExpressionTransformer.GT, ">");
			pop();
		}

		pushMethodNode(node.getNodeType(), node.getName().toString());
		pop();

		pushValuedNode(ASTExpressionTransformer.LEFTPARENTHESIS, "(");
		pop();

		visitList(node.arguments());

		pushValuedNode(ASTExpressionTransformer.RIGHTPARENTHESIS, ")");
		pop();

		return false;
	}

	@Override
	public void endVisit(SuperMethodInvocation node) {
		pop();
	}

	@Override
	public boolean visit(SwitchCase node) {
		/*
		 * case Expression : default :
		 */
		if (!rootAST.equals(node)) {
			push(node.getNodeType(), node.toString(), node.getStartPosition(),
					node.getLength());
		}
		boolean isDefault = node.isDefault();
		if (isDefault) {
			pushValuedNode(ASTExpressionTransformer.DEFAULT, "default");
			pop();
		} else {
			pushValuedNode(ASTExpressionTransformer.CASE, "case ");
			pop();

			node.getExpression().accept(this);
		}

		pushValuedNode(ASTExpressionTransformer.COLON, ":");
		pop();
		return false;
	}

	@Override
	public void endVisit(SwitchCase node) {
		pop();
	}

	@Override
	public boolean visit(ThisExpression node) {
		// format like: [ClassName.]this
		String variableName = node.toString();
		String typeName = variableName;
		String qName = typeName;
		ITypeBinding binding = node.resolveTypeBinding();
		int nodeType = node.getNodeType();
		if (binding != null) {
			typeName = binding.getName();
			qName = binding.getQualifiedName();
		} else {
			System.out.println("The type name for " + node.toString()
					+ " is not resolved");
		}
		if (!rootAST.equals(node)) {
			push(node.getNodeType(), node.toString(), node.getStartPosition(),
					node.getLength());			
		}
		Term vTerm = getOrCreateVTerm(variableName, typeName, qName,
				nodeType);
		getCurrentParent().setTerm(vTerm);
		return false;
	}

	@Override
	public void endVisit(ThisExpression node) {
		pop();
	}

	@Override
	public boolean visit(TypeLiteral node) {
		// format like: (Type|void).class
		push(node.getNodeType(), node.toString(), node.getStartPosition(),
				node.getLength());

		if (node.getType() != null) {
			node.getType().accept(this);			
//			try {
//				pushTypeNode(node.getType().toString(), node
//						.resolveTypeBinding().getQualifiedName(),
//						node.getNodeType());
//			} catch (Exception e) {
//				pushTypeNode(node.getType().toString(), node.getType()
//						.toString(), node.getNodeType());
//			}
//			pop();
			pushValuedNode(ASTExpressionTransformer.DOT, ".");
			pop();
		}

		pushValuedNode(ASTExpressionTransformer.CLASS, "class");
		pop();
		return false;
	}

	@Override
	public void endVisit(TypeLiteral node) {
		pop();
	}

	@Override
	public boolean visit(VariableDeclarationExpression node) {
		// format like: {ExtendedModifier}Type VariableDeclarationFragment{,
		// VariableDeclarationFragment}
		// since the structure is complicated, and it is uncommon; I don't parse
		// out information carefully from it
		if (!rootAST.equals(node)) {
			push(node.getNodeType(), node.toString(), node.getStartPosition(),
					node.getLength());
		}
		visitModifiers(node.modifiers());

		node.getType().accept(this);

		pushValuedNode(ASTExpressionTransformer.SPACE, " ");
		pop();

		visitList(node.fragments());

		return false;
	}

	@Override
	public void endVisit(VariableDeclarationExpression node) {
		pop();
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		if (!rootAST.equals(node))
			push(node.getNodeType(), node.toString(), node.getStartPosition(),
					node.getLength());

		node.getName().accept(this);

		if (node.getInitializer() != null) {
			pushValuedNode(ASTExpressionTransformer.EQUAL, "=");
			pop();
			node.getInitializer().accept(this);
		}
		return false;
	}

	@Override
	public void endVisit(VariableDeclarationFragment node) {
		pop();
	}

	/**
	 * { ExtendedModifier } Type VariableDeclarationFragment { ,
	 * VariableDeclarationFragment } ;
	 */
	@Override
	public boolean visit(VariableDeclarationStatement node) {
		if (!rootAST.equals(node)) {
			push(node.getNodeType(), node.toString(), node.getStartPosition(),
					node.getLength());
		}
		visitModifiers(node.modifiers());

		node.getType().accept(this);

		pushValuedNode(ASTExpressionTransformer.SPACE, " ");
		pop();

		visitList(node.fragments());
		return false;
	}

	@Override
	public void endVisit(VariableDeclarationStatement node) {
		pop();
	}

	private SimpleASTNode getCurrentParent() {
		return fNodeStack.peek();
	}

	private void pop() {
		fNodeStack.pop();
	}

	private void push(int nodeType, String strValue, int startPosition,
			int length) {
		SimpleASTNode sNode = new SimpleASTNode(nodeType, strValue,
				startPosition, length);
		getCurrentParent().add(sNode);
		fNodeStack.push(sNode);
	}

	private void push(Term term, int nodeType) {
		push(nodeType, term.getName(), 0, 0);
		fNodeStack.peek().setTerm(term);
	}

	private void pushValuedNode(int nodeType, String strValue) {
		push(nodeType, strValue, 0, 0);
	}

	private Term getOrCreateVTerm(String variableName, String typeName,
			String qName, int nodeType) {
		String keyValue = variableName + "+" + typeName;
		VariableTypeBindingTerm vTerm = variableTypeMap.get(keyValue);
		if (vTerm == null) {
			String abstractName = Term.createAbstractName(ASTExpressionTransformer.ABSTRACT_VARIABLE, this.abstractVariableIndex++);
			if(variableName.equals(this)){
				vTerm = new ThisTerm(nodeType, variableName, abstractName);
			}else{
				vTerm = new VariableTypeBindingTerm(nodeType, variableName, abstractName);
			}			
			TypeNameTerm tTerm = getTypeNameTerm(typeName, qName);
			vTerm.setTypeNameTerm(tTerm);
			variableTypeMap.put(keyValue, vTerm);
		}
		return vTerm;
	}

	private void pushValueTypeNode(String variableName, String typeName,
			String qName, int nodeType) {
		Term vTerm = getOrCreateVTerm(variableName, typeName, qName, nodeType);
		push(vTerm, nodeType);
	}

	private void pushTypeNode(String typeName, String qName, int nodeType) {
		TypeNameTerm tTerm = getOrCreateTypeNameTerm(typeName, qName, nodeType);
		push(tTerm, nodeType);
	}

	public void pushMethodNode(int nodeType, String methodName) {
		MethodNameTerm mTerm = methodMap.get(methodName);
		if (mTerm == null) {
			mTerm = new MethodNameTerm(nodeType, methodName,
					Term.createAbstractName(
							ASTExpressionTransformer.ABSTRACT_METHOD,
							this.abstractMethodIndex++));
			methodMap.put(methodName, mTerm);
		}
		push(mTerm, nodeType);
	}

	private TypeNameTerm getTypeNameTerm(String typeName, String qName) {
		return getOrCreateTypeNameTerm(typeName, qName, 0);
	}

	private TypeNameTerm getOrCreateTypeNameTerm(String typeName, String qName,
			int nodeType) {
		TypeNameTerm tTerm = typeMap.get(typeName);
		if (tTerm == null) {
			tTerm = new TypeNameTerm(nodeType, typeName, qName);
			tTerm.setAbstractTypeName(Term.createAbstractName(
					ASTExpressionTransformer.ABSTRACT_TYPE,
					this.abstractTypeIndex++));
			typeMap.put(typeName, tTerm);
		}
		return tTerm;
	}

	private void visitList(List<ASTNode> list) {
		int numOfComma = list.size() - 1;
		int counter = 0;
		
		pushValuedNode(ASTExpressionTransformer.LIST,
					SimpleASTNode.LIST_LITERAL);	
		for (ASTNode nodeInList : list) {
			nodeInList.accept(this);
			if (counter++ < numOfComma) {
				pushValuedNode(ASTExpressionTransformer.COMMA, ",");
				pop();
			}
		}
		pop();		
	}

	private void visitModifiers(List<IExtendedModifier> modifiers) {
		IExtendedModifier modifier = null;
		for (int i = 0; i < modifiers.size(); i++) {
			modifier = modifiers.get(i);// do not process MarkerAnnotation
										// (@Override)
			if (modifier instanceof Modifier) {
				pushValuedNode(ASTExpressionTransformer.MODIFIER,
						((Modifier) modifier).getKeyword().toString());
				pop();

				pushValuedNode(ASTExpressionTransformer.SPACE, " ");
				pop();
			}
		}
	}
}
