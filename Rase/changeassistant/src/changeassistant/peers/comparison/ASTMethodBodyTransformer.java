package changeassistant.peers.comparison;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import changeassistant.internal.ASTAnonymousClassDeclarationFinder;
import changeassistant.peers.SourceCodeRange;

/**
 * Here for the abstract representation of AST, Expressions are all converted to
 * string to represent each node, while Structure nodes above Expressions are
 * all converted to nodes which are represented with Expressions
 * 
 * @author ibm
 * 
 */
public class ASTMethodBodyTransformer extends ASTVisitor {

	private Stack<Node> fNodeStack;
	private Node n;

	private ASTNode methodNode;

	private ASTAnonymousClassDeclarationFinder acdFinder;

	public Node createMethodBodyTree(ASTNode methodNode) {
		fNodeStack = new Stack<Node>();
		acdFinder = new ASTAnonymousClassDeclarationFinder();
		this.methodNode = methodNode;
		this.methodNode.accept(this);
		fNodeStack = null;
		this.methodNode = null;
		return n;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		// Currently, the MethodDeclaration node is not kept as part of the
		// created tree
		ASTNode[] astExpressions = new ASTNode[] { node };
		if (!fNodeStack.isEmpty()) {
			push("method:" + node.getName().getIdentifier(), node,
					astExpressions);
		} else {
			n = new Node(node.getNodeType(), "method declaration",
					new SourceCodeRange(node.getStartPosition(),
							node.getLength()), astExpressions);
			fNodeStack.push(n);
		}
		return true;
	}

	@Override
	public void endVisit(MethodDeclaration node) {
		fNodeStack.pop();
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		push("anonyClass", node);
		return true;
	}

	@Override
	public void endVisit(AnonymousClassDeclaration node) {
		pop(node);
	}

	@Override
	public boolean visit(ArrayType node) {
		return false;
	}

	@Override
	public boolean visit(AssertStatement node) {
		push("assert", node, node.getExpression());
		return true;
	}

	@Override
	public void endVisit(AssertStatement node) {
		pop(node);
	}

	@Override
	public boolean visit(BooleanLiteral node) {
		return false;
	}

	@Override
	public boolean visit(BreakStatement node) {
		push("break", node, node.getLabel());
		return false;
	}

	@Override
	public void endVisit(BreakStatement node) {
		pop(node);
	}

	@Override
	public boolean visit(CatchClause node) {
		push("catch", node, node.getException());
		node.getBody().accept(this);
		return false;
		// return true;
	}

	@Override
	public void endVisit(CatchClause node) {
		pop(node);
	}

	@Override
	public boolean visit(CharacterLiteral node) {
		return false;
	}

	/**
	 * ConstructorInvocation: [ < Type { , Type } > ] this ( [ Expression { ,
	 * Expression } ] ) ;
	 */
	@Override
	public boolean visit(ConstructorInvocation node) {
		push("this()", node, node);
		return true;
	}

	@Override
	public void endVisit(ConstructorInvocation node) {
		pop(node);
	}

	@Override
	public boolean visit(ContinueStatement node) {
		push("continue", node, node.getLabel());
		return false;
	}

	@Override
	public void endVisit(ContinueStatement node) {
		pop(node);
	}

	@Override
	public boolean visit(DoStatement node) {
		push("do-while", node, node.getExpression());
		return true;
	}

	@Override
	public void endVisit(DoStatement node) {
		pop(node);
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		push("en-for", node, node.getParameter(), node.getExpression());
		return true;
	}

	@Override
	public void endVisit(EnhancedForStatement node) {
		pop(node);
	}

	@Override
	public boolean visit(ExpressionStatement node) {
		System.out.print("");
		ASTNode tmpNode = acdFinder.findACD(node);
		if (tmpNode == null)
			push("expr", node, node.getExpression());
		else {
			int endIndex = node.toString().indexOf(tmpNode.toString());
			push("expr:" + node.toString().substring(0, endIndex), node,
					node.getExpression());
		}
		return true;
	}

	@Override
	public void endVisit(ExpressionStatement node) {
		pop(node);
	}

	@Override
	public boolean visit(FieldAccess node) {
		return false;
	}

	@Override
	public boolean visit(ForStatement node) {
		List<ASTNode> expressions = new ArrayList<ASTNode>();
		expressions.addAll(node.initializers());
		expressions.add(node.getExpression());
		expressions.addAll(node.updaters());
		push("for", node, expressions);
		return true;
	}

	@Override
	public void endVisit(ForStatement node) {
		pop(node);
	}

	@Override
	public boolean visit(IfStatement node) {
		push("if", node, node.getExpression());

		if (node.getThenStatement() != null) {
			Statement thenStmt = node.getThenStatement();

			push("then", thenStmt, Collections.EMPTY_LIST);// the node's type
															// should be BLOCK
			thenStmt.accept(this);
			pop(thenStmt);
		}

		if (node.getElseStatement() != null) {
			Statement elseStmt = node.getElseStatement();

			push("else", elseStmt, Collections.EMPTY_LIST);
			elseStmt.accept(this);
			pop(elseStmt);
		}
		return false;
	}

