package ca.mcgill.cs.swevo.ppa.ui;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;

import ca.mcgill.cs.swevo.ppa.PPAOptions;

public class PPAUtilTest {

	private String snippet1;
	private File snippet1f;
	private String snippet2;
	private File snippet2f;
	private String snippet3;
	private File snippet3f;

	@Before
	public void setUp() {
		String mainPath = null;
		try {
			Bundle ppaTestBundle = Platform.getBundle("ca.mcgill.cs.swevo.ppa.ui.tests");
			mainPath = FileLocator.toFileURL(ppaTestBundle.getEntry("sourcefiles/snippets"))
					.getFile();
			snippet1f = new File(mainPath, "java1.java");
			FileInputStream fis = new FileInputStream(snippet1f);
			snippet1 = IOUtils.toString(fis);
			fis.close();
			snippet2f = new File(mainPath, "java2.java");
			fis = new FileInputStream(snippet2f);
			snippet2 = IOUtils.toString(fis);
			fis.close();
			snippet3f = new File(mainPath, "java3.java");
			fis = new FileInputStream(snippet3f);
			snippet3 = IOUtils.toString(fis);
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@After
	public void tearDown() {
		PPAUtil.cleanUpAll();
	}

	@Test
	public void testSnippet() {
		System.out.println(snippet1);
		CompilationUnit cu1 = PPAUtil.getCU(snippet1f, new PPAOptions());
		NameMapVisitor nmv = new NameMapVisitor(true,false);
		cu1.accept(nmv);
		nmv.print();
		assertEquals(13, nmv.getBindings().size());

		System.out.println(snippet1);
		cu1 = PPAUtil.getCU(snippet1, new PPAOptions());
		nmv = new NameMapVisitor(true,false);
		cu1.accept(nmv);
		nmv.print();
		assertEquals(13, nmv.getBindings().size());

		System.out.println(snippet2);
		ASTNode node = PPAUtil.getSnippet(snippet2, new PPAOptions(), true);
		nmv = new NameMapVisitor(true,false);
		node.accept(nmv);
		nmv.print();
		assertEquals(17, nmv.getBindings().size());

		node = PPAUtil.getSnippet(snippet2, new PPAOptions(), true);
		nmv = new NameMapVisitor(true,true);
		node.accept(nmv);
		nmv.print();
		assertEquals(15, nmv.getBindings().size());

		System.out.println(snippet3);
		node = PPAUtil.getSnippet(snippet3, new PPAOptions(), false);
		nmv = new NameMapVisitor(true,false);
		node.accept(nmv);
		nmv.print();
		assertTrue(true);
		assertEquals(17, nmv.getBindings().size());

		node = PPAUtil.getSnippet(snippet3, new PPAOptions(), false);
		nmv = new NameMapVisitor(true,true);
		node.accept(nmv);
		nmv.print();
		assertTrue(true);
		assertEquals(14, nmv.getBindings().size());
	}

	@Test
	public void testWrongSnippet() {
		ASTNode node = PPAUtil.getSnippet(snippet1, new PPAOptions(), true);
		NameMapVisitor nmv = new NameMapVisitor(true,false);
		node.accept(nmv);
		nmv.print();
		assertEquals(15,nmv.getBindings().size());

		node = PPAUtil.getSnippet(snippet1, new PPAOptions(), false);
		nmv = new NameMapVisitor(true,false);
		node.accept(nmv);
		nmv.print();
		assertEquals(18,nmv.getBindings().size());

		CompilationUnit cu1 = PPAUtil.getCU(snippet2f, new PPAOptions());
		nmv = new NameMapVisitor(true,false);
		cu1.accept(nmv);
		nmv.print();
		assertEquals(7,nmv.getBindings().size());
		
		node = PPAUtil.getSnippet(snippet2, new PPAOptions(), false);
		nmv = new NameMapVisitor(true,false);
		node.accept(nmv);
		nmv.print();
		assertEquals(12,nmv.getBindings().size());

		cu1 = PPAUtil.getCU(snippet3f, new PPAOptions());
		nmv = new NameMapVisitor(true,false);
		cu1.accept(nmv);
		nmv.print();
		assertEquals(0,nmv.getBindings().size());
		
		node = PPAUtil.getSnippet(snippet3, new PPAOptions(), true);
		nmv = new NameMapVisitor(true,false);
		node.accept(nmv);
		nmv.print();
		assertEquals(5,nmv.getBindings().size());
	}

}
