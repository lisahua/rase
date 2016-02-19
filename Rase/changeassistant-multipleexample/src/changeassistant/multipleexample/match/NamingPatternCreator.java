package changeassistant.multipleexample.match;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import changeassistant.classhierarchy.ClassContext;
import changeassistant.classhierarchy.ProjectResource;
import changeassistant.internal.WorkspaceUtilities;
import changeassistant.multipleexample.main.CachedProjectMap;
import changeassistant.versions.comparison.ChangedMethodADT;

public class NamingPatternCreator {

	public static String RegForIdentifier = "[a-zA-Z0-9_]+";
	public static String RegForUndefined = ".+";
	public static String RegForDot = "\\u002E";
	public static String RegForLetters = "[a-zA-Z]*";
	public static String RegForNumbers = "[0-9]*";
	public static String RegForAny = "[a-zA-Z0-9_]*";
	public static String RegForAnyPac = "[\\u002E[a-zA-Z0-9]+]*";

	private static List<ClassContext> ccList = null;
	private static List<String> methodList = null;
	private static List<String> packageList = null;
	private static List<String> fileList = null;
	private static List<String> classList = null;
	private static CodePattern pat = null;

	private static String constructRegularExpression(List<String> segRegular,
			String separator) {
		StringBuffer buffer = new StringBuffer();
		String tmpStr = null;
		for (int i = 0; i < segRegular.size(); i++) {
			tmpStr = segRegular.get(i);
			if (RegForAnyPac.equals(tmpStr)) {
				buffer.setLength(buffer.length() - separator.length());
			}
			buffer.append(segRegular.get(i)).append(separator);
		}
		buffer.setLength(buffer.length() - separator.length());
		return buffer.toString();
	}

	private static String createRegularSegments(
			List<List<String>> segmentsList, String separator) {
		List<String> segments = null;
		List<String> segList = new ArrayList<String>(segmentsList.get(0));
		for (int i = 1; i < segmentsList.size(); i++) {
			segList.retainAll(segmentsList.get(i));
		}
		// System.out.print("");
		List<String> segRegular = new ArrayList<String>();
		int index = -1;
		boolean noWildCard = true;
		for (String seg : segList) {
			noWildCard = true;
			for (int i = 0; i < segmentsList.size(); i++) {
				segments = segmentsList.get(i);
				index = segments.indexOf(seg);
				if (index > 0) {
					noWildCard = false;
				}
				try {
					segments = segments.subList(index + 1, segments.size());
				} catch (Exception e) {
					segments = Collections.EMPTY_LIST;
				}
				segmentsList.set(i, segments);
			}
			if (!noWildCard)
				segRegular.add(RegForAny);
			segRegular.add(seg);
		}
		noWildCard = true;
		for (int i = 0; i < segmentsList.size(); i++) {
			segments = segmentsList.get(i);
			if (!segments.isEmpty()) {
				noWildCard = false;
				break;
			}
		}
		if (!noWildCard) {
			int indexOfDot = segments.indexOf(".");
			if (indexOfDot == 0) {
				segRegular.add(RegForAnyPac);
			} else if (indexOfDot == -1) {
				segRegular.add(RegForAny);
			} else {
				segRegular.add(RegForAny);
				segRegular.add(RegForAnyPac);
			}
		}
		return constructRegularExpression(segRegular, separator);
	}

	private static String createRegularSegments(
			List<List<String>> segmentsList, String separator, String regForPack) {
		List<String> segments = null;
		List<String> segList = new ArrayList<String>(segmentsList.get(0));
		for (int i = 1; i < segmentsList.size(); i++) {
			segList.retainAll(segmentsList.get(i));
		}
		System.out.print("");
		List<String> segRegular = new ArrayList<String>();
		int index = -1;
		int maxIndex = -1;
		for (String seg : segList) {
			maxIndex = -1;
			for (int i = 0; i < segmentsList.size(); i++) {
				segments = segmentsList.get(i);
				index = segments.indexOf(seg);
				if (index > 0) {
					if (index > maxIndex)
						maxIndex = index;
				}
				try {
					segments = segments.subList(index + 1, segments.size());
				} catch (Exception e) {
					segments = Collections.EMPTY_LIST;
				}
				segmentsList.set(i, segments);
			}
			if (maxIndex == 1)
				segRegular.add(RegForAny);
			else if (maxIndex > 1)
				// if there is more than one segment between the two, the dot
				// should be also included for match
				segRegular.add(RegForAny + RegForAnyPac);
			segRegular.add(seg);
		}
		maxIndex = -1;
		for (int i = 0; i < segmentsList.size(); i++) {
			segments = segmentsList.get(i);
			if (!segments.isEmpty()) {
				if (maxIndex < segments.size()) {
					maxIndex = segments.size();
				}
			}
		}
		if (maxIndex == 1) {
			segRegular.add(RegForAnyPac);
		} else if (maxIndex > 1) {
			segRegular.add(RegForAny + RegForAnyPac);
		}
		return constructRegularExpression(segRegular, separator);
	}

