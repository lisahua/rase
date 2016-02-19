package changeassistant.multipleexample.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import changeassistant.main.ProjectMethod;
import changeassistant.main.ProjectMethodGroup;
import changeassistant.main.ProjectMethodPair;

public class XMLReaderWithOracle {

	public static List<ProjectMethodPair> readXML(String xmlPath,
			List<Oracle> oracles) {
		List<ProjectMethodPair> pmps = new ArrayList<ProjectMethodPair>();
		String src, leftProjectName, rightProjectName, leftClassName1, leftClassName2, rightClassName1, 
			rightClassName2, leftFilePath1, leftFilePath2, rightFilePath1, rightFilePath2, 
			leftMethodName1, leftMethodName2, rightMethodName1, rightMethodName2;
		Oracle oracle = null;
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
				oracle = new Oracle();
				oracles.add(oracle);
				setOracle(projectPair, oracle);
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
	
	private static void setOracle(Element projectElem, Oracle oracle){
		String comparison = projectElem.getChildTextTrim("costs");
		if (comparison != null) {
			oracle.put(Constants.COMPARISON, comparison);
		}
		String delta = projectElem.getChildTextTrim("delta");
		if(delta != null)
			oracle.put(Constants.DELTA, delta);
		String extractedMethod = projectElem.getChildTextTrim("extractMethod");					
		if (extractedMethod != null) {
			oracle.put(Constants.EXTRACT_METHOD, extractedMethod);
		}
		String RetObjDecl = projectElem.getChildTextTrim("RetObj");
		if(RetObjDecl != null){
			oracle.put(Constants.RETOBJ, RetObjDecl);
		}
		Element tmpElem = projectElem.getChild("modifiedMethods");
		if (tmpElem != null) {
			List<Element> children = tmpElem.getChildren();
			for (int i = 0; i < children.size(); i++) {
				oracle.put(Integer.valueOf(i), children.get(i)
						.getTextTrim());
			}
		}
	}

	public static List<ProjectMethodGroup> readGroupXML(String xmlPath, List<Oracle> oracles) {
		List<ProjectMethodGroup> pmgs = new ArrayList<ProjectMethodGroup>();
		String src, leftProjectName, rightProjectName, leftClassName1, leftClassName2, rightClassName1, rightClassName2, leftFilePath1, leftFilePath2, rightFilePath1, rightFilePath2, leftMethodName1, leftMethodName2, rightMethodName1, rightMethodName2;
		Oracle oracle = null;
		File file = new File(xmlPath);
		SAXBuilder builder = new SAXBuilder();
		try {
			Document doc = builder.build(file);
			Element listElem = doc.getRootElement();
			List<Element> projectGroups = listElem.getChildren();
			List<Element> rightMethods;
			ProjectMethod pm;
			ProjectMethodGroup pmg;
			for (Element projectElem : projectGroups) {
				src = projectElem.getChildText("src").trim();
				leftProjectName = projectElem.getChildText("leftProjectName")
						.trim();
				rightProjectName = projectElem
						.getChildText("rightProjectName").trim();
				leftClassName1 = projectElem.getChildText("leftClassName1")
						.trim();
				leftClassName2 = projectElem.getChildText("leftClassName2")
						.trim();
				leftFilePath1 = projectElem.getChildText("leftFilePath1")
						.trim();
				leftFilePath2 = projectElem.getChildText("leftFilePath2")
						.trim();
				leftMethodName1 = projectElem.getChildText("leftMethodName1")
						.trim();
				leftMethodName2 = projectElem.getChildText("leftMethodName2")
						.trim();
				pmg = new ProjectMethodGroup(src, leftProjectName,
						rightProjectName, leftClassName1, leftClassName2,
						leftFilePath1, leftFilePath2, leftMethodName1,
						leftMethodName2);
				rightMethods = projectElem.getChild("rightGroup")
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
				oracle = new Oracle();
				oracles.add(oracle);
				setOracle(projectElem, oracle);
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
