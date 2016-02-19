import static junit.framework.Assert.assertEquals;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import ca.mcgill.cs.swevo.ppa.PPAOptions;
import ca.mcgill.cs.swevo.ppa.ui.NameMapVisitor;
import ca.mcgill.cs.swevo.ppa.ui.PPAUtil;

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