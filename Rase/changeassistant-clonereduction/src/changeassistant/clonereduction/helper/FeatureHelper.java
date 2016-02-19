package changeassistant.clonereduction.helper;

import java.lang.reflect.Modifier;
import java.util.List;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import changeassistant.classhierarchy.ClassContext;
import changeassistant.classhierarchy.ProjectResource;
import changeassistant.clonereduction.datastructure.ExtractedMethodFeature;
import changeassistant.versions.comparison.ChangedMethodADT;

public class FeatureHelper {
	ProjectResource pr;

	public FeatureHelper(ProjectResource pr) {
		this.pr = pr;
	}

	public ExtractedMethodFeature inferFeatures(
			List<ChangedMethodADT> knownADTList) {
		int modifiers = getModifiers(knownADTList);
		ExtractedMethodFeature feature = new ExtractedMethodFeature(modifiers);
		return feature;
	}

	private int getModifiers(List<ChangedMethodADT> knownADTList) {
		int modifierStatic = Modifier.STATIC;
		ClassContext cc = null;
		MethodDeclaration md = null;
		for (ChangedMethodADT adt : knownADTList) {
			cc = pr.findClassContext(adt.classname);
			md = (MethodDeclaration) cc.getMethodAST(adt.methodSignature);
			modifierStatic &= md.getModifiers();
		}
		return modifierStatic;
	}
}
