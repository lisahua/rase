package changeassistant.multipleexample.ccfinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * @author ray
 * 
 */
public class RunCCFinder {

	private Hashtable<Integer, String> fileList;
	private Hashtable<String, Integer> reverseFileList;
	private Hashtable<String, StringBuilder> rawPairList;
	private Hashtable<String, StringBuilder> newPairList;
	private ArrayList<Hashtable<Integer, Integer>> lineNumList;

	public RunCCFinder() {
		fileList = new Hashtable<Integer, String>();
		reverseFileList = new Hashtable<String, Integer>();
		rawPairList = new Hashtable<String, StringBuilder>();
		newPairList = new Hashtable<String, StringBuilder>();
		lineNumList = new ArrayList<Hashtable<Integer, Integer>>();
	}

	public void clear() {
		fileList.clear();
		reverseFileList.clear();
		rawPairList.clear();
		newPairList.clear();
		lineNumList.clear();
	}

	public Hashtable<String, StringBuilder> getNewPairList() {
		return newPairList;
	}

	public Hashtable<String, Integer> getReverseFileList() {
		return reverseFileList;
	}

	/*
	 * Test functions.
	 */
	public void testFunction(String test) {
		if (test.equals("fileList")) {
			for (Iterator<Integer> itr = fileList.keySet().iterator(); itr
					.hasNext();) {
				Integer key = itr.next();
				String value = fileList.get(key);
				System.out.println(key + " " + value);
			}
		}
		if (test.equals("reverseFileList")) {
			for (Iterator<String> itr = reverseFileList.keySet().iterator(); itr
					.hasNext();) {
				String key = itr.next();
				Integer value = reverseFileList.get(key);
				System.out.println(key + " " + value);
			}
		}
		if (test.equals("newPairList")) {
			for (Iterator<String> itr = newPairList.keySet().iterator(); itr
					.hasNext();) {
				String key = itr.next();
				StringBuilder value = newPairList.get(key);
				System.out.println(key + " " + value);
			}
		}
		if (test.equals("rawPairList")) {
			for (Iterator<String> itr = rawPairList.keySet().iterator(); itr
					.hasNext();) {
				String key = itr.next();
				StringBuilder value = rawPairList.get(key);
				System.out.println(key + " " + value);
			}
		}
	}

