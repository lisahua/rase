package changeassistant.multipleexample.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class XMLReader2 {

	public static String SRC = "src";
	public static String OLD_PROJECT_NAME = "oldProjectName";
	public static String NEW_PROJECT_NAME = "newProjectName";
	public static String OLD_CLASS_NAME = "oldClassName";
	public static String NEW_CLASS_NAME = "newClassName";
	public static String OLD_FILE_PATH = "oldFilePath";
	public static String NEW_FILE_PATH = "newFilePath";
	public static String OLD_METHOD_NAME = "oldMethodName";
	public static String NEW_METHOD_NAME = "newMethodName";
	public static String CANDIDATE_PROJECT = "candidateProject";

	public static List<ProjectMethodGroup2> readXML(String xmlPath) {
		List<ProjectMethodGroup2> pmg2 = new ArrayList<ProjectMethodGroup2>();
		String src, oldProjectName, newProjectName, oldClassName, newClassName, oldFilePath, newFilePath, oldMethodName, newMethodName;
		ProjectMethod pm = null;
		List<ProjectMethod> pms = null;
		List<String> projects = null;
		File file = new File(xmlPath);
		SAXBuilder builder = new SAXBuilder();
		try {
			Document doc = builder.build(file);
			Element listElem = doc.getRootElement();
			List<Element> projectGroups = listElem.getChildren();
			for (Element projectGroup : projectGroups) {
				List<Element> projectMethods = projectGroup.getChildren();
				pms = new ArrayList<ProjectMethod>();
				projects = new ArrayList<String>();
				for (Element projectMethod : projectMethods) {
					if (projectMethod.getChildText(SRC) != null) {
						src = projectMethod.getChildText(SRC).trim();
						oldProjectName = projectMethod.getChildText(
								OLD_PROJECT_NAME).trim();
						newProjectName = projectMethod.getChildText(
								NEW_PROJECT_NAME).trim();
						oldClassName = projectMethod.getChildText(
								OLD_CLASS_NAME).trim();
						newClassName = projectMethod.getChildText(
								NEW_CLASS_NAME).trim();
						oldFilePath = projectMethod.getChildText(OLD_FILE_PATH)
								.trim();
						newFilePath = projectMethod.getChildText(NEW_FILE_PATH)
								.trim();
						oldMethodName = projectMethod.getChildText(
								OLD_METHOD_NAME).trim();
						newMethodName = projectMethod.getChildText(
								NEW_METHOD_NAME).trim();
						pm = new ProjectMethod(src, oldProjectName,
								newProjectName, oldClassName, newClassName,
								oldFilePath, newFilePath, oldMethodName,
								newMethodName);
						pms.add(pm);
					} else {
						List<Element> prjs = projectMethod.getChildren();
						for (Element prj : prjs) {
							projects.add(prj.getText());
						}
					}

				}
				pmg2.add(new ProjectMethodGroup2(pms, projects));
			}
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pmg2;
	}
}
