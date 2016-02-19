package changeassistant.changesuggestion.astrewrite;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import changeassistant.change.group.model.SubTreeModel;

public class ASTRewriteBasedUpdate {

	public static void apply(ASTRewrite rewrite, ASTNode astNodeToUpdate,
			List<String> expressionStrings) {
		List<ASTNode> expressions = ASTNodeGenerator2.generateExpressions(
				astNodeToUpdate.getAST(), expressionStrings);

		switch (astNodeToUpdate.getNodeType()) {
		case ASTNode.EXPRESSION_STATEMENT: {
			rewrite.replace(
					((ExpressionStatement) astNodeToUpdate).getExpression(),
					expressions.get(0), null);
		}
			break;
		case ASTNode.IF_STATEMENT: {
			rewrite.replace(((IfStatement) astNodeToUpdate).getExpression(),
					expressions.get(0), null);
		}
			break;
		case ASTNode.RETURN_STATEMENT: {
			rewrite.replace(
					((ReturnStatement) astNodeToUpdate).getExpression(),
					expressions.get(0), null);
		}
			break;
		case ASTNode.SWITCH_CASE: {
			rewrite.replace(((SwitchCase) astNodeToUpdate).getExpression(),
					expressions.get(0), null);
		}
			break;
		case ASTNode.SWITCH_STATEMENT: {
			rewrite.replace(
					((SwitchStatement) astNodeToUpdate).getExpression(),
					expressions.get(0), null);
		}
			break;
		case ASTNode.SUPER_CONSTRUCTOR_INVOCATION: {
			// no expression can be generated since this is a statement
			expressions = ASTNodeGenerator2.generateStatements(
					astNodeToUpdate.getAST(), expressionStrings);
			if (expressions.isEmpty()) {
				String expr = expressionStrings.get(0);
				expr = expr.substring(expr.indexOf('(') + 1);
				expr = expr.substring(0, expr.lastIndexOf(')'));
				StringTokenizer st = new StringTokenizer(expr, ",");
				List<String> args = new ArrayList<String>();
				while (st.hasMoreElements()) {
					args.add(st.nextToken());
				}
				expressions = ASTNodeGenerator2.generateExpressions(
						astNodeToUpdate.getAST(), args);
				SuperConstructorInvocation sci = astNodeToUpdate.getAST()
						.newSuperConstructorInvocation();
				sci.arguments().addAll(expressions);
				rewrite.replace(astNodeToUpdate, sci, null);
			} else {
				rewrite.replace(astNodeToUpdate, expressions.get(0), null);
			}

		}
			break;
		case ASTNode.THROW_STATEMENT: {
			rewrite.replace(((ThrowStatement) astNodeToUpdate).getExpression(),
					expressions.get(0), null);
		}
			break;
		case ASTNode.VARIABLE_DECLARATION_STATEMENT: {
			rewrite.replace(
					astNodeToUpdate,
					ASTNodeGenerator2.generateStatements(
							astNodeToUpdate.getAST(), expressionStrings).get(0),
					null);
		}
			break;
		case ASTNode.WHILE_STATEMENT: {// to update the expression
			rewrite.replace(((WhileStatement) astNodeToUpdate).getExpression(),
					expressions.get(0), null);
		}
			break;
		}
		expressions = null;
	}

	public static void apply(ASTRewrite rewrite, ASTNode astNodeToUpdate,
			SubTreeModel newNode) throws RewriteException {
		ASTNode newASTNode = ASTNodeGenerator2.createASTNode(
				astNodeToUpdate.getAST(), newNode);
		switch (astNodeToUpdate.getNodeType()) {
		case ASTNode.EXPRESSION_STATEMENT: {
			switch (newNode.getNodeType()) {
			case ASTNode.IF_STATEMENT: {
				rewrite.replace(astNodeToUpdate, newASTNode, null);
			}
				break;
			}
		}
		case ASTNode.IF_STATEMENT: {
			switch (newNode.getNodeType()) {
			case ASTNode.EXPRESSION_STATEMENT: {
				rewrite.replace(astNodeToUpdate, newASTNode, null);
			}
				break;
			}
		}
			break;
		case ASTNode.RETURN_STATEMENT: {
			switch (newNode.getNodeType()) {
			case ASTNode.EXPRESSION_STATEMENT: {
				rewrite.replace(astNodeToUpdate, newASTNode, null);
			}
				break;
			}
		}
			break;
		case ASTNode.WHILE_STATEMENT: {
			switch (newNode.getNodeType()) {
			case ASTNode.FOR_STATEMENT: {
				ASTNode block = ASTNode.copySubtree(newASTNode.getAST(),
						((WhileStatement) astNodeToUpdate).getBody());
				rewrite.set(newASTNode, ForStatement.BODY_PROPERTY, block, null);
				rewrite.replace(astNodeToUpdate, newASTNode, null);
			}
				break;
			case ASTNode.IF_STATEMENT: {// by default, the while body is convert
										// to then block
			// ASTNode block =
			// rewrite.createCopyTarget(((WhileStatement)astNodeToUpdate).getBody());
				ASTNode block = ASTNode.copySubtree(newASTNode.getAST(),
						((WhileStatement) astNodeToUpdate).getBody());
				rewrite.set(newASTNode, IfStatement.THEN_STATEMENT_PROPERTY,
						block, null);
				rewrite.replace(astNodeToUpdate, newASTNode, null);
			}
				break;
			}
		}
			break;
		}
		newASTNode = null;
	}
}
