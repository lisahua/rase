package changeassistant.versions.comparison;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.compare.internal.StructureCreatorDescriptor;
import org.eclipse.compare.structuremergeviewer.DiffContainer;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.DocumentRangeNode;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.compare.structuremergeviewer.IStructureCreator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;

public class DiffUtil {

	// from JavaNode; unfortunately JavaNode is not public
    private static final int INTERFACE = 4;
    private static final int CLASS = 5;
    private static final int FIELD = 8;
    private static final int CONSTRUCTOR = 10;
    private static final int METHOD = 11;
    private static final int IMPORT = 2;

	public static IStructureCreator getStructureCreator(String type){
		if (type == null) {
            return null;
        }
        String subType = type;
        if (type.startsWith(".")) {
            subType = type.substring(1);
        }        
        StructureCreatorDescriptor scd = CompareUIPlugin.getDefault().getStructureCreator(subType);
        if (scd == null) {
            return null;
        }
        return scd.createStructureCreator();
	}
    
	public static DiffNode compare(IFile left, IFile right){				
		IStructureCreator creator = getStructureCreator(left.getFileExtension());		
		Differencer differencer = new Differencer();
		try{
		Object structureDiffs = differencer.findDifferences(
				false,/*threeWay*/
				new NullProgressMonitor()/*progress monitor*/, 
				null /*data*/, 
				null /*ancestor*/, 
				creator.getStructure(new ResourceNode(left)), 
				creator.getStructure(new ResourceNode(right)));
		if(structureDiffs != null &&
				structureDiffs instanceof DiffNode){
			return (DiffNode)structureDiffs;
		}
		}catch(Exception e){
			e.printStackTrace();
		}		
		return null;		
	}
	/**
	 * the elements may contain more than one class diff node
	 * @param elements
	 * @return
	 */
	public static List<DiffNode> findClass(IDiffElement[] elements) {
		List<DiffNode> result = new ArrayList<DiffNode>();
        for (IDiffElement element : elements) {
            DiffContainer container = (DiffContainer) element;
            if (container instanceof DiffNode) {
                DiffNode dn = (DiffNode) container;
                if (dn.getId() instanceof DocumentRangeNode) {
                    DocumentRangeNode drn = (DocumentRangeNode) dn.getId();                    
                    if (DiffUtil.isClassOrInterface(drn)) {
                        result.add(dn);
                    } else if (container.hasChildren()) {
                    	result.addAll(findClass(container.getChildren()));
//                        dn = findClass(container.getChildren());
//                        if (dn != null) {
//                            return dn;
//                        }
                    }
                }
            }
        }
        return result;
    }
	
	public static DiffNode findImport(IDiffElement[] elements){		
		for(IDiffElement element : elements){
			DiffContainer container = (DiffContainer)element;
			if(container instanceof DiffNode){
				DiffNode dn = (DiffNode)container;				
				if(DiffUtil.isImportDeclaration(dn))
					return dn;
				if(container.hasChildren()){
					dn = findImport(container.getChildren());
					if(dn != null)return dn;
				}
			}
		}
		return null;
	}
	
    public static boolean isAttribute(DiffNode diffNode) {
        return ((DocumentRangeNode) diffNode.getId()).getTypeCode() == FIELD;
    }
	
    public static boolean isAttribute(DocumentRangeNode documentRangeNode) {
        return documentRangeNode.getTypeCode() == FIELD;
    }
	
	public static boolean isChange(DiffNode diffNode) {
        return diffNode.getKind() == Differencer.CHANGE;
    }
	
	public static boolean isClassOrInterface(DiffNode diffNode) {
        DocumentRangeNode drn = (DocumentRangeNode) diffNode.getId();
        return (drn.getTypeCode() == DiffUtil.CLASS) || (drn.getTypeCode() == DiffUtil.INTERFACE);
    }
	
	public static boolean isClassOrInterface(DocumentRangeNode documentRangeNode) {
        return (documentRangeNode.getTypeCode() == CLASS) || (documentRangeNode.getTypeCode() == INTERFACE);
    }

	public static boolean isDeclaration(DiffNode diffNode) {
        return isClassOrInterface(diffNode) || isMethodOrConstructor(diffNode) || isAttribute(diffNode);
    }
	
	public static boolean isDeletion(DiffNode diffNode) {
        return diffNode.getKind() == Differencer.DELETION;
    }
	
	public static boolean isImportDeclaration(DiffNode diffNode){
		DocumentRangeNode drn = (DocumentRangeNode)diffNode.getId();
		return drn.getTypeCode() == IMPORT;
	}
	
	public static boolean isImportDeclaration(DocumentRangeNode documentRangeNode){
		return documentRangeNode.getTypeCode() == IMPORT;
	}
	
	public static boolean isInsert(DiffNode diffNode) {
        return diffNode.getKind() == Differencer.ADDITION;
    }
	
	public static boolean isMethodOrConstructor(DiffNode diffNode) {
        DocumentRangeNode drn = (DocumentRangeNode) diffNode.getId();
        return (drn.getTypeCode() == DiffUtil.METHOD)
                || (drn.getTypeCode() == DiffUtil.CONSTRUCTOR);
    }
	
	public static boolean isMethodOrConstructor(DocumentRangeNode documentRangeNode) {
        return (documentRangeNode.getTypeCode() == METHOD) || (documentRangeNode.getTypeCode() == CONSTRUCTOR);
    }
	
	public static boolean isUsable(DiffNode diffNode){
		if ((diffNode.getLeft() == null) && (diffNode.getRight() instanceof DocumentRangeNode)) {
            return true;
        } else if ((diffNode.getLeft() instanceof DocumentRangeNode) && (diffNode.getRight() == null)) {
            return true;
        } else if ((diffNode.getLeft() instanceof DocumentRangeNode)
                && (diffNode.getRight() instanceof DocumentRangeNode)) {
            return true;
        }
        return false;
	}
	
	public static String simplifySig(String methodSignature){
		StringBuffer result = new StringBuffer();
		String name = methodSignature.substring(0, methodSignature.indexOf('('));
		String parms = methodSignature.substring(methodSignature.indexOf('(') + 1, 
				methodSignature.indexOf(')'));
		StringTokenizer st = new StringTokenizer(parms, " ");
		List<String> parameters = new ArrayList<String>();
		while(st.hasMoreElements()){
			parameters.add((String)st.nextElement());
		}
		result.append(name + "(");
		String temp;
		for(int i = 0; i < parameters.size(); i++){
			if(i != 0){
				result.append(' ');
			}
			temp = parameters.get(i);
			if(temp.contains(".")){
				temp = temp.substring(temp.lastIndexOf('.') + 1);
			}
			result.append(temp);
		}
		result.append(")");
		return result.toString();	
	}
}
