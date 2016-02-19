package changeassistant.multipleexample.main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import changeassistant.multipleexample.match.MatchResult;

public class Oracle {

	public final boolean IGNORE = false;
	public final boolean THROW_EXCEPTION = false;
	private Map<Object, String> map;

	public Oracle() {
		map = new HashMap<Object, String>();
	}
	
	public void checkDelta(String delta) throws Exception{
		if(IGNORE)
			return;
		String del = map.get(Constants.DELTA);
		if(!del.equals(delta)){
			String str = "The delta " + delta + " is different from expectation" + del;			
			if(THROW_EXCEPTION){
				throw new Exception(str);
			}else{
				System.err.println(str);
			}
		}
	}

	public void checkComparison(String comparison) throws Exception {
		String comp = map.get(Constants.COMPARISON);
		if (!comp.equals(comparison)) {
			String str = "The comparison " + comparison
			+ " is different from expectation " + comp;
			if(THROW_EXCEPTION){
				throw new Exception(str);
			}else{
				System.err.println(str);
			}
		}
	}
	
	private void check(String key, String created) throws Exception{
		if (IGNORE)
			return;
		created = created.trim();
		String expected = map.get(key);
		if(expected == null ||!expected.equals(created)){
			System.err.println("Wrong " + key);
			printError(expected, created);
			if(THROW_EXCEPTION){
				throw new Exception(
					"The created string does not match expected string");
			}
		}		
	}
	
	public void checkReturnObject(String retObjString) throws Exception{
		if(IGNORE)
			return;
		check(Constants.RETOBJ, retObjString);		
	}

	public void checkExtractedMethod(String methodString) throws Exception {
		check(Constants.EXTRACT_METHOD, methodString);
	}

	private void printError(String expectedString, String methodString) {
		System.err.println("Expected method: ");
		System.err.println(expectedString);
		System.err.println("Created method: ");
		System.err.println(methodString);
	}

	public void checkModifiedMethod(int index, String methodString)
			throws Exception {
		if (IGNORE)
			return;
		String expectedString = map.get(index);
		methodString = methodString.trim();
		if (expectedString == null || !expectedString.equals(methodString)) {
			System.err.println("Wrongly modified method");
			printError(expectedString, methodString);
			if(THROW_EXCEPTION)
				throw new Exception(
					"The created method does not match the expected method");
		}
	}

	public void checkModifiedMethods(List<MatchResult> mResults)
			throws Exception {
		for (int i = 0; i < mResults.size(); i++) {
			try {
				checkModifiedMethod(i, mResults.get(i).getMethodString());
			} catch (Exception e) {
				String str = Integer.toString(i) + ": " + e.getMessage();
				if(THROW_EXCEPTION){
					throw new Exception(str);
				}else{
					System.err.println(str);
				}				
			}
		}
	}

	public void put(Object key, String value) {
		map.put(key, value);
	}
}
