package changeassistant.multipleexample.util;

import changeassistant.multipleexample.main.EnhancedChangeAssistantMain;

public class PathUtil {

	public static String createPath(int counter, int tmpCounter) {
		StringBuffer buf = new StringBuffer(counter + "_" + tmpCounter + "_"
				+ EnhancedChangeAssistantMain.EXAMPLE_NUMBER + ".txt");
		return createPath(buf.toString());
	}

	public static String createPath(int counter, int tmpCounter,
			String projectName) {
		StringBuffer buf = new StringBuffer(counter + "_" + tmpCounter + "_"
				+ projectName + "_"
				+ EnhancedChangeAssistantMain.EXAMPLE_NUMBER + ".txt");
		return createPath(buf.toString());
	}

	public static String createPath(String fileName) {
		StringBuffer path = new StringBuffer(
				PropertyLoader.props.getProperty("Project_Home_Path") + "tmp/");
		path.append(fileName);
		return path.toString();
	}
}
