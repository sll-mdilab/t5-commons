package net.sllmdilab.commons.util;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sllmdilab.commons.exceptions.XmlParsingException;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ParserUtils {

	public static Document parseXmlString(String response) {
		try {
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return documentBuilder.parse(new InputSource(new StringReader(response)));
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new XmlParsingException(e);
		}
	}

	public static String requireAttribute(Element element, String attributeName) {
		String attributeValue = element.getAttribute(attributeName);

		if (StringUtils.isBlank(attributeValue)) {
			throw new XmlParsingException("Missing attribute " + attributeName);
		}

		return attributeValue;
	}
	
	public static String requireContent(Element element) {
		String content = element.getTextContent();

		if (StringUtils.isBlank(content)) {
			throw new XmlParsingException("Missing element content");
		}

		return content;
	}
}
