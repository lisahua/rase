package changeassistant.classhierarchy;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import changeassistant.change.group.model.SubTreeModel;
import changeassistant.changesuggestion.expression.representation.AbstractExpressionRepresentationGenerator;
import changeassistant.internal.ASTNodeFinder;
import changeassistant.peers.SourceCodeRange;
import changeassistant.peers.comparison.ASTMethodBodyTransformer;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.comparison.DiffUtil;

public class ClassContext {

	public String packageName;
	public String name;
	private WeakReference<CompilationUnit> cu;
	public String relativeFilePath;
	public Map<String, SourceCodeRange> methodMap;
	public Map<String, SourceCodeRange> fieldMap;
	public Map<String, SoftReference<Node>> cachedMethodNodeMap;
	public Map<String, SoftReference<SubTreeModel>> cachedMethodSubTreeMap;
	public String superName;
	public List<String> interfaces;

	public static boolean Use_Cache_For_SubTree = false;

	public static boolean Use_Cache_For_MethodNode = true;

	public boolean allInterfacesKnown;
	public Set<String> allInterfaces;

	public boolean allSupersKnown;
	public List<String> allSupers;

	public boolean isInterface;

	private ProjectResource pr;

	public ClassContext(String name) {
		this.name = name;
		this.cu = null;
		this.pr = null;
		init();
	}

	public ClassContext(String name, ProjectResource pr, String relativeFilePath) {
		this.name = name;
		this.pr = pr;
		this.relativeFilePath = relativeFilePath;
		init();
	}

	// this constructor is too expensive since all CompilationUnits are stored
	// directly
	// public ClassContext(String name, CompilationUnit cu, ProjectResource pr,
	// String relativeFilePath){
	// this.name = name;
	// this.cu = new WeakReference<CompilationUnit>(cu);
	// this.pr = pr;
	// this.relativeFilePath = relativeFilePath;
	// init();
	// }

	public Set<String> getAllInterfaceNames() {
		if (this.name.isEmpty() || this.pr == null) {
			return this.allInterfaces;
		}
		if (!allInterfacesKnown) {
			lookforAllInterfaceNames();
		}
		return this.allInterfaces;
	}

	public List<String> getAllSuperClassNames() {
		if (this.name.isEmpty() || pr == null)
			return this.allSupers;
		if (!allSupersKnown) {
			lookforAllSuperClassNames();
		}
		return this.allSupers;
	}

	public ClassContext getAncestorClass() {
		if (this.superName.isEmpty())
			return null;
		ClassContext tmpSuperClass = pr.findClassContext(this.superName);
		while (tmpSuperClass.getSuperClass() != null) {
			tmpSuperClass = tmpSuperClass.getSuperClass();
		}
		return tmpSuperClass;
	}

	public CompilationUnit getCU() {
		if (this.cu == null || this.cu.get() == null) {
			cu = new WeakReference<CompilationUnit>(pr.createCU(JavaCore
					.createCompilationUnitFrom(pr.getFile(relativeFilePath))));
		}
		return cu.get();
	}

	/**
	 * It is possible that the return value is null when the method is not found
	 * 
	 * @param methodSignature
	 * @return
	 */
	public ASTNode getMethodAST(String methodSignature) {
		ASTNode methodAST = null;
		// ASTNodeFinder finder = new ASTNodeFinder();
		if (methodMap.containsKey(methodSignature)) {
			methodAST = this.getMethodNode(methodSignature)
					.getMethodDeclaration();
			// finder.lookforASTNode(getCU(), methodMap.get(methodSignature));
		}
		return methodAST;
	}

	public Node getAmbiguousMethodNode(String methodSignature) {
		Node methodNode = null;
		ASTNodeFinder finder = new ASTNodeFinder();
		String name = methodSignature
				.substring(0, methodSignature.indexOf('('));
		if (cachedMethodNodeMap == null) {
			cachedMethodNodeMap = new HashMap<String, SoftReference<Node>>();
		}
		if (cachedMethodNodeMap.containsKey(methodSignature)
				&& cachedMethodNodeMap.get(methodSignature).get() != null) {
			methodNode = cachedMethodNodeMap.get(methodSignature).get();
		} else {
			String key;
			for (Entry<String, SourceCodeRange> entry : methodMap.entrySet()) {
				key = entry.getKey();
				if (key.contains(name)
						&& DiffUtil.simplifySig(key).equals(methodSignature)) {
					ASTNode methodAST = finder.lookforASTNode(getCU(),
							entry.getValue());
					methodNode = new ASTMethodBodyTransformer()
							.createMethodBodyTree(methodAST);
					cachedMethodNodeMap.put(methodSignature,
							new SoftReference<Node>(methodNode));
				}
			}
		}
		if (methodNode == null) {
			return null;
		}
		return (Node) methodNode.deepCopy();
	}

	public Map<String, SourceCodeRange> getFieldMap() {
		return fieldMap;
	}

	public Map<String, SourceCodeRange> getMethodMap() {
		return methodMap;
	}

