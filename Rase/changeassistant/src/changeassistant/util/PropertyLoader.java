package changeassistant.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class PropertyLoader {

	public static Properties props = new Properties();
	
	public static boolean load(File f){
		try{
			props.load(new FileInputStream(f));
		}catch(Exception e){
			System.out.println("Fail to load config/properties file");
			return false;
		}
		return true;
	}
}
