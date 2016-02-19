package changeassistant.clonereduction.diff;

import java.io.IOException;

public class RunDiff {

	public String runDiff(String path1, String path2) {
		String result = null;
		String runCommand = "diff " + path1 + " " + path2;
		try {
			Process myProcess = Runtime.getRuntime().exec(runCommand);
			myProcess.waitFor();
			int len = myProcess.getErrorStream().available();
			byte[] buf = null;
			if (len > 0) {
				buf = new byte[len];
				myProcess.getErrorStream().read(buf);
				System.out.println("The Diff cannot execute correctly! "
						+ new String(buf));
			} else {
				len = myProcess.getInputStream().available();
				buf = new byte[len];
				myProcess.getInputStream().read(buf);
				result = new String(buf);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
}
