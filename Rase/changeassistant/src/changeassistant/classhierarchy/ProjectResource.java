package changeassistant.classhierarchy;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import changeassistant.internal.WorkspaceUtilities;
import changeassistant.main.ChangeAssistantMain;
import changeassistant.peers.ContextCollector;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.comparison.ChangedMethodADT;

public class ProjectResource {

	IProject iproject;
	IJavaProject javaProject;
	public String projectFullPath;
	public String projectLocation;
	public String projectName;
	private Map<String, PackageResource> packageMap;
	private Map<String, ClassContext> classMap;
	Map<String, ClassContext> libResource;

	private HashMap<String, WeakReference<ClassContext>> cachedClassMap = new HashMap<String, WeakReference<ClassContext>>();
	private HashMap<String, WeakReference<Set<String>>> cachedSuperAndSubs = new HashMap<String, WeakReference<Set<String>>>();
	private HashMap<String, WeakReference<Set<String>>> cachedInterfaceAndImplementations = new HashMap<String, WeakReference<Set<String>>>();

	private void init(IProject iproject) {
		this.packageMap = new HashMap<String, PackageResource>();
		this.classMap = new HashMap<String, ClassContext>();
		this.libResource = new HashMap<String, ClassContext>();
		this.iproject = iproject;
		this.javaProject = JavaCore.create(iproject);
		this.projectFullPath = iproject.getFullPath().toOSString();// like
																	// "\org.eclipse.core.runtime_v20060317"
		this.projectLocation = iproject.getLocation().toOSString();// like
																	// "E:\runtime-EclipseApplication\org.eclipse.core.runtime_v20060317"
	}

	public ProjectResource(IProject iproject, String projectName) {
		this(iproject);
		this.projectName = projectName;
	}

	public ProjectResource(IProject iproject) {
		init(iproject);
		// this.parser = ASTParser.newParser(AST.JLS3);
		// this.parser.setProject(javaProject);
		List<File> javaFiles = new ArrayList<File>();
		if (ChangeAssistantMain.UsePPA) {
			WorkspaceUtilities.collectJavaFiles(javaFiles, new File(
					projectLocation));
			// PPAUtil.getCUs(javaFiles, new PPAOptions(), iproject.getName());
		} else {
			collectClassContexts(javaProject);
		}
	}

	public ProjectResource(IProject iproject, String[] filePaths) {
		init(iproject);
		for (String filePath : filePaths) {
			IFile file = iproject.getFile(filePath);
			List<ClassContext> classContexts = createClassContexts(JavaCore
					.create(file));
			for (ClassContext cc : classContexts) {
				String className = cc.name;
				String packageName = WorkspaceUtilities
						.getPackageName(className);
				if (packageName.isEmpty()) {
					classMap.put(className, cc);
				} else {
					PackageResource pr = packageMap.get(packageName);
					if (pr == null) {
						pr = new PackageResource();
						pr.setPackageName(packageName);
						pr.addClassContext(cc);
						packageMap.put(packageName, pr);
					} else {
						pr.addClassContext(cc);
					}
				}
			}
		}
	}

	/**
	 * This initializer can initialize a projectResource containing only one
	 * Java file to speed up the initialization phase
	 * 
	 * @param iproject
	 * @param filePath
	 */
	public ProjectResource(IProject iproject, String src, String... filePaths) {
		init(iproject);
		for (String filePath : filePaths) {
			IFile file = iproject.getFile(src + File.separator + filePath
					+ ".java");
			List<ClassContext> classContexts = createClassContexts(JavaCore
					.create(file));
			for (ClassContext cc : classContexts) {
				String className = cc.name;
				String packageName = WorkspaceUtilities
						.getPackageName(className);
				if (packageName.isEmpty()) {
					classMap.put(className, cc);
				} else {
					PackageResource pr = packageMap.get(packageName);
					if (pr == null) {
						pr = new PackageResource();
						pr.setPackageName(packageName);
						pr.addClassContext(cc);
						packageMap.put(packageName, pr);
					} else {
						pr.addClassContext(cc);
					}
				}
			}
		}
	}

	public Iterator<ClassContext> classContextIterator() {
		return this.classMap.values().iterator();
	}

	public void findAllImplementingClasses(Set<String> implementations,
			String className) {
		if (cachedInterfaceAndImplementations.containsKey(className)
				&& cachedInterfaceAndImplementations.get(className).get() != null) {
			implementations.addAll(cachedInterfaceAndImplementations.get(
					className).get());
			return;
		}
		findImplementation(implementations, classMap, className);
		Iterator<PackageResource> prIterator = packageMap.values().iterator();
		while (prIterator.hasNext()) {
			PackageResource pr = prIterator.next();
			findImplementation(implementations, pr.getClassContexts(),
					className);
		}

		cachedInterfaceAndImplementations.put(className,
				new WeakReference<Set<String>>(implementations));
	}

