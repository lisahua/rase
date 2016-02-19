package changeassistant.clonereduction.helper;

import java.util.ArrayList;
import java.util.List;

import changeassistant.classhierarchy.ClassContext;
import changeassistant.classhierarchy.ProjectResource;
import changeassistant.clonereduction.datastructure.MethodRelation;
import changeassistant.clonereduction.search.CloneReductionMatchResult;
import changeassistant.versions.comparison.ChangedMethodADT;

public class MethodRelationHelper {

	private ProjectResource pr;

	public MethodRelationHelper(ProjectResource pr) {
		this.pr = pr;
	}

	public MethodRelation inferRelation2(
			List<CloneReductionMatchResult> matchResults) {
		List<ChangedMethodADT> adts = new ArrayList<ChangedMethodADT>();
		for (CloneReductionMatchResult r : matchResults) {
			adts.add(r.getAdt());
		}
		return inferRelation(adts);
	}

	public MethodRelation inferRelation(List<ChangedMethodADT> adts) {
		MethodRelation rel = new MethodRelation();
		ChangedMethodADT adt0 = adts.get(0);
		String knownClassName = adt0.classname;
		String outerClassName = adt0.classname;
		String className = null;
		boolean inSameClass = true;
		for (int i = 1; i < adts.size(); i++) {
			className = adts.get(i).classname;
			if(className.length() < outerClassName.length()){
				outerClassName = className;
			}
			if (!className.equals(knownClassName)) {
				inSameClass = false;
			}			
		}
		rel.inSameClass = inSameClass;
		boolean outerAndInnerClass = true;
		String tmpClassName = null;
		String subStr = null;
		if(!inSameClass){
			for(int i = 0; i < adts.size(); i++){
				tmpClassName = adts.get(i).classname;
				if(!tmpClassName.contains(outerClassName)){
					outerAndInnerClass = false;
					break;
				}else if(tmpClassName.equals(outerClassName)){
					//do nothing
				}else{
					subStr = tmpClassName.substring(outerClassName.length());
					if(!subStr.contains(".")){
						outerAndInnerClass = false;
						break;
					}
				}
			}
		}
		rel.outerAndInnerClass = outerAndInnerClass;
		if(outerAndInnerClass){
			rel.outerClassName = outerClassName;
		}
		List<String> supers = null;
		if (!inSameClass) {
			String[] classNames = new String[adts.size()];
			for (int i = 0; i < adts.size(); i++) {
				classNames[i] = adts.get(i).classname;
			}
			ClassContextHelper ccHelper = new ClassContextHelper();
			supers = ccHelper.getSuperClasses(pr, classNames);
			if (!supers.isEmpty()) {
				ClassContext cc = pr.findClassContext(supers.get(0));
				if (cc.relativeFilePath != null) {
					rel.hasSameSuperClass = true;
					rel.supers = supers;
				}
			}
		}
		return rel;
	}
}
