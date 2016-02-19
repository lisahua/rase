package changeassistant.changesuggestion.astrewrite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import changeassistant.change.group.model.SubTreeModel;
import changeassistant.changesuggestion.expression.representation.AbstractExpressionRepresentationGenerator;

public class ASTNodeGenerator2 {
	
	private static ASTParser expressionParser;
	
	private static ASTParser statementParser;
	
	public static List<String> primitiveTypeStrings = Arrays.asList(new String[]{"byte",
	    "short", "char", "int", "long", "float", "double", "boolean", "void"});
	
	static{
		expressionParser = ASTParser.newParser(AST.JLS3);
		expressionParser.setKind(ASTParser.K_EXPRESSION);
		statementParser = ASTParser.newParser(AST.JLS3);
		statementParser.setKind(ASTParser.K_STATEMENTS);
	}
	
	public static ASTNode createASTNode(AST ast, SubTreeModel node) throws RewriteException{
//		System.out.print("");

		ASTNode astNode = null;
		try{
		List<String> expressionStrings = AbstractExpressionRepresentationGenerator.createConcreteStringList(node.getAbstractExpressions());
		List<ASTNode> expressions = generateExpressions(ast, expressionStrings);
		if(node.getStrValue().equals("then:") || node.getStrValue().equals("else:")){
			return ast.newBlock();
		}
		if(node.getStrValue().equals("try-body:")){
			return astNode;
		}
		switch(node.getNodeType()){
		case ASTNode.ANONYMOUS_CLASS_DECLARATION:{
			astNode = ast.newAnonymousClassDeclaration();
		}break;
		case ASTNode.BLOCK:{
			astNode = ast.newBlock();
		}break;
		case ASTNode.BREAK_STATEMENT:{
			astNode = ast.newBreakStatement();
		}break;
		case ASTNode.CATCH_CLAUSE:{//TODO
			if(expressions.get(0) instanceof CompilationUnit){//here a SingleVariableDeclaration is needed
				astNode = ast.newCatchClause();
				SingleVariableDeclaration svd = 
					createSingleVariableDeclaration(ast, expressionStrings.get(0));
				((CatchClause)astNode).setException(svd);
				svd = null;
			}
		}break;
		case ASTNode.EXPRESSION_STATEMENT:{
			astNode = ast.newExpressionStatement((Expression) expressions.get(0));
		}break;
		case ASTNode.FOR_STATEMENT:{
			astNode = ast.newForStatement();
			List<ASTNode> initializers;
			if(expressions.get(0) != null &&expressions.get(0) instanceof CompilationUnit){
				List<String> exprList = new ArrayList<String>();
				exprList.add(expressionStrings.get(0));
				initializers = generateStatements(ast, exprList);
				if(initializers.size() == 1){
					ASTNode initializer = initializers.get(0);
					if(initializer instanceof VariableDeclarationStatement){
						VariableDeclarationStatement vds = (VariableDeclarationStatement)initializer;
						List<VariableDeclarationFragment> frags = vds.fragments();
						VariableDeclarationFragment frag = (VariableDeclarationFragment)vds.fragments().get(0);
						VariableDeclarationExpression vde = ast.newVariableDeclarationExpression((VariableDeclarationFragment)
								ASTNode.copySubtree(ast, frag));
						for(int i = 1; i < frags.size(); i++){
							vde.fragments().add(ASTNode.copySubtree(ast, frags.get(i)));
						}
						vde.setType((Type) ASTNode.copySubtree(ast, vds.getType()));
						((ForStatement)astNode).initializers().add(vde);
						vds = null;
						vde = null;
						frags = null;
						frag = null;
					}
				}else{
					System.out.println("There is more than one initializers!");
				}
				exprList = null;
				initializers = null;
			}
			if(expressions.size() > 1){//condition
				((ForStatement)astNode).setExpression((Expression)expressions.get(1));
			}
			if(expressions.size() > 2){
				((ForStatement)astNode).updaters().add((Expression)expressions.get(2));
			}
		}break;
		case ASTNode.IF_STATEMENT:{
			astNode = ast.newIfStatement();
			if(expressions.isEmpty()){//this is else statement
				//do nothing 
			}else if(expressions.get(0) instanceof CompilationUnit){
				throw new RewriteException("the expression " + expressionStrings.get(0) + " cannot serve as" +
						" an if condition");
			}else{
				((IfStatement)astNode).setExpression((Expression)expressions.get(0));
			}
		}break;
		case ASTNode.METHOD_DECLARATION:{
			astNode = ast.newMethodDeclaration();
			((MethodDeclaration)astNode).setReturnType2(
					createType(ast, expressionStrings.get(0), expressions.get(0)));
			((MethodDeclaration)astNode).setName((SimpleName) expressions.get(1));
			for(int i = 2; i < expressions.size(); i++){
				System.out.print("Wait for further process");
			}
		}break;
		case ASTNode.METHOD_INVOCATION:{
			System.out.print("Wait for further process");
		}break;
		case ASTNode.RETURN_STATEMENT:{
			if(expressionStrings.isEmpty()){
				astNode = ast.newReturnStatement();
			}else{
				//there must be only one expression string in the list
				expressionStrings.set(0, "return " + expressionStrings.get(0));
				List<ASTNode> stmts = ASTNodeGenerator2.generateStatements(ast, expressionStrings);
				if(stmts.isEmpty()){
					throw new RewriteException("the expression in the return statement is not compilable");
				}
				astNode = ASTNodeGenerator2.generateStatements(ast, expressionStrings).get(0);
			}
		}break;
		case ASTNode.SWITCH_CASE:{
			astNode = ast.newSwitchCase();
			if(expressions.size() != 0){
				((SwitchCase)astNode).setExpression((Expression)expressions.get(0));
			}else{
				((SwitchCase)astNode).setExpression(null);
			}
		}break;
		case ASTNode.SWITCH_STATEMENT:{
			astNode = ast.newSwitchStatement();
			((SwitchStatement)astNode).setExpression((Expression)expressions.get(0));
		}break;
		case ASTNode.SYNCHRONIZED_STATEMENT:{
			astNode = ast.newSynchronizedStatement();
			((SynchronizedStatement)astNode).setExpression((Expression)expressions.get(0));
		}break;
		case ASTNode.THROW_STATEMENT:{
			astNode = ast.newThrowStatement();
			((ThrowStatement)astNode).setExpression((Expression)expressions.get(0));
		}break;
		case ASTNode.TRY_STATEMENT:{
			astNode = ast.newTryStatement();
			CatchClause defaultCatch = ast.newCatchClause();
			SingleVariableDeclaration svd = createSingleVariableDeclaration(ast, "Exception e");
			defaultCatch.setException(svd);
			((TryStatement)astNode).catchClauses().add(defaultCatch);//for compilation
			defaultCatch = null;
			svd = null;
		}break;
		case ASTNode.VARIABLE_DECLARATION_STATEMENT: {
			astNode = ASTNodeGenerator2.generateStatements(ast, expressionStrings).get(0);
		}break;
		case ASTNode.WHILE_STATEMENT:{
			astNode = ast.newWhileStatement();
			((WhileStatement)astNode).setExpression((Expression)expressions.get(0));
		}break;
		}
		expressionStrings = null;
		expressions = null;
		}catch(Exception e){
//			e.printStackTrace();
			throw new RewriteException(e.getMessage() + " The node cannot be created");
		}
		return astNode;
	}

	
	/**
	 * format like: Type variable
	 * @param str
	 */
	private static SingleVariableDeclaration createSingleVariableDeclaration(
			AST ast, String str){
//		System.out.print("");
		StringTokenizer st = new StringTokenizer(str, " ");
		SingleVariableDeclaration svd = ast.newSingleVariableDeclaration();
		String typeName, varName;
		if(st.countTokens() == 2){
			typeName = (String)st.nextElement();
			varName = (String)st.nextElement();
			svd.setType(ast.newSimpleType(ast.newSimpleName(typeName)));
			svd.setName(ast.newSimpleName(varName));
		}else{
			System.out.println("The parameters used to create SingleVariableDeclaration are too many!");
		}
		return svd;
	}

