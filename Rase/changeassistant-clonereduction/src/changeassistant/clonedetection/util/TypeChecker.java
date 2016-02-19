package changeassistant.clonedetection.util;

import java.util.Arrays;
import java.util.List;

public class TypeChecker {

	private static List<String> passByValueTypes;

	static {
		passByValueTypes = Arrays.asList(new String[] { "int", "float",
				"double", "short", "long", "char", "boolean", "Integer",
				"Float", "Double", "Short", "Long", "Character", "Boolean" });
	}

	public static boolean isPassByValueType(String typeName) {
		return passByValueTypes.contains(typeName);
	}
}
