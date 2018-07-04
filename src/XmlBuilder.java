import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XmlBuilder {
	public static Properties prop = new Properties();

	public static void createPackageXml(File folder, Document document, Element rootElement) {
		for (File subFolder : folder.listFiles()) {
			if (subFolder.isDirectory()) {
				Element types = document.createElement("types");
				Element name = document.createElement("name");
				String typeName = prop.getProperty(subFolder.getName().toLowerCase());
				name.setTextContent(typeName);

				for (File file : subFolder.listFiles()) {
					if (file.isFile()) {
						Element members = document.createElement("members");
						String fileName = file.getName().substring(0, file.getName().indexOf("."));
						String ext = file.getName().substring(file.getName().lastIndexOf("."));
						if (!".xml".equals(ext)) {
							members.setTextContent(fileName);
							types.appendChild(members);
						}
					}
				}
				types.appendChild(name);
				rootElement.appendChild(types);

			}

		}

	}



	public static void main(String[] args) throws Exception {

		File packageFolder = new File("E:\\test\\");

		try {
			InputStream input = Main.class.getResourceAsStream("config.properties");

			prop.load(input);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.newDocument();
		Element rootElement = document.createElement("Package");

		createPackageXml(packageFolder, document, rootElement);

		Element version = document.createElement("version");
		version.setTextContent("36.0");
		rootElement.appendChild(version);
		document.appendChild(rootElement);

		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

			DOMSource source = new DOMSource(document);
			File packageXml = new File(packageFolder.getPath() + "\\package.xml");
			packageXml.createNewFile();
			StreamResult result = new StreamResult(packageXml);
			transformer.transform(source, result);
		} catch (Exception e) {

		}
	}
}