	public Set<ChangedMethodADT> findAllMethods() {
		Set<ChangedMethodADT> result = new HashSet<ChangedMethodADT>();
		Iterator<PackageResource> prIterator = packageMap.values().iterator();
		Iterator<ClassContext> ccIterator;
		ClassContext cc;
		String classname;
		Set<String> methodSet;
		while (prIterator.hasNext()) {
			PackageResource pr = prIterator.next();
			ccIterator = pr.getClassContextIterator();
			while (ccIterator.hasNext()) {
				cc = ccIterator.next();
				classname = cc.name;
				methodSet = cc.methodMap.keySet();
				for (String mName : methodSet) {
					result.add(new ChangedMethodADT(classname, mName));
				}
			}
		}

		ccIterator = this.classContextIterator();
		while (ccIterator.hasNext()) {
			cc = ccIterator.next();
			classname = cc.name;
			methodSet = cc.methodMap.keySet();
			for (String mName : methodSet) {
				result.add(new ChangedMethodADT(classname, mName));
			}
		}
		return result;
	}

	public Set<ChangedMethodADT> findAllMethodsWithSimilarName(
			ChangedMethodADT knownADT) {
		Set<ChangedMethodADT> result = new HashSet<ChangedMethodADT>();
		Iterator<PackageResource> prIterator = packageMap.values().iterator();
		Iterator<ClassContext> ccIterator;
		ClassContext cc;
		Set<String> methodSet;
		String knownMethodName = ChangedMethodADT
				.getMethodName2(knownADT.methodSignature);
		String tmpMethodName;
		while (prIterator.hasNext()) {
			PackageResource pr = prIterator.next();
			ccIterator = pr.getClassContextIterator();
			while (ccIterator.hasNext()) {
				cc = ccIterator.next();
				methodSet = cc.methodMap.keySet();
				for (String mName : methodSet) {
					tmpMethodName = ChangedMethodADT.getMethodName2(mName);
					if (ContextCollector.isSimilarMethodName(tmpMethodName,
							knownMethodName)) {
						result.add(new ChangedMethodADT(cc.name, mName));
					}
				}
			}
		}
		return result;
	}

	public Set<ChangedMethodADT> findAllMethodsWithSimilarStructure(
			ChangedMethodADT knownADT) {
		Set<ChangedMethodADT> result = new HashSet<ChangedMethodADT>();
		ClassContext cc = this.findClassContext(knownADT.classname);
		Node knownContext = cc.getMethodNode(knownADT.methodSignature);
		Iterator<PackageResource> prIterator = packageMap.values().iterator();
		Node canContext;
		Iterator<ClassContext> ccIterator;
		Set<String> methodSet;
		while (prIterator.hasNext()) {
			PackageResource pr = prIterator.next();
			ccIterator = pr.getClassContextIterator();
			while (ccIterator.hasNext()) {
				cc = ccIterator.next();
				methodSet = cc.methodMap.keySet();
				for (String mName : methodSet) {
					canContext = cc.getMethodNode(mName);
					if (ContextCollector.isSimilarMethodStructure(knownContext,
							canContext)) {
						result.add(new ChangedMethodADT(cc.name, mName));
					}
				}
			}
		}
		result.remove(knownADT);
		return result;
	}

	public void findAllSubClasses(Set<String> subs, String className) {
		if (cachedSuperAndSubs.containsKey(className)
				&& cachedSuperAndSubs.get(className).get() != null) {
			subs.addAll(cachedSuperAndSubs.get(className).get());
			return;
		}
		findSubClass(subs, classMap, className);
		Iterator<PackageResource> prIterator = packageMap.values().iterator();
		while (prIterator.hasNext()) {
			PackageResource pr = prIterator.next();
			findSubClass(subs, pr.getClassContexts(), className);
		}

		cachedSuperAndSubs.put(className, new WeakReference<Set<String>>(subs));
	}

	public List<ClassContext> findClassContexts(String relativeFilePath) {
		List<ClassContext> ccs = new ArrayList<ClassContext>();
		String[] segs = relativeFilePath.split("/");
		StringBuffer buffer = new StringBuffer();
		// assumption: the first one is the file folder, starting with
		// "/"--which should not be considered in package name
		for (int i = 2; i < segs.length - 1; i++) {
			buffer.append(segs[i]).append(".");
		}
		buffer.setLength(buffer.length() - 1);
		String packageName = buffer.toString();
		PackageResource pacResource = this.findPackageResource(packageName);
		Iterator<ClassContext> ccIter = pacResource.classContextIterator();
		ClassContext cc = null;
		while (ccIter.hasNext()) {
			cc = ccIter.next();
			if (cc.relativeFilePath.equals(relativeFilePath)) {
				ccs.add(cc);
			}
		}
		return ccs;
	}

	public Set<String> getAllPeerClasses(String className) {// do not include
															// the class itself
		Set<String> peers = new HashSet<String>();
		ClassContext cc = findClassContext(className);
		Set<String> supers = new HashSet<String>(cc.getAllSuperClassNames());
		if (!cc.isInterface)
			supers.add(className);
		for (String sName : supers) {
			findAllSubClasses(peers, sName);
		}
		supers = cc.getAllInterfaceNames();
		if (cc.isInterface)
			supers.add(className);
		for (String sName : supers) {
			findAllImplementingClasses(peers, sName);
		}
		peers.remove(className);
		return peers;
	}

