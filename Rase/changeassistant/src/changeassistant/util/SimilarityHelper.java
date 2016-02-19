package changeassistant.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import changeassistant.main.ChangeAssistantMain;
import changeassistant.peers.comparison.ASTMethodBodyTransformer;
import changeassistant.peers.comparison.Node;

class CompareResult {
	double precision;
	double recall;
	double accuracy;

	public CompareResult(double precision, double recall, double accuracy) {
		this.precision = precision;
		this.recall = recall;
		this.accuracy = accuracy;
	}
}

public class SimilarityHelper {

	public static int NUM = 6;

	public static CompareResult basicCompare(File[] files,
			Map<String, Node> knownMethods) {
		BufferedReader br = null;
		String line = null, className = null, signature = null, firstLine = null;
		String productName = null;
		StringBuffer secondBuffer = new StringBuffer();
		Matcher matcher = null;
		Node node1 = null, node2 = null;
		double sim = 0;
		ChangeAssistantMain computer = new ChangeAssistantMain();
		Pattern pat = Pattern
				.compile("[0-9a-zA-Z-_]+\t[a-zA-Z.]+  [0-9a-zA-Z_]+\\([0-9a-zA-Z,\\s\\[\\]]*\\)  \\(start position = [0-9]+  length = [0-9]+\\)");
		Pattern methodHeadPat = Pattern
				.compile("(public |private |protected )[[a-zA-Z\\[\\]]+ ]*[a-zA-Z]+\\u0028.*\\u0029\\u007B");
		int counter = 0;
		int counterOfFoundMethod = 0;
		File file = null;

		for (int i = 0; i < files.length; i++) {
			file = files[i];
			try {
				br = new BufferedReader(new InputStreamReader(
						new FileInputStream(file)));
				line = br.readLine();
				productName = null;
				while (line != null) {
					if (methodHeadPat.matcher(line.trim()).find()) {
						if (methodHeadPat.matcher(line.trim()).find()) {
							System.out.println(line.trim());
							counterOfFoundMethod++;
						}
					}
					matcher = pat.matcher(line.trim());
					if (matcher.matches()) {
						productName = line.substring(line.indexOf('-') + 1,
								line.indexOf("\t"));
						line = line.substring(line.indexOf("\t") + 1).trim();
						className = line.substring(0, line.indexOf(" "));
						// counterOfFoundMethod++;
					}
					if (productName == null) {
						signature = className + " " + line.trim();
					} else {
						signature = productName + " " + className + " "
								+ line.trim();
					}
					if (knownMethods.containsKey(signature)) {
						firstLine = line;
						node1 = knownMethods.get(signature);
						secondBuffer = new StringBuffer();
						while (line != null
								&& !(!line.isEmpty() && line.charAt(0) <= '9' && line
										.charAt(0) >= '0')
								&& !line.startsWith("There are")) {
							secondBuffer.append(line);
							line = br.readLine();
						}
						node2 = constructMethodNode(secondBuffer.toString());
						counter++;
						double tmpSim = computer.computeSimilarity(
								(Node) node1.deepCopy(), node2);
						System.out.println(signature + " " + firstLine + " "
								+ tmpSim);
						sim += tmpSim;
					} else {
						line = br.readLine();
					}
				}
				br.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		CompareResult result = new CompareResult(counter * 1.0
				/ counterOfFoundMethod, counter * 1.0 / knownMethods.size(),
				sim / counter);
		return result;
	}

	private static File[] getIndexedFiles(final int indexOfGroup, File dir) {
		File[] files = dir.listFiles(new FileFilter() {
			Pattern pattern = Pattern.compile(Integer.toString(indexOfGroup)
					+ "_.*_[0-9]+\\u002Etxt");

			public boolean accept(File file) {
				return pattern.matcher(file.getName()).matches();
			}
		});
		return files;
	}

	private static Map<String, Node> readKnownMethods() {
		File f = new File(
				"/Users/mn8247/Software/workspaceForStaticAnalysis/changeassistant/tmp/known_methods.txt");
		Map<String, Node> knownMethods = new HashMap<String, Node>();
		String signature = null;
		StringBuffer firstBuffer = null;
		String firstLine = null;
		String className = null;
		String productName = null;
		Node node = null;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(f)));
			String line = null;
			line = br.readLine();
			while (line != null) {
				if (line.startsWith("//")
						&& !line.startsWith("//end of method")) {
					if (line.contains(" ")) {
						productName = line.substring(2, line.indexOf(" "));
						className = line.substring(line.indexOf(" ") + 1);
					} else {
						className = line.substring(2);
					}
				} else {
					firstBuffer = new StringBuffer();
					if (line.contains(" ("))
						line = line.substring(0, line.indexOf(" (")) + "("
								+ line.substring(line.indexOf(" (") + 2);
					if (line.contains(") "))
						line = line.substring(0, line.indexOf(") ")) + ")"
								+ line.substring(line.indexOf(") ") + 2);
					String[] segs = line.split(", ");
					StringBuffer buff = new StringBuffer();
					for (int i = 0; i < segs.length; i++) {
						if (i == 0) {
							buff.append(segs[i]);
						} else {
							buff.append("," + segs[i]);
						}
					}
					line = buff.toString();
					firstLine = line;
					while (line != null && !line.equals("//end of method")) {
						firstBuffer.append(line.trim()).append("\n");
						line = br.readLine();
					}
					node = constructMethodNode(firstBuffer.toString());
					if (productName == null) {
						signature = className + " " + firstLine.trim();
					} else {
						signature = productName + " " + className + " "
								+ firstLine.trim();
					}
					knownMethods.put(signature, node);
					if (line == null)
						break;
				}
				line = br.readLine();
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return knownMethods;
	}

	/**
	 * compare suggested methods with the known methods. Include a product
	 * version name if any, and a class name.
	 */
	public static void compare3() {
		Map<String, Node> knownMethods = readKnownMethods();
		File dir = new File(
				"/Users/mn8247/Software/workspaceForStaticAnalysis/changeassistant/tmp");
		double precision = 0, recall = 0, accuracy = 0;
		CompareResult result = null;
		int counter = 0;
		if (dir.isDirectory()) {
			int indexOfGroup = 1;
			for (; indexOfGroup <= NUM; indexOfGroup++) {
				File[] files = getIndexedFiles(indexOfGroup, dir);
				if (files.length != 0) {
					result = basicCompare(files, knownMethods);
					precision += result.precision;
					recall += result.recall;
					accuracy += result.accuracy;
					counter++;
				}
			}
			// int counter = indexOfGroup - 1;
			System.out.println("precision = " + precision / counter
					+ ", recall = " + recall / counter + ", accuracy = "
					+ accuracy / counter);
		}
	}

	/**
	 * compare suggested methods with the known methods for product line code
	 */
	public static void compare1() {
		File f = new File(
				"/Users/mn8247/Software/workspaceForStaticAnalysis/changeassistant/tmp/known_methods.txt");
		Map<String, Node> knownMethods = new HashMap<String, Node>();
		Pattern pat = Pattern.compile("[0-9]+-[0-9a-z]+");
		// Pattern methodHeadPat = Pattern
		// .compile("[public |private |protected ]*[[a-zA-Z]+ ]*[a-zA-Z]+\\u0028.*\\u0029\\u007B");
		Pattern methodHeadPat = Pattern
				.compile("public |private |protected [[a-zA-Z]+ ]*[a-zA-Z]+\\u0028.*\\u0029\\u007B");
		Matcher matcher = null;
		StringBuffer firstBuffer = null;
		StringBuffer secondBuffer = new StringBuffer();
		String firstLine = null;
		String productName = null;
		int counterOfFoundMethod = 0;
		Node node = null, node1 = null, node2 = null;
		ChangeAssistantMain computer = new ChangeAssistantMain();
		int counter = 0;
		double sim = 0;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(f)));
			String line = null;
			line = br.readLine();
			while (line != null) {
				if (line.startsWith("//")
						&& !line.startsWith("//end of method")) {
					productName = line.substring(2);
				} else {
					firstBuffer = new StringBuffer();
					if (line.contains(" ("))
						line = line.substring(0, line.indexOf(" (")) + "("
								+ line.substring(line.indexOf(" (") + 2);
					if (line.contains(") "))
						line = line.substring(0, line.indexOf(") ")) + ")"
								+ line.substring(line.indexOf(") ") + 2);
					String[] segs = line.split(", ");
					StringBuffer buff = new StringBuffer();
					for (int i = 0; i < segs.length; i++) {
						if (i == 0) {
							buff.append(segs[i]);
						} else {
							buff.append("," + segs[i]);
						}
					}
					line = buff.toString();
					firstLine = line;
					while (line != null && !line.equals("//end of method")) {
						firstBuffer.append(line.trim());
						line = br.readLine();
					}
					node = constructMethodNode(firstBuffer.toString());
					knownMethods.put(productName + " " + firstLine, node);
					if (line == null)
						break;
				}
				line = br.readLine();
			}
			br.close();
			File dir = new File(
					"/Users/mn8247/Software/workspaceForStaticAnalysis/changeassistant/tmp");
			if (dir.isDirectory()) {
				File[] files = dir.listFiles(new FileFilter() {
					Pattern pattern = Pattern
							.compile("[0-9]+.*[0-9]+\\u002Etxt");

					public boolean accept(File file) {
						return pattern.matcher(file.getName()).matches();
					}
				});
				Arrays.sort(files);
				counterOfFoundMethod = 0;
				File file = null;
				for (int i = 0; i < files.length; i++) {
					file = files[i];
					br = new BufferedReader(new InputStreamReader(
							new FileInputStream(file)));
					line = br.readLine();
					while (line != null) {
						if (methodHeadPat.matcher(line.trim()).find()) {
							if (methodHeadPat.matcher(line.trim()).find()) {
								System.out.println(line.trim());
								counterOfFoundMethod++;
							}
						}
						matcher = pat.matcher(line.trim());
						if (matcher.find()) {
							productName = matcher.group();
							productName = productName.substring(productName
									.indexOf('-') + 1);
						}
						if (knownMethods.containsKey(productName + " "
								+ line.trim())) {
							firstLine = line;
							node1 = knownMethods.get(productName + " " + line);
							secondBuffer = new StringBuffer();
							while (line != null
									&& !(!line.isEmpty()
											&& line.charAt(0) <= '9' && line
											.charAt(0) >= '0')) {
								secondBuffer.append(line);
								line = br.readLine();
							}
							node2 = constructMethodNode(secondBuffer.toString());
							counter++;
							double tmpSim = computer.computeSimilarity(
									(Node) node1.deepCopy(), node2);
							System.out.println(firstLine + " " + tmpSim);
							sim += tmpSim;
						} else {
							line = br.readLine();
						}
					}
					br.close();
				}
				System.out.println("The number of methods found is "
						+ counterOfFoundMethod);
			}
			System.out.println("The average method similarity is "
					+ (sim = sim / counter));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * compare a known method with a suggested method
	 */
	public static void compare2() {
		File f = new File(
				"/Users/mn8247/Software/workspaceForStaticAnalysis/changeassistant/tmp/tmp.txt");
		String line = null;
		StringBuffer buffer = null;
		BufferedReader br = null;
		Node node = null, node2 = null;
		ChangeAssistantMain computer = new ChangeAssistantMain();
		try {
			br = new BufferedReader(new InputStreamReader(
					new FileInputStream(f)));
			line = br.readLine();
			buffer = new StringBuffer();
			while (line != null && !line.startsWith("//end of method")) {
				buffer.append(line);
				line = br.readLine();
			}
			node = constructMethodNode(buffer.toString());
			if (line != null) {
				buffer = new StringBuffer();
				line = br.readLine();
				while (line != null) {
					buffer.append(line);
					line = br.readLine();
				}
				node2 = constructMethodNode(buffer.toString());
			}
			if (node != null && node2 != null) {
				double tmpSim = computer.computeSimilarity(node, node2);
				System.out.println("The similarity is " + tmpSim);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		// String s =
		// "[0-9a-zA-Z-_]+\t[a-zA-Z.]+  [0-9a-zA-Z_]+\\([0-9a-zA-Z,\\s]*\\)  \\(start position = [0-9]+  length = [0-9]+\\)";
		// Pattern pat = Pattern.compile(s);
		// if (pat.matcher(
		// "13515-win32	org.eclipse.swt.widgets.ExpandBar  WM_LBUTTONUP(int, int)  (start position = 18482  length = 610)")
		// .matches()) {
		// System.out.println("Match");
		// } else {
		// System.out.println("No match");
		// }
		compare3();
		// compare2();
		// compare1();
		// Pattern pat = Pattern
		// .compile("[0-9a-zA-Z-]+\t[a-zA-Z.]+  [0-9a-zA-Z]+\\([0-9a-zA-Z,\\s]*\\)  \\(start position = [0-9]+  length = [0-9]+\\)");
		// String s =
		// "14502-motif	org.eclipse.swt.dnd.DropTarget  DropTarget(Control, int)  (start position = 5153  length = 5548)";
		// if (pat.matcher(s).matches()) {
		// System.out.println("Matched");
		// } else {
		// System.out.println("Not matched");
		// }
	}

	private static Node constructMethodNode(String bodyStr) {
		ASTMethodBodyTransformer transformer = new ASTMethodBodyTransformer();
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(bodyStr.toCharArray());
		parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
		ASTNode astNode = parser.createAST(null);
		MethodDeclaration m = (((TypeDeclaration) astNode).getMethods())[0];
		Node node = transformer.createMethodBodyTree(m);
		return node;

	}
}