	public static void init(List<ChangedMethodADT> adts, CodePattern pattern) {
		ccList = new ArrayList<ClassContext>();
		packageList = new ArrayList<String>();
		classList = new ArrayList<String>();
		fileList = new ArrayList<String>();
		methodList = new ArrayList<String>();
		pat = pattern;

		ProjectResource pr = null;
		ClassContext cc = null;
		String wholeClassName = null;
		String className = null;
		String packageName = null;
		String fileName = null;
		String methodName = null;
		for (ChangedMethodADT adt : adts) {
			wholeClassName = adt.classname;

			pr = CachedProjectMap.get(adt.getProjectName());
			// create ccList
			cc = pr.findClassContext(wholeClassName);
			ccList.add(cc);

			// create packageList
			packageName = WorkspaceUtilities.getPackageName(wholeClassName);
			packageList.add(packageName);

			// create classList
			className = WorkspaceUtilities.getSimpleClassName(wholeClassName,
					packageName);
			classList.add(className);

			// create fileList
			fileName = WorkspaceUtilities
					.getSimpleFileName(cc.relativeFilePath);
			fileList.add(fileName);

			// create methodList
			methodName = adt.methodSignature.substring(0,
					adt.methodSignature.indexOf('('));
			methodList.add(methodName);
		}
	}

	private static void partitionString(String name, int counter,
			List<String> segments, StringBuffer buffer) {
		System.out.print("");
		char ch = ' ';
		char ch2 = ' ';
		while (counter < name.length()) {
			ch = name.charAt(counter);
			if (!(ch <= 'Z' && ch >= 'A') && ch != '.') {
				buffer.append(ch);
			} else if (ch == '.') {
				segments.add(buffer.toString());
				buffer = new StringBuffer();
				buffer.append(ch);
			} else {
				if (counter + 1 < name.length()) {
					ch2 = name.charAt(counter + 1);
					if (ch2 <= 'z' && ch2 >= 'a' || ch2 == '.') {
						// the current ch is a head of a seg
						segments.add(buffer.toString());
						buffer = new StringBuffer();
						buffer.append(ch);
					} else {
						buffer.append(ch);
					}
				} else {
					buffer.append(ch);
				}
			}
			counter++;
		}
		if (buffer.length() != 0) {
			segments.add(buffer.toString());
		}
	}

	public static void process(List<ChangedMethodADT> adts, CodePattern pat) {
		init(adts, pat);
		processPackageName();
		processClassName();
		processFileName();
		processMethodName();
	}

	public static void processClassName() {
		List<List<String>> segmentsList = new ArrayList<List<String>>();
		List<String> segments = null;
		int counter = 0;
		StringBuffer buffer = null;
		for (String className : classList) {
			segments = new ArrayList<String>();
			buffer = new StringBuffer();
			buffer.append(className.charAt(0));
			counter = 1;
			partitionString(className, counter, segments, buffer);
			segmentsList.add(segments);
		}
		// System.out.print("");
		String patString = createRegularSegments(segmentsList, "");
		pat.setClassNamingPattern(Pattern.compile(patString));
	}

	public static void processFileName() {
		List<List<String>> segmentsList = new ArrayList<List<String>>();
		List<String> segments = null;
		int counter = 0;
		StringBuffer buffer = null;
		for (String fileName : fileList) {
			segments = new ArrayList<String>();
			buffer = new StringBuffer();
			buffer.append(fileName.charAt(0));
			counter = 1;
			partitionString(fileName, counter, segments, buffer);
			segmentsList.add(segments);
		}
		String patString = createRegularSegments(segmentsList, "");
		int index = patString.lastIndexOf('.');
		if (index != -1) {
			patString = patString.replace(patString.substring(index), RegForDot
					+ patString.substring(index + 1));
		}
		pat.setFileNamingPattern(Pattern.compile(".*" + patString));
	}

	public static void processMethodName() {
		List<List<String>> segmentsList = new ArrayList<List<String>>();
		List<String> segments = null;
		int counter = 0;
		StringBuffer buffer = null;
		for (String methodName : methodList) {
			segments = new ArrayList<String>();
			buffer = new StringBuffer();
			counter = 0;
			partitionString(methodName, counter, segments, buffer);
			segmentsList.add(segments);
		}

		String patString = createRegularSegments(segmentsList, "");
		pat.setMethodNamingPattern(Pattern.compile(patString));
	}

	public static void processPackageName() {
		List<List<String>> segmentsList = new ArrayList<List<String>>();
		List<String> segments = null;
		for (String packageName : packageList) {
			segments = Arrays.asList(packageName.split(RegForDot));
			segmentsList.add(segments);
		}
		String patString = createRegularSegments(segmentsList, RegForDot,
				RegForAnyPac);
		pat.setPackageNamingPattern(Pattern.compile(patString));
	}

	public static void main(String[] args) {
		packageList = new ArrayList<String>();
		packageList.add("org.eclipse.jdt.internal.compiler.ast");
		processPackageName();
	}
}
