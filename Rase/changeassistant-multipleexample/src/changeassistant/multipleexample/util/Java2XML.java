package changeassistant.multipleexample.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import changeassistant.classhierarchy.ProjectResource;
import changeassistant.multipleexample.partition.datastructure.EditInCommonCluster;
import changeassistant.multipleexample.partition.datastructure.EditInCommonGroup;
import changeassistant.versions.comparison.ChangedMethodADT;
import changeassistant.versions.comparison.MethodModification;

public class Java2XML {
	
	public static void writeGroupXML(String outputPath, ProjectResource prLeft, 
			List<EditInCommonGroup> groups)
		throws FileNotFoundException, IOException{
		int idCounter = 1;
		//create root node list
		Element root = new Element("list");
		// root node is added to the document
		Document Doc = new Document(root);

		MethodModification mm = null;
		ChangedMethodADT adt;
		
		Element className, knownChange, methodSig, filePath;
		Set<Integer> knownInstances = new HashSet<Integer>();
		List<EditInCommonCluster> clusters = null;
		List<MethodModification> mms = null;
		List<Integer> instances = null;
		for(EditInCommonGroup group : groups){
			clusters = group.getClusters();
			mms = group.getMMList();
			for(EditInCommonCluster cluster : clusters){
				Element element = new Element("SystematicChange");
				element.setAttribute("id", "" + idCounter++);
				instances = cluster.getInstances();
				for(Integer instance : instances){
					if(knownInstances.contains(instance))
						continue;
					knownInstances.add(instance);
					mm = mms.get(instance);
					knownChange = new Element("KnownChange");
					adt = mm.originalMethod;
					
						className = new Element("ClassName");
						className.setText(adt.classname);
						knownChange.addContent(className);
					
						filePath = new Element("FilePath");
						filePath.setText(prLeft.findClassContext(adt.classname).relativeFilePath);
						knownChange.addContent(filePath);
				
						methodSig = new Element("MethodSignature");
						methodSig.setText(adt.methodSignature);
						knownChange.addContent(methodSig);
				
					element.addContent(knownChange);
				}
				root.addContent(element);
			}
		}
		
		XMLOutputter XMLOut = new XMLOutputter();
		XMLOut.setFormat(Format.getPrettyFormat());

		// output .xml file
		XMLOut.output(Doc, new FileOutputStream(outputPath));
	}
}