	/*
	 * Run the CcFinder in the program.
	 */
	public void runCcfinder(String oldVersion, String newVersion) {
		String s = null;
		String ccFinder = "/Users/nm8247/Software/ccfinder/ccfx-src/ubuntu32/ccfx";
		String runCommand = ccFinder + " d java -b 3 " + oldVersion + " -is "
				+ newVersion + " -w f+w- ";
		// System.out.print("");
		String showCommand = ccFinder + " p a.ccfxd";
		try {
			Process myRunProcess = Runtime.getRuntime().exec(runCommand);
			int exitValue = myRunProcess.waitFor();
			if (exitValue == 0) {
				myRunProcess.destroy();
			} else {
				System.out.println("The CcFinder can not executed correctly!");
				return;
			}
			Process myShowProcess = Runtime.getRuntime().exec(showCommand);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(
					myShowProcess.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(
					myShowProcess.getErrorStream()));

			// Store the file lists and clone pairs.
			boolean fileFlag = false;
			boolean pairFlag = false;
			while ((s = stdInput.readLine()) != null) {
				String[] tokens = s.split("[\t_ ]+");
				if (fileFlag && !endFileList(tokens)) {
					storeFileListProcess(tokens);
					storeReverseFileList(tokens);
				}
				if (beginFileList(tokens)) {
					fileFlag = true;
				} else if (endFileList(tokens)) {
					fileFlag = false;
				}
				if (pairFlag && !endPairList(tokens)) {
					storeRawPairListProcess(tokens);
				}
				if (beginPairList(tokens)) {
					pairFlag = true;
				} else if (endPairList(tokens)) {
					pairFlag = false;
				}
			}

			readRawFile(oldVersion, newVersion);
			listTransform();

			stdInput.close();
			while ((s = stdError.readLine()) != null) {
				System.out.println(s);
			}
			stdError.close();
			myShowProcess.destroy();
		}

		catch (IOException e) {
			System.out.println("IO exception happened!");
			e.printStackTrace();
			System.exit(-1);
		} catch (InterruptedException e) {
			System.out.println("Interrupted exception happened!");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/*
	 * Store the string to the hashtable.
	 */
	private void storeFileListProcess(String[] tokens) {
		Integer key = new Integer(Integer.parseInt(tokens[0]));
		fileList.put(key, tokens[1]);
	}

	/*
	 * Store the reverse file list.
	 */
	private void storeReverseFileList(String[] tokens) {
		Integer value = new Integer(Integer.parseInt(tokens[0]));
		reverseFileList.put(tokens[1], value);
	}

	/*
	 * Get the files number.
	 */
	public Integer getFileNum(String targetFile) {
		return reverseFileList.get(targetFile);
	}

	/*
	 * Store the clone pairs.
	 */
	private void storeRawPairListProcess(String[] tokens) {
		String key = tokens[1];
		String value = tokens[2];
		if (rawPairList.containsKey(key)) {
			rawPairList.get(key).append(" " + value);
		} else {
			StringBuilder temp = new StringBuilder();
			temp.append(value);
			rawPairList.put(key, temp);
		}
	}

	/*
	 * Read from pre-process files from CCFinder.
	 */
	private void readRawFile(String oldProject, String newProject)
			throws IOException {
		try {
			String suffix = ".java.2_0_0_0.default.ccfxprep";
			for (int index = 1; index <= fileList.size(); index++) {
				String fileName = fileList.get(index);
				String baseName = null;
				String project = null;
				if (fileName.contains(oldProject)) {
					baseName = fileName.substring(oldProject.length());
					project = oldProject;
				} else if (fileName.contains(newProject)) {
					baseName = fileName.substring(newProject.length());
					project = newProject;
				}
				// String dir = ".ccfxprepdir";
				String dir = "";
				String rawFileName = project + /* "/" + */dir + baseName
						+ suffix;
				File rawFile = new File(rawFileName);
				FileReader fr = new FileReader(rawFile);
				BufferedReader br = new BufferedReader(fr);
				String readOneLine;
				int i = 1;
				Hashtable<Integer, Integer> table = new Hashtable<Integer, Integer>();
				while ((readOneLine = br.readLine()) != null) {
					String[] tokens = readOneLine.split("[.]+");
					String result = tokens[0];
					int realLineNum = Integer.parseInt(result, 16);
					table.put(i, realLineNum);
					i++;
				}
				lineNumList.add(table);
				br.close();
				fr.close();
			}
		} catch (FileNotFoundException e) {
			System.out.println("The preprocess file does not exist!");
		} catch (IOException e) {
			System.out.println("The exception happens during input!");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/*
	 * Transform the line information string.
	 */
	private int getLineInfo(String target, int position) {
		String[] tokens = target.split("[.-]+");
		if (position == 0) {
			return Integer.parseInt(tokens[0]);
		} else if (position == 1) {
			return Integer.parseInt(tokens[1]);
		} else if (position == 2) {
			return Integer.parseInt(tokens[2]);
		} else {
			return -1;
		}
	}

	/*
	 * Transform the raw line information string to real line information
	 * string.
	 */
	private String transformLine(String target) {
		int index = getLineInfo(target, 0);
		Integer sLine = getLineInfo(target, 1);
		Integer eLine = getLineInfo(target, 2);

		int newSLine = lineNumList.get(index - 1).get(sLine + 1);
		int newELine = lineNumList.get(index - 1).get(eLine + 1);
		return index + "." + newSLine + "-" + newELine;
	}

	/*
	 * Transform the StringBuilder to the new one.
	 */
	private StringBuilder transformStringBuilder(StringBuilder target) {
		StringBuilder result = new StringBuilder();
		String targetStrings = target.toString();
		String[] tokens = targetStrings.split("[ ]");
		for (String each : tokens) {
			String newEach = transformLine(each);
			result.append(newEach + " ");
		}
		return result;
	}

	/*
	 * Transform from raw list to final new list.
	 */
	private void listTransform() {
		Enumeration<String> e = rawPairList.keys();
		while (e.hasMoreElements()) {
			String rawKey = (String) e.nextElement();
			String newKey = transformLine(rawKey);
			StringBuilder newValue = new StringBuilder();
			newValue = transformStringBuilder(rawPairList.get(rawKey));
			newPairList.put(newKey, newValue);
		}
	}

	/*
	 * 
	 */
	public String getFileName(String fileName) {
		String[] tokens = fileName.split("[.-]");
		return fileList.get(Integer.parseInt(tokens[0]));
	}

	/*
	 * Judge if the string is beginning of the file lists.
	 */
	private boolean beginFileList(String[] tokens) {
		if (tokens[0].equals("source") && tokens[1].equals("files")
				&& tokens[2].equals("{") && tokens.length == 3) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * Judge if the string is the end of the file list.
	 */
	private boolean endFileList(String[] tokens) {
		if (tokens[0].equals("}") && tokens.length == 1) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * Judge if the string is beginning of the clone pairs.
	 */
	private boolean beginPairList(String[] tokens) {
		if (tokens[0].equals("clone") && tokens[1].equals("pairs")
				&& tokens[2].equals("{") && tokens.length == 3) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * Judge if the string is the end of the clone pair.
	 */
	private boolean endPairList(String[] tokens) {
		if (tokens[0].equals("}") && tokens.length == 1) {
			return true;
		} else {
			return false;
		}
	}

}
