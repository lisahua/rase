package changeassistant.changesuggestion.astrewrite;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

//import ca.mcgill.cs.swevo.ppa.PPAOptions;
//import ca.mcgill.cs.swevo.ppa.ui.PPAUtil;
import changeassistant.classhierarchy.ClassContext;
import changeassistant.classhierarchy.ProjectResource;
import changeassistant.internal.WorkspaceUtilities;
import changeassistant.main.ChangeAssistantMain;
import changeassistant.util.PropertyLoader;

public class ManipulatorHelper {
	private Random rnd = null;
	private File tmpDir = null;
	
	public ManipulatorHelper() {
		rnd = new Random(System.currentTimeMillis());
		tmpDir = new File(PropertyLoader.props.getProperty("Temp_File_Path"));
	}
	
	public boolean MODIFY_ORIGINAL = false;
	
	public IPath path = null;
	
	public IPath backPath;

	private IProject iproject;
	
	private ITextFileBufferManager bufferManager = FileBuffers
		       .getTextFileBufferManager();//get the buffer manager
	
	private ITextFileBuffer textFileBuffer;
	
	/**
	 * Implementing classes provide a {@link TextEdit} object. The way to obtain
	 * such an instance differs when the AST is directly manipulated or changes
	 * are logged to an {@link ASTRewrite} instance. To avoid to write two save
	 * methods, this interface has been created.
	 * <p>
	 * Project page: <a target="_blank"
	 * href="http://sourceforge.net/projects/earticleast">http://sourceforge.net/projects/earticleast</a>
	 * </p>
	 *
	 * @author Thomas Kuhn
	 */
	public interface TextEditProvider {

		/**
		 * Provides a {@link TextEdit} document.
		 *
		 * @param document
		 *            the docuement the {@link TextEdit} object will be applied
		 *            to
		 * @return the {@link TextEdit} instance
		 */
		TextEdit getTextEdit(IDocument document);
	}
	
	public void clear(){
		try {
			ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path, LocationKind.LOCATION);
			textFileBuffer.commit(null, true);
			bufferManager.disconnect(path, LocationKind.LOCATION, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		path = null;
		backPath = null;
		iproject = null;
	}
	
	public void commit(CompilationUnit unit, ASTRewrite rewrite) throws CoreException{
		try {						
			// connect the path
			bufferManager.connect(path, null);

			ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path);
				
			System.out.println(textFileBuffer.getDocument().get());
//			textFileBuffer.commit(null, false);
		} catch (MalformedTreeException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			// disconnect the path
			bufferManager.disconnect(path, null);
		}
	}
	
