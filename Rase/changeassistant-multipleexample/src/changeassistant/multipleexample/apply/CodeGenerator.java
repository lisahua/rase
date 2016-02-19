package changeassistant.multipleexample.apply;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.multipleexample.main.Constants;
import changeassistant.multipleexample.match.ProgramTransformationGenerator;
import changeassistant.multipleexample.partition.SimpleASTCreator;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.peers.comparison.ASTMethodBodyTransformer;
import changeassistant.peers.comparison.Node;

public class CodeGenerator {

	public static void main(String[] args){
		StringBuffer buffer = new StringBuffer();
		buffer.append("void m(){\n");
		buffer.append("fMemberTable.addSelectionListener(\n");
		buffer.append("new SelectionAdapter(){\n");
		buffer.append("public void widgetSelected(SelectionEvent e) {\n");
		buffer.append("if(e.detail==SWT.CHECK){\n");
		buffer.append("if(e.item instanceof TableItem){\n");
		buffer.append("TableItem ti=(TableItem)e.item;\n");
		buffer.append("}\n");
		buffer.append("}\n");
		buffer.append("handleMemberSelect(e.item);\n");
		buffer.append("}\n");
		buffer.append("});\n");
		buffer.append("}\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		buffer.append("\n");
		TypeDeclaration astNode = (TypeDeclaration)create(buffer.toString(), ASTParser.K_CLASS_BODY_DECLARATIONS);
		MethodDeclaration md = astNode.getMethods()[0];
		ASTMethodBodyTransformer transformer = new ASTMethodBodyTransformer();
		Node node = transformer.createMethodBodyTree(md);
		SimpleTreeNode sTree = new SimpleTreeNode(node, true, 1);		
		SimpleASTCreator sCreator = new SimpleASTCreator();
		List<List<SimpleASTNode>> sNodesList = sCreator.createSimpleASTNodesList(node);
		sTree = ProgramTransformationGenerator.normalizeTree(sTree, sNodesList);
		Enumeration<SimpleTreeNode> sEnum = sTree.children();
		List<SimpleTreeNode> list = new ArrayList<SimpleTreeNode>();
		while(sEnum.hasMoreElements()){
			list.add(sEnum.nextElement());
		}
		String str = CodeGenerator.createCodeString(list);
		System.out.println(str);
//		System.out.println(create(buffer.toString(), ASTParser.K_CLASS_BODY_DECLARATIONS));
	}
	
	public static String createCodeString(List<SimpleTreeNode> sTrees){
		StringBuffer buffer = new StringBuffer();
		for (SimpleTreeNode sTree : sTrees) {
			createCode(sTree, buffer);
		}
		return buffer.toString();
	}
	
	public static ASTNode create(List<SimpleTreeNode> sTrees, int kind) {
		StringBuffer buffer = new StringBuffer();
		for (SimpleTreeNode sTree : sTrees) {
			createCode(sTree, buffer);
		}
		return create(buffer.toString(), kind);
	}

	public static ASTNode create(SimpleTreeNode sTree, int kind) {
		StringBuffer buffer = new StringBuffer();
//		System.out.print("");
		createCode(sTree, buffer);
		return create(buffer.toString(), kind);
	}

	public static ASTNode create(String string, int kind) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(string.toCharArray());
		parser.setKind(kind);
		ASTNode astNode = parser.createAST(null);
		return astNode;
	}

