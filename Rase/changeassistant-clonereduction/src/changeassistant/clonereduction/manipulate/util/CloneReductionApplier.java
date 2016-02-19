package changeassistant.clonereduction.manipulate.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;
import changeassistant.classhierarchy.ClassContext;
import changeassistant.classhierarchy.ProjectResource;
import changeassistant.clonereduction.datastructure.ExtractedMethodFeature;
import changeassistant.clonereduction.datastructure.MethodRelation;
import changeassistant.clonereduction.datastructure.Wildcard;
import changeassistant.clonereduction.helper.FeatureHelper;
import changeassistant.clonereduction.main.CloneReductionMain;
import changeassistant.clonereduction.manipulate.refactoring.RefactoringMetaData;
import changeassistant.clonereduction.manipulate.refactoring.ReturnObjectCreator;
import changeassistant.clonereduction.manipulate.refactoring.TemplateClassHierarchyCreator;
import changeassistant.clonereduction.search.CloneReductionMatchResult;
import changeassistant.internal.ASTMethodFinder;
import changeassistant.multipleexample.apply.EditScriptApplier;
import changeassistant.multipleexample.datastructure.Pair;
import changeassistant.multipleexample.main.Oracle;
import changeassistant.multipleexample.partition.datastructure.SimpleTreeNode;
import changeassistant.multipleexample.util.PathUtil;
import changeassistant.peers.comparison.Node;
import changeassistant.versions.comparison.ChangedMethodADT;
import changeassistant.versions.treematching.edits.ITreeEditOperation.EDIT;

public class CloneReductionApplier {

	boolean DEBUG = true;

	List<SimpleTreeNode> oNodes;
	List<ChangedMethodADT> adts;
	String methodString;
	MethodRelation rel;
	ExtractedMethodFeature feature;
	Wildcard wData;
	Set<Node> flowNodes;
	List<CloneReductionMatchResult> matchResults;	

	BufferedWriter output;
	ProjectResource pr;
	
	public Set<VariableTypeBindingTerm> outputTerms;
	public Set<Node> returnNodes;
	
	private ICompilationUnit icu;
	private IType typeToEdit;
	private ASTMethodFinder finder;
	private ASTParser parser;
	private Oracle oracle;

	public CloneReductionApplier(ProjectResource pr, Wildcard wData) {
		finder = new ASTMethodFinder();
		parser = ASTParser.newParser(AST.JLS3);
		this.pr = pr;
		this.wData = wData;
	}

