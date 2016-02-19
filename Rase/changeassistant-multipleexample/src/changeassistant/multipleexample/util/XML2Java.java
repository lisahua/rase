package changeassistant.multipleexample.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import changeassistant.versions.comparison.ChangedMethodADT;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class XML2Java {

	public static List<List<ChangedMethodADT>> readXML(String inputPath,
			Set<String> filePaths) {
		List<List<ChangedMethodADT>> adtLists = new ArrayList<List<ChangedMethodADT>>();
		SAXBuilder builder = new SAXBuilder();
		File file = new File(inputPath);
		String className, methodName;
		List<Element> knownChanges = null;
		List<ChangedMethodADT> adtList = null;
		ChangedMethodADT adt = null;
		try {
			Document doc = builder.build(file);
			Element listElem = doc.getRootElement();
			List<Element> sysChanges = listElem.getChildren();
			for(Element sysChange : sysChanges){
				adtList = new ArrayList<ChangedMethodADT>();
				knownChanges = sysChange.getChildren();
				for(Element knownChange : knownChanges){
					className = knownChange.getChildText("ClassName").trim();
					filePaths.add(knownChange.getChildText("FilePath").trim());
					methodName = knownChange.getChildText("MethodSignature").trim();
					adt = new ChangedMethodADT(className, methodName);
					adtList.add(adt);
				}
				adtLists.add(adtList);
			}
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return adtLists;
	}

	public static List<List<ChangedMethodADT>> readGroupXML(String inputPath, 
			Set<String> filePaths){
		List<List<ChangedMethodADT>> adtLists = new ArrayList<List<ChangedMethodADT>>();
		SAXBuilder builder = new SAXBuilder();
		File file = new File(inputPath);
		String className, methodName;
		List<Element> knownChanges = null;
		List<ChangedMethodADT> adtList = null;
		ChangedMethodADT adt = null;
		try {
			Document doc = builder.build(file);
			Element listElem = doc.getRootElement();
			List<Element> sysChanges = listElem.getChildren();
			for(Element sysChange : sysChanges){
				adtList = new ArrayList<ChangedMethodADT>();
				knownChanges = sysChange.getChildren();
				for(Element knownChange : knownChanges){
					className = knownChange.getChildText("ClassName").trim();
					filePaths.add(knownChange.getChildText("FilePath").trim());
					methodName = knownChange.getChildText("MethodSignature").trim();
					adt = new ChangedMethodADT(className, methodName);
					adtList.add(adt);
				}
				adtLists.add(adtList);
			}
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return adtLists;
	}
}