	private static Type createType(AST ast, String typeStr, ASTNode expression){
		if(primitiveTypeStrings.contains(typeStr)){//primitive type
			return ast.newPrimitiveType(PrimitiveType.toCode(typeStr));
		}else if(typeStr.endsWith("[]")){//array type
			String temp = typeStr;
			int counter = 0;
			while(temp.contains("[]")){
				temp = temp.substring(temp.indexOf("[]") + 2);//remove one "[]"
				counter ++;
			}
			List<String> exprStrs = Arrays.asList(temp);
			List<ASTNode> expressions = generateExpressions(ast, exprStrs);
			Type elementType = createType(ast, temp, expressions.get(0));
			return ast.newArrayType(elementType, counter);
		}else{//simple type
			if(expression instanceof Name)
				return ast.newSimpleType((Name)expression);
			else
				return null;
		}
	}
	
	public static List<ASTNode> generateExpressions(AST ast, List<String> expressionStrings){
		List<ASTNode> expressions = new ArrayList<ASTNode>();
		ASTNode astNewNode;
		for(int i = 0; i < expressionStrings.size(); i ++){
			expressionParser.setKind(ASTParser.K_EXPRESSION);
			expressionParser.setSource(expressionStrings.get(i).toCharArray());
			astNewNode = expressionParser.createAST(null);
			astNewNode = ASTNode.copySubtree(ast, astNewNode);
			expressions.add(astNewNode);
		}
		return expressions;
	}
	
	public static List<ASTNode> generateStatements(AST ast, List<String> expressionStrings){ 
//		System.out.print("");
		List<ASTNode> statements = new ArrayList<ASTNode>();
		List<String> statementStrings = new ArrayList<String>();
		String temp = null;
		for(int i = 0; i < expressionStrings.size(); i ++){
			temp = expressionStrings.get(i);
			if(temp.endsWith(";")){
				//do nothing
			}else{
				temp = temp + ";";
			}
			statementStrings.add(temp);
		}
		for(int i = 0; i < statementStrings.size(); i ++){
			statementParser.setKind(ASTParser.K_STATEMENTS);
			statementParser.setSource(statementStrings.get(i).toCharArray());
			statements.addAll(((Block)statementParser.createAST(null)).statements());
		}
		List<ASTNode> result = new ArrayList<ASTNode>();
		for(ASTNode stmt : statements){
			result.add(ASTNode.copySubtree(ast, stmt));
		}
		statements = null;
		statementStrings = null;
		return result;
	}
}
