package changeassistant.multipleexample.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtil {

	public static String createTmpFile(String content, String fileName) {
		String path = PathUtil.createPath(fileName);
		File f = new File(path);
		BufferedWriter output = null;
		try {
			output = new BufferedWriter(new FileWriter(f));
			output.write(content);
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return path;
	}
}
