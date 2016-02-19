package changeassistant.changesuggestion.expression.representation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
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
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

public class ASTExpressionTransformer extends ASTVisitor {

	public static int OPERATOR = 106;
	public static int DOT = 107;
	public static int LEFTPARENTHESIS = 108;
	public static int RIGHTPARENTHESIS = 109;
	public static int COMMA = 110;
	public static int NEW = 111;
	public static int LT = 112;
	public static int GT = 113;
	public static int LEFTBRACKET = 114;
	public static int RIGHTBRACKET = 115;
	public static int THIS = 116;
	public static int LEFTBRACE = 117;
	public static int RIGHTBRACE = 118;
	public static int INTERROGATION = 119;// interrogation = ?
	public static int COLON = 120;
	public static int INSTANCEOF = 121;
	public static int SUPER = 122;
	public static int SEMICOLON = 123;
	public static int EQUAL = 124;
	public static int SPACE = 125;
	public static int MODIFIER = 126;
	public static int ELLIPSIS = 127;
	public static int THROWS = 128;
	public static int ASSERT = 129;
	public static int BREAK = 130;
	public static int CATCH = 131;
	public static int CONTINUE = 135;
	public static int DO = 136;
	public static int WHILE = 137;
	public static int FOR = 138;
	public static int IF = 139;
	public static int ELSE = 140;
	public static int RETURN = 141;
	public static int CASE = 142;
	public static int SWITCH = 143;
	public static int SYNCHRONIZED = 144;
	public static int THROW = 145;
	public static int TRY = 146;
	public static int FINALLY = 147;
	public static int LABEL = 148;
	public static int DEFAULT = 149;
	public static int ANONYMOUS_CLASS_DECLARATION = 150;
	public static int LIST = 151;
	public static int CLASS = 152;
	public static int UNKNOWN_STATEMENT = 153;
	public static int INFIX_OPERATORS = 154;
	public static int PREFIX_OPERATORS = 155;
	public static int POSTFIX_OPERATORS = 156;
	public static int LIST_LITERAL = 157;
	public static int THEN = 158;

	public static final String ABSTRACT_TYPE = "t$_";
	public static final String ABSTRACT_METHOD = "m$_";
	public static final String ABSTRACT_VARIABLE = "v$_";
	public static final String ABSTRACT_UNKNOWN = "u$_";
	public static final String ABSTRACT_STATEMENT = "s$_";
	public static final String ARGS_PRE = "args: ";

	List<Term> fValueList = null;
	Map<String, VariableTypeBindingTerm> variableTypeMap;
	// the key is the variable name + type name used

	Map<String, TypeNameTerm> typeMap;
	// the key is the type name used

	Map<String, MethodNameTerm> methodMap;
	// the key is the method name + type name used

	int abstractVariableIndex = 0, abstractTypeIndex = 0,
			abstractMethodIndex = 0;

	public ASTExpressionTransformer() {
		this.fValueList = new ArrayList<Term>();
	}

	public ASTExpressionTransformer(
			List<VariableTypeBindingTerm> variableTypeList,
			List<TypeNameTerm> typeBindingList,
			List<MethodNameTerm> methodTypeList) {
		this.fValueList = new ArrayList<Term>();
		this.variableTypeMap = new HashMap<String, VariableTypeBindingTerm>();
		this.typeMap = new HashMap<String, TypeNameTerm>();
		this.methodMap = new HashMap<String, MethodNameTerm>();
		abstractVariableIndex = abstractTypeIndex = abstractMethodIndex = 0;
	}

	public ASTExpressionTransformer(List<Term> terms) {
		fValueList = terms;
	}

	public void clear() {
		fValueList.clear();
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		return false;
	}

	@Override
	public boolean visit(ArrayAccess node) {
		// format: Expression[Expression]
		node.getArray().accept(this);// Expression outside

		pushValuedNode(LEFTBRACKET, "[");
		pop();

		node.getIndex().accept(this);// Expression inside

		pushValuedNode(RIGHTBRACKET, "]");
		pop();
		return false;
	}

