package changeassistant.changesuggestion.astrewrite;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class BigCompiler {

	public static boolean compile(String filePath) throws IOException{
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector diagnostics = new DiagnosticCollector();
		StandardJavaFileManager fileManager = 
			compiler.getStandardFileManager(diagnostics, null, null);
		List<String> options = new ArrayList<String>();
		options.add("-classpath");
		StringBuilder sb = new StringBuilder();
//		URL url2 = new URL("file:/Users/mn8247/Software/workspaceForStaticAnalysis/changeassistant/simpack-647-bin.jar");
//		System.out.println(url2);
		URLClassLoader urlClassLoader = (URLClassLoader)Thread.currentThread().getContextClassLoader();
		for(URL url : urlClassLoader.getURLs()){
			sb.append(url.getFile()).append(File.pathSeparator);
		}
		options.add(sb.toString());
		Iterable<? extends JavaFileObject> compilationUnits = 
			fileManager.getJavaFileObjectsFromStrings(Arrays.asList(filePath));
		JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager,
				diagnostics, options, null, compilationUnits);
		Boolean success = task.call();
		Diagnostic diagnostic;
		for(Object obj : diagnostics.getDiagnostics()){
			System.out.println((Diagnostic)obj);
		}
		fileManager.close();
		System.out.println("Succes: " + success);
		return success.booleanValue();
	}
	
	public static void main(String[] args){
		String filePath = "/Users/mn8247/Software/runtime-EclipseApplication/" +
				"org.eclipse.compare_v20060605/compare/org/eclipse/" +
				"compare/CompareEditorInput.java";
		
		try {
			System.out.println(compile(filePath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
