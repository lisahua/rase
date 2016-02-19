//package changeassistant.changesuggestion.astrewrite;
//
//import java.util.ArrayList;
//import java.util.Enumeration;
//import java.util.Iterator;
//import java.util.List;
//
////import org.eclipse.core.runtime.CoreException;
//import org.eclipse.jdt.core.dom.AST;
//import org.eclipse.jdt.core.dom.ASTNode;
//import org.eclipse.jdt.core.dom.ASTParser;
//import org.eclipse.jdt.core.dom.ASTVisitor;
//import org.eclipse.jdt.core.dom.Block;
//import org.eclipse.jdt.core.dom.CatchClause;
//import org.eclipse.jdt.core.dom.CompilationUnit;
//import org.eclipse.jdt.core.dom.Expression;
//import org.eclipse.jdt.core.dom.ExpressionStatement;
//import org.eclipse.jdt.core.dom.IfStatement;
//import org.eclipse.jdt.core.dom.MethodDeclaration;
//import org.eclipse.jdt.core.dom.MethodInvocation;
//import org.eclipse.jdt.core.dom.Statement;
//import org.eclipse.jdt.core.dom.StringLiteral;
//import org.eclipse.jdt.core.dom.SwitchStatement;
//import org.eclipse.jdt.core.dom.TryStatement;
//import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
//import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
//import org.eclipse.jface.text.IDocument;
//import org.eclipse.text.edits.TextEdit;
//
//import changeassistant.changesuggestion.astrewrite.ManipulatorHelper.TextEditProvider;
//import changeassistant.changesuggestion.astrewrite.editcheckers.DeleteChecker;
//import changeassistant.changesuggestion.astrewrite.editcheckers.EditChecker;
//import changeassistant.changesuggestion.astrewrite.editcheckers.InsertChecker;
//import changeassistant.changesuggestion.astrewrite.editcheckers.UpdateChecker;
//import changeassistant.changesuggestion.expression.representation.ASTRefiner;
//import changeassistant.changesuggestion.expression.representation.AbstractExpressionRepresentationGenerator;
//import changeassistant.classhierarchy.ClassContext;
//import changeassistant.classhierarchy.ProjectResource;
//import changeassistant.internal.ASTChildFinder;
//import changeassistant.internal.ASTMethodFinder;
//import changeassistant.internal.ASTNodeFinder;
//import changeassistant.internal.WorkspaceUtilities;
//import changeassistant.peers.MethodPair;
//import changeassistant.peers.PeerMethodADT;
//import changeassistant.peers.SourceCodeRange;
//import changeassistant.peers.comparison.ASTMethodBodyTransformer;
//import changeassistant.peers.comparison.Node;
//import changeassistant.versions.comparison.MethodModification;
//import changeassistant.versions.treematching.edits.DeleteOperation;
//import changeassistant.versions.treematching.edits.ITreeEditOperation;
//import changeassistant.versions.treematching.edits.InsertOperation;
//import changeassistant.versions.treematching.edits.MoveOperation;
//import changeassistant.versions.treematching.edits.UpdateOperation;
//
//public class ASTRewriteBasedManipulator {
//
//	private ASTRewrite rewrite;
//	
//	protected void beforeManipulate(CompilationUnit unit){
//		rewrite = ASTRewrite.create(unit.getAST());		
//	}
//	
//	protected void afterManipulate(CompilationUnit unit){
//		try {
//			ManipulatorHelper.saveASTRewriteContents(unit, rewrite);
//		} catch (CoreException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch(Exception e){
//			e.printStackTrace();
//		}
//	}
//	
//	protected void commit(CompilationUnit unit){
//		try{
//			ManipulatorHelper.commit(unit, rewrite);
//		}catch(CoreException e){
//			e.printStackTrace();
//		}
//	}
//	
//	public void manipulate(ProjectResource prLeft, ProjectResource prRight, MethodPair mp, 
//			MethodModification mm){			
//		// the change should happen on a temp file
//		PeerMethodADT method1 = mp.method1;
//		PeerMethodADT method2 = mp.method2;
//		ClassContext cc1 = prLeft.findClassContext(method1.classname);
//		ClassContext cc2 = prLeft.findClassContext(method2.classname);
//		CompilationUnit originalCu = cc2.cu;
//		ListRewrite listRewrite = null;
//		try{
//			if(mp.isSame){/*
//				//there is no need to replicate the edits, just copy and paste is enough
//				beforeManipulate(originalCu);//there is no need to make a copy, since the change is only once
//				ASTNode method1ASTNode = cc1.getMethodAST(method1.methodSignature);
//				ASTNode updatedMethod1ASTNode = prRight.findClassContext(method1.classname)
//											.getMethodAST(method1.methodSignature);
//				ASTNode method2ASTNode = cc2.getMethodAST(method2.methodSignature);
//				
//				ASTNode newMethodBody = ASTNode.copySubtree(method1ASTNode.getAST(), 
//						((MethodDeclaration)updatedMethod1ASTNode).getBody());
//				rewrite.replace(((MethodDeclaration)method2ASTNode).getBody(), newMethodBody, null);
//				ManipulatorHelper.print(originalCu, rewrite);
//			*/}else{
//				List<ITreeEditOperation> edits = mm.getEdits();
//				Node originalNode = cc2.getMethodNode(method2.methodSignature);
//				CompilationUnit cu = ManipulatorHelper.initializeCopy(prLeft, method2.classname);
//				ASTMethodBodyTransformer transformer = new ASTMethodBodyTransformer();
//				ASTMethodFinder methodFinder = new ASTMethodFinder();
//				ASTNodeSubstitutor substitutor = null;
//				Node newNode = null;
//				EditChecker editChecker = null;
//				boolean changed = false;
//				boolean quit = false;
//			try{
//				for(int i = 0; i < edits.size(); i++){
//					ITreeEditOperation edit = edits.get(i);
//					cu = ManipulatorHelper.createCopy();// read in the new CompilationUnit
//					//I doubt whether this is compilable
//					changed = false;
//					newNode = transformer.createMethodBodyTree(methodFinder
//									.lookforMethod(cu, method2.methodSignature));						
//					beforeManipulate(cu);//create rewrite object with the known cu				
//					switch(edit.getOperationType()){
//					case INSERT: 	{
//						InsertOperation insert = (InsertOperation)edit;						
//						Node currentParent = newNode.lookforNodeBasedOnPosition(insert.getParentNode());
//						editChecker = new InsertChecker();
//						if(editChecker.check(insert, currentParent)){
//							ASTNode parentASTNode = lookforASTNode(cu, currentParent.getSourceCodeRange());
//							ASTNode newASTNode;
//							if(insert.getNodeToInsert().getExpressions().size() > 0){
//								ASTNode expr1 = (ASTNode)insert.getNodeToInsert().getFirstExpression();							
//								substitutor = new ASTNodeSubstitutor(mp, parentASTNode.getAST());
//								expr1.accept(substitutor);
//							    newASTNode = ASTNodeGenerator.createNewASTNode(parentASTNode.getAST(),
//							    		insert.getNodeToInsert(), substitutor.getComposedASTNode());			
//							}else{
//								newASTNode = ASTNodeGenerator.createNewASTNode(parentASTNode.getAST(), insert.getNodeToInsert());
//							}							
//							listRewrite = getListRewrite(parentASTNode);
//							if(listRewrite != null){
//								int position = insert.getPosition();
//								switch(parentASTNode.getNodeType()){
//								case ASTNode.TRY_STATEMENT: {
//									position--;//since the try-body is also counted 				
//								}break;
//								}
//								listRewrite.insertAt(newASTNode, position, null);
//							}else{
//								Node nodeToInsert = insert.getNodeToInsert();
//								if(nodeToInsert.getStrValue().equals("then")){
//									((IfStatement)parentASTNode).setThenStatement((Statement) newASTNode);
//								}else if(nodeToInsert.getStrValue().equals("else")){
//									((IfStatement)parentASTNode).setElseStatement((Statement)newASTNode);
//								}
//							}
//							
//							changed = true;
//						}else{//because the changes coming together may depend on each other
//							quit = true;//since the insertion is not valid, there is no need to add other changes 
//						}
//					}
//					break;
//					case DELETE:	{//all deletions come last and come together
//						//there is no need to care about "then" or "else", since such edits are not counted
//						DeleteOperation delete = (DeleteOperation)edit;					
//						Node currentParent = newNode.lookforNodeBasedOnPosition(delete.getParentNode());
//						if(currentParent == null){
//							System.out.println("The parent node is null!");
//						}
//						Node currentChild = (Node)currentParent.getChildAt(delete.getPosition());
//						editChecker = new DeleteChecker();
//						if(editChecker.check(delete, currentParent)){
//							ASTNode parentASTNode = lookforASTNode(cu, currentParent.getSourceCodeRange());
//							ASTNode currentASTNode = lookforASTNode(cu, currentChild.getSourceCodeRange());
//							if(parentASTNode.equals(currentASTNode)){//the parent is the node itself
//								Block block = parentASTNode.getAST().newBlock();
//								rewrite.replace(parentASTNode, block, null);
//							}else{
//								listRewrite = getListRewrite(parentASTNode); 
//								listRewrite.remove(
//										(ASTNode)listRewrite.getRewrittenList().get(delete.getPosition()),null);									
//							}							
//							changed = true;
//						}else{
//							quit = true;
//						}
//						}
//						break;
//					case UPDATE: {
//						UpdateOperation update = (UpdateOperation)edit;	
//						Node currentParent = newNode.lookforNodeBasedOnPosition(update.getParentNode());
//						Node currentNodeToUpdate = (Node)currentParent.getChildAt(update.getPosition());
//						ASTNode astNodeToUpdate = lookforASTNode(cu, currentNodeToUpdate.getSourceCodeRange());
//						ASTNode parentASTNode = astNodeToUpdate.getParent();						
//						ASTNode astNewNode = null;
//						editChecker = new UpdateChecker();
//						if(((UpdateChecker)editChecker)
//								.checkDirectApply(update, currentNodeToUpdate)){//to check whether the two nodes are the same
//							astNewNode = ASTNodeGenerator.createUpdatedASTNode(astNodeToUpdate, update); 						
//							rewrite.replace(astNodeToUpdate, astNewNode, null);
//							changed = true;
//						}else if(((UpdateChecker)editChecker)
//								.checkIndirectApply(update, currentNodeToUpdate)){
////							List<String> tokens1 = AbstractExpressionRepresentationGenerator
////											.generateTokenizedRepresentation(
////													(ASTNode)update.getNodeToUpdate().getFirstExpression()); 
////							List<String> tokens2 = AbstractExpressionRepresentationGenerator
////											.generateTokenizedRepresentation(
////													(ASTNode)currentNodeToUpdate.getFirstExpression()); 
////							List<String> newTokens1 = AbstractExpressionRepresentationGenerator
////											.generateTokenizedRepresentation(
////													(ASTNode)update.getNewNode().getFirstExpression());						
//							
//							String expr1 = ((ASTNode)update.getNodeToUpdate().getFirstExpression()).toString();
//							String expr2 = ((ASTNode)currentNodeToUpdate.getFirstExpression()).toString();
//							String newExpr1= null;
//							if(update.getNewNode().getExpressions().size() != 0){
//								newExpr1 = ((ASTNode)update.getNewNode().getFirstExpression()).toString();
//							}
//							if(newExpr1 == null){
////								astNewNode = parentASTNode.getAST().newBlock();
////								listRewrite = getListRewrite(astNewNode);
////								listRewrite.insertAt(astNodeToUpdate, 0, null);								
//							}else{
//								StringDifferencer sd = new StringDifferencer();
//								sd.editScript(expr1, newExpr1);
//								String newExpr2 = sd.apply(expr2, sd.getEditScript());
//								if(newExpr2 != null){
//									ASTParser parser = ASTParser.newParser(AST.JLS3);
//									parser.setKind(ASTParser.K_EXPRESSION);
//									parser.setSource(newExpr2.toCharArray());
//									astNewNode = parser.createAST(null);
//									astNewNode = ASTNode.copySubtree(parentASTNode.getAST(), astNewNode);
//									rewrite.replace(astNodeToUpdate, astNewNode, null);				
//									changed = true;					
//								}
//							}
//							
//						}else{
//							quit = true;
//						}								
//						}
//						break;
//					case MOVE:      {
//						MoveOperation move = (MoveOperation)edit;				
//						Node parent = newNode.lookforNodeBasedOnPosition(move.getParentNode());
//						Node currentMovedNode = (Node)parent.getChildAt(move.getPosition());
//					    ASTNode astNodeToMove = lookforASTNode(cu, currentMovedNode.getSourceCodeRange());								
//						ASTNode parentASTNode = astNodeToMove.getParent();
//						if(parentASTNode.getNodeType() == ASTNode.IF_STATEMENT &&
//								astNodeToMove.getNodeType() != ASTNode.BLOCK){
//							rewrite.remove(astNodeToMove, null);
//						}else{
//							listRewrite = getListRewrite(parentASTNode);
//							listRewrite.remove(
//									(ASTNode)listRewrite.getRewrittenList().get(move.getPosition()),
//									null);	
//						}						
//						Node newParent = newNode.lookforNodeBasedOnPosition(move.getNewParentNode());
//						if(newParent == null){//the node to find is a "then" block, which happens to be empty
//							//so look for the parent IfStatement instead
//							newParent = newNode.lookforNodeBasedOnPosition((Node)move.getNewParentNode().getParent());
//						}
//						ASTNode newParentASTNode = lookforASTNode(cu, newParent.getSourceCodeRange());
//						if(newParentASTNode instanceof IfStatement){
//							if(move.getNodeToMove().getStrValue().equals("then") ||
//									((Node)move.getNodeToMove().getParent()).getStrValue().equals("then")){								
//								rewrite.set(
//										(IfStatement)newParentASTNode, IfStatement.THEN_STATEMENT_PROPERTY, astNodeToMove, null);								
//							}else if(move.getNodeToMove().getStrValue().equals("else")){
//								((IfStatement)newParentASTNode).setElseStatement((Statement)astNodeToMove);							
//							}
//						}else{
//							listRewrite = getListRewrite(newParentASTNode);
//							listRewrite.insertAt(astNodeToMove, move.getNewPosition(), null);
//						}						
//						changed = true;
//						}
//						break;
//					}
//					if(changed){
//						afterManipulate(cu);	
//					}
//					if(quit){
//						break;
//					}
//				}
////				commit(cu);
//			}catch(Exception e){
//				e.printStackTrace();
//			}
//			}
//		}catch(Exception e){
//			e.printStackTrace();
//		}				
//}
//	
//	private ListRewrite getListRewrite(ASTNode parentASTNode){
//		ListRewrite listRewrite = null;
//		switch(parentASTNode.getNodeType()){
//		case ASTNode.BLOCK: {
//			listRewrite = rewrite.getListRewrite(parentASTNode, Block.STATEMENTS_PROPERTY);			
//		}break;
//		case ASTNode.CATCH_CLAUSE:{
//			listRewrite = rewrite.getListRewrite(((CatchClause)parentASTNode).getBody(), 
//					Block.STATEMENTS_PROPERTY);
//		}break;
//		case ASTNode.IF_STATEMENT:{//directly return null
//		}break;
//		case ASTNode.METHOD_DECLARATION: {
//			ASTNode block = ((MethodDeclaration) parentASTNode).getBody();
//			listRewrite = rewrite.getListRewrite(
//					block, Block.STATEMENTS_PROPERTY);		
//		}break;
//		case ASTNode.SWITCH_STATEMENT: {
//			listRewrite = rewrite.getListRewrite(parentASTNode, SwitchStatement.STATEMENTS_PROPERTY);
//		}break;
//		case ASTNode.TRY_STATEMENT: {//the child must be a catch clause			
//			listRewrite = rewrite.getListRewrite(parentASTNode, TryStatement.CATCH_CLAUSES_PROPERTY);
//		}break;
//		default: {
//			listRewrite = rewrite.getListRewrite(parentASTNode, Block.STATEMENTS_PROPERTY);					
//		}break;
//		}
//		return listRewrite;
//	}
//	
//	private ASTNode lookforASTNode(CompilationUnit cu, SourceCodeRange range){
//		ASTNodeFinder finder = new ASTNodeFinder();
//		ASTNode astNode = finder.lookforASTNode(cu, range);
//		return astNode;
//	}
//}