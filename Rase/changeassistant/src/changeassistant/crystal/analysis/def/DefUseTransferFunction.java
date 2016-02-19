package changeassistant.crystal.analysis.def;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import changeassistant.crystal.FieldVariable;
import changeassistant.internal.ASTElementSearcher;
import changeassistant.main.ChangeAssistantMain;
import changeassistant.peers.SourceCodeRange;
import edu.cmu.cs.crystal.flow.AnalysisDirection;
import edu.cmu.cs.crystal.flow.ILatticeOperations;
import edu.cmu.cs.crystal.simple.AbstractingTransferFunction;
import edu.cmu.cs.crystal.simple.TupleLatticeElement;
import edu.cmu.cs.crystal.simple.TupleLatticeOperations;
import edu.cmu.cs.crystal.tac.eclipse.CompilationUnitTACs;
import edu.cmu.cs.crystal.tac.eclipse.EclipseTAC;
import edu.cmu.cs.crystal.tac.model.ArrayInitInstruction;
import edu.cmu.cs.crystal.tac.model.BinaryOperation;
import edu.cmu.cs.crystal.tac.model.CastInstruction;
import edu.cmu.cs.crystal.tac.model.ConstructorCallInstruction;
import edu.cmu.cs.crystal.tac.model.CopyInstruction;
import edu.cmu.cs.crystal.tac.model.DotClassInstruction;
import edu.cmu.cs.crystal.tac.model.EnhancedForConditionInstruction;
import edu.cmu.cs.crystal.tac.model.InstanceofInstruction;
import edu.cmu.cs.crystal.tac.model.LoadArrayInstruction;
import edu.cmu.cs.crystal.tac.model.LoadFieldInstruction;
import edu.cmu.cs.crystal.tac.model.LoadLiteralInstruction;
import edu.cmu.cs.crystal.tac.model.MethodCallInstruction;
import edu.cmu.cs.crystal.tac.model.NewArrayInstruction;
import edu.cmu.cs.crystal.tac.model.NewObjectInstruction;
import edu.cmu.cs.crystal.tac.model.OneOperandInstruction;
import edu.cmu.cs.crystal.tac.model.ReturnInstruction;
import edu.cmu.cs.crystal.tac.model.SourceVariable;
import edu.cmu.cs.crystal.tac.model.SourceVariableDeclaration;
import edu.cmu.cs.crystal.tac.model.SourceVariableReadInstruction;
import edu.cmu.cs.crystal.tac.model.StoreArrayInstruction;
import edu.cmu.cs.crystal.tac.model.StoreFieldInstruction;
import edu.cmu.cs.crystal.tac.model.TACInstruction;
import edu.cmu.cs.crystal.tac.model.TempVariable;
import edu.cmu.cs.crystal.tac.model.UnaryOperation;
import edu.cmu.cs.crystal.tac.model.Variable;

