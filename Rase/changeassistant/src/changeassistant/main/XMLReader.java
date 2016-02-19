package changeassistant.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class XMLReader {

	public static List<ProjectMethodPair> readXML(String xmlPath) {
		List<ProjectMethodPair> pmps = new ArrayList<ProjectMethodPair>();
		String src, leftProjectName, rightProjectName, leftClassName1, leftClassName2, rightClassName1, rightClassName2, leftFilePath1, leftFilePath2, rightFilePath1, rightFilePath2, leftMethodName1, leftMethodName2, rightMethodName1, rightMethodName2;
		File file = new File(xmlPath);
		SAXBuilder builder = new SAXBuilder();
		try {
			Document doc = builder.build(file);
			Element listElem = doc.getRootElement();
			List<Element> projectPairs = listElem.getChildren();
			ProjectMethodPair pmp;
			for (Element projectPair : projectPairs) {
				src = projectPair.getChildText("src").trim();
				leftProjectName = projectPair.getChildText("leftProjectName")
						.trim();
				rightProjectName = projectPair.getChildText("rightProjectName")
						.trim();
				leftClassName1 = projectPair.getChildText("leftClassName1")
						.trim();
				leftClassName2 = projectPair.getChildText("leftClassName2")
						.trim();
				rightClassName1 = projectPair.getChildText("rightClassName1")
						.trim();
				rightClassName2 = projectPair.getChildText("rightClassName2")
						.trim();
				leftFilePath1 = projectPair.getChildText("leftFilePath1")
						.trim();
				leftFilePath2 = projectPair.getChildText("leftFilePath2")
						.trim();
				rightFilePath1 = projectPair.getChildText("rightFilePath1")
						.trim();
				rightFilePath2 = projectPair.getChildText("rightFilePath2")
						.trim();
				leftMethodName1 = projectPair.getChildText("leftMethodName1")
						.trim();
				leftMethodName2 = projectPair.getChildText("leftMethodName2")
						.trim();
				rightMethodName1 = projectPair.getChildText("rightMethodName1")
						.trim();
				rightMethodName2 = projectPair.getChildText("rightMethodName2")
						.trim();
				pmp = new ProjectMethodPair(src, leftProjectName,
						rightProjectName, leftClassName1, leftClassName2,
						rightClassName1, rightClassName2, leftFilePath1,
						leftFilePath2, rightFilePath1, rightFilePath2,
						leftMethodName1, leftMethodName2, rightMethodName1,
						rightMethodName2);
				pmp = new ProjectMethodPair(src, leftProjectName,
						rightProjectName, rightClassName1, rightClassName2,
						leftClassName1, leftClassName2, rightFilePath1,
						rightFilePath2, leftFilePath1, leftFilePath2,
						rightMethodName1, rightMethodName2, leftMethodName1,
						leftMethodName2);

				pmps.add(pmp);
			}
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pmps;
	}

	public static List<ProjectMethodGroup> readGroupXML(String xmlPath) {
		List<ProjectMethodGroup> pmgs = new ArrayList<ProjectMethodGroup>();
		String src, leftProjectName, rightProjectName, leftClassName1, leftClassName2, rightClassName1, rightClassName2, leftFilePath1, leftFilePath2, rightFilePath1, rightFilePath2, leftMethodName1, leftMethodName2, rightMethodName1, rightMethodName2;
		File file = new File(xmlPath);
		SAXBuilder builder = new SAXBuilder();
		try {
			Document doc = builder.build(file);
			Element listElem = doc.getRootElement();
			List<Element> projectGroups = listElem.getChildren();
			List<Element> rightMethods;
			ProjectMethod pm;
			ProjectMethodGroup pmg;
			for (Element projectGroup : projectGroups) {
				src = projectGroup.getChildText("src").trim();
				leftProjectName = projectGroup.getChildText("leftProjectName")
						.trim();
				rightProjectName = projectGroup
						.getChildText("rightProjectName").trim();
				leftClassName1 = projectGroup.getChildText("leftClassName1")
						.trim();
				leftClassName2 = projectGroup.getChildText("leftClassName2")
						.trim();
				leftFilePath1 = projectGroup.getChildText("leftFilePath1")
						.trim();
				leftFilePath2 = projectGroup.getChildText("leftFilePath2")
						.trim();
				leftMethodName1 = projectGroup.getChildText("leftMethodName1")
						.trim();
				leftMethodName2 = projectGroup.getChildText("leftMethodName2")
						.trim();
				pmg = new ProjectMethodGroup(src, leftProjectName,
						rightProjectName, leftClassName1, leftClassName2,
						leftFilePath1, leftFilePath2, leftMethodName1,
						leftMethodName2);
				rightMethods = projectGroup.getChild("rightGroup")
						.getChildren();
				for (Element element : rightMethods) {
					rightClassName1 = element.getChildText("className1").trim();
					rightClassName2 = element.getChildText("className2").trim();
					rightFilePath1 = element.getChildText("filePath1").trim();
					rightFilePath2 = element.getChildText("filePath2").trim();
					rightMethodName1 = element.getChildText("methodName1")
							.trim();
					rightMethodName2 = element.getChildText("methodName2")
							.trim();
					pm = new ProjectMethod(rightClassName1, rightClassName2,
							rightFilePath1, rightFilePath2, rightMethodName1,
							rightMethodName2);
					pmg.addProjectMethod(pm);
				}
				pmgs.add(pmg);
			}
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pmgs;
	}
}
