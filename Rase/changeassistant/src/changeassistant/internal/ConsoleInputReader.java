package changeassistant.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleInputReader {

	InputStreamReader in = null;
	BufferedReader br = null;
	
	public ConsoleInputReader(){
		in = new InputStreamReader(System.in);
		br = new BufferedReader(in);
	}
	
	public String readOracle(){
		try {
			return br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
}
