package changeassistant.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import changeassistant.change.group.model.MatchingInfo;
import changeassistant.change.group.model.SubTreeModel;
import changeassistant.changesuggestion.expression.representation.MethodNameTerm;
import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.TypeNameTerm;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;
import changeassistant.main.ChangeAssistantMain;
import changeassistant.peers.PeerFinder2;

public class WorkspaceUtilities {

	private static final int LIMIT = 10;

	private static Map<ICompilationUnit, ASTNode> knownMap = new HashMap<ICompilationUnit, ASTNode>(
			LIMIT);

	public static final String SEPARATOR_BETWEEN_PARAMETERS = ", ";

	public static void collectJavaFiles(List<File> javaFiles, File directory) {
		File[] files = directory.listFiles();
		File temp = null;
		int size = files.length;
		// to traverse all files in the directory
		for (int i = 0; i < size; i++) {
			temp = files[i];
			if (temp.isDirectory()) {// if this is a directory, call it
										// iteratively
				collectJavaFiles(javaFiles, temp);
			} else if (temp.getName().endsWith(".java")) {// if this is a java
															// file, add it
				javaFiles.add(temp);
			}
		}
	}

	public static List<ICompilationUnit> collectCompilationUnits(
			IJavaElement javaElement) {
		List<ICompilationUnit> list = null, temp = null;
		// CompilationUnit: just return it with the list
		if (javaElement.getElementType() == IJavaElement.COMPILATION_UNIT) {
			list = new ArrayList<ICompilationUnit>();
			list.add((ICompilationUnit) javaElement);
			return list;
		}

		// Non-CompilationUnit: traverse inside recursively
		if (javaElement instanceof IParent) {
			IParent parent = (IParent) javaElement;

			// Do not traverse PACKAGE_FRAGMENT_ROOTs to ignore libraries and
			// .class files
			if (javaElement.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT
					&& javaElement.isReadOnly())
				return null;

			try {
				if (parent.hasChildren()) {
					IJavaElement[] children = parent.getChildren();
					for (int i = 0; i < children.length; i++) {
						temp = collectCompilationUnits(children[i]);
						if (temp != null)
							if (list == null)
								list = temp;
							else
								list.addAll(temp);
					}
				}
			} catch (JavaModelException jme) {
				jme.printStackTrace();
			}
		} else {
			System.out.println("Encountered a model element"
					+ "which is neither a CompilationUnit nor parent!");
		}
		return list;
	}

