package changeassistant.clonereduction.manipulate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import changeassistant.changesuggestion.expression.representation.Term;
import changeassistant.changesuggestion.expression.representation.VariableTypeBindingTerm;
import changeassistant.classhierarchy.ClassContext;
import changeassistant.classhierarchy.ProjectResource;
import changeassistant.clonereduction.main.CloneReductionMain;
import changeassistant.clonereduction.manipulate.util.CloneReductionUtil;
import changeassistant.multipleexample.apply.EditScriptApplier;
import changeassistant.multipleexample.contextualize.ContextController;
import changeassistant.multipleexample.main.Oracle;
import changeassistant.multipleexample.match.CodePattern;
import changeassistant.multipleexample.match.MatchResult;
import changeassistant.multipleexample.match.PatternUtil;
import changeassistant.multipleexample.match.search.SearchController;
import changeassistant.multipleexample.partition.datastructure.ChangeSummary;
import changeassistant.multipleexample.partition.datastructure.EditInCommonCluster;
import changeassistant.multipleexample.partition.datastructure.EditInCommonGroup;
import changeassistant.multipleexample.partition.datastructure.SimpleASTNode;
import changeassistant.versions.comparison.ChangedMethodADT;
import changeassistant.versions.comparison.MethodModification;

public class SystematicEditController {

	protected List<EditInCommonGroup> groups;
	protected List<MatchResult> mResults;
	protected EditInCommonCluster cluster;
	protected Oracle oracle;

	public boolean check(ProjectResource pr, List<EditInCommonGroup> groups,
			Oracle oracle, ProjectResource newPr) {
		ContextController cc = new ContextController();
		this.groups = cc.contextualize(groups);
		this.groups = createSysEdits(this.groups);
		this.oracle = oracle;
		if (this.groups.size() == 0)
			return false;
		EditInCommonGroup group = this.groups.get(0);
		cluster = group.getLargestCluster();
		if(cluster != null){
			CodePattern pat = cluster.getCodePattern();
			pat.setUsedFields(identifyUsedFields(newPr));
			SearchController sc = new SearchController();
			mResults = sc.matchMethods(pat, pr);
			if(mResults.size() == 0){
				mResults = CloneReductionUtil.matchMethods(pat,
					CloneReductionUtil.getOldADTs(this.groups.get(0)));
			}
//			filterForFields(pat.getUsedFields(), newPr,
//					CloneReductionUtil.getOldADTs(group),
//					CloneReductionUtil.getNewADTs(group));
			return true;
		}
		return false;
	}

	private void filterForFields(Set<String> usedFields, ProjectResource newPr,
			List<ChangedMethodADT> oldADTs, List<ChangedMethodADT> newADTs) {
		ChangedMethodADT adt = null;
		ClassContext cc = null;
		Set<String> fields = null;
		List<MatchResult> newMResults = new ArrayList<MatchResult>();
		for (MatchResult mResult : mResults) {
			adt = mResult.getADT();
			if (oldADTs.contains(adt)) {
				adt = newADTs.get(oldADTs.indexOf(adt));
			}
			cc = newPr.findClassContext(adt.classname);
			if (cc != null) {
				fields = cc.fieldMap.keySet();
				if (fields.containsAll(usedFields)) {
					newMResults.add(mResult);
				}
			}
		}
		mResults = newMResults;
	}

	private Set<String> identifyUsedFields(ProjectResource newPr) {
		EditInCommonGroup group = groups.get(0);
		MethodModification mm = group.getMMList().get(0);
		ClassContext cc = newPr.findClassContext(mm.newMethod.classname);
		Set<String> fields = cc.fieldMap.keySet();
		List<ChangeSummary> csList = cluster.getConChgSum();
		List<List<List<SimpleASTNode>>> exprsLists = cluster
				.getSimpleExprsLists();
		Set<Term> terms = new HashSet<Term>();
		List<List<SimpleASTNode>> exprsList = null;
		Set<String> usedFields = new HashSet<String>();
		ChangeSummary cs = null;
		for (int i = 0; i < csList.size(); i++) {
			cs = csList.get(i);
			switch (cs.editType) {
			case INSERT:
				exprsList = exprsLists.get(i);
				PatternUtil.collectTerms(terms, exprsList.get(0));
				break;
			case UPDATE:
				exprsList = exprsLists.get(i);
				PatternUtil.collectTerms(terms, exprsList.get(1));
				break;
			}
		}
		String tName = null;
		for (Term t : terms) {
			tName = t.getName();
			if (t instanceof VariableTypeBindingTerm && fields.contains(tName)) {
				usedFields.add(tName);
			}
		}
		return usedFields;
	}

	public void apply() throws Exception {

		EditScriptApplier esApplier = new EditScriptApplier();
		esApplier.apply(mResults, CloneReductionMain.counter, 0, cluster);
		oracle.checkModifiedMethods(mResults);
	}

	public int estimateCost() {
		return 0;
	}

	private List<EditInCommonGroup> createSysEdits(
			List<EditInCommonGroup> groups) {
		List<EditInCommonGroup> newGroups = new ArrayList<EditInCommonGroup>();
		EditInCommonGroup group = null;
		Iterator<EditInCommonCluster> iter = null;
		EditInCommonCluster eClus = null;
		for (int i = 0; i < groups.size(); i++) {
			group = groups.get(i);
			group.initializeIterator();
			iter = group.getIterator();
			if (!iter.hasNext()) {
				continue;
			} else {
				newGroups.add(group);
			}
			int counter = 0;
			while (iter.hasNext()) {
				eClus = iter.next();
				eClus.createEdits(group);
				counter++;
			}
			if (counter > 1) {
				System.out.println("More process is needed");
			}
		}
		return newGroups;
	}

	public List<MatchResult> getMatchResults() {
		return mResults;
	}

	public EditInCommonCluster getCluster() {
		return cluster;
	}

}