	@Override
	public boolean visit(ArrayCreation node) {
		// for the reason that this is very complicated, I prefer to realize it
		// in a simpler way
		// new PrimitiveType [ Expression ] { [ Expression ] } { [ ] }
		// new TypeName [ Expression ] { [ Expression ] } { [ ] }
		// new PrimitiveType [ ] { [ ] } ArrayInitializer
		// new TypeName [ ] { [ ] } ArrayInitializer
		pushValuedNode(NEW, "new ");// new
		pop();

		int numOfDimensions = ((ArrayType) node.getType()).getDimensions();
		Type elementType = ((ArrayType) node.getType()).getElementType();
		elementType.accept(this);

		// pushValuedNode(elementType.getNodeType(),
		// elementType.toString());//element type
		// pop(elementType);

		List<Expression> dimensions = node.dimensions();// dimensions

		for (int i = 0; i < numOfDimensions; i++) {
			pushValuedNode(LEFTBRACKET, "[");// [
			pop();

			if (i < dimensions.size()) {
				dimensions.get(i).accept(this);
			}

			pushValuedNode(RIGHTBRACKET, "]");// ]
			pop();
		}

		if (node.getInitializer() != null) {
			node.getInitializer().accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(ArrayInitializer node) {
		// format like: {[ Expression { , Expression} [ , ]]}
		pushValuedNode(LEFTBRACE, "{");
		pop();

		List<ASTNode> list = node.expressions();
		visitList(list);

		pushValuedNode(RIGHTBRACE, "}");
		pop();
		return false;
	}

	@Override
	public boolean visit(ArrayType node) {
		// format like: Type [ ]
		node.getComponentType().accept(this);

		pushValuedNode(LEFTBRACKET, "[");
		pop();

		pushValuedNode(RIGHTBRACKET, "]");
		pop();
		return false;
	}

	// format like: assert Expression [ : Expression ] ;
	@Override
	public boolean visit(AssertStatement node) {
		pushValuedNode(ASSERT, "assert");
		pop();

		pushValuedNode(SPACE, " ");
		pop();

		node.getExpression().accept(this);

		Expression message = node.getMessage();
		if (message != null) {
			pushValuedNode(COLON, ":");
			pop();

			message.accept(this);
		}

		pushValuedNode(SEMICOLON, ";");
		pop();
		return false;
	}

	@Override
	public boolean visit(Assignment node) {
		node.getLeftHandSide().accept(this);
		pushValuedNode(OPERATOR, node.getOperator().toString());
		pop();
		node.getRightHandSide().accept(this);
		return false;
	}

	// format like: { { Statement } }
	@Override
	public boolean visit(Block node) {
		pushValuedNode(LEFTBRACE, "{");
		pop();

		List<Statement> statements = node.statements();
		for (Statement stat : statements) {
			stat.accept(this);
		}

		pushValuedNode(RIGHTBRACE, "}");
		pop();
		return false;
	}

	@Override
	public boolean visit(BooleanLiteral node) {
		// format like: true false
		pushValueTypeNode(String.valueOf(node.booleanValue()),
				node.resolveTypeBinding().getName(),// use simple name
				node.resolveTypeBinding().getQualifiedName(),
				node.getNodeType());
		pop(node);
		return false;
	}

	// format like:break [ Identifier ] ;
	@Override
	public boolean visit(BreakStatement node) {
		pushValuedNode(BREAK, "break");
		pop();

		SimpleName label = node.getLabel();
		if (label != null) {
			pushValuedNode(SPACE, " ");
			pop();

			pushValuedNode(LABEL, label.getIdentifier());
			pop();
		}

		pushValuedNode(SEMICOLON, ";");
		pop();
		return false;
	}

	@Override
	public boolean visit(CastExpression node) {
		// (Type) Expression
		try {
			pushValuedNode(LEFTPARENTHESIS, "(");
			pop();

			// pushValuedNode(node.getType().getNodeType(),
			// node.getType().toString());
			// pop();
			node.getType().accept(this);

			pushValuedNode(RIGHTPARENTHESIS, ")");
			pop();

			node.getExpression().accept(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	// format like: catch ( FormalParameter ) Block
	@Override
	public boolean visit(CatchClause node) {
		pushValuedNode(CATCH, "catch");
		pop();

		pushValuedNode(LEFTPARENTHESIS, "(");
		pop();

		node.getException().accept(this);

		pushValuedNode(RIGHTPARENTHESIS, ")");
		pop();

		node.getBody().accept(this);
		return false;
	}

	@Override
	public boolean visit(CharacterLiteral node) {
		// String typeName = node.resolveTypeBinding().getQualifiedName();
		// String constantValue = String.valueOf(node.charValue());
		// pushValuedNode(constantValue, typeName, node.getNodeType());
		pushValueTypeNode(node.getEscapedValue(),
				node.resolveTypeBinding().getName(),// use simple name
				node.resolveTypeBinding().getQualifiedName(),
				node.getNodeType());
		pop();
		return false;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		// format like "[ Expression . ] new Name
		// ( [ Expression { , Expression } ] )
		// [ AnonymousClassDeclaration ]--this part is only considered as string
		try {
			if (node.getExpression() != null) {
				node.getExpression().accept(this); // [Expression.]
				pushValuedNode(DOT, ".");
				pop();
			}

			pushValuedNode(NEW, "new ");// new
			pop();

			node.getType().accept(this);

			// pushValuedNode(node.getType().getNodeType(),
			// node.getType().toString());
			// pop();

			pushValuedNode(LEFTPARENTHESIS, "(");// (
			pop();

			List<ASTNode> list = node.arguments();
			visitList(list);

			pushValuedNode(RIGHTPARENTHESIS, ")");// )
			pop();

			// AnonymousClassDeclaration is like: { ClassBodyDeclaration }
			AnonymousClassDeclaration anonymous = node
					.getAnonymousClassDeclaration();
			if (anonymous != null) {
				// anonymous.accept(this);
				// pushValuedNode(ANONYMOUS_CLASS_DECLARATION,
				// "anonymous_class_declaration");
				// pop();
				// pushValuedNode(LEFTBRACE, "{");
				// pop();
				//
				// List<BodyDeclaration> bodyDeclarations =
				// node.getAnonymousClassDeclaration().bodyDeclarations();
				// for(BodyDeclaration body : bodyDeclarations){
				// body.accept(this);
				// }
				//
				// pushValuedNode(RIGHTBRACE, "}");
				// pop();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean visit(ConditionalExpression node) {
		// format like "Expression ? Expression : Expression"
		node.getExpression().accept(this);

		pushValuedNode(INTERROGATION, " ? ");
		pop();

		node.getThenExpression().accept(this);

		pushValuedNode(COLON, " : ");
		pop();

		node.getElseExpression().accept(this);
		return false;
	}

	// format like: [ < Type { , Type } > ]
	// this ( [ Expression { , Expression } ] ) ;
	@Override
	public boolean visit(ConstructorInvocation node) {
		visitTypeList(node.typeArguments());

		pushValuedNode(SPACE, " ");
		pop();

		pushMethodNode(node.getNodeType(), "this");
		pop();

		pushValuedNode(LEFTPARENTHESIS, "(");
		pop();

		visitList(node.arguments());

		pushValuedNode(RIGHTPARENTHESIS, ")");
		pop();

		pushValuedNode(SEMICOLON, ";");
		pop();
		return false;
	}

	// format like: continue [ Identifier ] ;
	@Override
	public boolean visit(ContinueStatement node) {
		pushValuedNode(CONTINUE, "continue");
		pop();

		SimpleName label = node.getLabel();
		if (label != null) {
			pushValuedNode(SPACE, " ");
			pop();
			pushValuedNode(LABEL, label.getIdentifier());
			pop();
		}

		pushValuedNode(SEMICOLON, ";");
		pop();
		return false;
	}

	// format like: do Statement while ( Expression ) ;
	@Override
	public boolean visit(DoStatement node) {
		pushValuedNode(DO, "do");
		pop();

		node.getBody().accept(this);

		pushValuedNode(WHILE, "while");
		pop();

		pushValuedNode(LEFTPARENTHESIS, "(");
		pop();

		node.getExpression().accept(this);

		pushValuedNode(RIGHTPARENTHESIS, ")");
		pop();

		pushValuedNode(SEMICOLON, ";");
		pop();
		return false;
	}

	// format like: for ( FormalParameter : Expression )Statement
	@Override
	public boolean visit(EnhancedForStatement node) {
		pushValuedNode(FOR, "for");
		pop();

		pushValuedNode(LEFTPARENTHESIS, "(");
		pop();
		// formal parameter
		node.getParameter().accept(this);

		pushValuedNode(COLON, ":");
		pop();

		// expression
		node.getExpression().accept(this);

		pushValuedNode(RIGHTPARENTHESIS, ")");
		pop();

		node.getBody().accept(this);
		return false;
	}

	// format like: StatementExpression ;
	@Override
	public boolean visit(ExpressionStatement node) {
		node.getExpression().accept(this);

		pushValuedNode(SEMICOLON, ";");
		pop();
		return false;
	}

	@Override
	public boolean visit(FieldAccess node) {
		// format like: Expression . Identifier
		if (node.getExpression() != null) {
			node.getExpression().accept(this);

			pushValuedNode(DOT, ".");
			pop();
		}

		node.getName().accept(this);
		// pushValueTypeNode(String.valueOf(node.toString()),
		// node.resolveTypeBinding().getName(),//use simple name
		// node.getNodeType());
		return false;
	}

	/*
	 * format like: ForStatement: for ( [ ForInit ]; [ Expression ] ; [
	 * ForUpdate ] ) Statement ForInit: Expression { , Expression } ForUpdate:
	 * Expression { , Expression }
	 */
	@Override
	public boolean visit(ForStatement node) {
		pushValuedNode(FOR, "for");
		pop();

		pushValuedNode(LEFTPARENTHESIS, "(");
		pop();
		// ForInit
		visitList(node.initializers());

		pushValuedNode(SEMICOLON, ";");
		pop();

		node.getExpression().accept(this);

		pushValuedNode(SEMICOLON, ";");
		pop();

		visitList(node.updaters());

		pushValuedNode(RIGHTPARENTHESIS, ")");
		pop();

		node.getBody().accept(this);
		return false;
	}

	// format like: if ( Expression ) Statement [ else Statement]
	@Override
	public boolean visit(IfStatement node) {
		pushValuedNode(IF, "if");
		pop();

		pushValuedNode(LEFTPARENTHESIS, "(");
		pop();

		node.getExpression().accept(this);

		pushValuedNode(RIGHTPARENTHESIS, ")");
		pop();

		pushValuedNode(THEN, "then");
		pop();

		node.getThenStatement().accept(this);

		Statement elseStat = node.getElseStatement();
		if (elseStat != null) {
			pushValuedNode(ELSE, "else");
			pop();

			// pushValuedNode(SPACE, " ");
			elseStat.accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(InfixExpression node) {
		// format like
		// "Expression InfixOperator Expression {InfixOperation Expression}"
		node.getLeftOperand().accept(this);// left hand operand

		pushValuedNode(OPERATOR, node.getOperator().toString());
		pop();

		node.getRightOperand().accept(this);

		if (node.hasExtendedOperands()) {
			List<ASTNode> list = node.extendedOperands();
			for (ASTNode nodeInList : list) {
				pushValuedNode(OPERATOR, node.getOperator().toString());
				pop();
				nodeInList.accept(this);
			}
		}

		return false;
	}

	@Override
	public boolean visit(InstanceofExpression node) {
		// format like: Expression instanceof Type
		node.getLeftOperand().accept(this);// Expression

		pushValuedNode(INSTANCEOF, " instanceof ");
		pop();

		node.getRightOperand().accept(this);
		// pushValuedNode(node.getRightOperand().getNodeType(),
		// node.getRightOperand().toString());
		// pop(node.getRightOperand());
		return false;
	}

	// format like: Identifier : Statement
	@Override
	public boolean visit(LabeledStatement node) {
		pushValuedNode(LABEL, node.getLabel().getIdentifier());
		pop();

		pushValuedNode(COLON, ":");
		pop();

		node.getBody().accept(this);
		return false;
	}

	// format like "MethodDeclaration:
	// [ Javadoc ] { Modifier } ( Type | void ) Identifier (
	// [ FormalParameter
	// { , FormalParameter } ] ) {[ ] }
	// [ throws TypeName { , TypeName } ] ( Block | ; )"
	@Override
	public boolean visit(MethodDeclaration node) {
		// return type
		node.getReturnType2().accept(this);

		pushValuedNode(SPACE, " ");
		pop();
		// method name
		pushMethodNode(node.getName().getNodeType(), node.getName().toString());// Name
		pop(node.getName());

		// (
		pushValuedNode(LEFTPARENTHESIS, "(");
		pop();

		// formal parameters
		visitList(node.parameters());

		// )
		pushValuedNode(RIGHTPARENTHESIS, ")");
		pop();
		// [][]
		int extraDimensions = node.getExtraDimensions();
		for (int i = 0; i < extraDimensions; i++) {
			pushValuedNode(LEFTBRACKET, "[");
			pop();

			pushValuedNode(RIGHTBRACKET, "]");
			pop();
		}

		pushValuedNode(SPACE, " ");
		pop();

		// exceptions
		List<ASTNode> exceptions = node.thrownExceptions();
		if (exceptions != null) {
			pushValuedNode(THROWS, "throws");
			pop();

			pushValuedNode(SPACE, " ");
			pop();

			visitTypeList(exceptions);
		}

		node.getBody().accept(this);
		return false;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		// format like "MethodInvocation:
		// [ Expression . ]
		// [ < Type { , Type } > ]
		// Identifier ( [ Expression { , Expression } ] )
		if (node.getExpression() != null) {
			node.getExpression().accept(this);
			// if(node.getExpression() instanceof ThisExpression){
			// pushValueTypeNode(node.getExpression().toString(),
			// node.getExpression().resolveTypeBinding().getName(),
			// node.getExpression().getNodeType());
			// pop();
			// }else{
			// node.getExpression().accept(this); //Expression
			// }
			pushValuedNode(DOT, "."); // .
			pop();
		}

		if (node.typeArguments().size() > 0) {
			pushValuedNode(LT, "<"); // <
			pop();

			List<ASTNode> list = node.typeArguments();
			visitTypeList(list);

			pushValuedNode(GT, ">"); // >
			pop();
		}

		// IMethodBinding methodBinding = node.resolveMethodBinding();
		// ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
		//
		// StringBuffer parameterType = new StringBuffer("(");
		// for(int i = 0; i < parameterTypes.length; i ++){
		// ITypeBinding itb = parameterTypes[i];
		// parameterType.append(itb.getName());
		// if(i < list.size() - 1){
		// parameterType.append(',');
		// }
		// }
		// parameterType.append(')');
		// String typeName = methodBinding.getDeclaringClass().getName();
		// pushMethodTypeNode(node.getName().toString() +
		// parameterType.toString()
		// + "+" + typeName,
		// node.getName().toString(),
		// typeName,
		// node.getNodeType());

		// pushValuedNode(node.getName().toString(), "method_name",
		// node.getNodeType());//method invocations are all represented with
		// "method_name"
		pushMethodNode(node.getName().getNodeType(), node.getName().toString());// Name
		pop(node.getName());
		pushValuedNode(LEFTPARENTHESIS, "(");// (
		pop();

		visitList(node.arguments());

		pushValuedNode(RIGHTPARENTHESIS, ")");// )
		pop();
		return false;
	}

	@Override
	public boolean visit(NullLiteral node) {
		pushValuedNode(node.getNodeType(), node.toString());
		pop();
		return false;
	}

	@Override
	public boolean visit(NumberLiteral node) {
		// String typeName = node.resolveTypeBinding().getQualifiedName();
		// String constantValue = node.getToken();
		// pushValuedNode(constantValue, typeName, node.getNodeType());
		pushValueTypeNode(node.getToken(),
				node.resolveTypeBinding().getName(),// use simple name
				node.resolveTypeBinding().getQualifiedName(),
				node.getNodeType());
		pop();
		return false;
	}

	@Override
	public boolean visit(ParenthesizedExpression node) {
		// format like: (Expression)
		pushValuedNode(LEFTPARENTHESIS, "(");
		pop();

		node.getExpression().accept(this);

		pushValuedNode(RIGHTPARENTHESIS, ")");
		pop();
		return false;
	}

	@Override
	public boolean visit(PostfixExpression node) {
		// format like: Expression PostfixOperator
		node.getOperand().accept(this);
		pushValuedNode(OPERATOR, node.getOperator().toString());
		pop();
		return false;
	}

	@Override
	public boolean visit(PrefixExpression node) {
		// format like : PrefixOperator Expression
		pushValuedNode(OPERATOR, node.getOperator().toString());
		pop();

		node.getOperand().accept(this);
		return false;
	}

	@Override
	public boolean visit(PrimitiveType node) {
		pushTypeNode(node.toString(), node.toString(), node.getNodeType());
		// pushValuedNode(node.getNodeType(), node.toString());
		pop();
		return false;
	}

	@Override
	public boolean visit(QualifiedName node) {
		// format like: Name. SimpleName
		node.getQualifier().accept(this);

		pushValuedNode(DOT, ".");
		pop();

		node.getName().accept(this);

		// if(node.resolveTypeBinding() == null){
		// System.out.println("The type name for " + node.toString() +
		// " is not resolved");
		// }else{
		// pushValueTypeNode(String.valueOf(node.toString()),
		// node.resolveTypeBinding().getName(),//use simple name
		// node.getNodeType());
		// pop();
		// }
		// String typeName = node.getFullyQualifiedName();
		// if(node.resolveTypeBinding() == null){
		// System.out.println("The type name " + typeName + " is not resolved");
		// }else{
		// typeName = node.resolveTypeBinding().getQualifiedName();
		// }
		// String variableName = node.getFullyQualifiedName();
		// pushValuedNode(variableName, typeName, node.getNodeType());

		return false;
	}

	@Override
	public boolean visit(QualifiedType node) {
		// format like: QualifiedType:
		// Type . SimpleName
		pushTypeNode(node.toString(), node.resolveBinding().getQualifiedName(),
				node.getNodeType());
		// pushValuedNode(node.getNodeType(), node.toString());
		pop();
		return false;
	}

	// format like: return [ Expression ] ;
	@Override
	public boolean visit(ReturnStatement node) {
		pushValuedNode(RETURN, "return");
		pop();

		Expression expr = node.getExpression();
		if (expr != null) {
			pushValuedNode(SPACE, " ");
			pop();

			expr.accept(this);
		}

		pushValuedNode(SEMICOLON, ";");
		pop();
		return false;
	}

	@Override
	public boolean visit(SimpleName node) {
		if (node.resolveTypeBinding() == null) {
			pushValueTypeNode(node.getIdentifier(), node.getIdentifier(),
					node.getIdentifier(), node.getNodeType());
		} else {
			pushValueTypeNode(String.valueOf(node.toString()),
					node.resolveTypeBinding().getName(),// use simple name
					node.resolveTypeBinding().getQualifiedName(),
					node.getNodeType());
		}

		// String typeName = node.getFullyQualifiedName();
		// if(node.resolveTypeBinding() == null){
		// System.out.println("The type name " + typeName + " is not resolved");
		// }else{
		// typeName = node.resolveTypeBinding().getQualifiedName();
		// }
		// String variableName = node.getIdentifier();
		// pushValuedNode(variableName, typeName, node.getNodeType());
		pop(node);
		return false;
	}

	@Override
	public boolean visit(SimpleType node) {
		// pushValuedNode(node.getName().toString(), "type_name",
		// node.getNodeType());
		// pushValuedNode(node.getNodeType(), node.toString());
		try {
			pushTypeNode(node.toString(), node.resolveBinding()
					.getQualifiedName(), node.getNodeType());
		} catch (Exception e) {
			pushTypeNode(node.toString(), node.toString(), node.getNodeType());
		}
		pop();
		return false;
	}

	// format like: SingleVariableDeclaration:
	// { ExtendedModifier } Type [ ... ] Identifier { [] } [ = Expression ]
	@Override
	public boolean visit(SingleVariableDeclaration node) {
		// Type
		node.getType().accept(this);

		// [...]
		if (node.isVarargs()) {
			pushValuedNode(ELLIPSIS, "...");
			pop();
		}
		// space
		pushValuedNode(SPACE, " ");
		pop();

		// Identifier
		node.getName().accept(this);

		// extra dimensions
		int dimensions = node.getExtraDimensions();
		for (int i = 0; i < dimensions; i++) {
			// [
			pushValuedNode(LEFTBRACKET, "[");
			pop();
			// ]
			pushValuedNode(RIGHTBRACKET, "]");
			pop();
		}
		// initializer
		Expression init = node.getInitializer();
		if (init != null) {
			pushValuedNode(EQUAL, "=");
			pop();

			init.accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(StringLiteral node) {
		// String typeName = node.resolveTypeBinding().getQualifiedName();
		String constantValue = node.getEscapedValue();
		// pushValuedNode(constantValue, typeName, node.getNodeType());
		pushValueTypeNode(constantValue,
				node.resolveTypeBinding().getName(),// use simple name
				node.resolveTypeBinding().getQualifiedName(),
				node.getNodeType());
		pop(node);
		return false;
	}

	/*
	 * format like: [ Expression . ] [ < Type { , Type } > ] super ( [
	 * Expression { , Expression } ] ) ;
	 */
	@Override
	public boolean visit(SuperConstructorInvocation node) {
		Expression expr = node.getExpression();
		if (expr != null) {
			expr.accept(this);

			pushValuedNode(DOT, ".");
			pop();
		}
		// <Type {, Type}>
		List<ASTNode> types = node.typeArguments();
		if (types != null && types.size() > 0) {
			pushValuedNode(LT, "<");
			pop();

			visitTypeList(types);

			pushValuedNode(GT, ">");
			pop();
		}

		pushValuedNode(SUPER, "super");
		pop();

		pushValuedNode(LEFTPARENTHESIS, "(");
		pop();

		visitList(node.arguments());

		pushValuedNode(RIGHTPARENTHESIS, ")");
		pop();

		pushValuedNode(SEMICOLON, ";");
		pop();
		return false;
	}

	@Override
	public boolean visit(SuperFieldAccess node) {
		// format like:
		// SuperFieldAccess:
		// [ ClassName . ] super . Identifier
		if (node.getQualifier() != null) {
			pushTypeNode(node.getQualifier().toString(), node.getQualifier()
					.resolveTypeBinding().getQualifiedName(),
					node.getNodeType());
			pop(node.getQualifier());

			pushValuedNode(DOT, ".");
			pop();
		}

		pushValuedNode(SUPER, "super");
		pop();

		pushValuedNode(DOT, ".");
		pop();

		if (node.getName().resolveTypeBinding() != null) {
			pushValueTypeNode(node.getName().getIdentifier(), node.getName()
					.resolveTypeBinding().getName(), node.getName()
					.resolveTypeBinding().getQualifiedName(),
					node.getNodeType());
		} else {
			pushValueTypeNode(node.getName().getIdentifier(), node.getName()
					.getIdentifier(), node.getName().getIdentifier(),
					node.getNodeType());
		}
		pop(node.getName());

		return false;
	}

	@Override
	public boolean visit(SuperMethodInvocation node) {
		// format like: [ ClassName . ] super .
		// [ < Type { , Type } > ]
		// Identifier ( [ Expression { , Expression } ] )
		if (node.getQualifier() != null) {// ClassName
			pushValuedNode(node.getQualifier().getNodeType(), node
					.getQualifier().toString());
			pop();

			pushValuedNode(DOT, ".");// .
			pop();
		}

		pushValuedNode(SUPER, "super");// super
		pop();
		pushValuedNode(DOT, ".");// .
		pop();

		if (node.typeArguments().size() > 0) {
			pushValuedNode(LT, "<"); // <
			pop();

			visitTypeList(node.typeArguments()); // types

			pushValuedNode(GT, ">"); // >
			pop();
		}

		pushMethodNode(node.getNodeType(), node.getName().toString());
		pop();

		pushValuedNode(LEFTPARENTHESIS, "("); // (
		pop();

		visitList(node.arguments()); // arguments

		pushValuedNode(RIGHTPARENTHESIS, ")"); // )
		pop();

		return false;
	}

	/*
	 * case Expression : default :
	 */
	@Override
	public boolean visit(SwitchCase node) {
		boolean isDefault = node.isDefault();
		if (isDefault) {
			pushValuedNode(DEFAULT, "default");
			pop();
		} else {
			pushValuedNode(CASE, "case");
			pop();

			pushValuedNode(SPACE, " ");
			pop();

			node.getExpression().accept(this);
		}
		pushValuedNode(COLON, ":");
		pop();
		return false;
	}

	/*
	 * format like: switch ( Expression ) { { SwitchCase | Statement } } }
	 */
	@Override
	public boolean visit(SwitchStatement node) {
		pushValuedNode(SWITCH, "switch");
		pop();

		pushValuedNode(LEFTPARENTHESIS, "(");
		pop();

		node.getExpression().accept(this);

		pushValuedNode(RIGHTPARENTHESIS, ")");
		pop();

		pushValuedNode(LEFTBRACE, "{");
		pop();

		visitList(node.statements());

		pushValuedNode(RIGHTBRACE, "}");
		pop();
		return false;
	}

	// format like: synchronized ( Expression ) Block
	@Override
	public boolean visit(SynchronizedStatement node) {
		pushValuedNode(SYNCHRONIZED, "synchronized");
		pop();

		pushValuedNode(LEFTPARENTHESIS, "(");
		pop();

		node.getExpression().accept(this);

		pushValuedNode(RIGHTPARENTHESIS, ")");
		pop();

		node.getBody().accept(this);

		return false;
	}

	// here the "this" is not mapped to a type
	@Override
	public boolean visit(ThisExpression node) {
		// format like: [ClassName.]this
		if (node.getQualifier() != null) {// heuristic: if this is
											// ClassName.this,
			node.getQualifier().accept(this);

			pushValuedNode(ASTExpressionTransformer.DOT, ".");
			pop();
		}
		pushValuedNode(ASTExpressionTransformer.THIS, "this");
		pop();

		return false;
	}

	// format like: throw Expression ;
	@Override
	public boolean visit(ThrowStatement node) {
		pushValuedNode(THROW, "throw");
		pop();

		pushValuedNode(SPACE, " ");
		pop();

		node.getExpression().accept(this);

		pushValuedNode(SEMICOLON, ";");
		pop();

		return false;
	}

	/*
	 * format like: try Block { CatchClause } [ finally Block ]
	 */
	@Override
	public boolean visit(TryStatement node) {
		pushValuedNode(TRY, "try");
		pop();

		node.getBody().accept(this);

		List<ASTNode> catches = node.catchClauses();
		for (ASTNode catchClause : catches) {
			catchClause.accept(this);
		}

		pushValuedNode(FINALLY, "finally");
		pop();

		node.getFinally().accept(this);
		return false;
	}

	@Override
	public boolean visit(TypeLiteral node) {
		// format like: (Type|void).class
		try {
			pushTypeNode(node.toString(), node.resolveTypeBinding()
					.getQualifiedName(), node.getNodeType());
		} catch (Exception e) {
			pushTypeNode(node.toString(), node.toString(), node.getNodeType());
		}
		// pushValuedNode(node.getNodeType(), node.toString());
		pop(node);
		return false;
	}

	@Override
	public boolean visit(VariableDeclarationExpression node) {
		// format like: {ExtendedModifier}Type VariableDeclarationFragment{,
		// VariableDeclarationFragment}
		// since the structure is complicated, and it is uncommon; I don't parse
		// out information carefully from it
		node.getType().accept(this);

		pushValuedNode(SPACE, " ");
		pop();

		visitList(node.fragments());

		// pushValuedNode(node.getNodeType(), node.toString());
		// pop(node);
		return false;
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		node.getName().accept(this);

		if (node.getInitializer() != null) {
			pushValuedNode(EQUAL, "=");
			pop();
			node.getInitializer().accept(this);
		}
		return false;
	}

	/**
	 * { ExtendedModifier } Type VariableDeclarationFragment { ,
	 * VariableDeclarationFragment } ;
	 */
	@Override
	public boolean visit(VariableDeclarationStatement node) {
		List<IExtendedModifier> modifiers = node.modifiers();
		if (modifiers == null || modifiers.size() == 0) {
			// do nothing
		} else {
			for (int i = 0; i < modifiers.size(); i++) {
				pushValuedNode(MODIFIER, ((Modifier) modifiers.get(i))
						.getKeyword().toString());
				pop();

				pushValuedNode(SPACE, " ");
				pop();
			}
		}
		node.getType().accept(this);

		pushValuedNode(SPACE, " ");
		pop();

		visitList(node.fragments());

		return false;
	}

	// format like: while ( Expression ) Statement
	@Override
	public boolean visit(WhileStatement node) {
		pushValuedNode(WHILE, "while");
		pop();

		pushValuedNode(LEFTPARENTHESIS, "(");
		pop();

		node.getExpression().accept(this);

		pushValuedNode(RIGHTPARENTHESIS, ")");
		pop();

		node.getBody().accept(this);
		return false;
	}

	private void pop(ASTNode node) {
		// fValueList.pop();
	}

	private void pop() {
		// fValueList.pop();
	}

	private void pushValuedNode(int nodeType, String nodeValue) {
		push(new Term(nodeType, nodeValue));
	}

	private void pushValueTypeNode(String variableName, String typeName,
			String qName, int nodeType) {
		String keyValue = variableName + "+" + typeName;
		VariableTypeBindingTerm vTerm = variableTypeMap.get(keyValue);
		if (vTerm == null) {
			vTerm = new VariableTypeBindingTerm(nodeType, variableName,
					Term.createAbstractName(ABSTRACT_VARIABLE,
							this.abstractVariableIndex++));
			TypeNameTerm tTerm = getTypeNameTerm(typeName, qName);
			vTerm.setTypeNameTerm(tTerm);
			variableTypeMap.put(keyValue, vTerm);
		}
		push(vTerm);
	}

	private void pushTypeNode(String typeName, String qName, int nodeType) {
		TypeNameTerm tTerm = getTypeNameTerm(typeName, qName, nodeType);
		push(tTerm);
	}

	public void pushMethodNode(int nodeType, String methodName) {
		MethodNameTerm mTerm = methodMap.get(methodName);
		if (mTerm == null) {
			mTerm = new MethodNameTerm(nodeType, methodName,
					Term.createAbstractName(ABSTRACT_METHOD,
							this.abstractMethodIndex++));
			methodMap.put(methodName, mTerm);
		}
		push(mTerm);
		// push(new MethodTypeBindingTerm(methodName, typeName, nodeType));
	}

	private void push(Term term) {
		fValueList.add(term);
	}

	private TypeNameTerm getTypeNameTerm(String typeName, String qName) {
		return getTypeNameTerm(typeName, qName, 0);
	}

	private TypeNameTerm getTypeNameTerm(String typeName, String qName,
			int nodeType) {
		TypeNameTerm tTerm = null;
		for (Entry<String, TypeNameTerm> entry : typeMap.entrySet()) {
			if (entry.getValue().getQualifiedName().equals(qName)) {
				tTerm = entry.getValue();
				break;
			}
		}
		if (tTerm == null) {
			for (Entry<String, TypeNameTerm> entry : typeMap.entrySet()) {
				if (entry.getKey().equals(typeName)) {
					tTerm = entry.getValue();
					break;
				}
			}
		}
		if (tTerm == null) {
			tTerm = new TypeNameTerm(nodeType, typeName, qName);
			tTerm.setAbstractTypeName(Term.createAbstractName(ABSTRACT_TYPE,
					this.abstractTypeIndex++));
			typeMap.put(typeName, tTerm);
		}
		return tTerm;
	}

	/**
	 * This method is not limited to visit expression node, but also any other
	 * kind of AST node
	 * 
	 * @param list
	 */
	private void visitList(List<ASTNode> list) {
		int numOfComma = list.size() - 1;
		int counter = 0;
		for (ASTNode nodeInList : list) {
			// if(nodeInList instanceof Expression){
			nodeInList.accept(this); // Argument'n'
			if (counter++ < numOfComma) {
				pushValuedNode(COMMA, ",");// ,
				pop();
			}
			// }
		}
	}

	private void visitTypeList(List<ASTNode> typeList) {
		int numOfComma = typeList.size() - 1;
		int counter = 0;
		for (ASTNode type : typeList) {
			if (type instanceof Type) {
				pushValuedNode(type.getNodeType(), type.toString()); // Type 'n'
				pop();
				if (counter++ < numOfComma) {
					pushValuedNode(COMMA, ",");// ,
					pop();
				}
			}
		}
	}
}