	//the copy is always created from text buffer
	public CompilationUnit createCopy() throws CoreException{
		CompilationUnit cu = null;
//		ITextFileBufferManager bufferManager = FileBuffers
//					.getTextFileBufferManager();//get the buffer manager
		try {	
//			bufferManager.connect(path, LocationKind.LOCATION, null);

			textFileBuffer = bufferManager.getTextFileBuffer(path, LocationKind.LOCATION);
			// retrieve the buffer
			IDocument document = textFileBuffer.getDocument();
						
			if(ChangeAssistantMain.UsePPA){
//				cu = PPAUtil.getCU(document.get(), new PPAOptions());
			}else{
				ASTParser parser = ASTParser.newParser(AST.JLS3);
				parser.setProject(JavaCore.create(iproject));
				parser.setResolveBindings(true);
				parser.setSource(document.get().toCharArray());
				cu = (CompilationUnit)parser.createAST(null);
				parser = null;
			}
			textFileBuffer = null;
			document = null;
		} catch (MalformedTreeException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			// disconnect the path
//			bufferManager.disconnect(path, LocationKind.LOCATION, null);
//			bufferManager = null;
		}
//		return cu;			
//		try{					
//			FileReader fr = new FileReader(path.toOSString());
//			char[] chars = new char[1024];
//			List<Character> charList = new ArrayList<Character>();
//			int charread = 0;
//			while((charread = fr.read(chars)) != -1){
//				for(int i = 0; i < charread; i++){
//					charList.add(chars[i]);
//				}
//			}
//			char[] charResult = new char[charList.size()];
//			for(int i = 0; i < charList.size(); i++){
//				charResult[i] = charList.get(i);
//			}
//			ASTParser parser = ASTParser.newParser(AST.JLS3);
//			parser.setSource(charResult);
//			cu = (CompilationUnit)parser.createAST(null);
//			
//		}catch (FileNotFoundException e) {		
//			e.printStackTrace();
//		} catch (IOException e) {			
//			e.printStackTrace();
//		}	
//			StringBuffer sb = new StringBuffer();
//			FileInputStream in;
//			try {
//				in = new FileInputStream(new File(path.toOSString()));
//				byte[] buffer = new byte[1024];
//				int byteread = 0;
//				while((byteread = in.read(buffer)) != -1){
//					sb.append(buffer.toString());
//				}
//				in.close();
//				ASTParser parser = ASTParser.newParser(AST.JLS3);
//				parser.setSource(sb.toString().toCharArray());
//				cu = (CompilationUnit)parser.createAST(null);
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		return cu;
	}
	
//	public static CompilationUnit initializeCopy(ProjectResource pr, String classname, boolean flag){
//		CompilationUnit cu = null; 
//		if(flag){
//			backPath = new Path(PropertyLoader.props.getProperty("Temp_File_Path")
//					+ classname + ".java");
//			File newFile = new File(backPath.toOSString());
//			if(newFile.exists()){
//				try {
//					cu = createCopy();
//				} catch (CoreException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}else{
//				System.out.println("The new file does not exist!");
//			}
//		}
//		return cu;
//	}
	
/**
 * make a copy for the java file to change in the temp file path 
 * @param pr
 * @param classname
 */
	public void initializeCopy(ProjectResource pr, String classname){
		ClassContext cc = pr.findClassContext(classname);
		iproject = pr.getIProject();
		String className = WorkspaceUtilities.getSimpleName(cc.name) + "123";//to guarantee that the prefix is not too short
		File tmpFile = null;
		while(true){
			try {
				tmpFile = File.createTempFile(className, ".java", tmpDir);
				break;
			} catch (IOException e) {
				//do nothing, continue generating a temp file
			}
		}

		backPath = new Path(tmpFile.getAbsolutePath()); 
//		new Path(PropertyLoader.props.getProperty("Temp_File_Path") + 
//					WorkspaceUtilities.getSimpleName(cc.name) + ".java");
		//the original file's location
		path = new Path(pr.projectLocation + File.separator + cc.relativeFilePath);
		WorkspaceUtilities.copyFile(path, backPath);	
		if(MODIFY_ORIGINAL){
			//do nothing
		}else{
			path = backPath;
		}
		try {
			bufferManager.connect(path, LocationKind.LOCATION, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	public static void print(CompilationUnit cu, 
//			final ASTRewrite rewrite) throws CoreException{
//		TextEditProvider tep = new TextEditProvider(){
//			@Override
//			public TextEdit getTextEdit(IDocument document) {
//				// TODO Auto-generated method stub
//				TextEdit te = rewrite.rewriteAST(document, null);
//				return te;
//			}
//		};
//		
//		ITextFileBufferManager bufferManager = FileBuffers
//									.getTextFileBufferManager();//get the buffer manager
//		IPath path = cu.getJavaElement().getPath();
//		try {
//			// connect the path
//			bufferManager.connect(path, null);
//
//			ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path);
//			
//			// retrieve the buffer
//			IDocument document = textFileBuffer
//					.getDocument();
//
//			// ask the textEditProvider for the change information
//			TextEdit edit = tep.getTextEdit(document);
//
//			// apply the changes to the document
//			edit.apply(document);
//			
//			System.out.println(document.get());
//
////			textFileBuffer.commit(null, false);
//		} catch (MalformedTreeException e) {
//			e.printStackTrace();
//		} catch (BadLocationException e) {
//			e.printStackTrace();
//		} catch (Exception e){
//			e.printStackTrace();
//		} finally {
//			// disconnect the path
//			bufferManager.disconnect(path, null);
//		}
//	}
	
	/**
	 * Writes changes to Java source. This method does not distinguish how the
	 * AST was changed.
	 *
	 * @param unit
	 *            the AST node of the compilation unit that has been changed
	 * @param textEditProvider
	 *            provides the change information
	 * @throws CoreException
	 *             thrown if file paths cannot be connected or disconnected
	 */
	@SuppressWarnings("deprecation")
	public void save(CompilationUnit unit,
			TextEditProvider textEditProvider) throws CoreException {
//		IPath path = unit.getJavaElement().getPath();//unit: instance of CompilationUnit
		try {
			// connect the path
//			bufferManager.connect(path, null);
//			bufferManager.connect(path, LocationKind.LOCATION, null);

//			ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path);
//			IFileBuffer[] buffers = bufferManager.getFileBuffers();
			textFileBuffer = bufferManager.getTextFileBuffer(path, LocationKind.LOCATION);
			// retrieve the buffer
			IDocument document = textFileBuffer.getDocument();
			
			// ask the textEditProvider for the change information
			TextEdit edit = textEditProvider.getTextEdit(document);

			// apply the changes to the document
			edit.apply(document);
			
//			System.out.println(document.get());

			textFileBuffer.commit(null, false);
		
			textFileBuffer = null;
			document = null;
			edit = null;
		} catch (MalformedTreeException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			// disconnect the path
//			bufferManager.disconnect(path, LocationKind.LOCATION, null);
//			bufferManager.disconnect(path, null);
		}
	}

	/**
	 * Convenience method that saves changes that have been made directly to an
	 * AST.
	 *
	 * @param unit
	 *            the unit that contains changes.
	 * @throws CoreException
	 *             forwards the exception from
	 *             {@link #save(CompilationUnit, net.sourceforge.earticleast.app.ManipulatorHelper.TextEditProvider)}
	 */
	public void saveDirectlyModifiedUnit(final CompilationUnit unit)
			throws CoreException {
		save(unit, new TextEditProvider() {
			public TextEdit getTextEdit(IDocument document) {
				return unit.rewrite(document, null);
			}
		});
	}

	/**
	 * Convenience method that saves changes to an AST that have been recorded
	 * in by an instance of {@link ASTRewrite}.
	 *
	 * @param unit
	 *            the unit (in its origininal state)
	 * @param rewrite
	 *            contains the rewrite instructions
	 * @throws CoreException
	 *             forwards the exception from
	 *             {@link #save(CompilationUnit, net.sourceforge.earticleast.app.ManipulatorHelper.TextEditProvider)}
	 */
	public void saveASTRewriteContents(CompilationUnit unit,
			final ASTRewrite rewrite) throws CoreException {
		save(unit, new TextEditProvider() {
			public TextEdit getTextEdit(IDocument document) {
				TextEdit te = rewrite.rewriteAST(document, null);
				return te;
			}
		});
	}
}