	public static void createCode(SimpleTreeNode sTree2, StringBuffer buffer) {
		Enumeration<SimpleTreeNode> cEnum = null;
		String str = sTree2.getStrValue();
		if (str.equals("then") ||str.equals("then:") || str.equals("else") || str.equals("else:")
				|| str.equals("method declaration") || str.equals("method declaration:") || str.equals("finally")
				||str.equals("finally:") || str.equals("anonyClass") || str.equals("anonyClass:")) {
			if (str.equals("else") || str.equals("finally")) {
				buffer.append(str);
				if (sTree2.getNodeType() != ASTNode.IF_STATEMENT) {
					buffer.append("{\n");
				} else {
					buffer.append(" ");
				}
			} else {
				buffer.append("{\n");
			}
			cEnum = sTree2.children();
			while (cEnum.hasMoreElements()) {
				createCode(cEnum.nextElement(), buffer);
			}
			if (!(sTree2.getStrValue().equals("else") && sTree2.getNodeType() == ASTNode.IF_STATEMENT)) {
				buffer.append("}\n");
			}
			return;
		}

		if (str.equals("try-body")) {
			cEnum = sTree2.children();
			while (cEnum.hasMoreElements()) {
				createCode(cEnum.nextElement(), buffer);
			}
			buffer.append("}\n");
			return;
		}

		switch (sTree2.getNodeType()) {
		case ASTNode.DO_STATEMENT:
			buffer.append("do{\n");
			cEnum = sTree2.children();
			while (cEnum.hasMoreElements()) {
				createCode(cEnum.nextElement(), buffer);
			}
			buffer.append("}while(").append(sTree2.getStrValue())
					.append(");\n");
			break;
		case ASTNode.EMPTY_STATEMENT:
			buffer.append(";\n");
			break;
		case ASTNode.CATCH_CLAUSE:
		case ASTNode.ENHANCED_FOR_STATEMENT:
		case ASTNode.FOR_STATEMENT:
		case ASTNode.LABELED_STATEMENT:
		case ASTNode.SWITCH_STATEMENT:
		case ASTNode.SYNCHRONIZED_STATEMENT:
		case ASTNode.TRY_STATEMENT:
		case ASTNode.WHILE_STATEMENT:
			buffer.append(sTree2.getStrValue()).append("{\n");
			cEnum = sTree2.children();
			while (cEnum.hasMoreElements()) {
				createCode(cEnum.nextElement(), buffer);
			}
			if (sTree2.getNodeType() != ASTNode.TRY_STATEMENT)
				buffer.append("}\n");
			break;
		case ASTNode.IF_STATEMENT:
			buffer.append(sTree2.getStrValue());
			cEnum = sTree2.children();
			while (cEnum.hasMoreElements()) {
				createCode(cEnum.nextElement(), buffer);
			}
			break;
		case ASTNode.METHOD_DECLARATION:
			if (sTree2.getStrValue().contains("{")) {
				buffer.append(sTree2.getStrValue().substring(0,
						sTree2.getStrValue().indexOf('{')));
			} else {
				buffer.append(sTree2.getStrValue());
			}
			buffer.append("{\n");
			cEnum = sTree2.children();
			while (cEnum.hasMoreElements()) {
				createCode(cEnum.nextElement(), buffer);
			}
			buffer.append("}\n");
			break;
		case ASTNode.ASSERT_STATEMENT:
		case ASTNode.BREAK_STATEMENT:
		case ASTNode.CONSTRUCTOR_INVOCATION:
		case ASTNode.CONTINUE_STATEMENT:
		case ASTNode.RETURN_STATEMENT:
		case ASTNode.SUPER_CONSTRUCTOR_INVOCATION:
			buffer.append(sTree2.getStrValue());
			if (!sTree2.getStrValue().endsWith(";"))
				buffer.append(";");
			buffer.append("\n");
			break;
		case ASTNode.SWITCH_CASE:
		case ASTNode.THROW_STATEMENT:
		case ASTNode.TYPE_DECLARATION_STATEMENT:
			buffer.append(sTree2.getStrValue());
			buffer.append("\n");
			break;
		case ASTNode.EXPRESSION_STATEMENT:
		case ASTNode.VARIABLE_DECLARATION_STATEMENT:
			if (Term.U_Pattern.matcher(sTree2.getStrValue()).matches()) {
				buffer.append("/*");
			}
			buffer.append(sTree2.getStrValue());
			if (sTree2.getChildCount() != 0) {
				cEnum = sTree2.children();
				while (cEnum.hasMoreElements()) {
					SimpleTreeNode child = cEnum.nextElement();
					if (child.getNodeType() == ASTNode.ANONYMOUS_CLASS_DECLARATION) {
						String tmp = sTree2.getStrValue();
						int locationOfNew = tmp.lastIndexOf("new");
						do {
							int counterOfLeft = 0, counterOfRight = 0;
							for (int i = tmp.length() - 1; i >= locationOfNew
									+ "new".length(); i--) {
								if (tmp.charAt(i) == '(')
									counterOfLeft++;
								else if (tmp.charAt(i) == ')')
									counterOfRight++;
							}
							int extraRight = counterOfRight - counterOfLeft;
							if (extraRight > 0) {
								if (counterOfLeft != 0) {
									buffer.setLength(buffer.length()
											- extraRight);
									createCode(child, buffer);
									for (int i = 0; i < extraRight; i++) {
										buffer.append(')');
									}
									break;
								} else {
									String tmp2 = tmp.substring(0,
											locationOfNew);
									locationOfNew = tmp2.lastIndexOf("new");
								}
							} else {
								createCode(child, buffer);
								break;
							}
						} while (true);
					} else {
						createCode(child, buffer);
					}
				}
			}
			if (!sTree2.getStrValue().endsWith(";"))
				buffer.append(";");
			if (Term.U_Pattern.matcher(sTree2.getStrValue()).matches()) {
				buffer.append("*/");
			}
			buffer.append("\n");
			break;
		default:
			System.out.print("Need more process!");
			break;
		}
	}
}