	public IFile getFile(String relativeFilePath) {
		return iproject.getFile(relativeFilePath);
	}

	public IProject getIProject() {
		return this.iproject;
	}

	public Iterator<PackageResource> packageResourceIterator() {
		return this.packageMap.values().iterator();
	}

	private void collectClassContexts(IJavaElement javaElement) {
		if (javaElement.getElementType() == IJavaElement.COMPILATION_UNIT) {
			// one compilation unit may correspond to multiple class contexts
			List<ClassContext> classContexts = createClassContexts(javaElement);
			for (ClassContext cc : classContexts) {
				String className = cc.name;
				String packageName = WorkspaceUtilities
						.getPackageName(className);
				if (packageName.isEmpty()) {
					classMap.put(className, cc);
				} else {
					PackageResource pr = packageMap.get(packageName);
					if (pr == null) {
						pr = new PackageResource();
						pr.setPackageName(packageName);
						pr.addClassContext(cc);
						packageMap.put(packageName, pr);
					} else {
						pr.addClassContext(cc);
					}
				}
			}
		} else if (javaElement instanceof IParent) {// no compilation unit, then
													// further traversed
			IParent parent = (IParent) javaElement;
			// do not traverse Package_Fragment_Roots that are ReadOnly
			// to ignore libraries and .class files
			if (javaElement.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT
					&& javaElement.isReadOnly()) {
				return;
			}
			// Traverse
			try {
				if (parent.hasChildren()) {
					IJavaElement[] children = parent.getChildren();
					for (int i = 0; i < children.length; i++) {
						collectClassContexts(children[i]);
					}
				}
			} catch (JavaModelException jme) {
				System.out.println("Problem traversing Java model element: "
						+ parent);
				jme.printStackTrace();
			}
		} else {
			System.out
					.println("Encountered a model element that's not a comp unit or parent: "
							+ javaElement);
		}
	}

	private List<ClassContext> createClassContexts(IJavaElement javaElement) {
		ClassContextVisitor ccv = new ClassContextVisitor(this,
				javaElement.getPath());
		createCU(javaElement).accept(ccv);
		return ccv.getClassContexts();
	}

	public CompilationUnit createCU(IJavaElement javaElement) {
		if (ChangeAssistantMain.UsePPA) {
			// CompilationUnit cu = PPAUtil.getCU(javaElement.getResource()
			// .getLocation().toFile(), new PPAOptions());
			// return cu;
			return null;// dummy return result
		} else {
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setResolveBindings(true);
			parser.setSource((ICompilationUnit) javaElement);
			CompilationUnit cu = (CompilationUnit) parser.createAST(null);
			return cu;
		}
	}

	/**
	 * This method may return null when the class does not exist at all in the
	 * project
	 * 
	 * @param className
	 * @return
	 */
	public ClassContext findClassContext(String className) {
		String standardClassName = className.replace('/', '.');
		// first: look for the classContext in cache
		if (cachedClassMap.get(standardClassName) != null
				&& cachedClassMap.get(standardClassName).get() != null)
			return cachedClassMap.get(standardClassName).get();

		// second: try to find out the context from the whole hierarchy
		ClassContext cc = null;
		// this is buggy, since it is possible that a className is a.b.C.D
		String packageName = WorkspaceUtilities.getPackageName(className);
		if (packageName.isEmpty()) {
			cc = classMap.get(className);
		} else {
			if (packageMap.get(packageName) == null) {
				// do nothing, since the package is not in the project
			} else if (packageMap.get(packageName).getClassContext(className) == null) {
				return null;// the class context is not found in the designated
							// package, because it does not exist
			} else {
				cc = packageMap.get(packageName).getClassContext(className);
			}
		}
		// cc is not contained in the project, so it should be a referenced
		// classContext
		if (cc == null) {
			if (libResource.get(standardClassName) != null) {
				cc = libResource.get(standardClassName);
			} else {
				cc = new ClassContext(standardClassName);
				libResource.put(standardClassName, cc);
			}
		}
		cachedClassMap.put(standardClassName, new WeakReference<ClassContext>(
				cc));
		return cc;
	}

	public PackageResource findPackageResource(String prName) {
		return this.packageMap.get(prName);
	}

	private void findImplementation(Set<String> subs,
			Map<String, ClassContext> classMap, String className) {
		Iterator<ClassContext> ccIterator = classMap.values().iterator();
		while (ccIterator.hasNext()) {
			ClassContext cc = ccIterator.next();
			if (!cc.isInterface && cc.isImplementationOf(className)) {
				subs.add(cc.name);
			}
		}
	}

	private void findSubClass(Set<String> subs,
			Map<String, ClassContext> classMap, String className) {
		Iterator<ClassContext> ccIterator = classMap.values().iterator();
		while (ccIterator.hasNext()) {
			ClassContext cc = ccIterator.next();
			// with this condition (cc.name == className), we also care about
			// the super class itself
			if (cc.name == className || cc.isSubClassOf(className)) {
				subs.add(cc.name);
			}
		}
	}

	public Map<String, ClassContext> getClassMap() {
		return classMap;
	}
}