public class DefUseTransferFunction extends
		AbstractingTransferFunction<TupleLatticeElement<Integer, DefUseLE>> {

//	private Map<Variable, TupleLatticeElement<Integer, DefUseLE>> variableContext;
	// map between temp variable and source variable/field variable

	private EclipseTAC tac;// default tac

	private CompilationUnitTACs cTac;

	private MethodDeclaration d;

	private ASTElementSearcher searcher;

	private List<Variable> varList;
	private Map<String, FieldVariable> fieldMap;

	public DefUseTransferFunction(CompilationUnitTACs tac, MethodDeclaration d) {
		this.d = d;
		this.searcher = new ASTElementSearcher(d);
		this.cTac = tac;
		this.tac = tac.getMethodTAC(d);
//		this.variableContext = new HashMap<Variable, TupleLatticeElement<Integer, DefUseLE>>();
		this.fieldMap = new HashMap<String, FieldVariable>();
		this.varList = new ArrayList<Variable>();
	}

	private final TupleLatticeOperations<Integer, DefUseLE> ops = new TupleLatticeOperations<Integer, DefUseLE>(
			new DefUseLatticeOps(), DefUseLE.bottom());

	@Override
	public ILatticeOperations<TupleLatticeElement<Integer, DefUseLE>> getLatticeOperations() {
		return ops;
	}

	@Override
	public TupleLatticeElement<Integer, DefUseLE> createEntryValue(
			MethodDeclaration method) {
		return ops.getDefault();
	}

	public AnalysisDirection getAnalysisDirection() {
		return AnalysisDirection.FORWARD_ANALYSIS;
	}

	public Map<String, FieldVariable> getFieldMap() {
		return fieldMap;
	}

	public List<Variable> getVarList() {
		return varList;
	}

	public TupleLatticeElement<Integer, DefUseLE> transfer(
			ArrayInitInstruction instr,
			TupleLatticeElement<Integer, DefUseLE> value) {
		prepare(instr, value);

		addUse(value, instr.getInitOperands());
		addDef(value, instr.getTarget(), instr.getNode());
		return value;
	}

	public TupleLatticeElement<Integer, DefUseLE> transfer(
			BinaryOperation binop, TupleLatticeElement<Integer, DefUseLE> value) {
		prepare(binop, value);
		addUse(value, binop.getOperand1());
		addUse(value, binop.getOperand2());
		addDef(value, binop.getTarget(), binop.getNode());
		return value;
	}

	public TupleLatticeElement<Integer, DefUseLE> transfer(
			CastInstruction instr, TupleLatticeElement<Integer, DefUseLE> value) {
		prepare(instr, value);
		Variable operand = instr.getOperand();
		addUse(value, instr.getOperand());
		addDef(value, operand, instr.getTarget(), instr.getNode());
		return value;
	}

	public TupleLatticeElement<Integer, DefUseLE> transfer(
			ConstructorCallInstruction instr,
			TupleLatticeElement<Integer, DefUseLE> value) {
		prepare(instr, value);
		addUse(value, instr.getArgOperands());
		return value;
	}

	/**
	 * This method is not needed since the addDef() is already called in another
	 * transfer function
	 */
	public TupleLatticeElement<Integer, DefUseLE> transfer(
			CopyInstruction instr, TupleLatticeElement<Integer, DefUseLE> value) {
		prepare(instr, value);
		Variable operand = instr.getOperand();
		addUse(value, operand);
		if (!(instr.getTarget() instanceof TempVariable)) {
			addDef(value, operand, instr.getTarget(), instr.getNode());
		}
		return value;
	}

	public TupleLatticeElement<Integer, DefUseLE> transfer(
			DotClassInstruction instr,
			TupleLatticeElement<Integer, DefUseLE> value) {
		prepare(instr, value);
		addDef(value, instr.getTarget(), instr.getNode());
		return value;
	}

	public TupleLatticeElement<Integer, DefUseLE> transfer(
			EnhancedForConditionInstruction instr,
			TupleLatticeElement<Integer, DefUseLE> value) {
		prepare(instr, value);
		addUse(value, instr.getIteratedOperand());

		return value;
	}

	public TupleLatticeElement<Integer, DefUseLE> transfer(
			InstanceofInstruction instr,
			TupleLatticeElement<Integer, DefUseLE> value) {
		prepare(instr, value);
		addUse(value, instr.getOperand());
		addDef(value, instr.getTarget(), instr.getNode());
		return value;
	}

	public TupleLatticeElement<Integer, DefUseLE> transfer(
			LoadArrayInstruction instr,
			TupleLatticeElement<Integer, DefUseLE> value) {
		prepare(instr, value);
		addUse(value, instr.getSourceArray());
		addUse(value, instr.getArrayIndex());
		addDef(value, instr.getTarget(), instr.getNode());
		return value;
	}

	public TupleLatticeElement<Integer, DefUseLE> transfer(
			LoadFieldInstruction instr,
			TupleLatticeElement<Integer, DefUseLE> value) {
		prepare(instr, value);
		IVariableBinding binding = instr.resolveFieldBinding();
		FieldVariable fieldVariable = new FieldVariable(
				instr.getAccessedObjectOperand(), binding, instr.getFieldName());
		addUse(value, fieldVariable);
		addDef(value, fieldVariable, instr.getTarget(), instr.getNode());
		return value;
	}

	/*
	 * private IVariableBinding getFieldBinding(ASTNode astNode) {
	 * IVariableBinding binding = null; if (astNode instanceof SimpleName) {
	 * binding = (IVariableBinding) ((SimpleName) astNode) .resolveBinding(); }
	 * else if (astNode instanceof FieldAccess) { binding = ((FieldAccess)
	 * astNode).resolveFieldBinding(); } else if (astNode instanceof
	 * SuperFieldAccess) { binding = ((SuperFieldAccess)
	 * astNode).resolveFieldBinding(); } else if (astNode instanceof
	 * QualifiedName) { binding = (IVariableBinding) ((QualifiedName) astNode)
	 * .resolveBinding(); } else { System.err
	 * .println("The AST Node type is not designed to handle yet!"); } return
	 * binding; }
	 */
	/**
	 * The method is used to keep mapping between temp variables and number
	 * literals
	 */
	public TupleLatticeElement<Integer, DefUseLE> transfer(
			LoadLiteralInstruction instr,
			TupleLatticeElement<Integer, DefUseLE> value) {
		prepare(instr, value);
		// addDef(value, instr.getTarget(), instr.getNode());
		return value;
	}

	public TupleLatticeElement<Integer, DefUseLE> transfer(
			MethodCallInstruction instr,
			TupleLatticeElement<Integer, DefUseLE> value) {
		prepare(instr, value);

		Variable receiver = instr.getReceiverOperand();
		if (receiver != null) {
			// for fContainer.processContent() methods, we need to first connect
			// the variable to the field, and then append def
			addUse(value, receiver);
		}
		addUse(value, instr.getArgOperands());
		addDef(value, instr.getTarget(), instr.getNode());
		return value;
	}

	public TupleLatticeElement<Integer, DefUseLE> transfer(
			NewArrayInstruction instr,
			TupleLatticeElement<Integer, DefUseLE> value) {
		prepare(instr, value);
		addUse(value, instr.getDimensionOperands());
		if (instr.isInitialized()) {
			addUse(value, instr.getInitOperand());
		}
		addDef(value, instr.getTarget(), instr.getNode());
		return value;
	}

	public TupleLatticeElement<Integer, DefUseLE> transfer(
			NewObjectInstruction instr,
			TupleLatticeElement<Integer, DefUseLE> value) {
		prepare(instr, value);
		addUse(value, instr.getArgOperands());
		addDef(value, instr.getTarget(), instr.getNode());
		return value;
	}

	public TupleLatticeElement<Integer, DefUseLE> transfer(
			OneOperandInstruction instr,
			TupleLatticeElement<Integer, DefUseLE> value) {
		prepare(instr, value);
		addUse(value, instr.getOperand());
		addDef(value, instr.getTarget(), instr.getNode());
		return value;
	}

	public TupleLatticeElement<Integer, DefUseLE> transfer(
			ReturnInstruction instr,
			TupleLatticeElement<Integer, DefUseLE> value) {
		prepare(instr, value);
		try {
			if (instr.getReturnedVariable() != null)
				addUse(value, instr.getReturnedVariable());
		} catch (Exception e) {
			// do nothing
		}
		return value;
	}

	public TupleLatticeElement<Integer, DefUseLE> transfer(
			StoreArrayInstruction instr,
			TupleLatticeElement<Integer, DefUseLE> value) {
		prepare(instr, value);
		addUse(value, instr.getArrayIndex());
		addUse(value, instr.getSourceOperand());
		appendDef(value, instr.getDestinationArray(), instr.getNode());
		return value;
	}

	public TupleLatticeElement<Integer, DefUseLE> transfer(
			StoreFieldInstruction instr,
			TupleLatticeElement<Integer, DefUseLE> value) {// the field is not
															// recorded here
		prepare(instr, value);
		try {
			addUse(value, instr.getSourceOperand());
			IVariableBinding binding = instr.resolveFieldBinding();
			FieldVariable fieldVariable = new FieldVariable(
					instr.getAccessedObjectOperand(), binding,
					instr.getFieldName());
			addDef(value, fieldVariable, instr.getNode());
		} catch (Exception e) {
			// ignore
		}
		return value;
	}

	public TupleLatticeElement<Integer, DefUseLE> transfer(
			SourceVariableDeclaration instr,
			TupleLatticeElement<Integer, DefUseLE> value) {
		prepare(instr, value);
		if (instr.getDeclaredVariable() != null) {
			addDef(value, instr.getDeclaredVariable(), instr.getNode());
		}
		return value;
	}

	// To read a variable--this has nothing to do with def
	public TupleLatticeElement<Integer, DefUseLE> transfer(
			SourceVariableReadInstruction instr,
			TupleLatticeElement<Integer, DefUseLE> value) {
		prepare(instr, value);
		addUse(value, instr.getVariable());
		return value;
	}

	public TupleLatticeElement<Integer, DefUseLE> transfer(UnaryOperation unop,
			TupleLatticeElement<Integer, DefUseLE> value) {
		prepare(unop, value);
		addUse(value, unop.getOperand());
		addDef(value, unop.getTarget(), unop.getNode());
		return value;
	}

	private void prepare(TACInstruction instr,
			TupleLatticeElement<Integer, DefUseLE> value) {
		// log(instr, value);
		if (ChangeAssistantMain.DEBUG) {
			System.out.print("ASTNode:" + instr.getNode().toString()
					+ " INSTRUCTION:" + instr.toString());
		}
		clearUses(value);
	}

	/**
	 * add def if var is equivalent to a source variable
	 */
	private void addDef(TupleLatticeElement<Integer, DefUseLE> value,
			Variable var, ASTNode astNode) {

		if (var == null || astNode == null)
			return;
		if (var instanceof TempVariable) {
//			variableContext.put(var, copyValue(value));
			return;
		} else {
			int index = getKey(var);
			DefUseLE targetLE = new DefUseLE(value.get(index));
			targetLE.defs.clear();
			targetLE.defs.add(new SourceCodeRange(astNode.getStartPosition(),
					astNode.getLength()));
			value.put(index, targetLE);
			if (var instanceof FieldVariable) {
				FieldVariable fv = (FieldVariable) var;
				String name = fv.getSourceString();
				fieldMap.put(name, fv);
			}
		}
		// if (var instanceof TempVariable) {
		// variableContext.put(index, copyValue(value));
		// }

	}

	private TupleLatticeElement<Integer, DefUseLE> copyValue(
			TupleLatticeElement<Integer, DefUseLE> value) {
		@SuppressWarnings("deprecation")
		TupleLatticeElement<Integer, DefUseLE> context = new TupleLatticeElement<Integer, DefUseLE>(
				DefUseLE.bottom(), DefUseLE.bottom());
		HashMap<Integer, DefUseLE> newMap = context.getElements();
		HashMap<Integer, DefUseLE> oldMap = value.getElements();
		for (Entry<Integer, DefUseLE> entry : oldMap.entrySet()) {
			newMap.put(entry.getKey(), new DefUseLE(entry.getValue()));
		}
		return context;
	}

	private int getKey(Variable var) {
		int index = varList.indexOf(var);
		if (index == -1) {
			index = varList.size();
			varList.add(var);
		}
		return index;
	}

	/**
	 * add def if var is equivalent to a source variable, otherwise, if add map
	 * between temp variable and a source variable if var is a temp variable and
	 * operand is equivalent to a source variable
	 * 
	 * @param value
	 * @param operand
	 * @param var
	 * @param astNode
	 */
	private void addDef(TupleLatticeElement<Integer, DefUseLE> value,
			Variable operand, Variable var, ASTNode astNode) {
		if (operand == null || var == null || astNode == null)
			return;
		if (var instanceof TempVariable) {
//			variableContext.put(var, copyValue(value));
			return;
		}
		DefUseLE targetLE = null;
		int index = getKey(var);
		targetLE = new DefUseLE(value.get(index));
		targetLE.defs.clear();
		targetLE.defs.add(new SourceCodeRange(astNode.getStartPosition(),
				astNode.getLength()));
		value.put(index, targetLE);

	}

	/**
	 * Only add use of source variable, but do not care about temp variable
	 * 
	 * @param value
	 * @param vars
	 */
	private void addUse(TupleLatticeElement<Integer, DefUseLE> value,
			List<Variable> vars) {
		if (vars == null)
			return;
		for (int i = 0; i < vars.size(); i++) {
			addUse(value, vars.get(i));
		}
	}

	/**
	 * to add use to a field
	 * 
	 * @param value
	 * @param var
	 */
	private void addUse(TupleLatticeElement<Integer, DefUseLE> value,
			Variable var) {
		if (var == null)
			return;
		if (var instanceof SourceVariable || var instanceof FieldVariable) {
			int index = getKey(var);
			DefUseLE knownLE = new DefUseLE(value.get(index));
			// all reaching definition can be used here
			knownLE.uses.addAll(knownLE.defs);
			if (var instanceof FieldVariable && knownLE.defs.isEmpty()) {
				FieldVariable fv = (FieldVariable) var;
				String name = fv.getSourceString();
				knownLE.fieldsAlreadyDefined.add(name);
				fieldMap.put(name, fv);
			}
			value.put(index, knownLE);
		} else if (var instanceof TempVariable) {// var instanceof TempVariable
			/*
			TupleLatticeElement<Integer, DefUseLE> tmpValue = variableContext
					.get(var);
			if (tmpValue != null) {
				Variable v = null;
				for (Integer key : tmpValue.getKeySet()) {
					v = varList.get(key);
					if (!(v instanceof TempVariable)) {
						DefUseLE knownLE = tmpValue.get(key);
						DefUseLE le = value.get(key);
						le.uses.addAll(knownLE.uses);
						if (v instanceof FieldVariable
								&& knownLE.defs.isEmpty()) {
							le.fieldsAlreadyDefined.add(((FieldVariable) v)
									.getSourceString());
						}
					}
				}
			}
			// assumption: each temp variable is read only once
			// variableContext.remove(index);
			variableContext.remove(var);
			*/
		}
	}

	private void clearUses(TupleLatticeElement<Integer, DefUseLE> value) {
		Set<Integer> keys = value.getKeySet();
		DefUseLE tmpLE = null;
		for (Integer key : keys) {
			tmpLE = value.get(key);
			tmpLE.uses.clear();
			tmpLE.fieldsAlreadyDefined.clear();
		}
	}

	/**
	 * for Array case, the def is appended
	 * 
	 * @param value
	 * @param variable
	 * @param astNode
	 * @return
	 */
	private void appendDef(TupleLatticeElement<Integer, DefUseLE> value,
			Variable variable, ASTNode astNode) {
		int index = getKey(variable);
		DefUseLE le = new DefUseLE(value.get(index));
		le.defs.add(new SourceCodeRange(astNode.getStartPosition(), astNode
				.getLength()));
		value.put(index, le);
	}
}