	public void manipulate(List<SimpleTreeNode> oNodes,
			List<ChangedMethodADT> adts,
			MethodRelation rel, String methodString, Oracle oracle)
			throws Exception {
		this.rel = rel;
		this.adts = adts;
		this.methodString = methodString;
		this.oracle = oracle;
		this.oNodes = oNodes;	

		String path = PathUtil.createPath("tmp.java");
		FeatureHelper fHelper = new FeatureHelper(pr);
		feature = fHelper.inferFeatures(adts);
		try {
			output = new BufferedWriter(new FileWriter(
					new File(path.toString())));
			if(RefactoringMetaData.isNeedRetObj() || RefactoringMetaData.isNeedExitFlags()){
				ReturnObjectCreator roCreator = new ReturnObjectCreator();
				roCreator.createReturnObject(oracle);
			}
			if (rel.inSameClass) {
				if(RefactoringMetaData.isNeedTemplateClass()){
					createTemplateAndModifySameCU();
				}else{
					modifySameCU();
				}				
			} else if (rel.hasSameSuperClass) {
				if(RefactoringMetaData.isNeedTemplateClass()){
					createTemplateAndModifyCus();
				}else{
					modifyDifferentCU();
				}				
			} else if(rel.outerAndInnerClass){
				modifySameFile(rel.outerClassName);
			} else {
				createTemplateAndModifyCus();
			}
			output.close();
			CloneReductionMain.refEdits.print();			
			oracle.checkDelta(Integer.toString(CloneReductionMain.deltaCounter.print()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String createExtractedMethodString( String... args) throws Exception {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < args.length; i++) {
			buffer.append(args[i]);
		}
		if ((feature.modifiers & Modifier.STATIC) != 0 && buffer.indexOf("static") == -1) {
			buffer.append("static ");
		}
		buffer.append(methodString);
		String methodString = buffer.toString();
		return methodString;
	}

	private void prepare(ClassContext cc) throws JavaModelException {
		icu = JavaCore.createCompilationUnitFrom(pr
				.getFile(cc.relativeFilePath));
		icu = icu.getWorkingCopy(null);
		typeToEdit = findTypeToEdit(icu, cc.name);
	}

	private void createTemplate() throws Exception {
		String tmpMethodString = null;
		if(RefactoringMetaData.isNeedTemplateHierarchy()){
			tmpMethodString = createExtractedMethodString("public ");
		}else{
			tmpMethodString = createExtractedMethodString("public static ");
		}
		TemplateClassHierarchyCreator tcCreator = new TemplateClassHierarchyCreator();
		String templateClasses = tcCreator.create(tmpMethodString);	
		oracle.checkExtractedMethod(templateClasses);
		output.write(templateClasses);
		System.out.println(templateClasses);
	}
	
	private void createTemplateAndModifyCus() throws Exception {
		createTemplate();		
		ChangedMethodADT adt = null;
		ClassContext cc = null;
		for (int i = 0; i < adts.size(); i++) {
			adt = adts.get(i);
			cc = pr.findClassContext(adt.classname);
			prepare(cc);
			modifyMethod(i, adt, oNodes.get(i));
			clear();
		}
	}
	
	private void createTemplateAndModifySameCU() throws Exception{
		createTemplate();
		ChangedMethodADT adt = adts.get(0);
		ClassContext cc = pr.findClassContext(adt.classname);
		prepare(cc);	
		for(int i = 0; i < adts.size(); i++){	
			modifyMethod(i, adts.get(i), oNodes.get(i));			
		}
		clear();	
	}
	
	/**
	 * create a map from the original identifier to the new identifier
	 * @param ids
	 * @return
	 */
	private List<Pair<String>> createPairList(Set<String> ids){
		List<Pair<String>> pairList = new ArrayList<Pair<String>>();
		if(ids.isEmpty())
			return pairList;
		String id = ids.iterator().next();
		String prefix = null;
		if(Term.M_Pattern.matcher(id).find()){
			prefix = "m";
		}else if(Term.T_Pattern.matcher(id).find()){
			prefix = "T";
		}
		int counter = 0;
		for(String tmpId : ids){
			pairList.add(new Pair<String>(tmpId, prefix + Integer.toString(counter++)));
		}
		return pairList;
	}

	private void modifyDifferentCU() throws Exception {
		String tmpMethodString = createExtractedMethodString("protected ");
		System.out.println(tmpMethodString);
		oracle.checkExtractedMethod(tmpMethodString);
		ChangedMethodADT adt = null;
		ClassContext cc = null;
		boolean inserted = false;
		String superName = rel.supers.get(0);
		try {
			for (int i = 0; i < adts.size(); i++) {
				adt = adts.get(i);
				cc = pr.findClassContext(adt.classname);
				prepare(cc);
				if (cc.name.equals(superName)) {
					typeToEdit.createMethod(tmpMethodString, null, false, null);
					inserted = true;
				}
				modifyMethod(i, adt, oNodes.get(i));
				clear();
			}
			if (!inserted) {
				cc = pr.findClassContext(superName);
				prepare(cc);
				typeToEdit.createMethod(tmpMethodString, null, false, null);
				clear();
			}
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void modifySameCU() throws Exception {
		ChangedMethodADT adt = adts.get(0);
		ClassContext cc = pr.findClassContext(adt.classname);
		try {
			prepare(cc);
			// method string for the specific scenario
			String tmpMethodString = createExtractedMethodString("private ");
			System.out.println(tmpMethodString);
			oracle.checkExtractedMethod(tmpMethodString);
			typeToEdit.createMethod(tmpMethodString, null, false, null);
			for (int i = 0; i < adts.size(); i++) {
				modifyMethod(i, adts.get(i), oNodes.get(i));
			}
			clear();
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void modifySameFile(String outerClassName) throws Exception{
		ChangedMethodADT adt = null;
		ClassContext cc = null;
		try{
			for(int i = 0; i < adts.size(); i++){
				adt = adts.get(i);
				cc = pr.findClassContext(adt.classname);
			    prepare(cc);
				if(adt.classname.equals(outerClassName)){
					String tmpMethodString = createExtractedMethodString("private ");
					System.out.println(tmpMethodString);
					oracle.checkExtractedMethod(tmpMethodString);
					typeToEdit.createMethod(tmpMethodString, null, false, null);
				}
				modifyMethod(i, adt, oNodes.get(i));
				clear();
			}
		} catch (JavaModelException e){
			e.printStackTrace();
		}
	}

	private void clear() throws JavaModelException {
		try {
			output.write(icu.getSource());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		icu.discardWorkingCopy();
	}

	protected void modifyMethod(int index, ChangedMethodADT adt,
			SimpleTreeNode oNode) throws Exception {
		parser.setSource(icu);
		parser.setResolveBindings(true);
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		MethodDeclaration md = finder.lookforMethod(cu, adt);

		int startPos = md.getStartPosition();
		int endPos = startPos + md.getLength();

		for (IMethod method : typeToEdit.getMethods()) {
			ISourceRange range = method.getSourceRange();
			int offset = range.getOffset();
			int length = range.getLength();
			if (offset <= startPos && offset + length >= endPos) {
				String tmpMethodString = method.getSource();
				method.delete(false, null);
				System.out.print("");
				tmpMethodString = EditScriptApplier
						.createMethodDeclarationString(oNode, tmpMethodString);
				oracle.checkModifiedMethod(index, tmpMethodString);
				System.out.println(tmpMethodString);
				typeToEdit.createMethod(tmpMethodString, null, false, null);
				break;
			}
		}
	}

	protected IType findTypeToEdit(ICompilationUnit icu, String classname) {
		boolean found = false;
		IType typeToEdit = null;
		try {
			for (IType type : icu.getTypes()) {
				if (type.getFullyQualifiedName().equals(classname)) {
					typeToEdit = type;
					found = true;
					break;
				}
				String typeName = type.getFullyQualifiedName();
				for (IType type2 : type.getTypes()) {
					if ((typeName + "." + type2.getElementName())
							.equals(classname)) {
						typeToEdit = type2;
						found = true;
						break;
					}
				}
				if (found)
					break;
			}
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return typeToEdit;
	}

}