	public static void copyFile(IPath srcPath, IPath desPath) {
		try {
			File file = new File(srcPath.toOSString());
			FileInputStream in = new FileInputStream(file);
			FileOutputStream out = new FileOutputStream(desPath.toOSString());
			byte[] buffer = new byte[1024];
			int byteread = 0;
			while ((byteread = in.read(buffer)) != -1) {
				out.write(buffer, 0, byteread);
			}
			in.close();
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ASTNode getASTNodeFromCompilationUnit(ICompilationUnit icu) {
		if (ChangeAssistantMain.UsePPA) {
			// if(knownMap.containsKey(icu))
			// return knownMap.get(icu);
			// ASTNode cu = PPAUtil.getCU(((IFile)
			// icu.getResource()).getLocation().toFile(),
			// new PPAOptions());
			// if(knownMap.size() >= LIMIT){
			// knownMap.clear();
			// }
			// knownMap.put(icu, cu);
			// return cu;
			return null;// dummy returned result
		} else {
			if (knownMap.containsKey(icu))
				return knownMap.get(icu);

			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setResolveBindings(true);
			parser.setSource(icu);
			ASTNode cu = parser.createAST(null);
			if (knownMap.size() >= LIMIT) {
				knownMap.clear();
			}
			knownMap.put(icu, cu);
			return cu;
		}
	}

	/**
	 * The method name is the unique ID for the method
	 * 
	 * @param node
	 * @return
	 */
	public static String getMethodSignatureFromASTNode(MethodDeclaration node) {
		StringBuffer name = new StringBuffer(node.getName().getIdentifier());
		name.append("(");
		List<SingleVariableDeclaration> parameters = node.parameters();
		int numOfComma = node.parameters().size() - 1;
		int counter = 0;
		for (SingleVariableDeclaration parameter : parameters) {
			String typeName = parameter.getType().toString();
			name.append(typeName);
			if (counter++ < numOfComma) {
				name.append(SEPARATOR_BETWEEN_PARAMETERS);
			}
		}
		name.append(")");
		return name.toString();
	}

	public static String getSimpleClassName(String wholeClassName,
			String packageName) {
		if (!packageName.isEmpty())
			return wholeClassName.substring(packageName.length() + 1);
		else
			return wholeClassName;
	}

	public static String getSimpleFileName(String filePath) {
		int index = filePath.lastIndexOf("/");
		String fileName = null;
		if (index >= 0) {
			fileName = filePath.substring(index + 1);
		} else {
			fileName = filePath;
		}
		return fileName;
	}

	public static String getPackageName(String className) {
		String result = "";
		if (!className.contains("."))
			return result;
		int index = -1;
		StringTokenizer st = new StringTokenizer(className, ".");
		while (st.hasMoreElements()) {
			String temp = (String) st.nextElement();
			if (temp.charAt(0) >= 'A' && temp.charAt(0) <= 'Z') {
				if (index == -1)
					result = "";
				else
					result = className.substring(0, index);
				break;
			} else {
				index += temp.length() + 1;
			}
		}
		return result;
	}

	public static String getSimpleName(String className) {
		String simpleName = className;
		int size = 0;
		if (simpleName.contains(".")) {
			StringTokenizer st = new StringTokenizer(simpleName, ".");
			size = st.countTokens();
			for (int i = 0; i < size - 1; i++) {
				st.nextElement();
			}
			simpleName = (String) st.nextElement();// the last token in a string
		} else {
			// do nothing
		}
		return simpleName;
	}

	public static boolean isMatched(MatchingInfo matchingInfo,
			SubTreeModel commonSubTree) {
		if (PeerFinder2.abstractMethod && PeerFinder2.abstractType
				&& PeerFinder2.abstractVariable)
			return true;
		Enumeration<SubTreeModel> enumeration = commonSubTree
				.breadthFirstEnumeration();
		List<List<Term>> termsList;
		List<Term> terms;
		Term term;
		SubTreeModel node;
		while (enumeration.hasMoreElements()) {
			node = enumeration.nextElement();
			if (node.getStrValue().equals("method declaration"))
				continue;
			termsList = node.getAbstractExpressions();
			for (int i = 0; i < termsList.size(); i++) {
				terms = termsList.get(i);
				for (int j = 0; j < terms.size(); j++) {
					term = terms.get(j);
					switch (term.getTermType()) {
					case TypeNameTerm: {
						if (PeerFinder2.abstractType)
							continue;
						TypeNameTerm tTerm1 = (TypeNameTerm) term;
						Map<TypeNameTerm, TypeNameTerm> typeMap = matchingInfo
								.getTypeMap();
						for (Entry<TypeNameTerm, TypeNameTerm> entry : typeMap
								.entrySet()) {
							try {
								if (entry.getValue().getName()
										.equals(tTerm1.getName())) {
									if (entry.getKey().getName()
											.equals(tTerm1.getName())) {
										break;
									} else {
										return false;
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
						break;
					case MethodNameTerm: {
						if (PeerFinder2.abstractMethod)
							continue;
						MethodNameTerm mTerm1 = (MethodNameTerm) term;
						Map<MethodNameTerm, MethodNameTerm> methodMap = matchingInfo
								.getMethodMap();
						for (Entry<MethodNameTerm, MethodNameTerm> entry : methodMap
								.entrySet()) {
							try {
								if (entry.getValue().getName()
										.equals(mTerm1.getName())) {
									if (entry.getKey().getName()
											.equals(mTerm1.getName())) {
										break;
									} else {
										return false;
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
						break;
					case VariableTypeBindingTerm: {
						if (PeerFinder2.abstractVariable)
							continue;
						VariableTypeBindingTerm vTerm1 = (VariableTypeBindingTerm) term;
						Map<VariableTypeBindingTerm, VariableTypeBindingTerm> variableMap = matchingInfo
								.getVariableMap();
						for (Entry<VariableTypeBindingTerm, VariableTypeBindingTerm> entry : variableMap
								.entrySet()) {
							try {
								if (entry.getValue().getName()
										.equals(vTerm1.getName())) {
									if (entry.getKey().getName()
											.equals(vTerm1.getName())) {
										break;
									} else {
										return false;
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
						break;
					}
				}
			}
		}
		return true;
	}
}