	/**
	 * return copy of the original method node
	 * 
	 * @param methodSignature
	 * @return
	 */
	public Node getMethodNode(String methodSignature) {
		Node methodNode = null;
		if (Use_Cache_For_MethodNode) {
			if (cachedMethodNodeMap == null) {
				cachedMethodNodeMap = new HashMap<String, SoftReference<Node>>();
			}

			if (cachedMethodNodeMap.containsKey(methodSignature)
					&& cachedMethodNodeMap.get(methodSignature).get() != null) {
				methodNode = cachedMethodNodeMap.get(methodSignature).get();
			} else {
				ASTNodeFinder finder = new ASTNodeFinder();
				if (methodMap.containsKey(methodSignature)) {
					ASTNode methodAST = finder.lookforASTNode(getCU(),
							methodMap.get(methodSignature));
					methodNode = new ASTMethodBodyTransformer()
							.createMethodBodyTree(methodAST);
					cachedMethodNodeMap.put(methodSignature,
							new SoftReference<Node>(methodNode));
					methodAST = null;
				}
			}
			if (methodNode == null) {
				return null;
			}
			return (Node) methodNode.deepCopy();
		} else {
			ASTNodeFinder finder = new ASTNodeFinder();
			ASTNode methodAST = finder.lookforASTNode(getCU(),
					methodMap.get(methodSignature));
			methodNode = new ASTMethodBodyTransformer()
					.createMethodBodyTree(methodAST);
			finder = null;
			methodAST = null;
		}
		return methodNode;
	}

	/**
	 * return the original subTree
	 * 
	 * @param methodSignature
	 * @return
	 */
	public SubTreeModel getMethodSubTree(String methodSignature) {
		SubTreeModel subTree = null;
		if (Use_Cache_For_SubTree) {
			if (cachedMethodSubTreeMap == null) {
				cachedMethodSubTreeMap = new HashMap<String, SoftReference<SubTreeModel>>();
			}

			if (cachedMethodSubTreeMap.containsKey(methodSignature)
					&& cachedMethodSubTreeMap.get(methodSignature).get() != null) {
				subTree = cachedMethodSubTreeMap.get(methodSignature).get();
			} else {
				Node methodNode = getMethodNode(methodSignature);
				subTree = new SubTreeModel(methodNode, methodNode, true,
						new AbstractExpressionRepresentationGenerator());
				cachedMethodSubTreeMap.put(methodSignature,
						new SoftReference<SubTreeModel>(subTree));
			}
		} else {
			Node methodNode = getMethodNode(methodSignature);
			subTree = new SubTreeModel(methodNode, methodNode, true,
					new AbstractExpressionRepresentationGenerator());
		}
		return subTree;
	}

	public ClassContext getSuperClass() {
		if (this.superName.isEmpty())// if the superName is empty, simply return
										// null
			return null;
		return pr.findClassContext(this.superName);
	}

	private void init() {
		fieldMap = new HashMap<String, SourceCodeRange>();
		methodMap = new HashMap<String, SourceCodeRange>();
		interfaces = Collections.emptyList();
		superName = "";
		allInterfacesKnown = false;
		allInterfaces = Collections.emptySet();
		allSupersKnown = false;
		allSupers = Collections.emptyList();
	}

	public boolean isSubClassOf(String className) {
		if (!allSupersKnown) {
			lookforAllSuperClassNames();
		}
		return allSupers.contains(className);
	}

	public boolean isImplementationOf(String className) {
		if (!allInterfacesKnown) {
			lookforAllInterfaceNames();
		}
		return allInterfaces.contains(className);
	}

	/**
	 * This is used to look for all super class names of a class context
	 * 
	 * @return
	 */
	private void lookforAllSuperClassNames() {
		List<String> allSuperClassNames = new ArrayList<String>();
		if (this.superName == "") {// no special super class
			this.allSupersKnown = true;
			return;
		}
		ClassContext cc = getSuperClass();
		if (cc != null && !cc.name.isEmpty()) {
			allSuperClassNames.add(cc.name);
			allSuperClassNames.addAll(cc.getAllSuperClassNames());
		}
		this.allSupers = Collections.unmodifiableList(allSuperClassNames);
		this.allSupersKnown = true;
	}

	private void lookforAllInterfaceNames() {
		Set<String> allInterfaceNames = new HashSet<String>();
		allInterfaceNames.addAll(interfaces);
		// look for interfaces and their super interfaces
		for (String interfaceName : interfaces) {
			ClassContext cc = pr.findClassContext(interfaceName);
			if (cc != null && !cc.name.isEmpty() && cc.pr != null) {
				allInterfaceNames.addAll(cc.getAllInterfaceNames());
			}
		}
		// look for interfaces of the super class
		ClassContext cc = getSuperClass();
		if (cc != null && !cc.name.isEmpty() && cc.pr != null) {
			allInterfaceNames.addAll(cc.getAllInterfaceNames());
		}
		this.allInterfaces = Collections.unmodifiableSet(allInterfaceNames);
		this.allInterfacesKnown = true;
	}

	public String toString() {
		return this.name;
	}
}
