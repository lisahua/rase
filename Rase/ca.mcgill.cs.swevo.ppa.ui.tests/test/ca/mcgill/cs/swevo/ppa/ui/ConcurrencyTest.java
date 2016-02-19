package ca.mcgill.cs.swevo.ppa.ui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.Bundle;

import ca.mcgill.cs.swevo.ppa.PPAOptions;

public class ConcurrencyTest {

	private static final int BOUND = 8;
	private final File[] files = new File[BOUND];
	private final String[] snippets = new String[BOUND];

	@Before
	public void setUp() {
		String mainPath = null;
		String mainPathSnippet = null;
		try {
			Bundle ppaTestBundle = Platform
					.getBundle("ca.mcgill.cs.swevo.ppa.ui.tests");
			mainPath = FileLocator.toFileURL(
					ppaTestBundle.getEntry("sourcefiles/complete")).getFile();
			mainPathSnippet = FileLocator.toFileURL(
					ppaTestBundle.getEntry("sourcefiles/snippets")).getFile();
			for (int i = 0; i < BOUND; i++) {
				files[i] = new File(mainPath, "PPAASTParser" + i + ".java");
				File tempFile = new File(mainPathSnippet, "snippet" + i + ".java");
				FileInputStream fis = new FileInputStream(tempFile);
				snippets[i] = IOUtils.toString(fis);
				fis.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private long runConcurrent(final int run, final boolean multiple) {
		Thread[] threads = new Thread[BOUND];

		for (int i = 0; i < BOUND; i++) {
			final int index = i;
			threads[i] = new Thread() {

				@Override
				public void run() {
					List<File> filesList = new ArrayList<File>();
					if (multiple) {
						filesList.add(files[(index + run + 5) % BOUND]);
						filesList.add(files[(index + run) % BOUND]);
					}
					filesList.add(files[(index + run + 2) % BOUND]);
					List<CompilationUnit> cus = PPAUtil.getCUs(filesList,
							new PPAOptions());
					NameBindingVisitor visitor = new NameBindingVisitor(
							new NullPrinter());
					cus.get(0).accept(visitor);
					if (multiple) {
						cus.get(1).accept(visitor);
						cus.get(2).accept(visitor);
					}
					PPAUtil.cleanUpAll();
				}
			};
		}

		long time = System.currentTimeMillis();
		for (int i = 0; i < BOUND; i++) {
			threads[i].start();
		}
		for (int i = 0; i < BOUND; i++) {
			try {
				threads[i].join();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		time = System.currentTimeMillis() - time;

		System.out.println("Concurrent time: " + time);

		return time;
	}

	private long runSequential(int run, boolean multiple) {
		long time = System.currentTimeMillis();
		for (int i = 0; i < BOUND; i++) {
			List<File> filesList = new ArrayList<File>();
			if (multiple) {
				filesList.add(files[(i + run + 5) % BOUND]);
				filesList.add(files[(i + run) % BOUND]);
			}
			filesList.add(files[(i + run + 2) % BOUND]);
			List<CompilationUnit> cus = PPAUtil.getCUs(filesList,
					new PPAOptions());
			NameBindingVisitor visitor = new NameBindingVisitor(
					new NullPrinter());
			cus.get(0).accept(visitor);
			if (multiple) {
				cus.get(1).accept(visitor);
				cus.get(2).accept(visitor);
			}
			PPAUtil.cleanUpAll();
		}
		time = System.currentTimeMillis() - time;
		System.out.println("Sequential time: " + time);

		return time;
	}

	@Test
	@Ignore
	public void testConcurrencyBenchmark() {
		long concurrentTimeAvg = 0;
		long sequentialTimeAvg = 0;
		for (int i = 0; i < 11; i++) {
			long tempTime = runConcurrent(i, true);
			if (i != 0) {
				concurrentTimeAvg += tempTime;
			}

		}

		for (int i = 0; i < 11; i++) {
			long tempTime = runSequential(i, true);
			if (i != 0) {
				sequentialTimeAvg += tempTime;
			}
		}

		System.out.println("Average Concurrent Time Multiple: "
				+ (concurrentTimeAvg / 10.0));
		System.out.println("Average Sequential Time Multiple: "
				+ (sequentialTimeAvg / 10.0));
	}

	@Test
	@Ignore
	public void testConcurrencyBenchmark2() {
		long concurrentTimeAvg = 0;
		long sequentialTimeAvg = 0;
		for (int i = 0; i < 11; i++) {
			long tempTime = runConcurrent(i, false);
			if (i != 0) {
				concurrentTimeAvg += tempTime;
			}

		}

		for (int i = 0; i < 11; i++) {
			long tempTime = runSequential(i, false);
			if (i != 0) {
				sequentialTimeAvg += tempTime;
			}
		}

		System.out.println("Average Concurrent Time Single: "
				+ (concurrentTimeAvg / 10.0));
		System.out.println("Average Sequential Time Single: "
				+ (sequentialTimeAvg / 10.0));
	}
	
	private long runConcurrentSnippet(final int run) {
		Thread[] threads = new Thread[BOUND];

		for (int i = 0; i < BOUND; i++) {
			final int index = i;
			threads[i] = new Thread() {

				@Override
				public void run() {
					File file = files[(index) % BOUND];
					ASTNode node = PPAUtil.getSnippet(file, new PPAOptions(), false);
					NameBindingVisitor visitor = new NameBindingVisitor(
							new NullPrinter());
					node.accept(visitor);
					PPAUtil.cleanUpSnippet();
				}
			};
		}

		long time = System.currentTimeMillis();
		for (int i = 0; i < BOUND; i++) {
			threads[i].start();
		}
		for (int i = 0; i < BOUND; i++) {
			try {
				threads[i].join();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		time = System.currentTimeMillis() - time;

		System.out.println("Concurrent time snippet: " + time);

		return time;
	}
	
	private long runSequentialSnippet(int run) {
		long time = System.currentTimeMillis();
		for (int i = 0; i < BOUND; i++) {
			File file = files[(i) % BOUND];
			ASTNode node = PPAUtil.getSnippet(file, new PPAOptions(), false);
			NameBindingVisitor visitor = new NameBindingVisitor(
					new NullPrinter());
			node.accept(visitor);
			PPAUtil.cleanUpSnippet();
		}
		time = System.currentTimeMillis() - time;
		System.out.println("Sequential time: " + time);

		return time;
	}
	
	@Test
	public void testConcurrencySnippet() {
		long concurrentTimeAvg = 0;
		long sequentialTimeAvg = 0;
		for (int i = 0; i < 11; i++) {
			long tempTime = runConcurrentSnippet(i);
			if (i != 0) {
				concurrentTimeAvg += tempTime;
			}

		}

		for (int i = 0; i < 11; i++) {
			long tempTime = runSequentialSnippet(i);
			if (i != 0) {
				sequentialTimeAvg += tempTime;
			}
		}

		System.out.println("Average Concurrent Time Snippet: "
				+ (concurrentTimeAvg / 10.0));
		System.out.println("Average Sequential Time Snippet: "
				+ (sequentialTimeAvg / 10.0));
	}
}

class NullPrinter extends PrintStream {

	public NullPrinter() {
		super(new ByteArrayOutputStream());
	}

	@Override
	public PrintStream append(char arg0) {
		return this;
	}

	@Override
	public PrintStream append(CharSequence arg0, int arg1, int arg2) {
		return this;
	}

	@Override
	public PrintStream append(CharSequence arg0) {
		return this;
	}

	@Override
	public boolean checkError() {
		return false;
	}

	@Override
	protected void clearError() {
	}

	@Override
	public void close() {
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub
		super.flush();
	}

	@Override
	public PrintStream format(Locale arg0, String arg1, Object... arg2) {
		return this;
	}

	@Override
	public PrintStream format(String arg0, Object... arg1) {
		return this;
	}

	@Override
	public void print(boolean arg0) {
	}

	@Override
	public void print(char arg0) {
	}

	@Override
	public void print(char[] arg0) {
	}

	@Override
	public void print(double arg0) {
	}

	@Override
	public void print(float arg0) {
	}

	@Override
	public void print(int arg0) {
	}

	@Override
	public void print(long arg0) {
	}

	@Override
	public void print(Object arg0) {
	}

	@Override
	public void print(String arg0) {
	}

	@Override
	public PrintStream printf(Locale arg0, String arg1, Object... arg2) {
		return this;
	}

	@Override
	public PrintStream printf(String arg0, Object... arg1) {
		return this;
	}

	@Override
	public void println() {
	}

	@Override
	public void println(boolean arg0) {
	}

	@Override
	public void println(char arg0) {
	}

	@Override
	public void println(char[] arg0) {
	}

	@Override
	public void println(double arg0) {
	}

	@Override
	public void println(float arg0) {
	}

	@Override
	public void println(int arg0) {
		// TODO Auto-generated method stub
		super.println(arg0);
	}

	@Override
	public void println(long arg0) {
	}

	@Override
	public void println(Object arg0) {
	}

	@Override
	public void println(String arg0) {
	}

	@Override
	protected void setError() {
	}

	@Override
	public void write(byte[] arg0, int arg1, int arg2) {
	}

	@Override
	public void write(int arg0) {
	}

}
