package changeassistant.changesuggestion.astrewrite;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import changeassistant.change.group.model.SubTreeModel;

public class ASTRewriteHelper {

	private static ListRewrite judgeBlockToRewrite(ASTNode astNode,
			ASTRewrite rewrite){
		if(astNode instanceof Block){
			return rewrite.getListRewrite(astNode, Block.STATEMENTS_PROPERTY);
		}else{
			Block block = null;
			block = astNode.getAST().newBlock();
			ASTNode newNode = rewrite.createCopyTarget(astNode);
			block.statements().add(newNode);
			rewrite.replace(astNode, block, null);
			return rewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);
		}
	}
	
	public static ListRewrite getListRewrite(ASTRewrite rewrite, ASTNode parentASTNode){
		ListRewrite listRewrite = null;
		switch(parentASTNode.getNodeType()){
		case ASTNode.ANONYMOUS_CLASS_DECLARATION:{
			listRewrite = rewrite.getListRewrite(
					parentASTNode, AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY);
		}break;
		case ASTNode.BLOCK: {
			listRewrite = rewrite.getListRewrite(parentASTNode, Block.STATEMENTS_PROPERTY);			
		}break;
		case ASTNode.CATCH_CLAUSE:{
			ASTNode astNode = ((CatchClause)parentASTNode).getBody();
			listRewrite = judgeBlockToRewrite(astNode, rewrite);
		}break;
		case ASTNode.DO_STATEMENT:{
			ASTNode astNode = ((DoStatement)parentASTNode).getBody();
			listRewrite = judgeBlockToRewrite(astNode, rewrite);
		}break;
		case ASTNode.ENHANCED_FOR_STATEMENT:{
			ASTNode astNode = ((EnhancedForStatement)parentASTNode).getBody();
			listRewrite = judgeBlockToRewrite(astNode, rewrite);
		}break;
		case ASTNode.EXPRESSION_STATEMENT:{
			
		}break;
		case ASTNode.FOR_STATEMENT:{
			ASTNode astNode = ((ForStatement)parentASTNode).getBody();
			listRewrite = judgeBlockToRewrite(astNode, rewrite);
		}break;
		case ASTNode.IF_STATEMENT:{//directly return null since it is hard to decide whether to return then-statement
			//or else-statement property
		}break;
		case ASTNode.LABELED_STATEMENT:{
			ASTNode astNode = ((LabeledStatement)parentASTNode).getBody();
			listRewrite = judgeBlockToRewrite(astNode, rewrite);
		}break;
		case ASTNode.METHOD_DECLARATION: {
			ASTNode block = ((MethodDeclaration) parentASTNode).getBody();
			if(block == null){
				block = parentASTNode.getAST().newBlock();
				rewrite.set(parentASTNode, MethodDeclaration.BODY_PROPERTY, block, null);
			}
			listRewrite = rewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);		
		}break;
		case ASTNode.SWITCH_STATEMENT: {
			listRewrite = rewrite.getListRewrite(parentASTNode, SwitchStatement.STATEMENTS_PROPERTY);
		}break;
		case ASTNode.SYNCHRONIZED_STATEMENT:{
			ASTNode astNode = ((SynchronizedStatement)parentASTNode).getBody();
			listRewrite = judgeBlockToRewrite(astNode, rewrite);
		}break;
		case ASTNode.TRY_STATEMENT: {//the child must be a catch clause			
			listRewrite = rewrite.getListRewrite(parentASTNode, TryStatement.CATCH_CLAUSES_PROPERTY);
		}break;
		case ASTNode.WHILE_STATEMENT: {
			ASTNode astNode = ((WhileStatement)parentASTNode).getBody();
			listRewrite = judgeBlockToRewrite(astNode, rewrite);
		}break;
		default: {
			listRewrite = rewrite.getListRewrite(parentASTNode, Block.STATEMENTS_PROPERTY);					
		}break;
		}
		return listRewrite;
	}
	
	public static void insert(ASTRewrite rewrite, ASTNode astNodeToInsert, ASTNode parentASTNode, 
			int position, SubTreeModel insertedNode) throws RewriteException{
//		System.out.print("");
		ListRewrite listRewrite = getListRewrite(rewrite, parentASTNode);
		if(listRewrite != null){
			switch(parentASTNode.getNodeType()){
			
			case ASTNode.SWITCH_STATEMENT:{
				if(position == 0 && !(astNodeToInsert instanceof SwitchCase)){
					throw new RewriteException("Insert a non-switch case at the 0th " +
							"position of SwitchStatement");
				}
			}break;
			case ASTNode.TRY_STATEMENT:{
				if(astNodeToInsert instanceof CatchClause){
					List<CatchClause> ccs = ((TryStatement)parentASTNode).catchClauses();
					List<SingleVariableDeclaration> svds = 
						new ArrayList<SingleVariableDeclaration>();
					for(CatchClause cc : ccs){
						svds.add(cc.getException());
					}
					if(svds.contains(((CatchClause)astNodeToInsert).getException())){
						return;//do not insert this catch because it is already there
					}
				}
				position--;
			}break;
			}
			listRewrite.insertAt(astNodeToInsert, position, null);
		}else{
			if(insertedNode.getStrValue().equals("then:") || insertedNode.getStrValue().equals("else:")){
				IfStatement parent = (IfStatement)parentASTNode;
				if(insertedNode.getStrValue().equals("then:")){
					if(parent.getThenStatement() == null){
						rewrite.set(parentASTNode, IfStatement.THEN_STATEMENT_PROPERTY, astNodeToInsert, null);
					}else{
//						System.out.print("then is not empty");
					}
				}else{
					if(insertedNode.getStrValue().equals("else:")){
						if(parent.getThenStatement() == null){
							rewrite.set(parentASTNode, 
									IfStatement.THEN_STATEMENT_PROPERTY, parentASTNode.getAST().newBlock(), null);
						}
						rewrite.set(parentASTNode, IfStatement.ELSE_STATEMENT_PROPERTY, astNodeToInsert, null);
					}
				}
				parent = null;
			}else if(insertedNode.getStrValue().contains("anonyClass:")){
				SpecificASTNodeFinder finder = new SpecificASTNodeFinder(ASTNode.CLASS_INSTANCE_CREATION);
				parentASTNode.accept(finder);
				rewrite.set(finder.result, ClassInstanceCreation.ANONYMOUS_CLASS_DECLARATION_PROPERTY, 
						astNodeToInsert, null);
			}
		}
		listRewrite = null;
	}
}
