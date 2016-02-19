package changeassistant.clonereduction.manipulate.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;

import changeassistant.classhierarchy.ClassContext;
import changeassistant.classhierarchy.ProjectResource;
import changeassistant.multipleexample.main.Constants;

public class TypeInfo {
	public String name;
	public String abstractName;
	public String content;
	public String qName;
	
	public TypeInfo(String name, String content, String abstractName){
		this.name = name;
		this.content = content;
		this.abstractName = abstractName;
	}
	
	public TypeInfo(String name, ASTNode astNode, String abstractName){
		this.name = name;
		this.content = astNode.toString();
		switch(astNode.getNodeType()){
		case ASTNode.SIMPLE_TYPE: this.qName = ((SimpleType)astNode).resolveBinding().getQualifiedName();
								  break;
		case ASTNode.SIMPLE_NAME: this.qName = ((SimpleName)astNode).resolveTypeBinding().getQualifiedName();
								  break;
		}
		
		this.abstractName = abstractName;
	}
	
	public static void merge(TypeInfo tInfo, TypeInfo tInfo2, ProjectResource pr){
		String name1 = tInfo.qName;
		String name2 = tInfo2.qName;
		String qName = Constants.OBJECT_TYPE;
		if(name1 == null || name2 == null)
			return;
		ClassContext cc1 = pr.findClassContext(name1);
		ClassContext cc2 = pr.findClassContext(name2);
		if(cc1 != null && cc2 != null){			
			List<String> supers1 = new ArrayList<String>(cc1.getAllSuperClassNames());
			List<String> supers2 = cc2.getAllSuperClassNames();
			supers1.retainAll(supers2);
			if(!supers1.isEmpty()){
				qName = supers1.get(0);
			}else{
				List<String> interfaces1 = new ArrayList<String>(cc1.getAllInterfaceNames());
				Set<String> interfaces2 = cc2.getAllInterfaceNames();
				interfaces1.retainAll(interfaces2);
				if(!interfaces1.isEmpty()){
					qName = interfaces1.get(0);
				}
			}	
		}
		tInfo.qName = qName;
		tInfo2.qName = qName;
	}
	
	@Override
	public String toString(){
		return name + "(" + content + ")";
	}
}
