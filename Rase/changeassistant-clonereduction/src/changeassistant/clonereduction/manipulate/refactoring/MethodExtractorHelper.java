package changeassistant.clonereduction.manipulate.refactoring;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import changeassistant.classhierarchy.ProjectResource;
import changeassistant.clonereduction.datastructure.MethodRelation;
import changeassistant.clonereduction.datastructure.Wildcard;
import changeassistant.clonereduction.main.CloneReductionMain;

/**
 * To mark the new version of the changed method based on its mapping with the
 * suggested modified version
 * 
 * @author mn8247
 * 
 */
public class MethodExtractorHelper {

	ProjectResource pr;

	private Wildcard wildcard;
	private MethodRelation rel;

	public MethodExtractorHelper(ProjectResource pr, Wildcard wildcard,
			MethodRelation rel) {
		this.pr = pr;
		this.wildcard = wildcard;
		this.rel = rel;		
	}

	public void checkNeedTemplateClass(){
		boolean flag = false;
		if(CloneReductionMain.refactoringOld){
			flag = !wildcard.oldWildMethods.isEmpty() || !wildcard.oldWildTypes.isEmpty();
		}else{
			flag = !wildcard.newWildMethods.isEmpty() || !wildcard.newWildTypes.isEmpty();
		}		
        flag = flag || (!rel.inSameClass && !rel.hasSameSuperClass && !rel.outerAndInnerClass);
        if(flag){
        	RefactoringMetaData.setNeedTemplateClass();
        }
	}
	
	public boolean checkNeedMethodReplacement() {
		return !wildcard.newWildMethods.isEmpty();
	}
}

class MDFinder extends ASTVisitor {

	private MethodDeclaration md;

	@Override
	public boolean visit(MethodDeclaration node) {
		md = node;
		return false;
	}

	public MethodDeclaration findMD(ASTNode cu) {
		cu.accept(this);
		return md;
	}
}