	@Override
	public void endVisit(IfStatement node) {
		pop(node);
	}

	@Override
	public boolean visit(LabeledStatement node) {
		push("label", node, node.getLabel());
		node.getBody().accept(this);
		return false;
	}

	@Override
	public void endVisit(LabeledStatement node) {
		pop(node);
	}

	@Override
	public boolean visit(NullLiteral node) {
		return false;
	}

	@Override
	public boolean visit(NumberLiteral node) {
		return false;
	}

	@Override
	public boolean visit(PrimitiveType node) {
		return false;
	}

	@Override
	public boolean visit(QualifiedName node) {
		return false;
	}

	@Override
	public boolean visit(QualifiedType node) {
		return false;
	}

	@Override
	public boolean visit(ReturnStatement node) {
		push("return", node, node.getExpression());
		return true;
	}

	@Override
	public void endVisit(ReturnStatement node) {
		pop(node);
	}

	@Override
	public boolean visit(SimpleName node) {
		return false;
	}

	@Override
	public boolean visit(SimpleType node) {
		return false;
	}

	@Override
	public boolean visit(StringLiteral node) {
		return false;
	}

	@Override
	public boolean visit(SuperConstructorInvocation node) {
		push("super()", node, node);
		return true;
	}

	@Override
	public void endVisit(SuperConstructorInvocation node) {
		pop(node);
	}

	@Override
	public boolean visit(SuperFieldAccess node) {
		return false;
	}

	@Override
	public boolean visit(SwitchCase node) {
		if (node.isDefault()) {
			push("default", node, node.getExpression());
		} else {
			push("case", node, node.getExpression());
		}
		return false;
	}

	@Override
	public void endVisit(SwitchCase node) {
		pop(node);
	}

	@Override
	public boolean visit(SwitchStatement node) {
		push("switch", node, node.getExpression());
		visitList(node.statements());
		return false;
		// return true;
	}

	@Override
	public void endVisit(SwitchStatement node) {
		pop(node);
	}

	@Override
	public boolean visit(SynchronizedStatement node) {
		push("sync", node, node.getExpression());
		return true;
	}

	@Override
	public void endVisit(SynchronizedStatement node) {
		pop(node);
	}

	@Override
	public boolean visit(ThisExpression node) {
		return false;
	}

	@Override
	public boolean visit(ThrowStatement node) {
		push("throw", node, node.getExpression());
		return false;
	}

	@Override
	public void endVisit(ThrowStatement node) {
		pop(node);
	}

	@Override
	public boolean visit(TryStatement node) {
		push("try", node, Collections.EMPTY_LIST); // push the outside "try"
													// node

		push("try-body", node.getBody(), Collections.EMPTY_LIST);
		node.getBody().accept(this);
		pop(node.getBody());

		visitList(node.catchClauses());
		if (node.getFinally() != null) {
			ASTNode fNode = node.getFinally();
			push("finally", fNode, Collections.EMPTY_LIST);
			fNode.accept(this);
			pop(fNode);
		}
		return false;
		// return true;
	}

	@Override
	public void endVisit(TryStatement node) {
		pop(node);
	}

	@Override
	public boolean visit(TypeLiteral node) {
		return false;
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		ASTNode tmpNode = acdFinder.findACD(node);
		if (tmpNode == null)
			push("decl", node, node);
		else {
			int endIndex = node.toString().indexOf(tmpNode.toString());
			push("decl:" + node.toString().substring(0, endIndex), node, node);
		}
		return true;
	}

	@Override
	public void endVisit(VariableDeclarationStatement node) {
		pop(node);
	}

	@Override
	public boolean visit(WhileStatement node) {
		push("while", node, node.getExpression());
		return true;
	}

	@Override
	public void endVisit(WhileStatement node) {
		pop(node);
	}

	private Node getCurrentParent() {
		return fNodeStack.peek();
	}

	private void pop(ASTNode node) {
		fNodeStack.pop();
	}

	private void push(String defaultStrValue, ASTNode node,
			ASTNode... expressions) {
		Node n = new Node(node.getNodeType(), defaultStrValue,
				new SourceCodeRange(node.getStartPosition(), node.getLength()),
				expressions);
		getCurrentParent().add(n);
		fNodeStack.push(n);
	}

	private void push(String defaultStrValue, ASTNode node,
			List<ASTNode> astNodeList) {
		push(defaultStrValue, node,
				astNodeList.toArray(new ASTNode[astNodeList.size()]));
	}

	private void visitList(List<ASTNode> list) {
		for (ASTNode element : list) {
			element.accept(this);
		}
	}
}
