//package changeassistant.changesuggestion.expression.representation;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import org.eclipse.jdt.core.dom.ASTMatcher;
//import org.eclipse.jdt.core.dom.ASTNode;
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
//import org.eclipse.jdt.core.dom.EmptyStatement;
//import org.eclipse.jdt.core.dom.EnhancedForStatement;
//import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
//import org.eclipse.jdt.core.dom.EnumDeclaration;
//import org.eclipse.jdt.core.dom.Expression;
//import org.eclipse.jdt.core.dom.ExpressionStatement;
//import org.eclipse.jdt.core.dom.FieldAccess;
//import org.eclipse.jdt.core.dom.FieldDeclaration;
//import org.eclipse.jdt.core.dom.ForStatement;
//import org.eclipse.jdt.core.dom.IfStatement;
//import org.eclipse.jdt.core.dom.ImportDeclaration;
//import org.eclipse.jdt.core.dom.InfixExpression;
//import org.eclipse.jdt.core.dom.Initializer;
//import org.eclipse.jdt.core.dom.InstanceofExpression;
//import org.eclipse.jdt.core.dom.LabeledStatement;
//import org.eclipse.jdt.core.dom.MemberRef;
//import org.eclipse.jdt.core.dom.MemberValuePair;
//import org.eclipse.jdt.core.dom.Message;
//import org.eclipse.jdt.core.dom.MethodDeclaration;
//import org.eclipse.jdt.core.dom.MethodInvocation;
//import org.eclipse.jdt.core.dom.MethodRef;
//import org.eclipse.jdt.core.dom.MethodRefParameter;
//import org.eclipse.jdt.core.dom.Modifier;
//import org.eclipse.jdt.core.dom.NullLiteral;
//import org.eclipse.jdt.core.dom.NumberLiteral;
//import org.eclipse.jdt.core.dom.PackageDeclaration;
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
//import org.eclipse.jdt.core.dom.TagElement;
//import org.eclipse.jdt.core.dom.TextElement;
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
//public class ASTStructureMatcher extends ASTMatcher {
//
//	public static final int NOT_SAME = 0;
//	public static final int SAME_TYPE = 1;
//	public static final int SAME_TYPE_STRING = 2;
//	
//	private MethodPair mp;
//	
//	private boolean isAmbiguouslyMatched;
//	
//	private List<ASTNode> method1Specific;
//	
//	private List<ASTNode> method2Specific;
//	
//	public ASTStructureMatcher(MethodPair mp){
//		this.mp = mp;		
//		this.isAmbiguouslyMatched = true;
//	}
//	
//	public ASTStructureMatcher(){
//		method1Specific = new ArrayList<ASTNode>();
//		method2Specific = new ArrayList<ASTNode>();
//	}
//	
//	public List<ASTNode> getMethodSpecific(int i){
//		switch(i){
//		case 1: return method1Specific;
//		case 2: return method2Specific;
//		}
//		return null;
//	}
//	
//	//keep matching until all nodes in the ASTNode are matched or until finding that they cannot
//	//match each other
//	
//	@Override
//	public boolean match(AnnotationTypeDeclaration node, Object other){
//		return true;
//	}
//	
//	@Override
//	public boolean match(AnnotationTypeMemberDeclaration node, Object other){
//		return true;
//	}
//	/**
//	 * AnonymousClassDeclaration:
//        { ClassBodyDeclaration }
//	 */
//	@Override
//	public boolean match(AnonymousClassDeclaration node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			AnonymousClassDeclaration another = (AnonymousClassDeclaration)other;
//			matchList(node.bodyDeclarations(), another.bodyDeclarations());			
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 *  ArrayAccess:
//    		Expression [ Expression ]
//	 */
//	@Override
//	public boolean match(ArrayAccess node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);//simple match and add to Method Pair List if not matched
//		if(flag == SAME_TYPE){
//			ArrayAccess another = (ArrayAccess)other;
//			node.getArray().subtreeMatch(this, another.getArray());
//			node.getIndex().subtreeMatch(this, another.getIndex());
//		}		
//		return flag == SAME_TYPE_STRING;
//		
//	}	
//	/**
//	 * new PrimitiveType [ Expression ] { [ Expression ] } { [ ] }
//       new TypeName [ < Type { , Type } > ]
//        	[ Expression ] { [ Expression ] } { [ ] }
//       new PrimitiveType [ ] { [ ] } ArrayInitializer
//       new TypeName [ < Type { , Type } > ]
//        	[ ] { [ ] } ArrayInitializer
//	 */
//	@Override
//	public boolean match(ArrayCreation node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			ArrayCreation another = (ArrayCreation)other;
//			node.getType().subtreeMatch(this, another.getType());
//			node.getInitializer().subtreeMatch(this, another.getInitializer());
//			matchList(node.dimensions(), another.dimensions());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	
//	/**
//	 * ArrayInitializer:
// 		{ [ Expression { , Expression} [ , ]] }
//	 */
//	@Override
//	public boolean match(ArrayInitializer node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			ArrayInitializer another = (ArrayInitializer)other;
//			matchList(node.expressions(), another.expressions());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * ArrayType:
//    	Type [ ]	
//	 */
//	@Override
//	public boolean match(ArrayType node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			ArrayType another = (ArrayType)other;
//			node.getComponentType().subtreeMatch(this, another.getComponentType());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * AssertStatement:
//    	assert Expression [ : Expression ] ;
//	 */
//	@Override
//	public boolean match(AssertStatement node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			AssertStatement another = (AssertStatement)other;
//			node.getExpression().subtreeMatch(this, another.getExpression());
//			node.getMessage().subtreeMatch(this, another.getExpression());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * Here AssignmentOperation is not saved
//	 * Assignment:
//    	Expression AssignmentOperator Expression
//	 */
//	@Override
//	public boolean match(Assignment node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			Assignment another = (Assignment)other;
//			node.getLeftHandSide().subtreeMatch(this, another.getLeftHandSide());
//			node.getRightHandSide().subtreeMatch(this, another.getRightHandSide());
//		}	
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 *  Block:
//    	{ { Statement } }
//	 */
//	@Override
//	public boolean match(Block node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			Block another = (Block)other;
//			matchList(node.statements(), another.statements());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * BooleanLiteral:
// 		true
// 		false
//	 */
//	@Override
//	public boolean match(BooleanLiteral node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * BreakStatement:
//    	break [ Identifier ] ;
//	 */	
//	@Override
//	public boolean match(BreakStatement node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			BreakStatement another = (BreakStatement)other;
//			node.getLabel().subtreeMatch(this, another.getLabel());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * CastExpression:
//    	( Type ) Expression 
//	 */
//	@Override
//	public boolean match(CastExpression node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			CastExpression another = (CastExpression)other;
//			node.getType().subtreeMatch(this, another.getType());
//			node.getExpression().subtreeMatch(this, another.getExpression());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * CatchClause:
//    	catch ( FormalParameter ) Block
//	 */
//	@Override
//	public boolean match(CatchClause node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			CatchClause another = (CatchClause)other;
//			node.getBody().subtreeMatch(this, another.getBody());
//			node.getException().subtreeMatch(this, another.getException());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * Character literal nodes. 
//	 */
//	@Override
//	public boolean match(CharacterLiteral node, Object other){
//		int flag  = simpleMatch(node, (ASTNode)other);
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 *  ClassInstanceCreation:
//        [ Expression . ]
//            new [ < Type { , Type } > ]
//            Type ( [ Expression { , Expression } ] )
//            [ AnonymousClassDeclaration ]
//	 */	
//	@Override
//	public boolean match(ClassInstanceCreation node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			ClassInstanceCreation another = (ClassInstanceCreation)other;
//			matchList(node.arguments(),another.arguments());
//			node.getAnonymousClassDeclaration().subtreeMatch(this, another.getAnonymousClassDeclaration());
//			node.getExpression().subtreeMatch(this, another.getExpression());
//			node.getType().subtreeMatch(this, another.getType());
//			matchList(node.typeArguments(), another.typeArguments());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * CompilationUnit:
//    [ PackageDeclaration ]
//        { ImportDeclaration }
//        { TypeDeclaration | EnumDeclaration | AnnotationTypeDeclaration 
//	 */
//	@Override
//	public boolean match(CompilationUnit node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			CompilationUnit another = (CompilationUnit)other;
//			matchList(node.types(), another.types());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * ConditionalExpression:
//    	Expression ? Expression : Expression
//	 */		
//	@Override
//	public boolean match(ConditionalExpression node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			ConditionalExpression another = (ConditionalExpression)other;
//			node.getExpression().subtreeMatch(this, another.getExpression());
//			node.getThenExpression().subtreeMatch(this, another.getThenExpression());
//			node.getElseExpression().subtreeMatch(this, another.getElseExpression());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * ConstructorInvocation:
//		this ( [ Expression { , Expression } ] ) ;
//	 */
//	@Override
//	public boolean match(ConstructorInvocation node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			ConstructorInvocation another = (ConstructorInvocation)other;
//			matchList(node.arguments(), another.arguments());
//			matchList(node.typeArguments(), another.typeArguments());
//		}		
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 *  ContinueStatement:
//    	continue [ Identifier ] ;
//	 */
//	@Override
//	public boolean match(ContinueStatement node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			ContinueStatement another = (ContinueStatement)other;
//			node.getLabel().subtreeMatch(this, another.getLabel());
//		}				
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 *  DoStatement:
//    	do Statement while ( Expression ) ;
//	 */
//	@Override
//	public boolean match(DoStatement node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			DoStatement another = (DoStatement)other;
//			node.getBody().subtreeMatch(this, another.getBody());
//			node.getExpression().subtreeMatch(this, another.getExpression());
//		}return flag == SAME_TYPE_STRING;		
//	}
//	/**
//	 *  EmptyStatement:
//    ;
//	 */
//	@Override
//	public boolean match(EmptyStatement node, Object other){
//		return true;
//	}
//	/**	
//	 * EnhancedForStatement:
//    for ( FormalParameter : Expression )
// 			Statement
//	 */
//	@Override
//	public boolean match(EnhancedForStatement node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			EnhancedForStatement another = (EnhancedForStatement)other;
//			node.getParameter().subtreeMatch(this, another.getParameter());
//			node.getExpression().subtreeMatch(this, another.getExpression());
//			node.getBody().subtreeMatch(this, another.getBody());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * EnumConstantDeclaration:
//     [ Javadoc ] { ExtendedModifier } Identifier
//         [ ( [ Expression { , Expression } ] ) ]
//         [ AnonymousClassDeclaration ]
//	 */
//	@Override
//	public boolean match(EnumConstantDeclaration node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			EnumConstantDeclaration another = (EnumConstantDeclaration)other;
//			matchList(node.arguments(), another.arguments());
//			node.getAnonymousClassDeclaration().subtreeMatch(this, another.getAnonymousClassDeclaration());
//			node.getName().subtreeMatch(this, another.getName());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * EnumDeclaration:
//     [ Javadoc ] { ExtendedModifier } enum Identifier
//         [ implements Type { , Type } ]
//         {
//         [ EnumConstantDeclaration { , EnumConstantDeclaration } ] [ , ]
//         [ ; { ClassBodyDeclaration | ; } ]
//         }
//	 */
//	@Override
//	public boolean match(EnumDeclaration node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			EnumDeclaration another = (EnumDeclaration)other;
//			matchList(node.enumConstants(), another.enumConstants());
//			matchList(node.superInterfaceTypes(), another.superInterfaceTypes());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * ExpressionStatement:
//    StatementExpression ;
//	 */
//	@Override
//	public boolean match(ExpressionStatement node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			ExpressionStatement another = (ExpressionStatement)other;
//			node.getExpression().subtreeMatch(this, another.getExpression());
//		}return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * FieldAccess: 
// 		Expression . Identifier
//	 */
//	@Override
//	public boolean match(FieldAccess node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			FieldAccess another = (FieldAccess)other;
//			node.getExpression().subtreeMatch(this, another.getExpression());
//			node.getName().subtreeMatch(this, another.getName());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * FieldDeclaration:
//    [Javadoc] { ExtendedModifier } Type VariableDeclarationFragment
//         { , VariableDeclarationFragment } ;
//	 */
//	@Override
//	public boolean match(FieldDeclaration node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			FieldDeclaration another = (FieldDeclaration)other;
//			matchList(node.fragments(), another.fragments());
//			node.getType().subtreeMatch(this, another.getType());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * ForStatement:
//    for (
// 			[ ForInit ];
// 			[ Expression ] ;
// 			[ ForUpdate ] )
// 			Statement
// ForInit:
// 		Expression { , Expression }
// ForUpdate:
// 		Expression { , Expression }
//	 */
//	@Override
//	public boolean match(ForStatement node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			 ForStatement another = (ForStatement)other;
//			 matchList(node.initializers(), another.initializers());
//			 node.getExpression().subtreeMatch(this, another.getExpression());
//			 matchList(node.updaters(), another.updaters());
//			 node.getBody().subtreeMatch(this, another.getBody());			 
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 *  IfStatement:
//    if ( Expression ) Statement [ else Statement]
//	 */
//	@Override
//	public boolean match(IfStatement node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			IfStatement another = (IfStatement)other;
//			node.getExpression().subtreeMatch(this, another.getExpression());
//			node.getThenStatement().subtreeMatch(this, another.getExpression());
//			node.getElseStatement().subtreeMatch(this, another.getElseStatement());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	
//	@Override
//	public boolean match(ImportDeclaration node, Object other){
//		return true;
//	}
//	/**
//	 * InfixExpression:
//    Expression InfixOperator Expression { InfixOperator Expression } 
//	 */
//	@Override
//	public boolean match(InfixExpression node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			InfixExpression another = (InfixExpression)other;
//			node.getLeftOperand().subtreeMatch(this, another.getLeftOperand());
//			node.getRightOperand().subtreeMatch(this, another.getRightOperand());
//			matchList(node.extendedOperands(), another.extendedOperands());
//		}		
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * Initializer:
//     [ static ] Block
//	 */	
//	@Override
//	public boolean match(Initializer node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			Initializer another = (Initializer)other;
//			node.getBody().subtreeMatch(this, another.getBody());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * InstanceofExpression:
//    Expression instanceof Type
//	 */
//	@Override
//	public boolean match(InstanceofExpression node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			InstanceofExpression another = (InstanceofExpression)other;
//			node.getLeftOperand().subtreeMatch(this, another.getLeftOperand());
//			node.getRightOperand().subtreeMatch(this, another.getRightOperand());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * LabeledStatement:
//    Identifier : Statement
//	 */
//	@Override
//	public boolean match(LabeledStatement node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			LabeledStatement another = (LabeledStatement)other;
//			node.getLabel().subtreeMatch(this, another.getLabel());
//			node.getBody().subtreeMatch(this, another.getBody());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	
//	/**
//	 * MemberRef:
// 		[ Name ] # Identifier
//	 */
//	@Override
//	public boolean match(MemberRef node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			MemberRef another = (MemberRef)other;
//			node.getName().subtreeMatch(this, another.getName());
//			node.getQualifier().subtreeMatch(this, another.getQualifier());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * MemberValuePair:
//   SimpleName = Expression
//	 */
//	@Override
//	public boolean match(MemberValuePair node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			MemberValuePair another = (MemberValuePair)other;			
//			node.getName().subtreeMatch(this, another.getName());
//			node.getValue().subtreeMatch(this, another.getValue());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 *  MethodDeclaration:
//    [ Javadoc ] { ExtendedModifier }
//		  [ < TypeParameter { , TypeParameter } > ]
//        ( Type | void ) Identifier (
//        [ FormalParameter 
// 		     { , FormalParameter } ] ) {[ ] }
//        [ throws TypeName { , TypeName } ] ( Block | ; )
// ConstructorDeclaration:
//    [ Javadoc ] { ExtendedModifier }
//		  [ < TypeParameter { , TypeParameter } > ]
//        Identifier (
// 		  [ FormalParameter
// 			 { , FormalParameter } ] )
//        [throws TypeName { , TypeName } ] Block
//	 */
//	@Override
//	public boolean match(MethodDeclaration node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			MethodDeclaration another = (MethodDeclaration)other;
//			node.getBody().subtreeMatch(this, another.getBody());
//			node.getName().subtreeMatch(this, another.getName());
//			node.getReturnType2().subtreeMatch(this, another.getReturnType2());
//			matchList(node.parameters(), another.parameters());
//			matchList(node.thrownExceptions(), another.thrownExceptions());
//			matchList(node.typeParameters(), another.typeParameters());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * MethodInvocation:
//     [ Expression . ]  
//         [ < Type { , Type } > ]
//         Identifier ( [ Expression { , Expression } ] 
//	 */
//	@Override
//	public boolean match(MethodInvocation node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			MethodInvocation another = (MethodInvocation)other;
//			matchList(node.arguments(), another.arguments());			
//			node.getExpression().subtreeMatch(this, another.getExpression());
//			node.getName().subtreeMatch(this, another.getName());
//			matchList(node.typeArguments(), another.typeArguments());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * MethodRef:
//     [ Name ] # Identifier  
//         ( [ MethodRefParameter | { , MethodRefParameter } ] )
//	 */
//	@Override
//	public boolean match(MethodRef node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			MethodRef another = (MethodRef)other;
//			node.getName().subtreeMatch(this, another.getName());
//			node.getQualifier().subtreeMatch(this, another.getQualifier());
//			matchList(node.parameters(), another.parameters());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * MethodRefParameter:
// 		Type [ ... ] [ Identifier ] 
//	 */	
//	@Override
//	public boolean match(MethodRefParameter node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			MethodRefParameter another = (MethodRefParameter)other;
//			node.getName().subtreeMatch(this, another.getName());
//			node.getType().subtreeMatch(this, another.getType());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * Null literal node. 
//	 */
//	@Override
//	public boolean match(NullLiteral node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * Number literal nodes. 
//	 */
//	@Override
//	public boolean match(NumberLiteral node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * PackageDeclaration:
//    [ Javadoc ] { Annotation } package Name ;
//	 */
//	@Override
//	public boolean match(PackageDeclaration node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			PackageDeclaration another = (PackageDeclaration)other;
//			node.getName().subtreeMatch(this, another.getName());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * ParameterizedType:
//    Type < Type { , Type } >
//	 */
//	@Override
//	public boolean match(ParameterizedType node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			ParameterizedType another = (ParameterizedType)other;
//			node.getType().subtreeMatch(this, another.getType());
//			matchList(node.typeArguments(), another.typeArguments());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * ParenthesizedExpression:
//     ( Expression )
//	 */
//	@Override
//	public boolean match(ParenthesizedExpression node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			ParenthesizedExpression another = (ParenthesizedExpression)other;
//			node.getExpression().subtreeMatch(this, another.getExpression());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * PostfixExpression:
//    Expression PostfixOperator
//	 */
//	@Override
//	public boolean match(PostfixExpression node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			PostfixExpression another = (PostfixExpression)other;
//			node.getOperand().subtreeMatch(this, another.getOperand());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * PrefixExpression:
//    PrefixOperator Expression 
//	 */
//	@Override
//	public boolean match(PrefixExpression node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			PrefixExpression another = (PrefixExpression)other;
//			node.getOperand().subtreeMatch(this, another.getOperand());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * PrimitiveType:
//    byte
//    short
//    char
//    int
//    long
//    float
//    double
//    boolean
//    void
//	 */
//	@Override
//	public boolean match(PrimitiveType node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * QualifiedName:
//    Name . SimpleName
//	 */
//	@Override
//	public boolean match(QualifiedName node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			QualifiedName another = (QualifiedName)other;
//			node.getName().subtreeMatch(this, another.getName());
//			node.getQualifier().subtreeMatch(this, another.getQualifier());
//		}			
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * QualifiedType:
//    Type . SimpleName
//	 */
//	@Override
//	public boolean match(QualifiedType node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			QualifiedType another = (QualifiedType)other;
//			node.getName().subtreeMatch(this, another.getName());
//			node.getQualifier().subtreeMatch(this, another.getQualifier());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * ReturnStatement:
//    return [ Expression ] ;
//	 */
//	@Override
//	public boolean match(ReturnStatement node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			ReturnStatement another = (ReturnStatement)other;
//			node.getExpression().subtreeMatch(this, another.getExpression());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * 
//	 */
//	@Override
//	public boolean match(SimpleName node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * Type node for a named class type, a named interface type, or a type variable. 
//	 */
//	@Override
//	public boolean match(SimpleType node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			SimpleType another = (SimpleType)other;
//			node.getName().subtreeMatch(this, another.getName());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * SingleVariableDeclaration:
//    { ExtendedModifier } Type [ ... ] Identifier { [] } [ = Expression ]
//	 */
//	@Override
//	public boolean match(SingleVariableDeclaration node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			SingleVariableDeclaration another = (SingleVariableDeclaration)node;
//			node.getInitializer().subtreeMatch(this, another.getInitializer());
//			node.getName().subtreeMatch(this, another.getName());
//			node.getType().subtreeMatch(this, another.getType());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * String literal nodes. 
//	 */
//	@Override
//	public boolean match(StringLiteral node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * SuperConstructorInvocation:
//     [ Expression . ]
//         [ < Type { , Type } > ]
//         super ( [ Expression { , Expression } ] ) ;
//	 */
//	@Override
//	public boolean match(SuperConstructorInvocation node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			SuperConstructorInvocation another = (SuperConstructorInvocation)other;
//			matchList(node.arguments(), another.arguments());
//			node.getExpression().subtreeMatch(this, another.getExpression());
//			matchList(node.typeArguments(), another.typeArguments());
//		}		
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 *  SuperFieldAccess:
//     [ ClassName . ] super . Identifier
//	 */
//	@Override
//	public boolean match(SuperFieldAccess node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			SuperFieldAccess another = (SuperFieldAccess)other;
//			node.getName().subtreeMatch(this, another.getName());
//			node.getQualifier().subtreeMatch(this, another.getQualifier());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * SuperMethodInvocation:
//     [ ClassName . ] super .
//         [ < Type { , Type } > ]
//         Identifier ( [ Expression { , Expression } ] )
//	 */
//	@Override
//	public boolean match(SuperMethodInvocation node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			SuperMethodInvocation another = (SuperMethodInvocation)other;
//			matchList(node.arguments(), another.arguments());
//			node.getName().subtreeMatch(this, another.getName());
//			node.getQualifier().subtreeMatch(this, another.getQualifier());
//			matchList(node.typeArguments(), another.typeArguments());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * SwitchCase:
//		case Expression  :
//		default :
//	 */
//	@Override
//	public boolean match(SwitchCase node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			SwitchCase another = (SwitchCase)other;
//			node.getExpression().subtreeMatch(this, another.getExpression());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * SwitchStatement:
//		switch ( Expression ) 
// 			{ { SwitchCase | Statement } } }
// SwitchCase:
//		case Expression  :
//		default :
//	 */
//	@Override
//	public boolean match(SwitchStatement node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			SwitchStatement another = (SwitchStatement)other;
//			node.getExpression().subtreeMatch(this, another.getExpression());
//			matchList(node.statements(), another.statements());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * SynchronizedStatement:
//    synchronized ( Expression ) Block
//	 */
//	@Override
//	public boolean match(SynchronizedStatement node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			SynchronizedStatement another = (SynchronizedStatement)other;
//			node.getBody().subtreeMatch(this, another.getBody());
//			node.getExpression().subtreeMatch(this, another.getExpression());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	
//	@Override
//	public boolean match(TagElement node, Object other){
//		return true;
//	}
//	
//	@Override
//	public boolean match(TextElement node, Object other){
//		return true;
//	}
//	/**
//	 * ThisExpression:
//     [ ClassName . ] this
//	 */
//	@Override
//	public boolean match(ThisExpression node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			ThisExpression another = (ThisExpression)other;
//			node.getQualifier().subtreeMatch(this, another.getQualifier());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * ThrowStatement:
//    throw Expression ;
//	 */
//	@Override
//	public boolean match(ThrowStatement node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			ThrowStatement another = (ThrowStatement)other;
//			node.getExpression().subtreeMatch(this, another.getExpression());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * TryStatement:
//     try Block 
//         { CatchClause }
//         [ finally Block ]
//	 */
//	@Override
//	public boolean match(TryStatement node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			TryStatement another = (TryStatement)node;
//			matchList(node.catchClauses(), another.catchClauses());
//			node.getBody().subtreeMatch(this, another.getBody());
//			node.getFinally().subtreeMatch(this, another.getFinally());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * TypeDeclaration:
// 		ClassDeclaration
// 		InterfaceDeclaration
// ClassDeclaration:
//      [ Javadoc ] { ExtendedModifier } class Identifier
//			[ < TypeParameter { , TypeParameter } > ]
//			[ extends Type ]
//			[ implements Type { , Type } ]
//			{ { ClassBodyDeclaration | ; } }
// InterfaceDeclaration:
//      [ Javadoc ] { ExtendedModifier } interface Identifier
//			[ < TypeParameter { , TypeParameter } > ]
//			[ extends Type { , Type } ]
// 			{ { InterfaceBodyDeclaration | ; } }
//	 */
//	@Override
//	public boolean match(TypeDeclaration node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){			
//			TypeDeclaration another = (TypeDeclaration)other;			
//			matchList(convertToList(node.getFields()), convertToList(another.getFields()));			
//			matchList(convertToList(node.getMethods()), convertToList(another.getMethods()));			
//			node.getSuperclassType().subtreeMatch(this, another.getSuperclassType());
//			matchList(convertToList(node.getTypes()), convertToList(another.getTypes()));
//			matchList(node.superInterfaceTypes(), another.superInterfaceTypes());
//			matchList(node.typeParameters(), another.typeParameters());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * TypeDeclarationStatement:
//    TypeDeclaration
//    EnumDeclaration
//	 */
//	@Override
//	public boolean match(TypeDeclarationStatement node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			TypeDeclarationStatement another = (TypeDeclarationStatement)other;
//			node.getDeclaration().subtreeMatch(this, another.getDeclaration());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * TypeLiteral:
//     ( Type | void ) . class
//	 */
//	@Override
//	public boolean match(TypeLiteral node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			TypeLiteral another = (TypeLiteral)other;
//			node.getType().subtreeMatch(this, another.getType());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * TypeParameter:
//    TypeVariable [ extends Type { & Type } ]
//	 */
//	@Override
//	public boolean match(TypeParameter node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			TypeParameter another = (TypeParameter)other;
//			node.getName().subtreeMatch(this, another.getName());
//			matchList(node.typeBounds(), another.typeBounds());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * VariableDeclarationExpression:
//    { ExtendedModifier } Type VariableDeclarationFragment
//         { , VariableDeclarationFragment } 
//	 */
//	@Override
//	public boolean match(VariableDeclarationExpression node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			VariableDeclarationExpression another = (VariableDeclarationExpression)other;
//			matchList(node.fragments(), another.fragments());
//			node.getType().subtreeMatch(this, another.getType());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * VariableDeclarationFragment:
//    Identifier { [] } [ = Expression ]
//	 */
//	@Override
//	public boolean match(VariableDeclarationFragment node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			VariableDeclarationFragment another = (VariableDeclarationFragment)other;
//			node.getInitializer().subtreeMatch(this, another.getInitializer());
//			node.getName().subtreeMatch(this, another.getName());			
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * VariableDeclarationStatement:
//    { ExtendedModifier } Type VariableDeclarationFragment 
//        { , VariableDeclarationFragment } ;
//	 */
//	@Override
//	public boolean match(VariableDeclarationStatement node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			VariableDeclarationStatement another = (VariableDeclarationStatement)other;
//			matchList(node.fragments(), another.fragments());
//			node.getType().subtreeMatch(this, another.getType());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * WhileStatement:
//    while ( Expression ) Statement
//	 */
//	@Override
//	public boolean match(WhileStatement node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			WhileStatement another = (WhileStatement)other;
//			node.getBody().subtreeMatch(this, another.getBody());
//			node.getExpression().subtreeMatch(this, another.getExpression());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//	/**
//	 * ? [ ( extends | super) Type ] 
//	 */
//	@Override
//	public boolean match(WildcardType node, Object other){
//		int flag = simpleMatch(node, (ASTNode)other);
//		if(flag == SAME_TYPE){
//			WildcardType another = (WildcardType)other;
//			node.getBound().subtreeMatch(this, another.getBound());
//		}
//		return flag == SAME_TYPE_STRING;
//	}
//
//	private void addToMp(ASTNode node1, ASTNode node2){
//		method1Specific.add(node1);
//		method2Specific.add(node2);
//	}
//	
//	private void matchList(List<ASTNode> list1, List<ASTNode> list2){
//		int count = Math.min(list1.size(), list2.size());
//		boolean list1Longer = true;
//		if(count > list1.size()){
//			list1Longer = false;
//		}
//		for(int i = 0; i < count; i ++){
//			list1.get(i).subtreeMatch(this, list2.get(i));
//		}
//		//only save the common part
//	}
//	
//	private int simpleMatch(ASTNode node, ASTNode other){
//		//the processing for null case
//		if(node == null || other == null){//do not add "null" into the matching list
//			return SAME_TYPE_STRING;
//		}
//		ASTNode astNode = (ASTNode)other;
//		if(node.getNodeType() != astNode.getNodeType()){
//			addToMp(node, astNode);
//			return NOT_SAME;
//		}
//		if(!node.toString().equals(astNode.toString())){
//			addToMp(node, astNode);
//			return SAME_TYPE;
//		}	
//		return SAME_TYPE_STRING;//may be continue compare inside
//	}
//	
//	private List<ASTNode> convertToList(Object[] array){
//		List<ASTNode> list = new ArrayList<ASTNode>();
//		for(int i = 0; i < array.length; i ++){
//			list.add((ASTNode)array[i]);
//		}
//		return list;	
//	}
//}