package org.eclipse.jdt.core.dom;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaProject;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.mcgill.cs.swevo.ppa.PPAOptions;
import ca.mcgill.cs.swevo.ppa.tests.NameVisitor;
import ca.mcgill.cs.swevo.ppa.tests.SetupTestUtil;

@SuppressWarnings("restriction")
public class PPAASTParserUtilTest {
	private static CompilationUnit cu1;
	private static NameVisitor cu1Visitor;

	
	@BeforeClass
	public static void setupSuite() {
		try {
			IJavaProject javaProject = SetupTestUtil.setupJavaProject();
			IProject project = javaProject.getProject();

			IFile file = SetupTestUtil.copyJavaSourceFile(project, "sourcefiles/parser", "p1", "ParserA.java");
			ICompilationUnit icu = (ICompilationUnit) JavaCore.create(file);

			PPATypeRegistry registry = new PPATypeRegistry(
					(JavaProject) JavaCore.create(icu.getUnderlyingResource()
							.getProject()));
			ASTNode node = null;
			PPAASTParser parser2 = new PPAASTParser(AST.JLS3);
			parser2.setStatementsRecovery(true);
			parser2.setResolveBindings(true);
			parser2.setSource(icu);
			node = parser2.createAST(null);
			PPAEngine ppaEngine = new PPAEngine(registry, new PPAOptions());

			cu1 = (CompilationUnit) node;

			
			ppaEngine.addUnitToProcess(cu1);
			ppaEngine.doPPA();
			ppaEngine.reset();
			
			cu1Visitor = new NameVisitor();
			cu1.accept(cu1Visitor);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGenerics() {
		Name f1 = cu1Visitor.getNames().get("entries").get(0);

		IVariableBinding f1Binding = (IVariableBinding) f1.resolveBinding();

		assertEquals("FBinding: java.util.Map<p1.Allo,p1.Holla> p1.ParserA:entries", PPABindingsUtil.getBindingText(f1Binding));
	}
	
	@Test
	public void testAnnotations() {
		Name n1 = cu1Visitor.getNames().get("Entity").get(1);
		
		ITypeBinding n1Binding = (ITypeBinding) n1.resolveBinding();
		assertTrue(n1Binding.isAnnotation());
	}
}
